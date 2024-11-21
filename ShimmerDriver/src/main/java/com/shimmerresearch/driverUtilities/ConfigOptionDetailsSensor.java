package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.List;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails.GUI_COMPONENT_TYPE;

/**
 * Used in Consensys to hold Shimmer configuration GUI information for each
 * configuration option to allow for dynamic GUI creation based on compatible
 * HW&FW version checking.
 * 
 * @author Mark Nolan
 */
public class ConfigOptionDetailsSensor extends ConfigOptionDetails implements Serializable {

	/** * */
	private static final long serialVersionUID = -8894717489924237791L;

	public static final class VALUE_INDEXES {
		public static final class LSM303_ACCEL_RATE{
			public static final int NOT_LPM = 0;
			public static final int IS_LPM = 1;
		}
		public static final class LIS3MDL_MAG_RATE{
			public static final int IS_LP = 0;
			public static final int IS_MP = 1;
			public static final int IS_HP = 2;
			public static final int IS_UP = 3;
    }
		public static final class LIS2DW12_ACCEL_RATE{
			public static final int NOT_LPM = 0;
			public static final int IS_LPM = 1;
		}
		public static final class EXG_RESPIRATION_DETECT_PHASE{
			public static final int PHASE_32KHZ = 0;
			public static final int PHASE_64KHZ = 1;
		}
		/** Used for providing different reference electrode options for individual ExG operation modes */
		public static final class EXG_REFERENCE_ELECTRODE{
			public static final int ECG = 0;
			public static final int EMG = 1;
			public static final int RESP = 2;
			public static final int TEST = 3;
			public static final int CUSTOM = 4;
			public static final int UNIPOLAR = 5;
		}
	}
	
	//TODO improve code below as a list or Map (implemented in the early days of Consensys)
	private String[] mGuiValuesAlt1;
	private Integer[] mConfigValuesAlt1;
	private String[] mGuiValuesAlt2;
	private Integer[] mConfigValuesAlt2;
	private String[] mGuiValuesAlt3;
	private Integer[] mConfigValuesAlt3;
	private String[] mGuiValuesAlt4;
	private Integer[] mConfigValuesAlt4;
	private String[] mGuiValuesAlt5;
	private Integer[] mConfigValuesAlt5;
	private String[] mGuiValuesAlt6;
	private Integer[] mConfigValuesAlt6;
	private int mIndexValuesToUse = 0;
	private COMMUNICATION_TYPE mCommunicationType;
	
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
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, GUI_COMPONENT_TYPE guiComponentType) {
		super(guiFriendlyName, dbHandle, guiComponentType);
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
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		super(guiFriendlyName, dbHandle, guiComponentType, compatibleVersionInfo);
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
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType) {
		super(guiFriendlyName, dbHandle, guiValues, configValues, guiComponentType);
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
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		super(guiFriendlyName, dbHandle, guiValues, configValues, guiComponentType, compatibleVersionInfo);
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
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, COMMUNICATION_TYPE commType) {
		super(guiFriendlyName, dbHandle, guiValues, configValues, guiComponentType);
		mCommunicationType = commType;
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
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo,COMMUNICATION_TYPE commType) {
		super(guiFriendlyName, dbHandle, guiValues, configValues, guiComponentType, compatibleVersionInfo);
		setmCommunicationType(commType);
	}
	
	public ConfigOptionDetailsSensor(String guiFriendlyName, String dbHandle, String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo, List<ConfigOptionObject> configOptionsList) {
		this(guiFriendlyName, dbHandle, guiValues, configValues, guiComponentType, compatibleVersionInfo);
		setConfigOptions(configOptionsList);
	}

	public void setConfigOptions(List<ConfigOptionObject> configOptionsList) {
		for(ConfigOptionObject configOptionObject:configOptionsList){
			setConfigValues(configOptionObject.index, configOptionObject.configValues);
			setGuiValues(configOptionObject.index, configOptionObject.guiValues);
		}
	}

	/**
	 * @return the mGuiValues
	 */
	@Override
	public String[] getGuiValues() {
		if(mIndexValuesToUse==1){
			return mGuiValuesAlt1;
		}
		else if(mIndexValuesToUse==2){
			return mGuiValuesAlt2;
		}
		else if(mIndexValuesToUse==3){
			return mGuiValuesAlt3;
		}
		else if(mIndexValuesToUse==4){
			return mGuiValuesAlt4;
		}
		else if(mIndexValuesToUse==5){
			return mGuiValuesAlt5;
		}
		else if(mIndexValuesToUse==6){
			return mGuiValuesAlt6;
		}
		else{
			return mGuiValues;
		}
	}

	/**
	 * @return the mConfigValues
	 */
	@Override
	public Integer[] getConfigValues() {
		if(mIndexValuesToUse==1){
			return mConfigValuesAlt1;
		}
		else if(mIndexValuesToUse==2){
			return mConfigValuesAlt2;
		}
		else if(mIndexValuesToUse==3){
			return mConfigValuesAlt3;
		}
		else if(mIndexValuesToUse==4){
			return mConfigValuesAlt4;
		}
		else if(mIndexValuesToUse==5){
			return mConfigValuesAlt5;
		}
		else if(mIndexValuesToUse==6){
			return mConfigValuesAlt6;
		}
		else{
			return mConfigValues;
		}
	}
	
	/**
	 * @param index
	 */
	public void setIndexOfValuesToUse(int index){
		this.mIndexValuesToUse = index;
	}
	
	/**
	 * @param mGuiValuesAlt1 the mGuiValuesAlt1 to set
	 */
	public void setGuiValues(int index, String[] guiValues) {
		if(index==0){
			this.mGuiValues = guiValues;
		}
		else if(index==1){
			this.mGuiValuesAlt1 = guiValues; 			
		}
		else if(index==2){
			this.mGuiValuesAlt2 = guiValues; 			
		}
		else if(index==3){
			this.mGuiValuesAlt3 = guiValues; 			
		}
		else if(index==4){
			this.mGuiValuesAlt4 = guiValues; 			
		}
		else if(index==5){
			this.mGuiValuesAlt5 = guiValues; 			
		}
		else if(index==6){
			this.mGuiValuesAlt6 = guiValues; 			
		}
	}

	/**
	 * @param mConfigValuesAlt1 the mConfigValuesAlt1 to set
	 */
	public void setConfigValues(int index, Integer[] configValues) {
		if(index==0){
			this.mConfigValues = configValues;
		}
		else if(index==1){
			this.mConfigValuesAlt1 = configValues; 			
		}
		else if(index==2){
			this.mConfigValuesAlt2 = configValues; 			
		}
		else if(index==3){
			this.mConfigValuesAlt3 = configValues; 			
		}
		else if(index==4){
			this.mConfigValuesAlt4 = configValues; 			
		}
		else if(index==5){
			this.mConfigValuesAlt5 = configValues; 			
		}
		else if(index==6){
			this.mConfigValuesAlt6 = configValues; 			
		}
	}

	public COMMUNICATION_TYPE getmCommunicationType() {
		return mCommunicationType;
	}

	public void setmCommunicationType(COMMUNICATION_TYPE mCommunicationType) {
		this.mCommunicationType = mCommunicationType;
	}


	
}
