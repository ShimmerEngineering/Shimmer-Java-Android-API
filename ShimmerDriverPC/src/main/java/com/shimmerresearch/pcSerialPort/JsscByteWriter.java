package com.shimmerresearch.pcSerialPort;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/** JsscByteWriter : an implementation of ByteWriter for Jssc
 * 
 * @author Bastien Aracil
 *
 * https://stackoverflow.com/questions/20034470/jssc-serial-connection-set-write-timeout/37076508#37076508
 *
 */
public class JsscByteWriter implements ByteWriter {

    private final SerialPort serialPort;

    public JsscByteWriter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void cancelWrite() throws IOException {
        try {
            serialPort.purgePort(SerialPort.PURGE_TXABORT);
            serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        try {
            serialPort.writeBytes(bytes);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte oneByte) throws IOException {
        try {
            serialPort.writeByte(oneByte);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] bytes, long timeout) throws IOException, InterruptedException, TimeoutException {
        if (timeout <= 0) {
            this.write(bytes);
        }
        else {
            new TimedOutByteWriting(this, bytes, timeout).write();
        }
    }

    @Override
    public void write(byte oneByte, long timeout) throws IOException, InterruptedException, TimeoutException {
        if (timeout <= 0) {
            this.write(oneByte);
        }
        else {
            new TimedOutByteWriting(this, oneByte, timeout).write();
        }
    }

}