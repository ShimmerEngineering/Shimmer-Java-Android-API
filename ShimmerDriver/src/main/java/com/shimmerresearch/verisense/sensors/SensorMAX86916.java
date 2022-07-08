package com.shimmerresearch.verisense.sensors;

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
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.payloads.OperationalConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

public class SensorMAX86916 extends SensorMAX86XXX {

	private static final long serialVersionUID = 519272511737130670L;

	public static final int MAX_DAC_VALUE = (int) (Math.pow(2, 5)-1); 
	public static final int MAX_LED_CURRENT_MILLIAMPS = 204; 
	
	private MAX86916_SAMPLE_RATE sampleRate = MAX86916_SAMPLE_RATE.SR_50_0_HZ;
	public static enum MAX86916_SAMPLE_RATE implements ISensorConfig {
		SR_50_0_HZ("50.0Hz", 0, 50.0),
		SR_100_0_HZ("100.0Hz", 1, 100.0),
		SR_200_0_HZ("200.0Hz", 2, 200.0),
		SR_400_0_HZ("400.0Hz", 3, 400.0),
		SR_800_0_HZ("800.0Hz", 4, 800.0),
		SR_1000_0_HZ("1000.0Hz", 5, 1000.0),
		SR_1600_0_HZ("1600.0Hz", 6, 1600.0),
		SR_3200_0_HZ("3200.0Hz", 7, 3200.0);
		
		String label;
		Integer configValue;
		double freqHz;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (MAX86916_SAMPLE_RATE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, MAX86916_SAMPLE_RATE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (MAX86916_SAMPLE_RATE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private MAX86916_SAMPLE_RATE(String label, int configValue, double freqHz) {
			this.label = label;
			this.configValue = configValue;
			this.freqHz = freqHz;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static MAX86916_SAMPLE_RATE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, SR_50_0_HZ.configValue, SR_3200_0_HZ.configValue));
		}
	}

	// Used during SD parsing
	protected int ppgLedAmplitudeGreenConfigValue = 0;
	protected int ppgLedAmplitudeBlueConfigValue = 0;
	protected int ppgLedAmplitudeRangeGreen = 0;
	protected int ppgLedAmplitudeRangeBlue = 0;

	/**
	 * Default LED pulse amplitude in mA
	 */
	protected int ppgDefaultCurrentAllLedsMilliamps = 40;
	/**
	 * Maximum LED Pulse Amplitude for Red and IR LEDs. If this value is lower than
	 * the default PA value set in byte 61 - 'PPG_MA_DEFAULT', this value will be
	 * used as the default. Units are in mA
	 */
	protected int ppgMaxCurrentRedIrLedsMilliamps = MAX_LED_CURRENT_MILLIAMPS;
	/**
	 * Maximum LED Pulse Amplitude for Green and Blue LEDs. If this value is lower
	 * than the default PA value set in byte 61 - 'PPG_MA_DEFAULT', this value will
	 * be used as the default. Units are in mA
	 */
	protected int ppgMaxCurrentGreenBlueLedsMilliamps = MAX_LED_CURRENT_MILLIAMPS;
	/**
	 * Used by the auto-gain driver to set the target average offset of the PPG
	 * channels within the ADC range. Values of 30% and 70% are given as examples in
	 * the Maxim driver code. Units are %.
	 */
	protected int ppgAutoGainControlTargetPercentOfRange = 30;

	/**
	 * LED pilot (i.e., the IR channel) Pulse Amplitude while in proximity detection mode. Units are in mA.
	 */
	protected int ppgProximityDetectionCurrentIrLedMilliamps = 10;

	protected int ppgDac1CrossTalk = 0;
	protected int ppgDac2CrossTalk = 0;
	protected int ppgDac3CrossTalk = 0;
	protected int ppgDac4CrossTalk = 0;

	protected PROX_DETECTION_MODE proximityDetectionMode = PROX_DETECTION_MODE.AUTO_GAIN_ON_PROX_DETECTION_ON_DRIVER;

	public enum PROX_DETECTION_MODE implements ISensorConfig {
			AUTO_GAIN_OFF_PROX_DETECTION_OFF("Auto-gain Off, Proximity Detection Off", 0),
			AUTO_GAIN_ON_PROX_DETECTION_ON_DRIVER("Auto-gain Off, Proximity Detection - Driver Mode", 1),
			AUTO_GAIN_ON_PROX_DETECTION_ON_HYBRID("Auto-gain Off, Proximity detection - Hybrid Mode", 2),
//			AUTO_GAIN_ON_PROX_DETECTION_OFF("Auto-gain On, Proximity detection Off", 3) //Not supported yet
			;
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (PROX_DETECTION_MODE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, PROX_DETECTION_MODE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (PROX_DETECTION_MODE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private PROX_DETECTION_MODE(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static PROX_DETECTION_MODE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, AUTO_GAIN_OFF_PROX_DETECTION_OFF.configValue, AUTO_GAIN_ON_PROX_DETECTION_ON_HYBRID.configValue));
		}
	}
	
	public class GuiLabelSensors {
		public static final String PPG_GREEN = "PPG Green";
		public static final String PPG_BLUE = "PPG Blue";
	}

	public class GuiLabelConfig {
		public static final String MAX86916_PPG_LED_GREEN_AMPLITUDE = "Green LED Amplitude";
		public static final String MAX86916_PPG_LED_BLUE_AMPLITUDE = "Blue LED Amplitude";
	}

	public static class ObjectClusterSensorName {
		public static final String MAX86916_PPG_GREEN = "PPG_Green";
		public static final String MAX86916_PPG_BLUE = "PPG_Blue";
	}
	
	public static class LABEL_SENSOR_TILE{
		public static final String PPG = "PPG";
	}

	public static class DatabaseChannelHandles{
		public static final String MAX86916_PPG_RED = "MAX86916_PPG_Red";
		public static final String MAX86916_PPG_IR = "MAX86916_PPG_IR";
		public static final String MAX86916_PPG_GREEN = "MAX86916_PPG_Green";
		public static final String MAX86916_PPG_BLUE = "MAX86916_PPG_Blue";
	}

  	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENSOR_MAX86916_PPG_RED = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_RED,
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_RED,
			GuiLabelSensorsCommon.PPG_RED,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
					GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_RED_AMPLITUDE),
			Arrays.asList(ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED));

	public static final SensorDetailsRef SENSOR_MAX86916_PPG_IR = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_IR,
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_IR,
			GuiLabelSensorsCommon.PPG_IR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
					GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_IR_AMPLITUDE),
			Arrays.asList(ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR));

	public static final SensorDetailsRef SENSOR_MAX86916_PPG_GREEN = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86916_PPG_GREEN,
			Configuration.Verisense.SensorBitmap.MAX86916_PPG_GREEN,
			GuiLabelSensors.PPG_GREEN,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
					GuiLabelConfig.MAX86916_PPG_LED_GREEN_AMPLITUDE),
			Arrays.asList(ObjectClusterSensorName.MAX86916_PPG_GREEN));

	public static final SensorDetailsRef SENSOR_MAX86916_PPG_BLUE = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86916_PPG_BLUE,
			Configuration.Verisense.SensorBitmap.MAX86916_PPG_BLUE,
			GuiLabelSensors.PPG_BLUE,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
					GuiLabelConfig.MAX86916_PPG_LED_BLUE_AMPLITUDE),
			Arrays.asList(ObjectClusterSensorName.MAX86916_PPG_BLUE));

  	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED, SensorMAX86916.SENSOR_MAX86916_PPG_RED);  
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR, SensorMAX86916.SENSOR_MAX86916_PPG_IR);  
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN, SensorMAX86916.SENSOR_MAX86916_PPG_GREEN);  
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE, SensorMAX86916.SENSOR_MAX86916_PPG_BLUE);  
		SENSOR_MAP_REF = Collections.unmodifiableMap(aMap);
	}

  	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
	
	public static final ChannelDetails CHANNEL_MAX86916_PPG_RED = new ChannelDetails(
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED,
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED,
			DatabaseChannelHandles.MAX86916_PPG_RED,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NANOAMPS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails CHANNEL_MAX86916_PPG_IR = new ChannelDetails(
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR,
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR,
			DatabaseChannelHandles.MAX86916_PPG_IR,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NANOAMPS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails CHANNEL_MAX86916_PPG_GREEN = new ChannelDetails(
			ObjectClusterSensorName.MAX86916_PPG_GREEN,
			ObjectClusterSensorName.MAX86916_PPG_GREEN,
			DatabaseChannelHandles.MAX86916_PPG_GREEN,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NANOAMPS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails CHANNEL_MAX86916_PPG_BLUE = new ChannelDetails(
			ObjectClusterSensorName.MAX86916_PPG_BLUE,
			ObjectClusterSensorName.MAX86916_PPG_BLUE,
			DatabaseChannelHandles.MAX86916_PPG_BLUE,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NANOAMPS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final Map<String, ChannelDetails> CHANNEL_MAP_REF;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorMAX86XXX.ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED, SensorMAX86916.CHANNEL_MAX86916_PPG_RED);
		aMap.put(SensorMAX86XXX.ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR, SensorMAX86916.CHANNEL_MAX86916_PPG_IR);
		aMap.put(SensorMAX86916.ObjectClusterSensorName.MAX86916_PPG_GREEN, SensorMAX86916.CHANNEL_MAX86916_PPG_GREEN);
		aMap.put(SensorMAX86916.ObjectClusterSensorName.MAX86916_PPG_BLUE, SensorMAX86916.CHANNEL_MAX86916_PPG_BLUE);

		CHANNEL_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Channel info end --------------
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_RATE = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_RATE,
			MAX86916_SAMPLE_RATE.getLabels(), 
			MAX86916_SAMPLE_RATE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916);

	
	public SensorMAX86916(VerisenseDevice verisenseDevice) {
		super(SENSORS.MAX86916, verisenseDevice);
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF, CHANNEL_MAP_REF);
	}
	
	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(CONFIG_OPTION_PPG_RATE);
		super.generateConfigOptionsMap();
	}
	
	@Override
	public void generateSensorGroupMapping() {
		
		int groupIndex = Configuration.Shimmer3.LABEL_SENSOR_TILE.MPU_OTHER.ordinal();
		
		if(mShimmerVerObject.isShimmerGenVerisense()){
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					LABEL_SENSOR_TILE.PPG,
					Arrays.asList(
							Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN,
							Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916));
		}
		super.updateSensorGroupingMap();
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		
		// get uncalibrated data for each (sub)sensor
		if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_RED)
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_IR)
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.PPG_GREEN)
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.PPG_BLUE)){
			rawData[0] = (byte) (rawData[0]&0x07);
		}
		
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
		if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_RED)){
			double ppgUncal = objectCluster.getFormatClusterValue(SensorMAX86916.CHANNEL_MAX86916_PPG_RED, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ppgUncal)) {
				objectCluster.addCalData(SensorMAX86916.CHANNEL_MAX86916_PPG_RED, calibratePpg(ppgUncal));
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_IR)){
			double ppgUncal = objectCluster.getFormatClusterValue(SensorMAX86916.CHANNEL_MAX86916_PPG_IR, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ppgUncal)) {
				objectCluster.addCalData(SensorMAX86916.CHANNEL_MAX86916_PPG_IR, calibratePpg(ppgUncal));
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.PPG_GREEN)){
			double ppgUncal = objectCluster.getFormatClusterValue(SensorMAX86916.CHANNEL_MAX86916_PPG_GREEN, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ppgUncal)) {
				objectCluster.addCalData(SensorMAX86916.CHANNEL_MAX86916_PPG_GREEN, calibratePpg(ppgUncal));
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.PPG_BLUE)){
			double ppgUncal = objectCluster.getFormatClusterValue(SensorMAX86916.CHANNEL_MAX86916_PPG_BLUE, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ppgUncal)) {
				objectCluster.addCalData(SensorMAX86916.CHANNEL_MAX86916_PPG_BLUE, calibratePpg(ppgUncal));
			}
		}
		return objectCluster;
	}
	
	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE)) {
			
			ConfigByteLayoutMax86916 configByteLayout = new ConfigByteLayoutMax86916(shimmerDevice, commType);
			
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				
				configBytes[configByteLayout.idxPpgConfig1] &= ~(0x7F);
				configBytes[configByteLayout.idxPpgConfig1] |= (getPpgPulseWidthConfigValue()&0x03)<<0;
				configBytes[configByteLayout.idxPpgConfig1] |= (getSampleRate().configValue&0x07)<<2;
				configBytes[configByteLayout.idxPpgConfig1] |= (getPpgAdcResolutionConfigValue()&0x03)<<5;
				
				configBytes[configByteLayout.idxPpgConfig2] &= ~(0x07<<5);
				configBytes[configByteLayout.idxPpgConfig2] |= (getPpgSampleAverageConfigValue()&0x07)<<5;
				
				if(commType==COMMUNICATION_TYPE.SD) {
					configBytes[configByteLayout.idxLed1Pa] = (byte) (getPpgLedAmplitudeRedConfigValue()&0xFF);
					configBytes[configByteLayout.idxLed2Pa] = (byte) (getPpgLedAmplitudeIrConfigValue()&0xFF);
					configBytes[configByteLayout.idxLed3Pa] = (byte) (getPpgLedAmplitudeGreenConfigValue()&0xFF);
					configBytes[configByteLayout.idxLed4Pa] = (byte) (getPpgLedAmplitudeBlueConfigValue()&0xFF);
					
					if(verisenseDevice.isPayloadDesignV7orAbove()) {
						configBytes[configByteLayout.idxLedRge] = (byte) (getPpgLedAmplitudeRangeConfigValue()&0xFF);
					}
				}
				
				if(configByteLayout.idxProxAgcMode>=0) {
					configBytes[configByteLayout.idxProxAgcMode] &= ~0x03;
					configBytes[configByteLayout.idxProxAgcMode] |= (byte) (proximityDetectionMode.configValue&0x03);
				}
			}
		}
	}
	
	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE)) {
			
			ConfigByteLayoutMax86916 configByteLayout = new ConfigByteLayoutMax86916(shimmerDevice, commType);

			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;

				setPpgPulseWidthConfigValue((configBytes[configByteLayout.idxPpgConfig1]>>0)&0x03);
				setRateConfigValue((configBytes[configByteLayout.idxPpgConfig1]>>2)&0x07);
				setPpgAdcResolutionConfigValue((configBytes[configByteLayout.idxPpgConfig1]>>5)&0x03);
				
				setPpgSampleAverageConfigValue((configBytes[configByteLayout.idxPpgConfig2]>>5)&0x07);
	
				if(commType==COMMUNICATION_TYPE.SD) {
					setPpgLedAmplitudeIrConfigValue(configBytes[configByteLayout.idxLed1Pa]&0xFF);
					setPpgLedAmplitudeRedConfigValue(configBytes[configByteLayout.idxLed2Pa]&0xFF);
					setPpgLedAmplitudeGreenConfigValue(configBytes[configByteLayout.idxLed3Pa]&0xFF);
					setPpgLedAmplitudeBlueConfigValue(configBytes[configByteLayout.idxLed4Pa]&0xFF);
					
					if(verisenseDevice.isPayloadDesignV7orAbove()) {
						setPpgLedAmplitudeRangeConfigValue((byte) (configBytes[configByteLayout.idxLedRge]&0xFF));
					}
				}
				
				if(configByteLayout.idxProxAgcMode>=0) {
					proximityDetectionMode = PROX_DETECTION_MODE.getForConfigValue(configBytes[configByteLayout.idxProxAgcMode]&0x03);
				}
			}
		}
	}
	
	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			super.setDefaultConfigForSensor(sensorId, isSensorEnabled);
			
			//TODO handle isSensorEnabled = true and false
			
			setSampleRate(MAX86916_SAMPLE_RATE.SR_50_0_HZ);
			setPpgDefaultCurrentAllLedsMilliamps(40);
			setPpgMaxCurrentGreenBlueLedsMilliamps(SensorMAX86916.MAX_LED_CURRENT_MILLIAMPS);
			setPpgMaxCurrentRedIrLedsMilliamps(SensorMAX86916.MAX_LED_CURRENT_MILLIAMPS);
			setPpgAutoGainControlTargetPercentOfRange(30);
			setPpgProximityDetectionCurrentIrLedMilliamps(10);
			setPpgDac1CrossTalk(0);
			setPpgDac2CrossTalk(0);
			setPpgDac3CrossTalk(0);
			setPpgDac4CrossTalk(0);
			setProximityDetectionMode(PROX_DETECTION_MODE.AUTO_GAIN_ON_PROX_DETECTION_ON_DRIVER);
			return true;
		}

		return false;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE):
				setRateConfigValue((int)valueToSet);
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabel(sensorId, configLabel, valueToSet);
				break;
		}	
		return returnValue;
	}
	
	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;

		switch(configLabel){
//			case(GuiLabelConfigCommon.RATE):
//				returnValue = getSampleRate().freqHz;
//				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE):
				returnValue = getSampleRate().configValue;
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabel(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		for(MAX86916_SAMPLE_RATE sampleRate:MAX86916_SAMPLE_RATE.values()) {
			if(samplingRateHz<=sampleRate.freqHz) {
				setSampleRate(sampleRate);
			}
		}
	}
	
	public void setRateConfigValue(int valueToSet) {
		setSampleRate(MAX86916_SAMPLE_RATE.getForConfigValue(valueToSet));
	}

	@Override
	public double getSamplingRateFreq() {
		return getSampleRate().freqHz;
	}

	public void setPpgLedAmplitudeGreenConfigValue(int valueToSet) {
		if(valueToSet >= 0 && valueToSet < 256) {
			ppgLedAmplitudeGreenConfigValue = valueToSet;
		}
	}

	public int getPpgLedAmplitudeGreenConfigValue() {
		return ppgLedAmplitudeGreenConfigValue;
	}

	public void setPpgLedAmplitudeBlueConfigValue(int valueToSet) {
		if(valueToSet >= 0 && valueToSet < 256) {
			ppgLedAmplitudeBlueConfigValue = valueToSet;
		}
	}

	public int getPpgLedAmplitudeBlueConfigValue() {
		return ppgLedAmplitudeBlueConfigValue;
	}

	public String getPpgLedAmplitudeGreenString() {
		return calculatePpgLedAmplitude(ppgLedAmplitudeGreenConfigValue, ppgLedAmplitudeRangeGreen);
	}

	public String getPpgLedAmplitudeBlueString() {
		return calculatePpgLedAmplitude(ppgLedAmplitudeBlueConfigValue, ppgLedAmplitudeRangeBlue);
	}
	
	public MAX86916_SAMPLE_RATE getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(MAX86916_SAMPLE_RATE sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getPpgLedAmplitudeRangeGreen() {
		return ppgLedAmplitudeRangeGreen;
	}

	public void setPpgLedAmplitudeRangeGreen(int ppgLedAmplitudeRangeGreen) {
		this.ppgLedAmplitudeRangeGreen = UtilShimmer.nudgeInteger(ppgLedAmplitudeRangeGreen, 0, MAX_LED_CURRENT_MILLIAMPS);
	}

	public int getPpgLedAmplitudeRangeBlue() {
		return ppgLedAmplitudeRangeBlue;
	}

	public void setPpgLedAmplitudeRangeBlue(int ppgLedAmplitudeRangeBlue) {
		this.ppgLedAmplitudeRangeBlue = UtilShimmer.nudgeInteger(ppgLedAmplitudeRangeBlue, 0, MAX_LED_CURRENT_MILLIAMPS);
	}

	public int getPpgDefaultCurrentAllLedsMilliamps() {
		return ppgDefaultCurrentAllLedsMilliamps;
	}

	public void setPpgDefaultCurrentAllLedsMilliamps(int ppgDefaultCurrentAllLedsMilliamps) {
		this.ppgDefaultCurrentAllLedsMilliamps = UtilShimmer.nudgeInteger(ppgDefaultCurrentAllLedsMilliamps, 0, MAX_LED_CURRENT_MILLIAMPS);
	}

	public int getPpgMaxCurrentRedIrLedsMilliamps() {
		return ppgMaxCurrentRedIrLedsMilliamps;
	}

	public void setPpgMaxCurrentRedIrLedsMilliamps(int ppgMaxCurrentRedIrLedsMilliamps) {
		this.ppgMaxCurrentRedIrLedsMilliamps = UtilShimmer.nudgeInteger(ppgMaxCurrentRedIrLedsMilliamps, 0, MAX_LED_CURRENT_MILLIAMPS);
	}

	public int getPpgMaxCurrentGreenBlueLedsMilliamps() {
		return ppgMaxCurrentGreenBlueLedsMilliamps;
	}

	public void setPpgMaxCurrentGreenBlueLedsMilliamps(int ppgMaxCurrentGreenBlueLedsMilliamps) {
		this.ppgMaxCurrentGreenBlueLedsMilliamps = UtilShimmer.nudgeInteger(ppgMaxCurrentGreenBlueLedsMilliamps, 0, MAX_LED_CURRENT_MILLIAMPS);
	}

	public int getPpgAutoGainControlTargetPercentOfRange() {
		return ppgAutoGainControlTargetPercentOfRange;
	}

	public void setPpgAutoGainControlTargetPercentOfRange(int ppgAutoGainControlTargetPercentOfRange) {
		this.ppgAutoGainControlTargetPercentOfRange = UtilShimmer.nudgeInteger(ppgAutoGainControlTargetPercentOfRange, 0, 100);
	}

	public int getPpgProximityDetectionCurrentIrLedMilliamps() {
		return ppgProximityDetectionCurrentIrLedMilliamps;
	}

	public void setPpgProximityDetectionCurrentIrLedMilliamps(int ppgProximityDetectionCurrentIrLedMilliamps) {
		this.ppgProximityDetectionCurrentIrLedMilliamps = UtilShimmer.nudgeInteger(ppgProximityDetectionCurrentIrLedMilliamps, 0, MAX_LED_CURRENT_MILLIAMPS);
	}

	public int getPpgDac1CrossTalk() {
		return ppgDac1CrossTalk;
	}

	public void setPpgDac1CrossTalk(int ppgDac1CrossTalk) {
		this.ppgDac1CrossTalk = UtilShimmer.nudgeInteger(ppgDac1CrossTalk, 0, MAX_DAC_VALUE);
	}

	public int getPpgDac2CrossTalk() {
		return ppgDac2CrossTalk;
	}

	public void setPpgDac2CrossTalk(int ppgDac2CrossTalk) {
		this.ppgDac2CrossTalk = UtilShimmer.nudgeInteger(ppgDac2CrossTalk, 0, MAX_DAC_VALUE);
	}

	public int getPpgDac3CrossTalk() {
		return ppgDac3CrossTalk;
	}

	public void setPpgDac3CrossTalk(int ppgDac3CrossTalk) {
		this.ppgDac3CrossTalk = UtilShimmer.nudgeInteger(ppgDac3CrossTalk, 0, MAX_DAC_VALUE);
	}

	public int getPpgDac4CrossTalk() {
		return ppgDac4CrossTalk;
	}

	public void setPpgDac4CrossTalk(int ppgDac4CrossTalk) {
		this.ppgDac4CrossTalk = UtilShimmer.nudgeInteger(ppgDac4CrossTalk, 0, MAX_DAC_VALUE);
	}

	public PROX_DETECTION_MODE getProximityDetectionMode() {
		return proximityDetectionMode;
	}

	public void setProximityDetectionMode(PROX_DETECTION_MODE proximityDetectionMode) {
		this.proximityDetectionMode = proximityDetectionMode;
	}

	@Override
	public byte getPpgLedAmplitudeRangeConfigValue() {
		byte ledRge = super.getPpgLedAmplitudeRangeConfigValue();
		ledRge |= ((ppgLedAmplitudeRangeGreen & 0x03) << 4);
		ledRge |= ((ppgLedAmplitudeRangeBlue & 0x03) << 6);
		return ledRge;
	}

	@Override
	public void setPpgLedAmplitudeRangeConfigValue(byte ledRge) {
		ppgLedAmplitudeRangeGreen = (ledRge >> 4) & 0x03;
		ppgLedAmplitudeRangeBlue = (ledRge >> 6) & 0x03;
		super.setPpgLedAmplitudeRangeConfigValue(ledRge);
	}

	@Override
	public void setSensorConfig(ISensorConfig sensorConfig) {
		if(sensorConfig instanceof MAX86916_SAMPLE_RATE) {
			setSampleRate((MAX86916_SAMPLE_RATE)sensorConfig);
		} else if(sensorConfig instanceof PROX_DETECTION_MODE) {
			setProximityDetectionMode((PROX_DETECTION_MODE)sensorConfig);
		} else {
			super.setSensorConfig(sensorConfig);
		}
	}

	@Override
	public List<ISensorConfig> getSensorConfig() {
		List<ISensorConfig> listOfSensorConfig = super.getSensorConfig();
		listOfSensorConfig.add(getSampleRate());
		listOfSensorConfig.add(getProximityDetectionMode());
		return listOfSensorConfig;
	}

	private class ConfigByteLayoutMax86916 {
		public int idxPpgConfig1 = -1, idxPpgConfig2 = -1, idxLed1Pa = -1, idxLed2Pa = -1, idxLed3Pa = -1, idxLed4Pa = -1, idxLedRge = -1, idxProxAgcMode = -1;
		
		public ConfigByteLayoutMax86916(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(commType==COMMUNICATION_TYPE.SD) {
					if(verisenseDevice.isPayloadDesignV8orAbove()) {
						idxPpgConfig1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG6;
						idxPpgConfig2 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG7;
						idxLed1Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG8;
						idxLed2Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG9;
						idxLed3Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG10;
						idxLed4Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG11;
						idxLedRge = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG12;
					} else {
						idxPpgConfig1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
						idxPpgConfig2 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
						idxLed1Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4;
						idxLed2Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5;
						idxLed3Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG6;
						idxLed4Pa = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG7;
						idxLedRge = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG8;
					}
				} else {
					idxPpgConfig1 = OP_CONFIG_BYTE_INDEX.PPG_MODE_CONFIG2;
					idxPpgConfig2 = OP_CONFIG_BYTE_INDEX.PPG_FIFO_CONFIG;
					idxProxAgcMode = OP_CONFIG_BYTE_INDEX.PROX_AGC_MODE;
				}
			}
		}
	}

}
