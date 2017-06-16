package com.shimmerresearch.sensors.bmpX80;

import com.shimmerresearch.driver.calibration.CalibDetails;

public abstract class CalibDetailsBmpX80 extends CalibDetails {

	private static final long serialVersionUID = 8601750188557924758L;

	public byte[] mPressureCalRawParams = new byte[23];

	public abstract double[] calibratePressureSensorData(double UP, double UT);
	
	public void setPressureRawCoefficients(byte[] pressureCalRawParams){
		mPressureCalRawParams = pressureCalRawParams;
	}

	public byte[] getPressureRawCoefficients(){
		return mPressureCalRawParams;
	}

}
