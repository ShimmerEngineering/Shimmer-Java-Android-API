package com.shimmerresearch.driver;

public class ChannelDetails {

	/**
	 * Indicates if sensors channel is enabled.
	 */
	public boolean mIslEnabled = false;
	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader = 0;
	public String mLabel = "";

	public ChannelDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label) {
		mIslEnabled = isChannelEnabled;
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		mLabel = label;
	}

}
