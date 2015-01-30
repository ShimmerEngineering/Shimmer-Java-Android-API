package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @param guiValues
 * @param values
 * @param optionType
 *
 * @author Mark Nolan
 */
public class SensorConfigOptionDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8894717489924237791L;

	public static final int COMBOBOX = 0;
	public static final int CHECKBOX = 1;
	public static final int TEXTFIELD = 2;
	
	public String[] mGuiValues;
	public Integer[] mConfigValues;
	public int mOptionType;
	
	public List<CompatibleVersionDetails> mCompatibleVersionInfo = null;  
	
	/**ComboBox (compatible with all HW, FW and Expansion Boards)
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public SensorConfigOptionDetails(String[] guiValues, Integer[] configValues, int optionType) {
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mOptionType = optionType;
		
		mCompatibleVersionInfo = null;
	}
	
	/**ComboBox (with compatible HW, FW, and Expansion Board information)
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public SensorConfigOptionDetails(String[] guiValues, Integer[] configValues, int optionType, List<CompatibleVersionDetails> compatibleVersionInfo) {
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mOptionType = optionType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}

	
	/**CheckBox (compatible with all HW, FW and Expansion Boards)
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public SensorConfigOptionDetails(int optionType) {
		mOptionType = optionType;
		
		mCompatibleVersionInfo = null;
	}
	
	/**CheckBox (with compatible HW, FW, and Expansion Board information)
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public SensorConfigOptionDetails(int optionType, List<CompatibleVersionDetails> compatibleVersionInfo) {
		mOptionType = optionType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}
	
	
}
