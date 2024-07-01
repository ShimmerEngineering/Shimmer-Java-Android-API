package com.shimmerresearch.verisense.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense;
import com.shimmerresearch.bluetooth.SystemTimestampPlot;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.SensorShimmerClock;
import com.shimmerresearch.sensors.SensorSystemTimeStamp;
import com.shimmerresearch.sensors.ShimmerStreamingProperties;
import com.shimmerresearch.sensors.SensorShimmerClock.GuiLabelSensors;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants;

public class SensorVerisenseClock extends AbstractSensor {

	private static final long serialVersionUID = -2624896596566484434L;
	
	private SystemTimestampPlot systemTimestampPlot = new SystemTimestampPlot();

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

	public static final SensorDetailsRef sensorVerisenseClock = new SensorDetailsRef (
			GuiLabelSensors.TIMESTAMP,
			Verisense.CompatibilityInfoForMaps.listOfCompatibleVersionInfoVbatt,
			Arrays.asList(SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP));
	{
		sensorVerisenseClock.mIsApiSensor = true; // Even though TIMESTAMP channel is an API channel, there is no enabledSensor bit for it
	}
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		//TODO move to SensorSystemTimeStamp class
//		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP, SensorSystemTimeStamp.sensorSystemTimeStampRef);
		aMap.put(Configuration.Verisense.SENSOR_ID.VERISENSE_TIMESTAMP, SensorVerisenseClock.sensorVerisenseClock);
//		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_SHIMMER_STREAMING_PROPERTIES, SensorShimmerClock.sensorShimmerStreamingProperties);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}

	public SensorVerisenseClock(VerisenseDevice verisenseDevice) {
		super(SENSORS.CLOCK, verisenseDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		Map<String, ChannelDetails> channelMapRef = new LinkedHashMap<String, ChannelDetails>();
		
//		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP, SensorShimmerClock.channelSystemTimestamp);
//		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, SensorShimmerClock.channelSystemTimestampPlot);
//		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE, SensorShimmerClock.channelSystemTimestampDiff);
//		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT_ZEROED, SensorSystemTimeStamp.channelSystemTimestampPlotZeroed);

		channelMapRef.put(SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP, getChannelDetailsForFwVerVersion(mShimmerDevice));
		
//		channelMapRef.put(SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP_DIFFERENCE, SensorShimmerClock.channelShimmerTsDiffernce);
//		channelMapRef.put(SensorShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET, SensorShimmerClock.channelShimmerClockOffset);
//		channelMapRef.put(SensorShimmerClock.ObjectClusterSensorName.REAL_TIME_CLOCK, SensorShimmerClock.channelRealTimeClock);
//		
//		channelMapRef.put(SensorBattVoltage.ObjectClusterSensorName.BATT_PERCENTAGE, SensorShimmerClock.channelBattPercentage);
//		
//		channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT, SensorShimmerClock.channelReceptionRateCurrent);
//		channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL, SensorShimmerClock.channelReceptionRateTrial);
		
//		channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.EVENT_MARKER, SensorShimmerClock.channelEventMarker);

		super.createLocalSensorMapWithCustomParser(mSensorMapRef, channelMapRef);
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pctimeStampMs) {
		if(sensorDetails.isEnabled(commType)){
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.TIMESTAMP)){
				
				objectCluster.addCalData(getChannelDetailsForFwVerVersion(mShimmerDevice), pctimeStampMs);
				objectCluster.setTimeStampMilliSecs(pctimeStampMs);
				
				if(commType!=COMMUNICATION_TYPE.SD) {
					objectCluster = systemTimestampPlot.processSystemTimestampPlot(objectCluster);
				}
				
			}
		}
		return objectCluster;
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

	public SystemTimestampPlot getSystemTimestampPlot() {
		return systemTimestampPlot;
	}

}
