package com.shimmerresearch.pcdriver;

import com.shimmerresearch.driver.Util;

public class SessionDetails {
	
	public String mTrialName;
	public String mSessionNameFull;
	public String mSessionNameParsed = "";
	public String mSessionFolderNumber = "";
	public int mSessionDuration;
	public String mSessionDurationParsed;
	public long mFileSize;
	public String mFileSizeParsed;
	public String mDockID;
	public int mUniqueSlotID;
	public String mMacAddress;
	public String mMacAddressParsed;
	public int mNewSessionId = -1;
	public double mStartingRTC;
	public String mStartingRTCParsed;
	public double mRTCUserInput;
	public int mSuggestionIndex;
	public double mRTCDifference; // if RTCDiff is = 0, there is no RTC
	private String mConfigTime;
	private String mConfigTimeParsed;
	
	//For the GUI
	public boolean mSelectedForDelete = false;
	public int mTrialListIndex = 0;
	public int mSessionListIndex = 0;

	public int mSuggestedSessionMatchIndex = -1;
	
	public SessionDetails(String mSessionName, int mSessionDuration,
			long mFileSize, String mDockID, int mUniqueSlotID,
			String mMacAddress, double startingRTC, double rtcDifference, String configTime) {
		super();
		this.mSessionNameFull = mSessionName;
		
		if(mSessionName.contains("-")){
			mSessionNameParsed = mSessionName.substring(0, mSessionName.lastIndexOf("-")); 
			if(mSessionName.lastIndexOf("-")<mSessionName.length()+1){
				mSessionFolderNumber = mSessionName.substring(mSessionName.lastIndexOf("-")+1);
			}
		}
		
		this.mSessionDuration = mSessionDuration;
		this.mSessionDurationParsed = Util.convertDuration(mSessionDuration);
		this.mFileSize = mFileSize;
		this.mFileSizeParsed = Util.convertBytes(mFileSize);
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mMacAddressParsed = parseMacID(mMacAddress);
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
		this.mSessionNameFull = mSessionName;
		
		if(mSessionName.contains("-")){
			mSessionNameParsed = mSessionName.substring(0, mSessionName.lastIndexOf("-")); 
			mSessionFolderNumber = mSessionName.substring(mSessionName.lastIndexOf("-"));
		}

		this.mSessionDuration = mSessionDuration;
		this.mSessionDurationParsed = Util.convertDuration(mSessionDuration);
		this.mFileSize = mFileSize;
		this.mFileSizeParsed = Util.convertBytes(mFileSize);
		this.mDockID = mDockID;
		this.mUniqueSlotID = mUniqueSlotID;
		this.mMacAddress = mMacAddress;
		this.mMacAddressParsed = parseMacID(mMacAddress);
		this.mNewSessionId = mNewSessionId;
		this.mStartingRTC = startingRTC;
		this.mStartingRTCParsed = convertTime(startingRTC);
		this.mRTCDifference = rtcDifference;
		this.mConfigTime = configTime;
		this.mConfigTimeParsed = convertTime(configTime);
	}
	
	
	//Only used in DialogSessionReview
	public Object clone(){
		SessionDetails details = new SessionDetails(this.mSessionNameFull, this.mSessionDuration, this.mFileSize, this.mDockID, this.mUniqueSlotID, this.mMacAddress, this.mStartingRTC, this.mRTCDifference, this.mConfigTime);
		details.mTrialName = this.mTrialName;
		details.mFileSize = this.mFileSize;
		details.mRTCUserInput = this.mRTCUserInput;
		details.mSuggestionIndex = this.mSuggestionIndex;
		return details;
	}
	
	
	public String convertTime(double time) {
		return Util.convertMilliSecondsToDateString((long) time);
	}
	
	public String parseMacID(String macID){
		String parsedMacID = macID;
		if(parsedMacID.contains(":")){
			parsedMacID = parsedMacID.replace(":", "");
			if(parsedMacID.length()>4){
				parsedMacID = parsedMacID.substring(parsedMacID.length()-4, parsedMacID.length()).toUpperCase();
			}
		}
		return parsedMacID;
	}
	
	public String convertTime(String time) {
		if(Util.isNumeric(time)) {
			long configTimeConverted = Long.parseLong(time)*1000;
			return Util.convertMilliSecondsToDateString(configTimeConverted);
		}
		return "";
	}
	
	/**
	 * @return the mConfigTime
	 */
	public String getConfigTime() {
		return mConfigTime;
	}

	/**
	 * @return the mConfigTimeParsed
	 */
	public String getConfigTimeParsed() {
		return mConfigTimeParsed;
	}

	
}
