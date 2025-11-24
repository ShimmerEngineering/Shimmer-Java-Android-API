package com.shimmerresearch.tools.matlab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.algorithms.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ShimmerJavaClass {
    private String comPort;
    private SensorDataReceived sdr = new SensorDataReceived();
    private String[] channelNames;
    private final ConcurrentLinkedQueue<String> connectedDevices = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, Queue<Object[]>> dataQueues = new ConcurrentHashMap<>();
    private List<float[]> collectedData = new ArrayList<>();

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private boolean mDebug = true;
    
//    private int rowIndex = 0;
    Object[] currentData = null;
    public static BasicShimmerBluetoothManagerPc mBluetoothManager;
	
    public ShimmerJavaClass() {
    	mBluetoothManager = new BasicShimmerBluetoothManagerPc(false);
        sdr.setWaitForData(mBluetoothManager.callBackObject);
        mBluetoothManager.addCallBack(sdr);
        if (mDebug) {
	        System.out.println("Callback Registered");
	        System.out.flush();
        }
    }
    
    public static void main(String[] args) {
        ShimmerJavaClass example = new ShimmerJavaClass();
        example.createAndShowGUI();
    }
    
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Shimmer Device Controller");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel();

        JTextField inputComPort = new JTextField("COM3");
        JButton connectButton = new JButton("Connect");
        JButton disconnectButton = new JButton("Disconnect");
        JButton startButton = new JButton("Start Streaming");
        JButton stopButton = new JButton("Stop Streaming");
        
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	comPort = inputComPort.getText();
            	mBluetoothManager.connectShimmerThroughCommPort(comPort);
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	mBluetoothManager.disconnectShimmer(comPort);
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                	mBluetoothManager.getShimmerDeviceBtConnected(comPort).startStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                
              while(true) {
            	if (hasDataToRead(comPort)) {
                    readData();
            	}
            	
            }
            }
        });

        
        
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	try {
            		mBluetoothManager.getShimmerDeviceBtConnected(comPort).stopStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        panel.add(inputComPort);
        panel.add(connectButton);
        panel.add(disconnectButton);
        panel.add(startButton);
        panel.add(stopButton);
        frame.add(panel);
        
        frame.setVisible(true);
    }

       public String[] retrieveSensorChannels() {
        LinkedHashMap<String, ChannelDetails> mapOfAllChannels = mBluetoothManager.getShimmerDeviceBtConnected(comPort).getMapOfAllChannelsForStoringToDB(COMMUNICATION_TYPE.BLUETOOTH, null, false, false);
        List<ChannelDetails> listOfChannelDetails = new ArrayList<ChannelDetails>(mapOfAllChannels.values());

        List<String> channelNamesList = new ArrayList<String>();
        for (ChannelDetails channel : listOfChannelDetails) {
            channelNamesList.add(channel.mObjectClusterName);
        }

        return channelNamesList.toArray(new String[0]);
    }
   public boolean hasDataToRead(String comPort) {
	    Queue<Object[]> queue = dataQueues.get(comPort);
	    return queue != null && !queue.isEmpty();
	}
   private void sendData(String comPort, Object[] data) {
	    dataQueues.computeIfAbsent(comPort, k -> new LinkedList<>()).add(data);
	}
    
   public void readData() {
	    for (String comPort : dataQueues.keySet()) {
	        Object[] data = receiveData(comPort);
	        if (data != null && mDebug) {
	            float[][] matrix = (float[][]) data[0];
	            String[] signalNames = (String[]) data[1];
	            String[] formats = (String[]) data[2];
	            String[] units = (String[]) data[3];

	            System.out.println("==== Data from device: " + comPort + " ====");
	            for (int i = 0; i < matrix.length; i++) {
	                System.out.print("Row " + i + ": ");
	                for (int j = 0; j < matrix[i].length; j++) {
	                    System.out.print(matrix[i][j] + " " + units[j] + " [" + signalNames[j] + "] | ");
	                }
	                System.out.println();
	            }
	        }
	    }
	}

    public Object[] receiveData(String comPort) {
        Queue<Object[]> queue = dataQueues.get(comPort);
        if (queue == null || queue.isEmpty()) return null;

        List<float[]> deviceData = new ArrayList<>();
        String[] signalNameArray = null, signalFormatArray = null, signalUnitArray = null;

        while (!queue.isEmpty()) {
            Object[] data = queue.poll();
            if (data == null) continue;  // skip this iteration safely
            
            float[] values = (float[]) data[1];
            if (values == null) continue;
            
            signalNameArray = (String[]) data[2];
            signalFormatArray = (String[]) data[3];
            signalUnitArray = (String[]) data[4];
            deviceData.add(values);
        }

        float[][] dataMatrix = deviceData.toArray(new float[0][0]);
        return new Object[] { dataMatrix, signalNameArray, signalFormatArray, signalUnitArray };
    }

    
    public class SensorDataReceived extends BasicProcessWithCallBack {

        @Override
        protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
            int ind = shimmerMSG.mIdentifier;
            Object object = shimmerMSG.mB;

            if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
                CallbackObject callbackObject = (CallbackObject) object;

                if (callbackObject.mState == BT_STATE.CONNECTING) {
                    System.out.println("Device State Change: " + callbackObject.mState);
                    System.out.flush();
                	
                } else if (callbackObject.mState == BT_STATE.CONNECTED) {
                    System.out.println("Device State Change: " + callbackObject.mState);
                    System.out.flush();
                    if (comPort == null) {
                    	comPort = callbackObject.mComPort;
                    }
                	channelNames = retrieveSensorChannels();
                	if (mDebug) {
	                	for(String channel : channelNames) {
	                		System.out.println(channel);
	                	}
                	}
                	System.out.println("Device State Change: CONNECTED");
                    pcs.firePropertyChange("ConnectedState", null, "CONNECTED");
                    
                } else if (callbackObject.mState == BT_STATE.DISCONNECTED || callbackObject.mState == BT_STATE.CONNECTION_LOST) {
                    System.out.println("Device State Change: " + callbackObject.mState);
                    System.out.flush();
                    System.out.println("Device State Change: DISCONNECTED");
                    pcs.firePropertyChange("ConnectedState", null, "DISCONNECTED");

                }
            } else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {

            } else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
                ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;

                float[] newData = new float[channelNames.length];
                String[] signalNameArray = new String[channelNames.length];
                String[] signalFormatArray = new String[channelNames.length];
                String[] signalUnitArray = new String[channelNames.length];
                
                for (int i = 0; i < channelNames.length; i++) {
                    FormatCluster formatCluster = objc.getFormatCluster(channelNames[i], "CAL");
                    if (formatCluster != null) {
                        newData[i] = (float) formatCluster.mData;
                        signalNameArray[i] = channelNames[i];
                        signalFormatArray[i] = formatCluster.mFormat;
                        signalUnitArray[i] = formatCluster.mUnits;
                    }
                }
                
                String comPort = objc.getComPort();
                Object[] outputData = { comPort, newData, signalNameArray, signalFormatArray, signalUnitArray };
                sendData(comPort, outputData);
            }
        }
    }
}