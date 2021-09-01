package com.shimmerresearch.driver.ble;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.shimmerresearch.pcSerialPort.SerialPortCommJssc;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;

import jssc.SerialPort;

public class VerisenseUSBDeviceTest {

	SerialPortByteCommunication radio1 = new SerialPortByteCommunication(new SerialPortCommJssc("COM4","COM4",SerialPort.BAUDRATE_115200));
	//BleRadioByteCommunication radio1 = new BleRadioByteCommunication("00001800-0000-1000-8000-00805f9b34fb", "bleconsoleapp\\BLEConsoleApp1.exe");
	SerialPortByteCommunication radio2 = new SerialPortByteCommunication(new SerialPortCommJssc("COM4","COM4",SerialPort.BAUDRATE_115200));
	VerisenseProtocolByteCommunication protocol1 = new VerisenseProtocolByteCommunication(radio1);
	VerisenseProtocolByteCommunication protocol2 = new VerisenseProtocolByteCommunication(radio2);
	VerisenseDevice device1 = new VerisenseDevice();
	VerisenseDevice device2 = new VerisenseDevice();

	public void initialize() {
		device1.setProtocol(protocol1);
		device2.setProtocol(protocol2);
		JFrame frame = new JFrame();
		frame.setSize(333, 369);
		frame.getContentPane().setLayout(null);

		JButton btnNewButton = new JButton("Connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.connect();
			}
		});
		btnNewButton.setBounds(12, 13, 124, 25);
		frame.getContentPane().add(btnNewButton);

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.disconnect();
			}
		});
		btnDisconnect.setBounds(12, 238, 124, 25);
		frame.getContentPane().add(btnDisconnect);

		JButton btnNewButton_1 = new JButton("Read Status");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.readStatus();
			}
		});
		btnNewButton_1.setBounds(12, 51, 124, 25);
		frame.getContentPane().add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Read Data");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.readLoggedData();
			}
		});
		btnNewButton_2.setBounds(12, 127, 124, 25);
		frame.getContentPane().add(btnNewButton_2);

		JButton button = new JButton("Connect");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.connect();
			}
		});
		button.setBounds(162, 13, 124, 25);
		frame.getContentPane().add(button);

		JButton button_1 = new JButton("Read Status");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.readStatus();
			}
		});
		button_1.setBounds(162, 51, 124, 25);
		frame.getContentPane().add(button_1);

		JButton button_2 = new JButton("Read Data");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.readLoggedData();
			}
		});
		button_2.setBounds(162, 127, 124, 25);
		frame.getContentPane().add(button_2);

		JButton button_3 = new JButton("Disconnect");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.disconnect();
			}
		});
		button_3.setBounds(162, 238, 124, 25);
		frame.getContentPane().add(button_3);

		JButton btnStartstreaming = new JButton("StartStreaming");
		btnStartstreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.startStreaming();
			}
		});
		btnStartstreaming.setBounds(12, 165, 124, 25);
		frame.getContentPane().add(btnStartstreaming);

		JButton btnStopstreaming = new JButton("StopStreaming");
		btnStopstreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.stopStreaming();
			}
		});

		btnStopstreaming.setBounds(12, 200, 124, 25);
		frame.getContentPane().add(btnStopstreaming);

		JButton button_4 = new JButton("StartStreaming");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.startStreaming();
			}
		});
		button_4.setBounds(162, 165, 124, 25);
		frame.getContentPane().add(button_4);

		JButton button_5 = new JButton("StopStreaming");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.stopStreaming();
			}
		});
		button_5.setBounds(162, 200, 124, 25);
		frame.getContentPane().add(button_5);

		JButton btnReadOp = new JButton("Read Op");
		btnReadOp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.readOperationalConfig();
			}
		});
		btnReadOp.setBounds(12, 89, 124, 25);
		frame.getContentPane().add(btnReadOp);

		JButton btnReadOp_1 = new JButton("Read Op");
		btnReadOp_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.readOperationalConfig();
			}
		});
		btnReadOp_1.setBounds(162, 89, 124, 25);
		frame.getContentPane().add(btnReadOp_1);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				protocol1.stop();
				protocol2.stop();
			}
		});
	}

	public static void main(String[] args) {
		VerisenseUSBDeviceTest test = new VerisenseUSBDeviceTest();
		test.initialize();

		// connect

		// System.out.println(p);
		// p.destroy();
	}
}
