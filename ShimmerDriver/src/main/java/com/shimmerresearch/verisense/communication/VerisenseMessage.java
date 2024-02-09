package com.shimmerresearch.verisense.communication;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.verisense.payloaddesign.CRC16CCITT;
import com.shimmerresearch.verisense.communication.VerisenseMessage.TIMEOUT_MS;
import com.shimmerresearch.verisense.communication.payloads.AbstractPayload;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.BYTE_COUNT;

public class VerisenseMessage implements Serializable{
	
	public static enum VERISENSE_COMMAND {
		READ((byte)0x10),
		WRITE((byte)0x20),
		RESPONSE((byte)0x30),
		ACK((byte)0x40),
		NACK_BAD_HEADER_COMMAND((byte)0x50),
		NACK_BAD_HEADER_PROPERTY((byte)0x60),
		NACK_GENERIC((byte)0x70),
		ACK_NEXT_STAGE((byte) 0x80);

		private byte commandMask;
		
		private VERISENSE_COMMAND(byte mask) {
			this.commandMask = mask;
		}

		private static final Map<Byte, VERISENSE_COMMAND> nameIndex = Maps.newHashMapWithExpectedSize(VERISENSE_COMMAND.values().length);
		static {
			for (VERISENSE_COMMAND suit : VERISENSE_COMMAND.values()) {
				nameIndex.put(suit.getCommandMask(), suit);
			}
		}

		public static VERISENSE_COMMAND lookupByMask(byte mask) {
			return nameIndex.get(mask);
		}

		public byte getCommandMask() {
			return commandMask;
		}
	}
	
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
		
		private byte propertyMask;
		
		private VERISENSE_PROPERTY(byte mask) {
			this.propertyMask = mask;
		}
		
		private static final Map<Byte, VERISENSE_PROPERTY> nameIndex = Maps.newHashMapWithExpectedSize(VERISENSE_PROPERTY.values().length);
		static {
			for (VERISENSE_PROPERTY suit : VERISENSE_PROPERTY.values()) {
				nameIndex.put(suit.getPropertyMask(), suit);
			}
		}

		public static VERISENSE_PROPERTY lookupByMask(byte mask) {
			return nameIndex.get(mask);
		}

		public byte getPropertyMask() {
			return propertyMask;
		}

		public byte readByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.READ.getCommandMask());
		}
		
		public byte writeByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.WRITE.getCommandMask());
		}
		
		public byte ackByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.ACK.getCommandMask());
		}
		
		public byte ackNextStageByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.ACK_NEXT_STAGE.getCommandMask());
		}
		public byte responseByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.RESPONSE.getCommandMask());
		}

		public byte nackByte() {
			return (byte) (propertyMask | VERISENSE_COMMAND.NACK_GENERIC.getCommandMask());
		}
	}

	public class VERISENSE_DEBUG_MODE {
		public static final byte FLASH_LOOKUP_TABLE_READ = 0x01;
//		public static final byte FLASH_LOOKUP_TABLE_ERASE = 0x02; // Recommend to use ERASE_FLASH_AND_LOOKUP instead
		public static final byte RWC_SCHEDULE_READ = 0x03;
//		public static final byte ERASE_LONG_TERM_FLASH = 0x04; // Recommend to use ERASE_FLASH_AND_LOOKUP instead
//		public static final byte ERASE_SHORT_TERM_FLASH1 = 0x05; // Recommend to use ERASE_FLASH_AND_LOOKUP instead
//		public static final byte ERASE_SHORT_TERM_FLASH2 = 0x06; // Recommend to use ERASE_FLASH_AND_LOOKUP instead
		public static final byte ERASE_OPERATIONAL_CONFIG = 0x07;
		public static final byte ERASE_PRODUCTION_CONFIG = 0x08;
		public static final byte CLEAR_PENDING_EVENTS = 0x09;
		public static final byte ERASE_FLASH_AND_LOOKUP = 0x0A;
		public static final byte TRANSFER_LOOP = 0x0B; // Internal FW testing only
//		public static final byte LOAD_FAKE_LOOKUPTABLE = 0x0C; // Internal FW testing only
//		public static final byte GSR_LED_TEST = 0x0D; //Unused
		public static final byte MAX86XXX_LED_TEST = 0x0E;
		public static final byte CHECK_PAYLOADS_FOR_CRC_ERRORS = 0x0F;
		public static final byte EVENT_LOG = 0x10;
//		public static final byte START_POWER_PROFILER_SEQ = 0x11; // Internal FW testing only
		public static final byte RECORD_BUFFER_DETAILS = 0x12;
	}
	
	public enum VERISENSE_TEST_MODE {
//		EXIT(0x00), // Not in use
		SHORT_TERM_FLASH1((byte) 0x01),
		SHORT_TERM_FLASH2((byte) 0x02),
		LONG_TERM_FLASH((byte) 0x03),
		EEPROM((byte) 0x04),
		ACCEL_1((byte) 0x05),
		BATT_VOLTAGE((byte) 0x06),
		USB_POWER_GOOD((byte) 0x07),
		ACCEL2_AND_GYRO((byte) 0x08),
		MAX86XXX((byte) 0x09),
		// 0x0A Reserved for PPG LED Test
		MAX30002((byte) 0x0B),
		ALL((byte) 0xFF);
		
		private byte testId;

		private VERISENSE_TEST_MODE(byte testId) {
			this.testId = testId;
		}

		public Byte getTestId() {
			return testId;
		}
	}

	public class STREAMING_COMMAND {
		public static final byte STREAMING_START = 0x01;
		public static final byte STREAMING_STOP = 0x02;
	}

	public class TIMEOUT_MS {
		public static final long STANDARD = 1 * 1000;
		public static final long DATA_TRANSFER = 10 * 1000;
		public static final long BETWEEN_PACKETS = 2 * 1000;
		public static final long ERASE_FLASH_AND_LOOKUP_TABLE = 40 * 1000;
		public static final long ERASE_LONG_TERM_FLASH = 40 * 1000;
		public static final long ERASE_SHORT_TERM_FLASH = 2 * 1000;
		public static final long ALL_TEST_TIMEOUT = 5 * 1000;
		public static final long READ_LOOKUP_TABLE = 20 * 1000;
		public static final long FLASH_AND_LOOKUP = ERASE_FLASH_AND_LOOKUP_TABLE + ERASE_LONG_TERM_FLASH;
		public static final long FAKE_LOOKUPTABLE = FLASH_AND_LOOKUP + ERASE_FLASH_AND_LOOKUP_TABLE;
		public static final long BETWEEN_NACK_AND_PAYLOAD = 1 * 1000;
	}

	byte commandAndProperty;
	byte commandMask;
	byte propertyMask;
	
	public byte[] payloadBytes;
	public int mExpectedLengthBytes;
	public int mCurrentLengthBytes;
	public long startTimeMs;
	public long endTimeMs;
	public long lastTransactionMs;
	public double mTransferRateByes;
	public final int mPacketMaxSize = 32767; // Considering we are using int16 to get the length the maximum value is 7F FF which is 32767
	public boolean mCRCErrorPayload = false;
	
	public int payloadIndex = -1;

	public VerisenseMessage(byte[] rxBytes, long timeMs) {
		startTimeMs = timeMs;
		lastTransactionMs = timeMs;

		setCommandAndProperty(rxBytes[0]);

		mExpectedLengthBytes = (int) AbstractPayload.parseByteArrayAtIndex(rxBytes, 1, CHANNEL_DATA_TYPE.UINT16);
		payloadBytes = new byte[mExpectedLengthBytes];
		
		// if message is greater then the 3 bytes for command|property and length bytes, append the remaining bytes to the payload byte buffer
		if (rxBytes.length > 3) {
			appendToDataChuck(rxBytes, 3, timeMs);
		}
		
		if (isCurrentLengthGreaterThanOrEqualToExpectedLength()) {
			endTimeMs = timeMs;
		}
	}

	public VerisenseMessage(byte commandAndProperty, byte[] payloadBytes) {
		setCommandAndProperty(commandAndProperty);
		this.payloadBytes = payloadBytes;
		mExpectedLengthBytes = payloadBytes.length;
		mCurrentLengthBytes = mExpectedLengthBytes;
	}

	private void setCommandAndProperty(byte commandAndProperty) {
		this.commandAndProperty = commandAndProperty;
		commandMask = (byte) (commandAndProperty & 0xF0);
		propertyMask = (byte) (commandAndProperty & 0x0F);
	}

	public void appendToDataChuck(byte[] rxBytes, long timeMs) {
		lastTransactionMs = timeMs;
		appendToDataChuck(rxBytes, 0, timeMs);
		
		if (isCurrentLengthGreaterThanOrEqualToExpectedLength()) {
			endTimeMs = timeMs;
		}
	}

	public void appendToDataChuck(byte[] rxBytes, int startByteIndex, long timeMs) {
		int numBytesToAppend = rxBytes.length-startByteIndex;
		System.arraycopy(rxBytes, startByteIndex, payloadBytes, mCurrentLengthBytes, numBytesToAppend);
		mCurrentLengthBytes += numBytesToAppend;

		if(commandAndProperty==VERISENSE_PROPERTY.DATA.responseByte() && payloadIndex==-1 && mCurrentLengthBytes>=2) {
			payloadIndex = (int) AbstractPayload.parseByteArrayAtIndex(payloadBytes, 0, CHANNEL_DATA_TYPE.UINT16);
		}
	}
	
	public boolean isCurrentLengthGreaterThanExpectedLength() {
		return mCurrentLengthBytes > mExpectedLengthBytes;
	}

	public boolean isCurrentLengthEqualToExpectedLength() {
		return mCurrentLengthBytes == mExpectedLengthBytes;
	}

	public boolean isCurrentLengthGreaterThanOrEqualToExpectedLength() {
		return isCurrentLengthGreaterThanExpectedLength() || isCurrentLengthEqualToExpectedLength();
	}

	public String consolePrintTransferTime(String macAddress) {
		long duration = endTimeMs - startTimeMs;
		System.out.println("Duration : " + duration);
		if (duration != 0) {
			mTransferRateByes = mCurrentLengthBytes / ((double) (endTimeMs - startTimeMs) / 1000);
			String syncProgress = String.format("%f KB/s", (mTransferRateByes / 1024.0)) + "(Payload Index : " + payloadIndex + ")";
			System.out.println(macAddress + " " + syncProgress);
			return syncProgress;
		}
		return null;
	}
	
	boolean CRCCheck() {
		byte[] payloadWithoutCrc = new byte[mExpectedLengthBytes-2];

		System.arraycopy(payloadBytes, 0, payloadWithoutCrc, 0, payloadWithoutCrc.length);

		CRC16CCITT crc16 = new CRC16CCITT();

		byte[] crcBytes = new byte[BYTE_COUNT.PAYLOAD_CRC];
		crcBytes[0] = payloadBytes[payloadBytes.length-2];
		crcBytes[1] = payloadBytes[payloadBytes.length-1];
		int crcOriginal = crc16.crcBytesToInt(crcBytes);

		boolean result = crc16.checkCrc(payloadWithoutCrc, crcOriginal);

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
		return result;
	}

	public byte[] generatePacket() {
		byte[] txBuf = new byte[3+mExpectedLengthBytes];
		txBuf[0] = commandAndProperty;
		txBuf[1] = (byte) (mExpectedLengthBytes & 0xFF);
		txBuf[2] = (byte) ((mExpectedLengthBytes >> 8) & 0xFF);
		if(payloadBytes.length>0) {
			System.arraycopy(payloadBytes, 0, txBuf, 3, mExpectedLengthBytes);
		}
		return txBuf;
	}

	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Command=" + VERISENSE_COMMAND.lookupByMask(commandMask).toString());
		sb.append(", Property=" + VERISENSE_PROPERTY.lookupByMask(propertyMask).toString());
		sb.append(", Expected Length = " + mExpectedLengthBytes);
		sb.append(", Current Length = " + mCurrentLengthBytes);
		
		return sb.toString();
	}

	public String generatePayloadByteString() {
		return UtilShimmer.bytesToHexStringWithSpacesFormatted(generatePacket());
	}

	public boolean isExpired(long timeMs) {
		return (timeMs - lastTransactionMs) > TIMEOUT_MS.BETWEEN_PACKETS;
	}

}
