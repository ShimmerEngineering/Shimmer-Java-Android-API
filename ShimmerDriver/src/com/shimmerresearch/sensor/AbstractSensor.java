package com.shimmerresearch.sensor;

import java.util.HashMap;

import com.shimmerresearch.driver.SensorConfigOptionDetails;

public abstract class AbstractSensor {
	protected String mSensorName;
	protected String[] mSignalOutputNameArray;
	protected String[] mSignalOutputFormatArray;
	protected String[] mSignalOutputUnitArray;
	protected int mFirmwareType;
	protected int mHardwareID;
	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor 
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
	public abstract String getSensorName();
	public abstract Object getSettings(String componentName);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet);
	public abstract Object processData(byte[] rawData,int FWType, int sensorFWID);
	
	public AbstractSensor(int hardwareID, int firmwareType){
		mFirmwareType = firmwareType;
		mHardwareID = hardwareID;
	}
	
	/** This returns a String array of the output signal name, the sequence of the format array MUST MATCH the array returned by the method returnSignalOutputFormatArray
	 * @return
	 */
	public String[] getSignalOutputNameArray() {
		// TODO Auto-generated method stub
		return mSignalOutputNameArray;
	}

	/** This returns a String array of the output signal format, the sequence of the format array MUST MATCH the array returned by the method returnSignalOutputNameArray
	 * @return
	 */
	public String[] getSignalOutputFormatArray() {
		// TODO Auto-generated method stub
		return mSignalOutputFormatArray;
	}

	/** This returns a String array of the output signal format, the sequence of the format array MUST MATCH the array returned by the method returnSignalOutputNameArray
	 * @return
	 */
	public String[] getSignalOutputUnitArray() {
		// TODO Auto-generated method stub
		return mSignalOutputUnitArray;
	}
	
	public HashMap<String, SensorConfigOptionDetails> getConfigMap() {
		// TODO Auto-generated method stub
		return mConfigOptionsMap;
	}

}
