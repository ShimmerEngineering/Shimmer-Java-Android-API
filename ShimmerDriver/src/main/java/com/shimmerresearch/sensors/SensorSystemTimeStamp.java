package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor.DatabaseChannelHandlesCommon;
import com.shimmerresearch.sensors.SensorShimmerClock.GuiLabelSensors;

//TODO I've only started on this class. I need to extract any old code from ShimmerClock and put it in here
/**
 * This class is currently only able to be used for devices that don't sent a
 * timestamp per packet. ShimmerClock has the code needed to handle offsets in
 * the clock value between multiple units - the code of which needs to be moved
 * here.
 * 
 * @author Mark Nolan
 *
 */
public class SensorSystemTimeStamp extends AbstractSensor {

	/** * */
	private static final long serialVersionUID = 8974371709657275355L;

	public boolean doesDeviceSendATimestamp = false; 

	//--------- Sensor specific variables start --------------
	public static class ObjectClusterSensorName{
		public static final String SYSTEM_TIMESTAMP = "System_Timestamp";
		public static final String SYSTEM_TIMESTAMP_PLOT = "System_Timestamp_Plot";
		public static final String SYSTEM_TIMESTAMP_DIFFERENCE = "System_Timestamp_Difference";
		public static final String SYSTEM_TIMESTAMP_PLOT_ZEROED = "System_Timestamp_Plot_Zeroed";
	}
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorSystemTimeStampRef = new SensorDetailsRef(
			GuiLabelSensors.SYSTEM_TIMESTAMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
					SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE,
					SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT_ZEROED
					));
	{
		sensorSystemTimeStampRef.mIsApiSensor = true;
	}

    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP, SensorSystemTimeStamp.sensorSystemTimeStampRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelSystemTimestamp = new ChannelDetails(
		ObjectClusterSensorName.SYSTEM_TIMESTAMP,
		ObjectClusterSensorName.SYSTEM_TIMESTAMP,
		DatabaseChannelHandlesCommon.TIMESTAMP_SYSTEM,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, true);
	{
		channelSystemTimestamp.mChannelSource = CHANNEL_SOURCE.API;
		channelSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
	public static final ChannelDetails channelSystemTimestampPlot = new ChannelDetails(
		ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
		ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
		DatabaseChannelHandlesCommon.NONE,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		channelSystemTimestampPlot.mChannelSource = CHANNEL_SOURCE.API;
		//channelSystemTimestampPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
	public static final ChannelDetails channelSystemTimestampPlotZeroed = new ChannelDetails(
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT_ZEROED,
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT_ZEROED,
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestampPlotZeroed.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestampPlotZeroed.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

	public static final ChannelDetails channelSystemTimestampDiff = new ChannelDetails(
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE,
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE,
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestampDiff.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(ObjectClusterSensorName.SYSTEM_TIMESTAMP, channelSystemTimestamp);
		aMap.put(ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, channelSystemTimestampPlot);
		//TODO why is below not enabled 
//		aMap.put(ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE, channelSystemTimestampDiff);
		aMap.put(ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT_ZEROED, channelSystemTimestampPlotZeroed);
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
//				objectCluster.mSystemTimeStampBytes = UtilShimmer.convertLongToByteArray(pcTimestamp);
				objectCluster.setSystemTimeStamp(pcTimestampMs);
				objectCluster.addCalData(channelDetails, pcTimestampMs);
				objectCluster.incrementIndexKeeper();
			}
			else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
				if(doesDeviceSendATimestamp) {
					//TODO move from ShimmerClock
				} else {
					objectCluster.addCalData(channelDetails, pcTimestampMs);
					objectCluster.incrementIndexKeeper();
				}
			}
		}
		
		return objectCluster;
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
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
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void parseConfigMap(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	//--------- Optional methods to override in Sensor Class start --------
	//--------- Optional methods to override in Sensor Class end --------


}
