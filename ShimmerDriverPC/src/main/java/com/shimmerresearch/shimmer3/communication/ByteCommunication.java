package com.shimmerresearch.shimmer3.communication;

public interface ByteCommunication{

    // Returns the number of bytes available in the input buffer.
    int getInputBufferBytesCount() throws Exception;

    // Checks if the communication port is open.
    boolean isOpened();

    // Closes the communication port.
    boolean closePort() throws Exception;

    // Opens the communication port.
    boolean openPort() throws Exception;

    // Reads 'byteCount' bytes from the communication port with a timeout.
    byte[] readBytes(int byteCount, int timeout) throws Exception;

    // Writes the specified byte buffer to the communication port.
    boolean writeBytes(byte[] buffer) throws Exception;

	boolean setParams(int i, int j, int k, int l) throws Exception;

	boolean purgePort(int i) throws Exception;

}