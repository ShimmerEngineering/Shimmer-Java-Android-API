package com.shimmerresearch.driver.shimmerGq;

import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_STATE;
import com.shimmerresearch.exceptions.ShimmerException.ExceptionLevel;

public class MsgSpan {

	
	public final static int MSG_ID_SPANMANAGER_INITIALIZED_SUCCESS = 0;
//	public final static int MSG_ID_SPAN_ENABLE_SPECTRUM_SCANNER_FAILED = 180;
	public final static int MSG_ID_SPAN_CHANNEL_UPDATE = 1;
	public final static int MSG_ID_SPAN_MODE_UPDATE = 2;
	public final static int MSG_ID_SPAN_SPECTRUM_ANALYSER_RESPONSE = 3;
	public final static int MSG_ID_SPAN_SPECTRUM_SCAN_SUCCESS = 4;
	
	public static final int MSG_ID_SPAN_JOB_STARTED_PER_SPAN = 5;
	public static final int MSG_ID_SPAN_JOB_FAIL_PER_SPAN = 6;
	public static final int MSG_ID_SPAN_JOB_SUCCESS_PER_SPAN = 7;
	public static final int MSG_ID_SPAN_OPERATION_PROGRESS = 8;
	public static final int MSG_ID_SPAN_OPERATION_FINISHED = 9;
	
	public static final int MSG_ID_SPAN_INITIALISED_STATE_CHANGE = 10;
	public static final int MSG_ID_SPAN_ENABLED_STATE_CHANGE = 11;
	public static final int MSG_ID_SHIMMER_UNPAIRED = 12;
	public static final int MSG_ID_SPAN_PLUGGED_UNPLUGGED = 13;
	public static final int MSG_ID_SPAN_STATE_CHANGE = 14;
	public static final int MSG_ID_SPAN_NO_RESPONSE = 15;

	public static final int MSG_ID_SHIMMER_RESPONSE_DATA_PACKET = 20;
	public static final int MSG_ID_SHIMMER_RESPONSE_STATUS_PACKET = 21;
	public static final int MSG_ID_SHIMMER_RECEPTION_RATE_CURRENT = 22;
	public static final int MSG_ID_SHIMMER_WARNING_NO_STATUS_RECEIVED = 23;

	public static final int MSG_ID_SHIMMER_DATA_OBJECT_CLUSTER = 24;
	public static final int MSG_ID_DATA_REQUEST_SENT = 25;
	public static final int MSG_ID_SHIMMER_RESPONSE_SYNC_PACKET = 26;
	public static final int MSG_ID_SHIMMER_DATA_OBJECT_CLUSTER_RESULT_AGGREGATOR = 27;
	
	public int mMsgID;
	public Object mObject;
	
	public String mSpanID = "";
 	public String mShimmerMacID = "";
 	public int mShimmerRadioID = -1;

	public int mErrorCode;
	public int mErrorCodeLowLevel;
	public int mErrorCodeLowBsl;
	
	public String mMessage = "";

	/** Contains the error message as copied from a caught exception. */
	public String mExceptionMsg;
	
	/** Contains the stracktrace as copied from a caught exception. */
	public StackTraceElement[] mExceptionStackTrace;
	
	/** Indicates whether the Exception is critical to the operation underway. */
	public ExceptionLevel mExceptionLevel = ExceptionLevel.HIGH;
//	public ExceptionLevelSpan mExceptionLevel = ExceptionLevelSpan.HIGH;
//	public static enum ExceptionLevelSpan {
//		LOW,
//		HIGH
//	}
	
	public DEVICE_STATE mSpanState;
//	public Object mCurrentJobDetails;
	public String mUniqueId = "";
	public String mShimmerUserAssignName = "";

	
	/** Used exclusively by the DockManager/ImportManager class for messages associated with no specific slot or dock
	 * @param msgId
	 */
	public MsgSpan(int msgId, Object myObject){
		mMsgID = msgId;
		this.mObject = myObject;
	}


	public MsgSpan(int msgId) {
		mMsgID = msgId;
	}


	/** Used by Span 
	 * @param msgID
	 * @param dockID
	 * @param slotIdentifier
	 */
	public MsgSpan(int msgID, String spanID, Object object) {
		mMsgID = msgID;
		mSpanID = spanID;
////		mSlotNumber = -1;
////		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
//		mUniqueID = mSpanID; 
				
		mObject = object;
//		mCurrentJobDetails = currentJobDetails;
	}

	
	
}
