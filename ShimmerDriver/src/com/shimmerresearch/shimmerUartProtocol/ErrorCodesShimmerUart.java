package com.shimmerresearch.shimmerUartProtocol;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author Mark Nolan
 *
 */
public class ErrorCodesShimmerUart {

	public final static int ERROR_CODES_ID = 2;
	
	// Error type/source/command
	public final static int SHIMMERUART_CMD_ERR_MAC_ID_GET = 			(ERROR_CODES_ID*1000) + 21;
	public final static int SHIMMERUART_CMD_ERR_VERSION_INFO_GET = 		(ERROR_CODES_ID*1000) + 22;
	public final static int SHIMMERUART_CMD_ERR_BATT_STATUS_GET = 		(ERROR_CODES_ID*1000) + 23;
	public final static int SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_SET = 	(ERROR_CODES_ID*1000) + 25;
	public final static int SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_GET = 	(ERROR_CODES_ID*1000) + 24;
	public final static int SHIMMERUART_CMD_ERR_RTC_CURRENT_TIME_GET = 	(ERROR_CODES_ID*1000) + 26;
	public final static int SHIMMERUART_CMD_ERR_DAUGHTER_ID_SET = 		(ERROR_CODES_ID*1000) + 28;
	public final static int SHIMMERUART_CMD_ERR_DAUGHTER_ID_GET = 		(ERROR_CODES_ID*1000) + 27;
	public final static int SHIMMERUART_CMD_ERR_DAUGHTER_MEM_SET = 		(ERROR_CODES_ID*1000) + 30;
	public final static int SHIMMERUART_CMD_ERR_DAUGHTER_MEM_GET = 		(ERROR_CODES_ID*1000) + 29;
	public final static int SHIMMERUART_CMD_ERR_INFOMEM_SET = 			(ERROR_CODES_ID*1000) + 32;
	public final static int SHIMMERUART_CMD_ERR_INFOMEM_GET = 			(ERROR_CODES_ID*1000) + 31;

	//SR7 (802.15.4 radio) related errors
	public final static int SHIMMERUART_CMD_ERR_RADIO_802154_SET_SETTINGS = 			(ERROR_CODES_ID*1000) + 33;
	public final static int SHIMMERUART_CMD_ERR_RADIO_802154_GET_SETTINGS = 			(ERROR_CODES_ID*1000) + 34;
	public final static int SHIMMERUART_CMD_ERR_RADIO_802154_SPECTRUM_ANALYSER = 		(ERROR_CODES_ID*1000) + 35;
	public final static int SHIMMERUART_CMD_ERR_RADIO_802154_RAW = 		(ERROR_CODES_ID*1000) + 36;

	
	// Errors related to specific commands
	public final static int SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE = (ERROR_CODES_ID*1000) + 1;
	public final static int SHIMMERUART_INFOMEM_READ_REQEST_EXCEEDS_INFO_RANGE = (ERROR_CODES_ID*1000) + 2;
	
    // low level errors
	public final static int ERR_NONE = 0;
	public final static int SHIMMERUART_COMM_ERR_PORT_EXCEPTION = 		(ERROR_CODES_ID*1000) + 4;
	public final static int SHIMMERUART_COMM_ERR_TIMEOUT = 				(ERROR_CODES_ID*1000) + 5;
	public final static int SHIMMERUART_COMM_ERR_CRC = 					(ERROR_CODES_ID*1000) + 6;
	public final static int SHIMMERUART_COMM_ERR_PACKAGE_FORMAT = 		(ERROR_CODES_ID*1000) + 7;
	public final static int SHIMMERUART_COMM_ERR_WRITING_DATA = 		(ERROR_CODES_ID*1000) + 8;
	public final static int SHIMMERUART_COMM_ERR_READING_DATA = 		(ERROR_CODES_ID*1000) + 9;
	public final static int SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD = 	(ERROR_CODES_ID*1000) + 10;
	public final static int SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG = 	(ERROR_CODES_ID*1000) + 11;
	public final static int SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC = 	(ERROR_CODES_ID*1000) + 12;
	public final static int SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED = 	(ERROR_CODES_ID*1000) + 13;
	public final static int SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS = 	(ERROR_CODES_ID*1000) + 14;

	public final static int SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING = 	(ERROR_CODES_ID*1000) + 15;
	public final static int SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING = 	(ERROR_CODES_ID*1000) + 16;
	
	
	public final static int SHIMMERUART_COMM_ERR_MAC_CHANGE = 			(ERROR_CODES_ID*1000) + 17;
	
	
    public static final Map<Integer, String> mMapOfErrorCodes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
    	aMap.put(ERR_NONE, "SHIMMERUART_COMM_ERR_NONE");

    	// Error type/source/command
    	aMap.put(SHIMMERUART_CMD_ERR_MAC_ID_GET, "Error reading MAC ID");
    	aMap.put(SHIMMERUART_CMD_ERR_VERSION_INFO_GET, "Error reading hw fw Shimmer version");
    	aMap.put(SHIMMERUART_CMD_ERR_BATT_STATUS_GET, "Error Reading Batt Status");
    	aMap.put(SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_SET, "Error writing RTC time");
    	aMap.put(SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_GET, "Error reading RTC config time");
    	aMap.put(SHIMMERUART_CMD_ERR_RTC_CURRENT_TIME_GET, "Error reading RTC current time");
    	aMap.put(SHIMMERUART_CMD_ERR_DAUGHTER_ID_SET, "Error writing expansion board ID");
    	aMap.put(SHIMMERUART_CMD_ERR_DAUGHTER_ID_GET, "Error reading expansion board ID");
    	aMap.put(SHIMMERUART_CMD_ERR_DAUGHTER_MEM_SET, "Error writing expansion board memory");
    	aMap.put(SHIMMERUART_CMD_ERR_DAUGHTER_MEM_GET, "Error reading expansion board memory");
    	aMap.put(SHIMMERUART_CMD_ERR_INFOMEM_SET, "Error writing InfoMem");
    	aMap.put(SHIMMERUART_CMD_ERR_INFOMEM_GET, "Error reading InfoMem");

    	//SR7 (802.15.4 radio) related errors
    	aMap.put(SHIMMERUART_CMD_ERR_RADIO_802154_GET_SETTINGS, "Error reading 802.15.4 radio settings");
    	aMap.put(SHIMMERUART_CMD_ERR_RADIO_802154_SET_SETTINGS, "Error writing 802.15.4 radio settings");
    	aMap.put(SHIMMERUART_CMD_ERR_RADIO_802154_SPECTRUM_ANALYSER, "Error reading spectrum analyser");
    	
    	// Errors related to specific commands
        aMap.put(SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE, "InfoMem write buffer exceeds InfoMem range");
        aMap.put(SHIMMERUART_INFOMEM_READ_REQEST_EXCEEDS_INFO_RANGE, "InfoMem read request exceeds InfoMem range");
        
        // low level errors
    	aMap.put(SHIMMERUART_COMM_ERR_PORT_EXCEPTION, "ShimmerUART_Comm_Err_Port_Exception");
    	aMap.put(SHIMMERUART_COMM_ERR_TIMEOUT, "COM port timeout");
    	aMap.put(SHIMMERUART_COMM_ERR_CRC, "ShimmerUART_Comm_Err_CRC");
        aMap.put(SHIMMERUART_COMM_ERR_PACKAGE_FORMAT, "ShimmerUART_Comm_Err_Package_Format");
        aMap.put(SHIMMERUART_COMM_ERR_WRITING_DATA, "ShimmerUART_Comm_Err_Writing_Data");
    	aMap.put(SHIMMERUART_COMM_ERR_READING_DATA, "ShimmerUART_Comm_Err_Reading_Data");
    	aMap.put(SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD, "ShimmerUART_Comm_Err_Response_Bad_CMD");
    	aMap.put(SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG, "ShimmerUART_Comm_Err_Resonse_Bad_ARG");
    	aMap.put(SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC, "ShimmerUART_Comm_Err_Response_Bad_CRC");
    	aMap.put(SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED, "ShimmerUART_Comm_Err_Response_Unexpected");
    	aMap.put(SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS, "ShimmerUART_Comm_Err_Message_Content");

    	aMap.put(SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING, "ShimmerUART_Comm_Err_Port_Exception_Opening");
    	aMap.put(SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING, "ShimmerUART_Comm_Err_Port_Exception_Closing");
    	
    	aMap.put(SHIMMERUART_COMM_ERR_MAC_CHANGE, "Change of Shimmer before an undock was detected");
    	
    	mMapOfErrorCodes = Collections.unmodifiableMap(aMap);
    }
   
	public ErrorCodesShimmerUart() {
	// TODO Auto-generated constructor stub
	}

}
