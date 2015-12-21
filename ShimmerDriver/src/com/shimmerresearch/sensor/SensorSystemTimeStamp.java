package com.shimmerresearch.sensor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class SensorSystemTimeStamp extends AbstractSensor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8452084854436139765L;

	public SensorSystemTimeStamp(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.SYSTEM_TIMESTAMP.toString();
	}

	
	
	@Override
	public String getSensorName() {
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo) {
		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer,ChannelDetails>();
		//COMMUNICATION_TYPE.IEEE802154
		int count=1;
		ChannelDetails cDSystemTimestop = new ChannelDetails(
						Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
						Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
						DatabaseChannelHandles.TIMESTAMP_SYSTEM,
						CHANNEL_DATA_TYPE.UINT64, 8, CHANNEL_DATA_ENDIAN.MSB,
						CHANNEL_UNITS.MILLISECONDS,
						Arrays.asList(CHANNEL_TYPE.CAL), false, true);
		cDSystemTimestop.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestop.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
		cDSystemTimestop.mIsEnabled = true;
		mapOfChannelDetails.put(1, cDSystemTimestop);
		
		count=2;
		ChannelDetails cDSystemTimestopPlot = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
				Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
				DatabaseChannelHandles.TIMESTAMP_SYSTEM,
				CHANNEL_UNITS.MILLISECONDS,
				Arrays.asList(CHANNEL_TYPE.CAL), false, false);
		cDSystemTimestopPlot.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestopPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
		cDSystemTimestopPlot.mIsEnabled = true;
		mapOfChannelDetails.put(2, cDSystemTimestopPlot);
		
		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.IEEE802154, mapOfChannelDetails);
		
		
		return mMapOfCommTypetoChannel;
	}
	

	@Override
	public Object processData(byte[] sensorByteArray, COMMUNICATION_TYPE comType, ObjectCluster objectCluster) {
		int index = 0;
		
		for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(comType).values()){
			if(channelDetails.mIsEnabled){
				if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
//					if(channelDetails.mChannelSource==CHANNEL_SOURCE.SHIMMER){
					//first process the data originating from the Shimmer sensor
					byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
					System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
					objectCluster = processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
					objectCluster.indexKeeper++;
					index=index+channelDetails.mDefaultNumBytes;
//					}
				}
				else if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
					//TODO: Hack -> just copying from
					double systemTime = 0;
					FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP), CHANNEL_TYPE.CAL.toString());
					if(f!=null){
						systemTime = f.mData;
					}

					objectCluster.mSystemTimeStamp = ByteBuffer.allocate(8).putLong((long) systemTime).array();;
					objectCluster.addCalData(channelDetails, systemTime);
					objectCluster.indexKeeper++;
				}
			}

		}
		
		return objectCluster;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo) {
		return null;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
	}

}
