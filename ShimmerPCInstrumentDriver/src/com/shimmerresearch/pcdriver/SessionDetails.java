package com.shimmerresearch.pcdriver;

import com.shimmerresearch.driver.ShimmerObject;

public class SessionDetails {
	
	public String mTrialName;
	public String mSessionName;
	public int mSessionDuration;
	public long mFileSize;
	public String mDockID;
	public int mUniqueSlotID;
	public String mMacAddress;
	public int mNewSessionId;
	public double mStartingRTC;
	public String mStartingRTCParsed;
	public double mRTCUserInput;
	public int mSuggestionIndex;
	public double mRTCDifference; // if RTCDiff is = 0, there is no RTC
	public String mConfigTime;
	public String mConfigTimeParsed;
	
	public int mAssignedSessionId = -1;
	public boolean mSelectedForDelete = false;
	//Testing
	public int mTrialListIndex = 0;
	public int mSessionListIndex = 0;

	
	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress, double startingRTC, double rtcDifference, String configTime) {
		super();
		this.mSessionName = mSessionName;
		this.mSessionDuration = mSessionDuration;
		this.mFileSize = mFileSize;
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mStartingRTC = startingRTC;
		this.mStartingRTCParsed = convertTime(startingRTC);

		this.mRTCDifference = rtcDifference;
		this.mConfigTime = configTime;
		this.mConfigTimeParsed = convertTime(configTime);
	}

	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress, int mNewSessionId, double startingRTC, double rtcDifference, String configTime) {
		super();
		this.mSessionName = mSessionName;
		this.mSessionDuration = mSessionDuration;
		this.mFileSize = mFileSize;
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mNewSessionId = mNewSessionId;
		this.mStartingRTC = startingRTC;
		this.mStartingRTCParsed = convertTime(startingRTC);
		this.mRTCDifference = rtcDifference;
		this.mConfigTime = configTime;
		this.mConfigTimeParsed = convertTime(configTime);
	}
	
	
	public Object clone(){
		SessionDetails details = new SessionDetails(this.mSessionName, this.mSessionDuration, this.mFileSize, this.mDockID, this.mUniqueSlotID, this.mMacAddress, this.mStartingRTC, this.mRTCDifference, this.mConfigTime);
		details.mTrialName = this.mTrialName;
		details.mFileSize = this.mFileSize;
		details.mRTCUserInput = this.mRTCUserInput;
		details.mSuggestionIndex = this.mSuggestionIndex;
		return details;
	}
	
	
	public String convertTime(double time) {
		return ShimmerObject.convertSecondsToDateString((long) time/1000);
	}
	
	public String convertTime(String time) {
		if(isNumeric(time)) {
			long configTimeConverted = Long.parseLong(time);
			//Util.convertSecondsToDateString not in API currently so using this
			return ShimmerObject.convertSecondsToDateString(configTimeConverted/1000);
		}
		return "";
	}
	
	public static boolean isNumeric(String str){
		if(str==null) {
			return false;
		}
		if(str.isEmpty()) {
			return false;
		}
		
	    for (char c : str.toCharArray()){
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
	}
	
}
