package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class ShimmerLogDetails implements Serializable{

	public String mAbsolutePath;
	public String mFileName;
	public String mTrialName;
	public String mConfigTime;
	public String mFullTrialName;
	public long mRTCDifference;
	public String mSessionName;
/*<<<<<<< HEAD
	public int mSessionNumber;
=======*/
	public int mDbSessionNumber = -1;
//>>>>>>> refs/remotes/origin/MN
	public long mFileSize;
	public long mConfigTimeStamp;
	public long mInitialTimeStamp;
	public long mDuration;
	public int mSlotID;
	public String mMacAddress;
	public String mUniqueId;
	public String mDockID;
	public String mAbsolutePathWhereFileWasCopied;
	public int mFirmwareIdentifier;
	public int mFirmwareVersionMajor;
	public int mFirmwareVersionMinor;
	public int mFirmwareVersionInternal;
	public String mNewSessionName;
	public int mNewSessionId;
	public double mStartingRTC; // this is = (mInitialTimeStamp/32768*1000) + (mRTCDifference/32768*1000);
	public int mNumOfShimmers;
	public double mRTCUserInput;
	public int mMasterShimmer;
	public double mSampleRate;
	public double mEnabledSensors;
	public double mDerivedSensors;
	public int mHardwareVersion;
	
	public boolean mFileTooSmall = false;

	//GQ related
	public LinkedHashMap<String, SensorParsingDetails> mMapOfSensorsToParse = new LinkedHashMap<String, SensorParsingDetails>();
	public String mHeaderFileAbsoluteFilePath = "";

	public ShimmerLogDetails(){
		
	};
	
	public ShimmerLogDetails(String mAbolutePath, String mFileName,
			String mTrialName, String mConfigTime,
			String mSessionName, long mFileSize, int mSlotID,
			String mMacAddress, String mDockID, String mUniqueId) {
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
		this.mUniqueId = mUniqueId;
	}
}
