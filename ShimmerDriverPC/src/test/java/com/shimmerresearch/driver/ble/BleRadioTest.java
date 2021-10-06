package com.shimmerresearch.driver.ble;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

public class BleRadioTest {
	
	BleRadioByteCommunication radio1 = new BleRadioByteCommunication("00000000-0000-0000-0000-e7452c6d6f14","bleconsoleapp\\BLEConsoleApp2.exe", new ByteCommunicationListener() {
		
		@Override
		public void eventNewBytesReceived(byte[] rxBytes) {
			// TODO Auto-generated method stub
			System.out.println("EVENT BYTES 6f14" + Hex.toHexString(rxBytes));
		}
		
		@Override
		public void eventDisconnected() {
			// TODO Auto-generated method stub
			System.out.println("EVENT DISCONNECTED 6f14");
		}
		
		@Override
		public void eventConnected() {
			// TODO Auto-generated method stub
			System.out.println("EVENT CONNECTED 6f14");
		}
	});
	
	BleRadioByteCommunication radio2 = new BleRadioByteCommunication("00000000-0000-0000-0000-daa619f04ad7","bleconsoleapp\\BLEConsoleApp1.exe", new ByteCommunicationListener() {
		
		@Override
		public void eventNewBytesReceived(byte[] rxBytes) {
			// TODO Auto-generated method stub
			System.out.println("EVENT BYTES 4ad7" + Hex.toHexString(rxBytes));
		}
		
		@Override
		public void eventDisconnected() {
			// TODO Auto-generated method stub
			System.out.println("EVENT DISCONNECTED 4ad7");
		}
		
		@Override
		public void eventConnected() {
			// TODO Auto-generated method stub
			System.out.println("EVENT CONNECTED 4ad7");
			
		}
	});
	
	public void initialize() {

		JFrame frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(331, 268);
		frame.getContentPane().setLayout(null);

		JButton btnNewButton = new JButton("Connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio1.WriteDataToProcess("Connect");
			}
		});
		btnNewButton.setBounds(12, 13, 124, 25);
		frame.getContentPane().add(btnNewButton);

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio1.WriteDataToProcess("Disconnect");
			}
		});
		btnDisconnect.setBounds(12, 183, 124, 25);
		frame.getContentPane().add(btnDisconnect);

		JButton btnNewButton_1 = new JButton("Read Status");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio1.WriteDataToProcess("Write110000");
			}
		});
		btnNewButton_1.setBounds(12, 51, 124, 25);
		frame.getContentPane().add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Sync");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio1.WriteDataToProcess("Write120000");
			}
		});
		btnNewButton_2.setBounds(12, 89, 124, 25);
		frame.getContentPane().add(btnNewButton_2);

		JButton button = new JButton("Connect");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio2.WriteDataToProcess("Connect");
			}
		});
		button.setBounds(162, 13, 124, 25);
		frame.getContentPane().add(button);

		JButton button_1 = new JButton("Read Status");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio2.WriteDataToProcess("Write110000");
			}
		});
		button_1.setBounds(162, 51, 124, 25);
		frame.getContentPane().add(button_1);

		JButton button_2 = new JButton("Sync");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio2.WriteDataToProcess("Write120000");
			}
		});
		button_2.setBounds(162, 89, 124, 25);
		frame.getContentPane().add(button_2);

		JButton button_3 = new JButton("Disconnect");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radio2.WriteDataToProcess("Disconnect");
			}
		});
		button_3.setBounds(162, 183, 124, 25);
		frame.getContentPane().add(button_3);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				radio1.DestroyProcess();
				radio2.DestroyProcess();
			}
		});
	}

	public static void main(String[] args) {
		BleRadioTest test = new BleRadioTest();
		test.initialize();

		// connect

		// System.out.println(p);
		// p.destroy();
	}
}
