package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

/**
 * @author Mark Nolan
 *
 */
//TODO add further support for this class
public class ShimmerProperties extends AbstractSensor {
	
	private static final long serialVersionUID = 3069449933266283483L;

	public static class ObjectClusterSensorName{
		public static final String BATT_PERCENTAGE = SensorBattVoltage.ObjectClusterSensorName.BATT_PERCENTAGE;
		public static final String PACKET_RECEPTION_RATE_CURRENT = "Packet_Reception_Rate_Current";
		public static final String PACKET_RECEPTION_RATE_OVERALL = "Packet_Reception_Rate_Trial";
		//RM changed (Aug 2016) from "Event Marker" as the empty space was causing issue with MAT file export (hopefully doesnt cause issue with GQ)
		public static final String EVENT_MARKER = "Event_Marker";

		public static final String RSSI = "RSSI";
		public static final String SENSOR_DISTANCE = "Distance";
	}
	
	public static final ChannelDetails channelRssi = new ChannelDetails(
			ObjectClusterSensorName.RSSI,
			ObjectClusterSensorName.RSSI,
			ObjectClusterSensorName.RSSI,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelRssi.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelSensorDistance = new ChannelDetails(
			ObjectClusterSensorName.SENSOR_DISTANCE,
			ObjectClusterSensorName.SENSOR_DISTANCE,
			ObjectClusterSensorName.SENSOR_DISTANCE,
			CHANNEL_UNITS.MILLIMETER,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelSensorDistance.mChannelSource = CHANNEL_SOURCE.API;
	}
	
	public ShimmerProperties(SENSORS sensorType, ShimmerDevice shimmerDevice) {
		super(sensorType, shimmerDevice);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pctimeStamp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] configBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configByteArrayParse(ShimmerDevice shimmerDevice, byte[] configBytes) {
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
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @param objectCluster
	 * @param rssi
	 * @return
	 */
	public static ObjectCluster calculateRssiChannels(ObjectCluster objectCluster, double rssi) {
		if(Double.isNaN(rssi)){
			rssi = -100.0;
		}

		double txPower = 0.0; //ShimmerGQ tx power is currently set to 0dB

		objectCluster.addCalDataToMap(ShimmerProperties.channelRssi, rssi);
		double distance = UtilShimmer.calculateDistanceFromRssi((long) rssi, txPower);
		objectCluster.addCalDataToMap(ShimmerProperties.channelSensorDistance, distance);
//		consolePrintErrLn("RSSI=" + Double.toString(rssi) + "\tDistance=" + Double.toString(distance));
		return objectCluster;
	}

}
