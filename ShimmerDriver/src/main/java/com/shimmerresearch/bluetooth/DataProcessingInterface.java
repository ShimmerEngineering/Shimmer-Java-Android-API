package com.shimmerresearch.bluetooth;

import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

/**
 * Provides an interface directly to the method BuildMSG. This can be used
 * to implement algorithm/filters/etc. Two methods are provided, processdata
 * to implement your methods, and InitializeProcessData which is called
 * everytime you startstreaming, in the event you need to reinitialize your
 * method/algorithm everytime a Shimmer starts streaming
 *
 */
public interface DataProcessingInterface {

	/**
	 * Initialise your method/algorithm here, this callback is called when
	 * startstreaming is called
	 */
	
	/**
	 * Initialise Process Data here. This is called whenever the
	 * startStreaming command is called and can be used to initialise
	 * algorithms
	 * 
	 */
	public void initializeProcessData(int samplingRate);
	
	/**
	 * Process data here, algorithms can access the object cluster built by
	 * the buildMsg method here
	 * 
	 * @param ojc
	 *            the objectCluster built by the buildMsg method
	 * @return the processed objectCluster
	 */
	public ObjectCluster processData(ObjectCluster ojc);

	
	
	
	//TODO temporarily locating updateMapOfAlgorithmModules() in DataProcessing
	public void updateMapOfAlgorithmModules();

	public void updateMapOfAlgorithmModules(ShimmerDevice shimmerDevice);

}