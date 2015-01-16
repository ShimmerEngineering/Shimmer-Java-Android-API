package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @param label
 * @param guiValues
 * @param values
 * @param optionType
 *
 * @author Mark Nolan
 */
public class ChannelOptionDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8894717489924237791L;

	public static final int COMBOBOX = 0;
	public static final int CHECKBOX = 1;
	
	public String mLabel = "";
	public String[] mGuiValues;
	public Integer[] mConfigValues;
	public int mOptionType;
	
	public List<HwFwExpBrdVersionDetails> mCompatibleVersionInfo = null;  
	
	/**ComboBox (compatible with all HW, FW and Expansion Boards)
	 * @param label
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public ChannelOptionDetails(String label, String[] guiValues, Integer[] configValues, int optionType) {
		mLabel = label;
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mOptionType = optionType;
		
		mCompatibleVersionInfo = null;
	}
	
	/**ComboBox (with compatible HW, FW, and Expansion Board information)
	 * @param label
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public ChannelOptionDetails(String label, String[] guiValues, Integer[] configValues, int optionType, List<HwFwExpBrdVersionDetails> compatibleVersionInfo) {
		mLabel = label;
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mOptionType = optionType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}

	
	/**CheckBox (compatible with all HW, FW and Expansion Boards)
	 * @param label
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public ChannelOptionDetails(String label, int optionType) {
		mLabel = label;
		mOptionType = optionType;
		
		mCompatibleVersionInfo = null;
	}
	
	/**CheckBox (with compatible HW, FW, and Expansion Board information)
	 * @param label
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public ChannelOptionDetails(String label, int optionType, List<HwFwExpBrdVersionDetails> compatibleVersionInfo) {
		mLabel = label;
		mOptionType = optionType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}
	
	
}
