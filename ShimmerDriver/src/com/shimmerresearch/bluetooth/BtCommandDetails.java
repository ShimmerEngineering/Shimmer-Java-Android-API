package com.shimmerresearch.bluetooth;

public class BtCommandDetails{
	public byte mCommand = (byte) 0x00;
	public String mDescription = " ";
//	public boolean waitForAck = false;
//	public boolean waitForResponse = false;

	public byte expectedResponse = 0x00;
	public int mExpectedResponseByteLength = 1;

//	public BtCommandDetails(byte command, String description, boolean waitForAck, boolean waitForResponse){
//		this.command = command;
//		this.description = description;
//		this.waitForAck = waitForAck;
//		this.waitForResponse = waitForResponse;
//	}

	/**Used by GET commands
	 * @param command
	 * @param description
	 * @param expectedResponse
	 */
	public BtCommandDetails(byte command, String description, byte expectedResponse){
		this.mCommand = command;
		this.mDescription = description;
		this.expectedResponse = expectedResponse;
	}
	
	/**Used by responses
	 * @param command 
	 * @param description
	 * @param expectedResponseByteLength
	 */
	public BtCommandDetails(byte command, String description, int expectedResponseByteLength){
		this.mCommand = command;
		this.mDescription = description;
		this.mExpectedResponseByteLength = expectedResponseByteLength;
	}


	/**Used by SET commands
	 * @param command
	 * @param description
	 */
	public BtCommandDetails(byte command, String description) {
		this.mCommand = command;
		this.mDescription = description;
	}

}