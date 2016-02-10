package com.shimmerresearch.shimmerUartProtocol;

import java.util.concurrent.ExecutionException;

import com.shimmerresearch.driver.UtilShimmer;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ShimmerUartJssc implements ShimmerUartOsInterface {

	private final static int SHIMMER_UART_BAUD_RATE = SerialPort.BAUDRATE_115200;
	private SerialPort serialPort = null;
	public String mUniqueId = "";
	public String mComPort = "";

	private ShimmerUartListener mShimmerUartListener;
	private boolean mIsSerialPortReaderStarted = false;
	private UartRxCallback mUartRxCallback;
	
	private UtilShimmer utilShimmer = new UtilShimmer(getClass().getSimpleName(), false);

	public ShimmerUartJssc(String comPort, String uniqueId) {
		mUniqueId = uniqueId;
		mComPort = comPort;
		
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
			            		SHIMMER_UART_BAUD_RATE, 
			            		SerialPort.DATABITS_8, 
			            		SerialPort.STOPBITS_1, 
			            		SerialPort.PARITY_NONE, 
			            		true,
			            		false);//Set params.
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        }
        catch (SerialPortException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        catch (Exception e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
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
	        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING); 
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
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
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
    	//TODO: pass rxSpeed from root method
    	int rxSpeed = 0;
    	if(rxSpeed == 0) { // normal speed
        	try {
        		for(int i = 0; i<buf.length;i++) {
        			serialPort.writeByte(buf[i]);
        			serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
        		}
    		} catch (SerialPortException e) {
            	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_WRITING_DATA); 
            	de.updateDockException(e.getMessage(), e.getStackTrace());
    			throw(de);
    		}
    	}
    	else { // fast speed
        	try {
    			serialPort.writeBytes(buf);
    		} catch (SerialPortException e) {
//    			e.printStackTrace();
            	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_WRITING_DATA); 
            	de.updateDockException(e.getMessage(), e.getStackTrace());
    			throw(de);
    		}
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
			return serialPort.readBytes(numBytes, ShimmerUart.SERIAL_PORT_TIMEOUT);
		} catch (SerialPortException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_READING_DATA); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		} catch (SerialPortTimeoutException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
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
	            if (event.getEventValue() > 0) {//Check bytes count in the input buffer
	                try {
	                	byte[] rxBuf = serialPort.readBytes(event.getEventValue(), ShimmerUart.SERIAL_PORT_TIMEOUT);

//	                	byte[] header = serialPort.readBytes(3, ShimmerUart.SERIAL_PORT_TIMEOUT);

	                	consolePrintLn("serialEvent Received" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
	            		
	            		if(rxBuf.length>=3){
	            			byte headerByte = rxBuf[0]; 
		                	if(headerByte==0x24){
		            			byte cmdByte = rxBuf[1]; 
		            			int payloadLength = rxBuf[2]&0xFF;
		            			
//		            			if(payloadLength<0){
//		            				System.err.println("ERR\t" + payloadLength);
//		            			}

		                		if(cmdByte == UartPacketDetails.PACKET_CMD.DATA_RESPONSE.toCmdByte()){
		                			int remainingByteCount = payloadLength - (rxBuf.length - 5);
		                			readRemainingBytes(rxBuf, remainingByteCount);
		                		}
		                		else if(cmdByte == UartPacketDetails.PACKET_CMD.ACK_RESPONSE.toCmdByte()
		                				|| cmdByte == UartPacketDetails.PACKET_CMD.BAD_ARG_RESPONSE.toCmdByte()
		                				|| cmdByte == UartPacketDetails.PACKET_CMD.BAD_CMD_RESPONSE.toCmdByte()
		                				|| cmdByte == UartPacketDetails.PACKET_CMD.BAD_CRC_RESPONSE.toCmdByte()){
		                			int remainingByteCount = 4 - rxBuf.length;
		                			readRemainingBytes(rxBuf, remainingByteCount);
		                		}
		                	}
	            		}


	                	
//	                    for (int i = 0; i < data.length; i++) {
//	                        stringBuilder.append((char) data[i]);
//	                    }

	                    
//	                    //split entire message by "\r\n"
//	            		int lineTerminationIndex = stringBuilder.indexOf("\r\n");
////	            		System.out.println(stringBuilder);
//	            		if(lineTerminationIndex>=1) {
//	            			String message = stringBuilder.substring(0, lineTerminationIndex+2).toString();  
//	            			stringBuilder.replace(0, lineTerminationIndex+2, "");
//	            			
//	                    	if(message.length() == 3) {
//	                            // SmartDock error response - Expected message = "E\r\n"
//	                        	if ((message.charAt(0)) == 'E') {
//	                                consolePrintLn("Error = " + message);
//	                    			//notify the SmartDockUart layer
//	        						MsgDock msg = new MsgDock(MsgDock.MSG_ID_SMARTDOCK_UART_ERROR,mDockID);
//	                    			msg.mMessage = message;
//	        						sendCallBackMsg(msg.mMsgID,msg);
//	                            }
//	                    	}
//
//	            		}
	            		
	                } catch (Exception ex) {
	                    System.out.println(ex);
	                }
	            }
	        }
		}

		private void readRemainingBytes(byte[] rxBuf, int remainingByteCount) throws SerialPortException, SerialPortTimeoutException {
			if(remainingByteCount>0){
    			byte[] data = serialPort.readBytes(remainingByteCount, ShimmerUart.SERIAL_PORT_TIMEOUT);
    			sendRxCallBack(rxBuf, data);
			}
			else {
//				if(remainingByteCount!=0){
//					System.err.println("WTF\t" + remainingByteCount);
//				}
				consolePrintLn("Complete RX Received" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
		    	mUartRxCallback.newMsg(rxBuf);
			}
		}
	}

	private void sendRxCallBack(byte[] header, byte[] data) {
		if(header.length>0 && data.length>0){
			byte[] packet = new byte[header.length + data.length];
			
			System.arraycopy(header, 0, packet, 0, header.length);
			System.arraycopy(data, 0, packet, header.length, data.length);
			
    		consolePrintLn("Complete RX Received" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
	    	mUartRxCallback.newMsg(packet);
		}
	}

	@Override
	public void registerRxCallback(UartRxCallback uartRxCallback) {
		mUartRxCallback = uartRxCallback;
	}

	private void consolePrintLn(String string) {
		utilShimmer.consolePrintLn(mUniqueId + "\t" + string);
	}

}
