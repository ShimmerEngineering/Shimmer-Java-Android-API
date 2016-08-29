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
//		public static final int DCU_SWEATSENSOR = 4;
		public static final int SHIMMER_GQ_BLE = 5;
//		public static final int BIOSENSICS = 6;
		public static final int SPAN = 7;
		public static final int SHIMMER_2R_GQ = 9; // Used for testing GQ
		public static final int SHIMMER_GQ_802154_LR = HW_ID_SR_CODES.SHIMMER_GQ_802154_LR; // Long Range
		public static final int SHIMMER_GQ_802154_NR = HW_ID_SR_CODES.SHIMMER_GQ_802154_NR; // Normal Range
		public static final int SHIMMER_4_SDK = HW_ID_SR_CODES.SHIMMER_4_SDK;

		//Third party devices
		public static final int NONIN_ONYX_II = 1000;
		public static final int QTI_DIRECT_TEMP = 1001;
		public static final int KEYBOARD_AND_MOUSE = 1002;
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
        aMap.put(HW_ID.SHIMMER_SR30, "Shimmer_SR30");
        aMap.put(HW_ID.SHIMMER_GQ_BLE, "ShimmerGQBle");
//        aMap.put(HW_ID.DCU_SWEATSENSOR, "DCU_SWEATSENSOR");
        aMap.put(HW_ID.SHIMMER_2R_GQ, "Shimmer2rGQ");
        aMap.put(HW_ID.SHIMMER_GQ_802154_LR, "ShimmerGQ");
        aMap.put(HW_ID.SHIMMER_GQ_802154_NR, "ShimmerGQ");
        aMap.put(HW_ID.SHIMMER_4_SDK, "Shimmer4 SDK");
        aMap.put(HW_ID.KEYBOARD_AND_MOUSE, "Keyboard and Mouse");
        mMapOfShimmerRevisions = Collections.unmodifiableMap(aMap);
    }
    
	//TODO change all to ENUMs with ints and Strings passed in
	public static final class HW_ID_SR_CODES {
		public static final int LOG_FILE = -2;
		public static final int UNKNOWN = -1;
		public static final int NONE = 255;
		
		public static final int EXP_BRD_BR_AMP = 8;
		public static final int EXP_BRD_BR_AMP_UNIFIED = 49;
		public static final int EXP_BRD_GSR = 14;
		public static final int EXP_BRD_GSR_UNIFIED = 48;
		public static final int EXP_BRD_PROTO3_MINI = 36;
		public static final int EXP_BRD_EXG = 37;
		public static final int EXP_BRD_EXG_UNIFIED = 47;
		public static final int EXP_BRD_PROTO3_DELUXE = 38;
		public static final int EXP_BRD_HIGH_G_ACCEL = 44;
		public static final int EXP_BRD_GPS = 46;
		
		public static final int SHIMMER_GQ_802154_LR = 56;
		public static final int SHIMMER_GQ_802154_NR = 57;
		public static final int SHIMMER_4_SDK = 58;

		public static final int SHIMMER3 = 31;
		public static final int BASE15U = 41;
		public static final int BASE6U = 42;
		public static final int SPAN = 9;
	}
	
	public static final Map<Integer, String> mMapOfShimmerHardware;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        aMap.put(HW_ID_SR_CODES.NONE, "None");
        aMap.put(HW_ID_SR_CODES.UNKNOWN, "Unknown");
        
        aMap.put(HW_ID_SR_CODES.EXP_BRD_BR_AMP, "Bridge Amplifier+");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED, "Bridge Amplifier+");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_GSR, "GSR+");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED, "GSR+");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI, "PROTO3 Mini");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_EXG, "ECG/EMG");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED, "ECG/EMG/Resp");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE, "PROTO3 Deluxe");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL, "High-g Accel");
        aMap.put(HW_ID_SR_CODES.EXP_BRD_GPS, "GPS");
        
        aMap.put(HW_ID_SR_CODES.SHIMMER_GQ_802154_LR, "ShimmerGQ 802.15.4");
        aMap.put(HW_ID_SR_CODES.SHIMMER_GQ_802154_NR, "ShimmerGQ 802.15.4");
        aMap.put(HW_ID_SR_CODES.SHIMMER_4_SDK, "Shimmer4 SDK");
        
        aMap.put(HW_ID_SR_CODES.SHIMMER3, "Shimmer3");//not used
        aMap.put(HW_ID_SR_CODES.BASE15U, "Base15U");
        aMap.put(HW_ID_SR_CODES.BASE6U, "Base6U");
        aMap.put(HW_ID_SR_CODES.SPAN, "Span");//not used
        
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
		public static final int DCU_SWEATSENSOR = 4;
		public static final int GQ_BLE = 5;
		public static final int GPIO_TEST = 6;
		public static final int GQ_802154 = 9;
		public static final int SPAN = 11;
		public static final int SHIMMER4_SDK_STOCK = 12;//7
		
		public static final Map<Integer, String> mMapOfFirmwareLabels;
	    static {
	        Map<Integer, String> aMap = new TreeMap<Integer,String>();
	        aMap.put(BTSTREAM, "BtStream");
	        aMap.put(SDLOG, "SDLog");
	        aMap.put(LOGANDSTREAM, "LogAndStream");
	        aMap.put(DCU_SWEATSENSOR, "Swatch");
	        aMap.put(GQ_BLE, "GQ_BLE");
	        aMap.put(GPIO_TEST, "GPIO_TEST");
	        aMap.put(GQ_802154, "GQ_802154");
	        aMap.put(SPAN, "SPAN");
	        aMap.put(SHIMMER4_SDK_STOCK, "Shimmer4SDK");
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
//		public static final String BOILERPLATE = "BoilerPlate";
//		public static final String BTSTREAM = "BtStream";
//		public static final String SDLOG = "SDLog";
//		public static final String LOGANDSTREAM = "LogAndStream";
//		public static final String DCU_SWEATSENSOR = "Swatch";
//		public static final String GQ_BLE = "GQ_BLE";
//		public static final String GPIO_TEST = "GPIO_TEST";
//		public static final String GQ_802154 = "GQ_802154";
	}


}
