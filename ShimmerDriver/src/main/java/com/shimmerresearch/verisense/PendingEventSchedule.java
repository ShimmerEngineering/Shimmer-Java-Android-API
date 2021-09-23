package com.shimmerresearch.verisense;

import java.io.Serializable;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.verisense.communication.payloads.AbstractPayload;

public class PendingEventSchedule implements Serializable{
	
	private int intervalHours, wakeupTimeMinutes, wakeupDurationMinutes, retryIntervalMinutes;
	
	public PendingEventSchedule(byte[] buf, int startIndex) {
		intervalHours = (int) AbstractPayload.parseByteArrayAtIndex(buf, startIndex+0, CHANNEL_DATA_TYPE.UINT8);
		wakeupTimeMinutes = (int) AbstractPayload.parseByteArrayAtIndex(buf, startIndex+1, CHANNEL_DATA_TYPE.UINT16);
		wakeupDurationMinutes = (int) AbstractPayload.parseByteArrayAtIndex(buf, startIndex+3, CHANNEL_DATA_TYPE.UINT8);
		retryIntervalMinutes = (int) AbstractPayload.parseByteArrayAtIndex(buf, startIndex+4, CHANNEL_DATA_TYPE.UINT16);
	}

	public PendingEventSchedule(int intervalHours, int wakeupTimeMinutes, int wakeupDurationMinutes, int retryIntervalMinutes) {
		this.intervalHours = intervalHours;
		this.wakeupTimeMinutes = wakeupTimeMinutes;
		this.wakeupDurationMinutes = wakeupDurationMinutes;
		this.retryIntervalMinutes = retryIntervalMinutes;
	}

	public int getIntervalHours() {
		return intervalHours;
	}

	public void setIntervalHours(int intervalHours) {
		this.intervalHours = UtilShimmer.nudgeInteger(intervalHours, 0, (int) (Math.pow(2, 8)-1));
	}

	public int getWakeupTimeMinutes() {
		return wakeupTimeMinutes;
	}

	public void setWakeupTimeMinutes(int wakeupTimeMinutes) {
		this.wakeupTimeMinutes = UtilShimmer.nudgeInteger(wakeupTimeMinutes, 0, (int) (Math.pow(2, 16)-1));
	}

	public int getWakeupDurationMinutes() {
		return wakeupDurationMinutes;
	}

	public void setWakeupDurationMinutes(int wakeupDurationMinutes) {
		this.wakeupDurationMinutes = UtilShimmer.nudgeInteger(wakeupDurationMinutes, 0, (int) (Math.pow(2, 8)-1));
	}

	public int getRetryIntervalMinutes() {
		return retryIntervalMinutes;
	}

	public void setRetryIntervalMinutes(int retryIntervalMinutes) {
		this.retryIntervalMinutes = UtilShimmer.nudgeInteger(retryIntervalMinutes, 0, (int) (Math.pow(2, 16)-1));
	}

	public byte[] generateByteArray() {
		byte[] byteArray = new byte[6];
		byteArray[0] = (byte) intervalHours;
		
		byteArray[1] = (byte) (wakeupTimeMinutes & 0xFF);
		byteArray[2] = (byte) ((wakeupTimeMinutes >> 8) & 0xFF);
		
		byteArray[3] = (byte) wakeupDurationMinutes;
		
		byteArray[4] = (byte) (retryIntervalMinutes & 0xFF);
		byteArray[5] = (byte) ((retryIntervalMinutes >> 8) & 0xFF);
		return byteArray;
	}

}
