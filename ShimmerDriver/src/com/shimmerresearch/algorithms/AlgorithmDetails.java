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
	
	/** AKA ObjectClusterName */
	public String mAlgorithmName = "";
	private List<ChannelDetails> mListOfChannelDetails = new ArrayList<ChannelDetails>();
	//TODO implement mGuiFriendlyName below across algorithms
	public String mGuiFriendlyName = "";
	public String mDatabaseChannelHandle = "";

	public String mGroupName = "";
	public String mUnits = "";
	public CHANNEL_TYPE mChannelType = CHANNEL_TYPE.CAL;
	public List<Integer> mDerivedSensorBitmapID = new ArrayList<Integer>(); 

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

	public AlgorithmDetails(
			String objectClusterName, 
			String guiFriendlyName, 
			List<String> listOfAssociatedSensors, 
			String groupName, 
			List<Integer> listOfDerivedSensorBitmapId, 
			List<Integer> listOfRequiredSensors, 
			String units){
		this(listOfRequiredSensors, units);
		mGuiFriendlyName = guiFriendlyName;
		mListOfAssociatedSensors = listOfAssociatedSensors;
		mAlgorithmName = objectClusterName;
		mDerivedSensorBitmapID.addAll(listOfDerivedSensorBitmapId);
		mGroupName = groupName;
		mListOfChannelDetails.add(generateChannelDetails());
	}
	
	public AlgorithmDetails(
			String objectClusterName, 
			List<ChannelDetails> listOfAlgortihmChannels, 
			String guiFriendlyName, 
			List<String> listOfAssociatedSensors, 
			String groupName, 
			List<Integer> listOfDerivedSensorBitmapId, 
			List<Integer> listOfRequiredSensors, 
			String units){
		this(objectClusterName, guiFriendlyName, listOfAssociatedSensors, groupName, listOfDerivedSensorBitmapId, listOfRequiredSensors, units);
		mListOfChannelDetails.addAll(listOfAlgortihmChannels);
	}

	//TODO this constructor is only used in the example -> remove?
	public AlgorithmDetails(
			List<Integer> listOfRequiredSensors, 
			String units, 
			SENSOR_CHECK_METHOD sensorCheckMethod){
		this(listOfRequiredSensors, units);
		mSensorCheckMethod = sensorCheckMethod;
	}

	public AlgorithmDetails(
			List<Integer> listOfRequiredSensors, 
			String units, 
			String AlgorithmName, 
			String groupName){
		this(listOfRequiredSensors, units);
		mAlgorithmName = AlgorithmName;
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
		signalStringArray[1] = mAlgorithmName;
		signalStringArray[2] = mChannelType.toString();
		signalStringArray[3] = mUnits;
		return signalStringArray;
	}
	
	private ChannelDetails generateChannelDetails(){
		
		ChannelDetails cD = new ChannelDetails(
				mAlgorithmName,
				mGuiFriendlyName,
				mDatabaseChannelHandle,
				mUnits,
				Arrays.asList(mChannelType));
		
		return cD;
	}

	public List<ChannelDetails> getChannelDetails() {
		return mListOfChannelDetails;
	}


}

