package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.exceptions.ShimmerException;

/**
 * @author Mark Nolan
 *
 */
public class ShimmerShell extends ShimmerDevice {

	/** * */
	private static final long serialVersionUID = 3505947289367382624L;

	public ShimmerShell() {
		setFirstDockRead();
	}

	/** Initialisation via dock
	 * @param dockId
	 * @param slotNumber
	 */
	public ShimmerShell(String dockId, int slotNumber){
		this();
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

	@Override
	public void connect() {
		try {
			super.connect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public boolean setSensorEnabledState(int sensorId, boolean state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Integer> sensorMapConflictCheck(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String groupName, String componentName, Object configValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configBytesParse(byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer, COMMUNICATION_TYPE commType) {
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
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createConfigBytesLayout() {
		// TODO Auto-generated method stub
		
	}

	public void setRadio(CommsProtocolRadio shimmerRadioProtocol) {
		//TODO
	}

	@Override
	protected void dataHandler(ObjectCluster ojc) {
		// TODO Auto-generated method stub
		
	}

}
