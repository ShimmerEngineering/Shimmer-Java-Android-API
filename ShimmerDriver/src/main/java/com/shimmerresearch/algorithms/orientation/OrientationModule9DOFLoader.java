package com.shimmerresearch.algorithms.orientation;

import java.util.LinkedHashMap;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class OrientationModule9DOFLoader implements AlgorithmLoaderInterface {

	@Override
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject svo, ExpansionBoardDetails eBD) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		
		if(svo.isShimmerGen3() || svo.isShimmerGen4()){
			mapOfSupportedAlgorithms.putAll(OrientationModule9DOF.mAlgorithmMapRef);
		}
		
		return mapOfSupportedAlgorithms;
	}

	@Override
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupported9DOFCh = getMapOfSupportedAlgorithms(shimmerDevice.getShimmerVerObject(), shimmerDevice.getExpansionBoardDetails());
		for (AlgorithmDetails algorithmDetails:mapOfSupported9DOFCh.values()) {
			OrientationModule9DOF orientationModule9DOF = new OrientationModule9DOF(algorithmDetails, shimmerDevice.getSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH));
			
			//TODO load any default settings here
			
			shimmerDevice.addAlgorithmModule(algorithmDetails.mAlgorithmName, orientationModule9DOF);
		}
	}

}
