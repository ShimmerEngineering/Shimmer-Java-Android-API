package com.shimmerresearch.verisense.payloaddesign;

import java.io.Serializable;

import com.shimmerresearch.verisense.SensorVerisenseClock;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.BYTE_COUNT;

public class VerisenseTimeDetails implements Serializable {
	
	private static final long serialVersionUID = -8512683733417198408L;
	
	public static final double DEFAULT_END_TIME_VALUE = Double.MIN_VALUE;
	public static final double DEFAULT_START_TIME_VALUE = Double.MAX_VALUE;
	
	private long endTimeMinutes = 0;
	private long endTimeTicks = 0;
	private double endTimeMs = DEFAULT_END_TIME_VALUE;
	private double startTimeMs = DEFAULT_START_TIME_VALUE;

	public VerisenseTimeDetails() {
		// TODO Auto-generated constructor stub
	}

	public void setEndTimeAndCalculateMs(long endTimeMinutes, long endTimeTicks) {
		setEndTimeMinutes(endTimeMinutes);
		setEndTimeTicks(endTimeTicks);
		
		calculateEndTimeMs();
	}

	public void calculateEndTimeMs() {
		// Convert minutes and clock ticks to milliseconds
		setEndTimeMs(SensorVerisenseClock.convertRtcMinutesAndTicksToMs(endTimeMinutes, endTimeTicks));
	}

	public String getEndTimeStr() {
		return UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long)getEndTimeMs());
	}
	
	public double getStartTimeMs() {
		return startTimeMs;
	}
	
	public void setStartTimeMs(double calculatedStartTimeMs) {
		startTimeMs = calculatedStartTimeMs;
	}
	
	public String getStartTimeStr() {
		return UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long)getStartTimeMs());
	}

	public boolean isStartTimeSet() {
		return startTimeMs != DEFAULT_START_TIME_VALUE;
	}

	public void setEndTimeMs(double endTimeMs) {
		this.endTimeMs = endTimeMs;
	}

	public double getEndTimeMs() {
		return endTimeMs;
	}

	public void setEndTimeMinutes(long endTimeMinutes) {
		this.endTimeMinutes = endTimeMinutes;
	}

	public long getEndTimeMinutes() {
		return endTimeMinutes;
	}

	public void setEndTimeTicks(long endTimeTicks) {
		this.endTimeTicks = endTimeTicks;
	}

	public long getEndTimeTicks() {
		return endTimeTicks;
	}

	public boolean isEndTimeSet() {
		return endTimeMs != DEFAULT_END_TIME_VALUE;
	}

	public void calculateAndSetStartTimeMs(int sampleCount, double timestampDiffInS) {
		setStartTimeMs(calculateStartTimeMs(getEndTimeMs(), sampleCount, timestampDiffInS));
	}
	
	public double calculateDurationMs() {
		return getEndTimeMs()-getStartTimeMs();
	}

	public String generateTimingReport() {
		return ("Timimg [Start=" + (isStartTimeSet()? UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long) getStartTimeMs()):"Not set")
		+ ", End=" + (isEndTimeSet()? UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long) getEndTimeMs()):"Not set")
		+ ", Duration=" + ((isStartTimeSet()&&isEndTimeSet())? UtilVerisenseDriver.convertSecondsToHHmmssSSS(calculateDurationMs()/1000):UtilVerisenseDriver.FEATURE_NOT_AVAILABLE)
		 + "]"
		);
	}
	
	public static double calculateStartTimeMs(double endTimeMs, long sampleCount, double timestampDiffInS) {
		// minus 1 from number of samples as the last sample is at the end time
		return (endTimeMs - ((sampleCount - 1) * timestampDiffInS * 1000));
	}

	public static long parseTimeTicksAtIndex(byte[] byteBuffer, int currentByteIndex) {
		long rtcEndTimeTicks = 0;
		for(int i=0;i<3;i++) {
			byte currentByte = byteBuffer[currentByteIndex++];
			rtcEndTimeTicks += ((((long)currentByte)&0xFF)<<(i*8));
		}
		return rtcEndTimeTicks;
	}

	public static long parseTimeMinutesAtIndex(byte[] byteBuffer, int currentByteIndex) {
		long rtcEndTimeMinutes = 0;
		for(int i=0;i<4;i++) {
			byte currentByte = byteBuffer[currentByteIndex++];
			rtcEndTimeMinutes += ((((long)currentByte)&0xFF)<<(i*8));
		}
		return rtcEndTimeMinutes;
	}

	public static byte[] generateMinutesAndTicksByteArray(double timeMs) {
		double timeS = (double)timeMs/1000;
		long minutes = calculateMinutesFromSeconds(timeS);
		long ticks = calculateTicksFromSeconds(timeS);
		return generateMinutesAndTicksByteArray(minutes, ticks);
	}

	public static long calculateMinutesFromSeconds(double timeS) {
		return (long) (timeS / 60);
	}

	public static long calculateTicksFromSeconds(double timeS) {
		return (long) ((timeS % 60) * AsmBinaryFileConstants.TICKS_PER_SECOND);
	}

	public static byte[] generateMinutesAndTicksByteArray(long minutes, long ticks) {
		byte[] bufArray = new byte[BYTE_COUNT.PAYLOAD_CONTENTS_RTC_BYTES];
		for(int x=0;x<4;x++) {
			bufArray[x] = (byte) ((minutes>>(x*8))&0xFF);
		}
		for(int x=0;x<3;x++) {
			bufArray[x+4] = (byte) ((ticks>>(x*8))&0xFF);
		}
		return bufArray;
	}

}
