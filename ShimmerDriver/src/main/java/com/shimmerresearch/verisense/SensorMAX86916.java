package com.shimmerresearch.verisense;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

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
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.verisense.communication.payloads.OperationalConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

public class SensorMAX86916 extends SensorMAX86XXX {

	private static final long serialVersionUID = 519272511737130670L;

	public static final String[] MAX86916_RATES = {"50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz","1000.0Hz","1600.0Hz","3200.0Hz"};
	public static final Integer[] MAX86916_RATE_CONFIG_VALUES = {0,1,2,3,4,5,6,7};

	protected int ppgLedAmplitudeGreenConfigValue = 0;
	protected int ppgLedAmplitudeBlueConfigValue = 0;
	protected int ppgLedAmplitudeRangeGreen = 0;
	protected int ppgLedAmplitudeRangeBlue = 0;

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
			MAX86916_RATES, 
			MAX86916_RATE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86916);

	
	public SensorMAX86916(VerisenseDevice verisenseDevice) {
		super(SENSORS.MAX86916, verisenseDevice);
		rate = MAX86916_RATE_CONFIG_VALUES[3];
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		// get uncalibrated data for each (sub)sensor
		if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_RED)
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_IR)
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.PPG_GREEN)
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.PPG_BLUE)){
			rawData[0] = (byte) (rawData[0]&0x07);
		}
		
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
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
		// TODO Add bit operations for payload config details
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE)) {
			
			ConfigByteLayoutMax86916 configByteLayout = new ConfigByteLayoutMax86916(shimmerDevice, commType);
			
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				
				configBytes[configByteLayout.idxPpgConfig1] |= (getPpgPulseWidthConfigValue()&0x03)<<0;
				configBytes[configByteLayout.idxPpgConfig1] |= (getRateConfigValue()&0x07)<<2;
				configBytes[configByteLayout.idxPpgConfig1] |= (getPpgAdcResolutionConfigValue()&0x03)<<5;
				
				configBytes[configByteLayout.idxPpgConfig2] |= (getPpgSmpAveConfigValue()&0x07)<<5;
				
				if(commType==COMMUNICATION_TYPE.SD) {
					configBytes[configByteLayout.idxLed1Pa] |= (getPpgLedAmplitudeRedConfigValue()&0xFF);
					configBytes[configByteLayout.idxLed2Pa] |= (getPpgLedAmplitudeIrConfigValue()&0xFF);
					configBytes[configByteLayout.idxLed3Pa] |= (getPpgLedAmplitudeGreenConfigValue()&0xFF);
					configBytes[configByteLayout.idxLed4Pa] |= (getPpgLedAmplitudeBlueConfigValue()&0xFF);
					
					if(verisenseDevice.isPayloadDesignV7orAbove()) {
						configBytes[configByteLayout.idxLedRge] |= (getPpgLedAmplitudeRangeConfigValue()&0xFF);
					}
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
				
				setPpgSmpAveConfigValue((configBytes[configByteLayout.idxPpgConfig2]>>5)&0x07);
	
				if(commType==COMMUNICATION_TYPE.SD) {
					setPpgLedAmplitudeIrConfigValue(configBytes[configByteLayout.idxLed1Pa]&0xFF);
					setPpgLedAmplitudeRedConfigValue(configBytes[configByteLayout.idxLed2Pa]&0xFF);
					setPpgLedAmplitudeGreenConfigValue(configBytes[configByteLayout.idxLed3Pa]&0xFF);
					setPpgLedAmplitudeBlueConfigValue(configBytes[configByteLayout.idxLed4Pa]&0xFF);
					
					if(verisenseDevice.isPayloadDesignV7orAbove()) {
						setPpgLedAmplitudeRangeConfigValue((byte) (configBytes[configByteLayout.idxLedRge]&0xFF));
					}
				}
				
			}
		}
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		int ppgRate = 0;
		
		if (samplingRateHz<=50){
			ppgRate = MAX86916_RATE_CONFIG_VALUES[0]; // 50Hz
		} else if (samplingRateHz<=100){
			ppgRate = MAX86916_RATE_CONFIG_VALUES[1]; // 100Hz
		} else if (samplingRateHz<=200){
			ppgRate = MAX86916_RATE_CONFIG_VALUES[2]; // 200Hz
		} else if (samplingRateHz<=400){
			ppgRate = MAX86916_RATE_CONFIG_VALUES[3]; // 400Hz
		} else if (samplingRateHz<=800){
			ppgRate = MAX86916_RATE_CONFIG_VALUES[4]; // 800Hz
		} else if (samplingRateHz<=1000){ 
			ppgRate = MAX86916_RATE_CONFIG_VALUES[5]; // 1000Hz
		} else if (samplingRateHz<=1600){ 
			ppgRate = MAX86916_RATE_CONFIG_VALUES[6]; // 1600Hz
		} else if (samplingRateHz<=3200){ 
			ppgRate = MAX86916_RATE_CONFIG_VALUES[7]; // 3200Hz
		}
		setRateConfigValue(ppgRate);
	}
	
	@Override
	public void setRateConfigValue(int valueToSet) {
		if(ArrayUtils.contains(MAX86916_RATE_CONFIG_VALUES, valueToSet)){
			super.setRateConfigValue(valueToSet);
		}
	}

	@Override
	public double getSamplingRateFreq() {
		String ppgRateString = CONFIG_OPTION_PPG_RATE.getConfigStringFromConfigValue(getRateConfigValue());
		return super.convertRateStringToDouble(ppgRateString);
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

	private class ConfigByteLayoutMax86916 {
		public int idxPpgConfig1 = -1, idxPpgConfig2 = -1, idxLed1Pa = -1, idxLed2Pa = -1, idxLed3Pa = -1, idxLed4Pa = -1, idxLedRge = -1;
		
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
				}
			}
		}
	}

}
