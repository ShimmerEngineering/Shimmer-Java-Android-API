package com.shimmerresearch.driverUtilities;

public class ConfigOptionObject{
	int index;
	String[] guiValues;
	Integer[] configValues;
	
	public ConfigOptionObject(int index, String[] guiValues, Integer[] configValues){
		this.index = index;
		this.guiValues = guiValues;
		this.configValues = configValues;
	}
}