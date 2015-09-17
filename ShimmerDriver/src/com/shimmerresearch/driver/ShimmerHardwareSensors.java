package com.shimmerresearch.driver;

import java.util.HashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.sensor.AbstractSensor;

public interface ShimmerHardwareSensors {

	
	void deleteSensor(int uniqueID);
	
	void addSensor(int uniqueID, AbstractSensor abstractSensor);
	
	AbstractSensor getSensor(int uniqueID);
	
	/** Uses the shimmer version object to generate and initialize the sensors
	 * @param svo
	 */
	HashMap<Integer,AbstractSensor> generateSensors(ShimmerVerObject svo);
		 
	
	
	/** Uses the sensor map to compile all the list of channels 
	 * @param svo
	 */
	HashMap<COMMUNICATION_TYPE,HashMap<Integer,ChannelDetails>> generateAllSensorChannels(HashMap<Integer,AbstractSensor> sensorMap);
	
}
