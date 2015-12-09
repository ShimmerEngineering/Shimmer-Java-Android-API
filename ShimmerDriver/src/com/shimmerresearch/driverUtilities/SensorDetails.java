package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Holds all information related individual sensor channels for dynamic GUI and
 * configuration purposes. Currently used in Consensys only.
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
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader = 0;
	
	public String mLabel = "";
	public List<Integer> mListOfSensorMapKeysRequired = new ArrayList<Integer>();
	public List<Integer> mListOfSensorMapKeysConflicting = new ArrayList<Integer>();
	public boolean mIntExpBoardPowerRequired = false;
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();  

	public List<String> mListOfChannelsRef = new ArrayList<String>();
	
	public boolean mIsDummySensor = false;

//	public LinkedHashMap<String,SensorDetails> mMapOfChildSensorDetails = new LinkedHashMap<String,SensorDetails>();

	//Testing for GQ BLE
	public String mHeaderFileLabel = "";
	public int mHeaderByteMask = 0;
	public int mNumChannels = 0;

	/**
	 * Holds all information related individual sensor channels for dynamic GUI
	 * and configuration purposes. Currently used in Consensys only.
	 * 
	 * This constructor is used for standard Shimmer3 firmware (SDLog,
	 * LogAndStream and BtStream)
	 * 
	 * @param isChannelEnabled
	 * @param sensorBitmapIDStreaming
	 * @param sensorBitmapIDSDLogHeader
	 * @param label
	 */
	public SensorDetails(long sensorBitmapIDStreaming, long sensorBitmapIDSDLogHeader, String label) {
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		mLabel = label;
		mIntExpBoardPowerRequired = false;
		mListOfCompatibleVersionInfo = null;
	}

	/**
	 * Holds all information related individual sensor channels for dynamic GUI
	 * and configuration purposes. Currently used in Consensys only.
	 * 
	 * This constructor is used for Shimmer3 GQ firmware
	 * 
	 * @param isChannelEnabled
	 * @param label
	 */
	public SensorDetails(String label) {
		mLabel = label;
		mIntExpBoardPowerRequired = false;
		mListOfCompatibleVersionInfo = null;
	}

}
