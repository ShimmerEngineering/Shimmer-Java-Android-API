package com.shimmerresearch.pcdriver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.pcdriver.MsgDock;

public class ProgressDetailsPerFile implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7835896317704401240L;

	public enum OperationState {
		PENDING("Pending"),
		INPROGRESS("In Progress"),
		SUCCESS("Success"),
		FAIL("Fail");
		
//		private int value;
		private String tag;
 
		private OperationState(String tag) {
			this.tag = tag;
//			this.value = value;
		}
	}
	
//	public SlotDetailsMini mSlotDetailsMini = new SlotDetailsMini();
	public List<ErrorDetails> mListOfErrors = new ArrayList<ErrorDetails>(); 
	
	public OperationState mOperationState = OperationState.PENDING;
	
	public String mLog = "";
//	public int mProgressCounter = 0;
	public int mProgressPercentageComplete = 0;
//	public int mProgressEndValue = 0;
//	public float mProgressSpeed = 0;
	
	
	public ProgressDetailsPerFile() {
//		updateSlotDetails(slotDetails);
	}
	
	/** Used to calculated the percentage progress based on the pre-set mOperationEndValue and the passed in operationProgress. 
	 * @param operationProgress
	 */
	public void updateProgress(int operationProgress) {
//		if(mProgressEndValue!=0) {
//			mProgressPercentageComplete = (int)(((double)operationProgress / (double)mProgressEndValue) * 100);
//		}
		mProgressPercentageComplete = operationProgress;
	}
	
	
	
//	public void updateSlotDetails(SlotDetails slotDetails) {
//		mSlotDetailsMini.mUniqueID = slotDetails.mUniqueID;
//		mSlotDetailsMini.mDockID = slotDetails.mDockID;
//		mSlotDetailsMini.mSlotNumber = slotDetails.mSlotNumber;
//		mSlotDetailsMini.mShimmerMacID = slotDetails.mShimmerMacID;
//		mSlotDetailsMini.mShimmerMacIDParsed = slotDetails.mShimmerMacIDParsed;
//		mSlotDetailsMini.mFirmwareVersionParsed  = slotDetails.mFirmwareVersionParsed;
//		mSlotDetailsMini.mListOfFailMsg = slotDetails.mListOfFailMsg;
//		mSlotDetailsMini.mShimmerUserAssignedName = slotDetails.getShimmerUserAssignedName();
//		mSlotDetailsMini.mShimmerLastReadRealTimeClockValue = slotDetails.mShimmerLastReadRealTimeClockValue;
//		mSlotDetailsMini.mShimmerLastReadRtcValueParsed = slotDetails.mShimmerLastReadRtcValueParsed;
//		
//		//TODO: add entry for sdlogdetails
//	}
	
	
	public void addErrorMessage(MsgDock msgDock) {
		ErrorDetails newError = new ErrorDetails();
		newError.msgID = msgDock.mMsgID;
		newError.msgIDParsed = MsgDock.mMapOfMsgCodes.get(msgDock.mMsgID);
		newError.action = msgDock.mErrorCodeLowLevel;
		newError.actionParsed = MsgDock.mMapOfMsgCodes.get(msgDock.mErrorCodeLowLevel);
		newError.lowLevelErrorCode = msgDock.mErrorCodeLowLevel;
		newError.lowLevelErrorCodeParsed = MsgDock.mMapOfMsgCodes.get(msgDock.mErrorCodeLowLevel);
//		newError.mExceptionStacktrace = msgDock.;
		newError.errMessage = msgDock.mExceptionMsg;
//		newError.errMessage = msgDock.mMessage;
		mListOfErrors.add(newError);
	}
	
	public class ErrorDetails implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3172068431540685782L;
		public int msgID;
		public String msgIDParsed;
		public int action;
		public String actionParsed;
		public int lowLevelErrorCode;
		public String lowLevelErrorCodeParsed;
		public StackTraceElement[] mExceptionStacktrace;
		public String errMessage;
	}

//	public class SlotDetailsMini implements Serializable {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 4289859702565448002L;
//		
//		public int mSlotNumber = -1;
//		public String mShimmerMacID = "";
//		public String mShimmerMacIDParsed = "";
//		
//		public String mDockID;
//		public String mUniqueID = "";
//
////		public String mActivityLog = "";
////		public int mFwImageWriteProgress = 0;
////		public int mFwImageTotalSize = 0;
////		public float mFwImageWriteSpeed = 0;
//		
//		public long mShimmerLastReadRealTimeClockValue = 0;
//		public String mShimmerLastReadRtcValueParsed = "";
//		
//		public List<MsgDock> mListOfFailMsg = new ArrayList<MsgDock>();
//		public String mFirmwareVersionParsed = "";
//		
//		public String mShimmerUserAssignedName = "";
//	}
}
