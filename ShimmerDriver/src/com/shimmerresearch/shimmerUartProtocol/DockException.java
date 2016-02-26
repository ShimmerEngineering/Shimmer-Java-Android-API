package com.shimmerresearch.shimmerUartProtocol;

import java.util.concurrent.ExecutionException;

import com.shimmerresearch.driver.MsgDock.ExceptionLevel;

/**
 * @author Mark Nolan
 *
 */
public class DockException extends ExecutionException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7922798090312830525L;
	public int mSlotNumber;
	public int mErrorCode;
	public int mErrorCodeLowLevel; 
	public String mComPort;
	public String mClassName;
	public String mDockID;
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

	

	/** Currently used by DeviceInfo - plan to change
	 * @param message
	 */
	public DockException(String message){
		super(message);
	}
	
	/** Used by BasicDock and SmartDock classes
	 * @param dockID
	 * @param slotNumber
	 * @param errorType
	 * @param lowLevelErrorCode
	 */
	public DockException(String dockID, int slotNumber, int errorType, int lowLevelErrorCode){
		mDockID = dockID;
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
		mErrorCode = errorType;
		mErrorCodeLowLevel = lowLevelErrorCode;
	}
	
	/** Used by BasicDock and SmartDock classes when Exception Level is needed
	 * @param dockID
	 * @param slotNumber
	 * @param errorType
	 * @param lowLevelErrorCode
	 */
	public DockException(String dockID, int slotNumber, int errorType, int lowLevelErrorCode, ExceptionLevel exceptionLevel){
		mDockID = dockID;
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
		mErrorCode = errorType;
		mErrorCodeLowLevel = lowLevelErrorCode;
		mExceptionLevel = exceptionLevel;
	}
	
	/**Used by MspBsl
	 * @param comPort
	 * @param errorCode
	 * @param errorCodeLowLevel
	 * @param uniqueID
	 */
	public DockException(String comPort, int errorCode, int errorCodeLowLevel, String uniqueID){
		mUniqueID = uniqueID;
		String[] subString = uniqueID.split("\\.");
		mDockID  = subString[0]+"."+subString[1];
		if(subString.length>=3) {
			mSlotNumber = Integer.parseInt(subString[2]);
		}
		else {
			mSlotNumber = -1;
		}
		mComPort = comPort;
		mErrorCode = errorCode;
		mErrorCodeLowLevel = errorCodeLowLevel;
	}

	/** Used by SmartDockUart and ShimmerUart. If coming from ShimmerUart, need 
	 * to set mSlotNumber and mUniqueID in next layer up where it is called from.
	 * @param dockID
	 * @param comPort
	 * @param errorType
	 * @param lowLevelErrorCode
	 */
	public DockException(String dockID, String comPort, int errorType, int lowLevelErrorCode){
		mDockID = dockID;
		mComPort = comPort;
		mErrorCode = errorType;
		mErrorCodeLowLevel = lowLevelErrorCode;
		mSlotNumber = -1;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
	}


	public void updateDockException(String uniqueID) {
		mUniqueID = uniqueID;
		String[] subString = uniqueID.split("\\.");
		mDockID  = subString[0]+"."+subString[1];
		mSlotNumber = Integer.parseInt(subString[2]);
	}
	
	public void updateDockException(String dockID, int slotNumber) {
		mDockID = dockID;
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
	}
	
	public void updateDockException(String exceptionMsg, StackTraceElement[] exceptionStacktrace) {
		mExceptionMsg = exceptionMsg;
		mExceptionStackTrace = exceptionStacktrace;
	}

	
}
