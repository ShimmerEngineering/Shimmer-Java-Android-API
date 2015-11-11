package com.shimmerresearch.uartViaDock;

import java.util.Arrays;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**Driver for managing and configuring the Shimmer through the Dock using the 
 * Shimmer's dock connected UART.
 * 
 * @author Mark Nolan
 *
 */
public abstract class ShimmerUartViaDock {
	protected String mDockID = "";
	public String mShimmerUARTComPort;
	public boolean mIsUARTInUse = false; 

    //the timeout value for connecting with the port
    protected final static int SERIAL_PORT_TIMEOUT = 500; // was 2000
	//this is the object that contains the opened port
	
//	public final long REFERENCE_TIME = 946684800000L; // 1st Jan 2000 in milliseconds since 1st Jan 1970
	
    protected abstract void shimmerUartConnect() throws DockException;
    protected abstract void shimmerUartDisconnect() throws DockException;
    protected abstract void clearSerialPortRxBuffer() throws DockException;
    protected abstract void shimmerUartTxBytes(byte[] buf) throws DockException;
    protected abstract byte[] shimmerUartRxBytes(int numBytes) throws DockException;

    
    /** byte order of the Version Response Data Packet
     *
     */
    private enum VerReponsePacketOrder {
        hwVer,
        fwVerLSB,
        fwVerMSB,
        fwMajorLSB,
        fwMajorMSB,
        fwMinor,
        fwRevision
    }
    
	public ShimmerUartViaDock(String comPort, String dockID){
		mShimmerUARTComPort = comPort;
		mDockID = dockID;
	}
	
	/**Opens the Shimmer UART Serial and throws a DockException if there is a 
	 * SerialPortException or general Exception  
	 * @throws DockException
	 */
	public void openSafely() throws DockException {
		try {
			shimmerUartConnect();
		} catch (DockException e) {
			closeSafely();
			mIsUARTInUse = false;
			throw(e);
		}
		mIsUARTInUse = true;
	}
	
	/**Closes the Shimmer UART Serial and throws a DockException if there is a 
	 * SerialPortException or general Exception  
	 * @throws DockException
	 */
	public void closeSafely() throws DockException {
		try {
			shimmerUartDisconnect();
		} catch (DockException e) {
			mIsUARTInUse = false;
			throw(e);
		}
		mIsUARTInUse = false;
	}
	
	/** Reads the MAC address from the Shimmer UART
	 * @return MAC address of docked Shimmer
	 * @throws ExecutionException 
	 */
	public String readMacId() throws ExecutionException {
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.MAC);
		}
		catch(ExecutionException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		// Parse response string
		if(rxBuf.length >= 6) {
			rxBuf = Arrays.copyOf(rxBuf, 6);
		}
		else {
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_MAC_ID_GET,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}
		
		return new String(bytesToHex(rxBuf));
	}

	/** Reads the battery charge status from the Shimmer UART
	 * @return battery charge status of docked Shimmer
	 * @see ShimmerBattStatusDetails
	 * @throws ExecutionException
	 */
	public ShimmerBattStatusDetails readBattStatus() throws ExecutionException{
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(UartPacketDetails.COMPONENT_PROPERTY.BAT.VALUE);
		}
		catch(ExecutionException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		// Parse response string
		ShimmerBattStatusDetails shimmerUartBattStatusDetails = new ShimmerBattStatusDetails();
		if(rxBuf.length >= 3) {
			// Parse response string
            int battAdc = (((rxBuf[1]&0xFF) << 8) + (rxBuf[0]&0xFF));
            int chargeStatus = rxBuf[2] & 0xC0;
			shimmerUartBattStatusDetails = new ShimmerBattStatusDetails(battAdc, chargeStatus);
		}
		else {
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_BATT_STATUS_GET,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}

		return shimmerUartBattStatusDetails;
	}
	
	/** Reads the firmware version from the Shimmer UART
	 * @return firmware version of docked Shimmer
	 * @see ShimmerHwFwDetails
	 * @throws ExecutionException
	 */
	public ShimmerVerObject readHwFwVersion() throws ExecutionException {
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.VER);
		}
		catch(ExecutionException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		// Parse response string
		ShimmerVerObject shimmerVerDetails = new ShimmerVerObject();
		if(rxBuf.length >= 7) {
			// Parse response string
			
			int mHardwareVersion = rxBuf[VerReponsePacketOrder.hwVer.ordinal()];
			int mFirmwareIdentifier = (rxBuf[VerReponsePacketOrder.fwVerLSB.ordinal()]) | (rxBuf[VerReponsePacketOrder.fwVerMSB.ordinal()]<<8);
			int mFirmwareVersionMajor = (rxBuf[VerReponsePacketOrder.fwMajorLSB.ordinal()]) | (rxBuf[(int)VerReponsePacketOrder.fwMajorMSB.ordinal()]<<8);
			int mFirmwareVersionMinor = rxBuf[VerReponsePacketOrder.fwMinor.ordinal()];
			int mFirmwareVersionRelease = rxBuf[VerReponsePacketOrder.fwRevision.ordinal()];
			
			shimmerVerDetails = new ShimmerVerObject(mHardwareVersion, mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionRelease);
		}
		else {
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_VERSION_INFO_GET,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}

		return shimmerVerDetails;
	}
	
	/** Read the real time clock config time of the Shimmer
	 * @return long the Shimmer RTC configure time in milliseconds UNIX time (since 1970-Jan-01 00:00:00 UTC)
	 * @throws ExecutionException
	 */
	public long readRealWorldClockConfigTime() throws ExecutionException {
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.RTC_CFG_TIME);
		}
		catch(ExecutionException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		// Parse response string
		long responseTime = 0;
		if(rxBuf.length >= 8) {
			byte[] rwcTimeArray = Arrays.copyOf(rxBuf, 8);
			responseTime = UtilShimmer.convertShimmerRtcDataBytesToSystemTime(rwcTimeArray);
		}
		else {
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_GET,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}

		return responseTime;
	}
	
	/** Read the current real time clock of the Shimmer
	 * @return long the current Shimmer RTC time in milliseconds UNIX time (since 1970-Jan-01 00:00:00 UTC)
	 * @throws ExecutionException
	 */
	public long readCurrentTime() throws ExecutionException {
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.CURR_LOCAL_TIME);
		}
		catch(ExecutionException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		// Parse response string
		long responseTime = 0;
		if(rxBuf.length >= 8) {
			byte[] rwcTimeArray = Arrays.copyOf(rxBuf, 8);
			responseTime = UtilShimmer.convertShimmerRtcDataBytesToSystemTime(rwcTimeArray);
		}
		else {
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_RTC_CURRENT_TIME_GET,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}

		return responseTime;
	}

	/** Writes the real time clock (current PC time) to the Shimmer UART
	 * @return System time in milliseconds that was written to the Shimmer 
	 * @throws ExecutionException
	 */
	public long writeRealWorldClockFromPcTime() throws ExecutionException{
		return writeRealWorldClock(System.currentTimeMillis());
	}
	
	/** Writes a specified real time clock value to the Shimmer through the Dock UART
	 * @return Time in milliseconds that was written to the Shimmer 
	 * @param l the real time clock value in milliseconds UNIX time (since 1970-Jan-01 00:00:00 UTC)
	 */
	public long writeRealWorldClock(long miliseconds) throws ExecutionException {
		byte[] rwcTimeArray = UtilShimmer.convertSystemTimeToShimmerRtcDataBytes(miliseconds);
		
		openSafely();
		try {
			shimmerUartSetCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.RTC_CFG_TIME, rwcTimeArray);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_SET;
			throw(de);
		}
		closeSafely();
		
		return miliseconds;
	}
	
	/** Reads from the Shimmer's InfoMem using Dock UART
	 * @return returns the infomem of the docked Shimmer device
	 * @throws ExecutionException
	 */
	public byte[] readInfoMem(int address, int size) throws ExecutionException{
		byte[] rxBuf = null;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetMemCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.INFOMEM, address, size);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_INFOMEM_GET;
			throw(de);
		}
		closeSafely();
		
		return rxBuf;
	}
	
	/** Writes to the Shimmer's InfoMem using Dock UART
	 * @param address the InfoMem address to write to
	 * @param buf the byte array to write
	 * @throws ExecutionException
	 */
	public void writeInfoMem(int address, byte[] buf) throws ExecutionException{
		openSafely();
		try {
			shimmerUartSetMemCommand(UartPacketDetails.COMPONENT_PROPERTY.SHIMMER.INFOMEM, address, buf);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_INFOMEM_SET;
			throw(de);
		}
		closeSafely();
	}
	
	/** Reads the daughter card ID via the Shimmer's Dock UART (the first 16 bytes of the daughter card memory)
	 * @return ShimmerUartExpansionBoardDetails the parsed daughter card ID information 
	 * @see ExpansionBoardDetails
	 * @throws ExecutionException
	 */
	public ExpansionBoardDetails readDaughterCardID() throws ExecutionException{
		byte[] rxBuf = null;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetMemCommand(UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID, 0, 16);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_DAUGHTER_ID_GET;
			throw(de);
		}
		closeSafely();
		
		//TODO: check rxBuf
		ExpansionBoardDetails shimmerUartExpansionBoardDetails = new ExpansionBoardDetails(rxBuf);
		return shimmerUartExpansionBoardDetails;
	}
	
	// Untested
	/** Writes the daughter card ID via the Shimmer's Dock UART
	 * @param address the new daughter card memory address to start writing at
	 * @param buf the byte array to be written to the daughter card ID (< 16 bytes)
	 * @throws ExecutionException
	 */
	public void writeDaughterCardId(int address, byte[] buf) throws ExecutionException{
		openSafely();
		try {
			shimmerUartSetMemCommand(UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID, address, buf);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_DAUGHTER_ID_SET;
			throw(de);
		}
		closeSafely();
	}

	
	//TODO TEST
	/** UNTESTED: Reads the daughter card memory via the Shimmer UART
	 * @param address the daughter card memory address to start reading at (0 - 239)
	 * @param size the number of bytes to read  (max = 240 - address)
	 * @return returns the daughter card memory of the docked Shimmer device
	 * @throws ExecutionException
	 */
	public byte[] readDaughterCardMemory(int address, int size) throws ExecutionException{
		//TODO separate reads into 128 bytes chunks
//	    if((uartDcMemLength<=256) && (uartDcMemOffset>=16) && (uartDcMemLength+uartDcMemOffset<=240)){
//	    }		

		byte[] rxBuf = null;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetMemCommand(UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_MEM, address, size);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_DAUGHTER_MEM_GET;
			throw(de);
		}
		closeSafely();
		
		return rxBuf;
	}
	
	//TODO TEST
	/** UNTESTED: Writes the number of bytes specified starting in the offset from the expansion board attached to the Shimmer Device
	 * @param address the point from where the function starts to read (0 - 239)
	 * @param buf the byte array to be written to the docked Shimmer device (max = 240 - address)
	 * @throws ExecutionException
	 */
	public void writeDaughterCardMemory(int address, byte[] buf) throws ExecutionException{
		//TODO separate write into 128 bytes chunks
//	    if((uartDcMemLength<=256) && (uartDcMemOffset>=16) && (uartDcMemLength+uartDcMemOffset<=240)){
//	    }		

		openSafely();
		try {
			shimmerUartSetMemCommand(UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_MEM, address, buf);
		}
		catch(ExecutionException e) {
			closeSafely();
			DockException de = (DockException) e;
			de.mErrorCode = ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_DAUGHTER_MEM_SET;
			throw(de);
		}
		closeSafely();
	}
	
	
	/** Reads the 802.15.4 radio settings for ShimmerGQ_802.15.4. Channel, Address and Group ID from the Shimmer UART
	 * @return 
	 * @throws ExecutionException 
	 */
	public byte[] read802154RadioSettings() throws ExecutionException {
		byte[] rxBuf;
		
		openSafely();
		try {
			rxBuf = shimmerUartGetCommand(UartPacketDetails.COMPONENT_PROPERTY.RADIO_802154.SETTINGS);
		}
		catch(ExecutionException e) {
			closeSafely();
			throw(e);
		}
		closeSafely();
		
		// Parse response string
		if(rxBuf.length >= 9) {
			rxBuf = Arrays.copyOf(rxBuf, 9);
		}
		else {
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_RADIO_802154_GET,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}
		
		return rxBuf;
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
		}
		catch(ExecutionException e) {
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
		}
		catch(ExecutionException e) {
			throw(e);
		}
		finally{
			closeSafely();
		}
	}
	
	
    /**Get information from the Shimmer based on passed in message argument
     * @param msgArg a byte array of containing the Component and Property to get
     * @return byte array containing data response from Shimmer
     * @throws ExecutionException
     */
    private byte[] shimmerUartGetCommand(ComponentPropertyDetails msgArg) throws ExecutionException {
		return shimmerUartCommand(UartPacketDetails.PACKET_CMD.GET, msgArg, null);
    }
    
    /**Get memory data from the Shimmer based on passed in message argument
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param address memory address to read from
     * @param size the number of memory bytes to read
     * @return a byte array with the received memory bytes
     * @throws ExecutionException
     */
    private byte[] shimmerUartGetMemCommand(ComponentPropertyDetails msgArg, int address, int size) throws ExecutionException {
    	
    	byte[] memLengthToRead = new byte[]{(byte) size};

    	byte[] memAddressToRead = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
    	if(msgArg==UartPacketDetails.COMPONENT_PROPERTY.DAUGHTER_CARD.CARD_ID) {
    		memAddressToRead = new byte[]{(byte) (address&0xFF)};
    	}
    	
    	ArrayUtils.reverse(memAddressToRead);
    	
    	byte[] valueBuffer = new byte[memLengthToRead.length + memAddressToRead.length];
    	System.arraycopy(memLengthToRead, 0, valueBuffer, 0, memLengthToRead.length);
    	System.arraycopy(memAddressToRead, 0, valueBuffer, memLengthToRead.length, memAddressToRead.length);

		return shimmerUartCommand(UartPacketDetails.PACKET_CMD.GET, msgArg, valueBuffer);
    }

    /**
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
    private byte[] shimmerUartSetCommand(ComponentPropertyDetails msgArg, byte[] valueBuffer) throws ExecutionException {
		return shimmerUartCommand(UartPacketDetails.PACKET_CMD.SET, msgArg, valueBuffer);
    }
    
    /**
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param address
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
    private byte[] shimmerUartSetMemCommand(ComponentPropertyDetails msgArg, int address, byte[] valueBuffer) throws ExecutionException {
    	
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
		
		return shimmerUartCommand(UartPacketDetails.PACKET_CMD.SET, msgArg, dataBuffer);
    }
    
    
    /**
     * @param command
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param valueBuffer
     * @return
     * @throws ExecutionException
     */
    private byte[] shimmerUartCommand(int command, ComponentPropertyDetails msgArg, byte[] valueBuffer) throws ExecutionException {
    	
		String consoleString = "UART cmd: " + UartPacketDetails.parseCmd(command)
				+ "\t comp:" + UartPacketDetails.parseComponent(msgArg.component)
				+ "\t prop:" + msgArg.propertyName
//				+ " ByteArray:" + msgArg.byteArray.length
				;
		if(command!=UartPacketDetails.PACKET_CMD.GET){
			String txBytes = UtilShimmer.bytesToHexStringWithSpaces(valueBuffer);
			if(txBytes == null){
				txBytes = "";
			}
			else {
				txBytes = "txbytes: " + txBytes;
			}
			consoleString += "\t - " + txBytes;
		}
//		System.out.print(consoleString);

    	
    	byte[] txPacket = assembleTxPacket(command,msgArg,valueBuffer);
//    	System.out.println(Util.bytesToHexStringWithSpaces(txPacket));
		shimmerUartTxBytes(txPacket); 
		
		// Receive header and type of response
		byte[] rxBuf = shimmerUartRxBytes(2);
		
		byte[] rxHeader = rxBuf; // Save for CRC calculation
		// Check header
		if(rxBuf[0] != UartPacketDetails.PACKET_HEADER.getBytes()[0]) {
			// Expected header byte not the first byte received
//			System.out.println(":\t Result: ERR_PACKAGE_FORMAT");
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PACKAGE_FORMAT,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PACKAGE_FORMAT);
		}
		
		byte[] rxPacket = null;
		if(command==UartPacketDetails.PACKET_CMD.SET) {
			checkForExpectedResponse(rxBuf,(byte)UartPacketDetails.PACKET_CMD.ACK_RESPONSE);
			
			// Get the CRC response
			rxBuf = shimmerUartRxBytes(2);
			byte[] rxCrc = rxBuf; // Save for CRC calculation
			rxPacket = new byte[rxHeader.length + rxCrc.length]; 
			System.arraycopy(rxHeader, 0, rxPacket, 0, rxHeader.length);
			System.arraycopy(rxCrc, 0, rxPacket, rxHeader.length, rxCrc.length);
		}
		else if(command==UartPacketDetails.PACKET_CMD.GET) {
			checkForExpectedResponse(rxBuf,(byte)UartPacketDetails.PACKET_CMD.DATA_RESPONSE);
		
			// Get message data length + getComponent + getProperty
			rxBuf = shimmerUartRxBytes(3);
			// check component and property
			if((rxBuf[1]!=msgArg.componentByte)||(rxBuf[2]!=msgArg.propertyByte)) {
//				System.out.println(":\t Result: ERR_RESPONSE_UNEXPECTED");
				throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
			}
			
			byte[] rxLenAndArg = rxBuf; // Save for CRC calculation 
			// Get message data + CRC
//	    	System.out.println(Util.bytesToHexStringWithSpaces(rxBuf));
			rxBuf = shimmerUartRxBytes((((int)rxBuf[0])&0xFF));
			
			byte[] rxMessage = rxBuf; // Save for CRC calculation 
			// Check response validity
			rxPacket = new byte[rxHeader.length + rxLenAndArg.length + rxMessage.length]; 
			System.arraycopy(rxHeader, 0, rxPacket, 0, rxHeader.length);
			System.arraycopy(rxLenAndArg, 0, rxPacket, rxHeader.length, rxLenAndArg.length);
			System.arraycopy(rxMessage, 0, rxPacket, rxHeader.length + rxLenAndArg.length, rxMessage.length);
		}
		
		if(rxPacket!=null) {
			if(shimmerUartCrcCheck(rxPacket)) {
				// Take off CRC
				rxBuf = Arrays.copyOfRange(rxBuf, 0, rxBuf.length-2); 
			}
			else {
				// CRC fail
//				System.out.println(":\t Result: ERR_CRC");
				throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_CRC);
			}
		}
		else {
//			System.out.println(":\t Result: ERR_RESPONSE_UNEXPECTED");
			throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
		}

		if(command == UartPacketDetails.PACKET_CMD.GET){
			String rxBytes = UtilShimmer.bytesToHexString(rxBuf);
			if(rxBytes==null){
				rxBytes ="";
			}
			else {
				rxBytes = "rxbytes: " + rxBytes;
			}
//			System.out.println(":\t" + rxBytes + "\t Result: OK");
		}
		else {
//			System.out.println("\t Result: OK");
		}

		return rxBuf;
    }
    
    /**
     * @param command
     * @param msgArg a two-byte array of containing the Component and respective Property to get
     * @param value
     * @return
     */
    private byte[] assembleTxPacket(int command, ComponentPropertyDetails msgArg, byte[] value) {
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

		byte[] calculatedCrc = shimmerUartCrcCalc(txBufPreCrc, txBufPreCrc.length);
		
		byte[] txBufPostCrc = new byte[txBufPreCrc.length + 2]; 
		System.arraycopy(txBufPreCrc, 0, txBufPostCrc, 0, txBufPreCrc.length);
		System.arraycopy(calculatedCrc, 0, txBufPostCrc, txBufPreCrc.length, calculatedCrc.length);
		
		return txBufPostCrc;
    }
    
	
	/**
	 * @param rxBuf
	 * @param expectedResponse
	 * @throws DockException
	 */
	private void checkForExpectedResponse(byte[] rxBuf, int expectedResponse) throws DockException {
		byte[] crcBuf;
		// Check for expected response
		if(rxBuf[1] != expectedResponse) {
			// Response is not the expected response type
			if(rxBuf[1] == (byte)UartPacketDetails.PACKET_CMD.BAD_CMD_RESPONSE) {
				crcBuf = shimmerUartRxBytes(2);
				throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD);
			}
			else if(rxBuf[1] == (byte)UartPacketDetails.PACKET_CMD.BAD_ARG_RESPONSE) {
				crcBuf = shimmerUartRxBytes(2);
				throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG);
			}
			else if(rxBuf[1] == (byte)UartPacketDetails.PACKET_CMD.BAD_CRC_RESPONSE) {
				crcBuf = shimmerUartRxBytes(2);
				throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC);
			}
			else {
				crcBuf = shimmerUartRxBytes(2);
				throw new DockException(mDockID,mShimmerUARTComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED);
			}
//			clearSerialPortRxBuffer();
		}
	}
	
    /** Calculate the CRC per byte
	 * @param crc the start CRC value
	 * @param b the byte to calculate the CRC on
	 * @return the new CRC value
	 */
    private static int shimmerUartCrcByte(int crc, byte b) {
    	crc &= 0xFFFF;
        crc = ((crc & 0xFFFF) >>> 8) | ((crc & 0xFFFF) << 8);
        crc ^= (b&0xFF);
        crc ^= (crc & 0xFF) >>> 4;
        crc ^= crc << 12;
        crc ^= (crc & 0xFF) << 5;
    	crc &= 0xFFFF;
        return crc;
    }

    /** Calculate the CRC for a byte array. array[0] is CRC LSB, array[1] is CRC MSB 
	 * @param msg the input byte array
	 * @param len the length of the byte array to calculate the CRC on
	 * @return the calculated CRC value
	 */
    public static byte[] shimmerUartCrcCalc(byte[] msg, int len) {
        int CRC_INIT = 0xB0CA;
        int crcCalc;
        int i;

        crcCalc = shimmerUartCrcByte(CRC_INIT, msg[0]);
        for (i = 1; i < len; i++) {
            crcCalc = shimmerUartCrcByte(crcCalc, (msg[i]));
        }
        if (len % 2 > 0) {
            crcCalc = shimmerUartCrcByte(crcCalc, (byte)0x00);
        }
        
        byte[] crcCalcArray = new byte[2];
        crcCalcArray[0] = (byte)(crcCalc & 0xFF);  // CRC LSB
        crcCalcArray[1] = (byte)((crcCalc >> 8) & 0xFF); // CRC MSB 
        
        return crcCalcArray;
    }

    /** Check the CRC stored at the end of the byte array 
	 * @param msg the input byte array
	 * @return a boolean value value, true if CRC matches and false if CRC doesn't match
	 */
    public static boolean shimmerUartCrcCheck(byte[] msg) {
        byte[] crc = shimmerUartCrcCalc(msg, msg.length - 2);
        
        if ((crc[0] == msg[msg.length - 2])
        		&& (crc[1] == msg[msg.length - 1]))
            return true;
        else
            return false;
    }

	public boolean isUARTinUse(){
		return mIsUARTInUse;
	}
    
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
