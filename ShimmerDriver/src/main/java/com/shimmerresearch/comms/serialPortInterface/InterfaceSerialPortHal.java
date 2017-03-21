package com.shimmerresearch.comms.serialPortInterface;

import com.shimmerresearch.exceptions.ShimmerException;

/** Hardware abstraction layer
 * 
 * @author Mark Nolan
 */
public interface InterfaceSerialPortHal {

//	//This command should be available across all byte level radios, so the shimmer version and protocol type can be determined
//	int GET_SHIMMER_VERSION_COMMAND = 36;
//	int GET_FW_VERSION_COMMAND = 46;
	
    public void connect() throws ShimmerException;
//	public void connect(ShimmerSerialEventCallback shimmerSerialEventCallback) throws DeviceException;
	public void disconnect() throws ShimmerException;
	public void closeSafely() throws ShimmerException;
	public void clearSerialPortRxBuffer() throws ShimmerException;
	public void txBytes(byte[] buf) throws ShimmerException;
	public byte[] rxBytes(int numBytes) throws ShimmerException;
  
	public boolean isSerialPortReaderStarted();
//	public void sendRxCallback(byte[] packet, long timestampMs);
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode);
	
	public boolean bytesAvailableToBeRead() throws ShimmerException;
	
	public int availableBytes() throws ShimmerException;
	
	public boolean isConnected();
	public boolean isDisonnected();
	public void eventDeviceConnected();
	public void eventDeviceDisconnected();
	
	public void registerSerialPortRxEventCallback(SerialPortListener shimmerSerialEventCallback);
	void addByteLevelDataCommListener(ByteLevelDataCommListener spl);
	public void clearByteLevelDataCommListener();
	public void setTimeout(int timeout);
  
}
