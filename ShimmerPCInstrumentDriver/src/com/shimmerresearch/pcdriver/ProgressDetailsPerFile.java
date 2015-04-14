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
		
		private String tag;
 
		private OperationState(String tag) {
			this.tag = tag;
		}
	}
	
	public List<ErrorDetails> mListOfErrors = new ArrayList<ErrorDetails>(); 
	
	public OperationState mOperationState = OperationState.PENDING;
	
	public String mLog = "";
	public double mProgressPercentageComplete = 0;
	
	
	public ProgressDetailsPerFile() {
		
	}
	
	/** Used to calculated the percentage progress based on the pre-set mOperationEndValue and the passed in operationProgress. 
	 * @param operationProgress
	 */
	public void updateProgress(double operationProgress) {
		mProgressPercentageComplete = operationProgress;
	}
	
	
	public void addErrorMessage(MsgDock msgDock) {
		ErrorDetails newError = new ErrorDetails();
		newError.msgID = msgDock.mMsgID;
		newError.msgIDParsed = MsgDock.mMapOfMsgCodes.get(msgDock.mMsgID);
		newError.action = msgDock.mErrorCodeLowLevel;
		newError.actionParsed = MsgDock.mMapOfMsgCodes.get(msgDock.mErrorCodeLowLevel);
		newError.lowLevelErrorCode = msgDock.mErrorCodeLowLevel;
		newError.lowLevelErrorCodeParsed = MsgDock.mMapOfMsgCodes.get(msgDock.mErrorCodeLowLevel);
		newError.errMessage = msgDock.mExceptionMsg;
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

}
