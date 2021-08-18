package com.shimmerresearch.driver.ble;

import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.comms.radioProtocol.RadioListener;

public class VerisenseProtocolByteCommunication {
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	
	AbstractByteCommunication mByteCommunication;
	
	public void addRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}
	
	public VerisenseProtocolByteCommunication(AbstractByteCommunication byteComm) {
		mByteCommunication = byteComm;
	}
	
	public void connect() {
		mByteCommunication.connect();
	}
	
	
}
