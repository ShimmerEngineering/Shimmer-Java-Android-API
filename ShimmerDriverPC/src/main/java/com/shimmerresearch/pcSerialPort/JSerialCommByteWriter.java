package com.shimmerresearch.pcSerialPort;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * JSerialCommByteWriter: an implementation of ByteWriter for jSerialComm
 * 
 * @author Shimmer Research
 * Based on JsscByteWriter by Bastien Aracil
 */
public class JSerialCommByteWriter implements ByteWriter {

    private final SerialPort serialPort;

    public JSerialCommByteWriter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void cancelWrite() throws IOException {
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
        byte[] singleByte = new byte[]{oneByte};
        int bytesWritten = serialPort.writeBytes(singleByte, 1);
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
