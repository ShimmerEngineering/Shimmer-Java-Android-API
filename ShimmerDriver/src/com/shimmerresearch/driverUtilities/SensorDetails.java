package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.datastructure.ShimmerObjectCluster.ObjectCluster2;
import com.shimmerresearch.datastructure.ShimmerObjectCluster.ObjectCluster2.CommunicationType;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.sensors.UtilParseData;

public class SensorDetails implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 1545530433767674139L;
	
//	public boolean mIsEnabled = false;
	/** by default load in communication types for Bluetooth and SD	 */
	public HashMap<COMMUNICATION_TYPE, Boolean> mapOfIsEnabledPerCommsType = new HashMap<COMMUNICATION_TYPE, Boolean>();
	{
		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.BLUETOOTH, false);
		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.SD, false);
//		mapOfIsEnabledPerCommsType.put(COMMUNICATION_TYPE.IEEE802154, false);
	}
	
	public long mDerivedSensorBitmapID = 0;
	public SensorDetailsRef mSensorDetails;
//	public List<String> mListOfChannels = new ArrayList<String>();
	public List<ChannelDetails> mListOfChannels = new ArrayList<ChannelDetails>();
	
	public SensorDetails(){
	}
	
	public SensorDetails(boolean isEnabled, long derivedSensorBitmapID, SensorDetailsRef sensorDetails){
		setIsEnabled(isEnabled);
		mDerivedSensorBitmapID = derivedSensorBitmapID;
		mSensorDetails = sensorDetails;
		
//		for(String channelName:sensorDetails.mListOfChannelsRef){
//			mListOfChannels.add(channelName);
//		}
	}

	public boolean isDerivedChannel() {
		if(mDerivedSensorBitmapID>0) {
			return true;
		}
		return false;
	}

	public boolean isEnabled(COMMUNICATION_TYPE commType) {
//		return mIsEnabled;
		
		if(mapOfIsEnabledPerCommsType.containsKey(commType)){
			return mapOfIsEnabledPerCommsType.get(commType);
		}
		return false;
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
		mapOfIsEnabledPerCommsType.put(commType, state);
	}

	public boolean isEnabled() {
		for(Boolean isEnabled:mapOfIsEnabledPerCommsType.values()){
			if(isEnabled){
				return true;
			}
		}
		return false;
	}

	
	public ObjectCluster processShimmerChannelData(byte[] rawData, ObjectCluster objectCluster) {
		for(ChannelDetails channelDetails:mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(rawData, 0, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//			objectCluster = processShimmerChannelData(rawData, channelDetails, objectCluster);
			long parsedChannelData = UtilParseData.parseData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
			objectCluster.addData(channelDetails.mObjectClusterName, channelDetails.mChannelFormatDerivedFromShimmerDataPacket, channelDetails.mDefaultUnit, (double)parsedChannelData);
		}
		return objectCluster;
	}

	public boolean isInternalExpBrdPowerRequired() {
		if(isEnabled() && mSensorDetails.mIntExpBoardPowerRequired) {
			return true;
		}
		return false;
	}

	
}