package com.shimmerresearch.algorithms.orientation;

import java.util.LinkedHashMap;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class OrientationModuleLoader6DOF implements AlgorithmLoaderInterface {

	@Override
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject svo, ExpansionBoardDetails eBD) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		
		//TODO Filter here depending on Shimmer version
		mapOfSupportedAlgorithms.putAll(OrientationModule6DOF.mAlgorithmMapRef);
		
		return mapOfSupportedAlgorithms;
	}

	@Override
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupported6DOFCh = getMapOfSupportedAlgorithms(shimmerDevice.getShimmerVerObject(), shimmerDevice.getExpansionBoardDetails());
		for (AlgorithmDetails algorithmDetails:mapOfSupported6DOFCh.values()) {
			OrientationModule6DOF orientationModule6DOF = new OrientationModule6DOF(algorithmDetails, shimmerDevice.getSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH));
			
			//TODO load any default settings here
			
			shimmerDevice.addAlgorithmModule(algorithmDetails.mAlgorithmName, orientationModule6DOF);
		}
	}

}
