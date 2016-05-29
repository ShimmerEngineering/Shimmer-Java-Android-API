package com.shimmerresearch.sensors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
		
//		aMap.put(SensorMapKey.HOST_PPG_DUMMY, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG_DUMMY));
//		aMap.put(SensorMapKey.HOST_PPG_A12, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG_A12));
//		aMap.get(SensorMapKey.HOST_PPG_A12).mSensorBitmapIDStreaming = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
//		aMap.get(SensorMapKey.HOST_PPG_A12).mSensorBitmapIDSDLogHeader = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
//		aMap.get(SensorMapKey.PPG_A12).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg_ADC12ADC13;
//		aMap.put(SensorMapKey.HOST_PPG_A13, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG_A13));
//		aMap.get(SensorMapKey.HOST_PPG_A13).mSensorBitmapIDStreaming = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
//		aMap.get(SensorMapKey.HOST_PPG_A13).mSensorBitmapIDSDLogHeader = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
//		aMap.get(SensorMapKey.PPG_A13).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg_ADC12ADC13;
//		
//		// Derived Channels - Proto3 Board
//		aMap.put(SensorMapKey.HOST_PPG1_DUMMY, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG1_DUMMY));
//		aMap.put(SensorMapKey.HOST_PPG1_A12, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG1_A12));
//		aMap.get(SensorMapKey.HOST_PPG1_A12).mSensorBitmapIDStreaming = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
//		aMap.get(SensorMapKey.HOST_PPG1_A12).mSensorBitmapIDSDLogHeader = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
//		aMap.get(SensorMapKey.PPG1_A12).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg1_ADC12ADC13;
		
		
//		aMap.put(SensorMapKey.HOST_PPG1_A13, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG1_A13));
//		aMap.get(SensorMapKey.HOST_PPG1_A13).mSensorBitmapIDStreaming = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
//		aMap.get(SensorMapKey.HOST_PPG1_A13).mSensorBitmapIDSDLogHeader = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
//		aMap.get(SensorMapKey.PPG1_A13).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg1_ADC12ADC13;
		
		
//		aMap.put(SensorMapKey.HOST_PPG2_DUMMY, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG2_DUMMY));
		
//		aMap.put(SensorMapKey.HOST_PPG2_A1, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG2_A1));
//		aMap.get(SensorMapKey.HOST_PPG2_A1).mSensorBitmapIDStreaming = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A1).mSensorBitmapIDStreaming;
//		aMap.get(SensorMapKey.HOST_PPG2_A1).mSensorBitmapIDSDLogHeader = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A1).mSensorBitmapIDSDLogHeader;
//		aMap.get(SensorMapKey.PPG2_A1).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg2_ADC1ADC14;
//		aMap.put(SensorMapKey.HOST_PPG2_A14, new SensorDetailsRef(0, 0, GuiLabelSensors.PPG2_A14));
//		aMap.get(SensorMapKey.HOST_PPG2_A14).mSensorBitmapIDStreaming = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A14).mSensorBitmapIDStreaming;
//		aMap.get(SensorMapKey.HOST_PPG2_A14).mSensorBitmapIDSDLogHeader = aMap.get(SensorMapKey.SHIMMER_INT_EXP_ADC_A14).mSensorBitmapIDSDLogHeader;
//		aMap.get(SensorMapKey.PPG2_A14).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg2_ADC1ADC14;

//		aMap.get(SensorMapKey.HOST_PPG_DUMMY).mIsDummySensor = true;
//		aMap.get(SensorMapKey.HOST_PPG1_DUMMY).mIsDummySensor = true;
//		aMap.get(SensorMapKey.HOST_PPG2_DUMMY).mIsDummySensor = true;
		
//		aMap.get(SensorMapKey.HOST_PPG_DUMMY).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
//		aMap.get(SensorMapKey.HOST_PPG_A12).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
//		aMap.get(SensorMapKey.HOST_PPG_A13).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
//		aMap.get(SensorMapKey.HOST_PPG1_DUMMY).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
//		aMap.get(SensorMapKey.HOST_PPG1_A12).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
//		aMap.get(SensorMapKey.HOST_PPG1_A13).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
//		aMap.get(SensorMapKey.HOST_PPG2_DUMMY).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
//		aMap.get(SensorMapKey.HOST_PPG2_A1).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
//		aMap.get(SensorMapKey.HOST_PPG2_A14).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
		
//		aMap.get(SensorMapKey.HOST_PPG_A12).mIntExpBoardPowerRequired = true;
//		aMap.get(SensorMapKey.HOST_PPG_A13).mIntExpBoardPowerRequired = true;
//		aMap.get(SensorMapKey.HOST_PPG1_A12).mIntExpBoardPowerRequired = true;
//		aMap.get(SensorMapKey.HOST_PPG1_A13).mIntExpBoardPowerRequired = true;
//		aMap.get(SensorMapKey.HOST_PPG2_A1).mIntExpBoardPowerRequired = true;
//		aMap.get(SensorMapKey.HOST_PPG2_A14).mIntExpBoardPowerRequired = true;
		
		//The A12 and A13 based PPG channels have the same channel exceptions as GSR with the addition of their counterpart channel 
//		aMap.get(SensorMapKey.HOST_PPG_A12).mListOfSensorMapKeysConflicting = new ArrayList<Integer>(aMap.get(SensorMapKey.SHIMMER_GSR).mListOfSensorMapKeysConflicting);
//		aMap.get(SensorMapKey.HOST_PPG_A12).mListOfSensorMapKeysConflicting.add(SensorMapKey.HOST_PPG_A13);
//		aMap.get(SensorMapKey.HOST_PPG_A12).mListOfSensorMapKeysConflicting.add(SensorMapKey.SHIMMER_INT_EXP_ADC_A12);
//		aMap.get(SensorMapKey.HOST_PPG_A12).mListOfSensorMapKeysConflicting.add(SensorMapKey.SHIMMER_INT_EXP_ADC_A13);
//		aMap.get(SensorMapKey.HOST_PPG_A13).mListOfSensorMapKeysConflicting = new ArrayList<Integer>(aMap.get(SensorMapKey.SHIMMER_GSR).mListOfSensorMapKeysConflicting);
//		aMap.get(SensorMapKey.HOST_PPG_A13).mListOfSensorMapKeysConflicting.add(SensorMapKey.HOST_PPG_A12);
//		aMap.get(SensorMapKey.HOST_PPG_A13).mListOfSensorMapKeysConflicting.add(SensorMapKey.SHIMMER_INT_EXP_ADC_A12);
//		aMap.get(SensorMapKey.HOST_PPG_A13).mListOfSensorMapKeysConflicting.add(SensorMapKey.SHIMMER_INT_EXP_ADC_A13);
        
//		aMap.get(SensorMapKey.HOST_PPG1_A12).mListOfSensorMapKeysConflicting = Arrays.asList(
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EMG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
////				Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
//				Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13);
		
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).mListOfSensorMapKeysConflicting = Arrays.asList(
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EMG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
////				Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
//				Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13);
		
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).mListOfSensorMapKeysConflicting = Arrays.asList(
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EMG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
////				Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
//				Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14);
		
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).mListOfSensorMapKeysConflicting = Arrays.asList(
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EMG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
////				Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
//				Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14);
		
		
		
		
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION);
        
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).mListOfChannelsRef = Arrays.asList(
//				Configuration.Shimmer3.ObjectClusterSensorName.PPG_A12);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).mListOfChannelsRef = Arrays.asList(
//				Configuration.Shimmer3.ObjectClusterSensorName.PPG_A13);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).mListOfChannelsRef = Arrays.asList(
//						Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A12);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).mListOfChannelsRef = Arrays.asList(
//				Configuration.Shimmer3.ObjectClusterSensorName.PPG1_A13);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).mListOfChannelsRef = Arrays.asList(
//				Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A1);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).mListOfChannelsRef = Arrays.asList(
//				Configuration.Shimmer3.ObjectClusterSensorName.PPG2_A14);
        
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
		if(ShimmerDevice.isDerivedSensorsSupported(svo)){
			super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		}
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

	//XXX references to Configuration -> refer to GuiLabelConfig class inside SensorPPG.java
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
	public void setSensorSamplingRate() {
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
	//--------- Abstract methods implemented end --------------

	
	//--------- Sensor specific methods start --------------

	
	//XXX - is this old, can it be removed, or does it still need to be checked? 
	//TODO Check if needed
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

	//XXX is this old, can it be removed, or does it still need to be checked? 
//	/**
//	 * Used to convert from the enabledSensors long variable read from the
//	 * Shimmer to the set enabled status of the relative entries in the Sensor
//	 * Map. Used in Consensys for dynamic GUI generation to configure a Shimmer.
//	 * 
//	 */
//	@Override
//	public void sensorMapUpdateFromEnabledSensorsVars() {
//
//		checkExgResolutionFromEnabledSensorsVar();
//
//		if(mSensorMap==null){
//			sensorAndConfigMapsCreate();
//		}
//		
//		if(mSensorMap!=null) {
//
//			if (getHardwareVersion() == HW_ID.SHIMMER_3) {
//				
//				for(Integer sensorMapKey:mSensorMap.keySet()) {
//					boolean skipKey = false;
//
//					// Skip if ExG channels here -> handle them after for loop.
//					if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_ECG)
//							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EMG)
//							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST)
//							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM)
//							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION)) {
//						mSensorMap.get(sensorMapKey).setIsEnabled(false);
//						skipKey = true;
//					}
//					// Handle derived sensors based on int adc channels (e.g. PPG vs. A12/A13)
//					else if(((sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12)
//						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13)
//						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1)
//						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14))){
//
//						//Check if a derived channel is enabled, if it is ignore disable and skip 
//						innerloop:
//						for(Integer conflictKey:mSensorMap.get(sensorMapKey).mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
//							if(mSensorMap.get(conflictKey).isDerivedChannel()) {
//								if((mDerivedSensors&mSensorMap.get(conflictKey).mDerivedSensorBitmapID) == mSensorMap.get(conflictKey).mDerivedSensorBitmapID) {
//									mSensorMap.get(sensorMapKey).setIsEnabled(false);
//									skipKey = true;
//									break innerloop;
//								}
//							}
//						}
//					}
////					else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.TIMESTAMP_SYNC 
//////							|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.TIMESTAMP
//////							|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK
////							|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK_SYNC){
////						mSensorMap.get(sensorMapKey).setIsEnabled(false);
////						skipKey = true;
////					}
//					else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES){
//						mSensorMap.get(sensorMapKey).setIsEnabled(true);
//						skipKey = true;
//					}
//
//
//					// Process remaining channels
//					if(!skipKey) {
//						mSensorMap.get(sensorMapKey).setIsEnabled(false);
//						// Check if this sensor is a derived sensor
//						if(mSensorMap.get(sensorMapKey).isDerivedChannel()) {
//							//Check if associated derived channels are enabled 
//							if((mDerivedSensors&mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID) == mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID) {
//								//TODO add comment
//								if((mEnabledSensors&mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) == mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) {
//									mSensorMap.get(sensorMapKey).setIsEnabled(true);
//								}
//							}
//						}
//						// This is not a derived sensor
//						else {
//							//Check if sensor's bit in sensor bitmap is enabled
//							if((mEnabledSensors&mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) == mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) {
//								mSensorMap.get(sensorMapKey).setIsEnabled(true);
//							}
//						}
//					}
//				}
//				
//				// Now that all main sensor channels have been parsed, deal with
//				// sensor channels that have special conditions. E.g. deciding
//				// what type of signal the ExG is configured for or what derived
//				// channel is enabled like whether PPG is on ADC12 or ADC13
//				
//				//Handle ExG sensors
//				internalCheckExgModeAndUpdateSensorMap(mSensorMap);
//
//				// Handle PPG sensors so that it appears in Consensys as a
//				// single PPG channel with a selectable ADC based on different
//				// hardware versions.
//				
//				//Used for Shimmer GSR hardware
//				if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12)!=null){
//				if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).isEnabled())) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).setIsEnabled(true);
//					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).isEnabled()) {
//						mPpgAdcSelectionGsrBoard = Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues[1]; // PPG_A12
//					}
//					else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).isEnabled()) {
//						mPpgAdcSelectionGsrBoard = Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues[0]; // PPG_A13
//
//					}
//				}
//				else {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).setIsEnabled(false);
//
//				}
//				}
//				//Used for Shimmer Proto3 Deluxe hardware
//				if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12)!=null){
//				if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).isEnabled())) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).setIsEnabled(true);
//					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).isEnabled()) {
//						mPpg1AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues[1]; // PPG1_A12
//					}
//					else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).isEnabled()) {
//						mPpg1AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues[0]; // PPG1_A13
//					}
//				}
//				else {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).setIsEnabled(false);
//				}
//				}
//				//Used for Shimmer Proto3 Deluxe hardware
//				if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1)!=null){
//					if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).isEnabled())) {
//						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).setIsEnabled(true);
//						if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).isEnabled()) {
//							mPpg2AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues[0]; // PPG2_A1
//						}
//						else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).isEnabled()) {
//							mPpg2AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues[1]; // PPG2_A14
//						}
//					}
//					else {
//						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).setIsEnabled(false);
//					}
//				}
//			}
//			else if (getHardwareVersion() == HW_ID.SHIMMER_GQ_BLE) {
//				
//			}
////			interpretDataPacketFormat();
//		}
//		
//		
//		//Debugging
////		for(SensorEnabledDetails sED:mSensorEnabledMap.values()){
////			if(sED.mIsEnabled){
////				System.out.println("SENSOR enabled:\t"+ sED.mSensorDetails.mGuiFriendlyLabel);
////			}
////		}
//		
//	}
//	
	
//	@Override
//	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
//		
//		if(mSensorMap!=null) {
//			
//			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
//			
//			if (getHardwareVersion() == HW_ID.SHIMMER_3){
//				
//				// Special case for Dummy entries in the Sensor Map
//				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY) {
//					sensorDetails.setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13;
//					}
//				}		
//				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY) {
//					sensorDetails.setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13;
//					}
//				}		
//				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY) {
//					sensorDetails.setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1;
//					}
//				}		
//				
//				// Automatically handle required channels for each sensor
//				List<Integer> listOfRequiredKeys = sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired;
//				if(listOfRequiredKeys != null && listOfRequiredKeys.size()>0) {
//					for(Integer i:listOfRequiredKeys) {
//						mSensorMap.get(i).setIsEnabled(state);
//					}
//				}
//				
//			}
//			else if (getHardwareVersion() == HW_ID.SHIMMER_GQ_BLE) {
//				
//			}
//			
//			//Set sensor state
//			sensorDetails.setIsEnabled(state);
//
//			sensorMapConflictCheckandCorrect(sensorMapKey);
//			setDefaultConfigForSensor(sensorMapKey, sensorDetails.isEnabled());
//
//			// Automatically control internal expansion board power
//			checkIfInternalExpBrdPowerIsNeeded();
//			
//			refreshEnabledSensorsFromSensorMap();
//
//			boolean result = sensorDetails.isEnabled();
//			
//			return (result==state? true:false);
//			
//		}
//		else {
//			return false;
//		}
//	}
	
	//TODO 2016-05-18 feed below into sensor map classes
		/**Automatically control internal expansion board power based on sensor map
		 */
////		@Override
//		protected boolean checkIfInternalExpBrdPowerIsNeeded(){
//
//			if (getHardwareVersion() == HW_ID.SHIMMER_3){
//				for(Integer channelKey:mSensorMap.keySet()) {
//					if(mSensorMap.get(channelKey).isEnabled() && mSensorMap.get(channelKey).mSensorDetailsRef.mIntExpBoardPowerRequired) {
//						mInternalExpPower = 1;
//						break;
//					}
//					else {
//						// Exception for Int ADC sensors 
//						//TODO need to check HW version??
//						if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1)
//							||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12)
//							||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13)
//							||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14)){
//							
//						}
//						else {
//							mInternalExpPower = 0;
//						}
//					}
//				}
//			}
//			return (mInternalExpPower > 0)? true:false;
//		}
	
	
	
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
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
}
