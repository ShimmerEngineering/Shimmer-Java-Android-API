package com.shimmerresearch.comms.serialPortInterface;

import com.shimmerresearch.driver.DeviceException;

/**
 * @author Mark Nolan
 *
 */
public interface InterfaceSerialPortHal {

//	//This command should be available across all byte level radios, so the shimmer version and protocol type can be determined
//	int GET_SHIMMER_VERSION_COMMAND = 36;
//	int GET_FW_VERSION_COMMAND = 46;
	
    public void connect() throws DeviceException;
//	public void connect(ShimmerSerialEventCallback shimmerSerialEventCallback) throws DeviceException;
	public void disconnect() throws DeviceException;
	public void closeSafely() throws DeviceException;
	public void clearSerialPortRxBuffer() throws DeviceException;
	public void txBytes(byte[] buf) throws DeviceException;
	public byte[] rxBytes(int numBytes) throws DeviceException;
  
	public boolean isSerialPortReaderStarted();
//	public void sendRxCallback(byte[] packet, long timestampMs);
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode);
	
	public boolean bytesAvailableToBeRead() throws DeviceException;
	
	public int availableBytes() throws DeviceException;
	
	public boolean isConnected();
	public boolean isDisonnected();
	public void eventDeviceConnected();
	public void eventDeviceDisconnected();
	
	public void registerSerialPortRxEventCallback(SerialPortListener shimmerSerialEventCallback);
	void addByteLevelDataCommListener(ByteLevelDataCommListener spl);
	public void clearByteLevelDataCommListener();
	public void setTimeout(int timeout);
  
}