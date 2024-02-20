package com.shimmerresearch.verisense.communication.payloads;

import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.payloaddesign.VerisenseTimeDetails;

/**
 * @author Mark Nolan
 *
 */
public class EventLogPayload extends AbstractPayload {

	public enum LOG_EVENT {
		NONE,
		BATTERY_FALL,
		BATTERY_RECOVER,
		WRITE_TO_FLASH_SUCCESS,
		WRITE_TO_FLASH_FAIL_GENERAL,
		WRITE_TO_FLASH_FULL,
		WRITE_TO_FLASH_FAIL_CHECK_ADDR_FREE,
		WRITE_TO_FLASH_FAIL_LOW_BATT_CHECK_ADDR_FREE,
		WRITE_TO_FLASH_FAIL_LOW_BATT_FLASH_ON,
		WRITE_TO_FLASH_FAIL_LOW_BATT_FLASH_WRITE,
		WRITE_TO_FLASH_FAIL_LOW_BATT_BEFORE_START,
		USB_PLUGGED_IN,
		USB_PLUGGED_OUT,
		RECORDING_PAUSED,
		RECORDING_RESUMED,
		BATTERY_RECOVER_IN_BATT_CHECK_TIMER,
		TSK_FREE_UP_FLASH,
		FREE_UP_FLASH_FAIL_LOW_BATT,
		PAYLOAD_PACKAGING_TASK_SET,
		PAYLOAD_PACKAGING_FUNCTION_CALL,
		BATTERY_VOLTAGE,
		TSK_WRITE_LOOKUP_TBL_CHANGES_TO_EEPROM,
		LPCOMP_ON,
		LPCOMP_ON_ALREADY,
		LPCOMP_OFF,
		LPCOMP_TRIED_BUT_BATT_LOW,
		BLE_CONNECTED,
		BLE_DISCONNECTED,
		TSK_WRITE_FLASH,
		PPG_TIMER_START,
		PAYLOAD_OVERSHOT,
		ADVERTISING_START,
		ADVERTISING_STOP,
		NIMH_BATT_PPG_BLOCKED_BLE_RETRY,
		NIMH_BATT_PPG_BLOCKED_BLE_ADAPT_SCH,
		NIMH_BATT_PPG_BLOCKED_BLE_PENDING_EVENTS,
		NIMH_BATT_BLE_BLOCKED_PPG,
		USB_PORT_OPEN,
		USB_PORT_CLOSED,
		FIFO_INT_SAFETY_CHECK_EVENT_ACCEL1,
		FIFO_INT_SAFETY_CHECK_EVENT_ACCEL2GYRO,
		FIFO_INT_SAFETY_CHECK_EVENT_MAX86XXX,
		FIFO_INT_SAFETY_CHECK_EVENT_MAX3000X,
		FIFO_INT_SAFETY_CHECK_EVENT_ADC
	}
	
	public final String NO_EVENTS_LOGGED_TXT = "No events logged.";
	
	List<EventLogEntry> listOfEventLogEntries = new ArrayList<EventLogEntry>();

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		this.payloadContents = payloadContents;
		isSuccess = false;
		
		listOfEventLogEntries.clear();
		
		if (payloadContents.length > 0) {
			for(int i=0;i<payloadContents.length;i+=8) {
				int event = payloadContents[i+7] & 0xFF;
				
				if(event==LOG_EVENT.NONE.ordinal()) {
					/* Skip */
				} else if(event==LOG_EVENT.BATTERY_VOLTAGE.ordinal()) {
					long batteryVoltage = parseByteArrayAtIndex(payloadContents, i, CHANNEL_DATA_TYPE.UINT24);
					listOfEventLogEntries.add(new EventLogEntry(event, batteryVoltage));
				} else {
					double timeMs = VerisenseTimeDetails.parseTimeMsFromMinutesAndTicksAtIndex(payloadContents, i);
					listOfEventLogEntries.add(new EventLogEntry(event, timeMs));
				}
			}
		}

		isSuccess = true;
		return false;
	}

	@Override
	public byte[] generatePayloadContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Received Response, length= " + payloadContents.length + " bytes:\n");
		sb.append(generateDebugStringNoHeader());
		return sb.toString();
	}

	public String generateDebugStringNoHeader() {
		StringBuilder sb = new StringBuilder();
		
		if(listOfEventLogEntries.size()==0) {
			sb.append(NO_EVENTS_LOGGED_TXT);
			sb.append("\n");
		} else {
			for(int i=0;i<listOfEventLogEntries.size();i++) {
				EventLogEntry eventLogEntry = listOfEventLogEntries.get(i);
				sb.append("Index = " + i + ", ");
				if(eventLogEntry.event==LOG_EVENT.BATTERY_VOLTAGE.ordinal()) {
					sb.append("Battery Level = " + eventLogEntry.batteryVoltage + "mV");
				} else {
					sb.append("Time = " + eventLogEntry.getTimeString());
					sb.append(", Event = " + eventLogEntry.getLogEventStr());
				}
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}

	public class EventLogEntry {

		public int event;
		public double timeMs;
		public long batteryVoltage;

		public EventLogEntry(int event, double timeMs) {
			this.event = event;
			this.timeMs = timeMs;
		}

		public EventLogEntry(int event, long batteryVoltage) {
			this.event = event;
			this.batteryVoltage = batteryVoltage;
		}
		
		public String getLogEventStr() {
			for(LOG_EVENT logEvent:LOG_EVENT.values()) {
				if(logEvent.ordinal()==event) {
					return logEvent.toString();
				}
			}
			return Integer.toString(event);
		}

		public String getTimeString() {
			return UtilVerisenseDriver.millisecondsToStringWitNanos(timeMs);
		}

	}

}
