package com.shimmerresearch.bluetooth;

import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsGet;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortComm;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class ShimmerRadioInitializer {

	protected AbstractSerialPortComm serialCommPort;
	public abstract AbstractSerialPortComm getSerialCommPort();
	
	
	public ShimmerVerObject getShimmerVerObject(){
		try {
			int hardwareVersion = getHardwareVersion();
			byte[] bufferFWVersion = getFirmwareVersion(); 
			ShimmerVerObject shimmerVersion = createShimmerVerObject(hardwareVersion, bufferFWVersion);
			
			return shimmerVersion;
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	protected int getHardwareVersion() throws DeviceException, InterruptedException{
		//every radio should have a method to get the version object, the command value should be the same across all byte radios
		byte[] instruction = {InstructionsGet.GET_SHIMMER_VERSION_COMMAND_VALUE}; 
		
		serialCommPort.txBytes(instruction);
		Thread.sleep(200);
		byte[] response = serialCommPort.rxBytes(3);
		int hardwareVersion = response[2];
		
		return hardwareVersion;
	}
	
	protected byte[] getFirmwareVersion() throws DeviceException, InterruptedException{
		byte[] instruction = {InstructionsGet.GET_FW_VERSION_COMMAND_VALUE};
		
		serialCommPort.txBytes(instruction);
		byte[] bufferInquiry = new byte[6]; 
		Thread.sleep(200);
		serialCommPort.rxBytes(1);
		serialCommPort.rxBytes(1);
		bufferInquiry = serialCommPort.rxBytes(6);
		
		return bufferInquiry;
	}
	
	protected ShimmerVerObject createShimmerVerObject(int hardwareVersion, byte[] bufferInquiry){
		int firmwareIdentifier=(int)((bufferInquiry[1]&0xFF)<<8)+(int)(bufferInquiry[0]&0xFF);
		int firmwareVersionMajor = (int)((bufferInquiry[3]&0xFF)<<8)+(int)(bufferInquiry[2]&0xFF);
		int firmwareVersionMinor = ((int)((bufferInquiry[4]&0xFF)));
		int firmwareVersionInternal=(int)(bufferInquiry[5]&0xFF);
		ShimmerVerObject shimmerVersion = new ShimmerVerObject(hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
		
		return shimmerVersion;
	}
}
