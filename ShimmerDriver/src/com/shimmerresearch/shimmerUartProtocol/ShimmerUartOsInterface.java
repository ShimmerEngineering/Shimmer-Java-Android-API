package com.shimmerresearch.shimmerUartProtocol;

public interface ShimmerUartOsInterface {

  void shimmerUartConnect() throws DockException;
  void shimmerUartDisconnect() throws DockException;
  void clearSerialPortRxBuffer() throws DockException;
  void shimmerUartTxBytes(byte[] buf) throws DockException;
  byte[] shimmerUartRxBytes(int numBytes) throws DockException;
  
  void registerRxCallback(UartRxCallback uartRxCallback);
}
