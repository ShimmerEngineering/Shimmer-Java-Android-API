package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class SensorECGToHR extends AbstractSensor implements Serializable{

	/** * */
	private static final long serialVersionUID = 4160314338085066414L;

	//--------- Sensor specific variables start --------------
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorEcgToHr = new SensorDetailsRef(
			0,
			0, 
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
			DatabaseChannelHandles.ECG_TO_HR,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.BEATS_PER_MINUTE,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelEcgToHr.mDefaultUnit = CHANNEL_UNITS.BEATS_PER_MINUTE;
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
	public SensorECGToHR(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.ECG_TO_HR.toString();
	}
	
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		
		//Update the derived sensor bit index
		SensorDetails sensorEcgToHr = mSensorMap.get(Configuration.Shimmer3.SensorMapKey.SHIMMER_ECG_TO_HR_FW);
		if(sensorEcgToHr!=null){
			sensorEcgToHr.mDerivedSensorBitmapID = 0x80 << (8*1);
		}
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			objectCluster = SensorDetails.processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
			objectCluster.indexKeeper++;
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
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
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



}