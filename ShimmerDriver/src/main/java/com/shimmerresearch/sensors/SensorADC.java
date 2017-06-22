package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */

public class SensorADC extends AbstractSensor {
	
	private static final long serialVersionUID = 7267049827248328113L;
	
	//--------- Sensor specific variables start --------------
	
	public class GuiLabelConfig{
		//No config options in this class
		
//		public static final String PPG_ADC_SELECTION =  SensorPPG.GuiLabelConfig.PPG_ADC_SELECTION; //"PPG Channel";
//		public static final String PPG1_ADC_SELECTION = SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION; //"Channel1";
//		public static final String PPG2_ADC_SELECTION = SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION; // "Channel2";
		
//		public static final String INT_EXP_BRD_POWER_BOOLEAN = "Internal Expansion Board Power";
//		public static final String INT_EXP_BRD_POWER_INTEGER = "Int Exp Power";
	}
	
	// GUI Label Sensors 
	public class GuiLabelSensors{
		public static final String EXT_EXP_A7 = "Ext A7";
		public static final String EXT_EXP_A6 = "Ext A6";
		public static final String EXT_EXP_A15 = "Ext A15";
		public static final String INT_EXP_A12 = "Int A12";
		public static final String INT_EXP_A13 = "Int A13";
		public static final String INT_EXP_A14 = "Int A14";
		public static final String INT_EXP_A1 = "Int A1";
	}
	
	// GUI Sensor Tiles
	public class GuiLabelSensorTiles{
		public static final String EXTERNAL_EXPANSION_ADC = "External Expansion ADCs";
		public static final String INTERNAL_EXPANSION_ADC = "Internal Expansion ADCs";
		
		public static final String PROTO3_MINI = "Proto Mini";
		public static final String PROTO3_DELUXE = "Proto Deluxe";
	}
	
	//DATABASE NAMES
	public static class DatabaseChannelHandles{
		public static final String EXT_ADC_A7 = "F5437a_Ext_A7";
		public static final String EXT_ADC_A6 = "F5437a_Ext_A6";
		public static final String EXT_ADC_A15 = "F5437a_Ext_A15";
		public static final String INT_ADC_A1 = "F5437a_Int_A1";
		public static final String INT_ADC_A12 = "F5437a_Int_A12";
		public static final String INT_ADC_A13 = "F5437a_Int_A13";
		public static final String INT_ADC_A14 = "F5437a_Int_A14";
	}
	
	//GUI AND EXPORT CHANNELS
	public static class ObjectClusterSensorName{
		public static String EXT_EXP_ADC_A7 = "Ext_Exp_A7";
		public static String EXT_EXP_ADC_A6 = "Ext_Exp_A6";
		public static String EXT_EXP_ADC_A15 = "Ext_Exp_A15";
		public static String INT_EXP_ADC_A1 = "Int_Exp_A1";
		public static String INT_EXP_ADC_A12 = "Int_Exp_A12";
		public static String INT_EXP_ADC_A13 = "Int_Exp_A13";
		public static String INT_EXP_ADC_A14 = "Int_Exp_A14";
	}
	
	
	protected int mPpgAdcSelectionGsrBoard = 0;
	protected int mPpg1AdcSelectionProto3DeluxeBoard = 0;
	protected int mPpg2AdcSelectionProto3DeluxeBoard = 0;
	
	
//--------- Sensor specific variables end --------------
	
	
	//--------- Bluetooth commands start --------------
	
//--------- Bluetooth commands end --------------
	
	//--------- Configuration options start --------------
//	public static final String[] ListofCompatibleSensors={"Low Noise Accelerometer","Wide Range Accelerometer","Gyroscope","Magnetometer","Battery Voltage","External ADC A7","External ADC A6","External ADC A15","Internal ADC A1","Internal ADC A12","Internal ADC A13","Internal ADC A14","Pressure","GSR","EXG1","EXG2","EXG1 16Bit","EXG2 16Bit", "Bridge Amplifier"}; 

	
//	public static final String[] ListOfPpgAdcSelection= SensorPPG.ListOfPpgAdcSelection; //{"Int A13","Int A12"};
//	public static final Integer[] ListOfPpgAdcSelectionConfigValues= SensorPPG.ListOfPpgAdcSelectionConfigValues; // {0,1};
//	public static final String[] ListOfPpg1AdcSelection=SensorPPG.ListOfPpg1AdcSelection; //{"Int A13","Int A12"};
//	public static final Integer[] ListOfPpg1AdcSelectionConfigValues=SensorPPG.ListOfPpg1AdcSelectionConfigValues; //{0,1};
//	public static final String[] ListOfPpg2AdcSelection=SensorPPG.ListOfPpg2AdcSelection; //{"Int A1","Int A14"};
//	public static final Integer[] ListOfPpg2AdcSelectionConfigValues= SensorPPG.ListOfPpg2AdcSelectionConfigValues; //{0,1
	
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorADC_EXT_EXP_ADC_A7Ref = new SensorDetailsRef(0x02<<(0*8), 0x02<<(0*8), GuiLabelSensors.EXT_EXP_A7,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			null,
			Arrays.asList(ObjectClusterSensorName.EXT_EXP_ADC_A7), 
			false);
	
	public static final SensorDetailsRef sensorADC_EXT_EXP_ADC_A6Ref = new SensorDetailsRef(0x01<<(0*8), 0x01<<(0*8), GuiLabelSensors.EXT_EXP_A6,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			null,
			Arrays.asList(ObjectClusterSensorName.EXT_EXP_ADC_A6), 
			false);

	public static final SensorDetailsRef sensorADC_EXT_EXP_ADC_A15Ref = new SensorDetailsRef(0x08<<(1*8), 0x08<<(1*8), GuiLabelSensors.EXT_EXP_A15,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			null,
			Arrays.asList(ObjectClusterSensorName.EXT_EXP_ADC_A15), 
			false);

	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A1Ref = new SensorDetailsRef(0x04<<(1*8), 0x04<<(1*8), GuiLabelSensors.INT_EXP_A1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA1,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_THREE_UNIPOLAR
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT
					),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER),
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A1), 
			false);
	
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A12Ref = new SensorDetailsRef(
			0x02<<(1*8), 
			0x02<<(1*8), 
			GuiLabelSensors.INT_EXP_A12,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA12,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER),
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A12), 
			false);
	
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A13Ref = new SensorDetailsRef(
			0x01<<(1*8), 
			0x01<<(1*8), 
			GuiLabelSensors.INT_EXP_A13,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA13,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER),
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A13), 
			false);
	
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A14Ref = new SensorDetailsRef(
			0x80<<(2*8), 
			0x80<<(2*8), 
			GuiLabelSensors.INT_EXP_A14,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA14,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER),
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A14), 
			false);

    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A7, sensorADC_EXT_EXP_ADC_A7Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A6, sensorADC_EXT_EXP_ADC_A6Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A15, sensorADC_EXT_EXP_ADC_A15Ref);
		
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1, sensorADC_INT_EXP_ADC_A1Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, sensorADC_INT_EXP_ADC_A12Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13, sensorADC_INT_EXP_ADC_A13Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14, sensorADC_INT_EXP_ADC_A14Ref);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }

    public static final SensorGroupingDetails sensorGroupExternalExpansionADCs = new SensorGroupingDetails(
			SensorADC.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A6,
						Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A7,
						Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A15),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

    public static final SensorGroupingDetails sensorGroupInternalExpansionADCs = new SensorGroupingDetails(
			GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC, 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntAdcsGeneral);
	
    //------------------Channel Details----------------------

    //External Channels
    public static final ChannelDetails channel_EXT_EXP_ADC_A7 = new ChannelDetails(
    		ObjectClusterSensorName.EXT_EXP_ADC_A7,
    		ObjectClusterSensorName.EXT_EXP_ADC_A7,
    		DatabaseChannelHandles.EXT_ADC_A7,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x0D);

    public static final ChannelDetails channel_EXT_EXP_ADC_A6 = new ChannelDetails(
    		ObjectClusterSensorName.EXT_EXP_ADC_A6,
    		ObjectClusterSensorName.EXT_EXP_ADC_A6,
    		DatabaseChannelHandles.EXT_ADC_A6,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x0E);


    public static final ChannelDetails channel_EXT_EXP_ADC_A15 = new ChannelDetails(
    		ObjectClusterSensorName.EXT_EXP_ADC_A15,
    		ObjectClusterSensorName.EXT_EXP_ADC_A15,
    		DatabaseChannelHandles.EXT_ADC_A15,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x0F);

    // Internal ADCs
    public static final ChannelDetails channel_INT_EXP_ADC_A1 = new ChannelDetails(
    		ObjectClusterSensorName.INT_EXP_ADC_A1,
    		ObjectClusterSensorName.INT_EXP_ADC_A1,
    		DatabaseChannelHandles.INT_ADC_A1,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x10);


    public static final ChannelDetails channel_INT_EXP_ADC_A12 = new ChannelDetails(
    		ObjectClusterSensorName.INT_EXP_ADC_A12,
    		ObjectClusterSensorName.INT_EXP_ADC_A12,
    		DatabaseChannelHandles.INT_ADC_A12,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x11);


    public static final ChannelDetails channel_INT_EXP_ADC_A13 = new ChannelDetails(
    		ObjectClusterSensorName.INT_EXP_ADC_A13,
    		ObjectClusterSensorName.INT_EXP_ADC_A13,
    		DatabaseChannelHandles.INT_ADC_A13,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x12);

    public static final ChannelDetails channel_INT_EXP_ADC_A14 = new ChannelDetails(
    		ObjectClusterSensorName.INT_EXP_ADC_A14,
    		ObjectClusterSensorName.INT_EXP_ADC_A14,
    		DatabaseChannelHandles.INT_ADC_A14,
    		CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x13);

    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
    	Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
    	// External ADCs
    	aMap.put(ObjectClusterSensorName.EXT_EXP_ADC_A7,  channel_EXT_EXP_ADC_A7);
    	aMap.put(ObjectClusterSensorName.EXT_EXP_ADC_A6,  channel_EXT_EXP_ADC_A6);
    	aMap.put(ObjectClusterSensorName.EXT_EXP_ADC_A15, channel_EXT_EXP_ADC_A15);
    	// Internal ADCs
    	aMap.put(ObjectClusterSensorName.INT_EXP_ADC_A1,  channel_INT_EXP_ADC_A1);
    	aMap.put(ObjectClusterSensorName.INT_EXP_ADC_A12, channel_INT_EXP_ADC_A12);
    	aMap.put(ObjectClusterSensorName.INT_EXP_ADC_A13, channel_INT_EXP_ADC_A13);
    	aMap.put(ObjectClusterSensorName.INT_EXP_ADC_A14, channel_INT_EXP_ADC_A14);
    	mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

    //--------- Sensor info end --------------
	
	
	//--------- Constructors for this class start --------------
	public SensorADC(ShimmerVerObject svo) {
		super(SENSORS.ADC, svo);
		initialise();
	}
	
	//--------- Constructors for this class end --------------
	
	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
//		mConfigOptionsMap.put(GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN, configOptionIntExpBrdPowerBoolean);
//		mConfigOptionsMap.put(GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER, configOptionIntExpBrdPowerInteger);
	}

	@Override
	public void generateSensorGroupMapping() {
		
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
//		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC.ordinal(), sensorGroupExternalExpansionADCs);
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC.ordinal(), sensorGroupInternalExpansionADCs);
//		}
		super.updateSensorGroupingMap();	
	}
		
		

	

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// Not In This Class
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled,long pcTimestamp) {
		return processMspAdcChannel(sensorDetails, rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
	}
	
	public static ObjectCluster processMspAdcChannel(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp){
		sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
		
		if(mEnableCalibration){
			int index = sensorDetails.mListOfChannels.size();
			for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				double calData = calibrateMspAdcChannel(unCalData);
				objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-index);
				index--;
			}
		}

		return objectCluster;
	}
	
	public static double calibrateMspAdcChannel(double unCalData){
		double offset = 0; double vRefP = 3; double gain = 1; 
		double calData = calibrateU12AdcValue(unCalData, offset, vRefP, gain);
		return calData;
	}
	

	@Override
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void parseConfigMapFromDb(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//--------- Abstract methods implemented end --------------
	//--------- Sensor specific methods start --------------
	public static double calibrateU12AdcValue(double uncalibratedData, double offset, double vRefP, double gain){
		double calibratedData = (uncalibratedData-offset) * (((vRefP*1000)/gain)/4095);
		return calibratedData;
	}
	public static double calibrateAdcValue(double uncalibratedData, double offset, double vRefP, double gain, CHANNEL_DATA_TYPE channelDataType){
		double calibratedData = (uncalibratedData-offset) * (((vRefP*1000)/gain)/channelDataType.getMaxVal());
		return calibratedData;
	}
	//--------- Sensor specific methods end --------------

	public static int uncalibrateU12AdcValue(double uncalibratedData, double offset, double vRefP, double gain) {
//		double calibratedData=(uncalibratedData-offset)*(((vRefP*1000)/gain)/4095);
//		return calibratedData;
		
		double adcVal = (uncalibratedData / (((vRefP*1000)/gain)/4095)) + offset;
		return (int) adcVal;
	}

	
}
