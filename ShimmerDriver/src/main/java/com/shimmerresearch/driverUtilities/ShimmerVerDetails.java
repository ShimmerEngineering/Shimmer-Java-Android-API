package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds Shimmer Hardware and Firmware version details.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerVerDetails implements Serializable {

	/** * */
	private static final long serialVersionUID = -7940733886215010795L;
	
	public static final int ANY_VERSION = -1;

	//TODO change all to ENUMs with ints and Strings passed in
	//WARNING! The ID can't change because Consensys relies on it.
	public static final class HW_ID {
		public static final int UNKNOWN = -1;
		public static final int SHIMMER_1 = 0;
		public static final int SHIMMER_2 = 1;
		public static final int SHIMMER_2R = 2;
		public static final int SHIMMER_3 = 3;
		public static final int SHIMMER_SR30 = 4;
		public static final int SWEATCH = 4;
		public static final int SHIMMER_GQ_BLE = 5;
//		public static final int BIOSENSICS = 6;
		public static final int SPAN = 7;
		public static final int SHIMMER_2R_GQ = 9; // Used for testing GQ
		public static final int SHIMMER_3R = 10;
		
		// For older devices the was a 'new' HW id given in firmware/software
		// for each device. For newer devices from GQ onwards, we decided to just
		// use the already defined SR number for the board.	
		
		public static final int SHIMMER_GQ_802154_LR 	= HW_ID_SR_CODES.SHIMMER_GQ_802154_LR; // Long Range
		public static final int SHIMMER_GQ_802154_NR 	= HW_ID_SR_CODES.SHIMMER_GQ_802154_NR; // Normal Range
		public static final int SHIMMER_4_SDK 			= HW_ID_SR_CODES.SHIMMER_4_SDK;
		public static final int SHIMMER_ECG_MD 			= HW_ID_SR_CODES.SHIMMER_ECG_MD;
		
		public static final int VERISENSE_IMU 			= HW_ID_SR_CODES.VERISENSE_IMU;
		public static final int VERISENSE_GSR_PLUS 		= HW_ID_SR_CODES.VERISENSE_GSR_PLUS;
		public static final int VERISENSE_PPG 			= HW_ID_SR_CODES.VERISENSE_PPG;
		public static final int VERISENSE_DEV_BRD 		= HW_ID_SR_CODES.VERISENSE_DEV_BRD;
		public static final int VERISENSE_PULSE_PLUS	= HW_ID_SR_CODES.VERISENSE_PULSE_PLUS;

		//Third party devices
		public static final int NONIN_ONYX_II 			= 1000;
		public static final int QTI_DIRECT_TEMP 		= 1001;
		public static final int KEYBOARD_AND_MOUSE 		= 1002;
		public static final int ARDUINO 				= 1003;
		public static final int WEBCAM_GENERIC 			= 1004;
		public static final int WEBCAM_LOGITECH_HD_C920	= 1005;
		public static final int WEBCAM_LOGITECH_HD_C930E	= 1006;
		public static final int HOST_CPU_USAGE				= 1007;
		public static final int WEBCAM_DIGIOPTIX_SMART_GLASSES	= 1008;
		public static final int KEYBOARD 		= 1009;
		
		// Any other 'devices' that don't have a ShimmerDevice instance should
		// go in at >=2000 (currently used in the database for the results
		// aggregators)
		public static final int RESULT_AGGREGATOR		= 2000;
	}
	
	//WARNING! The name can't change because the database relies on it.
	public static final Map<Integer, String> mMapOfShimmerRevisions;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        aMap.put(HW_ID.UNKNOWN, UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN);
        aMap.put(HW_ID.SHIMMER_1, "Shimmer1");
        aMap.put(HW_ID.SHIMMER_2, "Shimmer2");
        aMap.put(HW_ID.SHIMMER_2R, "Shimmer2r");
        aMap.put(HW_ID.SHIMMER_3, "Shimmer3");
        aMap.put(HW_ID.SHIMMER_3R, "Shimmer3R");
        aMap.put(HW_ID.SHIMMER_SR30, "Shimmer_SR30");
        aMap.put(HW_ID.SHIMMER_GQ_BLE, "ShimmerGQBle");
        aMap.put(HW_ID.SWEATCH, "SwEatch");
        aMap.put(HW_ID.SHIMMER_2R_GQ, "Shimmer2rGQ");
        aMap.put(HW_ID.SHIMMER_GQ_802154_LR, "ShimmerGQ");
        aMap.put(HW_ID.SHIMMER_GQ_802154_NR, "ShimmerGQ");
        aMap.put(HW_ID.SHIMMER_4_SDK, "Shimmer4 SDK");
        aMap.put(HW_ID.SHIMMER_ECG_MD, "Shimmer ECGmd");
                
        aMap.put(HW_ID.NONIN_ONYX_II, "Nonin Onyx II");
        aMap.put(HW_ID.QTI_DIRECT_TEMP, "QTI Direct Temp");
        aMap.put(HW_ID.KEYBOARD_AND_MOUSE, "Keyboard and Mouse");
        aMap.put(HW_ID.KEYBOARD, "Keyboard");
        aMap.put(HW_ID.ARDUINO, "Arduino");
        aMap.put(HW_ID.WEBCAM_GENERIC, "Webcam");
        aMap.put(HW_ID.WEBCAM_LOGITECH_HD_C920, "Webcam");
        aMap.put(HW_ID.WEBCAM_LOGITECH_HD_C930E, "Webcam");
        aMap.put(HW_ID.WEBCAM_DIGIOPTIX_SMART_GLASSES, "Webcam");
        aMap.put(HW_ID.VERISENSE_IMU, "Verisense IMU");
        aMap.put(HW_ID.VERISENSE_GSR_PLUS, "Verisense GSR+");
        aMap.put(HW_ID.VERISENSE_PPG, "Verisense PPG");
        aMap.put(HW_ID.VERISENSE_DEV_BRD, "Verisense Dev Brd");
        aMap.put(HW_ID.VERISENSE_PULSE_PLUS, "Verisense Pulse+");
        
        mMapOfShimmerRevisions = Collections.unmodifiableMap(aMap);
    }
    
	//TODO change all to ENUMs with ints and Strings passed in
	public static final class HW_ID_SR_CODES {
		public static final int LOG_FILE 					= -2; //MN: here for testing
		public static final int UNKNOWN 					= -1;
		public static final int NONE 						= 255;
		
		public static final int EXP_BRD_BR_AMP 				= 8;
		public static final int SPAN 						= 9;
		public static final int EXP_BRD_GSR 				= 14;
		public static final int SHIMMER3 					= 31;
		public static final int EXP_BRD_PROTO3_MINI 		= 36;
		public static final int EXP_BRD_EXG 				= 37;
		public static final int EXP_BRD_PROTO3_DELUXE 		= 38;
		public static final int BASE15U 					= 41;
		public static final int BASE6U 						= 42;
		public static final int EXP_BRD_ADXL377_ACCEL_200G 	= 44;
		public static final int EXP_BRD_GPS 				= 46;
		public static final int EXP_BRD_EXG_UNIFIED 		= 47;
		public static final int EXP_BRD_GSR_UNIFIED 		= 48;
		public static final int EXP_BRD_BR_AMP_UNIFIED 		= 49;
		public static final int EXP_BRD_H3LIS331DL_ACCEL_HIGH_G 	= 55;
		
		public static final int SHIMMER_GQ_802154_LR 		= 56;
		public static final int SHIMMER_GQ_802154_NR 		= 57;
		public static final int SHIMMER_4_SDK 				= 58;
		public static final int SHIMMER_ECG_MD 				= 59;
		public static final int VERISENSE_IMU				= 61;
		public static final int VERISENSE_GSR_PLUS			= 62;
		public static final int VERISENSE_PPG				= 63;
		public static final int VERISENSE_DEV_BRD			= 64;
		public static final int VERISENSE_PULSE_PLUS		= 68;
	}
	
	public static final Map<Integer, String> mMapOfShimmerHardware;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        aMap.put(HW_ID_SR_CODES.NONE, "None");
        aMap.put(HW_ID_SR_CODES.UNKNOWN, "Unknown");
        
        aMap.put(HW_ID_SR_CODES.EXP_BRD_BR_AMP, "Bridge Amplifier+");
        aMap.put(HW_ID_SR_CODES.SPAN, "Span");//not used
        aMap.put(HW_ID_SR_CODES.EXP_BRD_GSR, "GSR+");
        aMap.put(HW_ID_SR_CODES.SHIMMER3, "IMU");//not used
        aMap.put(HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI, "PROTO3 Mini");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_EXG, "ECG/EMG");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE, "PROTO3 Deluxe");
        aMap.put(HW_ID_SR_CODES.BASE15U, "Base15U");
        aMap.put(HW_ID_SR_CODES.BASE6U, "Base6U");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_ADXL377_ACCEL_200G, "200g Accel");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_GPS, "GPS");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED, "ECG/EMG/Resp");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED, "GSR+");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED, "Bridge Amplifier+");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_H3LIS331DL_ACCEL_HIGH_G, "High-g Accel");
        
        aMap.put(HW_ID_SR_CODES.SHIMMER_GQ_802154_LR, "ShimmerGQ 802.15.4");
        aMap.put(HW_ID_SR_CODES.SHIMMER_GQ_802154_NR, "ShimmerGQ 802.15.4");
        aMap.put(HW_ID_SR_CODES.SHIMMER_4_SDK, "Shimmer4 SDK");
        aMap.put(HW_ID_SR_CODES.SHIMMER_ECG_MD, "Shimmer ECGmd");
        aMap.put(HW_ID_SR_CODES.VERISENSE_IMU, "Verisense IMU");
        aMap.put(HW_ID_SR_CODES.VERISENSE_GSR_PLUS, "Verisense GSR+");
        aMap.put(HW_ID_SR_CODES.VERISENSE_PPG, "Verisense PPG");
        aMap.put(HW_ID_SR_CODES.VERISENSE_DEV_BRD, "Verisense Dev Brd");
        aMap.put(HW_ID_SR_CODES.VERISENSE_PULSE_PLUS, "Verisense Pulse+");
        
        mMapOfShimmerHardware = Collections.unmodifiableMap(aMap);
    }

	//TODO change all to ENUMs with ints and Strings passed in
	public static final class FW_ID {
		/**
		 * FW_ID is hardware generation dependent, not hardware version
		 * dependent (e.g., Shimmer3 covers a range of custom hardware all based
		 * on the same microcontroller in the Shimmmer3)
		 */
		public static final int UNKNOWN = -1;
		public static final int BOILER_PLATE = 0;
		public static final int BTSTREAM = 1;
		public static final int SDLOG = 2;
		public static final int LOGANDSTREAM = 3;
		public static final int SWEATCH = 4;
		public static final int GQ_BLE = 5;
		public static final int BIOSENSICS_GPIO_TEST = 6;
//		public static final int ? = 7;
		public static final int MOVOTEC_PSAD = 8;
		public static final int GQ_802154 = 9;
		public static final int EXGSTREAM = 10;
		public static final int SPAN = 11;
		public static final int SHIMMER4_SDK_STOCK = 12;
		public static final int BTSTREAM_UARTSTREAM = 13;
		public static final int BTSTREAM_CE = 14;
		public static final int STROKARE = 15;
		public static final int SHIMMER_ECG_MD = 16;
		public static final int VERISENSE = 17;
		
		public static final Map<Integer, String> mMapOfFirmwareLabels;
	    static {
	        Map<Integer, String> aMap = new TreeMap<Integer,String>();
	        aMap.put(FW_ID.BTSTREAM, "BtStream");
	        aMap.put(FW_ID.SDLOG, "SDLog");
	        aMap.put(FW_ID.LOGANDSTREAM, "LogAndStream");
	        aMap.put(FW_ID.SWEATCH, "SwEatch");
	        aMap.put(FW_ID.GQ_BLE, "GQ_BLE");
	        aMap.put(FW_ID.BIOSENSICS_GPIO_TEST, "GPIO_TEST");
	        aMap.put(FW_ID.GQ_802154, "GQ_802154");
	        aMap.put(FW_ID.SPAN, "SPAN");
	        aMap.put(FW_ID.SHIMMER4_SDK_STOCK, "Shimmer4SDK");
	        aMap.put(FW_ID.STROKARE, "StroKare");
	        aMap.put(FW_ID.SHIMMER_ECG_MD, "ShimmerECGmd");
	        aMap.put(FW_ID.VERISENSE, "Verisense");
	        mMapOfFirmwareLabels = Collections.unmodifiableMap(aMap);
	    }

		public static final class BASES {
			public static final int BASE15U_REV2 = 0;
			public static final int BASE15U_REV4 = 1;
			public static final int BASE6U = 2;
		}
	}
	
	public static final class FW_LABEL {
		public static final String UNKNOWN = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
	}


}
