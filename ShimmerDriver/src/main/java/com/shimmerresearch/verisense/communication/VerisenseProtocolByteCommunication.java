package com.shimmerresearch.verisense.communication;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.VerisenseMessage.STREAMING_COMMAND;
import com.shimmerresearch.verisense.communication.VerisenseMessage.TIMEOUT_MS;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_COMMAND;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_DEBUG_MODE;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_PROPERTY;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_TEST_MODE;
import com.shimmerresearch.verisense.communication.payloads.AbstractPayload;
import com.shimmerresearch.verisense.communication.payloads.EventLogPayload;
import com.shimmerresearch.verisense.communication.payloads.MemoryLookupTablePayload;
import com.shimmerresearch.verisense.communication.payloads.OperationalConfigPayload;
import com.shimmerresearch.verisense.communication.payloads.PendingEventsPayload;
import com.shimmerresearch.verisense.communication.payloads.ProductionConfigPayload;
import com.shimmerresearch.verisense.communication.payloads.RecordBufferDetailsPayload;
import com.shimmerresearch.verisense.communication.payloads.RwcSchedulePayload;
import com.shimmerresearch.verisense.communication.payloads.StatusPayload;
import com.shimmerresearch.verisense.communication.payloads.TimePayload;

public class VerisenseProtocolByteCommunication {

	private static final boolean DEBUG_TX_RX_MESSAGES = true;
	private static final boolean DEBUG_TX_RX_BYTES = true;
	
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	
	public VerisenseMessage rxVerisenseMessageInProgress;
	public CircularFifoBuffer rxVerisenseMessageBuffer = new CircularFifoBuffer(5);

	public int PreviouslyWrittenPayloadIndex;
	String dataFileName = "";
	public String dataFilePath = "";
	String binFileFolderDir = "";
	public String trialName = "DefaultTrial";
	public String participantID = "DefaultParticipant";
	protected final String BadCRC = "BadCRC";
	int mNACKcounter;
	int mNACKCRCcounter;
    private String mRootPathForBinFile=""; 
	int MaximumNumberOfBytesPerBinFile = 100000000; // 100MB limit (actually 95 MB because 100MB = 102,400KB = 104,857,600 bytes, not 100,000,000 bytes)

	//TODO this might be doubling up on setBluetoothRadioState inside ShimmerDevice, could we reuse that instead?
	public enum VerisenseProtocolState {
		None, Disconnected, Connecting, Connected, Streaming, StreamingLoggedData, Limited
	}

	VerisenseProtocolState mState = VerisenseProtocolState.None;
	AbstractByteCommunication mByteCommunication;

	private EventLogPayload latestEventLogPayload;
	private StatusPayload latestStatusPayload;
	private TimePayload latestTimePayload;
	private ProductionConfigPayload latestProductionConfigPayload;
	private OperationalConfigPayload latestOperationalConfigPayload;
	private RwcSchedulePayload latestRwcSchedulePayload;
	private MemoryLookupTablePayload latestMemoryLookupTablePayload;
	private RecordBufferDetailsPayload latestRecordBufferDetailsPayload;

	public VerisenseProtocolByteCommunication(AbstractByteCommunication byteComm) {
		mByteCommunication = byteComm;
		byteComm.setByteCommunicationListener(new ByteCommunicationListener() {

			@Override
			public void eventNewBytesReceived(byte[] rxBytes) {
				if(DEBUG_TX_RX_BYTES) {
					System.out.println("RX: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBytes));
				}
				
				long unixTimeinMS = System.currentTimeMillis();

				if(rxVerisenseMessageInProgress==null || rxVerisenseMessageInProgress.isExpired(unixTimeinMS)) {
					if(rxVerisenseMessageInProgress!=null && rxVerisenseMessageInProgress.isExpired(unixTimeinMS)) {
						System.out.println("Timeout on RX buf [" + rxVerisenseMessageInProgress.generateDebugString() + "], starting new buf");
					}
					rxVerisenseMessageInProgress = new VerisenseMessage(rxBytes, unixTimeinMS);
				} else {
					rxVerisenseMessageInProgress.appendToDataChuck(rxBytes, unixTimeinMS);
				}
				
				if(rxVerisenseMessageInProgress.isCurrentLengthGreaterThanExpectedLength()) {
					System.out.println("Unexpected payload size for RX buf [" + rxVerisenseMessageInProgress.generateDebugString() + "]");
					rxVerisenseMessageInProgress = null;
				} else if(rxVerisenseMessageInProgress.isCurrentLengthEqualToExpectedLength()) {
					if(DEBUG_TX_RX_MESSAGES) {
						System.out.println("RX:" + rxVerisenseMessageInProgress.generateDebugString());
					}

					handleResponse(rxVerisenseMessageInProgress);
					rxVerisenseMessageInProgress = null;
				} 
			}

			@Override
			public void eventDisconnected() {
				System.out.println("PROTOCOL DISCONNECTED");
				stateChange(VerisenseProtocolState.Disconnected);
			}

			@Override
			public void eventConnected() {
				System.out.println("PROTOCOL CONNECTED");
				stateChange(VerisenseProtocolState.Connected);
			}
		});
	}
	
	public void addRadioListener(RadioListener radioListener) {
		mRadioListenerList.add(radioListener);
	}

	protected void stateChange(VerisenseProtocolState state) {
		System.out.println("State Change: " + state.toString());
		if (!mState.equals(state)) {
			mState = state;
			if (mState.equals(VerisenseProtocolState.StreamingLoggedData)) {
				for (RadioListener rl : mRadioListenerList) {
					rl.isNowStreamLoggedDataCallback();
				}
			}
		}
	}

	public void removeRadioListenerList() {
		mRadioListenerList.clear();
	}

	void handleResponse(VerisenseMessage verisenseMessage) {
		try {
			if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.STATUS.responseByte()) {
				latestStatusPayload = new StatusPayload();
				if (latestStatusPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestStatusPayload);
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.DATA.ackByte()) {
				if (mState.equals(VerisenseProtocolState.StreamingLoggedData)) {
					stateChange(VerisenseProtocolState.Connected);
					for (RadioListener rl : mRadioListenerList) {
						rl.hasStopStreamLoggedDataCallback();
					}
				}
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.DATA.responseByte()) {
				stateChange(VerisenseProtocolState.StreamingLoggedData);
				verisenseMessage.consolePrintTransferTime();
				if (!verisenseMessage.CRCCheck()) {
					writeLoggedDataNack();
					return;
				}

				if ((dataFileName).isEmpty() || dataFileName.contains(BadCRC)) { //if the previous file name has a bad crc, create a new file, this has passed the crc check to reach here
					createBinFile(verisenseMessage, false);
				} else // if there is an existing file check the file size
				{
					// check size of file and create new bin file if required
					long length = new File(dataFilePath).length();
					if (length > MaximumNumberOfBytesPerBinFile) {
						// SaveBinFileToDB();
						// AdvanceLog(LogObject, "BinFileCheckNewFileRequired", dataFilePath + " size " + length, ASMName);
						createBinFile(verisenseMessage, false);
					}
				}
				FinishPayload(verisenseMessage, false);

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.CONFIG_PROD.responseByte()) {
				latestProductionConfigPayload = new ProductionConfigPayload();
				if (latestProductionConfigPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestProductionConfigPayload);
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.CONFIG_OPER.responseByte()) {
				latestOperationalConfigPayload = new OperationalConfigPayload();
				if (latestOperationalConfigPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestOperationalConfigPayload);
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.TIME.responseByte()) {
				latestTimePayload = new TimePayload();
				if(latestTimePayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestTimePayload);
				}
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.PENDING_EVENTS.responseByte()) {
				PendingEventsPayload pendingEventsPayload = new PendingEventsPayload();
				if(pendingEventsPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, pendingEventsPayload);
				}
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.FW_DEBUG.responseByte()) {
				byte debugMode = verisenseMessage.payloadBytes[0];
				switch (debugMode) {
				case VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_READ:
					latestMemoryLookupTablePayload = new MemoryLookupTablePayload();
					if(latestMemoryLookupTablePayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						System.out.println(latestMemoryLookupTablePayload.generateDebugString());
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestMemoryLookupTablePayload);
					}
					break;
				case VERISENSE_DEBUG_MODE.RWC_SCHEDULE_READ:
					latestRwcSchedulePayload = new RwcSchedulePayload();
					if(latestRwcSchedulePayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestRwcSchedulePayload);
					}
					break;
				case VERISENSE_DEBUG_MODE.TRANSFER_LOOP:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.CHECK_PAYLOADS_FOR_CRC_ERRORS:
					//TODO response is a array of bank indexes (2 bytes each LSB) that contain Payloads with CRC errors 
					List<Long> listOfBankIdsWithCrcErrors = new ArrayList<Long>();
					for(int i = 0;i<verisenseMessage.payloadBytes.length;i+=2) {
						listOfBankIdsWithCrcErrors.add(AbstractPayload.parseByteArrayAtIndex(verisenseMessage.payloadBytes, i, CHANNEL_DATA_TYPE.UINT16));
					}
					
					break;
				case VERISENSE_DEBUG_MODE.EVENT_LOG:
					latestEventLogPayload = new EventLogPayload();
					if(latestEventLogPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						System.out.println(latestEventLogPayload.generateDebugString());
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, latestEventLogPayload);
					}
					break;
				case VERISENSE_DEBUG_MODE.RECORD_BUFFER_DETAILS:
					RecordBufferDetailsPayload recordBufferDetailsPayload = new RecordBufferDetailsPayload();
					if(recordBufferDetailsPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						System.out.println(recordBufferDetailsPayload.generateDebugString());
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, recordBufferDetailsPayload);
					}
					break;
				default:
					break;
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.STREAMING.responseByte()) {
				System.out.println("New Streaming Payload: " + System.currentTimeMillis());
				for (RadioListener rl : mRadioListenerList) {
					rl.eventNewPacket(verisenseMessage.payloadBytes, System.currentTimeMillis());
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.STREAMING.ackByte()) {
				if (mState.equals(VerisenseProtocolState.Streaming)) {
					stateChange(VerisenseProtocolState.Connected);
					for (RadioListener rl : mRadioListenerList) {
						rl.hasStopStreamingCallback();
					}
					
				} else {
					stateChange(VerisenseProtocolState.Streaming);
					for (RadioListener rl : mRadioListenerList) {
						rl.isNowStreamingCallback();
					}
				}

			} else if(verisenseMessage.commandMask == VERISENSE_COMMAND.ACK.getCommandMask()) {
				//TODO handle general ACKs
				
			} else {
				// AdvanceLog(LogObject, "NonDataResponse", BitConverter.ToString(ResponseBuffer), ASMName);
				throw new Exception();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Add it after it has been processed.
		rxVerisenseMessageBuffer.add(verisenseMessage);
	}
	
	private void sendObjectToRadioListenerList(byte commandAndProperty, Object object) {
		for (RadioListener rl : mRadioListenerList) {
			//clone here because if the message is sent to a different thread there is a risk it could be set to null in the origal thread
			rl.eventResponseReceived(commandAndProperty, object);
		}
	}

	void FinishPayload(VerisenseMessage verisenseMessage, boolean CRCError) {
		if (CRCError) {
			verisenseMessage.mCRCErrorPayload = true;
		}
		
		for (RadioListener rl : mRadioListenerList) {
			rl.eventNewSyncPayloadReceived(verisenseMessage.payloadIndex, CRCError, verisenseMessage.mTransferRateByes);
		}
		
		try {
			WritePayloadToBinFile(verisenseMessage);
		} catch (Exception ex) {
			// DataTCS.TrySetResult(false);
			return;
		}
		writeLoggedDataAck();
		mNACKcounter = 0;
		mNACKCRCcounter = 0;
	}

	void createBinFile(VerisenseMessage verisenseMessage, boolean crcError) {
		try {
			// var asm = RealmService.GetSensorbyID(Asm_uuid.ToString());
			// var trialSettings = RealmService.LoadTrialSettings();

			// var participantID = asm.ParticipantID;
			
			if (mRootPathForBinFile.isEmpty()) {
				binFileFolderDir = String.format("%s/%s/%s/BinaryFiles", getTrialName(), getParticipantID(), mByteCommunication.getUuid());
			} else {
				binFileFolderDir = String.format("%s/%s/%s/%s/BinaryFiles",mRootPathForBinFile, getTrialName(), getParticipantID(), mByteCommunication.getUuid());
			}
			Path path = Paths.get(binFileFolderDir);

			// java.nio.file.Files;
			Files.createDirectories(path);
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
			String pIndex = String.format("%05d", verisenseMessage.payloadIndex);
			if (crcError) {
				dataFileName = String.format("%s_%s_%s.bin", new SimpleDateFormat("yyMMdd_HHmmss").format(new Date()), pIndex, BadCRC);
			} else {
				dataFileName = String.format("%s_%s.bin", new SimpleDateFormat("yyMMdd_HHmmss").format(new Date()), pIndex);
			}

			// AdvanceLog(LogObject, "BinFileNameCreated", dataFileName, ASMName);
			Path rootPath = Paths.get(binFileFolderDir);
			Path dfn = Paths.get(dataFileName);

			dataFilePath = rootPath.resolve(dfn).toString();

			// AdvanceLog(LogObject, "BinFileCreated", dataFilePath, ASMName);
		} catch (Exception ex) {
			// AdvanceLog(LogObject, "BinFileCreatedException", ex, ASMName);
		}
	}

	void WritePayloadToBinFile(VerisenseMessage verisenseMessage) {

		if (PreviouslyWrittenPayloadIndex != verisenseMessage.payloadIndex) {
			try {
				// System.Console.WriteLine("Write Payload To Bin File!");
				File f = new File(dataFilePath);
				if (!f.exists()) {
					f.createNewFile();
				}

				Files.write(Paths.get(dataFilePath), verisenseMessage.payloadBytes, StandardOpenOption.APPEND);
				/*
				using (var stream = new FileStream(dataFilePath, FileMode.Append))
				{
					stream.Write(DataBufferToBeSaved.Packets, 0, DataBufferToBeSaved.CurrentLength);
				}
				IsFileLocked(dataFilePath);

				*/
				if (verisenseMessage.mCRCErrorPayload) {
					//SaveBinFileToDB();
				} else {
					// only assume non crc error payload index is valid
					PreviouslyWrittenPayloadIndex = verisenseMessage.payloadIndex;
				}
				// DataBufferToBeSaved = null;
				// RealmService.UpdateSensorDataSyncDate(Asm_uuid.ToString());
				// UpdateSensorDataSyncDate();
			} catch (Exception ex) {
				// AdvanceLog(LogObject, "FileAppendException", ex, ASMName);
				// throw ex;
				System.out.println(ex.toString());
			}
		} else {
			// AdvanceLog(LogObject, "WritePayloadToBinFile", "Same Payload Index = " +
			// PayloadIndex.ToString(), ASMName);
		}

	}

	public void connect() throws ShimmerException {
		mByteCommunication.connect();
	}

	public void disconnect() throws ShimmerException{
		mByteCommunication.disconnect();
	}

	public void stop() {
		mByteCommunication.stop();
	}

	protected void writeMessageWithoutPayload(byte commandAndProperty) {
		writeMessageWithPayload(commandAndProperty, new byte[] {});
	}
	
	protected void writeMessageWithPayload(byte commandAndProperty, byte[] payloadContents) {
		VerisenseMessage txVerisenseMessage = new VerisenseMessage(commandAndProperty, payloadContents);
		byte[] txBuf = txVerisenseMessage.generatePacket();
		
		if(DEBUG_TX_RX_MESSAGES) {
			System.out.println("TX:" + txVerisenseMessage.generateDebugString());
		}
		if(DEBUG_TX_RX_BYTES) {
			System.out.println("TX:" + UtilShimmer.bytesToHexStringWithSpacesFormatted(txBuf));
		}

		mByteCommunication.writeBytes(txBuf);
	}
	
	public StatusPayload readStatus() throws ShimmerException {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.STATUS.readByte());
		waitForResponse(VERISENSE_PROPERTY.STATUS, TIMEOUT_MS.STANDARD);
		return latestStatusPayload;
	}

	public void startStreaming() throws ShimmerException {
		if(!mState.equals(VerisenseProtocolState.Streaming)) {
			writeMessageWithPayload(VERISENSE_PROPERTY.STREAMING.writeByte(), new byte[] {STREAMING_COMMAND.STREAMING_START});
			waitForAck(VERISENSE_PROPERTY.STREAMING, TIMEOUT_MS.STANDARD);
		} else {
			throw new ShimmerException("Device is already streaming");
		}
	}

	public void stopStreaming() throws ShimmerException {
		if(mState.equals(VerisenseProtocolState.Streaming)) {
			writeMessageWithPayload(VERISENSE_PROPERTY.STREAMING.writeByte(), new byte[] {STREAMING_COMMAND.STREAMING_STOP});
			waitForAck(VERISENSE_PROPERTY.STREAMING, TIMEOUT_MS.STANDARD);
		} else {
			throw new ShimmerException("Device is not streaming");
		}
	}

	public void writeTime() throws ShimmerException {
		TimePayload timePayload = new TimePayload();
		timePayload.setTimeMs(System.currentTimeMillis());
		writeMessageWithPayload(VERISENSE_PROPERTY.TIME.writeByte(), timePayload.getPayloadContents());
		waitForAck(VERISENSE_PROPERTY.TIME, TIMEOUT_MS.STANDARD);
	}

	public TimePayload readTime() throws ShimmerException {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.TIME.readByte());
		waitForResponse(VERISENSE_PROPERTY.TIME, TIMEOUT_MS.STANDARD);
		return latestTimePayload;
	}

	public void readLoggedData() {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.DATA.readByte());
	}

	public void writeLoggedDataAck() {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.DATA.ackNextStageByte());
	}

	public void writeLoggedDataNack() {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.DATA.nackByte());
	}

	public void writeProductionConfig(byte[] txBuf) throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.CONFIG_PROD.writeByte(), txBuf);
		waitForAck(VERISENSE_PROPERTY.CONFIG_PROD, TIMEOUT_MS.STANDARD);
	}

	public ProductionConfigPayload readProductionConfig() throws ShimmerException {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.CONFIG_PROD.readByte());
		waitForResponse(VERISENSE_PROPERTY.CONFIG_PROD, TIMEOUT_MS.STANDARD);
		return latestProductionConfigPayload;
	}

	public OperationalConfigPayload readOperationalConfig() throws ShimmerException {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.CONFIG_OPER.readByte());
		waitForResponse(VERISENSE_PROPERTY.CONFIG_OPER, TIMEOUT_MS.STANDARD);
		return latestOperationalConfigPayload;
	}

	public void writeAndReadOperationalConfig(byte[] operationalConfig) throws ShimmerException {
		writeOperationalConfig(operationalConfig);
		readOperationalConfig();
	}

	public void writeOperationalConfig(byte[] operationalConfig) throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.CONFIG_OPER.writeByte(), operationalConfig);
		waitForAck(VERISENSE_PROPERTY.CONFIG_OPER, TIMEOUT_MS.STANDARD);
	}
	
	public void writeDisconnectDeviceSide() throws ShimmerException {
		writeMessageWithoutPayload(VERISENSE_PROPERTY.DEVICE_DISCONNECT.writeByte());
		waitForAck(VERISENSE_PROPERTY.DEVICE_DISCONNECT, TIMEOUT_MS.STANDARD);
	}

	public RwcSchedulePayload readRwcSchedule() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.RWC_SCHEDULE_READ});
		waitForResponse(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.STANDARD);
		return latestRwcSchedulePayload;
	}

	public MemoryLookupTablePayload readFlashLookupTable() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_READ});
		waitForResponse(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.READ_LOOKUP_TABLE);
		return latestMemoryLookupTablePayload;
	}

	public EventLogPayload readSensorEventLog() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.EVENT_LOG});
		waitForResponse(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.READ_LOOKUP_TABLE);
		return latestEventLogPayload;
	}

	public void writeEraseProductionConfiguration() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.ERASE_PRODUCTION_CONFIG});
		waitForAck(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.STANDARD);
	}

	public void writeEraseOperationalConfiguration() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.ERASE_OPERATIONAL_CONFIG});
		waitForAck(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.STANDARD);
	}

	public void writeClearPendingEvents() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.CLEAR_PENDING_EVENTS});
		waitForAck(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.STANDARD);
	}

	public void writeEraseLoggedData() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.ERASE_FLASH_AND_LOOKUP});
		waitForAck(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.ERASE_FLASH_AND_LOOKUP_TABLE);
	}

	public RecordBufferDetailsPayload readRecordBufferDetails() throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.RECORD_BUFFER_DETAILS});
		waitForResponse(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.STANDARD);
		return latestRecordBufferDetailsPayload;
	}

	public boolean writeRunHardwareTestAll(int hwVersionMajor, int hwVersionMinor) throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_TEST.writeByte(), new byte[] {VERISENSE_TEST_MODE.ALL.getTestId(), (byte) hwVersionMajor, (byte) hwVersionMinor});
		VerisenseMessage verisenseMessage = waitForVerisenseMessage(VERISENSE_PROPERTY.FW_TEST, null, TIMEOUT_MS.ALL_TEST_TIMEOUT);
		return verisenseMessage.commandMask==VERISENSE_COMMAND.ACK.getCommandMask();
	}

	public LinkedHashMap<VERISENSE_TEST_MODE, Boolean> writeRunHardwareTestsSeparately(VerisenseDevice verisenseDevice) throws ShimmerException {
		LinkedHashMap<VERISENSE_TEST_MODE, Boolean> listOfTestsToRun = new LinkedHashMap<VERISENSE_TEST_MODE, Boolean>();
		
		if(verisenseDevice.doesHwSupportShortTermFlash()) {
			listOfTestsToRun.put(VERISENSE_TEST_MODE.SHORT_TERM_FLASH1, false);
			listOfTestsToRun.put(VERISENSE_TEST_MODE.SHORT_TERM_FLASH2, false);
		}
		listOfTestsToRun.put(VERISENSE_TEST_MODE.LONG_TERM_FLASH, false);
		listOfTestsToRun.put(VERISENSE_TEST_MODE.EEPROM, false);
		listOfTestsToRun.put(VERISENSE_TEST_MODE.ACCEL_1, false);
		listOfTestsToRun.put(VERISENSE_TEST_MODE.BATT_VOLTAGE, false);
		if(verisenseDevice.doesHwSupportUsb()) {
			listOfTestsToRun.put(VERISENSE_TEST_MODE.USB_POWER_GOOD, false);
		}
		if(verisenseDevice.doesHwSupportLsm6ds3()) {
			listOfTestsToRun.put(VERISENSE_TEST_MODE.ACCEL2_AND_GYRO, false);
		}
		if(verisenseDevice.doesHwSupportMax86xxx()) {
			listOfTestsToRun.put(VERISENSE_TEST_MODE.MAX86XXX, false);
		}
		if(verisenseDevice.doesHwSupportMax30002()) {
			listOfTestsToRun.put(VERISENSE_TEST_MODE.MAX30002, false);
		}
		
		for(VERISENSE_TEST_MODE test : listOfTestsToRun.keySet()) {
			writeMessageWithPayload(VERISENSE_PROPERTY.FW_TEST.writeByte(), new byte[] {test.getTestId(), (byte) verisenseDevice.getExpansionBoardId(), (byte) verisenseDevice.getExpansionBoardRev()});
			VerisenseMessage verisenseMessage = waitForVerisenseMessage(VERISENSE_PROPERTY.FW_TEST, null, TIMEOUT_MS.ALL_TEST_TIMEOUT);
			listOfTestsToRun.put(test, verisenseMessage.commandMask==VERISENSE_COMMAND.ACK.getCommandMask());
		}
		return listOfTestsToRun;
	}
	
	public void writeRunMax86XXXLedTest(boolean isEnabled) throws ShimmerException {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.MAX86XXX_LED_TEST, (byte) (isEnabled? 0x01:0x00)});
		waitForAck(VERISENSE_PROPERTY.FW_DEBUG, TIMEOUT_MS.STANDARD);
	}

	private void waitForResponse(VERISENSE_PROPERTY verisenseProperty, long timeoutMs) throws ShimmerException {
		waitForVerisenseMessage(verisenseProperty, VERISENSE_COMMAND.RESPONSE, timeoutMs);
	}

	private void waitForAck(VERISENSE_PROPERTY verisenseProperty, long timeoutMs) throws ShimmerException {
		waitForVerisenseMessage(verisenseProperty, VERISENSE_COMMAND.ACK, timeoutMs);
	}

	private VerisenseMessage waitForVerisenseMessage(VERISENSE_PROPERTY verisenseProperty, VERISENSE_COMMAND expectedCommand, long timeoutMs) throws ShimmerException {
		rxVerisenseMessageBuffer.clear();
		
		int loopCount = 0;
		int waitIntervalMs = 100;
		int loopCountTotal = (int) (timeoutMs / waitIntervalMs);

		while (true) {
			try {
				Thread.sleep(waitIntervalMs);
			} catch (InterruptedException e) {
				System.out.println("Thread sleep FAIL");
			}

			loopCount += 1;
			if(loopCount >= loopCountTotal) {
				break;
			}
			
			Iterator<VerisenseMessage> iterator = rxVerisenseMessageBuffer.iterator();
			while(iterator.hasNext()) {
				VerisenseMessage verisenseMessage = iterator.next();
				if(verisenseMessage.propertyMask==verisenseProperty.getPropertyMask()) {
					if(expectedCommand==null || verisenseMessage.commandMask==expectedCommand.getCommandMask()) {
						return verisenseMessage;
					} else {
						VERISENSE_COMMAND commandReceived = VERISENSE_COMMAND.lookupByMask(verisenseMessage.commandMask);
						throw new ShimmerException(commandReceived==null? "UNKNOWN":commandReceived.toString() 
								+ " received for Property = " + verisenseProperty.toString() 
								+ ", expected = " + expectedCommand.toString());
					}
				}
			}
		}

		throw new ShimmerException("TIMEOUT for Property = " + verisenseProperty.toString() + ", expected = " + expectedCommand.toString());
	}
	
	public void setTrialName(String trial) {
		trialName = trial;
	}
	
	public String getTrialName() {
		return trialName;
	}
	
	public void setParticipantID(String participant) {
		participantID = participant;
	}
	
	public String getParticipantID() {
		return participantID;
	}
	
	public void setRootPathForBinFile(String rootPath) {
		mRootPathForBinFile = rootPath;
	}

}
