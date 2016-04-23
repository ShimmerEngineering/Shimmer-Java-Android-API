package com.shimmerresearch.comms.serialPortInterface;

import com.shimmerresearch.driver.DeviceException;

/**
 * @author Mark Nolan
 *
 */
public interface ShimmerSerialPortInterface {

    //the timeout value for connecting with the port
    final static int SERIAL_PORT_TIMEOUT = 500; // was 2000

	public void connect() throws DeviceException;
//	public void connect(ShimmerSerialEventCallback shimmerSerialEventCallback) throws DeviceException;
	public void disconnect() throws DeviceException;
	public void closeSafely() throws DeviceException;
	public void clearSerialPortRxBuffer() throws DeviceException;
	public void txBytes(byte[] buf) throws DeviceException;
	public byte[] rxBytes(int numBytes) throws DeviceException;
  
	public void registerSerialPortRxEventCallback(ShimmerSerialEventCallback shimmerSerialEventCallback);
  
	public boolean isSerialPortReaderStarted();
//	public void sendRxCallback(byte[] packet, long timestampMs);
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode);
	
	public boolean bytesAvailableToBeRead();
	
	public int availableBytes();
  
}
