package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

public class AlgorithmDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 1L;
	
	public String mObjectClusterName = "";
	//TODO implement mGuiFriendlyName below across algorithms
	public String mGuiFriendlyName = "";
	public String mDatabaseChannelHandle = "";

	public String mGroupName = "";
	public String mUnits = "";
	public CHANNEL_TYPE mChannelType = CHANNEL_TYPE.CAL;
	public long mDerivedSensorBitmapID = 0; 

	public List<Integer> mListOfRequiredSensors = new ArrayList<Integer>();
	public List<String> mListOfAssociatedSensors = new ArrayList<String>();

	public SENSOR_CHECK_METHOD mSensorCheckMethod = SENSOR_CHECK_METHOD.ALL;
	public enum SENSOR_CHECK_METHOD{
		ALL,
		ANY
	}
	
	public AlgorithmDetails(List<Integer> listOfRequiredSensors, String units){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
	}

	public AlgorithmDetails(String objectClusterName, String guiFriendlyName, List<String> listOfAssociatedSensors, String groupName, long derivedSensorBitmapId, List<Integer> listOfRequiredSensors, String units){
		this(listOfRequiredSensors, units);
		mGuiFriendlyName = guiFriendlyName;
		mListOfAssociatedSensors = listOfAssociatedSensors;
		mObjectClusterName = objectClusterName;
		mDerivedSensorBitmapID = derivedSensorBitmapId;
		mGroupName = groupName;
	}

	//TODO this constructor is only used in the example -> remove?
	public AlgorithmDetails(List<Integer> listOfRequiredSensors, String units, SENSOR_CHECK_METHOD sensorCheckMethod){
		this(listOfRequiredSensors, units);
		mSensorCheckMethod = sensorCheckMethod;
	}

	public AlgorithmDetails(List<Integer> listOfRequiredSensors, String units, String AlgorithmName, String groupName){
		this(listOfRequiredSensors, units);
		mObjectClusterName = AlgorithmName;
		mGroupName = groupName;
	}
	
//	public AlgorithmDetails(List<Integer> listOfRequiredSensors,SENSOR_CHECK_METHOD sensorCheckMethod, int configByte,String AlgorithmName,String groupName, boolean enabled){
//		mListOfRequiredSensors = listOfRequiredSensors;
//		mConfigByte = configByte;
//		mAlgorithmName = AlgorithmName;
//		mGroupName = groupName;
//		mEnabled = enabled;
//	}

//	public boolean isEnabled() {
//		return mEnabled;
//	}
//
	
	//TODO maybe only array of 3? no Shimmer name?
	public String[] getSignalStringArray() {
		String[] signalStringArray = new String[4];
		signalStringArray[0] = "TEMP_SHIMMER_NAME";
		signalStringArray[1] = mObjectClusterName;
		signalStringArray[2] = mChannelType.toString();
		signalStringArray[3] = mUnits;
		return signalStringArray;
	}

	public ChannelDetails getChannelDetails() {
		ChannelDetails cD = new ChannelDetails(
				mObjectClusterName,
				mGuiFriendlyName,
				mDatabaseChannelHandle,
				mUnits,
				Arrays.asList(mChannelType));
		return cD;
	}


}

