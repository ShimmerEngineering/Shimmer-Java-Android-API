package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.SensorBMP180;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorMPU9X50;
import com.shimmerresearch.sensors.SensorSystemTimeStamp;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

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
		ActionSetting actionSetting = mMapOfSensorClasses.get(sensorID).setSettings(componentName, valueToSet, commType);
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
	public void sensorAndConfigMapsCreate() {
		if(UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
				HW_ID.SHIMMER_4_SDK, FW_ID.LOGANDSTREAM, ANY_VERSION, ANY_VERSION, ANY_VERSION)){
//			mMapOfSensorClasses.put(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStamp(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.BMP180, new SensorBMP180(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.MPU9X50, new SensorMPU9X50(mShimmerVerObject));
			
//			mMapOfSensorClasses.put(SENSORS.EXG, new SensorEXG(mShimmerVerObject));
//			mMapOfSensorClasses.put(SENSORS.GSR, new SensorGSR(mShimmerVerObject)); //for testing
		}
		
		updateSensorAndParserMaps();
	}
	
	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		//TODO don't think this is relevent for Shimmer4
	}

	// TODO need to move common infomem related activity to ShimmerDevice. Not
	// have duplicates in ShimmerObject, ShimmerGQ and Shimmer4. Some items only
	// copied here for example/testing purposes
	@Override
	public void infoMemByteArrayParse(byte[] infoMemBytes) {
		String shimmerName = "";

		if(!InfoMemLayout.checkInfoMemValid(infoMemBytes)){
			// InfoMem not valid
			setDefaultShimmerConfiguration();
//			mShimmerUsingConfigFromInfoMem = false;

//			mShimmerInfoMemBytes = infoMemByteArrayGenerate();
//			mShimmerInfoMemBytes = new byte[infoMemContents.length];
			mInfoMemBytes = infoMemBytes;
		}
		else {
			//TODO create for Shimmer4 or use Shimmer3?
			InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3) mInfoMemLayout;
	
			// Sensors
			mEnabledSensors = ((long)infoMemBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;
	
			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.infoMemByteArrayParse(this, mInfoMemBytes);
			}
	
			
			mDerivedSensors = (long)0;
			// Check if compatible and not equal to 0xFF
			if((infoMemLayoutCast.idxDerivedSensors0>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors0]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)
					&& (infoMemLayoutCast.idxDerivedSensors1>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors1]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)) { 
				
				mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors0] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors0;
				mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors1] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors1;
				
				// Check if compatible and not equal to 0xFF
				if((infoMemLayoutCast.idxDerivedSensors2>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)){ 
					mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors2] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors2;
				}
			}
			
			sensorAndConfigMapsCreate();
			sensorMapUpdateFromEnabledSensorsVars();
		}

		
	}

	// TODO need to move common infomem related activity to ShimmerDevice. Not
	// have duplicates in ShimmerObject, ShimmerGQ and Shimmer4. Some items only
	// copied here for example/testing purposes
	@Override
	public byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {
		//TODO refer to same method in ShimmerGQ/ShimmerObject

		//TODO create for Shimmer4 or use Shimmer3?
		InfoMemLayoutShimmer3 infoMemLayout = new InfoMemLayoutShimmer3(
				getFirmwareIdentifier(), 
				getFirmwareVersionMajor(), 
				getFirmwareVersionMinor(), 
				getFirmwareVersionInternal());
		
//		byte[] infoMemBackup = mInfoMemBytes.clone();
		mInfoMemBytes = infoMemLayout.createEmptyInfoMemByteArray();
		
		// Shimmer Name
		for (int i = 0; i < infoMemLayout.lengthShimmerName; i++) {
			if (i < mShimmerUserAssignedName.length()) {
				mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
			}
			else {
				mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) 0xFF;
			}
		}
		
		// Configuration from each Sensor settings
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			abstractSensor.infoMemByteArrayGenerate(this, mInfoMemBytes);
		}

		
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void createInfoMemLayout() {
		//TODO replace with Shimmer4?
		mInfoMemLayout = new InfoMemLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
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
