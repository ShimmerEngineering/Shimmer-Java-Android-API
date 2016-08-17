package com.shimmerresearch.sensors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

public class SensorSystemTimeStamp extends AbstractSensor {

	/** * */
	private static final long serialVersionUID = 8974371709657275355L;


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
			"SystemTimestmp",//TODO define as static somewhere
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			null,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT),
			false);

    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SYSTEM_TIMESTAMP, SensorSystemTimeStamp.sensorSystemTimeStampRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails cDSystemTimestamp = new ChannelDetails(
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
		DatabaseChannelHandlesCommon.TIMESTAMP_SYSTEM,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, true);
	{
		cDSystemTimestamp.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
	public static final ChannelDetails cDSystemTimestampPlot = new ChannelDetails(
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
		Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
		DatabaseChannelHandlesCommon.NONE,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		cDSystemTimestampPlot.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestampPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP, SensorSystemTimeStamp.cDSystemTimestamp);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, SensorSystemTimeStamp.cDSystemTimestampPlot);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------

	public SensorSystemTimeStamp(ShimmerVerObject svo) {
		super(SENSORS.SYSTEM_TIMESTAMP, svo);
		initialise();
	}

	public SensorSystemTimeStamp(ShimmerDevice shimmerDevice) {
		super(SENSORS.SYSTEM_TIMESTAMP, shimmerDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}
	
	@Override
	public void generateSensorGroupMapping() {
		// NOT NEEDED BECAUSE NOT DISPLAYED ON GUI CONFIG PANEL
	}

	@Override
	public void generateConfigOptionsMap() {
		// NOT NEEDED BECAUSE NO CONFIGURATION OPTIONS NEEDED
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
				objectCluster.mSystemTimeStamp = ByteBuffer.allocate(8).putLong(pcTimestamp).array();;
				objectCluster.addCalData(channelDetails, pcTimestamp);
				objectCluster.incrementIndexKeeper();
			}
			else if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
				objectCluster.addCalData(channelDetails, pcTimestamp);
				objectCluster.incrementIndexKeeper();
			}
		}
		
		return objectCluster;
	}


	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
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
	
	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	//--------- Optional methods to override in Sensor Class start --------
	//--------- Optional methods to override in Sensor Class end --------


}
