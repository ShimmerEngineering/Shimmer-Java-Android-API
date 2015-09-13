package com.shimmerresearch.sensor;


import java.util.HashMap;

import com.shimmerresearch.driver.SensorConfigOptionDetails;

public class KionixKXRB52042 extends AbstractSensor{

	public KionixKXRB52042(int hardwareID, int firmwareType) {
		super(hardwareID, firmwareType);
		// TODO Auto-generated constructor stub
	}

	{
		mSensorName = "KionixKXRB52042";
	}
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object processData(byte[] rawData, int FWType, int sensorFWID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> getConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateChannelDetailsMap(int firmwateType, int hardwareID) {
		// TODO Auto-generated method stub
		
	}

	



	
}
