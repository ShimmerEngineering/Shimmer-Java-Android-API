package com.shimmerresearch.driver.ble;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.grpc.GrpcBLERadioByteCommunication;
import com.shimmerresearch.grpc.GrpcBLERadioByteTools;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;

public class VerisenseProtocolByteGrpcCommunicationTest {

	//	GrpcBLERadioByteCommunication radio1 = new GrpcBLERadioByteCommunication("e7:45:2c:6d:6f:14");
	GrpcBLERadioByteCommunication radio1;
	//BTHLE\Dev_ec2ee3ebb799
	//BTHLE\Dev_ca09b224625a

	//GrpcBLERadioByteCommunication radio2 = new GrpcBLERadioByteCommunication("da:a6:19:f0:4a:d7");
	//GrpcBLERadioByteCommunication radio2 = new GrpcBLERadioByteCommunication("ec2ee3ebb799");
	GrpcBLERadioByteCommunication radio2;
	GrpcBLERadioByteCommunication radio3;

	VerisenseProtocolByteCommunication protocol1;
	VerisenseProtocolByteCommunication protocol2;
	VerisenseProtocolByteCommunication protocol3;

	public void initialize() {
		GrpcBLERadioByteTools grpcTool = new GrpcBLERadioByteTools();
		try {
			int port = grpcTool.startServer();
			//	GrpcBLERadioByteCommunication radio1 = new GrpcBLERadioByteCommunication("e7:45:2c:6d:6f:14");
			radio1 = new GrpcBLERadioByteCommunication("e7:45:2c:6d:6f:14","localhost",port);
			//BTHLE\Dev_ec2ee3ebb799
			//BTHLE\Dev_ca09b224625a

			//GrpcBLERadioByteCommunication radio2 = new GrpcBLERadioByteCommunication("da:a6:19:f0:4a:d7");
			//GrpcBLERadioByteCommunication radio2 = new GrpcBLERadioByteCommunication("ec2ee3ebb799");
			radio2 = new GrpcBLERadioByteCommunication("ec2ee3ebb799","localhost",port);
			radio3 = new GrpcBLERadioByteCommunication("ca09b224625a","localhost",port);

			protocol1 = new VerisenseProtocolByteCommunication(radio1);
			protocol2 = new VerisenseProtocolByteCommunication(radio2);
			protocol3 = new VerisenseProtocolByteCommunication(radio3);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		JFrame frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(500, 369);
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
		btnDisconnect.setBounds(12, 276, 124, 25);
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
		button_3.setBounds(162, 276, 124, 25);
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

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				protocol1.stop();
				protocol2.stop();
			}
		});
		
		JButton button_6 = new JButton("Speed Test");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol2.startSpeedTest();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button_6.setBounds(162, 200, 124, 25);
		frame.getContentPane().add(button_6);
		
		JButton button_7 = new JButton("Stop Speed Test");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol2.stopSpeedTest();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button_7.setBounds(162, 238, 124, 25);
		frame.getContentPane().add(button_7);
		
		JButton btnStartSpeedTest = new JButton("Speed Test");
		btnStartSpeedTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.startSpeedTest();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStartSpeedTest.setBounds(12, 200, 124, 25);
		frame.getContentPane().add(btnStartSpeedTest);
		
		JButton btnStopSpeedTest = new JButton("Stop Speed Test");
		btnStopSpeedTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol1.stopSpeedTest();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStopSpeedTest.setBounds(12, 238, 124, 25);
		frame.getContentPane().add(btnStopSpeedTest);
		
		JButton btnConnect3 = new JButton("Connect");
		btnConnect3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.connect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnConnect3.setBounds(312, 13, 124, 25);
		frame.getContentPane().add(btnConnect3);

		JButton btnDisconnect3 = new JButton("Disconnect");
		btnDisconnect3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.disconnect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnDisconnect3.setBounds(312, 276, 124, 25);
		frame.getContentPane().add(btnDisconnect3);

		JButton btnReadStatus3 = new JButton("Read Status");
		btnReadStatus3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.readStatus();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnReadStatus3.setBounds(312, 51, 124, 25);
		frame.getContentPane().add(btnReadStatus3);

		JButton btnReadData3 = new JButton("Read Data");
		btnReadData3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.readLoggedData();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnReadData3.setBounds(312, 89, 124, 25);
		frame.getContentPane().add(btnReadData3);
		
		JButton btnStartstreaming3 = new JButton("StartStreaming");
		btnStartstreaming3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.startStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStartstreaming3.setBounds(312, 127, 124, 25);
		frame.getContentPane().add(btnStartstreaming3);

		JButton btnStopstreaming3 = new JButton("StopStreaming");
		btnStopstreaming3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.stopStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStopstreaming3.setBounds(312, 162, 124, 25);
		frame.getContentPane().add(btnStopstreaming3);
		
		JButton btnStartSpeedTest3 = new JButton("Speed Test");
		btnStartSpeedTest3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.startSpeedTest();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStartSpeedTest3.setBounds(312, 200, 124, 25);
		frame.getContentPane().add(btnStartSpeedTest3);
		
		JButton btnStopSpeedTest3 = new JButton("Stop Speed Test");
		btnStopSpeedTest3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					protocol3.stopSpeedTest();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStopSpeedTest3.setBounds(312, 238, 124, 25);
		frame.getContentPane().add(btnStopSpeedTest3);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		VerisenseProtocolByteGrpcCommunicationTest test = new VerisenseProtocolByteGrpcCommunicationTest();
		test.initialize();

		// connect

		// System.out.println(p);
		// p.destroy();
	}
}
