//v0.1
package com.shimmerresearch.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.tools.Logging;

public class MultiShimmerPlayService extends Service {
	private static final String TAG = "MyService";
    public Shimmer shimmerDevice1 = null;
    public Logging shimmerLog1 = null;
    private boolean mEnableLogging=false;
	private BluetoothAdapter mBluetoothAdapter = null;
	private final IBinder mBinder = new LocalBinder();
	public HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(7);
	public HashMap<String, Logging> mLogShimmer = new HashMap<String, Logging>(7);
	private Handler mHandlerGraph=null;
	private boolean mGraphing=false;
	private String mGraphBluetoothAddress=""; //used to filter the msgs passed to the handler
	private String mLogFileName="Default";
	private int[][] mSoundIDArray = new int[7][3];
	private int[][] mSoundIDArray2 = new int[7][3];
	private int[][] mSoundIDStreamArray = new int[7][3];
	private String[][] mSoundPathArray = new String[7][3];
	private String[][] mSoundPathArray2 = new String[7][3];
	private String[][] mActivatedSensorNamesArray = new String [7][3];
	private boolean[][] mTriggerResting = new boolean [7][3];
	private boolean[][] mTriggerHitDetected = new boolean [7][3];
	private double[][] mMaxData = new double[7][3];
	private SoundPool mSoundPool;
	DescriptiveStatistics mDataBuffer= new DescriptiveStatistics(5);
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		mSoundPool = new SoundPool(7, AudioManager.STREAM_MUSIC, 0);
		for (boolean[] row : mTriggerResting)
		    Arrays.fill(row, true);
		for (boolean[] row : mTriggerHitDetected)
		    Arrays.fill(row, true);
		for (double[] row: mMaxData)
			Arrays.fill(row, 0);
		for (int[] row: mSoundIDArray)
			Arrays.fill(row, -1);
		for (int[] row: mSoundIDStreamArray)
			Arrays.fill(row, -1);
		Log.d(TAG, "onCreate");
	}

	public class LocalBinder extends Binder {
        public MultiShimmerPlayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MultiShimmerPlayService.this;
        }
    }
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
		
	}
	
	public void disconnectAllDevices(){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
		mMultiShimmer.clear();
		mLogShimmer.clear();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();

		Log.d(TAG, "onStart");

	}
	
	public void connectShimmer(String bluetoothAddress,String selectedDevice){
		Log.d("Shimmer","net Connection");
		Shimmer shimmerDevice=new Shimmer(this, mHandler,selectedDevice,false);
		mMultiShimmer.remove(bluetoothAddress);
		if (mMultiShimmer.get(bluetoothAddress)==null){
			mMultiShimmer.put(bluetoothAddress,shimmerDevice); 
			((Shimmer) mMultiShimmer.get(bluetoothAddress)).connect(bluetoothAddress,"default");
		}
	}
	
	public void onStop(){
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
	}
	
	public void toggleAllLEDS(){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.toggleLed();
			}
		}
	}
	
	
	  public final Handler mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
	            case Shimmer.MESSAGE_READ:
	            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
	            	   if (mEnableLogging==true){
		            	   shimmerLog1= (Logging)mLogShimmer.get(objectCluster.mBluetoothAddress);
		            	   if (shimmerLog1!=null){
		            		   shimmerLog1.logData(objectCluster);
		            	   } else {
		            			char[] bA=objectCluster.mBluetoothAddress.toCharArray();
		            			Logging shimmerLog;
		            			if (mLogFileName.equals("Default")){
		            				shimmerLog=new Logging(Long.toString(System.currentTimeMillis()) + " Device" + bA[12] + bA[13] + bA[15] + bA[16],"\t");
		            			} else {
		            				shimmerLog=new Logging(Long.toString(System.currentTimeMillis()) + mLogFileName,"\t");
		            			}
		            			mLogShimmer.remove(objectCluster.mBluetoothAddress);
		            			if (mLogShimmer.get(objectCluster.mBluetoothAddress)==null){
		            				mLogShimmer.put(objectCluster.mBluetoothAddress,shimmerLog); 
		            			}
		            	   }
	            	   }
	            	   
	            	   if (mGraphing==true && objectCluster.mBluetoothAddress.equals(mGraphBluetoothAddress)){
	            		   Log.d("ShimmerGraph","Sending");
	            		   mHandlerGraph.obtainMessage(Shimmer.MESSAGE_READ, objectCluster)
               	        .sendToTarget();
	            	   } 
	            	   
	            	   
	            	   //start sound playing here
	            	   Log.d("ShimmerPlay",objectCluster.mMyName);
	            	   
	            	   //first get the position
	            	   int mPosition = Integer.parseInt(objectCluster.mMyName);
	            	   long enabledSensors = getEnabledSensors(objectCluster.mBluetoothAddress);
	            	   //Multiset<String> listofKeys = objectCluster.mPropertyCluster.keys();
	            	   if ((enabledSensors & Shimmer.SENSOR_GYRO) >0){
	            	   Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(mActivatedSensorNamesArray[mPosition][0]);  // first retrieve all the possible formats for the current sensor device
	            	   FormatCluster formatCluster;
	            	   int maxData = 600; 
	            	   int dataLimit = 200;
	            	   if (ofFormats != null) { 
		            	   formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
		            	   if (formatCluster!=null){
		            		// for position 6 and 7 stop the 
		 	    				
		 	    				if ((formatCluster.mData)<dataLimit){
		 	    					mTriggerResting[mPosition][0]=true;
		 	    				}
		 	    				
		 	    				if ((formatCluster.mData)>dataLimit && mTriggerResting[mPosition][0] == true){ // if a hit is detected
									mTriggerHitDetected[mPosition][0]=true;
		 	    				}
								
								if (mTriggerHitDetected[mPosition][0]==true){ //hit is detected this monitor the intensity
									  if (mMaxData[mPosition][0]<formatCluster.mData)
									  {
										  mMaxData[mPosition][0]=formatCluster.mData; //obtain the max intensity
									  }
								      
								        if ((mMaxData[mPosition][0]-formatCluster.mData>0 || mMaxData[mPosition][0]>maxData) && mTriggerResting[mPosition][0] == true) {
								          
								            mTriggerHitDetected[mPosition][0]=false;
								            mTriggerResting[mPosition][0]=false;				            
								            
								            if (mSoundIDStreamArray[mPosition][0]!=-1){
			    								mSoundPool.stop(mSoundIDStreamArray[mPosition][0]);
			    							}
								            
			    							Log.d("MaxData","GyroZ:  " + Double.toString(mMaxData[mPosition][0]) );
			    							if (mSoundIDArray[mPosition][0]!=-1){
			    								mSoundIDStreamArray[mPosition][0] = mSoundPool.play(mSoundIDArray[mPosition][0], (float)0.75, 0.75f, 1, 0, 1);
			    							}
			    							mMaxData[mPosition][0]=0;
								        }
									}
		            	   		}
						}
	            	   
	            	   
	            	   ofFormats = objectCluster.mPropertyCluster.get(mActivatedSensorNamesArray[mPosition][1]);  // first retrieve all the possible formats for the current sensor device
	            	   if (ofFormats != null) { 
	            	   formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
	            	   if (formatCluster!=null){
	            		
	 	    				maxData = 600; 
	 	    				dataLimit = 200;
	 	    				if ((formatCluster.mData)<dataLimit){
	 	    					mTriggerResting[mPosition][1]=true;
	 	    				}
	 	    				
	 	    				if ((formatCluster.mData)>dataLimit && mTriggerResting[mPosition][1] == true){ // if a hit is detected
								mTriggerHitDetected[mPosition][1]=true;
	 	    				}
							
							if (mTriggerHitDetected[mPosition][1]==true){ //hit is detected this monitor the intensity
								  if (mMaxData[mPosition][1]<formatCluster.mData)
								  {
									  mMaxData[mPosition][1]=formatCluster.mData; //obtain the max intensity
								  }
							      
							        if ((mMaxData[mPosition][1]-formatCluster.mData>0 || mMaxData[mPosition][1]>maxData) && mTriggerResting[mPosition][1] == true) {
							          
							            mTriggerHitDetected[mPosition][1]=false;
							            mTriggerResting[mPosition][1]=false;				            
							            
							            if (mSoundIDStreamArray[mPosition][1]!=-1){
		    								mSoundPool.stop(mSoundIDStreamArray[mPosition][1]);
		    							}
							            
		    							Log.d("MaxData","GyroZ:  " + Double.toString(mMaxData[mPosition][1]) );
		    							if (mSoundIDArray[mPosition][1]!=-1){
		    								mSoundIDStreamArray[mPosition][1] = mSoundPool.play(mSoundIDArray[mPosition][1], (float)0.75, 0.75f, 1, 0, 1);
		    							}
		    							mMaxData[mPosition][1]=0;
							        }
							        
								}
							
							ofFormats = objectCluster.mPropertyCluster.get(mActivatedSensorNamesArray[mPosition][2]);  // first retrieve all the possible formats for the current sensor device
			            	   if (ofFormats != null) { 
			            		   formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
			            		   if (formatCluster!=null){
			            	   		if ((formatCluster.mData)<dataLimit){
			 	    					mTriggerResting[mPosition][2]=true;
			 	    				}
			 	    				
			 	    				if ((formatCluster.mData)>dataLimit && mTriggerResting[mPosition][2] == true){ // if a hit is detected
										mTriggerHitDetected[mPosition][2]=true;
			 	    				}
									
									if (mTriggerHitDetected[mPosition][2]==true){ //hit is detected this monitor the intensity
										  if (mMaxData[mPosition][2]<formatCluster.mData)
										  {
											  mMaxData[mPosition][2]=formatCluster.mData; //obtain the max intensity
										  }
									      
									        if ((mMaxData[mPosition][2]-formatCluster.mData>0 || mMaxData[mPosition][2]>maxData) && mTriggerResting[mPosition][2] == true) {
									          
									            mTriggerHitDetected[mPosition][2]=false;
									            mTriggerResting[mPosition][2]=false;				            
									            
									            if (mSoundIDStreamArray[mPosition][2]!=-1){
				    								mSoundPool.stop(mSoundIDStreamArray[mPosition][2]);
				    							}
									            
				    							Log.d("MaxData","GyroZ:  " + Double.toString(mMaxData[mPosition][2]) );
				    							if (mSoundIDArray[mPosition][2]!=-1){
				    								
				    								if ((enabledSensors & Shimmer.SENSOR_MAG) ==0){
				    									mSoundIDStreamArray[mPosition][2] = mSoundPool.play(mSoundIDArray[mPosition][2], (float)0.75, 0.75f, 1, 0, 1);
				    								} else {
				    									Collection<FormatCluster> ofFormatsMag = objectCluster.mPropertyCluster.get("Magnetometer Z");
				    									formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormatsMag,"CAL"));
				    									if (formatCluster.mData<0){
				    										mSoundIDStreamArray[mPosition][2] = mSoundPool.play(mSoundIDArray[mPosition][2], (float)0.75, 0.75f, 1, 0, 1);
				    									} else if (mSoundIDArray2[mPosition][2]!=-1){
				    										mSoundIDStreamArray[mPosition][2] = mSoundPool.play(mSoundIDArray2[mPosition][2], (float)0.75, 0.75f, 1, 0, 1);
				    									}
				    								}
				    							}
				    							mMaxData[mPosition][2]=0;
									        }
									        
										}
									
			            	   
			            	   }
			            	}
	            	   }
	            	   }
	            	   } else if ((enabledSensors & Shimmer.SENSOR_GSR) >0) {
	            		   Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(mActivatedSensorNamesArray[mPosition][0]);  // first retrieve all the possible formats for the current sensor device
		            	   FormatCluster formatCluster;
		            	   if (ofFormats != null) { 
			            	   formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
			            	   if (formatCluster!=null){
		            		   	   if (mSoundIDArray[mPosition][0]!=-1){
		            		   		   if (mSoundIDStreamArray[mPosition][0]==-1){
				            			   mSoundIDStreamArray[mPosition][0] = mSoundPool.play(mSoundIDArray[mPosition][0], (float)0.75, 0.75f, 1, -1, 1);
		            		   		   } else {
		            		   			   double gsrData= formatCluster.mData;
		            		   			   mDataBuffer.addValue(gsrData);
		            		   			   if (mDataBuffer.getMean()<9){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], 1f, 1f);
		            		   			   } else if (mDataBuffer.getMean()<10){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], .9f, .9f);
		            		   			   } else if (mDataBuffer.getMean()<11){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], .8f, .8f);
		            		   			   } else if (mDataBuffer.getMean()<12){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], .7f, .7f);
		            		   			   } else if (mDataBuffer.getMean()<13){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], .6f, .6f);
		            		   			   } else if (mDataBuffer.getMean()<14){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], .3f, .3f);
		            		   			   } else if (mDataBuffer.getMean()<15){
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], .1f, .1f);
		            		   			   } else {
		            		   				   mSoundPool.setVolume( mSoundIDStreamArray[mPosition][0], 0.0f, 0.0f);
		            		   			   }
		            		   		   }
				            	   }
			            	   }
		            	   }
	            	   }
	            	   
	            	}
	                break;
	                 case Shimmer.MESSAGE_TOAST:
	                	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
	                	Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),
	                            Toast.LENGTH_SHORT).show();
	                	if (msg.getData().getString(Shimmer.TOAST).equals("Device connection was lost")){
	                		
	                	}
	                break;
	                 case Shimmer.MESSAGE_STATE_CHANGE:
	                	 Intent intent = new Intent("com.shimmerresearch.service.MultiShimmerService");
	                	 Log.d("ShimmerGraph","Sending");
	            		 if (mGraphing==true){  
	            			 mHandlerGraph.obtainMessage(Shimmer.MESSAGE_STATE_CHANGE, msg.arg1, -1, msg.obj).sendToTarget();
	            		 }
	                	 switch (msg.arg1) {
	                     case Shimmer.STATE_CONNECTED:
	                    	 Log.d("Shimmer",((ObjectCluster) msg.obj).mBluetoothAddress + "  " + ((ObjectCluster) msg.obj).mMyName);
	                    	 
	                    	 intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).mBluetoothAddress );
	                    	 intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).mMyName );
	                    	 intent.putExtra("ShimmerState",Shimmer.STATE_CONNECTED);
	                    	 sendBroadcast(intent);

	                         break;
	                     case Shimmer.STATE_CONNECTING:
	                    	 intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).mBluetoothAddress );
	                    	 intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).mMyName );
	                    	 intent.putExtra("ShimmerState",Shimmer.STATE_CONNECTING);	                        
	                         break;
	                     case Shimmer.STATE_NONE:
	                    	 intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).mBluetoothAddress );
	                    	 intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).mMyName );
	                    	 intent.putExtra("ShimmerState",Shimmer.STATE_NONE);
	                    	 sendBroadcast(intent);
	                         break;
	                     }
	                	 
	                break;

                 case Shimmer.MESSAGE_STOP_STREAMING_COMPLETE:
                	 String address =  msg.getData().getString("Bluetooth Address");
                	 boolean stop  =  msg.getData().getBoolean("Stop Streaming");
                	 if (stop==true ){
                		 closeAndRemoveFile(address);
                	 }
                	break;
	            }
	        }
	    };


    public void stopStreamingAllDevices() {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.stopStreaming();
				if (mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] != -1){
					mSoundPool.stop(mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0]);
					mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] = -1;
				}
				if (mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][1] != -1){
					mSoundPool.stop(mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][1]);
					mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] = -1;
				}
				if (mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][2] != -1){
					mSoundPool.stop(mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][2]);
					mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] = -1;
				}
				
			}
		}
	}
	    
	public void startStreamingAllDevices() {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.startStreaming();
			}
		}
	}
	
	public void startStreamingAllDevicesGetSensorNames() {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.startStreaming();
				int mPosition = Integer.parseInt(stemp.getDeviceName());
				long mEnabledSensors = stemp.getEnabledSensors();
				if ((mEnabledSensors & Shimmer.SENSOR_ACCEL)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "Accelerometer X";
        			mActivatedSensorNamesArray[mPosition][1] = "Accelerometer Y";
        			mActivatedSensorNamesArray[mPosition][2] = "Accelerometer Z";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_GYRO)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "Gyroscope X";
        			mActivatedSensorNamesArray[mPosition][1] = "Gyroscope Y";
        			mActivatedSensorNamesArray[mPosition][2] = "Gyroscope Z";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_MAG)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "Magnetometer X";
        			mActivatedSensorNamesArray[mPosition][1] = "Magnetometer Y";
        			mActivatedSensorNamesArray[mPosition][2] = "Magnetometer Z";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_GSR)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "GSR";
        			mActivatedSensorNamesArray[mPosition][1] ="";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_EMG)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "EMG";
        			mActivatedSensorNamesArray[mPosition][1] ="";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_ECG)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "ECG RA-LL";
        			mActivatedSensorNamesArray[mPosition][1] = "ECG LA-LL";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_BRIDGE_AMP)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "Bridge Amplifier High";
        			mActivatedSensorNamesArray[mPosition][1] = "Bridge Amplifier Low";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_HEART)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "Heart Rate";
        			mActivatedSensorNamesArray[mPosition][1] ="";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_EXP_BOARD_A0)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "ExpBoard A0";
        			mActivatedSensorNamesArray[mPosition][1] ="";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		} else if ((mEnabledSensors & Shimmer.SENSOR_EXP_BOARD_A7)!=0){
        			mActivatedSensorNamesArray[mPosition][0] = "ExpBoard A7";
        			mActivatedSensorNamesArray[mPosition][1] ="";
        			mActivatedSensorNamesArray[mPosition][2] ="";
        		}
				
				
			}
		}
	}
	
	
	
	public void setEnableLogging(boolean enableLogging){
		mEnableLogging=enableLogging;
		Log.d("Shimmer","Logging :" + Boolean.toString(mEnableLogging));
	}
	public boolean getEnableLogging(){
		return mEnableLogging;
	}
	public void setAllSampingRate(double samplingRate) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.writeSamplingRate(samplingRate);
			}
		}
	}

	public void setAllAccelRange(int accelRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.writeAccelRange(accelRange);
			}
		}
	}

	public void setAllGSRRange(int gsrRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.writeGSRRange(gsrRange);
			}
		}
	}
	
	public void setAllEnabledSensors(int enabledSensors) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
				stemp.writeEnabledSensors(enabledSensors);
			}
		}
	}
	
	
	public void setEnabledSensors(int enabledSensors,String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeEnabledSensors(enabledSensors);
			}
		}
	}

	public void toggleLED(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.toggleLed();
			}
		}
	}
	
	public int getHWVersion(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int HWversion=0;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				HWversion = stemp.getHardwareVersion();
			}
		}
		return HWversion;
	}
	
	public long getEnabledSensors(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		long enabledSensors=0;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				enabledSensors = stemp.getEnabledSensors();
			}
		}
		return enabledSensors;
	}
	
	
	public void writeSamplingRate(String bluetoothAddress,double samplingRate) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeSamplingRate(samplingRate);
			}
		}
	}
	
	public void writeAccelRange(String bluetoothAddress,int accelRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeAccelRange(accelRange);
			}
		}
	}
	
	public void writeGSRRange(String bluetoothAddress,int gsrRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeGSRRange(gsrRange);
			}
		}
	}
	
	
	public double getSamplingRate(String bluetoothAddress) {
		// TODO Auto-generated method stub
		
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		double SRate=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				SRate= stemp.getSamplingRate();
			}
		}
		return SRate;
	}

	public int getAccelRange(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int aRange=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				aRange = stemp.getAccelRange();
			}
		}
		return aRange;
	}

	public int getShimmerState(String bluetoothAddress){

		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int status=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				status = stemp.getShimmerState();
				Log.d("ShimmerState",Integer.toString(status));
			}
		}
		return status;
	
	}
	
	public int getGSRRange(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int gRange=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				gRange = stemp.getGSRRange();
			}
		}
		return gRange;
	}

	public void startStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.startStreaming();
						int mPosition = Integer.parseInt(stemp.getDeviceName());
						long mEnabledSensors = stemp.getEnabledSensors();
						if ((mEnabledSensors & Shimmer.SENSOR_ACCEL)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "Accelerometer X";
		        			mActivatedSensorNamesArray[mPosition][1] = "Accelerometer Y";
		        			mActivatedSensorNamesArray[mPosition][2] = "Accelerometer Z";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_GYRO)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "Gyroscope X";
		        			mActivatedSensorNamesArray[mPosition][1] = "Gyroscope Y";
		        			mActivatedSensorNamesArray[mPosition][2] = "Gyroscope Z";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_MAG)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "Magnetometer X";
		        			mActivatedSensorNamesArray[mPosition][1] = "Magnetometer Y";
		        			mActivatedSensorNamesArray[mPosition][2] = "Magnetometer Z";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_GSR)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "GSR";
		        			mActivatedSensorNamesArray[mPosition][1] ="";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_EMG)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "EMG";
		        			mActivatedSensorNamesArray[mPosition][1] ="";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_ECG)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "ECG RA-LL";
		        			mActivatedSensorNamesArray[mPosition][1] = "ECG LA-LL";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_BRIDGE_AMP)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "Bridge Amplifier High";
		        			mActivatedSensorNamesArray[mPosition][1] = "Bridge Amplifier Low";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_HEART)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "Heart Rate";
		        			mActivatedSensorNamesArray[mPosition][1] ="";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_EXP_BOARD_A0)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "ExpBoard A0";
		        			mActivatedSensorNamesArray[mPosition][1] ="";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		} else if ((mEnabledSensors & Shimmer.SENSOR_EXP_BOARD_A7)!=0){
		        			mActivatedSensorNamesArray[mPosition][0] = "ExpBoard A7";
		        			mActivatedSensorNamesArray[mPosition][1] ="";
		        			mActivatedSensorNamesArray[mPosition][2] ="";
		        		}
						
						
					
						
						
					}
				}
	}
	
	public void stopStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED){
							stemp.stopStreaming();
							if (mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] != -1){
								mSoundPool.stop(mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0]);
								mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] = -1;
							}
							if (mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][1] != -1){
								mSoundPool.stop(mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][1]);
								mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] = -1;
							}
							if (mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][2] != -1){
								mSoundPool.stop(mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][2]);
								mSoundIDStreamArray[Integer.parseInt(stemp.getDeviceName())][0] = -1;
							}
						}
					}
				}
	}
	
	public void disconnectShimmer(String bluetoothAddress){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.stop();
				
			}
		}
		
		
		mLogShimmer.remove(bluetoothAddress);		
		mMultiShimmer.remove(bluetoothAddress);
		
	}
	
	public void setGraphHandler(Handler handler, String bluetoothAddress){
		mHandlerGraph=handler;
		mGraphBluetoothAddress=bluetoothAddress;
	}
	
	public void enableGraphingHandler(boolean setting){
		mGraphing=setting;
	}
	
	public boolean DevicesConnected(String bluetoothAddress){
		boolean deviceConnected=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getShimmerState()==Shimmer.STATE_CONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				deviceConnected=true;
			}
		}
		return deviceConnected;
	}
	
	public boolean DeviceIsStreaming(String bluetoothAddress){
		boolean deviceStreaming=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getStreamingStatus() == true  && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				deviceStreaming=true;
			}
		}
		return deviceStreaming;
	}
	
	public boolean GetInstructionStatus(String bluetoothAddress){
		boolean instructionStatus=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				instructionStatus=stemp.getInstructionStatus();
			}
		}
		return instructionStatus;
	}

	public void setLoggingName(String name){
		mLogFileName=name;
	}
	
	public void closeAndRemoveFile(String bluetoothAddress){
		if (mEnableLogging==true && mLogShimmer.get(bluetoothAddress)!=null){
			mLogShimmer.get(bluetoothAddress).closeFile();
			mLogShimmer.remove(bluetoothAddress);
		}
	}
	
	public void loadSound(String path, int position, int sourcePosition){
		
		mSoundIDArray[position][sourcePosition] = mSoundPool.load(path, 1);
		mSoundPathArray[position][sourcePosition] = path;
		
	}
	
	public void loadSound2(String path, int position, int sourcePosition){
		
		mSoundIDArray2[position][sourcePosition] = mSoundPool.load(path, 1);
		mSoundPathArray[position][sourcePosition] = path;
		
	}
	
	public String returnSoundPath(int position, int sourcePosition){
		return mSoundPathArray[position][sourcePosition];
	}
	
	public String returnSoundPath2(int position, int sourcePosition){
		return mSoundPathArray2[position][sourcePosition];
	}
	
}
