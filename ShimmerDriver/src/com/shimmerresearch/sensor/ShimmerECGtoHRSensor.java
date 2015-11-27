package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class ShimmerECGtoHRSensor extends AbstractSensor implements Serializable{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4160314338085066414L;

	public ShimmerECGtoHRSensor(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSOR_NAMES.ECG_TO_HR;

	}
	
	{
	mSensorName ="STMICROLSM303DLHC";
	}
	
	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE comType) {
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
	public Object processData(byte[] rawData, COMMUNICATION_TYPE comType, Object obj) {
		// TODO Auto-generated method stub
		if (comType == COMMUNICATION_TYPE.IEEE802154){
			String[] format = new String[1];
			format[0] = "u8";
			long[] rawValue = parsedData(rawData,format);
			ObjectCluster objectCluster = (ObjectCluster) obj;
			
		}
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

	

}
