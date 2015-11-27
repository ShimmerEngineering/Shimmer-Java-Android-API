package com.shimmerresearch.driver;

import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;

public class ShimmerShell extends ShimmerDevice {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3505947289367382624L;

	public ShimmerShell(String dockId, int slotNumber, COMMUNICATION_TYPE connectionType){

		mDockID = dockId;
		parseDockType();
		
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
		
		addCommunicationRoute(connectionType);
		
		//TODO: remove??
//		initialise(HW_ID.SHIMMER_3);
//		setDefaultShimmerConfiguration();
	
	}
	
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
	public List<Integer> sensorMapConflictCheck(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, SensorConfigOptionDetails> getConfigOptionsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object configValue) {
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
	public byte[] refreshShimmerInfoMemBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		// TODO Auto-generated method stub
		
	}

}
