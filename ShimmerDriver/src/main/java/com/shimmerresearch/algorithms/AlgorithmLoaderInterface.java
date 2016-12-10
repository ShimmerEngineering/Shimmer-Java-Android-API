package com.shimmerresearch.algorithms;

import java.util.LinkedHashMap;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/** This interface allows dynamic loading of algorithms based on Shimmer version information. It also serves as a good location to load default settings into an  
 * @author Mark Nolan
 *
 */
public interface AlgorithmLoaderInterface {
	
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice);
	
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject svo, ExpansionBoardDetails eBD);

}
