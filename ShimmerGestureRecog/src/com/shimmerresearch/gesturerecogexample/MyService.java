package com.shimmerresearch.gesturerecogexample;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;

public class MyService extends Service {
	private static final String TAG = "MyService";
    public Shimmer shimmerDevice1 = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	int test=1;
	private final IBinder mBinder = new LocalBinder();
	@Override
	
	
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	public class LocalBinder extends Binder {
        MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }
	@Override
	public void onCreate() {
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		shimmerDevice1.stopStreaming();
		shimmerDevice1.stop();
	}
	
	
	public void initialize(String bluetoothAddress,Handler mHandler, Context context){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        shimmerDevice1=new Shimmer(context, mHandler,"RightArm", false);
        shimmerDevice1.connect(bluetoothAddress,"default"); //Right Arm is a unique identifier for the shimmer unit
        Log.d("ConnectionStatus","Trying" + Integer.toString(test));
        while (shimmerDevice1.getInitialized()!=true){}; // wait for the connecting state to finish, users should note that this is considered bad practice, official examples show how to wait for the device to reach an initialized staged
        if (shimmerDevice1.getShimmerState()==Shimmer.STATE_CONNECTED){
	        Log.d("ConnectionStatus","Successful");
	        shimmerDevice1.writeSamplingRate(51.2);
	        shimmerDevice1.writeEnabledSensors(Shimmer.SENSOR_GYRO);
	        shimmerDevice1.startStreaming();
        }
	}
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
	    
	}
	
	

}
