package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.List;

/**
 * Used in Consensys to hold Shimmer configuration GUI information for each
 * configuration option to allow for dynamic GUI creation based on compatible
 * HW&FW version checking.
 * 
 * @author Mark Nolan
 */
public class SensorConfigOptionDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8894717489924237791L;

	public static enum GUI_COMPONENT_TYPE {
		COMBOBOX,
		CHECKBOX,
		TEXTFIELD,
		JPANEL
	};
	
	public static final class VALUE_INDEXES {
		public static final class LSM303DLHC_ACCEL_RATE{
			public static final int NOT_LPM = 0;
			public static final int IS_LPM = 1;
		}
		public static final class EXG_RESPIRATION_DETECT_PHASE{
			public static final int PHASE_32KHZ = 0;
			public static final int PHASE_64KHZ = 1;
		}
		public static final class EXG_REFERENCE_ELECTRODE{
			public static final int ECG = 0;
			public static final int EMG = 1;
			public static final int RESP = 2;
			public static final int TEST = 3;
			public static final int CUSTOM = 4;
		}
	}
	
	private String[] mGuiValues;
	private Integer[] mConfigValues;
	
	private String[] mGuiValuesAlt1;
	private Integer[] mConfigValuesAlt1;
	private String[] mGuiValuesAlt2;
	private Integer[] mConfigValuesAlt2;
	private String[] mGuiValuesAlt3;
	private Integer[] mConfigValuesAlt3;
	private String[] mGuiValuesAlt4;
	private Integer[] mConfigValuesAlt4;
	private int mIndexValuesToUse = 0;
	
	
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
	public SensorConfigOptionDetails(String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType) {
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
	public SensorConfigOptionDetails(String[] guiValues, Integer[] configValues, GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
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
	public SensorConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType) {
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
	public SensorConfigOptionDetails(GUI_COMPONENT_TYPE guiComponentType, List<ShimmerVerObject> compatibleVersionInfo) {
		mGuiComponentType = guiComponentType;
		
		mCompatibleVersionInfo = compatibleVersionInfo;
	}

	/**
	 * @return the mGuiValues
	 */
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
		else{
			return mGuiValues;
		}
	}

	/**
	 * @return the mConfigValues
	 */
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
		else{
			return mConfigValues;
		}
	}
	
	/**
	 * @param index
	 */
	public void setIndexOfValuesToUse(int index){
		mIndexValuesToUse = index;
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
	}

}
