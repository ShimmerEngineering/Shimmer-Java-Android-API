package com.shimmerresearch.simpleexamples;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.guiUtilities.plot.BasicPlotManagerPC;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import info.monitorenter.gui.chart.Chart2D;

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
import java.awt.Canvas;

public class SensorMapsExample extends BasicProcessWithCallBack {
	
	private JFrame frame;
	private JTextField textField;
	JTextPane textPaneStatus;
	static ShimmerPC shimmer = new ShimmerPC("ShimmerDevice"); 
	static ShimmerDevice shimmerDevice;
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	BasicPlotManagerPC plotManager = new BasicPlotManagerPC();
	String btComport;
	
	/**
	 * Initialize the contents of the frame
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
		frame = new JFrame("Shimmer SensorMaps Example");
		frame.setBounds(100, 100, 662, 591);
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
				
				btComport = textField.getText();
				btManager.connectShimmerThroughCommPort(btComport);
				//textPaneStatus.setText("connecting...");
				//shimmer.connect(textField.getText(),"");
				
			}
		});
		btnConnect.setToolTipText("attempt connection to Shimmer device");
		btnConnect.setBounds(161, 90, 199, 31);
		frame.getContentPane().add(btnConnect);
		
		JButton btnDisconnect = new JButton("DISCONNECT");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				btManager.disconnectShimmer(shimmer);
				
			}
		});
		btnDisconnect.setToolTipText("disconnect from Shimmer device");
		btnDisconnect.setBounds(392, 90, 187, 31);
		frame.getContentPane().add(btnDisconnect);
		
		JLabel lblShimmerStatus = new JLabel("Shimmer Status");
		lblShimmerStatus.setBounds(0, 139, 144, 23);
		frame.getContentPane().add(lblShimmerStatus);
		
		textPaneStatus = new JTextPane();
		textPaneStatus.setBounds(0, 181, 144, 36);
		frame.getContentPane().add(textPaneStatus);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 638, 23);
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
					} else {
						JOptionPane.showMessageDialog(frame, "Cannot configure sensors!\nDevice is streaming or SDLogging", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "No device connected!", "Info", JOptionPane.WARNING_MESSAGE);
				}
				
//				EnableSensorsDialog sensorsDialog = new EnableSensorsDialog(shimmerDevice);
//				sensorsDialog.initialize();
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
		
		JMenuItem mntmSignalsToPlot = new JMenuItem("Signals to plot");
		mnTools.add(mntmSignalsToPlot);
		
		JPanel plotPanel = new JPanel();
		plotPanel.setBounds(10, 236, 611, 272);
		frame.getContentPane().add(plotPanel);
		
		final Chart2D mChart = new Chart2D();
		mChart.setSize(300, 300);
		plotPanel.add(mChart);
		
		JButton btnStartStreaming = new JButton("START STREAMING");
		btnStartStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				shimmer.startStreaming();
				plotManager.addChart(mChart);
				
			}
		});
		btnStartStreaming.setBounds(161, 181, 199, 31);
		frame.getContentPane().add(btnStartStreaming);
		
		JButton btnStopStreaming = new JButton("STOP STREAMING");
		btnStopStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				shimmer.stopStreaming();
				
			}
		});
		btnStopStreaming.setBounds(392, 181, 187, 31);
		frame.getContentPane().add(btnStopStreaming);
		
		plotManager.setTitle("Plot");
		
		
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
				shimmer = (ShimmerPC) btManager.getShimmerDeviceBtConnected(btComport);
//				shimmerDevice = btManager.getShimmerDeviceBtConnected(btComport);
				//shimmer.startStreaming();
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
