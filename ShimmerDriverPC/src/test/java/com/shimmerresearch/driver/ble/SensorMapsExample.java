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
import com.shimmerresearch.verisense.communication.VerisenseMessage;

import bolts.Continuation;
import bolts.Task;
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
import java.util.List;
import java.awt.Canvas;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.Font;

public class SensorMapsExample extends BasicProcessWithCallBack {

	private JFrame frame;
	private JTextField textField;
	private JTextField btFriendlyNameTextField;
	private JTextField ParticipantNameTextField;
	private JTextField TrialNameTextField;
	private JLabel lblParticipantName;
	private JLabel lblTrialName;
	private JTextArea lblBinFileDirectory;
	private JButton btnSync;
	private JButton btnEraseData;
	private JButton btnDisableLogging;
	private JLabel lblPayloadIndex;
	JTextPane textPaneStatus;
	static ShimmerDevice shimmerDevice;
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	BasicPlotManagerPC plotManager = new BasicPlotManagerPC();
	String btComport;
	String macAddress;
	String btFriendlyName;
	String previousStatus;

	/**
	 * Initialize the contents of the frame
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
		frame = new JFrame("Shimmer SensorMaps Example");
		frame.setBounds(100, 100, 868, 596);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblSetComPort = new JLabel("Set COM Port or Mac Id");
		lblSetComPort.setBounds(10, 85, 154, 23);
		frame.getContentPane().add(lblSetComPort);

		textField = new JTextField();
		textField.setToolTipText("for example COM1, COM2, d0:2b:46:3d:a2:bb, etc");
		textField.setBounds(10, 106, 154, 29);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		JLabel lblSetFriendlyName = new JLabel("Set Friendly Name");
		lblSetFriendlyName.setBounds(10, 30, 154, 23);
		frame.getContentPane().add(lblSetFriendlyName);

		btFriendlyNameTextField = new JTextField();
		btFriendlyNameTextField.setToolTipText("for example Verisense-19092501A2BB, Shimmer3-1E59, etc");
		btFriendlyNameTextField.setBounds(10, 51, 154, 29);
		frame.getContentPane().add(btFriendlyNameTextField);
		btFriendlyNameTextField.setColumns(10);

		textField.setText("e7:45:2c:6d:6f:14");
//		textField.setText("d0:2b:46:3d:a2:bb");
//		textField.setText("e7:ec:37:a0:d2:34");
//		textField.setText("Com5");
//		btFriendlyNameTextField.setText("Shimmer-E6C8");
		btFriendlyNameTextField.setText("Verisense-19092501A2BB");

		JButton btnConnect = new JButton("CONNECT");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btFriendlyName = btFriendlyNameTextField.getText();
				if (btFriendlyName.contains("Shimmer")) {
					btComport = textField.getText();
					btManager.connectShimmerThroughCommPort(btComport);
				} else if (btFriendlyName.contains("Verisense")) {
					macAddress = textField.getText();
					btManager.setPathToVeriBLEApp("bleconsoleapp\\BLEConsoleApp1.exe");
					BluetoothDeviceDetails devDetails = new BluetoothDeviceDetails("", macAddress, "Verisense");
					btManager.connectShimmerThroughBTAddress(devDetails);
				}
			}
		});
		btnConnect.setToolTipText("attempt connection to Shimmer device");
		btnConnect.setBounds(210, 35, 175, 25);
		frame.getContentPane().add(btnConnect);

		lblParticipantName = new JLabel("Participant Name");
		lblParticipantName.setBounds(415, 30, 175, 23);
		frame.getContentPane().add(lblParticipantName);

		ParticipantNameTextField = new JTextField();
		ParticipantNameTextField.setToolTipText("enter your participant name");
		ParticipantNameTextField.setText("ExampleParticipant");
		ParticipantNameTextField.setBounds(415, 51, 175, 29);
		frame.getContentPane().add(ParticipantNameTextField);
		ParticipantNameTextField.setColumns(10);

		lblTrialName = new JLabel("Trial Name");
		lblTrialName.setBounds(415, 85, 175, 23);
		frame.getContentPane().add(lblTrialName);

		TrialNameTextField = new JTextField();
		TrialNameTextField.setToolTipText("enter the trial name");
		TrialNameTextField.setText("ExampleTrial");
		TrialNameTextField.setBounds(415, 106, 175, 29);
		frame.getContentPane().add(TrialNameTextField);
		TrialNameTextField.setColumns(10);

		lblPayloadIndex = new JLabel("Current payload index : ");
		lblPayloadIndex.setVerticalAlignment(SwingConstants.TOP);
		lblPayloadIndex.setBounds(415, 166, 408, 35);
		frame.getContentPane().add(lblPayloadIndex);
		
		lblBinFileDirectory = new JTextArea("");
		lblBinFileDirectory.setFont(lblPayloadIndex.getFont());
		lblBinFileDirectory.setBounds(415, 203, 408, 61);
		frame.getContentPane().add(lblBinFileDirectory);
		lblBinFileDirectory.setEditable(false);
		lblBinFileDirectory.setOpaque(false);    
		lblBinFileDirectory.setWrapStyleWord(true);
		lblBinFileDirectory.setLineWrap(true);
		lblBinFileDirectory.setText("Bin file path :");

		btnSync = new JButton("DATA SYNC");
		btnSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				previousStatus = textPaneStatus.getText();
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				verisenseDevice.setTrialName(TrialNameTextField.getText());
				verisenseDevice.setParticipantID(ParticipantNameTextField.getText());
				try {
					verisenseDevice.readLoggedData();
				} catch (ShimmerException e) {
					if(e.getMessage() == "A task is still ongoing") {
						textPaneStatus.setText(previousStatus);
						JOptionPane.showMessageDialog(frame,
								"Please wait until current task is finished", "Warning",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					e.printStackTrace();
				}
			}
		});
		btnSync.setToolTipText("Data Sync");
		btnSync.setBounds(210, 179, 175, 25);
		frame.getContentPane().add(btnSync);

		btnEraseData = new JButton("ERASE DATA");
		btnEraseData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					previousStatus = textPaneStatus.getText();
					textPaneStatus.setText("erasing data...");
					VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
					verisenseDevice.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).eraseDataTask().continueWith(new Continuation<VerisenseMessage, Void>() {
						@Override
						public Void then(Task<VerisenseMessage> completed) throws Exception {
							System.out.println("erased data completed");
							return null;
						}
					});
					//verisenseDevice.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).eraseData().waitForCompletion(60, TimeUnit.SECONDS);
				} catch (ShimmerException e) {
					if(e.getMessage() == "A task is still ongoing") {
						textPaneStatus.setText(previousStatus);
						JOptionPane.showMessageDialog(frame,
								"Please wait until current task is finished", "Warning",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					e.printStackTrace();
				}
			}
		});
		btnEraseData.setToolTipText("erase logged data");
		btnEraseData.setBounds(210, 215, 175, 25);
		frame.getContentPane().add(btnEraseData);
		
		btnDisableLogging = new JButton("DISABLE LOGGING");
		btnDisableLogging.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					previousStatus = textPaneStatus.getText();
					textPaneStatus.setText("writing op config...");
					VerisenseDevice verisenseDevice = ((VerisenseDevice)shimmerDevice).deepClone();
					verisenseDevice.setRecordingEnabled(!verisenseDevice.isRecordingEnabled());
					byte[] opConfig = verisenseDevice.configBytesGenerate(true, COMMUNICATION_TYPE.BLUETOOTH);
					((VerisenseDevice)shimmerDevice).getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).writeOperationalConfig(opConfig);
					btnDisableLogging.setText(verisenseDevice.isRecordingEnabled()?"DISABLE LOGGING":"ENABLE LOGGING");
				} catch (ShimmerException e) {
					if(e.getMessage() == "A task is still ongoing") {
						textPaneStatus.setText(previousStatus);
						JOptionPane.showMessageDialog(frame,
								"Please wait until current task is finished", "Warning",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					e.printStackTrace();
				}
			}
		});
		btnDisableLogging.setToolTipText("disable logging");
		btnDisableLogging.setBounds(210, 251, 175, 25);
		frame.getContentPane().add(btnDisableLogging);
		
		ParticipantNameTextField.setVisible(false);
		lblParticipantName.setVisible(false);
		TrialNameTextField.setVisible(false);
		lblTrialName.setVisible(false);
		btnSync.setVisible(false);
		btnEraseData.setVisible(false);
		btnDisableLogging.setVisible(false);
		lblPayloadIndex.setVisible(false);
		lblBinFileDirectory.setVisible(false);
		JButton btnDisconnect = new JButton("DISCONNECT");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btManager.disconnectShimmer(shimmerDevice);
			}
		});
		btnDisconnect.setToolTipText("disconnect from Shimmer device");
		btnDisconnect.setBounds(210, 71, 175, 25);
		frame.getContentPane().add(btnDisconnect);

		JLabel lblShimmerStatus = new JLabel("Shimmer Status");
		lblShimmerStatus.setBounds(10, 145, 154, 23);
		frame.getContentPane().add(lblShimmerStatus);

		textPaneStatus = new JTextPane();
		textPaneStatus.setBounds(10, 166, 154, 35);
		frame.getContentPane().add(textPaneStatus);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 638, 23);
		frame.getContentPane().add(menuBar);

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		JMenuItem mntmSelectSensors = new JMenuItem("Select sensors");
		mntmSelectSensors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				// Ensure the Shimmer is not streaming or SD logging before configuring it
				if (shimmerDevice.isConnected()) {
					if (!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
						EnableSensorsDialog sensorsDialog;
						sensorsDialog = new EnableSensorsDialog(shimmerDevice, btManager);
						sensorsDialog.showDialog();
					} else {
						JOptionPane.showMessageDialog(frame,
								"Cannot configure sensors!\nDevice is streaming or SDLogging", "Warning",
								JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "No device connected!", "Info", JOptionPane.WARNING_MESSAGE);
				}

//					EnableSensorsDialog sensorsDialog = new EnableSensorsDialog(shimmerDevice);
//					sensorsDialog.initialize();

			}
		});
		mnTools.add(mntmSelectSensors);

		JMenuItem mntmDeviceConfiguration = new JMenuItem("Sensor configuration");
		mntmDeviceConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (shimmerDevice.isConnected()) {
					if (!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
						SensorConfigDialog configDialog;
						configDialog = new SensorConfigDialog(shimmerDevice, btManager);
						configDialog.showDialog();
					} else {
						JOptionPane.showMessageDialog(frame,
								"Cannot configure sensors!\nDevice is streaming or SDLogging", "Warning",
								JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "No device connected!", "Info", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		mnTools.add(mntmDeviceConfiguration);

		JPanel plotPanel = new JPanel();
		plotPanel.setBounds(12, 274, 828, 272);
		frame.getContentPane().add(plotPanel);
		plotPanel.setLayout(null);

		final Chart2D mChart = new Chart2D();
		mChart.setLocation(12, 13);
		mChart.setSize(801, 246);
		plotPanel.add(mChart);
		plotManager.addChart(mChart);

		JMenuItem mntmSignalsToPlot = new JMenuItem("Signals to plot");
		mntmSignalsToPlot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SignalsToPlotDialog signalsToPlotDialog;
				if (btFriendlyName.contains("Verisense")) {
					signalsToPlotDialog = new SignalsToPlotDialog(true);
				} else {
					signalsToPlotDialog = new SignalsToPlotDialog();
				}

				signalsToPlotDialog.initialize(shimmerDevice, plotManager, mChart);
			}
		});

		mnTools.add(mntmSignalsToPlot);

		JButton btnStartStreaming = new JButton("START STREAMING");
		btnStartStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					shimmerDevice.startStreaming();
				} catch (ShimmerException e) {
					if(e.getMessage() == "A task is still ongoing") {
						JOptionPane.showMessageDialog(frame,
								"Please wait until current task is finished", "Warning",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					textPaneStatus.setText(e.getMessage());
					e.printStackTrace();
				}

			}
		});
		btnStartStreaming.setBounds(210, 107, 175, 25);
		frame.getContentPane().add(btnStartStreaming);

		JButton btnStopStreaming = new JButton("STOP STREAMING");
		btnStopStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					shimmerDevice.stopStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnStopStreaming.setBounds(210, 143, 175, 25);
		frame.getContentPane().add(btnStopStreaming);
		
		plotManager.setTitle("Plot");
	}

	public static void main(String args[]) {
		// shimmer
		SensorMapsExample s = new SensorMapsExample();

		s.initialize();
		s.frame.setVisible(true);
		s.setWaitForData(btManager.callBackObject);
		s.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		s.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (shimmerDevice instanceof VerisenseDevice) {
					((VerisenseDevice) shimmerDevice).stopCommunicationProcess(COMMUNICATION_TYPE.BLUETOOTH);
				}
			}
		});
		// s.setWaitForData(shimmer);
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		int ind = shimmerMSG.mIdentifier;

		Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject) object;

			if (callbackObject.mState == BT_STATE.CONNECTING) {
				textPaneStatus.setText("connecting...");
			} else if (callbackObject.mState == BT_STATE.CONNECTED) {
				textPaneStatus.setText("connected");

				if (btFriendlyName.contains("Verisense")) {
					ParticipantNameTextField.setVisible(true);
					lblParticipantName.setVisible(true);
					TrialNameTextField.setVisible(true);
					lblTrialName.setVisible(true);
					btnSync.setVisible(true);
					btnEraseData.setVisible(true);
					btnDisableLogging.setVisible(true);
					lblPayloadIndex.setVisible(true);
					lblBinFileDirectory.setVisible(true);
					shimmerDevice = btManager.getShimmerDeviceBtConnected(macAddress.toUpperCase());
					if(((VerisenseDevice)shimmerDevice).isRecordingEnabled()) {
						btnDisableLogging.setText("DISABLE LOGGING");
					}
					else {
						btnDisableLogging.setText("ENABLE LOGGING");
					}
				} else {
					shimmerDevice = btManager.getShimmerDeviceBtConnected(btComport);
				}

				// shimmer.startStreaming();
			} else if (callbackObject.mState == BT_STATE.DISCONNECTED
//					|| callbackObject.mState == BT_STATE.NONE
					|| callbackObject.mState == BT_STATE.CONNECTION_LOST) {
				textPaneStatus.setText("disconnected");

				ParticipantNameTextField.setVisible(false);
				lblParticipantName.setVisible(false);
				TrialNameTextField.setVisible(false);
				lblTrialName.setVisible(false);
				btnSync.setVisible(false);
				btnEraseData.setVisible(false);
				btnDisableLogging.setVisible(false);
				lblPayloadIndex.setVisible(false);
				lblBinFileDirectory.setVisible(false);
			} else if (callbackObject.mState == BT_STATE.STREAMING) {
				textPaneStatus.setText("device streaming");
			}else if (callbackObject.mState == BT_STATE.STREAMING_LOGGED_DATA) {
				textPaneStatus.setText("synchronizing data");
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
			CallbackObject callbackObject = (CallbackObject) object;
			int msg = callbackObject.mIndicator;
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED) {
				textPaneStatus.setText("device fully initialized");
			}
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
				textPaneStatus.setText("device stopped streaming");
			} else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
				textPaneStatus.setText("device streaming");
			} else {
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
			ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;

			try {
				plotManager.filterDataAndPlot(objc);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {

		} else if (ind == ShimmerBluetooth.MSG_IDENTIFIER_SYNC_PROGRESS) {
			CallbackObject callbackObject = (CallbackObject) object;
			lblPayloadIndex.setText("Current Payload Index : " + ((SyncProgressDetails)callbackObject.mMyObject).mPayloadIndex + " ; Speed(KBps) : " + ((SyncProgressDetails)callbackObject.mMyObject).mTransferRateBytes/1000 );
			String path = Paths.get(((SyncProgressDetails)callbackObject.mMyObject).mBinFilePath).toAbsolutePath().toString();
			lblBinFileDirectory.setText("Bin file path : " + path);
		} else if (ind == ShimmerBluetooth.MSG_IDENTIFIER_VERISENSE_ERASE_DATA_COMPLETED) {
			textPaneStatus.setText("connected");
		} else if (ind == ShimmerBluetooth.MSG_IDENTIFIER_VERISENSE_WRITE_OPCONFIG_COMPLETED) {
			textPaneStatus.setText("connected");
		}
	}
}
