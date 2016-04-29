package com.shimmerresearch.comms.serialPortInterface;

import java.util.concurrent.ExecutionException;


import com.shimmerresearch.driver.DeviceException;
//import com.shimmerresearch.comms.wiredProtocol.DeviceException;
//import com.shimmerresearch.comms.wiredProtocol.ErrorCodesWiredProtocol;
//import com.shimmerresearch.comms.wiredProtocol.ShimmerCommsWired;
import com.shimmerresearch.driver.UtilShimmer;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

/**
 * @author Mark Nolan
 *
 */
public class ShimmerSerialPortJssc extends SerialPortComm implements ShimmerSerialEventCallback{

	private SerialPort serialPort = null;
	private ByteLevelDataCommListener mSPL;
	public String mUniqueId = "";
	public String mComPort = "";
	private int mBaudToUse = SerialPort.BAUDRATE_115200;

	private ShimmerUartListener mShimmerUartListener;
	private boolean mIsSerialPortReaderStarted = false;
	private ShimmerSerialEventCallback mShimmerSerialEventCallback;
	
	/** 0 = normal speed (bad implementation), 1 = fast speed */
	public int mTxSpeed = 1;
	
	private boolean mIsDebugMode = true;
	private boolean mVerboseMode = true;
	private UtilShimmer mUtilShimmer = new UtilShimmer(getClass().getSimpleName(), mVerboseMode);
	
	public ShimmerSerialPortJssc(String comPort, String uniqueId, int baudToUse) {
		mUniqueId = uniqueId;
		mComPort = comPort;
		mBaudToUse = baudToUse;
        serialPort = new SerialPort(mComPort);
	}
	
	public ShimmerSerialPortJssc(String comPort, String uniqueId, int baudToUse, ShimmerSerialEventCallback shimmerSerialEventCallback) {
		this(comPort, uniqueId, baudToUse);
		registerSerialPortRxEventCallback(shimmerSerialEventCallback);
	}

	/** Opens and configures the Shimmer UART COM port
	 * @throws DeviceException 
	 */
	@Override
	public void connect() throws DeviceException {
        try {
//        	serialPort.setRTS(true);
//        	serialPort.setDTR(false);
            serialPort.openPort();//Open serial port
            serialPort.setParams(
			            		mBaudToUse, 
			            		SerialPort.DATABITS_8, 
			            		SerialPort.STOPBITS_1, 
			            		SerialPort.PARITY_NONE, 
			            		true,
			            		false);//Set params.
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            eventDeviceConnected();
        }
        catch (SerialPortException e) {
        	eventDeviceDisconnected();
        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        catch (Exception e) {
        	eventDeviceDisconnected();
        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        
		startSerialPortReader();
        
	}
	
	
	/** Closes the Shimmer UART COM port
	 * @throws DeviceException 
	 */
	@Override
	public void disconnect() throws DeviceException {
    	if(serialPort.isOpened()) {
	        try {
	        	serialPort.closePort();
	        	eventDeviceDisconnected();
			} catch (SerialPortException e) {
	        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING); 
	        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
				throw(de);
			}
    	}
    }
	
	@Override
	public void closeSafely() throws DeviceException {
		if(isSerialPortReaderStarted()){
			stopSerialPortReader();
		}
		else {
			disconnect();
		}
	}


	@Override
	public void clearSerialPortRxBuffer() throws DeviceException {
    	try {
			serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
		} catch (SerialPortException e) {
        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    	
    }

	/** Transmits a byte array to the Shimmer UART COM port
	 * @param buf the Tx buffer array
	 * @throws DeviceException 
	 */
	@Override
	public void txBytes(byte[] buf) throws DeviceException {
    	try {
        	if(mTxSpeed == 0) { // normal speed
        		for(int i = 0; i<buf.length;i++) {
        			serialPort.writeByte(buf[i]);
        			serialPort.purgePort(SerialPort.PURGE_TXCLEAR); // This doesn't make sense but was working for Consensys in Windows up to v0.4.0
        		}
        	}
        	else {  // fast speed - was causing issues with Windows but needed for OSX - Windows might be fixed now (23/03/2016)
    			serialPort.writeBytes(buf);
        	}
		} catch (SerialPortException e) {
        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_WRITING_DATA); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }    

    /** Receives a byte array from the Shimmer UART COM port
	 * @param numBytes the number of bytes to receive
	 * @return byte array of received bytes
     * @throws DeviceException 
	 * @see ShimmerUartRxCmdDetails
	 */
	@Override
	public byte[] rxBytes(int numBytes) throws DeviceException {
		try {
			byte[] rxBuf = serialPort.readBytes(numBytes, SERIAL_PORT_TIMEOUT);
			if(this.mIsDebugMode){
				System.out.println(mUniqueId + "\tSerial Port RX(" + rxBuf.length + ")" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
			}
			return rxBuf;
		} catch (SerialPortException e) {
        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_READING_DATA); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		} catch (SerialPortTimeoutException e) {
        	DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_TIMEOUT); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }
	
	public DeviceException generateException(int lowLevelError){
    	DeviceException de = new DeviceException(
    			mUniqueId, 
    			mComPort,
    			ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,
    			lowLevelError); 
    	return de;
	}
	
    public void startSerialPortReader() throws DeviceException {
    	
    	if(!mIsSerialPortReaderStarted){
            int mask = SerialPort.MASK_RXCHAR;//Prepare mask
        	mShimmerUartListener = new ShimmerUartListener(mUniqueId);
//        	mShimmerUartListener.setVerbose(mVerboseModeUart);
            try {
            	serialPort.setEventsMask(mask);//Set mask
            	serialPort.addEventListener(mShimmerUartListener);//Add SerialPortEventListener
//        		setWaitForData(mShimmerUartListener);
        		mIsSerialPortReaderStarted = true;
            } catch (SerialPortException e) {
        		mIsSerialPortReaderStarted = false;
    			DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_READER_START);
    			de.updateDeviceException(e.getMessage(), e.getStackTrace());
    			throw(de);
            }
    	}
    }

    public void stopSerialPortReader() throws DeviceException {
        try {
        	serialPort.removeEventListener();
        } catch (SerialPortException e) {
			DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_READER_STOP);
			de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        
    	try {
    		// Finish up
    		disconnect();
    		mIsSerialPortReaderStarted = false;
		} catch (ExecutionException e) {
			DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_READER_STOP);
			de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}

    }
    
    @Override
	public boolean isSerialPortReaderStarted(){
		return mIsSerialPortReaderStarted;
	}
	
	public class ShimmerUartListener implements SerialPortEventListener {
//	    StringBuilder stringBuilder = new StringBuilder(128);
//	    
	    String mUniqueId = "";
	    boolean mIsParserBusy = false;

	    ShimmerUartListener(String uniqueID) {
	    	mUniqueId = uniqueID;
	    }

		@Override
		public void serialEvent(SerialPortEvent event) {
	        if (event.isRXCHAR()) {//If data is available
	        	int eventLength = event.getEventValue();
	        	//Check bytes count in the input buffer. 
//	            if (eventLength > 3) { // was 0 but at least 3 gives a little filter
	            if (!mIsParserBusy && eventLength>0) { // was 0 but at least 3 gives a little filter
	            	mIsParserBusy = true;
	            	serialPortRxEvent(eventLength);
//	            	mShimmerUart.serialPortRxEvent(eventLength);
	            	
	            	mIsParserBusy = false;
	            }
	        }
		}

	}
	
	@Override
	public void serialPortRxEvent(int byteLength) {
		//	consolePrintLn("Complete RX Received(" + packet.length + "):" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
		if(mShimmerSerialEventCallback!=null){
			mShimmerSerialEventCallback.serialPortRxEvent(byteLength);
		}
	}
	
	@Override
	public void registerSerialPortRxEventCallback(ShimmerSerialEventCallback shimmerSerialEventCallback) {
		mShimmerSerialEventCallback = shimmerSerialEventCallback;
	}

//	public void sendRxCallback(byte[] packet, long timestampMs){
////		consolePrintLn("Complete RX Received(" + packet.length + "):" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
//		if(mUartRxCallback!=null){
//			mUartRxCallback.newMsg(packet, timestampMs);
//		}
//	}
//
//	public void registerRxCallback(UartRxCallback uartRxCallback) {
//		mUartRxCallback = uartRxCallback;
//	}

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
	public boolean bytesAvailableToBeRead() throws DeviceException {
		try {
			if(serialPort != null){
				if (serialPort.getInputBufferBytesCount()!=0){
					return true;
				}
			}

		} catch (SerialPortException e) {
			DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDeviceException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
		return false;
	}

	@Override
	public int availableBytes() throws DeviceException {
		try {
			if(serialPort != null){
				return serialPort.getInputBufferBytesCount();
			}
			else{
				return 0;
			}
			
		} catch (SerialPortException ex) {
			DeviceException de = generateException(ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDeviceException(ex.getMessage(), ex.getStackTrace());
			throw(de);
		}
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return serialPort.isOpened();
	}

	@Override
	public boolean isDisonnected() {
		// TODO Auto-generated method stub
		return serialPort.isOpened();
	}

	@Override
	public void eventDeviceConnected() {
		// TODO Auto-generated method stub
		if (mSPL!=null){
			mSPL.eventConnected();
		}
	}

	@Override
	public void eventDeviceDisconnected() {
		// TODO Auto-generated method stub
		if (mSPL!=null){
			mSPL.eventDisconnected();
		}
	}

	@Override
	public void setByteLevelDataCommListener(ByteLevelDataCommListener spl) {
		// TODO Auto-generated method stub
		mSPL = spl;
	}

}
