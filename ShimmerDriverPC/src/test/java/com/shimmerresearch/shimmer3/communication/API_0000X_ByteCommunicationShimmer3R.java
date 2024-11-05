package com.shimmerresearch.shimmer3.communication;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.bmpX80.CalibDetailsBmp390;
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
	ByteCommunicationSimulatorS3R mByteCommunicationSimulatorS3R;
	@Before
    public void setUp() {
		mByteCommunicationSimulatorS3R = new ByteCommunicationSimulatorS3R("COM99");
		mDevice = new ShimmerPC("COM99");
		mDevice.setTestRadio(mByteCommunicationSimulatorS3R);
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
    	if(!mByteCommunicationSimulatorS3R.isGetPressureCalibrationCoefficientsCommand) {
    		assert(false);
    	}
    	if(!mDevice.mSensorBMPX80.mSensorType.equals(SENSORS.BMP390)){
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


	@Test
	public void test002_ConnectandTestBMP390() {
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
    	
    	//BMP390 data parsing
    	try{
    		mDevice.mSensorBMPX80.parseCalParamByteArray(mByteCommunicationSimulatorS3R.getPressureResoTest(),CALIB_READ_SOURCE.INFOMEM);
    		long[] dataPacket = UtilParseData.parseData(mByteCommunicationSimulatorS3R.getTestDataPacket(), mByteCommunicationSimulatorS3R.getTestDataType());	//uncalib
    		double UT = (double)dataPacket[7];
			double UP = (double)dataPacket[8];
			
			double[] bmp390caldata = mDevice.mSensorBMPX80.calibratePressureSensorData(UP, UT);
			double pressure = bmp390caldata[0];
			double temperature = bmp390caldata[1];
			
			if(Math.round(pressure * 10000d) / 10000d != 100912.8176) {	//4d.p. accuracy
	    		assert(false);
			}
			
			if(Math.round(temperature * 10000d) / 10000d != 23.2659) {		//4d.p. accuracy
	    		assert(false);
			}
    	} catch(IndexOutOfBoundsException e){
    		throw(e);
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
