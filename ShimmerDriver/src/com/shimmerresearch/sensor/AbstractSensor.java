package com.shimmerresearch.sensor;

public abstract class AbstractSensor {
	
	public String mSensorName;
	
	public abstract double parseData(byte[] byteArray,int mFWID,int mHWID);
	
	
}
