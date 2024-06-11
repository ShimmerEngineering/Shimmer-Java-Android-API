package examples;

import edu.ucsd.sccn.LSL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.exceptions.ShimmerException;

public class SendData extends BasicProcessWithCallBack {
    
    static List<ShimmerDevice> shimmerDevices = new ArrayList<>();
    static List<LSL.StreamOutlet> outlets = new ArrayList<>(); // flat list for each sensor of each device

    static String[] btComports = {"Com8"}; // to use multiple shimmer device
    static String[] shimmerNames = {"Shimmer_6749"};
    static int NUM_DEVICES = btComports.length;
    static BasicShimmerBluetoothManagerPc[] btManagers = new BasicShimmerBluetoothManagerPc[NUM_DEVICES];
    static final double SAMPLE_RATE = 51.2;
    static final int NUM_CHANNELS = 3;
    
    // Accel labels
    static final String[] ACCEL_LABELS = {"Accel_X","Accel_Y","Accel_Z"};
    // Gyro labels
    static final String[] GYRO_LABELS = {"Gyro_X","Gyro_Y","Gyro_Z"};
    // Magnetometer labels
    static final String[] MAG_LABELS = {"Mag_X","Mag_Y","Mag_Z"};

    public static void main(String[] args) throws IOException, InterruptedException  {
        
        NativeLibraryLoader.loadLibrary(); // to use the lib/liblsl64.dll
        
        for (int i = 0; i < btManagers.length; i++) {
            btManagers[i] = new BasicShimmerBluetoothManagerPc();
            System.out.println("Creating StreamInfos for Device " + (i+1) + "...");
            for (int j = 0; j < 3; j++) { // outlet for each sensor
                String type = "";
                String streamName = "";
                if (j == 0) {
                    streamName += "_Accel";
                    type = "Accel";
                } else if (j == 1) {
                    streamName += "_Gyro";
                    type = "Gyro";
                } else if (j == 2) {
                    streamName += "_Mag";
                    type = "Mag";
                }
                LSL.StreamInfo info = new LSL.StreamInfo("SendData_Device" + (i+1) + "_" + btComports[i] + streamName, type, NUM_CHANNELS, SAMPLE_RATE, LSL.ChannelFormat.float32, "test");
                LSL.XMLElement chns = info.desc().append_child("channels");
                String[] labels = j == 0 ? ACCEL_LABELS : (j == 1 ? GYRO_LABELS : MAG_LABELS);
                for (int k = 0; k < NUM_CHANNELS; k++) {
                    chns.append_child("channel")
                        .append_child_value("label", labels[k])
                        .append_child_value("type", type);
                }
                System.out.println("Creating outlet for Device " + (i+1) + " Sensor " + type + "...");
                outlets.add(new LSL.StreamOutlet(info));
            }
        }
        
        SendData s = new SendData();
        for (BasicShimmerBluetoothManagerPc btManager : btManagers) {
            s.setWaitForData(btManager.callBackObject);
        }
        
        for (int i = 0; i < btManagers.length; i++) {
            btManagers[i].connectShimmerThroughCommPort(btComports[i]);
        }
    }
    
    @Override
    protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
        int ind = shimmerMSG.mIdentifier;
        Object object = (Object) shimmerMSG.mB;
        
        if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
            CallbackObject callbackObject = (CallbackObject) object;
            if (callbackObject.mState == BT_STATE.CONNECTED) {
                for (int i = 0; i < btManagers.length; i++) {
                    ShimmerDevice shimmerDevice = btManagers[i].getShimmerDeviceBtConnected(btComports[i]);
                    if (shimmerDevice != null) {
                        System.out.println("Device " + (i+1) + " connected.");
                        shimmerDevices.add(shimmerDevice);
                    }
                }
            }
        } else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
            CallbackObject callbackObject = (CallbackObject)object;
            int msg = callbackObject.mIndicator;
            if (msg == ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED) {
                try {
                    for (ShimmerDevice shimmerDevice : shimmerDevices) {
                        shimmerDevice.startStreaming();                 
                        System.out.println("Device " + shimmerDevice.getShimmerUserAssignedName() + " streamed.");
                    }
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        } else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
            handleDataPacket(shimmerMSG);
        }
    }
    
    private void handleDataPacket(ShimmerMsg shimmerMSG) {
        ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
        int deviceIndex = objc.getShimmerName().equals(shimmerNames[0]) ? 0 : 1;
        
        double[] sensorValues = {
            objc.getFormatClusterValue("Accel_LN_X", "CAL"),
            objc.getFormatClusterValue("Accel_LN_Y", "CAL"),
            objc.getFormatClusterValue("Accel_LN_Z", "CAL"),
            objc.getFormatClusterValue("Gyro_X", "CAL"),
            objc.getFormatClusterValue("Gyro_Y", "CAL"),
            objc.getFormatClusterValue("Gyro_Z", "CAL"),
            objc.getFormatClusterValue("Mag_X", "CAL"),
            objc.getFormatClusterValue("Mag_Y", "CAL"),
            objc.getFormatClusterValue("Mag_Z", "CAL")
        };
        
        for (int j = 0; j < 3; j++) {
            int outletIndex = deviceIndex * 3 + j;
            if (!Double.isNaN(sensorValues[j*3]) && !Double.isNaN(sensorValues[j*3 + 1]) && !Double.isNaN(sensorValues[j*3 + 2])) {
                float[] data = {(float)sensorValues[j*3], (float)sensorValues[j*3 + 1], (float)sensorValues[j*3 + 2]};
                outlets.get(outletIndex).push_sample(data);
                System.out.println("Device " + (deviceIndex + 1) + " " + outlets.get(outletIndex).info().type() + " data sent.");
            }
        }
    }
}
