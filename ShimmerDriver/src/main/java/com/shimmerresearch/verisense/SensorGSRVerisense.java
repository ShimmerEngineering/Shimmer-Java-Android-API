package com.shimmerresearch.verisense;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense;
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.ASM_CONFIG_BYTE_INDEX;

public class SensorGSRVerisense extends SensorGSR {

	private static final long serialVersionUID = -3937042079000714506L;

	//--------- Sensor specific variables start --------------
	int bitShiftGSRRange = 5;
	int maskGSRRange = 0x07;
	private double sensorSamplingRateHz = 0.0;
	//--------- Sensor specific variables end --------------
	
	// Values appropriate to the Verisense Pulse+ (SR68). The Verisense GSR+ (SR62) uses Shimmer3 values.
	public static final double[] VERISENSE_PULSE_PLUS_GSR_REF_RESISTORS_KOHMS = new double[] {
			21., 		//Range 0
			150.0, 		//Range 1
			562.0, 		//Range 2
			1740.0}; 	//Range 3
	public static final double[][] VERISENSE_PULSE_PLUS_GSR_RESISTANCE_MIN_MAX_KOHMS = new double[][] {
			{8.0, 63.0}, 		//Range 0
			{63.0, 220.0}, 		//Range 1
			{220.0, 680.0}, 	//Range 2
			{680.0, 4700.0}}; 	//Range 3
	public static final int VERISENSE_PULSE_PLUS_GSR_UNCAL_LIMIT_RANGE3 = 1134;

	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENSOR_GSR_VERISENSE = new SensorDetailsRef(
			(long)Verisense.SensorBitmap.GSR, 
			(long)Verisense.SensorBitmap.GSR, 
			GuiLabelSensors.GSR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			new ArrayList<Integer>(),
			Arrays.asList(GuiLabelConfig.GSR_RANGE),
			Arrays.asList(
					ObjectClusterSensorName.GSR_RESISTANCE,
					ObjectClusterSensorName.GSR_CONDUCTANCE,
					ObjectClusterSensorName.GSR_RANGE),
			true);
	
	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF_VERISENSE;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.GSR, SensorGSRVerisense.SENSOR_GSR_VERISENSE);
		SENSOR_MAP_REF_VERISENSE = Collections.unmodifiableMap(aMap);
	}
	//--------- Sensor info end --------------

	//--------- Constructors for this class start --------------
	public SensorGSRVerisense(ShimmerVerObject svo) {
		super(svo);
		
		if (svo.getHardwareVersion() == HW_ID.VERISENSE_PULSE_PLUS) {
			setCurrentGsrRefResistorsKohms(VERISENSE_PULSE_PLUS_GSR_REF_RESISTORS_KOHMS);
			setCurrentGsrResistanceKohmsMinMax(VERISENSE_PULSE_PLUS_GSR_RESISTANCE_MIN_MAX_KOHMS);
			setCurrentGsrUncalLimitRange3(VERISENSE_PULSE_PLUS_GSR_UNCAL_LIMIT_RANGE3);
		}
	}
	//--------- Constructors for this class end --------------
	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF_VERISENSE, mChannelMapRef);
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		mGSRRange = (configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5] >> bitShiftGSRRange) & maskGSRRange;
		
		int samplingRateSetting = (configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG13] >> 0) & 0x3F;
		setSamplingRateFromShimmer(VerisenseDevice.ADC_SAMPLING_RATES[samplingRateSetting][1]);
	}
	
	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5] |= (byte) ((mGSRRange & maskGSRRange) << bitShiftGSRRange);

		//TODO fill in the sampling rate byte
//		for(double[] entry:VerisenseDevice.ADC_SAMPLING_RATES) {
//			if(getSamplingRateShimmer())
//		}
//		
//		configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG13] |= (byte) ((mGSRRange & maskGSRRange) << bitShiftGSRRange);
//
//		int samplingRateSetting = (configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG13] >> 0) & 0x3F;
//		setSamplingRateFromShimmer(VerisenseDevice.ADC_SAMPLING_RATES[samplingRateSetting][1]);
	}
	
	public double getSensorSamplingRate() {
		return sensorSamplingRateHz;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		sensorSamplingRateHz = samplingRateHz;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;

		switch (configLabel) {
		case (GuiLabelConfigCommon.RATE):
			setSensorSamplingRate((double) valueToSet);
			returnValue = valueToSet;
			break;
		default:
			super.setConfigValueUsingConfigLabel(sensorId, configLabel, valueToSet);
			break;
		}
		
		return returnValue;
	}

	
	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		switch (configLabel) {
		case (GuiLabelConfigCommon.RATE):
			returnValue = getSensorSamplingRate();
			break;
		default:
			returnValue = super.getConfigValueUsingConfigLabel(sensorId, configLabel);
			break;
		}

		return returnValue;
	}

	//--------- Abstract methods implemented end --------------

}
