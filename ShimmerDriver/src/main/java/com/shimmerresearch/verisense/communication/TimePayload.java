package com.shimmerresearch.verisense.communication;

import com.shimmerresearch.verisense.SensorVerisenseClock;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.payloaddesign.VerisenseTimeDetails;

public class TimePayload extends AbstractPayload {

	private long timeMinutes = 0;
	private long timeTicks = 0;
	private double timeMs = 0.0;

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		
		timeMinutes = parseByteArrayAtIndex(payloadContents, 0, 4);
		timeTicks = parseByteArrayAtIndex(payloadContents, 4, 3);
		timeMs = SensorVerisenseClock.convertRtcMinutesAndTicksToMs(timeMinutes, timeTicks);
		return true;
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
