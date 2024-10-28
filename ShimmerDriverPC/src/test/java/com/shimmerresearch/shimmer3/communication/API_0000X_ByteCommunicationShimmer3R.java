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
public class API_0000X_ByteCommunicationShimmer3R extends BasicProcessWithCallBack{
	ShimmerPC mDevice;
	TaskCompletionSource<Boolean> mCalibrationTask;
	@Before
    public void setUp() {
		mDevice = new ShimmerPC("COM99");
		mDevice.setTestRadio(new ByteCommunicationSimulatorS3R("COM99"));
		setWaitForData(mDevice);
    }
    
    @Test
    public void test001_testConnectandDisconnect() {
    	mCalibrationTask = new TaskCompletionSource<Boolean>();
    	mDevice.connect("","");
    	
    		mCalibrationTask = new TaskCompletionSource<>();
    		try {
				boolean result = mCalibrationTask.getTask().waitForCompletion(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	if (!mDevice.isConnected()) {
    		assert(false);
    	}
    	if (!mDevice.getFirmwareVersionParsed().equals("LogAndStream v0.0.1")) {
    		assert(false);
    	}
    	if (!(mDevice.getHardwareVersion()==HW_ID.SHIMMER_3R)) {
    		assert(false);
    	}
    	
    	System.out.println(mDevice.getHardwareVersionParsed());
    	if (!mDevice.getHardwareVersionParsed().equals("Shimmer3r")) {
    		assert(false);
    	}
    	
    	ArrayList<SensorDetails> listofsensorDetails = (ArrayList<SensorDetails>) mDevice.getListOfEnabledSensors();
    	
    	for(SensorDetails sd: listofsensorDetails) {
    		if (sd.isEnabled()) {
    			for (ChannelDetails cd: sd.getListOfChannels()) {
    				System.out.print(cd.mGuiName + " ; ");
    			}
    			System.out.println();
    		}
    	}
    	
    	LinkedHashMap<SENSORS, AbstractSensor> mapOfSensors = mDevice.getMapOfSensorsClasses();
    	for (AbstractSensor sensor:mapOfSensors.values()) {
    		if (sensor.getNumberOfEnabledChannels(COMMUNICATION_TYPE.BLUETOOTH)>0) {
    			System.out.println(sensor.getClass().getName() + " ; " + sensor.getSensorName());
    		}
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
					if (mCalibrationTask!=null) {
						mCalibrationTask.setResult(true);
						mCalibrationTask = null;
					}
				}
		}
		
		}
	}
   
}
