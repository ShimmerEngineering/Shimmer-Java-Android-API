package com.shimmerresearch.shimmerUartProtocol;

import java.util.ArrayList;
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
	public boolean mIsUARTInUse = false;
	public String mUniqueId = "";
	public String mComPort = "";
	private int mBaudToUse = SerialPort.BAUDRATE_115200;

	public final static class SHIMMER_UART_BAUD_RATES{
		public final static int SHIMMER3_DOCKED = SerialPort.BAUDRATE_115200;
		public final static int SPAN = 230400;
	}
	
	public boolean mVerboseMode = true;
	private UtilShimmer utilShimmer = new UtilShimmer(getClass().getSimpleName(), mVerboseMode);
	
	public boolean mLeavePortOpen = false;
	private List<UartRxPacketObject> mListOfUartRxPacketObjects = new ArrayList<UartRxPacketObject>();
	public DockException mThrownException = null;
//	private UartRxCallback mUartRxCallback = null;
	public boolean mSendCallback = true;

    //the timeout value for connecting with the port
    protected final static int SERIAL_PORT_TIMEOUT = 500; // was 2000

	public ShimmerUart(String comPort, String uniqueId, int baudToUse){
		mComPort = comPort;
		mUniqueId = uniqueId;
		mBaudToUse = baudToUse;
		
		shimmerUartOs = new ShimmerUartJssc(mComPort, mUniqueId, mBaudToUse);
		shimmerUartOs.registerRxCallback(new CallbackUartRx());
	}

	public ShimmerUart(String comPort, String uniqueId){
		this(comPort, uniqueId, SHIMMER_UART_BAUD_RATES.SHIMMER3_DOCKED);
	}
	public boolean isUARTinUse(){
		return mIsUARTInUse;
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
			shimmerUartOs.shimmerUartDisconnect();
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
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(cPD, payload);
		} catch(DockException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		return rxBuf;
	}

	/** 
	 * @return 
	 * @throws ExecutionException
	 */
	public void uartSetCommand(UartComponentPropertyDetails cPD, byte[] txBuf) throws ExecutionException {
		openSafely();
		try {
			shimmerUartSetCommand(cPD, txBuf);
		} catch(ExecutionException e) {
			throw(e);
		} finally{
			closeSafely();
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
		for (Iterator<UartRxPacketObject> flavoursIter = mListOfUartRxPacketObjects.iterator(); flavoursIter.hasNext();) {
			UartRxPacketObject uRPO = flavoursIter.next();
			if(uRPO.mUartCommandByte==UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()){
				flavoursIter.remove();
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
			loopCount += 1;
			if(loopCount >= loopCountTotal) {
				break;
			}
			
			UartRxPacketObject uRPO = checkIfListContainsResponse(packetCmd, msgArg);
			if(uRPO!=null) {
				return uRPO.getPayload();
			}
			else if(mThrownException!=null){
				throw mThrownException;
			}
			utilShimmer.millisecondDelay(waitIntervalMs);
		}

		consolePrintLn("TIMEOUT_FAIL" + "\tComponent:" + msgArg.mComponent.toString() + "\tProperty:" + msgArg.mProperty);
//		DockException de = new DockException(mComPort, DockJobDetails.getJobErrorCode(dJD.currentJob), ErrorCodesDock.DOCK_TIMEOUT, mUniqueId);
//////		DockException de = new DockException(mUniqueId, dJD.slotNumber, DockJobDetails.getJobErrorCode(dJD.currentJob), ErrorCodesDock.DOCK_TIMEOUT);
//		DockException de = new DockException(mComPort, 0, ErrorCodesDock.DOCK_TIMEOUT, mUniqueId);
		DockException de = new DockException(mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT, mUniqueId);
		throw(de);
	}
	
	private UartRxPacketObject checkIfListContainsResponse(UART_PACKET_CMD packetCmd, UartComponentPropertyDetails msgArg){
		if(mListOfUartRxPacketObjects.size()==0){
			return null;
		}
		
		for (Iterator<UartRxPacketObject> uRPOIter = mListOfUartRxPacketObjects.iterator(); uRPOIter.hasNext();) {
			UartRxPacketObject uRPO = uRPOIter.next();
			if(packetCmd==UartPacketDetails.UART_PACKET_CMD.WRITE
					&& uRPO.mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.ACK_RESPONSE.toCmdByte()){
				uRPOIter.remove();
				return uRPO;
			}
			else if((packetCmd==UartPacketDetails.UART_PACKET_CMD.READ)
					&& uRPO.mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()
					&& msgArg.mComponentByte==uRPO.mUartComponentByte
					&& msgArg.mPropertyByte==uRPO.mUartPropertyByte){
				uRPOIter.remove();
				return uRPO;
			}
		}
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
				String txBytes = UtilShimmer.bytesToHexStringWithSpacesFormatted(valueBuffer);
				if(txBytes != null){
					consoleString += "\tPayload:" + txBytes;
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
		utilShimmer.consolePrintLn(mUniqueId + "\t" + string);
	}

	private class CallbackUartRx implements UartRxCallback{
		@Override
		public void newMsg(byte[] rxBuf) {
			try {
				parseRxPacket(rxBuf);
			} catch (DockException e) {
				mThrownException = e;
			}
		}

		@Override
		public void newParsedMsg(UartRxPacketObject uRPO) {
			//NOT USED IN THIS CLASS
		}
	}
	
	private void parseRxPacket(byte[] rxBuf) throws DockException {
		UartRxPacketObject uRPO = new UartRxPacketObject(rxBuf);
		
		uRPO.mSystemTimeMillis = System.currentTimeMillis();
		
		try {
			// Check CRC
			if(!ShimmerCrc.shimmerUartCrcCheck(uRPO.mRxPacket)) {
				consolePrintLn("RX\tERR_CRC");
				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC);
			}
			
			consolePrintRxPacketInfo(uRPO);

			//handle 'bad' responses
			try{
				processUnexpectedResponse(uRPO);
			} catch(DockException de){
				throw de;
			}

//			if(mUartRxCallback!=null){
//				mUartRxCallback.newParsedMsg(uRPO);
			if(mSendCallback){
				wrapMsgSpanAndSend(MsgDock.MSG_ID_SHIMMERUART_PACKET_RX, uRPO);
			}
			else {
				mListOfUartRxPacketObjects.add(uRPO);
			}
		} catch (DockException dE){
			throw(dE);
		} finally{
			if(uRPO.mLeftOverBytes!=null){
				parseRxPacket(uRPO.mLeftOverBytes);
			}
		}
	} 
	
//	public void registerRxCallback(UartRxCallback uartRxCallback) {
//		this.mUartRxCallback = uartRxCallback;
//	}

	public void setVerbose(boolean verboseMode) {
		mVerboseMode = verboseMode;
		utilShimmer.setVerboseMode(verboseMode);
	}
	
	
	private void wrapMsgSpanAndSend(int msgId, Object object) {
		ShimmerMsg msg = new ShimmerMsg(msgId, object);
		sendCallBackMsg(msg);
	}

	
}
