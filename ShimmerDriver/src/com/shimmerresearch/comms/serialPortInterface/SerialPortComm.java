package com.shimmerresearch.comms.serialPortInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driver.DeviceException;

public abstract class SerialPortComm implements ByteLevelDataComm, Serializable {
	//the timeout value for connecting with the port
    public int SERIAL_PORT_TIMEOUT = 500; // was 2000
    public String mAddress="";
    
	transient private List<ByteLevelDataCommListener> mByteLevelDataCommListener = new ArrayList<ByteLevelDataCommListener>();
    
	public void clearByteLevelDataCommListener(){
		mByteLevelDataCommListener.clear();
	}
	
    public void setAddress(String address){
    	mAddress = address;
    }
    
	@Override
	public void eventDeviceConnected() {
		// TODO Auto-generated method stub
		if (mByteLevelDataCommListener.size()!=0){
			for (ByteLevelDataCommListener commListerner:mByteLevelDataCommListener){
				commListerner.eventConnected();
			}
		}
	}

	@Override
	public void eventDeviceDisconnected() {
		// TODO Auto-generated method stub
		if (mByteLevelDataCommListener.size()!=0){
			for (ByteLevelDataCommListener commListerner:mByteLevelDataCommListener){
				commListerner.eventDisconnected();
			}
		}
	}

	@Override
	public void setByteLevelDataCommListener(ByteLevelDataCommListener spl) {
		// TODO Auto-generated method stub
		mByteLevelDataCommListener.add(spl);
	}
}