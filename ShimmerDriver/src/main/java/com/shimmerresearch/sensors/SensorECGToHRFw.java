package com.shimmerresearch.sensors;

import java.io.Serializable;
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
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class SensorECGToHRFw extends AbstractSensor implements Serializable{

	/** * */
	private static final long serialVersionUID = 4160314338085066414L;

	//--------- Sensor specific variables start --------------
	public static class ObjectClusterSensorName{
		public static String ECG_TO_HR_FW_GQ = "ECGtoHR_FW";
	}

	public static class DatabaseChannelHandles{
		public static final String ECG_TO_HR_FW = ObjectClusterSensorName.ECG_TO_HR_FW_GQ;
	}
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorEcgToHr = new SensorDetailsRef(
			(0x40 << (8*1)),
			(0x40 << (8*1)), 
			Configuration.Shimmer3.GuiLabelSensors.ECG_TO_HR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg,
			null,
			null,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR),
			true);
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_ECG_TO_HR_FW, sensorEcgToHr);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelEcgToHr  = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,
			Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,
			DatabaseChannelHandles.ECG_TO_HR_FW,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.BEATS_PER_MINUTE,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelEcgToHr.mDefaultUncalUnit = CHANNEL_UNITS.BEATS_PER_MINUTE;
		channelEcgToHr.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR, channelEcgToHr);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

	
	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorECGToHRFw(ShimmerVerObject svo) {
		super(SENSORS.ECG_TO_HR, svo);
		initialise();
	}
	
	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		
		//Update the derived sensor bit index
		SensorDetails sensorEcgToHr = mSensorMap.get(Configuration.Shimmer3.SensorMapKey.SHIMMER_ECG_TO_HR_FW);
		if(sensorEcgToHr!=null){
			sensorEcgToHr.mDerivedSensorBitmapID = 0x80 << (8*1);
		}
	}

	@Override
	public void generateSensorGroupMapping() {
		//NOT USED IN THIS CLASS
	}

	@Override
	public void generateConfigOptionsMap() {
		//NOT USED IN THIS CLASS
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
			objectCluster.incrementIndexKeeper();
			index=index+channelDetails.mDefaultNumBytes;
		}
		return objectCluster;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
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
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
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
	public void parseConfigMapFromDb(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
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


}
