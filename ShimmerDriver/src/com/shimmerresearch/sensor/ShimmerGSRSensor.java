package com.shimmerresearch.sensor;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerVerDetails;

public class ShimmerGSRSensor extends AbstractSensor{
	
	public ShimmerGSRSensor(int hardwareID, int firmwareType) {
		super(hardwareID, firmwareType);
		// TODO Auto-generated constructor stub
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
	public Object processData(byte[] rawData, int FWType, int sensorFWID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateChannelDetailsMap(int firmwareType, int hardwareID) {
		// TODO Auto-generated method stub
		if (firmwareType==ShimmerVerDetails.FW_ID.SHIMMER_GQ.GQ_802154){
			
		}
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
