package com.shimmerresearch.driverUtilities;

import java.io.Serializable;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;

public class OnTheFlyGyroOffsetCal implements Serializable {

	private static final long serialVersionUID = -4153196345016560456L;
	
	private boolean mIsEnabled = false;
	private double mOffsetThreshold = 1.2;
	private int bufferSize;
	
	transient DescriptiveStatistics mGyroXCal;
	transient DescriptiveStatistics mGyroYCal;
	transient DescriptiveStatistics mGyroZCal;
	transient DescriptiveStatistics mGyroXUncal;
	transient DescriptiveStatistics mGyroYUncal;
	transient DescriptiveStatistics mGyroZUncal;
	
	/**
	 * @param enable this enables the calibration of the gyroscope while streaming
	 * @param bufferSize sets the buffersize of the window used to determine the new calibration parameters, see implementation for more details
	 * @param threshold sets the threshold of when to use the incoming data to recalibrate gyroscope offset, this is in degrees, and the default value is 1.2
	 */
	public void enableOnTheFlyGyroCal(boolean state, int bufferSize, double threshold){
		enableOnTheFlyGyroCal(state);
		setGyroOnTheFlyCalThreshold(threshold);
		setBufferSize(bufferSize);
	}

	public void setBufferSizeFromSamplingRate(double samplingRate) {
		setBufferSize((int)Math.round(samplingRate));
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		
		if (mIsEnabled && mGyroXCal!=null){
			mGyroXCal.setWindowSize(bufferSize);
			mGyroYCal.setWindowSize(bufferSize);
			mGyroZCal.setWindowSize(bufferSize);
			mGyroXUncal.setWindowSize(bufferSize);
			mGyroYUncal.setWindowSize(bufferSize);
			mGyroZUncal.setWindowSize(bufferSize);
		}
	}

	public void setGyroOnTheFlyCalThreshold(double threshold) {
		this.mOffsetThreshold = threshold;
	}

	public double getGyroOnTheFlyCalThreshold() {
		return mOffsetThreshold;
	}

	public void enableOnTheFlyGyroCal(boolean state){
		mIsEnabled = state;
		
		if(mIsEnabled && mGyroXCal==null) {
			mGyroXCal = new DescriptiveStatistics(bufferSize);
			mGyroYCal = new DescriptiveStatistics(bufferSize);
			mGyroZCal = new DescriptiveStatistics(bufferSize);
			mGyroXUncal = new DescriptiveStatistics(bufferSize);
			mGyroYUncal = new DescriptiveStatistics(bufferSize);
			mGyroZUncal = new DescriptiveStatistics(bufferSize);
		}
	}

    public boolean isGyroOnTheFlyCalEnabled(){
		return mIsEnabled;
	}

	public void updateGyroOnTheFlyGyroOVCal(CalibDetailsKinematic calibDetails, double[] gyroCalibratedData, double[] gyroUncalibratedData) {
		updateGyroOnTheFlyGyroOVCal(calibDetails, gyroCalibratedData, gyroUncalibratedData[0], gyroUncalibratedData[1], gyroUncalibratedData[2]);
	}

	public void updateGyroOnTheFlyGyroOVCal(CalibDetailsKinematic calibDetails, double[] gyroCalibratedData, double uncalX, double uncalY, double uncalZ) {
		//Already checked if isEnabled in ShimmerDevice class/sensor class, no need to do a second time.
//		if (mIsEnabled){
			mGyroXCal.addValue(gyroCalibratedData[0]);
			mGyroYCal.addValue(gyroCalibratedData[1]);
			mGyroZCal.addValue(gyroCalibratedData[2]);
			mGyroXUncal.addValue(uncalX);
			mGyroYUncal.addValue(uncalY);
			mGyroZUncal.addValue(uncalZ);
			if (mGyroXCal.getStandardDeviation()<mOffsetThreshold 
					&& mGyroYCal.getStandardDeviation()<mOffsetThreshold 
					&& mGyroZCal.getStandardDeviation()<mOffsetThreshold){
				calibDetails.updateCurrentOffsetVector(mGyroXUncal.getMean(), mGyroYUncal.getMean(), mGyroZUncal.getMean());
			}
//		}
	}
	
}
