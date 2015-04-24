package com.shimmerresearch.pcdriver;

import java.util.List;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Util;


public class TrialDetails {
	public String mTrialName;
	private String mConfigTime;
	private String mConfigTimeParsed;
	public int mNShimmers;
	public List<SessionDetails> mListofSessionDetails;
	
	//For the GUI
	public int mTrialListIndex = -1;

	public int mSuggestionsCount = -1;
	public int mSuggestionsIndex = -1;

	public TrialDetails() {
		super();
		// TODO Auto-generated constructor stub
	}


	public TrialDetails(String mTrialName,List<SessionDetails> mListofSessionsNames) {
		super();
		this.mTrialName = mTrialName;
		this.mListofSessionDetails = mListofSessionsNames;
	}
	
	public void setConfigTime(String configTime){
		this.mConfigTime = configTime;
		mConfigTimeParsed = convertTime(configTime);
	}
	
	public void incrementSuggestionIndex(){
		mSuggestionsIndex+=1;
		if(mSuggestionsIndex>mSuggestionsCount){
			mSuggestionsIndex=1; // None selected
		}
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
