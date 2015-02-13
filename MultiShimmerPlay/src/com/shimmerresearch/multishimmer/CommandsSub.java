package com.shimmerresearch.multishimmer;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import com.shimmerresearch.multishimmerplay.R;

public class CommandsSub extends Activity{

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.shimmer_commands);

	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	    Bundle extras = getIntent().getExtras();
        String bluetoothAddress = extras.getString("Enabled_Sensors");
    	double mSamplingRateV = extras.getDouble("SamplingRate");
    	int mAccelerometerRangeV = extras.getInt("AccelerometerRange");
    	int mGSRRangeV = extras.getInt("GSRRange");
    	
    	String[] samplingRate = new String [] {"10","51.2","102.4","128","170.7","204.8","256","512"};
    	String[] accelRange = new String [] {"+/- 1.5g","+/- 6g"};
    	String[] gsrRange = new String [] {"10kOhm to 56kOhm","56kOhm to 220kOhm","220kOhm to 680kOhm","680kOhm to 4.7MOhm","Auto Range"};
    	
    	
    	
    	final ListView listViewSamplingRate = (ListView) findViewById(R.id.listView1);
        final ListView listViewAccelRange = (ListView) findViewById(R.id.listView2);
        final ListView listViewGsrRange = (ListView) findViewById(R.id.listView3);
        
        final TextView textViewCurrentSamplingRate = (TextView) findViewById(R.id.TextView4);
        final TextView textViewCurrentAccelRange = (TextView) findViewById(R.id.textView5);
        final TextView textViewCurrentGsrRange = (TextView) findViewById(R.id.textView6);
        
        textViewCurrentSamplingRate.setTextColor(Color.rgb(0, 135, 202));
        textViewCurrentAccelRange.setTextColor(Color.rgb(0, 135, 202));
        textViewCurrentGsrRange.setTextColor(Color.rgb(0, 135, 202));
        if (mSamplingRateV!=-1){
        	textViewCurrentSamplingRate.setText(Double.toString(mSamplingRateV));
        } else {
        	textViewCurrentSamplingRate.setText("");
        }
        
        if (mAccelerometerRangeV==0){
        	textViewCurrentAccelRange.setText("+/- 1.5g");
        }
        else if (mAccelerometerRangeV==3){
        	textViewCurrentAccelRange.setText("+/- 6g");
        } else {
        	textViewCurrentAccelRange.setText("");
        }
        
        if (mGSRRangeV==0) {
        	textViewCurrentGsrRange.setText("10kOhm to 56kOhm");
        } else if (mGSRRangeV==1) {
        	textViewCurrentGsrRange.setText("56kOhm to 220kOhm");
        } else if (mGSRRangeV==2) {
        	textViewCurrentGsrRange.setText("220kOhm to 680kOhm");
        } else if (mGSRRangeV==3) {
        	textViewCurrentGsrRange.setText("680kOhm to 4.7MOhm"); 
        } else if (mGSRRangeV==4) {
        	textViewCurrentGsrRange.setText("Auto Range");
        } else {
        	textViewCurrentGsrRange.setText("");
        }
        
      
        
    	ArrayList<String> samplingRateList = new ArrayList<String>();  
    	samplingRateList.addAll( Arrays.asList(samplingRate) );  
        ArrayAdapter<String> sR = new ArrayAdapter<String>(this, R.layout.commands_name,samplingRateList);
    	listViewSamplingRate.setAdapter(sR);
    	
    	ArrayList<String> accelRangeList = new ArrayList<String>();  
    	accelRangeList.addAll( Arrays.asList(accelRange) );  
        ArrayAdapter<String> sR2 = new ArrayAdapter<String>(this, R.layout.commands_name,accelRangeList);
    	listViewAccelRange.setAdapter(sR2);
    	
    	ArrayList<String> gsrRangeList = new ArrayList<String>();  
    	gsrRangeList.addAll( Arrays.asList(gsrRange) );  
        ArrayAdapter<String> sR3 = new ArrayAdapter<String>(this, R.layout.commands_name,gsrRangeList);
    	listViewGsrRange.setAdapter(sR3);
    	
    	
    	
    	
    	Button buttonToggleLED = (Button) findViewById(R.id.button1);
	    
    	buttonToggleLED.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
	            intent.putExtra("ToggleLED", true);
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
        	
        });
    	
    	listViewSamplingRate.setOnItemClickListener(new AdapterView.OnItemClickListener() {

  		  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

  		    Object o = listViewSamplingRate.getItemAtPosition(position);
  		    Log.d("Shimmer",o.toString());
  		    Intent intent = new Intent();
	            intent.putExtra("SamplingRate",Double.valueOf(o.toString()).doubleValue());
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
  		    
  		  }
  		});
  	
  	listViewAccelRange.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		    Object o = listViewAccelRange.getItemAtPosition(position);
		    Log.d("Shimmer",o.toString());
		    int accelRange=0;
		    if (o.toString()=="+/- 1.5g"){
		    	accelRange=0;
		    } else if (o.toString()=="+/- 6g"){
		    	accelRange=3;
		    }
		    Intent intent = new Intent();
	            intent.putExtra("AccelRange",accelRange);
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
		    
		  }
		});
  	
  	
  	listViewGsrRange.setOnItemClickListener(new AdapterView.OnItemClickListener() {

  		  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
  			
  		    Object o = listViewGsrRange.getItemAtPosition(position);
  		    Log.d("Shimmer",o.toString());
  		    int gsrRange=0;
  		    if (o.toString()=="10kOhm to 56kOhm"){
  		    	gsrRange=0;
  		    } else if (o.toString()=="56kOhm to 220kOhm"){
  		    	gsrRange=1;
  		    } else if (o.toString()=="220kOhm to 680kOhm"){
  		    	gsrRange=2;
  		    } else if (o.toString()=="680kOhm to 4.7MOhm"){
  		    	gsrRange=3;
  		    } else if (o.toString()=="Auto Range"){
  		    	gsrRange=4;
  		    }
  		    Intent intent = new Intent();
	            intent.putExtra("GSRRange",gsrRange);
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
  		    
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
