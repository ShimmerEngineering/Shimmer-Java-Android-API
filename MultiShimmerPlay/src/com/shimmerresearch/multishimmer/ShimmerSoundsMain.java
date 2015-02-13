package com.shimmerresearch.multishimmer;

import java.util.ArrayList;
import java.util.Arrays;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.shimmerresearch.multishimmer.*;
import com.shimmerresearch.multishimmerplay.R;
import com.shimmerresearch.service.MultiShimmerPlayService;
import com.shimmerresearch.service.MultiShimmerPlayService.LocalBinder;


public class ShimmerSoundsMain extends Activity{
	String mCurrentDevice=null;
	Button mButtonConnect;
	Button mButtonCommand;
	int mCurrentSlot=-1;
	int mCurrentSourcePosition=-1;
	int mCurrentPosition=-1;
	boolean mMainSoundLV=true;
	ListView mListViewSounds;
	ListView mListViewSounds2;
	MultiShimmerPlayService mService;
	private boolean mServiceBind=false;
	private String[] mChannelSounds = new String [] {"1","2","3"};
	private String[] mChannelSounds2 = new String [] {"1","2","3"};
	static final int REQUEST_SET_SOUND = 7;
	
	 public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.main_sounds);
		    Bundle extras = getIntent().getExtras();
		    mCurrentPosition = extras.getInt("Position");
		    
		    
		    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
	                android.R.layout.simple_list_item_1, mChannelSounds2);
		    mListViewSounds2 = (ListView)findViewById(R.id.listViewSounds2);
	        mListViewSounds2.setAdapter(adapter2);
		    
		    
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                android.R.layout.simple_list_item_1, mChannelSounds);
		    mListViewSounds = (ListView)findViewById(R.id.listViewSounds);
	        mListViewSounds.setAdapter(adapter);
	        
	        mListViewSounds.setOnItemClickListener(new OnItemClickListener() {
	 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
	/*         		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
	 		mSoundID = mSoundPool.load(mFileNames[position], 1);*/
	 				mMainSoundLV = true;
	 				Intent intent = new Intent(ShimmerSoundsMain.this, ShimmerSetSound.class);
	 				startActivityForResult(intent, REQUEST_SET_SOUND);
	 				mCurrentSourcePosition=position;
	 				// return this values to the main page
				}
			});
	        
	        mListViewSounds2.setOnItemClickListener(new OnItemClickListener() {
	 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
	/*         		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
	 		mSoundID = mSoundPool.load(mFileNames[position], 1);*/
	 				mMainSoundLV = false;
	 				Intent intent = new Intent(ShimmerSoundsMain.this, ShimmerSetSound.class);
	 				startActivityForResult(intent, REQUEST_SET_SOUND);
	 				mCurrentSourcePosition=position;
	 				// return this values to the main page
				}
			});
	        
	 }
	 
	 	
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	switch (requestCode) {
	    	case REQUEST_SET_SOUND:
	    		if (resultCode == Activity.RESULT_OK) {
	    			if (mMainSoundLV)
	    			{
			    		String soundaddress = data.getExtras()
			            .getString(ShimmerSetSound.EXTRA_SOUND_ADDRESS);
			    		Log.d("Shimmer",soundaddress);
			    		mChannelSounds[mCurrentSourcePosition] = soundaddress;        
			            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mChannelSounds);
			            mListViewSounds.setAdapter(adapter);
			            mService.loadSound(soundaddress, mCurrentPosition, mCurrentSourcePosition);
	    			} else {
	    				String soundaddress = data.getExtras().getString(ShimmerSetSound.EXTRA_SOUND_ADDRESS);
			    		Log.d("Shimmer",soundaddress);
	    			    mChannelSounds2[mCurrentSourcePosition] = soundaddress;        
	    			    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mChannelSounds2);
	    			    mListViewSounds2.setAdapter(adapter);
	    			    mService.loadSound2(soundaddress, mCurrentPosition, mCurrentSourcePosition);
	    			}
		    	}
	    	break;
	    	}
	    }
	    
	    public void onPause(){
	  	  super.onPause();
	  	  
	  	Log.d("ShimmerH","MCA on Pause");
	  	  if(mServiceBind == true){
	  		  getApplicationContext().unbindService(mTestServiceConnection);
	  	  }
	  	 }
	    
	  public void onResume(){
	  	super.onResume();

	  	Intent intent=new Intent(ShimmerSoundsMain.this, MultiShimmerPlayService.class);
	  	Log.d("ShimmerH","MCA on Resume");
	  	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
	  }
	    
	    
	    private ServiceConnection mTestServiceConnection = new ServiceConnection() {

	      	public void onServiceConnected(ComponentName arg0, IBinder service) {
	      		// TODO Auto-generated method stub
	      
	      		LocalBinder binder = (LocalBinder) service;
	      		mService = binder.getService();
	      		Log.d("Shimmer","Connected on Commands, position:" + Integer.toString(mCurrentPosition));
	      		//update the view
	      		mServiceBind=true;
	      		if (mService.returnSoundPath(mCurrentPosition, 0)!=null){
	      			mChannelSounds[0]=mService.returnSoundPath(mCurrentPosition, 0);
	      		}
	      		if (mService.returnSoundPath(mCurrentPosition, 1)!=null){
	      			mChannelSounds[1]=mService.returnSoundPath(mCurrentPosition, 1);
	      		}
	      		if (mService.returnSoundPath(mCurrentPosition, 2)!=null){
	      			mChannelSounds[2]=mService.returnSoundPath(mCurrentPosition, 2);
	      		}
	      		
	      		if (mService.returnSoundPath(mCurrentPosition, 0)!=null){
	      			mChannelSounds[0]=mService.returnSoundPath(mCurrentPosition, 0);
	      		}
	      		if (mService.returnSoundPath(mCurrentPosition, 1)!=null){
	      			mChannelSounds[1]=mService.returnSoundPath(mCurrentPosition, 1);
	      		}
	      		if (mService.returnSoundPath(mCurrentPosition, 2)!=null){
	      			mChannelSounds[2]=mService.returnSoundPath(mCurrentPosition, 2);
	      		}
	      		
	      	}

	      	public void onServiceDisconnected(ComponentName arg0) {
	      		// TODO Auto-generated method stub
	      		mServiceBind=false;
	      	}
        };

}
