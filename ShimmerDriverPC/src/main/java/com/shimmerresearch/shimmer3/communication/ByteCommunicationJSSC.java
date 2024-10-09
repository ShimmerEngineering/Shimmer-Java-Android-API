package com.shimmerresearch.shimmer3.communication;

import java.io.IOException;
import jssc.SerialNativeInterface;
import jssc.SerialPortException;

public class ByteCommunicationJSSC implements ByteCommunication {
	
	/** Ignore bytes with framing error or parity error **/
    private static final int PARAMS_FLAG_IGNPAR = 1;
    /** Mark bytes with parity error or framing error **/
    private static final int PARAMS_FLAG_PARMRK = 2;
   
    private String portName;
    private volatile long portHandle;
    private boolean portOpened = false;
    private boolean eventListenerAdded = false;
    private final SerialNativeInterface serialInterface;
    private EventThread eventThread;
    private SerialPortEventListener eventListener;
    
	private ByteCommunicationJSSC port;
	private int eventType;
	private int eventValue;

    public ByteCommunicationJSSC(String portName) {
        this.portName = portName;
		serialInterface = new SerialNativeInterface();
    }

    public ByteCommunicationJSSC(ByteCommunicationJSSC port, int eventType, int eventValue) {
        this.port = port;
        this.eventType = eventType;
        this.eventValue = eventValue;
		serialInterface = new SerialNativeInterface();
	}

    public ByteCommunicationJSSC() {
		this.serialInterface = new SerialNativeInterface();
	}

	@Override
    public int getInputBufferBytesCount() throws Exception {
        checkPortOpened();
        try {
            return serialInterface.getBuffersBytesCount(portHandle)[0];
        } catch (IOException ex) {
            throw new Exception("getInputBufferBytesCount");
        }
    }

    @Override
    public boolean isOpened() {
        return portOpened;
    }

    @Override
    public synchronized boolean closePort() throws Exception {
        checkPortOpened();
        if (eventListenerAdded) {
            removeEventListener();
        }
        boolean returnValue = serialInterface.closePort(portHandle);
        if (returnValue) {
            portOpened = false;
        }
        return returnValue;
    }

    @Override
    public synchronized boolean openPort() throws Exception {
        if(portOpened){
            throw new Exception(SerialPortException.TYPE_PORT_ALREADY_OPENED);
        }
        if(portName != null){
            boolean useTIOCEXCL = (System.getProperty(SerialNativeInterface.PROPERTY_JSSC_NO_TIOCEXCL) == null &&
                                   System.getProperty(SerialNativeInterface.PROPERTY_JSSC_NO_TIOCEXCL.toLowerCase()) == null);
            portHandle = serialInterface.openPort(portName, useTIOCEXCL);//since 2.3.0 -> (if JSSC_NO_TIOCEXCL defined, exclusive lock for serial port will be disabled)
        }
        else {
            throw new Exception(SerialPortException.TYPE_NULL_NOT_PERMITTED);//since 2.1.0 -> NULL port name fix
        }
        if(portHandle == SerialNativeInterface.ERR_PORT_BUSY){
            throw new Exception(SerialPortException.TYPE_PORT_BUSY);
        }
        else if(portHandle == SerialNativeInterface.ERR_PORT_NOT_FOUND){
            throw new Exception(SerialPortException.TYPE_PORT_NOT_FOUND);
        }
        else if(portHandle == SerialNativeInterface.ERR_PERMISSION_DENIED){
            throw new Exception(SerialPortException.TYPE_PERMISSION_DENIED);
        }
        else if(portHandle == SerialNativeInterface.ERR_INCORRECT_SERIAL_PORT){
            throw new Exception(SerialPortException.TYPE_INCORRECT_SERIAL_PORT);
        }
        portOpened = true;
        return true;
    }

    @Override
    public byte[] readBytes(int byteCount, int timeout) throws Exception {
        checkPortOpened();
        waitBytesWithTimeout(byteCount, timeout);
        return readBytes(byteCount);
    }
    
    public boolean writeByte(byte singleByte) throws Exception {
        checkPortOpened();
        return writeBytes(new byte[]{singleByte});
    }

    @Override
    public boolean writeBytes(byte[] buffer) throws Exception {
        checkPortOpened();
        try {
            return serialInterface.writeBytes(portHandle, buffer);
        } catch (Exception ex) {
            throw new Exception("writeBytes");
        }
    }

    private byte[] readBytes(int byteCount) throws Exception {
        checkPortOpened();
        try {
            return serialInterface.readBytes(portHandle, byteCount);
        } catch (Exception ex) {
            throw new Exception("readBytes");
        }
    }

    private void waitBytesWithTimeout(int byteCount, int timeout) throws Exception {
        checkPortOpened();
        boolean timeIsOut = true;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeout) {
            if (getInputBufferBytesCount() >= byteCount) {
                timeIsOut = false;
                break;
            }
        }
        if (timeIsOut) {
            throw new Exception("Timeout");
        }
    }

    private void checkPortOpened() throws Exception {
        if (!portOpened) {
            throw new Exception(SerialPortException.TYPE_PORT_NOT_OPENED);
        }
    }

    private synchronized boolean removeEventListener() throws Exception {
        checkPortOpened();
        if (!eventListenerAdded) {
            throw new Exception(SerialPortException.TYPE_CANT_REMOVE_LISTENER);
        }
        eventThread.terminateThread();
        setEventsMask(0);
        if (Thread.currentThread().getId() != eventThread.getId()) {
            if (eventThread.isAlive()) {
                try {
                    eventThread.join(5000);
                } catch (InterruptedException ex) {
                    throw new Exception(SerialPortException.TYPE_LISTENER_THREAD_INTERRUPTED);
                }
            }
        }
        eventListenerAdded = false;
        return true;
    }

    private synchronized boolean setEventsMask(int mask) throws Exception {
        checkPortOpened();
        if (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
            SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
            SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X) {
            return true;
        }
        boolean returnValue = serialInterface.setEventsMask(portHandle, mask);
        if (!returnValue) {
            throw new Exception(SerialPortException.TYPE_CANT_SET_MASK);
        }
        return returnValue;
    }

    private int[][] waitEvents() {
        return serialInterface.waitEvents(portHandle);
    }

    private class EventThread extends Thread {

        private boolean threadTerminated = false;
        
        @Override
        public void run() {
            while(!threadTerminated){
                int[][] eventArray = waitEvents();
                for(int[] event : eventArray){
                    if(event[0] > 0 && !threadTerminated){
                        eventListener.serialEvent(new ByteCommunicationJSSC(ByteCommunicationJSSC.this, event[0], event[1]));
                        //FIXME
                        /*if(methodErrorOccurred != null){
                            try {
                                methodErrorOccurred.invoke(eventListener, new Object[]{new SerialPortException(SerialPort.this, "method", "exception")});
                            }
                            catch (Exception ex) {
                                System.out.println(ex);
                            }
                        }*/
                    }
                }
            }
        }

        private void terminateThread(){
            threadTerminated = true;
        }
    }

    public interface SerialPortEventListener {
        void serialEvent(ByteCommunicationJSSC serialPortEvent);
    }

	@Override
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws Exception {
        return setParams(baudRate, dataBits, stopBits, parity, true, true);
    }
	
	public synchronized boolean setParams(int baudRate, int dataBits, int stopBits, int parity, boolean setRTS, boolean setDTR) throws Exception {
        checkPortOpened();
        if(stopBits == 1){
            stopBits = 0;
        }
        else if(stopBits == 3){
            stopBits = 1;
        }
        int flags = 0;
        if(System.getProperty(SerialNativeInterface.PROPERTY_JSSC_IGNPAR) != null || System.getProperty(SerialNativeInterface.PROPERTY_JSSC_IGNPAR.toLowerCase()) != null){
            flags |= PARAMS_FLAG_IGNPAR;
        }
        if(System.getProperty(SerialNativeInterface.PROPERTY_JSSC_PARMRK) != null || System.getProperty(SerialNativeInterface.PROPERTY_JSSC_PARMRK.toLowerCase()) != null){
            flags |= PARAMS_FLAG_PARMRK;
        }
        return serialInterface.setParams(portHandle, baudRate, dataBits, stopBits, parity, setRTS, setDTR, flags);
    }

	@Override
    public synchronized boolean purgePort(int flags) throws Exception {
        checkPortOpened();
        return serialInterface.purgePort(portHandle, flags);
    }
}
