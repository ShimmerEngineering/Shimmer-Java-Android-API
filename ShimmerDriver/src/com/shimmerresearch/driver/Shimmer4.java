package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensor.ActionSetting;
import com.shimmerresearch.sensor.SensorBMP180;
import com.shimmerresearch.sensor.SensorEXG;
import com.shimmerresearch.sensor.SensorGSR;
import com.shimmerresearch.sensor.SensorMPU9X50;
import com.shimmerresearch.sensor.SensorSystemTimeStamp;
import com.shimmerresearch.sensor.AbstractSensor.SENSORS;

public class Shimmer4 extends ShimmerDevice {
	
	/** * */
	private static final long serialVersionUID = 6916261534384275804L;
	
	protected ShimmerRadioProtocol mShimmerRadio;

	public Shimmer4() {
		// TODO Auto-generated constructor stub
	}
	
	public Shimmer4(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE communicationType) {
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(communicationType);
    	setSamplingRateShimmer(communicationType, 128);
    	setMacIdFromUart(macId);
	}

	public void setSetting(long sensorID, String componentName, Object valueToSet, COMMUNICATION_TYPE commType){
		ActionSetting actionSetting = mMapOfSensors.get(sensorID).setSettings(componentName, valueToSet, commType);
		if (actionSetting.mCommType == COMMUNICATION_TYPE.BLUETOOTH){
			//mShimmerRadio.actionSettingResolver(actionSetting);
		}
	}
	
	public void initialize(){
		if (mShimmerRadio!=null){ // the radio instance should be declared on a higher level and not in this class
			mShimmerRadio.setRadioListener(new RadioListener(){

			@Override
			public void connected() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void disconnected() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void eventNewPacket(byte[] packetByteArray) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void eventResponseReceived(byte[] responseBytes) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void eventAckReceived(byte[] instructionSent) {
				// TODO Auto-generated method stub
				
			}});
		}
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
	public void checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sensorAndConfigMapsCreate() {
//		if(UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
//				SVO_RELEASE_REV_0_1.mHardwareVersion, SVO_RELEASE_REV_0_1.mFirmwareIdentifier, SVO_RELEASE_REV_0_1.mFirmwareVersionMajor, SVO_RELEASE_REV_0_1.mFirmwareVersionMinor, SVO_RELEASE_REV_0_1.mFirmwareVersionInternal)){
//			
//		}
//		else {
			mMapOfSensors.put(SENSORS.SYSTEM_TIMESTAMP.sensorIndex(),new SensorSystemTimeStamp(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.MPU9X50.sensorIndex(),new SensorMPU9X50(mShimmerVerObject));
			
//			mMapOfSensors.put(SENSORS.EXG.sensorIndex(),new SensorEXG(mShimmerVerObject));
//			mMapOfSensors.put(SENSORS.GSR.sensorIndex(),new SensorGSR(mShimmerVerObject)); //for testing
//			mMapOfSensors.put(SENSORS.BMP180.sensorIndex(),new SensorBMP180(mShimmerVerObject));
//		}
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
	public Shimmer4 deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Shimmer4) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		//NOT USED IN THIS CLASS
	}

}
