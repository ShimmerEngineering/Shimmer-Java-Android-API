package com.shimmerresearch.sensor;

import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;

public class ShimmerECGSensor extends AbstractSensor{

	public ShimmerECGSensor(int hardwareID, int firmwareType) {
		super(hardwareID, firmwareType);
		// TODO Auto-generated constructor stub
		if (mFirmwareType == FW_ID.SHIMMER_GQ.GQ_802154){
			
		}
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

	
	
}
