package com.shimmerresearch.driver;

import java.util.HashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.sensor.AbstractSensor;

public interface ShimmerDataProcessing {

	/** /**This takes the raw data and processes it, note that the channelIdentifier is either the BTStream inquiry response of the SDLog Header, or etc. Communication type specifies whether its BT or SD etc.
	 * @param rawData
	 * @param channelIdentifier
	 * @param mapOfChannelToSensor
	 * @param commType
	 * @return
	 */
	public ObjectCluster processData(byte[] rawData, int[] channelIdentifier,HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>> mapOfAllChannels, COMMUNICATION_TYPE commType);
	

}
