package com.shimmerresearch.pcDriver;

import jssc.SerialPort;

import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.Shimmer4;
import com.shimmerresearch.driver.Shimmer4Test;
import com.shimmerresearch.pcserialport.ShimmerSerialPortJssc;

public class testShimmer4 {
	

	
	
	public static void main(String[] args) {
		Shimmer4 shimmer = new Shimmer4();
		shimmer.setRadio(new ShimmerRadioProtocol(new ShimmerSerialPortJssc("COM89","COM89",SerialPort.BAUDRATE_115200),new LiteProtocol()));
		try {
			shimmer.mShimmerRadioHWLiteProtocol.connect();
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
