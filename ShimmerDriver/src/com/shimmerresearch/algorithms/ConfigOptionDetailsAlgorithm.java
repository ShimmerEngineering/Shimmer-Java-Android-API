package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.List;

import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * Used in Consensys to hold Shimmer configuration GUI information for each
 * configuration option to allow for dynamic GUI creation based on compatible
 * HW&FW version checking.
 * 
 * @author Mark Nolan & Jong Chern
 */
public class ConfigOptionDetailsAlgorithm extends ConfigOptionDetails implements Serializable {

	/** * */
	private static final long serialVersionUID = 1146776013155535579L;

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
	public ConfigOptionDetailsAlgorithm(GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		super(guiComponentType, compatibleVersionInfo);
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
	public ConfigOptionDetailsAlgorithm(GUI_COMPONENT_TYPE guiComponentType) {
		super(guiComponentType, null);
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
	public ConfigOptionDetailsAlgorithm(
			String[] guiValues, 
			Integer[] configValues, 
			GUI_COMPONENT_TYPE guiComponentType, 
			List<ShimmerVerObject> compatibleVersionInfo) {
		super(guiValues, configValues, guiComponentType, compatibleVersionInfo);
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
	public ConfigOptionDetailsAlgorithm(
			String[] guiValues, 
			Integer[] configValues, 
			GUI_COMPONENT_TYPE guiComponentType) {
		super(guiValues, configValues, guiComponentType, null);
	}

	public ConfigOptionDetailsAlgorithm(
			String[] guiValues,
			GUI_COMPONENT_TYPE guiComponentType, 
			List<ShimmerVerObject> compatibleVersionInfo) {
		super(guiValues, guiComponentType, compatibleVersionInfo);
	}


}
