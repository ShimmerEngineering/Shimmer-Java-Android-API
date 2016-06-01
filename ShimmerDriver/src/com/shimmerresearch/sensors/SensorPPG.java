package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */

public class SensorPPG extends AbstractSensor {
	
	private static final long serialVersionUID = 2251208184040052082L;
	
	//--------- Sensor specific variables start --------------

	protected int mPpgAdcSelectionGsrBoard = 0;
	protected int mPpg1AdcSelectionProto3DeluxeBoard = 0;
	protected int mPpg2AdcSelectionProto3DeluxeBoard = 0;
	/** GQ BLE */
	protected int mSamplingDividerPpg = 0;
	
	protected int mInternalExpPower=-1;	
	
//	// Can be used to enable/disable GUI options depending on what HW, FW, Expansion boards versions are present
//	private static final ShimmerVerObject baseAnyIntExpBoardAndFw = new ShimmerVerObject(HW_ID.SHIMMER_GQ_BLE,FW_ID.GQ_BLE,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);
//	private static final List<ShimmerVerObject> listOfCompatibleVersionInfoGq = Arrays.asList(baseAnyIntExpBoardAndFw);
//	
	public class SDLogHeaderDerivedSensors{
		public final static int PPG2_1_14 = 1<<4;
		public final static int PPG1_12_13 = 1<<3;
		public final static int PPG_12_13 = 1<<2;
	}
	
	public class BTStreamDerivedSensors{
		public final static int PPG2_1_14 = 1<<4;
		public final static int PPG1_12_13 = 1<<3;
		public final static int PPG_12_13 = 1<<2;
	}
	

	public class GuiLabelConfig{
		public static final String SAMPLING_RATE_DIVIDER_PPG = "PPG Divider";
		public static final String PPG_ADC_SELECTION = "PPG Channel";
		public static final String PPG1_ADC_SELECTION = "Channel1";
		public static final String PPG2_ADC_SELECTION = "Channel2";
	}
	
	public class GuiLabelSensorTiles{
		public static final String PROTO3_DELUXE_SUPP = "PPG";
	}


	public class GuiLabelSensors{
		public static final String PPG = "PPG";
		public static final String PPG_DUMMY = "PPG";
		public static final String PPG_A12 = "PPG A12";
		public static final String PPG_A13 = "PPG A13";
		public static final String PPG1_DUMMY = "PPG1";
		public static final String PPG1_A12 = "PPG1 A12";
		public static final String PPG1_A13 = "PPG1 A13";
		public static final String PPG2_DUMMY = "PPG2";
		public static final String PPG2_A1 = "PPG2 A1";
		public static final String PPG2_A14 = "PPG2 A14";
	}

	 
	public static class DatabaseChannelHandles{
		public static final String PPG_A12 = "F5437a_PPG_A12";
		public static final String PPG_A13 = "F5437a_PPG_A13";
		public static final String PPG1_A12 = "F5437a_PPG1_A12";
		public static final String PPG1_A13 = "F5437a_PPG1_A13";
		public static final String PPG2_A1 = "F5437a_PPG2_A1";
		public static final String PPG2_A14 = "F5437a_PPG2_A14";
	}
	
	public static class ObjectClusterSensorName{
		public static  String PPG_A12 = "PPG_A12";
		public static  String PPG_A13 = "PPG_A13";
		public static  String PPG1_A12 = "PPG1_A12";
		public static  String PPG1_A13 = "PPG1_A13";
		public static  String PPG2_A1 = "PPG2_A1";
		public static  String PPG2_A14 = "PPG2_A14";
	}
	//--------- Sensor specific variables start --------------

	
	//--------- Bluetooth commands start --------------
	
	//			Not in this class
	
	//--------- Bluetooth commands end --------------
	
	
	//--------- Configuration options start --------------
	
	public static final String[] ListOfPpgAdcSelection = {"Int A13","Int A12"};
	public static final Integer[] ListOfPpgAdcSelectionConfigValues = {0,1};
	public static final String[] ListOfPpg1AdcSelection = {"Int A13","Int A12"};
	public static final Integer[] ListOfPpg1AdcSelectionConfigValues = {0,1};
	public static final String[] ListOfPpg2AdcSelection = {"Int A1","Int A14"};
	public static final Integer[] ListOfPpg2AdcSelectionConfigValues = {0,1};
	
	public static final Integer[] FixedConflictingSensorMapKeys = {Configuration.Shimmer3.SensorMapKey.HOST_ECG,Configuration.Shimmer3.SensorMapKey.HOST_EMG, 
		Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION, 
		Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP};

    List <Integer> FixedConflictingSensorMapKeysList = Arrays.asList(FixedConflictingSensorMapKeys);
    
    public static final SensorConfigOptionDetails configOptionPpgAdcSelection = new SensorConfigOptionDetails(
    		ListOfPpgAdcSelection, 
			ListOfPpgAdcSelectionConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr);
    
    public static final SensorConfigOptionDetails configOptionPpg1AdcSelection = new SensorConfigOptionDetails(
    		ListOfPpg1AdcSelection, 
			ListOfPpg1AdcSelectionConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe);
    
    public static final SensorConfigOptionDetails configOptionPpg2AdcSelection = new SensorConfigOptionDetails(
    		ListOfPpg2AdcSelection, 
			ListOfPpg2AdcSelectionConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe);
    
	//--------- Configuration options end --------------
	

	//--------- Sensor info start --------------	
	public static final SensorDetailsRef sensorPPG = new SensorDetailsRef(
			0x04<<(2*8), 
			0x04<<(2*8), 
			GuiLabelSensors.PPG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, GuiLabelConfig.PPG_ADC_SELECTION, GuiLabelConfig.PPG1_ADC_SELECTION, GuiLabelConfig.PPG2_ADC_SELECTION),
			Arrays.asList(ObjectClusterSensorName.PPG_A12,ObjectClusterSensorName.PPG_A13, ObjectClusterSensorName.PPG1_A12,ObjectClusterSensorName.PPG1_A13,
					      ObjectClusterSensorName.PPG2_A1,ObjectClusterSensorName.PPG2_A14)
			);

	
	public static final SensorDetailsRef sensorPpgHostDummy = new SensorDetailsRef(
			0, 0,
			GuiLabelSensors.PPG_DUMMY,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			null,
			Arrays.asList(GuiLabelConfig.PPG_ADC_SELECTION),
			null,
			false);
	{
		sensorPpgHostDummy.mIsDummySensor = true;
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG_A12 = new SensorDetailsRef(
			0,
			0, 
			GuiLabelSensors.PPG_A12,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM
					),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG_A12),
			true
			);
	{
		sensorPpgHostPPG_A12.mListOfSensorMapKeysConflicting.addAll(FixedConflictingSensorMapKeysList);
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG_A13 = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG_A13,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12,Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13, Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG_A13),
			true
			);
	
	{
		sensorPpgHostPPG_A13.mListOfSensorMapKeysConflicting.addAll(FixedConflictingSensorMapKeysList);
	}
	
	// Derived Channels - Proto3 Board
	public static final SensorDetailsRef sensorPpgHostPPG1Dummy = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG1_DUMMY,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			null,
			Arrays.asList(GuiLabelConfig.PPG1_ADC_SELECTION),
			null,
			true
			);
	{
		sensorPpgHostPPG1Dummy.mIsDummySensor = true;
	}
	public static final SensorDetailsRef sensorPpgHostPPG1A12 = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG1_A12,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12,Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13, Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, 
		            Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG1_A12),
			true
			);
	{
		sensorPpgHostPPG1A12.mListOfSensorMapKeysConflicting.addAll(FixedConflictingSensorMapKeysList);
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG1A13 = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG1_A13,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12,Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG1_A13),
			true
			);
	{
		sensorPpgHostPPG1A13.mListOfSensorMapKeysConflicting.addAll(FixedConflictingSensorMapKeysList);
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG2Dummy = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG2_DUMMY,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			null,
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION),
			null,
			true
			);
	{
		sensorPpgHostPPG2Dummy.mIsDummySensor = true;
	}
	
	
	public static final SensorDetailsRef sensorPpgHostPPG2_A1 = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG2_A1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG2_A1),
			true
			);
	{
		sensorPpgHostPPG2_A1.mListOfSensorMapKeysConflicting.addAll(FixedConflictingSensorMapKeysList);
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG2_A14 = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG2_A14,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG2_A14),
			true
			);
	
	{
		sensorPpgHostPPG2_A14.mListOfSensorMapKeysConflicting.addAll(FixedConflictingSensorMapKeysList);
	}

	    
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();

    	// Derived Channels - GSR Board
        
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY, sensorPpgHostDummy);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12, sensorPpgHostPPG_A12);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13, sensorPpgHostPPG_A13);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY,sensorPpgHostPPG1Dummy);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12, sensorPpgHostPPG1A12);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13, sensorPpgHostPPG1A13);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY ,sensorPpgHostPPG2Dummy);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1, sensorPpgHostPPG2_A1);
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14, sensorPpgHostPPG2_A14);

        mSensorMapRef = Collections.unmodifiableMap(aMap);
       }
	//--------- Sensor info end --------------
    
    
    //--------- Channel info start --------------
	 // PPG - Using GSR+ board
	public static final ChannelDetails channelPPG_A12 = new ChannelDetails(
			ObjectClusterSensorName.PPG_A12,
			ObjectClusterSensorName.PPG_A12,
			DatabaseChannelHandles.PPG_A12,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG_A13 = new ChannelDetails(
			ObjectClusterSensorName.PPG_A13,
			ObjectClusterSensorName.PPG_A13,
			DatabaseChannelHandles.PPG_A13,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// PPG - Using Proto3 Deluxe TRRS Socket 1
	
	public static final ChannelDetails channelPPG1_A12 = new ChannelDetails(
			ObjectClusterSensorName.PPG1_A12,
			ObjectClusterSensorName.PPG1_A12,
			DatabaseChannelHandles.PPG1_A12,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG1_A13 = new ChannelDetails(
			ObjectClusterSensorName.PPG1_A13,
			ObjectClusterSensorName.PPG1_A13,
			DatabaseChannelHandles.PPG1_A13,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// PPG - Using Proto3 Deluxe TRRS Socket 2
	public static final ChannelDetails channelPPG2_A1 = new ChannelDetails(
			ObjectClusterSensorName.PPG2_A1,
			ObjectClusterSensorName.PPG2_A1,
			DatabaseChannelHandles.PPG2_A1,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG2_A14 = new ChannelDetails(
			ObjectClusterSensorName.PPG2_A14,
			ObjectClusterSensorName.PPG2_A14,
			DatabaseChannelHandles.PPG2_A14,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	
	public static final Map<String, ChannelDetails> mChannelMapRef;
	static {
		 Map<String, ChannelDetails> aChannelMap = new LinkedHashMap<String, ChannelDetails>();
		
		aChannelMap.put(ObjectClusterSensorName.PPG_A12, SensorPPG.channelPPG1_A12);
		aChannelMap.put(ObjectClusterSensorName.PPG_A13, SensorPPG.channelPPG_A13); 
		// PPG - Using Proto3 Deluxe TRRS Socket 1
		aChannelMap.put(ObjectClusterSensorName.PPG1_A12, SensorPPG.channelPPG1_A12); 
		aChannelMap.put(ObjectClusterSensorName.PPG1_A13, SensorPPG.channelPPG1_A13);
		// PPG - Using Proto3 Deluxe TRRS Socket 2
		aChannelMap.put(ObjectClusterSensorName.PPG2_A1, SensorPPG.channelPPG2_A1);
		aChannelMap.put(ObjectClusterSensorName.PPG2_A14, SensorPPG.channelPPG2_A14);

		mChannelMapRef = Collections.unmodifiableMap(aChannelMap);
	}
	
// --------------------------- Channel info end ----------------------------------------
	
	
//--------- Constructors for this class start --------------	
	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorPPG(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.PPG.toString());
	}
//--------- Constructors for this class end --------------


//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	 
	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {

			mConfigOptionsMap.put(GuiLabelConfig.PPG_ADC_SELECTION , configOptionPpgAdcSelection); 
			mConfigOptionsMap.put(GuiLabelConfig.PPG1_ADC_SELECTION, configOptionPpg1AdcSelection); 
			mConfigOptionsMap.put(GuiLabelConfig.PPG2_ADC_SELECTION, configOptionPpgAdcSelection); 
//			mConfigOptionsMap.put(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, mConfigOptionsMapRef.get(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG)); 
	}

	
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		//NOT USED IN THIS CLASS
		
		return objectCluster;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		//XXX What about this?
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		//XXX What about this?
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		////XXX What about this? Are the newly introduced method handling the commented out stuff below?
//		
//		Object returnValue = null;
//		int buf = 0;
//
//		switch(componentName){
//			case(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION):
//				setPpgAdcSelectionGsrBoard((int)valueToSet);
//	    		break;
//			case(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION):
//				setPpg1AdcSelectionProto3DeluxeBoard((int)valueToSet);
//	    		break;
//			case(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION):
//				setPpg2AdcSelectionProto3DeluxeBoard((int)valueToSet);
//	    		break;
//			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
//				setSamplingDividerPpg((int)valueToSet);
//	    		break;
//		}
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		switch(componentName){
		case(GuiLabelConfig.PPG_ADC_SELECTION):
			returnValue = getPpgAdcSelectionGsrBoard();
		break;
		case(GuiLabelConfig.PPG1_ADC_SELECTION):
			returnValue = getPpg1AdcSelectionProto3DeluxeBoard();
		break;
		case(GuiLabelConfig.PPG2_ADC_SELECTION):
			returnValue = getPpg2AdcSelectionProto3DeluxeBoard();
		break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// XXX added the if + return true, nothing happen further
		if(mConfigOptionsMap.containsKey(stringKey)){
			return true;
		}
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
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}
	//--------- Abstract methods implemented end --------------

	
	//--------- Sensor specific methods start --------------

	
	
	

	
	public boolean checkIfSensorEnabled(int sensorMapKey){
		if (getHardwareVersion() == HW_ID.SHIMMER_3) {
			//Used for Shimmer GSR hardware
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey== Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey== Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14)))
					return true;
				else
					return false;
			}
			
			return isSensorEnabled(sensorMapKey);
		}
		return false;
	}

	public void handleDummyEntriesInSensorMap(int sensorMapKey, boolean state) {

		if(mSensorMap!=null) {

			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);

				// Special case for Dummy entries in the Sensor Map
				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY) {
					sensorDetails.setIsEnabled(state);
					if(Configuration.Shimmer3.ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12;
					}
					else {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13;
					}
				}		
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY) {
					sensorDetails.setIsEnabled(state);
					if(Configuration.Shimmer3.ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12;
					}
					else {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13;
					}
				}		
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY) {
					sensorDetails.setIsEnabled(state);
					if(Configuration.Shimmer3.ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14;
					}
					else {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1;
					}
				}		

//				// Automatically handle required channels for each sensor
//				List<Integer> listOfRequiredKeys = sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired;
//				if(listOfRequiredKeys != null && listOfRequiredKeys.size()>0) {
//					for(Integer i:listOfRequiredKeys) {
//						mSensorMap.get(i).setIsEnabled(state);
//					}
//				}
			}
	}
	/**
	 * @return the mPpgAdcSelectionGsrBoard
	 */
	public int getPpgAdcSelectionGsrBoard() {
		return mPpgAdcSelectionGsrBoard;
	}

	/**
	 * @return the mPpg1AdcSelectionProto3DeluxeBoard
	 */
	public int getPpg1AdcSelectionProto3DeluxeBoard() {
		return mPpg1AdcSelectionProto3DeluxeBoard;
	}

	/**
	 * @return the mPpg2AdcSelectionProto3DeluxeBoard
	 */
	public int getPpg2AdcSelectionProto3DeluxeBoard() {
		return mPpg2AdcSelectionProto3DeluxeBoard;
	}
	/**
	 * @return the mSamplingDividerPpg
	 */
	public int getSamplingDividerPpg() {
		return mSamplingDividerPpg;
	}

	//--------- Optional methods to override in Sensor Class start --------	
	@Override
	public boolean handleSpecCasesBeforeSensorMapUpdate(ShimmerDevice shimmerDevice, Integer sensorMapKey){
		if(((sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14))){
			shimmerDevice.ignoreAndDisable(sensorMapKey);
			return true;
		}

		return false;
	}

	
	@Override
	public void handleSpecCasesAfterSensorMapUpdate() {
		//Used for Shimmer GSR hardware
		if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).isEnabled()) {
					mPpgAdcSelectionGsrBoard = ListOfPpgAdcSelectionConfigValues[1]; // PPG_A12
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).isEnabled()) {
					mPpgAdcSelectionGsrBoard = ListOfPpgAdcSelectionConfigValues[0]; // PPG_A13

				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).setIsEnabled(false);

			}
		}
		//Used for Shimmer Proto3 Deluxe hardware
		if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).isEnabled()) {
					mPpg1AdcSelectionProto3DeluxeBoard = ListOfPpg1AdcSelectionConfigValues[1]; // PPG1_A12
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).isEnabled()) {
					mPpg1AdcSelectionProto3DeluxeBoard = ListOfPpg1AdcSelectionConfigValues[0]; // PPG1_A13
				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).setIsEnabled(false);
			}
		}
		//Used for Shimmer Proto3 Deluxe hardware
		if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).isEnabled()) {
					mPpg2AdcSelectionProto3DeluxeBoard = ListOfPpg2AdcSelectionConfigValues[0]; // PPG2_A1
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).isEnabled()) {
					mPpg2AdcSelectionProto3DeluxeBoard = ListOfPpg2AdcSelectionConfigValues[1]; // PPG2_A14
				}
			}
			else {
			mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).setIsEnabled(false);
			}
		}
	}

	
	@Override
	public void handleSpecCasesBeforeSetSensorState(int sensorMapKey, boolean state) {
		handleDummyEntriesInSensorMap(sensorMapKey, state);
	}
	//--------- Optional methods to override in Sensor Class end --------


}
