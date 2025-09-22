package com.shimmerresearch.driver.shimmer4sdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.bluetooth.ShimmerDeviceCommsProtocolAdaptor;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsResponse;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsSet;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerDeviceCallbackAdapter;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.SensorSTC3100Details;
import com.shimmerresearch.sensors.SensorADC;
import com.shimmerresearch.sensors.SensorBattVoltage;
import com.shimmerresearch.sensors.SensorBridgeAmp;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.bmpX80.SensorBMP180;
import com.shimmerresearch.sensors.bmpX80.SensorBMP280;
import com.shimmerresearch.sensors.kionix.SensorKionixKXRB52042;
import com.shimmerresearch.sensors.lsm303.SensorLSM303DLHC;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9250;
import com.shimmerresearch.sensors.SensorPPG;
import com.shimmerresearch.sensors.SensorSTC3100;
import com.shimmerresearch.sensors.SensorShimmerClock;

public class Shimmer4sdk extends ShimmerDevice {
	
	private static final long serialVersionUID = 6916261534384275804L;
	
	public BluetoothProgressReportPerDevice progressReportPerDevice;
	
	@Deprecated //assume always true?
	protected boolean mSendProgressReport = true;

	//TODO consider where to handle the below -> carried over from ShimmerObject
	protected boolean mButtonStart = true;
	protected boolean mDisableBluetooth = true;
	protected boolean mShowRtcErrorLeds = true;
	protected boolean mConfigFileCreationFlag = true;
	protected boolean mCalibFileCreationFlag = false;
	private boolean isOverrideShowRwcErrorLeds = true;
	
	protected int mBluetoothBaudRate=9; //460800
	
	//TODO migrate to using below
	protected transient ShimmerDeviceCallbackAdapter mDeviceCallbackAdapter = new ShimmerDeviceCallbackAdapter(this);
	private transient ShimmerDeviceCommsProtocolAdaptor mShimmerDeviceCommsProtocolAdaptor = new ShimmerDeviceCommsProtocolAdaptor(this);

	public Shimmer4sdk() {
		super();
	}
	
	public Shimmer4sdk(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE communicationType) {
		this();
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(communicationType);
    	setSamplingRateShimmer(communicationType, 128);
    	setMacIdFromUart(macId);
    	
    	//TODO HACK!!!!!
    	setSamplingRateShimmer(COMMUNICATION_TYPE.SD, 128.0);
    	setSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH, 128.0);
	}
	
	@Override
	public void setDefaultShimmerConfiguration() {
		super.setDefaultShimmerConfiguration();
		
		mInternalExpPower=0;
	}

	
	@Override
	public void sensorAndConfigMapsCreate() {
		createMapOfSensorClasses();
		super.sensorAndConfigMapsCreateCommon();
	}
	
	private void createMapOfSensorClasses() {
		mMapOfSensorClasses = new LinkedHashMap<SENSORS, AbstractSensor>();

		//Example code
//		if(UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
//				HW_ID.SHIMMER_4_SDK, FW_ID.LOGANDSTREAM, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION)){
//			putSensorClass(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStamp(mShimmerVerObject));
//		}
		
		addSensorClass(SENSORS.CLOCK, new SensorShimmerClock(this));
		
		addSensorClass(SENSORS.KIONIXKXRB52042, new SensorKionixKXRB52042(mShimmerVerObject));
		addSensorClass(SENSORS.LSM303, new SensorLSM303DLHC(this));
		addSensorClass(SENSORS.MPU9X50, new SensorMPU9250(this));
		addSensorClass(SENSORS.ADC, new SensorADC(mShimmerVerObject));
		addSensorClass(SENSORS.Battery, new SensorBattVoltage(this));
		addSensorClass(SENSORS.Bridge_Amplifier, new SensorBridgeAmp(mShimmerVerObject));
		
		//TODO push version checking into the sensor classes
		
		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_EXG 
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			addSensorClass(SENSORS.EXG, new SensorEXG(this));
		}

		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			addSensorClass(SENSORS.GSR, new SensorGSR(mShimmerVerObject));
		}

		if(getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED
				|| getExpansionBoardId()==HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE
				|| getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			if(isSupportedDerivedSensors()){
				addSensorClass(SENSORS.PPG, new SensorPPG(this));
			}
		}

		//Shimmer4 enhancements - Only available on the SDK
		if(getExpansionBoardId()==HW_ID_SR_CODES.SHIMMER_4_SDK){
			addSensorClass(SENSORS.BMP280, new SensorBMP280(mShimmerVerObject));
			addSensorClass(SENSORS.STC3100, new SensorSTC3100(mShimmerVerObject));
		}
		else{
			addSensorClass(SENSORS.BMP180, new SensorBMP180(mShimmerVerObject));
		}
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		//TODO don't think this is relevant for Shimmer4
	}

	@Override
	public void createConfigBytesLayout() {
		//TODO replace with Shimmer4?
		if (mShimmerVerObject.mHardwareVersion==HW_ID.UNKNOWN) {
			mConfigByteLayout = new ConfigByteLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(), HW_ID.SHIMMER_4_SDK);
		} else {
			mConfigByteLayout = new ConfigByteLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(), mShimmerVerObject.mHardwareVersion);
		}
	}

	// TODO need to move common infomem related activity to ShimmerDevice. Not
	// have duplicates in ShimmerObject, ShimmerGQ and Shimmer4. Some items only
	// copied here for example/testing purposes
	@Override
	public void configBytesParse(byte[] configBytes, COMMUNICATION_TYPE commType) {
		String shimmerName = "";
		mInfoMemBytesOriginal = configBytes;

		if(!ConfigByteLayout.checkConfigBytesValid(configBytes)){
			// InfoMem not valid
			setDefaultShimmerConfiguration();
//			mShimmerUsingConfigFromInfoMem = false;

//			mShimmerInfoMemBytes = infoMemByteArrayGenerate();
//			mShimmerInfoMemBytes = new byte[infoMemContents.length];
			mConfigBytes = configBytes;
		}
		else {
			createInfoMemLayoutObjectIfNeeded();
			//TODO create for Shimmer4 or use Shimmer3?
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) mConfigByteLayout;
			mConfigBytes = configBytes;

			// Parse Enabled and Derived sensor bytes in order to update sensor maps
			parseEnabledDerivedSensorsForMaps(configByteLayoutCast, configBytes);

			overwriteEnabledSensors();

			setSamplingRateShimmer(getSamplingClockFreq()/(double)((int)(configBytes[configByteLayoutCast.idxShimmerSamplingRate] & configByteLayoutCast.maskShimmerSamplingRate) 
					+ ((int)(configBytes[configByteLayoutCast.idxShimmerSamplingRate+1] & configByteLayoutCast.maskShimmerSamplingRate) << 8)));

			mInternalExpPower = (configBytes[configByteLayoutCast.idxConfigSetupByte3] >> configByteLayoutCast.bitShiftEXPPowerEnable) & configByteLayoutCast.maskEXPPowerEnable;


			mBluetoothBaudRate = configBytes[configByteLayoutCast.idxBtCommBaudRate] & configByteLayoutCast.maskBaudRate;

			
			// Shimmer Name
			byte[] shimmerNameBuffer = new byte[configByteLayoutCast.lengthShimmerName];
			System.arraycopy(configBytes, configByteLayoutCast.idxSDShimmerName, shimmerNameBuffer, 0 , configByteLayoutCast.lengthShimmerName);
			for(byte b : shimmerNameBuffer) {
				if(!UtilShimmer.isAsciiPrintable((char)b)) {
					break;
				}
				shimmerName += (char)b;
			}
			
			// Experiment Name
			byte[] experimentNameBuffer = new byte[configByteLayoutCast.lengthExperimentName];
			System.arraycopy(configBytes, configByteLayoutCast.idxSDEXPIDName, experimentNameBuffer, 0 , configByteLayoutCast.lengthExperimentName);
			String experimentName = "";
			for(byte b : experimentNameBuffer) {
				if(!UtilShimmer.isAsciiPrintable((char)b)) {
					break;
				}
				experimentName += (char)b;
			}
			mTrialName = new String(experimentName);

			//Configuration Time
			int bitShift = (configByteLayoutCast.lengthConfigTimeBytes-1) * 8;
			mConfigTime = 0;
			for(int x=0; x<configByteLayoutCast.lengthConfigTimeBytes; x++ ) {
				mConfigTime += (((long)(configBytes[configByteLayoutCast.idxSDConfigTime0+x] & 0xFF)) << bitShift);
				bitShift -= 8;
			}
			
			mButtonStart = ((configBytes[configByteLayoutCast.idxSDExperimentConfig0] >> configByteLayoutCast.bitShiftButtonStart) & configByteLayoutCast.maskButtonStart)>0? true:false;
			mDisableBluetooth = ((configBytes[configByteLayoutCast.idxSDExperimentConfig0] >> configByteLayoutCast.bitShiftDisableBluetooth) & configByteLayoutCast.maskDisableBluetooth)>0? true:false;
			mShowRtcErrorLeds = ((configBytes[configByteLayoutCast.idxSDExperimentConfig0] >> configByteLayoutCast.bitShiftShowErrorLedsRwc) & configByteLayoutCast.maskShowErrorLedsRwc)>0? true:false;

			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.configBytesParse(this, mConfigBytes, commType);
			}
			
			//need to update parser map here as ExG config bytes change which of ECG/EMG/Resp etc. is enabled
			generateParserMap();

		}
		checkAndCorrectShimmerName(shimmerName);
	}

	private void parseEnabledDerivedSensorsForMaps(ConfigByteLayoutShimmer3 infoMemLayoutCast, byte[] configBytes) {
		// Sensors
		mEnabledSensors = ((long)configBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
		mEnabledSensors += ((long)configBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
		mEnabledSensors += ((long)configBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;

		mEnabledSensors += ((long)configBytes[infoMemLayoutCast.idxSensors3] & 0xFF) << infoMemLayoutCast.bitShiftSensors3;
		mEnabledSensors += ((long)configBytes[infoMemLayoutCast.idxSensors4] & 0xFF) << infoMemLayoutCast.bitShiftSensors4;
		
		mDerivedSensors = (long)0;
		// Check if compatible and not equal to 0xFF
		if((infoMemLayoutCast.idxDerivedSensors0>0) && (configBytes[infoMemLayoutCast.idxDerivedSensors0]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)
				&& (infoMemLayoutCast.idxDerivedSensors1>0) && (configBytes[infoMemLayoutCast.idxDerivedSensors1]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)) { 
			
			mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors0] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors0;
			mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors1] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors1;
			
			// Check if compatible and not equal to 0xFF
			// RM commented out the below check sept 2016 as infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]  can be 0xFF if all 6DoF and 9DoF algorithms are enabled
			//if((infoMemLayoutCast.idxDerivedSensors2>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)){
			if(infoMemLayoutCast.idxDerivedSensors2>0){
				mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors2] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors2;
			}
			
			if(mShimmerVerObject.isSupportedEightByteDerivedSensors()){
				mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors3] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors3;
				mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors4] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors4;
				mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors5] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors5;
				mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors6] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors6;
				mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors7] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors7;
			}

		}

		setEnabledAndDerivedSensorsAndUpdateMaps(mEnabledSensors, mDerivedSensors);
	}

	private void overwriteEnabledSensors() {
		//Overrides - needed because there are no enabled/derived sensor bits for these
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.HOST_SHIMMER_STREAMING_PROPERTIES, true);

		//TODO
//		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP, true, COMMUNICATION_TYPE.SD);
//		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_TIMESTAMP, true, COMMUNICATION_TYPE.SD);

//		updateExpectedDataPacketSize();
		
		//for testing
//		printSensorAndParserMaps();
		//for testing
//		printMapOfConfigForDb();
	}

	// TODO need to move common infomem related activity to ShimmerDevice. Not
	// have duplicates in ShimmerObject, ShimmerGQ and Shimmer4. Some items only
	// copied here for example/testing purposes
	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer, COMMUNICATION_TYPE commType) {
		//TODO refer to same method in ShimmerGQ/ShimmerObject

		//TODO create for Shimmer4 or use Shimmer3?
		ConfigByteLayoutShimmer3 infoMemLayout = new ConfigByteLayoutShimmer3(
				getFirmwareIdentifier(), 
				getFirmwareVersionMajor(), 
				getFirmwareVersionMinor(), 
				getFirmwareVersionInternal(), HW_ID.SHIMMER_4_SDK);
		if (mShimmerVerObject.mHardwareVersion==HW_ID.UNKNOWN) {
			
		} else {
			infoMemLayout = new ConfigByteLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(), mShimmerVerObject.mHardwareVersion);
		}
		
		
		
//		byte[] infoMemBackup = mInfoMemBytes.clone();
		mConfigBytes = infoMemLayout.createConfigByteArrayEmpty();
		
		refreshEnabledSensorsFromSensorMap();
		
		int samplingRate = (int)(getSamplingClockFreq() / getSamplingRateShimmer());
		mConfigBytes[infoMemLayout.idxShimmerSamplingRate] = (byte) (samplingRate & infoMemLayout.maskShimmerSamplingRate); 
		mConfigBytes[infoMemLayout.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8) & infoMemLayout.maskShimmerSamplingRate); 

		
		mConfigBytes[infoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors0) & infoMemLayout.maskSensors);
		mConfigBytes[infoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors1) & infoMemLayout.maskSensors);
		mConfigBytes[infoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors2) & infoMemLayout.maskSensors);

		mConfigBytes[infoMemLayout.idxSensors3] = (byte) ((mEnabledSensors >> infoMemLayout.bitShiftSensors3) & 0xFF);
		mConfigBytes[infoMemLayout.idxSensors4] = (byte) ((mEnabledSensors >> infoMemLayout.bitShiftSensors4) & 0xFF);

		mConfigBytes[infoMemLayout.idxConfigSetupByte0] = (byte) (0x00);
		mConfigBytes[infoMemLayout.idxConfigSetupByte1] = (byte) (0x00);
		mConfigBytes[infoMemLayout.idxConfigSetupByte2] = (byte) (0x00);
		mConfigBytes[infoMemLayout.idxConfigSetupByte3] = (byte) (0x00);
		
		checkIfInternalExpBrdPowerIsNeeded();
		mConfigBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & infoMemLayout.maskEXPPowerEnable) << infoMemLayout.bitShiftEXPPowerEnable);
		
		// Derived Sensors
		if((infoMemLayout.idxDerivedSensors0>0)&&(infoMemLayout.idxDerivedSensors1>0)) { // Check if compatible
			mConfigBytes[infoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors0) & infoMemLayout.maskDerivedChannelsByte);
			mConfigBytes[infoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors1) & infoMemLayout.maskDerivedChannelsByte);
			if(infoMemLayout.idxDerivedSensors2>0) { // Check if compatible
				mConfigBytes[infoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors2) & infoMemLayout.maskDerivedChannelsByte);
			}
			
			if(mShimmerVerObject.isSupportedEightByteDerivedSensors()){
				mConfigBytes[infoMemLayout.idxDerivedSensors3] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors3) & infoMemLayout.maskDerivedChannelsByte);
				mConfigBytes[infoMemLayout.idxDerivedSensors4] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors4) & infoMemLayout.maskDerivedChannelsByte);
				mConfigBytes[infoMemLayout.idxDerivedSensors5] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors5) & infoMemLayout.maskDerivedChannelsByte);
				mConfigBytes[infoMemLayout.idxDerivedSensors6] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors6) & infoMemLayout.maskDerivedChannelsByte);
				mConfigBytes[infoMemLayout.idxDerivedSensors7] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors7) & infoMemLayout.maskDerivedChannelsByte);
			}
		}
		
		//TODO handle the below better
		mBluetoothBaudRate = 9;
		mConfigBytes[infoMemLayout.idxBtCommBaudRate] = (byte) (mBluetoothBaudRate & infoMemLayout.maskBaudRate);

		// Configuration from each Sensor settings
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			abstractSensor.configBytesGenerate(this, mConfigBytes, commType);
		}

		// Shimmer Name
		for (int i = 0; i < infoMemLayout.lengthShimmerName; i++) {
			if (i < mShimmerUserAssignedName.length()) {
				mConfigBytes[infoMemLayout.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
			}
			else {
				mConfigBytes[infoMemLayout.idxSDShimmerName + i] = (byte) 0xFF;
			}
		}
		
		// Experiment Name
		for (int i = 0; i < infoMemLayout.lengthExperimentName; i++) {
			if (i < mTrialName.length()) {
				mConfigBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) mTrialName.charAt(i);
			}
			else {
				mConfigBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) 0xFF;
			}
		}

		//Configuration Time
		mConfigBytes[infoMemLayout.idxSDConfigTime0] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime0) & 0xFF);
		mConfigBytes[infoMemLayout.idxSDConfigTime1] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime1) & 0xFF);
		mConfigBytes[infoMemLayout.idxSDConfigTime2] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime2) & 0xFF);
		mConfigBytes[infoMemLayout.idxSDConfigTime3] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime3) & 0xFF);

		
		mConfigBytes[infoMemLayout.idxSDExperimentConfig0] = (byte) ((mButtonStart? infoMemLayout.maskButtonStart:0) << infoMemLayout.bitShiftButtonStart);
		mConfigBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((mDisableBluetooth? infoMemLayout.maskDisableBluetooth:0) << infoMemLayout.bitShiftDisableBluetooth);
		if(this.isOverrideShowRwcErrorLeds){
			mConfigBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((infoMemLayout.maskShowErrorLedsRwc) << infoMemLayout.bitShiftShowErrorLedsRwc);
		}
		else {
			mConfigBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((mShowRtcErrorLeds? infoMemLayout.maskShowErrorLedsRwc:0) << infoMemLayout.bitShiftShowErrorLedsRwc);
		}

		if(generateForWritingToShimmer) {
			// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
			// (already set to 0xFF at start of method but just incase)
			System.arraycopy(infoMemLayout.invalidMacId, 0, mConfigBytes, infoMemLayout.idxMacAddress, infoMemLayout.lengthMacIdBytes);

			mConfigBytes[infoMemLayout.idxSDConfigDelayFlag] = 0;
			// Tells the Shimmer to create a new config file on undock/power cycle
			//TODO RM enabled the two lines below (MN had the below two lines commented out.. but need them to write config successfully over UART)
			byte configFileWriteBit = (byte) (mConfigFileCreationFlag? (infoMemLayout.maskSDCfgFileWriteFlag << infoMemLayout.bitShiftSDCfgFileWriteFlag):0x00);
			mConfigBytes[infoMemLayout.idxSDConfigDelayFlag] |= configFileWriteBit;

			mConfigBytes[infoMemLayout.idxSDConfigDelayFlag] |= infoMemLayout.bitShiftSDCfgFileWriteFlag;

			 // Tells the Shimmer to create a new calibration files on undock/power cycle
			byte calibFileWriteBit = (byte) (mCalibFileCreationFlag? (infoMemLayout.maskSDCalibFileWriteFlag << infoMemLayout.bitShiftSDCalibFileWriteFlag):0x00);
			mConfigBytes[infoMemLayout.idxSDConfigDelayFlag] |= calibFileWriteBit;
		}
		
		if(generateForWritingToShimmer) {
			// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
			// (already set to 0xFF at start of method but just in case)
			System.arraycopy(infoMemLayout.invalidMacId, 0, mConfigBytes, infoMemLayout.idxMacAddress, infoMemLayout.lengthMacIdBytes);

			//TODO only temporarily here to deal with fake Shimmer4 (i.e., a Shimmer3)
			mConfigBytes[infoMemLayout.idxSDConfigDelayFlag] |= infoMemLayout.bitShiftSDCfgFileWriteFlag;
		}
		
		
		return mConfigBytes;
	}
	
	@Override
	public Shimmer4sdk deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Shimmer4sdk) ois.readObject();
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
	/**
	 * @param commsRadio
	 */
	@Override
//	public void setRadio(AbstractSerialPortHal commsProtocolRadio) {
	public void setCommsProtocolRadio(CommsProtocolRadio commsProtocolRadio){
		super.setCommsProtocolRadio(commsProtocolRadio);
		initializeRadio();
	}

	/**
	 * 
	 */
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
					try {
						disconnect();
					} catch (ShimmerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					
//					setBluetoothRadioState(BT_STATE.DISCONNECTED);
//					mCommsProtocolRadio = null;
////					try {
////						clearCommsProtocolRadio();
////					} catch (DeviceException e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
				}
	
				@Override
				public void eventNewPacket(byte[] packetByteArray, long pcTimestamp) {
					buildMsg(packetByteArray, COMMUNICATION_TYPE.BLUETOOTH, false, pcTimestamp);
				}

				@Override
				public void eventAckReceived(int lastSentInstruction) {
					//Handled in LiteProtocol
				}

				@Override
				public void eventNewResponse(byte[] responseBytes) {
					//Handled in LiteProtocol
				}
	
				@Override
				public void eventResponseReceived(int responseCommand, Object parsedResponse) {
					if(responseCommand==InstructionsResponse.INQUIRY_RESPONSE_VALUE){ 
//						interpretInqResponse((byte[])parsedResponse);
						inquiryDone();
					}
					else if(responseCommand==InstructionsResponse.GET_SHIMMER_VERSION_RESPONSE_VALUE){ 
						setHardwareVersion((int)parsedResponse);
					}
					else if(responseCommand==InstructionsResponse.FW_VERSION_RESPONSE_VALUE){ 
						if(parsedResponse!=null){
							ShimmerVerObject shimmerVerObject = (ShimmerVerObject)parsedResponse;
							shimmerVerObject.setHardwareVersion(getHardwareVersion());
							setShimmerVersionObjectAndCreateSensorMap(shimmerVerObject);
						}
						else {
							initialiseDevice();
						}
					}
					else if(responseCommand==InstructionsResponse.DAUGHTER_CARD_ID_RESPONSE_VALUE){ 
						setExpansionBoardDetails((ExpansionBoardDetails)parsedResponse);
					}
					else if(responseCommand==InstructionsResponse.INFOMEM_RESPONSE_VALUE){ 
						configBytesParse((byte[])parsedResponse);
					}
					else if(responseCommand==InstructionsResponse.RSP_CALIB_DUMP_COMMAND_VALUE){
						calibByteDumpParse((byte[])parsedResponse, CALIB_READ_SOURCE.RADIO_DUMP);
					}
					else if(responseCommand==InstructionsResponse.BLINK_LED_RESPONSE_VALUE){ 
	//					mCurrentLEDStatus = byteled[0]&0xFF;
					}
					else if(responseCommand==InstructionsResponse.STATUS_RESPONSE_VALUE){ 
						consolePrintLn("STATUS RESPONSE RECEIVED");
						//Handled in LiteProtocol
	//					parseStatusByte((byte)parsedResponse);
					}
					else if(responseCommand==InstructionsResponse.VBATT_RESPONSE_VALUE){ 
						setBattStatusDetails((ShimmerBattStatusDetails)parsedResponse);
					}
					else if(responseCommand==InstructionsResponse.RWC_RESPONSE_VALUE){ 
						setLastReadRealTimeClockValue((long)parsedResponse);
					}
					else if(responseCommand==InstructionsResponse.RSP_I2C_BATT_STATUS_COMMAND_VALUE){
						SensorSTC3100Details sensorSTC3100Details = (SensorSTC3100Details)parsedResponse;
						
						consolePrintLn(sensorSTC3100Details.getDebugString());
						
						AbstractSensor sensor = getSensorClass(SENSORS.STC3100);
						if(sensor!=null && sensor instanceof SensorSTC3100){
							SensorSTC3100 sensorSTC3100 = (SensorSTC3100)sensor;
							sensorSTC3100.setStc3100Details(sensorSTC3100Details);
						}
					}
					else{
						Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
						while(iterator.hasNext()){
							AbstractSensor abstractSensor = iterator.next();
							boolean found = abstractSensor.processResponse(responseCommand, parsedResponse, COMMUNICATION_TYPE.BLUETOOTH);
							if(found){
								return;
							}
						}
						
						consolePrintLn("Unhandled Response In Shimmer4 class: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(new byte[]{(byte) responseCommand}));
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
					mCommsProtocolRadio.stopTimerReadStatus();
					mCommsProtocolRadio.readRealTimeClock();
	
					initaliseDataProcessing();
	
					resetShimmerClock();
					resetPacketLossVariables();
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
				public void eventError(ShimmerException dE) {
					if(dE!=null){
						consolePrint(dE.getErrStringFormatted());
					}
					else{
						consolePrintLn("null error from CommsProtocol");
					}
				}

				@Override
				public void isNowStreamLoggedDataCallback() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void hasStopStreamLoggedDataCallback(String binPath) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void eventNewSyncPayloadReceived(int payloadIndex, boolean crcError, double transferRateBytes, String binFilePath) {
					// TODO Auto-generated method stub
					
				}

			});
			
		}
	}
	
	@Override
	public ObjectCluster buildMsg(byte[] newPacket, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, double pcTimestampMs) {
		ObjectCluster objectCluster = super.buildMsg(newPacket, commType, isTimeSyncEnabled, pcTimestampMs);
		
//		if(commType==COMMUNICATION_TYPE.BLUETOOTH){
//			processEventMarkerCh(objectCluster);
//		}
		
		dataHandler(objectCluster);
		return objectCluster;
	}
	
	@Override
	public void connect() throws ShimmerException {
		mShimmerDeviceCommsProtocolAdaptor.connect();
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
//			setSamplingRateShimmer((getSamplingClockFreq()/(double)((int)(bufferInquiry[0] & 0xFF) + ((int)(bufferInquiry[1] & 0xFF) << 8))));
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
				if(!mIsStreaming && !isSDLogging() && isConnected() && mBluetoothRadioState!=BT_STATE.CONNECTED){
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

	
	private void initialiseDevice() {
//		initialise(HW_ID.SHIMMER_4_SDK);
		setHaveAttemptedToReadConfig(true);
		
		if(mSendProgressReport){
			operationPrepare();
			setBluetoothRadioState(BT_STATE.CONNECTING);
		}

		mCommsProtocolRadio.readExpansionBoardID();
		mCommsProtocolRadio.readLEDCommand();

//		if(this.mUseInfoMemConfigMethod && getFirmwareVersionCode()>=6){
			readConfigBytes();
			readCalibrationDump();

//			((CommsProtocolRadio)mCommsProtocolRadio).mRadioProtocol.readBattStatusPeriod();
			((CommsProtocolRadio)mCommsProtocolRadio).mRadioProtocol.writeBattStatusPeriod(1);

			//TODO improve below by putting into sensor classes
			if(mMapOfSensorClasses.containsKey(SENSORS.BMP180)){
				mCommsProtocolRadio.readPressureCalibrationCoefficients();
			}
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
		mCommsProtocolRadio.startTimerCheckIfAlive();
	}

//	//TODO - Copied from ShimmerObject
//	private void initialise(int hardwareVersion) {
//		setHardwareVersion(hardwareVersion);
//		sensorAndConfigMapsCreate();
//	}

	protected void hasStopStreaming() {
		mDeviceCallbackAdapter.hasStopStreaming();
		mCommsProtocolRadio.startTimerReadStatus();
	}
	
	protected void isNowStreaming() {
		mDeviceCallbackAdapter.isNowStreaming();
	}
	
	@Deprecated //TODO remove below? old approach?
	public void setSetting(long sensorID, String componentName, Object valueToSet, COMMUNICATION_TYPE commType){
		ActionSetting actionSetting = getSensorClass(sensorID).setSettings(componentName, valueToSet, commType);
		if (actionSetting.mCommType == COMMUNICATION_TYPE.BLUETOOTH){
			//mShimmerRadio.actionSettingResolver(actionSetting);
		}
	}
	
	//TODO copied from ShimmerPC
	protected void dockedStateChange() {
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE, getMacId(), getComPort());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE, callBackObject);
	}


	@Override
	public boolean setBluetoothRadioState(BT_STATE state){
		boolean isChanged = super.setBluetoothRadioState(state);
		mDeviceCallbackAdapter.setBluetoothRadioState(state, isChanged);
		return isChanged;
	}

	protected void inquiryDone() {
		mDeviceCallbackAdapter.inquiryDone();
		isReadyForStreaming();
	}
	
	@Override
	public void disconnect() throws ShimmerException {
		super.disconnect();
		clearCommsProtocolRadio();
		setBluetoothRadioState(BT_STATE.DISCONNECTED);
	}

	public void writeConfigBytes(byte[] shimmerInfoMemBytes) {
		if(mCommsProtocolRadio!=null && mConfigByteLayout!=null){
			mCommsProtocolRadio.writeInfoMem(mConfigByteLayout.MSP430_5XX_INFOMEM_D_ADDRESS, shimmerInfoMemBytes, mConfigByteLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
			readConfigBytes();
		}
	}

	public void readConfigBytes(){
		if(this.getFirmwareVersionCode()>=6){
			createInfoMemLayoutObjectIfNeeded();
//			int size = InfoMemLayoutShimmer3.calculateInfoMemByteLength(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
			int size = mConfigByteLayout.calculateConfigByteLength();
			mCommsProtocolRadio.readInfoMem(mConfigByteLayout.MSP430_5XX_INFOMEM_D_ADDRESS, size, mConfigByteLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
		}
	}
	
	public void toggleLed() {
		mCommsProtocolRadio.toggleLed();
	}

	public void writeCalibrationDump(byte[] calibDump) {
		mCommsProtocolRadio.writeCalibrationDump(calibDump);
	}
	
	public void readCalibrationDump() {
		mCommsProtocolRadio.readCalibrationDump();
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

	@Override
	public void startSDLogging() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
			mCommsProtocolRadio.startSDLogging();
		}
	}

	@Override
	public void stopSDLogging() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
			mCommsProtocolRadio.stopSDLogging();
		}
	}

	// ----------------- BT LiteProtocolInstructionSet End ------------------

	/**
	 * @param ojc
	 */
	protected void dataHandler(ObjectCluster ojc) {
		mDeviceCallbackAdapter.dataHandler(ojc);
	}
	
	@Override
	public void calculatePacketReceptionRateCurrent(int intervalMs) {
		super.calculatePacketReceptionRateCurrent(intervalMs);
		mDeviceCallbackAdapter.sendCallbackPacketReceptionRateCurrent();
	}

	/**
	 * @param pRPC
	 */
	protected void sendProgressReport(BluetoothProgressReportPerCmd pRPC) {
		mDeviceCallbackAdapter.sendProgressReport(pRPC);
	}
	
	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds){
		mDeviceCallbackAdapter.startOperation(currentOperation, totalNumOfCmds);
	}
	
	public void finishOperation(BT_STATE btState){
		mDeviceCallbackAdapter.finishOperation(btState);
	}
	
	//TODO copied from ShimmerPC
	// Use mDeviceCallbackAdapter.isReadyForStreaming(); instead
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
//        setIsInitialised(true);
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
	

	/**
	 * @return the mButtonStart
	 */
	public boolean isButtonStart() {
		return mButtonStart;
	}
	
	public boolean isDisableBluetooth() {
		return mDisableBluetooth;
	}

	/**
	 * @param state the mButtonStart state to set
	 */
	public void setButtonStart(boolean state) {
		mButtonStart = state;
	}
	
	public void setDisableBluetooth(boolean state) {
		mDisableBluetooth = state;
	}
	
	@Override
	public void generateConfigOptionsMap() {
		super.generateConfigOptionsMap();
		
		mConfigOptionsMapSensors.putAll(Configuration.Shimmer4.mConfigOptionsMapRef);
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap(COMMUNICATION_TYPE commType) {
		LinkedHashMap<String, Object> configMapForDb = super.generateConfigMap(commType);
		
		//TODO need to complete this for the config review in Consensys -> Manage Data
		
		return configMapForDb;
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
			case(Configuration.Shimmer3.GuiLabelConfig.SD_STREAM_WHEN_RECORDING):
				setDisableBluetooth((boolean)valueToSet);
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
			case(Configuration.Shimmer3.GuiLabelConfig.SD_STREAM_WHEN_RECORDING):
				returnValue = isDisableBluetooth();
				break;
//Integers
//Strings
	        default:
	        	returnValue = super.getConfigValueUsingConfigLabel(componentName);
	        	break;
		}
		
		return returnValue;

	}

	//TODO TEMP here to sync booleans in ShimmerDevice with mCommsProtocolRadio until we figure out a better system 
	@Override
	public boolean setIsDocked(boolean state) {
		boolean isChanged = super.setIsDocked(state);
		mShimmerDeviceCommsProtocolAdaptor.setIsDocked(state);
		return isChanged;
	}

	@Override
	public void setIsInitialised(boolean state) {
		super.setIsInitialised(state);
		mShimmerDeviceCommsProtocolAdaptor.setIsInitialised(state);
	}

	@Override
	public void setIsSensing(boolean state) {
		super.setIsSensing(state);
		mShimmerDeviceCommsProtocolAdaptor.setIsSensing(state);
	}

	@Override
	public void setIsStreaming(boolean state) {
		super.setIsStreaming(state);
		mShimmerDeviceCommsProtocolAdaptor.setIsStreaming(state);
	}
	
	@Override
	public void setIsSDLogging(boolean state) {
		super.setIsSDLogging(state);
		mShimmerDeviceCommsProtocolAdaptor.setIsSDLogging(state);
	}
	
	@Override
	public void setHaveAttemptedToReadConfig(boolean state) {
		super.setHaveAttemptedToReadConfig(state);
		mShimmerDeviceCommsProtocolAdaptor.setHaveAttemptedToReadConfig(state);
	}
	
	@Override
	public boolean isConnected() {
		boolean isConnected = mShimmerDeviceCommsProtocolAdaptor.isConnected();
		setIsConnected(isConnected);
		return isConnected;
	}
	
	@Override
	public void configureFromClone(ShimmerDevice shimmerDeviceClone) throws ShimmerException {
		super.configureFromClone(shimmerDeviceClone);
		
		writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
		writeCalibrationDump(shimmerDeviceClone.calibByteDumpGenerate());

	}
	
}
