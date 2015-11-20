package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class AbstractSensor implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3465427544416038676L;
	protected String mSensorName;
	protected String[] mSignalOutputNameArray;
	protected String[] mSignalOutputFormatArray;
	protected String[] mSignalOutputUnitArray;
	protected int mFirmwareType;
	protected int mHardwareID;
	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor 
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
	
	/** Each communication type might have a different Integer key representing the channel, e.g. BT Stream inquiry response (holds the channel sequence of the packet)
	 * 
	 */
	public HashMap<COMMUNICATION_TYPE,HashMap<Integer,ChannelDetails>> mMapOfChannel = new HashMap<COMMUNICATION_TYPE,HashMap<Integer,ChannelDetails>>(); 
	public abstract String getSensorName();
	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE comType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet,COMMUNICATION_TYPE comType);
	public abstract Object processData(byte[] rawData,int FWType, int sensorFWID);
	
	
	/**
	 * @param svo
	 * @return
	 */
	public abstract HashMap<COMMUNICATION_TYPE,HashMap<Integer,ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo);
	/**
	 * @param svo
	 * @return
	 */
	public abstract HashMap<String,SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo);
	
	
	public final static class SHIMMER3_BT_STREAM_CHANNEL_ID {
		public final static int GSR = 1;
		public final static int ECG = 1;
	}
	
	public final static class GQ_CHANNEL_ID {
		public final static int GSR = 1;
	}
	
	public AbstractSensor(ShimmerVerObject svo){
		mConfigOptionsMap = generateConfigOptionsMap(svo);
		mMapOfChannel = generateChannelDetailsMap(svo);
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
