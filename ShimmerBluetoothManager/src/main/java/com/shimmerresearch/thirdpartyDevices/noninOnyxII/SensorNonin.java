package com.shimmerresearch.thirdpartyDevices.noninOnyxII;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorNonin extends AbstractSensor{

	/** * */
	private static final long serialVersionUID = 5484199933550381154L;

	//--------- Sensor specific variables start --------------
	public static class GuiLabelConfig{
//		public static final String SAMPLING_RATE_DIVIDER_PPG = "PPG Divider";
	}
	public static class LABEL_SENSOR_TILE{
//		public static final String PROTO3_DELUXE_SUPP = "PPG";
	}
	public static class GuiLabelSensors{
		public static String NONIN_ONYX_II = SENSORS.NONIN_ONYX_II.toString();
	}
	public static class ObjectClusterSensorName{
		public static String PACKET_HEADER = "Packet Header";
		public static String HEART_RATE = "Heart Rate";
		public static String SPO2 = "%SpO2";
		public static String PACKET_FOOTER = "Packet Footer";
	}
	//--------- Sensor specific variables end --------------

	
	public static final SensorDetailsRef sensorNoninOynxII = new SensorDetailsRef(
			GuiLabelSensors.NONIN_ONYX_II,
			null,
			Arrays.asList(
					ObjectClusterSensorName.PACKET_HEADER,
					ObjectClusterSensorName.HEART_RATE,
					ObjectClusterSensorName.SPO2,
					ObjectClusterSensorName.PACKET_FOOTER));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.THIRD_PARTY_NONIN, sensorNoninOynxII);
        mSensorMapRef = Collections.unmodifiableMap(aMap);
   }

	public static final ChannelDetails channelHeader = new ChannelDetails(
			ObjectClusterSensorName.PACKET_HEADER,
			ObjectClusterSensorName.PACKET_HEADER,
			ObjectClusterSensorName.PACKET_HEADER,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL),
			false,
			false);
	{
		channelHeader.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

	public static final ChannelDetails channelHeartRate = new ChannelDetails(
			ObjectClusterSensorName.HEART_RATE,
			ObjectClusterSensorName.HEART_RATE,
			ObjectClusterSensorName.HEART_RATE,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.BEATS_PER_MINUTE,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		channelHeartRate.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

	public static final ChannelDetails channelSpO2 = new ChannelDetails(
			ObjectClusterSensorName.SPO2,
			ObjectClusterSensorName.SPO2,
			ObjectClusterSensorName.SPO2,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		channelSpO2.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

	public static final ChannelDetails channelFooter = new ChannelDetails(
			ObjectClusterSensorName.PACKET_FOOTER,
			ObjectClusterSensorName.PACKET_FOOTER,
			ObjectClusterSensorName.PACKET_FOOTER,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL),
			false,
			false);
	{
		channelFooter.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

	public static final Map<String, ChannelDetails> mChannelMapRef;
	static {
		 Map<String, ChannelDetails> aChannelMap = new LinkedHashMap<String, ChannelDetails>();
		aChannelMap.put(ObjectClusterSensorName.PACKET_HEADER, channelHeader);
		aChannelMap.put(ObjectClusterSensorName.HEART_RATE, channelHeartRate);
		aChannelMap.put(ObjectClusterSensorName.SPO2, channelSpO2); 
		aChannelMap.put(ObjectClusterSensorName.PACKET_FOOTER, channelFooter); 
		mChannelMapRef = Collections.unmodifiableMap(aChannelMap);
	}

	
	
	public SensorNonin(ShimmerDevice shimmerDevice) {
		super(SENSORS.NONIN_ONYX_II, shimmerDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pctimestampMs) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
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
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseConfigMap(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}

}
