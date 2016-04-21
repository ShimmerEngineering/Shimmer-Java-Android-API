package com.shimmerresearch.comms.serialPortInterface;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driverUtilities.AbstractErrorCodes;

/**
 * 
 * @author Mark Nolan
 *
 */
public class ErrorCodesSerialPort extends AbstractErrorCodes {

	public final static int ERROR_CODES_ID = 2;
	
    // low level errors
	public final static int ERR_NONE = 0;
	public final static int SHIMMERUART_COMM_ERR_PORT_EXCEPTION = 		(ERROR_CODES_ID*1000) + 4;
	public final static int SHIMMERUART_COMM_ERR_TIMEOUT = 				(ERROR_CODES_ID*1000) + 5;
	public final static int SHIMMERUART_COMM_ERR_PACKAGE_FORMAT = 		(ERROR_CODES_ID*1000) + 7;
	public final static int SHIMMERUART_COMM_ERR_WRITING_DATA = 		(ERROR_CODES_ID*1000) + 8;
	public final static int SHIMMERUART_COMM_ERR_READING_DATA = 		(ERROR_CODES_ID*1000) + 9;
	public final static int SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS = 	(ERROR_CODES_ID*1000) + 14;

	public final static int SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING = 	(ERROR_CODES_ID*1000) + 15;
	public final static int SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING = 	(ERROR_CODES_ID*1000) + 16;

	public final static int SHIMMERUART_COMM_ERR_PORT_READER_START = 	(ERROR_CODES_ID*1000) + 65;
	public final static int SHIMMERUART_COMM_ERR_PORT_READER_STOP = 	(ERROR_CODES_ID*1000) + 66;
	
    public static final Map<Integer, String> mMapOfErrorCodes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
    	aMap.put(ERR_NONE, "SHIMMERUART_COMM_ERR_NONE");
        // low level errors
    	aMap.put(SHIMMERUART_COMM_ERR_PORT_EXCEPTION, "ShimmerUART_Comm_Err_Port_Exception");
    	aMap.put(SHIMMERUART_COMM_ERR_TIMEOUT, "COM port timeout");
        aMap.put(SHIMMERUART_COMM_ERR_PACKAGE_FORMAT, "ShimmerUART_Comm_Err_Package_Format");
        aMap.put(SHIMMERUART_COMM_ERR_WRITING_DATA, "ShimmerUART_Comm_Err_Writing_Data");
    	aMap.put(SHIMMERUART_COMM_ERR_READING_DATA, "ShimmerUART_Comm_Err_Reading_Data");
    	aMap.put(SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS, "ShimmerUART_Comm_Err_Message_Content");

    	aMap.put(SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING, "ShimmerUART_Comm_Err_Port_Exception_Opening");
    	aMap.put(SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING, "ShimmerUART_Comm_Err_Port_Exception_Closing");
    	
    	aMap.put(SHIMMERUART_COMM_ERR_PORT_READER_START, "ShimmerUART_Comm_Err_Port_Starting_Reader");
    	aMap.put(SHIMMERUART_COMM_ERR_PORT_READER_STOP, "ShimmerUART_Comm_Err_Port_Stopping_Reader");
    	
    	mMapOfErrorCodes = Collections.unmodifiableMap(aMap);
    }
   
}
