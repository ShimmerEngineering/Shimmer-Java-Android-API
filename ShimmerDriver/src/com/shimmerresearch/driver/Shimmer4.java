package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsResponse;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsSet;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortComm;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.SensorADC;
import com.shimmerresearch.sensors.SensorBMP180;
import com.shimmerresearch.sensors.SensorBMP280;
import com.shimmerresearch.sensors.SensorBattVoltage;
import com.shimmerresearch.sensors.SensorBridgeAmp;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorKionixKXRB52042;
import com.shimmerresearch.sensors.SensorLSM303;
import com.shimmerresearch.sensors.SensorMPU9X50;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorPPG;
import com.shimmerresearch.sensors.SensorSTC3100;
import com.shimmerresearch.sensors.ShimmerClock;

public class Shimmer4 extends ShimmerDevice {
	
	/** * */
	private static final long serialVersionUID = 6916261534384275804L;
	
	private double mOffsetFirstTime;
	private boolean mFirstPacketParsed;
	public BluetoothProgressReportPerDevice progressReportPerDevice;
	
	protected boolean mSendProgressReport = true;

	//TODO consider where to handle the below -> carried over from ShimmerObject
	protected boolean mButtonStart = true;
	protected boolean mShowRtcErrorLeds = true;
	protected boolean mConfigFileCreationFlag = true;
	protected boolean mCalibFileCreationFlag = false;
	private boolean isOverrideShowRwcErrorLeds = true;
	
	protected int mBluetoothBaudRate=9; //460800

	public Shimmer4() {
		// TODO Auto-generated constructor stub
	}
	
	public Shimmer4(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE communicationType) {
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(communicationType);
    	setSamplingRateShimmer(communicationType, 128);
    	setMacIdFromUart(macId);
    	
    	//TODO HACK!!!!!
    	setSamplingRateShimmer(COMMUNICATION_TYPE.SD, 128.0);
    	setSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH, 128.0);
	}
	
	@Override
	public void sensorAndConfigMapsCreate() {
		mMapOfSensorClasses = new LinkedHashMap<SENSORS, AbstractSensor>();

		//Example code
//		if(UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
//				HW_ID.SHIMMER_4_SDK, FW_ID.LOGANDSTREAM, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION)){
//			mMapOfSensorClasses.put(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStamp(mShimmerVerObject));
//		}

		
		mMapOfSensorClasses.put(SENSORS.CLOCK, new ShimmerClock(this));
		
		mMapOfSensorClasses.put(SENSORS.KIONIXKXRB52042, new SensorKionixKXRB52042(mShimmerVerObject));
		mMapOfSensorClasses.put(SENSORS.LSM303, new SensorLSM303(mShimmerVerObject));
		mMapOfSensorClasses.put(SENSORS.MPU9X50, new SensorMPU9X50(mShimmerVerObject));
		mMapOfSensorClasses.put(SENSORS.ADC, new SensorADC(mShimmerVerObject));
		mMapOfSensorClasses.put(SENSORS.Battery, new SensorBattVoltage(this));
		mMapOfSensorClasses.put(SENSORS.Bridge_Amplifier, new SensorBridgeAmp(mShimmerVerObject));
		
		
		//TODO push version checking into the sensor classes
		
		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_EXG 
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			mMapOfSensorClasses.put(SENSORS.EXG, new SensorEXG(this));
		}

		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			mMapOfSensorClasses.put(SENSORS.GSR, new SensorGSR(mShimmerVerObject));
		}

		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			if(isDerivedSensorsSupported()){
				mMapOfSensorClasses.put(SENSORS.PPG, new SensorPPG(this));
			}
		}

		//Shimmer4 enhancements - Only available on the SDK
		if(getExpansionBoardId()==HW_ID_SR_CODES.SHIMMER_4_SDK){
			mMapOfSensorClasses.put(SENSORS.BMP280, new SensorBMP280(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.STC3100, new SensorSTC3100(mShimmerVerObject));
		}
		else{
			mMapOfSensorClasses.put(SENSORS.BMP180, new SensorBMP180(mShimmerVerObject));
		}
		
		generateSensorAndParserMaps();
		
		generateMapOfAlgorithmModules();
		generateMapOfAlgorithmConfigOptions();
		generateMapOfAlgorithmGroupingMap();
		
		handleSpecialCasesAfterSensorMapCreate();
	}
	
	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		//TODO don't think this is relevant for Shimmer4
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

			setSamplingRateShimmer((32768/(double)((int)(infoMemBytes[infoMemLayoutCast.idxShimmerSamplingRate] & infoMemLayoutCast.maskShimmerSamplingRate) 
					+ ((int)(infoMemBytes[infoMemLayoutCast.idxShimmerSamplingRate+1] & infoMemLayoutCast.maskShimmerSamplingRate) << 8))));

			// Sensors
			mEnabledSensors = ((long)infoMemBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;
	
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors3] & 0xFF) << infoMemLayoutCast.bitShiftSensors3;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors4] & 0xFF) << infoMemLayoutCast.bitShiftSensors4;
			
			mInternalExpPower = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte3] >> infoMemLayoutCast.bitShiftEXPPowerEnable) & infoMemLayoutCast.maskEXPPowerEnable;

			
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
			
			mBluetoothBaudRate = infoMemBytes[infoMemLayoutCast.idxBtCommBaudRate] & infoMemLayoutCast.maskBaudRate;

			
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
			
			mButtonStart = ((infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftButtonStart) & infoMemLayoutCast.maskButtonStart)>0? true:false;
			mShowRtcErrorLeds = ((infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftShowRwcErrorLeds) & infoMemLayoutCast.maskShowRwcErrorLeds)>0? true:false;

			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.infoMemByteArrayParse(this, mInfoMemBytes);
			}

			prepareAllAfterConfigRead();
		}
		checkAndCorrectShimmerName(shimmerName);
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
		
		int samplingRate = (int)(32768 / getSamplingRateShimmer());
		mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate] = (byte) (samplingRate & infoMemLayout.maskShimmerSamplingRate); 
		mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8) & infoMemLayout.maskShimmerSamplingRate); 

		
		mInfoMemBytes[infoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors0) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors1) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors2) & infoMemLayout.maskSensors);

		mInfoMemBytes[infoMemLayout.idxSensors3] = (byte) ((mEnabledSensors >> infoMemLayout.bitShiftSensors3) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSensors4] = (byte) ((mEnabledSensors >> infoMemLayout.bitShiftSensors4) & 0xFF);

		mInfoMemBytes[infoMemLayout.idxConfigSetupByte0] = (byte) (0x00);
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte1] = (byte) (0x00);
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte2] = (byte) (0x00);
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] = (byte) (0x00);
		
		checkIfInternalExpBrdPowerIsNeeded();
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & infoMemLayout.maskEXPPowerEnable) << infoMemLayout.bitShiftEXPPowerEnable);
		
		// Derived Sensors
		if((infoMemLayout.idxDerivedSensors0>0)&&(infoMemLayout.idxDerivedSensors1>0)) { // Check if compatible
			mInfoMemBytes[infoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors0) & infoMemLayout.maskDerivedChannelsByte);
			mInfoMemBytes[infoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors1) & infoMemLayout.maskDerivedChannelsByte);
			if(infoMemLayout.idxDerivedSensors2>0) { // Check if compatible
				mInfoMemBytes[infoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors2) & infoMemLayout.maskDerivedChannelsByte);
			}
		}
		
		//TODO handle the below better
		mBluetoothBaudRate = 9;
		mInfoMemBytes[infoMemLayout.idxBtCommBaudRate] = (byte) (mBluetoothBaudRate & infoMemLayout.maskBaudRate);

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

		
		mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] = (byte) ((mButtonStart? infoMemLayout.maskButtonStart:0) << infoMemLayout.bitShiftButtonStart);
		if(this.isOverrideShowRwcErrorLeds){
			mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((infoMemLayout.maskShowRwcErrorLeds) << infoMemLayout.bitShiftShowRwcErrorLeds);
		}
		else {
			mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((mShowRtcErrorLeds? infoMemLayout.maskShowRwcErrorLeds:0) << infoMemLayout.bitShiftShowRwcErrorLeds);
		}

		if(generateForWritingToShimmer) {
			// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
			// (already set to 0xFF at start of method but just incase)
			System.arraycopy(infoMemLayout.invalidMacId, 0, mInfoMemBytes, infoMemLayout.idxMacAddress, infoMemLayout.lengthMacIdBytes);

			mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] = 0;
			// Tells the Shimmer to create a new config file on undock/power cycle
			//TODO RM enabled the two lines below (MN had the below two lines commented out.. but need them to write config successfully over UART)
			byte configFileWriteBit = (byte) (mConfigFileCreationFlag? (infoMemLayout.maskSDCfgFileWriteFlag << infoMemLayout.bitShiftSDCfgFileWriteFlag):0x00);
			mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= configFileWriteBit;

			mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= infoMemLayout.bitShiftSDCfgFileWriteFlag;

			 // Tells the Shimmer to create a new calibration files on undock/power cycle
			byte calibFileWriteBit = (byte) (mCalibFileCreationFlag? (infoMemLayout.maskSDCalibFileWriteFlag << infoMemLayout.bitShiftSDCalibFileWriteFlag):0x00);
			mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= calibFileWriteBit;
		}
		
		if(generateForWritingToShimmer) {
			// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
			// (already set to 0xFF at start of method but just in case)
			System.arraycopy(infoMemLayout.invalidMacId, 0, mInfoMemBytes, infoMemLayout.idxMacAddress, infoMemLayout.lengthMacIdBytes);

			//TODO only temporarily here to deal with fake Shimmer4 (i.e., a Shimmer3)
			mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= infoMemLayout.bitShiftSDCfgFileWriteFlag;
		}
		
		
		return mInfoMemBytes;
	}
	
	//TODO improve flow of below, move to ShimmerDevice also?
	@Override
	public void prepareAllAfterConfigRead() {
		//Can't create again because any sensor configuration that was read from InfoMem will be over written
//		sensorAndConfigMapsCreate();
		
//		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.ALL);
//		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.BLUETOOTH);
//		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors, COMMUNICATION_TYPE.SD);
		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors);

		//Override Shimmer4 sensors
		setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.HOST_SYSTEM_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.SHIMMER_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES, true);

//		sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE.BLUETOOTH);

//		printSensorAndParserMaps();

		updateExpectedDataPacketSize();
	}
	
	private void updateExpectedDataPacketSize() {
		int expectedDataPacketSize = getExpectedDataPacketSize(COMMUNICATION_TYPE.BLUETOOTH);
//		int expectedDataPacketSize = getExpectedDataPacketSize(COMMUNICATION_TYPE.ALL);
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.setPacketSize(expectedDataPacketSize);
		}
	}

	@Override
	public void setDefaultShimmerConfiguration() {
		super.setDefaultShimmerConfiguration();
		
		mInternalExpPower=0;
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
	public void setRadio(CommsProtocolRadio commsRadio){
		setCommsProtocolRadio(commsRadio);
		initializeRadio();
	}

	private void initializeRadio(){
		setIsInitialised(false);
		if (mCommsProtocolRadio!=null){ // the radio instance should be declared on a higher level and not in this class
			mCommsProtocolRadio.addRadioListener(new RadioListener(){

			@Override
			public void connected() {
				setIsConnected(true);
//				initialise(hardwareVersion);
			}

			@Override
			public void disconnected() {
				setBluetoothRadioState(BT_STATE.DISCONNECTED);
				mCommsProtocolRadio = null;
			}

			@Override
			public void eventNewPacket(byte[] packetByteArray, long pcTimestamp) {
				buildMsg(packetByteArray, COMMUNICATION_TYPE.BLUETOOTH, true, pcTimestamp);
			}

			@Override
			public void eventResponseReceived(byte[] responseBytes) {
				
//				if ((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.FW_VERSION_RESPONSE_VALUE){
//					int firmwareIdentifier=(int)((responseBytes[2]&0xFF)<<8)+(int)(responseBytes[1]&0xFF);
//					int firmwareVersionMajor = (int)((responseBytes[4]&0xFF)<<8)+(int)(responseBytes[3]&0xFF);
//					int firmwareVersionMinor = ((int)((responseBytes[5]&0xFF)));
//					int firmwareVersionInternal=(int)(responseBytes[6]&0xFF);
//					ShimmerVerObject shimmerVerObject = new ShimmerVerObject(getHardwareVersion(), firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
//					setShimmerVersionObject(shimmerVerObject);
//					if (firmwareIdentifier==FW_ID.LOGANDSTREAM){
//						mNumberOfInfoMemReadsRequired = 4;
//					}
//					System.out.println("FW Version Response Received. FW Code: " + getFirmwareVersionCode());
//					System.out.println("FW Version Response Received: " + getFirmwareVersionParsed());
//				} 
//				else if ((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.GET_SHIMMER_VERSION_RESPONSE_VALUE){
//					setHardwareVersion(responseBytes[1]);
//					System.out.println("Shimmer Version Response Received. HW Code: " + getHardwareVersion());
//					createInfoMemLayout();
//					setBluetoothRadioState(BT_STATE.CONNECTING);
//					//MN REMOVED
////					readInfoMem();
//				} 
//				else if((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.INFOMEM_RESPONSE_VALUE) {
//					// Get data length to read
//					int lengthToRead = responseBytes.length-1;
//					byte[] rxBuf = new byte[lengthToRead];
//					System.arraycopy(responseBytes, 1, rxBuf, 0, lengthToRead);
//					System.out.println("INFOMEM_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
//					
//					//Copy to local buffer
//					System.arraycopy(rxBuf, 0, mInfoMemBuffer, mCurrentInfoMemAddress, lengthToRead);
//					//Update configuration when all bytes received.
//					if((mCurrentInfoMemAddress+mCurrentInfoMemLengthToRead)==mInfoMemLayout.calculateInfoMemByteLength()){
//						setShimmerInfoMemBytes(mInfoMemBuffer);
//						infoMemByteArrayParse(mInfoMemBuffer);
//						if (getBluetoothRadioState()==BT_STATE.CONNECTED){
//							isReadyForStreaming();
//						}
//					}
//					if (getBluetoothRadioState()==BT_STATE.CONNECTING){
//						int numofIns = mShimmerRadioHWLiteProtocol.mRadioProtocol.getListofInstructions().size();
//						sendProgressReport(new BluetoothProgressReportPerCmd(0, numofIns, getMacId(), getComPort()));
//					}
//				} 
//				else if((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.BMP180_CALIBRATION_COEFFICIENTS_RESPONSE_VALUE) {
//					if (getBluetoothRadioState()==BT_STATE.CONNECTING){
//						int numofIns = mShimmerRadioHWLiteProtocol.mRadioProtocol.getListofInstructions().size();
//						sendProgressReport(new BluetoothProgressReportPerCmd(0, numofIns, getMacId(), getComPort()));
//					}
//				}
//				else if((responseBytes[0]&0xff) == LiteProtocolInstructionSet.InstructionsResponse.STATUS_RESPONSE_VALUE) {
//					System.err.println("STATUS RESPONSE RECEIVED");
//				}
//				else{
//					System.out.println("POSSIBLE_SENSOR_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(responseBytes));
//					for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
//						abstractSensor.processResponse(responseBytes, COMMUNICATION_TYPE.BLUETOOTH);
//					}
//				}
				
				
			}

			@Override
			public void eventAckReceived(int lastSentInstruction) {
//				// TODO Auto-generated method stub
//				if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsGet.GET_INFOMEM_COMMAND_VALUE){
//					
//					// store current address/InfoMem segment
//					mCurrentInfoMemAddress = ((instructionSent[3]&0xFF)<<8)+(instructionSent[2]&0xFF);
//					mCurrentInfoMemLengthToRead = (instructionSent[1]&0xFF);
//				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.START_STREAMING_COMMAND_VALUE){
//					mFirstPacketParsed = true;
//					setBluetoothRadioState(BT_STATE.STREAMING);
//				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE){
//					hasStopStreaming();
//				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsGet.GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND_VALUE){
//					
//				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.SET_SENSORS_COMMAND_VALUE){
//					readConfigurationFromInfoMem();
//				} else if((instructionSent[0]&0xff)==LiteProtocolInstructionSet.InstructionsSet.SET_INFOMEM_COMMAND_VALUE){
//					mNumOfInfoMemSetCmds -= 1;
//					int numofIns = mShimmerRadioHWLiteProtocol.mRadioProtocol.getListofInstructions().size();
//					sendProgressReport(new BluetoothProgressReportPerCmd(0, numofIns, getMacId(), getComPort()));
//					if(mNumOfInfoMemSetCmds==0){
//						delayForBtResponse(DELAY_BETWEEN_INFOMEM_WRITES);
//						readConfigurationFromInfoMem();
//					}
//					else {
//						delayForBtResponse(DELAY_AFTER_INFOMEM_WRITE);
//					}
//				}
				
//				if(instructionSent[0]==InstructionsGet.GET_FW_VERSION_COMMAND_VALUE){
//					setShimmerVersionInfoAndCreateSensorMap(new ShimmerVerObject(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0));
//					
//	//				/*Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
//	//      	        Bundle bundle = new Bundle();
//	//      	        bundle.putString(TOAST, "Firmware Version: " +mFirmwareVersionParsed);
//	//      	        msg.setData(bundle);*/
//	//				if(!mDummy){
//	//					//mHandler.sendMessage(msg);
//	//				}
//					
//					initializeBoilerPlate();
//				}

			}

			@Override
			public void eventResponseReceived(int responseCommand, Object parsedResponse) {
				if(responseCommand==InstructionsResponse.INQUIRY_RESPONSE_VALUE){ 
					interpretInqResponse((byte[])parsedResponse);
					prepareAllAfterConfigRead();
					inquiryDone();
				}
				else if(responseCommand==InstructionsResponse.GET_SHIMMER_VERSION_RESPONSE_VALUE){ 
					setHardwareVersion((int)parsedResponse);
				}
				else if(responseCommand==InstructionsResponse.FW_VERSION_RESPONSE_VALUE){ 
					if(parsedResponse!=null){
						ShimmerVerObject shimmerVerObject = (ShimmerVerObject)parsedResponse;
						shimmerVerObject.setHardwareVersion(getHardwareVersion());
						//TODO check with ShimmerBluetooth
						setShimmerVersionInfoAndCreateSensorMap(shimmerVerObject);
//						setShimmerVersionObject(shimmerVerObject);
					}
					else {
						processFirmwareVerResponse();
					}
				}
				else if(responseCommand==InstructionsResponse.INFOMEM_RESPONSE_VALUE){ 
					setShimmerInfoMemBytes((byte[])parsedResponse);
				}
//				else if(responseCommand==InstructionsResponse.RSP_CALIB_DUMP_COMMAND){
//					parseCalibByteDump((byte[])parsedResponse, CALIB_READ_SOURCE.RADIO_DUMP);
//				}
				else if(responseCommand==InstructionsResponse.BLINK_LED_RESPONSE_VALUE){ 
//					mCurrentLEDStatus = byteled[0]&0xFF;
				}
				else if(responseCommand==InstructionsResponse.BMP180_CALIBRATION_COEFFICIENTS_RESPONSE_VALUE){ 
					AbstractSensor abstractSensor = mMapOfSensorClasses.get(SENSORS.BMP180);
					if(abstractSensor!=null && abstractSensor instanceof SensorBMP180){
						SensorBMP180 sensorBmp180 = (SensorBMP180)abstractSensor;
						sensorBmp180.retrievePressureCalibrationParametersFromPacket((byte[])parsedResponse, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
					}
				}
				else if(responseCommand==InstructionsResponse.STATUS_RESPONSE_VALUE){ 
					consolePrintLn("STATUS RESPONSE RECEIVED");
//					parseStatusByte((byte)parsedResponse);
				}
				else if(responseCommand==InstructionsResponse.VBATT_RESPONSE_VALUE){ 
					setBattStatusDetails((ShimmerBattStatusDetails)parsedResponse);
				}
				else if(responseCommand==InstructionsResponse.RWC_RESPONSE_VALUE){ 
					setLastReadRealTimeClockValue((long)parsedResponse);
				}
				else{
					consolePrintLn("Unhandled Response In Shimmer4 class: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(new byte[]{(byte) responseCommand}));

//					System.out.println("POSSIBLE_SENSOR_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(responseBytes));
//					for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
//						abstractSensor.processResponse(responseBytes, COMMUNICATION_TYPE.BLUETOOTH);
//					}
				}

			}

			@Override
			public void startOperationCallback(BT_STATE currentOperation, int totalNumOfCmds) {
				startOperation(currentOperation, totalNumOfCmds);
			}

			@Override
			public void finishOperationCallback(BT_STATE currentOperation) {
				finishOperation(currentOperation);
			}

			@Override
			public void sendProgressReportCallback(BluetoothProgressReportPerCmd progressReportPerCmd) {
				sendProgressReport(progressReportPerCmd);
			}

			@Override
			public void eventLogAndStreamStatusChangedCallback(int lastSentInstruction) {
				eventLogAndStreamStatusChanged(lastSentInstruction);
			}

			@Override
			public void eventDockedStateChange() {
				dockedStateChange();
			}

			@Override
			public void isNowStreamingCallback() {
				isNowStreaming();
			}
			
			@Override
			public void hasStopStreamingCallback() {
				hasStopStreaming();
			}

			@Override
			public void initialiseStreamingCallback() {
//				//provides a callback for users to initialize their algorithms when start streaming is called
//				if(mDataProcessing!=null){
//					mDataProcessing.InitializeProcessData();
//				} 	
//				else {
//					//do nothing
//				}
//				
				mPacketLossCountPerTrial = 0;
				setPacketReceptionRateOverall(100);
				mFirstPacketParsed=true;
//				mFirstTimeCalTime=true;
//				resetCalibratedTimeStamp();
//				mLastReceivedCalibratedTimeStamp = -1;
//				mSync=true; // a backup sync done every time you start streaming
			}

			@Override
			public void eventSetIsDocked(boolean isDocked) {
				setIsDocked(isDocked);
			}

			@Override
			public void eventSetIsStreaming(boolean isStreaming) {
				setIsStreaming(isStreaming);
			}

			@Override
			public void eventSetIsSensing(boolean isSensing) {
				setIsSensing(isSensing);
			}

			@Override
			public void eventSetIsSDLogging(boolean isSdLogging) {
				setIsSDLogging(isSdLogging);
			}

			@Override
			public void eventSetIsInitialised(boolean isInitialised) {
				setIsInitialised(isInitialised);
			}

			@Override
			public void eventSetHaveAttemptedToRead(boolean haveAttemptedToRead) {
				setHaveAttemptedToReadConfig(haveAttemptedToRead);
			}

			@Override
			public void eventError(DeviceException dE) {
				consolePrintException(dE.getMessage(), dE.getStackTrace());
			}

//			@Override
//			public void eventSyncStates(boolean isDocked, boolean isInitialised, boolean isSdLogging, boolean isSensing, boolean isStreaming, boolean haveAttemptedToRead) {
//				setIsDocked(isDocked);
//				setIsInitialised(isInitialised);
//				setIsSDLogging(isSdLogging);
//				setIsSensing(isSensing);
//				setIsConnected(mCommsProtocolRadio.isConnected());
//				setIsStreaming(isStreaming);
//				setHaveAttemptedToRead(haveAttemptedToRead);
//			}



			});
			
			
//			if (mShimmerRadioHWLiteProtocol.mSerialPort.isConnected()){
//				// TODO Auto-generated method stub
//				byte[] instructionFW = {LiteProtocolInstructionSet.InstructionsGet.GET_FW_VERSION_COMMAND_VALUE};
//				mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionFW);
//				byte[] instructionHW = {LiteProtocolInstructionSet.InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE};
//				mShimmerRadioHWLiteProtocol.mRadioProtocol.writeInstruction(instructionHW);
//			}
		}
	}
	
	@Override
	public ObjectCluster buildMsg(byte[] newPacket, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, long pcTimestamp) {
//		System.out.println("Packet: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(packetByteArray));

		ObjectCluster objectCluster = super.buildMsg(newPacket, COMMUNICATION_TYPE.BLUETOOTH, true, pcTimestamp);
		dataHandler(objectCluster);
		return objectCluster;
	}
	
	@Override
	public void connect() throws DeviceException {
		clearShimmerVersionObject();
		
		setBluetoothRadioState(BT_STATE.CONNECTING);
		if(mCommsProtocolRadio!=null){
			try {
				mCommsProtocolRadio.connect();
			} catch (DeviceException dE) {
				consolePrintException(dE.getMessage(), dE.getStackTrace());
				dE.printStackTrace();
				
				disconnect();
				setBluetoothRadioState(BT_STATE.CONNECTION_FAILED);
				throw(dE);
			}
		}
	}
	
	//TODO Copied from ShimmerObject
	protected void interpretInqResponse(byte[] bufferInquiry){
//		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
//			mPacketSize = mTimeStampPacketByteSize +bufferInquiry[3]*2; 
//			setSamplingRateShimmer((double)1024/bufferInquiry[0]);
//			if (mLSM303MagRate==3 && getSamplingRateShimmer()>10){
//				mLowPowerMag = true;
//			}
//			mAccelRange = bufferInquiry[1];
//			mConfigByte0 = bufferInquiry[2] & 0xFF; //convert the byte to unsigned integer
//			mNChannels = bufferInquiry[3];
//			mBufferSize = bufferInquiry[4];
//			byte[] signalIdArray = new byte[mNChannels];
//			System.arraycopy(bufferInquiry, 5, signalIdArray, 0, mNChannels);
//			updateEnabledSensorsFromChannels(signalIdArray);
//			interpretDataPacketFormat(mNChannels,signalIdArray);
//			mInquiryResponseBytes = new byte[5+mNChannels];
//			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
//		} 
//		else if (getHardwareVersion()==HW_ID.SHIMMER_3) {
//			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[6]*2; 
//			setSamplingRateShimmer((32768/(double)((int)(bufferInquiry[0] & 0xFF) + ((int)(bufferInquiry[1] & 0xFF) << 8))));
//			mNChannels = bufferInquiry[6];
//			mBufferSize = bufferInquiry[7];
//			mConfigByte0 = ((long)(bufferInquiry[2] & 0xFF) +((long)(bufferInquiry[3] & 0xFF) << 8)+((long)(bufferInquiry[4] & 0xFF) << 16) +((long)(bufferInquiry[5] & 0xFF) << 24));
//			mAccelRange = ((int)(mConfigByte0 & 0xC))>>2;//XXX-RS-LSM-SensorClass?
//			mGyroRange = ((int)(mConfigByte0 & 196608))>>16;
//			mMagRange = ((int)(mConfigByte0 & 14680064))>>21;//XXX-RS-LSM-SensorClass?
//			mLSM303DigitalAccelRate = ((int)(mConfigByte0 & 0xF0))>>4;//XXX-RS-LSM-SensorClass?
//			mMPU9150GyroAccelRate = ((int)(mConfigByte0 & 65280))>>8;
//			mLSM303MagRate = ((int)(mConfigByte0 & 1835008))>>18; //XXX-RS-LSM-SensorClass?
//			mPressureResolution = (((int)(mConfigByte0 >>28)) & 3);
//			mGSRRange  = (((int)(mConfigByte0 >>25)) & 7);
//			mInternalExpPower = (((int)(mConfigByte0 >>24)) & 1);
//			mInquiryResponseBytes = new byte[8+mNChannels];
//			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
//			if ((mLSM303DigitalAccelRate==2 && getSamplingRateShimmer()>10)){
//				mLowPowerAccelWR = true;
//			}
//			if ((mMPU9150GyroAccelRate==0xFF && getSamplingRateShimmer()>10)){
//				mLowPowerGyro = true;
//			}
//			if ((mLSM303MagRate==4 && getSamplingRateShimmer()>10)){
//				mLowPowerMag = true;
//			}
//			byte[] signalIdArray = new byte[mNChannels];
//			System.arraycopy(bufferInquiry, 8, signalIdArray, 0, mNChannels);
//			updateEnabledSensorsFromChannels(signalIdArray);
//			interpretDataPacketFormat(mNChannels,signalIdArray);
//			checkExgResolutionFromEnabledSensorsVar();
//		} 
//		else if (getHardwareVersion()==HW_ID.SHIMMER_SR30) {
//			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[2]*2; 
//			setSamplingRateShimmer((double)1024/bufferInquiry[0]);
//			mAccelRange = bufferInquiry[1];
//			mNChannels = bufferInquiry[2];
//			mBufferSize = bufferInquiry[3];
//			byte[] signalIdArray = new byte[mNChannels];
//			System.arraycopy(bufferInquiry, 4, signalIdArray, 0, mNChannels); // this is 4 because there is no config byte
//			interpretDataPacketFormat(mNChannels,signalIdArray);
//		}
	}
	
	//TODO copied from ShimmerPC
	protected void eventLogAndStreamStatusChanged(int currentCommand) {
		
//		if(currentCommand==START_LOGGING_ONLY_COMMAND){
//			TODO this causing a problem Shimmer Bluetooth disconnects
//			setState(BT_STATE.SDLOGGING);
//		}
		if(currentCommand==InstructionsSet.STOP_LOGGING_ONLY_COMMAND_VALUE){
			//TODO need to query the Bluetooth connection here!
			if(isStreaming()){
				setBluetoothRadioState(BT_STATE.STREAMING);
			}
			else if(isConnected()){
				setBluetoothRadioState(BT_STATE.CONNECTED);
			}
			else{
				setBluetoothRadioState(BT_STATE.DISCONNECTED);
			}
		}
		else{
			if(isStreaming() && isSDLogging()){
				setBluetoothRadioState(BT_STATE.STREAMING_AND_SDLOGGING);
			}
			else if(isStreaming()){
				setBluetoothRadioState(BT_STATE.STREAMING);
			}
			else if(isSDLogging()){
				setBluetoothRadioState(BT_STATE.SDLOGGING);
			}
			else{
//				if(!isStreaming() && !isSDLogging() && isConnected()){
				if(!mIsStreaming && !mIsSDLogging && isConnected() && mBluetoothRadioState!=BT_STATE.CONNECTED){
					setBluetoothRadioState(BT_STATE.CONNECTED);	
				}
//				if(getBTState() == BT_STATE.INITIALISED){
//					
//				}
//				else if(getBTState() != BT_STATE.CONNECTED){
//					setState(BT_STATE.CONNECTED);
//				}
				
//				CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, mBluetoothRadioState, getMacId(), getComPort());
//				sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
			}
		}
		
	}

	
	private void processFirmwareVerResponse() {
		if(getHardwareVersion()==HW_ID.SHIMMER_2R){
//			initializeShimmer2R();
		} 
		else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
//			initializeShimmer3();
		}
		else if(getHardwareVersion()==HW_ID.SHIMMER_4_SDK) {
			initializeShimmer4();
		}
		
		mCommsProtocolRadio.startTimerCheckIfAlive();
	}

	
	private void initializeShimmer4() {
		initialise(HW_ID.SHIMMER_4_SDK);
		setHaveAttemptedToReadConfig(true);
		
		if(mSendProgressReport){
			operationPrepare();
			setBluetoothRadioState(BT_STATE.CONNECTING);
		}
		
//		if(this.mUseInfoMemConfigMethod && getFirmwareVersionCode()>=6){
			readConfigurationFromInfoMem();
			mCommsProtocolRadio.readPressureCalibrationCoefficients();
//		}
//		else {
//			readSamplingRate();
//			readMagRange();
//			readAccelRange();
//			readGyroRange();
//			readAccelSamplingRate();
//			readCalibrationParameters("All");
//			readPressureCalibrationCoefficients();
//			readEXGConfigurations();
//			//enableLowPowerMag(mLowPowerMag);
//			
//			readDerivedChannelBytes();
//			
//			if(isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 5, 2)){
//				readTrial();
//				readConfigTime();
//				readShimmerName();
//				readExperimentName();
//			}
//		}
		
		mCommsProtocolRadio.readExpansionBoardID();
		mCommsProtocolRadio.readLEDCommand();

		if((isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 5, 2))
				||(isThisVerCompatibleWith(HW_ID.SHIMMER_4_SDK, FW_ID.SHIMMER4_SDK_STOCK, 0, 0, 1))){
			mCommsProtocolRadio.readStatusLogAndStream();
		}
		
		if((isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 5, 9))
				||(isThisVerCompatibleWith(HW_ID.SHIMMER_4_SDK, FW_ID.SHIMMER4_SDK_STOCK, 0, 0, 1))){
			mCommsProtocolRadio.readBattery();
		}
		
//		if(mSetupDevice){
//			//writeAccelRange(mDigitalAccelRange);
//			if(mSetupEXG){
//				writeEXGConfiguration();
//				mSetupEXG = false;
//			}
//			writeGSRRange(mGSRRange);
//			writeAccelRange(mAccelRange);
//			writeGyroRange(mGyroRange);
//			writeMagRange(mMagRange);
//			writeShimmerAndSensorsSamplingRate(getSamplingRateShimmer());	
//			writeInternalExpPower(1);
////			setContinuousSync(mContinousSync);
//			writeEnabledSensors(mSetEnabledSensors); //this should always be the last command
//		} 
//		else {
		mCommsProtocolRadio.inquiry();
//		}

		if(mSendProgressReport){
			// Just unlock instruction stack and leave logAndStream timer as
			// this is handled in the next step, i.e., no need for
			// operationStart() here
			startOperation(BT_STATE.CONNECTING, mCommsProtocolRadio.mRadioProtocol.getListofInstructions().size());
			
			mCommsProtocolRadio.mRadioProtocol.setInstructionStackLock(false);
		}
		
		mCommsProtocolRadio.startTimerReadStatus();	// if shimmer is using LogAndStream FW, read its status periodically
		mCommsProtocolRadio.startTimerReadBattStatus(); // if shimmer is using LogAndStream FW, read its status periodically
	}

	//TODO - Copied from ShimmerObject
	private void initialise(int hardwareVersion) {
		this.setHardwareVersion(hardwareVersion);
		sensorAndConfigMapsCreate();
	}

	//TODO - Copied from ShimmerPC
	protected void hasStopStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
				// Do something here
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STOP_STREAMING, getMacId(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		mCommsProtocolRadio.startTimerReadStatus();
		setBluetoothRadioState(BT_STATE.CONNECTED);
	}
	
	//TODO - Copied from ShimmerPC
	protected void isNowStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
		// Do something here
		
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_START_STREAMING, getMacId(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
		if (isSDLogging()){
			setBluetoothRadioState(BT_STATE.STREAMING_AND_SDLOGGING);
		} else {
			setBluetoothRadioState(BT_STATE.STREAMING);
		}
	}

	public void setSetting(long sensorID, String componentName, Object valueToSet, COMMUNICATION_TYPE commType){
		ActionSetting actionSetting = mMapOfSensorClasses.get(sensorID).setSettings(componentName, valueToSet, commType);
		if (actionSetting.mCommType == COMMUNICATION_TYPE.BLUETOOTH){
			//mShimmerRadio.actionSettingResolver(actionSetting);
		}
	}
	
	public void toggleLed() {
		mCommsProtocolRadio.toggleLed();
	}

	//TODO copied from ShimmerPC
	protected void dockedStateChange() {
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE, getMacId(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE, callBackObject);
	}


	//TODO copied from ShimmerPC
	@Override
	protected void setBluetoothRadioState(BT_STATE state){
		super.setBluetoothRadioState(state);

//		if (mBluetoothRadioState.equals(BT_STATE.CONNECTED)){
//			setIsConnected(true);
//			setIsStreaming(false);
//		} 
//		else if (mBluetoothRadioState.equals(BT_STATE.DISCONNECTED)){
//			setIsConnected(false);
//			setIsStreaming(false);
//			setIsInitialised(false);
//		} 
//		else if (mBluetoothRadioState.equals(BT_STATE.STREAMING)){
//			setIsStreaming(true);
//		} 
//		else if (mBluetoothRadioState.equals(BT_STATE.CONNECTING)){
//			setIsConnected(true);
//			setIsInitialised(false);
////			startOperation(BT_STATE.CONNECTING,mNumberOfInfoMemReadsRequired);
//		}

		if(mBluetoothRadioState==BT_STATE.CONNECTED){
			setIsInitialised(true);
			setIsStreaming(false);
		}
		else if(mBluetoothRadioState==BT_STATE.STREAMING){
			setIsStreaming(true);
		}		
		else if((mBluetoothRadioState==BT_STATE.DISCONNECTED)
				||(mBluetoothRadioState==BT_STATE.CONNECTION_LOST)
				||(mBluetoothRadioState==BT_STATE.CONNECTION_FAILED)){
			setIsConnected(false);
			setIsStreaming(false);
			setIsInitialised(false);
		}

		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, mBluetoothRadioState, getMacIdFromUart(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
	}
	

	protected void inquiryDone() {
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, mBluetoothRadioState, getMacIdFromUart(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
		isReadyForStreaming();
	}

	/**
	 * @return the mShimmerRadioHWLiteProtocol
	 */
	public CommsProtocolRadio getShimmerRadioHWLiteProtocol() {
		return mCommsProtocolRadio;
	}

	/**
	 * @param commsProtocolRadio the mShimmerRadioHWLiteProtocol to set
	 */
	public void setCommsProtocolRadio(CommsProtocolRadio commsProtocolRadio) {
		this.mCommsProtocolRadio = commsProtocolRadio;
	}
	
	@Override
	public void disconnect() throws DeviceException {
		super.disconnect();
		if(mCommsProtocolRadio!=null){
			try {
				mCommsProtocolRadio.disconnect();
				mCommsProtocolRadio = null;
			} catch (DeviceException e) {
				throw(e);
			}
		}
	}

	public void writeConfigurationToInfoMem(byte[] shimmerInfoMemBytes) {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.writeInfoMem(mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS, shimmerInfoMemBytes, mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
			readConfigurationFromInfoMem();
		}
	}

	public void readConfigurationFromInfoMem(){
		if(this.getFirmwareVersionCode()>=6){
			createInfoMemLayoutObjectIfNeeded();
//			int size = InfoMemLayoutShimmer3.calculateInfoMemByteLength(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
			int size = mInfoMemLayout.calculateInfoMemByteLength(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
			mCommsProtocolRadio.readInfoMem(mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS, size, mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
		}
	}
	
	public void writeCalibrationDump(byte[] calibDump) {
		mCommsProtocolRadio.writeCalibrationDump(calibDump);
	}
	
	public void readCalibrationDump() {
//		mCommsProtocolRadio.readCalibrationDump();
	}

	
	public void writeEnabledSensors(long enabledSensors) {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
//			mShimmerRadioHWLiteProtocol.
			mCommsProtocolRadio.writeEnabledSensors(enabledSensors);
		}
	}
	
	public void inquiry() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
			mCommsProtocolRadio.inquiry();
		}
	}

	public void startSDLogging() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
			mCommsProtocolRadio.startSDLogging();
		}
	}

	public void stopSDLogging() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
			mCommsProtocolRadio.stopSDLogging();
		}
	}

	// ----------------- BT LiteProtocolInstructionSet End ------------------

	protected void dataHandler(ObjectCluster ojc) {
		
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL, getMacId(), mComPort, getPacketReceptionRateOverall());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL, callBackObject);
		
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
	}
	

	protected void sendProgressReport(BluetoothProgressReportPerCmd pRPC) {
		if(progressReportPerDevice!=null){
			progressReportPerDevice.updateProgress(pRPC);
			int progress = progressReportPerDevice.mProgressPercentageComplete;
			
			CallbackObject callBackObject = new CallbackObject(mBluetoothRadioState, getMacId(), getComPort(), progressReportPerDevice.deepClone());
			sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
			
			consolePrintLn("MAC:\t" + getMacId()
					+ "\tCOM:\t" + getComPort()
					+ "\tProgressCounter" + progressReportPerDevice.mProgressCounter 
					+ "\tProgressEndValue " + progressReportPerDevice.mProgressEndValue);
			
			if(progressReportPerDevice.mCurrentOperationBtState==BT_STATE.CONNECTING){
				if(progressReportPerDevice.mProgressCounter==progressReportPerDevice.mProgressEndValue){
					isReadyForStreaming();
				}
			}
		}
	}
	
	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds){
		consolePrintLn(currentOperation + " START");

		progressReportPerDevice = new BluetoothProgressReportPerDevice(this, currentOperation, totalNumOfCmds);
		progressReportPerDevice.mOperationState = BluetoothProgressReportPerDevice.OperationState.INPROGRESS;
		
		CallbackObject callBackObject = new CallbackObject(mBluetoothRadioState, this.getMacId(), getComPort(), progressReportPerDevice.deepClone());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
	}
	
	public void finishOperation(BT_STATE btState){
		if(progressReportPerDevice!=null){
			consolePrintLn("CURRENT OPERATION " + progressReportPerDevice.mCurrentOperationBtState + "\tFINISHED:" + btState);
			
			if(progressReportPerDevice.mCurrentOperationBtState == btState){

				progressReportPerDevice.finishOperation();
				progressReportPerDevice.mOperationState = BluetoothProgressReportPerDevice.OperationState.SUCCESS;
				//JC: moved operationFinished to is ready for streaming, seems to be called before the inquiry response is received
				CallbackObject callBackObject = new CallbackObject(mBluetoothRadioState, getMacId(), getComPort(), progressReportPerDevice.deepClone());
				sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
				
				progressReportPerDevice = null;
			}
		}

	}
	
	//TODO copied from ShimmerPC
	public void isReadyForStreaming(){
//		mIsInitialised = true;
//		if (getBluetoothRadioState()==BT_STATE.CONNECTING){
//			finishOperation(progressReportPerDevice.mCurrentOperationBtState);
//		}
//		CallbackObject callBackObject2 = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, mMacIdFromUart, getComPort());
//		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject2);
//		if (getBluetoothRadioState()==BT_STATE.CONNECTING){
//			setBluetoothRadioState(BT_STATE.CONNECTED);
//		}
		
		// Send msg fully initialized, send notification message,  
		// Do something here
        setIsInitialised(true);
//        prepareAllAfterConfigRead();

        if (mSendProgressReport){
        	finishOperation(BT_STATE.CONNECTING);
        }
        
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, getMacId(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
		restartTimersIfNull();
		setBluetoothRadioState(BT_STATE.CONNECTED);
	}


	private void restartTimersIfNull() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null){
			mCommsProtocolRadio.mRadioProtocol.restartTimersIfNull();
		}
	}
	

	@Override
	public void calculatePacketReceptionRateCurrent(int intervalMs) {

		AbstractSensor abstractSensor = mMapOfSensorClasses.get(AbstractSensor.SENSORS.CLOCK);
		if(abstractSensor!=null && abstractSensor instanceof ShimmerClock){
			ShimmerClock shimmerClock = (ShimmerClock)abstractSensor;
			mPacketReceptionRateCurrent = shimmerClock.calculatePacketReceptionRateCurrent(intervalMs);
			CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, getMacId(), getComPort(), mPacketReceptionRateCurrent);
			sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, callBackObject);
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
	
	/**
	 * @return the mButtonStart
	 */
	public boolean isButtonStart() {
		return mButtonStart;
	}


	/**
	 * @param state the mButtonStart state to set
	 */
	public void setButtonStart(boolean state) {
		mButtonStart = state;
	}
	
	@Override
	public void generateConfigOptionsMap() {
		super.generateConfigOptionsMap();
		
		mConfigOptionsMap.putAll(Configuration.Shimmer4.mConfigOptionsMapRef);
	}
	
	@Override
	public Object setConfigValueUsingConfigLabel(String groupName, String componentName, Object valueToSet) {
		Object returnValue = null;
		int buf = 0;

		switch(componentName){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START):
				setButtonStart((boolean)valueToSet);
				break;
//Integers
//Strings
	        default:
	        	returnValue = super.setConfigValueUsingConfigLabel(groupName, componentName, valueToSet);
	        	break;
		}
	
		return returnValue;
	}
	
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		switch(componentName){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START):
				returnValue = isButtonStart();
				break;
//Integers
//Strings
	        default:
	        	returnValue = super.getConfigValueUsingConfigLabel(componentName);
	        	break;
		}
		
		return returnValue;

	}

	public boolean isReadyToConnect() {
		if (mCommsProtocolRadio==null 
				||mCommsProtocolRadio.mRadioHal==null
				||!mCommsProtocolRadio.mRadioHal.isConnected()){
			return true;
		}
		return false;
	}

	//TODO TEMP here to sync booleans in ShimmerDevice with mCommsProtocolRadio
	@Override
	public void setIsDocked(boolean state) {
		super.setIsDocked(state);
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null && mCommsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(mCommsProtocolRadio.mRadioProtocol)).mIsDocked = state;
		}
	}

	@Override
	public void setIsInitialised(boolean state) {
		super.setIsInitialised(state);
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null && mCommsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(mCommsProtocolRadio.mRadioProtocol)).mIsInitialised = state;
		}
	}

	@Override
	public void setIsSensing(boolean state) {
		super.setIsSensing(state);
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null && mCommsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(mCommsProtocolRadio.mRadioProtocol)).mIsSensing = state;
		}
	}

	@Override
	public void setIsStreaming(boolean state) {
		super.setIsStreaming(state);
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null && mCommsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(mCommsProtocolRadio.mRadioProtocol)).mIsStreaming = state;
		}
	}
	
	@Override
	public void setIsSDLogging(boolean state) {
		super.setIsSDLogging(state);
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null && mCommsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(mCommsProtocolRadio.mRadioProtocol)).mIsSDLogging = state;
		}
	}
	
	@Override
	public void setHaveAttemptedToReadConfig(boolean state) {
		super.setHaveAttemptedToReadConfig(state);
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioProtocol!=null && mCommsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(mCommsProtocolRadio.mRadioProtocol)).mHaveAttemptedToReadConfig = state;
		}
	}
	
	@Override
	public boolean isConnected() {
		boolean isConnected = false;
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.isConnected()){
			isConnected = true;
		}
		setIsConnected(isConnected);
		return isConnected;
	}

	//TODO copied from ShimmerBluetooth
	public void operationPrepare() {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.operationPrepare();
		}
	}

	//TODO copied from ShimmerBluetooth
	public void operationStart(BT_STATE configuring) {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.operationStart(configuring);
		}
	}

	//TODO copied from ShimmerBluetooth
	public void setSendProgressReport(boolean state) {
		mSendProgressReport = state;
	}

	
}
