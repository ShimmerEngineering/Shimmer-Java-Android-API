package com.shimmerresearch.driver.ble;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.guiUtilities.configuration.EnableSensorsDialog;
import com.shimmerresearch.guiUtilities.configuration.SensorConfigDialog;
import com.shimmerresearch.guiUtilities.configuration.SignalsToPlotDialog;
import com.shimmerresearch.guiUtilities.plot.BasicPlotManagerPC;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.SyncProgressDetails;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Canvas;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.Font;

public class SensorMapsExample2 extends BasicProcessWithCallBack {

	private JFrame frame;
	private JTextField[] macAddressTextField = new JTextField[2];
	private JTextField[] btFriendlyNameTextField = new JTextField[2];
	private JTextField[] ParticipantNameTextField = new JTextField[2];
	private JTextField[] TrialNameTextField = new JTextField[2];
	private JTextArea[] lblBinFileDirectory = new JTextArea[2];
	private JLabel[] lblPayloadIndex = new JLabel[2];
	private JLabel[] lblObjCluster = new JLabel[2];
	JTextPane[] textPaneStatus = new JTextPane[2];
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	String btComport;
	String btFriendlyName;
	
	String[] macAddress = new String[2];
	static ShimmerDevice[] shimmerDevice = new ShimmerDevice[2];

	/**
	 * Initialize the contents of the frame
	 * 
	 * @wbp.parser.entryPoint
	 */
	private JLabel addLabel(String text, int x, int y, int width, int height) {
		JLabel lbl = new JLabel(text);
		lbl.setBounds(x, y, width, height);
		frame.getContentPane().add(lbl);
		
		return lbl;
	}
	
	private JTextField addTextField(String tooltip, int x, int y, int width, int height, int column) {
		JTextField textField = new JTextField();
		textField.setToolTipText(tooltip);
		textField.setBounds(x, y, width, height);
		textField.setColumns(column);
		frame.getContentPane().add(textField);
		
		return textField;
	}
	
	public void initialize() {
		frame = new JFrame("Shimmer SensorMaps Example");
		frame.setBounds(100, 100, 868, 596);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		addLabel("Set Friendly Name", 10, 30, 154, 23);

		btFriendlyNameTextField[0] = addTextField("for example Verisense-19092501A2BB, Shimmer3-1E59, etc", 10, 51, 154, 29, 10);
		
		addLabel("Set COM Port or Mac Id", 10, 85, 154, 23);

		macAddressTextField[0] = addTextField("for example COM1, COM2, d0:2b:46:3d:a2:bb, etc", 10, 106, 154, 29, 10);

		addLabel("Shimmer Status", 10, 145, 154, 23);

		textPaneStatus[0] = new JTextPane();
		textPaneStatus[0].setBounds(10, 166, 154, 35);
		frame.getContentPane().add(textPaneStatus[0]);
		
		addLabel("Participant Name", 415, 30, 175, 23);
		
		ParticipantNameTextField[0] = addTextField("enter your participant name", 415, 51, 175, 29, 10);

		addLabel("Trial Name", 415, 85, 175, 23);

		TrialNameTextField[0] = addTextField("enter your trial name", 415, 106, 175, 29, 10);
		
		lblPayloadIndex[0] = addLabel("Current payload index : ", 415, 166, 408, 35);
		lblPayloadIndex[0].setVerticalAlignment(SwingConstants.TOP);
		
		lblBinFileDirectory[0] = new JTextArea("");
		lblBinFileDirectory[0].setFont(lblPayloadIndex[0].getFont());
		lblBinFileDirectory[0].setBounds(415, 203, 408, 61);
		frame.getContentPane().add(lblBinFileDirectory[0]);
		lblBinFileDirectory[0].setEditable(false);
		lblBinFileDirectory[0].setOpaque(false);    
		lblBinFileDirectory[0].setWrapStyleWord(true);
		lblBinFileDirectory[0].setLineWrap(true);
		lblBinFileDirectory[0].setText("Bin file path :");
		
		lblObjCluster[0] = addLabel("Object Cluster", 415, 135, 175, 23);
		
		JButton btnConnect = new JButton("CONNECT");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btFriendlyName = btFriendlyNameTextField[0].getText();
				if (btFriendlyName.contains("Shimmer")) {
					btComport = macAddressTextField[0].getText();
					btManager.connectShimmerThroughCommPort(btComport);
				} else if (btFriendlyName.contains("Verisense")) {
					macAddress[0] = macAddressTextField[0].getText().toUpperCase();
					btManager.setPathToVeriBLEApp("bleconsoleapp\\BLEConsoleApp1.exe");
					BluetoothDeviceDetails devDetails = new BluetoothDeviceDetails("", macAddress[0], "Verisense");
					btManager.connectShimmerThroughBTAddress(devDetails);
				}
			}
		});
		btnConnect.setToolTipText("attempt connection to Shimmer device");
		btnConnect.setBounds(210, 35, 175, 31);
		frame.getContentPane().add(btnConnect);
		
		JButton btnDisconnect = new JButton("DISCONNECT");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btManager.disconnectShimmer(shimmerDevice[0]);
			}
		});
		btnDisconnect.setToolTipText("disconnect from Shimmer device");
		btnDisconnect.setBounds(210, 76, 175, 31);
		frame.getContentPane().add(btnDisconnect);

		JButton btnStartStreaming = new JButton("START STREAMING");
		btnStartStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					shimmerDevice[0].startStreaming();
				} catch (ShimmerException e) {
					e.printStackTrace();
					textPaneStatus[0].setText(e.getMessage());
				}
			}
		});
		btnStartStreaming.setBounds(210, 117, 175, 31);
		frame.getContentPane().add(btnStartStreaming);

		JButton btnStopStreaming = new JButton("STOP STREAMING");
		btnStopStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					shimmerDevice[0].stopStreaming();
				} catch (ShimmerException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnStopStreaming.setBounds(210, 158, 175, 31);
		frame.getContentPane().add(btnStopStreaming);
		
		JButton btnSync = new JButton("DATA SYNC");
		btnSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice[0];
				verisenseDevice.setTrialName(TrialNameTextField[0].getText());
				verisenseDevice.setParticipantID(ParticipantNameTextField[0].getText());
				try {
					verisenseDevice.readLoggedData();
				} catch (ShimmerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnSync.setToolTipText("Data Sync");
		btnSync.setBounds(210, 199, 175, 31);
		frame.getContentPane().add(btnSync);
		
		//sensor2
		
		addLabel("Set Friendly Name", 10, 250, 154, 23);

		btFriendlyNameTextField[1] = addTextField("for example Verisense-19092501A2BB, Shimmer3-1E59, etc", 10, 271, 154, 29, 10);
		
		addLabel("Set COM Port or Mac Id", 10, 305, 154, 23);

		macAddressTextField[1] = addTextField("for example COM1, COM2, d0:2b:46:3d:a2:bb, etc", 10, 326, 154, 29, 10);

		addLabel("Shimmer Status", 10, 365, 154, 23);

		textPaneStatus[1] = new JTextPane();
		textPaneStatus[1].setBounds(10, 386, 154, 35);
		frame.getContentPane().add(textPaneStatus[1]);
		
		addLabel("Participant Name", 415, 250, 175, 23);
		
		ParticipantNameTextField[1] = new JTextField();
		ParticipantNameTextField[1].setToolTipText("enter your participant name");
		ParticipantNameTextField[1].setBounds(415, 271, 175, 29);
		frame.getContentPane().add(ParticipantNameTextField[1]);
		ParticipantNameTextField[1].setColumns(10);
		
		addLabel("Trial Name", 415, 305, 175, 23);

		TrialNameTextField[1] = new JTextField();
		TrialNameTextField[1].setToolTipText("enter the trial name");
		TrialNameTextField[1].setBounds(415, 326, 175, 29);
		frame.getContentPane().add(TrialNameTextField[1]);
		TrialNameTextField[1].setColumns(10);

		lblPayloadIndex[1] = new JLabel("Current payload index : ");
		lblPayloadIndex[1].setVerticalAlignment(SwingConstants.TOP);
		lblPayloadIndex[1].setBounds(415, 386, 408, 35);
		frame.getContentPane().add(lblPayloadIndex[1]);
		
		lblBinFileDirectory[1] = new JTextArea("");
		lblBinFileDirectory[1].setFont(lblPayloadIndex[1].getFont());
		lblBinFileDirectory[1].setBounds(415, 423, 408, 61);
		frame.getContentPane().add(lblBinFileDirectory[1]);
		lblBinFileDirectory[1].setEditable(false);
		lblBinFileDirectory[1].setOpaque(false);    
		lblBinFileDirectory[1].setWrapStyleWord(true);
		lblBinFileDirectory[1].setLineWrap(true);
		lblBinFileDirectory[1].setText("Bin file path :");
		
		lblObjCluster[1] = addLabel("Object Cluster", 415, 355, 175, 23);

		JButton btnConnect2 = new JButton("CONNECT");
		btnConnect2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btFriendlyName = btFriendlyNameTextField[1].getText();
				if (btFriendlyName.contains("Shimmer")) {
					btComport = macAddressTextField[1].getText();
					btManager.connectShimmerThroughCommPort(btComport);
				} else if (btFriendlyName.contains("Verisense")) {
					macAddress[1] = macAddressTextField[1].getText().toUpperCase();
					btManager.setPathToVeriBLEApp("bleconsoleapp\\BLEConsoleApp1.exe");
					BluetoothDeviceDetails devDetails = new BluetoothDeviceDetails("", macAddress[1], "Verisense");
					btManager.connectShimmerThroughBTAddress(devDetails);
				}
			}
		});
		
		btnConnect2.setToolTipText("attempt connection to Shimmer device");
		btnConnect2.setBounds(210, 255, 175, 31);
		frame.getContentPane().add(btnConnect2);

		JButton btnDisconnect2 = new JButton("DISCONNECT");
		btnDisconnect2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btManager.disconnectShimmer(shimmerDevice[1]);
			}
		});
		btnDisconnect2.setToolTipText("disconnect from Shimmer device");
		btnDisconnect2.setBounds(210, 296, 175, 31);
		frame.getContentPane().add(btnDisconnect2);

		JButton btnStartStreaming2 = new JButton("START STREAMING");
		btnStartStreaming2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					shimmerDevice[1].startStreaming();
				} catch (ShimmerException e) {
					e.printStackTrace();
					textPaneStatus[1].setText(e.getMessage());
				}
			}
		});
		btnStartStreaming2.setBounds(210, 337, 175, 31);
		frame.getContentPane().add(btnStartStreaming2);

		JButton btnStopStreaming2 = new JButton("STOP STREAMING");
		btnStopStreaming2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					shimmerDevice[1].stopStreaming();
				} catch (ShimmerException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnStopStreaming2.setBounds(210, 378, 175, 31);
		frame.getContentPane().add(btnStopStreaming2);
		
		JButton btnSync2 = new JButton("DATA SYNC");
		btnSync2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice[1];
				verisenseDevice.setTrialName(TrialNameTextField[1].getText());
				verisenseDevice.setParticipantID(ParticipantNameTextField[1].getText());
				try {
					verisenseDevice.readLoggedData();
				} catch (ShimmerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnSync2.setToolTipText("Data Sync");
		btnSync2.setBounds(210, 419, 175, 31);
		frame.getContentPane().add(btnSync2);
		
		macAddressTextField[1].setText("d0:2b:46:3d:a2:bb");
		//macAddressTextField[1].setText("e7:ec:37:a0:d2:34");
		btFriendlyNameTextField[1].setText("Verisense-19092501A2BB");
		
		//macAddressTextField[0].setText("d0:2b:46:3d:a2:bb");
		macAddressTextField[0].setText("e7:ec:37:a0:d2:34");
		btFriendlyNameTextField[0].setText("Verisense-19092501D234");
	}

	public static void main(String args[]) {
		// shimmer
		SensorMapsExample2 s = new SensorMapsExample2();

		s.initialize();
		s.frame.setVisible(true);
		s.setWaitForData(btManager.callBackObject);
		s.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		s.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (shimmerDevice[0] instanceof VerisenseDevice) {
					((VerisenseDevice) shimmerDevice[0]).stopCommunicationProcess(COMMUNICATION_TYPE.BLUETOOTH);
				}
				
				if (shimmerDevice[1] instanceof VerisenseDevice) {
					((VerisenseDevice) shimmerDevice[1]).stopCommunicationProcess(COMMUNICATION_TYPE.BLUETOOTH);
				}
			}
		});
		// s.setWaitForData(shimmer);
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		
		int ind = shimmerMSG.mIdentifier;
		//callbackObject.mBluetoothAddress (all upper case)
		Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject) object;
			int index = Arrays.asList(macAddress).indexOf(callbackObject.mBluetoothAddress);

			if (callbackObject.mState == BT_STATE.CONNECTING) {
				textPaneStatus[index].setText("connecting...");
			} else if (callbackObject.mState == BT_STATE.CONNECTED) {
				textPaneStatus[index].setText("connected");

				if (btFriendlyName.contains("Verisense")) {
					shimmerDevice[index] = (btManager.getShimmerDeviceBtConnected(macAddress[index]));
				} else {
					shimmerDevice[index] = (btManager.getShimmerDeviceBtConnected(btComport));
				}

				// shimmer.startStreaming();
			} else if (callbackObject.mState == BT_STATE.DISCONNECTED
//					|| callbackObject.mState == BT_STATE.NONE
					|| callbackObject.mState == BT_STATE.CONNECTION_LOST) {
				textPaneStatus[index].setText("disconnected");
			} else if (callbackObject.mState == BT_STATE.STREAMING) {
				textPaneStatus[index].setText("device streaming");
			}else if (callbackObject.mState == BT_STATE.STREAMING_LOGGED_DATA) {
				textPaneStatus[index].setText("synchronizing data");
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
			CallbackObject callbackObject = (CallbackObject) object;
			int index = Arrays.asList(macAddress).indexOf(callbackObject.mBluetoothAddress);
			int msg = callbackObject.mIndicator;
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED) {
				textPaneStatus[index].setText("device fully initialized");
			}
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
				textPaneStatus[index].setText("device stopped streaming");
			} else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
				textPaneStatus[index].setText("device streaming");
			} else {
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
			ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
			int index = Arrays.asList(macAddress).indexOf(objc.getMacAddress());

			try {
				lblObjCluster[index].setText("Object Cluster : " + objc.getFormatClusterValue(objc.getChannelNamesByInsertionOrder().get(0), CHANNEL_TYPE.UNCAL.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {

		} else if (ind == ShimmerBluetooth.MSG_IDENTIFIER_SYNC_PROGRESS) {
			CallbackObject callbackObject = (CallbackObject) object;
			int index = Arrays.asList(macAddress).indexOf(callbackObject.mBluetoothAddress);
			lblPayloadIndex[index].setText("Current Payload Index : " + ((SyncProgressDetails)callbackObject.mMyObject).mPayloadIndex + " ; Speed(KBps) : " + ((SyncProgressDetails)callbackObject.mMyObject).mTransferRateBytes/1000 );
			String path = Paths.get(((SyncProgressDetails)callbackObject.mMyObject).mBinFilePath).toAbsolutePath().toString();
			lblBinFileDirectory[index].setText("Bin file path : " + path);
		}
	}
}
