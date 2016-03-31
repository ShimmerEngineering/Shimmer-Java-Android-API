package com.shimmerresearch.shimmerUartProtocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import jssc.SerialPort;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.MsgDock;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.UART_PACKET_CMD;

/**Driver for managing and configuring the Shimmer through the Dock using the 
 * Shimmer's dock connected UART.
 * 
 * @author Mark Nolan
 *
 */
public abstract class ShimmerUart extends BasicProcessWithCallBack {
	
	public ShimmerUartOsInterface shimmerUartOs;
	/** Boolean only used if COM port is not left open */
	private boolean mIsUARTInUse = false;
	public String mUniqueId = "";
	public String mComPort = "";
	private int mBaudToUse = SerialPort.BAUDRATE_115200;
	
	public final static class SHIMMER_UART_BAUD_RATES{
		public final static int SHIMMER3_DOCKED = SerialPort.BAUDRATE_115200;
		public final static int SPAN = 230400;
	}
	
	byte[] carriedRxBuf = new byte[]{};

	public boolean mIsDebugMode = false;
	public boolean mVerboseMode = true;
	private UtilShimmer mUtilShimmer = new UtilShimmer(getClass().getSimpleName(), mVerboseMode);
	
	/**
	 * This boolean determines whether to leave the COM port open from the start
	 * or else open&close it for each and every read/write attempt. <br>
	 * <br>
	 * All Consensys versions up to v0.4.4 opened&closed the COM port for each
	 * operation as sometimes Shimmers would randomly fail during auto-read on
	 * the dock/bases. The open/close is time consuming. Driver improvements
	 * since v0.4.4 have meant the COM port can now be left open.
	 */
	public boolean mLeavePortOpen = true;
//	private List<UartRxPacketObject> mListOfUartRxPacketObjects = new ArrayList<UartRxPacketObject>();
	private List<UartRxPacketObject> mListOfUartRxPacketObjects = Collections.synchronizedList(new ArrayList<UartRxPacketObject>());
	public DockException mThrownException = null;
//	private UartRxCallback mUartRxCallback = null;
	private boolean mSendCallbackRxOverride = false;

    //the timeout value for connecting with the port
    protected final static int SERIAL_PORT_TIMEOUT = 500; // was 2000

	public ShimmerUart(String comPort, String uniqueId, int baudToUse){
		mComPort = comPort;
		mUniqueId = uniqueId;
		mBaudToUse = baudToUse;
		
		shimmerUartOs = new ShimmerUartJssc(this, mComPort, mUniqueId, mBaudToUse);
//		shimmerUartOs.registerRxCallback(new CallbackUartRx());
		
		setVerbose(mVerboseMode, mIsDebugMode);
		setThreadName(mUniqueId + "-" + this.getClass().getSimpleName());
	}

	public ShimmerUart(String comPort, String uniqueId){
		this(comPort, uniqueId, SHIMMER_UART_BAUD_RATES.SHIMMER3_DOCKED);
	}
	
	public boolean isUARTinUse(){
		return mIsUARTInUse;
	}
	
	public boolean isSerialPortReaderStarted(){
		return shimmerUartOs.isSerialPortReaderStarted();
	}

	public void processShimmerGetCommandNoWait(UartComponentPropertyDetails compPropDetails, int errorCode, byte[] payload) throws DockException{
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartGetCommandNoWait(compPropDetails, payload);
		} catch(DockException de) {
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
	}
	
	public void processShimmerSetCommandNoWait(UartComponentPropertyDetails compPropDetails, int errorCode, byte[] payload) throws DockException{
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartSetCommandNoWait(compPropDetails, payload);
		} catch(DockException de) {
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
	}
	
	public byte[] processShimmerGetCommand(UartComponentPropertyDetails compPropDetails, int errorCode) throws DockException{
		return processShimmerGetCommand(compPropDetails, errorCode, null);
	}

	public byte[] processShimmerGetCommand(UartComponentPropertyDetails compPropDetails, int errorCode, byte[] payload) throws DockException{
		byte[] rxBuf;
		if(!mLeavePortOpen) openSafely();
		try {
			rxBuf = shimmerUartGetCommand(compPropDetails, payload);
		} catch(DockException de) {
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
		return rxBuf;
	}

	public void processShimmerSetCommand(UartComponentPropertyDetails compPropDetails, byte[] txBuf, int errorCode) throws DockException{
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartSetCommand(compPropDetails, txBuf);
		} catch(DockException de) {
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
	}
    
	public byte[] processShimmerMemGetCommand(UartComponentPropertyDetails compPropDetails, int address, int size, int errorCode) throws DockException{
		byte[] rxBuf;
		if(!mLeavePortOpen) openSafely();
		try {
			rxBuf = shimmerUartGetMemCommand(compPropDetails, address, size);
		} catch(DockException de) {
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
		return rxBuf;
	}	
	
	public void processShimmerMemSetCommand(UartComponentPropertyDetails compPropDetails, int address, byte[] buf, int errorCode) throws DockException{
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartSetMemCommand(compPropDetails, address, buf);
		} catch(DockException de) {
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
	}	
	
	/**Opens the Shimmer UART Serial and throws a DockException if there is a 
	 * SerialPortException or general Exception  
	 * @throws DockException
	 */
	public void openSafely() throws DockException {
		try {
			shimmerUartOs.shimmerUartConnect();
		} catch (DockException de) {
			closeSafely();
			mIsUARTInUse = false;
			throw(de);
		}
		if(!mLeavePortOpen) mIsUARTInUse = true;
	}
	
	/**Closes the Shimmer UART Serial and throws a DockException if there is a 
	 * SerialPortException or general Exception  
	 * @throws DockException
	 */
	public void closeSafely() throws DockException {
		try {
			shimmerUartOs.closeSafely();
		} catch (DockException de) {
			throw(de);
		} finally{
			mIsUARTInUse = false;
		}
	}

	public byte[] uartGetCommand(UartComponentPropertyDetails cPD) throws DockException {
		return uartGetCommand(cPD, null);
	}

	/** 
	 * @return 
	 * @throws ExecutionException
	 */
	public byte[] uartGetCommand(UartComponentPropertyDetails cPD, byte[] payload) throws DockException {
		byte[] rxBuf;
		
		if(!mLeavePortOpen) openSafely();
		try {
			rxBuf = shimmerUartGetCommand(cPD, payload);
		} catch(DockException e) {
			throw(e);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
		
		return rxBuf;
	}

	/** 
	 * @return 
	 * @throws ExecutionException
	 */
	public void uartSetCommand(UartComponentPropertyDetails cPD, byte[] txBuf) throws ExecutionException {
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartSetCommand(cPD, txBuf);
		} catch(ExecutionException e) {
			throw(e);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
	}

	protected void shimmerUartGetCommandNoWait(UartComponentPropertyDetails msgArg, byte[] payload) throws DockException {
		mThrownException = null;
		txPacket(UartPacketDetails.UART_PACKET_CMD.READ, msgArg, payload);
	}
	
	protected void shimmerUartSetCommandNoWait(UartComponentPropertyDetails msgArg, byte[] payload) throws DockException {
		mThrownException = null;
		txPacket(UartPacketDetails.UART_PACKET_CMD.WRITE, msgArg, payload);
	}
	
    /**Get information from the Shimmer based on passed in message argument
     * @param msgArg a byte array of containing the Component and Property to get
     * @return byte array containing data response from Shimmer
     * @throws ExecutionException
     */
	protected byte[] shimmerUartGetCommand(UartComponentPropertyDetails msgArg, byte[] payload) throws DockException {
		return shimmerUartCommandTxRx(UartPacketDetails.UART_PACKET_CMD.READ, msgArg, payload);
    }
    
    /**Get memory data from the Shimmer based on passed in message argument
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param address memory address to read from
     * @param size the number of memory bytes to read
     * @return a byte array with the received memory bytes
     * @throws ExecutionException
     */
	protected byte[] shimmerUartGetMemCommand(UartComponentPropertyDetails msgArg, int address, int size) throws DockException {
    	
    	byte[] memLengthToRead = new byte[]{(byte) size};

    	byte[] memAddressToRead = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
    	if(msgArg==UartPacketDetails.UART_COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID) {
    		memAddressToRead = new byte[]{(byte) (address&0xFF)};
    	}
    	
    	ArrayUtils.reverse(memAddressToRead);
    	
    	byte[] payload = new byte[memLengthToRead.length + memAddressToRead.length];
    	System.arraycopy(memLengthToRead, 0, payload, 0, memLengthToRead.length);
    	System.arraycopy(memAddressToRead, 0, payload, memLengthToRead.length, memAddressToRead.length);

		return shimmerUartCommandTxRx(UartPacketDetails.UART_PACKET_CMD.READ, msgArg, payload);
    }

    /**
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
	protected byte[] shimmerUartSetCommand(UartComponentPropertyDetails msgArg, byte[] valueBuffer) throws DockException {
		clearAllAcks();
		return shimmerUartCommandTxRx(UartPacketDetails.UART_PACKET_CMD.WRITE, msgArg, valueBuffer);
    }
    
    private void clearAllAcks() {
    	synchronized (mListOfUartRxPacketObjects) {
    		for (Iterator<UartRxPacketObject> iterator = mListOfUartRxPacketObjects.iterator(); iterator.hasNext();) {
    			UartRxPacketObject uRPO = iterator.next();
    			if(uRPO.mUartCommandByte==UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()){
    				iterator.remove();
    			}
    		}
		}
	}


	/**
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param address
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
	protected byte[] shimmerUartSetMemCommand(UartComponentPropertyDetails msgArg, int address, byte[] valueBuffer) throws DockException {
    	
		byte[] memLengthToWrite = new byte[]{(byte) valueBuffer.length};
		
		byte[] memAddressToWrite = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
    	if(msgArg==UartPacketDetails.UART_COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID) {
    		memAddressToWrite = new byte[]{(byte) (address&0xFF)};
    	}
		ArrayUtils.reverse(memAddressToWrite);

		// TODO check I'm not missing the last two bytes here because the mem
		// address length is not being included in the length field
		byte[] payload = new byte[memLengthToWrite.length + memAddressToWrite.length + valueBuffer.length];
		System.arraycopy(memLengthToWrite, 0, payload, 0, memLengthToWrite.length);
		System.arraycopy(memAddressToWrite, 0, payload, memLengthToWrite.length, memAddressToWrite.length);
		System.arraycopy(valueBuffer, 0, payload, memLengthToWrite.length + memAddressToWrite.length, valueBuffer.length);
		
		clearAllAcks();
		return shimmerUartCommandTxRx(UartPacketDetails.UART_PACKET_CMD.WRITE, msgArg, payload);
    }
    
    
    /**
     * @param packetCmd
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param payload
     * @return
     * @throws ExecutionException
     */
	protected byte[] shimmerUartCommandTxRx(UART_PACKET_CMD packetCmd, UartComponentPropertyDetails msgArg, byte[] payload) throws DockException {
		mListOfUartRxPacketObjects.clear();
		
		txPacket(packetCmd, msgArg, payload);
		mThrownException = null;
		return waitForResponse(packetCmd, msgArg);
    }

	/**
	 * @param packetCmd
	 * @param msgArg
	 * @param valueBuffer
	 * @throws DockException
	 */
	private void txPacket(UART_PACKET_CMD packetCmd, UartComponentPropertyDetails msgArg, byte[] valueBuffer) throws DockException {
		consolePrintTxPacketInfo(packetCmd, msgArg, valueBuffer);
    	byte[] txPacket = assembleTxPacket(packetCmd.toCmdByte(), msgArg, valueBuffer);
    	shimmerUartOs.shimmerUartTxBytes(txPacket); 
	}

	/**
     * @param command
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param value
     * @return
     */
	protected byte[] assembleTxPacket(int command, UartComponentPropertyDetails msgArg, byte[] value) {
		byte[] header = (UartPacketDetails.PACKET_HEADER).getBytes();
		byte[] cmd = new byte[]{(byte) command};
		byte[] msgLength = new byte[]{(byte) 2};
		if(value!=null) {
			msgLength[0] += value.length;
		}
		
		byte[] txBufPreCrc;
		if(value!=null) {
			txBufPreCrc = new byte[header.length + cmd.length + msgArg.mCompPropByteArray.length + msgLength.length + value.length];
		}
		else {
			txBufPreCrc = new byte[header.length + cmd.length + msgArg.mCompPropByteArray.length + msgLength.length];
		}
		
		System.arraycopy(header, 0, txBufPreCrc, 0, header.length);
		System.arraycopy(cmd, 0, txBufPreCrc, header.length, cmd.length);
		System.arraycopy(msgLength, 0, txBufPreCrc, header.length + cmd.length, msgLength.length);
		System.arraycopy(msgArg.mCompPropByteArray, 0, txBufPreCrc, header.length + cmd.length + msgLength.length, msgArg.mCompPropByteArray.length);
		if(value!=null) {
			System.arraycopy(value, 0, txBufPreCrc, header.length + cmd.length + msgLength.length + msgArg.mCompPropByteArray.length, value.length);
		}

		byte[] calculatedCrc = ShimmerCrc.shimmerUartCrcCalc(txBufPreCrc, txBufPreCrc.length);
		
		byte[] txBufPostCrc = new byte[txBufPreCrc.length + 2]; 
		System.arraycopy(txBufPreCrc, 0, txBufPostCrc, 0, txBufPreCrc.length);
		System.arraycopy(calculatedCrc, 0, txBufPostCrc, txBufPreCrc.length, calculatedCrc.length);
		
		return txBufPostCrc;
    }
	
    
	private byte[] waitForResponse(UART_PACKET_CMD packetCmd, UartComponentPropertyDetails msgArg) throws DockException {
		boolean flag = true;
		
		int loopCount = 0;
		//100*100 = 10s
		int waitIntervalMs = 100;
		int loopCountTotal = SERIAL_PORT_TIMEOUT/waitIntervalMs;

		while(flag) {
			mUtilShimmer.millisecondDelay(waitIntervalMs);
			loopCount += 1;
			if(loopCount >= loopCountTotal) {
				break;
			}
			
			UartRxPacketObject uRPO = checkIfListContainsResponse(packetCmd, msgArg);
			if(uRPO!=null) {
				if(this.mIsDebugMode){
					consolePrintLn("RxObjectsSize=" + mListOfUartRxPacketObjects.size());
				}
				return uRPO.getPayload();
			}
			else if(mThrownException!=null){
				throw mThrownException;
			}
		}

		UartComponentPropertyDetails uCPD = UartPacketDetails.getUartPropertyParsed(msgArg.mComponent.toCmdByte(), msgArg.mPropertyByte);
		String propertyString = Integer.toString(msgArg.mProperty);
		if(uCPD!=null){
			propertyString = uCPD.mPropertyName;
		}
		consolePrintLn("TIMEOUT_FAIL" + "\tComponent:" + msgArg.mComponent.toString() + "\tProperty:" + propertyString);
		if(this.mIsDebugMode){
			consolePrintLn("RxObjectsSize=" + mListOfUartRxPacketObjects.size());
		}
		
		DockException de = new DockException(mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT, mUniqueId);
		throw(de);
	}
	
	private UartRxPacketObject checkIfListContainsResponse(UART_PACKET_CMD packetCmd, UartComponentPropertyDetails msgArg){
		if(mListOfUartRxPacketObjects.size()==0){
			return null;
		}
		
		// when iterating over a synchronized list, we need to synchronize access to the synchronized list
		synchronized (mListOfUartRxPacketObjects) {
			Iterator<UartRxPacketObject> iterator = mListOfUartRxPacketObjects.iterator();
			while (iterator.hasNext()) {
				UartRxPacketObject uRPO = iterator.next();
				if(packetCmd==UartPacketDetails.UART_PACKET_CMD.WRITE
						&& uRPO.mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()){
//					iterator.remove();
					return uRPO;
				}
				else if((packetCmd==UartPacketDetails.UART_PACKET_CMD.READ)
						&& uRPO.mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()
						&& msgArg.mComponentByte==uRPO.mUartComponentByte
						&& msgArg.mPropertyByte==uRPO.mUartPropertyByte){
//					iterator.remove();
					return uRPO;
				}
			}
		}

//		for (Iterator<UartRxPacketObject> uRPOIter = mListOfUartRxPacketObjects.iterator(); uRPOIter.hasNext();) {
//			UartRxPacketObject uRPO = uRPOIter.next();
//			if(packetCmd==UartPacketDetails.UART_PACKET_CMD.WRITE
//					&& uRPO.mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()){
//				uRPOIter.remove();
//				return uRPO;
//			}
//			else if((packetCmd==UartPacketDetails.UART_PACKET_CMD.READ)
//					&& uRPO.mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()
//					&& msgArg.mComponentByte==uRPO.mUartComponentByte
//					&& msgArg.mPropertyByte==uRPO.mUartPropertyByte){
//				uRPOIter.remove();
//				return uRPO;
//			}
//		}
		return null;
	}

	private void processUnexpectedResponse(UartRxPacketObject uRPO) throws DockException {
		// Response is not the expected response type
		byte commandByte = uRPO.mUartCommandByte;
		
		if(commandByte!=UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte() 
				&& commandByte!=UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
			if(commandByte == UartPacketDetails.UART_PACKET_CMD.BAD_CMD_RESPONSE.toCmdByte()) {
				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD);
			}
			else if(commandByte == UartPacketDetails.UART_PACKET_CMD.BAD_ARG_RESPONSE.toCmdByte()) {
				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG);
			}
			else if(commandByte == UartPacketDetails.UART_PACKET_CMD.BAD_CRC_RESPONSE.toCmdByte()) {
				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC);
			}
			else {
				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
			}
		}
	}
	
	protected void serialPortRxEvent(int eventLength){
        try {
        	byte[] rxBuf = shimmerUartOs.shimmerUartRxBytes(eventLength);
        	if(mIsDebugMode){
            	consolePrintLn("serialEvent Received(" + rxBuf.length + "):" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
        	}
        	processRxBuf(rxBuf);
        } catch (Exception ex) {
        	//TODO improve error handling here
        	consolePrintLn("Serial port ERROR");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
	}

	private void processRxBuf(byte[] rxBuf) throws DockException {
		
		byte headerByte = rxBuf[0];
    	if(headerByte==UartPacketDetails.PACKET_HEADER.toCharArray()[0]){
    		long timestampMs = System.currentTimeMillis();
    		
			// 1) check length before proceeding, need to have at least the
			// command byte. 3rd byte is just useful for data response
    		if(rxBuf.length<3){
    			int lengthToRead = 3-rxBuf.length;
    			byte[] tempBuf = shimmerUartOs.shimmerUartRxBytes(lengthToRead);
    			rxBuf = combineByteArrays(rxBuf, tempBuf);
    		}
    		
			byte cmdByte = rxBuf[1]; 
			boolean continueWithParsing = true;
			
			// 2) Determine how many more bytes the current packet needs to
			// proceed with parsing.
			int expectedResponseLength = 0;
			if(continueWithParsing){
        		if(cmdByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
        			int payloadLength = rxBuf[2]&0xFF;
        			// handle invalid payload length
        			if(payloadLength<0){
            			consolePrintLn("Invalid payload length: " + payloadLength);
            			carriedRxBuf = removeFirstByteFromArray(rxBuf);
            			continueWithParsing = false;
        			}
        			else {
            			expectedResponseLength = UartPacketDetails.PACKET_OVERHEAD_RESPONSE_DATA + payloadLength;
        			}
        		}
        		else if(cmdByte == UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()
        				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_ARG_RESPONSE.toCmdByte()
        				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_CMD_RESPONSE.toCmdByte()
        				|| cmdByte == UartPacketDetails.UART_PACKET_CMD.BAD_CRC_RESPONSE.toCmdByte()){
        			expectedResponseLength = UartPacketDetails.PACKET_OVERHEAD_RESPONSE_OTHER;
        		}
        		else{
        			consolePrintLn("Unknown command: " + cmdByte);
        			carriedRxBuf = removeFirstByteFromArray(rxBuf);
        			continueWithParsing = false;
        		}
			}
    		
			// 3) Make sure the current packet is complete - read any unread
			// bytes for the current packet and carry over any extra bytes
			// to the next packet
			byte[] packet = null;
    		if(continueWithParsing){
        		if(rxBuf.length==expectedResponseLength){
        			packet = rxBuf;
        		}
        		else if(rxBuf.length>expectedResponseLength){
        			packet = new byte[expectedResponseLength];
        			System.arraycopy(rxBuf, 0, packet, 0, expectedResponseLength);
        			
            		// add remaining bytes to start of next serial port read
        			int carriedOverLenth = rxBuf.length-expectedResponseLength;
            		carriedRxBuf = new byte[carriedOverLenth];
        			System.arraycopy(rxBuf, expectedResponseLength, carriedRxBuf, 0, carriedOverLenth);
        			
        			if(mIsDebugMode){
            			consolePrintLn("Overflow: All bytes(" + rxBuf.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
            			consolePrintLn("Overflow: 1st packet(" + packet.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
            			consolePrintLn("Overflow: 2nd packet(" + carriedRxBuf.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(carriedRxBuf));
            			consolePrintLn("");
        			}
        		}
        		else if(rxBuf.length<expectedResponseLength){
    				byte[] data = shimmerUartOs.shimmerUartRxBytes(expectedResponseLength-rxBuf.length);
        			packet = combineByteArrays(rxBuf, data);
        			if(mIsDebugMode && packet!=null){
            			consolePrintLn("Underflow: 1st buf(" + rxBuf.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
            			consolePrintLn("Underflow: 2st buf(" + data.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(data));
            			consolePrintLn("Underflow: Combined(" + packet.length + "):\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packet));
            			consolePrintLn("");
        			}
        		}
    		}

    		// 4) Parse current packet - verify CRC and then pass to upper levels
    		if(continueWithParsing && packet!=null){
    			try {
    				parseSinglePacket(packet, timestampMs);
				} catch (DockException e) {
					mThrownException = e;
					e.printStackTrace();
				}
    		}
    	}
    	else{ 
    		//remove first and add remaining bytes to start of next serial port read
    		carriedRxBuf = removeFirstByteFromArray(rxBuf);
    	}
		
		// Attempt to re-process any remaining bytes
		if(carriedRxBuf.length>0){
			byte[] tempBuf = carriedRxBuf; 
			carriedRxBuf = new byte[]{};
			processRxBuf(tempBuf);
		}

	}

	/** */
	private void parseSinglePacket(byte[] rxBuf, long timestampMs) throws DockException {
		UartRxPacketObject uRPO = new UartRxPacketObject(rxBuf, timestampMs);
		
		//check CRC before sending callback. If fails then remove first byte and carry forward
		try {
			// Check CRC
			if(!ShimmerCrc.shimmerUartCrcCheck(uRPO.mRxPacket)) {
				consolePrintLn("RX\tERR_CRC");
				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC);
			}
			consolePrintRxPacketInfo(uRPO);
		} catch (DockException dE){
			//Problem with the CRC, remove first byte and continue processing
			byte[] tempBuf = removeFirstByteFromArray(rxBuf);
			insertToCarrierBuffer(tempBuf);
			throw(dE);
		}
		
		processParsedPacket(uRPO, timestampMs);
	}
	
	private void processParsedPacket(UartRxPacketObject uRPO, long timestampMs) throws DockException {
		try {
			//handle 'bad' responses
			try{
				processUnexpectedResponse(uRPO);
			} catch(DockException de){
				throw de;
			}

			if(mSendCallbackRxOverride){
				wrapMsgSpanAndSend(MsgDock.MSG_ID_SHIMMERUART_PACKET_RX, uRPO);
			}
			else {
		    	synchronized (mListOfUartRxPacketObjects) {
		    		mListOfUartRxPacketObjects.add(uRPO);
		    	}
			}
		} catch (DockException dE){
			throw(dE);
		} finally{
			//If managed properly it should never get here
			if(uRPO.mLeftOverBytes!=null){
				insertToCarrierBuffer(uRPO.mLeftOverBytes);
			}
		}
	} 
	
	/** remove first and add remaining bytes to start of next serial port read 
	 * @return */
	private byte[] removeFirstByteFromArray(byte[] rxBuf){
		int lengthToCarry = rxBuf.length-1;
		byte[] processedArray = new byte[lengthToCarry];
		System.arraycopy(rxBuf, 1, processedArray, 0, lengthToCarry);
		return processedArray;
	}
	
	private byte[] combineByteArrays(byte[] buf1, byte[] buf2) {
		boolean condition1 = (buf1!=null && buf1.length>0)? true:false;
		boolean condition2 = (buf2!=null && buf2.length>0)? true:false;
		
		if(condition1 && condition2){
			byte[] packet = new byte[buf1.length + buf2.length];
			System.arraycopy(buf1, 0, packet, 0, buf1.length);
			System.arraycopy(buf2, 0, packet, buf1.length, buf2.length);
			return packet;
		}
		else if(condition1){
			return buf1;
		}
		else if(condition2){
			return buf2;
		}
		
		consolePrintLn("ERROR\1st Buf Length:" + buf1.length + "\t2nd Buf Length:" + buf2.length);
		return null;
	}
	
	public void insertToCarrierBuffer(byte[] leadingBytes){
		if(leadingBytes.length>0){
			if(carriedRxBuf.length>0){
				carriedRxBuf = combineByteArrays(leadingBytes, carriedRxBuf);
			}
			else {
				carriedRxBuf = leadingBytes; 
			}
		}
	}
	


//	private class CallbackUartRx implements UartRxCallback{
//		@Override
//		public void newMsg(byte[] rxBuf, long timestampMs) {
//			try {
//				parseRxPacket(rxBuf, timestampMs);
//			} catch (DockException e) {
//				mThrownException = e;
//			}
//		}
//
//		@Override
//		public void newParsedMsg(UartRxPacketObject uRPO) {
//			//NOT USED IN THIS CLASS
//		}
//
//	}


	
	private void wrapMsgSpanAndSend(int msgId, Object object) {
		ShimmerMsg msg = new ShimmerMsg(msgId, object);
		sendCallBackMsg(msg);
	}

	/**
	 * @return the mSendCallback
	 */
	public boolean isSendCallbackRxOverride() {
		return mSendCallbackRxOverride;
	}

	/**
	 * @param mSendCallbackRxOverride the mSendCallback to set
	 */
	public void setSendCallbackRxOverride(boolean state) {
		this.mSendCallbackRxOverride = state;
	}

	
	public void setVerbose(boolean verboseMode, boolean isDebugMode) {
		mVerboseMode = verboseMode;
		mIsDebugMode = isDebugMode;
		mUtilShimmer.setVerboseMode(mVerboseMode);
		shimmerUartOs.setVerboseMode(mVerboseMode, mIsDebugMode);
	}
	
	/**
	 * @param packetCmd
	 * @param msgArg
	 * @param valueBuffer
	 */
	private void consolePrintTxPacketInfo(UART_PACKET_CMD packetCmd, UartComponentPropertyDetails msgArg, byte[] valueBuffer) {
		//Requires extra processing so a waste if verbose mode is already off
		if(mVerboseMode){
			String consoleString = "TX\tCommand:" + packetCmd.toString()
					+ "\tComponent:" + msgArg.mComponent.toString()
					+ "\tProperty:" + msgArg.mPropertyName
	//				+ (valueBuffer==null? "":("\tPayload:" + UtilShimmer.bytesToHexStringWithSpacesFormatted(valueBuffer)))
					;
			
			if(packetCmd!=UartPacketDetails.UART_PACKET_CMD.READ){
				consoleString += "\tPayload" ;
				if(valueBuffer!=null){
					consoleString += "(" + valueBuffer.length + "):";
					String txBytes = UtilShimmer.bytesToHexStringWithSpacesFormatted(valueBuffer);
					if(txBytes != null){
						consoleString += txBytes;
					}
				}
				else{
					consoleString += ":none";
				}
			}
			consolePrintLn(consoleString);
		}
	}
	
	/**
	 * @param uRPO
	 * @see UartRxPacketObject
	 */
	private void consolePrintRxPacketInfo(UartRxPacketObject uRPO) {
		//Requires extra processing so a waste if verbose mode is already off
		if(mVerboseMode){
			consolePrintLn("RX\t" + uRPO.getConsoleString());
		}
	}
	
	private void consolePrintLn(String string) {
		mUtilShimmer.consolePrintLn(mUniqueId + "\t" + string);
	}
	
}
