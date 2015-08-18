package com.shimmerresearch.sensor;

import java.util.HashMap;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.SensorConfigOptionDetails;

public class STMICROLSM303DLHC extends AbstractSensor{

	{
		mSensorName ="STMICROLSM303DLHC";
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
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
	public ActionSetting setSettings(String componentName, Object valueToSet) {
		// 		Object returnValue = null;
		ActionSetting as = new ActionSetting();
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				mAccelRange = ((int)valueToSet);
				//
			if(mFirmwareType==Configuration.Shimmer3)
				return as;
        	break;
		}
    
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
	protected void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;

		setLSM303AccelRateFromFreq(mShimmerSamplingRate);
	}
	
	
	@Override
	public Object processData(byte[] rawData, int fwType, int fwSensorID) {
		// TODO Auto-generated method stub
		return null;
	}






}
