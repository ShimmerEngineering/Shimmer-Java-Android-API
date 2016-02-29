package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SensorEnabledDetails implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1545530433767674139L;
	
	public boolean mIsEnabled = false;
	public long mDerivedSensorBitmapID = 0;
	public SensorDetails mSensorDetails;
	public List<String> mListOfChannels = new ArrayList<String>();
	
	public SensorEnabledDetails(boolean isEnabled, long derivedSensorBitmapID, SensorDetails sensorDetails){
		mIsEnabled = isEnabled;
		mDerivedSensorBitmapID = derivedSensorBitmapID;
		mSensorDetails = sensorDetails;
		
		for(String channelName:sensorDetails.mListOfChannelsRef){
			mListOfChannels.add(channelName);
		}
	}

	public boolean isDerivedChannel() {
		if(mDerivedSensorBitmapID>0) {
			return true;
		}
		return false;
	}
	
	
}