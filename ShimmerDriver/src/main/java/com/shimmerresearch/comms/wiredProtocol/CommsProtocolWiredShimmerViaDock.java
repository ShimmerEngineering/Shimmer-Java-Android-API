package com.shimmerresearch.comms.wiredProtocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.shimmerresearch.comms.StringListener;
import com.shimmerresearch.comms.serialPortInterface.InterfaceSerialPortHal;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.shimmer2r3.BluetoothModuleVersionDetails;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import bolts.TaskCompletionSource;

/**Driver for managing and configuring the Shimmer through the Dock using the 
 * Shimmer's dock connected UART.
 * 
 * @author Mark Nolan
 *
 */
public class CommsProtocolWiredShimmerViaDock extends AbstractCommsProtocolWired {
	
	StringListener mStringTestListener;

	public CommsProtocolWiredShimmerViaDock(String comPort, String uniqueId, InterfaceSerialPortHal serialPortInterface){
		super(comPort, uniqueId, serialPortInterface);
		serialPortInterface.registerSerialPortRxEventCallback(this);
	}
	
	/** Reads the MAC address from the Shimmer UART
	 * @return MAC address of docked Shimmer
	 * @throws ExecutionException 
	 */
	public String readMacId() throws DockException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_MAC_ID_GET;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.MAC, errorCode);
		
		// Parse response string
		if(rxBuf.length >= 6) {
			rxBuf = Arrays.copyOf(rxBuf, 6);
		}
		else {
			throw new DockException(mUniqueId, mComPort, errorCode, ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}
		
		return UtilShimmer.bytesToHexString(rxBuf);
	}
	
	/** Reads the battery charge status from the Shimmer UART
	 * @return battery charge status of docked Shimmer
	 * @see ShimmerBattStatusDetails
	 * @throws ExecutionException
	 */
	public ShimmerBattStatusDetails readBattStatus() throws ExecutionException{
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_BATT_STATUS_GET;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.BAT.VALUE, errorCode);
		
		// Parse response string
		if(rxBuf.length < 3) {
			throw new DockException(mUniqueId, mComPort, errorCode, ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}
		
		ShimmerBattStatusDetails shimmerUartBattStatusDetails = new ShimmerBattStatusDetails(rxBuf);
		return shimmerUartBattStatusDetails;
	}
	
	/** Reads the firmware version from the Shimmer UART
	 * @return firmware version of docked Shimmer
	 * @see ShimmerHwFwDetails
	 * @throws ExecutionException
	 */
	public ShimmerVerObject readHwFwVersion() throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_VERSION_INFO_GET;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.VER, errorCode);
		
		// Parse response string
		if(rxBuf.length < 7) {
			throw new DockException(mUniqueId, mComPort, errorCode, ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}

		ShimmerVerObject shimmerVerDetails = new ShimmerVerObject(rxBuf);
		return shimmerVerDetails;
	}


	/** Read the real time clock config time of the Shimmer
	 * @return long the Shimmer RTC configure time in milliseconds UNIX time (since 1970-Jan-01 00:00:00 UTC)
	 * @throws ExecutionException
	 */
	public long readRealWorldClockConfigTime() throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_GET;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.RTC_CFG_TIME, errorCode);
		
		// Parse response string
		long responseTime = 0;
		if(rxBuf.length >= 8) {
			byte[] rwcTimeArray = Arrays.copyOf(rxBuf, 8);
			responseTime = UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(rwcTimeArray);
		}
		else {
			throw new DockException(mUniqueId, mComPort, errorCode, ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}

		return responseTime;
	}
	
	/** Read the current real time clock of the Shimmer
	 * @return long the current Shimmer RTC time in milliseconds UNIX time (since 1970-Jan-01 00:00:00 UTC)
	 * @throws ExecutionException
	 */
	public long readCurrentTime() throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_RTC_CURRENT_TIME_GET;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.CURR_LOCAL_TIME, errorCode);
		
		// Parse response string
		long responseTime = 0;
		if(rxBuf.length >= 8) {
			byte[] rwcTimeArray = Arrays.copyOf(rxBuf, 8);
			responseTime = UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(rwcTimeArray);
		}
		else {
			throw new DockException(mUniqueId, mComPort, errorCode, ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
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
		byte[] rwcTimeArray = UtilShimmer.convertMilliSecondsToShimmerRtcDataBytesLSB(miliseconds);
		
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_RTC_CONFIG_TIME_SET;
		processShimmerSetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.RTC_CFG_TIME, rwcTimeArray, errorCode);
		
		return miliseconds;
	}
	
	/** Reads from the Shimmer's InfoMem using Dock UART
	 * @return returns the infomem of the docked Shimmer device
	 * @throws ExecutionException
	 */
	public byte[] readInfoMem(int address, int size) throws ExecutionException{
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_INFOMEM_GET;
		byte[] rxBuf = processShimmerMemGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.INFOMEM, address, size, errorCode);
		
		return rxBuf;
	}
	
	/** Writes to the Shimmer's InfoMem using Dock UART
	 * @param address the InfoMem address to write to
	 * @param buf the byte array to write
	 * @throws ExecutionException
	 */
	public void writeInfoMem(int address, byte[] buf) throws ExecutionException{
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_INFOMEM_SET;
		processShimmerMemSetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.INFOMEM, address, buf, errorCode);
	}
	
	/** Reads the daughter card ID via the Shimmer's Dock UART (the first 16 bytes of the daughter card memory)
	 * @return ShimmerUartExpansionBoardDetails the parsed daughter card ID information 
	 * @see ExpansionBoardDetails
	 * @throws ExecutionException
	 */
	public ExpansionBoardDetails readDaughterCardID() throws ExecutionException{
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_DAUGHTER_ID_GET;
		byte[] rxBuf = processShimmerMemGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.DAUGHTER_CARD.CARD_ID, 0, 16, errorCode);
		
		//TODO: check rxBuf
		ExpansionBoardDetails shimmerUartExpansionBoardDetails = new ExpansionBoardDetails(rxBuf);
		return shimmerUartExpansionBoardDetails;
	}
	
	/** Writes the daughter card ID via the Shimmer's Dock UART
	 * @param address the new daughter card memory address to start writing at
	 * @param buf the byte array to be written to the daughter card ID (< 16 bytes)
	 * @throws ExecutionException
	 */
	public void writeDaughterCardId(int address, byte[] buf) throws ExecutionException{
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_DAUGHTER_ID_SET;
		processShimmerMemSetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.DAUGHTER_CARD.CARD_ID, address, buf, errorCode);
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

		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_DAUGHTER_MEM_GET;
		byte[] rxBuf = processShimmerMemGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.DAUGHTER_CARD.CARD_MEM, address, size, errorCode);
		
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

		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_DAUGHTER_MEM_SET;
		processShimmerMemSetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.DAUGHTER_CARD.CARD_MEM, address, buf, errorCode);
	}
	
	
	/** Reads the 802.15.4 radio settings for ShimmerGQ_802.15.4. Channel, Address and Group ID from the Shimmer UART
	 * @return 
	 * @throws ExecutionException 
	 */
	public byte[] read802154RadioSettings() throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_RADIO_802154_GET_SETTINGS;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.RADIO_802154.SETTINGS, errorCode);

		// Parse response string
		if(rxBuf.length >= 9) {
			rxBuf = Arrays.copyOf(rxBuf, 9);
		}
		else {
			throw new DockException(mUniqueId, mComPort,errorCode,ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}
		
		return rxBuf;
	}
	
	/** Read the BT FW version 
	 * @return String in bytes format
	 * @throws ExecutionException
	 */
	public BluetoothModuleVersionDetails readBtFwVersion() throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_BT_FW_VERSION_INFO_GET;
		byte[] rxBuf = processShimmerGetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.BLUETOOTH.VER, errorCode);
		
		// Parse response string
		if(rxBuf.length == 0) {
			throw new DockException(mUniqueId, mComPort,errorCode,ErrorCodesWiredProtocol.SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS);
		}
		BluetoothModuleVersionDetails bluetoothModuleVersionDetails = new BluetoothModuleVersionDetails();
		bluetoothModuleVersionDetails.parseBtModuleVerBytes(rxBuf);

		return bluetoothModuleVersionDetails;
	}

	public void addTestStringListener(StringListener stringTestListener) {
		mStringTestListener = stringTestListener;
	}
	boolean ackNotReceivedForTestCommand = true;
	/**
	 * @param timeoutInSeconds
	 * @return
	 * @throws ExecutionException
	 */
	public boolean readMainTest(UartComponentPropertyDetails details) throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_VERSION_INFO_GET;
		//rxBuf = processShimmerSetCommand(compPropDetails, txBuf, errorCode);(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.DEVICE_TEST.MAIN_TEST, -1);
		TaskCompletionSource tcs = new TaskCompletionSource<>();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ackNotReceivedForTestCommand = true;
		setListener(new ByteCommunicationListener() {
			
			@Override
			public void eventNewBytesReceived(byte[] rxBytes) {
				// TODO Auto-generated method stub
				System.out.println("Test : " + UtilShimmer.bytesToHexString(rxBytes));
				try {
					outputStream.write(rxBytes);
					String result = new String(outputStream.toByteArray());
					if (ackNotReceivedForTestCommand) {
						byte[] originalBytes = outputStream.toByteArray();
						boolean match = UtilShimmer.doesFirstBytesMatch(originalBytes, TEST_ACK);
						if (match) {
							outputStream.reset();
							outputStream.write(originalBytes, TEST_ACK.length, originalBytes.length - TEST_ACK.length);
							result = new String(outputStream.toByteArray());
							ackNotReceivedForTestCommand = false;
						}
						
						
					}
					
					System.out.println(result);
					if (mStringTestListener!=null){
						mStringTestListener.eventNewStringRx(result);
					}
					if (result.contains(TEST_ENDING)) {
						tcs.setResult(true);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					tcs.setResult(false);
				}
				
			}
			
			@Override
			public void eventDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void eventConnected() {
				// TODO Auto-generated method stub
				
			}
		});
        // Parse response string
		mTestStreaming = true;
		//processShimmerSetCommandNoWait(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.DEVICE_TEST.MAIN_TEST, errorCode, null);
		processShimmerSetCommandNoWait(details, errorCode, null);
		boolean completed = false;
		try {
			completed = tcs.getTask().waitForCompletion(TIMEOUT_IN_SHIMMER_TEST, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mTestStreaming = false;
			return false;
		}
		mTestStreaming = false;
		return completed;
	}
	
	public boolean isTestStreaming() {
		return mTestStreaming;
	}
	
	/** Read the BT FW version 
	 * @return String in bytes format
	 * @throws ExecutionException
	 */
	public void writeJumpToBootloaderMode(byte secondsDelay) throws ExecutionException {
		int errorCode = ErrorCodesWiredProtocol.SHIMMERUART_CMD_ERR_ENTER_BOOTLOADER_SET;
		processShimmerSetCommand(UartPacketDetails.UART_COMPONENT_AND_PROPERTY.MAIN_PROCESSOR.ENTER_BOOTLOADER, new byte[] {secondsDelay}, errorCode);
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}


}
