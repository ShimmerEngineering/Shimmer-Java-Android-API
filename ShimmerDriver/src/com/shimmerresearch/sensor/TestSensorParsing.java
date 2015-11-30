package com.shimmerresearch.sensor;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerGQ_802154;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class TestSensorParsing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ShimmerVerObject svo = new ShimmerVerObject();
		ShimmerGQ_802154 shimmer = new ShimmerGQ_802154(svo);
		
		byte[] packetByteArray = {7,1,2,3,4,5,6};//1descip+3timestamp+2gsr+1hr;
		
		shimmer.buildMsg(packetByteArray, COMMUNICATION_TYPE.IEEE802154);
		
		
		
	}

}
