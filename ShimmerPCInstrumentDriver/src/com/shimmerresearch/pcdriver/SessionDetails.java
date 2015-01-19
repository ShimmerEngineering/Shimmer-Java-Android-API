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
	public int mSuggestionIndex;
	
	
	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress, double startingRTC) {
		super();
		this.mSessionName = mSessionName;
		this.mSessionDuration = mSessionDuration;
		this.mFileSize = mFileSize;
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mStartingRTC = startingRTC;
	}


	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress, int mNewSessionId, double startingRTC) {
		super();
		this.mSessionName = mSessionName;
		this.mSessionDuration = mSessionDuration;
		this.mFileSize = mFileSize;
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mNewSessionId = mNewSessionId;
		this.mStartingRTC = startingRTC;
	}
	
	
	
	
}
