package com.shimmerresearch.shimmerUartProtocol;

public interface ShimmerUartOsInterface {

	public void shimmerUartConnect() throws DockException;
	public void shimmerUartDisconnect() throws DockException;
	public void closeSafely() throws DockException;
	public void clearSerialPortRxBuffer() throws DockException;
	public void shimmerUartTxBytes(byte[] buf) throws DockException;
	public byte[] shimmerUartRxBytes(int numBytes) throws DockException;
  
//	public void registerRxCallback(UartRxCallback uartRxCallback);
  
	public boolean isSerialPortReaderStarted();
//	public void sendRxCallback(byte[] packet, long timestampMs);
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode);
  
}
