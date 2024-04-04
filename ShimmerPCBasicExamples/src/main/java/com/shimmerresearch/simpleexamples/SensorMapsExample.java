package com.shimmerresearch.simpleexamples;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.grpc.ShimmerGRPC;
import com.shimmerresearch.guiUtilities.configuration.EnableSensorsDialog;
import com.shimmerresearch.guiUtilities.configuration.SensorConfigDialog;
import com.shimmerresearch.guiUtilities.configuration.SignalsToPlotDialog;
import com.shimmerresearch.guiUtilities.plot.BasicPlotManagerPC;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import info.monitorenter.gui.chart.Chart2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;

public class SensorMapsExample extends BasicProcessWithCallBack {
	
	private JFrame frame;
	private JTextField textField;
	JTextPane textPaneStatus;
	static ShimmerDevice shimmerDevice;
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	BasicPlotManagerPC plotManager = new BasicPlotManagerPC();
	static String btComport;
	final Chart2D mChart = new Chart2D();
	private JTextField textFieldSamplingRate;
	static JLabel lblPRR;
	
	/**
	 * Initialize the contents of the frame
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
		frame = new JFrame("Shimmer SensorMaps Example");
		frame.setBounds(100, 100, 1000, 591);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSetComPort = new JLabel("Set COM Port (e.g. COMX) or Mac Address (e.g. XX:XX:XX:XX:XX:XX)");
		lblSetComPort.setBounds(10, 25, 611, 23);
		frame.getContentPane().add(lblSetComPort);
		
		textField = new JTextField();
		textField.setToolTipText("for example COM1, COM2, etc");
		textField.setBounds(10, 56, 144, 29);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnConnect = new JButton("CONNECT");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				btComport = textField.getText();
				if (btComport.length()==17) {
					BluetoothDeviceDetails bdd = new BluetoothDeviceDetails("", btComport, "ShimmerGRPC");
					btManager.connectShimmer3BleGrpc(bdd);
				} else {
					btManager.connectShimmerThroughCommPort(btComport);
				}
				
			}
		});
		btnConnect.setToolTipText("attempt connection to Shimmer device");
		btnConnect.setBounds(185, 55, 199, 31);
		frame.getContentPane().add(btnConnect);
		
		JButton btnDisconnect = new JButton("DISCONNECT");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				btManager.disconnectShimmer(btComport);
				
			}
		});
		btnDisconnect.setToolTipText("disconnect from Shimmer device");
		btnDisconnect.setBounds(415, 55, 187, 31);
		frame.getContentPane().add(btnDisconnect);
		
		JButton btnSetBlinkLED = new JButton("Set Blink LED (Random)");
		btnSetBlinkLED.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    try {
			    	Random random = new Random();
			    	int ledvalue = random.nextInt(3-0)+0;
			    	System.out.println("LED Value to Write: " + ledvalue);
			    	((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).writeLEDCommand(ledvalue);
			    	Thread.sleep(400);//make sure to wait for the cmd to be written.
			    	int currentledvalue = ((ShimmerPC)btManager.getShimmerDeviceBtConnected(btComport)).getCurrentLEDStatus();
			    	System.out.println("Current ED Value: " + currentledvalue);
			    } catch (Exception e) {
			   
			    }
			}
		});
		btnSetBlinkLED.setBounds(625, 55, 187, 31);
		frame.getContentPane().add(btnSetBlinkLED);
		
		
		JLabel lblShimmerStatus = new JLabel("Shimmer Status");
		lblShimmerStatus.setBounds(10, 139, 144, 23);
		frame.getContentPane().add(lblShimmerStatus);
		
		textPaneStatus = new JTextPane();
		textPaneStatus.setBounds(10, 181, 144, 36);
		frame.getContentPane().add(textPaneStatus);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 984, 23);
		frame.getContentPane().add(menuBar);
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenuItem mntmSelectSensors = new JMenuItem("Select sensors");
		mntmSelectSensors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//Ensure the Shimmer is not streaming or SD logging before configuring it
				if(((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).isConnected()) {
					if(!((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).isStreaming() && !((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).isSDLogging()) {
						EnableSensorsDialog sensorsDialog = new EnableSensorsDialog(((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)),btManager);
						sensorsDialog.showDialog();
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
		
		JMenuItem mntmDeviceConfiguration = new JMenuItem("Sensor configuration");
		mntmDeviceConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).isConnected()) {
					if(!((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).isStreaming() && !((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).isSDLogging()) {
						SensorConfigDialog configDialog = new SensorConfigDialog(((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)),btManager);
						configDialog.showDialog();
					} else {
						JOptionPane.showMessageDialog(frame, "Cannot configure sensors!\nDevice is streaming or SDLogging", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "No device connected!", "Info", JOptionPane.WARNING_MESSAGE);
				}
				
			}
		});
		mnTools.add(mntmDeviceConfiguration);
		
		JPanel plotPanel = new JPanel();
		plotPanel.setBounds(10, 250, 611, 272);
		frame.getContentPane().add(plotPanel);
		plotPanel.setLayout(null);
		

		mChart.setLocation(12, 13);
		mChart.setSize(587, 246);
		plotPanel.add(mChart);
		plotManager.addChart(mChart);
		
		JMenuItem mntmSignalsToPlot = new JMenuItem("Signals to plot");
		mntmSignalsToPlot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SignalsToPlotDialog signalsToPlotDialog = new SignalsToPlotDialog();
				signalsToPlotDialog.initialize(((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)), plotManager, mChart);
			}
		});
		mnTools.add(mntmSignalsToPlot);
		
		JButton btnStartStreaming = new JButton("START STREAMING");
		btnStartStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).startStreaming();
				} catch (ShimmerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		btnStartStreaming.setBounds(185, 181, 199, 31);
		frame.getContentPane().add(btnStartStreaming);
		
		JButton btnStopStreaming = new JButton("STOP STREAMING");
		btnStopStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).stopStreaming();
				
			}
		});
		btnStopStreaming.setBounds(415, 181, 187, 31);
		frame.getContentPane().add(btnStopStreaming);
		
		JButton btnTakeSnapshot = new JButton("Take Snapshot");
		btnTakeSnapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
							
			    try {

			        File outputfile = new File("saved.png");
			        ImageIO.write(mChart.snapShot(), "png", outputfile);
			    } catch (IOException e) {
			   
			    }
			}
		});
		btnTakeSnapshot.setBounds(625, 181, 187, 31);
		frame.getContentPane().add(btnTakeSnapshot);
		
		textFieldSamplingRate = new JTextField();
		textFieldSamplingRate.setToolTipText("for example COM1, COM2, etc");
		textFieldSamplingRate.setColumns(10);
		textFieldSamplingRate.setBounds(10, 99, 144, 29);
		frame.getContentPane().add(textFieldSamplingRate);
		
		JButton btnWriteSamplingRate = new JButton("Write Sampling Rate");
		btnWriteSamplingRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ShimmerDevice sd = ((ShimmerDevice)btManager.getShimmerDeviceBtConnected(btComport));
				ShimmerDevice cloned = sd.deepClone();
				cloned.setSamplingRateShimmer(Double.parseDouble(textFieldSamplingRate.getText()));
				AssembleShimmerConfig.generateSingleShimmerConfig(cloned, COMMUNICATION_TYPE.BLUETOOTH);
                btManager.configureShimmer(cloned);
			}
		});
		btnWriteSamplingRate.setToolTipText("attempt connection to Shimmer device");
		btnWriteSamplingRate.setBounds(185, 97, 199, 31);
		frame.getContentPane().add(btnWriteSamplingRate);
		
		lblPRR = new JLabel("Packet Reception Rate: ");
		lblPRR.setBounds(10, 228, 372, 14);
		frame.getContentPane().add(lblPRR);
		
		plotManager.setTitle("Plot");		
	}

	public static void main(String args[]) {
		SensorMapsExample s = new SensorMapsExample();
		s.initialize();
		s.frame.setVisible(true);
		s.setWaitForData(btManager.callBackObject);		
		//s.setWaitForData(shimmer);
	}
	
	static Timer timer = null;
    static class PRRTask extends TimerTask {
        @Override
        public void run() {
        	ShimmerDevice sd = ((ShimmerDevice)btManager.getShimmerDeviceBtConnected(btComport));
        	if (sd!=null) {
        		lblPRR.setText("Packet Reception Rate: " +Double.toString(sd.getPacketReceptionRateOverall()));
        	}
        }
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
				if (timer!=null){
                    timer.cancel();
                    timer = new Timer();
                }
				textPaneStatus.setText("connected");
				//shimmer = (ShimmerPC) btManager.getShimmerDeviceBtConnected(btComport);
//				shimmerDevice = btManager.getShimmerDeviceBtConnected(btComport);
				//shimmer.startStreaming();
			} else if (callbackObject.mState == BT_STATE.DISCONNECTED
//					|| callbackObject.mState == BT_STATE.NONE
					|| callbackObject.mState == BT_STATE.CONNECTION_LOST){
				if (timer!=null) {
                    timer.cancel();
                    timer = null;
                }
				textPaneStatus.setText("disconnected");				
			}else if (callbackObject.mState == BT_STATE.STREAMING) {
				if (timer!=null){
                    timer.cancel();
                    timer = new Timer();
                } else {
                    timer = new Timer();
                }
                // Schedule a task to be executed after a delay of 2 seconds
                timer.schedule(new PRRTask(), 0 , 2000);
			
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
			CallbackObject callbackObject = (CallbackObject)object;
			int msg = callbackObject.mIndicator;
			if (msg== ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){
				textPaneStatus.setText("device fully initialized");
				textFieldSamplingRate.setText(Double.toString(((ShimmerBluetooth)btManager.getShimmerDeviceBtConnected(btComport)).getSamplingRateShimmer()));
			}
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
				textPaneStatus.setText("device stopped streaming");
			} else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
				textPaneStatus.setText("device streaming");
			} else {}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
			ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
			
			try {
				plotManager.filterDataAndPlot(objc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {
			
		}
	
	
		
		
	}
}

