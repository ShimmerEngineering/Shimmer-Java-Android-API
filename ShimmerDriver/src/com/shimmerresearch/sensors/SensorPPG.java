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
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

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
	
	
	public SensorPPG(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.PPG.toString();
	}
	
	
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

	public class SensorMapKey{
		public static final int PPG = 107;
	}
	//GUI SENSORs
	public class GuiLabelSensors{
		public static final String PPG_TO_HR = "PPG To HR";
		public static final String PPG = "PPG";
	}
	public class GuiLabelConfig{
		public static final String SAMPLING_RATE_DIVIDER_PPG = "PPG Divider";
	}
	
	public static class ObjectClusterSensorName{
		public static  String PPG_A12 = "PPG_A12";
		public static  String PPG_A13 = "PPG_A13";
		public static  String PPG1_A12 = "PPG1_A12";
		public static  String PPG1_A13 = "PPG1_A13";
		public static  String PPG2_A1 = "PPG2_A1";
		public static  String PPG2_A14 = "PPG2_A14";
		
		public static  String PPG_TO_HR = "PPGtoHR"; // check if needed?
	}
	
	
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();

		aMap.put(SensorMapKey.PPG, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG));

		aMap.get(SensorMapKey.PPG).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;

		// Associated config options for each channel (currently used for the ChannelTileMap)
		aMap.get(Configuration.ShimmerGqBle.SensorMapKey.PPG).mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	    
	    public static final Map<String, SensorConfigOptionDetails> mConfigOptionsMapRef;
	    static {
	    	Map<String, SensorConfigOptionDetails> aConfigMap = new LinkedHashMap<String, SensorConfigOptionDetails>(); 
	    	aConfigMap.put(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION, 
	    			new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfPpgAdcSelection, 
	    					Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues, 
	    					SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
	    					CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
	    	aConfigMap.put(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION, 
	    			new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfPpg1AdcSelection, 
	    					Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues, 
	    					SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
	    					CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe));
	    	aConfigMap.put(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION, 
	    			new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfPpg2AdcSelection, 
	    					Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues, 
	    					SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
	    					CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe));

	    	mConfigOptionsMapRef = Collections.unmodifiableMap(aConfigMap);
	    }
	    
	  //--------- Channel info start --------------
	 // PPG - Using GSR+ board
	public static final ChannelDetails channelPPG_A12 = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG_A12,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG_A12,
			DatabaseChannelHandles.PPG_A12,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG_A13 = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG_A13,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG_A13,
			DatabaseChannelHandles.PPG_A13,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// PPG - Using Proto3 Deluxe TRRS Socket 1
	
	public static final ChannelDetails channelPPG1_A12 = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A12,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A12,
			DatabaseChannelHandles.PPG1_A12,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG1_A13 = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A13,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A13,
			DatabaseChannelHandles.PPG1_A13,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// PPG - Using Proto3 Deluxe TRRS Socket 2
	public static final ChannelDetails channelPPG2_A1 = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A1,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A1,
			DatabaseChannelHandles.PPG2_A1,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG2_A14 = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A14,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A14,
			DatabaseChannelHandles.PPG2_A14,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG_TO_HR =new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,
			Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,
			DatabaseChannelHandles.PPG_TO_HR,
			CHANNEL_UNITS.BEATS_PER_MINUTE,
			Arrays.asList(CHANNEL_TYPE.CAL));
	//--------- Channel info end --------------
	
	public static final Map<String, ChannelDetails> mChannelMapRef;
	static {
		 Map<String, ChannelDetails> aChannelMap = new LinkedHashMap<String, ChannelDetails>();
		
		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG_A12, SensorPPG.channelPPG1_A12);
		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG_A13, SensorPPG.channelPPG_A13); 
		// PPG - Using Proto3 Deluxe TRRS Socket 1
		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A12, SensorPPG.channelPPG1_A12); 
		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A13, SensorPPG.channelPPG1_A13);
		// PPG - Using Proto3 Deluxe TRRS Socket 2
		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A1, SensorPPG.channelPPG2_A1);
		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A14, SensorPPG.channelPPG2_A14);

		aChannelMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR, SensorPPG.channelPPG_TO_HR);
		mChannelMapRef = Collections.unmodifiableMap(aChannelMap);
	}
	

	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,
			byte[] sensorByteArray, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean state) {
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
	
}
