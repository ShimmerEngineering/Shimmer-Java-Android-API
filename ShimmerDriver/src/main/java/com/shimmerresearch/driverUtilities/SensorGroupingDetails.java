package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.shimmerresearch.algorithms.AlgorithmDetails;

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
	public List<Integer> mListOfSensorIdsAssociated = new ArrayList<Integer>();
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();  
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	public List<String> mListofGuiConfigNames = new ArrayList<String>();

	//For algorithm grouping
	public List<AlgorithmDetails> mListOfAlgorithmDetails = new ArrayList<AlgorithmDetails>();
	
	public boolean mIsPermanentGroup = false;

	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param listOfSensorIdsAssociated
	 */
	public SensorGroupingDetails(String groupName, 
			List<Integer> listOfSensorIdsAssociated) {
		mGroupName = groupName;
		mListOfSensorIdsAssociated = listOfSensorIdsAssociated;
	}

	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param listOfSensorIdsAssociated
	 * @param listOfCompatibleVersionInfo
	 */
	public SensorGroupingDetails(String groupName, 
			List<Integer> listOfSensorIdsAssociated, 
			List<ShimmerVerObject> listOfCompatibleVersionInfo) {
		this(groupName, listOfSensorIdsAssociated);
		mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
	}

	
	/**
	 * Holds all information related the sensor 'tiles' used in Consensys for
	 * dynamic GUI and configuration purposes.
	 * 
	 * @param list
	 */
	
	//special case for orientation sensor grouping map 
	public SensorGroupingDetails(String groupName,
			List<AlgorithmDetails> listOfAlgorithmDetails,
			List<String> listOfConfigOptionKeysAssociated,
			Integer doesNothing) {
		mGroupName = groupName;
		
		mListOfAlgorithmDetails = listOfAlgorithmDetails;
		if(listOfConfigOptionKeysAssociated!=null){
			mListOfConfigOptionKeysAssociated = listOfConfigOptionKeysAssociated;
		}
	}

	public SensorGroupingDetails(String groupName,
			List<Integer> listOfSensorIdsAssociated,
			List<ShimmerVerObject> listOfCompatibleVersionInfo,
			boolean isConfigurable) {
		this(groupName, listOfSensorIdsAssociated, listOfCompatibleVersionInfo);
		mIsPermanentGroup = isConfigurable;
	}
	
}
