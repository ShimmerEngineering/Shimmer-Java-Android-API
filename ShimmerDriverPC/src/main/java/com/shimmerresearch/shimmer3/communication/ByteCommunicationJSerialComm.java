package com.shimmerresearch.shimmer3.communication;

import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

public class ByteCommunicationJSerialComm implements ByteCommunication {
	protected transient SerialPort mSerialPort = null;
	private String mAddress;
	private boolean mPrintValues = false;
	
	public ByteCommunicationJSerialComm(String address) {
		mAddress = address;
		mSerialPort = SerialPort.getCommPort(mAddress);
	}
	
	public ByteCommunicationJSerialComm(SerialPort serialPort) {
		mSerialPort = serialPort;
		mAddress = mSerialPort.getSystemPortName();
	}
	
	@Override
	public int getInputBufferBytesCount() throws jssc.SerialPortException {
		try {
			return mSerialPort.bytesAvailable();
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "getInputBufferBytesCount", e.getMessage());
		}
	}

	@Override
	public boolean isOpened() {
		return mSerialPort.isOpen();
	}

	@Override
	public boolean closePort() throws jssc.SerialPortException {
		try {
			return mSerialPort.closePort();
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "closePort", e.getMessage());
		}
	}

	@Override
	public boolean openPort() throws jssc.SerialPortException {
		try {
			boolean opened = mSerialPort.openPort();
			if (opened) {
				mSerialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
				mSerialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
			}
			return mSerialPort.isOpen();
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "openPort", e.getMessage());
		}
	}

	@Override
	public byte[] readBytes(int byteCount, int timeout) throws jssc.SerialPortTimeoutException, jssc.SerialPortException {
		try {
			mSerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, timeout, 0);
			byte[] rxbytes = new byte[byteCount];
			int bytesRead = mSerialPort.readBytes(rxbytes, byteCount);
			
			if (bytesRead < byteCount) {
				// Timeout occurred
				throw new jssc.SerialPortTimeoutException(mAddress, "readBytes", timeout);
			}
			
			if (mPrintValues) {
				System.out.println("READ BYTES: " + UtilShimmer.bytesToHexString(rxbytes));
			}
			return rxbytes;
		} catch (jssc.SerialPortTimeoutException e) {
			throw e; // Re-throw timeout exceptions as-is
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "readBytes", e.getMessage());
		}
	}

	@Override
	public boolean writeBytes(byte[] buffer) throws jssc.SerialPortException {
		try {
			if (mPrintValues) {
				System.out.println("WRITE BYTES: " + UtilShimmer.bytesToHexString(buffer));
			}
			int bytesWritten = mSerialPort.writeBytes(buffer, buffer.length);
			return bytesWritten == buffer.length;
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "writeBytes", e.getMessage());
		}
	}

	@Override
	public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws jssc.SerialPortException {
		try {
			// Map parameters to jSerialComm constants
			int stopBitsJSC = (stopBits == 1) ? SerialPort.ONE_STOP_BIT : SerialPort.TWO_STOP_BITS;
			int parityJSC = SerialPort.NO_PARITY;
			if (parity == 1) parityJSC = SerialPort.ODD_PARITY;
			else if (parity == 2) parityJSC = SerialPort.EVEN_PARITY;
			
			return mSerialPort.setComPortParameters(baudRate, dataBits, stopBitsJSC, parityJSC);
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "setParams", e.getMessage());
		}
	}

	@Override
	public boolean purgePort(int flags) throws jssc.SerialPortException {
		try {
			// JSSC purge flags: 1=PURGE_RXCLEAR, 2=PURGE_TXCLEAR
			// jSerialComm flushIOBuffers() clears both RX and TX
			return mSerialPort.flushIOBuffers();
		} catch (Exception e) {
			throw new jssc.SerialPortException(mAddress, "purgePort", e.getMessage());
		}
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
	
	public SerialPort getSerialPort() {
		return mSerialPort;
	}
}
