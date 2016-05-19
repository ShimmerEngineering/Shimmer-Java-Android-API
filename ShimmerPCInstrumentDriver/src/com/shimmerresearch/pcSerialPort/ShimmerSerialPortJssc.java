package com.shimmerresearch.pcSerialPort;

import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class ShimmerSerialPortJssc extends SerialPortJssc{
	
	public ShimmerSerialPortJssc(String comPort, int baudToUse) {
		this(comPort, comPort, baudToUse);
	}
	
	public ShimmerSerialPortJssc(String comPort, String uniqueId, int baudToUse) {
		super(comPort, uniqueId, baudToUse);
		SERIAL_PORT_TIMEOUT = 2000; // was 2000
	}
	
	public ShimmerVerObject getShimmerVerObject() {
		// TODO Auto-generated method stub
		byte[] instruction = {GET_SHIMMER_VERSION_COMMAND}; //every radio should have a method to get the version object, the command value should be the same across all byte radios
		byte[] response;
		try {
			txBytes(instruction);
			Thread.sleep(200);
			response = rxBytes(3);
			int hardwareVersion = response[2];
			instruction[0] = GET_FW_VERSION_COMMAND;
			txBytes(instruction);
			byte[] bufferInquiry = new byte[6]; 
			Thread.sleep(200);
			rxBytes(1);
			rxBytes(1);
			bufferInquiry = rxBytes(6);
			int firmwareIdentifier=(int)((bufferInquiry[1]&0xFF)<<8)+(int)(bufferInquiry[0]&0xFF);
			int firmwareVersionMajor = (int)((bufferInquiry[3]&0xFF)<<8)+(int)(bufferInquiry[2]&0xFF);
			int firmwareVersionMinor = ((int)((bufferInquiry[4]&0xFF)));
			int firmwareVersionInternal=(int)(bufferInquiry[5]&0xFF);
			ShimmerVerObject sVOHw = new ShimmerVerObject(hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
			return sVOHw;
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

}