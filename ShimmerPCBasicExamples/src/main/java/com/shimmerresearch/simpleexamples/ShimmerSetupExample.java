package com.shimmerresearch.simpleexamples;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class ShimmerSetupExample extends BasicProcessWithCallBack{

	public void initialize(){

		// TODO Auto-generated method stub
		ShimmerPC pc = new ShimmerPC("test", 512, 1, 1, ShimmerPC.SENSOR_ACCEL|ShimmerPC.SENSOR_DACCEL|ShimmerPC.SENSOR_GYRO, true, 1, 1);
		pc.connect("COM46", "");
		try {
			pc.connect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setWaitForData(pc);
	}
	
	public static void main(String[] args) {
		ShimmerSetupExample s = new ShimmerSetupExample();
		s.initialize();
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		  int ind = shimmerMSG.mIdentifier;

		  Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject)object;
			
			if (callbackObject.mState == BT_STATE.CONNECTING) {	//Never called
				
			} else if (callbackObject.mState == BT_STATE.CONNECTED) {
				System.out.println("CONNECTED");
			}
		}
	}

}
