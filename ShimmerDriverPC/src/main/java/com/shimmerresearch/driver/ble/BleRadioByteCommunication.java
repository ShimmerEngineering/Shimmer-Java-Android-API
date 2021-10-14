package com.shimmerresearch.driver.ble;

import java.lang.Runtime;
import java.util.concurrent.TimeUnit;
import java.io.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import bolts.TaskCompletionSource;

public class BleRadioByteCommunication extends AbstractByteCommunication {

	Process p;
	String executablePath;
	BufferedReader reader;
	BufferedWriter writer;
	Boolean flag;
	BleRadioByteCommunication radio1;
	BleRadioByteCommunication radio2;
	String uuid;
	TaskCompletionSource<String> mTaskConnect = new TaskCompletionSource<>();
	TaskCompletionSource<String> mTaskDisconnect = new TaskCompletionSource<>();
	

	public BleRadioByteCommunication(String uuid, String exePath, ByteCommunicationListener listener) {
		this.uuid = uuid;
		this.executablePath = exePath;
		mByteCommunicationListener = listener;
		InitializeProcess();
	}

	public BleRadioByteCommunication(String uuid, String exePath) {
		this.uuid = uuid;
		this.executablePath = exePath;
		InitializeProcess();
	}

	public BleRadioByteCommunication(BluetoothDeviceDetails btDevDetails, String exePath) {
		this.uuid = convertMacIDtoUUID(btDevDetails.mShimmerMacId);
		this.executablePath = exePath;
		InitializeProcess();
	}
	
	public String convertMacIDtoUUID(String MacID) {
		//00000000-0000-0000-0000-e7452c6d6f14
		String uuid = "00000000-0000-0000-0000-";
		uuid = String.join("", uuid, MacID.replace(":", ""));
		return uuid;
	}
	
	public void DestroyProcess() {
		if (p != null) {
			WriteDataToProcess("Stop");
			p.destroyForcibly();
			p=null;
		}
	}

	public void WriteDataToProcess(String s) {

		try {
			writer.write(s, 0, s.length());
			writer.newLine();
			writer.flush();
			// writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
		public static void WriteDataToProcess(byte[] buf) {
			try {
				p.getOutputStream().write(buf, 0, buf.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	 */

	protected void InitializeProcess() {
		Runtime runTime = Runtime.getRuntime();
		
		runTime.addShutdownHook(new Thread() {
			public void run() {
				DestroyProcess();
			}}
		);

		try {
			p = runTime.exec(executablePath + " " + uuid);
			writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Thread Example = new Thread() {
			public void run() {
				try {
					String line = null;
					while (true) {
						if ((line = reader.readLine()) != null) {
//							System.out.println("BleRadioByteComm: " + line);
							if (line.equals("Connected")) {
								mTaskConnect.setResult("Connected");
								if (mByteCommunicationListener != null) {
									mByteCommunicationListener.eventConnected();
								}
							} else if (line.equals("Disconnected")) {
								mTaskDisconnect.setResult("Disconnected");
								if (mByteCommunicationListener != null) {
									mByteCommunicationListener.eventDisconnected();
								}
							} else if (line.equals("Connect failed")) {
								if (mByteCommunicationListener != null) {
									mByteCommunicationListener.eventDisconnected();
								}
							} else if (line.substring(0, 4).equals(("RXB:"))) {
								String bytes = line.substring(4, line.length());
								try {
									byte[] dataBytes = Hex.decodeHex(bytes);
									if (mByteCommunicationListener != null) {
										mByteCommunicationListener.eventNewBytesReceived(dataBytes);
									}
								} catch (DecoderException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								System.out.println("BleRadioByteComm: " + line);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		Example.start();
	}

	@Override
	public void connect() throws ShimmerException{
		WriteDataToProcess("Connect");
		mTaskConnect = new TaskCompletionSource<>();
		try {
			boolean result = mTaskConnect.getTask().waitForCompletion(2, TimeUnit.SECONDS);
			if (result) {
				
			} else {
				throw new ShimmerException("Connect Failed");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ShimmerException("InterruptedException");
		}
	}

	@Override
	public void disconnect() throws ShimmerException {
		WriteDataToProcess("Disconnect");
		mTaskDisconnect = new TaskCompletionSource<>();
		try {
			boolean result = mTaskDisconnect.getTask().waitForCompletion(4, TimeUnit.SECONDS);
			if (result) {
				
			} else {
				throw new ShimmerException("Disconnect Failed");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ShimmerException("InterruptedException");
		}
	}

	@Override
	public void writeBytes(byte[] bytes) {
		String bytesstring = Hex.encodeHexString(bytes);
		WriteDataToProcess("Write" + bytesstring);
	}

	@Override
	public void stop() {
		DestroyProcess();
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return this.uuid;
	}

}
