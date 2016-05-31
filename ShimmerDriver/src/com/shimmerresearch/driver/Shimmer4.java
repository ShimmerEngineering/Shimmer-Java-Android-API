package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.serialPortInterface.SerialPortComm;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.SensorBMP180;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorKionixKXRB52042;
import com.shimmerresearch.sensors.SensorLSM303;
import com.shimmerresearch.sensors.SensorMPU9X50;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorPPG;
import com.shimmerresearch.sensors.ShimmerClock;

public class Shimmer4 extends ShimmerDevice {
	
	/** * */
	private static final long serialVersionUID = 6916261534384275804L;
	
	public ShimmerRadioProtocol mShimmerRadioHWLiteProtocol = null;

	byte[] mInfoMemBuffer;
	private int mCurrentInfoMemAddress = 0;
	private int mCurrentInfoMemLengthToRead = 0;
	private double mOffsetFirstTime;
	private boolean mFirstPacketParsed;
	public BluetoothProgressReportPerDevice progressReportPerDevice;
	private int mNumberOfInfoMemReadsRequired = 3;
	/**
	 * LogAndStream will try to recreate the SD config. file for each block of
	 * InfoMem that is written - need to give it time to do so.
	 */
	private static final int DELAY_BETWEEN_INFOMEM_WRITES = 100;
	/** Delay to allow LogAndStream to create SD config. file and reinitialise */
	private static final int DELAY_AFTER_INFOMEM_WRITE = 500;
	
	
	private int mNumOfInfoMemSetCmds;
	public Shimmer4() {
		// TODO Auto-generated constructor stub
	}
	
	public Shimmer4(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE communicationType) {
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(communicationType);
    	setSamplingRateShimmer(communicationType, 128);
    	setMacIdFromUart(macId);
	}
	
	@Override
	public void sensorAndConfigMapsCreate() {
//		if(UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
//				HW_ID.SHIMMER_4_SDK, FW_ID.LOGANDSTREAM, ANY_VERSION, ANY_VERSION, ANY_VERSION)){
		
//			mMapOfSensorClasses.put(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStamp(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.CLOCK, new ShimmerClock(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.KIONIXKXRB52042, new SensorKionixKXRB52042(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.LSM303, new SensorLSM303(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.BMP180, new SensorBMP180(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.MPU9X50, new SensorMPU9X50(mShimmerVerObject));
			
//		}
		
		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_EXG 
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED
//				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK
				){
			mMapOfSensorClasses.put(SENSORS.EXG, new SensorEXG(mShimmerVerObject));
		}

		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
//				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK
				){
			mMapOfSensorClasses.put(SENSORS.GSR, new SensorGSR(mShimmerVerObject));
		}

		//Commented out until PPG fully implemented
		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE
//				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK
				){
			if(isDerivedSensorsSupported()){
				mMapOfSensorClasses.put(SENSORS.PPG, new SensorPPG(mShimmerVerObject));
			}
		}

		generateSensorAndParserMaps();
	}
	
	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		//TODO don't think this is relevent for Shimmer4
	}

	@Override
	public void createInfoMemLayout() {
		//TODO replace with Shimmer4?
		mInfoMemLayout = new InfoMemLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
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
			mInfoMemBytes = infoMemBytes;

			//TODO create for Shimmer4 or use Shimmer3?
			InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3) mInfoMemLayout;

			createInfoMemLayoutObjectIfNeeded();

			// Sensors
			mEnabledSensors = ((long)infoMemBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;
	
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
			
			// Shimmer Name
			byte[] shimmerNameBuffer = new byte[infoMemLayoutCast.lengthShimmerName];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSDShimmerName, shimmerNameBuffer, 0 , infoMemLayoutCast.lengthShimmerName);
			for(byte b : shimmerNameBuffer) {
				if(!UtilShimmer.isAsciiPrintable((char)b)) {
					break;
				}
				shimmerName += (char)b;
			}
			
			// Experiment Name
			byte[] experimentNameBuffer = new byte[infoMemLayoutCast.lengthExperimentName];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSDEXPIDName, experimentNameBuffer, 0 , infoMemLayoutCast.lengthExperimentName);
			String experimentName = "";
			for(byte b : experimentNameBuffer) {
				if(!UtilShimmer.isAsciiPrintable((char)b)) {
					break;
				}
				experimentName += (char)b;
			}
			mTrialName = new String(experimentName);

			//Configuration Time
			int bitShift = (infoMemLayoutCast.lengthConfigTimeBytes-1) * 8;
			mConfigTime = 0;
			for(int x=0; x<infoMemLayoutCast.lengthConfigTimeBytes; x++ ) {
				mConfigTime += (((long)(infoMemBytes[infoMemLayoutCast.idxSDConfigTime0+x] & 0xFF)) << bitShift);
				bitShift -= 8;
			}
			
			prepareAllAfterConfigRead();
			
			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.infoMemByteArrayParse(this, mInfoMemBytes);
			}

		}
		checkAndCorrectShimmerName(shimmerName);
	}

	//TODO improve flow of below, move to ShimmerDevice also?
	@Override
	public void prepareAllAfterConfigRead() {
		sensorAndConfigMapsCreate();
		
		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.BLUETOOTH);
		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.SD);

		//Override Shimmer4 sensors
		setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.HOST_SYSTEM_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.SHIMMER_TIMESTAMP, true);

//		sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE.BLUETOOTH);
		
//		printSensorAndParserMaps();
		
		int expectedDataPacketSize = getExpectedDataPacketSize(COMMUNICATION_TYPE.BLUETOOTH);
		if(mShimmerRadioHWLiteProtocol!=null){
			mShimmerRadioHWLiteProtocol.mRadioProtocol.setPacketSize(expectedDataPacketSize);
		}
	}
	
	/** For use when debugging */
	private void printSensorAndParserMaps(){
		//For debugging
		for(SensorDetails sensorDetails:mSensorMap.values()){
			System.out.println("SENSOR\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
		}
		for(COMMUNICATION_TYPE commType:mParserMap.keySet()){
			for(SensorDetails sensorDetails:mParserMap.get(commType).values()){
				System.out.println("ENABLED SENSOR\tCOMM TYPE:\t" + commType + "\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
			}
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
		
		refreshEnabledSensorsFromSensorMap();
		mInfoMemBytes[infoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors0) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors1) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors2) & infoMemLayout.maskSensors);

		// Derived Sensors
		if((infoMemLayout.idxDerivedSensors0>0)&&(infoMemLayout.idxDerivedSensors1>0)) { // Check if compatible
			mInfoMemBytes[infoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors0) & infoMemLayout.maskDerivedChannelsByte);
			mInfoMemBytes[infoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors1) & infoMemLayout.maskDerivedChannelsByte);
			if(infoMemLayout.idxDerivedSensors2>0) { // Check if compatible
				mInfoMemBytes[infoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors2) & infoMemLayout.maskDerivedChannelsByte);
			}
		}

		// Configuration from each Sensor settings
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			abstractSensor.infoMemByteArrayGenerate(this, mInfoMemBytes);
		}

		// Shimmer Name
		for (int i = 0; i < infoMemLayout.lengthShimmerName; i++) {
			if (i < mShimmerUserAssignedName.length()) {
				mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
			}
			else {
				mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) 0xFF;
			}
		}
		
		// Experiment Name
		for (int i = 0; i < infoMemLayout.lengthExperimentName; i++) {
			if (i < mTrialName.length()) {
				mInfoMemBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) mTrialName.charAt(i);
			}
			else {
				mInfoMemBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) 0xFF;
			}
		}

		//Configuration Time
		mInfoMemBytes[infoMemLayout.idxSDConfigTime0] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime0) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime1] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime1) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime2] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime2) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime3] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime3) & 0xFF);

		/*XXX - RS (26/5/2016):
		 * 
		 * Undocking after write config -> enabled sensors not enabled when docking again.
		 * MN: Set this flag to 1.
		 * 
		 * */ 
		mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] = 1;
		
		if(generateForWritingToShimmer) {
			// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
			// (already set to 0xFF at start of method but just in case)
			System.arraycopy(infoMemLayout.invalidMacId, 0, mInfoMemBytes, infoMemLayout.idxMacAddress, infoMemLayout.lengthMacIdBytes);

			//TODO only temporarily here to deal with fake Shimmer4 (i.e., a Shimmer3)
			mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= infoMemLayout.bitShiftSDCfgFileWriteFlag;
		}
		
		return mInfoMemBytes;
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
	
	// ----------------- BT LiteProtocolInstructionSet Start ------------------
	public void setRadio(ShimmerRadioProtocol shimmerRadioHWLiteProtocol){
		setShimmerRadioHWLiteProtocol(shimmerRadioHWLiteProtocol);
		initializeRadio();
	}

	private void initializeRadio(){
		mIsInitialised = false;
		if (mShimmerRadioHWLiteProtocol!=null){ // the radio instance should be declared on a higher level and not in this class
			mShimmerRadioHWLiteProtocol.setRadioListener(new RadioListener(){

			@Override
			public void connected() {
				// TODO Auto-generated method stub
				byte[] instructionFW = {LiteProtocolInstructionSet.InstructionsGet.GET_FW_VERSION_COMMAND_VALUE};
				mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionFW);
				byte[] instructionHW = {LiteProtocolInstructionSet.InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE};
				mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionHW);
			}

			@Override
			public void disconnected() {
				// TODO Auto-generated method stub
				setBluetoothRadioState(BT_STATE.DISCONNECTED);
			}

			@Override
			public void eventNewPacket(byte[] packetByteArray) {
				System.out.println("Packet: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(packetByteArray));

				long pcTimestamp = System.currentTimeMillis();
				ObjectCluster objectCluster = buildMsg(packetByteArray, COMMUNICATION_TYPE.BLUETOOTH, true, pcTimestamp);
				
//				//Hack for debugging
//				//JC: This is temp, should actually be set using the real Shimmer Clock cal time
//				objectCluster.mShimmerCalibratedTimeStamp = System.currentTimeMillis();
//				if(mFirstPacketParsed) {
//					mFirstPacketParsed=false;
//					long systemTime = System.currentTimeMillis();
//					objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
//					byte[] bSystemTS = objectCluster.mSystemTimeStamp;
//					ByteBuffer bb = ByteBuffer.allocate(8);
//			    	bb.put(bSystemTS);
//			    	bb.flip();
//			    	long systemTimeStamp = bb.getLong();
//					mOffsetFirstTime = systemTimeStamp-objectCluster.mShimmerCalibratedTimeStamp;
//				}
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, new FormatCluster(CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, objectCluster.mShimmerCalibratedTimeStamp+mOffsetFirstTime));
				
				dataHandler(objectCluster);
			}

			@Override
			public void eventResponseReceived(byte[] responseBytes) {
				// TODO Auto-generated method stub
				if ((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.FW_VERSION_RESPONSE_VALUE){
					int firmwareIdentifier=(int)((responseBytes[2]&0xFF)<<8)+(int)(responseBytes[1]&0xFF);
					int firmwareVersionMajor = (int)((responseBytes[4]&0xFF)<<8)+(int)(responseBytes[3]&0xFF);
					int firmwareVersionMinor = ((int)((responseBytes[5]&0xFF)));
					int firmwareVersionInternal=(int)(responseBytes[6]&0xFF);
					ShimmerVerObject shimmerVerObject = new ShimmerVerObject(getHardwareVersion(), firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
					setShimmerVersionObject(shimmerVerObject);
					if (firmwareIdentifier==FW_ID.LOGANDSTREAM){
						mNumberOfInfoMemReadsRequired = 4;
					}
					System.out.println("FW Version Response Received. FW Code: " + getFirmwareVersionCode());
					System.out.println("FW Version Response Received: " + getFirmwareVersionParsed());
				} else if ((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.GET_SHIMMER_VERSION_RESPONSE_VALUE){
					setHardwareVersion(responseBytes[1]);
					System.out.println("Shimmer Version Response Received. HW Code: " + getHardwareVersion());
					createInfoMemLayout();
					setBluetoothRadioState(BT_STATE.CONNECTING);
					readInfoMem();
					byte[] instructionBuffer = {(byte) LiteProtocolInstructionSet.InstructionsGet.GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND_VALUE};
					mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionBuffer);
				} else if((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.INFOMEM_RESPONSE_VALUE) {
					// Get data length to read
					int lengthToRead = responseBytes.length-1;
					byte[] rxBuf = new byte[lengthToRead];
					System.arraycopy(responseBytes, 1, rxBuf, 0, lengthToRead);
					System.out.println("INFOMEM_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
					
					//Copy to local buffer
					System.arraycopy(rxBuf, 0, mInfoMemBuffer, mCurrentInfoMemAddress, lengthToRead);
					//Update configuration when all bytes received.
					if((mCurrentInfoMemAddress+mCurrentInfoMemLengthToRead)==mInfoMemLayout.calculateInfoMemByteLength()){
						setShimmerInfoMemBytes(mInfoMemBuffer);
						infoMemByteArrayParse(mInfoMemBuffer);
						if (getBluetoothRadioState()==BT_STATE.CONNECTED){
							isReadyForStreaming();
						}
					}
					if (getBluetoothRadioState()==BT_STATE.CONNECTING){
						int numofIns = mShimmerRadioHWLiteProtocol.mRadioProtocol.getListofInstructions().size();
						sendProgressReport(new BluetoothProgressReportPerCmd(0, numofIns, getMacId(), getComPort()));
					}
				} else{
					if((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.BMP180_CALIBRATION_COEFFICIENTS_RESPONSE_VALUE) {
						if (getBluetoothRadioState()==BT_STATE.CONNECTING){
							int numofIns = mShimmerRadioHWLiteProtocol.mRadioProtocol.getListofInstructions().size();
							sendProgressReport(new BluetoothProgressReportPerCmd(0, numofIns, getMacId(), getComPort()));
						}
					}
					System.out.println("POSSIBLE_SENSOR_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(responseBytes));
					for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
						abstractSensor.processResponse(responseBytes, COMMUNICATION_TYPE.BLUETOOTH);
					}
				}
				
				
			}

			@Override
			public void eventAckReceived(byte[] instructionSent) {
				// TODO Auto-generated method stub
				if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsGet.GET_INFOMEM_COMMAND_VALUE){
					
					// store current address/InfoMem segment
					mCurrentInfoMemAddress = ((instructionSent[3]&0xFF)<<8)+(instructionSent[2]&0xFF);
					mCurrentInfoMemLengthToRead = (instructionSent[1]&0xFF);
				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.START_STREAMING_COMMAND_VALUE){
					mFirstPacketParsed = true;
					setBluetoothRadioState(BT_STATE.STREAMING);
				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE){
					setBluetoothRadioState(BT_STATE.CONNECTED);
				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsGet.GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND_VALUE){
					
				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.SET_SENSORS_COMMAND_VALUE){
					readInfoMem();
				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.SET_INFOMEM_COMMAND_VALUE){
					mNumOfInfoMemSetCmds -= 1;
					if(mNumOfInfoMemSetCmds==0){
						delayForBtResponse(DELAY_BETWEEN_INFOMEM_WRITES);
						readInfoMem();
					}
					else {
						delayForBtResponse(DELAY_AFTER_INFOMEM_WRITE);
					}
				}
			}

			});
			if (mShimmerRadioHWLiteProtocol.mSerialPort.isConnected()){
				// TODO Auto-generated method stub
				byte[] instructionFW = {LiteProtocolInstructionSet.InstructionsGet.GET_FW_VERSION_COMMAND_VALUE};
				mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionFW);
				byte[] instructionHW = {LiteProtocolInstructionSet.InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE};
				mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionHW);
			}
		}
	}
	
	public void setSetting(long sensorID, String componentName, Object valueToSet, COMMUNICATION_TYPE commType){
		ActionSetting actionSetting = mMapOfSensorClasses.get(sensorID).setSettings(componentName, valueToSet, commType);
		if (actionSetting.mCommType == COMMUNICATION_TYPE.BLUETOOTH){
			//mShimmerRadio.actionSettingResolver(actionSetting);
		}
	}
	
	public void writeInfoMem(int startAddress, byte[] buf){
		this.mNumOfInfoMemSetCmds  = 0;
		
		if(this.getFirmwareVersionCode()>=6){
			int address = startAddress;
			if (buf.length > (mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS - address + 1)) {
//				err = ErrorCodesShimmerUart.SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE;
//				DockException de = new DockException(mDockID,mSlotNumber,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_INFOMEM_SET ,ErrorCodesShimmerUart.SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE);
//				throw(de);
			} 
			else {
				int currentStartAddr = startAddress;
				int currentPacketNumBytes;
				int numBytesRemaining = buf.length;
				int currentBytePointer = 0;
				int maxPacketSize = 128;

				while (numBytesRemaining > 0) {
					if (numBytesRemaining > maxPacketSize) {
						currentPacketNumBytes = maxPacketSize;
					} else {
						currentPacketNumBytes = numBytesRemaining;
					}

					byte[] infoSegBuf = Arrays.copyOfRange(buf, currentBytePointer, currentBytePointer + currentPacketNumBytes);

					mShimmerRadioHWLiteProtocol.writeMemCommand((byte)LiteProtocolInstructionSet.InstructionsSet.SET_INFOMEM_COMMAND_VALUE, currentStartAddr, infoSegBuf);
					mNumOfInfoMemSetCmds += 1;

					currentStartAddr += currentPacketNumBytes;
					numBytesRemaining -= currentPacketNumBytes;
					currentBytePointer += currentPacketNumBytes;
				}
			}
		}
		
	}
	
	//TODO the contents are very specific to ShimmerRadioProtocol, don't think should be in this class
	private void readInfoMem(){
		mInfoMemBuffer = new byte[mInfoMemLayout.calculateInfoMemByteLength()];
		int size = mInfoMemLayout.calculateInfoMemByteLength(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
		int address = mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS;
		if (size > (mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS - address + 1)) {
//			DockException de = new DockException(mDockID,mSlotNumber,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_INFOMEM_GET ,ErrorCodesShimmerUart.SHIMMERUART_INFOMEM_READ_REQEST_EXCEEDS_INFO_RANGE);
//			throw(de);
		} 
		else {
			int maxBytesRXed = 128;
			int numBytesRemaining = size;
			int currentPacketNumBytes = size;
			int currentBytePointer = 0;
			int currentStartAddr = address;

			while (numBytesRemaining > 0) {
				if (numBytesRemaining > maxBytesRXed) {
					currentPacketNumBytes = maxBytesRXed;
				} 
				else {
					currentPacketNumBytes = numBytesRemaining;
				}
				
				byte[] rxBuf = new byte[] {};
				byte[] memLengthToRead = new byte[]{(byte) currentPacketNumBytes};
		    	byte[] memAddressToRead = ByteBuffer.allocate(2).putShort((short)(currentStartAddr&0xFFFF)).array();
				ArrayUtils.reverse(memAddressToRead);

		    	byte[] instructionBuffer = new byte[1 + memLengthToRead.length + memAddressToRead.length];
		    	instructionBuffer[0] = (byte) LiteProtocolInstructionSet.InstructionsGet.GET_INFOMEM_COMMAND_VALUE;
		    	System.arraycopy(memLengthToRead, 0, instructionBuffer, 1, memLengthToRead.length);
		    	System.arraycopy(memAddressToRead, 0, instructionBuffer, 1 + memLengthToRead.length, memAddressToRead.length);

		    	mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionBuffer);

				currentBytePointer += currentPacketNumBytes;
				numBytesRemaining -= currentPacketNumBytes;
				currentStartAddr += currentPacketNumBytes;
			}
//			utilDock.consolePrintLn(mDockID + " - InfoMem Configuration Read = SUCCESS");
		}
	}
	
	//TODO the contents are very specific to ShimmerRadioProtocol, don't think should be in this class
	public void toggleLed() {
		byte[] instructionLED = {LiteProtocolInstructionSet.InstructionsSet.TOGGLE_LED_COMMAND_VALUE};
		mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionLED);
	}
	
	@Override
	protected void setBluetoothRadioState(BT_STATE state){

		if (state.equals(BT_STATE.CONNECTED)){
			mIsConnected = true;
			mIsStreaming = false;
		} else if (state.equals(BT_STATE.DISCONNECTED)){
			mIsConnected = false;
			mIsStreaming = false;
			mIsInitialised = false;
		} else if (state.equals(BT_STATE.STREAMING)){
			mIsStreaming = true;
		} else if (state.equals(BT_STATE.CONNECTING)){
			mIsConnected = true;
			mIsInitialised = false;
			startOperation(BT_STATE.CONNECTING,mNumberOfInfoMemReadsRequired);
		}
		CallbackObject callBackObject2 = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE,state, getMacIdFromUart(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject2);
		mBluetoothRadioState = state;		
	}
	
	/**
	 * @return the mShimmerRadioHWLiteProtocol
	 */
	public ShimmerRadioProtocol getShimmerRadioHWLiteProtocol() {
		return mShimmerRadioHWLiteProtocol;
	}

	/**
	 * @param shimmerRadioHWLiteProtocol the mShimmerRadioHWLiteProtocol to set
	 */
	public void setShimmerRadioHWLiteProtocol(ShimmerRadioProtocol shimmerRadioHWLiteProtocol) {
		this.mShimmerRadioHWLiteProtocol = shimmerRadioHWLiteProtocol;
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		if(mShimmerRadioHWLiteProtocol!=null){
			try {
				mShimmerRadioHWLiteProtocol.disconnect();
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getComPort() {
		if(mShimmerRadioHWLiteProtocol!=null && mShimmerRadioHWLiteProtocol.mSerialPort!=null){
			return ((SerialPortComm) mShimmerRadioHWLiteProtocol.mSerialPort).mAddress;
		}
		return null;
	}

	public void writeConfigurationToInfoMem(byte[] shimmerInfoMemBytes) {
		if(mShimmerRadioHWLiteProtocol!=null && mShimmerRadioHWLiteProtocol.mSerialPort!=null){
//			mShimmerRadioHWLiteProtocol.
			writeInfoMem(mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS, shimmerInfoMemBytes);
		}
	}

	public void writeEnabledSensors(long enabledSensors) {
		if(mShimmerRadioHWLiteProtocol!=null && mShimmerRadioHWLiteProtocol.mSerialPort!=null){
//			mShimmerRadioHWLiteProtocol.
			mShimmerRadioHWLiteProtocol.writeEnabledSensors(enabledSensors);
		}
	}

	// ----------------- BT LiteProtocolInstructionSet End ------------------

	protected void dataHandler(ObjectCluster ojc) {
		
		//CallbackObject callBackObject = new CallbackObject(MSG_IDENTIFIER_PACKET_RECEPTION_RATE, getBluetoothAddress(), mComPort, getPacketReceptionRate());
		//sendCallBackMsg(MSG_IDENTIFIER_PACKET_RECEPTION_RATE, callBackObject);
		
//		sendCallBackMsg(MSG_IDENTIFIER_PACKET_RECEPTION_RATE, getBluetoothAddress());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
	}
	
	protected void sendProgressReport(BluetoothProgressReportPerCmd pRPC) {
		if(progressReportPerDevice!=null){
			progressReportPerDevice.updateProgress(pRPC);
			int progress = progressReportPerDevice.mProgressPercentageComplete;
			CallbackObject callBackObject = new CallbackObject(mBluetoothRadioState, getMacId(), getComPort(), progressReportPerDevice);
			sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
			
//			consolePrintLn("ProgressCounter" + progressReportPerDevice.mProgressCounter + "\tProgressEndValue " + progressReportPerDevice.mProgressEndValue);
			
			if(progressReportPerDevice.mProgressCounter==progressReportPerDevice.mProgressEndValue){
				isReadyForStreaming();
			}
		}
	}
	
	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds){
		consolePrintLn(currentOperation + " START");

		progressReportPerDevice = new BluetoothProgressReportPerDevice(this, currentOperation, totalNumOfCmds);
		progressReportPerDevice.mOperationState = BluetoothProgressReportPerDevice.OperationState.INPROGRESS;
		
		CallbackObject callBackObject = new CallbackObject(mBluetoothRadioState, this.getMacId(), getComPort(), progressReportPerDevice);
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
	}
	
	public void finishOperation(BT_STATE btState){
		consolePrintLn("CURRENT OPERATION " + progressReportPerDevice.mCurrentOperationBtState + "\tFINISHED:" + btState);
		
		if(progressReportPerDevice.mCurrentOperationBtState == btState){

			progressReportPerDevice.finishOperation();
			progressReportPerDevice.mOperationState = BluetoothProgressReportPerDevice.OperationState.SUCCESS;
			//JC: moved operationFinished to is ready for streaming, seems to be called before the inquiry response is received
			CallbackObject callBackObject = new CallbackObject(mBluetoothRadioState, getMacId(), getComPort(), progressReportPerDevice);
			sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
			
		}

	}
	
	public void isReadyForStreaming(){
		mIsInitialised = true;
		if (getBluetoothRadioState()==BT_STATE.CONNECTING){
			finishOperation(progressReportPerDevice.mCurrentOperationBtState);
		}
		CallbackObject callBackObject2 = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, mMacIdFromUart, getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject2);
		if (getBluetoothRadioState()==BT_STATE.CONNECTING){
			setBluetoothRadioState(BT_STATE.CONNECTED);
		}
	}
	
	/**
	 * Due to the nature of the Bluetooth SPP stack a delay has been added to
	 * ensure the buffer is filled before it is read
	 * 
	 */
	private void delayForBtResponse(long millis){
		try {
			Thread.sleep(millis);	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
