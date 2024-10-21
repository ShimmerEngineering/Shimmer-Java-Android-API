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

import bolts.TaskCompletionSource;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
		CalibDetailsBmp390 calibDetailsBmp390 = new CalibDetailsBmp390();
		byte[] pressureResoResTest = {
			    (byte) 0xE7, (byte) 0x6B, (byte) 0xF0, (byte) 0x4A, (byte) 0xF9, 
			    (byte) 0xAB, (byte) 0x1C, (byte) 0x9B, (byte) 0x15, (byte) 0x06, 
			    (byte) 0x01, (byte) 0xD2, (byte) 0x49, (byte) 0x18, (byte) 0x5F, 
			    (byte) 0x03, (byte) 0xFA, (byte) 0x3A, (byte) 0x0F, (byte) 0x07, 
			    (byte) 0xF5
			};
		
		mDevice.mSensorBMPX80.parseCalParamByteArray(pressureResoResTest,CALIB_READ_SOURCE.INFOMEM);
    	byte[] newPacket = {-68, 19, 112, -53, 7, 9, 8, -66, 4, 24, -5, 7, 2, -16, -70, (byte) 0x00, (byte) 0xCF, (byte) 0x7F ,(byte) 0x00, (byte) 0x17, (byte) 0x64, -128, -70, -75, 80, 4, -87, 40, -128, 127, -1, -1, -3, 80, 71};

    	String[] dataType = {"u24", "i16", "i16", "i16", "i16", "i16", "i16", "u24", "u24", "u8", "i24r", "i24r", "u8", "i24r", "i24r", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
    	
    	//PARSE DATA
    	long[] newPacketInt = null;

    	try{
    		newPacketInt = UtilParseData.parseData(newPacket, dataType);	//uncalib
    		double UT = (double)newPacketInt[7];
			double UP = (double)newPacketInt[8];
			
			double[] bmp390caldata = mDevice.mSensorBMPX80.calibratePressureSensorData(UP, UT);
			double P = bmp390caldata[0];
			double T = bmp390caldata[1];
			
			if(Math.round(P * 10000d) / 10000d != 100912.8176) {	//4d.p. accuracy
	    		assert(false);
			}
			
			if(Math.round(T * 10000d) / 10000d != 23.2659) {		//4d.p. accuracy
	    		assert(false);
			}
    	} catch(IndexOutOfBoundsException e){
    		String debugString = "SignalDataTypeArray:";
    		for(String s:dataType){
    			debugString += "\t" + s;
    		}
    		System.out.println(debugString);
    		System.out.println("newPacket:\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(newPacket));
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
