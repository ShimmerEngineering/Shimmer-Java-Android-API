package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class ShimmerClock extends AbstractSensor {

	/** * */
	private static final long serialVersionUID = 4841055784366989272L;

	//--------- Sensor specific variables start --------------
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorShimmerClock = new SensorDetailsRef(
			0, 
			0, 
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);
	//TODO fill out
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelShimmerClock3byte = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock3byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelShimmerClock3byte.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
		channelShimmerClock3byte.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
//	channelDetails.mIsEnabled = true;
	}
	
	public static final ChannelDetails channelShimmerClock2byte = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock2byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelShimmerClock2byte.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
		channelShimmerClock2byte.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
//	channelDetails.mIsEnabled = true;
	}
	//--------- Channel info end --------------

	public ShimmerClock(ShimmerVerObject svo) {
		super(svo);
		// TODO Auto-generated constructor stub
		mSensorName = SENSORS.CLOCK.toString();
	}


	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		mSensorMap.clear();
		
		SensorDetails sensorDetails = new SensorDetails(false, 0, sensorShimmerClock);
		if(svo.getHardwareVersion()>=6){
			sensorDetails.mListOfChannels.add(channelShimmerClock3byte);
		}
		else{
			sensorDetails.mListOfChannels.add(channelShimmerClock2byte);
		}
		
		//TODO handle better (maybe inside the SensorDetails class?) and across all sensors
		if(svo.mFirmwareIdentifier==FW_ID.GQ_802154){
			sensorDetails.mapOfIsEnabledPerCommsType.clear();
			sensorDetails.mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.SD, false);
		}
		
		
		mSensorMap.put(Configuration.Shimmer3.SensorMapKey.TIMESTAMP, sensorDetails);

		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}
	
//	@Override
//	public ObjectCluster processData(byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
//		int index = 0;
//		for (ChannelDetails channelDetails:mMapOfChannelDetails.get(commType).values()){
//			//first process the data originating from the Shimmer sensor
//			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//			objectCluster = processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
//			objectCluster.indexKeeper++;
//			index=index+channelDetails.mDefaultNumBytes;
//		}
//		
//		return objectCluster;
//	}

//	@Override
//	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo) {
//		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer,ChannelDetails>();
//		//COMMUNICATION_TYPE.IEEE802154
//		int count=1;
//		ChannelDetails channelDetails = new ChannelDetails(
//				Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
//				Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
//				DatabaseChannelHandles.TIMESTAMP,
//				CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.LSB,
//				CHANNEL_UNITS.CLOCK_UNIT,
//				Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
//		channelDetails.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//		channelDetails.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
//		channelDetails.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
//		channelDetails.mIsEnabled = true;
//		mapOfChannelDetails.put(count, channelDetails);
//		mMapOfChannelDetails.put(COMMUNICATION_TYPE.IEEE802154, mapOfChannelDetails);
//		
//		
//		return mMapOfChannelDetails;
//	}


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
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
	}


//	@Override
//	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}



}
