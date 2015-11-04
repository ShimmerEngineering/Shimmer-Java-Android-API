package com.shimmerresearch.sensor;

import java.util.HashMap;

import com.shimmerresearch.driver.ChannelDetails;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.SensorConfigOptionDetails;
import com.shimmerresearch.driver.ShimmerVerDetails;
import com.shimmerresearch.driver.ShimmerVerObject;

public class ShimmerGSRSensor extends AbstractSensor{
	
	
	
	public ShimmerGSRSensor(ShimmerVerObject svo) {
		super(svo);
		
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
		if (comType == COMMUNICATION_TYPE.BLUETOOTH){
			
		} else if(comType == COMMUNICATION_TYPE.SD){
			
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
		
				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGSRRange, 
												Configuration.Shimmer3.ListofGSRRangeConfigValues, 
												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
												CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
				
		return null;
	}

	@Override
	public Object processData(byte[] rawData, int FWType, int sensorFWID) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
