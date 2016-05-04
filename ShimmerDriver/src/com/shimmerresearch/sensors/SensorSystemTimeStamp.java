package com.shimmerresearch.sensors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class SensorSystemTimeStamp extends AbstractSensor {

	/** * */
	private static final long serialVersionUID = 8452084854436139765L;

	//--------- Sensor specific variables start --------------
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	//TODO check below
	public static final SensorDetailsRef sensorSystemTimeStampRef = new SensorDetailsRef(
			0, 
			0, 
			Shimmer3.GuiLabelSensors.GSR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			null,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT),
			false);

	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails cDSystemTimestamp = new ChannelDetails(
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
		DatabaseChannelHandles.TIMESTAMP_SYSTEM,
		CHANNEL_DATA_TYPE.UINT64, 8, CHANNEL_DATA_ENDIAN.MSB,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, true);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		cDSystemTimestamp.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
	public static final ChannelDetails cDSystemTimestampPlot = new ChannelDetails(
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
		DatabaseChannelHandles.TIMESTAMP_SYSTEM,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		cDSystemTimestampPlot.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestampPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	//--------- Channel info end --------------

	public SensorSystemTimeStamp(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.SYSTEM_TIMESTAMP.toString();
	}

	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		mSensorMap.clear();
		
		//TODO load channels based on list of channels in the SensorDetailsRef rather then manually loading them here -> need to create a ChannelMapRef like in Configuration.Shimmer3 and then cycle through
		SensorDetails sensorDetails = new SensorDetails(false, 0, sensorSystemTimeStampRef);
		sensorDetails.mListOfChannels.add(cDSystemTimestamp);
		sensorDetails.mListOfChannels.add(cDSystemTimestampPlot);
		mSensorMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SYSTEM_TIMESTAMP, sensorDetails);
	}
	
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
	}


	//TODO: include somewhere (SensorDetails/ChannelDetails??)
//	@Override
//	public ObjectCluster processData(byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
//		int index = 0;
//		
//		for (ChannelDetails channelDetails:mMapOfChannelDetails.get(commType).values()){
////			if(channelDetails.mIsEnabled){
//				if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
////					if(channelDetails.mChannelSource==CHANNEL_SOURCE.SHIMMER){
//					//first process the data originating from the Shimmer sensor
//					byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//					System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//					objectCluster = processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
//					objectCluster.indexKeeper++;
//					index=index+channelDetails.mDefaultNumBytes;
////					}
//				}
//				else if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
//					//TODO: Hack -> just copying from elsewhere (forgotten where exactly)
//					double systemTime = 0;
//					FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP), CHANNEL_TYPE.CAL.toString());
//					if(f!=null){
//						systemTime = f.mData;
//					}
//
//					objectCluster.mSystemTimeStamp = ByteBuffer.allocate(8).putLong((long) systemTime).array();;
//					objectCluster.addCalData(channelDetails, systemTime);
//					objectCluster.indexKeeper++;
//				}
////			}
//
//		}
//		
//		return objectCluster;
//	}


	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
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
	public boolean setDefaultConfiguration(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			return true;
		}
		return false;
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


}
