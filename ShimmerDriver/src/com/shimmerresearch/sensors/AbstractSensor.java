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
	/** call either createLocalSensorMap() or createLocalSensorMapWithCustomParser() inside depending if a custom parser is needed. */
	public abstract void generateSensorMap(ShimmerVerObject svo);
	public abstract void generateConfigOptionsMap(ShimmerVerObject svo);
	public abstract void generateSensorGroupMapping(ShimmerVerObject svo);

	/** for use only if a custom parser is required, i.e. for calibrated data. Use in conjunction with createLocalSensorMapWithCustomParser()*/ 
	public abstract ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster);
//	public abstract ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster);
	
	public abstract void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet);
	public abstract Object getConfigValueUsingConfigLabel(String componentName);

	public abstract void setSamplingRateFromFreq();
	public abstract boolean setDefaultConfiguration(int sensorMapKey, boolean state);
	/** TODO populate in individual AbstractSensor classes the relevent entries from ShimmerObject */
	public abstract boolean checkConfigOptionValues(String stringKey);

	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);
	// --------------- Abstract methods end ----------------	

	protected String mSensorName;
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	
	protected boolean mEnableCalibration = true;
	
	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	protected String[] mSignalOutputNameArray;
	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	protected String[] mSignalOutputFormatArray;
	//TODO: below belongs in ChannelDetails and not in AbstractSensor?
	protected String[] mSignalOutputUnitArray;
	
	protected Double mMaxSetShimmerSamplingRate = 51.2;
	
	//TODO remove below?
	protected int mFirmwareType;
//	protected int mHardwareID;
//	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor
	
	public TreeMap<Integer, SensorDetails> mSensorMap = new TreeMap<Integer, SensorDetails>();
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
    public LinkedHashMap<String, SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();

	
	public AbstractSensor(ShimmerVerObject svo){
		mShimmerVerObject = svo;
		generateSensorMap(svo);
		generateConfigOptionsMap(svo);
		generateSensorGroupMapping(svo);
	}

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
		return mConfigOptionsMap;
	}
	
	public String getSensorName() {
		return mSensorName;
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
			mSensorGroupingMap.get(sensorGroup).mListOfConfigOptionKeysAssociated = new ArrayList<String>();
			for (Integer sensorKey:mSensorGroupingMap.get(sensorGroup).mListOfSensorMapKeysAssociated) {
				SensorDetails sensorEnabledDetails = mSensorMap.get(sensorKey);
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
	
	public void createLocalSensorMap(Map<Integer, SensorDetailsRef> sensorMapRef, Map<String, ChannelDetails> channelMapRef) {
		mSensorMap.clear();
		for(int sensorMapKey:sensorMapRef.keySet()){
			SensorDetailsRef sensorDetailsRef = sensorMapRef.get(sensorMapKey);
			SensorDetails sensorDetails = new SensorDetails(false, 0, sensorDetailsRef);
			updateSensorDetailsWithChannels(sensorDetails, channelMapRef);
			mSensorMap.put(sensorMapKey, sensorDetails);
		}
	}
	
	public void createLocalSensorMapWithCustomParser(Map<Integer, SensorDetailsRef> sensorMapRef, Map<String, ChannelDetails> channelMapRef) {
		mSensorMap.clear();
		for(int sensorMapKey:sensorMapRef.keySet()){
			SensorDetailsRef sensorDetailsRef = sensorMapRef.get(sensorMapKey);
			SensorDetails sensorDetails = new SensorDetails(false, 0, sensorDetailsRef){
				@Override
				public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster object) {
					return processDataCustom(this, rawData, commType, object);
//					return super.processData(rawData, commType, object);
				}
			};
			updateSensorDetailsWithChannels(sensorDetails, channelMapRef);
			mSensorMap.put(sensorMapKey, sensorDetails);
		}
	}

	public void updateSensorDetailsWithChannels(SensorDetails sensorDetails, Map<String, ChannelDetails> channelMapRef){
		for(String channelKey:sensorDetails.mSensorDetails.mListOfChannelsRef){
			ChannelDetails channelDetails = channelMapRef.get(channelKey);
			if(channelDetails!=null){
				sensorDetails.mListOfChannels.add(channelDetails);
			}
		}
	}
	
	public HashMap<String, SensorConfigOptionDetails> getConfigOptionsMap() {
		return mConfigOptionsMap;
	}
	
	public LinkedHashMap<String, SensorGroupingDetails> getSensorGroupingMap() {
		return mSensorGroupingMap;
	}

	protected boolean isSensorEnabled(int sensorMapKey){
		if(mSensorMap!=null) {
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
			if(sensorDetails!=null){
				return sensorDetails.isEnabled();
			}
		}		
		return false;
	}
	
	public void setMaxSetShimmerSamplingRate(double maxSetRate) {
		mMaxSetShimmerSamplingRate = maxSetRate;
	}
	


}