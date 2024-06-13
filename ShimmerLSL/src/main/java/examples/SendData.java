package examples;

import edu.ucsd.sccn.LSL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.exceptions.ShimmerException;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SendData extends BasicProcessWithCallBack {
    
    static List<LSL.StreamOutlet> outlets = new ArrayList<>(); // flat list for each sensor of each device

    static String[] btComports = {"COM33","COM6"}; // to use multiple shimmer device
    static List<String> shimmerNames = new ArrayList<>();
    static int NUM_DEVICES = btComports.length;
    static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
    static final double SAMPLE_RATE = 51.2;
    static final int NUM_CHANNELS = 3;
    
    // Accel labels
    static final String[] ACCEL_LABELS = {"Accel_X","Accel_Y","Accel_Z"};
    // Gyro labels
    static final String[] GYRO_LABELS = {"Gyro_X","Gyro_Y","Gyro_Z"};
    // Magnetometer labels
    static final String[] MAG_LABELS = {"Mag_X","Mag_Y","Mag_Z"};

    public static void main(String[] args) throws IOException, InterruptedException  {
        
    	JFrame frame = new JFrame();
    	frame.setSize(300, 300);
    	frame.getContentPane().setLayout(null);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);
    	JButton btnNewButton = new JButton("Connect All Devices");
    	btnNewButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			 for (int i = 0; i < btComports.length; i++) {
    		            btManager.connectShimmerThroughCommPort(btComports[i]);
    		     }
    		}
    	});
    	btnNewButton.setBounds(21, 21, 140, 23);
    	frame.getContentPane().add(btnNewButton);
    	
    	JButton button = new JButton("Start Streaming");
    	button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					 for (int i = 0; i < btComports.length; i++) {
						ShimmerDevice device = btManager.getShimmerDeviceBtConnected(btComports[i]);
						try {
							((ShimmerBluetooth)device).startStreaming();
						} catch (ShimmerException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					 }
				}
			});
    	button.setBounds(21, 93, 140, 23);
    	frame.getContentPane().add(button);
    	
        NativeLibraryLoader.loadLibrary(); // to use the lib/liblsl64.dll
        
        for (int i = 0; i < btComports.length; i++) {
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
       
        s.setWaitForData(btManager.callBackObject);
       
    }
    
    @Override
    protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
        int ind = shimmerMSG.mIdentifier;
        Object object = (Object) shimmerMSG.mB;
        
        if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
            CallbackObject callbackObject = (CallbackObject) object;
            if (callbackObject.mState == BT_STATE.CONNECTED) {
                for (int i = 0; i < btComports.length; i++) {
                    ShimmerDevice shimmerDevice = btManager.getShimmerDeviceBtConnected(btComports[i]);
                    if (!shimmerNames.contains(shimmerDevice.getShimmerUserAssignedName())) {
                    	shimmerNames.add(shimmerDevice.getShimmerUserAssignedName());
                    }
                }
            }
        } else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
            CallbackObject callbackObject = (CallbackObject)object;
            int msg = callbackObject.mIndicator;
            if (msg == ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED) {
            	
            }
        } else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
            handleDataPacket(shimmerMSG);
        }
    }
    
    private void handleDataPacket(ShimmerMsg shimmerMSG) {
        ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
        int deviceIndex = shimmerNames.indexOf(objc.getShimmerName());
        
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
