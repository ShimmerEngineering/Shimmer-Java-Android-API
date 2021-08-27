package com.shimmerresearch.verisense;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.SensorADC;
import com.shimmerresearch.sensors.SensorBattVoltage;
import com.shimmerresearch.sensors.SensorADC.MICROCONTROLLER_ADC_PROPERTIES;
import com.shimmerresearch.verisense.communication.OpConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

public class SensorBattVoltageVerisense extends SensorBattVoltage {

	private static final long serialVersionUID = -3662637648902741983L;

	//--------- Sensor specific variables start --------------
	private MICROCONTROLLER_ADC_PROPERTIES microcontrollerAdcProperties = null;
	private double sensorSamplingRateHz = 0.0;
	//--------- Sensor specific variables end --------------

	
	public static class ObjectClusterSensorNameVerisense {
		public static final String USB_CONNECTION_STATE = "USB_Connection_State";
		public static final String CHARGER_STATE = "Charger_State";
	}

	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENSOR_BATTERY_VOLTAGE_VERISENSE = new SensorDetailsRef(
			Verisense.SensorBitmap.VBATT,
			Verisense.SensorBitmap.VBATT,
			GuiLabelSensors.BATTERY,
			null,
			null,
			Arrays.asList(ObjectClusterSensorName.BATTERY,
					ObjectClusterSensorName.BATT_PERCENTAGE,
					ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE));

	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF_VERISENSE;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.VBATT, SensorBattVoltageVerisense.SENSOR_BATTERY_VOLTAGE_VERISENSE);  
		SENSOR_MAP_REF_VERISENSE = Collections.unmodifiableMap(aMap);
	}
	//--------- Sensor info end --------------

	//--------- Channel info start --------------
	public static final ChannelDetails CHANNEL_USB_CONNECTION_STATE = new ChannelDetails(
			ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE,
			ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE,
			ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL),
			true,
			false);

	public static final ChannelDetails CHANNEL_CHARGER_STATE = new ChannelDetails(
			ObjectClusterSensorNameVerisense.CHARGER_STATE,
			ObjectClusterSensorNameVerisense.CHARGER_STATE,
			ObjectClusterSensorNameVerisense.CHARGER_STATE,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL),
			true,
			false);
	// --------- Channel info end --------------
	
	
	//--------- Constructors for this class start --------------
	public SensorBattVoltageVerisense(ShimmerDevice shimmerDevice) {
		super(shimmerDevice);
		
		microcontrollerAdcProperties = MICROCONTROLLER_ADC_PROPERTIES.getMicrocontrollerAdcPropertiesForShimmerVersionObject(shimmerDevice.getShimmerVerObject());
	}
	//--------- Constructors for this class end --------------

	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap() {
		Map<String, ChannelDetails> channelMapRefVerisense = new LinkedHashMap<String, ChannelDetails>();
		channelMapRefVerisense.putAll(mChannelMapRef);
		
		//TODO battery percentage not supported yet in Verisense Zinc-air and NiMH based products
		if(mShimmerVerObject.getHardwareVersion()!=HW_ID.VERISENSE_GSR_PLUS) {
			channelMapRefVerisense.remove(ObjectClusterSensorName.BATT_PERCENTAGE);
		}
		
		if(mShimmerDevice.getHardwareVersion()==HW_ID.VERISENSE_GSR_PLUS) {
			channelMapRefVerisense.put(ObjectClusterSensorNameVerisense.CHARGER_STATE, CHANNEL_CHARGER_STATE);
		} else {
			channelMapRefVerisense.put(ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE, CHANNEL_USB_CONNECTION_STATE);
		}

		
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF_VERISENSE, channelMapRefVerisense);
	}
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

		for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.BATTERY)){
				double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				
				// Mask the lower 12-bits so that we're free to use the upper 4 bits for charging status/USB in/out etc.
				int battAdcValue = ((int)unCalData) & 4095;
				
				double calData = SensorADC.calibrateAdcChannelToMillivolts(battAdcValue, microcontrollerAdcProperties);
				
				if(mShimmerDevice.getShimmerVerObject().getHardwareVersion()==HW_ID.VERISENSE_GSR_PLUS) {
					getShimmerBattStatusDetails().setBattAdcValue(battAdcValue);

					// Multiply by 1.988 because the battery voltage is divided by 2 before entering the BMD-340 And the value is not quite equal to 2 due to the components used in the circuit
					calData *= BATTERY_VOLTAGE_DIVIDER_RATIO;
				}
				objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);

			} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.BATT_PERCENTAGE)){
				double estimatedChargePercentage = (double)getShimmerBattStatusDetails().getEstimatedChargePercentage();
				if(!Double.isNaN(estimatedChargePercentage) && !Double.isInfinite(estimatedChargePercentage)){
					objectCluster.addCalData(channelDetails, estimatedChargePercentage);
					objectCluster.incrementIndexKeeper();
				}

			} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE)
					|| channelDetails.mObjectClusterName.equals(ObjectClusterSensorNameVerisense.CHARGER_STATE)){
				double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelBattVolt.mObjectClusterName), channelBattVolt.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
	
				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorNameVerisense.USB_CONNECTION_STATE)){
					// The USB connection state is stored in Bit 15 of the two byte uncalibrated data
					objectCluster.addCalData(channelDetails, (((int)unCalData) >> 15) & 0x01);
					objectCluster.incrementIndexKeeper();
				} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorNameVerisense.CHARGER_STATE)){
					// The charging chip state, if supported, is stored in Bit 14 and Bit 13 of the two byte uncalibrated data
					int chargingStatus = (((int)unCalData) >> 13) & 0x03;
					objectCluster.addCalData(channelDetails, chargingStatus);
					objectCluster.incrementIndexKeeper();
	
					getShimmerBattStatusDetails().setChargingStatus(chargingStatus);
	
				}
			}
		}

		return objectCluster;
	}

	public double getSensorSamplingRate() {
		return sensorSamplingRateHz;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		sensorSamplingRateHz = samplingRateHz;
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayoutVerisenseAdc configByteLayoutVerisenseAdc = new ConfigByteLayoutVerisenseAdc(shimmerDevice, commType);
		if(configByteLayoutVerisenseAdc.idxAdcRate>=0) {
			int samplingRateSetting = (configBytes[configByteLayoutVerisenseAdc.idxAdcRate] >> 0) & 0x3F;
			setSamplingRateFromShimmer(VerisenseDevice.ADC_SAMPLING_RATES[samplingRateSetting][1]);
		}
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayoutVerisenseAdc configByteLayoutVerisenseAdc = new ConfigByteLayoutVerisenseAdc(shimmerDevice, commType);
		//TODO fill in the sampling rate byte
		if(configByteLayoutVerisenseAdc.idxAdcRate>=0) {
			
		}
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
	
	private class ConfigByteLayoutVerisenseAdc {
		public int idxAdcRate = -1;
		
		public ConfigByteLayoutVerisenseAdc(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV12orAbove()) {
					if(commType==COMMUNICATION_TYPE.SD) {
						idxAdcRate = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG13;
					} else {
						idxAdcRate = OP_CONFIG_BYTE_INDEX.ADC_SAMPLE_RATE;
					}
				}
			}
		}
	}

}

