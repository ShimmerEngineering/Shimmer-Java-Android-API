package com.shimmerresearch.sensor;

import java.util.HashMap;

import com.shimmerresearch.driver.ChannelDetails;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.SensorConfigOptionDetails;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerObject;

public class STMICROLSM303DLHC extends AbstractSensor{

	// list of compatible shimmer hw/fw for sensor not sensor options (see ShimmerVerObject claass)
	
	//list/map of shimmer ver objects  to specify what configoptions should be generated based on hw/fw id
	
	//COMTYPE should have dummy for no action setting
	
	//map infomem to fw, index, value
	
	public STMICROLSM303DLHC(int hardwareID, int firmwareType, int id) {
		
		
		// TODO Auto-generated constructor stub
		//build map here, 
		
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
	
	int mAccelRange;
	
	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSettings(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE comType) {
		// 		Object returnValue = null;
		ActionSetting actionsetting = new ActionSetting();
		/*
		 switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				mAccelRange = ((int)valueToSet);
				//
			if(mFirmwareType==FW_ID.SHIMMER3.BTSTREAM||mFirmwareType==FW_ID.SHIMMER3.LOGANDSTREAM){ //mcommtype
				actionsetting.mActionByteArray = new byte[]{ShimmerObject.SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
				return actionsetting;
			} else if (mFirmwareType==FW_ID.SHIMMER3.SDLOG){
				//compatiblity check and instruction generation
			}
        	break;
		}
		*/
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
	public Object processData(byte[] rawData, int fwType, int fwSensorID) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public HashMap<COMMUNICATION_TYPE, HashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	





}
