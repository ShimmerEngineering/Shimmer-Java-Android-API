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
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class AbstractSensor implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 3465427544416038676L;

	/**
	 * Used for the BtStream and LogAndStream firmware to indicate enabled sensors when connected over Bluetooth. 
	 */
	public long mSensorBitmapIDStreaming = 0;
	/**
	 * Used in the configuration header in RAW data logged to the Shimmer's on-board SD-card. 
	 */
	public long mSensorBitmapIDSDLogHeader = 0;
	
	public long mDerivedSensorBitmapID = 0;
	
	public String mGuiFriendlyLabel = "";
	public List<Integer> mListOfSensorMapKeysRequired = new ArrayList<Integer>();
	public List<Integer> mListOfSensorMapKeysConflicting = new ArrayList<Integer>();
	public boolean mIntExpBoardPowerRequired = false;
	public List<String> mListOfConfigOptionKeysAssociated = new ArrayList<String>();
	public List<ShimmerVerObject> mListOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();  
	
    public Map<String, SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();

//	//Testing for GQ BLE
//	public String mHeaderFileLabel = "";
//	public int mHeaderByteMask = 0;
//	public int mNumChannels = 0;
	
	//public LinkedHashMap<String,ChannelDetails> mMapOfChannels = new LinkedHashMap<String,ChannelDetails>();
	
	public boolean mIsEnabled = true;
	public boolean mIsDummySensor = false;
	
//	/** Shimmer3 Low-noise analog accelerometer */
//	public static final int A_ACCEL = 0;
//	/** Shimmer3 Gyroscope */
//	public static final int MPU9150_GYRO = 1;
//	/** Shimmer3 Primary magnetometer */
//	public static final int LSM303DLHC_MAG = 2;
////	public static final int EXG1_24BIT = 3;
////	public static final int EXG2_24BIT = 4;
//	public static final int GSR = 5;
//	public static final int EXT_EXP_ADC_A6 = 6;
//	public static final int EXT_EXP_ADC_A7 = 7;
//	public static final int BRIDGE_AMP = 8;
//	public static final int RESISTANCE_AMP = 9;
//	//public static final int HR = 9;
//	public static final int VBATT = 10;
//	/** Shimmer3 Wide-range digital accelerometer */
//	public static final int LSM303DLHC_ACCEL = 11;
//	public static final int EXT_EXP_ADC_A15 = 12;
//	public static final int INT_EXP_ADC_A1 = 13;
//	public static final int INT_EXP_ADC_A12 = 14;
//	public static final int INT_EXP_ADC_A13 = 15;
//	public static final int INT_EXP_ADC_A14 = 16;
//	/** Shimmer3 Alternative accelerometer */
//	public static final int MPU9150_ACCEL = 17;
//	/** Shimmer3 Alternative magnetometer */
//	public static final int MPU9150_MAG = 18;
////	public static final int EXG1_16BIT = 19;
////	public static final int EXG2_16BIT = 21;
//	public static final int BMP180_PRESSURE = 22;
//	//public static final int BMP180_TEMPERATURE = 23; // not yet implemented
//	//public static final int MSP430_TEMPERATURE = 24; // not yet implemented
//	public static final int MPU9150_TEMP = 25;
//	//public static final int LSM303DLHC_TEMPERATURE = 26; // not yet implemented
//	//public static final int MPU9150_MPL_TEMPERATURE = 1<<17; // same as SENSOR_SHIMMER3_MPU9150_TEMP 
//	public static final int MPU9150_MPL_QUAT_6DOF = 27;
//	public static final int MPU9150_MPL_QUAT_9DOF = 28;
//	public static final int MPU9150_MPL_EULER_6DOF = 29;
//	public static final int MPU9150_MPL_EULER_9DOF = 30;
//	public static final int MPU9150_MPL_HEADING = 31;
//	public static final int MPU9150_MPL_PEDOMETER = 32;
//	public static final int MPU9150_MPL_TAP = 33;
//	public static final int MPU9150_MPL_MOTION_ORIENT = 34;
//	public static final int MPU9150_MPL_GYRO = 35;
//	public static final int MPU9150_MPL_ACCEL = 36;
//	public static final int MPU9150_MPL_MAG = 37;
//	public static final int MPU9150_MPL_QUAT_6DOF_RAW = 38;
//
//	// Combination Channels
//	public static final int ECG = 100;
//	public static final int EMG = 101;
//	public static final int EXG_TEST = 102;
//	
//	// Derived Channels
//	public static final int EXG_RESPIRATION = 103;
//	public static final int SKIN_TEMPERATURE_PROBE = 104;
//
//	// Derived Channels - GSR Board
//	public static final int PPG_A12 = 106;
//	public static final int PPG_A13 = 107;
//	
//	// Derived Channels - Proto3 Deluxe Board
//	public static final int PPG1_A12 = 111;
//	public static final int PPG1_A13 = 112;
//	public static final int PPG2_A1 = 114;
//	public static final int PPG2_A14 = 115;
//	
//	public static final int EXG_CUSTOM = 116;
//	
//	public static final int TIMESTAMP = 150;
//	public static final int TIMESTAMP_SYNC = 151;
//	public static final int REAL_TIME_CLOCK = 152;
//	public static final int REAL_TIME_CLOCK_SYNC = 153;
//
//	public static final int PPG_DUMMY = 105;
//	public static final int PPG1_DUMMY = 110;
//	public static final int PPG2_DUMMY = 113;
//	
//	public static final int SHIMMER_STREAMING_PROPERTIES = 200;
	
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
	
	@Deprecated
	public SensorEnabledDetails mSensorEnabledDetails;
	
	protected boolean mEnableCalibration = true;
	protected String mSensorName;
	protected String[] mSignalOutputNameArray;
	protected String[] mSignalOutputFormatArray;
	protected String[] mSignalOutputUnitArray;
	protected int mFirmwareType;
	protected int mHardwareID;
	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor 
	public HashMap<String,SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	
	@Deprecated
	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>> mMapOfCommTypeToSensorMap = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>>();
	
	/** Each communication type might have a different Integer key representing the channel, e.g. BT Stream inquiry response (holds the channel sequence of the packet)
	 * 
	 */
	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>> mMapOfCommTypetoChannel = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>>(); 
	
	public abstract String getSensorName();
	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);
	public abstract ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster object);
	
//	/** Different firmwares might have different infomem layouts
//	 * @param svo
//	 * @return
//	 */
//	public abstract SensorInfoMem generateInfoMem(ShimmerVerObject svo);
	
	public abstract void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);

	public abstract void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);

	public abstract Map<String, SensorGroupingDetails> getSensorGroupingMap();

	public abstract Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet);
	public abstract Object getConfigValueUsingConfigLabel(String componentName);
	
	public abstract void setSamplingRateFromFreq();
	public abstract void setDefaultConfiguration();
	
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
			long parsedChannelData = parsedData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
			objectCluster.addData(channelDetails.mObjectClusterName, channelDetails.mChannelFormatDerivedFromShimmerDataPacket, channelDetails.mDefaultUnit, (double)parsedChannelData);
		}

		return objectCluster;
//		return object;
	
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
	public abstract List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo);
	public abstract List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo);
	public abstract Map<String, SensorGroupingDetails> generateSensorGroupMapping(ShimmerVerObject svo);
	
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
	
	/**
	 * Converts the raw packet byte values, into the corresponding calibrated and uncalibrated sensor values, the Instruction String determines the output 
	 * @param newPacket a byte array containing the current received packet
	 * @param Instructions an array string containing the commands to execute. It is currently not fully supported
	 * @return
	 */
	protected static long parsedData(byte[] data, String dataType, String dataEndian){
		
		int iData=0;
		long formattedData=0;
		
		
			if (dataType==CHANNEL_DATA_TYPE.UINT8) {
				formattedData=(int)0xFF & data[iData];
				iData=iData+1;
			} else if (dataType==CHANNEL_DATA_TYPE.INT8) {
				formattedData=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			} else if (dataType==CHANNEL_DATA_TYPE.UINT12 && dataEndian==CHANNEL_DATA_ENDIAN.LSB) {
				formattedData=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType==CHANNEL_DATA_TYPE.INT16_to_12) {
				formattedData=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				formattedData=formattedData>>4; // shift right by 4 bits
				iData=iData+2;
			} else if (dataType==CHANNEL_DATA_TYPE.UINT16 && dataEndian==CHANNEL_DATA_ENDIAN.LSB) {				
				formattedData=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType==CHANNEL_DATA_TYPE.UINT16 && dataEndian==CHANNEL_DATA_ENDIAN.MSB) {				
				formattedData=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData+0] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType==CHANNEL_DATA_TYPE.INT16 && dataEndian==CHANNEL_DATA_ENDIAN.LSB) {
				formattedData=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				//formattedData=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType==CHANNEL_DATA_TYPE.INT16 && dataEndian==CHANNEL_DATA_ENDIAN.MSB){
				formattedData=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				//formattedData=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType==CHANNEL_DATA_TYPE.UINT24 && dataEndian==CHANNEL_DATA_ENDIAN.MSB) {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData=xmsb + msb + lsb;
				iData=iData+3;
			}  else if (dataType==CHANNEL_DATA_TYPE.UINT24 && dataEndian==CHANNEL_DATA_ENDIAN.LSB) {				
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF));
				formattedData=xmsb + msb + lsb;
				iData=iData+3;
			} else if (dataType==CHANNEL_DATA_TYPE.INT24 && dataEndian==CHANNEL_DATA_ENDIAN.MSB) {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData=calculatetwoscomplement((int)(xmsb + msb + lsb),24);
				iData=iData+3;
			} else if (dataType==CHANNEL_DATA_TYPE.UINT32_SIGNED) {
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
			} else if (dataType==CHANNEL_DATA_TYPE.UINT32 && dataEndian==CHANNEL_DATA_ENDIAN.LSB) {
				long forthmsb =(((long)data[iData+3] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+2] & 0xFF) << 16);
				long msb =(((long)data[iData+1] & 0xFF) << 8);
				long lsb =(((long)data[iData+0] & 0xFF) << 0);
				formattedData=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType==CHANNEL_DATA_TYPE.UINT32 && dataEndian==CHANNEL_DATA_ENDIAN.MSB) {
				long forthmsb =(((long)data[iData+0] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+1] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+3] & 0xFF) << 0);
				formattedData=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType==CHANNEL_DATA_TYPE.INT32 && dataEndian==CHANNEL_DATA_ENDIAN.LSB) {
				long xxmsb =((long)(data[iData+3] & 0xFF) << 24);
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF) << 0);
				formattedData=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType==CHANNEL_DATA_TYPE.INT32 && dataEndian==CHANNEL_DATA_ENDIAN.MSB) {
				long xxmsb =((long)(data[iData+0] & 0xFF) << 24);
				long xmsb =((long)(data[iData+1] & 0xFF) << 16);
				long msb =((long)(data[iData+2] & 0xFF) << 8);
				long lsb =((long)(data[iData+3] & 0xFF) << 0);
				formattedData=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
				
			} else if (dataType==CHANNEL_DATA_TYPE.UINT64 && dataEndian==CHANNEL_DATA_ENDIAN.MSB) {
				long eigthmsb =(((long)data[iData+0] & 0x0FL) << 56);
				long seventhmsb =(((long)data[iData+1] & 0xFFL) << 48);
				long sixthmsb =(((long)data[iData+2] & 0xFFL) << 40);
				long fifthmsb =(((long)data[iData+3] & 0xFFL) << 32);
				long forthmsb =(((long)data[iData+4] & 0xFFL) << 24);
				long thirdmsb =(((long)data[iData+5] & 0xFFL) << 16);
				long msb =(((long)data[iData+6] & 0xFF) << 8);
				long lsb =(((long)data[iData+7] & 0xFF));
				formattedData=(eigthmsb + seventhmsb + sixthmsb + fifthmsb+ forthmsb+ thirdmsb + msb + lsb);
				iData=iData+8;
				
			} else if (dataType==CHANNEL_DATA_TYPE.UINT72_SIGNED){
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
	@Deprecated // Moving to constant data type declarations rather then declaring strings in multiple classes
	protected long[] parsedData(byte[] data, String[] dataType){
		
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
	private static int calculatetwoscomplement(int signedData, int bitLength){
		int newData=signedData;
		if (signedData>=(1<<(bitLength-1))) {
			newData=-((signedData^(int)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}

	private static long calculatetwoscomplement(long signedData, int bitLength){
		long newData=signedData;
		if (signedData>=(1L<<(bitLength-1))) {
			newData=-((signedData^(long)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
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
