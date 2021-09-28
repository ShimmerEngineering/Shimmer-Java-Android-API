package com.shimmerresearch.verisense.communication.payloads;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.payloaddesign.VerisenseTimeDetails;
import com.shimmerresearch.verisense.sensors.SensorVerisenseClock;

/**
 * @author Mark Nolan
 *
 */
public class TimePayload extends AbstractPayload {

	private long timeMinutes = 0;
	private long timeTicks = 0;
	private double timeMs = 0.0;

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		isSuccess = false;

		timeMinutes = parseByteArrayAtIndex(payloadContents, 0, CHANNEL_DATA_TYPE.UINT32);
		timeTicks = parseByteArrayAtIndex(payloadContents, 4, CHANNEL_DATA_TYPE.UINT24);
		timeMs = SensorVerisenseClock.convertRtcMinutesAndTicksToMs(timeMinutes, timeTicks);
		
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
		sb.append("ASM Time Parsed:\n");
		sb.append("\tParsed:\t" + getTimeParsed() + "\n");
		return sb.toString();
	}

	public long getTimeMinutes() {
		return timeMinutes;
	}

	public long getTimeTicks() {
		return timeTicks;
	}
	
	public double getTimeMs() {
		return timeMs;
	}
	
	public String getTimeParsed() {
		return UtilVerisenseDriver.convertMilliSecondsToDateString((long) timeMs, true);
	}
	
	public void setTimeMs(double timeMs) {
		double timeS = (double)timeMs/1000;
		timeMinutes = VerisenseTimeDetails.calculateMinutesFromSeconds(timeS);
		timeTicks = VerisenseTimeDetails.calculateTicksFromSeconds(timeS);
		payloadContents = VerisenseTimeDetails.generateMinutesAndTicksByteArray(timeMinutes, timeTicks);
	}

}
