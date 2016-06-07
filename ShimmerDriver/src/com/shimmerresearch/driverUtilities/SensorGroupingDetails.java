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

	/** *  */
	private static final long serialVersionUID = 4373658361698230203L;
	
	public String mGroupName = "";
	public List<Integer> mListOfSensorMapKeysAssociated = new ArrayList<Integer>();
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();  
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();

	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param listOfSensorMapKeysAssociated
	 */
	public SensorGroupingDetails(String groupName, 
			List<Integer> listOfSensorMapKeysAssociated) {
		mGroupName = groupName;
		mListOfSensorMapKeysAssociated = listOfSensorMapKeysAssociated;
	}

	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param listOfChannelMapKeysAssociated
	 * @param listOfCompatibleVersionInfo
	 */
	public SensorGroupingDetails(String groupName, 
			List<Integer> listOfChannelMapKeysAssociated, 
			List<ShimmerVerObject> listOfCompatibleVersionInfo) {
		this(groupName, listOfChannelMapKeysAssociated);
		mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
	}
	
}
