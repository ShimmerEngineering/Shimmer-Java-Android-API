package com.shimmerresearch.multishimmer;


import java.util.Collection;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerDetails;
import com.shimmerresearch.multishimmerplay.R;
import com.shimmerresearch.service.MultiShimmerPlayService;
import com.shimmerresearch.service.MultiShimmerPlayService.LocalBinder;

public class GraphActivity extends Activity{
	boolean mServiceBind=false;
	MultiShimmerPlayService mService;
	long mEnabledSensors=0;
	String BluetoothAddress="";
	private static GraphView mGraphDisplay;
	private static String mSensorView = ""; //The sensor device which should be viewed on the graph
	private static int mHardwareVersion=0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_view);

		Bundle extras = getIntent().getExtras();
		BluetoothAddress = extras.getString("BluetoothAddress");
		setTitle("Graph: " + BluetoothAddress);
		Intent intent=new Intent(this, MultiShimmerPlayService.class);
		getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		View mGraph = (View) findViewById(R.id.graph);
		mGraphDisplay = (GraphView)findViewById(R.id.graph);

		mGraph.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("ShimmerGraph","on long click");

				Intent mainCommandIntent=new Intent(GraphActivity.this,SensorViewActivity.class);
				mainCommandIntent.putExtra("Enabled_Sensors",mEnabledSensors);
				startActivityForResult(mainCommandIntent, MultiShimmerPlayActivity.REQUEST_CONFIGURE_GRAPH);
				return false;
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case MultiShimmerPlayActivity.REQUEST_CONFIGURE_GRAPH:
			if (resultCode == Activity.RESULT_OK) {
				mSensorView=data.getExtras().getString(ConfigureActivity.mDone);
				Log.d("Shimmer","Request Configure Graph" + mSensorView);
			}
			break;
		}
	}


	private ServiceConnection mTestServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// TODO Auto-generated method stub

			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			Log.d("Shimmer","Connected on Graph" + " " +BluetoothAddress);
			//update the view
			mServiceBind=true;
			mService.setGraphHandler(mHandler,BluetoothAddress);
			mService.enableGraphingHandler(true);
			mEnabledSensors = mService.getEnabledSensors(BluetoothAddress);
			mHardwareVersion = mService.getHWVersion(BluetoothAddress);
		}

		public void onServiceDisconnected(ComponentName arg0) {

			Log.d("Shimmer","Service Disconnected on Graph" + " " +BluetoothAddress);// TODO Auto-generated method stub
			mServiceBind=false;
		}
	};

	public void onPause(){
		super.onPause();

		Log.d("ShimmerH","Graph on Pause");
		if(mServiceBind == true){
			mService.enableGraphingHandler(false);
			getApplicationContext().unbindService(mTestServiceConnection);
		}
	}

	public void onResume(){
		super.onResume();

		Intent intent=new Intent(this, MultiShimmerPlayService.class);
		Log.d("ShimmerH","Graph on Resume");
		getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
	}


	private static Handler mHandler = new Handler() {


		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Shimmer.MESSAGE_READ:
				Log.d("ShimmerGraph","Received");
				if ((msg.obj instanceof ObjectCluster)){

					ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 



					int[] dataArray = new int[0];
					double[] calibratedDataArray = new double[0];
					String[] sensorName = new String[0];
					String units="";
					String calibratedUnits="";
					//mSensorView determines which sensor to graph
					if (mSensorView.equals("Accelerometer")){
						sensorName = new String[3]; // for x y and z axis
						dataArray = new int[3];
						calibratedDataArray = new double[3];
						if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_3){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
						} else if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R){
							sensorName[0] = Shimmer2.ObjectClusterSensorName.ACCEL_X;
							sensorName[1] = Shimmer2.ObjectClusterSensorName.ACCEL_Y;
							sensorName[2] = Shimmer2.ObjectClusterSensorName.ACCEL_Z;
						}
					}
					if (mSensorView.equals("Gyroscope")){
						sensorName = new String[3]; // for x y and z axis
						dataArray = new int[3];
						calibratedDataArray = new double[3];
						if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_3){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.GYRO_X;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.GYRO_Y;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.GYRO_Z;
						} else if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.GYRO_X;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.GYRO_Y;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.GYRO_Z;
						}
					}
					if (mSensorView.equals("Magnetometer")){
						sensorName = new String[3]; // for x y and z axis
						dataArray = new int[3];
						calibratedDataArray = new double[3];
						if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_3){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.MAG_X;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.MAG_Y;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.MAG_Z;
						} else if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.MAG_X;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.MAG_Y;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.MAG_Z;
						}
					}
					if (mSensorView.equals("GSR")){
						sensorName = new String[1]; 
						dataArray = new int[1];
						calibratedDataArray = new double[1];
						if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_3){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.GSR;
						} else if (mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || mHardwareVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R){
							sensorName[0] = Shimmer3.ObjectClusterSensorName.GSR;
						}
					}
					if (mSensorView.equals("EMG")){
						sensorName = new String[1]; 
						dataArray = new int[1];
						calibratedDataArray = new double[1];
						sensorName[0] = "EMG";
					}
					if (mSensorView.equals("ECG")){
						sensorName = new String[2]; 
						dataArray = new int[2];
						calibratedDataArray = new double[2];
						sensorName[0] = "ECG RA-LL";
						sensorName[1] = "ECG LA-LL";
					}
					if (mSensorView.equals("Bridge Amplifier")){
						sensorName = new String[2]; 
						dataArray = new int[2];
						calibratedDataArray = new double[2];
						sensorName[0] = "Bridge Amplifier High";
						sensorName[1] = "Bridge Amplifier Low";
					}
					if (mSensorView.equals("HeartRate")){
						sensorName = new String[1]; 
						dataArray = new int[1];
						calibratedDataArray = new double[1];
						sensorName[0] = "Heart Rate";
					}
					if (mSensorView.equals("ExpBoardA0")){
						sensorName = new String[1]; 
						dataArray = new int[1];
						calibratedDataArray = new double[1];
						sensorName[0] = "ExpBoard A0";
					}
					if (mSensorView.equals("ExpBoardA7")){
						sensorName = new String[1]; 
						dataArray = new int[1];
						calibratedDataArray = new double[1];
						sensorName[0] = "ExpBoard A7";
					}
					if (mSensorView.equals("TimeStamp")){
						sensorName = new String[1]; 
						dataArray = new int[1];
						calibratedDataArray = new double[1];
						sensorName[0] = "Timestamp";
					}

					String deviceName = objectCluster.mBluetoothAddress;
					//log data

					if (sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
						if (sensorName.length>0){
							Log.d("ShimmerGraph","Received2");
							Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[0]);  // first retrieve all the possible formats for the current sensor device
							FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
							if (formatCluster != null) {
								//Obtain data for text view
								calibratedDataArray[0] = formatCluster.mData;
								calibratedUnits = formatCluster.mUnits;

								//Obtain data for graph
								if (sensorName[0]=="Heart Rate"){ // Heart Rate has no uncalibrated data 
									dataArray[0] = (int)((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")).mData; 
									units = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")).mUnits; 
								} else {
									dataArray[0] = (int)((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 
								}
								units = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mUnits; //TODO: Update data structure to include Max and Min values. This is to allow easy graph adjustments for the length and width
							}
						}
						if (sensorName.length>1) {
							Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[1]);  // first retrieve all the possible formats for the current sensor device
							FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL"));
							if (formatCluster != null ) {
								calibratedDataArray[1] = formatCluster.mData;
								//Obtain data for text view

								//Obtain data for graph
								dataArray[1] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 
								units = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mUnits; //TODO: Update data structure to include Max and Min values. This is to allow easy graph adjustments for the length and width

							}
						}
						if (sensorName.length>2){

							Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[2]);  // first retrieve all the possible formats for the current sensor device
							FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
							if (formatCluster != null) {
								calibratedDataArray[2] = formatCluster.mData;


								//Obtain data for graph
								dataArray[2] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 
								units = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mUnits; //TODO: Update data structure to include Max and Min values. This is to allow easy graph adjustments for the length and width
							}

						}
						Log.d("ShimmerGraph","Received3");
						if (sensorName[0].equals("Magnetometer X")){
							mGraphDisplay.setDataWithAdjustment(dataArray,"","i16");
						} else {
							mGraphDisplay.setDataWithAdjustment(dataArray,"","u16");
						}
					}
				}

				break;
			}
		}
	};

}
