package com.shimmerresearch.comms.serialPortInterface;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSerialPortComm implements InterfaceByteLevelDataComm {
	
	//the timeout value for connecting with the port
    public int SERIAL_PORT_TIMEOUT = 500; // was 2000
    public String mAddress="";
    
	transient private List<ByteLevelDataCommListener> mByteLevelDataCommListener = new ArrayList<ByteLevelDataCommListener>();
    
	public final static class SHIMMER_UART_BAUD_RATES{
		public final static int SHIMMER3_DOCKED = 115200;
		public final static int SPAN = 230400;
	}
	
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