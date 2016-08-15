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

@Deprecated
public class SensorSystemTimeStampGq extends AbstractSensor {

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
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SYSTEM_TIMESTAMP, SensorSystemTimeStampGq.sensorSystemTimeStampRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
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
		DatabaseChannelHandles.NONE,
		CHANNEL_UNITS.MILLISECONDS,
		Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		cDSystemTimestampPlot.mChannelSource = CHANNEL_SOURCE.API;
		cDSystemTimestampPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP, SensorSystemTimeStampGq.cDSystemTimestamp);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, SensorSystemTimeStampGq.cDSystemTimestampPlot);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------

	public SensorSystemTimeStampGq(ShimmerVerObject svo) {
		super(SENSORS.SYSTEM_TIMESTAMP, svo);
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
		int index = 0;
		
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
//			if(channelDetails.mIsEnabled){
				if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
//					if(channelDetails.mChannelSource==CHANNEL_SOURCE.SHIMMER){
					//first process the data originating from the Shimmer sensor
					byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
					System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
					objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
					objectCluster.incrementIndexKeeper();
					index=index+channelDetails.mDefaultNumBytes;
//					}
				}
				else if(channelDetails.mObjectClusterName.equals(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
					//TODO: Hack -> just copying from elsewhere (forgotten where exactly)
					double systemTime = 0;
					FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP), CHANNEL_TYPE.CAL.toString());
					if(f!=null){
						systemTime = f.mData;
					}

					objectCluster.mSystemTimeStamp = ByteBuffer.allocate(8).putLong((long) systemTime).array();;
					objectCluster.addCalData(channelDetails, systemTime);
					objectCluster.incrementIndexKeeper();
				}
//			}

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
