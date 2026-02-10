package com.shimmerresearch.shimmer3.communication;

import com.fazecast.jSerialComm.SerialPort;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

// Note: JSSC exceptions are used to maintain API compatibility with ByteCommunication interface
// This allows jSerialComm implementation to be a drop-in replacement for JSSC
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

/**
 * ByteCommunicationJSerialComm - jSerialComm implementation for better macOS support
 * 
 * This implementation uses jSerialComm instead of JSSC to provide more reliable
 * Bluetooth Classic communications on macOS.
 * 
 * @author Shimmer Research
 */
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
	public int getInputBufferBytesCount() throws SerialPortException {
		try {
			return mSerialPort.bytesAvailable();
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "getInputBufferBytesCount", e.getMessage());
		}
	}

	@Override
	public boolean isOpened() {
		return mSerialPort.isOpen();
	}

	@Override
	public boolean closePort() throws SerialPortException {
		try {
			return mSerialPort.closePort();
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "closePort", e.getMessage());
		}
	}

	@Override
	public boolean openPort() throws SerialPortException {
		try {
			mSerialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
			boolean result = mSerialPort.openPort();
			return result && mSerialPort.isOpen();
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "openPort", e.getMessage());
		}
	}

	@Override
	public byte[] readBytes(int byteCount, int timeout) throws SerialPortTimeoutException, SerialPortException {
		try {
			// Set timeout for this read operation
			mSerialPort.setComPortTimeouts(
				SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
				timeout,
				0
			);
			
			byte[] rxbytes = new byte[byteCount];
			int totalBytesRead = 0;
			long startTime = System.currentTimeMillis();
			
			while (totalBytesRead < byteCount) {
				int bytesToRead = byteCount - totalBytesRead;
				int bytesRead = mSerialPort.readBytes(rxbytes, bytesToRead, totalBytesRead);
				totalBytesRead += bytesRead;
				
				// Check for timeout
				if (System.currentTimeMillis() - startTime > timeout) {
					throw new SerialPortTimeoutException(mAddress, "readBytes", timeout);
				}
				
				// Small delay to avoid busy waiting
				if (bytesRead == 0 && totalBytesRead < byteCount) {
					Thread.sleep(10);
				}
			}
			
			if (mPrintValues) {
				System.out.println("READ BYTES: " + UtilShimmer.bytesToHexString(rxbytes));
			}
			return rxbytes;
		} catch (SerialPortTimeoutException e) {
			throw e;
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "readBytes", e.getMessage());
		}
	}

	@Override
	public boolean writeBytes(byte[] buffer) throws SerialPortException {
		try {
			if (mPrintValues) {
				System.out.println("WRITE BYTES: " + UtilShimmer.bytesToHexString(buffer));
			}
			int bytesWritten = mSerialPort.writeBytes(buffer, buffer.length);
			return bytesWritten == buffer.length;
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "writeBytes", e.getMessage());
		}
	}

	@Override
	public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException {
		try {
			// Map parameters to jSerialComm constants
			int jscStopBits = (stopBits == 1) ? SerialPort.ONE_STOP_BIT : SerialPort.TWO_STOP_BITS;
			int jscParity = SerialPort.NO_PARITY; // Default to no parity
			if (parity == 1) jscParity = SerialPort.ODD_PARITY;
			else if (parity == 2) jscParity = SerialPort.EVEN_PARITY;
			
			mSerialPort.setComPortParameters(baudRate, dataBits, jscStopBits, jscParity);
			return true;
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "setParams", e.getMessage());
		}
	}

	@Override
	public boolean purgePort(int flags) throws SerialPortException {
		try {
			// jSerialComm uses flushIOBuffers to clear both input and output buffers
			return mSerialPort.flushIOBuffers();
		} catch (Exception e) {
			throw new SerialPortException(mAddress, "purgePort", e.getMessage());
		}
	}

	@Override
	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener) {
		// Implementation for listener if needed
	}

	@Override
	public void removeRadioListenerList() {
		// Implementation for removing listeners if needed
	}

	public void setSerialPort(SerialPort sp) {
		mSerialPort = sp;
	}
	
	public void setPrintValues(boolean printValues) {
		mPrintValues = printValues;
	}
}
