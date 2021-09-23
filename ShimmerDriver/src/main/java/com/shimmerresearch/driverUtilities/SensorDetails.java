package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;

public class SensorDetails implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 1545530433767674139L;
	
	/** by default load in communication types for Bluetooth and SD	 */
	public Map<COMMUNICATION_TYPE, Boolean> mapOfIsEnabledPerCommsType = new ConcurrentHashMap<COMMUNICATION_TYPE, Boolean>();
	{
		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.BLUETOOTH, false);
		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.SD, false);
		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.DOCK, false);
//		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.IEEE802154, false);
	}
//	public boolean mIsEnabled = false;
	
	public long mDerivedSensorBitmapID = 0;
	public SensorDetailsRef mSensorDetailsRef;
	public List<ChannelDetails> mListOfChannels = new ArrayList<ChannelDetails>();

	public SensorDetails(){
	}
	
	public SensorDetails(boolean isEnabled, long derivedSensorBitmapID, SensorDetailsRef sensorDetailsRef){
		setIsEnabled(isEnabled);
		mDerivedSensorBitmapID = derivedSensorBitmapID;
		mSensorDetailsRef = sensorDetailsRef;
	}

	public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		return processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
	}
	
	public ObjectCluster processDataCommon(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		if(rawData!=null && rawData.length>0){
			int index = 0;
			for (ChannelDetails channelDetails:mListOfChannels){
				//first process the data originating from the Shimmer sensor
				byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
				System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
				objectCluster = processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
				index += channelDetails.mDefaultNumBytes;
				objectCluster.incrementIndexKeeper();
			}
			
		}
		return objectCluster;
	}
	
	/** To process data originating from the Shimmer device
	 * @param channelByteArray The byte array packet, or byte array sd log
	 * @param commType The communication type
	 * @param object The packet/objectCluster to append the data to
	 * @return
	 */
	public static ObjectCluster processShimmerChannelData(byte[] channelByteArray, ChannelDetails channelDetails, ObjectCluster objectCluster){
		long parsedChannelData = UtilParseData.parseData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
		objectCluster.addData(channelDetails.mObjectClusterName, channelDetails.mChannelFormatDerivedFromShimmerDataPacket, channelDetails.mDefaultUncalUnit, (double)parsedChannelData);
		return objectCluster;
	}
	
	
	public void updateSensorDetailsWithCommsTypes(List<COMMUNICATION_TYPE> listOfSupportedCommsTypes) {
//		mapOfIsEnabledPerCommsType = new HashMap<COMMUNICATION_TYPE, Boolean>();
		for(COMMUNICATION_TYPE commsType:listOfSupportedCommsTypes){
			if(!mapOfIsEnabledPerCommsType.containsKey(commsType)){
				mapOfIsEnabledPerCommsType.put(commsType, false);
			}
		}
		
		Iterator<COMMUNICATION_TYPE> iterator = mapOfIsEnabledPerCommsType.keySet().iterator();
		while(iterator.hasNext()){
			COMMUNICATION_TYPE commsType = iterator.next();
			if(!listOfSupportedCommsTypes.contains(commsType)){
				iterator.remove();
			}
		}
	}

	public int getExpectedDataPacketSize() {
		int dataPacketSize = 0;
//		if(!mSensorDetails.mIsDummySensor){
			Iterator<ChannelDetails> iterator = mListOfChannels.iterator();
			while(iterator.hasNext()){
				ChannelDetails channelDetails = iterator.next();
				if(channelDetails.mChannelSource==CHANNEL_SOURCE.SHIMMER){
					dataPacketSize += channelDetails.mDefaultNumBytes;
//					System.err.println("Sensor:\t" + mSensorDetailsRef.mGuiFriendlyLabel + "\tChannel:\t" + channelDetails.mGuiName + " - BYTES SIZE:\t" + channelDetails.mDefaultNumBytes);
				}
			}
//		}
//		System.err.println("Sensor:\t" + mSensorDetailsRef.mGuiFriendlyLabel + " - PACKET SIZE:\t" + dataPacketSize);
		return dataPacketSize;
	}

	public List<ChannelDetails> getListOfChannels(){
		return mListOfChannels;
	}
	
	public int getNumberOfChannels() {
		return mListOfChannels.size();
	}

	public void setIsEnabled(boolean state){
		for(COMMUNICATION_TYPE commType:mapOfIsEnabledPerCommsType.keySet()){
			setIsEnabled(commType, state);
		}
	}

	public void setIsEnabled(COMMUNICATION_TYPE commType, boolean state) {
		if(commType==null){
			setIsEnabled(state);
		}
		else{
			mapOfIsEnabledPerCommsType.put(commType, state);
		}
	}

	public boolean isEnabled(COMMUNICATION_TYPE commType) {
//		return mIsEnabled;
		
		if(mapOfIsEnabledPerCommsType.containsKey(commType)){
			return mapOfIsEnabledPerCommsType.get(commType);
		}
		return false;
	}

	//TODO: temp here but probably not suitable for ShimmerObject
	public boolean isEnabled() {
		for(Boolean isEnabled:mapOfIsEnabledPerCommsType.values()){
			if(isEnabled){
				return true;
			}
		}
		return false;
	}
	
	public boolean isInternalExpBrdPowerRequired() {
		if(isEnabled() && mSensorDetailsRef.mIntExpBoardPowerRequired) {
			return true;
		}
		return false;
	}


	/** This cycles through the channels finding which are enabled and summing up the number of bytes
	 * @param commType
	 * @return
	 */
	public int getExpectedPacketByteArray(COMMUNICATION_TYPE commType) {
		int count = 0;
		if (isEnabled(commType)){
			for(ChannelDetails channelDetails:mListOfChannels){
				count += channelDetails.mDefaultNumBytes;
			}
		}
		return count;
	}

	public boolean isApiSensor() {
		return mSensorDetailsRef.mIsApiSensor ;
	}

	public boolean isDerivedChannel() {
		if(mDerivedSensorBitmapID>0) {
			return true;
		}
		return false;
	}

	public void updateFromEnabledSensorsVars(long enabledSensors, long derivedSensors) {
		updateFromEnabledSensorsVars(null, enabledSensors, derivedSensors);
	}

	public void updateFromEnabledSensorsVars(COMMUNICATION_TYPE commType, long enabledSensors, long derivedSensors) {
		if(isApiSensor()){
			return;
		}
		
		setIsEnabled(commType, false);
		// Check if this sensor is a derived sensor
		if(isDerivedChannel()) {
			//Check if associated derived channels are enabled 
			if((derivedSensors&mDerivedSensorBitmapID) == mDerivedSensorBitmapID) {
				//TODO add comment
				if((enabledSensors&mSensorDetailsRef.mSensorBitmapIDSDLogHeader)>0) {
					setIsEnabled(commType, true);
				}
			}
		}
		// This is not a derived sensor
		else {
			//Check if sensor's bit in sensor bitmap is enabled
			if((enabledSensors&mSensorDetailsRef.mSensorBitmapIDSDLogHeader)>0) {
				setIsEnabled(commType, true);
			}
		}
	}

	public void getLenghtOfCalibBytes() {
		// TODO Auto-generated method stub
		
	}

//	public int getLengthOfCalibBytes() {
//		return mSensorDetailsRef.mLengthOfCalibBytes;
//	}
	

}
