package com.shimmerresearch.pcSerialPort;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/** ByteWriter : an interface for a generic byte writing (I wanted to be able to switch from JSSC to any other framework)
 * 
 * @author Bastien Aracil 
 * 
 * https://stackoverflow.com/questions/20034470/jssc-serial-connection-set-write-timeout/37076508#37076508
 *
 */
public interface ByteWriter {

    void write(byte[] bytes) throws IOException;

    void write(byte oneByte) throws IOException;

    void write(byte[] bytes, long timeout) throws IOException, InterruptedException, TimeoutException;

    void write(byte oneByte, long timeout) throws IOException, InterruptedException, TimeoutException;

    void cancelWrite() throws IOException;

}