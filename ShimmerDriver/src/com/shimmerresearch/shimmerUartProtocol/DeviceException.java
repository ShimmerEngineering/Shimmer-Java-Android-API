package com.shimmerresearch.shimmerUartProtocol;

import java.util.concurrent.ExecutionException;


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

	
	public DeviceException() {
	}

	public DeviceException(String message) {
		super(message);
	}



}
