package com.shimmerresearch.driver.calibration;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driverUtilities.UtilShimmer;

public abstract class CalibDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 3071359258303179516L;

	public String mRangeString = "";
	public int mRangeValue = 0;
	public long mCalibTimeMs = 0;
	
	
	//In order of increasing priority
	public enum CALIB_READ_SOURCE{
		UNKNOWN,
		SD_HEADER,
		LEGACY_BT_COMMAND,
		INFOMEM,
		RADIO_DUMP,
		FILE_DUMP,
		USER_MODIFIED;
	}
	
	public CALIB_READ_SOURCE mCalibReadSource = CALIB_READ_SOURCE.UNKNOWN;

	public abstract byte[] generateCalParamByteArray();
	public abstract void parseCalParamByteArray(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource);
	public abstract void resetToDefaultParameters();
	
	public void setCalibTimeMs(long calibTimeMs){
		mCalibTimeMs = calibTimeMs;
	}
	
	public long getCalibTimeMs(){
		return mCalibTimeMs;
	}

	public String getCalibTimeParsed(){
		return UtilShimmer.convertMilliSecondsToDateString(getCalibTimeMs(), false);
//		return UtilShimmer.convertMilliSecondsToHrMinSecString(mCalibTime);
	}

	public boolean isCalibTimeZero(){
		return getCalibTimeMs() == 0;
	}

	public byte[] generateCalibDump() {
		byte[] rangeBytes = new byte[1];
		rangeBytes[0] = (byte)(mRangeValue&0xFF);

		byte[] timestamp = UtilShimmer.convertMilliSecondsToShimmerRtcDataBytesLSB(getCalibTimeMs());
		byte[] bufferCalibParam = generateCalParamByteArray();
		byte[] calibLength = new byte[]{(byte) bufferCalibParam.length};
		
		byte[] returnArray = ArrayUtils.addAll(rangeBytes, calibLength);
		returnArray = ArrayUtils.addAll(returnArray, timestamp);
		returnArray = ArrayUtils.addAll(returnArray, bufferCalibParam);
		return returnArray;
	}

	public void parseCalibDump(byte[] calibTimeBytesTicks, byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		long calibTimeMs = UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(calibTimeBytesTicks);
		
		// Parse in order of priority
		if(calibTimeMs>getCalibTimeMs() || calibReadSource.ordinal()>=getCalibReadSource().ordinal()){
			if(UtilShimmer.isAllFF(bufferCalibrationParameters)
					||UtilShimmer.isAllZeros(bufferCalibrationParameters)){
				return;
			}

			setCalibTimeMs(calibTimeMs);
			parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
		}
	}
	
	protected void setCalibReadSource(CALIB_READ_SOURCE calibReadSource) {
		mCalibReadSource = calibReadSource;
	}
	
	public CALIB_READ_SOURCE getCalibReadSource() {
		return mCalibReadSource;
	}
	
	public void resetToDefaultParametersCommon() {
		setCalibTimeMs(0);
//		setCalibReadSource(CALIB_READ_SOURCE.USER_MODIFIED);
	}

}
