package com.shimmerresearch.orientationexample;





import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.shimmer3dexample.R;
/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson04.java
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */

/*
 * 3D Cube generation taken from above
 */

public class Shimmer3DOrientationExample extends Activity {

	/** The OpenGL View */
	private GLSurfaceView glSurface;
	Button buttonConnect;
	Button buttonSet;
	Button buttonReset;
	MyGLSurfaceView t;
	TextView mTVQ1;
	TextView mTVQ2;
	TextView mTVQ3;
	TextView mTVQ4;
	Matrix3d invm3d = new Matrix3d();
	Matrix3d fm3d = new Matrix3d();
	Matrix3d m3d = new Matrix3d();
    private Shimmer mShimmerDevice1 = null;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exercise);
		t= new MyGLSurfaceView(this);
		//Create an Instance with this Activity
		glSurface = (GLSurfaceView)findViewById(R.id.graphics_glsurfaceview1);
		buttonSet = (Button)findViewById(R.id.buttonSet);
		buttonReset = (Button)findViewById(R.id.buttonReset);
		//Set our own Renderer
		glSurface.setRenderer(t);
		//Set the GLSurface as View to this Activity
		//setContentView(glSurface);
		invm3d = new Matrix3d();
		fm3d = new Matrix3d();
		m3d = new Matrix3d();
		invm3d.setIdentity();
		mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm", 51.2, 0, 0, Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO|Shimmer.SENSOR_MAG, false); 
		mShimmerDevice1.enableOnTheFlyGyroCal(true, 102, 1.2);
		mShimmerDevice1.enable3DOrientation(true);
		buttonReset.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				invm3d.setIdentity();
			}
        	
        });

		buttonSet.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				//invm3d.set(fm3d);
				invm3d.set(m3d);
				invm3d.invert();
			}
        	
        });
	
		 mTVQ1 = (TextView)findViewById(R.id.textViewQ1);
		 mTVQ2 = (TextView)findViewById(R.id.textViewQ2);
		 mTVQ3 = (TextView)findViewById(R.id.textViewQ3);
		 mTVQ4 = (TextView)findViewById(R.id.textViewQ4);
		
	}

	/**
	 * Remember to resume the glSurface
	 */
	@Override
	protected void onResume() {
		super.onResume();
		glSurface.onResume();
	}

	/**
	 * Also pause the glSurface
	 */
	@Override
	protected void onPause() {
		super.onPause();
		glSurface.onPause();
	}

	 private final Handler mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
	            case Shimmer.MESSAGE_READ:
	            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
	            	    if (objectCluster.mMyName=="RightArm"){
	                	    Collection<FormatCluster> accelXFormats = objectCluster.mPropertyCluster.get("Axis Angle A");  // first retrieve all the possible formats for the current sensor device
				 	    	float angle = 0,x = 0,y=0,z=0;
	                	    if (accelXFormats != null){
				 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
				 	    		angle = (float) formatCluster.mData;
				 	    	}
				 	    	Collection<FormatCluster> accelYFormats = objectCluster.mPropertyCluster.get("Axis Angle X");  // first retrieve all the possible formats for the current sensor device
				 	    	if (accelYFormats != null){
				 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
				 	    		x=(float) formatCluster.mData;
				 	    	}
				 	    	Collection<FormatCluster> accelZFormats = objectCluster.mPropertyCluster.get("Axis Angle Y");  // first retrieve all the possible formats for the current sensor device
				 	    	if (accelZFormats != null){
				 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
				 	    		y=(float) formatCluster.mData;
				 	    	}
				 	    	Collection<FormatCluster> aaFormats = objectCluster.mPropertyCluster.get("Axis Angle Z");  // first retrieve all the possible formats for the current sensor device
				 	    	if (aaFormats != null){
				 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(aaFormats,"CAL")); // retrieve the calibrated data
				 	    		z=(float) formatCluster.mData;
				 	    		AxisAngle4d aa=new AxisAngle4d(x,y,z,angle);
				 	    		Quat4d qt = new Quat4d();
				 	    		qt.set(aa);
				 	    		mTVQ1.setText(Double.toString(round(qt.w, 3, BigDecimal.ROUND_HALF_UP)));
				 	    		mTVQ2.setText(Double.toString(round(qt.x, 3, BigDecimal.ROUND_HALF_UP)));
				 	    		mTVQ3.setText(Double.toString(round(qt.y, 3, BigDecimal.ROUND_HALF_UP)));
				 	    		mTVQ4.setText(Double.toString(round(qt.z, 3, BigDecimal.ROUND_HALF_UP)));
				 	    		m3d.set(aa);
				 	    		Matrix3d fm3dtemp = new Matrix3d();
				 	    		
				 	    		//set function, the purpose of this is to find a rotation such that the orientation output of the Shimmer device, matches the orientation of the cube when the orange side is facing the user which in terms of rotation matrix is the identity matrix. 
				 	    		fm3dtemp.set(invm3d);
				 	    		fm3dtemp.mul(m3d);
				 	    		aa.set(fm3dtemp);
				 	    		t.setAngleAxis((float) (aa.angle*180/Math.PI), (float)aa.x, (float)aa.y, (float)aa.z);
				 	    	}
			 	    	}
	            	}
	                break;
	                 case Shimmer.MESSAGE_TOAST:
	                	Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),Toast.LENGTH_SHORT).show();
	                break;

	                 case Shimmer.MESSAGE_STATE_CHANGE:
	                	 switch (msg.arg1) {
	                     	case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	                    	    if (mShimmerDevice1.getShimmerState()==Shimmer.STATE_CONNECTED){
	                    	        Log.d("ConnectionStatus","Successful");
	                    	        //because the default mag range for Shimmer2 and 3 are 0 and 1 respectively, please be aware of what range you use when calibrating using Shimmer 9DOF Cal App, and use the same range here
	                    	        if (mShimmerDevice1.getShimmerVersion()==Shimmer.SHIMMER_2R){ 
	                    	        	mShimmerDevice1.writeMagRange(0);
	                    	        } else {
	                    	        	mShimmerDevice1.writeMagRange(1);
	                    	        }
	                    	        mShimmerDevice1.startStreaming();
	                    	     }
	                    	    break;
		                    case Shimmer.STATE_CONNECTING:
		                    	Log.d("ConnectionStatus","Connecting");
	                	        break;
		                    case Shimmer.STATE_NONE:
		                    	Log.d("ConnectionStatus","No State");
		                    	break;
	                     }
	                break;
	                
	            }
	        }
	    };
	    
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu_options, menu);
			return true;
		}
	
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			Intent serverIntent;
			switch (item.getItemId()) {
			
			case R.id.itemConnect:
				serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);	
				
				return true;
				
			case R.id.itemDisconnect:
				mShimmerDevice1.stop();
				return true;
			case R.id.itemConfigure:
				serverIntent = new Intent(this, ConfigureActivity.class);
				serverIntent.putExtra("LowPowerMag",mShimmerDevice1.isLowPowerMagEnabled());
				serverIntent.putExtra("GyroOnTheFlyCal",mShimmerDevice1.isGyroOnTheFlyCalEnabled());
				startActivityForResult(serverIntent, REQUEST_CONFIGURE_SHIMMER);
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		
		
		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			
	    	switch (requestCode) {

	    	case REQUEST_CONNECT_SHIMMER:
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	            	if (mShimmerDevice1.getStreamingStatus()==true){
						mShimmerDevice1.stop();
					} else {
						String bluetoothAddress= data.getExtras()
		                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
						mShimmerDevice1.connect(bluetoothAddress,"default"); 
						invm3d = new Matrix3d();
						invm3d.setIdentity();
						fm3d = new Matrix3d();
						m3d = new Matrix3d();
					}
	            }
	            break;
	            
	    	case REQUEST_CONFIGURE_SHIMMER:
	    		if (resultCode == Activity.RESULT_OK) {
	    			if (data.getExtras().getString("Command").equals("Mag")){
	    				if (mShimmerDevice1.getStreamingStatus()){
	    					mShimmerDevice1.stopStreaming();
	    					mShimmerDevice1.enableLowPowerMag(data.getExtras().getBoolean("Enable"));
	    					mShimmerDevice1.startStreaming();
	    				} else {
	    					mShimmerDevice1.enableLowPowerMag(data.getExtras().getBoolean("Enable"));
	    				}
	    			} else if (data.getExtras().getString("Command").equals("Gyro")) {
	    				mShimmerDevice1.enableOnTheFlyGyroCal(data.getExtras().getBoolean("Enable"), 100, 1.2);
	    			}
	            }
	            break;
	    	}
	    }
		
		public static double round(double unrounded, int precision, int roundingMode)
		{
		    BigDecimal bd = new BigDecimal(unrounded);
		    BigDecimal rounded = bd.setScale(precision, roundingMode);
		    return rounded.doubleValue();
		}
		
		
		public double[] quattoeuler(double q0, double q1 , double q2, double q3){
			double[] euler = new double[3];

					euler[0]=Math.atan2(2*(q0*q1+q2*q3),(1-(2*(q1*q1+q2*q2))));
					euler[0]= Double.parseDouble(new DecimalFormat("#.#").format(euler[0]/Math.PI*180));
					euler[1]=Double.parseDouble(new DecimalFormat("#.#").format(Math.asin(2*(q0*q2-q3*q1))));
					euler[1]= Double.parseDouble(new DecimalFormat("#.#").format(euler[1]/Math.PI*180));
					euler[2]=Math.atan2(2*(q0*q3+q1*q2),1-(2*(q2*q2+q3*q3)));
					euler[2]= Double.parseDouble(new DecimalFormat("#.#").format(euler[2]/Math.PI*180));
					return euler;
		}
		
}