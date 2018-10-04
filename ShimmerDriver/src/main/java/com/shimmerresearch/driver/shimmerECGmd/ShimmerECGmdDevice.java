package com.shimmerresearch.driver.shimmerECGmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerDeviceCommsProtocolAdaptor;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.AbstractCommsProtocol;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsResponse;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.SensorBattVoltage;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorShimmerClock;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.shimmerConfig.FixedShimmerConfigs.FIXED_SHIMMER_CONFIG_MODE;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerDeviceCallbackAdapter;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.exceptions.ShimmerException;

public class ShimmerECGmdDevice extends ShimmerDevice {

	private static final long serialVersionUID = -1962424117416963231L;
	
	protected transient ShimmerDeviceCallbackAdapter mDeviceCallbackAdapter = new ShimmerDeviceCallbackAdapter(this);
	private transient ShimmerDeviceCommsProtocolAdaptor mShimmerDeviceCommsProtocolAdaptor = new ShimmerDeviceCommsProtocolAdaptor(this);

	protected int mBluetoothBaudRate = 9; //460800

	private byte[] mDieRecord = new byte[8];

	public ShimmerECGmdDevice() {
		super();
	}

	public ShimmerECGmdDevice(String macAddress) {
		this();
    	setMacIdFromUart(macAddress);
	}

	public ShimmerECGmdDevice(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE communicationType) {
		this();
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(communicationType);
    	setSamplingRateShimmer(communicationType, 128);
    	setMacIdFromUart(macId);
    	
    	//TODO HACK!!!!!
    	setSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH, 128.0);
	}

	@Override
	public ShimmerDevice deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ShimmerECGmdDevice) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		mMapOfSensorClasses = new LinkedHashMap<SENSORS, AbstractSensor>();

		addSensorClass(SENSORS.CLOCK, new SensorShimmerClock(this));
		addSensorClass(SENSORS.Battery, new SensorBattVoltage(this));
		addSensorClass(SENSORS.EXG, new SensorEXG(this));
		
		super.sensorAndConfigMapsCreateCommon();
	}
	
	@Override
	public void setDefaultShimmerConfiguration() {
		super.setDefaultShimmerConfiguration();
		
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_VBATT, true);
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.HOST_ECG, true);

		setShimmerAndSensorsSamplingRate(512.0);
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		//NOT USED IN THIS CLASS
	}

	/**
	 * Parse the Shimmer's Information Memory when read through the Shimmer
	 * Dock/Consensys Base. The Information Memory is a region of the Shimmer's
	 * inbuilt RAM where all configuration information is stored.
	 * 
	 * @param configBytes
	 *            the array of InfoMem bytes.
	 */
	@Override
	public void configBytesParse(byte[] configBytes) {
		String shimmerName = "";

		mInfoMemBytesOriginal = configBytes;
		
		if(!ConfigByteLayout.checkConfigBytesValid(configBytes)){
			// InfoMem not valid
			setDefaultShimmerConfiguration();
			mConfigBytes = configBytes;
		}
		else {
			// configBytes are valid
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) mConfigByteLayout;
			mConfigBytes = configBytes;
			createInfoMemLayoutObjectIfNeeded();
			
			// Parse Enabled and Derived sensor bytes in order to update sensor maps
			parseEnabledDerivedSensorsForMaps(configByteLayoutCast, configBytes);
			
			// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
			// Sampling Rate
			byte samplingRateMSB = (byte) (configBytes[configByteLayoutCast.idxShimmerSamplingRate+1] & configByteLayoutCast.maskShimmerSamplingRate);
			byte samplingRateLSB = (byte) (configBytes[configByteLayoutCast.idxShimmerSamplingRate] & configByteLayoutCast.maskShimmerSamplingRate);
			double samplingRate = convertSamplingRateBytesToFreq(samplingRateLSB, samplingRateMSB, getSamplingClockFreq());
			setShimmerAndSensorsSamplingRate(samplingRate);
	
			mInternalExpPower = (configBytes[configByteLayoutCast.idxConfigSetupByte3] >> configByteLayoutCast.bitShiftEXPPowerEnable) & configByteLayoutCast.maskEXPPowerEnable;
			mBluetoothBaudRate = configBytes[configByteLayoutCast.idxBtCommBaudRate] & configByteLayoutCast.maskBaudRate;
			
			byte[] macIdBytes = new byte[configByteLayoutCast.lengthMacIdBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxMacAddress, macIdBytes, 0 , configByteLayoutCast.lengthMacIdBytes);
			//TODO needed for anything?
//			mMacIdFromInfoMem = UtilShimmer.bytesToHexString(macIdBytes);
				
			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.configBytesParse(this, mConfigBytes);
			}
		}
		
		printSensorParserAndAlgoMaps();
		
		checkAndCorrectShimmerName(shimmerName);
	}
	
	private void parseEnabledDerivedSensorsForMaps(ConfigByteLayoutShimmer3 infoMemLayoutCast, byte[] configBytes) {
		// Sensors
		mEnabledSensors = ((long)configBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
		mEnabledSensors += ((long)configBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
		mEnabledSensors += ((long)configBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;

		mDerivedSensors = (long)0;
		// Check if compatible and not equal to 0xFF
		if((infoMemLayoutCast.idxDerivedSensors0>0) && (configBytes[infoMemLayoutCast.idxDerivedSensors0]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)
				&& (infoMemLayoutCast.idxDerivedSensors1>0) && (configBytes[infoMemLayoutCast.idxDerivedSensors1]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)) { 
			
			mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors0] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors0;
			mDerivedSensors |= ((long)configBytes[infoMemLayoutCast.idxDerivedSensors1] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors1;
			
			// Check if compatible and not equal to 0xFF
			// RM commented out the below check sept 2016 as infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]  can be 0xFF if all 6DoF and 9DoF algorithms are enabled
			//if((infoMemLayoutCast.idxDerivedSensors2>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)){ 
			if (infoMemLayoutCast.idxDerivedSensors2 > 0) {
				mDerivedSensors |= ((long) configBytes[infoMemLayoutCast.idxDerivedSensors2] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors2;
			}
		}

		setEnabledAndDerivedSensorsAndUpdateMaps(mEnabledSensors, mDerivedSensors);
		
		overwriteEnabledSensors();
	}

	private void overwriteEnabledSensors() {
		//Overrides - needed because there are no enabled/derived sensor bits for these
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_TIMESTAMP, true);
		setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.HOST_SHIMMER_STREAMING_PROPERTIES, true);
	}

	/**
	 * Generate the Shimmer's Information Memory byte array based on the
	 * settings stored in ShimmerObject. These bytes can then be written to the
	 * Shimmer via the Shimmer Dock/Consensys Base. The Information Memory is is
	 * a region of the Shimmer's inbuilt RAM where all configuration information
	 * is stored.
	 * 
	 * @param generateForWritingToShimmer
	 * @return
	 */
	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer) {

		ConfigByteLayoutShimmer3 configByteLayoutCast = new ConfigByteLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
		
		byte[] configByteBackup = mConfigBytes.clone();
		
		// InfoMem defaults to 0xFF on firmware flash
		mConfigBytes = configByteLayoutCast.createConfigByteArrayEmpty(mConfigBytes.length);
		
		// If not being generated from scratch then copy across existing InfoMem contents
		if(!generateForWritingToShimmer) {
			System.arraycopy(configByteBackup, 0, mConfigBytes, 0, (configByteBackup.length > mConfigBytes.length) ? mConfigBytes.length:configByteBackup.length);
		}	
		
		// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
		// Sampling Rate
		byte[] samplingRateBytes = convertSamplingRateFreqToBytes(getSamplingRateShimmer(), getSamplingClockFreq());
		mConfigBytes[configByteLayoutCast.idxShimmerSamplingRate] = samplingRateBytes[0]; 
		mConfigBytes[configByteLayoutCast.idxShimmerSamplingRate+1] = samplingRateBytes[1]; 
	
		//FW not using this feature and BtStream will reject infomem if this isn't set to '1'
		mConfigBytes[configByteLayoutCast.idxBufferSize] = (byte) 1;//(byte) (mBufferSize & mInfoMemLayout.maskBufferSize); 
		
		// Sensors
		//JC: The updateEnabledSensorsFromExgResolution(), seems to be working incorrectly because of the boolean values of mIsExg1_24bitEnabled, so updating this values first 
		refreshEnabledSensorsFromSensorMap();
		
		// Configuration
		mConfigBytes[configByteLayoutCast.idxConfigSetupByte0] = (byte) (0x00);
		mConfigBytes[configByteLayoutCast.idxConfigSetupByte1] = (byte) (0x00);
		mConfigBytes[configByteLayoutCast.idxConfigSetupByte2] = (byte) (0x00);
		mConfigBytes[configByteLayoutCast.idxConfigSetupByte3] = (byte) (0x00);
		
		checkIfInternalExpBrdPowerIsNeeded();
		mConfigBytes[configByteLayoutCast.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & configByteLayoutCast.maskEXPPowerEnable) << configByteLayoutCast.bitShiftEXPPowerEnable);
		
		//EXG Configuration
//		exgBytesGetFromConfig(); //update mEXG1Register and mEXG2Register
//		System.arraycopy(mEXG1RegisterArray, 0, mConfigBytes, configByteLayoutCast.idxEXGADS1292RChip1Config1, 10);
//		System.arraycopy(mEXG2RegisterArray, 0, mConfigBytes, configByteLayoutCast.idxEXGADS1292RChip2Config1, 10);
		
		mConfigBytes[configByteLayoutCast.idxBtCommBaudRate] = (byte) (mBluetoothBaudRate & configByteLayoutCast.maskBaudRate);
	
		// Derived Sensors
		if((configByteLayoutCast.idxDerivedSensors0>0)&&(configByteLayoutCast.idxDerivedSensors1>0)) { // Check if compatible
			mConfigBytes[configByteLayoutCast.idxDerivedSensors0] = (byte) ((mDerivedSensors >> configByteLayoutCast.byteShiftDerivedSensors0) & configByteLayoutCast.maskDerivedChannelsByte);
			mConfigBytes[configByteLayoutCast.idxDerivedSensors1] = (byte) ((mDerivedSensors >> configByteLayoutCast.byteShiftDerivedSensors1) & configByteLayoutCast.maskDerivedChannelsByte);
			if(configByteLayoutCast.idxDerivedSensors2>0) { // Check if compatible
				mConfigBytes[configByteLayoutCast.idxDerivedSensors2] = (byte) ((mDerivedSensors >> configByteLayoutCast.byteShiftDerivedSensors2) & configByteLayoutCast.maskDerivedChannelsByte);
			}
		}
		
		// Configuration from each Sensor settings
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			abstractSensor.configBytesGenerate(this, mConfigBytes);
		}
		
		return mConfigBytes;
	}
	
	@Override
	public void createConfigBytesLayout() {
		mConfigByteLayout = new ConfigByteLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		//NOT USED IN THIS CLASS
	}
	
	/**
	 * @param commsRadio
	 */
	@Override
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
					//Sweatch only as handled in LiteProtocol?
//					stopTimerReadBattStatus();
					setBluetoothRadioState(BT_STATE.DISCONNECTED);
					mCommsProtocolRadio = null;
//					try {
//					clearCommsProtocolRadio();
//				} catch (DeviceException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}					
//					try {
//						handleDisconnect();
//					} catch (ShimmerException e) {
//						e.printStackTrace();
//					}
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
						//TODO?
//						interpretInqResponse((byte[])parsedResponse);
						inquiryDone();
					}
//					else if(responseCommand==InstructionsResponse.SAMPLING_RATE_RESPONSE_VALUE){ 
//						byte[] bufferInquiry = (byte[])parsedResponse;
//						setSamplingRateShimmerSweatch(bufferInquiry[0], bufferInquiry[1]);
//					}
//					else if(responseCommand==InstructionsResponse.CONFIG_BYTE0_RESPONSE_VALUE){ 
//						mConfigByte0 = (byte[])parsedResponse;
////						processResponseThroughSensorClasses(responseCommand, parsedResponse, COMMUNICATION_TYPE.BLUETOOTH);
//						configBytesParse(mConfigByte0);
//					}
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
//					else if(responseCommand==InstructionsResponse.BLINK_LED_RESPONSE_VALUE){ 
//						mCurrentLEDStatus = (int)parsedResponse;
//					}
//					else if(responseCommand==InstructionsResponse.BUFFER_SIZE_RESPONSE_VALUE){ 
//						mBufferSize = (int)parsedResponse;
//					}
					else if(responseCommand==InstructionsResponse.UNIQUE_SERIAL_RESPONSE_VALUE){
						mDieRecord = (byte[])parsedResponse;
					}
//					else if(responseCommand==InstructionsResponse.BAUD_RATE_RESPONSE_VALUE){ 
//						mBluetoothBaudRate = (int)parsedResponse;
//					}
					else if(responseCommand==InstructionsResponse.INFOMEM_RESPONSE_VALUE){
						configBytesParse((byte[])parsedResponse);
					}
					else{
						if(processResponseThroughSensorClasses(responseCommand, parsedResponse, COMMUNICATION_TYPE.BLUETOOTH)) {
							return;
						}
						consolePrintLn("Unhandled Response In ShimmerECGmd class: "
								+ UtilShimmer.bytesToHexStringWithSpacesFormatted(new byte[]{(byte) responseCommand}));
					}
				}
	
				private boolean processResponseThroughSensorClasses(
						int responseCommand, 
						Object parsedResponse,
						COMMUNICATION_TYPE bluetooth) {
					Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
					while(iterator.hasNext()){
						AbstractSensor abstractSensor = iterator.next();
						boolean found = abstractSensor.processResponse(responseCommand, parsedResponse, COMMUNICATION_TYPE.BLUETOOTH);
						if(found){
							return true;
						}
					}
					return false;
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
					//NOT USED IN THIS DEVICE
				}
	
				@Override
				public void eventDockedStateChange() {
					//NOT USED IN THIS DEVICE
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

			});
			
		}
	}
	
	@Override
	public void connect() throws ShimmerException {
		mShimmerDeviceCommsProtocolAdaptor.connect();
	}
	
	@Override
	public void disconnect() throws ShimmerException {
		super.disconnect();
		//Sweatch only as handled in LiteProtocol?
//		stopTimerReadBattStatus();
		clearCommsProtocolRadio();
		setBluetoothRadioState(BT_STATE.DISCONNECTED);
	}

	@Override
	public void startStreaming() {
		super.startStreaming();
		
		//Clear ADC filter buffer
//		SensorSweatchAdc sensorSweatchAdc = (SensorSweatchAdc) getSensorClass(SENSORS.SWEATCH_ADC);
//		sensorSweatchAdc.clearMovingAvgFilterBuffer();
		
//		restartTimerReadBattStatus();
		mDeviceCallbackAdapter.startStreaming();
	}
	
	private void initialiseDevice() {
//		setupSweatchSensorMaps();
//		sensorAndConfigMapsCreate();
		
		setHaveAttemptedToReadConfig(true);
		
		operationPrepare();
		setBluetoothRadioState(BT_STATE.CONNECTING);

		AbstractCommsProtocol radioProtocol = mCommsProtocolRadio.mRadioProtocol;
		if(radioProtocol instanceof LiteProtocol) {
			LiteProtocol liteProtocol = (LiteProtocol)radioProtocol;
			
			if(getFixedShimmerConfigMode()!=FIXED_SHIMMER_CONFIG_MODE.NONE) {
				setDefaultShimmerConfiguration();
				liteProtocol.writeShimmerAndSensorsSamplingRate(getSamplingRateBytesShimmer());
			}
			
			liteProtocol.readUniqueSerial();
			liteProtocol.readBaudRate();
			liteProtocol.readUniqueSerial();
			liteProtocol.readExpansionBoardID();
		}
		readConfigBytes();
		mCommsProtocolRadio.inquiry();
		
		// Just unlock instruction stack and leave logAndStream timer as
		// this is handled in the next step, i.e., no need for
		// operationStart() here
		startOperation(BT_STATE.CONNECTING, mCommsProtocolRadio.mRadioProtocol.getListofInstructions().size());
		mCommsProtocolRadio.mRadioProtocol.setInstructionStackLock(false);
		
		mCommsProtocolRadio.startTimerCheckIfAlive();
	}
	
	
	protected void dataHandler(ObjectCluster ojc) {
		mDeviceCallbackAdapter.dataHandler(ojc);
		super.mLastProcessedObjectCluster = ojc;
	}

	@Override
	public boolean setBluetoothRadioState(BT_STATE state) {
		boolean isChanged = super.setBluetoothRadioState(state);
		mDeviceCallbackAdapter.setBluetoothRadioState(state, isChanged);
		return isChanged;
	}
	
	public void isReadyForStreaming(){
		mDeviceCallbackAdapter.isReadyForStreaming();
	}
	
	//TODO Is this needed? just go straight to isReadyForStreaming()?
	protected void inquiryDone() {
		mDeviceCallbackAdapter.inquiryDone();
		isReadyForStreaming();
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
	
	protected void hasStopStreaming() {
		mDeviceCallbackAdapter.hasStopStreaming();
		//Sweatch only as handled in LiteProtocol?
//		stopTimerReadBattStatus();
	}
	
	protected void isNowStreaming() {
		mDeviceCallbackAdapter.isNowStreaming();
		//Sweatch only as handled in LiteProtocol?
//		startTimerReadBattStatus();
	}

	@Override
	public void calculatePacketReceptionRateCurrent(int intervalMs) {
		super.calculatePacketReceptionRateCurrent(intervalMs);
		mDeviceCallbackAdapter.sendCallbackPacketReceptionRateCurrent();
	}
	
	
	public void writeConfigBytes(byte[] shimmerInfoMemBytes) {
		if(mCommsProtocolRadio!=null && mConfigByteLayout!=null){
			mCommsProtocolRadio.writeInfoMem(mConfigByteLayout.MSP430_5XX_INFOMEM_D_ADDRESS, shimmerInfoMemBytes, mConfigByteLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
			readConfigBytes();
		}
	}

	public void readConfigBytes(){
		createInfoMemLayoutObjectIfNeeded();
		int size = mConfigByteLayout.calculateConfigByteLength();
		mCommsProtocolRadio.readInfoMem(mConfigByteLayout.MSP430_5XX_INFOMEM_D_ADDRESS, size, mConfigByteLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
	}

	@Override
	public ObjectCluster buildMsg(byte[] newPacket, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, long pcTimestamp) {
		ObjectCluster objectCluster = super.buildMsg(newPacket, commType, isTimeSyncEnabled, pcTimestamp);
		dataHandler(objectCluster);
		return objectCluster;
	}

}
