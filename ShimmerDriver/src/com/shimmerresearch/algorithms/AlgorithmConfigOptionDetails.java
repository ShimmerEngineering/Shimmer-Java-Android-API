package com.shimmerresearch.algorithms;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * Used in Consensys to hold Shimmer configuration GUI information for each
 * configuration option to allow for dynamic GUI creation based on compatible
 * HW&FW version checking.
 * 
 * @author Mark Nolan & Jong Chern
 */
public class AlgorithmConfigOptionDetails implements Serializable {

	public String[] mArrayofComboBoxOptions;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1146776013155535579L;

	public static enum GUI_COMPONENT_TYPE {
		COMBOBOX,
		CHECKBOX,
		TEXTFIELD,
		JPANEL
	};
	
	public String[] mGuiValues;
	public Integer[] mConfigValues;
	public GUI_COMPONENT_TYPE mGuiComponentType;
	
	public List<ShimmerVerObject> mCompatibleVersionInfo = null;  
	
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
	public AlgorithmConfigOptionDetails(String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType) {
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mGuiComponentType = guiComponentType;
		
		mCompatibleVersionInfo = null;
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
	public AlgorithmConfigOptionDetails(String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mGuiComponentType = guiComponentType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
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
	public AlgorithmConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType) {
		mGuiComponentType = guiComponentType;
		
		mCompatibleVersionInfo = null;
	}
	
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
	public AlgorithmConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		mGuiComponentType = guiComponentType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}
	
	
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
	public AlgorithmConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo,String[] comboBoxOptions) {
		mGuiComponentType = guiComponentType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
		mArrayofComboBoxOptions = comboBoxOptions;
	}
}