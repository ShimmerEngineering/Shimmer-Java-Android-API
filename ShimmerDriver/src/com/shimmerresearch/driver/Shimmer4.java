package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.serialPortInterface.SerialPortComm;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
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
	
	public transient ShimmerRadioProtocol mShimmerRadioHWLiteProtocol = null;

	byte[] mInfoMemBuffer;
	private int mCurrentInfoMemAddress = 0;
	private int mCurrentInfoMemLengthToRead = 0;
	
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
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			mMapOfSensorClasses.put(SENSORS.EXG, new SensorEXG(mShimmerVerObject));
		}

		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			mMapOfSensorClasses.put(SENSORS.GSR, new SensorGSR(mShimmerVerObject));
		}

		//Commented out until PPG fully implemented
//		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
//				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
//				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE
//				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
//			if(isDerivedSensorsSupported()){
//				mMapOfSensorClasses.put(SENSORS.PPG, new SensorPPG(mShimmerVerObject));
//			}
//		}

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
			//TODO create for Shimmer4 or use Shimmer3?
			InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3) mInfoMemLayout;

			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.infoMemByteArrayParse(this, mInfoMemBytes);
			}

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
		}
		checkAndCorrectShimmerName(shimmerName);
	}

	//TODO improve flow of below, move to ShimmerDevice also?
	private void prepareAllAfterConfigRead() {
		sensorAndConfigMapsCreate();
		
		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.BLUETOOTH);
		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.SD);
		
		setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.TIMESTAMP, true);
		
//		sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE.BLUETOOTH);
		
//		//For debugging
//		for(SensorDetails sensorDetails:mSensorMap.values()){
//			System.out.println("SENSOR\t" + sensorDetails.mSensorDetails.mGuiFriendlyLabel);
//		}
//		for(COMMUNICATION_TYPE commType:mParserMap.keySet()){
//			for(SensorDetails sensorDetails:mParserMap.get(commType).values()){
//				System.out.println("ENABLED SENSOR\tCOMM TYPE:\t" + commType + "\t" + sensorDetails.mSensorDetails.mGuiFriendlyLabel);
//			}
//		}
		
		int expectedDataPacketSize = getExpectedDataPacketSize(COMMUNICATION_TYPE.BLUETOOTH);
		if(mShimmerRadioHWLiteProtocol!=null){
			mShimmerRadioHWLiteProtocol.mRadioProtocol.setPacketSize(expectedDataPacketSize);
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

		
		if(generateForWritingToShimmer) {
			// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
			// (already set to 0xFF at start of method but just incase)
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
		if (mShimmerRadioHWLiteProtocol!=null){ // the radio instance should be declared on a higher level and not in this class
			mShimmerRadioHWLiteProtocol.setRadioListener(new RadioListener(){

			@Override
			public void connected() {
				setBluetoothRadioState(BT_STATE.CONNECTING);
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
				ObjectCluster objectCluster = buildMsg(packetByteArray, COMMUNICATION_TYPE.BLUETOOTH);
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
					
					System.out.println("FW Version Response Received. FW Code: " + getFirmwareVersionCode());
					System.out.println("FW Version Response Received: " + getFirmwareVersionParsed());
				} else if ((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.GET_SHIMMER_VERSION_RESPONSE_VALUE){
					setHardwareVersion(responseBytes[1]);
					System.out.println("Shimmer Version Response Received. HW Code: " + getHardwareVersion());
					createInfoMemLayout();
					readInfoMem();
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
						String comPort = getComPort();
						mIsInitialised = true;
						setBluetoothRadioState(BT_STATE.CONNECTED);
						CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, mMacIdFromUart, comPort);
						sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
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
					setBluetoothRadioState(BT_STATE.STREAMING);
				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE){
					setBluetoothRadioState(BT_STATE.CONNECTED);
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
		}
	}

	public void writeEnabledSensors(long enabledSensors) {
		if(mShimmerRadioHWLiteProtocol!=null && mShimmerRadioHWLiteProtocol.mSerialPort!=null){
//			mShimmerRadioHWLiteProtocol.
		}
	}

	// ----------------- BT LiteProtocolInstructionSet End ------------------

}
