package com.shimmerresearch.driver.ble;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;

public class VerisenseProtocolByteJavelinCommunicationTest {

	JavelinBLERadioByteCommunication radio1 = new JavelinBLERadioByteCommunication("e7:45:2c:6d:6f:14");
	JavelinBLERadioByteCommunication radio2 = new JavelinBLERadioByteCommunication("da:a6:19:f0:4a:d7");
	VerisenseProtocolByteCommunication protocol1 = new VerisenseProtocolByteCommunication(radio1);
	VerisenseProtocolByteCommunication protocol2 = new VerisenseProtocolByteCommunication(radio2);

	public void initialize() {

		JFrame frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(333, 369);
		frame.getContentPane().setLayout(null);

		JButton btnNewButton = new JButton("Connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.connect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton.setBounds(12, 13, 124, 25);
		frame.getContentPane().add(btnNewButton);

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.disconnect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnDisconnect.setBounds(12, 200, 124, 25);
		frame.getContentPane().add(btnDisconnect);

		JButton btnNewButton_1 = new JButton("Read Status");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.readStatus();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton_1.setBounds(12, 51, 124, 25);
		frame.getContentPane().add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Read Data");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.readLoggedData();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton_2.setBounds(12, 89, 124, 25);
		frame.getContentPane().add(btnNewButton_2);

		JButton button = new JButton("Connect");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol2.connect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button.setBounds(162, 13, 124, 25);
		frame.getContentPane().add(button);

		JButton button_1 = new JButton("Read Status");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol2.readStatus();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button_1.setBounds(162, 51, 124, 25);
		frame.getContentPane().add(button_1);

		JButton button_2 = new JButton("Read Data");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol2.readLoggedData();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button_2.setBounds(162, 89, 124, 25);
		frame.getContentPane().add(button_2);

		JButton button_3 = new JButton("Disconnect");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol2.disconnect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button_3.setBounds(162, 200, 124, 25);
		frame.getContentPane().add(button_3);

		JButton btnStartstreaming = new JButton("StartStreaming");
		btnStartstreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.startStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStartstreaming.setBounds(12, 127, 124, 25);
		frame.getContentPane().add(btnStartstreaming);

		JButton btnStopstreaming = new JButton("StopStreaming");
		btnStopstreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.stopStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				protocol1.stop();
				protocol2.stop();
			}
		});
	}

	public static void main(String[] args) {
		VerisenseProtocolByteJavelinCommunicationTest test = new VerisenseProtocolByteJavelinCommunicationTest();
		test.initialize();

		// connect

		// System.out.println(p);
		// p.destroy();
	}
}
