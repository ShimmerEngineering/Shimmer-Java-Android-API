package com.shimmerresearch.driver;

import java.awt.Dimension;

public class OsInfo {
	
	//to get the OS
	public final static String OS_SYSTEM_PROPERTY = System.getProperty("os.name").toLowerCase();
	
	public static OS OS_CURRENT = OS.UNCHECKED;
	public static enum OS {
		UNCHECKED,
		UNKNOWN,
		WINDOWS,
		MAC,
		LINUX
	}
	
	public static void checkOS(){
		if((OS_SYSTEM_PROPERTY.indexOf("win") >= 0)){
			OS_CURRENT = OS.WINDOWS;
			return;
		}
		else if((OS_SYSTEM_PROPERTY.indexOf("mac") >= 0)){
			OS_CURRENT = OS.MAC;
			return;
		}
		else if((OS_SYSTEM_PROPERTY.indexOf("nix") >= 0 || OS_SYSTEM_PROPERTY.indexOf("nux") >= 0 || OS_SYSTEM_PROPERTY.indexOf("aix") > 0 )){
			OS_CURRENT = OS.LINUX;
			return;
		}
		else {
			OS_CURRENT = OS.UNKNOWN;
			return;
		}
	}

}
