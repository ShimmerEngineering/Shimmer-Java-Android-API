package com.shimmerresearch.multishimmer;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import com.shimmerresearch.multishimmerplay.R;

public class ConfigureActivity extends Activity{
	// Return Intent extra
    public static String mDone = "Done";
	private int mReturnEnabledSensors = 0;
	
	private final int SENSOR_ACCEL=0x80;
	private final int SENSOR_GYRO=0x40;
	private final int SENSOR_MAG=0x20;
	private final int SENSOR_ECG=0x10;
	private final int SENSOR_EMG=0x08;
	private final int SENSOR_GSR=0x04;
	private final int SENSOR_EXP_BOARD_A7=0x02;
	private final int SENSOR_EXP_BOARD_A0=0x01;
	private final int SENSOR_BRIDGE_AMP=0x8000;
	private final int SENSOR_HEART_RATE=0x4000;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	// Setup the window	    
    	setContentView(R.layout.configure);
 	   final CheckBox cboxGyro = (CheckBox) findViewById(R.id.checkBoxGyroscope);
 	   final CheckBox cboxAccel = (CheckBox) findViewById(R.id.checkBoxAccelerometer);
 	   final CheckBox cboxMag = (CheckBox) findViewById(R.id.checkBoxMagnetometer);
 	   final CheckBox cboxBA = (CheckBox) findViewById(R.id.checkBoxBridgeAmplifier);
 	   final CheckBox cboxECG = (CheckBox) findViewById(R.id.checkBoxECG);
 	   final CheckBox cboxEMG = (CheckBox) findViewById(R.id.checkBoxEMG);
 	   final CheckBox cboxGSR = (CheckBox) findViewById(R.id.checkBoxGSR);
 	   final CheckBox cboxHR = (CheckBox) findViewById(R.id.checkBoxHeartRate);
 	   final CheckBox cboxA7 = (CheckBox) findViewById(R.id.checkBoxExpBoardA7);
 	   final CheckBox cboxA0 = (CheckBox) findViewById(R.id.checkBoxExpBoardA0);
 	   
    	Button enableDone = (Button) findViewById(R.id.enable_sensors_done);
	    enableDone.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		//First run through the buttons
	    		   if (cboxGyro.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_GYRO;
	    		   }
	    		   if (cboxAccel.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_ACCEL;
	    		   }
	    		   if (cboxMag.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_MAG;
	    		   }
	    		   if (cboxBA.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_BRIDGE_AMP;
	    		   }
	    		   if (cboxECG.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_ECG;
	    		   }
	    		   if (cboxEMG.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_EMG;
	    		   }
	    		   if (cboxGSR.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_GSR;
	    		   }
	    		   if (cboxHR.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_HEART_RATE;
	    		   }
	    		   if (cboxA7.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_EXP_BOARD_A7;
	    		   }
	    		   if (cboxA0.isChecked()) {
	    			   mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_EXP_BOARD_A0;
	    		   }
	    		
	    		// Create the result Intent
	    		Intent intent = new Intent();
	            intent.putExtra(mDone, mReturnEnabledSensors);
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            mReturnEnabledSensors=0;
	    		finish();
	    	}
	    });
	    

	   cboxAccel.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxAccel.isChecked()){
	    			mReturnEnabledSensors=mReturnEnabledSensors|SENSOR_ACCEL;
	    		}
	    		
	     	}
	    });
	   
	   cboxGyro.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxGyro.isChecked() || cboxMag.isChecked()  )
	    		{
	    			cboxBA.setChecked(false);
	    			cboxGSR.setChecked(false);
	    			cboxECG.setChecked(false);
	    			cboxEMG.setChecked(false);
	    		} 
	    	}
	    });
	   
	   cboxMag.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxGyro.isChecked() || cboxMag.isChecked()  )
	    		{
	    			cboxBA.setChecked(false);
	    			cboxGSR.setChecked(false);
	    			cboxECG.setChecked(false);
	    			cboxEMG.setChecked(false);
	    		} 
	    	
	    	}
	    });
	   
	   cboxBA.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxBA.isChecked())
	    		{
	    			cboxGyro.setChecked(false);
	    			cboxMag.setChecked(false);
	    			cboxECG.setChecked(false);
	    			cboxEMG.setChecked(false);
	    			cboxGSR.setChecked(false);
	    		} 
	    	
	    	}
	    });
	   
	   cboxGSR.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxGSR.isChecked())
	    		{
	    			cboxGyro.setChecked(false);
	    			cboxMag.setChecked(false);
	    			cboxECG.setChecked(false);
	    			cboxEMG.setChecked(false);
	    			cboxBA.setChecked(false);
	    		}
	    	
	    	}
	    });
	   
	   cboxECG.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxECG.isChecked())
	    		{
	    			cboxGyro.setChecked(false);
	    			cboxMag.setChecked(false);
	    			cboxGSR.setChecked(false);
	    			cboxEMG.setChecked(false);
	    			cboxBA.setChecked(false);
	    		}
	    	
	    	}
	    });
	   
	   cboxEMG.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxEMG.isChecked())
	    		{
	    			cboxGyro.setChecked(false);
	    			cboxMag.setChecked(false);
	    			cboxGSR.setChecked(false);
	    			cboxECG.setChecked(false);
	    			cboxBA.setChecked(false);
	    		}
	    	
	    	}
	    });
	   
	   cboxHR.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxHR.isChecked())
	    		{
	    			cboxA0.setChecked(false);
	    			cboxA7.setChecked(false);
	    			}
	    	}
	    });
	   
	   cboxA0.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxA0.isChecked() || cboxA7.isChecked())
	    		{
	    			cboxHR.setChecked(false);
	    			}
	    	}
	    });
	   
	   cboxA7.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if (cboxA0.isChecked() || cboxA7.isChecked())
	    		{
	    			cboxHR.setChecked(false);
	    			}
	    	}
	    });
	   
	   
    }
    
	 
	/*
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	*/
}
