package com.shimmerresearch.simpleexamples;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SensorMapsExample extends BasicProcessWithCallBack {
	
	private JFrame frame;
	private JTextField textField;
	JTextPane textPaneStatus;
	static ShimmerPC shimmer = new ShimmerPC("ShimmerDevice");
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	
	/**
	 * Initialize the contents of the frame
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
		frame = new JFrame("Shimmer SensorMaps Example");
		frame.setBounds(100, 100, 560, 487);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSetComPort = new JLabel("Set COM Port");
		lblSetComPort.setBounds(0, 60, 119, 23);
		frame.getContentPane().add(lblSetComPort);
		
		textField = new JTextField();
		textField.setToolTipText("for example COM1, COM2, etc");
		textField.setBounds(0, 91, 144, 29);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnConnect = new JButton("CONNECT");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				btManager.connectShimmerThroughCommPort(textField.getText());
				//shimmer.connect(textField.getText(),"");
				
			}
		});
		btnConnect.setToolTipText("attempt connection to Shimmer device");
		btnConnect.setBounds(161, 90, 154, 31);
		frame.getContentPane().add(btnConnect);
		
		JButton btnDisconnect = new JButton("DISCONNECT");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//btManager.disconnectShimmer(shimmerDevice);
//				try {
//					shimmer.disconnect();
//				} catch(ShimmerException e1) {
//					e1.printStackTrace();
//				}
				
			}
		});
		btnDisconnect.setToolTipText("disconnect from Shimmer device");
		btnDisconnect.setBounds(332, 90, 154, 31);
		frame.getContentPane().add(btnDisconnect);
		
		JLabel lblShimmerStatus = new JLabel("Shimmer Status");
		lblShimmerStatus.setBounds(0, 139, 144, 23);
		frame.getContentPane().add(lblShimmerStatus);
		
		textPaneStatus = new JTextPane();
		textPaneStatus.setBounds(0, 181, 144, 36);
		frame.getContentPane().add(textPaneStatus);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 536, 23);
		frame.getContentPane().add(menuBar);
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenuItem mntmSelectSensors = new JMenuItem("Select sensors");
		mntmSelectSensors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//Ensure the Shimmer is not streaming or SD logging before configuring it
				if(shimmer.isConnected()) {
					if(!shimmer.isStreaming() && !shimmer.isSDLogging()) {
						EnableSensorsDialog sensorsDialog = new EnableSensorsDialog(shimmer);
						sensorsDialog.initialize();
						sensorsDialog.main(null);
					} else {
						JOptionPane.showMessageDialog(frame, "Cannot configure sensors!\nDevice is streaming or SDLogging", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "No device connected!", "Info", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		mnTools.add(mntmSelectSensors);
		
		JMenuItem mntmDeviceConfiguration = new JMenuItem("Device configuration");
		mntmDeviceConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(shimmer.isConnected()) {
					if(!shimmer.isStreaming() && !shimmer.isSDLogging()) {
						SensorConfigDialog configDialog = new SensorConfigDialog();
						configDialog.initialize(shimmer);
					} else {
						JOptionPane.showMessageDialog(frame, "Cannot configure sensors!\nDevice is streaming or SDLogging", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "No device connected!", "Info", JOptionPane.WARNING_MESSAGE);
				}
				
			}
		});
		mnTools.add(mntmDeviceConfiguration);
		
		//TODO: Test
//		JCheckBox[] cBoxTest = new JCheckBox[3];
//		cBoxTest[0] = new JCheckBox("Test 0", true);
//		cBoxTest[0].setBounds(50, 50, 50, 50);
//		frame.getContentPane().add(cBoxTest[0]);
//		cBoxTest[0].setVisible(true);
//		
//		cBoxTest[1] = new JCheckBox("Test 1", true);
//		cBoxTest[1].setBounds(75, 100, 100, 100);
//		frame.getContentPane().add(cBoxTest[1]);
//		cBoxTest[1].setVisible(true);
		
	}

	public static void main(String args[]) {
		SensorMapsExample s = new SensorMapsExample();
		s.initialize();
		s.frame.setVisible(true);
		s.setWaitForData(btManager.callBackObject);		
		//s.setWaitForData(shimmer);
	}
	
	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		  int ind = shimmerMSG.mIdentifier;

		  Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject)object;
			
			if (callbackObject.mState == BT_STATE.CONNECTING) {
				textPaneStatus.setText("connecting...");
			} else if (callbackObject.mState == BT_STATE.CONNECTED) {
				textPaneStatus.setText("connected");
			} else if (callbackObject.mState == BT_STATE.DISCONNECTED
//					|| callbackObject.mState == BT_STATE.NONE
					|| callbackObject.mState == BT_STATE.CONNECTION_LOST){
				textPaneStatus.setText("disconnected");				
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
			CallbackObject callbackObject = (CallbackObject)object;
			int msg = callbackObject.mIndicator;
			if (msg== ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){
				textPaneStatus.setText("device fully initialized");
			}
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
				textPaneStatus.setText("device stopped streaming");
			} else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
				textPaneStatus.setText("device streaming");
			} else {}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {
			
		}
	
	
		
		
	}
}
