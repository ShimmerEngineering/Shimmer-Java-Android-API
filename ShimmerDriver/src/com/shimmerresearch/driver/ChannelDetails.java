package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mark Nolan
 *
 */
public class ChannelDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4567211941610864326L;
	
	/**
	 * Indicates if sensors channel is enabled.
	 */
	public boolean mIsEnabled = false;
	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader = 0;
	public String mLabel = "";
	public Integer[] mSensorMapKeysRequired = null;
	public Integer[] mSensorMapKeysConflicting = null;
	public boolean mIntExpBoardPowerRequired = false;
	
	public List<HwFwExpBrdVersionDetails> mCompatibleVersionInfo = null;  

	//Needed for Shimmer2?
	public ChannelDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label) {
		mIsEnabled = isChannelEnabled;
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		mLabel = label;
		mIntExpBoardPowerRequired = false;
		
		mCompatibleVersionInfo = null;
	}

	public ChannelDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label, boolean intExpBoardPowerRequired) {
		mIsEnabled = isChannelEnabled;
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		mLabel = label;
		mIntExpBoardPowerRequired = intExpBoardPowerRequired;
		
		mCompatibleVersionInfo = null;
	}

	public ChannelDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label, boolean intExpBoardPowerRequired, List<HwFwExpBrdVersionDetails> compatibleVersionInfo) {
		mIsEnabled = isChannelEnabled;
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		mLabel = label;
		mIntExpBoardPowerRequired = intExpBoardPowerRequired;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}
	
	public void setEnabledState(boolean state) {
		mIsEnabled = state;
	}
	

}
