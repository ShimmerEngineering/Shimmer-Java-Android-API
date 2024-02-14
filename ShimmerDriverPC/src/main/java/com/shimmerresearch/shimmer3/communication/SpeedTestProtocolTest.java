package com.shimmerresearch.shimmer3.communication;

import javax.swing.JFrame;

import com.shimmerresearch.comms.SerialPortByteCommunication;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcSerialPort.SerialPortCommJssc;

import jssc.SerialPort;

public class SpeedTestProtocolTest {

	static JFrame frame;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		frame = new JFrame();
		frame.setSize(200, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		SerialPortCommJssc portspp = new SerialPortCommJssc("COM7","COM7",SerialPort.BAUDRATE_115200);
		portspp.setVerboseMode(false, false);
		SerialPortByteCommunication port = new SerialPortByteCommunication(portspp);
		SpeedTestProtocol protocol = new SpeedTestProtocol(port);
		try {
			protocol.connect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		protocol.startSpeedTest();
		
	}

}
