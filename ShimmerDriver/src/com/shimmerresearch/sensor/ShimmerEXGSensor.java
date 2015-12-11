package com.shimmerresearch.sensor;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails;
import com.shimmerresearch.exgConfig.ExGConfigOption;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTINGS;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTING_OPTIONS;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;

public class ShimmerEXGSensor extends AbstractSensor{

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
		
		// TODO Auto-generated method stub
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

	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * 
	 */
	 public void setDefaultECGConfiguration(double shimmerSamplingRate) {
		 if (mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 45,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};

			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.ECG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.GAIN_PGA_CH1.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.GAIN_PGA_CH2.GAIN_4);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_NEG_INPUTS_CH2.RLD_CONNECTED_TO_IN2N);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH2.RLD_CONNECTED_TO_IN2P);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH1.RLD_CONNECTED_TO_IN1P);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);

			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		 }
	}
	 
	 protected void clearExgConfig(){
			setExgChannelBitsPerMode(-1);
			mExGConfigBytesDetails.startNewExGConig();

			mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
			mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		}
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
	 protected void setExgPropertyBothChips(ExGConfigOption option){
			mExGConfigBytesDetails.setExgPropertyBothChips(option);
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
		
		/**
		 * @param valueToSet the valueToSet to set
		 */
		protected void setEXGRateSetting(int valueToSet) {
			setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
			setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
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
		
		public void exgBytesGetConfigFrom(byte[] mEXG1RegisterArray, byte[] mEXG2RegisterArray){
			exgBytesGetConfigFrom(1, mEXG1RegisterArray);
			exgBytesGetConfigFrom(2, mEXG2RegisterArray);
			internalCheckExgModeAndUpdateSensorMap();
		}
		
		private void internalCheckExgModeAndUpdateSensorMap(){
			/*
			if(mSensorEnabledMap!=null){
				if(getHardwareVersion()==HW_ID.SHIMMER_3){
//					if((mIsExg1_24bitEnabled||mIsExg2_24bitEnabled||mIsExg1_16bitEnabled||mIsExg2_16bitEnabled)){
//					if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))
//							||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT))
//							||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))
//							||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))) {
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
//							setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.ECG, false);
//							setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EMG, false);
//							setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_TEST, false);
//							setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM, false);
//							setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION, false);
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
							mSensorEnabledMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
						}
					}
//				}
			}
			*/
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

		@Override
		public SensorInfoMem getInfoMem(ShimmerVerObject svo) {
			// TODO Auto-generated method stub
			return null;
		}
}
