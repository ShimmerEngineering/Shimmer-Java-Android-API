package com.shimmerresearch.pcDriver;

import jssc.SerialPort;

import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.Shimmer4Test;
import com.shimmerresearch.pcserialport.ShimmerSerialPortJssc;

public class testShimmer4 {
	

	
	
	public static void main(String[] args) {
		Shimmer4Test shimmer = new Shimmer4Test(new ShimmerSerialPortJssc("COM89","COM89",SerialPort.BAUDRATE_115200));
		try {
			shimmer.mShimmerRadio.connect();
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
