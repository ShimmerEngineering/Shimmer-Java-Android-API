package com.shimmerresearch.algorithms;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

public class AlgorithmDetails {

	public AlgorithmDetailsRef mAlgorithmDetailsRef = null;
	public boolean mEnabled = false;

	public AlgorithmDetails(AlgorithmDetailsRef algorithmDetailsRef){
		mAlgorithmDetailsRef = algorithmDetailsRef;
	}
	
	public boolean isEnabled() {
		return mEnabled;
	}

	//TODO maybe only array of 3? no Shimmer name?
	public String[] getSignalStringArray() {
		String[] signalStringArray = new String[4];
		signalStringArray[0] = "TEMP_SHIMMER_NAME";
		signalStringArray[1] = mAlgorithmDetailsRef.mAlgorithmName;
		signalStringArray[2] = CHANNEL_TYPE.CAL.toString(); //temp hard coded here
		signalStringArray[3] = mAlgorithmDetailsRef.mUnits;
		return signalStringArray;
	}
	
}
