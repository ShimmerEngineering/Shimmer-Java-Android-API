package com.shimmerresearch.driver.ble;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driverUtilities.ByteUtils;

public class VerisenseProtocolByteCommunication {

	public class VERISENSE_COMMAND {
		public static final byte READ = 0x10;
		public static final byte WRITE = 0x20;
		public static final byte RESPONSE = 0x30;
		public static final byte ACK = 0x40;
		public static final byte NACK_BAD_HEADER_COMMAND = 0x50;
		public static final byte NACK_BAD_HEADER_PROPERTY = 0x60;
		public static final byte NACK_GENERIC = 0x70;
		public static final byte ACK_NEXT_STAGE = (byte) 0x80;
	}
	
//	public class VERISENSE_PROPERTY {
//		public static final byte STATUS = 0x01;
//		public static final byte DATA = 0x02;
//		public static final byte CONFIG_PROD = 0x03;
//		public static final byte CONFIG_OPER = 0x04;
//		public static final byte TIME = 0x05;
//		public static final byte DFU_MODE = 0x06;
//		public static final byte PENDING_EVENTS = 0x07;
//		public static final byte FW_TEST = 0x08;
//		public static final byte FW_DEBUG = 0x09;
//		public static final byte DEVICE_DISCONNECT = 0x0B;
//		public static final byte STREAMING = 0x0A;
//	}

	public static enum VERISENSE_PROPERTY {
		STATUS((byte)0x01),
		DATA((byte)0x02),
		CONFIG_PROD((byte)0x03),
		CONFIG_OPER((byte)0x04),
		TIME((byte)0x05),
		DFU_MODE((byte)0x06),
		PENDING_EVENTS((byte)0x07),
		FW_TEST((byte)0x08),
		FW_DEBUG((byte)0x09),
		DEVICE_DISCONNECT((byte)0x0B),
		STREAMING((byte)0x0A);
		
		byte propertyMask;
		
		private VERISENSE_PROPERTY(byte mask) {
			this.propertyMask = mask;
		}

		public byte mask() {
			return propertyMask;
		}

		public byte readByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.READ);
		}
		
		public byte writeByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.WRITE);
		}
		
		public byte ackByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.ACK);
		}
		
		public byte responseByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.RESPONSE);
		}
	}

	public class VERISENSE_DEBUG_MODE {
		public static final byte FLASH_LOOKUP_TABLE_READ = 0x01;
		public static final byte FLASH_LOOKUP_TABLE_ERASE = 0x02;
		public static final byte RWC_SCHEDULE_READ = 0x03;
		public static final byte ERASE_LONG_TERM_FLASH = 0x04;
		public static final byte ERASE_SHORT_TERM_FLASH1 = 0x05;
		public static final byte ERASE_SHORT_TERM_FLASH2 = 0x06;
		public static final byte ERASE_OPERATIONAL_CONFIG = 0x07;
		public static final byte ERASE_PRODUCTION_CONFIG = 0x08;
		public static final byte CLEAR_PENDING_EVENTS = 0x09;
		public static final byte ERASE_FLASH_AND_LOOKUP = 0x0A;
		public static final byte TRANSFER_LOOP = 0x0B;
		public static final byte LOAD_FAKE_LOOKUPTABLE = 0x0C;
		public static final byte GSR_LED_TEST = 0x0D;
		public static final byte MAX86XXX_LED_TEST = 0x0E;
		public static final byte CHECK_PAYLOADS_FOR_CRC_ERRORS = 0x0F;
		public static final byte EVENT_LOG = 0x10;
		public static final byte START_POWER_PROFILER_SEQ = 0x11;
		public static final byte RECORD_BUFFER_DETAILS = 0x12;
	}

	public class StreamingStartStop {
		public static final byte STREAMING_START = 0x01;
		public static final byte STREAMING_STOP = 0x02;
	}

//	byte[] ReadStatusRequest = new byte[] { 0x11, 0x00, 0x00 };
//	byte[] ReadDataRequest = new byte[] { 0x12, 0x00, 0x00 };
//	byte[] StreamDataRequest = new byte[] { 0x2A, 0x01, 0x00, 0x01 };
//	byte[] StopStreamRequest = new byte[] { 0x2A, 0x01, 0x00, 0x02 };
//	byte[] ReadProdConfigRequest = new byte[] { 0x13, 0x00, 0x00 };
//	byte[] ReadOpConfigRequest = new byte[] { 0x14, 0x00, 0x00 };
//	byte[] ReadTimeRequest = new byte[] { 0x15, 0x00, 0x00 };
//	byte[] ReadPendingEventsRequest = new byte[] { 0x17, 0x00, 0x00 };
//	byte[] DFUCommand = new byte[] { 0x26, 0x00, 0x00 };
//	byte[] DisconnectRequest = new byte[] { 0x2B, 0x00, 0x00 };
//	byte[] dataACK = new byte[] { (byte) 0x82, 0x00, 0x00 };
//	byte[] dataNACK = new byte[] { 0x72, 0x00, 0x00 };
//	byte dataEndHeader = 0x42;

	StatusPayload mStatusPayload;
	OpConfigPayload mOpConfigPayload;
	boolean mNewPayload;
	boolean mNewStreamPayload;
	boolean WaitingForStopStreamingCommand = false;
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	public DataChunkNew DataBufferToBeSaved;
	public DataChunkNew DataCommandBuffer;
	public DataChunkNew DataStreamingBuffer;
	public DataChunkNew DataBuffer;
	public int PayloadIndex;
	public int PreviouslyWrittenPayloadIndex;
	String dataFileName = "";
	String dataFilePath = "";
	String binFileFolderDir = "";
	protected final String BadCRC = "BadCRC";
	int mNACKcounter;
	int mNACKCRCcounter;

	int MaximumNumberOfBytesPerBinFile = 100000000; // 100MB limit

	//TODO this might be doubling up on setBluetoothRadioState inside ShimmerDevice, could we reuse that instead?
	public enum VerisenseProtocolState {
		None, Disconnected, Connecting, Connected, Streaming, StreamingLoggedData, Limited
	}

	VerisenseProtocolState mState = VerisenseProtocolState.None;
	boolean mNewCommandPayload = false;
	AbstractByteCommunication mByteCommunication;

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

	public VerisenseProtocolByteCommunication(AbstractByteCommunication byteComm) {
		mByteCommunication = byteComm;
		byteComm.setByteCommunicationListener(new ByteCommunicationListener() {

			@Override
			public void eventNewBytesReceived(byte[] rxBytes) {
				// TODO Auto-generated method stub
				// System.out.println("PROTOCOL EVENT BYTES" + Hex.toHexString(rxBytes));

				if (mState.equals(VerisenseProtocolState.StreamingLoggedData)) {
					if (mNewPayload) {
						createNewPayload(rxBytes);
					} else {
						handleDataChunk(rxBytes);
					}

					return;
				} else if (mState.equals(VerisenseProtocolState.Streaming)) {

					// System.Console.WriteLine("STREAMING DATA (" + bytes.Length + ") :" +
					// String.Join(" ", bytes));
					if (rxBytes.length == 3 && rxBytes[0] == VERISENSE_PROPERTY.STREAMING.ackByte())// 4A 00 00
					{
						if (WaitingForStopStreamingCommand) {
							handleCommonResponse(rxBytes);
							WaitingForStopStreamingCommand = false;
							return;
						} else {
							stateChange(VerisenseProtocolState.Streaming);
							return;
						}
					}

					if (mNewStreamPayload) {
						createNewStreamPayload(rxBytes);
					} else {
						handleStreamDataChunk(rxBytes);
					}

					return;

				} else {
					if (rxBytes.length == 3 && ((rxBytes[0] & VERISENSE_COMMAND.ACK) == VERISENSE_COMMAND.ACK))// if it is an ack
					{
						handleCommonResponse(rxBytes);
					} else {
						if (mNewCommandPayload) {
							createNewCommandPayload(rxBytes);
						} else {
							handleCommandDataChunk(rxBytes);
						}
					}
				}

			}

			@Override
			public void eventDisconnected() {
				// TODO Auto-generated method stub
				System.out.println("PROTOCOL DISCONNECTED");
				stateChange(VerisenseProtocolState.Disconnected);
			}

			@Override
			public void eventConnected() {
				// TODO Auto-generated method stub
				System.out.println("PROTOCOL CONNECTED");
				stateChange(VerisenseProtocolState.Connected);
			}
		});
	}

	void handleCommandDataChunk(byte[] payload) {
		try {
			System.arraycopy(payload, 0, DataCommandBuffer.mPackets, DataCommandBuffer.mCurrentLength, payload.length);
			DataCommandBuffer.mCurrentLength += payload.length;

			//JC: This causes too many msgs in the logs we need a better implementation of this, maybe just logs last 10 msgs if a failure occurs
			//AutoSyncLogger.AddLog(LogObject, "PayloadChunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}",
			//	payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength), ASMName);
			//InternalWriteConsoleAndLog("Payload Chunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength));
			//FinalChunkLogMsgForNack = string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataCommandBuffer.CurrentLength, DataCommandBuffer.ExpectedLength);
			
			//Note: for readability it would be better for this to be == because > will cause the crc to fail anyway via a Buffer.BlockCopy exception e.g. "System.ArgumentException","Message":"Offset and length were out of bounds for the array or count is greater than the number of elements from index to the end of the source collection."
			if (DataCommandBuffer.mCurrentLength >= DataCommandBuffer.mExpectedLength) {
				handleCompleteCommandPayload();
			}
		} catch (Exception ex) {
			// SendDataNACK(false);
			// AdvanceLog(LogObject, "HandleDataChunk", ex, ASMName);
		}
	}

	void createNewCommandPayload(byte[] payload) {
		try {
			byte header = payload[0];

			byte[] lengthBytes = new byte[2];
			System.arraycopy(payload, 1, lengthBytes, 0, 2);

			int length = (int) ByteUtils.bytesToShort(lengthBytes, ByteOrder.LITTLE_ENDIAN);
			int offset = 3;
			DataCommandBuffer = new DataChunkNew(length + offset);
			DataCommandBuffer.mExpectedLength = length + offset;
			// var sensorid = reader.ReadByte();
			// var tickBytes = reader.ReadBytes(3);
			// var bArray = addByteToArray(tickBytes, 0);
			// var tick = BitConverter.ToUInt32(bArray, 0);
			byte[] remainingBytes = new byte[payload.length - offset];
			System.arraycopy(payload, 3, remainingBytes, 0, payload.length - offset);
			byte[] offsetBytes = new byte[] { header, lengthBytes[0], lengthBytes[1] };
			byte[] remainingBytesWStartingOffset = new byte[remainingBytes.length + offset];
			remainingBytesWStartingOffset[0] = header;
			remainingBytesWStartingOffset[1] = lengthBytes[0];
			remainingBytesWStartingOffset[2] = lengthBytes[1];

			System.arraycopy(remainingBytes, 0, remainingBytesWStartingOffset, 3, remainingBytes.length);
			System.arraycopy(remainingBytesWStartingOffset, 0, DataCommandBuffer.mPackets, DataCommandBuffer.mCurrentLength, remainingBytesWStartingOffset.length);

			DataCommandBuffer.mCurrentLength += remainingBytesWStartingOffset.length;
			mNewCommandPayload = false;

			//AdvanceLog(LogObject, "PayloadIndex", string.Format("Payload Index = {0}; Expected Length = {1}", PayloadIndex, DataCommandBuffer.ExpectedLength), ASMName);

			if (DataCommandBuffer.mCurrentLength > DataCommandBuffer.mExpectedLength) {
				//AdvanceLog(LogObject, "CreateNewPayload", "Error Current Length: " + DataCommandBuffer.CurrentLength + " bigger than Expected Length: " + DataCommandBuffer.ExpectedLength, ASMName);
				//SendDataNACK(true);
			} else if (DataCommandBuffer.mCurrentLength == DataCommandBuffer.mExpectedLength) { // this might occur if the payload length is very small
				//AdvanceLog(LogObject, "CreateNewPayload", "HandleCompleteStreamingPayload", ASMName);
				handleCompleteCommandPayload();
			}
		} catch (Exception ex) {
			// AdvanceLog(LogObject, "CreateChunk Exception", ex, ASMName);
			// SendDataNACK(false);
			return;
		}

	}

	public void handleCompleteCommandPayload() {
		mNewCommandPayload = true;
		handleCommonResponse(DataCommandBuffer.mPackets);
		DataCommandBuffer = new DataChunkNew();
	}

	void handleCommonResponse(byte[] ResponseBuffer) {
		try {
			//TODO suggest we parse these (command, property and payloadLength) in DataChunkNew as with and pass that object in here
			byte commandAndProperty = ResponseBuffer[0];
			byte command = (byte) (commandAndProperty & 0xF0);
			byte property = (byte) (commandAndProperty & 0x0F);

			int payloadLength = (ResponseBuffer[2] << 8) | ResponseBuffer[1];

			byte[] payloadContents = new byte[payloadLength];
			if(payloadLength>0) {
				System.arraycopy(ResponseBuffer, 3, ResponseBuffer, 0, payloadLength);
			}

			if(commandAndProperty == VERISENSE_PROPERTY.STATUS.responseByte()) {
				StatusPayload statusData = new StatusPayload();
				boolean statusResult = statusData.ProcessPayload(ResponseBuffer);
				if (statusResult) {
					mStatusPayload = statusData;
					sendObjectToRadioListenerList(commandAndProperty, mStatusPayload);
				} else {

				}
			} else if(commandAndProperty == VERISENSE_PROPERTY.DATA.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.CONFIG_PROD.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.CONFIG_OPER.responseByte()) {
				// TODO Suggest we just sent payloadContents to eventResponseReceived and let it be parsed by VerisenseDevice.configBytesParse()
				OpConfigPayload opData = new OpConfigPayload();
				boolean opResult = opData.processPayload(ResponseBuffer);
				if (opResult) {
					mOpConfigPayload = opData;
					sendObjectToRadioListenerList(commandAndProperty, mOpConfigPayload);
				}
			} else if(commandAndProperty == VERISENSE_PROPERTY.TIME.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.DFU_MODE.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.PENDING_EVENTS.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.FW_TEST.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.FW_DEBUG.responseByte()) {
				byte debugMode = payloadContents[0];
				switch (debugMode) {
				case VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_READ:
					break;
				case VERISENSE_DEBUG_MODE.FLASH_LOOKUP_TABLE_ERASE:
					break;
				case VERISENSE_DEBUG_MODE.RWC_SCHEDULE_READ:
					break;
				case VERISENSE_DEBUG_MODE.ERASE_LONG_TERM_FLASH:
					break;
				case VERISENSE_DEBUG_MODE.ERASE_SHORT_TERM_FLASH1:
					break;
				case VERISENSE_DEBUG_MODE.ERASE_SHORT_TERM_FLASH2:
					break;
				case VERISENSE_DEBUG_MODE.ERASE_OPERATIONAL_CONFIG:
					break;
				case VERISENSE_DEBUG_MODE.ERASE_PRODUCTION_CONFIG:
					break;
				case VERISENSE_DEBUG_MODE.CLEAR_PENDING_EVENTS:
					break;
				case VERISENSE_DEBUG_MODE.ERASE_FLASH_AND_LOOKUP:
					break;
				case VERISENSE_DEBUG_MODE.TRANSFER_LOOP:
					break;
				case VERISENSE_DEBUG_MODE.LOAD_FAKE_LOOKUPTABLE:
					break;
				case VERISENSE_DEBUG_MODE.GSR_LED_TEST:
					break;
				case VERISENSE_DEBUG_MODE.MAX86XXX_LED_TEST:
					break;
				case VERISENSE_DEBUG_MODE.CHECK_PAYLOADS_FOR_CRC_ERRORS:
					break;
				case VERISENSE_DEBUG_MODE.EVENT_LOG:
					break;
				case VERISENSE_DEBUG_MODE.START_POWER_PROFILER_SEQ:
					break;
				case VERISENSE_DEBUG_MODE.RECORD_BUFFER_DETAILS:
					break;
				default:
					break;
				}
			} else if(commandAndProperty == VERISENSE_PROPERTY.DEVICE_DISCONNECT.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.STREAMING.responseByte()) {
			} else if(commandAndProperty == VERISENSE_PROPERTY.STREAMING.ackByte()) {
				// var baseDataSS = new BasePayload();
				// var baseResultSS = baseDataSS.ProcessPayload(ResponseBuffer);
				if (mState.equals(VerisenseProtocolState.Streaming)) {
					stateChange(VerisenseProtocolState.Connected);
				} else {
					mNewStreamPayload = true;
					stateChange(VerisenseProtocolState.Streaming);
				}
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

	void createNewStreamPayload(byte[] payload) {
		try {
			// AutoSyncLogger.AddLog(LogObject, "NewPayloadHead",
			// BitConverter.ToString(payload), ASMName);

			byte header = payload[0];

			byte[] lengthBytes = new byte[2];
			System.arraycopy(payload, 1, lengthBytes, 0, 2);

			int length = (int) ByteUtils.bytesToShort(lengthBytes, ByteOrder.LITTLE_ENDIAN);
			int offset = 3;

			DataStreamingBuffer = new DataChunkNew(length);
			DataStreamingBuffer.mExpectedLength = length;
			// var sensorid = reader.ReadByte();
			// var tickBytes = reader.ReadBytes(3);
			// var bArray = addByteToArray(tickBytes, 0);
			// var tick = BitConverter.ToUInt32(bArray, 0);
			byte[] remainingBytes = new byte[payload.length - offset];
			System.arraycopy(payload, 3, remainingBytes, 0, payload.length - offset);
			System.arraycopy(remainingBytes, 0, DataStreamingBuffer.mPackets, DataStreamingBuffer.mCurrentLength, remainingBytes.length);

			DataStreamingBuffer.mCurrentLength += remainingBytes.length;
			mNewStreamPayload = false;

			//AdvanceLog(LogObject, "PayloadIndex", string.Format("Payload Index = {0}; Expected Length = {1}", PayloadIndex, DataStreamingBuffer.ExpectedLength), ASMName);

			if (DataStreamingBuffer.mCurrentLength > DataStreamingBuffer.mExpectedLength) {
				//AdvanceLog(LogObject, "CreateNewPayload", "Error Current Length: " + DataStreamingBuffer.mCurrentLength + " bigger than Expected Length: " + DataStreamingBuffer.ExpectedLength, ASMName);
				//SendDataNACK(true);
			} else if (DataStreamingBuffer.mCurrentLength == DataStreamingBuffer.mExpectedLength) { // this might occur if the payload length is very small
				//AdvanceLog(LogObject, "CreateNewPayload", "HandleCompleteStreamingPayload", ASMName);
				handleCompleteStreamingPayload();
			}
		} catch (Exception ex) {
			// AdvanceLog(LogObject, "CreateChunk Exception", ex, ASMName);

			// SendDataNACK(false);
			return;
		}

	}

	void handleStreamDataChunk(byte[] payload) {
		try {
			System.arraycopy(payload, 0, DataStreamingBuffer.mPackets, DataStreamingBuffer.mCurrentLength, payload.length);
			DataStreamingBuffer.mCurrentLength += payload.length;

			//JC: This causes too many msgs in the logs we need a better implementation of this, maybe just logs last 10 msgs if a failure occurs
			//AutoSyncLogger.AddLog(LogObject, "PayloadChunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}",
			//	payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength), ASMName);
			//InternalWriteConsoleAndLog("Payload Chunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength));
			//FinalChunkLogMsgForNack = string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataStreamingBuffer.CurrentLength, DataStreamingBuffer.ExpectedLength);
			
			//Note: for readability it would be better for this to be == because > will cause the crc to fail anyway via a Buffer.BlockCopy exception e.g. "System.ArgumentException","Message":"Offset and length were out of bounds for the array or count is greater than the number of elements from index to the end of the source collection."
			if (DataStreamingBuffer.mCurrentLength >= DataStreamingBuffer.mExpectedLength) {
				handleCompleteStreamingPayload();
			}
		} catch (Exception ex) {
			// SendDataNACK(false);
			// AdvanceLog(LogObject, "HandleDataChunk", ex, ASMName);
		}
	}

	void handleCompleteStreamingPayload() {
		mNewStreamPayload = true;
		// parseStreamingPayload();
		System.out.println("New Streaming Payload: " + System.currentTimeMillis());
		for (RadioListener rl : mRadioListenerList) {
			rl.eventNewPacket(DataStreamingBuffer.mPackets, System.currentTimeMillis());
		}

		DataStreamingBuffer = new DataChunkNew();
	}

	void createNewPayload(byte[] payload) {
		if (payload.length == 3 && payload[0] == VERISENSE_PROPERTY.DATA.ackByte()) {
			stateChange(VerisenseProtocolState.Connected);
			return;
		}

		try {
			// AutoSyncLogger.AddLog(LogObject, "NewPayloadHead",
			// BitConverter.ToString(payload), ASMName);
			DataBuffer.mUnixStartTimeinMS = System.currentTimeMillis();
			byte header = payload[0];

			byte[] lengthBytes = new byte[2];
			System.arraycopy(payload, 1, lengthBytes, 0, 2);

			int length = (int) ByteUtils.bytesToShort(lengthBytes, ByteOrder.LITTLE_ENDIAN);
			int offset = 3;

			stateChange(VerisenseProtocolState.StreamingLoggedData);

			DataBuffer.mExpectedLength = length;
			byte[] indexBytes = new byte[2];
			System.arraycopy(payload, 3, indexBytes, 0, 2);
			PayloadIndex = (int) ByteUtils.bytesToShort(indexBytes, ByteOrder.LITTLE_ENDIAN);
			System.out.println("Payload Index: " + PayloadIndex);
			offset = 5;

			System.arraycopy(indexBytes, 0, DataBuffer.mPackets, 0, indexBytes.length);
			DataBuffer.mCurrentLength += 2; // this is the index?

			byte[] remainingBytes = new byte[payload.length - offset];
			System.arraycopy(payload, offset, remainingBytes, 0, payload.length - offset);
			System.arraycopy(remainingBytes, 0, DataBuffer.mPackets, DataBuffer.mCurrentLength, remainingBytes.length);

			DataBuffer.mCurrentLength += remainingBytes.length;
			mNewPayload = false;

			//AdvanceLog(LogObject, "PayloadIndex", string.Format("Payload Index = {0}; Expected Length = {1}", PayloadIndex, DataBuffer.ExpectedLength), ASMName);

			if (DataBuffer.mCurrentLength > DataBuffer.mExpectedLength) {
				//AdvanceLog(LogObject, "CreateNewPayload", "Error Current Length: " + DataBuffer.CurrentLength + " bigger than Expected Length: " + DataBuffer.ExpectedLength, ASMName);
				//SendDataNACK(true);
			} else if (DataBuffer.mCurrentLength == DataBuffer.mExpectedLength) { // this might occur if the payload length is very small
				// AdvanceLog(LogObject, "CreateNewPayload", "HandleCompletePayload", ASMName);
				handleCompletePayload();
			}
		} catch (Exception ex) {
			// AdvanceLog(LogObject, "CreateChunk Exception", ex, ASMName);
			// SendDataNACK(false);
			return;
		}

	}

	void handleCompletePayload() {
		try {
			DataBuffer.mUnixFinishTimeinMS = System.currentTimeMillis();
			long duration = DataBuffer.mUnixFinishTimeinMS - DataBuffer.mUnixStartTimeinMS;
			System.out.println("Duration : " + duration);
			if (duration != 0) {
				DataBuffer.mTransfer = DataBuffer.mCurrentLength / ((double) (DataBuffer.mUnixFinishTimeinMS - DataBuffer.mUnixStartTimeinMS) / 1000);
				String syncProgress = String.format("%f KB/s", (DataBuffer.mTransfer / 1024.0)) + "(Payload Index : " + PayloadIndex + ")";
				System.out.println(syncProgress);
			}
			//AdvanceLog(LogObject, "Payload transfer rate", syncProgress, ASMName);
			//InvokeSyncEvent(Asm_uuid.ToString(), new SyncEventData { ASMID = Asm_uuid.ToString(), CurrentEvent = SyncEvent.DataSync, SyncProgress = syncProgress });
			//if (ShimmerBLEEvent != null)
				//ShimmerBLEEvent.Invoke(null, new ShimmerBLEEventData { ASMID = Asm_uuid.ToString(), CurrentEvent = VerisenseBLEEvent.SyncLoggedDataNewPayload, Message = syncProgress });

			if (!CRCCheck()) {
				// SendDataNACK(true);
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
		} catch (Exception ex) {
			// AdvanceLog(LogObject, "ProcessingDataPayloadException", ex, ASMName);
			System.out.println(ex.toString());
		}

	}

	void FinishPayload(boolean CRCError) {
		DataBufferToBeSaved = DataBuffer;

		if (CRCError) {
			DataBufferToBeSaved.mCRCErrorPayload = true;
		}
		try {
			WritePayloadToBinFile();

		} catch (Exception ex) {
			// DataTCS.TrySetResult(false);
			return;
		}
		SendDataACK();
		mNewPayload = true;
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
			String pIndex = String.format("%05d", PayloadIndex);
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

	void SendDataACK() {
		DataBuffer = new DataChunkNew();

		// AutoSyncLogger.AddLog(LogObject, "DataACKRequest",
		// BitConverter.ToString(dataACK), ASMName);

		try {
			//LastDataTransferReplySent = LastDataTransferReplySentFromBS.ACK;
			/*//ASM-931 only used for testing
			Random rnd = new Random();
			int test = rnd.Next(0, 10);
			if (test <= 2)
			{
				throw new Exception("Testing delete last payload exception");
			}
			*
			*/
			writeMessageNoPayload(VERISENSE_PROPERTY.DATA.ackByte());
		} catch (Exception ex) {
			//Delete the last payload written to the bin file, if it isnt crc error
			/*
			if (!DataBufferToBeSaved.CRCErrorPayload)
			{
				DeleteLastPayloadFromBinFile();
			}

			AdvanceLog(LogObject, "SendDataACKException", ex.Message, ASMName);
			DataTCS.TrySetResult(false);
			*/
		}

	}

	void WritePayloadToBinFile() {

		if (PreviouslyWrittenPayloadIndex != PayloadIndex) {
			try {
				// System.Console.WriteLine("Write Payload To Bin File!");
				File f = new File(dataFilePath);
				if (!f.exists()) {
					f.createNewFile();
				}

				byte[] bytesToWrite = new byte[DataBufferToBeSaved.mCurrentLength];
				System.arraycopy(DataBufferToBeSaved.mPackets, 0, bytesToWrite, 0, bytesToWrite.length);
				Files.write(Paths.get(dataFilePath), bytesToWrite, StandardOpenOption.APPEND);
				/*
				using (var stream = new FileStream(dataFilePath, FileMode.Append))
				{
					stream.Write(DataBufferToBeSaved.Packets, 0, DataBufferToBeSaved.CurrentLength);
				}
				IsFileLocked(dataFilePath);

				*/
				if (DataBufferToBeSaved.mCRCErrorPayload) {
					//SaveBinFileToDB();
				} else {
					// only assume non crc error payload index is valid
					PreviouslyWrittenPayloadIndex = PayloadIndex;
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

	void handleDataChunk(byte[] payload) {
		try {
			System.arraycopy(payload, 0, DataBuffer.mPackets, DataBuffer.mCurrentLength, payload.length);
			DataBuffer.mCurrentLength += payload.length;

			//JC: This causes too many msgs in the logs we need a better implementation of this, maybe just logs last 10 msgs if a failure occurs
			//AutoSyncLogger.AddLog(LogObject, "PayloadChunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}",
			//	payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength), ASMName);
			//InternalWriteConsoleAndLog("Payload Chunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength));
			//FinalChunkLogMsgForNack = string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength);
			
			//Note: for readability it would be better for this to be == because > will cause the crc to fail anyway via a Buffer.BlockCopy exception e.g. "System.ArgumentException","Message":"Offset and length were out of bounds for the array or count is greater than the number of elements from index to the end of the source collection."
			if (DataBuffer.mCurrentLength >= DataBuffer.mExpectedLength) {
				handleCompletePayload();
			}
		} catch (Exception ex) {
			// SendDataNACK(false);
			// AdvanceLog(LogObject, "HandleDataChunk", ex, ASMName);
		}
	}

	boolean CRCCheck() {
		try {
			byte[] completeChunk = new byte[DataBuffer.mExpectedLength];

			System.arraycopy(DataBuffer.mPackets, 0, completeChunk, 0, DataBuffer.mCurrentLength);
			/*
			var result = BitHelper.CheckCRC(completeChunk);

			if (!result.result)
			{
				AdvanceLog(LogObject, "CRCCheck", result, ASMName);
				//see ASM-1142, ASM-1131
				//AutoSyncLogger.AddLog(LogObject, "Failed CRC Payload", BitConverter.ToString(DataBuffer.Packets), ASMName);
			}

			return result.result;
			*/
			return true;
		} catch (Exception ex) {
			// AdvanceLog(LogObject, "CRCCheck Exception", ex, ASMName);
			return false;
		}

	}

	public void connect() {
		mByteCommunication.connect();
	}

	public void disconnect() {
		mByteCommunication.disconnect();
	}

	private void writeMessageNoPayload(byte commandAndProperty) {
		byte[] txBuf = new byte[] {commandAndProperty, 0x00, 0x00};
		mByteCommunication.writeBytes(txBuf);
	}
	
	private void writeMessageWithPayload(byte commandAndProperty, byte[] payloadContents) {
		int payloadLength = payloadContents.length;
		byte[] txBuf = new byte[3+payloadLength];
		txBuf[0] = commandAndProperty;
		txBuf[1] = (byte) (payloadLength & 0xFF);
		txBuf[2] = (byte) ((payloadLength >> 8) & 0xFF);
		System.arraycopy(payloadContents, 0, txBuf, 3, payloadLength);
		mByteCommunication.writeBytes(txBuf);
	}

	public void readStatus() {
		mNewCommandPayload = true;
		writeMessageNoPayload(VERISENSE_PROPERTY.STATUS.readByte());
	}

	public void startStreaming() {
		writeMessageWithPayload(VERISENSE_PROPERTY.STREAMING.writeByte(), new byte[] {StreamingStartStop.STREAMING_START});
	}

	public void readLoggedData() {
		DataBuffer = new DataChunkNew();
		stateChange(VerisenseProtocolState.StreamingLoggedData);
		mNewPayload = true;
		writeMessageNoPayload(VERISENSE_PROPERTY.DATA.readByte());
	}

	public void readOpConfig() {
		mNewCommandPayload = true;
		writeMessageNoPayload(VERISENSE_PROPERTY.CONFIG_OPER.readByte());
	}

	public void stopStreaming() {
		WaitingForStopStreamingCommand = true;
		writeMessageWithPayload(VERISENSE_PROPERTY.STREAMING.writeByte(), new byte[] {StreamingStartStop.STREAMING_STOP});
	}

	public void stop() {
		mByteCommunication.stop();
	}
}
