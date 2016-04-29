package com.shimmerresearch.androidradiodriver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import it.gerdavax.easybluetooth.BtSocket;
import it.gerdavax.easybluetooth.LocalDevice;
import it.gerdavax.easybluetooth.RemoteDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialEventCallback;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataComm;
import com.shimmerresearch.driver.DeviceException;

public class ShimmerSerialPortAndroid implements ByteLevelDataComm {
	//generic UUID for serial port protocol
	private UUID mSPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		
	public String mBluetoothAddress = "";
	private BluetoothAdapter mBluetoothAdapter = null;
	public BT_STATE mState = BT_STATE.DISCONNECTED;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private final BluetoothAdapter mAdapter;
	private DataInputStream mInStream;
	private OutputStream mmOutStream=null;
	
	public ShimmerSerialPortAndroid(String bluetoothAddress){
		
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public void connect() throws DeviceException {

			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothAddress);

			// Cancel any thread attempting to make a connection
			if (mState == BT_STATE.CONNECTING) {
				if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
			}
			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

			// Start the thread to connect with the given device
			mConnectThread = new ConnectThread(device);
			mConnectThread.start();
			 
		
	
		
	}

	@Override
	public void disconnect() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeSafely() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSerialPortRxBuffer() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void txBytes(byte[] buf) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] rxBytes(int numBytes) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerSerialPortRxEventCallback(
			ShimmerSerialEventCallback shimmerSerialEventCallback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSerialPortReaderStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean bytesAvailableToBeRead() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int availableBytes() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}
	


	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(mSPP_UUID); // If your device fails to pair try: device.createInsecureRfcommSocketToServiceRecord(mSPP_UUID)
			} catch (IOException e) {
				eventDeviceDisconnected();

			}
			mmSocket = tmp;
		}

		public void run() {
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				eventDeviceDisconnected();
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}
			// Reset the ConnectThread because we're done
			synchronized (ShimmerSerialPortAndroid.this) {
				mConnectThread = null;
			}
			// Start the connected thread
			//connected(mmSocket, mmDevice);
			// Cancel the thread that completed the connection
			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
			// Start the thread to manage the connection and perform transmissions
			mConnectedThread = new ConnectedThread(mmSocket);
		}
		
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}
	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread{
		private BluetoothSocket mSocket=null;
		public ConnectedThread(BluetoothSocket socket) {

			mSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				eventDeviceDisconnected();
			}

			//mInStream = new BufferedInputStream(tmpIn);
			mInStream = new DataInputStream(tmpIn);
			mmOutStream = tmpOut;
		}
		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) { }
		}
	}







	@Override
	public boolean isDisonnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eventDeviceConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventDeviceDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setByteLevelDataCommListener(ByteLevelDataCommListener spl) {
		// TODO Auto-generated method stub

	}
}

	


