package com.shimmerresearch.driver.ble;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import javelin.javelin;

public class JavelinBLERadioByteCommunication extends AbstractByteCommunication {
	String macaddress;
	static String[] javelinBLEDevices;
	String javelinID;
	String l_device;
	static boolean Initialized = false;
	public static final String ServiceUUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
	public static final String RXUUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
	public static final String TXUUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
	static final byte[] ReadStatusRequest = new byte[] { 0x11, 0x00, 0x00 };
	static final byte[] DisconnectRequest = new byte[] { 0x2B, 0x00, 0x00 };

	public JavelinBLERadioByteCommunication() {
		// TODO Auto-generated constructor stub

	}


	public static String getAbsoluteDLLPath(String dllPath) {
		// Get the absolute path to the JAR file's directory
		String jarDirectory;
		try {
			jarDirectory = new File(JavelinBLERadioByteCommunication.class.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI())
					.getParent();
			String directory = new File(jarDirectory).getParent();
			directory="C:\\GHCon\\Shimmer-Java-Android-API\\ShimmerDriverPC";
			String absoluteDLLPath = new File(directory, dllPath).getAbsolutePath();
			return absoluteDLLPath;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ""; 	


	}


	public void InitializeProcess() {

		// Construct the absolute path to the DLL
		System.out.println("Loading dlls");
		System.load(getAbsoluteDLLPath("libs/javelin.dll"));
		System.load(getAbsoluteDLLPath("libs/msvcp140d_app.dll"));
		System.load(getAbsoluteDLLPath("libs/vcruntime140_1d_app.dll"));
		System.load(getAbsoluteDLLPath("libs/VCRUNTIME140D_APP.dll"));
		System.load(getAbsoluteDLLPath("libs/CONCRT140D_APP.dll"));
		System.load(getAbsoluteDLLPath("libs/ucrtbased.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-synch-l1-2-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-synch-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-processthreads-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-debug-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-errorhandling-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-string-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-profile-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-sysinfo-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-interlocked-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-winrt-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-heap-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-memory-l1-1-0.dll"));
		System.load(getAbsoluteDLLPath("libs/api-ms-win-core-libraryloader-l1-2-0.dll"));
		System.load(getAbsoluteDLLPath("libs/OLEAUT32.dll"));

		javelinBLEDevices = javelin.listBLEDevices();
		Initialized = true;
	}

	/** 
	 * Initialize a BLE radio with a radio listener
	 * @param macaddress  e.g. e8:eb:1b:93:68:dd
	 * @param listener listen to the events
	 * @see ByteCommunicationListener
	 */
	public JavelinBLERadioByteCommunication(String macaddress) {
		//if (!Initialized) {
			InitializeProcess();
		//}
		this.macaddress = macaddress;
		for (String bleDev:javelinBLEDevices) {
			if (bleDev.contains(macaddress)) {
				javelinID = bleDev;
			}
		}
		if (javelinID.isEmpty()) {

		} else {
			System.out.println(javelinID);
		}

	}

	/** 
	 * Initialize a BLE radio with a radio listener
	 * @param macaddress  e.g. e8:eb:1b:93:68:dd
	 * @param listener listen to the events
	 * @see ByteCommunicationListener
	 */
	public JavelinBLERadioByteCommunication(BluetoothDeviceDetails bdd) {
		if (!Initialized) {
			InitializeProcess();
		}
		this.macaddress = bdd.mComPort;
		for (String bleDev:javelinBLEDevices) {
			if (bleDev.toUpperCase().contains(macaddress.toUpperCase())) {
				javelinID = bleDev;
			}
		}
		if (javelinID.isEmpty()) {

		} else {
			System.out.println(javelinID);
		}

	}
	
	
	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// TODO Auto-generated method stub
		//BTHLE\Dev_e7452c6d6f14

		JavelinBLERadioByteCommunication ble = new JavelinBLERadioByteCommunication("e7:45:2c:6d:6f:14");
		try {
			ble.connect();
			ble.getStatus();
			Thread.sleep(1000);
			ble.disconnect();
			//BTHLE\Dev_daa619f04ad7
			/*
		JavelinBLERadioByteCommunication ble2 = new JavelinBLERadioByteCommunication("da:a6:19:f0:4a:d7",null);
		ble2.connect();
			 */
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}

	@Override
	public void connect() throws ShimmerException {
		// TODO Auto-generated method stub
		String l_name = javelin.getBLEDeviceName(javelinID);
		String l_services[] = javelin.listBLEDeviceServices(javelinID);
		//String l_chars[] = javelin.listBLEServiceCharacteristics(l_device, l_services[2]);
		String l_chars[] = javelin.listBLEServiceCharacteristics(javelinID, ServiceUUID);
		System.out.println("  Name: "+l_name);
		boolean connected = javelin.watchBLECharacteristicChanges(javelinID,
				ServiceUUID,
				RXUUID);
		if (connected) {
			if (MyRunnable.running) {
				MyRunnable.stringKeyMap.put(javelinID,mByteCommunicationListener);
			} else {
				Thread thread = new Thread(new MyRunnable(javelinID,mByteCommunicationListener));
				thread.start(); // Start the thread
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(l_name + " Connected");
			mByteCommunicationListener.eventConnected();
		} else {
			mByteCommunicationListener.eventDisconnected();
			throw new ShimmerException();
		}
	}
	public void getStatus() {
		/*
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(javelinID,
				ServiceUUID,
				TXUUID,
				new byte[] {0x11,0x00,0x00}));
		 */
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(javelinID,
				ServiceUUID,
				TXUUID,
				ReadStatusRequest));

		byte[] l_bytes = javelin.waitForBLECharacteristicChanges(javelinID,
				ServiceUUID,
				RXUUID, 10000);
		System.out.print("Wait 1 said: ");
		if(l_bytes!=null) for(byte l_byte : l_bytes)
		{
			System.out.print(" "+l_byte);
		}
	}
	@Override
	public void disconnect() throws ShimmerException {

		/*
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(javelinID,
				ServiceUUID,
				TXUUID,
				new byte[] {0x11,0x00,0x00}));
		 */
		/*
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(javelinID,
				ServiceUUID,
				TXUUID,
				DisconnectRequest));

		byte[] l_bytes = javelin.waitForBLECharacteristicChanges(javelinID,
				ServiceUUID,
				RXUUID, 10000);
		System.out.print("Wait 1 said: ");
		if(l_bytes!=null) for(byte l_byte : l_bytes)
		{
			System.out.print(" "+l_byte);
		}
		 */
		/*
		javelin.unWatchBLECharacteristicChanges(javelinID, ServiceUUID, RXUUID);
		javelin.unWatchBLECharacteristicChanges(javelinID, ServiceUUID, TXUUID);
		javelin.clearBLECharacteristicChanges(javelinID, ServiceUUID, RXUUID);
		javelin.clearBLECharacteristicChanges(javelinID, ServiceUUID, TXUUID);
		*/
		//mByteCommunicationListener.eventDisconnected();

	}

	@Override
	public void writeBytes(byte[] bytes) {
		// TODO Auto-generated method stub
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(javelinID,
				ServiceUUID,
				TXUUID,
				bytes));
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class MyRunnable implements Runnable {
		public static Map<String, ByteCommunicationListener> stringKeyMap = new ConcurrentHashMap<>();
		
		public static boolean running = false;
		public MyRunnable(String javelinID,ByteCommunicationListener listener){
			
			stringKeyMap.put(javelinID, listener);
		}
		
		public void run() {
			running = true;	
			// Thread's code here
			while(true) {
				Set<String> keySet = stringKeyMap.keySet();
				for (String key : keySet) {
					System.out.print(key + ": wait for ble");
					byte[] l_bytes = javelin.waitForBLECharacteristicChanges(key,
							ServiceUUID,
							RXUUID, 1);
					if(l_bytes!=null) {
						if (l_bytes.length>0) {
							stringKeyMap.get(key).eventNewBytesReceived(l_bytes);
							System.out.print(key + ": ");
							for(byte l_byte : l_bytes)
							{
								System.out.print(" "+l_byte);
							}
						}
					}
				}

			}
		}
	}

}
