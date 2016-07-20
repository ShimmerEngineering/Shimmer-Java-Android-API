package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

public abstract class CalibDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 3071359258303179516L;

	public String mRangeString = "";
	public int mRangeValue = 0;
	public long mCalibTimeMs = 0;

	public abstract byte[] generateCalParamByteArray();
	public abstract void parseCalParamByteArray(byte[] bufferCalibrationParameters);
	
	
	public void setCalibTimeMs(long calibTimeMs){
		mCalibTimeMs = calibTimeMs;
	}
	
	public long getCalibTime(){
		return mCalibTimeMs;
	}

	public String getCalibTimeParsed(){
		return UtilShimmer.convertMilliSecondsToDateString(mCalibTimeMs);
//		return UtilShimmer.convertMilliSecondsToHrMinSecString(mCalibTime);
	}

	public boolean isCalibTimeZero(){
		return mCalibTimeMs == 0;
	}

	public byte[] generateCalParamByteArrayWithTimestamp() {
		byte[] rangeBytes = new byte[1];
		rangeBytes[0] = (byte)(mRangeValue&0xFF);

		byte[] timestamp = UtilShimmer.convertMilliSecondsToShimmerRtcDataBytesLSB(mCalibTimeMs);
		byte[] bufferCalibParam = generateCalParamByteArray();
		byte[] calibLength = new byte[]{(byte) bufferCalibParam.length};
		
		byte[] returnArray = ArrayUtils.addAll(rangeBytes, calibLength);
		returnArray = ArrayUtils.addAll(returnArray, timestamp);
		returnArray = ArrayUtils.addAll(returnArray, bufferCalibParam);
		return returnArray;
	}

	public void parseCalParamByteArray(byte[] calibTimeBytesTicks, byte[] bufferCalibrationParameters) {
		long calibTimeMs = UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(calibTimeBytesTicks);
		setCalibTimeMs(calibTimeMs);
		parseCalParamByteArray(bufferCalibrationParameters);
	}

}
