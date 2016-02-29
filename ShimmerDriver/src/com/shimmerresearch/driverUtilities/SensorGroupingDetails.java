package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds all information related the sensor 'tiles' used in Consensys for
 * dynamic GUI and configuration purposes.
 * 
 * @author Mark Nolan
 *
 */
public class SensorGroupingDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4373658361698230203L;
	/**
	 * Indicates if sensors channel is enabled.
	 */
	public boolean mIsEnabled = false;
	
	public List<Integer> mListOfSensorMapKeysAssociated = new ArrayList<Integer>();
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = null;  

	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param listOfSensorMapKeysAssociated
	 */
	public SensorGroupingDetails(List<Integer> listOfSensorMapKeysAssociated) {
		mListOfSensorMapKeysAssociated = listOfSensorMapKeysAssociated;
		mListOfCompatibleVersionInfo = null;
	}

	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param listOfChannelMapKeysAssociated
	 * @param listOfCompatibleVersionInfo
	 */
	public SensorGroupingDetails(List<Integer> listOfChannelMapKeysAssociated, List<ShimmerVerObject> listOfCompatibleVersionInfo) {
		mListOfSensorMapKeysAssociated = listOfChannelMapKeysAssociated;
		mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
	}
	
}
