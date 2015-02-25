package com.shimmerresearch.multishimmer;




import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import com.shimmerresearch.multishimmerplay.R;

public class LoggingActivity extends Activity{
	
	 CheckBox mEnableLoggingCB;
	 CheckBox mDisableLoggingCB;
	
	 public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.logging_enable);
	    mEnableLoggingCB = (CheckBox) findViewById(R.id.checkEnableLog);
	    mDisableLoggingCB = (CheckBox) findViewById(R.id.checkDisableLog);
		    
	    Bundle extras = getIntent().getExtras();
        boolean enableLogging = extras.getBoolean("EnableLogging");
        if (enableLogging==true){
        	mDisableLoggingCB.setChecked(false);
        	mEnableLoggingCB.setChecked(true);
        } else {
        	mDisableLoggingCB.setChecked(true);
        	mEnableLoggingCB.setChecked(false);
        }
        
	    mEnableLoggingCB.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mEnableLoggingCB.isChecked()) {
					mDisableLoggingCB.setChecked(false);
					Intent intent = new Intent();
		            intent.putExtra("EnableLogging",true);
		            // Set result and finish this Activity
		            setResult(Activity.RESULT_OK, intent);
		            finish();
				}
			}
	    	
	    	
	    	
	    });
	    
	    mDisableLoggingCB.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mDisableLoggingCB.isChecked()) {
					mEnableLoggingCB.setChecked(false);
	    		    Intent intent = new Intent();
		            intent.putExtra("EnableLogging",false);
		            // Set result and finish this Activity
		            setResult(Activity.RESULT_OK, intent);
		            finish();
				} 
			}
	    	
	    	
	    	
	    });
	    
		
	
	}
	
}
