package com.shimmerresearch.algorithms;

import java.util.LinkedHashMap;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/** This interface allows dynamic loading of algorithms based on Shimmer version information. It also serves as a good location to load default settings into an  
 * @author mnolan
 *
 */
public interface AlgorithmLoaderInterface {
	
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject svo, ExpansionBoardDetails eBD);

	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice);

}
