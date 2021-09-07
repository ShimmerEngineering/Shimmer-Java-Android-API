package com.shimmerresearch.bluetooth;

import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.SensorSystemTimeStamp;

public class SystemTimestampPlot {

	private boolean mIsFirstSystemTimestampOffsetStored = false;
	private double mOffsetFirstTime=-1;
	private boolean mIsFirstSystemTimestampOffsetPlotStored = false;
	private double mFirstSystemTimestampPlot = -1;

	public ObjectCluster processSystemTimestampPlot(ObjectCluster objectCluster) {
		if(!mIsFirstSystemTimestampOffsetStored) {
			mIsFirstSystemTimestampOffsetStored = true;
			long systemTimeStamp = objectCluster.mSystemTimeStamp;
			mOffsetFirstTime = systemTimeStamp-objectCluster.getTimestampMilliSecs();
		}
		
		double calTimestamp = objectCluster.getTimestampMilliSecs();
		double systemTimestampPlot = calTimestamp+mOffsetFirstTime;
		
		if(!mIsFirstSystemTimestampOffsetPlotStored) {
			mIsFirstSystemTimestampOffsetPlotStored = true;
			mFirstSystemTimestampPlot  = systemTimestampPlot;
		}

		objectCluster.addDataToMap(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, systemTimestampPlot);
		
		double systemTimestampPlotZeroed = 0;
		if(mIsFirstSystemTimestampOffsetPlotStored) {
			systemTimestampPlotZeroed = systemTimestampPlot - mFirstSystemTimestampPlot;
		}
		objectCluster.addCalData(SensorSystemTimeStamp.channelSystemTimestampPlotZeroed, systemTimestampPlotZeroed);
		return objectCluster;
	}

	public void reset() {
		mIsFirstSystemTimestampOffsetStored = false;
		mIsFirstSystemTimestampOffsetPlotStored = false;
	}

}
