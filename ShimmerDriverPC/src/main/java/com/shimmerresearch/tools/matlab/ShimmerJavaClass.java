package com.shimmerresearch.tools.matlab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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

public class ShimmerJavaClass {
    private String comPort;
    private SensorDataReceived sdr = new SensorDataReceived();
    private String[] channelNames;
    private Queue<Object[]> dataQueue = new LinkedList<>();
    private List<float[]> collectedData = new ArrayList<>();
    private String[] signalNameArray;
    private String[] signalFormatArray;
    private String[] signalUnitArray;
//    private int rowIndex = 0;
    Object[] currentData = null;
    public static BasicShimmerBluetoothManagerPc mBluetoothManager = new BasicShimmerBluetoothManagerPc();
	
    public ShimmerJavaClass() {
        sdr.setWaitForData(mBluetoothManager.callBackObject);
        mBluetoothManager.addCallBack(sdr);
        System.out.println("Callback Registered");
        System.out.flush();
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
            	if(!dataQueue.isEmpty()) {
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

   public Object[] receiveData() {
       while (!dataQueue.isEmpty()) {
           Object[] currentData = dataQueue.poll();

           float[] newData = (float[]) currentData[0];
           signalNameArray = (String[]) currentData[1];
           signalFormatArray = (String[]) currentData[2];
           signalUnitArray = (String[]) currentData[3];

           collectedData.add(newData);
       }

       // Convert list to 2D float array (m × n)
       float[][] dataMatrix = collectedData.toArray(new float[0][0]);

       return new Object[]{dataMatrix, signalNameArray, signalFormatArray, signalUnitArray};
   }

    private void sendData(Object[] data) {
        dataQueue.add(data);
    }
    
    public void readData() { //Test receive data for all Object Cluster
    	Object[] data = receiveData();
    	System.out.println("New Data : " + data[0] + "\tChannel Name  : " + data[1]
    			+ "\tFormat : " + data[2] + "\tUnit : " + data[3]);
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
                	for(String channel : channelNames) {
                		System.out.println(channel);
                	}
                } else if (callbackObject.mState == BT_STATE.DISCONNECTED || callbackObject.mState == BT_STATE.CONNECTION_LOST) {
                    System.out.println("Device State Change: " + callbackObject.mState);
                    System.out.flush();

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
                
                Object[] outputData = { newData, signalNameArray, signalFormatArray, signalUnitArray };

                sendData(outputData);
            }
        }
    }
}