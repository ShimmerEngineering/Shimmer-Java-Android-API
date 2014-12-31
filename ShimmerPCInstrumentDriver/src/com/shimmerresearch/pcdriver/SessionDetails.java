package com.shimmerresearch.pcdriver;

public class SessionDetails {
	
	public String mTrialName;
	public String mSessionName;
	public int mSessionDuration;
	public long mFileSize;
	public String mDockID;
	public int mUniqueSlotID;
	public String mMacAddress;
	public String mNewSessionName;
	
	
	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress) {
		super();
		this.mSessionName = mSessionName;
		this.mSessionDuration = mSessionDuration;
		this.mFileSize = mFileSize;
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
	}


	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress, String mNewSessionName) {
		super();
		this.mSessionName = mSessionName;
		this.mSessionDuration = mSessionDuration;
		this.mFileSize = mFileSize;
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mNewSessionName = mNewSessionName;
	}
	
	
	
	
}
