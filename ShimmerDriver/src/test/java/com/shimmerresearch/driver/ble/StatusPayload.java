package com.shimmerresearch.driver.ble;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.driverUtilities.UtilShimmer;

public class StatusPayload {
	String Payload;
	String Header;
	int Length;
	public String ASMID;
	public long StatusTimestamp;
	public int BatteryLevel;
	public int BatteryPercent;
	public long TransferSuccessTimestamp;
	public long TransferFailTimestamp;
	public long BaseStationTimestamp;
	public int FreeStorage;
	public boolean IsSuccess;
	public long VBattFallCounter;
	public long StatusFlags;
	public boolean UsbPowered;
	public boolean RecordingPaused;
	public boolean FlashIsFull;
	public boolean PowerIsGood;
	public boolean AdaptiveScheduler;
	public boolean DfuServiceOn;
	public int SyncMode;
	public long NextSyncAttemptTimestamp;
	public int StorageFull;
	public int StorageToDel;
	public int StorageBad;
	public final static long MaxFourByteUnsignedValue = 4294967295l; // 2^32 -1
	public final static int SensorClockFrequency = 32768;

	private long ConvertMinuteToMS(long timestamp) {
		if (timestamp != MaxFourByteUnsignedValue) { // special condition where the sensor/fw returns all FF values
			timestamp = timestamp * 60 * 1000; // convert from minutes to milliseconds
		} else {
			timestamp = -1;
		}
		return timestamp;
	}

	private String reverse(String s) {
		char[] charArray = s.toCharArray();
		ArrayUtils.reverse(charArray);
		return new String(charArray);
	}

	private long ConvertTicksTomS(long ticks) {
		return (long) ((ticks / (double) SensorClockFrequency) * 1000.0);
	}

	private long AppendToCurrentTimestamp(long timestamp, double durationToAppend) {
		return (long) (timestamp + durationToAppend);
	}

	public boolean ProcessPayload(byte[] response) {
		//TODO it very inefficient to be converting a byte are to a String and then converting it back into ints and longs. We have methods in UtilShimmer to convert directly from a byte array to int/longs
		try {
			Payload = Hex.toHexString(response);
			ByteArrayInputStream input = new ByteArrayInputStream(response);
			InputStreamShimmer reader = new InputStreamShimmer(input);
			Header = Hex.toHexString(reader.readBytes(1));

			byte[] lengthBytes = reader.readBytes(2);
			ArrayUtils.reverse(lengthBytes);

			Length = Integer.parseInt(Hex.toHexString(lengthBytes).replace("-", ""), 16);

			byte[] idBytes = reader.readBytes(6);
			ArrayUtils.reverse(idBytes);
			ASMID = Hex.toHexString(idBytes).replace("-", "");

			byte[] tsBytes = reader.readBytes(4);
			ArrayUtils.reverse(tsBytes);
			StatusTimestamp = Long.parseLong(Hex.toHexString(tsBytes).replace("-", ""), 16);
			StatusTimestamp = ConvertMinuteToMS(StatusTimestamp);
			byte[] batteryBytes = reader.readBytes(2);
			ArrayUtils.reverse(batteryBytes);
			BatteryLevel = Integer.parseInt(Hex.toHexString(batteryBytes).replace("-", ""), 16);

			BatteryPercent = Integer.parseInt(Hex.toHexString(reader.readBytes(1)).replace("-", ""), 16);

			byte[] successBytes = reader.readBytes(4);
			ArrayUtils.reverse(successBytes);
			TransferSuccessTimestamp = Long.parseLong(Hex.toHexString(successBytes).replace("-", ""), 16);
			TransferSuccessTimestamp = ConvertMinuteToMS(TransferSuccessTimestamp);
			byte[] failBytes = reader.readBytes(4);
			ArrayUtils.reverse(failBytes);
			TransferFailTimestamp = Long.parseLong(Hex.toHexString(failBytes).replace("-", ""), 16);
			TransferFailTimestamp = ConvertMinuteToMS(TransferFailTimestamp);

			byte[] storageBytes = reader.readBytes(3);
			ArrayUtils.reverse(storageBytes);
			FreeStorage = Integer.parseInt(Hex.toHexString(storageBytes).replace("-", ""), 16);
			/*
			 * I am moving this to the UI level, because this values might be meaningful in
			 * the web DB if (FreeStorage > App.MaxSensorStorageCapacityKB) { FreeStorage =
			 * App.MaxSensorStorageCapacityKB; }
			 */
			if (Length <= 24) // old fw, no VBattFallCounter bytes
			{
				VBattFallCounter = -1; // set to null because 0 can be a valid value
			} else {
				byte[] battFallBytes = reader.readBytes(2);
				ArrayUtils.reverse(battFallBytes);
				VBattFallCounter = Long.parseLong(Hex.toHexString(battFallBytes).replace("-", ""), 16);
			}

			if (Length > 26) // new fw support StatusFlags bytes
			{

				byte[] statusFlagsBytes = reader.readBytes(8);
				ArrayUtils.reverse(statusFlagsBytes);
				// eg 0000000000000009 where 09 is the LSB (byte 26) will result in a
				// StatusFlags value of 9
				StatusFlags = Long.parseLong(Hex.toHexString(statusFlagsBytes).replace("-", ""), 16);
				// reverse so the value of 9 00001001 will be 10010000 which is easier to read
				// via index/table provided in the document ASM-DES04
				UsbPowered = ((statusFlagsBytes[7] & 1) > 0) ? true : false;
				RecordingPaused = ((statusFlagsBytes[7] & 0b10) > 0) ? true : false;
				FlashIsFull = ((statusFlagsBytes[7] & 0b100) > 0) ? true : false;
				PowerIsGood = ((statusFlagsBytes[7] & 0b1000) > 0) ? true : false;
				AdaptiveScheduler = ((statusFlagsBytes[7] & 0b10000) > 0) ? true : false;
				DfuServiceOn = ((statusFlagsBytes[7] & 0b100000) > 0) ? true : false;
			}

			if (Length > 34) // supported fw for ASM-1329
			{
				byte[] statusTimestampTicksBytes = reader.readBytes(3);
				ArrayUtils.reverse(statusTimestampTicksBytes);
				long statusTimestampTicks = Long.parseLong(Hex.toHexString(statusTimestampTicksBytes).replace("-", ""), 16);
				StatusTimestamp = AppendToCurrentTimestamp(StatusTimestamp, ConvertTicksTomS(statusTimestampTicks));

				byte[] transferSuccessTimestampTicksBytes = reader.readBytes(3);
				if (TransferSuccessTimestamp != -1) {
					ArrayUtils.reverse(transferSuccessTimestampTicksBytes);
					long transferSuccessTimestampTicks = Long.parseLong(Hex.toHexString(transferSuccessTimestampTicksBytes).replace("-", ""), 16);
					TransferSuccessTimestamp = AppendToCurrentTimestamp(TransferSuccessTimestamp, ConvertTicksTomS(transferSuccessTimestampTicks));
				}

				byte[] transferFailTimestampTicksBytes = reader.readBytes(3);
				if (TransferFailTimestamp != -1) {
					ArrayUtils.reverse(transferFailTimestampTicksBytes);
					long transferFailTimestampTicks = Long.parseLong(Hex.toHexString(transferFailTimestampTicksBytes).replace("-", ""), 16);
					TransferFailTimestamp = AppendToCurrentTimestamp(TransferFailTimestamp, ConvertTicksTomS(transferFailTimestampTicks));
				}

				byte[] nextSyncAttemptTimeBytes = reader.readBytes(4);
				ArrayUtils.reverse(nextSyncAttemptTimeBytes);
				NextSyncAttemptTimestamp = Long.parseLong(Hex.toHexString(nextSyncAttemptTimeBytes).replace("-", ""), 16);
				NextSyncAttemptTimestamp = ConvertMinuteToMS((long) NextSyncAttemptTimestamp);

				byte[] storageFullBytes = reader.readBytes(3);
				ArrayUtils.reverse(storageFullBytes);
				StorageFull = Integer.parseInt(Hex.toHexString(storageFullBytes).replace("-", ""), 16);
				/*
				 * I am moving this to the UI level, because this values might be meaningful in
				 * the web DB if (StorageFull > App.MaxSensorStorageCapacityKB) { StorageFull =
				 * App.MaxSensorStorageCapacityKB; }
				 */
				byte[] storageToDelBytes = reader.readBytes(3);
				ArrayUtils.reverse(storageToDelBytes);
				StorageToDel = Integer.parseInt(Hex.toHexString(storageToDelBytes).replace("-", ""), 16);

				byte[] storageBadBytes = reader.readBytes(3);
				ArrayUtils.reverse(storageBadBytes);
				StorageBad = Integer.parseInt(Hex.toHexString(storageBadBytes).replace("-", ""), 16);
			}

			// SyncMode = (int)syncMode;
			// BaseStationTimestamp = DateHelper.GetTimestamp(DateTime.Now);

			IsSuccess = true;
		} catch (Exception ex) {
			// System.Console.WriteLine(ex.ToString());
		}

		return IsSuccess;
	}
	
	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ASM Status:");
		sb.append("\tASM Identifier:\t\t\t" + ASMID);
//		sb.append("\tStatus Timestamp:\t\t{}, {}".format(status_timestamp, byte_array_to_hex_string(status_timestamp_array)))
//		sb.append("\tBattery Level:\t\t\t{}mV {}".format(asm_batt_level, byte_array_to_hex_string(asm_batt_level_array)))
//		sb.append(("\tBattery Percentage:\t\t%d%% " % asm_batt_percentage) + byte_array_to_hex_string(asm_batt_percentage_array))
//		sb.append("\tLast Ok Data Transfer:\t{} {}".format(last_ok_data_transfer_str, byte_array_to_hex_string(last_ok_data_transfer_array)))
//		sb.append("\tLast Fail Data Transfer:\t{} {}".format(last_fail_data_transfer_str, byte_array_to_hex_string(last_fail_data_transfer_array)))
//		sb.append("\tMemory free:\t\t\t%d kBytes / %d kBytes => %.2f%% " % (memory_free_kB, memory_LTF_capacity_kB, memory_free_percent) + byte_array_to_hex_string(memory_free_array))
//		sb.append("\tMemory Used:\t\t\t%d kBytes" % memory_used)
//		if len(response) >= 38:
//			sb.append("\t\tFULL Banks:\t\t\t%d kBytes / %d kBytes => %.2f%% " % (full_banks_memory_kB, memory_LTF_capacity_kB, full_banks_memory_percent) + byte_array_to_hex_string(full_banks_memory_array))
//			sb.append("\t\t2DEL Banks:\t\t\t%d kBytes / %d kBytes => %.2f%% " % (toBeDel_banks_memory_kB, memory_LTF_capacity_kB, toBeDel_banks_memory_percent) + byte_array_to_hex_string(toBeDel_banks_memory_array))
//			sb.append("\t\tBAD Banks:\t\t\t%d kBytes / %d kBytes => %.2f%% " % (bad_banks_memory_array_kB, memory_LTF_capacity_kB, bad_banks_memory_percent) + byte_array_to_hex_string(bad_banks_memory_array))
//			# Update the memory used for the progress bar to be based on 'FULL' banks only instead
//			memory_used = full_banks_memory_kB
//			sb.append("\t\tOther:\t\t\t\t%d kBytes / %d kBytes => %.2f%%" % (other_memory_kb, memory_LTF_capacity_kB, other_memory_percent))
//
//		# Parse the battery fall counter info
//		if len(response) >= 26:
//			vBattFallCounter_array = response[24:26]
//			vBattFallCounter = byte_array_to_int(vBattFallCounter_array, True)
//			sb.append("\tVBatt Fall Counter:\t\t{} {}".format(vBattFallCounter, byte_array_to_hex_string(vBattFallCounter_array)))
//
//		# Parse the Status Message Flags - 64bit Value Reserved
//		if len(response) >= 34:
//			status_flags = response[26:34]
//			sb.append("\tStatus Message Flags:")
//
//			# First byte - Bits 0-7
//			sb.append('\t\tUSB_PLUGGED_IN:\t\t\t\t {}' .format(status_flags[0] & StatusFlagsBitMasks.USB_PLUGGED_IN))
//			sb.append('\t\tRECORDING_PAUSED:\t\t\t {}'.format((status_flags[0] & StatusFlagsBitMasks.RECORDING_PAUSED) >> 1))
//			sb.append('\t\tFLASH_IS_FULL:\t\t\t\t {}'.format((status_flags[0] & StatusFlagsBitMasks.FLASH_IS_FULL) >> 2))
//			sb.append('\t\tPOWER_IS_GOOD:\t\t\t\t {}'.format((status_flags[0] & StatusFlagsBitMasks.POWER_IS_GOOD) >> 3))
//			sb.append('\t\tADAPTIVE_SCHEDULER_ON:\t\t {}'.format((status_flags[0] & StatusFlagsBitMasks.ADAPTIVE_SCHEDULER) >> 4))
//			sb.append('\t\tDFU_SERVICE_ON:\t\t\t\t {}'.format((status_flags[0] & StatusFlagsBitMasks.DFU_SERVICE_ON) >> 5))
//			sb.append('\t\tFIRST BOOT ON:\t\t\t\t {}'.format((status_flags[0] & StatusFlagsBitMasks.FIRST_BOOT) >> 6))
//
//			# 7th byte - Bits 0-7
//			ltf_write_fail_counter_bytes = status_flags[5:7]
//			ltf_write_fail_counter = byte_array_to_int(ltf_write_fail_counter_bytes, True)
//			sb.append('\t\tLTF WRITE FAIL COUNTER:\t\t {}'.format(ltf_write_fail_counter))
//
//			# Last byte - Bits 0-7
//			sb.append('\t\tFAIL SYNC COUNTER:\t\t\t {}'.format((status_flags[7])))
//
//		# Parse the Next Sync Attempt time
//		if len(response) >= 56:
//			next_sync_time_bytes = response[43:47]
//			next_sync_attempt = byte_array_to_int(next_sync_time_bytes, True)
//			next_sync_attempt_str = UtilTime.minutes_to_time_str(next_sync_attempt)
//			if next_sync_attempt==0:
//				sb.append("\tNext Sync Attempt\t= Not Set")
//			else:
//				sb.append("\tNext Sync Attempt Will Happen At: {}".format(next_sync_attempt_str))
//				
		return sb.toString();
	}
}
