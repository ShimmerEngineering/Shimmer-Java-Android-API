package com.shimmerresearch.tools.bluetooth;

import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class BluetootManagerTest extends BasicProcessWithCallBack{
	static BluetootManagerTest bmt = new BluetootManagerTest();
	static BasicShimmerBluetoothManagerPc manager = new BasicShimmerBluetoothManagerPc();
	static ShimmerPC shimmer;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		bmt.setWaitForData(manager.callBackObject);
		manager.connectShimmerThroughCommPort("COM35");
		JFrame n = new JFrame();
				n.setVisible(true);
		n.setSize(200, 200);
		n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		System.out.println(shimmerMSG);
		//Modify format to make it usable for Abstract GUI MultiSensor
		if (shimmerMSG.mIdentifier==ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET){
			System.out.println(shimmerMSG.mB);
		}
		else if (shimmerMSG.mIdentifier==ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE){
			CallbackObject callbackObject = (CallbackObject) shimmerMSG.mB;
			if(callbackObject.mState == BT_STATE.DISCONNECTED){
			
			}
			
			if(callbackObject.mState == BT_STATE.CONNECTED){
				shimmer = (ShimmerPC) manager.getShimmerDeviceBtConnected("COM35");
				System.out.println("State Connected");
				try {
					shimmer.startStreaming();
				} catch (ShimmerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
	}

}
