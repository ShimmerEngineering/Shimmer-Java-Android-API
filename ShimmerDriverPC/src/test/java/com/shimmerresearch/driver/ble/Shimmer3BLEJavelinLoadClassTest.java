package com.shimmerresearch.driver.ble;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

public class Shimmer3BLEJavelinLoadClassTest {
	public static Shimmer3BLEJavelinLoadClassTest s_t;
	static byte[] l_bytes = {0x3F};//hwid
	static byte[] t_bytes = {0x06};
	//static String l_device = "BluetoothLE#BluetoothLE8c:b8:7e:0b:48:2e-e8:eb:1b:97:67:ad";
	static String l_device = "BluetoothLE#BluetoothLE8c:b8:7e:0b:48:2e-e8:eb:1b:93:68:dd";
	//static String l_device = "BluetoothLE#BluetoothLE8c:b8:7e:0b:48:2e-e8:eb:1b:71:3e:36";
	static String l_name = "";
	static URLClassLoader classLoader;
	static Class<?> loadedClass;
	public Shimmer3BLEJavelinLoadClassTest()
	{
		Object instance;
		try {
			instance = loadedClass.newInstance();

			System.out.println("Hello");


			Method method = loadedClass.getMethod("listBLEDevices");

			method.invoke(instance);
			System.out.println("Back in Java:");
			//if(l_devices != null)
			{

				/*l_name = javelin.getBLEDeviceName(l_device);
			String l_services[] = javelin.listBLEDeviceServices(l_device);
			//String l_chars[] = javelin.listBLEServiceCharacteristics(l_device, l_services[2]);
			String l_chars[] = javelin.listBLEServiceCharacteristics(l_device, "49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase());
			System.out.println("  Name: "+l_name);
			boolean connected = javelin.watchBLECharacteristicChanges(l_device,
					"49535343-fe7d-4ae5-8fa9-9fafd205e455".toUpperCase(),
					"49535343-1e4d-4bd9-ba61-23c647249616".toUpperCase());
			if (connected) {
				//testToggleLED();
				//testHWID();
				//testFWID();
				//startStreaming();
			}
				 */
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static String getAbsoluteDLLPath(String dllPath) {
		// Get the absolute path to the JAR file's directory
		String jarDirectory;
		try {
			jarDirectory = new File(Shimmer3BLEJavelinLoadClassTest.class.getProtectionDomain()
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

			
			// Define the relative path to the DLL
			String dllPath = "libs/javelin.dll"; // Replace with the actual relative path

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
			


			// Replace this with the path to your JAR file

			// Create a URL representing the JAR file
			File jarFile = new File(getAbsoluteDLLPath("libs/javelin.jar"));
			URL jarUrl = jarFile.toURI().toURL();

			// Create a class loader with the JAR file's URL
			classLoader = new URLClassLoader(new URL[] { jarUrl });

			// Load a class from the JAR file (replace with the class you need)
			String className = "javelin.javelin";
			loadedClass = classLoader.loadClass(className);

			// Instantiate the loaded class (replace with your specific logic)
			Object instance = loadedClass.newInstance();

			// Now you can work with the loaded class and its methods/fields
			System.out.println("Loaded class: " + loadedClass.getName());
			// Define the relative path to the DLL
			
			// Load the DLL
			

			// Now you can use native methods from the loaded DLL using JNI
			// For example: NativeClass.nativeMethod();
		} catch (Exception e) {
			e.printStackTrace();
		}


		//System.loadLibrary("javelin");	
		System.out.println("dlls loaded");
		s_t = new Shimmer3BLEJavelinLoadClassTest();
	}

}
