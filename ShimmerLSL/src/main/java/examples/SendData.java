package examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSL.StreamInfo;
import edu.ucsd.sccn.LSL.StreamOutlet;
import javax.swing.JLabel;

public class SendData extends BasicProcessWithCallBack {

    static List<StreamOutlet> outlets = new ArrayList<>(); // LSL stream outlets
    static List<String> shimmerNames = new ArrayList<>();
    static final String[] btComports = {"COM8"}; // Bluetooth COM ports
    static final int NUM_DEVICES = btComports.length;
    static final BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
    static final double SAMPLE_RATE = 51.2;
    static boolean streamingState = false;
    static TreeMap<Integer, SensorGroupingDetails> compatibleSensorGroupMap;
    static int sensorKeys[];

    // Sensor labels
    static final String[] SENSOR_LABELS = {
            "Accel_LN_X", "Accel_LN_Y", "Accel_LN_Z",
            "Gyro_X", "Gyro_Y", "Gyro_Z",
            "Mag_X", "Mag_Y", "Mag_Z"};


    public static void main(String[] args) {
   
            JFrame frame = new JFrame();
            frame.setSize(509, 400);
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
            
            JLabel lblNewLabel = new JLabel("Please enable LN Accel, Gyro and Mag before connecting");
            lblNewLabel.setBounds(168, 25, 315, 14);
            frame.getContentPane().add(lblNewLabel);
            
            JButton configButton = new JButton("Configure Sensors");
            configButton.addActionListener(e -> configureSensors());
            configButton.setBounds(21, 237, 140, 23);
            //frame.getContentPane().add(configButton);

            frame.setVisible(true);

            SendData s = new SendData();
            s.setWaitForData(btManager.callBackObject);

    }

    private static void connectAllDevices() {
        for (String comport : btComports) {
            btManager.connectShimmerThroughCommPort(comport);
        }
    }

    private static void startStreaming() {
        streamingState = true;
        
        for (String comport : btComports) {
            ShimmerPC device = (ShimmerPC)btManager.getShimmerDeviceBtConnected(comport);
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
	        	ShimmerPC device = (ShimmerPC)btManager.getShimmerDeviceBtConnected(comport);
	            if (device instanceof ShimmerBluetooth) {
	                ((ShimmerBluetooth) device).stopStreaming();
	            }
	        }
        
    }
    
    protected static boolean isSensorGroupCompatible(ShimmerDevice device, SensorGroupingDetails groupDetails) {
        List<ShimmerVerObject> listOfCompatibleVersionInfo = groupDetails.mListOfCompatibleVersionInfo;
        return device.isVerCompatibleWithAnyOf(listOfCompatibleVersionInfo);
    }
    
    private static void configureSensors() {
    	List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
    	for (String comport : btComports) {
    		ShimmerPC device = (ShimmerPC) btManager.getShimmerDeviceBtConnected(comport);
            ShimmerDevice cloneDevice = device.deepClone();
			cloneDevice.setEnabledAndDerivedSensorsAndUpdateMaps(0, 0);
			cloneDevice.setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, true);
			cloneDevice.setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, true);
			cloneDevice.setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, true);
			cloneDevice.setSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH, SAMPLE_RATE);;
			AssembleShimmerConfig.generateSingleShimmerConfig(cloneDevice, COMMUNICATION_TYPE.BLUETOOTH);
			cloneList.add(cloneDevice);
	 	}
    	btManager.configureShimmers(cloneList);
 		
    	System.out.println("Writing config to Shimmer...");
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
                System.out.println("Device " + shimmerName + " " + SENSOR_LABELS[i] + " " + sensorValue + " data sent.");
            }
        }
    }
}
