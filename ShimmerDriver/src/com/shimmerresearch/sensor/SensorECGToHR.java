package com.shimmerresearch.sensor;

import java.io.Serializable;
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
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class SensorECGToHR extends AbstractSensor implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4160314338085066414L;

	public SensorECGToHR(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.ECG_TO_HR.toString();
	}
	
	@Override
	public String getSensorName() {
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectCluster processData(byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		int index = 0;
		for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(commType).values()){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			objectCluster = processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
			objectCluster.indexKeeper++;
			index=index+channelDetails.mDefaultNumBytes;
		}
		return objectCluster;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo) {
		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer,ChannelDetails>();

		int count=1;
		ChannelDetails channelDetails  = new ChannelDetails(
				Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,
				Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,
				DatabaseChannelHandles.ECG_TO_HR,
				CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
				CHANNEL_UNITS.BEATS_PER_MINUTE,
				Arrays.asList(CHANNEL_TYPE.CAL));
		mapOfChannelDetails.put(count,channelDetails);
		channelDetails.mIsEnabled = true;
		channelDetails.mDefaultUnit = CHANNEL_UNITS.BEATS_PER_MINUTE;
		channelDetails.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.IEEE802154, mapOfChannelDetails);
		
		return mMapOfCommTypetoChannel; 
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
		return mListOfConfigOptionKeysAssociated;
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
		return mListOfSensorMapKeysConflicting;
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> generateSensorGroupMapping(ShimmerVerObject svo) {
		return mSensorGroupingMap;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
		
	}



}