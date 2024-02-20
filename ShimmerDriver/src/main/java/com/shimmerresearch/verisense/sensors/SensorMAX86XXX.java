package com.shimmerresearch.verisense.sensors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.verisense.VerisenseDevice;

public abstract class SensorMAX86XXX extends AbstractSensor {

	private static final long serialVersionUID = 5304298774921285416L;
	
	public static final int MAX_SAMPLES_PER_FIFO = 17;//32;
	public static final int MAX_FIFOS_IN_PAYLOAD_1_CHANNEL = 641;
	public static final int MAX_FIFOS_IN_PAYLOAD_2_CHANNELS = 320;
	public static final int MAX_FIFOS_IN_PAYLOAD_3_CHANNELS = 213;
	public static final int MAX_FIFOS_IN_PAYLOAD_4_CHANNELS = 160;

	public static final double[] LED_RANGE_STEPS_MILLIAMPS = {0.2,0.4,0.6,0.8};

	protected MAX86XXX_ADC_RESOLUTION adcResolution = MAX86XXX_ADC_RESOLUTION.RESOLUTION_15_BIT;
	protected MAX86XXX_PULSE_WIDTH pulseWidth = MAX86XXX_PULSE_WIDTH.PW_400_US;
	protected MAX86XXX_SAMPLE_AVG sampleAverage = MAX86XXX_SAMPLE_AVG.NO_AVERAGING;
	
	protected int ppgLedAmplitudeRedConfigValue = 0;
	protected int ppgLedAmplitudeIrConfigValue = 0;
	protected int ppgLedAmplitudeRangeRed = 0;
	protected int ppgLedAmplitudeRangeIr = 0;

	// --------------- Configuration options start ----------------

	public static enum MAX86XXX_ADC_RESOLUTION implements ISensorConfig {
		RESOLUTION_12_BIT("12-bit", 0, Math.pow(2, 7), 7.8125),
		RESOLUTION_13_BIT("13-bit", 1, Math.pow(2, 6), 15.625),
		RESOLUTION_14_BIT("14-bit", 2, Math.pow(2, 5), 31.25),
		RESOLUTION_15_BIT("15-bit", 3, Math.pow(2, 4), 62.5);
		
		String label;
		Integer configValue;
		double bitShift, lsb;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (MAX86XXX_ADC_RESOLUTION e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, MAX86XXX_ADC_RESOLUTION> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (MAX86XXX_ADC_RESOLUTION e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private MAX86XXX_ADC_RESOLUTION(String label, int configValue, double bitShift, double lsb) {
			this.label = label;
			this.configValue = configValue;
			this.bitShift = bitShift;
			this.lsb = lsb;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static MAX86XXX_ADC_RESOLUTION getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, RESOLUTION_12_BIT.configValue, RESOLUTION_15_BIT.configValue));
		}
	}

	public static enum MAX86XXX_PULSE_WIDTH implements ISensorConfig {
		PW_50_US("50us", 0),
		PW_100_US("100us", 1),
		PW_200_US("200us", 2),
		PW_400_US("400us", 3),;
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (MAX86XXX_PULSE_WIDTH e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, MAX86XXX_PULSE_WIDTH> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (MAX86XXX_PULSE_WIDTH e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private MAX86XXX_PULSE_WIDTH(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static MAX86XXX_PULSE_WIDTH getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, PW_50_US.configValue, PW_400_US.configValue));
		}
	}

	public static enum MAX86XXX_SAMPLE_AVG implements ISensorConfig {
		NO_AVERAGING("No averaging", 0),
		SAMPLES_2("2", 1),
		SAMPLES_4("4", 2),
		SAMPLES_8("8", 3),
		SAMPLES_16("16", 4),
		SAMPLES_32("32", 5);
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (MAX86XXX_SAMPLE_AVG e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, MAX86XXX_SAMPLE_AVG> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (MAX86XXX_SAMPLE_AVG e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private MAX86XXX_SAMPLE_AVG(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static MAX86XXX_SAMPLE_AVG getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, NO_AVERAGING.configValue, SAMPLES_32.configValue));
		}
	}

	public class GuiLabelSensorsCommon {
		public static final String PPG_RED = "PPG Red"; 
		public static final String PPG_IR = "PPG IR"; 
	}

	public class GuiLabelConfigCommonMax86 {
		public static final String MAX86XXX_PPG_RATE = "PPG Rate";
		public static final String MAX86XXX_PPG_ADC_RESOLUTION = "ADC Range";
		public static final String MAX86XXX_PPG_PULSE_WIDTH = "Pulse Width";
		public static final String MAX86XXX_PPG_SMPAVE = "Sample Average";
		public static final String MAX86XXX_PPG_LED_RED_AMPLITUDE = "Red LED Amplitude";
		public static final String MAX86XXX_PPG_LED_IR_AMPLITUDE = "IR LED Amplitude";
	}

	public static class ObjectClusterSensorNameCommon {
		public static String MAX86XXX_PPG_RED = "PPG_Red";
		public static String MAX86XXX_PPG_IR = "PPG_IR";
	}

	public static final class DatabaseConfigHandle{
		public static final String MAX86XXX_RATE = "MAX86XXX_PPG_Rate";
		public static final String MAX86XXX_PPG_ADC_RESOLUTION = "MAX86XXX_PPG_ADC_RANGE";
		public static final String MAX86XXX_PPG_PULSE_WIDTH = "MAX86XXX_PPG_PULSE_WIDTH";
		public static final String MAX86XXX_PPG_SMPAVE = "MAX86XXX_PPG_SAMPLE_AVERAGE";
		public static final String MAX86XXX_PPG_PULSE_AMPLITUDE = "MAX86XXX_PPG_PULSE_AMPLITUDE";
	}
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_PULSE_WIDTH = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_PULSE_WIDTH,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_PPG_PULSE_WIDTH,
			MAX86XXX_PULSE_WIDTH.getLabels(), 
			MAX86XXX_PULSE_WIDTH.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_ADC_RESOLUTION = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_ADC_RESOLUTION,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_PPG_ADC_RESOLUTION,
			MAX86XXX_ADC_RESOLUTION.getLabels(), 
			MAX86XXX_ADC_RESOLUTION.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_SAMPLE_AVG = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_SMPAVE,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_PPG_SMPAVE,
			MAX86XXX_SAMPLE_AVG.getLabels(), 
			MAX86XXX_SAMPLE_AVG.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150);

	// --------------- Configuration options end ----------------
	
	public abstract double getSamplingRateFreq();
	
	public SensorMAX86XXX(SENSORS sensorType, VerisenseDevice verisenseDevice) {
		super(sensorType, verisenseDevice);
		initialise();
	}
	
	@Override
	public void generateConfigOptionsMap() {
		// Overwritten in parent class
		addConfigOption(CONFIG_OPTION_PPG_ADC_RESOLUTION);
		addConfigOption(CONFIG_OPTION_PPG_PULSE_WIDTH);
		addConfigOption(CONFIG_OPTION_PPG_SAMPLE_AVG);
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		
	}

	protected double calibratePpg(double ppgUncal) {
		// Bit shit the channel by the appropriate value
		double ppgCal = ppgUncal / adcResolution.bitShift;
		ppgCal = (ppgCal*adcResolution.lsb);
		//To convert from pA to nA
		ppgCal = ppgCal / 1000;
		return ppgCal;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_ADC_RESOLUTION):
				setPpgAdcResolutionConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_SMPAVE):
				setPpgSampleAverageConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_PULSE_WIDTH):
				setPpgPulseWidthConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_RED_AMPLITUDE):
				setPpgLedAmplitudeRedConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_IR_AMPLITUDE):
				setPpgLedAmplitudeIrConfigValue((int)valueToSet);
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;

		switch(configLabel){
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_ADC_RESOLUTION):
				returnValue = getPpgAdcResolutionConfigValue();
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_SMPAVE):
				returnValue = getPpgSampleAverageConfigValue();
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_PULSE_WIDTH):
				returnValue = getPpgPulseWidthConfigValue();
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_RED_AMPLITUDE):
				returnValue = getPpgLedAmplitudeRedConfigValue();
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_LED_IR_AMPLITUDE):
				returnValue = getPpgLedAmplitudeIrConfigValue();
				break;
			case(GuiLabelConfigCommon.RATE):
				returnValue = getSamplingRateFreq();
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			setPpgAdcResolution(MAX86XXX_ADC_RESOLUTION.RESOLUTION_15_BIT);
			setPpgPulseWidth(MAX86XXX_PULSE_WIDTH.PW_400_US);
			setPpgSampleAverage(MAX86XXX_SAMPLE_AVG.NO_AVERAGING);
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
	
	@Override
	public void initialise() {
		super.initialise();
		
		updateCurrentPpgRedCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		// TODO calib details accel
	}

	public void updateCurrentPpgRedCalibInUse(){
		// TODO add method to update calib values
	}
	
	public void setPpgLedAmplitudeRedConfigValue(int valueToSet) {
		if(valueToSet >= 0 && valueToSet < 256) {
			ppgLedAmplitudeRedConfigValue = valueToSet;
		}
	}

	public int getPpgLedAmplitudeRedConfigValue() {
		return ppgLedAmplitudeRedConfigValue;
	}

	public void setPpgLedAmplitudeIrConfigValue(int valueToSet) {
		if(valueToSet >= 0 && valueToSet < 256) {
			ppgLedAmplitudeIrConfigValue = valueToSet;
		}
	}

	public int getPpgLedAmplitudeIrConfigValue() {
		return ppgLedAmplitudeIrConfigValue;
	}

	public void setPpgPulseWidthConfigValue(int valueToSet) {
		setPpgPulseWidth(MAX86XXX_PULSE_WIDTH.getForConfigValue(valueToSet));
	}

	public void setPpgPulseWidth(MAX86XXX_PULSE_WIDTH pulseWidth) {
		this.pulseWidth = pulseWidth;
	}

	public void setPpgSampleAverageConfigValue(int valueToSet) {
		setPpgSampleAverage(MAX86XXX_SAMPLE_AVG.getForConfigValue(valueToSet));
	}

	public void setPpgSampleAverage(MAX86XXX_SAMPLE_AVG sampleAverage) {
		this.sampleAverage = sampleAverage;
	}

	public void setPpgAdcResolutionConfigValue(int valueToSet) {
		setPpgAdcResolution(MAX86XXX_ADC_RESOLUTION.getForConfigValue(valueToSet));
	}
	
	public int getPpgPulseWidthConfigValue() {
		return getPpgPulseWidth().configValue;
	}
	
	public MAX86XXX_PULSE_WIDTH getPpgPulseWidth() {
		return pulseWidth;
	}

	public int getPpgSampleAverageConfigValue() {
		return getPpgSampleAverage().configValue;
	}

	public MAX86XXX_SAMPLE_AVG getPpgSampleAverage() {
		return sampleAverage;
	}

	public int getPpgAdcResolutionConfigValue() {
		return getPpgAdcResolution().configValue;
	}

	public void setPpgAdcResolution(MAX86XXX_ADC_RESOLUTION valueToSet) {
		adcResolution = valueToSet;
	}

	public MAX86XXX_ADC_RESOLUTION getPpgAdcResolution() {
		return adcResolution;
	}

	public String getPpgLedAmplitudeRedString() {
		return calculatePpgLedAmplitude(ppgLedAmplitudeRedConfigValue, ppgLedAmplitudeRangeRed);
	}

	public String getPpgLedAmplitudeIrString() {
		return calculatePpgLedAmplitude(ppgLedAmplitudeIrConfigValue, ppgLedAmplitudeRangeIr);
	}

	protected String calculatePpgLedAmplitude(int ledAmplitudeConfigValue, int ledRange) {
		return (String.format("%.1f", ledAmplitudeConfigValue*LED_RANGE_STEPS_MILLIAMPS[ledRange]) + " " + CHANNEL_UNITS.MILLIAMPS);
	}

	public double convertRateStringToDouble(String ppgRateString) {
		double ppgRate = 0;
		if(ppgRateString.contains(CHANNEL_UNITS.FREQUENCY)){
			ppgRate = Double.parseDouble(ppgRateString.replace(CHANNEL_UNITS.FREQUENCY, ""));
		}
		return ppgRate;
	}
	
	public byte getPpgLedAmplitudeRangeConfigValue() {
		byte ledRge = 0;
		ledRge |= ((ppgLedAmplitudeRangeIr & 0x03) << 0);
		ledRge |= ((ppgLedAmplitudeRangeRed & 0x03) << 2);
		return ledRge;
	}

	public void setPpgLedAmplitudeRangeConfigValue(byte ledRge) {
		ppgLedAmplitudeRangeIr = (ledRge >> 0) & 0x03;
		ppgLedAmplitudeRangeRed = (ledRge >> 2) & 0x03;
	}
	
	@Override
	public void setSensorConfig(ISensorConfig sensorConfig) {
		if(sensorConfig instanceof MAX86XXX_ADC_RESOLUTION) {
			setPpgAdcResolution((MAX86XXX_ADC_RESOLUTION)sensorConfig);
		} else if(sensorConfig instanceof MAX86XXX_PULSE_WIDTH) {
			setPpgPulseWidth((MAX86XXX_PULSE_WIDTH)sensorConfig);
		} else if(sensorConfig instanceof MAX86XXX_SAMPLE_AVG) {
			setPpgSampleAverage((MAX86XXX_SAMPLE_AVG)sensorConfig);
		} else {
			super.setSensorConfig(sensorConfig);
		}
	}

	@Override
	public List<ISensorConfig> getSensorConfig() {
		List<ISensorConfig> listOfSensorConfig = super.getSensorConfig();
		listOfSensorConfig.add(getPpgAdcResolution());
		listOfSensorConfig.add(getPpgPulseWidth());
		listOfSensorConfig.add(getPpgSampleAverage());
		return listOfSensorConfig;
	}

}
