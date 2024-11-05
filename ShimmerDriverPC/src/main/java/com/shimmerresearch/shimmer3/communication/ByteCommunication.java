package com.shimmerresearch.shimmer3.communication;

import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public interface ByteCommunication{

    // Returns the number of bytes available in the input buffer.
    int getInputBufferBytesCount() throws SerialPortException;

    // Checks if the communication port is open.
    boolean isOpened();

    // Closes the communication port.
    boolean closePort() throws SerialPortException;

    // Opens the communication port.
    boolean openPort() throws SerialPortException;

    // Reads 'byteCount' bytes from the communication port with a timeout.
    byte[] readBytes(int byteCount, int timeout) throws SerialPortTimeoutException, SerialPortException;

    // Writes the specified byte buffer to the communication port.
    boolean writeBytes(byte[] buffer) throws SerialPortException;

	boolean setParams(int i, int j, int k, int l) throws SerialPortException;

	boolean purgePort(int i) throws SerialPortException;

	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener);

	public void removeRadioListenerList();

}