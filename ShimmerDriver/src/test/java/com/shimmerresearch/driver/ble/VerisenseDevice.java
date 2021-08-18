package com.shimmerresearch.driver.ble;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;

public class VerisenseDevice extends ShimmerDevice  {

	
	
	@Override
	public ShimmerDevice deepClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(byte[] configBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createConfigBytesLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

}
