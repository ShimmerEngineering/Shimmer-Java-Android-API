package com.shimmerresearch.multishimmerexample;


import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
public class MultiShimmerExampleActivity extends Activity {
	/** Called when the activity is first created. */


	private Shimmer mShimmerDevice1 = null;
	private Shimmer mShimmerDevice2 = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	Timer mTimer;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Configuration.setTooLegacyObjectClusterSensorNames();
		mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm",false); //Right Arm is a unique identifier for the shimmer unit
		mShimmerDevice2 = new Shimmer(this, mHandler, "LeftArm", 51.2, 0, 0, Shimmer.SENSOR_ACCEL, false); //Left Arm is a unique identifier for the shimmer unit

		String bluetoothAddress="00:06:66:66:8B:FF";
		mShimmerDevice1.connect(bluetoothAddress,"default");         

	}
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
			case Shimmer.MESSAGE_READ:
				if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
					ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
					Collection<FormatCluster> accelXFormats = objectCluster.getCollectionOfFormatClusters(Shimmer2.ObjectClusterSensorName.ACCEL_X);  // first retrieve all the possible formats for the current sensor device
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.getShimmerName() + " AccelX: " + formatCluster.mData + " "+ formatCluster.mUnits);
					}
					Collection<FormatCluster> accelYFormats = objectCluster.getCollectionOfFormatClusters(Shimmer2.ObjectClusterSensorName.ACCEL_Y);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.getShimmerName() + " AccelY: " + formatCluster.mData + " "+formatCluster.mUnits);
					}
					Collection<FormatCluster> accelZFormats = objectCluster.getCollectionOfFormatClusters(Shimmer2.ObjectClusterSensorName.ACCEL_Z);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.getShimmerName() + " AccelZ: " + formatCluster.mData + " "+formatCluster.mUnits);
					}


					accelXFormats = objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.getShimmerName() + " AccelLNX: " + formatCluster.mData + " "+ formatCluster.mUnits);
					}
					accelYFormats = objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.getShimmerName() + " AccelLNY: " + formatCluster.mData + " "+formatCluster.mUnits);
					}
					accelZFormats = objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.getShimmerName() + " AccelLNZ: " + formatCluster.mData + " "+formatCluster.mUnits);
					}

				}
				break;
			case Shimmer.MESSAGE_TOAST:
				Log.d("toast",msg.getData().getString(Shimmer.TOAST));
				Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),
						Toast.LENGTH_SHORT).show();
				break;


			case Shimmer.MESSAGE_STATE_CHANGE:
				 switch (((ObjectCluster)msg.obj).mState) {
                 case CONNECTED:
					//Next connect the next device
					Log.d("ConnectionStatus","Fully Initialized" + ((ObjectCluster)msg.obj).getMacAddress());

					if (mShimmerDevice2.getBluetoothRadioState()==BT_STATE.DISCONNECTED){
						mShimmerDevice2.connect("00:06:66:66:96:86","default"); 
					} else if (mShimmerDevice1.getState()==BT_STATE.CONNECTED && mShimmerDevice1.getStreamingStatus()==false && mShimmerDevice2.getBluetoothRadioState()==BT_STATE.CONNECTED && mShimmerDevice2.getStreamingStatus()==false){
						Log.d("ConnectionStatus","Successful!");

						mShimmerDevice1.startStreaming();

						mShimmerDevice2.startStreaming();
						shimmerTimer(30); //disconnect in 30 seconds
					} 

					break;
				}
				break;
			}
		}
	};

	public synchronized void shimmerTimer(int seconds) {
		mTimer = new Timer();
		mTimer.schedule(new responseTask(), seconds*1000);
	}

	class responseTask extends TimerTask {
		public void run() {
			mShimmerDevice1.stopStreaming();
			mShimmerDevice1.stop();
			mShimmerDevice2.stopStreaming();
			mShimmerDevice2.stop();
		}
	}



}





