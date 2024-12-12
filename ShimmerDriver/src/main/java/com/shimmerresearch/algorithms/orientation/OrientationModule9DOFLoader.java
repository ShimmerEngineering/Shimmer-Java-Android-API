package com.shimmerresearch.algorithms.orientation;

import java.util.LinkedHashMap;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;

public class OrientationModule9DOFLoader implements AlgorithmLoaderInterface {

	@Override
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerDevice shimmerDevice) {
		ShimmerVerObject svo = shimmerDevice.getShimmerVerObject();
		ExpansionBoardDetails eBD = shimmerDevice.getExpansionBoardDetails();
		
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();

		if(svo.getFirmwareIdentifier()==FW_ID.STROKARE){
			mapOfSupportedAlgorithms.put(OrientationModule9DOF.algo9DoFOrientation_WR_Acc.mAlgorithmName, OrientationModule9DOF.algo9DoFOrientation_WR_Acc);
		}
		else {
			if(svo.isShimmerGen3R()){
				mapOfSupportedAlgorithms.putAll(OrientationModule9DOF.mAlgorithmMapRef);
			}
			else {
				mapOfSupportedAlgorithms = AlgorithmDetails.loadAlgorithmsWhereSensorsAreAvailable(shimmerDevice, OrientationModule9DOF.mAlgorithmMapRef);
			}
		}
		
		return mapOfSupportedAlgorithms;
	}

	@Override
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE comType) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupported9DOFCh = getMapOfSupportedAlgorithms(shimmerDevice);
		for (AlgorithmDetails algorithmDetails:mapOfSupported9DOFCh.values()) {
			OrientationModule9DOF orientationModule9DOF = new OrientationModule9DOF(shimmerDevice, algorithmDetails, shimmerDevice.getSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH));
			
			//TODO load any default settings here
			
			shimmerDevice.addAlgorithmModule(orientationModule9DOF);
		}
	}

}
