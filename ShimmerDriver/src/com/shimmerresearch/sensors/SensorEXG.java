package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.InfoMemLayoutShimmer3;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails;
import com.shimmerresearch.exgConfig.ExGConfigOption;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTINGS;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTING_OPTIONS;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

public class SensorEXG extends AbstractSensor{

	/** * */
	private static final long serialVersionUID = -9150699518448307506L;
	
	//--------- Sensor specific variables start --------------
	protected ExGConfigBytesDetails mExGConfigBytesDetails = new ExGConfigBytesDetails(); 
	protected byte[] mEXG1RegisterArray = new byte[10];
	protected byte[] mEXG2RegisterArray = new byte[10];
	
	protected int mExGResolution = 1;
	private boolean mIsExg1_24bitEnabled = false;
	private boolean mIsExg2_24bitEnabled = false;
	private boolean mIsExg1_16bitEnabled = false;
	private boolean mIsExg2_16bitEnabled = false;

	@Deprecated
	protected int mEXG1RateSetting; //setting not value
	@Deprecated
	protected int mEXG1CH1GainSetting; // this is the setting not to be confused with the actual value
	@Deprecated
	protected int mEXG1CH1GainValue; // this is the value
	@Deprecated
	protected int mEXG1CH2GainSetting; // this is the setting not to be confused with the actual value
	@Deprecated
	protected int mEXG1CH2GainValue; // this is the value
	@Deprecated
	protected int mEXG2RateSetting; //setting not value
	@Deprecated
	protected int mEXG2CH1GainSetting; // this is the setting not to be confused with the actual value
	@Deprecated
	protected int mEXG2CH1GainValue; // this is the value
	@Deprecated
	protected int mEXG2CH2PowerDown;//Not used in ShimmerBluetooth
	@Deprecated
	protected int mEXG2CH2GainSetting; // this is the setting not to be confused with the actual value
	@Deprecated
	protected int mEXG2CH2GainValue; // this is the value
	//EXG ADVANCED
	@Deprecated
	protected int mEXGReferenceElectrode=-1;
	@Deprecated
	protected int mLeadOffDetectionMode;
	@Deprecated
	protected int mEXG1LeadOffCurrentMode;
	@Deprecated
	protected int mEXG2LeadOffCurrentMode;
	@Deprecated
	protected int mEXG1Comparators;
	@Deprecated
	protected int mEXG2Comparators;
	@Deprecated
	protected int mEXGRLDSense;
	@Deprecated
	protected int mEXG1LeadOffSenseSelection;
	@Deprecated
	protected int mEXG2LeadOffSenseSelection;
	@Deprecated
	protected int mEXGLeadOffDetectionCurrent;
	@Deprecated
	protected int mEXGLeadOffComparatorTreshold;
	@Deprecated
	protected int mEXG2RespirationDetectState;//Not used in ShimmerBluetooth
	@Deprecated
	protected int mEXG2RespirationDetectFreq;//Not used in ShimmerBluetooth
	@Deprecated
	protected int mEXG2RespirationDetectPhase;//Not used in ShimmerBluetooth
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	
//	aMap.put(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT, new SensorDetailsRef(0x10<<(0*8), 0x10<<(0*8), Configuration.Shimmer3.GuiLabelSensors.EXG1_24BIT));
//	aMap.put(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT, new SensorDetailsRef(0x08<<(0*8), 0x08<<(0*8), Configuration.Shimmer3.GuiLabelSensors.EXG2_24BIT));
//	aMap.put(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT, new SensorDetailsRef(0x10<<(2*8), 0x10<<(2*8), Configuration.Shimmer3.GuiLabelSensors.EXG1_16BIT));
//	aMap.put(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT, new SensorDetailsRef(0x08<<(2*8), 0x08<<(2*8), Configuration.Shimmer3.GuiLabelSensors.EXG2_16BIT));

	public static final SensorDetailsRef sDRefEcg = new SensorDetailsRef(0, 0, Configuration.Shimmer3.GuiLabelSensors.ECG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
					
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
//							Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
					
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT
//							Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT
					),
					true);
	public static final SensorDetailsRef sDRefExgTest = new SensorDetailsRef(0, 0, Configuration.Shimmer3.GuiLabelSensors.EXG_TEST,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
					
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,
					
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT),
			true);
	public static final SensorDetailsRef sDRefExgRespiration = new SensorDetailsRef(0, 0, Configuration.Shimmer3.GuiLabelSensors.EXG_RESPIRATION,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
					
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
					
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT),
			true);
	public static final SensorDetailsRef sDRefEmg =  new SensorDetailsRef(0, 0, Configuration.Shimmer3.GuiLabelSensors.EMG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,
					
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT),
			true);
	public static final SensorDetailsRef sDRefExgCustom =  new SensorDetailsRef(0, 0, Configuration.Shimmer3.GuiLabelSensors.EXG_CUSTOM,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
					
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,
					
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT),
			true);
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	//ExG - Status
	public static final ChannelDetails cDExg1Status = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
					DatabaseChannelHandles.EXG1_STATUS,
					CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg2Status = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
					DatabaseChannelHandles.EXG2_STATUS,
					CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.UNCAL));
	
	//ExG - General
	public static final ChannelDetails cDExg1Ch1_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
					DatabaseChannelHandles.EXG1_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg1Ch2_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
					DatabaseChannelHandles.EXG1_CH2_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	public static final ChannelDetails cDExg2Ch1_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
					DatabaseChannelHandles.EXG2_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg2Ch2_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,
					DatabaseChannelHandles.EXG2_CH2_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	public static final ChannelDetails cDExg1Ch1_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
					DatabaseChannelHandles.EXG1_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg1Ch2_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
					DatabaseChannelHandles.EXG1_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	public static final ChannelDetails cDExg2Ch1_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
					DatabaseChannelHandles.EXG2_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg2Ch2_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,
					DatabaseChannelHandles.EXG2_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	//ExG - EMG
	public static final ChannelDetails cDEmgCh1_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
					DatabaseChannelHandles.EXG1_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDEmgCh2_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,
					DatabaseChannelHandles.EXG1_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDEmgCh1_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
					DatabaseChannelHandles.EXG1_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDEmgCh2_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,
					DatabaseChannelHandles.EXG1_CH2_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	//ExG - ECG
	public static final ChannelDetails cDEcg_LL_RA_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
					DatabaseChannelHandles.EXG1_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDEcg_LA_RA_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
					DatabaseChannelHandles.EXG1_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDEcg_VX_RL_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
					DatabaseChannelHandles.EXG2_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));	
	public static final ChannelDetails cDEcg_RESP_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
					DatabaseChannelHandles.EXG2_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));	
	public static final ChannelDetails cDEcg_LL_RA_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
					DatabaseChannelHandles.EXG1_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDEcg_LA_RA_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
					DatabaseChannelHandles.EXG1_CH2_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));		
	public static final ChannelDetails cDEcg_VX_RL_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,
					DatabaseChannelHandles.EXG2_CH2_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));					
	public static final ChannelDetails cDEcg_RESP_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,
					DatabaseChannelHandles.EXG2_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));	
	
	//ECG derived
	public static final ChannelDetails cDEcg_LL_LA_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL));
	public static final ChannelDetails cDEcg_LL_LA_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL));
	
	//ExG - Test signal
	public static final ChannelDetails cDExg_Test_CHIP1_CH1_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,
					DatabaseChannelHandles.EXG1_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg_Test_CHIP1_CH2_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,
					DatabaseChannelHandles.EXG1_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg_Test_CHIP2_CH1_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,
					DatabaseChannelHandles.EXG2_CH1_16BITS,
					CHANNEL_DATA_TYPE.INT16, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));	
	public static final ChannelDetails cDExg_Test_CHIP2_CH2_16bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,
					DatabaseChannelHandles.EXG2_CH2_16BITS,
					CHANNEL_DATA_TYPE.INT16, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));	
	public static final ChannelDetails cDExg_Test_CHIP1_CH1_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,
					DatabaseChannelHandles.EXG1_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg_Test_CHIP1_CH2_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,
					DatabaseChannelHandles.EXG1_CH2_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));		
	public static final ChannelDetails cDExg_Test_CHIP2_CH1_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,
					DatabaseChannelHandles.EXG2_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails cDExg_Test_CHIP2_CH2_24bit = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,
					Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,
					DatabaseChannelHandles.EXG2_CH1_24BITS,
					CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));	
	//--------- Channel info end --------------

	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader =  0;

    public Map<String, SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();

    @Deprecated
	public Map<Integer, SensorDetails> mSensorEnabledMap;

	
	public SensorEXG(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.EXG.toString();
	}
	
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		mSensorMap.clear();
		
		//TODO load channels based on list of channels in the SensorDetailsRef rather then manually loading them here -> need to create a ChannelMapRef like in Configuration.Shimmer3 and then cycle through
		SensorDetails sensorDetails = new SensorDetails(false, 0, sDRefEcg);
		sensorDetails.mListOfChannels.add(cdhannelGsr);
		
		sensorDetails = new SensorDetails(false, 0, sDRefEcg);
		sensorDetails.mListOfChannels.add(chdhannelGsr);
		sensorDetails = new SensorDetails(false, 0, sDRefEcg);
		sensorDetails.mListOfChannels.add(cdhannelGsr);
		
		mSensorMap.put(Configuration.Shimmer3.SensorMapKey.HOST_ECG, sensorDetails);
	}
	
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_ECG,
								Configuration.Shimmer3.SensorMapKey.HOST_EMG,
								Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
								Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
								Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;

//			aMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);		
		}
		else if((svo.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR)
				||(svo.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR)
				||(svo.mHardwareVersion==HW_ID.SHIMMER_2R_GQ)){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_ECG)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			
//			mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
//					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);
	
		}
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.clear();
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGGain, 
										Configuration.Shimmer3.ListOfExGGainConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGResolutions, 
										Configuration.Shimmer3.ListOfExGResolutionsConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));

		//Advanced ExG		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfECGReferenceElectrode, 
										Configuration.Shimmer3.ListOfECGReferenceElectrodeConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG, Configuration.Shimmer3.ListOfEMGReferenceElectrode);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG, Configuration.Shimmer3.ListOfEMGReferenceElectrodeConfigValues);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM, Configuration.Shimmer3.ListOfExGReferenceElectrodeAll);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM, Configuration.Shimmer3.ListOfExGReferenceElectrodeConfigValuesAll);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP, Configuration.Shimmer3.ListOfRespReferenceElectrode);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP, Configuration.Shimmer3.ListOfRespReferenceElectrodeConfigValues);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST, Configuration.Shimmer3.ListOfTestReferenceElectrode);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST, Configuration.Shimmer3.ListOfTestReferenceElectrodeConfigValues);
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_BYTES, 
				new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.JPANEL,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGRate, 
										Configuration.Shimmer3.ListOfExGRateConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGLeadOffDetection, 
										Configuration.Shimmer3.ListOfExGLeadOffDetectionConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGLeadOffCurrent, 
										Configuration.Shimmer3.ListOfExGLeadOffCurrentConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGLeadOffComparator, 
										Configuration.Shimmer3.ListOfExGLeadOffComparatorConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGRespirationDetectFreq, 
										Configuration.Shimmer3.ListOfExGRespirationDetectFreqConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										Configuration.Shimmer3.CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration));
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khz, 
										Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khzConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										Configuration.Shimmer3.CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration));
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ, Configuration.Shimmer3.ListOfExGRespirationDetectPhase64khz);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ, Configuration.Shimmer3.ListOfExGRespirationDetectPhase64khzConfigValues);
	}

	@Override
	public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		
		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN):
				if (commType == COMMUNICATION_TYPE.BLUETOOTH){
					//consolePrintLn("before set " + getExGGain());
					setExGGainSetting((int)valueToSet);
					byte[] reg = mEXG1RegisterArray;
					byte[] command = new byte[]{ShimmerBluetooth.SET_EXG_REGS_COMMAND,(byte)(EXG_CHIP_INDEX.CHIP1.ordinal()),0,10,reg[0],reg[1],reg[2],reg[3],reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]};
					//consolePrintLn("after set " + getExGGain());
					actionSetting.mActionListByteArray.add(command);
					
					reg = mEXG1RegisterArray;
					command = new byte[]{ShimmerBluetooth.SET_EXG_REGS_COMMAND,(byte)(EXG_CHIP_INDEX.CHIP2.ordinal()),0,10,reg[0],reg[1],reg[2],reg[3],reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]};
					//consolePrintLn("after set " + getExGGain());
					actionSetting.mActionListByteArray.add(command);
					
		        	break;
				} else if (commType == COMMUNICATION_TYPE.DOCK){
					
				} else if (commType == COMMUNICATION_TYPE.CLASS){
					//this generates the infomem
					setExGGainSetting((int)valueToSet);
				}
			break;
		}
		return actionSetting;

	}

//	@Override
//	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo) {
////		HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> channelDetailsMapPerComm = new HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>>(); 
////		LinkedHashMap<Integer, ChannelDetails> channelDetailsMap = new LinkedHashMap<Integer, ChannelDetails>();
////		
////		//ExG - Status
////		channelDetailsMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
////						DatabaseChannelHandles.EXG1_STATUS,
////						CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
////						CHANNEL_UNITS.NO_UNITS,
////						Arrays.asList(CHANNEL_TYPE.UNCAL)));
////		channelDetailsMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
////						DatabaseChannelHandles.EXG2_STATUS,
////						CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.LSB,
////						CHANNEL_UNITS.NO_UNITS,
////						Arrays.asList(CHANNEL_TYPE.UNCAL)));
////		
////		//ExG - General
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
////						DatabaseChannelHandles.EXG1_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
////						DatabaseChannelHandles.EXG1_CH2_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
////						DatabaseChannelHandles.EXG2_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,
////						DatabaseChannelHandles.EXG2_CH2_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
////						DatabaseChannelHandles.EXG1_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
////						DatabaseChannelHandles.EXG1_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
////						DatabaseChannelHandles.EXG2_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,
////						DatabaseChannelHandles.EXG2_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////
////		//ExG - EMG
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
////						DatabaseChannelHandles.EXG1_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,
////						DatabaseChannelHandles.EXG1_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
////						DatabaseChannelHandles.EXG1_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,
////						DatabaseChannelHandles.EXG1_CH2_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		
////		//ExG - ECG
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
////						DatabaseChannelHandles.EXG1_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,
////						DatabaseChannelHandles.EXG1_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,
////						DatabaseChannelHandles.EXG2_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));	
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,
////						DatabaseChannelHandles.EXG2_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));	
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
////						DatabaseChannelHandles.EXG1_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
////						DatabaseChannelHandles.EXG1_CH2_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));		
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,
////						DatabaseChannelHandles.EXG2_CH2_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));					
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,
////						DatabaseChannelHandles.EXG2_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));	
////		
////		//ECG derived
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL)));
////		
////		//ExG - Test signal
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,
////						DatabaseChannelHandles.EXG1_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,
////						DatabaseChannelHandles.EXG1_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,
////						DatabaseChannelHandles.EXG2_CH1_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));	
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,
////						DatabaseChannelHandles.EXG2_CH2_16BITS,
////						CHANNEL_DATA_TYPE.INT16, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));	
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,
////						DatabaseChannelHandles.EXG1_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,
////						DatabaseChannelHandles.EXG1_CH2_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));		
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,
////						DatabaseChannelHandles.EXG2_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));
////		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,
////				new ChannelDetails(
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,
////						Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,
////						DatabaseChannelHandles.EXG2_CH1_24BITS,
////						CHANNEL_DATA_TYPE.INT24, 3, CHANNEL_DATA_ENDIAN.MSB,
////						CHANNEL_UNITS.MILLIVOLTS,
////						Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL)));		
//		
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		mShimmerVerObject = shimmerDevice.mShimmerVerObject;
//		mSensorEnabledMap = shimmerDevice.getSensorEnabledMap();
				
		int idxEXGADS1292RChip1Config1 =         10;// exg bytes
		int idxEXGADS1292RChip2Config1 =         20;
		
		//TODO temp here
//		byte[] mEXG1RegisterArray = new byte[]{(byte) 0x00,(byte) 0xa3,(byte) 0x10,(byte) 0x05,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x01}; //WP test array
//		byte[] mEXG1RegisterArray = new byte[]{(byte) 0x02,(byte) 0xa0,(byte) 0x10,(byte) 0x40,(byte) 0xc0,(byte) 0x20,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x03}; //WP ECG array
//		byte[] mEXG2RegisterArray = new byte[]{(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
		
//		setDefaultECGConfiguration(shimmerDevice.getShimmerSamplingRate());
		
		//EXG Configuration
		exgBytesGetFromConfig(); //update mEXG1Register and mEXG2Register
		System.arraycopy(mEXG1RegisterArray, 0, mInfoMemBytes, idxEXGADS1292RChip1Config1, 10);
		if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
			System.arraycopy(mEXG2RegisterArray, 0, mInfoMemBytes, idxEXGADS1292RChip2Config1, 10);
		}
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] infoMemBytes) {

		int idxEXGADS1292RChip1Config1 =         10;// exg bytes
		int idxEXGADS1292RChip2Config1 =         20;

		System.arraycopy(infoMemBytes, idxEXGADS1292RChip1Config1, mEXG1RegisterArray, 0, 10);
		if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
			System.arraycopy(infoMemBytes, idxEXGADS1292RChip2Config1, mEXG2RegisterArray, 0, 10);
		}
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	


	//-------------------- ExG Start -----------------------------------
	
	/**
	 * Populates the individual ExG related variables in ShimmerObject per ExG
	 * chip based on the ExG configuration byte arrays
	 * 
	 * @param chipIndex
	 *            indicates whether the bytes are specific to chip 1 or chip 2
	 *            on the ExG expansion board.
	 * @param byteArray
	 *            the configuration byte array for an individual chip (10-bytes
	 *            long)
	 */
	public void exgBytesGetConfigFrom(int chipIndex, byte[] byteArray) {
		// to overcome possible backward compatability issues (where
		// bufferAns.length was 11 or 12 using all of the ExG config bytes)
		int index = 1;
		if(byteArray.length == 10) {
			index = 0;
		}
		
		if (chipIndex==1){
			System.arraycopy(byteArray, index, mEXG1RegisterArray, 0, 10);
			// retrieve the gain and rate from the the registers
			mEXG1RateSetting = mEXG1RegisterArray[0] & 7;
			mEXGLeadOffDetectionCurrent = (mEXG1RegisterArray[2] >> 2) & 3;
			mEXGLeadOffComparatorTreshold = (mEXG1RegisterArray[2] >> 5) & 7;
			mEXG1CH1GainSetting = (mEXG1RegisterArray[3] >> 4) & 7;
			mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
			mEXG1CH2GainSetting = (mEXG1RegisterArray[4] >> 4) & 7;
			mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
			mEXGReferenceElectrode = mEXG1RegisterArray[5] & 0x0F;
			mEXG1LeadOffCurrentMode = mEXG1RegisterArray[2] & 1;
			mEXG1Comparators = mEXG1RegisterArray[1] & 0x40;								
			mEXGRLDSense = mEXG1RegisterArray[5] & 0x10;
			mEXG1LeadOffSenseSelection = mEXG1RegisterArray[6] & 0x0f; //2P1N1P
			
			mExGConfigBytesDetails.updateFromRegisterArray(EXG_CHIP_INDEX.CHIP1, mEXG1RegisterArray);
		} 
		
		else if (chipIndex==2){
			System.arraycopy(byteArray, index, mEXG2RegisterArray, 0, 10);
			mEXG2RateSetting = mEXG2RegisterArray[0] & 7;
			mEXG2CH1GainSetting = (mEXG2RegisterArray[3] >> 4) & 7;
			mEXG2CH2PowerDown = (mEXG2RegisterArray[3] >> 7) & 1;
			mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
			mEXG2CH2GainSetting = (mEXG2RegisterArray[4] >> 4) & 7;
			mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
			mEXG2LeadOffCurrentMode = mEXG2RegisterArray[2] & 1;
			mEXG2Comparators = mEXG2RegisterArray[1] & 0x40;
			mEXG2LeadOffSenseSelection = mEXG2RegisterArray[6] & 0x0f; //2P
			
			mEXG2RespirationDetectState = (mEXG2RegisterArray[8] >> 6) & 0x03;
			mEXG2RespirationDetectPhase = (mEXG2RegisterArray[8] >> 2) & 0x0F;
			mEXG2RespirationDetectFreq = (mEXG2RegisterArray[9] >> 2) & 0x01;
			
			mExGConfigBytesDetails.updateFromRegisterArray(EXG_CHIP_INDEX.CHIP2, mEXG2RegisterArray);
		}
		
	}

	public void exgBytesGetConfigFrom(byte[] mEXG1RegisterArray, byte[] mEXG2RegisterArray){
		exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		exgBytesGetConfigFrom(2, mEXG2RegisterArray);
		internalCheckExgModeAndUpdateSensorMap();
	}
	
	//TODO:2015-06-16 remove the need for this by using map
	/**
	 * Generates the ExG configuration byte arrays based on the individual ExG
	 * related variables stored in ShimmerObject. The resulting arrays are
	 * stored in the global variables mEXG1RegisterArray and mEXG2RegisterArray.
	 * 
	 */
	public void exgBytesGetFromConfig() {
		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	
	
//	/**
//	 * Checks if 16 bit ECG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 16 bit ECG is set
//	 */
//	public boolean isEXGUsingECG16Configuration(long enabledSensors){
//		boolean using = false;
//		if ((enabledSensors & SENSOR_EXG1_16BIT)>0 && (enabledSensors & SENSOR_EXG2_16BIT)>0){
//			if(isEXGUsingDefaultECGConfiguration()){
//				using = true;
//			}
//		}
//		return using;
//	}
//	
//	/**
//	 * Checks if 24 bit ECG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 24 bit ECG is set
//	 */
//	public boolean isEXGUsingECG24Configuration(long enabledSensors){
//		boolean using = false;
//		if ((enabledSensors & SENSOR_EXG1_24BIT)>0 && (enabledSensors & SENSOR_EXG2_24BIT)>0){
//			if(isEXGUsingDefaultECGConfiguration()){
//				using = true;
//			}
//		}
//		return using;
//	}
//	
//	/**
//	 * Checks if 16 bit EMG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 16 bit EMG is set
//	 */
//	public boolean isEXGUsingEMG16Configuration(long enabledSensors){
//		boolean using = false;
//		if ((enabledSensors & SENSOR_EXG1_16BIT)>0 && (enabledSensors & SENSOR_EXG2_16BIT)>0){
//			if(isEXGUsingDefaultEMGConfiguration()){
//				using = true;
//			}
//		}
//		return using;
//	}
//	
//	/**
//	 * Checks if 24 bit EMG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 24 bit EMG is set
//	 */
//	public boolean isEXGUsingEMG24Configuration(long enabledSensors){
//		boolean using = false;
//		if ((enabledSensors & SENSOR_EXG1_24BIT)>0 && (enabledSensors & SENSOR_EXG2_24BIT)>0){
//			if(isEXGUsingDefaultEMGConfiguration()){
//				using = true;
//			}
//		}
//		return using;
//	}
//	
//	/**
//	 * Checks if 16 bit test signal configuration is set on the Shimmer device.
//	 * Do not use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 24 bit test signal is set
//	 */
//	public boolean isEXGUsingTestSignal16Configuration(long enabledSensors){
//		boolean using = false;
//		if ((enabledSensors & SENSOR_EXG1_16BIT)>0 && (enabledSensors & SENSOR_EXG2_16BIT)>0){
//			if(isEXGUsingDefaultTestSignalConfiguration()){
//				using = true;
//			}
//		}
//		return using;
//	}
//	
//	/**
//	 * Checks if 24 bit test signal configuration is set on the Shimmer device.
//	 * @return true if 24 bit test signal is set
//	 */
//	public boolean isEXGUsingTestSignal24Configuration(long enabledSensors){
//		boolean using = false;
//		if ((enabledSensors & SENSOR_EXG1_24BIT)>0 && (enabledSensors & SENSOR_EXG2_24BIT)>0){
//			if(isEXGUsingDefaultTestSignalConfiguration()){
//				using = true;
//			}
//		}
//		return using;
//	}	
	
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * 
	 */
	 public void setDefaultECGConfiguration(double shimmerSamplingRate) {
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 45,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
		
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_ECG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
		
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_4);
		
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_NEG_INPUTS_CH2.RLD_CONNECTED_TO_IN2N);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_POS_INPUTS_CH2.RLD_CONNECTED_TO_IN2P);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_POS_INPUTS_CH1.RLD_CONNECTED_TO_IN1P);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
		
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 */
	 protected void setDefaultEMGConfiguration(double shimmerSamplingRate){
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 105,(byte) 96,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 129,(byte) 129,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
		
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EMG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.ROUTE_CH3_TO_CH1);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.NORMAL);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG4.CH1_POWER_DOWN.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.SHORTED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.SHORTED);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal
	 * (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be
	 * enabled
	 */
	 protected void setEXGTestSignal(double shimmerSamplingRate){
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
		
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.TEST_SIGNAL);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.TEST_SIGNAL);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}
	 
		
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * 
	 */
	 protected void setDefaultRespirationConfiguration(double shimmerSamplingRate) {
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 234,(byte) 1};
		
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_4);

//			setExgPropertySingleChip(CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT); //TODO:2015-06 check!!
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
	
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG9.RESPIRATION_DEMOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG9.RESPIRATION_MOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG9.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}	 

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the default
	 * setting for the 'custom' channel.
	 * 
	 * Ben: I believe ExG can do 3 channels single ended maybe 4. For 3, The INN
	 * channels are set to "RLD" and then tie RL to ground (Won't be perfect
	 * because of R4 but should be reasonable). The RLD buffer needs to be
	 * disabled and inputs to the buffer likely set to not connected... For the
	 * 4th, it seems like we could use the RESPMOD input to get RLD - RA (and
	 * then invert in SW?). But this may always be zero if RLD is ground... We
	 * would end up with:
	 * <p>
	 * <li>Chip1 Ch1: LL - RLD 
	 * <li>Chip1 Ch2: LA - RLD 
	 * <li>Chip2 Ch1: Nothing? 
	 * <li>Chip2 Ch2: V1 - RLD
	 * <p>
	 * However there may be an advanced configuration where we use VDD/2 as
	 * input to a channel of Chip1 and then buffer that via RLD and then tie
	 * that buffered 1.5V via RLD to the ground of a sensor and the various
	 * inputs. That config would be best for AC signals (giving a Vdd/2
	 * reference) but limits peak amplitude of the incoming signal.
	 * 
	 * 
	 */
	 protected void setEXGCustom(double shimmerSamplingRate){
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 7,(byte) 7,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 7,(byte) 7,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
		
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}
	 
	public void setExgGq(double shimmerSamplingRate){
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 0x00,(byte) 0xa3,(byte) 0x10,(byte) 0x05,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x01}; //WP test array
//			mEXG1RegisterArray = new byte[]{(byte) 0x02,(byte) 0xA0,(byte) 0x10,(byte) 0x40,(byte) 0xc0,(byte) 0x20,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x03}; //WP ECG array

			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EMG);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
		
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
			
			//Chip 1 - Channel 1
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_4);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG4.CH1_POWER_DOWN.NORMAL_OPERATION);
			
			//LA-RA
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.NORMAL);
//			//LA-RL
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
//			//RL-RA
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.RLDIN_CONNECTED_TO_POS_INPUT);

			//Chip 1 - Channel 2
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_4);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.POWER_DOWN);
			//LA-RL
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.NORMAL);
			
			setExGRateFromFreq(shimmerSamplingRate);
			
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}

	protected void clearExgConfig(){
		setExgChannelBitsPerMode(-1);
		mExGConfigBytesDetails.startNewExGConig();

		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	 
	/** Note: Doesn't update the Sensor Map
	 * @param chipIndex
	 * @param option
	 */
	protected void setExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, ExGConfigOption option){
		mExGConfigBytesDetails.setExgPropertySingleChip(chipIndex,option);
		if(chipIndex==EXG_CHIP_INDEX.CHIP1){
			mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
			exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		}
		else if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
			exgBytesGetConfigFrom(2, mEXG2RegisterArray);
		}
	}
	
	protected boolean isExgPropertyEnabled(EXG_CHIP_INDEX chipIndex, ExGConfigOption option){
		return mExGConfigBytesDetails.isExgPropertyEnabled(chipIndex, option);
	}
	 
	protected void setExgPropertyBothChips(ExGConfigOption option){
		mExGConfigBytesDetails.setExgPropertyBothChips(option);
		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	
	/** Note: Doesn't update the Sensor Map
	 * @param chipIndex
	 * @param propertyName
	 * @param value
	 */
	public void setExgPropertySingleChipValue(EXG_CHIP_INDEX chipIndex, String propertyName, int value){
		mExGConfigBytesDetails.setExgPropertyValue(chipIndex,propertyName,value);
		if(chipIndex==EXG_CHIP_INDEX.CHIP1){
			mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
			exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		}
		else if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
			exgBytesGetConfigFrom(2, mEXG2RegisterArray);
		}
	}
	
	public int getExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, String propertyName){
		return mExGConfigBytesDetails.getExgPropertySingleChip(chipIndex, propertyName);
	}
	
	
	public HashMap<String, Integer> getMapOfExGSettingsChip1(){
		return mExGConfigBytesDetails.mMapOfExGSettingsChip1ThisShimmer;
	}

	public HashMap<String, Integer> getMapOfExGSettingsChip2(){
		return mExGConfigBytesDetails.mMapOfExGSettingsChip2ThisShimmer;
	}
	
//	protected void checkExgResolutionFromEnabledSensorsVar(long enabledSensors){
//		InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3)mInfoMemLayout;
//
//		mIsExg1_24bitEnabled = ((enabledSensors & infoMemLayoutCast.maskExg1_24bitFlag)==infoMemLayoutCast.maskExg1_24bitFlag)? true:false;
//		mIsExg2_24bitEnabled = ((enabledSensors & infoMemLayoutCast.maskExg2_24bitFlag)==infoMemLayoutCast.maskExg2_24bitFlag)? true:false;
//		mIsExg1_16bitEnabled = ((enabledSensors & infoMemLayoutCast.maskExg1_16bitFlag)==infoMemLayoutCast.maskExg1_16bitFlag)? true:false;
//		mIsExg2_16bitEnabled = ((enabledSensors & infoMemLayoutCast.maskExg2_16bitFlag)==infoMemLayoutCast.maskExg2_16bitFlag)? true:false;
//		
//		if(mIsExg1_16bitEnabled||mIsExg2_16bitEnabled){
//			mExGResolution = 0;
//		}
//		else if(mIsExg1_24bitEnabled||mIsExg2_24bitEnabled){
//			mExGResolution = 1;
//		}
//	}
//
//	private void updateEnabledSensorsFromExgResolution(long enabledSensors){
//		InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3)mInfoMemLayout;
//
//		//JC: should this be here -> checkExgResolutionFromEnabledSensorsVar()
//		
//		enabledSensors &= ~infoMemLayoutCast.maskExg1_24bitFlag;
//		enabledSensors |= (mIsExg1_24bitEnabled? infoMemLayoutCast.maskExg1_24bitFlag:0);
//		
//		enabledSensors &= ~infoMemLayoutCast.maskExg2_24bitFlag;
//		enabledSensors |= (mIsExg2_24bitEnabled? infoMemLayoutCast.maskExg2_24bitFlag:0);
//		
//		enabledSensors &= ~infoMemLayoutCast.maskExg1_16bitFlag;
//		enabledSensors |= (mIsExg1_16bitEnabled? infoMemLayoutCast.maskExg1_16bitFlag:0);
//		
//		enabledSensors &= ~infoMemLayoutCast.maskExg2_16bitFlag;
//		enabledSensors |= (mIsExg2_16bitEnabled? infoMemLayoutCast.maskExg2_16bitFlag:0);
//	}
	
	private void setExgChannelBitsPerMode(int sensorMapKey){
		mIsExg1_24bitEnabled = false;
		mIsExg2_24bitEnabled = false;
		mIsExg1_16bitEnabled = false;
		mIsExg2_16bitEnabled = false;
		
		boolean chip1Enabled = false;
		boolean chip2Enabled = false;
		if(sensorMapKey==-1){
			chip1Enabled = false;
			chip2Enabled = false;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_ECG){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EMG){
			chip1Enabled = true;
			chip2Enabled = false;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		
		if(mExGResolution==1){
			mIsExg1_24bitEnabled = chip1Enabled;
			mIsExg2_24bitEnabled = chip2Enabled;
		}
		else {
			mIsExg1_16bitEnabled = chip1Enabled;
			mIsExg2_16bitEnabled = chip2Enabled;
		}
	}
	
//	private boolean checkIfOtherExgChannelEnabled(Map<Integer,SensorEnabledDetails> sensorEnabledMap) {
//		for(Integer sensorMapKey:sensorEnabledMap.keySet()) {
//			if(sensorEnabledMap.get(sensorMapKey).mIsEnabled) {
//				if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.ECG)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EMG)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_TEST)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION) ){
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	/**
	 * @return the mEXG1RateSetting
	 */
	public int getEXG1RateSetting() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG1_DATA_RATE);
	}

	/**
	 * @return the mEXGReferenceElectrode
	 */
	public int getEXGReferenceElectrode() {
		return mExGConfigBytesDetails.getEXGReferenceElectrode();
	}
	
	/**
	 * @return the mEXGLeadOffDetectionCurrent
	 */
	public int getEXGLeadOffDetectionCurrent() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT);
	}


	/**
	 * @return the mEXGLeadOffComparatorTreshold
	 */
	public int getEXGLeadOffComparatorTreshold() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD);
	}

	/**
	 * @return the mEXG2RespirationDetectFreq
	 */
	public int getEXG2RespirationDetectFreq() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY);
	}

	/**
	 * @return the mEXG2RespirationDetectPhase
	 */
	public int getEXG2RespirationDetectPhase() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG9_RESPIRATION_PHASE);
	}

	
	/**
	 * @return the mEXG1RegisterArray
	 */
	public byte[] getEXG1RegisterArray() {
		return mEXG1RegisterArray;
	}

	/**
	 * @return the mEXG2RegisterArray
	 */
	public byte[] getEXG2RegisterArray() {
		return mEXG2RegisterArray;
	}
	
	public int getExg1CH1GainValue(){
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN);
//		return mEXG1CH1GainValue;
	}
	
	public int getExg1CH2GainValue(){
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN);
//		return mEXG1CH2GainValue;
	}
	
	public int getExg2CH1GainValue(){
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN);
//		return mEXG2CH1GainValue;
	}
	
	public int getExg2CH2GainValue(){
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN);
//		return mEXG2CH2GainValue;
	}
	
	public boolean areExgChannelGainsEqual(List<EXG_CHIP_INDEX> listOfChipsToCheck){
		boolean areEqual = true;
		if(listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP1)){
			if(getExg1CH1GainValue() != getExg1CH2GainValue()) {
				areEqual = false;
			}
		}

		if(listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP2)){
			if(getExg2CH1GainValue() != getExg2CH2GainValue()) {
				areEqual = false;
			}
		}

		if(listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP1) && listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP2)){
			if(getExg1CH1GainValue() != getExg2CH1GainValue()) {
				areEqual = false;
			}
		}
		return areEqual;
	}


	protected void setExGGainSetting(EXG_CHIP_INDEX chipID,  int channel, int value){
		if(chipID==EXG_CHIP_INDEX.CHIP1){
			if(channel==1){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
			}
			else if(channel==2){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
			}
		}
		else if(chipID==EXG_CHIP_INDEX.CHIP2){
			if(channel==1){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
			}
			else if(channel==2){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
			}
		}
	}

	protected void setExGGainSetting(int value){
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
	}
	
	protected void setExGResolution(int i){
		mExGResolution = i;
		
		if(i==0) { // 16-bit
			//this is needed so the BT can write the correct sensor
			/*if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG1_16BIT;
			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG2_16BIT;
			}
			mEnabledSensors = SENSOR_EXG1_16BIT|SENSOR_EXG2_16BIT;
			*/
			//
			
			if(mIsExg1_24bitEnabled){
				mIsExg1_24bitEnabled = false;
				mIsExg1_16bitEnabled = true;
			}
			if(mIsExg2_24bitEnabled){
				mIsExg2_24bitEnabled = false;
				mIsExg2_16bitEnabled = true;
			}
		}
		else if(i==1) { // 24-bit
			/*if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG1_24BIT;
			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG2_24BIT;
			}
			mEnabledSensors = SENSOR_EXG1_24BIT|SENSOR_EXG2_24BIT;
			*/
			if(mIsExg1_16bitEnabled){
				mIsExg1_24bitEnabled = true;
				mIsExg1_16bitEnabled = false;
			}
			if(mIsExg2_16bitEnabled){
				mIsExg2_24bitEnabled = true;
				mIsExg2_16bitEnabled = false;
			}
		}
		
		//TODO - needs updating from ShimmerObject
//		updateEnabledSensorsFromExgResolution(enabledSensors);
//		
////		if(mSensorMap != null) {
////			if(i==0) { // 16-bit
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).setIsEnabled(false);
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).setIsEnabled(true);
////				}
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).setIsEnabled(false);
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).setIsEnabled(true);
////				}
////			}
////			else if(i==1) { // 24-bit
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).setIsEnabled(false);
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).setIsEnabled(true);
////				}
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).setIsEnabled(false);
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).setIsEnabled(true);
////				}
////			}
////		}
	}
	
	public int getExGGainSetting(){
//		mEXG1CH1GainSetting = i;
//		mEXG1CH2GainSetting = i;
//		mEXG2CH1GainSetting = i;
//		mEXG2CH2GainSetting = i;
//		System.out.println("SlotDetails: getExGGain - Setting: = " + mEXG1CH1GainSetting + " - Value = " + mEXG1CH1GainValue);
		return this.mEXG1CH1GainSetting;
	}
	
	/** Note: Doesn't update the Sensor Map
	 * @param mEXG1RegisterArray the mEXG1RegisterArray to set
	 */
	protected void setEXG1RegisterArray(byte[] EXG1RegisterArray) {
		this.mEXG1RegisterArray = EXG1RegisterArray;
		exgBytesGetConfigFrom(1, EXG1RegisterArray);
	}

	/** Note: Doesn't update the Sensor Map
	 * @param mEXG2RegisterArray the mEXG2RegisterArray to set
	 */
	protected void setEXG2RegisterArray(byte[] EXG2RegisterArray) {
		this.mEXG2RegisterArray = EXG2RegisterArray;
		exgBytesGetConfigFrom(2, EXG2RegisterArray);
	}


	/**
	 *This can only be used for Shimmer3 devices (EXG) 
	 *When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	 protected void enableDefaultECGConfiguration(double shimmerSamplingRate) {
		setDefaultECGConfiguration(shimmerSamplingRate);
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG)
	 * When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	protected void enableDefaultEMGConfiguration(double shimmerSamplingRate){
		setDefaultEMGConfiguration(shimmerSamplingRate);
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be enabled
	 */
	protected void enableEXGTestSignal(double shimmerSamplingRate){
		setEXGTestSignal(shimmerSamplingRate);
	}

	/**
	 * @param valueToSet the valueToSet to set
	 */
	protected void setEXGRateSetting(int valueToSet) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
	}
	
	/**
	 * @param valueToSet the valueToSet to set
	 */
	protected void setEXGRateSetting(EXG_CHIP_INDEX chipID, int valueToSet) {
		if(chipID==EXG_CHIP_INDEX.CHIP1){
			setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
		}
		else if(chipID==EXG_CHIP_INDEX.CHIP2){
			setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
		}
	}

	/**
	 * @param mEXGReferenceElectrode the mEXGReferenceElectrode to set
	 */
	protected void setEXGReferenceElectrode(int valueToSet) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS,((valueToSet&0x08) == 0x08)? 1:0);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS,((valueToSet&0x04) == 0x04)? 1:0);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS,((valueToSet&0x02) == 0x02)? 1:0);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS,((valueToSet&0x01) == 0x01)? 1:0);
	}

	protected void setEXGLeadOffCurrentMode(int mode){
		if(mode==0){//Off
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.LEAD_OFF_COMPARATORS.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);
			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.POWER_DOWN);
			}
		}
		else if(mode==1){//DC Current
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.LEAD_OFF_COMPARATORS.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);

			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.NORMAL_OPERATION);
			}
		}
		else if(mode==2){//AC Current
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.AC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.LEAD_OFF_COMPARATORS.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);

			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.NORMAL_OPERATION);
			}
		}
	}
	
	protected int getEXGLeadOffCurrentMode(){
		if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
				){
			if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC)){
				return 1;//DC Current
			}
			else if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.AC)){
				return 2;//AC Current
			}
		}
		return 0;//Off
	}

	/**
	 * @param mEXGLeadOffDetectionCurrent the mEXGLeadOffDetectionCurrent to set
	 */
	protected void setEXGLeadOffDetectionCurrent(int mEXGLeadOffDetectionCurrent) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, mEXGLeadOffDetectionCurrent);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, mEXGLeadOffDetectionCurrent);
	}


	/**
	 * @param mEXGLeadOffComparatorTreshold the mEXGLeadOffComparatorTreshold to set
	 */
	protected void setEXGLeadOffComparatorTreshold(int mEXGLeadOffComparatorTreshold) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, mEXGLeadOffComparatorTreshold);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, mEXGLeadOffComparatorTreshold);
	}

//	/**
//	 * @param mEXG2RespirationDetectFreq the mEXG2RespirationDetectFreq to set
//	 */
//	protected void setEXG2RespirationDetectFreq(int mEXG2RespirationDetectFreq) {
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY, mEXG2RespirationDetectFreq);
//		checkWhichExgRespPhaseValuesToUse();
//		
//		if(isExgRespirationDetectFreq32kHz()) {
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
//		}
//		else {
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.RESPIRATION_PHASE_AT_64KHZ.PHASE_157_5);
//		}
//	}

	/**
	 * @param mEXG2RespirationDetectPhase the mEXG2RespirationDetectPhase to set
	 */
	protected void setEXG2RespirationDetectPhase(int mEXG2RespirationDetectPhase) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG9_RESPIRATION_PHASE, mEXG2RespirationDetectPhase);
	}
	
	protected int convertEXGGainSettingToValue(int setting){

		if (setting==0){
			return 6;
		} else if (setting==1){
			return 1;
		} else if (setting==2){
			return 2;
		} else if (setting==3){
			return 3;
		} else if (setting==4){
			return 4;
		} else if (setting==5){
			return 8;
		} else if (setting==6){
			return 12;
		}
		else {
			return -1; // -1 means invalid value
		}

	}

	
	public boolean isEXGUsingDefaultECGConfigurationForSDFW(){
//		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==0)&&((mEXG2RegisterArray[4] & 0x0F)==7)){
				return true;
			}
//		}
		return false;

	}

	public boolean isEXGUsingDefaultECGConfiguration(){
		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==0)&&((mEXG2RegisterArray[4] & 0x0F)==7)){
				return true;
			}
		}
		return false;
	}

	public boolean isEXGUsingDefaultEMGConfiguration(){
		if((mIsExg1_16bitEnabled&&!mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&!mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==9)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==1)&&((mEXG2RegisterArray[4] & 0x0F)==1)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isEXGUsingDefaultTestSignalConfiguration(){
		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==5)&&((mEXG1RegisterArray[4] & 0x0F)==5)&& ((mEXG2RegisterArray[3] & 0x0F)==5)&&((mEXG2RegisterArray[4] & 0x0F)==5)){
				return true;
			}
		}
		return false;
	}

	public boolean isEXGUsingDefaultRespirationConfiguration(){
		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if((mEXG2RegisterArray[8] & 0xC0)==0xC0){
	//		if(isEXGUsingDefaultECGConfiguration()&&((mEXG2RegisterArray[8] & 0xC0)==0xC0)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isEXGUsingCustomSignalConfiguration(){
		if(mIsExg1_16bitEnabled||mIsExg2_16bitEnabled||mIsExg1_24bitEnabled||mIsExg2_24bitEnabled){
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if ExG respiration detection frequency is 32kHz and false if 64kHz
	 */
	public boolean isExgRespirationDetectFreq32kHz() {
		if(getEXG2RespirationDetectFreq()==0)
//		if(mEXG2RespirationDetectFreq==0)
			return true;
		else
			return false;
	}

	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setExGRateFromFreq(double freq) {
		
		int valueToSet = 0x00; // 125Hz
		if (freq<=125) {
			valueToSet = 0x00; // 125Hz
		} else if (freq<=250) {
			valueToSet = 0x01; // 250Hz
		} else if (freq<=500) {
			valueToSet = 0x02; // 500Hz
		} else if (freq<=1000) {
			valueToSet = 0x03; // 1000Hz
		} else if (freq<=2000) {
			valueToSet = 0x04; // 2000Hz
		} else if (freq<=4000) {
			valueToSet = 0x05; // 4000Hz
		} else if (freq<=8000) {
			valueToSet = 0x06; // 8000Hz
		} else {
			valueToSet = 0x02; // 500Hz
		}
		setEXGRateSetting(valueToSet);
		return mEXG1RateSetting;
	}
	
	private void internalCheckExgModeAndUpdateSensorMap(){
		if(mSensorEnabledMap!=null){
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//				if((mIsExg1_24bitEnabled||mIsExg2_24bitEnabled||mIsExg1_16bitEnabled||mIsExg2_16bitEnabled)){
//				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))) {
					if(isEXGUsingDefaultRespirationConfiguration()) { // Do Respiration check first
						if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(true);
						}
					}
					else if(isEXGUsingDefaultECGConfiguration()) {
						if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(true);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
						}
					}
					else if(isEXGUsingDefaultEMGConfiguration()) {
						if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(true);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
						}
					}
					else if(isEXGUsingDefaultTestSignalConfiguration()){
						if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(true);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
						}
					}
					else if(isEXGUsingCustomSignalConfiguration()){
						if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(true);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
						}
					}
					else {
						if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
						}
					}
				}
//			}
//		}
	}
	
	/**
	 * @return the mExGResolution
	 */
	public int getExGResolution() {
		//System.out.println("mExGResolution: " +mExGResolution);
		return mExGResolution;
	}


	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		super.updateSensorGroupingMap();
		
		//TODO hack, should be loaded from channels in updateSensorGroupingMap
		
		mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXG).mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
				Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
				Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
				Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
				Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
				Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
				Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
				Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);

		
		return mSensorGroupingMap;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		Object returnValue = null;
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION):
				setExGResolution((int)valueToSet);
				returnValue = valueToSet;
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN):
				//consolePrintLn("before set " + getExGGain());
				setExGGainSetting((int)valueToSet);
				returnValue = valueToSet;
				//consolePrintLn("after set " + getExGGain());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE):
				returnValue = getEXG1RateSetting();
				//returnValue = getEXG2RateSetting();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE):
				returnValue = getEXGReferenceElectrode();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION):
				returnValue = getEXGLeadOffCurrentMode();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT):
				returnValue = getEXGLeadOffDetectionCurrent();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR):
				returnValue = getEXGLeadOffComparatorTreshold();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ):
				returnValue = getEXG2RespirationDetectFreq();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE):
				returnValue = getEXG2RespirationDetectPhase();
            	break;

	        default:
	        	//TODO not needed here - only a ShimmerObject thing or do we want to set properties in AbstractSensor?
//	        	returnValue = super.setConfigValueUsingConfigLabel(componentName, valueToSet);
	        	break;
		}
		
        if((componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE))){
        	//TODO
//        	checkConfigOptionValues(componentName);
        }
			
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		
        if((componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE))){
        	//TODO
//        	checkConfigOptionValues(componentName);
        }
		
		Object returnValue = null;
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN):
				returnValue = getExGGainSetting();
				//consolePrintLn("Get " + configValue);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION):
				returnValue = getExGResolution();
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE):
				returnValue = getEXG1RateSetting();
				//returnValue = getEXG2RateSetting();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE):
				returnValue = getEXGReferenceElectrode();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION):
				returnValue = getEXGLeadOffCurrentMode();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT):
				returnValue = getEXGLeadOffDetectionCurrent();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR):
				returnValue = getEXGLeadOffComparatorTreshold();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ):
				returnValue = getEXG2RespirationDetectFreq();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE):
				returnValue = getEXG2RespirationDetectPhase();
            	break;
	        default:
	        	//TODO not needed here - only a ShimmerObject thing or do we want to set properties in AbstractSensor?
//	        	returnValue = super.getConfigValueUsingConfigLabel(componentName);
	        	break;
		}
	        	
	        	
		return returnValue;
	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
		
	}


	//-------------------- ExG End -----------------------------------	
}
