package com.shimmerresearch.sensor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.ChannelDataEndian;
import com.shimmerresearch.driverUtilities.ChannelDetails.ChannelDataType;

public class ShimmerClock extends AbstractSensor {

	
	
	public ShimmerClock(ShimmerVerObject svo) {
		super(svo);
		// TODO Auto-generated constructor stub
		mSensorName = SENSORS.CLOCK.toString();
		
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
	public Object processData(byte[] sensorByteArray, COMMUNICATION_TYPE comType,
			Object object) {
		int index = 0;
		for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(comType).values()){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			object = processShimmerChannelData(sensorByteArray, channelDetails, object);
			((ObjectCluster)object).indexKeeper++;
			index=index+channelDetails.mDefaultNumBytes;
		}
		
		return object;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer,ChannelDetails>();
		//COMMUNICATION_TYPE.IEEE802154
		int count=1;
		ChannelDetails channelDetails = new ChannelDetails(
				Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
				Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
				DatabaseChannelHandles.TIMESTAMP,
				ChannelDataType.UINT24, 3, ChannelDataEndian.LSB,
				CHANNEL_UNITS.CLOCK_UNIT,
				Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
		channelDetails.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelDetails.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
		channelDetails.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
		channelDetails.mIsEnabled = true;
		mapOfChannelDetails.put(count, channelDetails);
		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.IEEE802154, mapOfChannelDetails);
		
		
		return mMapOfCommTypetoChannel;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(
			ShimmerVerObject svo) {
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


}
