package com.shimmerresearch.verisense.communication;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.verisense.communication.VerisenseMessage.STREAMING_COMMAND;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_COMMAND;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_DEBUG_MODE;
import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_PROPERTY;
import com.shimmerresearch.verisense.communication.payloads.EventLogPayload;
import com.shimmerresearch.verisense.communication.payloads.MemoryLookupTablePayload;
import com.shimmerresearch.verisense.communication.payloads.OpConfigPayload;
import com.shimmerresearch.verisense.communication.payloads.PendingEventsPayload;
import com.shimmerresearch.verisense.communication.payloads.ProdConfigPayload;
import com.shimmerresearch.verisense.communication.payloads.RecordBufferDetailsPayload;
import com.shimmerresearch.verisense.communication.payloads.RwcSchedulePayload;
import com.shimmerresearch.verisense.communication.payloads.StatusPayload;
import com.shimmerresearch.verisense.communication.payloads.TimePayload;

public class VerisenseProtocolByteCommunication {

	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	
	public VerisenseMessage rxVerisenseMessageBuffer;
	public VerisenseMessage rxVerisenseMessageLatest;

	public int PreviouslyWrittenPayloadIndex;
	String dataFileName = "";
	String dataFilePath = "";
	String binFileFolderDir = "";
	protected final String BadCRC = "BadCRC";
	int mNACKcounter;
	int mNACKCRCcounter;

	int MaximumNumberOfBytesPerBinFile = 100000000; // 100MB limit (actually 95 MB because 100MB = 102,400KB = 104,857,600 bytes, not 100,000,000 bytes)

	//TODO this might be doubling up on setBluetoothRadioState inside ShimmerDevice, could we reuse that instead?
	public enum VerisenseProtocolState {
		None, Disconnected, Connecting, Connected, Streaming, StreamingLoggedData, Limited
	}

	VerisenseProtocolState mState = VerisenseProtocolState.None;
	AbstractByteCommunication mByteCommunication;

	public VerisenseProtocolByteCommunication(AbstractByteCommunication byteComm) {
		mByteCommunication = byteComm;
		byteComm.setByteCommunicationListener(new ByteCommunicationListener() {

			@Override
			public void eventNewBytesReceived(byte[] rxBytes) {
				// System.out.println("PROTOCOL EVENT BYTES" + Hex.toHexString(rxBytes));
				long unixTimeinMS = System.currentTimeMillis();

				if(rxVerisenseMessageBuffer==null || rxVerisenseMessageBuffer.isExpired(unixTimeinMS)) {
					if(rxVerisenseMessageBuffer!=null && rxVerisenseMessageBuffer.isExpired(unixTimeinMS)) {
						System.out.println("Timeout on RX buf [" + rxVerisenseMessageBuffer.generateDebugString() + "], starting new buf");
					}
					rxVerisenseMessageBuffer = new VerisenseMessage(rxBytes, unixTimeinMS);
				} else {
					rxVerisenseMessageBuffer.appendToDataChuck(rxBytes, unixTimeinMS);
				}
				
				if(rxVerisenseMessageBuffer.isCurrentLengthGreaterThanExpectedLength()) {
					System.out.println("Unexpected payload size for RX buf [" + rxVerisenseMessageBuffer.generateDebugString() + "]");
					rxVerisenseMessageBuffer = null;
				} else if(rxVerisenseMessageBuffer.isCurrentLengthEqualToExpectedLength()) {
					handleResponse(rxVerisenseMessageBuffer);
					rxVerisenseMessageBuffer = null;
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
		mState = state;
	}

	public void removeRadioListenerList() {
		mRadioListenerList.clear();
	}

	void handleResponse(VerisenseMessage verisenseMessage) {
		rxVerisenseMessageLatest = verisenseMessage;
		try {
			if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.STATUS.responseByte()) {
				StatusPayload statusPayload = new StatusPayload();
				if (statusPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, statusPayload);
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.DATA.ackByte()) {
				stateChange(VerisenseProtocolState.Connected);
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.DATA.responseByte()) {
				stateChange(VerisenseProtocolState.StreamingLoggedData);

				verisenseMessage.consolePrintTransferTime();

				if (!verisenseMessage.CRCCheck()) {
					writeLoggedDataNack();
					return;
				}

				if ((dataFileName).isEmpty() || dataFileName.contains(BadCRC)) { //if the previous file name has a bad crc, create a new file, this has passed the crc check to reach here
					createBinFile(false);
				} else // if there is an existing file check the file size
				{
					// check size of file and create new bin file if required
					long length = new File(dataFilePath).length();
					if (length > MaximumNumberOfBytesPerBinFile) {
						// SaveBinFileToDB();
						// AdvanceLog(LogObject, "BinFileCheckNewFileRequired", dataFilePath + " size " + length, ASMName);
						createBinFile(false);
					}
				}

				FinishPayload(false);

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.CONFIG_PROD.responseByte()) {
				ProdConfigPayload prodConfigPayload = new ProdConfigPayload();
				if (prodConfigPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, prodConfigPayload);
				}
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.CONFIG_OPER.responseByte()) {
				OpConfigPayload opConfig = new OpConfigPayload();
				if (opConfig.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, opConfig);
				}
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.TIME.responseByte()) {
				TimePayload timePayload = new TimePayload();
				if(timePayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, timePayload);
				}
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.PENDING_EVENTS.responseByte()) {
				PendingEventsPayload pendingEventsPayload = new PendingEventsPayload();
				if(pendingEventsPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
					sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, pendingEventsPayload);
				}
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.FW_TEST.responseByte()) {
				
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.FW_DEBUG.responseByte()) {
				byte debugMode = verisenseMessage.payloadBytes[0];
				switch (debugMode) {
				case VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_READ:
					MemoryLookupTablePayload memoryLookupTablePayload = new MemoryLookupTablePayload();
					if(memoryLookupTablePayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						System.out.println(memoryLookupTablePayload.generateDebugString());
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, memoryLookupTablePayload);
					}
					break;
				case VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_ERASE:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.RWC_SCHEDULE_READ:
					RwcSchedulePayload rwcSchedulePayload = new RwcSchedulePayload();
					if(rwcSchedulePayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						System.out.println(rwcSchedulePayload.generateDebugString());
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, rwcSchedulePayload);
					}
					break;
				case VERISENSE_DEBUG_MODE.ERASE_LONG_TERM_FLASH:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.ERASE_SHORT_TERM_FLASH1:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.ERASE_SHORT_TERM_FLASH2:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.ERASE_OPERATIONAL_CONFIG:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.ERASE_PRODUCTION_CONFIG:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.CLEAR_PENDING_EVENTS:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.ERASE_FLASH_AND_LOOKUP:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.TRANSFER_LOOP:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.LOAD_FAKE_LOOKUPTABLE:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.GSR_LED_TEST:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.MAX86XXX_LED_TEST:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.CHECK_PAYLOADS_FOR_CRC_ERRORS:
					//TODO 
					break;
				case VERISENSE_DEBUG_MODE.EVENT_LOG:
					EventLogPayload eventLogPayload = new EventLogPayload();
					if(eventLogPayload.parsePayloadContents(verisenseMessage.payloadBytes)) {
						System.out.println(eventLogPayload.generateDebugString());
						sendObjectToRadioListenerList(verisenseMessage.commandAndProperty, eventLogPayload);
					}
					break;
				case VERISENSE_DEBUG_MODE.START_POWER_PROFILER_SEQ:
					//TODO 
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
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.DEVICE_DISCONNECT.ackByte()) {
				//TODO if needed
			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.STREAMING.responseByte()) {
				System.out.println("New Streaming Payload: " + System.currentTimeMillis());
				for (RadioListener rl : mRadioListenerList) {
					rl.eventNewPacket(verisenseMessage.payloadBytes, System.currentTimeMillis());
				}

			} else if(verisenseMessage.commandAndProperty == VERISENSE_PROPERTY.STREAMING.ackByte()) {
				if (mState.equals(VerisenseProtocolState.Streaming)) {
					stateChange(VerisenseProtocolState.Connected);
				} else {
					stateChange(VerisenseProtocolState.Streaming);
				}
			} else if(verisenseMessage.command == VERISENSE_COMMAND.ACK.mask()) {
				//TODO handle general ACKs
			} else {
				// AdvanceLog(LogObject, "NonDataResponse", BitConverter.ToString(ResponseBuffer), ASMName);
				throw new Exception();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void sendObjectToRadioListenerList(byte commandAndProperty, Object object) {
		for (RadioListener rl : mRadioListenerList) {
			rl.eventResponseReceived(commandAndProperty, object);
		}
	}

	void FinishPayload(boolean CRCError) {
		if (CRCError) {
			rxVerisenseMessageLatest.mCRCErrorPayload = true;
		}
		try {
			WritePayloadToBinFile();
		} catch (Exception ex) {
			// DataTCS.TrySetResult(false);
			return;
		}
		writeLoggedDataAck();
		mNACKcounter = 0;
		mNACKCRCcounter = 0;
	}

	void createBinFile(boolean crcError) {
		try {
			// var asm = RealmService.GetSensorbyID(Asm_uuid.ToString());
			// var trialSettings = RealmService.LoadTrialSettings();

			// var participantID = asm.ParticipantID;
			binFileFolderDir = String.format("%s/%s/%s/BinaryFiles", "trialname", "participantID", "uuid");
			Path path = Paths.get(binFileFolderDir);

			// java.nio.file.Files;
			Files.createDirectories(path);
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
			String pIndex = String.format("%05d", rxVerisenseMessageLatest.payloadIndex);
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

	void WritePayloadToBinFile() {

		if (PreviouslyWrittenPayloadIndex != rxVerisenseMessageLatest.payloadIndex) {
			try {
				// System.Console.WriteLine("Write Payload To Bin File!");
				File f = new File(dataFilePath);
				if (!f.exists()) {
					f.createNewFile();
				}

				Files.write(Paths.get(dataFilePath), rxVerisenseMessageLatest.payloadBytes, StandardOpenOption.APPEND);
				/*
				using (var stream = new FileStream(dataFilePath, FileMode.Append))
				{
					stream.Write(DataBufferToBeSaved.Packets, 0, DataBufferToBeSaved.CurrentLength);
				}
				IsFileLocked(dataFilePath);

				*/
				if (rxVerisenseMessageLatest.mCRCErrorPayload) {
					//SaveBinFileToDB();
				} else {
					// only assume non crc error payload index is valid
					PreviouslyWrittenPayloadIndex = rxVerisenseMessageLatest.payloadIndex;
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

	public void connect() {
		mByteCommunication.connect();
	}

	public void disconnect() {
		mByteCommunication.disconnect();
	}

	private void writeMessageNoPayload(byte commandAndProperty) {
		VerisenseMessage txVerisenseMessage = new VerisenseMessage(commandAndProperty, new byte[] {});
		mByteCommunication.writeBytes(txVerisenseMessage.generatePacket());
	}
	
	private void writeMessageWithPayload(byte commandAndProperty, byte[] payloadContents) {
		VerisenseMessage txVerisenseMessage = new VerisenseMessage(commandAndProperty, payloadContents);
		mByteCommunication.writeBytes(txVerisenseMessage.generatePacket());
	}

	public void readStatus() {
		writeMessageNoPayload(VERISENSE_PROPERTY.STATUS.readByte());
	}

	public void startStreaming() {
		writeMessageWithPayload(VERISENSE_PROPERTY.STREAMING.writeByte(), new byte[] {STREAMING_COMMAND.STREAMING_START});
	}

	public void stopStreaming() {
		writeMessageWithPayload(VERISENSE_PROPERTY.STREAMING.writeByte(), new byte[] {STREAMING_COMMAND.STREAMING_STOP});
	}

	public void writeTime() {
		TimePayload timePayload = new TimePayload();
		timePayload.setTimeMs(System.currentTimeMillis());
		writeMessageWithPayload(VERISENSE_PROPERTY.TIME.writeByte(), timePayload.getPayloadContents());
	}

	public void readTime() {
		writeMessageNoPayload(VERISENSE_PROPERTY.TIME.readByte());
	}

	public void readLoggedData() {
		writeMessageNoPayload(VERISENSE_PROPERTY.DATA.readByte());
	}

	void writeLoggedDataAck() {
		writeMessageNoPayload(VERISENSE_PROPERTY.DATA.ackNextStageByte());
	}

	public void writeLoggedDataNack() {
		writeMessageNoPayload(VERISENSE_PROPERTY.DATA.nackByte());
	}

	public void readOpConfig() {
		writeMessageNoPayload(VERISENSE_PROPERTY.CONFIG_OPER.readByte());
	}

	
	public void readRwcSchedule() {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.RWC_SCHEDULE_READ});
	}

	public void readFlashLookupTable() {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_READ});
	}

	public void readSensorEventLog() {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.EVENT_LOG});
	}

	public void readRecordBufferDetails() {
		writeMessageWithPayload(VERISENSE_PROPERTY.FW_DEBUG.writeByte(), new byte[] {VERISENSE_DEBUG_MODE.RECORD_BUFFER_DETAILS});
	}

	public void stop() {
		mByteCommunication.stop();
	}
}
