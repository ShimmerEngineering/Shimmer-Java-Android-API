package com.shimmerresearch.verisense.communication.payloads;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.sensors.SensorVerisenseClock;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.UtilParseData;

/**
 * @author Mark Nolan
 *
 */
public class StatusPayload extends AbstractPayload implements Serializable{
	
	public String verisenseId;
	public double batteryLevelMillivolts = 0.0;
	public double batteryPercentage = 0.0;
	
	public double verisenseStatusTimestampMs = 0.0;
	public double lastTransferSuccessTimestampMs = 0.0;
	public double lastTransferFailTimestampMs = 0.0;
	public long baseStationTimestampMs = 0;
	public double nextSyncAttemptTimestampMs = 0.0;
	
	public long batteryVoltageFallCounter = 0;
	
	public long statusFlags = -1;
	public long statusFlagsReversed;
	public boolean usbPowered;
	public boolean recordingPaused;
	public boolean flashIsFull;
	public boolean powerIsGood;
	public boolean adaptiveSchedulerEnabled;
	public boolean dfuServiceOn;
	public boolean statusFlagFirstBoot;
	public boolean secondaryStatusMsg;
	
	public int syncType = -1;
	
	public int storageFreekB = -1;
	public int storageFullkB = -1;
	public int storageToDelkB = -1;
	public int storageBadkB = -1;
	public int storageOtherkB = -1;
	public int storageCapacitykB = -1;

	public long failedBleConnectionAttemptCount = 0;
	public int flashWriteFailCounter;
	public long timestampNextSyncAttempt;
	public int flashWriteRetryCounterShortTry = -1;
	public int flashWriteRetryCounterLongTry = -1;

	public boolean isChargerChipPresent = false;
	public int batteryChargerStatusValue = -1;
	public Object batteryChargerStatus = null;

	public static final double MAX_FOUR_BTE_UNSIGNED_VALUE = Math.pow(2, 32) - 1;
	
	private int hwVerMajor = -1;
	private int hwVerMinor = -1;
	private int hwVerInternal = -1;
	
	public enum SyncType {
		READ_AND_CLEAR_PENDING_EVENTS ("PendingEvent"),
		FORCE_DATA_TRANSFER_SYNC ("Forced"),
		UNPAIR ("Unpair"),
		PAIR ("Pair");
		
		String name = "";
		
		SyncType(String name){
			this.name = name;
		}
		
		public String getName(){
			return name;
		}
	}

	public class STATUS_FLAGS {
		/* Byte 0 */
		public static final int BIT_MASK_USB_PLUGGED_IN = (1 << 0);
		public static final int BIT_MASK_RECORDING_PAUSED = (1 << 1);
		public static final int BIT_MASK_FLASH_IS_FULL = (1 << 2);
		public static final int BIT_MASK_POWER_IS_GOOD = (1 << 3);
		public static final int BIT_MASK_ADAPTIVE_SCHEDULER_ON = (1 << 4);
		public static final int BIT_MASK_DFU_SERVICE_ON = (1 << 5);
		public static final int BIT_MASK_FIRST_BOOT = (1 << 6);
		public static final int BIT_MASK_SECONDARY_STATUS = (1 << 7);

		public static final int BIT_SHIFT_FLASH_WRITE_RETRY_COUNTER_SHORT_TRY_LSB = (8 * 1);
		public static final int BIT_SHIFT_FLASH_WRITE_RETRY_COUNTER_SHORT_TRY_MSB = (8 * 2);
		public static final int BIT_SHIFT_FLASH_WRITE_RETRY_COUNTER_LONG_TRY_LSB = (8 * 3);
		public static final int BIT_SHIFT_FLASH_WRITE_RETRY_COUNTER_LONG_TRY_MSB = (8 * 4);
		public static final int BIT_SHIFT_FAIL_COUNT_FLASH_WRITE_LSB = (8 * 5);
		public static final int BIT_SHIFT_FAIL_COUNT_FLASH_WRITE_MSB = (8 * 6);
		public static final int BIT_SHIFT_FAIL_COUNT_BLE_SYNC = (8 * 7);
	}

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		isSuccess = false;

		verisenseId = parseVerisenseId(payloadContents, 0);

		long statusTimestampMinutes = parseByteArrayAtIndex(payloadContents, 6, CHANNEL_DATA_TYPE.UINT32);
		long statusTimestampTicks = 0;
		if (payloadContents.length > 34) { // supported fw for ASM-1329
			statusTimestampTicks = parseByteArrayAtIndex(payloadContents, 34, CHANNEL_DATA_TYPE.UINT24);
		}
		verisenseStatusTimestampMs = SensorVerisenseClock.convertRtcMinutesAndTicksToMs(statusTimestampMinutes, statusTimestampTicks);
		
		batteryLevelMillivolts = parseByteArrayAtIndex(payloadContents, 10, CHANNEL_DATA_TYPE.UINT16);
		batteryPercentage = payloadContents[12];
		
		long lastSuccessfulDataTransferMinutes = parseByteArrayAtIndex(payloadContents, 13, CHANNEL_DATA_TYPE.UINT32);
		long lastSuccessfulDataTransferTicks = 0;
		long lastFailedDataTransferMinutes = parseByteArrayAtIndex(payloadContents, 17, CHANNEL_DATA_TYPE.UINT32);
		long lastFailedDataTransferTicks = 0;
		if (payloadContents.length > 34) { // supported fw for ASM-1329
			lastSuccessfulDataTransferTicks = parseByteArrayAtIndex(payloadContents, 37, CHANNEL_DATA_TYPE.UINT24);
			lastFailedDataTransferTicks = parseByteArrayAtIndex(payloadContents, 40, CHANNEL_DATA_TYPE.UINT24);
		}
		// special condition where the sensor/fw returns all FF values
		lastTransferSuccessTimestampMs = (lastSuccessfulDataTransferMinutes == MAX_FOUR_BTE_UNSIGNED_VALUE)? -1 : SensorVerisenseClock.convertRtcMinutesAndTicksToMs(lastSuccessfulDataTransferMinutes, lastSuccessfulDataTransferTicks);
		lastTransferFailTimestampMs = (lastFailedDataTransferMinutes == MAX_FOUR_BTE_UNSIGNED_VALUE)? -1 : SensorVerisenseClock.convertRtcMinutesAndTicksToMs(lastFailedDataTransferMinutes, lastFailedDataTransferTicks);

		if (isStatusFromFwV1_02_102_onwards()) {
			storageCapacitykB = (int) parseByteArrayAtIndex(payloadContents, 60, CHANNEL_DATA_TYPE.UINT32);
		} else {
			updateStorageCapacitykBBasedOnHw();
		}

		if (isStatusFromFwV1_02_102_onwards()) {
			storageFreekB = parseStorageValueSplitFwV1_02_102(payloadContents, 21, 57);
		} else {
			storageFreekB = (int) parseByteArrayAtIndex(payloadContents, 21, CHANNEL_DATA_TYPE.UINT24);
		}
		
		if (isStatusFromFwV1_02_055_onwards()) {
			if (isStatusFromFwV1_02_102_onwards()) {
				storageFullkB = parseStorageValueSplitFwV1_02_102(payloadContents, 47, 58);
				storageToDelkB = parseStorageValueSplitFwV1_02_102(payloadContents, 50, 59);
				storageBadkB = (int) parseByteArrayAtIndex(payloadContents, 53, CHANNEL_DATA_TYPE.UINT32);
			} else {
				storageFullkB = (int) parseByteArrayAtIndex(payloadContents, 47, CHANNEL_DATA_TYPE.UINT24);
				storageToDelkB = (int) parseByteArrayAtIndex(payloadContents, 50, CHANNEL_DATA_TYPE.UINT24);
				storageBadkB = (int) parseByteArrayAtIndex(payloadContents, 53, CHANNEL_DATA_TYPE.UINT24);
			}
			calculateStorageOther();
		}
		
		if (payloadContents.length <= 24) { // old fw, no VBattFallCounter bytes
			batteryVoltageFallCounter = -1; // set to null because 0 can be a valid value
		} else {
			batteryVoltageFallCounter = parseByteArrayAtIndex(payloadContents, 24, CHANNEL_DATA_TYPE.UINT16);
		}

		if (payloadContents.length > 26) { // new fw support StatusFlags bytes
			statusFlags = parseByteArrayAtIndex(payloadContents, 26, CHANNEL_DATA_TYPE.UINT64);

			byte[] statusFlagBytes = new byte[8];
			System.arraycopy(payloadContents, 26, statusFlagBytes, 0, statusFlagBytes.length);
			ArrayUtils.reverse(statusFlagBytes);
			// eg 0000000000000009 where 09 is the LSB (byte 26) will result in a
			// StatusFlags value of 9
			statusFlagsReversed = Long.parseLong(Hex.toHexString(statusFlagBytes).replace("-", ""), 16);
			// reverse so the value of 9 00001001 will be 10010000 which is easier to read
			// via index/table provided in the document ASM-DES04
			
			parseStatusFlagBytes(statusFlags);
		}

		if (payloadContents.length > 34) { // supported fw for ASM-1329
			long nextSyncAttemptTimestampMinutes = parseByteArrayAtIndex(payloadContents, 43, CHANNEL_DATA_TYPE.UINT32);
			nextSyncAttemptTimestampMs = nextSyncAttemptTimestampMinutes == MAX_FOUR_BTE_UNSIGNED_VALUE ? -1 : SensorVerisenseClock.convertRtcMinutesAndTicksToMs(nextSyncAttemptTimestampMinutes, 0);
		}
		
		if (isStatusFromFwV1_02_055_onwards()) {
			timestampNextSyncAttempt = parseByteArrayAtIndex(payloadContents, 43, CHANNEL_DATA_TYPE.UINT32);
		}

		if (isStatusFromFwV1_02_102_onwards()) {
			isChargerChipPresent = ((payloadContents[64] & 0x01) > 0) ? true : false;
			batteryChargerStatusValue = (payloadContents[64] >> 1) & 0x03;
			parseBatteryChargerStatusValue();
		}

		// SyncMode = (int)syncMode;
		// BaseStationTimestamp = DateHelper.GetTimestamp(DateTime.Now);

		isSuccess = true;
		return isSuccess;
	}
	
	public void parseStatusFlagBytes(long statusFlags) {
		if(isStatusFlagValid()) {
			usbPowered = ((statusFlags & STATUS_FLAGS.BIT_MASK_USB_PLUGGED_IN) > 0) ? true : false;
			recordingPaused = ((statusFlags & STATUS_FLAGS.BIT_MASK_RECORDING_PAUSED) > 0) ? true : false;
			flashIsFull = ((statusFlags & STATUS_FLAGS.BIT_MASK_FLASH_IS_FULL) > 0) ? true : false;
			powerIsGood = ((statusFlags & STATUS_FLAGS.BIT_MASK_POWER_IS_GOOD) > 0) ? true : false;
			adaptiveSchedulerEnabled = ((statusFlags & STATUS_FLAGS.BIT_MASK_ADAPTIVE_SCHEDULER_ON) > 0) ? true : false;
			dfuServiceOn = ((statusFlags & STATUS_FLAGS.BIT_MASK_DFU_SERVICE_ON) > 0) ? true : false;
			statusFlagFirstBoot = ((statusFlags & STATUS_FLAGS.BIT_MASK_FIRST_BOOT) > 0) ? true : false;
			
			// FW v1.02.124 & FW v1.04.000 onwards (not the versions in between)
			secondaryStatusMsg = ((statusFlags & STATUS_FLAGS.BIT_MASK_SECONDARY_STATUS) > 0) ? true : false;

			// For a number of previous FW versions, the timestamp in ticks was stored in
			// byte 5, 6 and 7. A better approach is to know what version of FW it is so it
			// can be parsed correctly but that information isn't available at this point in
			// the code.

			// FW v1.02.123 onwards
			flashWriteRetryCounterShortTry = (int) ((statusFlags >> STATUS_FLAGS.BIT_SHIFT_FLASH_WRITE_RETRY_COUNTER_SHORT_TRY_LSB) & 0xFFFF);
			flashWriteRetryCounterLongTry = (int) ((statusFlags >> STATUS_FLAGS.BIT_SHIFT_FLASH_WRITE_RETRY_COUNTER_LONG_TRY_LSB) & 0xFFFF);

			// FW v1.02.084 onwards
			flashWriteFailCounter = (int) ((statusFlags >> STATUS_FLAGS.BIT_SHIFT_FAIL_COUNT_FLASH_WRITE_LSB) & 0xFFFF);

			// FW v1.02.063 onwards
			failedBleConnectionAttemptCount = (int) ((statusFlags >> STATUS_FLAGS.BIT_SHIFT_FAIL_COUNT_BLE_SYNC) & 0xFF);
		}
	}

	public void parseBatteryChargerStatusValue() {
		if(hwVerMajor!=-1 && VerisenseDevice.isChargerLm3658dPresent(hwVerMajor, hwVerMinor, hwVerInternal)) {
			batteryChargerStatus = VerisenseDevice.CHARGER_STATUS_LM3658D.values()[batteryChargerStatusValue];
		} else if(hwVerMajor!=-1 && VerisenseDevice.isChargerLm3658dPresent(hwVerMajor, hwVerMinor, hwVerInternal)) {
			batteryChargerStatus = VerisenseDevice.CHARGER_STATUS_LTC4123.values()[batteryChargerStatusValue];
		} else {
			batteryChargerStatus = null;
		}
	}

	@Override
	public byte[] generatePayloadContents() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void calculateStorageOther() {
		storageOtherkB = storageCapacitykB - (storageFreekB + storageFullkB + storageToDelkB + storageBadkB);
	}

	public double getStorageFreePercent() {
		double memoryFreePercent = (storageFreekB*100.0/storageCapacitykB);
		return memoryFreePercent;
	}

	public double getStorageFullPercent() {
		double memoryFullPercent = (storageFullkB*100.0/storageCapacitykB);
		return memoryFullPercent;
	}

	public double getStorageToDeletePercent() {
		double memory2DelPercent = (storageToDelkB*100.0/storageCapacitykB);
		return memory2DelPercent;
	}
	
	public double getStorageBadPercent() {
		double memoryBadPercent = (storageBadkB*100.0/storageCapacitykB);
		return memoryBadPercent;
	}

	public double getStorageOtherPercent() {
		double memoryOtherPercent = (storageOtherkB * 100.0 / storageCapacitykB);
		return memoryOtherPercent;
	}

	public boolean isStatusFlagValid() {
		return statusFlags!=-1;
	}
	
	public boolean isStatusFlagRecordingPaused() {
		return recordingPaused;
	}

	public boolean isStatusFlagFirstBoot() {
		return statusFlagFirstBoot;
	}
	
	public boolean isStatusFlagSecondaryStatusMsg() {
		return secondaryStatusMsg;
	}

	@Override
	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ASM Status:\n");
		sb.append("\tASM Identifier:\t\t\t" + verisenseId + "\n");
		sb.append("\tStatus Timestamp:\t\t"+ UtilShimmer.convertMilliSecondsToDateString((long) verisenseStatusTimestampMs, false) + "\n");
		sb.append("\tBattery Level:\t\t\t" + batteryLevelMillivolts + "mV\n");
		sb.append("\tBattery Percentage:\t\t" + batteryPercentage + "%\n");
		sb.append("\tLast Ok Data Transfer:\t\t" + (lastTransferSuccessTimestampMs==-1? "Not valid":UtilShimmer.convertMilliSecondsToDateString((long) lastTransferSuccessTimestampMs, false)) + "\n");
		sb.append("\tLast Fail Data Transfer:\t" + (lastTransferFailTimestampMs==-1? "Not valid":UtilShimmer.convertMilliSecondsToDateString((long) lastTransferFailTimestampMs, false)) + "\n");
		
		int memoryUsedkB = storageCapacitykB-storageFreekB;

		sb.append("\tMemory free:\t\t\t" + storageFreekB + " kBytes / " + storageCapacitykB + " kBytes => " + UtilShimmer.formatDoubleToNdecimalPlaces(getStorageFreePercent(), 2)  +" %\n");
		sb.append("\tMemory Used:\t\t\t" + memoryUsedkB + " kBytes\n");
		if (payloadContents.length >= 38) {
			sb.append("\t\tFULL Banks:\t\t" + storageFullkB + " kBytes / " + storageCapacitykB + " kBytes => " + UtilShimmer.formatDoubleToNdecimalPlaces(getStorageFullPercent(), 2)  +" %\n");
			sb.append("\t\t2DEL Banks:\t\t" + storageToDelkB + " kBytes / " + storageCapacitykB + " kBytes => " + UtilShimmer.formatDoubleToNdecimalPlaces(getStorageToDeletePercent(), 2)  +" %\n");
			sb.append("\t\tBAD Banks:\t\t" + storageBadkB + " kBytes / " + storageCapacitykB + " kBytes => " + UtilShimmer.formatDoubleToNdecimalPlaces(getStorageBadPercent(), 2)  +" %\n");
			
			//TODO
			// Update the memory used for the progress bar to be based on 'FULL' banks only instead;
			memoryUsedkB = storageFullkB;
			
			sb.append("\t\tOther:\t\t\t" + storageOtherkB + " kBytes / " + storageCapacitykB + " kBytes => " + UtilShimmer.formatDoubleToNdecimalPlaces(getStorageOtherPercent(), 2)  +" %\n");
		}

		// Parse the battery fall counter info
		if (payloadContents.length >= 26) {
			sb.append("\tVBatt Fall Counter:\t\t" + batteryVoltageFallCounter + "\n");
		}

		// Parse the Status Message Flags - 64bit Value Reserved
		if (payloadContents.length >= 34) {
			sb.append("\tStatus Message Flags:\n");

			// Byte 0 - Status flags
			sb.append("\t\tUSB_PLUGGED_IN:\t\t\t" + usbPowered + "\n");
			sb.append("\t\tRECORDING_PAUSED:\t\t" + recordingPaused + "\n");
			sb.append("\t\tFLASH_IS_FULL:\t\t\t" + flashIsFull + "\n");
			sb.append("\t\tPOWER_IS_GOOD:\t\t\t" + powerIsGood + "\n");
			sb.append("\t\tADAPTIVE_SCHEDULER_ON:\t\t" + adaptiveSchedulerEnabled + "\n");
			sb.append("\t\tDFU_SERVICE_ON:\t\t\t" + dfuServiceOn + "\n");
			sb.append("\t\tFIRST BOOT ON:\t\t\t" + statusFlagFirstBoot + "\n");
			sb.append("\t\tSecondary Status:\t\t" + secondaryStatusMsg + "\n");

			// Bytes 1-4 - Pause counters
			sb.append("\t\tLTF WRITE - Short Retry Counter (2 minutes gap, 10 retries):\t" + flashWriteRetryCounterShortTry + "\n");
			sb.append("\t\tLTF WRITE - Long Retry Counter (4hrs gap, 1 retry):\t\t" + flashWriteRetryCounterLongTry + "\n");

			// Bytes 5-6 - LTF write fail counter
			sb.append("\t\tLTF WRITE FAIL COUNTER:\t\t" + failedBleConnectionAttemptCount + "\n");

			// Byte 7 - Sync fail counter
			sb.append("\t\tFAIL SYNC COUNTER:\t\t" + flashWriteFailCounter + "\n");
		}

		// Parse the Next Sync Attempt time
		if (isStatusFromFwV1_02_055_onwards()) {
			sb.append("\tNext Sync Attempt:\t\t" + (timestampNextSyncAttempt==0? "Not Set":("Will Happen At:" + timestampNextSyncAttempt)) + "\n");
		}
				
		return sb.toString();
	}

	public void setShimmerHwVer(int hwVerMajor, int hwVerMinor, int hwVerInternal) {
		this.hwVerMajor = hwVerMajor;
		this.hwVerMinor = hwVerMinor;
		this.hwVerInternal = hwVerInternal;
	}

	public boolean isStatusFromFwV1_02_055_onwards() {
		return payloadContents.length >= 56;
	}

	public boolean isStatusFromFwV1_02_102_onwards() {
		return payloadContents.length >= 64;
	}
	
	private int parseStorageValueSplitFwV1_02_102(byte[] payloadContents, int idxOf3LSB, int idxOfMSB) {
		byte[] buf = new byte[3];
		System.arraycopy(payloadContents, idxOf3LSB, buf, 0, 3);
		System.arraycopy(payloadContents, idxOfMSB, buf, 3, 1);
		return (int) UtilParseData.parseData(buf, CHANNEL_DATA_TYPE.UINT32, CHANNEL_DATA_ENDIAN.LSB);
	}

	public void updateStorageCapacitykBBasedOnHw() {
		if(hwVerMajor!=-1) {
			updateStorageCapacitykBBasedOnHw(hwVerMajor, hwVerMinor, hwVerInternal);
		} else {
			updateStorageCapacitykBBasedOnHw(HW_ID.VERISENSE_IMU, 1, 0);
		}
	}

	public void updateStorageCapacitykBBasedOnHw(int hwVerMajor, int hwVerMinor, int hwVerInternal) {
		if (hwVerMajor == HW_ID.VERISENSE_PULSE_PLUS && hwVerMinor == 8) {
			storageCapacitykB = 128 * 1024; // LTF is 128MB
		} else {
			storageCapacitykB = 512 * 1024; // LTF is 512MB
		}
	}

	public String getChargerChipStatusDescriptionShort(){
		String batteryChargerStatusStr = UtilVerisenseDriver.FEATURE_NOT_AVAILABLE;
		if(isChargerChipPresent && batteryChargerStatus!=null) {
			if(batteryChargerStatus instanceof VerisenseDevice.CHARGER_STATUS_LTC4123) {
				batteryChargerStatusStr = ((VerisenseDevice.CHARGER_STATUS_LTC4123)batteryChargerStatus).descriptionShort;
			}
			else if(batteryChargerStatus instanceof VerisenseDevice.CHARGER_STATUS_LM3658D) {
				batteryChargerStatusStr = ((VerisenseDevice.CHARGER_STATUS_LM3658D)batteryChargerStatus).descriptionShort;
			}
		}
		return batteryChargerStatusStr;
	}
	
}
