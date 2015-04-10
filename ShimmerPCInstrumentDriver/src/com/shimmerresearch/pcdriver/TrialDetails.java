package com.shimmerresearch.pcdriver;

import java.util.List;

import com.shimmerresearch.driver.ShimmerObject;


public class TrialDetails {
	public String mTrialName;
	public String mConfigTime;
	public String mConfigTimeParsed;
	public int mNShimmers;
	public List<SessionDetails> mListofSessionDetails;
	
	//Testing
	public int mTrialListIndex = 0;
	
	public TrialDetails() {
		super();
		// TODO Auto-generated constructor stub
	}


	public TrialDetails(String mTrialName,
			List<SessionDetails> mListofSessionsNames) {
		super();
		this.mTrialName = mTrialName;
		this.mListofSessionDetails = mListofSessionsNames;
	}
	
	public void setConfigTime(String configTime){
		this.mConfigTime = configTime;
		mConfigTimeParsed = convertTime(configTime);
	}
	
	public String convertTime(String time) {
		if(isNumeric(time)) {
			long configTimeConverted = Long.parseLong(time);
			//Util.convertSecondsToDateString not in API currently so using this
			return ShimmerObject.convertMilliSecondsToDateString(configTimeConverted);
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
