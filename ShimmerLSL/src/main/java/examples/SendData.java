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
    static List<LSL.StreamOutlet> outlets = new ArrayList<>();
    static BasicShimmerBluetoothManagerPc[] btManagers = new BasicShimmerBluetoothManagerPc[2];
    static String[] btComports = {"Com5", "Com7"}; //to use multiple shimmer device
    static final double SAMPLE_RATE = 51.2;
    static final int NUM_CHANNELS = 3;
    
    public static void main(String[] args) throws IOException, InterruptedException  {
        
        NativeLibraryLoader.loadLibrary(); //to use the lib/liblsl64.dll
        
        for (int i = 0; i < btManagers.length; i++) {
            btManagers[i] = new BasicShimmerBluetoothManagerPc();
        }
        
        for (int i = 0; i < btManagers.length; i++) {
            System.out.println("Creating new StreamInfo for Device " + (i+1) + "...");
            LSL.StreamInfo info = new LSL.StreamInfo("SendData_Device" + (i+1) + "_" + btComports[i],"Accel", NUM_CHANNELS, SAMPLE_RATE,LSL.ChannelFormat.float32,"test");
            System.out.println("Creating outlet for Device " + (i+1) + "...");
            outlets.add(new LSL.StreamOutlet(info));
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
            double data = objc.getFormatClusterValue("Accel_LN_X", "CAL");
            if(data != Double.NaN) {
                float[] dataArray = new float[3];
                dataArray[0] = (float)objc.getFormatClusterValue("Accel_LN_X", "CAL");
                dataArray[1] = (float)objc.getFormatClusterValue("Accel_LN_Y", "CAL");
                dataArray[2] = (float)objc.getFormatClusterValue("Accel_LN_Z", "CAL");
                
                long timestamp = System.nanoTime();

                for (LSL.StreamOutlet outlet : outlets) {
                	outlet.push_sample(dataArray,timestamp);
                    //outlet.push_sample(dataArray);
                }
            }
        }
    }
}
