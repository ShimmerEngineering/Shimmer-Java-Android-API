package com.shimmerresearch.driverUtilities;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;

public class OnTheFlyCalGyro {

	protected double mGyroOVCalThreshold = 1.2;
	DescriptiveStatistics mGyroX;
	DescriptiveStatistics mGyroY;
	DescriptiveStatistics mGyroZ;
	DescriptiveStatistics mGyroXRaw;
	DescriptiveStatistics mGyroYRaw;
	DescriptiveStatistics mGyroZRaw;
	protected boolean mEnableOntheFlyGyroOVCal = false;
	
	/**
	 * @param enable this enables the calibration of the gyroscope while streaming
	 * @param bufferSize sets the buffersize of the window used to determine the new calibration parameters, see implementation for more details
	 * @param threshold sets the threshold of when to use the incoming data to recalibrate gyroscope offset, this is in degrees, and the default value is 1.2
	 */
	public void enableOnTheFlyGyroCal(boolean state, int bufferSize, double threshold){
		setOnTheFlyGyroCal(state);
		if (mEnableOntheFlyGyroOVCal){
			mGyroOVCalThreshold=threshold;
			mGyroX=new DescriptiveStatistics(bufferSize);
			mGyroY=new DescriptiveStatistics(bufferSize);
			mGyroZ=new DescriptiveStatistics(bufferSize);
			mGyroXRaw=new DescriptiveStatistics(bufferSize);
			mGyroYRaw=new DescriptiveStatistics(bufferSize);
			mGyroZRaw=new DescriptiveStatistics(bufferSize);
		}
	}

	public void setOnTheFlyGyroCal(boolean state){
		mEnableOntheFlyGyroOVCal = state;
	}

    public boolean isGyroOnTheFlyCalEnabled(){
		return mEnableOntheFlyGyroOVCal;
	}

	public void updateGyroOnTheFlyGyroOVCal(CalibDetailsKinematic calibDetails, double[] gyroCalibratedData, double uncalX, double uncalY, double uncalZ) {
		if (mEnableOntheFlyGyroOVCal){
			mGyroX.addValue(gyroCalibratedData[0]);
			mGyroY.addValue(gyroCalibratedData[1]);
			mGyroZ.addValue(gyroCalibratedData[2]);
			mGyroXRaw.addValue(uncalX);
			mGyroYRaw.addValue(uncalY);
			mGyroZRaw.addValue(uncalZ);
			if (mGyroX.getStandardDeviation()<mGyroOVCalThreshold && mGyroY.getStandardDeviation()<mGyroOVCalThreshold && mGyroZ.getStandardDeviation()<mGyroOVCalThreshold){
//				mOffsetVectorGyroscope[0][0]=mGyroXRaw.getMean();
//				mOffsetVectorGyroscope[1][0]=mGyroYRaw.getMean();
//				mOffsetVectorGyroscope[2][0]=mGyroZRaw.getMean();
				calibDetails.updateCurrentOffsetVector(mGyroXRaw.getMean(), mGyroYRaw.getMean(), mGyroZRaw.getMean());
			}
		}
	}
}
