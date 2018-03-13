package com.shimmerresearch.algorithms.gyroOnTheFlyCal;

import java.io.Serializable;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;

public class OnTheFlyGyroOffsetCal implements Serializable {

	private static final long serialVersionUID = -4153196345016560456L;
	
	public static final double DEFAULT_THRESHOLD = 1.2;
	
	private boolean mIsEnabled = false;
	private double mOffsetThreshold = DEFAULT_THRESHOLD;
	private int bufferSize = 1;
	
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
	public void setIsEnabled(boolean state, int bufferSize, double threshold){
		setIsEnabled(state);
		setOffsetThreshold(threshold);
		setBufferSize(bufferSize);
	}

	public void setIsEnabled(boolean state){
		mIsEnabled = state;
		setupBuffers();
	}

	public boolean isEnabled() {
		return mIsEnabled;
	}

	public void setBufferSizeFromSamplingRate(double samplingRate) {
		setBufferSize((int)Math.round(samplingRate));
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		
		if(mIsEnabled && bufferSize>0) {
			if(mGyroXCal==null) {
				setupBuffers();
			} else {
				mGyroXCal.setWindowSize(bufferSize);
				mGyroYCal.setWindowSize(bufferSize);
				mGyroZCal.setWindowSize(bufferSize);
				mGyroXUncal.setWindowSize(bufferSize);
				mGyroYUncal.setWindowSize(bufferSize);
				mGyroZUncal.setWindowSize(bufferSize);
			}
		}
	}

	protected void setupBuffers() {
		if(mIsEnabled && mGyroXCal==null && bufferSize>0) {
			mGyroXCal = new DescriptiveStatistics(bufferSize);
			mGyroYCal = new DescriptiveStatistics(bufferSize);
			mGyroZCal = new DescriptiveStatistics(bufferSize);
			mGyroXUncal = new DescriptiveStatistics(bufferSize);
			mGyroYUncal = new DescriptiveStatistics(bufferSize);
			mGyroZUncal = new DescriptiveStatistics(bufferSize);
		}
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setOffsetThreshold(double threshold) {
		this.mOffsetThreshold = threshold;
	}

	public double getOffsetThreshold() {
		return mOffsetThreshold;
	}

	public void updateGyroOnTheFlyGyroOVCal(CalibDetailsKinematic calibDetails, double[] gyroCalibratedData, double[] gyroUncalibratedData) {
		updateGyroOnTheFlyGyroOVCal(calibDetails, gyroCalibratedData, gyroUncalibratedData[0], gyroUncalibratedData[1], gyroUncalibratedData[2]);
	}

	public void updateGyroOnTheFlyGyroOVCal(CalibDetailsKinematic calibDetails, double[] gyroCalibratedData, double uncalX, double uncalY, double uncalZ) {
		if(mGyroXCal==null) {
			System.err.println("Gyro on-the-fly gyro calibration wasn't setup on connection...setting up now");
			setupBuffers();
		}
	
		mGyroXCal.addValue(gyroCalibratedData[0]);
		mGyroYCal.addValue(gyroCalibratedData[1]);
		mGyroZCal.addValue(gyroCalibratedData[2]);
		mGyroXUncal.addValue(uncalX);
		mGyroYUncal.addValue(uncalY);
		mGyroZUncal.addValue(uncalZ);
		
		//Process if the buffer is full
		if(mGyroYCal.getWindowSize()==mGyroYCal.getN()) {
			if (mGyroXCal.getStandardDeviation()<mOffsetThreshold 
					&& mGyroYCal.getStandardDeviation()<mOffsetThreshold 
					&& mGyroZCal.getStandardDeviation()<mOffsetThreshold){
				calibDetails.updateCurrentOffsetVector(mGyroXUncal.getMean(), mGyroYUncal.getMean(), mGyroZUncal.getMean());
//				System.err.println("UPDATING\tBufferSize=" + bufferSize);
			}
		}
	}

}
