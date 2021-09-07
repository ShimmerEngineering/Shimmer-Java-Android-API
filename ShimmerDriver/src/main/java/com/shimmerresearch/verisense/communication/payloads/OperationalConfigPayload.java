package com.shimmerresearch.verisense.communication.payloads;

import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * @author Mark Nolan
 *
 */
public class OperationalConfigPayload extends AbstractPayload {

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

	public static int calculatePayloadConfigBytesSize(ShimmerVerObject mShimmerVerObject) {
		//TODO add FW checks if supporting multiple versions of FW
		return 72;
	}
}
