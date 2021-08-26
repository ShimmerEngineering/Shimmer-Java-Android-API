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
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

public class SensorMAX86150 extends SensorMAX86XXX {

	private static final long serialVersionUID = 519272511737130670L;

	public static final String[] MAX86150_RATES = {"10.0Hz","20.0Hz","50.0Hz","84.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz","1000.0Hz","1600.0Hz","3200.0Hz"};
	public static final Integer[] MAX86150_RATE_CONFIG_VALUES = {0,1,2,3,4,5,6,7,8,9,10};

	public class GuiLabelSensors {
		public static final String ECG = "ECG"; 
	}

	public static class ObjectClusterSensorName {
		public static String MAX86150_ECG= "ECG";
	}

	public static class DatabaseChannelHandles{
		public static final String MAX86150_PPG_RED = "MAX86150_PPG_Red";
		public static final String MAX86150_PPG_IR = "MAX86150_PPG_IR";
		public static final String MAX86150_ECG = "MAX86150_ECG";
	}

  	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorMAX86150PpgRed = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_RED,
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_RED,
			GuiLabelSensorsCommon.PPG_RED,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
					GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_RED_AMPLITUDE),
			Arrays.asList(ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED));

	public static final SensorDetailsRef sensorMAX86150PpgIR = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_IR,
			Configuration.Verisense.SensorBitmap.MAX86XXX_PPG_IR,
			GuiLabelSensorsCommon.PPG_IR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
					GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_IR_AMPLITUDE),
			Arrays.asList(ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR));

	public static final SensorDetailsRef sensorMAX86150Ecg = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.MAX86150_ECG,
			Configuration.Verisense.SensorBitmap.MAX86150_ECG,
			GuiLabelSensors.ECG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150,
			Arrays.asList(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE),
			Arrays.asList(ObjectClusterSensorName.MAX86150_ECG));

  	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED, SensorMAX86150.sensorMAX86150PpgRed);  
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR, SensorMAX86150.sensorMAX86150PpgIR);  
		aMap.put(Configuration.Verisense.SENSOR_ID.MAX86150_ECG, SensorMAX86150.sensorMAX86150Ecg);  
		SENSOR_MAP_REF = Collections.unmodifiableMap(aMap);
	}

  	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
	
	public static final ChannelDetails CHANNEL_MAX86150_PPG_RED = new ChannelDetails(
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED,
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED,
			DatabaseChannelHandles.MAX86150_PPG_RED,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NANOAMPS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails CHANNEL_MAX86150_PPG_IR = new ChannelDetails(
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR,
			ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR,
			DatabaseChannelHandles.MAX86150_PPG_IR,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NANOAMPS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails CHANNEL_MAX86150_PPG_ECG = new ChannelDetails(
			ObjectClusterSensorName.MAX86150_ECG,
			ObjectClusterSensorName.MAX86150_ECG,
			DatabaseChannelHandles.MAX86150_ECG,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final Map<String, ChannelDetails> CHANNEL_MAP_REF;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorMAX86XXX.ObjectClusterSensorNameCommon.MAX86XXX_PPG_RED, SensorMAX86150.CHANNEL_MAX86150_PPG_RED);
		aMap.put(SensorMAX86XXX.ObjectClusterSensorNameCommon.MAX86XXX_PPG_IR, SensorMAX86150.CHANNEL_MAX86150_PPG_IR);
		aMap.put(SensorMAX86150.ObjectClusterSensorName.MAX86150_ECG, SensorMAX86150.CHANNEL_MAX86150_PPG_ECG);

		CHANNEL_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Channel info end --------------
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_RATE = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_RATE,
			MAX86150_RATES, 
			MAX86150_RATE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150);

	
	public SensorMAX86150(VerisenseDevice verisenseDevice) {
		super(SENSORS.MAX86150, verisenseDevice);
		rate = MAX86150_RATE_CONFIG_VALUES[3];
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
				|| sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_IR)){
			rawData[0] = (byte) (rawData[0]&0x07);
		}
		
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
		if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_RED)){
			double ppgUncal = objectCluster.getFormatClusterValue(SensorMAX86150.CHANNEL_MAX86150_PPG_RED, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ppgUncal)) {
				objectCluster.addCalData(SensorMAX86150.CHANNEL_MAX86150_PPG_RED, calibratePpg(ppgUncal));
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensorsCommon.PPG_IR)){
			double ppgUncal = objectCluster.getFormatClusterValue(SensorMAX86150.CHANNEL_MAX86150_PPG_IR, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ppgUncal)) {
				objectCluster.addCalData(SensorMAX86150.CHANNEL_MAX86150_PPG_IR, calibratePpg(ppgUncal));
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ECG)){
			double ecgUncal = objectCluster.getFormatClusterValue(SensorMAX86150.CHANNEL_MAX86150_PPG_ECG, CHANNEL_TYPE.UNCAL);
			if(Double.isFinite(ecgUncal)) {
//				double calValueInMillivolt = SensorADC.calibrateAdcValueToMillivolts(objectCluster.mUncalData[index], offset, vRefP, gain, CHANNEL_DATA_TYPE.UINT16);
				
			}
		}
		return objectCluster;
	}
	
	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Add bit operations for payload config details
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED) ||
				isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)) {
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1] |= (getPpgAdcResolutionConfigValue()&0x03)<<6;
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1] |= (getRateConfigValue()&0x0F)<<2;
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1] |= (getPpgPulseWidthConfigValue()&0x03)<<0;
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3] |= (getPpgSmpAveConfigValue()&0x07)<<0;
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4] |= (getPpgLedAmplitudeRedConfigValue()&0xFF);
			
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV5orAbove()) {
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5] |= (getPpgLedAmplitudeIrConfigValue()&0xFF);
				}
			}
		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED) ||
				isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)) {
			
			setPpgPulseWidthConfigValue((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1]>>0)&0x03);
			setRateConfigValue((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1]>>2)&0x0F);
			setPpgAdcResolutionConfigValue((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1]>>6)&0x03);
			
			setPpgSmpAveConfigValue((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3]>>0)&0x07);
			
			setPpgLedAmplitudeRedConfigValue(configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4]&0xFF);
			
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV5orAbove()) {
					setPpgLedAmplitudeIrConfigValue(configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5]&0xFF);
				}
			}
		}
	}
	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		int ppgRate = 0;
		
		if (samplingRateHz<=10){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[0]; // 10Hz
		} else if (samplingRateHz<=20){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[1]; // 20Hz
		} else if (samplingRateHz<=50){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[2]; // 50Hz
		} else if (samplingRateHz<=84){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[3]; // 84Hz
		} else if (samplingRateHz<=100){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[4]; // 100Hz
		} else if (samplingRateHz<=200){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[5]; // 200Hz
		} else if (samplingRateHz<=400){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[6]; // 400Hz
		} else if (samplingRateHz<=800){
			ppgRate = MAX86150_RATE_CONFIG_VALUES[7]; // 800Hz
		} else if (samplingRateHz<=1000){ 
			ppgRate = MAX86150_RATE_CONFIG_VALUES[8]; // 1000Hz
		} else if (samplingRateHz<=1600){ 
			ppgRate = MAX86150_RATE_CONFIG_VALUES[9]; // 1600Hz
		} else if (samplingRateHz<=3200){ 
			ppgRate = MAX86150_RATE_CONFIG_VALUES[10]; // 3200Hz
		}
		setRateConfigValue(ppgRate);
	}
	
	@Override
	public void setRateConfigValue(int valueToSet) {
		if(ArrayUtils.contains(MAX86150_RATE_CONFIG_VALUES, valueToSet)){
			super.setRateConfigValue(valueToSet);
		}
	}

	@Override
	public double getSamplingRateFreq() {
		String ppgRateString = CONFIG_OPTION_PPG_RATE.getConfigStringFromConfigValue(getRateConfigValue());
		return super.convertRateStringToDouble(ppgRateString);
	}

}
