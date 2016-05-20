package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.sensors.SensorBMP180.GuiLabelConfig;
import com.shimmerresearch.sensors.SensorBMP180.ObjectClusterSensorName;

public class ShimmerClock extends AbstractSensor {

	/** * */
	private static final long serialVersionUID = 4841055784366989272L;

	//--------- Sensor specific variables start --------------
	protected boolean mFirstTime = true;
	double mFirstRawTS = 0;
	public int OFFSET_LENGTH = 9;
	protected long mInitialTimeStamp = 0;
	protected long mRTCOffset = 0; //this is in ticks
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorShimmerClock = new SensorDetailsRef(
			0, 
			0, 
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP));
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.TIMESTAMP, ShimmerClock.sensorShimmerClock);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelShimmerClock3byte = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock3byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelShimmerClock3byte.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
		channelShimmerClock3byte.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	
	public static final ChannelDetails channelShimmerClock2byte = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock2byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelShimmerClock2byte.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
		channelShimmerClock2byte.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	//--------- Channel info end --------------

	public ShimmerClock(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.CLOCK.toString());
	}

	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		Map<String, ChannelDetails> channelMapRef = new LinkedHashMap<String, ChannelDetails>();
		if(svo.getHardwareVersion()>=6){
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock3byte);
		}
		else{
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock2byte);
		}
		
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, channelMapRef);
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		//NOT USED IN THIS CLASS
	}
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {

		//TODO
////		int iTimeStamp=getSignalIndex(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP); //find index
//		if(mFirstTime && fwType == FW_TYPE_SD){
//			//this is to make sure the Raw starts from zero
//			mFirstRawTS = (double)newPacketInt[iTimeStamp];
//			mFirstTime = false;
//		}
//		double calibratedTS = calibrateTimeStamp((double)newPacketInt[iTimeStamp]);
//
//		//TIMESTAMP
//		if (fwType == FW_TYPE_SD){
//			// RTC timestamp uncal. (shimmer timestamp + RTC offset from header); unit = ticks
//			double unwrappedrawtimestamp = calibratedTS*32768/1000;
//			if (mShimmerVerObject.getFirmwareVersionMajor() ==0 && mShimmerVerObject.getFirmwareVersionMinor()==5){
//				
//			} else {
//				unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
//			}
//			long sdlograwtimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp;
//			objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)sdlograwtimestamp));
//			
//			uncalibratedData[iTimeStamp] = (double)sdlograwtimestamp;
//			if (mShimmerVerObject.getFirmwareVersionMajor() ==0 && mShimmerVerObject.getFirmwareVersionMinor()==5){
//				uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
//			}
//			uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.CLOCK_UNIT;
//
//			if (mEnableCalibration){
//				double sdlogcaltimestamp = (double)sdlograwtimestamp/32768*1000;
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,sdlogcaltimestamp));
//				calibratedData[iTimeStamp] = sdlogcaltimestamp;
//				calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
//			}
//		} 
//		else if (fwType == FW_TYPE_BT){
//			objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]));
//			uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
//			uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.NO_UNITS;
//			if (mEnableCalibration){
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibratedTS));
//				calibratedData[iTimeStamp] = calibratedTS;
//				calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
//				objectCluster.mShimmerCalibratedTimeStamp = calibratedTS;
//			}
//		}
//
//		//RAW RTC
//		if ((fwType == FW_TYPE_SD) && mRTCOffset!=0) {
////		if (fwType == FW_TYPE_SD) {
//			double unwrappedrawtimestamp = calibratedTS*32768/1000;
//			unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
//			long rtctimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp + mRTCOffset;
//			objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)rtctimestamp));
//			uncalibratedData[sensorNames.length-1] = (double)rtctimestamp;
//			uncalibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.CLOCK_UNIT;
//			sensorNames[sensorNames.length-1]= Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK;
//			if (mEnableCalibration){
//				double rtctimestampcal = calibratedTS;
//				if(mInitialTimeStamp!=0){
//					rtctimestampcal += ((double)mInitialTimeStamp/32768.0*1000.0);
//				}
//				if(mRTCOffset!=0){
//					rtctimestampcal += ((double)mRTCOffset/32768.0*1000.0);
//				}
//				if(mFirstRawTS!=0){
//					rtctimestampcal -= (mFirstRawTS/32768.0*1000.0);
//				}
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,rtctimestampcal));
//				calibratedData[sensorNames.length-1] = rtctimestampcal;
//				calibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.MILLISECONDS;
//			}
//		}
//
//		//OFFSET
//		if(timeSync==1 && (fwType == FW_TYPE_SD)){
//			int iOffset=getSignalIndex("Offset"); //find index
//			double offsetValue = Double.NaN;
//			if (OFFSET_LENGTH==9){
//				if(newPacketInt[iOffset] == 1152921504606846975L){
//					offsetValue=Double.NaN;
//				} else {
//					offsetValue=(double)newPacketInt[iOffset];
//				}	
//			}
//			else{
//				if(newPacketInt[iOffset] == 4294967295L){ //this is 4 bytes
//					offsetValue=Double.NaN;
//				} else {
//					offsetValue=(double)newPacketInt[iOffset];
//				}
//			}
//
//			objectCluster.mPropertyCluster.put("Offset",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,offsetValue));
//			uncalibratedData[iOffset] = offsetValue;
//			calibratedData[iOffset] = Double.NaN;
//			uncalibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
//			calibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
//		} 
		return objectCluster;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public void setSamplingRateFromFreq() {
		//NOT USED IN THIS CLASS
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			return true;
		}
		return false;
	}
	
	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		//NOT USED IN THIS CLASS
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		//NOT USED IN THIS CLASS
		return null;
	}
	
}
