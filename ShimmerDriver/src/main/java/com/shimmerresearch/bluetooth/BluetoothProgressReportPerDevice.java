package com.shimmerresearch.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.ShimmerDevice;

/** Hold progress details per device for Bluetooth activity.
 * @author mnolan
 *
 */
//TODO add proper comments
//TODO remove unnecessary code carried over from dock progress details
public class BluetoothProgressReportPerDevice implements Serializable {
	
	/** * */
	private static final long serialVersionUID = -7997745169511235203L;
	
	public int mCommandCompleted;
	public int mNumberofRemainingCMDsInBuffer;
	public String mBluetoothAddress;
	public String mComPort;
	
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
	public OperationState mOperationState = OperationState.PENDING;

	public BT_STATE mCurrentOperationBtState = BT_STATE.DISCONNECTED;
    
	public ShimmerBluetoothDetailsMini mShimmerBluetoothDetailsMini = new ShimmerBluetoothDetailsMini();
	
	public int mProgressCounter = 0;
	public int mProgressPercentageComplete = 0;
	public int mProgressEndValue = 100;
	public float mProgressSpeed = 0;

//	public List<ErrorDetails> mListOfErrors = new ArrayList<ErrorDetails>(); 
//	public String mLog = "";

	public BluetoothProgressReportPerDevice(String comPort, BT_STATE currentOperationBtState, int endValue) {
		mComPort = comPort;
		mCurrentOperationBtState = currentOperationBtState;
		mProgressEndValue = endValue;
	}

	public BluetoothProgressReportPerDevice(ShimmerDevice shimmerDevice, BT_STATE currentOperationBtState, int endValue) {
		this(shimmerDevice.getComPort(), currentOperationBtState, endValue);
		updateShimmerDeviceMini(shimmerDevice);
	}

	/**
	 * Used to calculated the percentage progress based on the pre-set
	 * mOperationEndValue and the passed in ProgressReportPerCmd.
	 * 
	 * @param pRPC the ProgressReportPerCmd
	 */
	public void updateProgress(BluetoothProgressReportPerCmd pRPC) {
		updateProgress(pRPC.mNumberofRemainingCMDsInBuffer, pRPC.mCommandCompleted);
	}

	public void updateProgress(int numberofRemainingCMDsInBuffer, int commandsCompleted) {
		mProgressCounter = mProgressEndValue - numberofRemainingCMDsInBuffer + 1;
		
		mCommandCompleted = commandsCompleted;

		if(mProgressCounter<0) mProgressCounter=0;
		if(mProgressCounter>mProgressEndValue) mProgressCounter=mProgressEndValue;
		
		if(mProgressEndValue!=0) {
			mProgressPercentageComplete = (int)(((double)mProgressCounter / (double)mProgressEndValue) * 100);
		}
	}

//	public void setShimmerBluetooth(ShimmerBluetooth shimmerBluetooth) {
//		mShimmerBluetoothDetailsMini.mUniqueID = shimmerBluetooth.getComPort();
//		mShimmerBluetoothDetailsMini.mShimmerMacID = shimmerBluetooth.getBluetoothAddress();
//		mShimmerBluetoothDetailsMini.mShimmerMacIDParsed = shimmerBluetooth.getMacIdFromBtParsed();
//		
//		mShimmerBluetoothDetailsMini.mFirmwareVersionParsed  = shimmerBluetooth.getFirmwareVersionParsed();
//		mShimmerBluetoothDetailsMini.mShimmerUserAssignedName = shimmerBluetooth.getShimmerUserAssignedName();
//		
////		mShimmerBluetoothDetailsMini.mListOfFailMsg = shimmerBluetooth.mListOfFailMsg;
////		mShimmerBluetoothDetailsMini.mShimmerLastReadRealTimeClockValue = shimmerBluetooth.mShimmerLastReadRealTimeClockValue;
////		mShimmerBluetoothDetailsMini.mShimmerLastReadRtcValueParsed = shimmerBluetooth.mShimmerLastReadRtcValueParsed;
//		
//		//TODO: add entry for sdlogdetails
//	}
	
	public void updateShimmerDeviceMini(ShimmerDevice shimmerDevice) {
		if(shimmerDevice!=null){
			if(shimmerDevice instanceof ShimmerBluetooth){
				ShimmerBluetooth shimmerBluetooth = (ShimmerBluetooth)shimmerDevice;
				mShimmerBluetoothDetailsMini.mUniqueID = shimmerBluetooth.getComPort();
				mShimmerBluetoothDetailsMini.mShimmerMacID = shimmerBluetooth.getBluetoothAddress();
				mShimmerBluetoothDetailsMini.mShimmerMacIDParsed = shimmerBluetooth.getMacIdFromBtParsed();
			}
			else {
				mShimmerBluetoothDetailsMini.mUniqueID = shimmerDevice.getComPort();
				mShimmerBluetoothDetailsMini.mShimmerMacID = shimmerDevice.getMacId();
				mShimmerBluetoothDetailsMini.mShimmerMacIDParsed = shimmerDevice.getMacIdParsed();
			}
			
			mShimmerBluetoothDetailsMini.mFirmwareVersionParsed  = shimmerDevice.getFirmwareVersionParsed();
			mShimmerBluetoothDetailsMini.mShimmerUserAssignedName = shimmerDevice.getShimmerUserAssignedName();
			
	//		mshimmerBluetoothMini.mListOfFailMsg = shimmerBluetooth.mListOfFailMsg;
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
		/** * */
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

	public void finishOperation() {
		mProgressCounter = mProgressEndValue;
		mProgressPercentageComplete = 100;
	}


	/**Performs a deep copy of ProgressDetailsAll by Serializing
	 * @return ProgressDetailsAll the deep copy of the current ProgressDetailsAll
	 * @see java.io.Serializable
	 */
	public BluetoothProgressReportPerDevice deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (BluetoothProgressReportPerDevice) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
