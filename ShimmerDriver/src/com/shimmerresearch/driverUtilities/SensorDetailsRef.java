package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Holds all information related individual sensor channels for dynamic GUI and
 * configuration purposes. Normally instantiate as Static.
 * 
 * @author Mark Nolan
 *
 */
public class SensorDetailsRef implements Serializable {

	/** * */
	private static final long serialVersionUID = 4567211941610864326L;
	
	//TODO no real need to have an ID for streaming and a different one for sd header -> remove one
	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader = 0;
	
	public String mGuiFriendlyLabel = "";
	public List<Integer> mListOfSensorMapKeysRequired = null; //needs to be null by default
	public List<Integer> mListOfSensorMapKeysConflicting = null; //needs to be null by default
	public boolean mIntExpBoardPowerRequired = false;
	public List<String> mListOfConfigOptionKeysAssociated = null; //needs to be null by default
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = null; //needs to be null by default  

	public List<String> mListOfChannelsRef = new ArrayList<String>();
	
	public boolean mIsDummySensor = false;

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
	 * @param guiFriendlyLabel
	 */
	public SensorDetailsRef(long sensorBitmapIDStreaming, 
			long sensorBitmapIDSDLogHeader, 
			String guiFriendlyLabel) {
		mSensorBitmapIDStreaming = sensorBitmapIDStreaming;
		mSensorBitmapIDSDLogHeader = sensorBitmapIDSDLogHeader;
		if(guiFriendlyLabel!=null){
			mGuiFriendlyLabel = guiFriendlyLabel;
		}
	}

	/**
	 * Holds all information related individual sensor channels for dynamic GUI
	 * and configuration purposes.
	 * 
	 * @param sensorBitmapIDStreaming
	 * @param sensorBitmapIDSDLogHeader
	 * @param guiFriendlyLabel
	 * @param listOfCompatibleVersionInfo
	 * @param listOfConfigOptionKeysAssociated
	 * @param listOfChannelsRef
	 *
	 */
	public SensorDetailsRef(
			long sensorBitmapIDStreaming, 
			long sensorBitmapIDSDLogHeader, 
			String guiFriendlyLabel, 
			List<ShimmerVerObject> listOfCompatibleVersionInfo, 
			List<String> listOfConfigOptionKeysAssociated, 
			List<String> listOfChannelsRef) {
		this(sensorBitmapIDStreaming, sensorBitmapIDSDLogHeader, guiFriendlyLabel);
		if(listOfCompatibleVersionInfo!=null){
			mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
		}
		if(listOfConfigOptionKeysAssociated!=null){
			mListOfConfigOptionKeysAssociated = listOfConfigOptionKeysAssociated;
		}
		if(listOfChannelsRef!=null){
			mListOfChannelsRef = listOfChannelsRef;
		}
	}

	/**
	 * Holds all information related individual sensor channels for dynamic GUI
	 * and configuration purposes.
	 * @param sensorBitmapIDStreaming
	 * @param sensorBitmapIDSDLogHeader
	 * @param guiFriendlyLabel
	 * @param listOfCompatibleVersionInfo
	 * @param listOfSensorMapKeysConflicting
	 * @param listOfConfigOptionKeysAssociated
	 * @param listOfChannelsRef
	 */
	public SensorDetailsRef(
			long sensorBitmapIDStreaming, 
			long sensorBitmapIDSDLogHeader, 
			String guiFriendlyLabel, 
			List<ShimmerVerObject> listOfCompatibleVersionInfo, 
			List<Integer> listOfSensorMapKeysConflicting, 
			List<String> listOfConfigOptionKeysAssociated, 
			List<String> listOfChannelsRef,
			boolean intExpBoardPowerRequired) {
		this(sensorBitmapIDStreaming, 
				sensorBitmapIDSDLogHeader, 
				guiFriendlyLabel, 
				listOfCompatibleVersionInfo, 
				listOfConfigOptionKeysAssociated, 
				listOfChannelsRef);
		if(listOfSensorMapKeysConflicting!=null){
			mListOfSensorMapKeysConflicting = listOfSensorMapKeysConflicting;
		}
		mIntExpBoardPowerRequired = intExpBoardPowerRequired;
	}


//	/**
//	 * Holds all information related individual sensor channels for dynamic GUI
//	 * and configuration purposes. Currently used in Consensys only.
//	 * 
//	 * This constructor is used for Shimmer3 GQ BLE firmware
//	 * 
//	 * @param isChannelEnabled
//	 * @param label
//	 */
//	public SensorDetails(String label) {
//		mLabel = label;
//		mIntExpBoardPowerRequired = false;
//		mListOfCompatibleVersionInfo = null;
//	}

}
