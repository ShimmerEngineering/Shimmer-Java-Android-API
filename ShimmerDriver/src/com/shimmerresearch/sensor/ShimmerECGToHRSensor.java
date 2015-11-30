package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.ChannelDataEndian;
import com.shimmerresearch.driverUtilities.ChannelDetails.ChannelDataType;

public class ShimmerECGToHRSensor extends AbstractSensor implements Serializable{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4160314338085066414L;

	public ShimmerECGToHRSensor(ShimmerVerObject svo) {
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
	public Object processData(byte[] sensorByteArray, COMMUNICATION_TYPE comType, Object object) {

		int index = 0;
		for (ChannelDetails channelDetails:mMapOfComTypetoChannel.get(comType).values()){
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
		ChannelDetails channelDetails  = new ChannelDetails(
				Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,
				Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,
				DatabaseChannelHandles.ECG_TO_HR,
				ChannelDataType.UINT8, 1, ChannelDataEndian.LSB,
				CHANNEL_UNITS.BEATS_PER_MINUTE,
				Arrays.asList(CHANNEL_TYPE.CAL));
		mapOfChannelDetails.put(count,channelDetails);
		channelDetails.mIsEnabled = true;
		channelDetails.mDefaultUnit = CHANNEL_UNITS.BEATS_PER_MINUTE;
		channelDetails.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
		mMapOfComTypetoChannel.put(COMMUNICATION_TYPE.IEEE802154, mapOfChannelDetails);
		
		return mMapOfComTypetoChannel; 
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	

}