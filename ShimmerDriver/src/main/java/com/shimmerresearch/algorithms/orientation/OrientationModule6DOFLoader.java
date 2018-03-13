package com.shimmerresearch.algorithms.orientation;

import java.util.LinkedHashMap;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class OrientationModule6DOFLoader implements AlgorithmLoaderInterface {

	@Override
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerDevice shimmerDevice) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		
		ShimmerVerObject svo = shimmerDevice.getShimmerVerObject();
		if(svo.isShimmerGen3() || svo.isShimmerGen4()){
			mapOfSupportedAlgorithms.putAll(OrientationModule6DOF.mAlgorithmMapRef);
		}
		
		return mapOfSupportedAlgorithms;
	}

	@Override
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupported6DOFCh = getMapOfSupportedAlgorithms(shimmerDevice);
		for (AlgorithmDetails algorithmDetails:mapOfSupported6DOFCh.values()) {
			OrientationModule6DOF orientationModule6DOF = new OrientationModule6DOF(algorithmDetails, shimmerDevice.getSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH));
			
			//TODO load any default settings here
			
			shimmerDevice.addAlgorithmModule(orientationModule6DOF);
		}
	}

}
