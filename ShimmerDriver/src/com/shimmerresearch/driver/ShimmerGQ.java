package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.sensor.AbstractSensor;

public class ShimmerGQ extends ShimmerDevice implements ShimmerHardwareSensors, ShimmerDataProcessing, Serializable {
	
	/**Each integer is a unique identifier
	 * 
	 */
	Map<Integer,AbstractSensor> mMapOfSensors = new HashMap<Integer,AbstractSensor>();
	
	/** This is derived from all the sensors
	 * 
	 */
	public HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>> mMapOfChannel = new HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>>(); 

	//This maps the channel ID to sensor
	//Map<Integer,AbstractSensor> mMapofSensorChannelToSensor = new HashMap<Integer,AbstractSensor>();
	
	//Action Setting Checker, if cant do pass it to next layer to be handled
	
	//priority of comm type to know whether it is dock and radio connected, if dock connected send uart priority
	
	//

	public ShimmerGQ(ShimmerVerObject sVO) {
		super.setShimmerVersionObject(sVO);
	}

	/**
	 * @param uniqueID unique id of the shimmer
	 * @param shimmerVersionObject the FW and HW details of the devices
	 */
	public ShimmerGQ(ShimmerGQInitSettings settings){
		mUniqueID = settings.mShimmerGQID;
	}
	
	/** 
	 * @param dockId
	 * @param slotNumber
	 * @param macId 
	 */
	public ShimmerGQ(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE connectionType){
		this(dockId, slotNumber, connectionType);
		setMacIdFromUart(macId);
	}

	public ShimmerGQ(String dockID, int slotNumber, COMMUNICATION_TYPE connectionType) {
		mDockID = dockID;
		parseDockType();
		
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
		
		addCommunicationRoute(connectionType);
	}

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
			HashMap<COMMUNICATION_TYPE, HashMap<String, ChannelDetails>> mapOfAllChannels, COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Integer, AbstractSensor> generateSensors(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, HashMap<Integer, ChannelDetails>> generateAllSensorChannels(HashMap<Integer, AbstractSensor> sensorMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

	/**Performs a deep copy of ShimmerGQ by Serializing
	 * @return ShimmerGQ the deep copy of the current ShimmerGQ
	 * @see java.io.Serializable
	 */
	@Override
	public ShimmerGQ deepClone() {
//		System.out.println("Cloning:" + mUniqueID);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ShimmerGQ) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void checkBattery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorConfigOptionDetails> getConfigOptionsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, SensorEnabledDetails> getSensorMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(byte[] infoMemContents) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object configValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultShimmerConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] refreshShimmerInfoMemBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> sensorMapConflictCheck(Integer key) {
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
