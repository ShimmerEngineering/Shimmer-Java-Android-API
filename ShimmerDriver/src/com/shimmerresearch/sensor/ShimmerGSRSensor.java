package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.HashMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerObject;

public class ShimmerGSRSensor extends AbstractSensor implements Serializable{
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1773291747371088953L;

	public ShimmerGSRSensor(ShimmerVerObject svo) {
		super(svo);
		
	}

	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return null;
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
		ActionSetting actionSetting = new ActionSetting(comType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
				if (comType == COMMUNICATION_TYPE.BLUETOOTH){
					
				} else if (comType == COMMUNICATION_TYPE.DOCK){
					
				}
			break;
		}
		return actionSetting;
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
		if (svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SHIMMER3.BTSTREAM ||
				svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SHIMMER3.SDLOG)
				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGSRRange, 
												Configuration.Shimmer3.ListofGSRRangeConfigValues, 
												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
												CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
				
		return mConfigOptionsMap;
	}

	@Override
	public Object processData(byte[] rawData, int FWType, int sensorFWID) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
