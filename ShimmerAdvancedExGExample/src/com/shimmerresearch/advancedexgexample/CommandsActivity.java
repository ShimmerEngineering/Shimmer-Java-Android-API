package com.shimmerresearch.advancedexgexample;


import com.example.shimmeradvancedexgexample.R;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class CommandsActivity extends ServiceActivity{
	
	String mBluetoothAddress;
	Button buttonSampleRate;
	Button buttonGain;
	Button buttonReferenceElectrode;
	Button buttonLeadOffCurrent;
	Button buttonLeadOffComparator;
	Button buttonLeadOffDetection;
	Button buttonDone;
	
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.shimmer_commands);
        
    	Bundle extras = getIntent().getExtras();
        mBluetoothAddress = extras.getString("BluetoothAddress");
        
        final String[] samplingRate = new String [] {"8","16","51.2","102.4","128","204.8","256","512","1024","2048"};
        final String[] exgResolution = new String [] {"16 bits","24 bits"};
        buttonSampleRate = (Button) findViewById(R.id.buttonRate);
        buttonGain = (Button) findViewById(R.id.buttonGain);
        buttonReferenceElectrode = (Button) findViewById(R.id.buttonReferenceElectrode);
        buttonLeadOffComparator = (Button) findViewById(R.id.buttonLeadOffComparator);
        buttonLeadOffCurrent = (Button) findViewById(R.id.buttonLeadOffCurrent);
        buttonLeadOffDetection = (Button) findViewById(R.id.buttonLeadOffDetection);
        buttonDone = (Button) findViewById(R.id.buttonDone);
        
        
        //Getting the configuration from the device and setting the Button text
        
        double mSamplingRateV = mService.getSamplingRate(mBluetoothAddress);
        String rate = Double.toString(mSamplingRateV);
        buttonSampleRate.setText("SAMPLING RATE "+"\n"+"("+rate+" HZ)");
        

    	
    	int exgGain = mService.getEXGGain(mBluetoothAddress);
    	if(exgGain!=-1)
    		buttonGain.setText("ExG Gain"+"\n ("+Configuration.Shimmer3.ListOfExGGain[exgGain]+")");
    	else
    		buttonGain.setText("ExG Gain"+"\n (No gain)");
        
    	int referenceElectrode = mService.getExGReferenceElectrode(mBluetoothAddress);
    	if(referenceElectrode==0)
    		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (Fixed Potencial)");
    	else if(referenceElectrode==13)
    		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (Inverse Wilson CT)");
    	else if(referenceElectrode==3)
    		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (Inverse of Ch1)");
    	else
    		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (No Ref.)");
    	
    	int leadOffDetection = mService.getExGLeadOffDetectionMode(mBluetoothAddress);
    	if(leadOffDetection!=-1)
    		buttonLeadOffDetection.setText("Lead-Off Detection"+"\n ("+Configuration.Shimmer3.ListOfExGLeadOffDetection[leadOffDetection]+")");
    	else
    		buttonLeadOffDetection.setText("Lead-Off Detection"+"\n (No detec.)");
    	
    	int leadOffCurrent = mService.getExGLeadOffCurrent(mBluetoothAddress);
    	if(leadOffCurrent!=-1)
    		buttonLeadOffCurrent.setText("Lead-Off Current"+"\n ("+Configuration.Shimmer3.ListOfExGLeadOffCurrent[leadOffCurrent]+")");
    	else
    		buttonLeadOffCurrent.setText("Lead-Off Current"+"\n (No current)");
    	
    	int leadOffComparator = mService.getExGLeadOffComparatorTreshold(mBluetoothAddress);
    	if(leadOffComparator!=-1)
    		buttonLeadOffComparator.setText("Lead-Off Comparator"+"\n ("+Configuration.Shimmer3.ListOfExGLeadOffComparator[leadOffComparator]+")");
    	else
    		buttonLeadOffComparator.setText("Lead-Off Comparator"+"\n (No comp.)");
    	
    	
    	//Displaying configuration options and applying it
    	
        final AlertDialog.Builder dialogRate = new AlertDialog.Builder(this);		 
        dialogRate.setTitle("Sampling Rate").setItems(samplingRate, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",samplingRate[item]);
                		 double newRate = Double.valueOf(samplingRate[item]);
                		 ServiceActivity.mService.writeSamplingRate(mBluetoothAddress, newRate);
                		 Toast.makeText(getApplicationContext(), "Sample rate changed. New rate = "+newRate+" Hz", Toast.LENGTH_SHORT).show();
                		 buttonSampleRate.setText("Sampling Rate "+"\n"+"("+newRate+" HZ)");
                }
        });
    	
    	buttonSampleRate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub		        
		        dialogRate.show();
			}
		});
    	
    	
    	
    	final AlertDialog.Builder dialogGain = new AlertDialog.Builder(this);
    	dialogGain.setTitle("ExG Gain").setItems(Configuration.Shimmer3.ListOfExGGain, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGGain[arg1]);
				String newGain = Configuration.Shimmer3.ListOfExGGain[arg1];
				ServiceActivity.mService.writeExGGain(mBluetoothAddress, arg1);
				Toast.makeText(getApplicationContext(), "Gain changed. New Gain = "+newGain, Toast.LENGTH_SHORT).show();
				buttonGain.setText("ExG Gain "+"\n ("+newGain+")");
			}
		});
    	
    	buttonGain.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialogGain.show();
			}
		});
    	
    	
    	final String [] listOfReference;
    	if(ServiceActivity.mService.isEXGUsingECG16Configuration(mBluetoothAddress) ||
    			ServiceActivity.mService.isEXGUsingECG24Configuration(mBluetoothAddress))
    		listOfReference = Configuration.Shimmer3.ListOfECGReferenceElectrode;
    	else
    		listOfReference = Configuration.Shimmer3.ListOfEMGReferenceElectrode;
    		
    	final AlertDialog.Builder dialogReference = new AlertDialog.Builder(this);
    	dialogReference.setTitle("ExG Reference Electrode").setItems(listOfReference, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.d("Shimmer ",listOfReference[arg1]);
				String newReference = listOfReference[arg1];
				int reference = 0;
				if(ServiceActivity.mService.isEXGUsingECG16Configuration(mBluetoothAddress) ||
		    			ServiceActivity.mService.isEXGUsingECG24Configuration(mBluetoothAddress)){
					if(listOfReference[arg1].equals("Fixed Potential"))
						reference = 0;
					else
						reference = 13;
				}
				else{
					if(listOfReference[arg1].equals("Fixed Potential"))
						reference = 0;
					else
						reference = 3;
				}
				ServiceActivity.mService.writeExGReferenceElectrode(mBluetoothAddress, reference);
				Toast.makeText(getApplicationContext(), "Referecen electrode changed. New reference electrode = "+newReference, Toast.LENGTH_SHORT).show();
				buttonReferenceElectrode.setText("Ref. Electrode "+"\n ("+newReference+")");
			}
		});
    	
    	buttonReferenceElectrode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialogReference.show();
			}
		});
    	
    	
    	
    	final AlertDialog.Builder dialogLeadOffDetection = new AlertDialog.Builder(this);
    	dialogLeadOffDetection.setTitle("Lead-Off Detection Mode").setItems(Configuration.Shimmer3.ListOfExGLeadOffDetection, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGLeadOffDetection[arg1]);
				String newDetection = Configuration.Shimmer3.ListOfExGLeadOffDetection[arg1];
				ServiceActivity.mService.writeExGLeadOffDetectionMode(mBluetoothAddress, arg1);
				Toast.makeText(getApplicationContext(), "Lead-Off Detection changed. New Lead-Off Detection = "+newDetection, Toast.LENGTH_SHORT).show();
				buttonLeadOffDetection.setText("Lead-Off Detection "+"\n ("+newDetection+")");
			}
		});
    	
    	buttonLeadOffDetection.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialogLeadOffDetection.show();
			}
		});
    	
	
	
		final AlertDialog.Builder dialogLeadOffCurrent = new AlertDialog.Builder(this);
		dialogLeadOffCurrent.setTitle("Lead-Off Current").setItems(Configuration.Shimmer3.ListOfExGLeadOffCurrent, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGLeadOffCurrent[arg1]);
				String newCurrent = Configuration.Shimmer3.ListOfExGLeadOffCurrent[arg1];
				ServiceActivity.mService.writeExGLeadOffDetectionCurrent(mBluetoothAddress, arg1);
				Toast.makeText(getApplicationContext(), "Lead-Off Current changed. New Lead-Off Current = "+newCurrent, Toast.LENGTH_SHORT).show();
				buttonLeadOffCurrent.setText("Lead-Off Current "+"\n ("+newCurrent+")");
			}
		});
		
		buttonLeadOffCurrent.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialogLeadOffCurrent.show();
			}
		});
		
		
		
		final AlertDialog.Builder dialogLeadOffComparator = new AlertDialog.Builder(this);
		dialogLeadOffComparator.setTitle("Lead-Off Comparator Threshold").setItems(Configuration.Shimmer3.ListOfExGLeadOffComparator, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGLeadOffComparator[arg1]);
				String newComparator = Configuration.Shimmer3.ListOfExGLeadOffComparator[arg1];
				ServiceActivity.mService.writeExGLeadOffDetectionCurrent(mBluetoothAddress, arg1);
				ServiceActivity.mService.writeExGLeadOffDetectionComparatorTreshold(mBluetoothAddress, arg1);
				Toast.makeText(getApplicationContext(), "Lead-Off Comparator changed. New Lead-Off Comparator = "+newComparator, Toast.LENGTH_SHORT).show();
				buttonLeadOffComparator.setText("Lead-Off Comparator "+"\n ("+newComparator+")");
			}
		});
		
		buttonLeadOffComparator.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialogLeadOffComparator.show();
			}
		});
		
		
		buttonDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		

	}

}
