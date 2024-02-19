package com.shimmerresearch.shimmer3.communication;

import javax.swing.JFrame;

import com.shimmerresearch.comms.SerialPortByteCommunication;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcSerialPort.SerialPortCommJssc;
import com.shimmerresearch.shimmer3.communication.SpeedTestProtocol.SpeedTestResult;

import jssc.SerialPort;
import javax.swing.JLabel;

public class SpeedTestProtocolTest {

	static JFrame frame;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		frame = new JFrame();
		frame.setSize(1356, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setBounds(10, 11, 1219, 58);
		frame.getContentPane().add(lblNewLabel);
		SerialPortCommJssc portspp = new SerialPortCommJssc("COM7","COM7",SerialPort.BAUDRATE_115200);
		portspp.setVerboseMode(false, false);
		SerialPortByteCommunication port = new SerialPortByteCommunication(portspp);
		SpeedTestProtocol protocol = new SpeedTestProtocol(port);
		protocol.setListener(new SpeedTestResult() {
			
			@Override
			public void onNewResult(String result) {
				// TODO Auto-generated method stub
				lblNewLabel.setText(result);
			}

			@Override
			public void onConnected() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDisconnected() {
				// TODO Auto-generated method stub
				
			}
		});
		try {
			protocol.connect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		protocol.startSpeedTest();
		
	}
}
