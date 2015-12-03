package com.shimmerresearch.uartViaDock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.uartViaDock.ComponentPropertyDetails.PERMISSION;

/**
 * Contains the packet contents for communication via the Shimmer's UART.
 * 
 * @author Mark Nolan
 *
 */
public class UartPacketDetails {
	
	public static String PACKET_HEADER = "$"; 
	
	/** Class listing all of the Shimmer UART data packet commands
	 *
	 */
	public static class PACKET_CMD {
		final static int SET                   = 0x01;
		final static int DATA_RESPONSE         = 0x02;
		final static int GET                   = 0x03;
		final static int BAD_CMD_RESPONSE      = 0xfc; //252
		final static int BAD_ARG_RESPONSE      = 0xfd; //253
		final static int BAD_CRC_RESPONSE      = 0xfe; //254
		final static int ACK_RESPONSE          = 0xff; //255
	}
	
	/** Class listing all of the components that can be configured using the Shimmer UART commands
	 *
	 */
	private static class COMPONENT {
		final static int SHIMMER          = 0x01;
		final static int BAT              = 0x02; // this is seen as a sensor
		final static int DAUGHTER_CARD    = 0x03;
		final static int PPG              = 0x04;
		final static int GSR              = 0x05;
		final static int LSM303DLHC_ACCEL = 0x06;
		final static int MPU9X50_ACCEL	  = 0x07;
		final static int BEACON			  = 0x08;
		final static int RADIO_802154     = 0x09;
	}
	
	static ShimmerVerObject baseGqBle = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_BLE,FW_ID.GQ_BLE,0,0,5,ShimmerObject.ANY_VERSION);
	static ShimmerVerObject baseGq802154NR = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_802154_NR,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION);
	static ShimmerVerObject baseGq802154LR = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_802154_LR,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION);
	
	//TODO improve
	static List<ShimmerVerObject> listOfCompatibleVersionInfoGqBle = Arrays.asList(baseGqBle);
	static List<ShimmerVerObject> listOfCompatibleVersionInfoGq802154 = Arrays.asList(baseGq802154NR, baseGq802154LR);
	static List<ShimmerVerObject> listOfCompatibleVersionInfoGq = Arrays.asList(baseGqBle, baseGq802154NR, baseGq802154LR);
	

	
	/** Class listing all of the components and property combinations that can be used with the Shimmer UART commands
	 *
	 */
	public static class COMPONENT_PROPERTY {
		public static class SHIMMER {
			public final static ComponentPropertyDetails ENABLE           = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public final static ComponentPropertyDetails SAMPLE_RATE      = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x01, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "SAMPLE_RATE");
			public final static ComponentPropertyDetails MAC              = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x02, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "MAC");
			public final static ComponentPropertyDetails VER              = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x03, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "VER");
			public final static ComponentPropertyDetails RTC_CFG_TIME     = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x04, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "RTC_CFG_TIME");
			public final static ComponentPropertyDetails CURR_LOCAL_TIME  = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x05, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "CURR_LOCAL_TIME");
			public final static ComponentPropertyDetails INFOMEM          = new ComponentPropertyDetails(COMPONENT.SHIMMER, 0x06, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "INFOMEM");
		}
		public static class BAT {
			public final static ComponentPropertyDetails ENABLE           = new ComponentPropertyDetails(COMPONENT.BAT, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public final static ComponentPropertyDetails VALUE            = new ComponentPropertyDetails(COMPONENT.BAT, 0x02, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "VALUE");
			public final static ComponentPropertyDetails FREQ_DIVIDER     = new ComponentPropertyDetails(COMPONENT.BAT, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		public static class GSR {
			public final static ComponentPropertyDetails ENABLE           = new ComponentPropertyDetails(COMPONENT.GSR, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public final static ComponentPropertyDetails RANGE            = new ComponentPropertyDetails(COMPONENT.GSR, 0x03, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "RANGE");
			public final static ComponentPropertyDetails FREQ_DIVIDER     = new ComponentPropertyDetails(COMPONENT.GSR, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		public static class PPG {
			public final static ComponentPropertyDetails ENABLE           = new ComponentPropertyDetails(COMPONENT.PPG, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public final static ComponentPropertyDetails FREQ_DIVIDER     = new ComponentPropertyDetails(COMPONENT.PPG, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		public static class DAUGHTER_CARD {
			public final static ComponentPropertyDetails CARD_ID          = new ComponentPropertyDetails(COMPONENT.DAUGHTER_CARD, 0x02, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGq, "CARD_ID");
			public final static ComponentPropertyDetails CARD_MEM         = new ComponentPropertyDetails(COMPONENT.DAUGHTER_CARD, 0x03, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGq, "CARD_MEM");
		}
		public static class LSM303DLHC_ACCEL {
			public final static ComponentPropertyDetails ENABLE           = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public final static ComponentPropertyDetails DATA_RATE        = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x02, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DATA_RATE");
			public final static ComponentPropertyDetails RANGE            = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x03, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "RANGE");
			public final static ComponentPropertyDetails LP_MODE          = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x04, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "LP_MODE");
			public final static ComponentPropertyDetails HR_MODE          = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x05, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "HR_MODE");
			public final static ComponentPropertyDetails FREQ_DIVIDER     = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "FREQ_DIVIDER");
			public final static ComponentPropertyDetails CALIBRATION      = new ComponentPropertyDetails(COMPONENT.LSM303DLHC_ACCEL, 0x07, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "CALIBRATION");
		}
		public static class BEACON {
			public final static ComponentPropertyDetails ENABLE           = new ComponentPropertyDetails(COMPONENT.BEACON, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public final static ComponentPropertyDetails FREQ_DIVIDER     = new ComponentPropertyDetails(COMPONENT.BEACON, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		
		public static class RADIO_802154 {
			public final static ComponentPropertyDetails SETTINGS         = new ComponentPropertyDetails(COMPONENT.RADIO_802154, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGq802154, "SETTINGS");
		}

	}

	public static final List<ComponentPropertyDetails> mListOfUartCommandsConfig;
    static {
    	List<ComponentPropertyDetails> aMap = new ArrayList<ComponentPropertyDetails>();
        
        aMap.add(COMPONENT_PROPERTY.BAT.ENABLE);
        aMap.add(COMPONENT_PROPERTY.BAT.FREQ_DIVIDER);

    	aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.ENABLE);
        aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.DATA_RATE);
        aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.RANGE);
        aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.LP_MODE);
        aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.HR_MODE);
        aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.FREQ_DIVIDER);
        aMap.add(COMPONENT_PROPERTY.LSM303DLHC_ACCEL.CALIBRATION);
        
        aMap.add(COMPONENT_PROPERTY.GSR.ENABLE);
        aMap.add(COMPONENT_PROPERTY.GSR.RANGE);
        aMap.add(COMPONENT_PROPERTY.GSR.FREQ_DIVIDER);

        aMap.add(COMPONENT_PROPERTY.BEACON.ENABLE);
        aMap.add(COMPONENT_PROPERTY.BEACON.FREQ_DIVIDER);

        aMap.add(COMPONENT_PROPERTY.RADIO_802154.SETTINGS);

        mListOfUartCommandsConfig = Collections.unmodifiableList(aMap);
    }

    
    
    public static String parseCmd(int cmd){
    	if(cmd == PACKET_CMD.SET){
    		return "SET";
    	}
    	else if(cmd == PACKET_CMD.DATA_RESPONSE){
    		return "DATA_RESPONSE";
    	}
    	else if(cmd == PACKET_CMD.GET){
    		return "GET";
    	}
    	else if(cmd == PACKET_CMD.BAD_CMD_RESPONSE){
    		return "BAD_CMD_RESPONSE";
    	}
    	else if(cmd == PACKET_CMD.BAD_ARG_RESPONSE){
    		return "BAD_ARG_RESPONSE";
    	}
    	else if(cmd == PACKET_CMD.BAD_CRC_RESPONSE){
    		return "BAD_CRC_RESPONSE";
    	}
    	else if(cmd == PACKET_CMD.ACK_RESPONSE){
    		return "ACK_RESPONSE";
    	}
    	return Integer.toString(cmd);
    }
    
    public static String parseComponent(int component){
    	if(component == COMPONENT.SHIMMER){
    		return "SHIMMER";
    	}
    	else if(component == COMPONENT.BAT){
    		return "BAT";
    	}
    	else if(component == COMPONENT.DAUGHTER_CARD){
    		return "DAUGHTER_CARD";
    	}
    	else if(component == COMPONENT.LSM303DLHC_ACCEL){
    		return "LSM303DLHC_ACCEL";
    	}
    	else if(component == COMPONENT.GSR){
    		return "GSR";
    	}
    	else if(component == COMPONENT.MPU9X50_ACCEL){
    		return "MPU9X50_ACCEL";
    	}
    	else if(component == COMPONENT.BEACON){
    		return "BEACON";
    	}
    	else if(component == COMPONENT.RADIO_802154){
    		return "RADIO_802154";
    	}
    	return Integer.toString(component);
    }

}    
