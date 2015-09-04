package com.shimmerresearch.driver;

public class BtCommandDetails{
	public byte command = (byte) 0x00;
	public String description = " ";
	public boolean waitForAck = false;
	public boolean waitForResponse = false;

	public int expectedResponseByteLength = 1;

	public BtCommandDetails(byte command, String description, boolean waitForAck, boolean waitForResponse){
		this.command = command;
		this.description = description;
		this.waitForAck = waitForAck;
		this.waitForResponse = waitForResponse;
	}
	
	public BtCommandDetails(byte command, String description, int expectedResponseByteLength){
		this.command = command;
		this.description = description;
		this.expectedResponseByteLength = expectedResponseByteLength;
	}

}