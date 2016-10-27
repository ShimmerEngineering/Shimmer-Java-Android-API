package com.shimmerresearch.driverUtilities;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;

import com.shimmerresearch.driver.ShimmerDevice;

public class ShimmerLogDetails implements Serializable{

	private static final long serialVersionUID = 1413674780783463461L;
	
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
	
	//GQ related
	public LinkedHashMap<String, SensorParsingDetails> mMapOfSensorsToParse = new LinkedHashMap<String, SensorParsingDetails>();
	public String mHeaderFileAbsoluteFilePath = "";

	public ShimmerLogDetails(){
	};
	
	public ShimmerLogDetails(String mAbolutePath, String mFileName,
			String mTrialName, String mConfigTime,
			String mSessionName, long mFileSize, int mSlotID,
			String mMacAddress, String mDockID, String mUniqueId) {
		this();
		
		this.mAbsolutePath = mAbolutePath;
		this.mFileName = mFileName;
		this.mFileSize = mFileSize;

		setTrialSessionConfigTime(mTrialName, mSessionName, mConfigTime);

		this.mMacAddress = mMacAddress;

		this.mDockID = mDockID;
		this.mUniqueId = mUniqueId;
		this.mSlotID = mSlotID;
	}

	public void updateFromShimmerDevice(ShimmerDevice shimmerDevice) {
		mConfigTimeStamp = shimmerDevice.getConfigTime();
		mMacAddress = shimmerDevice.getMacId();
		mFirmwareIdentifier = shimmerDevice.getFirmwareIdentifier();
		mFirmwareVersionMajor = shimmerDevice.getFirmwareVersionMajor();
		mFirmwareVersionMinor = shimmerDevice.getFirmwareVersionMinor();
		mFirmwareVersionInternal = shimmerDevice.getFirmwareVersionInternal();
		mSampleRate = shimmerDevice.getSamplingRateShimmer();
		mEnabledSensors = shimmerDevice.getEnabledSensors();
		mDerivedSensors = shimmerDevice.getDerivedSensors();
		mHardwareVersion = shimmerDevice.getHardwareVersion();
	}

	public void updateFromFile(File l) {
		mAbsolutePath = l.getAbsolutePath();
		mFileName = l.getName();
		mFileSize  = l.length();
	}

	public void setTrialSessionConfigTime(String trialName, String sessionName, String configTime) {
		mTrialName = trialName;
		mSessionName = sessionName;
		mConfigTime = configTime;
		mFullTrialName = mTrialName+"_"+mConfigTime;
	}


}
