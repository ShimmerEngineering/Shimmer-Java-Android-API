package com.shimmerresearch.sensors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DerivedSensorsBitMask;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

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
	
	
	public static class SDLogHeaderDerivedSensors{
		public final static int PPG2_1_14 = 1<<4;
		public final static int PPG1_12_13 = 1<<3;
		public final static int PPG_12_13 = 1<<2;
	}
	
	public static class BTStreamDerivedSensors{
		public final static int PPG2_1_14 = 1<<4;
		public final static int PPG1_12_13 = 1<<3;
		public final static int PPG_12_13 = 1<<2;
	}
	

	public static class GuiLabelConfig{
		public static final String SAMPLING_RATE_DIVIDER_PPG = "PPG Divider";
		public static final String PPG_ADC_SELECTION = "PPG Channel";
		public static final String PPG1_ADC_SELECTION = "Channel1";
		public static final String PPG2_ADC_SELECTION = "Channel2";
	}
	
	public static class LABEL_SENSOR_TILE{
		public static final String PROTO3_DELUXE_SUPP = "PPG";
	}


	public static class GuiLabelSensors{
		//BLE
		public static final String PPG = "PPG";
		
		//Other
		public static final String PPG_DUMMY = "PPG";
		public static final String PPG_A12 = "PPG A12";
		public static final String PPG_A13 = "PPG A13";
		public static final String PPG1_DUMMY = "PPG1";
		public static final String PPG1_A12 = "PPG1 A12";
		public static final String PPG1_A13 = "PPG1 A13";
		public static final String PPG2_DUMMY = "PPG2";
		public static final String PPG2_A1 = "PPG2 A1";
		public static final String PPG2_A14 = "PPG2 A14";
		
		public static final String PPG_A0 = "PPG A0";
		public static final String PPG_A1 = "PPG A1";
		public static final String PPG1_A0 = "PPG1 A0";
		public static final String PPG1_A1 = "PPG1 A1";
		public static final String PPG2_A2 = "PPG2 A2";
		public static final String PPG2_A3 = "PPG2 A3";
	}

	 
	public class DatabaseChannelHandles{
		public static final String PPG_A12 = "F5437a_PPG_A12";
		public static final String PPG_A13 = "F5437a_PPG_A13";
		public static final String PPG1_A12 = "F5437a_PPG1_A12";
		public static final String PPG1_A13 = "F5437a_PPG1_A13";
		public static final String PPG2_A1 = "F5437a_PPG2_A1";
		public static final String PPG2_A14 = "F5437a_PPG2_A14";
		
		public static final String PPG_A0 = "F5437a_PPG_A0";
		public static final String PPG_A1 = "F5437a_PPG_A1";
		public static final String PPG1_A0 = "F5437a_PPG1_A0";
		public static final String PPG1_A1 = "F5437a_PPG1_A1";
		public static final String PPG2_A2 = "F5437a_PPG2_A2";
		public static final String PPG2_A3 = "F5437a_PPG2_A3";
	}
	
	public static class DatabaseConfigHandle{
		public static final String PPG_ADC_SELECTION_BOARD = "PPG_ADC_Selection_Board";
		public static final String PPG1_ADC_SELECTION_BOARD = "PPG1_ADC_Selection_Board";
		public static final String PPG2_ADC_SELECTION_BOARD = "PPG2_ADC_Selection_Board";
	}
	
	public static class ObjectClusterSensorName{
		public static  String PPG_A12 = "PPG_A12";
		public static  String PPG_A13 = "PPG_A13";
		public static  String PPG1_A12 = "PPG1_A12";
		public static  String PPG1_A13 = "PPG1_A13";
		public static  String PPG2_A1 = "PPG2_A1";
		public static  String PPG2_A14 = "PPG2_A14";
		
		public static  String PPG_A0 = "PPG_A0";
		public static  String PPG_A1 = "PPG_A1";
		public static  String PPG1_A0 = "PPG1_A0";
		public static  String PPG1_A1 = "PPG1_A1";
		public static  String PPG2_A2 = "PPG2_A2";
		public static  String PPG2_A3 = "PPG2_A3";
	}
	//--------- Sensor specific variables End --------------

	
	//--------- Bluetooth commands start --------------
	
	//			Not in this class
	
	//--------- Bluetooth commands end --------------
	
	
	//--------- Configuration options start --------------
	
	public static final String[] ListOfPpgAdcSelection = {"Int A13","Int A12"};
	public static final String[] ListOfPpgAdcSelection3r = {"Int A1","Int A0"};
	public static final Integer[] ListOfPpgAdcSelectionConfigValues = {0,1};
	public static final String[] ListOfPpg1AdcSelection = {"Int A13","Int A12"};
	public static final String[] ListOfPpg1AdcSelection3r = {"Int A1","Int A0"};
	public static final Integer[] ListOfPpg1AdcSelectionConfigValues = {0,1};
	public static final String[] ListOfPpg2AdcSelection = {"Int A1","Int A14"};
	public static final String[] ListOfPpg2AdcSelection3r = {"Int A3","Int A2"};
	public static final Integer[] ListOfPpg2AdcSelectionConfigValues = {0,1};
	
	public static final Integer[] FixedConflictingSensorIds = {
		Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A1,
		Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A14,
		Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
		Configuration.Shimmer3.SENSOR_ID.HOST_EMG, 
		Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
		Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM,
		Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION, 
		Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
		Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
		Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP};

    static List <Integer> FixedConflictingSensorIdsList = Arrays.asList(FixedConflictingSensorIds);
    
    public static final ConfigOptionDetailsSensor configOptionPpgAdcSelection = new ConfigOptionDetailsSensor(
    		SensorPPG.GuiLabelConfig.PPG_ADC_SELECTION,
    		null,
    		ListOfPpgAdcSelection, 
			ListOfPpgAdcSelectionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr);
    
    public static final ConfigOptionDetailsSensor configOptionPpgAdcSelection3r = new ConfigOptionDetailsSensor(
    		SensorPPG.GuiLabelConfig.PPG_ADC_SELECTION,
    		null,
    		ListOfPpgAdcSelection3r, 
			ListOfPpgAdcSelectionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr);
    
    public static final ConfigOptionDetailsSensor configOptionPpg1AdcSelection = new ConfigOptionDetailsSensor(
    		SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION,
    		null,
    		ListOfPpg1AdcSelection, 
			ListOfPpg1AdcSelectionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe);
    
    public static final ConfigOptionDetailsSensor configOptionPpg1AdcSelection3r = new ConfigOptionDetailsSensor(
    		SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION,
    		null,
    		ListOfPpg1AdcSelection3r, 
			ListOfPpg1AdcSelectionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe);
    
    public static final ConfigOptionDetailsSensor configOptionPpg2AdcSelection = new ConfigOptionDetailsSensor(
    		SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION,
    		null,
    		ListOfPpg2AdcSelection, 
			ListOfPpg2AdcSelectionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe);
    
    public static final ConfigOptionDetailsSensor configOptionPpg2AdcSelection3r = new ConfigOptionDetailsSensor(
    		SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION,
    		null,
    		ListOfPpg2AdcSelection3r, 
			ListOfPpg2AdcSelectionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe);
    
	//--------- Configuration options end --------------
	

	//--------- Sensor info start --------------	
	
	public static final SensorDetailsRef sensorPpgDummy = new SensorDetailsRef(
			0, 
			0,
			GuiLabelSensors.PPG_DUMMY,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			null,
			Arrays.asList(GuiLabelConfig.PPG_ADC_SELECTION),
			null,
			true);	
	{
		sensorPpgDummy.mIsDummySensor = true;
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG_A12 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG_A12,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			getListSensorPpgHostPPG_A12(), // RM passed in list into constructor as settings the list outside of constructor wasn't working
			null,
			Arrays.asList(ObjectClusterSensorName.PPG_A12),
			true);
	{
		List<Integer> listOfKeysConflicting =  new ArrayList<Integer>();
		listOfKeysConflicting.addAll(FixedConflictingSensorIdsList);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13);
		sensorPpgHostPPG_A12.mListOfSensorIdsConflicting = Collections.unmodifiableList(listOfKeysConflicting);
		
//		sensorPpgHostPPG_A12.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG_12_13; 
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG_A0 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG_A0,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			getListSensorPpgHostPPG_A12(), // RM passed in list into constructor as settings the list outside of constructor wasn't working
			null,
			Arrays.asList(ObjectClusterSensorName.PPG_A0),
			true);
	{
		List<Integer> listOfKeysConflicting =  new ArrayList<Integer>();
		listOfKeysConflicting.addAll(FixedConflictingSensorIdsList);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13);
		sensorPpgHostPPG_A0.mListOfSensorIdsConflicting = Collections.unmodifiableList(listOfKeysConflicting);
		
//		sensorPpgHostPPG_A12.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG_12_13; 
	}
	
	/**
	 * Hack method put in to return list of conflicting sensors for PPG A12 as the setting the list value
	 * outside of the constructor was not working (talk to MN)
	 * @return listOfConflictingSensorsForPPG_A12
	 */
	//TODO Talk to MN about replacing
	public static final List<Integer> getListSensorPpgHostPPG_A12(){
		List<Integer> listOfKeysConflicting =  new ArrayList<Integer>();
		listOfKeysConflicting.addAll(FixedConflictingSensorIdsList);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13);
		
		return listOfKeysConflicting;
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG_A13 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG_A13,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			getListSensorPpgHostPPG_A13(), // RM passed in list into constructor as settings the list outside of constructor wasn't working
			null,
			Arrays.asList(ObjectClusterSensorName.PPG_A13),
			true);
	
	{
		List<Integer> listOfKeysConflicting =  new ArrayList<Integer>();
		listOfKeysConflicting.addAll(FixedConflictingSensorIdsList);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13);
		sensorPpgHostPPG_A13.mListOfSensorIdsConflicting = Collections.unmodifiableList(listOfKeysConflicting);
		
//		sensorPpgHostPPG_A13.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG_12_13; 
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG_A1 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG_A1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			getListSensorPpgHostPPG_A13(), // RM passed in list into constructor as settings the list outside of constructor wasn't working
			null,
			Arrays.asList(ObjectClusterSensorName.PPG_A1),
			true);
	
	{
		List<Integer> listOfKeysConflicting =  new ArrayList<Integer>();
		listOfKeysConflicting.addAll(FixedConflictingSensorIdsList);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13);
		sensorPpgHostPPG_A1.mListOfSensorIdsConflicting = Collections.unmodifiableList(listOfKeysConflicting);
		
//		sensorPpgHostPPG_A13.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG_12_13; 
	}
	
	/**
	 * Hack method put in to return list of conflicting sensors for PPG A13as the setting the list value
	 * outside of the constructor was not working (talk to MN)
	 * @return listOfConflictingSensorsForPPG_A13
	 */
	//TODO Talk to MN about replacing
	public static final List<Integer> getListSensorPpgHostPPG_A13(){
		List<Integer> listOfKeysConflicting =  new ArrayList<Integer>();
		listOfKeysConflicting.addAll(FixedConflictingSensorIdsList);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12);
		listOfKeysConflicting.add(Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13);
		
		return listOfKeysConflicting;
	}
	
	// Derived Channels - Proto3 Board
	public static final SensorDetailsRef sensorPpg1Dummy = new SensorDetailsRef(
			0, 
			0, 
			GuiLabelSensors.PPG1_DUMMY,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			null,
			Arrays.asList(GuiLabelConfig.PPG1_ADC_SELECTION),
			null,
			true);
	{
		sensorPpg1Dummy.mIsDummySensor = true;
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG1_A12 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG1_A12,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG1_A12),
			true);
//	{
//		sensorPpgHostPPG1_A12.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG1_12_13; 
//	}
	public static final SensorDetailsRef sensorPpgHostPPG1_A0 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A12Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG1_A0,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG1_A0),
			true);
	
	public static final SensorDetailsRef sensorPpgHostPPG1_A13 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG1_A13,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG1_A13),
			true);
//	{
//		sensorPpgHostPPG1_A13.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG1_12_13; 
//	}
	public static final SensorDetailsRef sensorPpgHostPPG1_A1 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A13Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG1_A1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG1_A1),
			true);
	
	public static final SensorDetailsRef sensorPpg2Dummy = new SensorDetailsRef(
			0, 0, 
			GuiLabelSensors.PPG2_DUMMY,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			null,
			Arrays.asList(GuiLabelConfig.PPG2_ADC_SELECTION),
			null,
			true);
	{
		sensorPpg2Dummy.mIsDummySensor = true;
	}
	
	public static final SensorDetailsRef sensorPpgHostPPG2_A1 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG2_A1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG2_A1),
			true);
//	{
//		sensorPpgHostPPG2_A1.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG2_1_14; 
//	}
	public static final SensorDetailsRef sensorPpgHostPPG2_A3 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A1Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG2_A3,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG2_A3),
			true);
	
	public static final SensorDetailsRef sensorPpgHostPPG2_A14 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A14Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A14Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG2_A14,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG2_A14),
			true);
//	{
//		sensorPpgHostPPG2_A1.mDerivedSensorBitmapID = ShimmerObject.DerivedSensorsBitMask.PPG2_1_14; 
//	}
	public static final SensorDetailsRef sensorPpgHostPPG2_A2 = new SensorDetailsRef(
			SensorADC.sensorADC_INT_EXP_ADC_A14Ref.mSensorBitmapIDStreaming,
			SensorADC.sensorADC_INT_EXP_ADC_A14Ref.mSensorBitmapIDSDLogHeader,
			GuiLabelSensors.PPG2_A2,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe,
			Arrays.asList(
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_GSR,
					Configuration.Shimmer3.SENSOR_ID.HOST_ECG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EMG,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_TEST,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SENSOR_ID.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_16BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG1_24BIT,
//					Configuration.Shimmer3.SENSOR_ID.EXG2_24BIT,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.PPG2_A2),
			true);
	    
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();

    	// Derived Channels - GSR Board
        
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY, sensorPpgDummy);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12, sensorPpgHostPPG_A12);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13, sensorPpgHostPPG_A13);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY,sensorPpg1Dummy);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12, sensorPpgHostPPG1_A12);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13, sensorPpgHostPPG1_A13);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY ,sensorPpg2Dummy);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1, sensorPpgHostPPG2_A1);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14, sensorPpgHostPPG2_A14);

        mSensorMapRef = Collections.unmodifiableMap(aMap);
       }
    
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef3r;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();

    	// Derived Channels - GSR Board
        
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY, sensorPpgDummy);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12, sensorPpgHostPPG_A0);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13, sensorPpgHostPPG_A1);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY,sensorPpg1Dummy);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12, sensorPpgHostPPG1_A0);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13, sensorPpgHostPPG1_A1);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY ,sensorPpg2Dummy);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1, sensorPpgHostPPG2_A3);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14, sensorPpgHostPPG2_A2);

        mSensorMapRef3r = Collections.unmodifiableMap(aMap);
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
	
	 // PPG - Using GSR+ board
	public static final ChannelDetails channelPPG_A0 = new ChannelDetails(
			ObjectClusterSensorName.PPG_A0,
			ObjectClusterSensorName.PPG_A0,
			DatabaseChannelHandles.PPG_A0,
			CHANNEL_DATA_TYPE.UINT14, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG_A1 = new ChannelDetails(
			ObjectClusterSensorName.PPG_A1,
			ObjectClusterSensorName.PPG_A1,
			DatabaseChannelHandles.PPG_A1,
			CHANNEL_DATA_TYPE.UINT14, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// PPG - Using Proto3 Deluxe TRRS Socket 1
	
	public static final ChannelDetails channelPPG1_A0 = new ChannelDetails(
			ObjectClusterSensorName.PPG1_A0,
			ObjectClusterSensorName.PPG1_A0,
			DatabaseChannelHandles.PPG1_A0,
			CHANNEL_DATA_TYPE.UINT14, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG1_A1 = new ChannelDetails(
			ObjectClusterSensorName.PPG1_A1,
			ObjectClusterSensorName.PPG1_A1,
			DatabaseChannelHandles.PPG1_A1,
			CHANNEL_DATA_TYPE.UINT14, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// PPG - Using Proto3 Deluxe TRRS Socket 2
	public static final ChannelDetails channelPPG2_A3 = new ChannelDetails(
			ObjectClusterSensorName.PPG2_A3,
			ObjectClusterSensorName.PPG2_A3,
			DatabaseChannelHandles.PPG2_A3,
			CHANNEL_DATA_TYPE.UINT14, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelPPG2_A2 = new ChannelDetails(
			ObjectClusterSensorName.PPG2_A2,
			ObjectClusterSensorName.PPG2_A2,
			DatabaseChannelHandles.PPG2_A2,
			CHANNEL_DATA_TYPE.UINT14, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	
	public static final Map<String, ChannelDetails> mChannelMapRef;
	static {
		 Map<String, ChannelDetails> aChannelMap = new LinkedHashMap<String, ChannelDetails>();
		
		// PPG - Using GSR+ board
		aChannelMap.put(ObjectClusterSensorName.PPG_A12, SensorPPG.channelPPG_A12);
		aChannelMap.put(ObjectClusterSensorName.PPG_A13, SensorPPG.channelPPG_A13); 
		// PPG - Using Proto3 Deluxe TRRS Socket 1
		aChannelMap.put(ObjectClusterSensorName.PPG1_A12, SensorPPG.channelPPG1_A12); 
		aChannelMap.put(ObjectClusterSensorName.PPG1_A13, SensorPPG.channelPPG1_A13);
		// PPG - Using Proto3 Deluxe TRRS Socket 2
		aChannelMap.put(ObjectClusterSensorName.PPG2_A1, SensorPPG.channelPPG2_A1);
		aChannelMap.put(ObjectClusterSensorName.PPG2_A14, SensorPPG.channelPPG2_A14);

		mChannelMapRef = Collections.unmodifiableMap(aChannelMap);
	}
	
	public static final Map<String, ChannelDetails> mChannelMapRef3r;
	static {
		 Map<String, ChannelDetails> aChannelMap = new LinkedHashMap<String, ChannelDetails>();
		
		// PPG - Using GSR+ board
		aChannelMap.put(ObjectClusterSensorName.PPG_A0, SensorPPG.channelPPG_A0);
		aChannelMap.put(ObjectClusterSensorName.PPG_A1, SensorPPG.channelPPG_A1); 
		// PPG - Using Proto3 Deluxe TRRS Socket 1
		aChannelMap.put(ObjectClusterSensorName.PPG1_A0, SensorPPG.channelPPG1_A0); 
		aChannelMap.put(ObjectClusterSensorName.PPG1_A1, SensorPPG.channelPPG1_A1);
		// PPG - Using Proto3 Deluxe TRRS Socket 2
		aChannelMap.put(ObjectClusterSensorName.PPG2_A3, SensorPPG.channelPPG2_A3);
		aChannelMap.put(ObjectClusterSensorName.PPG2_A2, SensorPPG.channelPPG2_A2);

		mChannelMapRef3r = Collections.unmodifiableMap(aChannelMap);
	}
	
// --------------------------- Channel info end ----------------------------------------
	
	
//--------- Constructors for this class start --------------	
	/** Constructor for this Sensor
	 * @param svo
	 */
//	public SensorPPG(ShimmerVerObject svo) {
//		super(svo);
//		setSensorName(SENSORS.PPG.toString());
//	}
	
	public SensorPPG(ShimmerDevice shimmerDevice) {
		super(SENSORS.PPG, shimmerDevice);
		initialise();
	}

//--------- Constructors for this class end --------------


//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap() {
		//TODO either use createLocalSensorMap or fill in the "processDataCustom" method
		if(getHardwareVersion() == HW_ID.SHIMMER_3R) {
			super.createLocalSensorMapWithCustomParser(mSensorMapRef3r, mChannelMapRef3r);
		} else {
			super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		}

		//Update the derived sensor bit index
		for(Integer sensorId:mSensorMap.keySet()){
			long derivedSensorBitmapID = 0;
			if(sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12
					||sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13){
				derivedSensorBitmapID = DerivedSensorsBitMask.PPG_12_13;
			}
			else if(sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12
					||sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13){
				derivedSensorBitmapID = DerivedSensorsBitMask.PPG1_12_13;
			}
			else if(sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1
					||sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14){
				derivedSensorBitmapID = DerivedSensorsBitMask.PPG2_1_14;
			}
			
			if(derivedSensorBitmapID>0){
				SensorDetails sensorPpg = mSensorMap.get(sensorId);
				sensorPpg.mDerivedSensorBitmapID = derivedSensorBitmapID;
			}
		}
	}
	 
	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		if(getHardwareVersion() == HW_ID.SHIMMER_3R) {
			addConfigOption(configOptionPpgAdcSelection3r);
			addConfigOption(configOptionPpg1AdcSelection3r);
			addConfigOption(configOptionPpg2AdcSelection3r);
		} else {
			addConfigOption(configOptionPpgAdcSelection);
			addConfigOption(configOptionPpg1AdcSelection);
			addConfigOption(configOptionPpg2AdcSelection);
		}


//		mConfigOptionsMap.put(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, mConfigOptionsMapRef.get(GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG)); 
	}
	
	@Override
	public void generateSensorGroupMapping() {
		int groupIndex = Configuration.Shimmer3.LABEL_SENSOR_TILE.PROTO3_DELUXE_SUPP.ordinal();
		
		if(mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					LABEL_SENSOR_TILE.PROTO3_DELUXE_SUPP,
					Arrays.asList(
//							Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY,
							Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY,
							Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
		}
		super.updateSensorGroupingMap();	
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		return SensorADC.processMspAdcChannel(sensorDetails, rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		//XXX What about this?
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		//XXX What about this?
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		////XXX What about this? Are the newly introduced method handling the commented out stuff below?
		
		Object returnValue = null;
		int buf = 0;

		switch(configLabel){
			case(GuiLabelConfig.PPG_ADC_SELECTION):
				setPpgAdcSelectionGsrBoard((int)valueToSet);
	    		break;
			case(GuiLabelConfig.PPG1_ADC_SELECTION):
				setPpg1AdcSelectionProto3DeluxeBoard((int)valueToSet);
	    		break;
			case(GuiLabelConfig.PPG2_ADC_SELECTION):
				setPpg2AdcSelectionProto3DeluxeBoard((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
				setSamplingDividerPpg((int)valueToSet);
	    		break;
		}
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
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
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
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
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(DatabaseConfigHandle.PPG_ADC_SELECTION_BOARD, getPpgAdcSelectionGsrBoard());
		mapOfConfig.put(DatabaseConfigHandle.PPG1_ADC_SELECTION_BOARD, getPpg1AdcSelectionProto3DeluxeBoard());
		mapOfConfig.put(DatabaseConfigHandle.PPG2_ADC_SELECTION_BOARD, getPpg2AdcSelectionProto3DeluxeBoard());

		return mapOfConfig;
	}
	
	@Override
	public void parseConfigMap(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}
	//--------- Abstract methods implemented end --------------

	
	//--------- Sensor specific methods start --------------
	
	public boolean checkIfSensorEnabled(int sensorId){
		//TODO update for proper Shimmer4 sensors
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			//Used for Shimmer GSR hardware
			if(sensorId==Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12))||(isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13)))
					return true;
				else
					return false;
			}
			else if(sensorId== Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12))||(isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13)))
					return true;
				else
					return false;
			}
			else if(sensorId== Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1))||(isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14)))
					return true;
				else
					return false;
			}
			
			return isSensorEnabled(sensorId);
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
	 * @param ppgAdcSelectionGsrBoard the mPpgAdcSelectionGsrBoard to set
	 */
	protected void setPpgAdcSelectionGsrBoard(int ppgAdcSelectionGsrBoard) {
		this.mPpgAdcSelectionGsrBoard = ppgAdcSelectionGsrBoard;
		int key = Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY;
		mShimmerDevice.setSensorEnabledState(key, mSensorMap.get(key).isEnabled());
	}

	/**
	 * @param ppg1AdcSelectionProto3DeluxeBoard the mPpg1AdcSelectionProto3DeluxeBoard to set
	 */
	protected void setPpg1AdcSelectionProto3DeluxeBoard(int ppg1AdcSelectionProto3DeluxeBoard) {
		this.mPpg1AdcSelectionProto3DeluxeBoard = ppg1AdcSelectionProto3DeluxeBoard;
		int key = Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY;
		mShimmerDevice.setSensorEnabledState(key, mSensorMap.get(key).isEnabled());
	}

	/**
	 * @param ppg2AdcSelectionProto3DeluxeBoard the mPpg2AdcSelectionProto3DeluxeBoard to set
	 */
	protected void setPpg2AdcSelectionProto3DeluxeBoard(int ppg2AdcSelectionProto3DeluxeBoard) {
		this.mPpg2AdcSelectionProto3DeluxeBoard = ppg2AdcSelectionProto3DeluxeBoard;
		int key = Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY;
		mShimmerDevice.setSensorEnabledState(key, mSensorMap.get(key).isEnabled());
	}
	
	/** ShimmerGQBle
	 * @param mSamplingDividerPpg the mSamplingDividerPpg to set
	 */
	public void setSamplingDividerPpg(int mSamplingDividerPpg) {
		this.mSamplingDividerPpg = mSamplingDividerPpg;
	}

	/**
	 * @return the mSamplingDividerPpg
	 */
	public int getSamplingDividerPpg() {
		return mSamplingDividerPpg;
	}

	//--------- Optional methods to override in Sensor Class start --------	
	@Override
	public boolean handleSpecCasesBeforeSensorMapUpdatePerSensor(ShimmerDevice shimmerDevice, Integer sensorId){
		if(((sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A12)
				||(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A13)
				||(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A1)
				||(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_INT_EXP_ADC_A14))){
			shimmerDevice.ignoreAndDisable(sensorId);
			return true;
		}

		return false;
	}

	
	@Override
	public void handleSpecCasesAfterSensorMapUpdateFromEnabledSensors() {
		//Used for Shimmer GSR hardware
		if (mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12).isEnabled()) {
					mPpgAdcSelectionGsrBoard = ListOfPpgAdcSelectionConfigValues[1]; // PPG_A12
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13).isEnabled()) {
					mPpgAdcSelectionGsrBoard = ListOfPpgAdcSelectionConfigValues[0]; // PPG_A13

				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY).setIsEnabled(false);

			}
		}
		//Used for Shimmer Proto3 Deluxe hardware
		if (mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12).isEnabled()) {
					mPpg1AdcSelectionProto3DeluxeBoard = ListOfPpg1AdcSelectionConfigValues[1]; // PPG1_A12
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13).isEnabled()) {
					mPpg1AdcSelectionProto3DeluxeBoard = ListOfPpg1AdcSelectionConfigValues[0]; // PPG1_A13
				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY).setIsEnabled(false);
			}
		}
		//Used for Shimmer Proto3 Deluxe hardware
		if (mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1).isEnabled()) {
					mPpg2AdcSelectionProto3DeluxeBoard = ListOfPpg2AdcSelectionConfigValues[0]; // PPG2_A1
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14).isEnabled()) {
					mPpg2AdcSelectionProto3DeluxeBoard = ListOfPpg2AdcSelectionConfigValues[1]; // PPG2_A14
				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY).setIsEnabled(false);
			}
		}
	}

	
	@Override
	public int handleSpecCasesBeforeSetSensorState(int sensorId, boolean state) {
		// Special case for Dummy entries in the Sensor Map
		if(sensorId == Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY) {
			mSensorMap.get(sensorId).setIsEnabled(state);
			if(ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
				return Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A12;
			}
			else {
				return Configuration.Shimmer3.SENSOR_ID.HOST_PPG_A13;
			}
		}		
		else if(sensorId == Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_DUMMY) {
			mSensorMap.get(sensorId).setIsEnabled(state);
			if(ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
				return Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A12;
			}
			else {
				return Configuration.Shimmer3.SENSOR_ID.HOST_PPG1_A13;
			}
		}		
		else if(sensorId == Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_DUMMY) {
			mSensorMap.get(sensorId).setIsEnabled(state);
			if(ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
				return Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A14;
			}
			else {
				return Configuration.Shimmer3.SENSOR_ID.HOST_PPG2_A1;
			}
		}	
		return sensorId;
	}
	//--------- Optional methods to override in Sensor Class end --------


}
