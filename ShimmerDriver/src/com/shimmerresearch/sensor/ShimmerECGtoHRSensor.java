package com.shimmerresearch.sensor;

import java.util.HashMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class ShimmerECGtoHRSensor extends AbstractSensor {


	
	public ShimmerECGtoHRSensor(ShimmerVerObject svo) {
		super(svo);
		// TODO Auto-generated constructor stub
	}
	
	{
	mSensorName ="STMICROLSM303DLHC";
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
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object processData(byte[] rawData, int FWType, int sensorFWID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, HashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
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

}
