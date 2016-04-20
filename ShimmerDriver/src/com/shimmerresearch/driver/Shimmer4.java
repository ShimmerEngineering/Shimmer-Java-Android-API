package com.shimmerresearch.driver;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class Shimmer4 extends ShimmerDevice {
	
	/** * */
	private static final long serialVersionUID = 6916261534384275804L;
	
	
	
	
	


	@Override
	protected void checkBattery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ShimmerDevice deepClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultShimmerConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Integer> sensorMapConflictCheck(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, SensorConfigOptionDetails> getConfigOptionsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(byte[] infoMemContents) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] refreshShimmerInfoMemBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createInfoMemLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesSensorKeyExist(int sensorKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChannelEnabled(int sensorKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getChannelLabel(int sensorKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ShimmerVerObject> getListOfCompatibleVersionInfo(int sensorKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Integer> getSensorMapKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

}
