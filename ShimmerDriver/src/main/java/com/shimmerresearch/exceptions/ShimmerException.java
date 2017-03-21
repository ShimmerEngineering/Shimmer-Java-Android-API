package com.shimmerresearch.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.shimmerresearch.driverUtilities.UtilShimmer;

/**
 * @author Mark Nolan
 *
 */
public class ShimmerException extends ExecutionException {

	private static final long serialVersionUID = -8040452709544630044L;

	public int mErrorCode;
	public int mErrorCodeLowLevel; 
	public String mComPort;
	public String mClassName;
	public String mUniqueID;
	
	/**Used to store additional error info (e.g. file path when copying data) 
	 * 
	 */
	public String mMessage = ""; // Currently used for SD copy fail messages
	/** Contains the error message as copied from a caught exception. 
	 * 
	 */
	public String mExceptionMsg = "";
	/** Contains the stracktrace as copied from a caught exception. 
	 * 
	 */
	public StackTraceElement[] mExceptionStackTrace;
	/**Indicates whether the Exception is critical to the operation underway.
	 * 
	 */
	public ExceptionLevel mExceptionLevel = ExceptionLevel.HIGH;
	
	public static enum ExceptionLevel {
		LOW,
		HIGH
	}

	/** ERROR_CODES_ID reference list
	 * 
	 * 0-999 = MsgDock
	 * 1000+ = ErrorCodesMspBsl
	 * 2000+ = ErrorCodesSerialPort
	 * 3000+ = ErrorCodesDock
	 * 4000+ = DockJobDetails
	 * 5000+ = ErrorCodesSpan
	 * 6000+ = ErrorCodesUpdateCheck
	 * 7000+ = ErrorCodesWiredProtocol
	 * 8000+ = SpanJobDetails
	 * 9000+ = ErrorCodesVideoManager
	 * 10000+ = 
	 * 
	 */
	public static HashMap<Integer,String> mMapOfErrorCodes = new HashMap<Integer,String>();
	
	public ShimmerException() {
		mExceptionStackTrace = UtilShimmer.getCurrentStackTrace();
	}

	public ShimmerException(String message) {
		super(message);
		mExceptionStackTrace = UtilShimmer.getCurrentStackTrace();
	}
	
	public ShimmerException(Exception e) {
		setStackTrace(e.getStackTrace());
		mMessage = e.getLocalizedMessage();
		mExceptionMsg = e.getMessage();
	}
	
	public ShimmerException(String uniqueId, String comPort, int errorType, int lowLevelErrorCode) {
		mUniqueID = uniqueId;
		mComPort = comPort;
		mErrorCode = errorType;
		mErrorCodeLowLevel = lowLevelErrorCode;
		mExceptionStackTrace = UtilShimmer.getCurrentStackTrace();
	}


	/** Currently used in VideoManager
	 * @param errorCode
	 * @param message
	 */
	public ShimmerException(int errorCode, String message) {
		mErrorCode = errorCode;
		mMessage = message;
		mExceptionStackTrace = UtilShimmer.getCurrentStackTrace();
	}

	public String getErrStringFormatted() {
		return getErrStringFormatted(mMapOfErrorCodes);
	}

	private String getErrStringFormatted(Map<Integer, String> mapOfErrorCodes) {
		String errorString = "";

		String id = mUniqueID;
//		if(mSlotNumber == -1) {
//			id = mDockID;
//		}
		String errorCode = "Unknown Error";
		String lowLevelErrorCode = "Unknown Error";
		if(mapOfErrorCodes!=null){
			if(mapOfErrorCodes.containsKey(mErrorCode)) {
				errorCode = mapOfErrorCodes.get(mErrorCode);
			}
			if(mapOfErrorCodes.containsKey(mErrorCodeLowLevel)) {
				lowLevelErrorCode = mapOfErrorCodes.get(mErrorCodeLowLevel);
			}
		}
		String exceptionInfo = "";
		if(mExceptionMsg!=null && !mExceptionMsg.isEmpty()) {
			exceptionInfo = "Further info: " + mExceptionMsg;
		}
		String messageInfo = "";
		if(mMessage!=null && !mMessage.isEmpty()) {
			messageInfo = "Further info: " + mMessage;
		}

		errorString += ("CAUGHT SHIMMER DEVICE EXCEPTION\n");
		errorString += ("\t" + "UniqueID: " + id
				+ "\n\t" + "Action: " + "(" + mErrorCode + ") " + errorCode 
				+ "\n\t" + "LowLevelError: " + "(" + mErrorCodeLowLevel + ") " + lowLevelErrorCode
				+ "\n\t" + exceptionInfo
				+ "\n\t" + messageInfo
				+ "\n");
		
		String stackTraceString = convertStackTraceToString();
		if(!stackTraceString.isEmpty()) {
			errorString += (stackTraceString);
		}
		
		return errorString;
	}

	public String convertStackTraceToString(){
		if(mExceptionStackTrace!=null){
			return UtilShimmer.convertStackTraceToString(mExceptionStackTrace);
		}
		else {
			return "";
		}
	}

	public void updateDeviceException(String exceptionMsg, StackTraceElement[] exceptionStacktrace) {
		mExceptionMsg = exceptionMsg;
		mExceptionStackTrace = exceptionStacktrace;
	}

	public void updateDeviceException(Exception e) {
		updateDeviceException(e.getMessage(), e.getStackTrace());
	}

	public static void addToMapOfErrorCodes(Map<Integer, String> mapOfErrorCodes) {
//		System.out.println("Adding to ErrorMap.\t Was size = " + mMapOfErrorCodes.size());
		mMapOfErrorCodes.putAll(mapOfErrorCodes);
//		System.out.println("Adding to ErrorMap.\t Now size = " + mMapOfErrorCodes.size());
	}

	public static void addToMapOfErrorCodes(List<Map<Integer, String>> errorMapsToLoad) {
		for(Map<Integer, String> mapOfErrorCodes:errorMapsToLoad){
			addToMapOfErrorCodes(mapOfErrorCodes);
		}
	}

}
