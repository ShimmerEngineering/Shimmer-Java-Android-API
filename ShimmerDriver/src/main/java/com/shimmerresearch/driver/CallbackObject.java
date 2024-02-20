//Object for callback method. Two components: an indicator(shows the type of notification message or the state) and the bluetooth address

//Rev0.1

/*
 * Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim
 * @date   October, 2013
 * 
 * 
 */
package com.shimmerresearch.driver;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;

public class CallbackObject {
	
	public int mIndicator;
	public BT_STATE mState = BT_STATE.DISCONNECTED;
	public String mBluetoothAddress;
	public String mComPort;
	public double mPacketReceptionRate;
	public BluetoothProgressReportPerDevice mProgressReportPerDevice;
	public Object mMyObject;

	
	/**used for hasStopStreaming, isNowStreaming and isReadyForStreaming notifications
	 * @param ind
	 * @param myBlueAdd
	 * @param comPort
	 */
	public CallbackObject(int ind, String myBlueAdd, String comPort){
		mIndicator = ind;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
	}
	
	
	/**
	 * @param ind
	 * @param state
	 * @param myBlueAdd
	 * @param comPort
	 */
	public CallbackObject(int ind, BT_STATE state, String myBlueAdd, String comPort){
		mIndicator = ind;
		mState = state;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
	}
	
	/** used for unwrapped callbacks -> currently used by packetratereception callbacks
	 * @param ind
	 * @param myBlueAdd
	 * @param comPort
	 * @param packetReceptionRate
	 */
	public CallbackObject(int ind, String myBlueAdd, String comPort, double packetReceptionRate){
		mIndicator = ind;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
		this.mPacketReceptionRate = packetReceptionRate;
	}

	/**used by progress report callbacks
	 * @param state
	 * @param myBlueAdd
	 * @param comPort
	 * @param progressReportPerDevice
	 */
	public CallbackObject(BT_STATE state, String myBlueAdd, String comPort, BluetoothProgressReportPerDevice progressReportPerDevice) {
		mState = state;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
		mProgressReportPerDevice = progressReportPerDevice;
	}
	
	public CallbackObject(String myBlueAdd, Object myObject) {
		mBluetoothAddress = myBlueAdd;
		mMyObject = myObject;
	}
	
	public CallbackObject(String myBlueAdd, String comPort, Object myObject) {
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
		mMyObject = myObject;
	}

}
