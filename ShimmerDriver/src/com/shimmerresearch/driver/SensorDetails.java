package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mark Nolan
 *
 */
public class SensorDetails implements Serializable {

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
	public long mDerivedChannelBitmapBitShift = 0;
	public long mDerivedChannelBitmapMask = 0;
	public String mLabel = "";
	public List<Integer> mListOfSensorMapKeysRequired = null;
	public List<Integer> mListOfSensorMapKeysConflicting = null;
	public boolean mIntExpBoardPowerRequired = false;
	public List<String> mListOfConfigOptionKeysAssociated = null;
	
	public List<CompatibleVersionDetails> mListOfCompatibleVersionInfo = null;  

	public SensorDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label) {
		mIsEnabled = isChannelEnabled;
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		mLabel = label;
		mIntExpBoardPowerRequired = false;
		
		mListOfCompatibleVersionInfo = null;
	}

//	public ChannelDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label, boolean intExpBoardPowerRequired) {
//		mIsEnabled = isChannelEnabled;
//		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
//		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
//		mLabel = label;
//		mIntExpBoardPowerRequired = intExpBoardPowerRequired;
//		
//		mListOfCompatibleVersionInfo = null;
//	}
	
//	public ChannelDetails(boolean isChannelEnabled, long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label, boolean intExpBoardPowerRequired, List<CompatibleVersionDetails> listOfCompatibleVersionInfo) {
//		mIsEnabled = isChannelEnabled;
//		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
//		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
//		mLabel = label;
//		mIntExpBoardPowerRequired = intExpBoardPowerRequired;
//		
//		mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
//	}
	
	public void setEnabledState(boolean state) {
		mIsEnabled = state;
	}
	
	public void setDerivedChannelInfo(long derivedChannelBitmapBitShift,long derivedChannelBitmapMask) {
		mDerivedChannelBitmapBitShift = derivedChannelBitmapBitShift;
		mDerivedChannelBitmapMask = derivedChannelBitmapMask;
	}

}
