package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ShimmerObject.DerivedSensorsBitMask;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorLSM303.GuiLabelConfig;
import com.shimmerresearch.sensors.SensorLSM303.GuiLabelSensors;
import com.shimmerresearch.sensors.SensorLSM303.ObjectClusterSensorName;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */

public class SensorBridgeAmp extends AbstractSensor{


	private static final long serialVersionUID = 2251208184040052082L; //TODO generate correct value


	//--------- Sensor specific variables start --------------

	public class SDLogHeaderDerivedSensors{                  // Check this
		public final static int BRIDGE_AMP = 1<<15;
		public final static int RES_AMP = 1<<0;
		public final static int SKIN_TEMP = 1<<1;
	}

	public class BTStreamDerivedSensors{                     // Check this
		public final static int BRIDGE_AMP = 1<<15;
		public final static int RES_AMP = 1<<0;
		public final static int SKIN_TEMP = 1<<1;
	}

	public class GuiLabelConfig{
		//Not in this class ?
	}

	public class GuiLabelSensorTiles{
		public static final String BRIDGE_AMPLIFIER = "Bridge Amplifier+";
		public static final String BRIDGE_AMPLIFIER_SUPP ="Skin Temperature";
	}

	public class GuiLabelSensors{
		public static final String BRIDGE_AMPLIFIER = "Bridge Amplifier+";
		public static final String RESISTANCE_AMP = "Resistance Amp";
		public static final String SKIN_TEMP_PROBE = "Skin Temperature";
		public static final String BRAMP_HIGHGAIN = "High Gain";
		public static final String BRAMP_LOWGAIN = "Low Gain";
	}


	public static class DatabaseChannelHandles{
		public static final String BRIDGE_AMPLIFIER_HIGH = "F5437a_Int_A13_BR_AMP_HIGH";
		public static final String BRIDGE_AMPLIFIER_LOW = "F5437a_Int_A13_BR_AMP_LOW";
		public static final String SKIN_TEMPERATURE = "Philips_21091A_Int_A1_SKIN_TEMP";
		public static final String RESISTANCE_AMPLIFIER = "F5437a_Int_A1_RES_AMP";
	}

	public static class ObjectClusterSensorName{
		public static final String BRIDGE_AMP_HIGH = "Bridge_Amp_High";
		public static final String BRIDGE_AMP_LOW = "Bridge_Amp_Low";
		public static final String RESISTANCE_AMP = "Resistance_Amp";
		public static final String SKIN_TEMPERATURE_PROBE = "Skin_Temperature";
	}

	//--------- Sensor specific variables End --------------


	//--------- Bluetooth commands start --------------

	//			Not in this class

	//--------- Bluetooth commands end --------------


	//--------- Configuration options start ------------
	//--------- Configuration options end --------------


	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorBridgeAmplifierRef = new SensorDetailsRef(
			0x80<<(1*8), 
			0x80<<(1*8), 
			GuiLabelSensors.BRIDGE_AMPLIFIER,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT
					),
			null,
			Arrays.asList(ObjectClusterSensorName.BRIDGE_AMP_HIGH,
					ObjectClusterSensorName.BRIDGE_AMP_LOW),
			true);


	public static final SensorDetailsRef sensorResistanceAmpRef = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDSDLogHeader, 
			GuiLabelSensors.RESISTANCE_AMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT
					),
			null,
			Arrays.asList(ObjectClusterSensorName.RESISTANCE_AMP),
			true);

	public static final SensorDetailsRef sensorSkinTempProbeRef = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDSDLogHeader,  
			GuiLabelSensors.SKIN_TEMP_PROBE,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					),
			null,
			Arrays.asList(ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE),
			true);	


	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,sensorBridgeAmplifierRef );
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,sensorResistanceAmpRef );
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE,sensorSkinTempProbeRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}

	//--------- Sensor info end --------------


	//--------- Channel info start --------------

	public static ChannelDetails channelBridgeAmpHigh = new ChannelDetails(
			ObjectClusterSensorName.BRIDGE_AMP_HIGH,
			ObjectClusterSensorName.BRIDGE_AMP_HIGH,
			DatabaseChannelHandles.BRIDGE_AMPLIFIER_HIGH,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));


	public static ChannelDetails channelBridgeAmpLow = new ChannelDetails(
			ObjectClusterSensorName.BRIDGE_AMP_LOW,
			ObjectClusterSensorName.BRIDGE_AMP_LOW,
			DatabaseChannelHandles.BRIDGE_AMPLIFIER_LOW,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// Philips Skin Temperature Probe (through Bridge Amp)
	public static ChannelDetails channelSkinTemp = new ChannelDetails(
			ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE,
			ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE,
			DatabaseChannelHandles.SKIN_TEMPERATURE,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_CELSUIS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// Resistance Amplifier
	public static ChannelDetails channelResistanceAmp = new ChannelDetails(
			ObjectClusterSensorName.RESISTANCE_AMP,
			ObjectClusterSensorName.RESISTANCE_AMP,
			DatabaseChannelHandles.RESISTANCE_AMPLIFIER,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));


	public static final Map<String, ChannelDetails> mChannelMapRef;
	static {
		Map<String, ChannelDetails> aChannelMap = new LinkedHashMap<String, ChannelDetails>();

		aChannelMap.put(ObjectClusterSensorName.BRIDGE_AMP_HIGH, channelBridgeAmpHigh);
		aChannelMap.put(ObjectClusterSensorName.BRIDGE_AMP_LOW, channelBridgeAmpLow); 
		aChannelMap.put(ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE, channelSkinTemp ); 
		aChannelMap.put(ObjectClusterSensorName.RESISTANCE_AMP, channelResistanceAmp );

		mChannelMapRef = Collections.unmodifiableMap(aChannelMap);
	}

	// --------------------------- Channel info end ----------------------------------------

	public static final SensorGroupingDetails sensorBridgeAmp =  new SensorGroupingDetails(
			GuiLabelSensorTiles.BRIDGE_AMPLIFIER,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp);

	public static final SensorGroupingDetails sensorBATemp = new SensorGroupingDetails(
			GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp);


	//--------- Constructors for this class start --------------

	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorBridgeAmp(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.Bridge_Amplifier.toString());
	}

	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		
		//Update the derived sensor bit index
		for(Integer sensorMapKey:mSensorMap.keySet()){
			long derivedSensorBitmapID = 0;
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE){
				derivedSensorBitmapID = SDLogHeaderDerivedSensors.SKIN_TEMP;
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP){
				derivedSensorBitmapID = SDLogHeaderDerivedSensors.BRIDGE_AMP;
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP){
				derivedSensorBitmapID = SDLogHeaderDerivedSensors.RES_AMP;
			}
			
			if(derivedSensorBitmapID>0){
				SensorDetails sensorBridgeAmp = mSensorMap.get(sensorMapKey);
				sensorBridgeAmp.mDerivedSensorBitmapID = derivedSensorBitmapID;
			}
		}
	}
	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// Not in this class
	}
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER.ordinal(), sensorBridgeAmp );
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP.ordinal(), sensorBATemp);
		}
		super.updateSensorGroupingMap();

	}
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub

	}
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled,
			long pcTimestamp) {

//		return processBAAdcChannel(sensorDetails, rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

		sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
		if(mEnableCalibration){
			int index = sensorDetails.mListOfChannels.size();
			for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.BRIDGE_AMP_HIGH)){
					double offset = 60; double vRefP = 3; double gain = 551; 
					double calData = processBAAdcChannel(channelDetails,rawData,commType,objectCluster,isTimeSyncEnabled,pcTimestamp,offset,vRefP,gain);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
				}
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.BRIDGE_AMP_LOW)){
					double offset = 1950; double vRefP = 3; double gain = 183.7;  
					double calData = processBAAdcChannel(channelDetails,rawData,commType,objectCluster,isTimeSyncEnabled,pcTimestamp,offset,vRefP,gain);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
				}
				
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.RESISTANCE_AMP)){
					double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					double calData = SensorADC.calibrateU12AdcValue(unCalData,0,3,1);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
				}
				
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE)){
					double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					double calData = calibratePhillipsSkinTemperatureData(unCalData);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
				}
			}
		}
		return objectCluster;
	}
	
	public static double processBAAdcChannel(ChannelDetails channelDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp,
			double offset, double vRefP, double gain){
//		sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
//		
//		double offset = 60; double vRefP = 3; double gain = 551;  
//		if(mEnableCalibration){
		
//			for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){

					double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					double calData = SensorADC.calibrateU12AdcValue(unCalData, offset, vRefP, gain);
					
					
//			}
//		}
		

		return calData;
	}
	
	
	protected static double calibratePhillipsSkinTemperatureData(double uncalibratedData){
		double x = (200.0*uncalibratedData)/((10.1)*3000-uncalibratedData);
		double y = -27.42*Math.log(x) + 56.502;
		return y;
	}
	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub

	}
	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub

	}
	@Override
	public Object setConfigValueUsingConfigLabel(String groupName,
			String componentName, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey,
			boolean isSensorEnabled) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			return true;
		}
		return false;
	}
	@Override
	public Object getSettings(String componentName,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ActionSetting setSettings(String componentName,
			Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub

	}

}	