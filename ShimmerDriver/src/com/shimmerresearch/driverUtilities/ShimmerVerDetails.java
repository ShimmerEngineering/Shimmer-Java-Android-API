package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.UtilShimmer;

/**
 * Holds Shimmer Hardware and Firmware version details.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerVerDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7940733886215010795L;
	
	public final static int EXP_BRD_NONE_ID = 255;
	public static final String EXP_BRD_NONE = "None";
	
	public final static class HW_ID {
		public final static int UNKNOWN = -1;
		public final static int SHIMMER_1 = 0;
		public final static int SHIMMER_2 = 1;
		public final static int SHIMMER_2R = 2;
		public final static int SHIMMER_3 = 3;
		public final static int SHIMMER_SR30 = 4;
//		public final static int DCU_SWEATSENSOR = 4;
		public final static int SHIMMER_GQ_BLE = 5;
//		public final static int BIOSENSICS = 6;
		public final static int SHIMMER_GQ_802154 = 56;
	}
	
	public static final Map<Integer, String> mMapOfShimmerRevisions;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        aMap.put(HW_ID.UNKNOWN, UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN);
        aMap.put(HW_ID.SHIMMER_1, "Shimmer1");
        aMap.put(HW_ID.SHIMMER_2, "Shimmer2");
        aMap.put(HW_ID.SHIMMER_2R, "Shimmer2r");
        aMap.put(HW_ID.SHIMMER_3, "Shimmer3");
        aMap.put(HW_ID.SHIMMER_SR30, "Shimmer SR30");
        aMap.put(HW_ID.SHIMMER_GQ_BLE, "ShimmerGQBle");
//        aMap.put(HW_ID.DCU_SWEATSENSOR, "DCU_SWEATSENSOR");
        aMap.put(HW_ID.SHIMMER_GQ_802154, "ShimmerGQ");
        mMapOfShimmerRevisions = Collections.unmodifiableMap(aMap);
    }
    
	public static final class HW_ID_SR_CODES {
		public final static int UNKNOWN = -1;
		public final static int EXP_BRD_BR_AMP = 8;
		public final static int EXP_BRD_BR_AMP_UNIFIED = 49;
		public final static int EXP_BRD_GSR = 14;
		public final static int EXP_BRD_GSR_UNIFIED = 48;
		public final static int EXP_BRD_PROTO3_MINI = 36;
		public final static int EXP_BRD_EXG = 37;
		public final static int EXP_BRD_EXG_UNIFIED = 47;
		public final static int EXP_BRD_PROTO3_DELUXE = 38;
		public final static int EXP_BRD_HIGH_G_ACCEL = 44;
		public final static int EXP_BRD_GPS = 46;
		
		public final static int SHIMMER_GQ_802154 = 56;
		
		public final static int SHIMMER3 = 31;
		public final static int BASE15U = 41;
		public final static int BASE6U = 42;
	}
	
	public static final Map<Integer, String> mMapOfShimmerHardware;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
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
        
        aMap.put(HW_ID_SR_CODES.SHIMMER_GQ_802154, "ShimmerGQ 802.15.4");
        
        aMap.put(HW_ID_SR_CODES.BASE15U, "Base15U");
        aMap.put(HW_ID_SR_CODES.BASE6U, "Base6U");
        mMapOfShimmerHardware = Collections.unmodifiableMap(aMap);
    }

	public final static class FW_ID {
		/**
		 * FW_ID is hardware generation dependent, not hardware version
		 * dependent (e.g., Shimmer3 covers a range of custom hardware all based
		 * on the same microcontroller in the Shimmmer3)
		 */
		public final static int BOILER_PLATE = 0;
		public final static int BTSTREAM = 1;
		public final static int SDLOG = 2;
		public final static int LOGANDSTREAM = 3;
		public final static int DCU_SWEATSENSOR = 4;
		public final static int GQ_BLE = 5;
		public final static int GPIO_TEST = 6;
		public final static int GQ_802154 = 9;
		
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
	        mMapOfFirmwareLabels = Collections.unmodifiableMap(aMap);
	    }

		public final static class BASES {
			public final static int BASE15U_REV2 = 0;
			public final static int BASE15U_REV4 = 1;
			public final static int BASE6U = 2;
		}
		
		public static final int UNKNOWN = -1;
	}
	
	public final static class FW_LABEL {
		public final static String UNKNOWN = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
//		public final static String BOILERPLATE = "BoilerPlate";
//		public final static String BTSTREAM = "BtStream";
//		public final static String SDLOG = "SDLog";
//		public final static String LOGANDSTREAM = "LogAndStream";
//		public final static String DCU_SWEATSENSOR = "Swatch";
//		public final static String GQ_BLE = "GQ_BLE";
//		public final static String GPIO_TEST = "GPIO_TEST";
//		public final static String GQ_802154 = "GQ_802154";
	}


}
