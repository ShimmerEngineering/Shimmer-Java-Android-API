package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class AbstractSensor implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 3465427544416038676L;

	//TODO decide whether to use Configuration.Shimmer3.SensorMapKey
	public enum SENSORS{
//		/** Shimmer3 Low-noise analog accelerometer */
//		A_ACCEL(""),
//		/** Shimmer3 Gyroscope */
//		MPU9150_GYRO(""),
//		/** Shimmer3 Primary magnetometer */
//		LSM303DLHC_MAG(""),
//		UNUSED1("UNUSED1"),//EXG1_24BIT(""),
//		UNUSED2("UNUSED2"),//EXG2_24BIT(""),

		GSR("GSR", Configuration.Shimmer3.SensorMapKey.GSR),
		ECG_TO_HR("ECG to Heart Rate", Configuration.Shimmer3.SensorMapKey.ECG_TO_HR_FW),
		EXG("EXG", Configuration.Shimmer3.SensorMapKey.ECG),
		CLOCK("Clock", Configuration.Shimmer3.SensorMapKey.TIMESTAMP),
		SYSTEM_TIMESTAMP("PC time", Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK_SYNC),
		MPU9X50("MPU Accel", Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL),
		BMP180("BMP180",Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE);
		
	    private final String text;
	    private final int index;

	    /** @param text */
	    private SENSORS(final String text, final int index) {
	        this.text = text;
	        this.index = index;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	    
	    public int sensorIndex() {
	        return index;
	    }

	}
	
//	public final static class SHIMMER3_BT_STREAM_CHANNEL_ID {
//	public final static int GSR = 1;
//	public final static int ECG = 1;
//}
//public final static class GQ_CHANNEL_ID {
//	public final static int GSR = 1;
//}
	
	// --------------- Abstract methods start ----------------	
	public abstract ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster object);
	
	public abstract void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet);
	public abstract Object getConfigValueUsingConfigLabel(String componentName);

	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);

	public abstract void setSamplingRateFromFreq();
	public abstract void setDefaultConfiguration();
	public abstract Map<String, SensorGroupingDetails> getSensorGroupingMap();
	
	public abstract HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo);
	public abstract HashMap<String,SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo);
	public abstract List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo);
	public abstract List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo);
	public abstract Map<String, SensorGroupingDetails> generateSensorGroupMapping(ShimmerVerObject svo);

	// --------------- Abstract methods end ----------------	

	// --------------- Carried from static SensorDetails() start ----------------	
	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader = 0;
	
	public String mGuiFriendlyLabel = "";
	public List<Integer> mListOfSensorMapKeysRequired = new ArrayList<Integer>();
	public List<Integer> mListOfSensorMapKeysConflicting = new ArrayList<Integer>();
	public boolean mIntExpBoardPowerRequired = false;
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();  

	public boolean mIsDummySensor = false;
	
//	//Testing for GQ BLE
//	public String mHeaderFileLabel = "";
//	public int mHeaderByteMask = 0;
//	public int mNumChannels = 0;
	// --------------- Carried from static SensorDetails() end ----------------	

	// --------------- Carried from SensorEnabledDetails() start ----------------	
	public boolean mIsEnabled = false;
	public long mDerivedSensorBitmapID = 0;
	public SensorDetails mSensorDetails;
	public List<String> mListOfChannels = new ArrayList<String>();
	// --------------- Carried from SensorEnabledDetails() end ----------------	


	protected String mSensorName;
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	
	protected boolean mEnableCalibration = true;
	protected String[] mSignalOutputNameArray;
	protected String[] mSignalOutputFormatArray;
	protected String[] mSignalOutputUnitArray;
	protected int mFirmwareType;
//	protected int mHardwareID;
//	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor
	
	//public LinkedHashMap<String,ChannelDetails> mMapOfChannels = new LinkedHashMap<String,ChannelDetails>();
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
//    public Map<Integer, SensorDetails> mSensorMapRef = new LinkedHashMap<Integer, SensorDetails>();
    public Map<String, SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();

//	@Deprecated
//	public SensorEnabledDetails mSensorEnabledDetails;
	@Deprecated
	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>> mMapOfCommTypeToSensorMap = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>>();
	/**
	 * Each communication type might have a different Integer key representing
	 * the channel, e.g. BT Stream inquiry response (holds the channel sequence
	 * of the packet)
	 */
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> mMapOfCommTypetoChannel = new HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>>(); 
	

	
	/** To process data originating from the Shimmer device
	 * @param channelByteArray The byte array packet, or byte array sd log
	 * @param commType The communication type
	 * @param object The packet/objectCluster to append the data to
	 * @return
	 */
	public ObjectCluster processShimmerChannelData(byte[] channelByteArray, ChannelDetails channelDetails, ObjectCluster objectCluster){

//		if (channelDetails.mIsEnabled){
//			//byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//			long rawData = parsedData(channelByteArray,channelDetails.mDefaultChannelDataType,channelDetails.mDefaultChannelDataEndian);
//			ObjectCluster objectCluster = (ObjectCluster) object;
//			objectCluster.mPropertyCluster.put(channelDetails.mObjectClusterName,new FormatCluster(channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString(),channelDetails.mDefaultUnit,(double)rawData));
//			objectCluster.mSensorNames[objectCluster.indexKeeper] = channelDetails.mObjectClusterName;
//			if (channelDetails.mChannelFormatDerivedFromShimmerDataPacket==CHANNEL_TYPE.UNCAL){
//				objectCluster.mUncalData[objectCluster.indexKeeper]=(double)rawData;
//				objectCluster.mUnitUncal[objectCluster.indexKeeper]=channelDetails.mDefaultUnit;	
//			} else if (channelDetails.mChannelFormatDerivedFromShimmerDataPacket==CHANNEL_TYPE.CAL){
//				objectCluster.mCalData[objectCluster.indexKeeper]=(double)rawData;
//				objectCluster.mUnitCal[objectCluster.indexKeeper]=channelDetails.mDefaultUnit;
//			}
//			
//		}

		if(channelDetails.mIsEnabled){
			long parsedChannelData = UtilParseData.parseData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
			objectCluster.addData(channelDetails.mObjectClusterName, channelDetails.mChannelFormatDerivedFromShimmerDataPacket, channelDetails.mDefaultUnit, (double)parsedChannelData);
		}

		return objectCluster;
//		return object;
	
	}
	
	
	public AbstractSensor(ShimmerVerObject svo){
		mShimmerVerObject = svo;
		
		mConfigOptionsMap = generateConfigOptionsMap(svo);
		// Null if not implemented in the Sensor class
		if(mConfigOptionsMap==null){
			mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
		}
		
		mMapOfCommTypetoChannel = generateChannelDetailsMap(svo);
		// Null if not implemented in the Sensor class
		if(mMapOfCommTypetoChannel==null){
			mMapOfCommTypetoChannel = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>>();
		}
		
		mListOfConfigOptionKeysAssociated = generateListOfConfigOptionKeysAssociated(svo);
		if(mListOfConfigOptionKeysAssociated == null){
			mListOfConfigOptionKeysAssociated = new ArrayList<String>() ;
		}
		
		mListOfSensorMapKeysConflicting = generateListOfSensorMapKeysConflicting(svo);
		if(mListOfConfigOptionKeysAssociated == null){
			mListOfConfigOptionKeysAssociated = new ArrayList<String>() ;
		}
		
		mSensorGroupingMap = generateSensorGroupMapping(svo);
		if(mSensorGroupingMap==null){
			mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
		}
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
	
	public String getSensorName() {
		return mSensorName;
	}
	
	/** This cycles through the channels finding which are enabled and summing up the number of bytes
	 * @param commType
	 * @return
	 */
	public int getExpectedPacketByteArray(COMMUNICATION_TYPE commType) {
		int count = 0; 
		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfCommTypetoChannel.get(commType);
		if(channelsPerCommType!=null){
			for (ChannelDetails channelDetails:channelsPerCommType.values()){
				if (channelDetails.mIsEnabled){
					count = count+channelDetails.mDefaultNumBytes;
				}
			}
		}
		return count;
	}

	public int getNumberOfEnabledChannels(COMMUNICATION_TYPE commType){
		int count = 0;
		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfCommTypetoChannel.get(commType);
		if(channelsPerCommType!=null){
			for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(commType).values()){
				if (channelDetails.mIsEnabled){
					count = count+1;
				}
			}
		}
		return count;
	}

	public boolean isAnySensorChannelEnabled(COMMUNICATION_TYPE commType){
		return (getNumberOfEnabledChannels(commType)>0? true:false);
	}

	public void disableSensorChannels(COMMUNICATION_TYPE commType){
		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfCommTypetoChannel.get(commType);
		if(channelsPerCommType!=null){
			for (ChannelDetails channelDetails:channelsPerCommType.values()){
				channelDetails.mIsEnabled = false;
			}
		}
	}
	
	public void enableSensorChannels(COMMUNICATION_TYPE commType){
		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfCommTypetoChannel.get(commType);
		if(channelsPerCommType!=null){
			for(ChannelDetails channelDetails:channelsPerCommType.values()){
//				if(channelDetails.mChannelSource==CHANNEL_SOURCE.SHIMMER){
					channelDetails.mIsEnabled = true;
//				}
			}
		}
	}
	
	public void setSensorChannelsState(COMMUNICATION_TYPE commType, boolean state){
		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfCommTypetoChannel.get(commType);
		if(channelsPerCommType!=null){
			for (ChannelDetails channelDetails:channelsPerCommType.values()){
//				if(channelDetails.mChannelSource==CHANNEL_SOURCE.SHIMMER){
					channelDetails.mIsEnabled = state;
//				}
			}
		}
	}
	
	//TODO MN: under devel
	public void updateStateFromEnabledSensorsVars(COMMUNICATION_TYPE commType, long enabledSensors, long derivedSensors) {
		//TODO: enabledSensors should be directed at channels coming from the Shimmer, derivedSensors at channels from the API 
		//TODO move to abstact or override in the extended sensor classes so complexities like EXG can be handled
		mIsEnabled = false;
		boolean state = (enabledSensors & mSensorBitmapIDStreaming)>0? true:false;
		mIsEnabled = state;
		setSensorChannelsState(commType, state);
	}
	
	
	public void updateSensorGroupingMap() {
		for (String sensorGroup:mSensorGroupingMap.keySet()) {
			// Ok to clear here because variable is initiated in the class
			mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.clear();
			for (Integer sensor:mSensorGroupingMap.get(sensorGroup).mListOfSensorMapKeysAssociated) {
				
				if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
					if(Configuration.Shimmer3.mSensorMapRef.containsKey(sensor)){
						List<String> associatedConfigOptions = Configuration.Shimmer3.mSensorMapRef.get(sensor).mListOfConfigOptionKeysAssociated;
						if (associatedConfigOptions != null) {
							for (String configOption:associatedConfigOptions) {
								// do not add duplicates
								if (!(mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
									mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.add(configOption);
								}
							}
						}
					}
				}
				
				else {
//				else if((mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR)
//						||(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR)
//						||(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_2R_GQ)){
					for (String configOption:mListOfConfigOptionKeysAssociated) {
						// do not add duplicates
						if (!(mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
							mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.add(configOption);
						}
					}
				}
			}
		}
	}
	
}
