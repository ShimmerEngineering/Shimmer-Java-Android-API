package com.shimmerresearch.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

    public class myReceiver extends BroadcastReceiver {
    	
   
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
    		Bundle extras = arg1.getExtras();
    		if (extras != null) {
    			Log.d("Receiver", Double.toString(arg1.getDoubleExtra("AccelZ", 0)));
    		}
		} 
    } 