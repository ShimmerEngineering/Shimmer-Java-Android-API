package com.shimmerresearch.driver.ble;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFrame;

import javelin.javelin;

public class Shimmer3BLEJavelinTest {
	public static Shimmer3BLEJavelinTest s_t;
	static byte[] l_bytes = {0x3F};//hwid
	static byte[] t_bytes = {0x06};
	//static String l_device = "BluetoothLE#BluetoothLE8c:b8:7e:0b:48:2e-e8:eb:1b:97:67:ad";
	static String l_device = "BluetoothLE#BluetoothLE8c:b8:7e:0b:48:2e-e8:eb:1b:93:68:dd";
	//static String l_device = "BluetoothLE#BluetoothLE8c:b8:7e:0b:48:2e-e8:eb:1b:71:3e:36";
	static String l_name = "";
	static URLClassLoader classLoader;
	static Class<?> loadedClass;
	public Shimmer3BLEJavelinTest()
	{
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println("Hello");
		String l_devices[] = javelin.listBLEDevices();
		System.out.println("Back in Java:");
		if(l_devices != null)
		{

			l_name = javelin.getBLEDeviceName(l_device);
			String l_services[] = javelin.listBLEDeviceServices(l_device);
			//String l_chars[] = javelin.listBLEServiceCharacteristics(l_device, l_services[2]);
			String l_chars[] = javelin.listBLEServiceCharacteristics(l_device, "49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase());
			System.out.println("  Name: "+l_name);
			boolean connected = javelin.watchBLECharacteristicChanges(l_device,
					"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
					"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase());
			if (connected) {
				testToggleLED();
				testHWID();
				testFWID();
				startStreaming();
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//stopStreaming();
				
				//disconnect();
			}
		}
	}
	
	public static void disconnect() {
		
		
		javelin.clearBLECharacteristicChanges(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase());
		//javelin.disconnectDevice(l_device);
		
	}

	public static void startStreaming() {
		Thread thread = new Thread(new MyRunnable());
		thread.start(); // Start the thread
	}

	public static void stopStreaming() {
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-8841-43f4-a8d4-ecbe34729bb3".toUpperCase(),
				new byte[] {0x20}));
	}
	
	public static void testToggleLED() {
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-8841-43f4-a8d4-ecbe34729bb3".toUpperCase(),
				new byte[] {0x06}));
		l_bytes = javelin.waitForBLECharacteristicChanges(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase(), 10000);
		System.out.print("Wait 1 said: ");
		if(l_bytes!=null) for(byte l_byte : l_bytes)
		{
			System.out.print(" "+l_byte);
		}
	}

	public static void testHWID() {
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-8841-43f4-a8d4-ecbe34729bb3".toUpperCase(),
				new byte[] {0x3f}));
		l_bytes = javelin.waitForBLECharacteristicChanges(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase(), 10000);
		System.out.print("Wait 1 said: ");
		if(l_bytes!=null) for(byte l_byte : l_bytes)
		{
			System.out.print(" "+l_byte);
		}
	}

	public static void testFWID() {
		System.out.println("Set char said: "+javelin.setBLECharacteristicValue(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-8841-43f4-a8d4-ecbe34729bb3".toUpperCase(),
				new byte[] {0x2e}));
		l_bytes = javelin.waitForBLECharacteristicChanges(l_device,
				"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
				"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase(), 10000);
		System.out.print("Wait 1 said: ");
		if(l_bytes!=null) for(byte l_byte : l_bytes)
		{
			System.out.print(" "+l_byte);
		}
	}

    public static String getAbsoluteDLLPath(String dllPath) {
		// Get the absolute path to the JAR file's directory
		String jarDirectory;
		try {
			jarDirectory = new File(Shimmer3BLEJavelinTest.class.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI())
					.getParent();
			String directory = new File(jarDirectory).getParent();
			String absoluteDLLPath = new File(directory, dllPath).getAbsolutePath();
			return absoluteDLLPath;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ""; 	
		
    	
    }

	public static void main(String[] args) {
		try {
			
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

		} catch (Exception e) {
			e.printStackTrace();
		}


		//System.loadLibrary("javelin");	
		System.out.println("dlls loaded");
		s_t = new Shimmer3BLEJavelinTest();
	}
	public static class MyRunnable implements Runnable {
		public void run() {
			System.out.println("Set char said: "+javelin.setBLECharacteristicValue(l_device,
					"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
					"49535343-8841-43f4-a8d4-ecbe34729bb3".toUpperCase(),
					new byte[] {0x07}));
			// Thread's code here
			while(true) {
				l_bytes = javelin.waitForBLECharacteristicChanges(l_device,
						"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
						"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase(), 100);
				System.out.print(l_name + ": ");
				if(l_bytes!=null) for(byte l_byte : l_bytes)
				{
					System.out.print(" "+l_byte);
				}
			}
		}
	}
}
