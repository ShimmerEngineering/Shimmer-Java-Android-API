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
import com.shimmerresearch.verisense.communication.payloads.OperationalConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

public class SensorGSRVerisense extends SensorGSR {

	private static final long serialVersionUID = -3937042079000714506L;

	//--------- Sensor specific variables start --------------
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
		
		ConfigByteLayoutGsr cbl = new ConfigByteLayoutGsr(shimmerDevice, commType);
		
		if(cbl.idxGsrRange>=0 && cbl.idxAdcRate>=0) {
			mGSRRange = (configBytes[cbl.idxGsrRange] >> cbl.bitShiftGSRRange) & cbl.maskGSRRange;
			
			int samplingRateSetting = (configBytes[cbl.idxAdcRate] >> 0) & cbl.maskAdcRate;
			setSamplingRateFromShimmer(VerisenseDevice.ADC_SAMPLING_RATES[samplingRateSetting][1]);
		}
	}
	
	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {

		ConfigByteLayoutGsr cbl = new ConfigByteLayoutGsr(shimmerDevice, commType);

		if(cbl.idxGsrRange>=0 && cbl.idxAdcRate>=0) {
			configBytes[cbl.idxGsrRange] &= ~(cbl.maskGSRRange << cbl.bitShiftGSRRange);
			configBytes[cbl.idxGsrRange] |= (byte) ((mGSRRange & cbl.maskGSRRange) << cbl.bitShiftGSRRange);
	
			configBytes[cbl.idxAdcRate] &= ~cbl.maskAdcRate;
			for(double[] entry:VerisenseDevice.ADC_SAMPLING_RATES) {
				if(getSensorSamplingRate()==entry[1]) {
					configBytes[cbl.idxAdcRate] |= (byte)entry[0] & cbl.maskAdcRate;
				}
			}
		}
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

	private class ConfigByteLayoutGsr {
		public int idxAdcRate = -1, idxGsrRange = -1, maskAdcRate = 0x3F, maskGSRRange = 0x07, bitShiftGSRRange = 5;
		
		public ConfigByteLayoutGsr(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV12orAbove()) {
					if(commType==COMMUNICATION_TYPE.SD) {
						idxAdcRate = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG13;
						idxGsrRange = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5;
					} else {
						idxAdcRate = OP_CONFIG_BYTE_INDEX.ADC_SAMPLE_RATE;
						idxGsrRange = OP_CONFIG_BYTE_INDEX.GSR_RANGE_SETTING;
					}
				}
			}
		}
	}
	

}
