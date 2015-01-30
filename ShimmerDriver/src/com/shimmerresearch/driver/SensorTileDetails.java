package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Mark Nolan
 *
 */
public class SensorTileDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4373658361698230203L;
	/**
	 * Indicates if sensors channel is enabled.
	 */
	public boolean mIsEnabled = false;
	
//	public Integer[] mChannelMapKeysRequired = null;
//	public Integer[] mChannelMapKeysConflicting = null;
//	public boolean mIntExpBoardPowerRequired = false;
	public List<Integer> mListOfSensorMapKeysAssociated = null;
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	
	public List<CompatibleVersionDetails> mListOfCompatibleVersionInfo = null;  

	public SensorTileDetails(List<Integer> listOfChannelMapKeysAssociated) {
		mListOfSensorMapKeysAssociated = listOfChannelMapKeysAssociated;
		mListOfCompatibleVersionInfo = null;
	}

	public SensorTileDetails(List<Integer> listOfChannelMapKeysAssociated, List<CompatibleVersionDetails> listOfCompatibleVersionInfo) {
		mListOfSensorMapKeysAssociated = listOfChannelMapKeysAssociated;
		mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
	}

}
