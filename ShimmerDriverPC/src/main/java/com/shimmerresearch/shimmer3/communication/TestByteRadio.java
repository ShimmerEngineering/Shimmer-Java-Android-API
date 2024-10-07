package com.shimmerresearch.shimmer3.communication;

public class TestByteRadio extends ByteCommunicationJSSC {
	

	public static final byte START_STREAMING_COMMAND = (byte) 0x07;
	public static final byte GET_FW_VERSION_COMMAND = (byte) 0x2e;
	public static final byte GET_SHIMMER_VERSION_COMMAND_NEW = (byte) 0x3f;
	
	public static void main(String[] args) {
		ByteCommunicationJSSC testByteCommunication = new ByteCommunicationJSSC("COM3");
		
		try {
			testByteCommunication.openPort();
			testByteCommunication.writeByte((byte) START_STREAMING_COMMAND);
			testByteCommunication.readBytes(1, 2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


