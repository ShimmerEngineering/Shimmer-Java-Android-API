package com.shimmerresearch.verisense.communication.payloads;

import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.payloaddesign.VerisenseTimeDetails;

/**
 * @author Mark Nolan
 *
 */
public class RwcSchedulePayload extends AbstractPayload {

	public double currentTimeMs;
	public String bleControlCounter;
	public long bleNextAlarmTimeMinutesDataTransfer;
	public long bleNextAlarmTimeMinutesStatus;
	public long bleNextAlarmTimeMinutesRtcSync;
	public long bleNextAlarmTimeMinutesRetry;
	public byte retryCount;
	public String retryOperation;
	public long bleNextAlarmTimeMinutesAdaptiveSch;
	public String adaptiveScheduler;
	public byte syncFailCounter;
	public long bleNextAlarmTimeMinutesFlashWriteRetry;
	public String currentOperation;
	public byte failCounterShort;
	public byte failCounterLong;

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		this.payloadContents = payloadContents;
		isSuccess = false;
		
		int idx = 0;
		this.currentTimeMs = VerisenseTimeDetails.parseTimeMsFromMinutesAndTicksAtIndex(payloadContents, 0);
		idx += 7;
		
		this.bleControlCounter = "";
		if ((payloadContents[idx] & 0xFF) == 0) {
			bleControlCounter = "Data Transfer";
		} else if ((payloadContents[idx] & 0xFF) == 1) {
			bleControlCounter = "Status";
		} else if ((payloadContents[idx] & 0xFF) == 2) {
			bleControlCounter = "RTC Sync";
		} else if ((payloadContents[idx] & 0xFF) == 0xFF) {
			bleControlCounter = "Never";
		}
		idx += 1;

		int schDebugTimeLen = 8;

		this.bleNextAlarmTimeMinutesDataTransfer = VerisenseTimeDetails.parseTimeMinutesAtIndex(payloadContents, idx);
		idx += schDebugTimeLen;

		this.bleNextAlarmTimeMinutesStatus = VerisenseTimeDetails.parseTimeMinutesAtIndex(payloadContents, idx);
		idx += schDebugTimeLen;

		this.bleNextAlarmTimeMinutesRtcSync = VerisenseTimeDetails.parseTimeMinutesAtIndex(payloadContents, idx);
		idx += schDebugTimeLen;

		this.bleNextAlarmTimeMinutesRetry = VerisenseTimeDetails.parseTimeMinutesAtIndex(payloadContents, idx);
		idx += schDebugTimeLen;
		this.retryCount = payloadContents[idx];
		idx += 1;

		if (payloadContents[idx] == 1) {
			this.retryOperation = "BLE ON";
		} else {
			this.retryOperation = "BLE OFF";
		}
		idx += 1;

		if (payloadContents.length>=52) {
			this.bleNextAlarmTimeMinutesAdaptiveSch = VerisenseTimeDetails.parseTimeMinutesAtIndex(payloadContents, idx);
			idx += schDebugTimeLen;

			if (payloadContents[idx] == 1) {
				this.adaptiveScheduler = "On";
			} else {
				this.adaptiveScheduler = "Off";
			}
			idx += 1;

			this.syncFailCounter = payloadContents[idx];
			idx += 1;
		}

		if (payloadContents.length>=63) {
			this.bleNextAlarmTimeMinutesFlashWriteRetry = VerisenseTimeDetails.parseTimeMinutesAtIndex(payloadContents, idx);
			idx += schDebugTimeLen;

			if(payloadContents[idx] == 0) {
				this.currentOperation = "FLASH_WRITE_RETRY_INACTIVE";
			} else if (payloadContents[idx] == 1) {
				this.currentOperation = "SHORT_FLASH_WRITE_RETRY";
			} else if (payloadContents[idx] == 2) {
				this.currentOperation = "ATTEMPT_FLASH_WRITE";
			} else if (payloadContents[idx] == 3) {
				this.currentOperation = "LONG_FLASH_WRITE_RETRY";
			} else if (payloadContents[idx] == 4) {
				this.currentOperation = "SENSOR_PAUSED_UNTIL_USB_PLUG_IN";
			}
			idx += 1;
			this.failCounterShort = payloadContents[idx];
			idx += 1;
			this.failCounterLong = payloadContents[idx];
			idx += 1;
		}

		isSuccess = true;
		return isSuccess;
	}
	
	@Override
	public byte[] generatePayloadContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();

		sb.append("RWC Schedule:\n");
		sb.append(CONSOLE_DIVIDER_STRING);
		
		sb.append("Verisense Current Time:\n");
		sb.append("\t" + UtilVerisenseDriver.millisecondsToStringWitNanos(currentTimeMs) + "\n");

		sb.append("BLE Control Counter:\n");
		sb.append("\t" + bleControlCounter + "\n");

		sb.append("BLE_DATA_TRANS:\n");
		if (bleNextAlarmTimeMinutesDataTransfer==0) {
			sb.append("\tNext Alarm Time\t= Not Set\n");
		} else {
			sb.append("\tNext Alarm Time\t= " + bleNextAlarmTimeMinutesDataTransfer + " minutes\n");
			sb.append("\tNext Alarm TIme Will Happen At: " + UtilVerisenseDriver.millisecondsToString(bleNextAlarmTimeMinutesDataTransfer * 60 * 1000) + "\n");
		}

		sb.append("BLE_STATUS:\n");
		if (bleNextAlarmTimeMinutesStatus==0) {
			sb.append("\tNext Alarm Time\t= Not Set\n");
		} else {
			sb.append("\tNext Alarm Time\t= " + bleNextAlarmTimeMinutesStatus + " minutes\n");
			sb.append("\tNext Alarm TIme Will Happen At: " + UtilVerisenseDriver.millisecondsToString(bleNextAlarmTimeMinutesStatus * 60 * 1000) + "\n");
		}

		sb.append("BLE_RTC_SYNC:\n");
		if (bleNextAlarmTimeMinutesRtcSync==0) {
			sb.append("\tNext Alarm Time\t= Not Set\n");
		} else {
			sb.append("\tNext Alarm Time\t= " + bleNextAlarmTimeMinutesRtcSync + " minutes\n");
			sb.append("\tNext Alarm TIme Will Happen At: " + UtilVerisenseDriver.millisecondsToString(bleNextAlarmTimeMinutesRtcSync * 60 * 1000) + "\n");
		}

		sb.append("BLE RETRY COUNTER:\n");
		sb.append("\tNext Alarm Time will turn: " + retryOperation + "\n");
		sb.append("\tRetry Count= " + retryCount + "\n");
		sb.append("\tNext Alarm Time\t= " + bleNextAlarmTimeMinutesRetry + " minutes\n");
		sb.append("\tNext Alarm TIme Will Happen At: " + UtilVerisenseDriver.millisecondsToString(bleNextAlarmTimeMinutesRetry * 60 * 1000) + "\n");
		sb.append(CONSOLE_DIVIDER_STRING);

		sb.append("ADAPTIVE SCHEDULER:\n");
		if (payloadContents.length>=52) {
			sb.append("\tCurrent Status: " + adaptiveScheduler + "\n");
			sb.append("\tSync Fail Counter: " + syncFailCounter + "\n");
			sb.append("\tNext Alarm Time\t= " + bleNextAlarmTimeMinutesAdaptiveSch + " minutes\n");
			sb.append("\tNext Alarm TIme Will Happen At: " + UtilVerisenseDriver.millisecondsToString(bleNextAlarmTimeMinutesAdaptiveSch * 60 * 1000) + "\n");
		} else {
			sb.append("\tNot supported");
		}

		sb.append(CONSOLE_DIVIDER_STRING);
		
		sb.append("LTF Fail Retry Counter:");
		if (payloadContents.length>=63) {
			sb.append("\tCurrent Operation: " + currentOperation + "\n");
			sb.append("\tShort Fail Counter: " + failCounterShort + "\n");
			sb.append("\tLong Fail Counter: " + failCounterLong + "\n");
			sb.append("\tNext Alarm Time\t= " + bleNextAlarmTimeMinutesFlashWriteRetry + " minutes\n");
			sb.append("\tNext Alarm TIme Will Happen At: " + UtilVerisenseDriver.millisecondsToString(bleNextAlarmTimeMinutesFlashWriteRetry * 60 * 1000) + "\n");
		} else {
			sb.append("\tNot supported\n");
		}
		
		sb.append(CONSOLE_DIVIDER_STRING);

		return sb.toString();
	}

}
