package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensor.AbstractSensor.SENSORS;

public class SensorBMP180 extends AbstractSensor implements Serializable {

	/**
	 * 
	 */
	// Needs to be set
	private static final long serialVersionUID = 1L; 
	
	
	public double pressTempAC1 = 408;
	public double pressTempAC2 = -72;
	public double pressTempAC3 = -14383;
	public double pressTempAC4 = 332741;
	public double pressTempAC5 = 32757;
	public double pressTempAC6 = 23153;
	public double pressTempB1 = 6190;
	public double pressTempB2 = 4;
	public double pressTempMB = -32767;
	public double pressTempMC = -8711;
	public double pressTempMD = 2868;
	
	protected byte[] mPressureCalRawParams = new byte[23];
	protected byte[] mPressureRawParams  = new byte[23];
	
	public int mPressureResolution = 0;
	
	public SensorBMP180(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.BMP180.toString();
		mGuiFriendlyLabel = Shimmer3.GuiLabelSensors.BMP_180;
		 mIntExpBoardPowerRequired = false; 
	}

	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,COMMUNICATION_TYPE commType) {
		
		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
		 		break;
		}
		return actionSetting;
	}

	
	@Override
	public ObjectCluster processData(byte[] rawData,COMMUNICATION_TYPE commType, ObjectCluster object) {
		int index = 0;
		for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(commType).values()){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			object = processShimmerChannelData(rawData, channelDetails, object);
		}
		return object;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		Object returnValue = null;
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
				returnValue = valueToSet;
		 		break;
	}
		return returnValue;
	}
	
	public void setPressureResolution(int i){
		mPressureResolution = i;
	}
	
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		
		switch(componentName){
		case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
			returnValue = getPressureResolution();
	 		break;
		  }
		return returnValue;
	}

	public int getPressureResolution(){
		return mPressureResolution;
	}
	
	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		
		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer, ChannelDetails>();
		ChannelDetails channelDetails = null;
		  int index = 0;
				
		  channelDetails = new ChannelDetails(
				Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,
				Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,
				DatabaseChannelHandles.TEMPERATURE,
				CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.MSB,
				CHANNEL_UNITS.DEGREES_CELSUIS,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
		mapOfChannelDetails.put(index++, channelDetails);
					 
		channelDetails = new ChannelDetails(
	  		 Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,
				Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,
				DatabaseChannelHandles.PRESSURE,
				CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
				CHANNEL_UNITS.KPASCAL,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
		mapOfChannelDetails.put(index++, channelDetails);					  


		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.SD, mapOfChannelDetails);
		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.BLUETOOTH, mapOfChannelDetails);

		return mMapOfCommTypetoChannel;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo) {
				mConfigOptionsMap.clear();
		
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION , 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofPressureResolution, 
											Configuration.Shimmer3.ListofPressureResolutionConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180));
			return mConfigOptionsMap;
	
	}

	@Override
	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
		
		return mListOfSensorMapKeysConflicting;
	}

	@Override
	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
		return mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION);
	}

	@Override
	public Map<String, SensorGroupingDetails> generateSensorGroupMapping(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}


}
