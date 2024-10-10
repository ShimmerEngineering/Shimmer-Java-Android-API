package com.shimmerresearch.shimmer3.communication;

import java.util.concurrent.ConcurrentLinkedQueue;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

public class TestByteRadio implements ByteCommunication {

    public static final byte[] GET_SHIMMER_VERSION_COMMAND_NEW_VALUE = {0x3f};
    public static final byte[] GET_FW_VERSION_COMMAND_VALUE = {0x2e};

    private static final ConcurrentLinkedQueue<Byte> mQueue = new ConcurrentLinkedQueue<>();
    private ByteCommunicationListener mByteCommunicationListener;
    private final ByteCommunication mByteCommunication;
    private TestByte mTestByteListener;

    public interface TestByte {
        void onNewResult(String result);
        void onConnected();
        void onDisconnected();
    }
    
    public TestByteRadio() {
		this.mByteCommunication = null;
    	
    }

    public TestByteRadio(ByteCommunication byteComm) {
        this.mByteCommunication = byteComm;
        mByteCommunication.setByteCommunicationListener(new ByteCommunicationListener() {
            @Override
            public void eventConnected() {
                if (mTestByteListener != null) {
                    mTestByteListener.onConnected();
                }
            }

            @Override
            public void eventDisconnected() {
                if (mTestByteListener != null) {
                    mTestByteListener.onDisconnected();
                }
            }

            @Override
            public void eventNewBytesReceived(byte[] rxBytes) {
                for (byte b : rxBytes) {
                    mQueue.add(b);
                }
            }
        });
    }

    @Override
    public int getInputBufferBytesCount() throws Exception {
        return 0;
    }

    @Override
    public boolean isOpened() {
        return true;
    }

    @Override
    public boolean closePort() throws Exception {
        return true;
    }

    @Override
    public boolean openPort() throws Exception {
        return true;
    }

    @Override
    public byte[] readBytes(int byteCount, int timeout) throws Exception {
        return null;
    }

    @Override
    public boolean writeBytes(byte[] buffer) throws Exception {
        if (buffer.length == 1) {
            byte command = buffer[0];

            if (command == GET_FW_VERSION_COMMAND_VALUE[0]) {
                byte[] result = {0x03, 0x00, 0x00, 0x00, 0x10, 0x09};
                if (mByteCommunicationListener != null) {
                    mByteCommunicationListener.eventNewBytesReceived(result);
                }
            } else if (command == GET_SHIMMER_VERSION_COMMAND_NEW_VALUE[0]) {
                byte[] result = {0x03};
                if (mByteCommunicationListener != null) {
                    mByteCommunicationListener.eventNewBytesReceived(result);
                }
            }
        }
        return true;
    }

    @Override
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws Exception {
        return true;
    }

    @Override
    public boolean purgePort(int i) throws Exception {
        return true;
    }

    @Override
    public void setByteCommunicationListener(ByteCommunicationListener byteCommListener) {
        this.mByteCommunicationListener = byteCommListener;
    }

    @Override
    public void removeRadioListenerList() {
        this.mByteCommunicationListener = null;
    }
}
