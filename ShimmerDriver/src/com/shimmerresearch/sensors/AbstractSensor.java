package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class AbstractSensor implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 3465427544416038676L;

	public enum SENSORS{
		GSR("GSR"),
		ECG_TO_HR("ECG to Heart Rate"),
		EXG("EXG"),
		CLOCK("Clock"),
		SYSTEM_TIMESTAMP("PC time"),
		MPU9X50("MPU Accel"),
		BMP180("BMP180"),
		KIONIXKXRB52042("Analog Accelerometer");
		
	    private final String text;

	    /** @param text */
	    private SENSORS(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	// --------------- Abstract methods start ----------------
	public abstract void generateSensorMap(ShimmerVerObject svo);
//	public abstract LinkedHashMap<Integer, ChannelDetails> generateChannelDetailsMap(ShimmerVerObject svo);
	public abstract void generateConfigOptionsMap(ShimmerVerObject svo);
//	public abstract List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo);
//	public abstract List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo);
	public abstract void generateSensorGroupMapping(ShimmerVerObject svo);

	public abstract ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster);
	
	public abstract void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet);
	public abstract Object getConfigValueUsingConfigLabel(String componentName);

	public abstract void setSamplingRateFromFreq();
	public abstract void setDefaultConfiguration();
	public abstract Map<String, SensorGroupingDetails> getSensorGroupingMap();
	
	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);
	// --------------- Abstract methods end ----------------	

//	// --------------- Carried from static SensorDetails() start ----------------	
//	/**
//	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
//	 */
//	public long mSensorBitmapIDStreaming = 0;
//	/**
//	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
//	 */
//	public long mSensorBitmapIDSDLogHeader = 0;
//	
//	public String mGuiFriendlyLabel = "";
//	public List<Integer> mListOfSensorMapKeysRequired = new ArrayList<Integer>();
//	public List<Integer> mListOfSensorMapKeysConflicting = new ArrayList<Integer>();
//	public boolean mIntExpBoardPowerRequired = false;
//	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
//	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();  
//
//	public boolean mIsDummySensor = false;
//	
////	//Testing for GQ BLE
////	public String mHeaderFileLabel = "";
////	public int mHeaderByteMask = 0;
////	public int mNumChannels = 0;
//	// --------------- Carried from static SensorDetails() end ----------------	
//
//	// --------------- Carried from SensorEnabledDetails() start ----------------	
//	public boolean mIsEnabled = false;
//	public long mDerivedSensorBitmapID = 0;
//	public SensorDetails mSensorDetails;
//	public List<String> mListOfChannels = new ArrayList<String>();
//	// --------------- Carried from SensorEnabledDetails() end ----------------	


	protected String mSensorName;
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	
	protected boolean mEnableCalibration = true;
	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	protected String[] mSignalOutputNameArray;
	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	protected String[] mSignalOutputFormatArray;
	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	protected String[] mSignalOutputUnitArray;
	//TODO remove below?
	protected int mFirmwareType;
//	protected int mHardwareID;
//	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor
	
	//public LinkedHashMap<String,ChannelDetails> mMapOfChannels = new LinkedHashMap<String,ChannelDetails>();
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
//    public Map<Integer, SensorDetails> mSensorMapRef = new LinkedHashMap<Integer, SensorDetails>();
	
    public Map<String, SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
//	public HashMap<COMMUNICATION_TYPE, TreeMap<String, SensorGroupingDetails>> mSensorGroupingMap = new LinkedHashMap<COMMUNICATION_TYPE,TreeMap<String, SensorGroupingDetails>>();

    
//	public HashMap<COMMUNICATION_TYPE,TreeMap<Integer, SensorEnabledDetails>> mSensorEnabledMap = new HashMap<COMMUNICATION_TYPE,TreeMap<Integer, SensorEnabledDetails>>();
	public TreeMap<Integer, SensorDetails> mSensorMap = new TreeMap<Integer, SensorDetails>();
	/**
	 * Each communication type might have a different Integer key representing
	 * the channel, e.g. BT Stream inquiry response (holds the channel sequence
	 * of the packet)
	 */
//	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> mMapOfCommTypetoChannel = new HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>>(); 
//	public LinkedHashMap<Integer, ChannelDetails> mMapOfChannelDetails = new LinkedHashMap<Integer, ChannelDetails>(); 
	

	
//	/** To process data originating from the Shimmer device
//	 * @param channelByteArray The byte array packet, or byte array sd log
//	 * @param commType The communication type
//	 * @param object The packet/objectCluster to append the data to
//	 * @return
//	 */
//	public static ObjectCluster processShimmerChannelData(byte[] channelByteArray, ChannelDetails channelDetails, ObjectCluster objectCluster){
//
////		if (channelDetails.mIsEnabled){
////			//byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
////			long rawData = parsedData(channelByteArray,channelDetails.mDefaultChannelDataType,channelDetails.mDefaultChannelDataEndian);
////			ObjectCluster objectCluster = (ObjectCluster) object;
////			objectCluster.mPropertyCluster.put(channelDetails.mObjectClusterName,new FormatCluster(channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString(),channelDetails.mDefaultUnit,(double)rawData));
////			objectCluster.mSensorNames[objectCluster.indexKeeper] = channelDetails.mObjectClusterName;
////			if (channelDetails.mChannelFormatDerivedFromShimmerDataPacket==CHANNEL_TYPE.UNCAL){
////				objectCluster.mUncalData[objectCluster.indexKeeper]=(double)rawData;
////				objectCluster.mUnitUncal[objectCluster.indexKeeper]=channelDetails.mDefaultUnit;	
////			} else if (channelDetails.mChannelFormatDerivedFromShimmerDataPacket==CHANNEL_TYPE.CAL){
////				objectCluster.mCalData[objectCluster.indexKeeper]=(double)rawData;
////				objectCluster.mUnitCal[objectCluster.indexKeeper]=channelDetails.mDefaultUnit;
////			}
////			
////		}
//
//		if(channelDetails.mIsEnabled){
//			long parsedChannelData = UtilParseData.parseData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
//			objectCluster.addData(channelDetails.mObjectClusterName, channelDetails.mChannelFormatDerivedFromShimmerDataPacket, channelDetails.mDefaultUnit, (double)parsedChannelData);
//		}
//
//		return objectCluster;
////		return object;
//	
//	}
	
	
	public AbstractSensor(ShimmerVerObject svo){
		mShimmerVerObject = svo;
		
		generateSensorMap(svo);
		generateConfigOptionsMap(svo);
		generateSensorGroupMapping(svo);

//		mMapOfChannelDetails = generateChannelDetailsMap(svo);
//		// Null if not implemented in the Sensor class
//		if(mMapOfChannelDetails==null){
////			mMapOfCommTypetoChannel = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>>();
//			mMapOfChannelDetails = new LinkedHashMap<Integer,ChannelDetails>();
//		}
//		
//		mListOfConfigOptionKeysAssociated = generateListOfConfigOptionKeysAssociated(svo);
//		if(mListOfConfigOptionKeysAssociated == null){
//			mListOfConfigOptionKeysAssociated = new ArrayList<String>() ;
//		}
//		
//		mListOfSensorMapKeysConflicting = generateListOfSensorMapKeysConflicting(svo);
//		if(mListOfConfigOptionKeysAssociated == null){
//			mListOfConfigOptionKeysAssociated = new ArrayList<String>() ;
//		}
		
	}
	
	

	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	/**
	 * This returns a String array of the output signal name, the sequence of
	 * the format array MUST MATCH the array returned by the method
	 * returnSignalOutputFormatArray
	 * 
	 * @return mSignalOutputNameArray
	 */
	public String[] getSignalOutputNameArray() {
		return mSignalOutputNameArray;
	}

	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	/**
	 * This returns a String array of the output signal format, the sequence of
	 * the format array MUST MATCH the array returned by the method
	 * returnSignalOutputNameArray
	 * 
	 * @return mSignalOutputFormatArray
	 */
	public String[] getSignalOutputFormatArray() {
		return mSignalOutputFormatArray;
	}

	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	/**
	 * This returns a String array of the output signal format, the sequence of
	 * the format array MUST MATCH the array returned by the method
	 * returnSignalOutputNameArray
	 * 
	 * @return mSignalOutputUnitArray
	 */
	public String[] getSignalOutputUnitArray() {
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
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorEnabledDetails = iterator.next();
			if (sensorEnabledDetails.isEnabled(commType)){
				for(ChannelDetails channelDetails:sensorEnabledDetails.mListOfChannels){
					count += channelDetails.mDefaultNumBytes;
				}
			}
		}

//		int count = 0; 
//		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfChannelDetails.get(commType);
//		if(channelsPerCommType!=null){
//			for (ChannelDetails channelDetails:channelsPerCommType.values()){
//				if (channelDetails.mIsEnabled){
//					count = count+channelDetails.mDefaultNumBytes;
//				}
//			}
//		}
		return count;
	}

	public int getNumberOfEnabledChannels(COMMUNICATION_TYPE commType){
		int count = 0;
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorEnabledDetails = iterator.next();
			if (sensorEnabledDetails.isEnabled(commType)){
				count += sensorEnabledDetails.mListOfChannels.size();
			}
		}
		
//		int count = 0;
//		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfChannelDetails.get(commType);
//		if(channelsPerCommType!=null){
//			for (ChannelDetails channelDetails:mMapOfChannelDetails.get(commType).values()){
//				if (channelDetails.mIsEnabled){
//					count = count+1;
//				}
//			}
//		}
		return count;
	}

	public boolean isAnySensorChannelEnabled(COMMUNICATION_TYPE commType){
		return (getNumberOfEnabledChannels(commType)>0? true:false);
	}

	public void setIsEnabledSensorChannels(COMMUNICATION_TYPE commType, boolean state){
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorEnabledDetails = iterator.next();
			sensorEnabledDetails.setIsEnabled(commType, state);
		}

//		LinkedHashMap<Integer, ChannelDetails> channelsPerCommType = mMapOfChannelDetails.get(commType);
//		if(channelsPerCommType!=null){
//			for (ChannelDetails channelDetails:channelsPerCommType.values()){
//				channelDetails.mIsEnabled = false;
//			}
//		}
	}
	
	//TODO MN: under devel
	public void updateStateFromEnabledSensorsVars(COMMUNICATION_TYPE commType, long enabledSensors, long derivedSensors) {
//		TreeMap<Integer, SensorEnabledDetails> sensorMapForCommType = mSensorEnabledMap.get(commType);
		
		for(SensorDetails sensorEnabledDetails:mSensorMap.values()){
			sensorEnabledDetails.setIsEnabled(commType, (enabledSensors & sensorEnabledDetails.mSensorDetails.mSensorBitmapIDStreaming)>0? true:false);
		}
		
		
//		//TODO: enabledSensors should be directed at channels coming from the Shimmer, derivedSensors at channels from the API 
//		//TODO move to abstact or override in the extended sensor classes so complexities like EXG can be handled
//		mIsEnabled = false;
//		boolean state = (enabledSensors & mSensorBitmapIDStreaming)>0? true:false;
//		mIsEnabled = state;
//		setSensorChannelsState(commType, state);
	}
	
	
	public void updateSensorGroupingMap() {
		for (String sensorGroup:mSensorGroupingMap.keySet()) {
			// Ok to clear here because variable is initiated in the class
			mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.clear();
			for (Integer sensor:mSensorGroupingMap.get(sensorGroup).mListOfSensorMapKeysAssociated) {
				
				SensorDetails sensorEnabledDetails = mSensorMap.get(sensor);
				if(sensorEnabledDetails!=null){
					for (String configOption:sensorEnabledDetails.mSensorDetails.mListOfConfigOptionKeysAssociated) {
						// do not add duplicates
						if (!(mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
							mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.add(configOption);
						}
					}
				}
				
//				if(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_3){
//					if(Configuration.Shimmer3.mSensorMapRef.containsKey(sensor)){
//						List<String> associatedConfigOptions = Configuration.Shimmer3.mSensorMapRef.get(sensor).mListOfConfigOptionKeysAssociated;
//						if (associatedConfigOptions != null) {
//							for (String configOption:associatedConfigOptions) {
//								// do not add duplicates
//								if (!(mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
//									mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.add(configOption);
//								}
//							}
//						}
//					}
//				}
//				
//				else {
////				else if((mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR)
////						||(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR)
////						||(mShimmerVerObject.mHardwareVersion==HW_ID.SHIMMER_2R_GQ)){
//					
////					for(SensorEnabledDetails sensorEnabledDetails:sensorMapForCommType.values()){
////						
////					}
//					for (String configOption:mListOfConfigOptionKeysAssociated) {
//						// do not add duplicates
//						if (!(mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
//							mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated.add(configOption);
//						}
//					}
//				}
			}
		}
	}

	public String getSensorGuiFriendlyLabel(int sensorKey) {
		SensorDetails sensorDetails = mSensorMap.get(sensorKey);
		if(sensorDetails!=null){
			return sensorDetails.mSensorDetails.mGuiFriendlyLabel;
		}
		return null;
	}

	public List<ShimmerVerObject> getSensorListOfCompatibleVersionInfo(int sensorKey) {
		SensorDetails sensorDetails = mSensorMap.get(sensorKey);
		if(sensorDetails!=null){
			return sensorDetails.mSensorDetails.mListOfCompatibleVersionInfo;
		}
		return null;
	}
	
}
