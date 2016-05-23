package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

public class AlgorithmDetailsNew implements Serializable {
	
	public List<Integer> mListOfRequiredSensors = new ArrayList<Integer>();
	public String mUnits = "";
	public String mAlgorithmName = "";
	public String mGroupName = "";
	public int mConfigByte = 0;
	public long mDerivedSensorBitmap = 0; 

	public SENSOR_CHECK_METHOD mSensorCheckMethod = SENSOR_CHECK_METHOD.ALL;

	public boolean mEnabled = false;

	public enum SENSOR_CHECK_METHOD{
		ALL,
		ANY
	}
//(List<Integer>, String, AlgorithmDetailsNew.SENSOR_CHECK_METHOD) 
	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
	}

	public AlgorithmDetailsNew(String algorithmName, long sensorBitmap, List<Integer> listOfRequiredSensors, String units){
		this(listOfRequiredSensors, units);
		mAlgorithmName = algorithmName;
		mDerivedSensorBitmap = sensorBitmap;
	}

	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units, SENSOR_CHECK_METHOD sensorCheckMethod){
		this(listOfRequiredSensors, units);
		mSensorCheckMethod = sensorCheckMethod;
	}

	
	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units, String AlgorithmName,String groupName, boolean enabled){
		this(listOfRequiredSensors, units);
		//mSensorCheckMethod = sensorCheckMethod;
		mAlgorithmName = AlgorithmName;
		mGroupName = groupName;
		mEnabled = enabled;
	}
	
	//or PPG 
	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors,SENSOR_CHECK_METHOD sensorCheckMethod, int configByte,String AlgorithmName,String groupName, boolean enabled){
		mListOfRequiredSensors = listOfRequiredSensors;
		mConfigByte = configByte;
		mAlgorithmName = AlgorithmName;
		mGroupName = groupName;
		mEnabled = enabled;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public String[] getSignalStringArray() {
		String[] signalStringArray = new String[4];
		signalStringArray[0] = "TEMP_SHIMMER_NAME";
		signalStringArray[1] = mAlgorithmName;
		signalStringArray[2] = CHANNEL_TYPE.CAL.toString(); //temp hard coded here
		signalStringArray[3] = mUnits;
		return signalStringArray;
	}

	

}

