package com.shimmerresearch.sensor;

import java.util.HashMap;

import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class ShimmerClock extends AbstractSensor {

	
	
	public ShimmerClock(ShimmerVerObject svo) {
		super(svo);
		// TODO Auto-generated constructor stub
		mSensorName = SENSOR_NAMES.CLOCK;
		
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
	public Object processData(byte[] rawData, COMMUNICATION_TYPE comType,
			Object object) {
		if (comType == COMMUNICATION_TYPE.IEEE802154){
			String[] format = new String[1];
			format[0] = "u24";
			long[] rawValue = parsedData(rawData,format);
			ObjectCluster objectCluster = (ObjectCluster) object;
		} else if (comType == COMMUNICATION_TYPE.SD){
			
		}
		
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
