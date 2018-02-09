package com.shimmerresearch.driver;

public class SensorDataArray {
	
	public String[] mUncalSensorNames;
	public String[] mCalSensorNames;
	public String[] mUncalUnits;
	public String[] mCalUnits;
	public double[] mUncalData;
	public double[] mCalData;
	public boolean[] mIsUsingDefaultCalibrationParams;
	
	int mCalArraysIndex;
	int mUncalArraysIndex;
	
	
	public SensorDataArray(int length) {
		mUncalSensorNames = new String[length];
		mCalSensorNames = new String[length];
		mUncalUnits = new String[length];
		mCalUnits = new String[length];
		mUncalData = new double[length];
		mCalData = new double[length];
		mIsUsingDefaultCalibrationParams = new boolean[length];
		
		mCalArraysIndex = 0;
		mUncalArraysIndex = 0;
	}
	
	
}
