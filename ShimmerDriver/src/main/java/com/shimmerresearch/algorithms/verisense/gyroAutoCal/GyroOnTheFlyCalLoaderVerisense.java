package com.shimmerresearch.algorithms.verisense.gyroAutoCal;

import java.util.LinkedHashMap;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;

public class GyroOnTheFlyCalLoaderVerisense implements AlgorithmLoaderInterface{

	@Override
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = getMapOfSupportedAlgorithms(shimmerDevice);
		for (AlgorithmDetails algorithmDetails:mapOfSupportedAlgorithms.values()) {
			GyroOnTheFlyCalModuleVerisense algorithmModule = new GyroOnTheFlyCalModuleVerisense(shimmerDevice, algorithmDetails, shimmerDevice.getSamplingRateShimmer(commType));
			shimmerDevice.addAlgorithmModule(algorithmModule);
		}
	}

	@Override
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerDevice shimmerDevice) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		mapOfSupportedAlgorithms = AlgorithmDetails.loadAlgorithmsWhereSensorsAreAvailable(shimmerDevice, GyroOnTheFlyCalModuleVerisense.mAlgorithmMapRefVerisense);
		return mapOfSupportedAlgorithms;
	}

}
