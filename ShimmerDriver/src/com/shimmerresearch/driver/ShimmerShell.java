package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * @author Mark Nolan
 *
 */
public class ShimmerShell extends ShimmerDevice {

	/** * */
	private static final long serialVersionUID = 3505947289367382624L;

	/** Initialisation via dock
	 * @param dockId
	 * @param slotNumber
	 */
	public ShimmerShell(String dockId, int slotNumber){
		setDockInfo(dockId, slotNumber);
	}

	/** Initialisation via dock
	 * @param dockId
	 * @param slotNumber
	 * @param connectionType
	 */
	public ShimmerShell(String dockId, int slotNumber, COMMUNICATION_TYPE connectionType){
		this(dockId, slotNumber);
		addCommunicationRoute(connectionType);
	}

	
	/** Initialisation via bluetooth manager
	 * @param comPort
	 * @param shimmerMacId
	 * @param myName
	 * @param timeSyncTrainingPeriod
	 */
	public ShimmerShell(String comPort, String shimmerMacId, String myName, int timeSyncTrainingPeriod) {
		//TODO
//		super(comPort, shimmerMacId, myName, true);
//		mTimeSyncTrainingPeriod = timeSyncTrainingPeriod;
	}

	
	
	@Override
	protected void checkBattery() {
		// TODO Auto-generated method stub
	}

	@Override
	public ShimmerShell deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ShimmerShell) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setDefaultShimmerConfiguration() {
		// TODO Auto-generated method stub
		
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

	@Override
	protected void interpretDataPacketFormat(Object object,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public Map<Integer, SensorEnabledDetails> getSensorEnabledMap() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createInfoMemLayout() {
		// TODO Auto-generated method stub
		
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
	public boolean doesSensorKeyExist(int sensorKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Integer> getSensorMapKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

}
