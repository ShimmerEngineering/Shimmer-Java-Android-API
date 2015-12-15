package com.shimmerresearch.sensor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.InfoMemLayoutShimmer3;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails;
import com.shimmerresearch.exgConfig.ExGConfigOption;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTINGS;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTING_OPTIONS;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;

public class ShimmerEXGSensor extends AbstractSensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9150699518448307506L;
	
	protected ExGConfigBytesDetails mExGConfigBytesDetails = new ExGConfigBytesDetails(); 
	protected byte[] mEXG1RegisterArray = new byte[10];
	protected byte[] mEXG2RegisterArray = new byte[10];
	protected int mEXG1RateSetting; //setting not value
	protected int mEXG1CH1GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG1CH1GainValue; // this is the value
	protected int mEXG1CH2GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG1CH2GainValue; // this is the value
	protected int mEXG2RateSetting; //setting not value
	protected int mEXG2CH1GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG2CH1GainValue; // this is the value
	protected int mEXG2CH2PowerDown;//Not used in ShimmerBluetooth
	protected int mEXG2CH2GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG2CH2GainValue; // this is the value
	protected int mExGResolution = 1;
	private boolean mIsExg1_24bitEnabled = false;
	private boolean mIsExg2_24bitEnabled = false;
	private boolean mIsExg1_16bitEnabled = false;
	private boolean mIsExg2_16bitEnabled = false;
	
	//EXG ADVANCED
	protected int mEXGReferenceElectrode=-1;
	protected int mLeadOffDetectionMode;
	protected int mEXG1LeadOffCurrentMode;
	protected int mEXG2LeadOffCurrentMode;
	protected int mEXG1Comparators;
	protected int mEXG2Comparators;
	protected int mEXGRLDSense;
	protected int mEXG1LeadOffSenseSelection;
	protected int mEXG2LeadOffSenseSelection;
	protected int mEXGLeadOffDetectionCurrent;
	protected int mEXGLeadOffComparatorTreshold;
	protected int mEXG2RespirationDetectState;//Not used in ShimmerBluetooth
	protected int mEXG2RespirationDetectFreq;//Not used in ShimmerBluetooth
	protected int mEXG2RespirationDetectPhase;//Not used in ShimmerBluetooth
	
	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader =  0;

	
	public Map<Integer, SensorEnabledDetails> mSensorEnabledMap;

	
	public ShimmerEXGSensor(ShimmerVerObject svo) {
		super(svo);
		// TODO Auto-generated constructor stub
		if (mFirmwareType == FW_ID.GQ_802154){
			
		} 
		
		
	}

	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object processData(byte[] rawData, COMMUNICATION_TYPE comTYPE, Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE comType) {
		
		ActionSetting actionSetting = new ActionSetting(comType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN):
				if (comType == COMMUNICATION_TYPE.BLUETOOTH){
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
				} else if (comType == COMMUNICATION_TYPE.DOCK){
					
				} else if (comType == COMMUNICATION_TYPE.CLASS){
					//this generates the infomem
					setExGGainSetting((int)valueToSet);
				}
			break;
		}
		return actionSetting;

	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		mShimmerVerObject = shimmerDevice.mShimmerVerObject;
		mSensorEnabledMap = shimmerDevice.getSensorEnabledMap();
				
		int idxEXGADS1292RChip1Config1 =         10;// exg bytes
		int idxEXGADS1292RChip1Config2 =         11;
		
		//TODO temp here
//		byte[] mEXG1RegisterArray = new byte[]{(byte) 0x00,(byte) 0xa3,(byte) 0x10,(byte) 0x05,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x01}; //WP test array
//		byte[] mEXG1RegisterArray = new byte[]{(byte) 0x02,(byte) 0xa0,(byte) 0x10,(byte) 0x40,(byte) 0xc0,(byte) 0x20,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x03}; //WP ECG array
//		byte[] mEXG2RegisterArray = new byte[]{(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
		
//		setDefaultECGConfiguration(shimmerDevice.getShimmerSamplingRate());
		
		//EXG Configuration
		exgBytesGetFromConfig(); //update mEXG1Register and mEXG2Register
		System.arraycopy(mEXG2RegisterArray, 0, mInfoMemBytes, idxEXGADS1292RChip1Config1, 10);
		System.arraycopy(mEXG2RegisterArray, 0, mInfoMemBytes, idxEXGADS1292RChip1Config2, 10);
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
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.ECG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
		
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH1_PGA_GAIN.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH2_PGA_GAIN.GAIN_4);
		
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.CH2_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_NEG_INPUTS_CH2.RLD_CONNECTED_TO_IN2N);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH2.RLD_CONNECTED_TO_IN2P);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH1.RLD_CONNECTED_TO_IN1P);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
		
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
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EMG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.CH1_PGA_GAIN.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.CH2_PGA_GAIN.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.CH1_INPUT_SELECTION.ROUTE_CH3_TO_CH1);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.CH2_INPUT_SELECTION.NORMAL);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.CH1_POWER_DOWN.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.CH1_INPUT_SELECTION.SHORTED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.CH2_POWER_DOWN.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.CH2_INPUT_SELECTION.SHORTED);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
			
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
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EXG_TEST);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH1_INPUT_SELECTION.TEST_SIGNAL);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH2_INPUT_SELECTION.TEST_SIGNAL);
			
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
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH1_PGA_GAIN.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH2_PGA_GAIN.GAIN_4);

//			setExgPropertySingleChip(CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT); //TODO:2015-06 check!!
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
	
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.RESPIRATION_DEMOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.RESPIRATION_MOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
			
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
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);
	
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH1_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH2_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
//		 }
	}
	 
	public void setExgGq(double shimmerSamplingRate){
//		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 0x00,(byte) 0xa3,(byte) 0x10,(byte) 0x05,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x01}; //WP test array
//			mEXG1RegisterArray = new byte[]{(byte) 0x02,(byte) 0xA0,(byte) 0x10,(byte) 0x40,(byte) 0xc0,(byte) 0x20,(byte) 0x00,(byte) 0x00,(byte) 0x02,(byte) 0x03}; //WP ECG array

			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EMG);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
		
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
			
			//Chip 1 - Channel 1
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH1_PGA_GAIN.GAIN_4);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.CH1_POWER_DOWN.NORMAL_OPERATION);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.CH1_INPUT_SELECTION.NORMAL);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH2.NOT_CONNECTED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH1.NOT_CONNECTED);

			//Chip 1 - Channel 2
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CH2_PGA_GAIN.GAIN_4);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.CH2_POWER_DOWN.NORMAL_OPERATION);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.CH2_INPUT_SELECTION.NORMAL);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH2.NOT_CONNECTED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_NEG_INPUTS_CH2.RLD_CONNECTED_TO_IN2N);
			

			mEXG2RegisterArray = new byte[]{(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};

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
	
//	protected boolean isExgPropertyEnabled(EXG_CHIP_INDEX chipIndex, ExGConfigOption option){
//		return mExGConfigBytesDetails.isExgPropertyEnabled(chipIndex, option);
//	}
	 
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
	
//	public int getExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, String propertyName){
//		return mExGConfigBytesDetails.getExgPropertySingleChip(chipIndex, propertyName);
//	}
//	
//	
//	public HashMap<String, Integer> getMapOfExGSettingsChip1(){
//		return mExGConfigBytesDetails.mMapOfExGSettingsChip1ThisShimmer;
//	}
//
//	public HashMap<String, Integer> getMapOfExGSettingsChip2(){
//		return mExGConfigBytesDetails.mMapOfExGSettingsChip2ThisShimmer;
//	}
//	
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
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.ECG){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EMG){
			chip1Enabled = true;
			chip2Enabled = false;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_TEST){
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
//	
//	/**
//	 * @return the mEXG1RateSetting
//	 */
//	public int getEXG1RateSetting() {
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG1_DATA_RATE);
//	}
//
//	/**
//	 * @return the mEXGReferenceElectrode
//	 */
//	public int getEXGReferenceElectrode() {
//		return mExGConfigBytesDetails.getEXGReferenceElectrode();
//	}
//	
//	/**
//	 * @return the mEXGLeadOffDetectionCurrent
//	 */
//	public int getEXGLeadOffDetectionCurrent() {
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT);
//	}
//
//
//	/**
//	 * @return the mEXGLeadOffComparatorTreshold
//	 */
//	public int getEXGLeadOffComparatorTreshold() {
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD);
//	}
//
//	/**
//	 * @return the mEXG2RespirationDetectFreq
//	 */
//	public int getEXG2RespirationDetectFreq() {
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY);
//	}
//
//	/**
//	 * @return the mEXG2RespirationDetectPhase
//	 */
//	public int getEXG2RespirationDetectPhase() {
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG9_RESPIRATION_PHASE);
//	}
//	
//	/**
//	 * @return the mEXG1RegisterArray
//	 */
//	public byte[] getEXG1RegisterArray() {
//		return mEXG1RegisterArray;
//	}
//
//	/**
//	 * @return the mEXG2RegisterArray
//	 */
//	public byte[] getEXG2RegisterArray() {
//		return mEXG2RegisterArray;
//	}

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
	
//	protected void setExGResolution(int i, long enabledSensors){
//		mExGResolution = i;
//		
//		if(i==0) { // 16-bit
//			//this is needed so the BT can write the correct sensor
//			/*if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
//				mEnabledSensors=mEnabledSensors^SENSOR_EXG1_16BIT;
//			}
//			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
//				mEnabledSensors=mEnabledSensors^SENSOR_EXG2_16BIT;
//			}
//			mEnabledSensors = SENSOR_EXG1_16BIT|SENSOR_EXG2_16BIT;
//			*/
//			//
//			
//			if(mIsExg1_24bitEnabled){
//				mIsExg1_24bitEnabled = false;
//				mIsExg1_16bitEnabled = true;
//			}
//			if(mIsExg2_24bitEnabled){
//				mIsExg2_24bitEnabled = false;
//				mIsExg2_16bitEnabled = true;
//			}
//		}
//		else if(i==1) { // 24-bit
//			/*if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
//				mEnabledSensors=mEnabledSensors^SENSOR_EXG1_24BIT;
//			}
//			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
//				mEnabledSensors=mEnabledSensors^SENSOR_EXG2_24BIT;
//			}
//			mEnabledSensors = SENSOR_EXG1_24BIT|SENSOR_EXG2_24BIT;
//			*/
//			if(mIsExg1_16bitEnabled){
//				mIsExg1_24bitEnabled = true;
//				mIsExg1_16bitEnabled = false;
//			}
//			if(mIsExg2_16bitEnabled){
//				mIsExg2_24bitEnabled = true;
//				mIsExg2_16bitEnabled = false;
//			}
//		}
//		
//		updateEnabledSensorsFromExgResolution(enabledSensors);
//		
////		if(mSensorMap != null) {
////			if(i==0) { // 16-bit
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mIsEnabled = false;
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mIsEnabled = true;
////				}
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mIsEnabled = false;
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mIsEnabled = true;
////				}
////			}
////			else if(i==1) { // 24-bit
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mIsEnabled = false;
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mIsEnabled = true;
////				}
////				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)) {
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mIsEnabled = false;
////					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mIsEnabled = true;
////				}
////			}
////		}
//	}
//	
//	public int getExGGainSetting(){
////		mEXG1CH1GainSetting = i;
////		mEXG1CH2GainSetting = i;
////		mEXG2CH1GainSetting = i;
////		mEXG2CH2GainSetting = i;
////		System.out.println("SlotDetails: getExGGain - Setting: = " + mEXG1CH1GainSetting + " - Value = " + mEXG1CH1GainValue);
//		return this.mEXG1CH1GainSetting;
//	}
//	
//	/** Note: Doesn't update the Sensor Map
//	 * @param mEXG1RegisterArray the mEXG1RegisterArray to set
//	 */
//	protected void setEXG1RegisterArray(byte[] EXG1RegisterArray) {
//		this.mEXG1RegisterArray = EXG1RegisterArray;
//		exgBytesGetConfigFrom(1, EXG1RegisterArray);
//	}
//
//	/** Note: Doesn't update the Sensor Map
//	 * @param mEXG2RegisterArray the mEXG2RegisterArray to set
//	 */
//	protected void setEXG2RegisterArray(byte[] EXG2RegisterArray) {
//		this.mEXG2RegisterArray = EXG2RegisterArray;
//		exgBytesGetConfigFrom(2, EXG2RegisterArray);
//	}
//
//
//	/**
//	 *This can only be used for Shimmer3 devices (EXG) 
//	 *When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
//	 */
//	 protected void enableDefaultECGConfiguration(double shimmerSamplingRate) {
//		setDefaultECGConfiguration(shimmerSamplingRate);
//	}
//
//	/**
//	 * This can only be used for Shimmer3 devices (EXG)
//	 * When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
//	 */
//	protected void enableDefaultEMGConfiguration(double shimmerSamplingRate){
//		setDefaultEMGConfiguration(shimmerSamplingRate);
//	}
//
//	/**
//	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be enabled
//	 */
//	protected void enableEXGTestSignal(double shimmerSamplingRate){
//		setEXGTestSignal(shimmerSamplingRate);
//	}

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

//	/**
//	 * @param mEXGReferenceElectrode the mEXGReferenceElectrode to set
//	 */
//	protected void setEXGReferenceElectrode(int valueToSet) {
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS,((valueToSet&0x08) == 0x08)? 1:0);
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS,((valueToSet&0x04) == 0x04)? 1:0);
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS,((valueToSet&0x02) == 0x02)? 1:0);
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS,((valueToSet&0x01) == 0x01)? 1:0);
//	}
//
//	protected void setEXGLeadOffCurrentMode(int mode){
//		if(mode==0){//Off
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.DC);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_COMPARATORS.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.OFF);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.OFF);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);
//			if(isEXGUsingDefaultEMGConfiguration()){
//				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.POWER_DOWN_CH2.POWER_DOWN);
//			}
//		}
//		else if(mode==1){//DC Current
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.DC);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_COMPARATORS.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
//			
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);
//
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);
//
//			if(isEXGUsingDefaultEMGConfiguration()){
//				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.POWER_DOWN_CH2.NORMAL_OPERATION);
//			}
//		}
//		else if(mode==2){//AC Current
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.AC);
//			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_COMPARATORS.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
//			
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);
//
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
//			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);
//
//			if(isEXGUsingDefaultEMGConfiguration()){
//				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.POWER_DOWN_CH2.NORMAL_OPERATION);
//			}
//		}
//	}
//
//	protected int getEXGLeadOffCurrentMode(){
//		if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
//				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
//				){
//			if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.DC)){
//				return 1;//DC Current
//			}
//			else if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.AC)){
//				return 2;//AC Current
//			}
//		}
//		return 0;//Off
//	}
//
//	/**
//	 * @param mEXGLeadOffDetectionCurrent the mEXGLeadOffDetectionCurrent to set
//	 */
//	protected void setEXGLeadOffDetectionCurrent(int mEXGLeadOffDetectionCurrent) {
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, mEXGLeadOffDetectionCurrent);
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, mEXGLeadOffDetectionCurrent);
//	}
//
//
//	/**
//	 * @param mEXGLeadOffComparatorTreshold the mEXGLeadOffComparatorTreshold to set
//	 */
//	protected void setEXGLeadOffComparatorTreshold(int mEXGLeadOffComparatorTreshold) {
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, mEXGLeadOffComparatorTreshold);
//		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, mEXGLeadOffComparatorTreshold);
//	}
//
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
	
//	/**
//	 * @return true if ExG respiration detection frequency is 32kHz and false if 64kHz
//	 */
//	public boolean isExgRespirationDetectFreq32kHz() {
//		if(getEXG2RespirationDetectFreq()==0)
////		if(mEXG2RespirationDetectFreq==0)
//			return true;
//		else
//			return false;
//	}

	
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
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = true;
					}
					else if(isEXGUsingDefaultECGConfiguration()) {
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = true;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else if(isEXGUsingDefaultEMGConfiguration()) {
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = true;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else if(isEXGUsingDefaultTestSignalConfiguration()){
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = true;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else if(isEXGUsingCustomSignalConfiguration()){
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = true;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else {
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.ECG, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EMG, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_TEST, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION, false);
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
				}
//			}
//		}
	}
	
	//-------------------- ExG End -----------------------------------	
}
