package com.shimmerresearch.shimmer3.communication;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

import bolts.TaskCompletionSource;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Test methods will be run in alphabetical order
public class API_0000X_ByteCommunicationShimmer3lns0_16_11_w_sync_slave extends BasicProcessWithCallBack{
	ShimmerPC mDevice;
	TaskCompletionSource<Boolean> mWaitTask;
	@Before
    public void setUp() {
		mDevice = new ShimmerPC("COM99");
		mDevice.setTestRadio(new ByteCommunicationSimulatorS3LNS0_16_11_w_sync_slave("COM99"));
		setWaitForData(mDevice);
    }
    
    @Test
    public void test001_testConnectandDisconnect() {
    	mWaitTask = new TaskCompletionSource<Boolean>();
    	mDevice.connect("","");
    	
    		mWaitTask = new TaskCompletionSource<>();
    		try {
				boolean result = mWaitTask.getTask().waitForCompletion(3, TimeUnit.SECONDS);//Just to give time to connect to finish
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	if (!mDevice.isConnected()) {
    		assert(false);
    	}
    	if (!mDevice.getFirmwareVersionParsed().equals("LogAndStream v0.16.11")) {
    		assert(false);
    	}
    	if (!(mDevice.getHardwareVersion()==HW_ID.SHIMMER_3)) {
    		assert(false);
    	}
    	
    	System.out.println(mDevice.getHardwareVersionParsed());
    	if (!mDevice.getHardwareVersionParsed().equals("Shimmer3")) {
    		assert(false);
    	}
    	
    	if (!mDevice.isSupportedSdLogSync()) {
    		assert(false);
    	}
    	
    	if (mDevice.isMasterShimmer()) {
    		assert(false);
    	}
    	
    	if (mDevice.isDisableBluetooth()) {
    		assert(false);
    	}
    	
    	if (mDevice.getSyncNodesList().size()<1) {
    		assert(false);
    	}
    	
    	if (!mDevice.isSyncWhenLogging()) {
    		assert(false);
    	}
    	if (mDevice.getTrialDurationEstimatedInSecs()!=5) {
    		assert(false);
    	}
    	for (String s:mDevice.getSyncNodesList()) {
    		System.out.println("Node List: " + s);
    	}
    	if (!mDevice.getSyncNodesList().contains("E8EB1B713E36") || !mDevice.getSyncNodesList().contains("D8478F04B9A7")) {
    		assert(false);
    	}
    	
    }

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		  int ind = shimmerMSG.mIdentifier;

		  Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
				CallbackObject callbackObject = (CallbackObject)object;
				
				if (callbackObject.mState == BT_STATE.CONNECTED) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
		
		}
	}
   
}
