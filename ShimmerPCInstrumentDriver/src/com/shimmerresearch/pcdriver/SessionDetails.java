package com.shimmerresearch.pcdriver;

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
	public double mRTCUserInput;
	public int mSuggestionIndex;
	public double mRTCDifference; // if RTCDiff is = 0, there is no RTC
	public String mConfigTime;
	
	
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
		this.mRTCDifference = rtcDifference;
		this.mConfigTime = configTime;
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
		this.mRTCDifference = rtcDifference;
		this.mConfigTime = configTime;
	}
	
	
	public Object clone(){
		SessionDetails details = new SessionDetails(this.mSessionName, this.mSessionDuration, this.mFileSize, this.mDockID, this.mUniqueSlotID, this.mMacAddress, this.mStartingRTC, this.mRTCDifference, this.mConfigTime);
		details.mTrialName = this.mTrialName;
		details.mFileSize = this.mFileSize;
		details.mRTCUserInput = this.mRTCUserInput;
		details.mSuggestionIndex = this.mSuggestionIndex;
		return details;
	}
	
}
