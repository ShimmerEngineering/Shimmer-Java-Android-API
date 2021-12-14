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
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Canvas;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.Font;

public class ConnectionTest extends BasicProcessWithCallBack {

	private JFrame frame;
	private JTextField textFieldStatus;
    private JTextField textFieldInterval;
    private JTextField textFieldSuccessCount;
    private JTextField textFieldFailureCount;
    private JTextField textFieldTotalIteration;
    private JTextField textFieldTestProgress;
    private JTextField textFieldFirmware;
    private JTextField textFieldRetryCountLimit;
    private JTextField textFieldRetryCount;
    private JTextField textFieldTotalRetries;
    private JTextField textFieldMacId;
    private JTextField textFieldBtFriendlyName;

    private int successCount = 0;
    private int failureCount = 0;
    private int totalIterationLimit = 10;
    private int currentIteration = 0;
    private int retryCount = 0;
    private int retryCountLimit = 5;
    private int durationBetweenTest = 1;
    private int totalRetries = 0;

    private String macAddOrBtComport = "D0:2B:46:3D:A2:BB";
    //private String macAddOrBtComport = "Com5";
    private String btFriendlyName = "Verisense-19092501A2BB";
    //private String btFriendlyName = "Shimmer3-E6C8";
    private boolean isTestStarted = false;
    private Timer timer;
    HashMap<Integer,Integer> ResultMap = new HashMap<>(); //-1,0,1 , unknown, fail, pass
    
	static ShimmerDevice shimmerDevice;
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	
	private void addLabel(String text, int x, int y, int width, int height) {
		JLabel lbl = new JLabel(text);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setBounds(x, y, width, height);
		frame.getContentPane().add(lbl);
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
		frame = new JFrame("Connection Test");
		frame.setBounds(100, 100, 600, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		addLabel("Set COM Port or Mac Id", 10, 30, 154, 23);
		textFieldMacId = addTextField("for example COM1, COM2, d0:2b:46:3d:a2:bb, etc", 10, 60, 154, 29, 10);
		addLabel("Set Friendly Name", 210, 30, 154, 23);
		textFieldBtFriendlyName = addTextField("for example Verisense-19092501A2BB, Shimmer3-1E59, etc", 210, 60, 154, 29, 10);
		addLabel("Test Iterations", 10, 100, 154, 23);
		textFieldTotalIteration = addTextField("", 10, 130, 154, 29, 10);
		addLabel("Attempt Intervals(s)", 210, 100, 154, 23);
		textFieldInterval = addTextField("", 210, 130, 154, 29, 10);
		addLabel("Retry Count Limit", 410, 100, 154, 23);
		textFieldRetryCountLimit = addTextField("", 410, 130, 154, 29, 10);
		addLabel("Status", 10, 170, 154, 23);
		textFieldStatus = addTextField("", 10, 200, 154, 29, 10);
		addLabel("Retry Count", 210, 170, 154, 23);
		textFieldRetryCount = addTextField("", 210, 200, 154, 29, 10);
		addLabel("Firmware", 410, 170, 154, 23);
		textFieldFirmware = addTextField("", 410, 200, 154, 29, 10);
		addLabel("Test Progress", 10, 240, 100, 23);
		textFieldTestProgress = addTextField("", 10, 270, 100, 29, 10);
		addLabel("Success", 140, 240, 100, 23);
		textFieldSuccessCount = addTextField("", 140, 270, 100, 29, 10);
		addLabel("Fail", 270, 240, 100, 23);
		textFieldFailureCount = addTextField("", 270, 270, 100, 29, 10);
		addLabel("Total Retries", 400, 240, 100, 23);
		textFieldTotalRetries = addTextField("", 400, 270, 100, 29, 10);
		
		textFieldStatus.setEnabled(false);
		textFieldRetryCount.setEnabled(false);
		textFieldFirmware.setEnabled(false);
		textFieldTestProgress.setEnabled(false);
		textFieldSuccessCount.setEnabled(false);
		textFieldFailureCount.setEnabled(false);
		textFieldTotalRetries.setEnabled(false);

		textFieldBtFriendlyName.setText(btFriendlyName);
		textFieldMacId.setText(macAddOrBtComport);
		textFieldRetryCountLimit.setText(Integer.toString(retryCountLimit));
        textFieldTotalIteration.setText(Integer.toString(totalIterationLimit));
        textFieldInterval.setText(Integer.toString(durationBetweenTest));
		
		JButton startTest = new JButton("START TEST");
		startTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!isTestStarted){
		            totalRetries = 0;
		            
		            if (btManager==null) {
	                    btManager = new BasicShimmerBluetoothManagerPc();
	                    btManager.setPathToVeriBLEApp("bleconsoleapp\\BLEConsoleApp1.exe");
		            }
		            
		            if(btManager.getShimmerDeviceBtConnected(macAddOrBtComport) != null)
		            {
		            	btManager.disconnectShimmer(macAddOrBtComport);
		            }
		            
	                totalIterationLimit = Integer.parseInt(textFieldTotalIteration.getText());
	                retryCountLimit = Integer.parseInt(textFieldRetryCountLimit.getText());
	                currentIteration = 0;
	                successCount = 0;
	                failureCount = 0;
	                textFieldFailureCount.setText(String.valueOf(failureCount));
	                textFieldSuccessCount.setText(String.valueOf(successCount));
	                textFieldTotalRetries.setText(String.valueOf(totalRetries));
	                textFieldTestProgress.setText("0 of" + String.valueOf(totalIterationLimit));
	                textFieldInterval.setEnabled(false);
	                textFieldTotalIteration.setEnabled(false);
	                textFieldRetryCountLimit.setEnabled(false);
	                isTestStarted = true;

	                timer = new Timer();
	                timer.schedule(new ConnectTask(),0);
		        }
			}
		});
		startTest.setBounds(175, 350, 150, 31);
		frame.getContentPane().add(startTest);
		
		JButton stopTest = new JButton("STOP TEST");
		stopTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isTestStarted){
		            textFieldInterval.setEnabled(true);
		            textFieldTotalIteration.setEnabled(true);
		            textFieldRetryCountLimit.setEnabled(true);
		            if(timer != null){
		                timer.cancel();
		            }
		            isTestStarted = false;
		        }
			}
		});
		stopTest.setBounds(175, 390, 150, 31);
		frame.getContentPane().add(stopTest);
	}

	public static void main(String args[]) {
		ConnectionTest s = new ConnectionTest();

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
		int ind = shimmerMSG.mIdentifier;

		Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject) object;

			if (callbackObject.mState == BT_STATE.CONNECTING) {
				textFieldStatus.setText("CONNECTING");
			} else if (callbackObject.mState == BT_STATE.CONNECTED) {
				textFieldStatus.setText("CONNECTED");
                ResultMap.put(currentIteration,1);
				
				if (btFriendlyName.contains("Verisense")) {
					shimmerDevice = btManager.getShimmerDeviceBtConnected(macAddOrBtComport.toUpperCase());
					successCount += 1;
	                ResultMap.put(currentIteration,1);
	                textFieldSuccessCount.setText(String.valueOf(successCount));
                	btManager.disconnectShimmer(shimmerDevice);
                	try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
		            if (isTestStarted) {
		                timer = new Timer();
		                timer.schedule(new ConnectTask(), Integer.parseInt(textFieldInterval.getText().toString()) * 1000);
		            }
				}
			} else if (callbackObject.mState == BT_STATE.DISCONNECTED
					|| callbackObject.mState == BT_STATE.CONNECTION_LOST) {
				textFieldStatus.setText("DISCONNECTED");
                if (ResultMap.get(currentIteration)==-1){
                    if (retryCount<retryCountLimit) {
                        retryCount++;
                        totalRetries++;
                        textFieldRetryCount.setText(Integer.toString(retryCount));
                        textFieldTotalRetries.setText(Integer.toString(totalRetries));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //shimmer = null;
                        //shimmer = new Shimmer(mHandler);
                        //shimmer.connect(macAdd, "default");
                        //btManager.removeShimmerDeviceBtConnected(macAddOrBtComport);
                        
                        if (btFriendlyName.contains("Shimmer")) {
    						btManager.connectShimmerThroughCommPort(macAddOrBtComport);
    					} else if (btFriendlyName.contains("Verisense")) {
    						BluetoothDeviceDetails devDetails = new BluetoothDeviceDetails("", macAddOrBtComport, "Verisense");
    						btManager.connectShimmerThroughBTAddress(devDetails);
    					}
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {

                        if (ResultMap.get(currentIteration)==-1) {
                            ResultMap.put(currentIteration, 0);

                            timer.cancel();
                            failureCount += 1;
                            textFieldFailureCount.setText(String.valueOf(failureCount));
                        }
                        if (isTestStarted && currentIteration< totalIterationLimit) {

                            timer = new Timer();
                            timer.schedule(new ConnectTask(), Integer.parseInt(textFieldInterval.getText().toString()) * 1000);
                        }
                    }
                }
			} else if (callbackObject.mState == BT_STATE.STREAMING) {
			}else if (callbackObject.mState == BT_STATE.STREAMING_LOGGED_DATA) {
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
			CallbackObject callbackObject = (CallbackObject) object;
			int msg = callbackObject.mIndicator;
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED) {
				if (btFriendlyName.contains("Verisense")) {
					shimmerDevice = btManager.getShimmerDeviceBtConnected(macAddOrBtComport.toUpperCase());
				} else {
					//textFieldShimmerDeviceName.setText(((ObjectCluster) msg.obj).getShimmerName());
					shimmerDevice = btManager.getShimmerDeviceBtConnected(macAddOrBtComport);
				}
				
				successCount += 1;
                ResultMap.put(currentIteration,1);
                textFieldSuccessCount.setText(String.valueOf(successCount));
                btManager.disconnectShimmer(shimmerDevice);
                if(shimmerDevice != null) {
                	btManager.removeShimmerDeviceBtConnected(shimmerDevice.getBtConnectionHandle());
	            }
	            if (isTestStarted) {
	                timer = new Timer();
	                timer.schedule(new ConnectTask(), Integer.parseInt(textFieldInterval.getText().toString()) * 1000);
	            }
			}
			if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
			} else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
			} else {
			}
		} else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {


		} else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {

		} else if (ind == ShimmerBluetooth.MSG_IDENTIFIER_SYNC_PROGRESS) {
		}
	}
	
	public class ConnectTask extends  TimerTask{
        @Override
        public void run() {
            if (isTestStarted) {
                retryCount = 0;
                textFieldRetryCount.setText(Integer.toString(retryCount));

                if (currentIteration >= totalIterationLimit) {
                	textFieldInterval.setEnabled(true);
                	textFieldTotalIteration.setEnabled(true);
                    timer.cancel();
                    isTestStarted = false;
                    return;
                } else {
                    currentIteration += 1;
                    textFieldTestProgress.setText(String.valueOf(currentIteration) + " of " + String.valueOf(totalIterationLimit));

                    ResultMap.put(currentIteration,-1);
                    //shimmer = null;
                    //shimmer = new Shimmer(mHandler);
                    //shimmer.connect(macAdd, "default");
//                    if(shimmerDevice != null) {
//                    	btManager.removeShimmerDeviceBtConnected(shimmerDevice.getBtConnectionHandle());
//                    }
                    
                    if (btFriendlyName.contains("Shimmer")) {
						btManager.connectShimmerThroughCommPort(macAddOrBtComport);
					} else if (btFriendlyName.contains("Verisense")) {
						BluetoothDeviceDetails devDetails = new BluetoothDeviceDetails("", macAddOrBtComport, "Verisense");
						btManager.connectShimmerThroughBTAddress(devDetails);
					}
                }

            }
        }
    }
}
