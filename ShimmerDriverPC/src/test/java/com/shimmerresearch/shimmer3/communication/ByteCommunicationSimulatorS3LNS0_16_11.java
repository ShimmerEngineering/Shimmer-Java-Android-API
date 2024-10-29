package com.shimmerresearch.shimmer3.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ByteCommunicationSimulatorS3LNS0_16_11 extends ByteCommunicationSimulatorS3{

	public ByteCommunicationSimulatorS3LNS0_16_11(String address) {
		super(address);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void txShimmerVersion() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x25);
		mBuffer.add((byte) 0x03);
	}
	
	@Override 
	protected void txFirmwareVersion() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x2f);
		mBuffer.add((byte) 0x03);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x10);
		mBuffer.add((byte) 0x0B);
	}
}
