package com.shimmerresearch.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.sensor.AbstractSensor;

public class ShimmerGQ extends ShimmerDevice implements ShimmerHardwareSensors, ShimmerDataProcessing {
	

	/**Each integer is a unique identifier
	 * 
	 */
	Map<Integer,AbstractSensor> mMapofSensors = new HashMap<Integer,AbstractSensor>();
	
	//This maps the channel ID to sensor
	//Map<Integer,AbstractSensor> mMapofSensorChannelToSensor = new HashMap<Integer,AbstractSensor>();
	
	//Action Setting Checker, if cant do pass it to next layer to be handled
	
	//priority of comm type to know whether it is dock and radio connected, if dock connected send uart priority
	
	//
	
	
	/** This is derived from all the sensors
	 * 
	 */
	public HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>> mMapOfChannel = new HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>>(); 
	
	
	


	@Override
	public void deleteSensor(int uniqueID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSensor(int uniqueID, AbstractSensor abstractSensor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractSensor getSensor(int uniqueID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectCluster processData(
			byte[] rawData,
			int[] channelIdentifier,
			HashMap<COMMUNICATION_TYPE, HashMap<String, ChannelDetails>> mapOfAllChannels,
			COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Integer, AbstractSensor> generateSensors(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, HashMap<Integer, ChannelDetails>> generateAllSensorChannels(
			HashMap<Integer, AbstractSensor> sensorMap) {
		// TODO Auto-generated method stub
		return null;
	}





	
	
	/*
	public Object generateInfoMem(enum fwtype){
		byte[] array = new byte[x];
		for(sensor){
			array = sensor.gimmeInfoMem(array) 
		}
		//determine if you can act on it if not
		//return action setting
 	}
	*/

}
