package com.shimmerresearch.verisense;

import java.util.Arrays;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.SensorShimmerClock;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants;

public class SensorVerisenseClock extends AbstractSensor {

	private static final long serialVersionUID = -2624896596566484434L;
	
	public static final String CHANNEL_UNITS_TIMESTAMP = "Unix_" + CHANNEL_UNITS.MILLISECONDS;
	public static final String CHANNEL_UNITS_TIMESTAMP_LOCAL = CHANNEL_UNITS_TIMESTAMP + "_plus_local_time_zone_offset";
	
	public static final ChannelDetails channelTimestamp = new ChannelDetails(
			SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP,
			SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP,
			SensorShimmerClock.DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_UNITS_TIMESTAMP,
			Arrays.asList(CHANNEL_TYPE.CAL), false, true);

	public static final ChannelDetails channelTimestampLocal = new ChannelDetails(
			SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP,
			SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP,
			SensorShimmerClock.DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_UNITS_TIMESTAMP_LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL), false, true);

	public SensorVerisenseClock(VerisenseDevice verisenseDevice) {
		super(SENSORS.CLOCK, verisenseDevice);
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

	public ObjectCluster processDataCustom(ObjectCluster objectCluster, double timeStampMs) {
		objectCluster.addCalData(getChannelDetailsForFwVerVersion(mShimmerDevice), timeStampMs);
		objectCluster.setTimeStampMilliSecs(timeStampMs);
		return objectCluster;
	}
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pctimeStamp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
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
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}

	public static double convertRtcMinutesAndTicksToMs(long rtcEndTimeMinutes, long rtcEndTimeTicks) {
		double rtcEndTimeMs = ((rtcEndTimeMinutes*60)+((double)rtcEndTimeTicks/AsmBinaryFileConstants.TICKS_PER_SECOND))*1000;
		return rtcEndTimeMs;
	}

	public static ChannelDetails getChannelDetailsForFwVerVersion(ShimmerDevice shimmerDevice) {
		if(shimmerDevice instanceof VerisenseDevice) {
			return getChannelDetails(((VerisenseDevice)shimmerDevice).isCsvHeaderDesignAzMarkingPoint());
		}
		return null;
	}
	
	public static ChannelDetails getChannelDetails(boolean isLegacyCsvHeaderDesign) {
		if(isLegacyCsvHeaderDesign) {
			return SensorVerisenseClock.channelTimestamp;
		} else {
			return SensorVerisenseClock.channelTimestampLocal;
		}
	}

}
