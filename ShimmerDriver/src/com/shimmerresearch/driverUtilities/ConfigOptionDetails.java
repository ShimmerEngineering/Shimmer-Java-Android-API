package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.List;

public class ConfigOptionDetails implements Serializable {
	
	public static enum GUI_COMPONENT_TYPE {
		COMBOBOX,
		CHECKBOX,
		TEXTFIELD,
		JPANEL
	};
	
	public String[] mGuiValues;
	String mGuiFriendlyName;
	public Integer[] mConfigValues;
	public GUI_COMPONENT_TYPE mGuiComponentType;
	
	public List<ShimmerVerObject> mCompatibleVersionInfo = null;  

	/**
	 * Used in Consensys to hold Shimmer configuration GUI information for
	 * each configuration option to allow for dynamic GUI creation based on
	 * compatible HW&FW version checking.
	 * 
	 * This constructor = CheckBox (with compatible HW, FW, and Expansion Board information)
	 * 
	 * @param guiValues
	 * @param configValues
	 * @param guiComponentType
	 */
	public ConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		mGuiComponentType = guiComponentType;
		mCompatibleVersionInfo = compatibleVersionInfo;
	}

	/**
	 * Used in Consensys to hold Shimmer configuration GUI information for
	 * each configuration option to allow for dynamic GUI creation based on
	 * compatible HW&FW version checking.
	 * 
	 * This constructor = ComboBox (with compatible HW, FW, and Expansion Board information)
	 * 
	 * @param guiValues
	 * @param configValues
	 * @param guiComponentType
	 */
	public ConfigOptionDetails(String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mGuiComponentType = guiComponentType;
		mCompatibleVersionInfo = compatibleVersionInfo;
	}
	
	public ConfigOptionDetails(String[] guiValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo, String guiFriendlyName) {
		mGuiValues = guiValues;
		mGuiComponentType = guiComponentType;
		mCompatibleVersionInfo = compatibleVersionInfo;
		mGuiFriendlyName = guiFriendlyName;
	}
	
	/**
	 * Used in Consensys to hold Shimmer configuration GUI information for
	 * each configuration option to allow for dynamic GUI creation based on
	 * compatible HW&FW version checking.
	 * 
	 * This constructor = CheckBox (compatible with all HW, FW and Expansion Boards)
	 * 
	 * @param guiValues
	 * @param configValues
	 * @param guiComponentType
	 */
	public ConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType) {
		this(guiComponentType, null);
	}

	/**
	 * Used in Consensys to hold Shimmer configuration GUI information for
	 * each configuration option to allow for dynamic GUI creation based on
	 * compatible HW&FW version checking.
	 * 
	 * This constructor = ComboBox (compatible with all HW, FW and Expansion Boards)
	 * 
	 * @param guiValues array of configuration values to show in the GUI
	 * @param configValues bit/bytes values written to the Shimmer corresponding to the shown GUI options.
	 * @param guiComponentType
	 */
	public ConfigOptionDetails(String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType) {
		this(guiValues, configValues, guiComponentType, null);
	}
	
	
	public ConfigOptionDetails(
			String[] guiValues,
			GUI_COMPONENT_TYPE guiComponentType, 
			List<ShimmerVerObject> listSVO) {
		mGuiValues = guiValues;
		mConfigValues = new Integer[guiValues.length];
		mGuiComponentType = guiComponentType;
		for(int i=0;i<mConfigValues.length;i++){
			mConfigValues[i] = i;
		}
		
		if(listSVO!=null){
			mCompatibleVersionInfo = listSVO;
		}
	}

	public String[] getGuiValues() {
		return mGuiValues;
	}
	
	public String getGuiFriendlyName() {
		return mGuiFriendlyName;
	}

	public Integer[] getConfigValues() {
		return mConfigValues;
	}



}
