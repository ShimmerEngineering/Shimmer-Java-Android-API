package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.ChannelDataEndian;
import com.shimmerresearch.driverUtilities.ChannelDetails.ChannelDataType;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class AbstractSensor implements Serializable{
	
	public static class SENSOR_NAMES{
		public static final String GSR = "GSR";
		public static final String ECG_TO_HR = "ECG to Heart Rate";
		public static final String CLOCK = "Clock";
	}
	
	public SensorEnabledDetails mSensorEnabledDetails;
	
	
	/**
	 * 
	 */
	protected boolean mEnableCalibration = true;
	private static final long serialVersionUID = 3465427544416038676L;
	protected String mSensorName;
	protected String[] mSignalOutputNameArray;
	protected String[] mSignalOutputFormatArray;
	protected String[] mSignalOutputUnitArray;
	protected int mFirmwareType;
	protected int mHardwareID;
	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor 
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	
	
	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>> mMapOfCommTypeToSensorMap = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>>();
	
	/** Each communication type might have a different Integer key representing the channel, e.g. BT Stream inquiry response (holds the channel sequence of the packet)
	 * 
	 */
	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>> mMapOfComTypetoChannel = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>>(); 
	public abstract String getSensorName();
	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE comType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet,COMMUNICATION_TYPE comType);
	public abstract Object processData(byte[] rawData, COMMUNICATION_TYPE comType,Object object);
	
	
	
	/** To process data originating from the Shimmer device
	 * @param channelByteArray The byte array packet, or byte array sd log
	 * @param comType The communication type
	 * @param object The packet/objectCluster to append the data to
	 * @return
	 */
	public Object processShimmerChannelData(byte[] channelByteArray, ChannelDetails channelDetails, Object object){

		if (channelDetails.mIsEnabled){
			//byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			long rawData = parsedData(channelByteArray,channelDetails.mDefaultChannelDataType,channelDetails.mDefaultChannelDataEndian);
			ObjectCluster objectCluster = (ObjectCluster) object;
			objectCluster.mPropertyCluster.put(channelDetails.mObjectClusterName,new FormatCluster(channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString(),channelDetails.mDefaultUnit,(double)rawData));
			objectCluster.mSensorNames[objectCluster.indexKeeper] = channelDetails.mObjectClusterName;
			if (channelDetails.mChannelFormatDerivedFromShimmerDataPacket==CHANNEL_TYPE.UNCAL){
				objectCluster.mUncalData[objectCluster.indexKeeper]=(double)rawData;
				objectCluster.mUnitUncal[objectCluster.indexKeeper]=channelDetails.mDefaultUnit;	
			} else if (channelDetails.mChannelFormatDerivedFromShimmerDataPacket==CHANNEL_TYPE.CAL){
				objectCluster.mCalData[objectCluster.indexKeeper]=(double)rawData;
				objectCluster.mUnitCal[objectCluster.indexKeeper]=channelDetails.mDefaultUnit;
			}
			
		}
		
		return object;
	
	}
	
	
	/**
	 * @param svo
	 * @return
	 */
	public abstract HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo);
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
		mShimmerVerObject = svo;
		mConfigOptionsMap = generateConfigOptionsMap(svo);
		mMapOfComTypetoChannel = generateChannelDetailsMap(svo);
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
	
	/**
	 * Converts the raw packet byte values, into the corresponding calibrated and uncalibrated sensor values, the Instruction String determines the output 
	 * @param newPacket a byte array containing the current received packet
	 * @param Instructions an array string containing the commands to execute. It is currently not fully supported
	 * @return
	 */
	protected long parsedData(byte[] data,String dataType,String dataEndian){
		
		int iData=0;
		long formattedData=0;
		
		
			if (dataType==ChannelDataType.UINT8) {
				formattedData=(int)0xFF & data[iData];
				iData=iData+1;
			} else if (dataType==ChannelDataType.INT8) {
				formattedData=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			} else if (dataType==ChannelDataType.UINT12 && dataEndian==ChannelDataEndian.LSB) {
				formattedData=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType==ChannelDataType.INT16_to_12) {
				formattedData=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				formattedData=formattedData>>4; // shift right by 4 bits
				iData=iData+2;
			} else if (dataType==ChannelDataType.UINT16 && dataEndian==ChannelDataEndian.LSB) {				
				formattedData=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType==ChannelDataType.UINT16 && dataEndian==ChannelDataEndian.MSB) {				
				formattedData=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData+0] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType==ChannelDataType.INT16 && dataEndian==ChannelDataEndian.LSB) {
				formattedData=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				//formattedData=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType==ChannelDataType.INT16 && dataEndian==ChannelDataEndian.MSB){
				formattedData=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				//formattedData=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType==ChannelDataType.UINT24 && dataEndian==ChannelDataEndian.MSB) {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData=xmsb + msb + lsb;
				iData=iData+3;
			}  else if (dataType==ChannelDataType.UINT24 && dataEndian==ChannelDataEndian.LSB) {				
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF));
				formattedData=xmsb + msb + lsb;
				iData=iData+3;
			} else if (dataType==ChannelDataType.INT24 && dataEndian==ChannelDataEndian.MSB) {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData=calculatetwoscomplement((int)(xmsb + msb + lsb),24);
				iData=iData+3;
			} else if (dataType==ChannelDataType.UINT32_SIGNED) {
				//TODO: should this be called i32?
				//TODO: are the indexes incorrect, current '+1' to '+4', should this be '+0' to '+3' the the others listed here?
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				long xxmsb =(((long)data[iData+4] & 0xFF) << 24);
				long xmsb =(((long)data[iData+3] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData=(1-2*offset)*(xxmsb + xmsb + msb + lsb);
				iData=iData+5;
			//TODO: Newly added below up to u72 - check
			} else if (dataType==ChannelDataType.UINT32 && dataEndian==ChannelDataEndian.LSB) {
				long forthmsb =(((long)data[iData+3] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+2] & 0xFF) << 16);
				long msb =(((long)data[iData+1] & 0xFF) << 8);
				long lsb =(((long)data[iData+0] & 0xFF) << 0);
				formattedData=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType==ChannelDataType.UINT32 && dataEndian==ChannelDataEndian.MSB) {
				long forthmsb =(((long)data[iData+0] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+1] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+3] & 0xFF) << 0);
				formattedData=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType==ChannelDataType.INT32 && dataEndian==ChannelDataEndian.LSB) {
				long xxmsb =((long)(data[iData+3] & 0xFF) << 24);
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF) << 0);
				formattedData=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType==ChannelDataType.INT32 && dataEndian==ChannelDataEndian.MSB) {
				long xxmsb =((long)(data[iData+0] & 0xFF) << 24);
				long xmsb =((long)(data[iData+1] & 0xFF) << 16);
				long msb =((long)(data[iData+2] & 0xFF) << 8);
				long lsb =((long)(data[iData+3] & 0xFF) << 0);
				formattedData=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType==ChannelDataType.UINT72_SIGNED){
				// do something to parse the 9 byte data
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				
				long eigthmsb =(((long)data[iData+8] & 0x0FL) << 56);
				long seventhmsb =(((long)data[iData+7] & 0xFFL) << 48);
				long sixthmsb =(((long)data[iData+6] & 0xFFL) << 40);
				long fifthmsb =(((long)data[iData+5] & 0xFFL) << 32);
				long forthmsb =(((long)data[iData+4] & 0xFFL) << 24);
				long thirdmsb =(((long)data[iData+3] & 0xFFL) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData=(1-2*offset)*(eigthmsb + seventhmsb + sixthmsb + fifthmsb+ forthmsb+ thirdmsb + msb + lsb);
				iData=iData+9;
			}
		return formattedData;
	}
	
	/**
	 * Converts the raw packet byte values, into the corresponding calibrated and uncalibrated sensor values, the Instruction String determines the output 
	 * @param newPacket a byte array containing the current received packet
	 * @param Instructions an array string containing the commands to execute. It is currently not fully supported
	 * @return
	 */
	protected long[] parsedData(byte[] data,String[] dataType){
		
		int iData=0;
		long[] formattedData=new long[dataType.length];

		for (int i=0;i<dataType.length;i++)
			if (dataType[i]=="u8") {
				formattedData[i]=(int)0xFF & data[iData];
				iData=iData+1;
			} else if (dataType[i]=="i8") {
				formattedData[i]=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			} else if (dataType[i]=="u12") {

				formattedData[i]=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="i12>") {
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				formattedData[i]=formattedData[i]>>4; // shift right by 4 bits
				iData=iData+2;
			} else if (dataType[i]=="u16") {				
				formattedData[i]=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="u16r") {				
				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData+0] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="i16") {
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				//formattedData[i]=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType[i]=="i16r"){
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				//formattedData[i]=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType[i]=="u24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=xmsb + msb + lsb;
				iData=iData+3;
			}  else if (dataType[i]=="u24") {				
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF));
				formattedData[i]=xmsb + msb + lsb;
				iData=iData+3;
			} else if (dataType[i]=="i24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=calculatetwoscomplement((int)(xmsb + msb + lsb),24);
				iData=iData+3;
			} else if (dataType[i]=="u32signed") {
				//TODO: should this be called i32?
				//TODO: are the indexes incorrect, current '+1' to '+4', should this be '+0' to '+3' the the others listed here?
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				long xxmsb =(((long)data[iData+4] & 0xFF) << 24);
				long xmsb =(((long)data[iData+3] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData[i]=(1-2*offset)*(xxmsb + xmsb + msb + lsb);
				iData=iData+5;
			//TODO: Newly added below up to u72 - check
			} else if (dataType[i]=="u32") {
				long forthmsb =(((long)data[iData+3] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+2] & 0xFF) << 16);
				long msb =(((long)data[iData+1] & 0xFF) << 8);
				long lsb =(((long)data[iData+0] & 0xFF) << 0);
				formattedData[i]=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType[i]=="u32r") {
				long forthmsb =(((long)data[iData+0] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+1] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+3] & 0xFF) << 0);
				formattedData[i]=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType[i]=="i32") {
				long xxmsb =((long)(data[iData+3] & 0xFF) << 24);
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF) << 0);
				formattedData[i]=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType[i]=="i32r") {
				long xxmsb =((long)(data[iData+0] & 0xFF) << 24);
				long xmsb =((long)(data[iData+1] & 0xFF) << 16);
				long msb =((long)(data[iData+2] & 0xFF) << 8);
				long lsb =((long)(data[iData+3] & 0xFF) << 0);
				formattedData[i]=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType[i]=="u72"){
				// do something to parse the 9 byte data
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				
				long eigthmsb =(((long)data[iData+8] & 0x0FL) << 56);
				long seventhmsb =(((long)data[iData+7] & 0xFFL) << 48);
				long sixthmsb =(((long)data[iData+6] & 0xFFL) << 40);
				long fifthmsb =(((long)data[iData+5] & 0xFFL) << 32);
				long forthmsb =(((long)data[iData+4] & 0xFFL) << 24);
				long thirdmsb =(((long)data[iData+3] & 0xFFL) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData[i]=(1-2*offset)*(eigthmsb + seventhmsb + sixthmsb + fifthmsb+ forthmsb+ thirdmsb + msb + lsb);
				iData=iData+9;
			}
		return formattedData;
	}
	private int calculatetwoscomplement(int signedData, int bitLength){
		int newData=signedData;
		if (signedData>=(1<<(bitLength-1))) {
			newData=-((signedData^(int)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}

	private long calculatetwoscomplement(long signedData, int bitLength){
		long newData=signedData;
		if (signedData>=(1L<<(bitLength-1))) {
			newData=-((signedData^(long)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}
	
	/** This cycles through the channels finding which are enabled and summing up the number of bytes
	 * @param comType
	 * @return
	 */
	public int getExpectedPacketByteArray(COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		int count = 0; 
		for (ChannelDetails channelDetails: mMapOfComTypetoChannel.get(comType).values()){
			if (channelDetails.mIsEnabled){
				count = count+channelDetails.mDefaultNumBytes;
			}
		}
		return count;
	}

	public int getNumberOfEnabledChannels(COMMUNICATION_TYPE comType){
		int count = 0;
		for (ChannelDetails channelDetails: mMapOfComTypetoChannel.get(comType).values()){
			if (channelDetails.mIsEnabled){
				count = count+1;
			}
		}
		return count;
	}
	
	public void disableSensorChannels(COMMUNICATION_TYPE comType){
		for (ChannelDetails channelDetails :mMapOfComTypetoChannel.get(comType).values()){
			channelDetails.mIsEnabled = false;
		}
	}
	
	public void enableSensorChannels(COMMUNICATION_TYPE comType){
		for (ChannelDetails channelDetails :mMapOfComTypetoChannel.get(comType).values()){
			channelDetails.mIsEnabled = true;
		}
	}
	
}
