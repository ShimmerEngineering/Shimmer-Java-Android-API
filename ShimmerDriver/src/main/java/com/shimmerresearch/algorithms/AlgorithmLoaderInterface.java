package com.shimmerresearch.algorithms;

import java.util.LinkedHashMap;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;

/** This interface allows dynamic loading of algorithms based on Shimmer version information. It also serves as a good location to load default settings into an  
 * @author Mark Nolan
 *
 */
public interface AlgorithmLoaderInterface {
	
	public void initialiseSupportedAlgorithms(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType);
	
	public LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerDevice shimmerDevice);

}
