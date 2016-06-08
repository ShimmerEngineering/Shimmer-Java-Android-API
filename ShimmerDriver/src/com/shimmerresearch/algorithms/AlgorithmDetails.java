package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

//TODO merge with ChannelDetails?
public class AlgorithmDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 1L;
	
	/** AKA ObjectClusterName */
	public String mAlgorithmName = "";
	private List<ChannelDetails> mListOfChannelDetails = new ArrayList<ChannelDetails>();
	//TODO implement mGuiFriendlyName below across algorithms
	public String mGuiFriendlyName = "";
	public String mDatabaseChannelHandle = "";

	@Deprecated //this mixes the driver level and the gui level -> use SensorGroupDetails instead
	public String mGroupName = "";
	public String mUnits = "";
	public CHANNEL_TYPE mChannelType = CHANNEL_TYPE.CAL;
//	public List<Integer> mDerivedSensorBitmapID = new ArrayList<Integer>(); 
	public long mDerivedSensorBitmapID = 0; 

	public List<Integer> mListOfRequiredSensors = new ArrayList<Integer>();
	public List<String> mListOfAssociatedSensors = new ArrayList<String>();
	@Deprecated //TODO replace with compatible versions list
	public List<Integer> mListOfCompatableExpBoards = new ArrayList<Integer>();

	public SENSOR_CHECK_METHOD mSensorCheckMethod = SENSOR_CHECK_METHOD.ALL;
	public enum SENSOR_CHECK_METHOD{
		ALL,
		ANY
	}
	
	/**
	 * @param listOfRequiredSensors
	 * @param units
	 */
	public AlgorithmDetails(List<Integer> listOfRequiredSensors, String units){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
	}

	//TODO this constructor is only used in the example -> remove?
	/**
	 * @param listOfRequiredSensors
	 * @param units
	 * @param sensorCheckMethod
	 */
	public AlgorithmDetails(
			List<Integer> listOfRequiredSensors, 
			String units, 
			SENSOR_CHECK_METHOD sensorCheckMethod){
		this(listOfRequiredSensors, units);
		mSensorCheckMethod = sensorCheckMethod;
	}
	
	/**
	 * @param objectClusterName
	 * @param guiFriendlyName
	 * @param listOfAssociatedSensors
	 * @param groupName
	 * @param listOfDerivedSensorBitmapId
	 * @param listOfRequiredSensors
	 * @param units
	 */
	public AlgorithmDetails(
			String objectClusterName, 
			String guiFriendlyName, 
			List<String> listOfAssociatedSensors, 
			String groupName, 
			long derivedSensorBitmapId, 
			List<Integer> listOfRequiredSensors, 
			String units){
		this(listOfRequiredSensors, units);
		mAlgorithmName = objectClusterName;
		mGuiFriendlyName = guiFriendlyName;
		mListOfAssociatedSensors = listOfAssociatedSensors;
		mDerivedSensorBitmapID = derivedSensorBitmapId;
		mGroupName = groupName;
		mListOfChannelDetails.add(generateChannelDetails());
	}
	
	/**
	 * @param objectClusterName
	 * @param listOfAlgortihmChannels
	 * @param guiFriendlyName
	 * @param listOfAssociatedSensors
	 * @param groupName
	 * @param listOfDerivedSensorBitmapId
	 * @param listOfRequiredSensors
	 * @param units
	 */
	public AlgorithmDetails(
			String objectClusterName, 
			String guiFriendlyName, 
			List<String> listOfAssociatedSensors, 
			String groupName, 
			long derivedSensorBitmapId, 
			List<Integer> listOfRequiredSensors, 
			String units,
			List<ChannelDetails> listOfAlgortihmChannels){
		this(objectClusterName, guiFriendlyName, listOfAssociatedSensors, groupName, derivedSensorBitmapId, listOfRequiredSensors, units);
		mListOfChannelDetails.addAll(listOfAlgortihmChannels);
	}
	
	public AlgorithmDetails(
			String objectClusterName, 
			String guiFriendlyName, 
			List<String> listOfAssociatedSensors, 
			String groupName, 
			long derivedSensorBitmapId, 
			List<Integer> listOfCompatibleExpBoards,
			List<Integer> listOfRequiredSensors, 
			String units){
		this(objectClusterName, guiFriendlyName, listOfAssociatedSensors, groupName, derivedSensorBitmapId, listOfRequiredSensors, units);
		mListOfCompatableExpBoards=listOfCompatibleExpBoards;
	}
	
	
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

