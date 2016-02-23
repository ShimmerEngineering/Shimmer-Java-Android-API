package com.shimmerresearch.shimmerUartProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.shimmerUartProtocol.UartComponentPropertyDetails.PERMISSION;

/**
 * Contains the packet contents for communication via the Shimmer's UART.
 * 
 * @author Mark Nolan
 *
 */
public class UartPacketDetails {
	
	public static String PACKET_HEADER = "$"; 
	
	/** Enum listing all of the Shimmer UART data packet commands
	 *
	 */
	public static enum UART_PACKET_CMD {
		WRITE				((byte)0x01),
		DATA_RESPONSE		((byte)0x02),
		READ				((byte)0x03),
		BAD_CMD_RESPONSE	((byte)0xfc),	//252
		BAD_ARG_RESPONSE	((byte)0xfd),	//253
		BAD_CRC_RESPONSE	((byte)0xfe),	//254
		ACK_RESPONSE		((byte)0xff);	//255
		
	    private final byte command;

	    /** @param command */
	    private UART_PACKET_CMD(final byte command) {
	        this.command = command;
	    }
	    
	    public byte toCmdByte() {
	        return command;
	    }
	}

	/** Enum listing all of the components that can be configured using the Shimmer UART commands
	 *
	 */
	public static enum UART_COMPONENT {
		MAIN_PROCESSOR		((byte)0x01),
		BAT					((byte)0x02), // this is treated as a sensor
		DAUGHTER_CARD		((byte)0x03),
		PPG					((byte)0x04),
		GSR					((byte)0x05),
		LSM303DLHC_ACCEL	((byte)0x06),
		MPU9X50_ACCEL		((byte)0x07),
		BEACON				((byte)0x08),
		RADIO_802154		((byte)0x09);
		
	    private final byte command;

	    /** @param command */
	    private UART_COMPONENT(final byte command) {
	        this.command = command;
	    }
	    
	    public byte toCmdByte() {
	        return command;
	    }
	}
	
	public static ShimmerVerObject baseGqBle = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_BLE,FW_ID.GQ_BLE,0,0,5,ShimmerObject.ANY_VERSION);
	public static ShimmerVerObject baseGq802154NR = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_802154_NR,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION);
	public static ShimmerVerObject baseGq802154LR = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_802154_LR,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION);
	public static ShimmerVerObject baseGq802154Shimmer2r  = 	new ShimmerVerObject(HW_ID.SHIMMER_2R_GQ,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION,ShimmerObject.ANY_VERSION);
	
	//TODO improve
	public static List<ShimmerVerObject> listOfCompatibleVersionInfoGqBle = Arrays.asList(baseGqBle);
	public static List<ShimmerVerObject> listOfCompatibleVersionInfoGq802154 = Arrays.asList(baseGq802154NR, baseGq802154LR, baseGq802154Shimmer2r);
	public static List<ShimmerVerObject> listOfCompatibleVersionInfoGq = Arrays.asList(baseGqBle, baseGq802154NR, baseGq802154LR, baseGq802154Shimmer2r);

	
	/** Class listing all of the components and property combinations that can be used with the Shimmer UART commands
	 *
	 */
	public static class UART_COMPONENT_PROPERTY {
		/** AKA the Shimmer itself or a SPAN dongle */
		public static class MAIN_PROCESSOR { 
			public static final UartComponentPropertyDetails ENABLE           = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public static final UartComponentPropertyDetails SAMPLE_RATE      = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x01, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "SAMPLE_RATE");
			public static final UartComponentPropertyDetails MAC              = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x02, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "MAC");
			public static final UartComponentPropertyDetails VER              = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x03, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "VER");
			public static final UartComponentPropertyDetails RTC_CFG_TIME     = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x04, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "RTC_CFG_TIME");
			public static final UartComponentPropertyDetails CURR_LOCAL_TIME  = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x05, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "CURR_LOCAL_TIME");
			public static final UartComponentPropertyDetails INFOMEM          = new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "INFOMEM");
			public static final UartComponentPropertyDetails LED0_TOGGLE		= new UartComponentPropertyDetails(UART_COMPONENT.MAIN_PROCESSOR, 0x07, PERMISSION.WRITE_ONLY, listOfCompatibleVersionInfoGq802154, "LED_TOGGLE");
		}
		public static class BAT {
			public static final UartComponentPropertyDetails ENABLE           = new UartComponentPropertyDetails(UART_COMPONENT.BAT, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public static final UartComponentPropertyDetails VALUE            = new UartComponentPropertyDetails(UART_COMPONENT.BAT, 0x02, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGqBle, "VALUE");
			public static final UartComponentPropertyDetails FREQ_DIVIDER     = new UartComponentPropertyDetails(UART_COMPONENT.BAT, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		public static class GSR {
			public static final UartComponentPropertyDetails ENABLE           = new UartComponentPropertyDetails(UART_COMPONENT.GSR, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public static final UartComponentPropertyDetails RANGE            = new UartComponentPropertyDetails(UART_COMPONENT.GSR, 0x03, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "RANGE");
			public static final UartComponentPropertyDetails FREQ_DIVIDER     = new UartComponentPropertyDetails(UART_COMPONENT.GSR, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		public static class PPG {
			public static final UartComponentPropertyDetails ENABLE           = new UartComponentPropertyDetails(UART_COMPONENT.PPG, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public static final UartComponentPropertyDetails FREQ_DIVIDER     = new UartComponentPropertyDetails(UART_COMPONENT.PPG, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		public static class DAUGHTER_CARD {
			public static final UartComponentPropertyDetails CARD_ID          = new UartComponentPropertyDetails(UART_COMPONENT.DAUGHTER_CARD, 0x02, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGq, "CARD_ID");
			public static final UartComponentPropertyDetails CARD_MEM         = new UartComponentPropertyDetails(UART_COMPONENT.DAUGHTER_CARD, 0x03, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGq, "CARD_MEM");
		}
		public static class LSM303DLHC_ACCEL {
			public static final UartComponentPropertyDetails ENABLE           = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public static final UartComponentPropertyDetails DATA_RATE        = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x02, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DATA_RATE");
			public static final UartComponentPropertyDetails RANGE            = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x03, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "RANGE");
			public static final UartComponentPropertyDetails LP_MODE          = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x04, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "LP_MODE");
			public static final UartComponentPropertyDetails HR_MODE          = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x05, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "HR_MODE");
			public static final UartComponentPropertyDetails FREQ_DIVIDER     = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "FREQ_DIVIDER");
			public static final UartComponentPropertyDetails CALIBRATION      = new UartComponentPropertyDetails(UART_COMPONENT.LSM303DLHC_ACCEL, 0x07, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "CALIBRATION");
		}
		public static class BEACON {
			public static final UartComponentPropertyDetails ENABLE           = new UartComponentPropertyDetails(UART_COMPONENT.BEACON, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "ENABLE");
			public static final UartComponentPropertyDetails FREQ_DIVIDER     = new UartComponentPropertyDetails(UART_COMPONENT.BEACON, 0x06, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGqBle, "DIVIDER");
		}
		
		public static class RADIO_802154 {
			public static final UartComponentPropertyDetails SETTINGS			= new UartComponentPropertyDetails(UART_COMPONENT.RADIO_802154, 0x00, PERMISSION.READ_WRITE, listOfCompatibleVersionInfoGq802154, "SETTINGS");
			public static final UartComponentPropertyDetails TX_TO_SHIMMER		= new UartComponentPropertyDetails(UART_COMPONENT.RADIO_802154, 0x05, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGq802154, "TX_TO_SHIMMER");
			public static final UartComponentPropertyDetails RX_FROM_SHIMMER	= new UartComponentPropertyDetails(UART_COMPONENT.RADIO_802154, 0x06, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGq802154, "RX_FROM_SHIMMER");
			public static final UartComponentPropertyDetails SPECTRUM_SCAN		= new UartComponentPropertyDetails(UART_COMPONENT.RADIO_802154, 0x07, PERMISSION.READ_ONLY, listOfCompatibleVersionInfoGq802154, "SPECTRUM_SCAN");
		}

	}

	public static final List<UartComponentPropertyDetails> mListOfUartCommandsConfig;
    static {
    	List<UartComponentPropertyDetails> aMap = new ArrayList<UartComponentPropertyDetails>();
        
        aMap.add(UART_COMPONENT_PROPERTY.BAT.ENABLE);
        aMap.add(UART_COMPONENT_PROPERTY.BAT.FREQ_DIVIDER);

    	aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.ENABLE);
        aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.DATA_RATE);
        aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.RANGE);
        aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.LP_MODE);
        aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.HR_MODE);
        aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.FREQ_DIVIDER);
        aMap.add(UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.CALIBRATION);
        
        aMap.add(UART_COMPONENT_PROPERTY.GSR.ENABLE);
        aMap.add(UART_COMPONENT_PROPERTY.GSR.RANGE);
        aMap.add(UART_COMPONENT_PROPERTY.GSR.FREQ_DIVIDER);

        aMap.add(UART_COMPONENT_PROPERTY.BEACON.ENABLE);
        aMap.add(UART_COMPONENT_PROPERTY.BEACON.FREQ_DIVIDER);

        aMap.add(UART_COMPONENT_PROPERTY.RADIO_802154.SETTINGS);

        mListOfUartCommandsConfig = Collections.unmodifiableList(aMap);
    }

}    
