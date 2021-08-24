package com.shimmerresearch.verisense;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public abstract class SensorMAX86XXX extends AbstractSensor {

	private static final long serialVersionUID = 5304298774921285416L;
	
	public static final int MAX_SAMPLES_PER_FIFO = 17;//32;
	public static final int MAX_FIFOS_IN_PAYLOAD_1_CHANNEL = 641;
	public static final int MAX_FIFOS_IN_PAYLOAD_2_CHANNELS = 320;
	public static final int MAX_FIFOS_IN_PAYLOAD_3_CHANNELS = 213;
	public static final int MAX_FIFOS_IN_PAYLOAD_4_CHANNELS = 160;

	public static final double[] LED_RANGE_STEPS_MILLIAMPS = {0.2,0.4,0.6,0.8};

	public static final String[] MAX86XXX_ADC_RESOLUTION = {"12-bit","13-bit","14-bit","15-bit"};
	public static final Integer[] MAX86XXX_ADC_RESOLUTION_CONFIG_VALUES = {0,1,2,3};
	public static final double[] MAX86XXX_ADC_LSB = {7.8125,15.625,31.25,62.5};
	public static final double[] MAX86XXX_ADC_BIT_SHIFT = {Math.pow(2, 7),Math.pow(2, 6),Math.pow(2, 5),Math.pow(2, 4)};
	
	public static final String[] MAX86XXX_PULSE_WIDTH = {"50us","100us","200us","400us"};
	public static final Integer[] MAX86XXX_PULSE_WIDTH_CONFIG_VALUES = {0,1,2,3};
	
	public static final String[] MAX86XXX_SAMPLE_AVG = {"No averaging","2","4","8","16","32","32","32"};
	public static final Integer[] MAX86XXX_SAMPLE_AVG_CONFIG_VALUES = {0,1,2,3,4,5,6,7};

	protected int rate = 0;
	protected int adcResolution = MAX86XXX_ADC_RESOLUTION_CONFIG_VALUES[3];
	protected int pulseWidth = MAX86XXX_PULSE_WIDTH_CONFIG_VALUES[0];
	protected int sampleAverage = MAX86XXX_SAMPLE_AVG_CONFIG_VALUES[0];
	protected int ppgLedAmplitudeRedConfigValue = 0;
	protected int ppgLedAmplitudeIrConfigValue = 0;
	protected int ppgLedAmplitudeRangeRed = 0;
	protected int ppgLedAmplitudeRangeIr = 0;

	// --------------- Configuration options start ----------------

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
			MAX86XXX_PULSE_WIDTH, 
			MAX86XXX_PULSE_WIDTH_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_ADC_RESOLUTION = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_ADC_RESOLUTION,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_PPG_ADC_RESOLUTION,
			MAX86XXX_ADC_RESOLUTION, 
			MAX86XXX_ADC_RESOLUTION_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMAX86150);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_PPG_SAMPLE_AVG = new ConfigOptionDetailsSensor (
			SensorMAX86XXX.GuiLabelConfigCommonMax86.MAX86XXX_PPG_SMPAVE,
			SensorMAX86XXX.DatabaseConfigHandle.MAX86XXX_PPG_SMPAVE,
			MAX86XXX_SAMPLE_AVG, 
			MAX86XXX_SAMPLE_AVG_CONFIG_VALUES, 
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
		double ppgCal = ppgUncal / MAX86XXX_ADC_BIT_SHIFT[adcResolution];
		ppgCal = (ppgCal*MAX86XXX_ADC_LSB[adcResolution]);
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
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE):
				setRateConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_ADC_RESOLUTION):
				setPpgAdcResolutionConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_SMPAVE):
				setPpgSmpAveConfigValue((int)valueToSet);
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
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_RATE):
				returnValue = getRateConfigValue();
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_ADC_RESOLUTION):
				returnValue = getPpgAdcResolutionConfigValue();
				break;
			case(GuiLabelConfigCommonMax86.MAX86XXX_PPG_SMPAVE):
				returnValue = getPpgSmpAveConfigValue();
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
	
	public int getRateConfigValue() {
		return rate;
	}

	public void setRateConfigValue(int valueToSet) {
		rate = valueToSet;
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
		if(ArrayUtils.contains(MAX86XXX_PULSE_WIDTH_CONFIG_VALUES, valueToSet)){
			pulseWidth = valueToSet;
		}
	}

	public void setPpgSmpAveConfigValue(int valueToSet) {
		if(ArrayUtils.contains(MAX86XXX_SAMPLE_AVG_CONFIG_VALUES, valueToSet)){
			sampleAverage = valueToSet;
		}
		
	}

	public void setPpgAdcResolutionConfigValue(int valueToSet) {
		if(ArrayUtils.contains(MAX86XXX_ADC_RESOLUTION_CONFIG_VALUES, valueToSet)){
			adcResolution = valueToSet;
		}
	}
	
	public int getPpgPulseWidthConfigValue() {
		return pulseWidth;
	}

	public int getPpgSmpAveConfigValue() {
		return sampleAverage;
	}

	public int getPpgAdcResolutionConfigValue() {
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

}
