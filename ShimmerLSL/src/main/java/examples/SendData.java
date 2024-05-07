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
    static List<LSL.StreamOutlet>[] outlets = new ArrayList[3]; // for each sensor type
    static BasicShimmerBluetoothManagerPc[] btManagers = new BasicShimmerBluetoothManagerPc[2];
    static String[] btComports = {"Com5", "Com7"}; //to use multiple shimmer device
    static final double SAMPLE_RATE = 51.2;
    static final int NUM_CHANNELS = 3;
    
    // Accel labels
    static final String[] ACCEL_LABELS = {"Accel_X","Accel_Y","Accel_Z"};
    // Gyro labels
    static final String[] GYRO_LABELS = {"Gyro_X","Gyro_Y","Gyro_Z"};
    // Magnetometer labels
    static final String[] MAG_LABELS = {"Mag_X","Mag_Y","Mag_Z"};

    public static void main(String[] args) throws IOException, InterruptedException  {
        
        NativeLibraryLoader.loadLibrary(); //to use the lib/liblsl64.dll
        
        for (int i = 0; i < btManagers.length; i++) {
            btManagers[i] = new BasicShimmerBluetoothManagerPc();
        }
        
        for (int i = 0; i < btManagers.length; i++) {
            System.out.println("Creating new StreamInfo for Device " + (i+1) + "...");
            for(int j = 0; j < 3; j++) { // outlet for each sensor
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
                for (int k = 0; k < labels.length; k++) {
                    chns.append_child("channel")
                            .append_child_value("label", labels[k])
                            //.append_child_value("unit", "microvolts")
                            .append_child_value("type", type);
                }
                System.out.println("Creating outlet for Device " + (i+1) + "...");
                if(outlets[j] == null)
                    outlets[j] = new ArrayList<>();
                outlets[j].add(new LSL.StreamOutlet(info));
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
                    if(shimmerDevice != null) {
                        System.out.println("Device " + (i+1) + " connected.");
                        shimmerDevices.add(shimmerDevice);
                    }
                }
            }
        }
        else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
            CallbackObject callbackObject = (CallbackObject)object;
            int msg = callbackObject.mIndicator;
            if (msg== ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){
                try {
                    for (ShimmerDevice shimmerDevice : shimmerDevices) {
                        shimmerDevice.startStreaming();
                    }
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
            System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
            ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
            double accelX = objc.getFormatClusterValue("Accel_LN_X", "CAL");
            double accelY = objc.getFormatClusterValue("Accel_LN_Y", "CAL");
            double accelZ = objc.getFormatClusterValue("Accel_LN_Z", "CAL");
            double gyroX = objc.getFormatClusterValue("Gyro_X", "CAL");
            double gyroY = objc.getFormatClusterValue("Gyro_Y", "CAL");
            double gyroZ = objc.getFormatClusterValue("Gyro_Z", "CAL");
            double magX = objc.getFormatClusterValue("Mag_X", "CAL");
            double magY = objc.getFormatClusterValue("Mag_Y", "CAL");
            double magZ = objc.getFormatClusterValue("Mag_Z", "CAL");

            double timestamp = objc.mSystemTimeStamp;

            float[] accelData = {(float)accelX, (float)accelY, (float)accelZ};
            float[] gyroData = {(float)gyroX, (float)gyroY, (float)gyroZ};
            float[] magData = {(float)magX, (float)magY, (float)magZ};

            if(!Double.isNaN(accelX) && !Double.isNaN(accelY) && !Double.isNaN(accelZ)) {
                for (LSL.StreamOutlet outlet : outlets[0]) {
                    outlet.push_sample(accelData, timestamp);
                }
            }
            if(!Double.isNaN(gyroX) && !Double.isNaN(gyroY) && !Double.isNaN(gyroZ)) {
                for (LSL.StreamOutlet outlet : outlets[1]) {
                    outlet.push_sample(gyroData, timestamp);
                }
            }
            if(!Double.isNaN(magX) && !Double.isNaN(magY) && !Double.isNaN(magZ)) {
                for (LSL.StreamOutlet outlet : outlets[2]) {
                    outlet.push_sample(magData, timestamp);
                }
            }
        }
    }
}
