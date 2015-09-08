package com.shimmerresearch.driver;

public class BtCommandDetails{
	public byte command = (byte) 0x00;
	public String description = " ";
//	public boolean waitForAck = false;
//	public boolean waitForResponse = false;

	public byte expectedResponse = 0x00;
	public int expectedResponseByteLength = 1;

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
		this.command = command;
		this.description = description;
		this.expectedResponse = expectedResponse;
	}

	
	/**Used by responses
	 * @param command
	 * @param description
	 * @param expectedResponse
	 */
	public BtCommandDetails(byte command, String description, int expectedResponseByteLength){
		this.command = command;
		this.description = description;
		this.expectedResponseByteLength = expectedResponseByteLength;
	}


	/**Used by SET commands
	 * @param command
	 * @param description
	 */
	public BtCommandDetails(byte command, String description) {
		this.command = command;
		this.description = description;
	}

}