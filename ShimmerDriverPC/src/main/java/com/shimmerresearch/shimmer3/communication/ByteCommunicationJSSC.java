package com.shimmerresearch.shimmer3.communication;

import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ByteCommunicationJSSC implements ByteCommunication{
	protected transient SerialPort mSerialPort=null;
	private String mAddress;
	private boolean mPrintValues = false;
	
	public ByteCommunicationJSSC(String address) {
		mAddress = address;
		mSerialPort = new SerialPort(mAddress);
	}
	
	public ByteCommunicationJSSC(SerialPort serialPort) {
		mSerialPort = serialPort;
		mAddress = mSerialPort.getPortName();
	}
	
	@Override
	public int getInputBufferBytesCount() throws SerialPortException {
		// TODO Auto-generated method stub
		return mSerialPort.getInputBufferBytesCount();
	}

	@Override
	public boolean isOpened() {
		// TODO Auto-generated method stub
		return mSerialPort.isOpened();
	}

	@Override
	public boolean closePort() throws SerialPortException {
		// TODO Auto-generated method stub
		return mSerialPort.closePort();
	}

	@Override
	public boolean openPort() throws SerialPortException {
		/*
		consolePrintLn("Connecting to Shimmer on " + address);
		consolePrintLn("Port open: " + mSerialPort.openPort());
		consolePrintLn("Params set: " + mSerialPort.setParams(115200, 8, 1, 0));
		consolePrintLn("Port Status : " + Boolean.toString(mSerialPort.isOpened()));
		*/
		mSerialPort.openPort();
		mSerialPort.setParams(115200, 8, 1, 0);
		return mSerialPort.isOpened();
	}

	@Override
	public byte[] readBytes(int byteCount, int timeout) throws SerialPortTimeoutException, SerialPortException {
		// TODO Auto-generated method stub
		byte[] rxbytes = mSerialPort.readBytes(byteCount, timeout);
		if (mPrintValues)
		System.out.println("READ BYTES: " + UtilShimmer.bytesToHexString(rxbytes));
		return rxbytes;
	}

	@Override
	public boolean writeBytes(byte[] buffer) throws SerialPortException {
		// TODO Auto-generated method stub
		if(mPrintValues)
		System.out.println("WRITE BYTES: " + UtilShimmer.bytesToHexString(buffer));
		return mSerialPort.writeBytes(buffer);
	}

	@Override
	public boolean setParams(int i, int j, int k, int l) throws SerialPortException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean purgePort(int i) throws SerialPortException {
		// TODO Auto-generated method stub
		return mSerialPort.purgePort(i);
	}

	@Override
	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRadioListenerList() {
		// TODO Auto-generated method stub
		
	}

	public void setSerialPort(SerialPort sp) {
		mSerialPort = sp;
	}
}
