package com.shimmerresearch.comms.serialPortInterface;

import com.shimmerresearch.driver.DeviceException;

/**
 * @author Mark Nolan
 *
 */
public interface ShimmerSerialPortInterface {

    //the timeout value for connecting with the port
    final static int SERIAL_PORT_TIMEOUT = 500; // was 2000

	public void shimmerUartConnect() throws DeviceException;
	public void shimmerUartDisconnect() throws DeviceException;
	public void closeSafely() throws DeviceException;
	public void clearSerialPortRxBuffer() throws DeviceException;
	public void shimmerUartTxBytes(byte[] buf) throws DeviceException;
	public byte[] shimmerUartRxBytes(int numBytes) throws DeviceException;
  
//	public void registerRxCallback(UartRxCallback uartRxCallback);
  
	public boolean isSerialPortReaderStarted();
//	public void sendRxCallback(byte[] packet, long timestampMs);
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode);
  
}
