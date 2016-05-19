package com.shimmerresearch.sensors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorBMP180.GuiLabelConfig;
import com.shimmerresearch.sensors.SensorBMP180.GuiLabelSensors;
import com.shimmerresearch.sensors.SensorBMP180.ObjectClusterSensorName;

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
	
	public static class ShimmerGqBle{
		public class SensorMapKey{
			public static final int PPG = 107;
		}
	}
	
	public static class SensorMapKey{
		// Derived Channels - GSR Board
		public static final int HOST_PPG_A12 = 106;
		public static final int HOST_PPG_A13 = 107;
		// Derived Channels - Proto3 Deluxe Board
		public static final int HOST_PPG1_A12 = 111;
		public static final int HOST_PPG1_A13 = 112;
		public static final int HOST_PPG2_A1 = 114;
		public static final int HOST_PPG2_A14 = 115;
		public static final int HOST_PPG_DUMMY = 105;
		public static final int HOST_PPG1_DUMMY = 110;
		public static final int HOST_PPG2_DUMMY = 113;
	}
	//GUI SENSORs
	public class GuiLabelSensors{
		public static final String PPG = "PPG";
	}
	public class GuiLabelConfig{
		public static final String SAMPLING_RATE_DIVIDER_PPG = "PPG Divider";
		public static final String PPG_ADC_SELECTION = "PPG Channel";
		public static final String PPG1_ADC_SELECTION = "Channel1";
		public static final String PPG2_ADC_SELECTION = "Channel2";
	}
	
	public static class ObjectClusterSensorName{
		public static  String PPG_A12 = "PPG_A12";
		public static  String PPG_A13 = "PPG_A13";
		public static  String PPG1_A12 = "PPG1_A12";
		public static  String PPG1_A13 = "PPG1_A13";
		public static  String PPG2_A1 = "PPG2_A1";
		public static  String PPG2_A14 = "PPG2_A14";
	}
	
	//--------- Bluetooth commands start --------------
	// Check 
	//--------- Bluetooth commands end --------------
	
	//--------- Configuration options start --------------
	
	public static final String[] ListOfPpgAdcSelection = {"Int A13","Int A12"};
	public static final Integer[] ListOfPpgAdcSelectionConfigValues = {0,1};
	public static final String[] ListOfPpg1AdcSelection = {"Int A13","Int A12"};
	public static final Integer[] ListOfPpg1AdcSelectionConfigValues = {0,1};
	public static final String[] ListOfPpg2AdcSelection = {"Int A1","Int A14"};
	public static final Integer[] ListOfPpg2AdcSelectionConfigValues = {0,1};
	
	public static final Map<String, SensorConfigOptionDetails> mConfigOptionsMapRef;
	static {
		Map<String, SensorConfigOptionDetails> aConfigMap = new LinkedHashMap<String, SensorConfigOptionDetails>(); 
		aConfigMap.put(GuiLabelConfig.PPG_ADC_SELECTION, 
				new SensorConfigOptionDetails(
						ListOfPpgAdcSelection, 
						ListOfPpgAdcSelectionConfigValues, 
						SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
						CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
		
		aConfigMap.put(GuiLabelConfig.PPG1_ADC_SELECTION, 
				new SensorConfigOptionDetails(
						ListOfPpg1AdcSelection, 
						ListOfPpg1AdcSelectionConfigValues, 
						SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
						CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe));
		
		aConfigMap.put(GuiLabelConfig.PPG2_ADC_SELECTION, 
				new SensorConfigOptionDetails(
						ListOfPpg2AdcSelection, 
						ListOfPpg2AdcSelectionConfigValues, 
						SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
						CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe));

		mConfigOptionsMapRef = Collections.unmodifiableMap(aConfigMap);
	}
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorPPG = new SensorDetailsRef(
			0x04<<(2*8), // check
			0x04<<(2*8), 
			GuiLabelSensors.PPG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, GuiLabelConfig.PPG_ADC_SELECTION, GuiLabelConfig.PPG1_ADC_SELECTION, GuiLabelConfig.PPG2_ADC_SELECTION),
			Arrays.asList(ObjectClusterSensorName.PPG_A12,ObjectClusterSensorName.PPG_A13, ObjectClusterSensorName.PPG1_A12,ObjectClusterSensorName.PPG1_A13,
					      ObjectClusterSensorName.PPG2_A1,ObjectClusterSensorName.PPG2_A14)
			);
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(ShimmerGqBle.SensorMapKey.PPG, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG));
		aMap.get(ShimmerGqBle.SensorMapKey.PPG).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;
		// Associated config options for each channel (currently used for the ChannelTileMap)
		aMap.get(Configuration.ShimmerGqBle.SensorMapKey.PPG).mListOfConfigOptionKeysAssociated = Arrays.asList(
				 Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	    


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
	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorPPG(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.PPG.toString();
	}
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		if(ShimmerDevice.isDerivedSensorsSupported(svo)){
			super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
		}
	}
	@Override
	public String getSensorName(){
		return mSensorName;
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {

//		if (svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.BTSTREAM 
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SDLOG
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.LOGANDSTREAM
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.GQ_802154
//				) {
			mConfigOptionsMap.put(GuiLabelConfig.PPG_ADC_SELECTION , mConfigOptionsMapRef.get(GuiLabelConfig.PPG_ADC_SELECTION )); 
			mConfigOptionsMap.put(GuiLabelConfig.PPG1_ADC_SELECTION, mConfigOptionsMapRef.get(GuiLabelConfig.PPG1_ADC_SELECTION)); 
			mConfigOptionsMap.put(GuiLabelConfig.PPG2_ADC_SELECTION, mConfigOptionsMapRef.get(GuiLabelConfig.PPG2_ADC_SELECTION)); 
			mConfigOptionsMap.put(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, mConfigOptionsMapRef.get(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG)); 
//		}
	}

	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		//NOT USED IN THIS CLASS
		
		return objectCluster;
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
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
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
		case(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION):
			returnValue = getPpgAdcSelectionGsrBoard();
		break;
		case(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION):
			returnValue = getPpg1AdcSelectionProto3DeluxeBoard();
		break;
		case(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION):
			returnValue = getPpg2AdcSelectionProto3DeluxeBoard();
		break;
		case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
			returnValue = getSamplingDividerPpg();
		break;
		}
		return returnValue;
	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
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
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//TODO Check if needed
	public boolean checkIfSensorEnabled(int sensorMapKey){
		if (getHardwareVersion() == HW_ID.SHIMMER_3) {
			//Used for Shimmer GSR hardware
			if(sensorMapKey==SensorMapKey.HOST_PPG_DUMMY){
				if((isSensorEnabled(SensorMapKey.HOST_PPG_A12))||(isSensorEnabled(SensorMapKey.HOST_PPG_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey== SensorMapKey.HOST_PPG1_DUMMY){
				if((isSensorEnabled(SensorMapKey.HOST_PPG1_A12))||(isSensorEnabled(SensorMapKey.HOST_PPG1_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey== SensorMapKey.HOST_PPG2_DUMMY){
				if((isSensorEnabled(SensorMapKey.HOST_PPG2_A1))||(isSensorEnabled(SensorMapKey.HOST_PPG2_A14)))
					return true;
				else
					return false;
			}
			
			return isSensorEnabled(sensorMapKey);
		}
		return false;
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
	
}
