package com.shimmerresearch.pcdriver;

import java.util.List;


public class TrialDetails {
	public String mTrialName;
	public String mConfigTime;
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
	
	
}
