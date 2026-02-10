package com.shimmerresearch.pcSerialPort;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortDataListener;

import com.shimmerresearch.comms.serialPortInterface.ErrorCodesSerialPort;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.SerialPortListener;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.exceptions.ShimmerException;

/**
 * SerialPortCommJSerialComm - jSerialComm implementation for better macOS Bluetooth support
 * 
 * This replaces JSSC with jSerialComm which provides more reliable Bluetooth Classic
 * communications on macOS, addressing port connection and response issues.
 * 
 * @author Shimmer Research
 */
public class SerialPortCommJSerialComm extends AbstractSerialPortHal implements SerialPortListener {

	protected transient SerialPort mSerialPort = null;
	// Using JSerialCommByteWriter for timeout support
	protected JSerialCommByteWriter jSerialCommByteWriter = null;
	
	public String mUniqueId = "";
	public String mComPort = "";
	private int mBaudToUse = 115200;
	private boolean setRtsOnConnect = true;
	private boolean setDtrOnConnect = false;

	private transient ShimmerUartListener mShimmerUartListener;
	private boolean mIsSerialPortReaderStarted = false;
	private transient SerialPortListener mShimmerSerialEventCallback;
	
	/** 0 = normal speed (bad implementation), 1 = fast speed */
	public int mTxSpeed = 1;
	
	private boolean mIsDebugMode = true;
	private boolean mVerboseMode = true;
	private UtilShimmer mUtilShimmer = new UtilShimmer(getClass().getSimpleName(), mVerboseMode);
	
	public SerialPortCommJSerialComm(String comPort, String uniqueId, int baudToUse) {
		mUniqueId = uniqueId;
		mComPort = comPort;
		setConnectionHandle(comPort);
		mBaudToUse = baudToUse;
        mSerialPort = SerialPort.getCommPort(mComPort);
        jSerialCommByteWriter = new JSerialCommByteWriter(mSerialPort);
	}

	public SerialPortCommJSerialComm(String comPort, String uniqueId, int baudToUse, SerialPortListener shimmerSerialEventCallback) {
		this(comPort, uniqueId, baudToUse);
		registerSerialPortRxEventCallback(shimmerSerialEventCallback);
	}

	/** Opens and configures the Shimmer UART COM port
	 * @throws ShimmerException 
	 */
	@Override
	public void connect() throws ShimmerException {
        try {
    		consolePrintLn("Connecting to COM port:" + mComPort);
    		
    		// Configure serial port parameters
    		mSerialPort.setComPortParameters(
    			mBaudToUse,
    			8, // data bits
    			SerialPort.ONE_STOP_BIT,
    			SerialPort.NO_PARITY
    		);
    		
    		// Set timeouts - read timeout in milliseconds
    		mSerialPort.setComPortTimeouts(
    			SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
    			mSerialPortTimeout,
    			0
    		);
    		
    		// Open port
    		boolean portOpened = mSerialPort.openPort();
    		consolePrintLn("Port open: " + portOpened);
    		
    		if (!portOpened) {
    			throw new Exception("Failed to open serial port: " + mComPort);
    		}
    		
    		// Set RTS and DTR
    		if (setRtsOnConnect) {
    			mSerialPort.setRTS();
    		} else {
    			mSerialPort.clearRTS();
    		}
    		
    		if (setDtrOnConnect) {
    			mSerialPort.setDTR();
    		} else {
    			mSerialPort.clearDTR();
    		}
    		
    		// Disable flow control (similar to JSSC FLOWCONTROL_NONE)
    		mSerialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    		
    		consolePrintLn("Port Status : " + mSerialPort.isOpen());
            eventDeviceConnected();
        }
        catch (Exception e) {
        	eventDeviceDisconnected();
        	ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        
		startSerialPortReader();
	}
	
	/** Closes the Shimmer UART COM port
	 * @throws ShimmerException 
	 */
	@Override
	public void disconnect() throws ShimmerException {
    	if(mSerialPort != null && mSerialPort.isOpen()) {
	        try {
	        	mSerialPort.flushIOBuffers();
	        	mSerialPort.closePort();
	        	eventDeviceDisconnected();
			} catch (Exception e) {
	        	ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING); 
	        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
				throw(de);
			}
    	}
    }
	
	@Override
	public void closeSafely() throws ShimmerException {
		if(isSerialPortReaderStarted()){
			stopSerialPortReader();
		}
		else {
			disconnect();
		}
	}

	@Override
	public void clearSerialPortRxBuffer() throws ShimmerException {
    	try {
			mSerialPort.flushIOBuffers();
		} catch (Exception e) {
        	ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
	}

	/** Transmits a byte array to the Shimmer UART COM port
	 * @param buf the Tx buffer array
	 * @throws ShimmerException 
	 */
	@Override
	public void txBytes(byte[] buf) throws ShimmerException {
    	try {
        	if(mTxSpeed == 0) { // normal speed
        		for(int i = 0; i<buf.length;i++) {
        			jSerialCommByteWriter.write(buf[i], mSerialPortTimeout);
        		}
        	}
        	else {  // fast speed
    			jSerialCommByteWriter.write(buf, mSerialPortTimeout);
        	}
		} catch (IOException | InterruptedException | TimeoutException e) {
        	ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_WRITING_DATA); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }    

    /** Receives a byte array from the Shimmer UART COM port
	 * @param numBytes the number of bytes to receive
	 * @return byte array of received bytes
     * @throws ShimmerException 
	 */
	@Override
	public byte[] rxBytes(int numBytes) throws ShimmerException {
		try {
			byte[] rxBuf = new byte[numBytes];
			int totalBytesRead = 0;
			long startTime = System.currentTimeMillis();
			
			// Read with timeout
			while (totalBytesRead < numBytes) {
				int bytesToRead = numBytes - totalBytesRead;
				int bytesRead = mSerialPort.readBytes(rxBuf, bytesToRead, totalBytesRead);
				totalBytesRead += bytesRead;
				
				// Check for timeout
				if (System.currentTimeMillis() - startTime > mSerialPortTimeout) {
					ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_TIMEOUT);
					throw(de);
				}
				
				// Small delay to avoid busy waiting
				if (bytesRead == 0 && totalBytesRead < numBytes) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
			
			if(this.mIsDebugMode){
				consolePrintLn("Serial Port RX(" + rxBuf.length + ")" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
			}
			return rxBuf;
		} catch (ShimmerException e) {
			throw e;
		} catch (Exception e) {
        	ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_READING_DATA); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }
	
	public ShimmerException generateException(int lowLevelError){
    	ShimmerException de = new ShimmerException(
    			mUniqueId, 
    			mComPort,
    			ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,
    			lowLevelError); 
    	return de;
	}
	
    public void startSerialPortReader() throws ShimmerException {
    	if(!mIsSerialPortReaderStarted){
        	mShimmerUartListener = new ShimmerUartListener(mUniqueId);
            try {
            	mSerialPort.addDataListener(mShimmerUartListener);
        		mIsSerialPortReaderStarted = true;
            } catch (Exception e) {
        		mIsSerialPortReaderStarted = false;
    			ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_READER_START);
    			de.updateDeviceException(e.getMessage(), e.getStackTrace());
    			throw(de);
            }
    	}
    }

    public void stopSerialPortReader() throws ShimmerException {
        try {
        	if (mSerialPort != null) {
        		mSerialPort.removeDataListener();
        	}
        } catch (Exception e) {
			ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_READER_STOP);
			de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        
    	try {
    		// Finish up
    		disconnect();
    		mIsSerialPortReaderStarted = false;
		} catch (ExecutionException e) {
			ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_READER_STOP);
			de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }
    
    @Override
	public boolean isSerialPortReaderStarted(){
		return mIsSerialPortReaderStarted;
	}
	
	public class ShimmerUartListener implements SerialPortDataListener {
	    String mUniqueId = "";
	    boolean mIsParserBusy = false;

	    ShimmerUartListener(String uniqueID) {
	    	mUniqueId = uniqueID;
	    }

		@Override
		public int getListeningEvents() {
			return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
		}

		@Override
		public void serialEvent(SerialPortEvent event) {
	        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
	        	int bytesAvailable = mSerialPort.bytesAvailable();
	            if (!mIsParserBusy && bytesAvailable > 0) {
	            	mIsParserBusy = true;
	            	serialPortRxEvent(bytesAvailable);
	            	mIsParserBusy = false;
	            }
	        }
		}
	}
	
	@Override
	public void serialPortRxEvent(int byteLength) {
		if(mShimmerSerialEventCallback != null){
			mShimmerSerialEventCallback.serialPortRxEvent(byteLength);
		}
	}
	
	@Override
	public void registerSerialPortRxEventCallback(SerialPortListener shimmerSerialEventCallback) {
		mShimmerSerialEventCallback = shimmerSerialEventCallback;
	}

	private void consolePrintLn(String string) {
		mUtilShimmer.consolePrintLn(mUniqueId + "\t" + string);
	}

	@Override
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode) {
		mVerboseMode = verboseMode;
		mIsDebugMode = isDebugMode;
		mUtilShimmer.setVerboseMode(verboseMode);
	}

	@Override
	public boolean bytesAvailableToBeRead() throws ShimmerException {
		try {
			if(mSerialPort != null){
				if (mSerialPort.bytesAvailable() != 0){
					return true;
				}
			}
		} catch (Exception e) {
			ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
		return false;
	}

	@Override
	public int availableBytes() throws ShimmerException {
		try {
			if(mSerialPort != null){
				return mSerialPort.bytesAvailable();
			}
			else{
				return 0;
			}
		} catch (Exception ex) {
			ShimmerException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDeviceException(ex.getMessage(), ex.getStackTrace());
			throw(de);
		}
	}

	@Override
	public boolean isConnected() {
		return mSerialPort != null && mSerialPort.isOpen();
	}

	@Override
	public boolean isDisonnected() {
		return mSerialPort == null || !mSerialPort.isOpen();
	}

	public SerialPort getSerialPort(){
		return mSerialPort;
	}

	public boolean isSetRtsOnConnect() {
		return setRtsOnConnect;
	}

	public void setRtsOnConnect(boolean setRtsOnConnect) {
		this.setRtsOnConnect = setRtsOnConnect;
	}

	public boolean isSetDtrOnConnect() {
		return setDtrOnConnect;
	}

	public void setDtrOnConnect(boolean setDtrOnConnect) {
		this.setDtrOnConnect = setDtrOnConnect;
	}
}
