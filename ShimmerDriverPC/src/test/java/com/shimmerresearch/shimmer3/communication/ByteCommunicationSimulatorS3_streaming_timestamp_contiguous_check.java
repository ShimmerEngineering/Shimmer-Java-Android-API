package com.shimmerresearch.shimmer3.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ByteCommunicationSimulatorS3_streaming_timestamp_contiguous_check extends ByteCommunicationSimulatorS3{

	public ByteCommunicationSimulatorS3_streaming_timestamp_contiguous_check(String address) {
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
		 int numberOfPackets = 3;

		    mBuffer.add((byte)0xFF);
		    mBuffer.add((byte)0xF4);

	
		    Thread streamingThread = new Thread(() -> {
		        try {
		            Thread.sleep(200);
		    	    int baseTimestamp = 0x000000;   // EF BE 1B you used earlier

		            for (int i = 0; i < numberOfPackets; i++) {

		            	byte[] packet = buildPacket(baseTimestamp);
		            	baseTimestamp += (0.5 * ShimmerObject.CONTIGUOUS_TIMESTAMP_TICKS_LIMIT);


		                for (byte b : packet) {
		                    mBuffer.add(b);
		                }

		                Thread.sleep(100);
		            }

		        } catch (InterruptedException e) {
		            Thread.currentThread().interrupt();
		        }
		    });

		    streamingThread.start();
    }
	
}
