package com.shimmerresearch.sensor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;

public class SensorSTMICROLSM303DLHC extends AbstractSensor{

	// list of compatible shimmer hw/fw for sensor not sensor options (see ShimmerVerObject claass)
	
	//list/map of shimmer ver objects  to specify what configoptions should be generated based on hw/fw id
	
	//COMTYPE should have dummy for no action setting
	
	//map infomem to fw, index, value
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2119834127313796684L;

	// --- Configuration variables specific to this Sensor - Start --- 
	int mAccelRange;
	// --- Configuration variables specific to this Sensor - End --- 

	
	public SensorSTMICROLSM303DLHC(ShimmerVerObject svo) {
		super(svo);
	}


	{
		mSensorName ="STMICROLSM303DLHC";
		
		//config options maps should be configured based on fw and hw id
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.BLUETOOTH));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.BLUETOOTH));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.SD));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.SD));
		
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues);
	}
	
	@Override
	public String getSensorName() {
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// 		Object returnValue = null;
		ActionSetting actionsetting = new ActionSetting(commType);
		
		 switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				mAccelRange = ((int)valueToSet);
				//
			if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
				actionsetting.mActionByteArray = new byte[]{ShimmerObject.SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
				return actionsetting;
			} else if (mFirmwareType==FW_ID.SDLOG){
				//compatiblity check and instruction generation
			}
        	break;
		}
		
		return actionsetting;
		
	}

	/**
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 * 
	 * @param enable
	 */
	protected void setLowPowerAccelWR(boolean enable){}
	
	
	@Override
	public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
		
		
		return null;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
		return mListOfConfigOptionKeysAssociated;
		
		
	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
		return mListOfSensorMapKeysConflicting;
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> generateSensorGroupMapping(ShimmerVerObject svo) {
		return mSensorGroupingMap;
		// TODO Auto-generated method stub
		
	}

}
