package com.shimmerresearch.driver.ble;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.bouncycastle.util.encoders.Hex;

public class VerisenseProtocolByteCommunicationTest {
	
	BleRadioByteCommunication radio1 = new BleRadioByteCommunication("00000000-0000-0000-0000-e7452c6d6f14","C:\\repos\\ShimmerCSharpBLEAPI_Example\\Source\\ConsoleApp1\\bin\\Debug\\netcoreapp3.1\\ConsoleApp1.exe");
	BleRadioByteCommunication radio2 = new BleRadioByteCommunication("00000000-0000-0000-0000-daa619f04ad7","C:\\repos\\ShimmerCSharpBLEAPI_Example\\Source\\ConsoleApp1\\bin\\Debug\\netcoreapp3.1\\ConsoleApp2.exe");
	VerisenseProtocolByteCommunication protocol1 = new VerisenseProtocolByteCommunication(radio1);
	VerisenseProtocolByteCommunication protocol2 = new VerisenseProtocolByteCommunication(radio2);
	public void initialize() {
		
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
		btnDisconnect.setBounds(12, 200, 124, 25);
		frame.getContentPane().add(btnDisconnect);
		
		JButton btnNewButton_1 = new JButton("Read Status");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.readStatus();
			}
		});
		btnNewButton_1.setBounds(12, 51, 124, 25);
		frame.getContentPane().add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Sync");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.syncData();
			}
		});
		btnNewButton_2.setBounds(12, 89, 124, 25);
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
		
		JButton button_2 = new JButton("Sync");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.syncData();
			}
		});
		button_2.setBounds(162, 89, 124, 25);
		frame.getContentPane().add(button_2);
		
		JButton button_3 = new JButton("Disconnect");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol2.disconnect();
			}
		});
		button_3.setBounds(162, 200, 124, 25);
		frame.getContentPane().add(button_3);
		
		JButton btnStartstreaming = new JButton("StartStreaming");
		btnStartstreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.startStreaming();
			}
		});
		btnStartstreaming.setBounds(12, 127, 124, 25);
		frame.getContentPane().add(btnStartstreaming);
		
		JButton btnStopstreaming = new JButton("StopStreaming");
		btnStopstreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocol1.stopStreaming();
			}
		});
		
		btnStopstreaming.setBounds(12, 162, 124, 25);
		frame.getContentPane().add(btnStopstreaming);
		
		JButton button_4 = new JButton("StartStreaming");
		button_4.setBounds(162, 127, 124, 25);
		frame.getContentPane().add(button_4);
		
		JButton button_5 = new JButton("StopStreaming");
		button_5.setBounds(162, 162, 124, 25);
		frame.getContentPane().add(button_5);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
            	protocol1.stop();
            	protocol2.stop();
            }
        });
	}
	public static void main(String[] args) {
		VerisenseProtocolByteCommunicationTest test = new VerisenseProtocolByteCommunicationTest();
		test.initialize();
		
		//connect
		
		
		//System.out.println(p);
		//p.destroy();
}
}
