package com.shimmerresearch.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import com.shimmerresearch.driverUtilities.UtilShimmer;

/**
 * @author Mark Nolan
 *
 */
public class DeviceException extends ExecutionException {

	/**
	 * 
	 */
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

	public static HashMap<Integer,String> mMapOfErrorCodes = new HashMap<Integer,String>();
	
	public DeviceException() {
	}

	public DeviceException(String message) {
		super(message);
	}
	
	
	public DeviceException(String uniqueId, String comPort, int errorType, int lowLevelErrorCode) {
		mUniqueID = uniqueId;
		mComPort = comPort;
		mErrorCode = errorType;
		mErrorCodeLowLevel = lowLevelErrorCode;
	}


	public String getShimmerDeviceExceptionErrString() {
		return getShimmerDeviceExceptionErrString(mMapOfErrorCodes);
	}

	public String getShimmerDeviceExceptionErrString(Map<Integer, String> mapOfErrorCodes) {
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

		errorString += ("CAUGHT SHIMMER DEVICE EXCEPTION\n");
		errorString += ("\t" + "UniqueID: " + id
				+ "\n\t" + "Action: " + "(" + mErrorCode + ") " + errorCode 
				+ "\n\t" + "LowLevelError: " + "(" + mErrorCodeLowLevel + ") " + lowLevelErrorCode
				+ "\n\t" + exceptionInfo
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
		mMapOfErrorCodes.putAll(mapOfErrorCodes);
	}

}
