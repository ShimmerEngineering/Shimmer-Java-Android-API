package examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.exceptions.ShimmerException;

import edu.ucsd.sccn.*;
import edu.ucsd.sccn.LSL.StreamInfo;
import edu.ucsd.sccn.LSL.StreamOutlet;

public class SendData extends BasicProcessWithCallBack {

    static List<StreamOutlet> outlets = new ArrayList<>(); // LSL stream outlets
    static List<String> shimmerNames = new ArrayList<>();
    static final String[] btComports = {"COM8"}; // Bluetooth COM ports
    static final int NUM_DEVICES = btComports.length;
    static final BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
    static final double SAMPLE_RATE = 51.2;
    //static final int NUM_CHANNELS = 3;
    static boolean streamingState = false;

    // Sensor labels
    static final String[] SENSOR_LABELS = {
    		"Accel_LN_X","Accel_LN_Y","Accel_LN_Z"
        		,"Gyro_X","Gyro_Y","Gyro_Z"
        		,"Mag_X","Mag_Y","Mag_Z"};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setSize(300, 300);
            frame.getContentPane().setLayout(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JButton connectButton = new JButton("Connect All Devices");
            connectButton.addActionListener(e -> connectAllDevices());
            connectButton.setBounds(21, 21, 140, 23);
            frame.getContentPane().add(connectButton);

            JButton startButton = new JButton("Start Streaming");
            startButton.addActionListener(e -> startStreaming());
            startButton.setBounds(21, 93, 140, 23);
            frame.getContentPane().add(startButton);

            JButton stopButton = new JButton("Stop Streaming");
            stopButton.addActionListener(e -> stopStreaming());
            stopButton.setBounds(21, 165, 140, 23);
            frame.getContentPane().add(stopButton);

            frame.setVisible(true);

            SendData s = new SendData();
            s.setWaitForData(btManager.callBackObject);
        });
    }

    private static void connectAllDevices() {
        for (String comport : btComports) {
            btManager.connectShimmerThroughCommPort(comport);
        }
    }

    private static void startStreaming() {
        streamingState = true;
        for (String comport : btComports) {
            ShimmerDevice device = btManager.getShimmerDeviceBtConnected(comport);
            if (device instanceof ShimmerBluetooth) {
                try {
                    ((ShimmerBluetooth) device).startStreaming();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void stopStreaming() {
        streamingState = false;
        for (String comport : btComports) {
            ShimmerDevice device = btManager.getShimmerDeviceBtConnected(comport);
            if (device instanceof ShimmerBluetooth) {
                ((ShimmerBluetooth) device).stopStreaming();
            }
        }
    }

    @Override
    protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
        int ind = shimmerMSG.mIdentifier;
        Object object = shimmerMSG.mB;

        if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
            CallbackObject callbackObject = (CallbackObject) object;
            if (callbackObject.mState == ShimmerBluetooth.BT_STATE.CONNECTED) {
                for (String comport : btComports) {
                    ShimmerDevice shimmerDevice = btManager.getShimmerDeviceBtConnected(comport);
                    if (shimmerDevice != null && !shimmerNames.contains(shimmerDevice.getShimmerUserAssignedName())) {
                        shimmerNames.add(shimmerDevice.getShimmerUserAssignedName());
                    }
                }
            }
        } else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
            CallbackObject callbackObject = (CallbackObject) object;
            int msg = callbackObject.mIndicator;
            if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
                if (streamingState) {
                    createStreamOutlet();
                }
            } else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
                if (!streamingState) {
                    destroyStreamOutlet();
                }
            }
        } else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
            handleDataPacket(shimmerMSG);
        }
    }

    private static void createStreamOutlet() {
    	NativeLibraryLoader.loadLibrary();
    	
        for (int i = 0; i < btComports.length; i++) {
            System.out.println("Creating StreamInfos for Device " + (i + 1) + "...");
            for (String label : SENSOR_LABELS) {
                String streamName = "SendData_Device" + (i + 1) + "_" + btComports[i] + "_" + label;
                StreamInfo info = new StreamInfo(streamName, "SensorData", 1, SAMPLE_RATE, LSL.ChannelFormat.float32, "test");
                try {
					outlets.add(new LSL.StreamOutlet(info));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                System.out.println("Created outlet for " + streamName);
            }
        }
    }

    private static void destroyStreamOutlet() {
        for (StreamOutlet outlet : outlets) {
            outlet.close();
            System.out.println("Destroyed outlet for " + outlet.info().name());
        }
        outlets.clear();
    }

    private void handleDataPacket(ShimmerMsg shimmerMSG) {
        ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
        String shimmerName = objc.getShimmerName();
        int deviceIndex = shimmerNames.indexOf(shimmerName);

        for (int i = 0; i < SENSOR_LABELS.length; i++) {
        	double sensorValue = objc.getFormatClusterValue(SENSOR_LABELS[i], "CAL");
            if (!Double.isNaN(sensorValue)) {
                float[] data = {(float) sensorValue};
                int outletIndex = deviceIndex * SENSOR_LABELS.length + i;
                outlets.get(outletIndex).push_sample(data);
                System.out.println("Device " + shimmerName + " " + SENSOR_LABELS[i] + " data sent.");
            }
        }
    }
}
