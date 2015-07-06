package com.shimmerresearch.driver;

import java.io.Serializable;

public class SensorEnabledDetails implements Serializable{
	public boolean mIsEnabled = false;
	public long mDerivedSensorBitmapID = 0;
	public SensorDetails mSensorDetails;
	
	public SensorEnabledDetails(boolean isEnabled, long derivedSensorBitmapID, SensorDetails sensorDetails){
		mIsEnabled = isEnabled;
		mDerivedSensorBitmapID = derivedSensorBitmapID;
		mSensorDetails = sensorDetails;
	}

	public boolean isDerivedChannel() {
		if(mDerivedSensorBitmapID>0) {
			return true;
		}
		return false;
	}
	
	
}