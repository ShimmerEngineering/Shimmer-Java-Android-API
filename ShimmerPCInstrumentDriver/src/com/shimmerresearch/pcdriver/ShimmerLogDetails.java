package com.shimmerresearch.pcdriver;

public class ShimmerLogDetails {

	public String mAbsolutePath;
	public String mFileName;
	public String mTrialName;
	public String mConfigTime;
	public String mFullTrialName;
	public String mSessionName;
	public long mFileSize;
	public long mConfigTimeStamp;
	public long mInitialTimeStamp;
	public long mDuration;
	public int mSlotID;
	public String mMacAddress;
	public String mDockID;
	public String mAbsolutePathWhereFileWasCopied;
	public String mNewSessionName;
	public ShimmerLogDetails(){};
	public ShimmerLogDetails(String mAbolutePath, String mFileName,
			String mTrialName, String mConfigTime,
			String mSessionName, long mFileSize, int mSlotID,
			String mMacAddress, String mDockID) {
		super();
		this.mAbsolutePath = mAbolutePath;
		this.mFileName = mFileName;
		this.mTrialName = mTrialName;
		this.mConfigTime = mConfigTime;
//		this.mFullTrialName = mFullTrialName;
		this.mSessionName = mSessionName;
		this.mFileSize = mFileSize;
		this.mSlotID = mSlotID;
		this.mMacAddress = mMacAddress;
		this.mDockID = mDockID;
	}
}
