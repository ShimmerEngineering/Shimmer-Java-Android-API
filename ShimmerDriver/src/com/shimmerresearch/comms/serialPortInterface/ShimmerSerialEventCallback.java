package com.shimmerresearch.comms.serialPortInterface;

/**
 * @author Mark Nolan
 *
 */
public interface ShimmerSerialEventCallback {

	public void serialPortRxEvent(int byteLength);
	
}
