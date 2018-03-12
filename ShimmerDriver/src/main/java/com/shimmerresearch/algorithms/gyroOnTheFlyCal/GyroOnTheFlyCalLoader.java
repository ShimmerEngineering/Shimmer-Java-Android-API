package com.shimmerresearch.algorithms.gyroOnTheFlyCal;

import java.util.LinkedHashMap;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;

public class GyroOnTheFlyCalLoader implements AlgorithmLoaderInterface {

	@Override
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = getMapOfSupportedAlgorithms(shimmerDevice);
		for (AlgorithmDetails algorithmDetails:mapOfSupportedAlgorithms.values()) {
			GyroOnTheFlyCalModule algorithmModule = new GyroOnTheFlyCalModule(shimmerDevice, algorithmDetails, shimmerDevice.getSamplingRateShimmer(commType));
			shimmerDevice.addAlgorithmModule(algorithmModule);
		}
	}

	@Override
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerDevice shimmerDevice) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		mapOfSupportedAlgorithms = AlgorithmDetails.loadAlgorithmsWhereSensorsAreAvailable(shimmerDevice, GyroOnTheFlyCalModule.mAlgorithmMapRef);
		return mapOfSupportedAlgorithms;
	}

}
