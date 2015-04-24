package com.shimmerresearch.gesturerecogexample;


import java.io.IOException;
import java.util.Collection;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.*;
import com.shimmerresearch.gesturerecogexample.MyService.LocalBinder;




public class ShimmerGestureRecogActivity extends Activity implements OnClickListener{
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	MyService mService;
	static Timer mTimer = new Timer();
	static MediaPlayer mp        = null;
	static int MusicId=0;
	static int MusicId2=0;
	String Baddress="";
	private static ImageView mImageView;
	private static boolean mRecordGesture1;
	private static boolean mRecordGesture2;
	private static double[] mGesture1xDouble;
	private static double[] mGesture1yDouble;
	private static double[] mGesture1zDouble;
	private static double[] mGesture2xDouble;
	private static double[] mGesture2yDouble;
	private static double[] mGesture2zDouble;
	private static double[] mGesture3xDouble;
	private static double[] mGesture3yDouble;
	private static double[] mGesture3zDouble;
	static Stack<Double> mGesture1xStack = new Stack<Double>();
	static Stack<Double> mGesture1yStack = new Stack<Double>();
	static Stack<Double> mGesture1zStack = new Stack<Double>();
	static Stack<Double> mGesture2xStack = new Stack<Double>();
	static Stack<Double> mGesture2yStack = new Stack<Double>();
	static Stack<Double> mGesture2zStack = new Stack<Double>();
	static Stack<Double> mGesture3xStack = new Stack<Double>();
	static Stack<Double> mGesture3yStack = new Stack<Double>();
	static Stack<Double> mGesture3zStack = new Stack<Double>();
	static Stack<Double> mGesturexRecogStack = new Stack<Double>();
	static Stack<Double> mGestureyRecogStack = new Stack<Double>();
	static Stack<Double> mGesturezRecogStack = new Stack<Double>();
	Button mStartRecordingGesture1=null;
	Button mStartRecordingGesture2=null;
	Button mStartRecordingGesture3=null;
	Button mStartRecogGesture=null;
	Button mStopRecogGesture=null;
	Button mCalculateDifference=null;
	private static TextView mTextX;
	private static TextView mTextY;
	private static TextView mTextZ;
	private static TextView mTextX2;
	private static TextView mTextY2;
	private static TextView mTextZ2;
	private static TextView mTextX3;
	private static TextView mTextY3;
	private static TextView mTextZ3;
	private static double disX;
	private static double disY;
	private static double disZ;
	private static double disX2;
	private static double disY2;
	private static double disZ2;
	private static double disX3;
	private static double disY3;
	private static double disZ3;
	static TextView mTextCorrect;
	static TextView mTextCorrect2;
	static TextView mTextCorrect3;
	private static GraphView mGraph;
	private static GraphView mGraphrec1;
	private static GraphView mGraphrec2;
	private static GraphView mGraphrec3;
	static boolean mRecordCanStart=false;
	static boolean mMovementTimerStarted=true;
	static boolean mEnableGestureRecog=false;
	static boolean mEnableGestureRecord1=false;
	static boolean mEnableGestureRecord2=false;
	static boolean mEnableGestureRecord3=false;
	private static boolean mNewValue=false;
    private static int mFinalDecisionGesture1=0;
    private static int mFinalDecisionGesture2=0;
    private static int mFinalDecisionGesture3=0;
    private static double mDTWlimits=0.12;
    private static int mCurrentPlaying=0;
    private static Context myContext;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.setTooLegacyObjectClusterSensorNames();
        setContentView(R.layout.activity_main);

        //ActionBar gets initiated
        ActionBar actionbar = getActionBar();
      //Tell the ActionBar we want to use Tabs.
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      //initiating both tabs and set text to it.
        ActionBar.Tab PlayerTab = actionbar.newTab().setText("Home");
        ActionBar.Tab StationsTab = actionbar.newTab().setText("Connect Shimmer");
        ActionBar.Tab DisconnectTab = actionbar.newTab().setText("Disconnect Shimmer");
 
     //create the two fragments we want to use for display content
        Fragment PlayerFragment = new AFragment();
        Fragment StationsFragment = new BFragment();
        Fragment DisconnectFragment = new CFragment();
 
    //set the Tab listener. Now we can listen for clicks.
        PlayerTab.setTabListener(new MyTabsListener(PlayerFragment));
        StationsTab.setTabListener(new MyTabsListener(StationsFragment));
        DisconnectTab.setTabListener(new MyTabsListener(DisconnectFragment));
 
   //add the two tabs to the actionbar
        actionbar.addTab(PlayerTab);
        actionbar.addTab(StationsTab);
        actionbar.addTab(DisconnectTab);
        

        mGraph = (GraphView)findViewById(R.id.graph);
        mGraphrec1 = (GraphView)findViewById(R.id.graphrec1);
        mGraphrec2 = (GraphView)findViewById(R.id.graphrec2);
        mGraphrec3 = (GraphView)findViewById(R.id.graphrec3);
        mStartRecordingGesture1=(Button)findViewById(R.id.button1);
        mStartRecogGesture=(Button)findViewById(R.id.button3);
        mStopRecogGesture=(Button)findViewById(R.id.button4);
        mStartRecordingGesture2=(Button)findViewById(R.id.button6);
        mStartRecordingGesture3=(Button)findViewById(R.id.button7);
        mImageView=(ImageView)findViewById(R.id.imageView1);
        mStartRecordingGesture1.setOnClickListener(this);    
        mStartRecordingGesture2.setOnClickListener(this);    
        mStartRecordingGesture3.setOnClickListener(this);    

        mStartRecogGesture.setOnClickListener(this);
        mStopRecogGesture.setOnClickListener(this);

        mImageView.setBackgroundColor(Color.BLACK);
        mp = MediaPlayer.create(this, R.raw.jew1);
        myContext=this;
        
        
        
	}

   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, ShimmerGestureRecogActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
  
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    class MyTabsListener implements ActionBar.TabListener {
    	public Fragment fragment;
    	 
    	public MyTabsListener(Fragment fragment) {
    	this.fragment = fragment;
    	}
    	 
  		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			Log.d("Shimmer","reSelected");
		}

		public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			Log.d("Shimmer","Selected");
			if (arg0.getText()=="Connect Shimmer"){
				Intent serverIntent = new Intent(ShimmerGestureRecogActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
		        
			}
			if (arg0.getText()=="Disconnect Shimmer"){
				getApplicationContext().unbindService(mTestServiceConnection);
				stopService(new Intent(ShimmerGestureRecogActivity.this, MyService.class));
			}
			
		}

		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			Log.d("Shimmer","unSelected");
	
		}
    	 
    	}
    
    public static class AFragment extends Fragment {
    	 
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
        	Log.d("Shimmer","InflateA");
            return inflater.inflate(R.layout.device_list, container, false);
           
        }
     
    }
    
    public static class BFragment extends Fragment {
   	 
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
        	Log.d("Shimmer","InflateB");
            return inflater.inflate(R.layout.activity_main, container, false);
        }
     
    }
    
    public static class CFragment extends Fragment {
      	 
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
        	Log.d("Shimmer","InflateB");
            return inflater.inflate(R.layout.activity_main, container, false);
        }
     
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {	
    	case REQUEST_CONNECT_SHIMMER:
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
            String address = data.getExtras()
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            Baddress=address;
           Log.d("Shimmer",address);
           Intent intent=new Intent(this, MyService.class).putExtra("DeviceID",address);
           startService(intent);
           getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
        }
        break;
    	}
    }
    private ServiceConnection mTestServiceConnection = new ServiceConnection() {

    	public void onServiceConnected(ComponentName arg0, IBinder service) {
    		// TODO Auto-generated method stub
    		Log.d("Shimmer","Connected");
    		LocalBinder binder = (LocalBinder) service;
    		mService = binder.getService();
    		mService.initialize(Baddress,mHandler,ShimmerGestureRecogActivity.this);
    	}

    	public void onServiceDisconnected(ComponentName arg0) {
    		// TODO Auto-generated method stub
    		
    	}
      };


public static final Handler mHandler = new Handler() {
      public void handleMessage(Message msg) {
          switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
          case Shimmer.MESSAGE_READ:
          	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
          	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
          	    if (objectCluster.mMyName=="RightArm"){
              	    Collection<FormatCluster> GyroXFormats = objectCluster.mPropertyCluster.get("Gyroscope X");  // first retrieve all the possible formats for the current sensor device
              	    int[] data=new int[3];
			 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(GyroXFormats,"CAL")); // retrieve the Calibrated data
			 	    	if (formatCluster != null){
			 	    		//Log.d("CalibratedData","GyroX: " + formatCluster.mData + " "+ formatCluster.mUnits);
			 	    		data[0]=(int)formatCluster.mData;
			 	    	}
			 	    	Collection<FormatCluster> GyroYFormats = objectCluster.mPropertyCluster.get("Gyroscope Y");  // first retrieve all the possible formats for the current sensor device
			 	    	formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(GyroYFormats,"CAL")); // retrieve the Calibrated data
		 	    		if (formatCluster != null){
				 	    	//Log.d("CalibratedData","GyroY: " + formatCluster.mData + " "+formatCluster.mUnits);
				 	    	data[1]=(int)formatCluster.mData;
		 	    		}
			 	    	Collection<FormatCluster> GyroZFormats = objectCluster.mPropertyCluster.get("Gyroscope Z");  // first retrieve all the possible formats for the current sensor device
			 	    	formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(GyroZFormats,"CAL")); // retrieve the Calibrated data
				 	    if (formatCluster != null){
				 			//Log.d("CalibratedData","GyroZ: " + formatCluster.mData + " "+formatCluster.mUnits);
				 			data[2]=(int)formatCluster.mData;
			 	    	}
				 		mGraph.setDataWithAdjustment(data,"Shimmer : " + objectCluster.mMyName,"800");
				 		
				 		
				 		//only start pushing data if the shimmer device has been stationary for 200ms
				 		//Log.d("Shimmer",Double.toString(data[0]) + " " + Double.toString(data[1]) + " " + Double.toString(data[2]));
				 		
				 	
					 		if (Math.abs(data[0])<10 && Math.abs(data[1])<10 && Math.abs(data[2])<10 && mMovementTimerStarted==false) {
					 			mTimer.cancel();
					 			mMovementTimerStarted=true;
					 			shimmerTimer(200);
					 			
					 			//Log.d("Shimmer","StartTimer");
					 		} else if (((Math.abs(data[0])>10 || Math.abs(data[1])>10 || Math.abs(data[2])>10))){
					 			mMovementTimerStarted=false;
					 			mRecordCanStart=false;
					 			mTimer.cancel();
					 			mRecordGesture2=true;
					 			
					 			//Log.d("Shimmer","StopTimer");
					 		} 
					 		
					 		if (mRecordGesture2==true && mEnableGestureRecog==true){
					 				//Log.d("Shimmer","logging");
						 			mGesturexRecogStack.push((double) data[0]);
						 			mGestureyRecogStack.push((double) data[1]);
						 			mGesturezRecogStack.push((double) data[2]);
						 			
					 		}
					 		
					 		if (mRecordGesture2==true && mEnableGestureRecord1==true){
				 				Log.d("Shimmer","logging");
				 				mGesture1xStack.push((double) data[0]);
					 			mGesture1yStack.push((double) data[1]);
					 			mGesture1zStack.push((double) data[2]);
					 			
					 			mGraphrec1.setDataWithAdjustment(data,"Gesture 1","800");
					 		} 
					 		if (mRecordGesture2==true && mEnableGestureRecord2==true){
			 				Log.d("Shimmer","logging2");
				 				mGesture2xStack.push((double) data[0]);
					 			mGesture2yStack.push((double) data[1]);
					 			mGesture2zStack.push((double) data[2]);
					 			mGraphrec2.setDataWithAdjustment(data,"Gesture 2","800");
					 		}
					 		if (mRecordGesture2==true && mEnableGestureRecord3==true){
				 				Log.d("Shimmer","logging3");
				 				mGesture3xStack.push((double) data[0]);
					 			mGesture3yStack.push((double) data[1]);
					 			mGesture3zStack.push((double) data[2]);
					 			mGraphrec3.setDataWithAdjustment(data,"Gesture 3","800");
				 		}
				 		
				 		
				 		if (mNewValue==true && mEnableGestureRecog==true){
				 			mNewValue=false;

			 			      int x=0;
			 			      int y=0;
			 			      int z=0;
			 			      if (disX>mDTWlimits) {
			 			    	  x=1;

			 			      } else {

			 			      }
			 			      if (disY>mDTWlimits) {
			 			    	  y=1;
	
			 			      } else {

			 			      }
			 			     
			 			      if (disZ>mDTWlimits) {
			 			    	  z=1;
	
			 			      } else {

			 			      }
			 			      
			 			      if ((x+y+z)>=2)
			 			      {

			 			 
			 			    	 mFinalDecisionGesture1=1;
			 			   
			 			      } else {
			 		
			 			    	 mFinalDecisionGesture1=0;
			 			      }
			 			      ////////////
			 			    
			 			      x=0;
			 			      y=0;
			 			      z=0;
			 			      if (disX2>mDTWlimits) {
			 			    	  x=1;
			 			    	
			 			      } else {
			 			   
			 			      }
			 			      if (disY2>mDTWlimits) {
			 			    	  y=1;
			 			    	
			 			      } else {
			 			    	 
			 			      }
			 			     
			 			      if (disZ2>mDTWlimits) {
			 			    	  z=1;
			 			    	
			 			      } else {
			 			    	  
			 			      }
			 			      
			 			      if ((x+y+z)>=2)
			 			      {

			 			    	
			 			    	 mFinalDecisionGesture2=1;
			 			   
			 			      } else {
			 			
			 			    	 mFinalDecisionGesture2=0;
			 			      }
			 			      ////////////
			 			     
			 			     x=0;
			 			      y=0;
			 			      z=0;
			 			      if (disX3>mDTWlimits) {
			 			    	  x=1;
			 			    	 
			 			      } else {
			 		
			 			      }
			 			      if (disY3>mDTWlimits) {
			 			    	  y=1;
			 			    	
			 			      } else {
			
			 			      }
			 			     
			 			      if (disZ3>mDTWlimits) {
			 			    	  z=1;
			 			    	 
			 			      } else {
			 			    	  
			 			      }
			 			      
			 			      if ((x+y+z)>=2)
			 			      {

			 			    
			 			    	 mFinalDecisionGesture3=1;
			 			   
			 			      } else {
			 			    	 
			 			    	 mFinalDecisionGesture3=0;
			 			      }
			 			      
			 			      
			 			      
			 			
			 			
			 			//now calculate the final decision
			 			if ((disX+disY+disZ)>(disX2+disY2+disZ2) && (disX+disY+disZ)>(disX3+disY3+disZ3) && mFinalDecisionGesture1==1){
			 				
			 				if (mCurrentPlaying==1){
			 					mp = MediaPlayer.create(myContext, R.raw.jew2);
			 					Log.d("ShimmerPlay","G1" + Integer.toString(mCurrentPlaying));
			 				} else if (mCurrentPlaying==0){
			 					mp = MediaPlayer.create(myContext, R.raw.jew1);
			 					Log.d("ShimmerPlay","G1" + Integer.toString(mCurrentPlaying));
			 				}
			 				try {
								mp.prepare();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			 				mp.start();
			 				
			 				
			 				mImageView.setImageResource(R.drawable.play);
			 				Log.d("ShimmerPlay","G1");
			 			}else if ((disX2+disY2+disZ2)>(disX+disY+disZ) && (disX2+disY2+disZ2)>(disX3+disY3+disZ3) && mFinalDecisionGesture2==1){
			 				if(mp.isPlaying()==true){
				 				mp.stop();
				 				Log.d("ShimmerPlay","G2stop");
				 				mImageView.setImageResource(R.drawable.stop);
			 				}
			 				Log.d("ShimmerPlay","G2");
			 			}else if ((disX3+disY3+disZ3)>(disX+disY+disZ) && (disX3+disY3+disZ3)>(disX2+disY2+disZ2) && mFinalDecisionGesture3==1){
			 				if(mp.isPlaying()==true){
				 				mp.stop();
				 				Log.d("ShimmerPlay","G3stop");
			 				}
			 				if (mCurrentPlaying==1){
			 					mp = MediaPlayer.create(myContext, R.raw.jew1);
			 					mCurrentPlaying=0;
			 					Log.d("ShimmerPlay","G3" + Integer.toString(mCurrentPlaying));
			 				} else if (mCurrentPlaying==0){
			 					mp = MediaPlayer.create(myContext, R.raw.jew2);
			 					mCurrentPlaying=1;
			 					Log.d("ShimmerPlay","G3" + Integer.toString(mCurrentPlaying));
			 				}
			 				try {
								mp.prepare();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			 				
			 				mImageView.setImageResource(R.drawable.next);
			 				mp.start();
			 			}
			 			
			 			}
				 		
				 		
		 	    	}
          	}
              break;
              
          }
      }
  };
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("my.shimmer");
      
    }
    protected void onPause() {
    	super.onPause();

    }

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		 switch(arg0.getId()){ 
		 case R.id.button6:
			  mGesture2xStack.clear();
			  mGesture2yStack.clear();
			  mGesture2zStack.clear();
			//start recording gesture1
			  mEnableGestureRecord2=true;
			  mGraphrec2.clearGraph();
			 break;
		 case R.id.button7:
			 mGesture3xStack.clear();
			  mGesture3yStack.clear();
			  mGesture3zStack.clear();
			//start recording gesture1
			  mEnableGestureRecord3=true;
			  mGraphrec3.clearGraph();
			 break; 
		  case R.id.button1:
			  //first clear the stack
			  mGesture1xStack.clear();
			  mGesture1yStack.clear();
			  mGesture1zStack.clear();
			//start recording gesture1
			  mEnableGestureRecord1=true;
			  mGraphrec1.clearGraph();
		       break; 
		 
		  case R.id.button3: 
			  mGesture2xStack.clear();
			  mGesture2yStack.clear();
			  mGesture2zStack.clear();
			  mEnableGestureRecog=true;
		       break; 
		  case R.id.button4: 
			  mEnableGestureRecog=false;
		       break;
		  
		  }
	}
	
	public static void calculateDifference(){
		Log.d("Shimmer","0");
		//first convert stack to double
		  if (mGesture1xStack.size()>1){
			  mGesture1xDouble=convertStacktoDouble(mGesture1xStack);
			  mGesture1yDouble=convertStacktoDouble(mGesture1yStack);
			  mGesture1zDouble=convertStacktoDouble(mGesture1zStack);
		  }
		  if (mGesture2xStack.size()>1){
			  mGesture2xDouble=convertStacktoDouble(mGesture2xStack);
			  mGesture2yDouble=convertStacktoDouble(mGesture2yStack);
			  mGesture2zDouble=convertStacktoDouble(mGesture2zStack);
		  }
		  
		  if (mGesture3xStack.size()>1){
			  mGesture3xDouble=convertStacktoDouble(mGesture3xStack);
			  mGesture3yDouble=convertStacktoDouble(mGesture3yStack);
			  mGesture3zDouble=convertStacktoDouble(mGesture3zStack);
		  }
		  
		  Log.d("Shimmer","1");
		  if (mGesturexRecogStack.size()>0){
		  Instance instancegtx = new DenseInstance(mGesture1xDouble);
	      Instance instancegty = new DenseInstance(mGesture1yDouble);
		  Instance instancegtz = new DenseInstance(mGesture1zDouble);
		  Log.d("Shimmer","2");
		  Instance instance2x = new DenseInstance(convertStacktoDouble(mGesturexRecogStack));
	      Instance instance2y = new DenseInstance(convertStacktoDouble(mGestureyRecogStack));
	      Instance instance2z = new DenseInstance(convertStacktoDouble(mGesturezRecogStack));
	      DTWSimilarity dtw=new DTWSimilarity();
	      Log.d("Shimmer","3");
	      
	      ///////
	      disX=dtw.measure(instancegtx, instance2x);
	      disY=dtw.measure(instancegty, instance2y);
	      disZ=dtw.measure(instancegtz, instance2z);
	      Log.d("ShimmerXDTW",Double.toString(disX));
	      Log.d("ShimmerYDTW",Double.toString(disY));
	      Log.d("ShimmerZDTW",Double.toString(disZ));
	      
	      //////
	      instancegtx = new DenseInstance(mGesture2xDouble);
	      instancegty = new DenseInstance(mGesture2yDouble);
		  instancegtz = new DenseInstance(mGesture2zDouble);
		  disX2=dtw.measure(instancegtx, instance2x);
	      disY2=dtw.measure(instancegty, instance2y);
	      disZ2=dtw.measure(instancegtz, instance2z);
	      
	      //////
	      instancegtx = new DenseInstance(mGesture3xDouble);
	      instancegty = new DenseInstance(mGesture3yDouble);
		  instancegtz = new DenseInstance(mGesture3zDouble);
		  disX3=dtw.measure(instancegtx, instance2x);
	      disY3=dtw.measure(instancegty, instance2y);
	      disZ3=dtw.measure(instancegtz, instance2z);
	      
	      
	     
		  }
		  mNewValue=true;
	}
	
	public static double[] convertStacktoDouble(Stack<Double> stack){
		int size=stack.size();
		double[] returnDouble=new double[size];
   		for (int i=0;i<size;i++) {
   			returnDouble[size-1-i]=(double) stack.pop();
   		}
   		return returnDouble;
	}
    
	 public synchronized static void shimmerTimer(int milliseconds) {
	        mTimer = new Timer();
	        mTimer.schedule(new TimerTask(){
	        	  public void run(){
	        		  mRecordGesture2=false;
	        		  if (mEnableGestureRecord1==true){
	        			  mEnableGestureRecord1=false;
	        		  }
	        		  if (mEnableGestureRecord2==true){
	        			  mEnableGestureRecord2=false;
	        		  }
	        		  if (mEnableGestureRecord3==true){
	        			  mEnableGestureRecord3=false;
	        		  }
	        		  Log.d("Shimmer","No Movement");
	        		  calculateDifference();
	        		  }
	        		  }, milliseconds);
		}
	    
	  
 
	
}
