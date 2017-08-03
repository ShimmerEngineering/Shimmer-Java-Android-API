package com.shimmerresearch.driver.calibration;

public class OldCalDetails{
	
	public String mFileSearchString = "";
	public int mSensorId = 0;
	public int mRange = 0;
	
	public OldCalDetails(String fileSearchString, int sensorId, int range){
		this.mFileSearchString = fileSearchString;
		this.mSensorId = sensorId;
		this.mRange = range;
	}
}