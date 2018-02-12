package com.shimmerresearch.driver;

public class SensorDataArray {
	
	public String[] mSensorNames;
	public String[] mUncalUnits;
	public String[] mCalUnits;
	public double[] mUncalData;
	public double[] mCalData;
	public boolean[] mIsUsingDefaultCalibrationParams;
	
	public int mCalArraysIndex;
	public int mUncalArraysIndex;
	
	
	public SensorDataArray(int length) {
		mSensorNames = new String[length];
		mUncalUnits = new String[length];
		mCalUnits = new String[length];
		mUncalData = new double[length];
		mCalData = new double[length];
		mIsUsingDefaultCalibrationParams = new boolean[length];
		
		mCalArraysIndex = 0;
		mUncalArraysIndex = 0;
	}
	
	
}
