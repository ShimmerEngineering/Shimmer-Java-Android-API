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
	private UartRxCallback mUartRxCallback;
	
	/** 0 = normal speed (bad implementation), 1 = fast speed */
	public int mTxSpeed = 1;
	
	private boolean mVerboseMode = false;
	private UtilShimmer mUtilShimmer = new UtilShimmer(getClass().getSimpleName(), mVerboseMode);
	private boolean mIsDebugMode = false;

	public ShimmerUartJssc(String comPort, String uniqueId, int baudToUse) {
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
        
		if(!isSerialPortReaderStarted()){
			startSerialPortReader();
		}
        
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
//			return serialPort.readBytes(numBytes, ShimmerUart.SERIAL_PORT_TIMEOUT);
			return internalShimmerUartRxBytes(numBytes);
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
	
	private byte[] internalShimmerUartRxBytes(int numBytes) throws SerialPortException, SerialPortTimeoutException{
		byte[] rxBuf = serialPort.readBytes(numBytes, ShimmerUart.SERIAL_PORT_TIMEOUT);
		if(this.mIsDebugMode ){
			System.out.println(mUniqueId + "\tSerial Port RX(" + rxBuf.length + ")" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
		}
		return rxBuf;
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
    	try {
    		// Open COM port
    		mIsSerialPortReaderStarted = true;
    		shimmerUartConnect();
		} catch (ExecutionException e) {
    		mIsSerialPortReaderStarted = false;
		}
    	
        int mask = SerialPort.MASK_RXCHAR;//Prepare mask
        try {
        	mShimmerUartListener = new ShimmerUartListener(mUniqueId);
//        	mShimmerUartListener.setVerbose(mVerboseModeUart);
        	serialPort.setEventsMask(mask);//Set mask
        	serialPort.addEventListener(mShimmerUartListener);//Add SerialPortEventListener
//    		setWaitForData(mShimmerUartListener);
        } catch (SerialPortException e) {
//			DockException de = new DockException(mDockID,mSmartDockUARTComPort,ErrorCodesDock.DOCKUART_PORT_EXCEPTON_ERR,ErrorCodesDock.DOCKUART_PORT_EXCEPTON_ERR_PORT_READER_START);
//			de.updateDockException(e.getMessage(), e.getStackTrace());
//			throw(de);
        }

    }

    public void stopSerialPortReader() throws DockException {
        try {
        	serialPort.removeEventListener();
        } catch (SerialPortException e) {
//			DockException de = new DockException(mDockID,mSmartDockUARTComPort,ErrorCodesDock.DOCKUART_PORT_EXCEPTON_ERR,ErrorCodesDock.DOCKUART_PORT_EXCEPTON_ERR_PORT_READER_STOP);
//			de.updateDockException(e.getMessage(), e.getStackTrace());
        }
        
    	try {
    		// Finish up
    		shimmerUartDisconnect();
    		mIsSerialPortReaderStarted = false;
		} catch (ExecutionException e) {
//			DockException de = new DockException(mDockID,mSmartDockUARTComPort,ErrorCodesDock.DOCKUART_PORT_EXCEPTON_ERR,ErrorCodesDock.DOCKUART_PORT_EXCEPTON_ERR_PORT_READER_STOP);
//			de.updateDockException(e.getMessage(), e.getStackTrace());
		}

    }
    
	public boolean isSerialPortReaderStarted(){
		return mIsSerialPortReaderStarted;
	}
	
	public class ShimmerUartListener implements SerialPortEventListener {
	    StringBuilder stringBuilder = new StringBuilder(128);
	    
	    String mUniqueId = "";

	    ShimmerUartListener(String uniqueID) {
	    	mUniqueId = uniqueID;
	    }

		@Override
		public void serialEvent(SerialPortEvent event) {
	        if (event.isRXCHAR()) {//If data is available
	        	//New Shimmer UART messages must have a header, command and payload length
//	            if (event.getEventValue() > 0) {//Check bytes count in the input buffer. 
	            if (event.getEventValue() > 3) {//Check bytes count in the input buffer. 
	                try {
	                	byte[] rxBuf = internalShimmerUartRxBytes(event.getEventValue());

//	                	byte[] header = serialPort.readBytes(3, ShimmerUart.SERIAL_PORT_TIMEOUT);

	                	consolePrintLn("serialEvent Received" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
	            		
	                	processRxBuf(rxBuf);
	                	
	                } catch (Exception ex) {
	                	consolePrintLn("Serial port ERROR");
	                    System.out.println(ex);
	                }
	            }
	        }
		}

		//TODO this does not handle if multiple messages are in rxBuf
		private void processRxBuf(byte[] rxBuf) throws SerialPortException, SerialPortTimeoutException {
			//Should be 3 anyway because of previous event.getEventValue() check 
    		if(rxBuf.length>=3){
    			byte headerByte = rxBuf[0]; 
            	if(headerByte==0x24){
            		long timestampMs = System.currentTimeMillis();
            		
        			byte cmdByte = rxBuf[1]; 
        			int payloadLength = rxBuf[2]&0xFF;
        			
//        			if(payloadLength<0){
//        				System.err.println("ERR\t" + payloadLength);
//        			}

        			int remainingByteCount = 0;
            		if(cmdByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
            			remainingByteCount = payloadLength - (rxBuf.length - UartPacketDetails.PACKET_OVERHEAD_RESPONSE_DATA);
            		}
            		else if(cmdByte == UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()
            				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_ARG_RESPONSE.toCmdByte()
            				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_CMD_RESPONSE.toCmdByte()
            				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_CRC_RESPONSE.toCmdByte()){
            			remainingByteCount = UartPacketDetails.PACKET_OVERHEAD_RESPONSE_OTHER - rxBuf.length;
            		}
            		else{
            			consolePrintLn("Unknown command: " + cmdByte);
            		}
            		
        			readRemainingBytes(rxBuf, remainingByteCount, timestampMs);
        			
            		//TODO add remaining bytes to start of next serial port read
            	}
            	else{ 
            		//TODO remove byte 0 and add remaining bytes to start of next serial port read
            	}
    		}
        	else{ 
        		//TODO add remaining bytes to start of next serial port read
        	}
		}

		private void readRemainingBytes(byte[] rxBuf, int remainingByteCount, long timestampMs) throws SerialPortException, SerialPortTimeoutException {
			// More bytes remaining in buffer, read the remainder and then send callback
			if(remainingByteCount>0){
				byte[] data = internalShimmerUartRxBytes(remainingByteCount);
    			finishedRxRead(rxBuf, data, timestampMs);
			}
			else {
				//Nothing left in buffer so send callback straight away
				sendRxCallback(rxBuf, timestampMs);
			}
		}
	}

	private void finishedRxRead(byte[] header, byte[] data, long timestampMs) {
		if(header.length>0 && data.length>0){
			byte[] packet = new byte[header.length + data.length];
			
			System.arraycopy(header, 0, packet, 0, header.length);
			System.arraycopy(data, 0, packet, header.length, data.length);
			
			sendRxCallback(packet, timestampMs);
		}
		else{
			System.err.print("ERROR\tHeader Length:" + header.length + "\tData Length:" + data.length);
		}
	}
	
	private void sendRxCallback(byte[] packet, long timestampMs){
		consolePrintLn("Complete RX Received" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
		if(mUartRxCallback!=null){
			mUartRxCallback.newMsg(packet, timestampMs);
		}
	}

	@Override
	public void registerRxCallback(UartRxCallback uartRxCallback) {
		mUartRxCallback = uartRxCallback;
	}

	private void consolePrintLn(String string) {
		mUtilShimmer.consolePrintLn(mUniqueId + "\t" + string);
	}

}
