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
package com.shimmerresearch.pcdriver;

import com.shimmerresearch.bluetooth.ProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.CURRENT_OPERATION;

public class CallbackObject {
	public int mIndicator;
	public CURRENT_OPERATION mCurrentOperation;
	public String mBluetoothAddress;
	public String mComPort;
	public double mPacketReceptionRate;
	public ProgressReportPerDevice mProgressReportPerDevice;
	
	public CallbackObject(int ind, String myBlueAdd, String comPort){
		mIndicator = ind;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
	}
	
	public CallbackObject(int ind, CURRENT_OPERATION currentOperation, String myBlueAdd, String comPort){
		mIndicator = ind;
		mCurrentOperation = currentOperation;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
	}
	
	public CallbackObject(int ind, String myBlueAdd, String comPort, double packetReceptionRate){
		mIndicator = ind;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
		this.mPacketReceptionRate = packetReceptionRate;
	}

	public CallbackObject(int ind, String myBlueAdd, String comPort, ProgressReportPerDevice progressReportPerDevice) {
		mIndicator = ind;
		mBluetoothAddress = myBlueAdd;
		mComPort = comPort;
		mProgressReportPerDevice = progressReportPerDevice;
	}

}
