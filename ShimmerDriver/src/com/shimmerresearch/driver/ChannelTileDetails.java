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
public class ChannelTileDetails implements Serializable {

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
	public List<Integer> mListOfChannelMapKeysAssociated = null;
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	
	public List<CompatibleVersionDetails> mListOfCompatibleVersionInfo = null;  

	public ChannelTileDetails(List<Integer> listOfChannelMapKeysAssociated) {
		mListOfChannelMapKeysAssociated = listOfChannelMapKeysAssociated;
		mListOfCompatibleVersionInfo = null;
	}

	public ChannelTileDetails(List<Integer> listOfChannelMapKeysAssociated, List<CompatibleVersionDetails> listOfCompatibleVersionInfo) {
		mListOfChannelMapKeysAssociated = listOfChannelMapKeysAssociated;
		mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
	}

}
