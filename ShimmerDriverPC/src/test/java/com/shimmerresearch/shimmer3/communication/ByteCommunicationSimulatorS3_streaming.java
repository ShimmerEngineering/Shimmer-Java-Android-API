package com.shimmerresearch.shimmer3.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ByteCommunicationSimulatorS3_streaming extends ByteCommunicationSimulatorS3{

	public ByteCommunicationSimulatorS3_streaming(String address) {
		super(address);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void inquiryResponse() {
		byte[] bytes = UtilShimmer.hexStringToByteArray("FF02800201FF010803010001020F");
		for (byte b:bytes) {
			mBuffer.add(b);
		}
	}
	
	@Override
	protected void streaming() {
		int numberOfPackets = 2;
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0xf4);
		//0x00 0xEF 0xBE 0x1B 0xE7 0x09 0x3C 0x08 0x3F 0x09 0x7A 
		byte[] bytes = UtilShimmer.hexStringToByteArray("00EFBE1BE7093C083F097A");
		/*
		for (byte b:bytes) {
			mBuffer.add(b);
		}
		*/

	    // Start a new thread to send data periodically
	    int intervalMs = 100; // replace with desired interval in milliseconds
	    Thread streamingThread = new Thread(() -> {
	        try {
	        	Thread.sleep(200);
	        	for (int i=0;i<numberOfPackets;i++) {
	        		for (int j=0;j<bytes.length;j++){
                    	 mBuffer.add(bytes[j]);
                    }
	                Thread.sleep(intervalMs);
	            }
	        } catch (InterruptedException e) {
	            // Handle thread interruption (e.g., when stopping the stream)
	            Thread.currentThread().interrupt();
	        }
	    });
	    streamingThread.start();
    }
	
}
