package com.shimmerresearch.comms.wiredProtocol;

import com.google.common.base.Strings;
import com.shimmerresearch.exceptions.ShimmerException;

/**
 * @author Mark Nolan
 *
 */
public class DockException extends ShimmerException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7922798090312830525L;
	public int mSlotNumber;
	public String mDockID = "";

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
		if(!Strings.isNullOrEmpty(uniqueID)){
			mUniqueID = uniqueID;
			String[] subString = uniqueID.split("\\.");
			mDockID  = subString[0]+"."+subString[1];
			if(subString.length>=3) {
				mSlotNumber = Integer.parseInt(subString[2]);
			}
			else {
				mSlotNumber = -1;
			}
		}
		mComPort = comPort;
		mErrorCode = errorCode;
		mErrorCodeLowLevel = errorCodeLowLevel;
	}

	/**Used by MspBsl
	 * @param comPort
	 * @param errorCode
	 * @param errorCodeLowLevel
	 * @param uniqueID
	 */
	public DockException(String comPort, int errorCode, int errorCodeLowLevel, String uniqueID, String exceptionMsg, StackTraceElement[] exceptionStackTrace) {
		this(comPort, errorCode, errorCodeLowLevel, uniqueID);
		mExceptionMsg = exceptionMsg;
		mExceptionStackTrace = exceptionStackTrace;
	}
	
	/** Used by SmartDockUart and ShimmerUart. If coming from ShimmerUart, need 
	 * to set mSlotNumber and mUniqueID in next layer up where it is called from.
	 * @param dockID
	 * @param comPort
	 * @param errorType
	 * @param lowLevelErrorCode
	 */
	public DockException(String dockID, String comPort, int errorType, int lowLevelErrorCode){
		super(dockID, comPort, errorType, lowLevelErrorCode);
		mDockID = dockID;
		mSlotNumber = -1;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
	}
	
	
	// TODO 2016-04-21 Mark currently working on
	public DockException(ShimmerException dE) {
		this(dE.mComPort, dE.mErrorCode, dE.mErrorCodeLowLevel, dE.mUniqueID, dE.mExceptionMsg, dE.mExceptionStackTrace);
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
		super.updateDeviceException(exceptionMsg, exceptionStacktrace);
	}

	
}
