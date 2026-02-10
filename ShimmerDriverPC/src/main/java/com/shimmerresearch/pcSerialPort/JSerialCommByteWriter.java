package com.shimmerresearch.pcSerialPort;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * JSerialCommByteWriter: an implementation of ByteWriter for jSerialComm
 * 
 * This provides better macOS support compared to JSSC for Bluetooth Classic communications.
 * 
 * @author Shimmer Research
 */
public class JSerialCommByteWriter implements ByteWriter {

    private final SerialPort serialPort;

    public JSerialCommByteWriter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void cancelWrite() throws IOException {
        // Flush the output buffer to cancel any pending writes
        serialPort.flushIOBuffers();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        int bytesWritten = serialPort.writeBytes(bytes, bytes.length);
        if (bytesWritten != bytes.length) {
            throw new IOException("Failed to write all bytes. Expected: " + bytes.length + ", Written: " + bytesWritten);
        }
    }

    @Override
    public void write(byte oneByte) throws IOException {
        byte[] buffer = new byte[]{oneByte};
        int bytesWritten = serialPort.writeBytes(buffer, 1);
        if (bytesWritten != 1) {
            throw new IOException("Failed to write byte");
        }
    }

    @Override
    public void write(byte[] bytes, long timeout) throws IOException, InterruptedException, TimeoutException {
        if (timeout <= 0) {
            this.write(bytes);
        } else {
            new TimedOutByteWriting(this, bytes, timeout).write();
        }
    }

    @Override
    public void write(byte oneByte, long timeout) throws IOException, InterruptedException, TimeoutException {
        if (timeout <= 0) {
            this.write(oneByte);
        } else {
            new TimedOutByteWriting(this, oneByte, timeout).write();
        }
    }
}
