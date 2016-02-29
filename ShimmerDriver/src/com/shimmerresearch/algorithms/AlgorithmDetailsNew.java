package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class AlgorithmDetailsNew implements Serializable {
	
	public List<Integer> mListOfRequiredSensors = new ArrayList<Integer>();
	public String mUnits = "";
	public SENSOR_CHECK_METHOD mSensorCheckMethod = SENSOR_CHECK_METHOD.ALL;
	
	public enum SENSOR_CHECK_METHOD{
		ALL,
		ANY
	}

	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
	}

	public AlgorithmDetailsNew(List<Integer> listOfRequiredSensors, String units, SENSOR_CHECK_METHOD sensorCheckMethod){
		mListOfRequiredSensors = listOfRequiredSensors;
		mUnits = units;
		mSensorCheckMethod = sensorCheckMethod;
	}

}
