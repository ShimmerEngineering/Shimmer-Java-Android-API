package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class AlgorithmDetailsNew implements Serializable {
	
	public List<Integer> mListOfRequiredSensors = new ArrayList<Integer>();
	public String mUnits = "";
	public SENSOR_CHECK_METHOD mSensorCheckMethod = SENSOR_CHECK_METHOD.ALL;
	public String mAlgorithmName = "";
	public String mGroupName = "";
	public boolean mEnabled =false;
	public int mConfigByte  = 0;

	
	public enum SENSOR_CHECK_METHOD{
		ALL,
		ANY
	}
//(List<Integer>, String, AlgorithmDetailsNew.SENSOR_CHECK_METHOD) 
	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
	}
	
	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units, SENSOR_CHECK_METHOD sensorCheckMethod){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
		mSensorCheckMethod = sensorCheckMethod;

	}

	
	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units, String AlgorithmName,String groupName, boolean enabled){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
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

	

}

