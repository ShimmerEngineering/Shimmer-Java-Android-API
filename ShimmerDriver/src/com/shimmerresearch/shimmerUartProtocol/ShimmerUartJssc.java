package com.shimmerresearch.shimmerUartProtocol;

import java.util.concurrent.ExecutionException;

import org.omg.CORBA.REBIND;

import com.shimmerresearch.driver.UtilShimmer;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ShimmerUartJssc implements ShimmerUartOsInterface {

	private SerialPort serialPort = null;
	public String mUniqueId = "";
	public String mComPort = "";
	private int mBaudToUse = SerialPort.BAUDRATE_115200;

	private ShimmerUartListener mShimmerUartListener;
	private boolean mIsSerialPortReaderStarted = false;
//	private UartRxCallback mUartRxCallback;
	
	/** 0 = normal speed (bad implementation), 1 = fast speed */
	public int mTxSpeed = 1;
	
	private boolean mIsDebugMode = true;
	private boolean mVerboseMode = true;
	private UtilShimmer mUtilShimmer = new UtilShimmer(getClass().getSimpleName(), mVerboseMode);
	
//	byte[] carriedRxBuf = new byte[]{};
	private ShimmerUart mShimmerUart;

	public ShimmerUartJssc(ShimmerUart shimmerUart, String comPort, String uniqueId, int baudToUse) {
		this.mShimmerUart = shimmerUart;
		
		mUniqueId = uniqueId;
		mComPort = comPort;
		mBaudToUse = baudToUse;
		
        serialPort = new SerialPort(mComPort);
	}

	/** Opens and configures the Shimmer UART COM port
	 * @throws DockException 
	 */
	@Override
	public void shimmerUartConnect() throws DockException {
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
        }
        catch (SerialPortException e) {
        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        catch (Exception e) {
        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        
		startSerialPortReader();
        
	}
	
	
	/** Closes the Shimmer UART COM port
	 * @throws DockException 
	 */
	@Override
	public void shimmerUartDisconnect() throws DockException {
    	if(serialPort.isOpened()) {
	        try {
	        	serialPort.closePort();
			} catch (SerialPortException e) {
	        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING); 
	        	de.updateDockException(e.getMessage(), e.getStackTrace());
				throw(de);
			}
    	}
    }
	
	@Override
	public void closeSafely() throws DockException {
		if(isSerialPortReaderStarted()){
			stopSerialPortReader();
		}
		else {
			shimmerUartDisconnect();
		}
	}


	@Override
	public void clearSerialPortRxBuffer() throws DockException {
    	try {
			serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
		} catch (SerialPortException e) {
        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    	
    }

	/** Transmits a byte array to the Shimmer UART COM port
	 * @param buf the Tx buffer array
	 * @throws DockException 
	 */
	@Override
	public void shimmerUartTxBytes(byte[] buf) throws DockException {
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
        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_WRITING_DATA); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }    

    /** Receives a byte array from the Shimmer UART COM port
	 * @param numBytes the number of bytes to receive
	 * @return byte array of received bytes
     * @throws DockException 
	 * @see ShimmerUartRxCmdDetails
	 */
	@Override
	public byte[] shimmerUartRxBytes(int numBytes) throws DockException {
		try {
			byte[] rxBuf = serialPort.readBytes(numBytes, ShimmerUart.SERIAL_PORT_TIMEOUT);
			if(this.mIsDebugMode){
				System.out.println(mUniqueId + "\tSerial Port RX(" + rxBuf.length + ")" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
			}
			return rxBuf;
		} catch (SerialPortException e) {
        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_READING_DATA); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		} catch (SerialPortTimeoutException e) {
        	DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }
	
	private DockException generateException(int lowLevelError){
    	DockException de = new DockException(
    			mUniqueId, 
    			mComPort,
    			ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,
    			lowLevelError); 
    	return de;
	}
	
    public void startSerialPortReader() throws DockException {
    	
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
    			DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_READER_START);
    			de.updateDockException(e.getMessage(), e.getStackTrace());
    			throw(de);
            }
    	}
    }

    public void stopSerialPortReader() throws DockException {
        try {
        	serialPort.removeEventListener();
        } catch (SerialPortException e) {
			DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_READER_STOP);
			de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        
    	try {
    		// Finish up
    		shimmerUartDisconnect();
    		mIsSerialPortReaderStarted = false;
		} catch (ExecutionException e) {
			DockException de = generateException(ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_READER_STOP);
			de.updateDockException(e.getMessage(), e.getStackTrace());
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
//	            	serialPortRxEvent(eventLength);
	            	mShimmerUart.serialPortRxEvent(eventLength);
	            	mIsParserBusy = false;
	            }
	        }
		}
		
//		private void serialPortRxEvent(int eventLength){
//            try {
//            	byte[] rxBuf = internalShimmerUartRxBytes(eventLength);
//            	consolePrintLn("serialEvent Received(" + rxBuf.length + "):" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
//            	processRxBuf(rxBuf);
//            	
//            } catch (Exception ex) {
//            	//TODO improve error handling here
//            	consolePrintLn("Serial port ERROR");
//                System.out.println(ex.getMessage());
//                ex.printStackTrace();
//            }
//		}
//
//		//TODO move up a level to ShimmerUART
//		private void processRxBuf(byte[] rxBuf) throws SerialPortException, SerialPortTimeoutException {
//			
//			byte headerByte = rxBuf[0];
//        	if(headerByte==UartPacketDetails.PACKET_HEADER.toCharArray()[0]){
//        		long timestampMs = System.currentTimeMillis();
//        		
//				// 1) check length before proceeding, need to have at least the
//				// command byte. 3rd byte is just useful for data response
//        		if(rxBuf.length<3){
//        			int lengthToRead = 3-rxBuf.length;
//        			byte[] tempBuf = internalShimmerUartRxBytes(lengthToRead);
//        			rxBuf = combineByteArrays(rxBuf, tempBuf);
//        		}
//        		
//    			byte cmdByte = rxBuf[1]; 
//    			boolean continueWithParsing = true;
//    			
//				// 2) Determine how many more bytes the current packet needs to
//				// proceed with parsing.
//    			int expectedResponseLength = 0;
//    			if(continueWithParsing){
//            		if(cmdByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
//            			int payloadLength = rxBuf[2]&0xFF;
//            			// handle invalid payload length
//            			if(payloadLength<0){
//                			consolePrintLn("Invalid payload length: " + payloadLength);
//                			removeFirstByteAndCarry(rxBuf);
//                			continueWithParsing = false;
//            			}
//            			else {
//                			expectedResponseLength = UartPacketDetails.PACKET_OVERHEAD_RESPONSE_DATA + payloadLength;
//            			}
//            		}
//            		else if(cmdByte == UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()
//            				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_ARG_RESPONSE.toCmdByte()
//            				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_CMD_RESPONSE.toCmdByte()
//            				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_CRC_RESPONSE.toCmdByte()){
//            			expectedResponseLength = UartPacketDetails.PACKET_OVERHEAD_RESPONSE_OTHER;
//            		}
//            		else{
//            			consolePrintLn("Unknown command: " + cmdByte);
//            			removeFirstByteAndCarry(rxBuf);
//            			continueWithParsing = false;
//            		}
//    			}
//        		
//				// 3) Make sure the current packet is complete - read any unread
//				// bytes for the current packet and carry over any extra bytes
//				// to the next packet
//    			byte[] packet = null;
//        		if(continueWithParsing){
//            		if(rxBuf.length==expectedResponseLength){
//            			packet = rxBuf;
//            		}
//            		else if(rxBuf.length>expectedResponseLength){
//            			packet = new byte[expectedResponseLength];
//            			System.arraycopy(rxBuf, 0, packet, 0, expectedResponseLength);
//            			
//                		// add remaining bytes to start of next serial port read
//            			int carriedOverLenth = rxBuf.length-expectedResponseLength;
//                		carriedRxBuf = new byte[carriedOverLenth];
//            			System.arraycopy(rxBuf, expectedResponseLength, carriedRxBuf, 0, carriedOverLenth);
//            			
//            			if(mIsDebugMode){
//                			consolePrintLn("Overflow: All bytes(" + rxBuf.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
//                			consolePrintLn("Overflow: 1st packet(" + packet.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
//                			consolePrintLn("Overflow: 2nd packet(" + carriedRxBuf.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(carriedRxBuf));
//                			consolePrintLn("");
//            			}
//            		}
//            		else if(rxBuf.length<expectedResponseLength){
//        				byte[] data = internalShimmerUartRxBytes(expectedResponseLength-rxBuf.length);
//            			packet = combineByteArrays(rxBuf, data);
//            			if(mIsDebugMode && packet!=null){
//                			consolePrintLn("Underflow: 1st buf(" + rxBuf.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
//                			consolePrintLn("Underflow: 2st buf(" + data.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(data));
//                			consolePrintLn("Underflow: Combined(" + packet.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
//                			consolePrintLn("");
//            			}
//            		}
//        		}
//
//        		// 4) TODO check CRC before sending callback. If fails then remove first byte and carry forward
//        		
//        		// 5) Current packet is good and ready to pass to upper levels
//        		if(continueWithParsing && packet!=null){
//        			sendRxCallback(packet, timestampMs);
//        		}
//        	}
//        	else{ 
//        		//remove first and add remaining bytes to start of next serial port read
//        		removeFirstByteAndCarry(rxBuf);
//        	}
//    		
//    		// Attempt to re-process any remaining bytes
//    		if(carriedRxBuf.length>0){
//    			byte[] tempBuf = carriedRxBuf; 
//    			carriedRxBuf = new byte[]{};
//    			processRxBuf(tempBuf);
//    		}
//
//		}
//
//		/** remove first and add remaining bytes to start of next serial port read */
//		private void removeFirstByteAndCarry(byte[] rxBuf){
//    		int lengthToCarry = rxBuf.length-1;
//    		carriedRxBuf = new byte[lengthToCarry];
//			System.arraycopy(rxBuf, 1, carriedRxBuf, 0, lengthToCarry);
//		}
	}

//	@Override
//	public void sendRxCallback(byte[] packet, long timestampMs){
//		consolePrintLn("Complete RX Received(" + packet.length + "):" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
//		if(mUartRxCallback!=null){
//			mUartRxCallback.newMsg(packet, timestampMs);
//		}
//	}
//
//	@Override
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

}
