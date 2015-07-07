package com.shimmerresearch.bluetooth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.ShimmerObject;

public class ProgressReportPerDevice implements Serializable {
	
	public int mCommandCompleted;
	public int mNumberofRemainingCMDsInBuffer;
	public String mBluetoothAddress;
	
//	public ProgressReportPerDevice(int command, int numberofcmdsleft, String address){
//		mCommandCompleted = command;
//		mNumberofRemainingCMDsInBuffer = numberofcmdsleft;
//		mBluetoothAddress = address;
//	}

	public ProgressReportPerDevice(ShimmerObject shimmerObject) {
		if(shimmerObject instanceof ShimmerBluetooth){
			ShimmerBluetooth shimmerBluetooth = (ShimmerBluetooth) shimmerObject;
			setShimmerBluetooth(shimmerBluetooth);
		}
	}
	
	
	public static enum OperationState {
		PENDING,
		INPROGRESS,
		SUCCESS,
		FAIL_WITH_WARNING,
		FAIL_OUTRIGHT,
		CANCELLED
	}
	
    public static final String[] mListOfOperationStates = new String[]{
    	"Pending",
    	"In Progress",
    	"Success",
    	"Warning",
    	"Fail",
    	"Cancelled"
    };
    
	public ShimmerBluetoothDetailsMini mShimmerBluetoothDetailsMini = new ShimmerBluetoothDetailsMini();
//	public List<ErrorDetails> mListOfErrors = new ArrayList<ErrorDetails>(); 
	
	public OperationState mOperationState = OperationState.PENDING;
	
//	public String mLog = "";
	public int mProgressCounter = 0;
	public int mProgressPercentageComplete = 0;
	public int mProgressEndValue = 0;
	public float mProgressSpeed = 0;
//	public String mOperationTimeRemaining = 0;

	
//	public DockProgressDetailsPerDevice(ShimmerDocked shimmerBluetooth) {
//		setshimmerBluetooth(shimmerBluetooth);
//	}
//	
//	public DockProgressDetailsPerDevice(String dockID) {
//		// TODO Auto-generated constructor stub
//		
//		mSmartDockMini.mDockID = dockID;
////		mSmartDockMini.mDrivePath = drivePath;
////		mSmartDockMini.mListOfFailMsg = smartDock.mListOfFailMsg;
//		
//	}

//	/** Used to calculated the percentage progress based on the pre-set mOperationEndValue and the passed in operationProgress. 
//	 * @param operationProgress
//	 */
//	public void updateProgress(int operationProgress) {
//		if(mProgressEndValue!=0) {
//			mProgressPercentageComplete = (int)(((double)operationProgress / (double)mProgressEndValue) * 100);
//		}
//	}
	
	public void updateProgress(ProgressReportPerCmd pRPC) {
		mProgressCounter = mProgressEndValue - pRPC.mNumberofRemainingCMDsInBuffer;

		if(mProgressEndValue!=0) {
			mProgressPercentageComplete = (int)(((double)mProgressCounter / (double)mProgressEndValue) * 100);
		}
	}
	
	public void setShimmerBluetooth(ShimmerBluetooth shimmerBluetooth) {
		mShimmerBluetoothDetailsMini.mUniqueID = shimmerBluetooth.mUniqueID;
		mShimmerBluetoothDetailsMini.mShimmerMacID = shimmerBluetooth.getBluetoothAddress();
		mShimmerBluetoothDetailsMini.mShimmerMacIDParsed = shimmerBluetooth.getMacIdFromBtParsed();
		mShimmerBluetoothDetailsMini.mFirmwareVersionParsed  = shimmerBluetooth.mFirmwareVersionParsed;
//		mShimmerBluetoothDetailsMini.mListOfFailMsg = shimmerBluetooth.mListOfFailMsg;
		mShimmerBluetoothDetailsMini.mShimmerUserAssignedName = shimmerBluetooth.getShimmerUserAssignedName();
//		mShimmerBluetoothDetailsMini.mShimmerLastReadRealTimeClockValue = shimmerBluetooth.mShimmerLastReadRealTimeClockValue;
//		mShimmerBluetoothDetailsMini.mShimmerLastReadRtcValueParsed = shimmerBluetooth.mShimmerLastReadRtcValueParsed;
		
		//TODO: add entry for sdlogdetails
	}
	
	public void updateProgressDetails(ShimmerBluetooth shimmerBluetooth) {
		if(shimmerBluetooth!=null){
			mShimmerBluetoothDetailsMini.mUniqueID = shimmerBluetooth.mUniqueID;
			mShimmerBluetoothDetailsMini.mShimmerMacID = shimmerBluetooth.getBluetoothAddress();
			mShimmerBluetoothDetailsMini.mShimmerMacIDParsed = shimmerBluetooth.getMacIdFromBtParsed();
			mShimmerBluetoothDetailsMini.mFirmwareVersionParsed  = shimmerBluetooth.mFirmwareVersionParsed;
	//		mshimmerBluetoothMini.mListOfFailMsg = shimmerBluetooth.mListOfFailMsg;
			mShimmerBluetoothDetailsMini.mShimmerUserAssignedName = shimmerBluetooth.getShimmerUserAssignedName();
//			mShimmerBluetoothDetailsMini.mShimmerLastReadRealTimeClockValue = shimmerBluetooth.mShimmerLastReadRealTimeClockValue;
//			mShimmerBluetoothDetailsMini.mShimmerLastReadRtcValueParsed = shimmerBluetooth.mShimmerLastReadRtcValueParsed;
		}
		
		//TODO: add entry for sdlogdetails
	}
	
//	public void addErrorMessage(Map<Integer,String> mapOfErrorCodes, MsgDock msgDock) {
//		ErrorDetails newError = new ErrorDetails();
//		newError.msgID = msgDock.mMsgID;
//		newError.msgIDParsed = mapOfErrorCodes.get(msgDock.mMsgID);
//		newError.action = msgDock.mErrorCode;
//		newError.actionParsed = mapOfErrorCodes.get(msgDock.mErrorCode);
//		newError.lowLevelErrorCode = msgDock.mErrorCodeLowLevel;
//		newError.lowLevelErrorCodeParsed = mapOfErrorCodes.get(msgDock.mErrorCodeLowLevel);
////		newError.mExceptionStacktrace = msgDock.;
//		newError.errMessage = msgDock.mExceptionMsg;
////		newError.errMessage = msgDock.mMessage;
//		mListOfErrors.add(newError);
//	}
	
	
//	public class ErrorDetails implements Serializable {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 3172068431540685782L;
//		public int msgID;
//		public String msgIDParsed;
//		public int action;
//		public String actionParsed;
//		public int lowLevelErrorCode;
//		public String lowLevelErrorCodeParsed;
//		public StackTraceElement[] mExceptionStacktrace;
//		public String errMessage;
//	}

	public class ShimmerBluetoothDetailsMini implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4289859702565448002L;
		
		public int mSlotNumber = -1;
		public String mShimmerMacID = "";
		public String mShimmerMacIDParsed = "";
		
		public String mDockID = "";
		public String mUniqueID = "";

//		public String mActivityLog = "";
//		public int mFwImageWriteProgress = 0;
//		public int mFwImageTotalSize = 0;
//		public float mFwImageWriteSpeed = 0;
		
		public long mShimmerLastReadRealTimeClockValue = 0;
		public String mShimmerLastReadRtcValueParsed = "";
		
//		public List<MsgDock> mListOfFailMsg = new ArrayList<MsgDock>();
		public String mFirmwareVersionParsed = "";
		
		public String mShimmerUserAssignedName = "";
	}




	
}
