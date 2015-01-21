package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mark Nolan
 *
 */
public class ChannelGroupingDetails implements Serializable {

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
	public Integer[] mAssociatedChannels = null;
//	public String[] mAssociatedConfigurationOptions = null;
	
	public List<HwFwExpBrdVersionDetails> mCompatibleVersionInfo = null;  

	public ChannelGroupingDetails(Integer[] associatedChannels) {
		mAssociatedChannels = associatedChannels;
		mCompatibleVersionInfo = null;
	}

	public ChannelGroupingDetails(Integer[] associatedChannels, List<HwFwExpBrdVersionDetails> compatibleVersionInfo) {
		mAssociatedChannels = associatedChannels;
		mCompatibleVersionInfo = compatibleVersionInfo;
	}

}
