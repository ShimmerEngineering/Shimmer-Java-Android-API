package com.shimmerresearch.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.sensor.AbstractSensor;

public class ShimmerGQ extends ShimmerObject implements ShimmerHardwareSensors {
	

	List<AbstractSensor> mListofSensors = new ArrayList<AbstractSensor>();
	
	//This maps the channel ID to sensor
	Map<Integer,AbstractSensor> mMapofSensorChannelToSensor = new HashMap<Integer,AbstractSensor>();
	
	
	
	
	@Override
	public void updateSensors() {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void processRawDataUsingSensors() {
		// TODO Auto-generated method stub
		//first parse the byte array data
	}

	@Override
	public void deleteSensor() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void manageSensor() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void checkBattery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateRawDataFormat(int[] packetChannelLayout, int fwID) {
		// TODO Auto-generated method stub
		//iterates through list and updates mMapofSensorChannelToSensor
		
		
	}


}
