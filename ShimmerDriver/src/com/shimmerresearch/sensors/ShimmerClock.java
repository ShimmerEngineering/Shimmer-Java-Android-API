package com.shimmerresearch.sensors;

import java.nio.ByteBuffer;
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
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class ShimmerClock extends AbstractSensor {

	/** * */
	private static final long serialVersionUID = 4841055784366989272L;

	//--------- Sensor specific variables start --------------
	protected boolean mFirstTime = true;
	double mFirstRawTS = 0;
	public int OFFSET_LENGTH = 9;
	protected long mInitialTimeStamp = 0;
	protected long mRTCOffset = 0; //this is in ticks
	protected int mTimeStampPacketRawMaxValue = 65536;// 16777216 or 65536 
	protected double mLastReceivedCalibratedTimeStamp=-1; 
	protected double mLastReceivedTimeStamp=0;
	protected double mCurrentTimeStampCycle=0;
	protected boolean mFirstTimeCalTime=true;	
	protected double mCalTimeStart;	
	
	private boolean mFirstPacketParsed=true;
	private double mOffsetFirstTime=-1;

	//TODO hack!
//	public boolean mFlagPacketLoss = false;
	
//	protected long mPacketLossCount = 0;		//Used by ShimmerBluetooth
//	protected double mPacketReceptionRate = 100;
//	protected double mPacketReceptionRateCurrent = 100;
	
	public static class GuiLabelSensors{
		public static String SYSTEM_TIMESTAMP = Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP; 
		public static String TIMESTAMP = Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP; 
		public static final String DEVICE_PROPERTIES = "Device Properties"; 
	}

	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorSystemTimeStampRef = new SensorDetailsRef(
			GuiLabelSensors.SYSTEM_TIMESTAMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT));
	{
		sensorSystemTimeStampRef.mIsApiSensor = true;
	}

	public static final SensorDetailsRef sensorShimmerClock = new SensorDetailsRef(
			GuiLabelSensors.TIMESTAMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
					Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
					Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET));
	{
		sensorSystemTimeStampRef.mIsApiSensor = true; // Even though TIMESTAMP channel is an API channel, there is no enabledSensor bit for it
	}

	public static final SensorDetailsRef sensorShimmerPacketReception = new SensorDetailsRef(
			GuiLabelSensors.DEVICE_PROPERTIES,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(
//					Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
//					Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_SYNC,
					//temp only! JC: delete after db sync works
//					Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
					Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
					Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,
					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL,
					Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER
					
//					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,
//					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL
					));
	{
		sensorShimmerPacketReception.mIsApiSensor = true;
	}
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SYSTEM_TIMESTAMP, ShimmerClock.sensorSystemTimeStampRef);
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_TIMESTAMP, ShimmerClock.sensorShimmerClock);
        aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES, ShimmerClock.sensorShimmerPacketReception);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelSystemTimestamp = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP_SYSTEM,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, true);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestamp.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
		
	public static final ChannelDetails channelSystemTimestampPlot = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
			Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
			DatabaseChannelHandles.NONE,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestampPlot.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestampPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
    
	public static final ChannelDetails channelShimmerClock3byte = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock3byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelShimmerClock3byte.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	
	public static final ChannelDetails channelShimmerClock2byte = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock2byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
	}
	
	public static final ChannelDetails channelShimmerClockOffset = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET,
			Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET,
			DatabaseChannelHandles.OFFSET_TIMESTAMP,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClockOffset.mChannelSource = CHANNEL_SOURCE.API;
	}
	
	public static final ChannelDetails channelRealTimeClock = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
			Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
			DatabaseChannelHandles.REAL_TIME_CLOCK,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelRealTimeClock.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelRealTimeClockSync = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
			Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
			DatabaseChannelHandles.REAL_TIME_CLOCK_SYNC,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, true);
	{
		//TODO put into above constructor
		channelRealTimeClockSync.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelBattPercentage = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
			Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
			DatabaseChannelHandles.NONE,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelBattPercentage.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelReceptionRateCurrent = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,
			"Packet Reception Rate (per second)",
			DatabaseChannelHandles.NONE,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelReceptionRateCurrent.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelReceptionRateTrial = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL,
			"Packet Reception Rate (overall)",
			DatabaseChannelHandles.NONE,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelReceptionRateTrial.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelEventMarker = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER,
			Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER,
			DatabaseChannelHandles.NONE,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelEventMarker.mChannelSource = CHANNEL_SOURCE.API;
	}
	
	
	//--------- Channel info end --------------

	public ShimmerClock(ShimmerDevice shimmerDevice) {
		super(SENSORS.CLOCK, shimmerDevice);
	}

	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		Map<String, ChannelDetails> channelMapRef = new LinkedHashMap<String, ChannelDetails>();
		
		channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP, ShimmerClock.channelSystemTimestamp);
		channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, ShimmerClock.channelSystemTimestampPlot);

		if(svo.getFirmwareIdentifier()==FW_ID.GQ_802154){
			//
		}
		else if(svo.getFirmwareIdentifier()!=FW_ID.UNKNOWN){
			if(svo.getFirmwareVersionCode()>=6){
				channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock3byte);
			}
			else{
				channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock2byte);
			}
			
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET, ShimmerClock.channelShimmerClockOffset);
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK, ShimmerClock.channelRealTimeClock);
			
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC, ShimmerClock.channelRealTimeClockSync);
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE, ShimmerClock.channelBattPercentage);
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT, ShimmerClock.channelReceptionRateCurrent);
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL, ShimmerClock.channelReceptionRateTrial);
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER, ShimmerClock.channelEventMarker);
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		//Debugging
//		if(sensorDetails.getExpectedDataPacketSize()>0){
//			String byteString = "";
//			if(sensorByteArray.length>0){
//				byteString = UtilShimmer.bytesToHexStringWithSpacesFormatted(sensorByteArray); 
//			}
//			System.out.println(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel + " bytes\t" +( (byteString.isEmpty())? "EMPTY BYTES":byteString));
//		}

		//TIMESTAMP
		if(sensorDetails.isEnabled(commType)){
				
			//These if statements are not necessarily needed but good for sorting the sensors
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.TIMESTAMP)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP)){
						
						//PARSE DATA
//						long[] newPacketInt = UtilParseData.parseData(newPacket, mSignalDataTypeArray);
						//TODO replaces newPacketInt[iTimeStamp], need to parse from packetdata
//						double newTimestamp = 0.0;
						//newPacketInt[iOffset]
						double newOffset = 0.0;
						
						byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
						System.arraycopy(sensorByteArray, 0, channelByteArray, 0, channelDetails.mDefaultNumBytes);
						double newTimestamp = UtilParseData.parseData(channelByteArray, channelDetails.mDefaultChannelDataType, channelDetails.mDefaultChannelDataEndian);
						
						if(mFirstTime && commType==COMMUNICATION_TYPE.SD){
							//this is to make sure the Raw starts from zero
							mFirstRawTS = newTimestamp;
							mFirstTime = false;
						}
						double calibratedTS = calibrateTimeStamp(newTimestamp);
						
						double timestampCalToSave = calibratedTS;
						double timestampUnCalToSave = newTimestamp; 

						if(commType==COMMUNICATION_TYPE.SD){
							//TIMESTAMP
							// RTC timestamp uncal. (shimmer timestamp + RTC offset from header); unit = ticks
							double unwrappedRawTimestamp = calibratedTS*32768/1000;
							unwrappedRawTimestamp -= mFirstRawTS; //deduct this so it will start from 0
							
							long sdlogRawTimestamp = (long)mInitialTimeStamp + (long)unwrappedRawTimestamp;
							timestampUnCalToSave = (double)sdlogRawTimestamp;
							if (mEnableCalibration){
								double sdLogCalTimestamp = (double)sdlogRawTimestamp/32768*1000;
								timestampCalToSave = sdLogCalTimestamp; 
							}

//							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)sdlogRawTimestamp));
//							uncalibratedData[iTimeStamp] = (double)sdlogRawTimestamp;
//							uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.CLOCK_UNIT;
//							sensorNames[iTimeStamp]= Shimmer3.ObjectClusterSensorName.TIMESTAMP;
				//
//							if (mEnableCalibration){
//								double sdLogcalTimestamp = (double)sdlogRawTimestamp/32768*1000;
//								
//								objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,sdLogcalTimestamp));
//								calibratedData[iTimeStamp] = sdLogcalTimestamp;
//								calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
//							}

						}
						else if(commType==COMMUNICATION_TYPE.BLUETOOTH){
							//TIMESTAMP
							timestampUnCalToSave = newTimestamp;
							if (mEnableCalibration){
								timestampCalToSave = calibratedTS; 
								objectCluster.mShimmerCalibratedTimeStamp = calibratedTS;
							}
							
//							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,newTimestamp));
//							uncalibratedData[iTimeStamp] = newTimestamp;
//							uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.NO_UNITS;
//							if (mEnableCalibration){
//								objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibratedTS));
//								calibratedData[iTimeStamp] = calibratedTS;
//								calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
//								
//								objectCluster.mShimmerCalibratedTimeStamp = calibratedTS;
//							}
						}
						
						
						objectCluster.addData(channelDetails, timestampUnCalToSave, timestampCalToSave);
						objectCluster.incrementIndexKeeper();
					}
					
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK)){
						
						if(commType==COMMUNICATION_TYPE.SD){
							FormatCluster fUncal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.UNCAL.toString());
							FormatCluster fCal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.CAL.toString());
							if(fUncal!=null && fCal!=null){
								long timestampUncal = (long) fUncal.mData;
								long timestampCal = (long) fUncal.mData;
								
								//RAW RTC
								if(mRTCOffset!=0){
									long rtctimestamp = timestampUncal + mRTCOffset;
									double rtctimestampcal = Double.NaN;
									if (mEnableCalibration){
										rtctimestampcal = timestampCal;
										if(mInitialTimeStamp!=0){
											rtctimestampcal += ((double)mInitialTimeStamp/32768.0*1000.0);
										}
										if(mRTCOffset!=0){
											rtctimestampcal += ((double)mRTCOffset/32768.0*1000.0);
										}
										if(mFirstRawTS!=0){
											rtctimestampcal -= (mFirstRawTS/32768.0*1000.0);
										}
									}
									objectCluster.addData(channelRealTimeClock, (double)rtctimestamp, rtctimestampcal);
									objectCluster.incrementIndexKeeper();

//									objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)rtctimestamp));
//									uncalibratedData[sensorNames.length-1] = (double)rtctimestamp;
//									uncalibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.CLOCK_UNIT;
//									sensorNames[sensorNames.length-1]= Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK;
//									
//									if (mEnableCalibration){
//										double rtctimestampcal = calibratedTS;
//										if(mInitialTimeStamp!=0){
//											rtctimestampcal += ((double)mInitialTimeStamp/32768.0*1000.0);
//										}
//										if(mRTCOffset!=0){
//											rtctimestampcal += ((double)mRTCOffset/32768.0*1000.0);
//										}
//										if(mFirstRawTS!=0){
//											rtctimestampcal -= (mFirstRawTS/32768.0*1000.0);
//										}
//										
//										objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,rtctimestampcal));
//										calibratedData[sensorNames.length-1] = rtctimestampcal;
//										calibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.MILLISECONDS;
//									}
								}							
							}

						}
						
					}

					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET)){
						
						if(commType==COMMUNICATION_TYPE.SD){
							//OFFSET
							if(isTimeSyncEnabled){
								double offsetValue = Double.NaN;
								
								//TODO parse offset
								double newOffset = 0.0;

								if(((OFFSET_LENGTH==9)&&(newOffset != 1152921504606846975L))
										||(newOffset != 4294967295L)){ //this is 4 bytes
									offsetValue=(double)newOffset;
								}
								objectCluster.addData(channelShimmerClockOffset, offsetValue, Double.NaN);
								objectCluster.incrementIndexKeeper();
								
								
		//						int iOffset=getSignalIndex(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET); //find index
		//						double offsetValue = Double.NaN;
		//						if (OFFSET_LENGTH==9){
		//							if(newPacketInt[iOffset] != 1152921504606846975L){
		//								offsetValue=(double)newPacketInt[iOffset];
		//							}	
		//						}
		//						else{
		//							if(newPacketInt[iOffset] != 4294967295L){ //this is 4 bytes
		//								offsetValue=(double)newPacketInt[iOffset];
		//							}
		//						}
		//						objectCluster.mPropertyCluster.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,offsetValue));
		//						uncalibratedData[iOffset] = offsetValue;
		//						calibratedData[iOffset] = Double.NaN;
		//						uncalibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
		//						calibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
							} 
						}
					}
					
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC)){
						
					}
				}
			}
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.SYSTEM_TIMESTAMP)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){

					if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
						long systemTime = pcTimestamp;
						if(commType==COMMUNICATION_TYPE.SD){
							systemTime = System.currentTimeMillis();
						}
						objectCluster.mShimmerCalibratedTimeStamp = systemTime;
						objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,systemTime));
						objectCluster.addData(channelDetails, Double.NaN, systemTime);
						objectCluster.incrementIndexKeeper();
					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
						if(mFirstPacketParsed) {
							mFirstPacketParsed=false;
							FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.CAL.toString());
							byte[] bSystemTS = objectCluster.mSystemTimeStamp;
							ByteBuffer bb = ByteBuffer.allocate(8);
					    	bb.put(bSystemTS);
					    	bb.flip();
					    	long systemTimeStamp = bb.getLong();
							mOffsetFirstTime = systemTimeStamp-objectCluster.mShimmerCalibratedTimeStamp;
						}
						
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, objectCluster.mShimmerCalibratedTimeStamp+mOffsetFirstTime);
						objectCluster.addCalData(channelDetails, objectCluster.mShimmerCalibratedTimeStamp+mOffsetFirstTime);
						objectCluster.incrementIndexKeeper();
						
//						//Hack for debugging
//						//JC: This is temp, should actually be set using the real Shimmer Clock cal time
//						objectCluster.mShimmerCalibratedTimeStamp = System.currentTimeMillis();
//						if(mFirstPacketParsed) {
//							mFirstPacketParsed=false;
//							long systemTime = System.currentTimeMillis();
//							objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
//							byte[] bSystemTS = objectCluster.mSystemTimeStamp;
//							ByteBuffer bb = ByteBuffer.allocate(8);
//					    	bb.put(bSystemTS);
//					    	bb.flip();
//					    	long systemTimeStamp = bb.getLong();
//							mOffsetFirstTime = systemTimeStamp-objectCluster.mShimmerCalibratedTimeStamp;
//						}
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, new FormatCluster(CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, objectCluster.mShimmerCalibratedTimeStamp+mOffsetFirstTime));
						
						
//						//TODO: Hack -> just copying from elsewhere (forgotten where exactly)
//						double systemTime = 0;
//						FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP), CHANNEL_TYPE.CAL.toString());
//						if(f!=null){
//							systemTime = f.mData;
//						}
	//
//						objectCluster.mSystemTimeStamp = ByteBuffer.allocate(8).putLong((long) systemTime).array();;
//						objectCluster.addCalData(channelDetails, systemTime);
//						objectCluster.incrementIndexKeeper();
					}
				}
			}
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.DEVICE_PROPERTIES)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE)){

						objectCluster.addCalData(channelBattPercentage, mShimmerDevice.getEstimatedChargePercentage());
						objectCluster.incrementIndexKeeper();
	//
////						objectCluster.addData(Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mShimmerBattStatusDetails.mEstimatedChargePercentage);
////						calibratedData[additionalChannelsOffset] = (double)mShimmerBattStatusDetails.mEstimatedChargePercentage;
////						calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
////						uncalibratedData[additionalChannelsOffset] = Double.NaN;
////						uncalibratedDataUnits[additionalChannelsOffset] = "";
////						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE;
////						additionalChannelsOffset+=1;

					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT)){
//						objectCluster.addData(channelReceptionRateCurrent, Double.NaN, mShimmerDevice.getPacketReceptionRateCurrent());
						objectCluster.addCalData(channelReceptionRateCurrent, mShimmerDevice.getPacketReceptionRateCurrent());
						objectCluster.incrementIndexKeeper();

//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mPacketReceptionRateCurrent);
//						calibratedData[additionalChannelsOffset] = (double)mPacketReceptionRateCurrent;
//						calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
//						uncalibratedData[additionalChannelsOffset] = Double.NaN;
//						uncalibratedDataUnits[additionalChannelsOffset] = "";
//						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT;
//						additionalChannelsOffset+=1;

						
					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL)){

//						objectCluster.addData(channelReceptionRateCurrent, Double.NaN, mShimmerDevice.getPacketReceptionRate());
						objectCluster.addCalData(channelReceptionRateTrial, mShimmerDevice.getPacketReceptionRateOverall());
						objectCluster.incrementIndexKeeper();

//						calibratedData[additionalChannelsOffset] = (double)mPacketReceptionRate;
//						calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
//						uncalibratedData[additionalChannelsOffset] = Double.NaN;
//						uncalibratedDataUnits[additionalChannelsOffset] = "";
//						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL;
//						additionalChannelsOffset+=1;
					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER)){
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EVENT_MARKER,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, mEventMarkers);
//						untriggerEventIfLastOneWasPulse();
						objectCluster.addCalData(channelEventMarker, mShimmerDevice.mEventMarkers);
						objectCluster.incrementIndexKeeper();

					}					
				}
				
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL, CHANNEL_TYPE.CAL.toString()} 
							));
				}
			}
		}


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
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
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
	
	protected double calibrateTimeStamp(double timeStamp){
		//first convert to continuous time stamp
		double calibratedTimeStamp = 0;
		if (mLastReceivedTimeStamp>(timeStamp+(mTimeStampPacketRawMaxValue*mCurrentTimeStampCycle))){ 
			mCurrentTimeStampCycle=mCurrentTimeStampCycle+1;
		}

		mLastReceivedTimeStamp = (timeStamp+(mTimeStampPacketRawMaxValue*mCurrentTimeStampCycle));
		calibratedTimeStamp = mLastReceivedTimeStamp/32768*1000;   // to convert into mS
		if (mFirstTimeCalTime){
			mFirstTimeCalTime=false;
			mCalTimeStart = calibratedTimeStamp;
		}
		
		Long mTotalNumberofPackets = (long) ((calibratedTimeStamp-mCalTimeStart)/(1/mShimmerDevice.getSamplingRateShimmer()*1000));

		if (mLastReceivedCalibratedTimeStamp!=-1){
			double timeDifference=calibratedTimeStamp-mLastReceivedCalibratedTimeStamp;
			double expectedTimeDifference = (1/mShimmerDevice.getSamplingRateShimmer())*1000;
			double expectedTimeDifferenceLimit = expectedTimeDifference + (expectedTimeDifference*0.1); 
			//if (timeDifference>(1/(mShimmerSamplingRate-1))*1000){
			if (timeDifference>expectedTimeDifferenceLimit){
//				mPacketLossCount=mPacketLossCount+1;
				mShimmerDevice.setPacketLossCount(mShimmerDevice.getPacketLossCount() + (long) (timeDifference/expectedTimeDifferenceLimit));
			}
		}
		
		double packetReceptionRateTrial = (double)((mTotalNumberofPackets-mShimmerDevice.getPacketLossCount())/(double)mTotalNumberofPackets)*100;
//		packetReceptionRateTrial = UtilShimmer.nudgeDouble(mShimmerDevice.getPacketReceptionRateTrial(), 0.0, 100.0);
		mShimmerDevice.setPacketReceptionRateOverall(packetReceptionRateTrial);
		
//		if (mLastReceivedCalibratedTimeStamp!=-1){
//			mFlagPacketLoss = true;
//			//TODO
////			mShimmerDevice.sendStatusMsgPacketLossDetected();
//		}
		
		
//		if (mLastReceivedCalibratedTimeStamp!=-1){
//			double timeDifference=calibratedTimeStamp-mLastReceivedCalibratedTimeStamp;
//			//TODO don't use mMaxSetShimmerSamplingRate? base it on communication type?
////			double expectedTimeDifference = (1/getSamplingRateShimmer())*1000;
//			double expectedTimeDifference = (1/mMaxSetShimmerSamplingRate)*1000;
//			double expectedTimeDifferenceLimit = expectedTimeDifference + (expectedTimeDifference*0.1); 
//			//if (timeDifference>(1/(mShimmerSamplingRate-1))*1000){
//			if (timeDifference>expectedTimeDifferenceLimit){
////				mPacketLossCount=mPacketLossCount+1;
//				mPacketLossCount+= (long) (timeDifference/expectedTimeDifferenceLimit);
//				//TODO don't use mMaxSetShimmerSamplingRate? base it on communication type?
////				Long mTotalNumberofPackets=(long) ((calibratedTimeStamp-mCalTimeStart)/(1/getSamplingRateShimmer()*1000));
//				Long mTotalNumberofPackets=(long) ((calibratedTimeStamp-mCalTimeStart)/(1/mMaxSetShimmerSamplingRate*1000));
//
//				mPacketReceptionRate = (double)((mTotalNumberofPackets-mPacketLossCount)/(double)mTotalNumberofPackets)*100;
//				//TODO
////				sendStatusMsgPacketLossDetected();
//			}
//		}
		
		mLastReceivedCalibratedTimeStamp = calibratedTimeStamp;
		return calibratedTimeStamp;
	}

	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

//	protected double mLastReceivedCalibratedTimeStamp=-1; 
	double mLastSavedCalibratedTimeStamp = 0.0;
	
	public double calculatePacketReceptionRateCurrent(int intervalMs) {
		double numPacketsShouldHaveReceived = (((double)intervalMs)/1000) * mMaxSetShimmerSamplingRate;
//		double numPacketsShouldHaveReceived = (((double)intervalMs)/1000) * getSamplingRateShimmer();
		
		if (mLastReceivedCalibratedTimeStamp!=-1){
			double timeDifference=mLastReceivedCalibratedTimeStamp-mLastSavedCalibratedTimeStamp;
			double numPacketsReceived= ((timeDifference/1000) * mMaxSetShimmerSamplingRate);
//			double numPacketsReceived= ((timeDifference/1000) * getSamplingRateShimmer());
			mShimmerDevice.setPacketReceptionRateCurrent((numPacketsReceived/numPacketsShouldHaveReceived)*100.0);
		}	

		mShimmerDevice.setPacketReceptionRateCurrent(UtilShimmer.nudgeDouble(mShimmerDevice.getPacketReceptionRateCurrent(), 0.0, 100.0));

		mLastSavedCalibratedTimeStamp = mLastReceivedCalibratedTimeStamp;
		return mShimmerDevice.getPacketReceptionRateCurrent();
	}
	
	//--------- Optional methods to override in Sensor Class start --------
	//--------- Optional methods to override in Sensor Class end --------

}
