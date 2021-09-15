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
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class SensorECGToHRFw extends AbstractSensor implements Serializable{

	/** * */
	private static final long serialVersionUID = 4160314338085066414L;

	public static final double INVALID_HR_SUBSTITUTE = -1;

	//--------- Sensor specific variables start --------------
	public static class ObjectClusterSensorName{
		public static String ECG_TO_HR_FW_GQ = "ECGtoHR_FW";
	}

	public static class DatabaseChannelHandles{
		public static final String ECG_TO_HR_FW = "ECGToHR";//ObjectClusterSensorName.ECG_TO_HR_FW_GQ;
	}
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorEcgToHrFw = new SensorDetailsRef(
			(0x40 << (8*1)),
			(0x40 << (8*1)), 
			Configuration.Shimmer3.GuiLabelSensors.ECG_TO_HR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoExgEcgGq,
			null,
			null,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR_FW),
			true);
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ECG_TO_HR_FW, sensorEcgToHrFw);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelEcgToHrFw  = new ChannelDetails(
			SensorECGToHRFw.ObjectClusterSensorName.ECG_TO_HR_FW_GQ,
			SensorECGToHRFw.ObjectClusterSensorName.ECG_TO_HR_FW_GQ,
			SensorECGToHRFw.DatabaseChannelHandles.ECG_TO_HR_FW,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.BEATS_PER_MINUTE,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelEcgToHrFw.mDefaultUncalUnit = CHANNEL_UNITS.BEATS_PER_MINUTE;
		channelEcgToHrFw.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR_FW, channelEcgToHrFw);
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
		SensorDetails sensorEcgToHr = mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ECG_TO_HR_FW);
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);

			//Old
//			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
			
			//New
			double parsedChannelData = (double)UtilParseData.parseData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
			//Substitute 255 (i.e., invalid HR from FW) in streamed data parsing for a -1 (which the SW normally gives as invalid HR)
			if(parsedChannelData==255) {
				parsedChannelData = INVALID_HR_SUBSTITUTE;
			}
			objectCluster.addData(channelDetails.mObjectClusterName, channelDetails.mChannelFormatDerivedFromShimmerDataPacket, channelDetails.mDefaultUncalUnit, parsedChannelData);
			
			objectCluster.incrementIndexKeeper();
			index=index+channelDetails.mDefaultNumBytes;
		}
		return objectCluster;
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
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
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}


}
