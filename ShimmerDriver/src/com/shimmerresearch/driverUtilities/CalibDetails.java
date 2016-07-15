package com.shimmerresearch.driverUtilities;

import java.io.Serializable;

public abstract class CalibDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 3071359258303179516L;

	public long mCalibTime = 0;

	public abstract byte[] generateCalParamByteArrayWithTimestamp();
	
	
	public void setCalibTime(long calibTime){
		mCalibTime = calibTime;
	}

	public long getCalibTime(){
		return mCalibTime;
	}

	public String getCalibTimeParsed(){
		return UtilShimmer.convertMilliSecondsToDateString(mCalibTime);
//		return UtilShimmer.convertMilliSecondsToHrMinSecString(mCalibTime);
		
	}

	public boolean isCalibTimeZero(){
		return mCalibTime == 0;
	}
	

}
