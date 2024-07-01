package com.shimmerresearch.comms;

import java.nio.ByteOrder;

import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.SerialPortListener;
import com.shimmerresearch.driverUtilities.ByteUtils;
import com.shimmerresearch.exceptions.ShimmerException;

public class TestRadioSerialPort extends AbstractSerialPortHal implements SerialPortListener {
	private transient SerialPortListener mShimmerSerialEventCallback;
	@Override
	public void connect() throws ShimmerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() throws ShimmerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeSafely() throws ShimmerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSerialPortRxBuffer() throws ShimmerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void txBytes(byte[] bytes) throws ShimmerException {
		// TODO Auto-generated method stub
		 if (bytes[0] == (byte)0xA4 && bytes[1] == (byte)1)
         {
         	mShimmerSerialEventCallback.serialPortRxEvent(1);

         }
         if (bytes[0] == (byte)0xA4 && bytes[1] == (byte)0)
         {
             stopRadioSimulator();
         }
         
	}

	boolean mFirstTime = true;
	int mCount = 0;
    byte header = (byte)0xA5;
	@Override
	public byte[] rxBytes(int numBytes) throws ShimmerException {
		if (mFirstTime) {
			mFirstTime = false;
			startRadioSimulator();
			return new byte[] {0};
		}
		byte[] finalPacket = new byte[mPacketSize];
		for(int i=0;i<mPacketSize;i=i+5) {
		byte[] data = ByteUtils.intToByteArray(mCount, ByteOrder.LITTLE_ENDIAN);
		byte[] packet = new byte[5];
		packet[0]=header;
		System.arraycopy(data, 0, packet, 1, data.length);
		mCount++;
		System.arraycopy(packet, 0, finalPacket, i, packet.length);
		}
		return finalPacket;
	}

	@Override
	public boolean isSerialPortReaderStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean bytesAvailableToBeRead() throws ShimmerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int availableBytes() throws ShimmerException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisonnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerSerialPortRxEventCallback(SerialPortListener shimmerSerialEventCallback) {
		mShimmerSerialEventCallback = shimmerSerialEventCallback;
	}

	@Override
	public void serialPortRxEvent(int byteLength) {
		// TODO Auto-generated method stub
		
	}
	boolean mRadioSimulatorThread = true;
	int mPacketSize = 5*100;
	public void startRadioSimulator() {
		
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            	mRadioSimulatorThread = true;
                while (mRadioSimulatorThread) {
                		
                		mShimmerSerialEventCallback.serialPortRxEvent(mPacketSize);
                		
                		try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                		
                }
                
            }
        });
        thread.start();
    }
	
	public void stopRadioSimulator() {
		mRadioSimulatorThread=false;
		mCount = 0;
		mFirstTime = true;
	}
}
