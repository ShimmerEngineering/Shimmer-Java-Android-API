package com.shimmerresearch.shimmerUartProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.dockManager.ErrorCodesDock;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.DockJobDetails;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.PACKET_CMD;

/**Driver for managing and configuring the Shimmer through the Dock using the 
 * Shimmer's dock connected UART.
 * 
 * @author Mark Nolan
 *
 */
public abstract class ShimmerUart {
	public ShimmerUartOsInterface shimmerUartOs;
	public boolean mIsUARTInUse = false;
	public String mUniqueId = "";
	public String mComPort = "";

	public static boolean mLeavePortOpen = true;
	
    //the timeout value for connecting with the port
    protected final static int SERIAL_PORT_TIMEOUT = 500; // was 2000
	
	public ShimmerUart(String comPort, String uniqueId){
		shimmerUartOs = new ShimmerUartJssc(comPort, uniqueId);
		
		shimmerUartOs.registerRxCallback(new CallbackUartRx());
		
		mUniqueId = uniqueId;
		mComPort = comPort;
	}
	
	private class CallbackUartRx implements UartRxCallback{
		@Override
		public void newMsg(byte[] rxBuf) {
			parseRxPacket(rxBuf);
		}
	}
	
	public class UartRxPacketObject{
		byte[] mCrc = null;
		byte[] mRxBuf = null;
		byte mResponseType;

		public UartRxPacketObject(byte[] rxBuf) {
			mRxBuf = rxBuf;
			mResponseType = mRxBuf[1];
			mCrc = Arrays.copyOfRange(rxBuf, rxBuf.length-2, rxBuf.length);
		}
	}
	
	private List<UartRxPacketObject> mListOfUartRxPacketObjects = new ArrayList<UartRxPacketObject>();
	
	
	private void parseRxPacket(byte[] rxBuf) {
		// Receive header and type of response
//		byte[] rxBuf = shimmerUartOs.shimmerUartRxBytes(2);
		
//		byte[] rxHeader = rxBuf; // Save for CRC calculation
		// Check header
		if(rxBuf[0] != UartPacketDetails.PACKET_HEADER.getBytes()[0]) {
			// Expected header byte not the first byte received
//				System.out.println(":\t Result: ERR_PACKAGE_FORMAT");
//			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PACKAGE_FORMAT,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PACKAGE_FORMAT);
			return;
		}
		
//		byte[] rxPacket = null;
		if(rxBuf[1]==UartPacketDetails.PACKET_CMD.ACK_RESPONSE.toCmdByte()){
//			// Get the CRC response
//			rxBuf = shimmerUartOs.shimmerUartRxBytes(2);
//			byte[] rxCrc = rxBuf; // Save for CRC calculation
//			rxPacket = new byte[rxHeader.length + rxCrc.length]; 
//			System.arraycopy(rxHeader, 0, rxPacket, 0, rxHeader.length);
//			System.arraycopy(rxCrc, 0, rxPacket, rxHeader.length, rxCrc.length);
		}
		else if(rxBuf[1]==UartPacketDetails.PACKET_CMD.DATA_RESPONSE.toCmdByte()){
//			// Get message data length + getComponent + getProperty
//			rxBuf = shimmerUartOs.shimmerUartRxBytes(3);
//			// check component and property
//			if((rxBuf[1]!=msgArg.componentByte)||(rxBuf[2]!=msgArg.propertyByte)) {
////					System.out.println(":\t Result: ERR_RESPONSE_UNEXPECTED");
//				throw new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
//			}
//			
//			byte[] rxLenAndArg = rxBuf; // Save for CRC calculation 
//			// Get message data + CRC
////		    	System.out.println(Util.bytesToHexStringWithSpaces(rxBuf));
//			rxBuf = shimmerUartOs.shimmerUartRxBytes((((int)rxBuf[0])&0xFF));
//			
//			byte[] rxMessage = rxBuf; // Save for CRC calculation 
//			// Check response validity
//			rxPacket = new byte[rxHeader.length + rxLenAndArg.length + rxMessage.length]; 
//			System.arraycopy(rxHeader, 0, rxPacket, 0, rxHeader.length);
//			System.arraycopy(rxLenAndArg, 0, rxPacket, rxHeader.length, rxLenAndArg.length);
//			System.arraycopy(rxMessage, 0, rxPacket, rxHeader.length + rxLenAndArg.length, rxMessage.length);
		}
		else {
//			processUnexpectedResponse(rxBuf[1]);
		}

		if(rxBuf!=null) {
			if(ShimmerCrc.shimmerUartCrcCheck(rxBuf)) {
				// Take off CRC
//				rxBuf = Arrays.copyOfRange(rxBuf, 0, rxBuf.length-2); 
			}
			else {
				// CRC fail
//					System.out.println(":\t Result: ERR_CRC");
//				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC);
				return;
			}
		}
		else {
//				System.out.println(":\t Result: ERR_RESPONSE_UNEXPECTED");
//			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
			return;
		}

		mListOfUartRxPacketObjects.add(new UartRxPacketObject(rxBuf));

		if(rxBuf[1]==UartPacketDetails.PACKET_CMD.DATA_RESPONSE.toCmdByte()){
//		if(packetCmd == UartPacketDetails.PACKET_CMD.GET){
			String rxBytes = UtilShimmer.bytesToHexString(rxBuf);
			if(rxBytes==null){
				rxBytes ="";
			}
			else {
				rxBytes = "rxbytes: " + rxBytes;
			}
//				System.out.println(":\t" + rxBytes + "\t Result: OK");
		}
		else {
//				System.out.println("\t Result: OK");
		}
		
	} 

	
	public boolean isUARTinUse(){
		return mIsUARTInUse;
	}
	
	public byte[] processShimmerGetCommand(ComponentPropertyDetails compPropDetails, int errorCode) throws ExecutionException{
		byte[] rxBuf;
		if(!mLeavePortOpen) openSafely();
		try {
			rxBuf = shimmerUartGetCommand(compPropDetails);
		} catch(ExecutionException e) {
			DockException de = (DockException) e;
			de.mErrorCode = errorCode;
			throw(e);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
		return rxBuf;
	}

	public void processShimmerSetCommand(ComponentPropertyDetails compPropDetails, byte[] txBuf, int errorCode) throws ExecutionException{
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartSetCommand(compPropDetails, txBuf);
		} catch(ExecutionException e) {
			DockException de = (DockException) e;
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
	}
    
	public byte[] processShimmerMemGetCommand(ComponentPropertyDetails compPropDetails, int address, int size, int errorCode) throws ExecutionException{
		byte[] rxBuf;
		if(!mLeavePortOpen) openSafely();
		try {
			rxBuf = shimmerUartGetMemCommand(compPropDetails, address, size);
		} catch(ExecutionException e) {
			DockException de = (DockException) e;
			de.mErrorCode = errorCode;
			throw(de);
		} finally{
			if(!mLeavePortOpen) closeSafely();
		}
		return rxBuf;
	}	
	
	public void processShimmerMemSetCommand(ComponentPropertyDetails compPropDetails, int address, byte[] buf, int errorCode) throws ExecutionException{
		if(!mLeavePortOpen) openSafely();
		try {
			shimmerUartSetMemCommand(compPropDetails, address, buf);
		} catch(ExecutionException e) {
			DockException de = (DockException) e;
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
		} catch (DockException e) {
			closeSafely();
			mIsUARTInUse = false;
			throw(e);
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
		} catch (DockException e) {
			throw(e);
		} finally{
			mIsUARTInUse = false;
		}
	}

	/** 
	 * @return 
	 * @throws ExecutionException
	 */
	public byte[] uartGetCommand(ComponentPropertyDetails cPD) throws ExecutionException {
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(cPD);
		} catch(ExecutionException e) {
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
	public void uartSetCommand(ComponentPropertyDetails cPD, byte[] txBuf) throws ExecutionException {
		openSafely();
		try {
			shimmerUartSetCommand(cPD, txBuf);
		} catch(ExecutionException e) {
			throw(e);
		} finally{
			closeSafely();
		}
	}
	
    /**Get information from the Shimmer based on passed in message argument
     * @param msgArg a byte array of containing the Component and Property to get
     * @return byte array containing data response from Shimmer
     * @throws ExecutionException
     */
	protected byte[] shimmerUartGetCommand(ComponentPropertyDetails msgArg) throws ExecutionException {
		return shimmerUartCommandTxRx(UartPacketDetails.PACKET_CMD.GET, msgArg, null);
    }
    
    /**Get memory data from the Shimmer based on passed in message argument
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param address memory address to read from
     * @param size the number of memory bytes to read
     * @return a byte array with the received memory bytes
     * @throws ExecutionException
     */
	protected byte[] shimmerUartGetMemCommand(ComponentPropertyDetails msgArg, int address, int size) throws ExecutionException {
    	
    	byte[] memLengthToRead = new byte[]{(byte) size};

    	byte[] memAddressToRead = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
    	if(msgArg==UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID) {
    		memAddressToRead = new byte[]{(byte) (address&0xFF)};
    	}
    	
    	ArrayUtils.reverse(memAddressToRead);
    	
    	byte[] valueBuffer = new byte[memLengthToRead.length + memAddressToRead.length];
    	System.arraycopy(memLengthToRead, 0, valueBuffer, 0, memLengthToRead.length);
    	System.arraycopy(memAddressToRead, 0, valueBuffer, memLengthToRead.length, memAddressToRead.length);

		return shimmerUartCommandTxRx(UartPacketDetails.PACKET_CMD.GET, msgArg, valueBuffer);
    }

    /**
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
	protected byte[] shimmerUartSetCommand(ComponentPropertyDetails msgArg, byte[] valueBuffer) throws ExecutionException {
		return shimmerUartCommandTxRx(UartPacketDetails.PACKET_CMD.SET, msgArg, valueBuffer);
    }
    
    /**
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param address
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
	protected byte[] shimmerUartSetMemCommand(ComponentPropertyDetails msgArg, int address, byte[] valueBuffer) throws ExecutionException {
    	
		byte[] memLengthToWrite = new byte[]{(byte) valueBuffer.length};
		
		byte[] memAddressToWrite = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
    	if(msgArg==UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID) {
    		memAddressToWrite = new byte[]{(byte) (address&0xFF)};
    	}
		ArrayUtils.reverse(memAddressToWrite);

		// TODO check I'm not missing the last two bytes here because the mem
		// address length is not being included in the length field
		byte[] dataBuffer = new byte[memLengthToWrite.length + memAddressToWrite.length + valueBuffer.length];
		System.arraycopy(memLengthToWrite, 0, dataBuffer, 0, memLengthToWrite.length);
		System.arraycopy(memAddressToWrite, 0, dataBuffer, memLengthToWrite.length, memAddressToWrite.length);
		System.arraycopy(valueBuffer, 0, dataBuffer, memLengthToWrite.length + memAddressToWrite.length, valueBuffer.length);
		
		return shimmerUartCommandTxRx(UartPacketDetails.PACKET_CMD.SET, msgArg, dataBuffer);
    }
    
    
    /**
     * @param packetCmd
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
	protected byte[] shimmerUartCommandTxRx(PACKET_CMD packetCmd, ComponentPropertyDetails msgArg, byte[] valueBuffer) throws ExecutionException {
//		consolePrintTxPacketInfo(packetCmd, msgArg, valueBuffer);
		txPacket(packetCmd, msgArg, valueBuffer);
//		return rxPacket(packetCmd, msgArg);
		
		return waitForResponse(packetCmd, msgArg);
    }

	private void txPacket(PACKET_CMD packetCmd, ComponentPropertyDetails msgArg, byte[] valueBuffer) throws DockException {
    	byte[] txPacket = assembleTxPacket(packetCmd.toCmdByte(),msgArg,valueBuffer);
//    	System.out.println(Util.bytesToHexStringWithSpaces(txPacket));
    	shimmerUartOs.shimmerUartTxBytes(txPacket); 
	}

	/**
     * @param command
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param value
     * @return
     */
	protected byte[] assembleTxPacket(int command, ComponentPropertyDetails msgArg, byte[] value) {
		byte[] header = (UartPacketDetails.PACKET_HEADER).getBytes();
		byte[] cmd = new byte[]{(byte) command};
		byte[] msgLength = new byte[]{(byte) 2};
		if(value!=null) {
			msgLength[0] += value.length;
		}
		
		byte[] txBufPreCrc;
		if(value!=null) {
			txBufPreCrc = new byte[header.length + cmd.length + msgArg.compPropByteArray.length + msgLength.length + value.length];
		}
		else {
			txBufPreCrc = new byte[header.length + cmd.length + msgArg.compPropByteArray.length + msgLength.length];
		}
		
		System.arraycopy(header, 0, txBufPreCrc, 0, header.length);
		System.arraycopy(cmd, 0, txBufPreCrc, header.length, cmd.length);
		System.arraycopy(msgLength, 0, txBufPreCrc, header.length + cmd.length, msgLength.length);
		System.arraycopy(msgArg.compPropByteArray, 0, txBufPreCrc, header.length + cmd.length + msgLength.length, msgArg.compPropByteArray.length);
		if(value!=null) {
			System.arraycopy(value, 0, txBufPreCrc, header.length + cmd.length + msgLength.length + msgArg.compPropByteArray.length, value.length);
		}

		byte[] calculatedCrc = ShimmerCrc.shimmerUartCrcCalc(txBufPreCrc, txBufPreCrc.length);
		
		byte[] txBufPostCrc = new byte[txBufPreCrc.length + 2]; 
		System.arraycopy(txBufPreCrc, 0, txBufPostCrc, 0, txBufPreCrc.length);
		System.arraycopy(calculatedCrc, 0, txBufPostCrc, txBufPreCrc.length, calculatedCrc.length);
		
		return txBufPostCrc;
    }
	
    
	private byte[] waitForResponse(PACKET_CMD packetCmd, ComponentPropertyDetails msgArg) {
		boolean flag = true;
		boolean success = false;
		
		int loopCount = 0;
		//100*100 = 10s
		int interval = 100;
		int loopCountTotal = 100;

		while(flag) {
			loopCount += 1;
			if(loopCount >= loopCountTotal) {
				break;
			}
			
			if(checkIfListContainsResponse(packetCmd, msgArg)!=null) {
				success = true;
				break;
			}
			utilDock.dockDelay(interval);
		}

		if(!success){
			utilDock.consolePrintLn("TIMEOUT FAIL " + dJD.currentJob);
			DockException de = new DockException(mDockID, dJD.slotNumber, DockJobDetails.getJobErrorCode(dJD.currentJob), ErrorCodesDock.DOCK_TIMEOUT);
			throw(de);
		}
		
	}
	
	
	private UartRxPacketObject checkIfListContainsResponse(PACKET_CMD packetCmd, ComponentPropertyDetails msgArg){
		if(mListOfUartRxPacketObjects.size()==0){
			return null;
		}
		
		for(UartRxPacketObject uRPO:mListOfUartRxPacketObjects){
			if(packetCmd==UartPacketDetails.PACKET_CMD.SET) {
				if(uRPO.mResponseType == UartPacketDetails.PACKET_CMD.ACK_RESPONSE.toCmdByte()){
					return uRPO;
				}
			}
			else if(packetCmd==UartPacketDetails.PACKET_CMD.GET) {
				if(uRPO.mResponseType == UartPacketDetails.PACKET_CMD.DATA_RESPONSE.toCmdByte()){
					return uRPO;
				}
			}
		}
		
	}

	
//    private byte[] rxPacket(PACKET_CMD packetCmd, ComponentPropertyDetails msgArg) throws DockException {
//		// Receive header and type of response
//		byte[] rxBuf = shimmerUartOs.shimmerUartRxBytes(2);
//		
//		byte[] rxHeader = rxBuf; // Save for CRC calculation
//		// Check header
//		if(rxBuf[0] != UartPacketDetails.PACKET_HEADER.getBytes()[0]) {
//			// Expected header byte not the first byte received
////			System.out.println(":\t Result: ERR_PACKAGE_FORMAT");
//			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PACKAGE_FORMAT,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PACKAGE_FORMAT);
//		}
//		
//		byte[] rxPacket = null;
//		if(packetCmd==UartPacketDetails.PACKET_CMD.SET) {
//			checkForExpectedResponse(rxBuf, UartPacketDetails.PACKET_CMD.ACK_RESPONSE);
//			
//			// Get the CRC response
//			rxBuf = shimmerUartOs.shimmerUartRxBytes(2);
//			byte[] rxCrc = rxBuf; // Save for CRC calculation
//			rxPacket = new byte[rxHeader.length + rxCrc.length]; 
//			System.arraycopy(rxHeader, 0, rxPacket, 0, rxHeader.length);
//			System.arraycopy(rxCrc, 0, rxPacket, rxHeader.length, rxCrc.length);
//		}
//		else if(packetCmd==UartPacketDetails.PACKET_CMD.GET) {
//			checkForExpectedResponse(rxBuf, UartPacketDetails.PACKET_CMD.DATA_RESPONSE);
//		
//			// Get message data length + getComponent + getProperty
//			rxBuf = shimmerUartOs.shimmerUartRxBytes(3);
//			// check component and property
//			if((rxBuf[1]!=msgArg.componentByte)||(rxBuf[2]!=msgArg.propertyByte)) {
////				System.out.println(":\t Result: ERR_RESPONSE_UNEXPECTED");
//				throw new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
//			}
//			
//			byte[] rxLenAndArg = rxBuf; // Save for CRC calculation 
//			// Get message data + CRC
////	    	System.out.println(Util.bytesToHexStringWithSpaces(rxBuf));
//			rxBuf = shimmerUartOs.shimmerUartRxBytes((((int)rxBuf[0])&0xFF));
//			
//			byte[] rxMessage = rxBuf; // Save for CRC calculation 
//			// Check response validity
//			rxPacket = new byte[rxHeader.length + rxLenAndArg.length + rxMessage.length]; 
//			System.arraycopy(rxHeader, 0, rxPacket, 0, rxHeader.length);
//			System.arraycopy(rxLenAndArg, 0, rxPacket, rxHeader.length, rxLenAndArg.length);
//			System.arraycopy(rxMessage, 0, rxPacket, rxHeader.length + rxLenAndArg.length, rxMessage.length);
//		}
//		
//		if(rxPacket!=null) {
//			if(ShimmerCrc.shimmerUartCrcCheck(rxPacket)) {
//				// Take off CRC
//				rxBuf = Arrays.copyOfRange(rxBuf, 0, rxBuf.length-2); 
//			}
//			else {
//				// CRC fail
////				System.out.println(":\t Result: ERR_CRC");
//				throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC);
//			}
//		}
//		else {
////			System.out.println(":\t Result: ERR_RESPONSE_UNEXPECTED");
//			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
//		}
//
//		if(packetCmd == UartPacketDetails.PACKET_CMD.GET){
//			String rxBytes = UtilShimmer.bytesToHexString(rxBuf);
//			if(rxBytes==null){
//				rxBytes ="";
//			}
//			else {
//				rxBytes = "rxbytes: " + rxBytes;
//			}
////			System.out.println(":\t" + rxBytes + "\t Result: OK");
//		}
//		else {
////			System.out.println("\t Result: OK");
//		}
//
//		return rxBuf;
//	}
	
	/**
	 * @param rxBuf
	 * @param expectedResponse
	 * @throws DockException
	 */
	protected void checkForExpectedResponse(byte[] rxBuf, PACKET_CMD expectedResponse) throws DockException {
		// Check for expected response
		if(rxBuf[1] != expectedResponse.toCmdByte()) {
			processUnexpectedResponse(rxBuf[1]);
		}
	}
	
	private void processUnexpectedResponse(byte rxBuf) throws DockException {
		byte[] crcBuf;
		// Response is not the expected response type
		if(rxBuf == UartPacketDetails.PACKET_CMD.BAD_CMD_RESPONSE.toCmdByte()) {
			crcBuf = shimmerUartOs.shimmerUartRxBytes(2);
			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD);
		}
		else if(rxBuf == UartPacketDetails.PACKET_CMD.BAD_ARG_RESPONSE.toCmdByte()) {
			crcBuf = shimmerUartOs.shimmerUartRxBytes(2);
			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG);
		}
		else if(rxBuf == UartPacketDetails.PACKET_CMD.BAD_CRC_RESPONSE.toCmdByte()) {
			crcBuf = shimmerUartOs.shimmerUartRxBytes(2);
			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC);
		}
		else {
			crcBuf = shimmerUartOs.shimmerUartRxBytes(2);
			throw new DockException(mUniqueId, mComPort, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED, ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
		}
//		clearSerialPortRxBuffer();
	}


	private void consolePrintTxPacketInfo(PACKET_CMD packetCmd, ComponentPropertyDetails msgArg, byte[] valueBuffer) {
		String consoleString = "UART cmd: " + packetCmd.toString()
				+ "\t comp:" + msgArg.component.toString()
				+ "\t prop:" + msgArg.propertyName
//				+ " ByteArray:" + msgArg.byteArray.length
				;
		if(packetCmd!=UartPacketDetails.PACKET_CMD.GET){
			String txBytes = UtilShimmer.bytesToHexStringWithSpaces(valueBuffer);
			if(txBytes == null){
				txBytes = "";
			}
			else {
				txBytes = "txbytes: " + txBytes;
			}
			consoleString += "\t - " + txBytes;
		}
		System.out.print(consoleString);
	}
	
	//Copied from SmartDockUartListener
//	public class ShimmerUartListener implements SerialPortEventListener {
//	    StringBuilder stringBuilder = new StringBuilder(128);
//
//		@Override
//		public void serialEvent(SerialPortEvent event) {
//	        if (event.isRXCHAR()) {//If data is available
//	            if (event.getEventValue() > 0) {//Check bytes count in the input buffer
//	                try {
//	                    byte[] data = shimmerUartOs.shimmerUartRxBytes(event.getEventValue());
//
//	                    for (int i = 0; i < data.length; i++) {
//	                        stringBuilder.append((char) data[i]);
//	                    }
//
//	                    
////	                    //split entire message by "\r\n"
////	            		int lineTerminationIndex = stringBuilder.indexOf("\r\n");
//////	            		System.out.println(stringBuilder);
////	            		if(lineTerminationIndex>=1) {
////	            			String message = stringBuilder.substring(0, lineTerminationIndex+2).toString();  
////	            			stringBuilder.replace(0, lineTerminationIndex+2, "");
////	            			
////	                    	if(message.length() == 3) {
////	                            // SmartDock error response - Expected message = "E\r\n"
////	                        	if ((message.charAt(0)) == 'E') {
////	                                consolePrintLn("Error = " + message);
////	                    			//notify the SmartDockUart layer
////	        						MsgDock msg = new MsgDock(MsgDock.MSG_ID_SMARTDOCK_UART_ERROR,mDockID);
////	                    			msg.mMessage = message;
////	        						sendCallBackMsg(msg.mMsgID,msg);
////	                            }
////	                    	}
////
////	            		}
//	            		
//	            		
//	                } catch (Exception ex) {
//	                    System.out.println(ex);
//	                }
//	            }
//	        }
//		}
//	}
	
}
