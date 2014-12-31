package com.shimmerresearch.shimmergraph;


import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.BiMap;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
//import android.graphics.Matrix;
public class ShimmerGraph extends Activity {

	private static Context context;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	private static GraphView mGraph;
	Dialog mDialog;
	//private DataMethods DM;
	private static TextView mTitle;
	private static TextView mValueSensor1;
	private static TextView mValueSensor2;
	private static TextView mValueSensor3;
	
	private static TextView mTextSensor1;
	private static TextView mTextSensor2;
	private static TextView mTextSensor3;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
    private static String mConnectedDeviceName = null;
    // Member object for communication services
    private static Shimmer mShimmerDevice = null;
    
    private static String mSensorView = ""; //The sensor device which should be viewed on the graph
    private static int mGraphSubSamplingCount = 0; 
    long dialogEnabledSensors=0;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTitle.setText(R.string.title_not_connected);
        mDialog = new Dialog(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
        }
        ShimmerGraph.context = getApplicationContext();
        setupMain();
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    	if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
    	else {
    		if(mShimmerDevice == null) ;
    	}
    }
    
    private void setupMain() {
    	mGraph = (GraphView)findViewById(R.id.graph);
        mValueSensor1 = (TextView) findViewById(R.id.sensorvalue1);
        mValueSensor2 = (TextView) findViewById(R.id.sensorvalue2);
        mValueSensor3 = (TextView) findViewById(R.id.sensorvalue3);
        mTextSensor1 =  (TextView) findViewById(R.id.LabelSensor1);
        mTextSensor2 =  (TextView) findViewById(R.id.LabelSensor2);
        mTextSensor3 =  (TextView) findViewById(R.id.LabelSensor3);
    	mShimmerDevice = new Shimmer(null, mHandler,"Device 1",false);
    	
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		//finish();
	}
    
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mShimmerDevice != null) mShimmerDevice.stop();
	}
	
	
	// The Handler that gets information back from the BluetoothChatService
    private static Handler mHandler = new Handler() {
   

		public void handleMessage(Message msg) {
        	
            switch (msg.what) {
            
            case Shimmer.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case Shimmer.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);    
                    break;
                case Shimmer.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case Shimmer.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);;
                    // this also stops streaming
                    break;
                }
                break;
            case Shimmer.MESSAGE_READ:
        	    if ((msg.obj instanceof ObjectCluster)){
        	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
        	    Log.d("Shimmer","MSGREAD");
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
        			sensorName[0] = "Accelerometer X";
        			sensorName[1] = "Accelerometer Y";
        			sensorName[2] = "Accelerometer Z";
        			units="u12";
        		}
        		if (mSensorView.equals("Low Noise Accelerometer")){
        			sensorName = new String[3]; // for x y and z axis
        			dataArray = new int[3];
        			calibratedDataArray = new double[3];
        			sensorName[0] = "Low Noise Accelerometer X";
        			sensorName[1] = "Low Noise Accelerometer Y";
        			sensorName[2] = "Low Noise Accelerometer Z";
        			units="u12"; // units are just merely an indicator to correct the graph
        			
        		}
        		if (mSensorView.equals("Wide Range Accelerometer")){
        			sensorName = new String[3]; // for x y and z axis
        			dataArray = new int[3];
        			calibratedDataArray = new double[3];
        			sensorName[0] = "Wide Range Accelerometer X";
        			sensorName[1] = "Wide Range Accelerometer Y";
        			sensorName[2] = "Wide Range Accelerometer Z";
        			units="i16";
        		}
        		if (mSensorView.equals("Gyroscope")){
        			sensorName = new String[3]; // for x y and z axis
        			dataArray = new int[3];
        			calibratedDataArray = new double[3];
        			sensorName[0] = "Gyroscope X";
        			sensorName[1] = "Gyroscope Y";
        			sensorName[2] = "Gyroscope Z";
        			units="i16";
        		}
        		if (mSensorView.equals("Magnetometer")){
        			sensorName = new String[3]; // for x y and z axis
        			dataArray = new int[3];
        			calibratedDataArray = new double[3];
        			sensorName[0] = "Magnetometer X";
        			sensorName[1] = "Magnetometer Y";
        			sensorName[2] = "Magnetometer Z";
        			units="i12";
        		}
        		if (mSensorView.equals("GSR")){
        			sensorName = new String[1]; 
        			dataArray = new int[1];
        			calibratedDataArray = new double[1];
        			sensorName[0] = "GSR";
        			units="u16";
        		}
        		if (mSensorView.equals("EMG")){
        			sensorName = new String[1]; 
        			dataArray = new int[1];
        			calibratedDataArray = new double[1];
        			sensorName[0] = "EMG";
        			units="u12";
        		}
        		if (mSensorView.equals("ECG")){
        			sensorName = new String[2]; 
        			dataArray = new int[2];
        			calibratedDataArray = new double[2];
        			sensorName[0] = "ECG RA-LL";
        			sensorName[1] = "ECG LA-LL";
        			units="u12";
        		}
        		if (mSensorView.equals("Bridge Amplifier")){
        			sensorName = new String[2]; 
        			dataArray = new int[2];
        			calibratedDataArray = new double[2];
        			sensorName[0] = "Bridge Amplifier High";
        			sensorName[1] = "Bridge Amplifier Low";
        			units="u12";
        		}
        		if (mSensorView.equals("Heart Rate")){
        			sensorName = new String[1]; 
        			dataArray = new int[1];
        			calibratedDataArray = new double[1];
        			sensorName[0] = "Heart Rate";
        			units="u8";
        			if (mShimmerDevice.getFirmwareCode()>=1){
        				units="u16";
        			}
        		}
        		if (mSensorView.equals("ExpBoardA0")){
        			sensorName = new String[1]; 
        			dataArray = new int[1];
        			calibratedDataArray = new double[1];
        			sensorName[0] = "ExpBoard A0";
        			units="u12";
        		}
        		if (mSensorView.equals("ExpBoardA7")){
        			sensorName = new String[1]; 
        			dataArray = new int[1];
        			calibratedDataArray = new double[1];
        			sensorName[0] = "ExpBoard A7";
        			units="u12";
        		}
        		if (mSensorView.equals("Battery Voltage")){
        			sensorName = new String[2]; 
        			dataArray = new int[2];
        			calibratedDataArray = new double[2];
        			sensorName[0] = "VSenseReg";
        			sensorName[1] = "VSenseBatt";
        			units="u12";
        		}
        		if (mSensorView.equals("Timestamp")){
        			sensorName = new String[1]; 
        			dataArray = new int[1];
        			calibratedDataArray = new double[1];
        			sensorName[0] = "Timestamp";
        			units="u16";
        		}
        		
        		String deviceName = objectCluster.mMyName;
        		//log data
            		if (deviceName=="Device 1" && sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
				 	    if (sensorName.length>0){
				 	    	
				 	    	Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[0]);  // first retrieve all the possible formats for the current sensor device
				 	    	FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
				 	    	if (formatCluster != null) {
				 	    		//Obtain data for text view
				 	    		calibratedDataArray[0] = formatCluster.mData;
				 	    		calibratedUnits = formatCluster.mUnits;
				 	    		
				 	    		//Obtain data for graph
					 	    	dataArray[0] = (int)((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 
							 	
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
					 	    	

				 	    	}
				 	    }
				 	    if (sensorName.length>2){
				 	    
				 	    	Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[2]);  // first retrieve all the possible formats for the current sensor device
				 	    	FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
				 	    	if (formatCluster != null) {
				 	    		calibratedDataArray[2] = formatCluster.mData;
					 	    	
					 	   	    
				 	    		//Obtain data for graph
				 	    		dataArray[2] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 
					 	    	
				 	    	}
				 	    	
			            }
				 	    //in order to prevent LAG the number of data points plotted is REDUCED
				 	    int maxNumberofSamplesPerSecond=50; //Change this to increase/decrease the number of samples which are graphed
				 	    int subSamplingCount=0;
				 	    if (mShimmerDevice.getSamplingRate()>maxNumberofSamplesPerSecond){
				 	    	subSamplingCount=(int) (mShimmerDevice.getSamplingRate()/maxNumberofSamplesPerSecond);
				 	    	mGraphSubSamplingCount++;
				 	    }
				 	    if (mGraphSubSamplingCount==subSamplingCount){
						mGraph.setDataWithAdjustment(dataArray,"Shimmer : " + deviceName,units);
						if (calibratedDataArray.length>0) {
							mValueSensor1.setText(String.format("%.4f",calibratedDataArray[0]));
							mTextSensor1.setText(calibratedUnits);
						}
						if (calibratedDataArray.length>1) {
							mValueSensor2.setText(String.format("%.4f",calibratedDataArray[1]));
							mTextSensor2.setText(calibratedUnits);
						}
						if (calibratedDataArray.length>2) {
							mValueSensor3.setText(String.format("%.4f",calibratedDataArray[2]));
							mTextSensor3.setText(calibratedUnits);
						}
						

						
	        			
	        			
						mGraphSubSamplingCount=0;
				 	    }
					}
            	}
				
                break;
            case Shimmer.MESSAGE_ACK_RECEIVED:
            	
            	break;
            case Shimmer.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = mShimmerDevice.getBluetoothAddress();
                Toast.makeText(getContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
       
            	
            case Shimmer.MESSAGE_TOAST:
                Toast.makeText(getContext(), msg.getData().getString(Shimmer.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
           
            }
        }
    };
	
    private static Context getContext(){
    	return ShimmerGraph.context;
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
    	switch (requestCode) {
    	case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	if(mShimmerDevice == null) setupMain();
                //setMessage("\nBluetooth is now enabled");
                Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occured
            	Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                finish();       
            }
            break;
    	case REQUEST_CONNECT_SHIMMER:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras()
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mShimmerDevice.connect(address,"default");
            }
            break;
    	case REQUEST_CONFIGURE_SHIMMER:
    		if (resultCode == Activity.RESULT_OK) {
    			mShimmerDevice.writeEnabledSensors(data.getExtras().getInt(ConfigureActivity.mDone));
    		}
    		break;
    	case REQUEST_CONFIGURE_VIEW_SENSOR:
    		if (resultCode == Activity.RESULT_OK) {
    			mSensorView=data.getExtras().getString(ConfigureActivity.mDone);
    			mTextSensor1.setText("");
    			mTextSensor2.setText("");
    			mTextSensor3.setText("");
    			if (mSensorView.equals("Accelerometer")){
        			mTextSensor1.setText("AccelerometerX");
        			mTextSensor2.setText("AccelerometerY");
        			mTextSensor3.setText("AccelerometerZ");
        		}
        		if (mSensorView.equals("Gyroscope")){
        			mTextSensor1.setText("GyroscopeX");
        			mTextSensor2.setText("GyroscopeY");
        			mTextSensor3.setText("GyroscopeZ");
        		}
        		if (mSensorView.equals("Magnetometer")){
        			mTextSensor1.setText("MagnetometerX");
        			mTextSensor2.setText("MagnetometerY");
        			mTextSensor3.setText("MagnetometerZ");
        		}
        		if (mSensorView.equals("GSR")){
        			mTextSensor1.setText("GSR");
        		}
        		if (mSensorView.equals("EMG")){
        			mTextSensor1.setText("EMG");
        		}
        		if (mSensorView.equals("ECG")){
        			mTextSensor1.setText("ECGRALL");
        			mTextSensor2.setText("ECGLALL");
        		}
        		if (mSensorView.equals("Bridge Amplifier")){
        			mTextSensor1.setText("Bridge Amplifier High");
        			mTextSensor2.setText("Bridge Amplifier Low");
        		}
        		if (mSensorView.equals("HeartRate")){
        			mTextSensor1.setText("Heart Rate");
        		}
        		if (mSensorView.equals("ExpBoardA0")){
        			mTextSensor1.setText("ExpBoardA0");
        		}
        		if (mSensorView.equals("ExpBoardA7")){
        			mTextSensor1.setText("ExpBoardA7");
        		}
        		if (mSensorView.equals("TimeStamp")){
        			mTextSensor1.setText("TimeStamp");
        		}
    		}
    		break;
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		MenuItem streamItem = menu.findItem(R.id.stream);
		streamItem.setEnabled(false);
		MenuItem settingsItem = menu.findItem(R.id.settings);
		settingsItem.setEnabled(false);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	
		//disable graph edit for sensors which are not enabled
		
		MenuItem scanItem = menu.findItem(R.id.scan);
		MenuItem streamItem = menu.findItem(R.id.stream);
		MenuItem settingsItem = menu.findItem(R.id.settings);
		
		if((mShimmerDevice != null) && (mShimmerDevice.getShimmerState() == Shimmer.STATE_CONNECTED)){
			scanItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			scanItem.setTitle(R.string.disconnect);
			streamItem.setEnabled(true);
			settingsItem.setEnabled(true);
		}
		else {
			scanItem.setIcon(android.R.drawable.ic_menu_search);
			scanItem.setTitle(R.string.connect);
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
		}
		if(mShimmerDevice.getStreamingStatus() == true && mShimmerDevice.getShimmerState() == Shimmer.STATE_CONNECTED){
			streamItem.setIcon(R.drawable.ic_menu_stop);
			streamItem.setTitle(R.string.stopstream);
		}
		if(mShimmerDevice.getStreamingStatus() == false && mShimmerDevice.getShimmerState() == Shimmer.STATE_CONNECTED && mShimmerDevice.getInstructionStatus()==true){
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setTitle(R.string.startstream);
		}	
		if (mShimmerDevice.getInstructionStatus() == false || (mShimmerDevice.getShimmerState() != Shimmer.STATE_CONNECTED)){ 
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
		}
		if (mShimmerDevice.getStreamingStatus()==true){
			settingsItem.setEnabled(false);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			if ((mShimmerDevice.getShimmerState() == Shimmer.STATE_CONNECTED)) {
				mShimmerDevice.stop();
		    	mShimmerDevice = new Shimmer(this, mHandler,"Device 1",false);
			} else {
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
			}
			return true;

		case R.id.stream:
			if (mShimmerDevice.getStreamingStatus() == true) {
				mShimmerDevice.stopStreaming();
			} else {
				mShimmerDevice.startStreaming();
			}
			
     		return true;
     	case R.id.settings:
     		showEnableSensors(mShimmerDevice.getListofSupportedSensors(),mShimmerDevice.getEnabledSensors());
			
			return true;
     	case R.id.viewsensor:
     		showSelectSensorPlot();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void showSelectSensorPlot(){mDialog.setContentView(R.layout.dialog_sensor_view);
	TextView title = (TextView) mDialog.findViewById(android.R.id.title);
		title.setText("Select Signal");
		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
	listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	List<String> sensorList = null;
	
		sensorList = mShimmerDevice.getListofEnabledSensors();
		sensorList.add("Timestamp");
	
	
	final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);
	ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
	listView.setAdapter(adapterSensorNames);
	
	listView.setOnItemClickListener(new OnItemClickListener(){

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
				mSensorView=sensorNames[arg2];
			
			mDialog.dismiss();
		}
		
	});
	
	mDialog.show();}
	
	
	public void showEnableSensors(final String[] sensorNames, long enabledSensors){
		dialogEnabledSensors=enabledSensors;
		mDialog.setContentView(R.layout.dialog_enable_sensor_view);
		TextView title = (TextView) mDialog.findViewById(android.R.id.title);
 		title.setText("Select Signal");
 		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
		listView.setAdapter(adapterSensorNames);
		final BiMap<String,String> sensorBitmaptoName;
		sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mShimmerDevice.getShimmerVersion());
		for (int i=0;i<sensorNames.length;i++){
			int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
			if( (iDBMValue & enabledSensors) >0){
				listView.setItemChecked(i, true);
			}
		}
				
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex,
					long arg3) {
					int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
					//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
					dialogEnabledSensors = mShimmerDevice.sensorConflictCheckandCorrection(dialogEnabledSensors,sensorIdentifier);
					//update the checkbox accordingly
					for (int i=0;i<sensorNames.length;i++){
						int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
						if( (iDBMValue & dialogEnabledSensors) >0){
							listView.setItemChecked(i, true);
						} else {
							listView.setItemChecked(i, false);
						}
					}
			}
		});
		
		Button mDoneButton = (Button)mDialog.findViewById(R.id.buttonEnableSensors);
		
		mDoneButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mShimmerDevice.writeEnabledSensors(dialogEnabledSensors);
				mDialog.dismiss();
			}});
		
		
		mDialog.show();
 		
	}
	
	
	
}