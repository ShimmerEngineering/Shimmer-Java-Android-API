package com.shimmerresearch.driver.calibration;

public class OldCalDetails{
	
	public String mFileSearchString = "";
	public int mSensorMapKey = 0;
	public int mRange = 0;
	
	public OldCalDetails(String fileSearchString, int sensorMapKey, int range){
		this.mFileSearchString = fileSearchString;
		this.mSensorMapKey = sensorMapKey;
		this.mRange = range;
	}
}