package com.shimmerresearch.algorithms;

import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_RESULT_TYPE;


public class AlgorithmResultObject {

	public ALGORITHM_RESULT_TYPE mAlgorithmResultType;
	public Object mResult;
	public String mTrialName;
	
	public AlgorithmResultObject(ALGORITHM_RESULT_TYPE type, Object result){
		mAlgorithmResultType = type;
		mResult = result;
	}
	
	public AlgorithmResultObject(ALGORITHM_RESULT_TYPE type, Object result, String trialName){
		mAlgorithmResultType = type;
		mResult = result;
		mTrialName = trialName;
	}
}
