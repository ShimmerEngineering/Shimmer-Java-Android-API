package com.shimmerresearch.verisense.communication.payloads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.verisense.PendingEventSchedule;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.VerisenseDevice.BATTERY_TYPE;
import com.shimmerresearch.verisense.VerisenseDevice.BLE_TX_POWER;
import com.shimmerresearch.verisense.VerisenseDevice.FW_CHANGES;
import com.shimmerresearch.verisense.VerisenseDevice.PASSKEY_MODE;
import com.shimmerresearch.verisense.payloaddesign.PayloadContentsDetails;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.DATA_COMPRESSION_MODE;
import com.shimmerresearch.verisense.sensors.SensorBattVoltageVerisense;
import com.shimmerresearch.verisense.sensors.SensorGSRVerisense;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3;
import com.shimmerresearch.verisense.sensors.SensorMAX86916;
import com.shimmerresearch.verisense.sensors.SensorBattVoltageVerisense.ADC_SAMPLING_RATES;
import com.shimmerresearch.verisense.sensors.SensorGSRVerisense.GSR_RANGE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_ACCEL_RANGE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_ACCEL_RATE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_BW_FILT;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_FIFO_MODE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_FIFO_THRESHOLD;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_FILTERED_DATA_TYPE_SELECTION;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_HP_REF_MODE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_LOW_NOISE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_LP_MODE;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_MODE;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.FIFO_DECIMATION_ACCEL;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.FIFO_DECIMATION_GYRO;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.FIFO_MODE;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.LSM6DS3_ACCEL_RANGE;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.LSM6DS3_GYRO_RANGE;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3.LSM6DS3_RATE;
import com.shimmerresearch.verisense.sensors.SensorMAX86916.MAX86916_SAMPLE_RATE;
import com.shimmerresearch.verisense.sensors.SensorMAX86916.PROX_DETECTION_MODE;
import com.shimmerresearch.verisense.sensors.SensorMAX86XXX.MAX86XXX_ADC_RESOLUTION;
import com.shimmerresearch.verisense.sensors.SensorMAX86XXX.MAX86XXX_PULSE_WIDTH;
import com.shimmerresearch.verisense.sensors.SensorMAX86XXX.MAX86XXX_SAMPLE_AVG;

/**
 * @author Mark Nolan
 *
 */
public class OperationalConfigPayload extends AbstractPayload implements Serializable{

	public static final byte[] DEFAULT_OP_CONFIG_BYTES_FW_1_02_70 = new byte[] {0x5A, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x20, 0x00, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0xF4, 0x18, 0x3C, 0x00, 0x0A, 0x0F, 0x00, 0x18, 0x3C,
			0x00, 0x0A, 0x0F, 0x00, 0x18, 0x3C, 0x00, 0x0A, 0x0F, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, 0x3C, 0x00, 0x0E, 0x00, 0x00, 0x63, 0x28, (byte) 0xCC, (byte) 0xCC, 0x1E, 0x00, 0x0A, 0x00,
			0x00, 0x00, 0x00, 0x01};

	public class OP_CONFIG_BYTE_INDEX {
		public static final int HEADER_BYTE = 0;
		
		public static final int GEN_CFG_0 = 1;
		public static final int GEN_CFG_1 = 2;
		public static final int GEN_CFG_2 = 3;
		
		public static final int UNUSED_BYTE_4 = 4;
		
		public static final int ACCEL1_CFG_0 = 5;
		public static final int ACCEL1_CFG_1 = 6;
		public static final int ACCEL1_CFG_2 = 7;
		public static final int ACCEL1_CFG_3 = 8;
		
		public static final int UNUSED_BYTE_9 = 9;

		public static final int GYRO_ACCEL2_CFG_0 = 10;
		public static final int GYRO_ACCEL2_CFG_1 = 11;
		public static final int GYRO_ACCEL2_CFG_2 = 12;
		public static final int GYRO_ACCEL2_CFG_3 = 13;
		public static final int GYRO_ACCEL2_CFG_4 = 14;
		public static final int GYRO_ACCEL2_CFG_5 = 15;
		public static final int GYRO_ACCEL2_CFG_6 = 16;
		public static final int GYRO_ACCEL2_CFG_7 = 17;
		
		public static final int UNUSED_BYTE_18 = 18;
		public static final int UNUSED_BYTE_19 = 19;
		public static final int UNUSED_BYTE_20 = 20;
		
		public static final int START_TIME_BYTE_0 = 21;
		public static final int START_TIME_BYTE_1 = 22;
		public static final int START_TIME_BYTE_2 = 23;
		public static final int START_TIME_BYTE_3 = 24;
		public static final int END_TIME_BYTE_0 = 25;
		public static final int END_TIME_BYTE_1 = 26;
		public static final int END_TIME_BYTE_2 = 27;
		public static final int END_TIME_BYTE_3 = 28;
		
		public static final int UNUSED_BYTE_29 = 29;
		
		public static final int BLE_RETRY_COUNT = 30;
		public static final int BLE_TX_POWER = 31;
		
		public static final int BLE_DATA_TRANS_WKUP_INT_HRS = 32;
		public static final int BLE_DATA_TRANS_WKUP_TIME_LSB = 33;
		public static final int BLE_DATA_TRANS_WKUP_TIME_MSB = 34;
		public static final int BLE_DATA_TRANS_WKUP_DUR = 35;
		public static final int BLE_DATA_TRANS_RETRY_INT_LSB = 36;
		public static final int BLE_DATA_TRANS_RETRY_INT_MSB = 37;
		
		public static final int BLE_STATUS_WKUP_INT_HRS = 38;
		public static final int BLE_STATUS_WKUP_TIME_LSB = 39;
		public static final int BLE_STATUS_WKUP_TIME_MSB = 40;
		public static final int BLE_STATUS_WKUP_DUR = 41;
		public static final int BLE_STATUS_RETRY_INT_LSB = 42;
		public static final int BLE_STATUS_RETRY_INT_MSB = 43;
		
		public static final int BLE_RTC_SYNC_WKUP_INT_HRS = 44;
		public static final int BLE_RTC_SYNC_WKUP_TIME_LSB = 45;
		public static final int BLE_RTC_SYNC_WKUP_TIME_MSB = 46;
		public static final int BLE_RTC_SYNC_WKUP_DUR = 47;
		public static final int BLE_RTC_SYNC_RETRY_INT_LSB = 48;
		public static final int BLE_RTC_SYNC_RETRY_INT_MSB = 49;
		
		public static final int ADC_SAMPLE_RATE = 50;
		public static final int GSR_RANGE_SETTING = 51;
		
		public static final int ADAPTIVE_SCHEDULER_INT_LSB = 52;
		public static final int ADAPTIVE_SCHEDULER_INT_MSB = 53;
		public static final int ADAPTIVE_SCHEDULER_FAILCOUNT_MAX = 54;
		
		public static final int PPG_REC_DUR_SECS_LSB = 55;
		public static final int PPG_REC_DUR_SECS_MSB = 56;
		public static final int PPG_REC_INT_MINS_LSB = 57;
		public static final int PPG_REC_INT_MINS_MSB = 58;
		public static final int PPG_FIFO_CONFIG = 59;
		public static final int PPG_MODE_CONFIG2 = 60;
		public static final int PPG_MA_DEFAULT = 61;
		public static final int PPG_MA_MAX_RED_IR = 62;
		public static final int PPG_MA_MAX_GREEN_BLUE = 63;
		public static final int PPG_AGC_TARGET_PERCENT_OF_RANGE = 64;
		
		public static final int UNUSED_BYTE_65 = 65;
		
		public static final int PPG_MA_LED_PILOT = 66;
		public static final int PPG_DAC1_CROSSTALK = 67;
		public static final int PPG_DAC2_CROSSTALK = 68;
		public static final int PPG_DAC3_CROSSTALK = 69;
		public static final int PPG_DAC4_CROSSTALK = 70;
		public static final int PROX_AGC_MODE = 71;
	}
	
	public class OP_CONFIG_BIT_MASK {
		// GEN_CFG_0
		public static final int BLUETOOTH_ENABLED 		= 0x01 << 4;
		public static final int USB_ENABLED 			= 0x01 << 3;
		public static final int PRIORITISE_LONG_TERM_FLASH_STORAGE = 0x01 << 2;
		public static final int DEVICE_ENABLED 			= 0x01 << 1;
		public static final int RECORDING_ENABLED 		= 0x01 << 0;
		public static final int ENABLED_SENSORS_GEN_CFG_0	= 0xE0;
		
		// GEN_CFG_1
		public static final int ENABLED_SENSORS_GEN_CFG_1	= 0xFC;
		public static final int DATA_COMPRESSION_MODE	= 0x03;

		// GEN_CFG_2
		public static final int ENABLED_SENSORS_GEN_CFG_2	= 0x02;
		public static final int PASSKEY_MODE	= 0x03;
		public static final int BATTERY_TYPE	= 0x01;
	}

	public class OP_CONFIG_BIT_SHIFT {
		// GEN_CFG_2
		public static final int BATTERY_TYPE	= 0;
		public static final int PASSKEY_MODE	= 2;
	}

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		isSuccess = (payloadContents[OP_CONFIG_BYTE_INDEX.HEADER_BYTE] == VALID_CONFIG_BYTE);
		return isSuccess;
	}

	@Override
	public byte[] generatePayloadContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDebugString() {
		return "";
	}

	public static byte[] getDefaultPayloadConfigForFwVersion(ShimmerVerObject mShimmerVerObject) {
		if(PayloadContentsDetails.isPayloadDesignV7orAbove(mShimmerVerObject)) {
			//TODO change over here when we can get the byte arrays to match in main() within this file
			return DEFAULT_OP_CONFIG_BYTES_FW_1_02_70.clone();
//			return generateDefaultOpConfigBytesFw_1_02_070();
		} else {
			return null;
		}
	}

	public static int getPayloadConfigSizeForFwVersion(ShimmerVerObject mShimmerVerObject) {
		if(PayloadContentsDetails.isPayloadDesignV7orAbove(mShimmerVerObject)) {
			return 72;
		} else {
			return 0;
		}
	}

	public static byte[] generateDefaultOpConfigBytesFw_1_02_070() {
		VerisenseDevice verisenseDevice = new VerisenseDevice();

		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_DEV_BRD, 1, 0);

		verisenseDevice.setShimmerVersionObject(svo);
		verisenseDevice.setHardwareVersion(ebd.getExpansionBoardId());
		verisenseDevice.setExpansionBoardDetails(ebd);
		verisenseDevice.sensorAndConfigMapsCreate();
		
		verisenseDevice.disableAllSensors();
		
//		verisenseDevice.setSensorEnabledStateAccel1(true);
//		verisenseDevice.setSensorEnabledStateAccel2(true);
//		verisenseDevice.setSensorEnabledStateGyro(true);
		
		//verisenseDevice.printSensorParserAndAlgoMaps();
		
		// General settings
		verisenseDevice.setDataCompressionMode(DATA_COMPRESSION_MODE.NONE);
		verisenseDevice.setBatteryType(BATTERY_TYPE.ZINC_AIR);
		verisenseDevice.setBluetoothEnabled(false);
		verisenseDevice.setUsbEnabled(false);
		verisenseDevice.setPrioritiseLongTermFlash(false);
		verisenseDevice.setDeviceEnabled(false);
		verisenseDevice.setRecordingEnabled(false);
		verisenseDevice.setAdaptiveSchedulerFailCount(255);
		verisenseDevice.setPasskeyMode(PASSKEY_MODE.SECURE);
		verisenseDevice.setRecordingStartTimeMinutes(0);
		verisenseDevice.setRecordingEndTimeMinutes(0);
		
		verisenseDevice.setBleConnectionRetriesPerDay(3);
		verisenseDevice.setBleTxPower(BLE_TX_POWER.MINUS_12_DBM);
		
		PendingEventSchedule pendingEventSchedule = new PendingEventSchedule(24, 60, 10, 15);
		verisenseDevice.setPendingEventScheduleDataTransfer(pendingEventSchedule);
		verisenseDevice.setPendingEventScheduleRwcSync(pendingEventSchedule);
		verisenseDevice.setPendingEventScheduleStatusSync(pendingEventSchedule);
		
		verisenseDevice.setAdaptiveSchedulerInterval((int) (Math.pow(2, 16)-1));
		verisenseDevice.setAdaptiveSchedulerFailCount((int) (Math.pow(2, 8)-1));
		
		verisenseDevice.setPpgRecordingDurationSeconds(60);
		verisenseDevice.setPpgRecordingIntervalMinutes(14);
		
		//TODO the following won't make any changes to the config bytes as the sensor classes currently check whether each sensor is enabled before parsing/generating config bytes. Need to consider changing this but also keep in mind that this will cause issues for bin file parsing (as bits/bytes are reused for Accel1/Accel2) 
		
		// Accel1 basic settings
		SensorLIS2DW12 sensorLIS2DW12 = verisenseDevice.getSensorLIS2DW12();
		sensorLIS2DW12.setAccelRange(LIS2DW12_ACCEL_RANGE.RANGE_4G);
		sensorLIS2DW12.setAccelRate(LIS2DW12_ACCEL_RATE.LOW_POWER_25_0_HZ);
		sensorLIS2DW12.setAccelMode(LIS2DW12_MODE.LOW_POWER);
		sensorLIS2DW12.setAccelLpMode(LIS2DW12_LP_MODE.LOW_POWER1_12BIT_4_5_MG_NOISE);
		
		// Accel1 advanced settings
		sensorLIS2DW12.setBwFilt(LIS2DW12_BW_FILT.ODR_DIVIDED_BY_2);
		sensorLIS2DW12.setFds(LIS2DW12_FILTERED_DATA_TYPE_SELECTION.LOW_PASS_FILTER_PATH_SELECTED);
		sensorLIS2DW12.setLowNoise(LIS2DW12_LOW_NOISE.DISABLED);
		sensorLIS2DW12.setHpFilterMode(LIS2DW12_HP_REF_MODE.DISABLED);
		sensorLIS2DW12.setFifoMode(LIS2DW12_FIFO_MODE.CONTINUOUS_TO_FIFO_MODE);
		sensorLIS2DW12.setFifoThreshold(LIS2DW12_FIFO_THRESHOLD.SAMPLE_31);

		// Accel2/Gyro basic settings
		SensorLSM6DS3 sensorLSM6DS3 = verisenseDevice.getSensorLSM6DS3();
		sensorLSM6DS3.setGyroRange(LSM6DS3_GYRO_RANGE.RANGE_500DPS);
		sensorLSM6DS3.setAccelRange(LSM6DS3_ACCEL_RANGE.RANGE_4G);
		sensorLSM6DS3.setRate(LSM6DS3_RATE.RATE_52_HZ);
		
		// Accel2/Gyro advanced settings
		sensorLSM6DS3.setTimerPedoFifodEnable(false);
		sensorLSM6DS3.setTimerPedoFifodDrdy(false);
		sensorLSM6DS3.setDecimationFifoAccel(FIFO_DECIMATION_ACCEL.NO_DECIMATION);
		sensorLSM6DS3.setDecimationFifoGyro(FIFO_DECIMATION_GYRO.NO_DECIMATION);
		sensorLSM6DS3.setFifoMode(FIFO_MODE.CONTINUOUS_MODE);
		sensorLSM6DS3.setAccelAntiAliasingBandwidthFilter(ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER.AT_400HZ);
		sensorLSM6DS3.setGyroFullScaleAt12dps(false);
		sensorLSM6DS3.setGyroHighPerformanceMode(false);
		sensorLSM6DS3.setGyroDigitalHighPassFilterEnable(false);
		sensorLSM6DS3.setGyroHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO.AT_0_0081_HZ);
		sensorLSM6DS3.setGyroDigitalHighPassFilterReset(false);
		sensorLSM6DS3.setRoundingStatus(false);
		sensorLSM6DS3.setAccelLowPassFilterLpf2Selection(false);
		sensorLSM6DS3.setAccelHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.SLOPE);
		sensorLSM6DS3.setAccelHighPassOrSlopeFilterSelectionEnable(false);
		sensorLSM6DS3.setLowPassOn6D(false);

		// PPG
		SensorMAX86916 sensorMax86916 = verisenseDevice.getSensorMax86916();
		sensorMax86916.setPpgAdcResolution(MAX86XXX_ADC_RESOLUTION.RESOLUTION_15_BIT);
		sensorMax86916.setPpgPulseWidth(MAX86XXX_PULSE_WIDTH.PW_400_US);
		sensorMax86916.setPpgSampleAverage(MAX86XXX_SAMPLE_AVG.NO_AVERAGING);
		sensorMax86916.setSampleRate(MAX86916_SAMPLE_RATE.SR_50_0_HZ);
		sensorMax86916.setPpgDefaultCurrentAllLedsMilliamps(40);
		sensorMax86916.setPpgMaxCurrentGreenBlueLedsMilliamps(SensorMAX86916.MAX_LED_CURRENT_MILLIAMPS);
		sensorMax86916.setPpgMaxCurrentRedIrLedsMilliamps(SensorMAX86916.MAX_LED_CURRENT_MILLIAMPS);
		sensorMax86916.setPpgAutoGainControlTargetPercentOfRange(30);
		sensorMax86916.setPpgProximityDetectionCurrentIrLedMilliamps(10);
		sensorMax86916.setPpgDac1CrossTalk(0);
		sensorMax86916.setPpgDac2CrossTalk(0);
		sensorMax86916.setPpgDac3CrossTalk(0);
		sensorMax86916.setPpgDac4CrossTalk(0);
		sensorMax86916.setProximityDetectionMode(PROX_DETECTION_MODE.AUTO_GAIN_ON_PROX_DETECTION_ON_DRIVER);

		// Battery Voltage
		SensorBattVoltageVerisense sensorBatteryVoltage = verisenseDevice.getSensorBatteryVoltage();
		sensorBatteryVoltage.setSensorSamplingRate(ADC_SAMPLING_RATES.OFF);

		// GSR
//		SensorGSRVerisense sensorGsr = verisenseDevice.getSensorGsr();
//		sensorGsr.setSensorSamplingRate(ADC_SAMPLING_RATES.OFF);
//		sensorGsr.setGsrRange(GSR_RANGE.AUTO_RANGE);

		return verisenseDevice.configBytesGenerate(false, COMMUNICATION_TYPE.BLUETOOTH);
	}
	
	public static void main(String[] args) {
		OperationalConfigPayload.testByteArrays(OperationalConfigPayload.DEFAULT_OP_CONFIG_BYTES_FW_1_02_70, OperationalConfigPayload.generateDefaultOpConfigBytesFw_1_02_070());
	}

	private static void testByteArrays(byte[] byteArray1, byte[] byteArray2) {
		List<Integer> listOfByteIndexesWithDifferences = new ArrayList<Integer>();
		for(int i=0;i<byteArray1.length;i++) {
			System.out.println(i + ", " + UtilShimmer.byteToHexString(byteArray1[i]) + ", " + UtilShimmer.byteToHexString(byteArray2[i]));
			if(byteArray1[i] != byteArray2[i]) {
				listOfByteIndexesWithDifferences.add(i);
			}
		}
		if(listOfByteIndexesWithDifferences.size()>0) {
			String result = "byte comparison failed at indexes " + listOfByteIndexesWithDifferences;
			System.out.println(result);
		}
	}

}
