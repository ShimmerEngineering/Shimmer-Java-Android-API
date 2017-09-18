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
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class ShimmerClock extends AbstractSensor {

	private static final long serialVersionUID = 4841055784366989272L;

	//--------- Sensor specific variables start --------------
	protected boolean mFirstTime = true;
	double mFirstRawTS = 0;
	long mSystemTimeStamp = 0;
	public int OFFSET_LENGTH = 9;
	protected long mInitialTimeStamp = 0;
	protected long mRTCOffset = 0; //this is in ticks
	protected int mTimeStampPacketRawMaxValue = 16777216; // or 65536 
	protected double mLastReceivedCalibratedTimeStamp=-1; 
	protected double mLastReceivedTimeStamp=0;
	protected double mCurrentTimeStampCycle=0;
	
	protected boolean mStreamingStartTimeSaved = false;	
	protected double mStreamingStartTimeMs;	
	
	private boolean mFirstPacketParsed=true;
	private double mOffsetFirstTime=-1;

	//For debugging only
	double mPreviousTimeStamp = 0;
	double mSystemTimeStampPrevious = 0;
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static class GuiLabelSensors{
		public static final String SYSTEM_TIMESTAMP = SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP; 
		public static final String TIMESTAMP = ObjectClusterSensorName.TIMESTAMP; 
		public static final String DEVICE_PROPERTIES = "Device Properties"; 
	}

	public static class LABEL_SENSOR_TILE{
		public static final String STREAMING_PROPERTIES = GuiLabelSensors.DEVICE_PROPERTIES; 
	}

	public static class ObjectClusterSensorName{
		public static final String TIMESTAMP = "Timestamp";
		
		public static final String TIMESTAMP_DIFFERENCE = "Timestamp Difference";
		public static final String REAL_TIME_CLOCK = "RealTime";
		
//		public static final String SYSTEM_TIMESTAMP = "System_Timestamp";
//		public static final String SYSTEM_TIMESTAMP_PLOT = "System_Timestamp_plot";
//		public static final String SYSTEM_TIMESTAMP_DIFFERENCE = "System_Timestamp_Difference";

		public static final String TIMESTAMP_OFFSET = "Offset";
	}
	
	public static class DatabaseChannelHandles{
		public static final String TIMESTAMP = "TimeStamp";
		public static final String TIMESTAMP_EXPORT = "Timestamp";
		public static final String OFFSET_TIMESTAMP = "OFFSET";//"Offset";
		
		public static final String REAL_TIME_CLOCK = "Real_Time_Clock";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String INITIAL_TIMESTAMP = "Initial_Timestamp";

	}
	public static final SensorDetailsRef sensorSystemTimeStampRef = new SensorDetailsRef(
			GuiLabelSensors.SYSTEM_TIMESTAMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
					SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE
					));
	{
		sensorSystemTimeStampRef.mIsApiSensor = true;
	}

	public static final SensorDetailsRef sensorShimmerClock = new SensorDetailsRef(
			GuiLabelSensors.TIMESTAMP,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(ShimmerClock.ObjectClusterSensorName.TIMESTAMP,
					ShimmerClock.ObjectClusterSensorName.TIMESTAMP_DIFFERENCE,
					ShimmerClock.ObjectClusterSensorName.REAL_TIME_CLOCK,
					ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET
					));
	{
		sensorShimmerClock.mIsApiSensor = true; // Even though TIMESTAMP channel is an API channel, there is no enabledSensor bit for it
	}

	//Uncomment channels if you want them to appear in Consensys LiveData
	public static final SensorDetailsRef sensorShimmerStreamingProperties = new SensorDetailsRef(
			GuiLabelSensors.DEVICE_PROPERTIES,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(
//					ObjectClusterSensorName.TIMESTAMP,
//					ObjectClusterSensorName.REAL_TIME_CLOCK,
					SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
					
					//TODO move to different class?
					Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,
					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL,
					Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER,
					ShimmerStreamingProperties.ObjectClusterSensorName.RSSI,
					ShimmerStreamingProperties.ObjectClusterSensorName.SENSOR_DISTANCE
					
//					ObjectClusterSensorName.TIMESTAMP_DIFFERENCE,
//					ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE
					));
	{
		sensorShimmerStreamingProperties.mIsApiSensor = true;
	}
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP, ShimmerClock.sensorSystemTimeStampRef);
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_TIMESTAMP, ShimmerClock.sensorShimmerClock);
        aMap.put(Configuration.Shimmer3.SENSOR_ID.HOST_SHIMMER_STREAMING_PROPERTIES, ShimmerClock.sensorShimmerStreamingProperties);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
    
    public static final SensorGroupingDetails sensorGroupStreamingProperties = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.STREAMING_PROPERTIES,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.HOST_SHIMMER_STREAMING_PROPERTIES),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			true);

	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
    //TODO move to SensorSystemTimeStamp class
	public static final ChannelDetails channelSystemTimestamp = new ChannelDetails(
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
			DatabaseChannelHandlesCommon.TIMESTAMP_SYSTEM,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, true);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestamp.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}
		
    //TODO move to SensorSystemTimeStamp class
	public static final ChannelDetails channelSystemTimestampPlot = new ChannelDetails(
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestampPlot.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestampPlot.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

    //TODO move to SensorSystemTimeStamp class
	public static final ChannelDetails channelSystemTimestampDiff = new ChannelDetails(
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE,
			SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE,
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelSystemTimestamp.mChannelSource = CHANNEL_SOURCE.API;
//		channelSystemTimestamp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.CAL;
	}

	public static final ChannelDetails channelShimmerClock3byte = new ChannelDetails(
			ObjectClusterSensorName.TIMESTAMP,
			ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock3byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelShimmerClock3byte.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	
	public static final ChannelDetails channelShimmerTsDiffernce = new ChannelDetails(
			ObjectClusterSensorName.TIMESTAMP_DIFFERENCE,
			ObjectClusterSensorName.TIMESTAMP_DIFFERENCE,
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		channelShimmerTsDiffernce.mChannelSource = CHANNEL_SOURCE.API;
	}
	
	public static final ChannelDetails channelShimmerClock2byte = new ChannelDetails(
			ObjectClusterSensorName.TIMESTAMP,
			ObjectClusterSensorName.TIMESTAMP,
			DatabaseChannelHandles.TIMESTAMP,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.CLOCK_UNIT,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClock2byte.mChannelSource = CHANNEL_SOURCE.SHIMMER;
	}
	
	public static final ChannelDetails channelShimmerClockOffset = new ChannelDetails(
			ObjectClusterSensorName.TIMESTAMP_OFFSET,
			ObjectClusterSensorName.TIMESTAMP_OFFSET,
			DatabaseChannelHandles.OFFSET_TIMESTAMP,
			//TODO UtilParseData.parseData currently can not handle over 64 bits. LSB byte for offset is ?a sign byte? for the remaining 8 bytes so default parser won't work
//			CHANNEL_DATA_TYPE.UINT72, 9, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.UNCAL), false, true);
	{
		//TODO put into above constructor
		channelShimmerClockOffset.mChannelSource = CHANNEL_SOURCE.API;
	}
	
	public static final ChannelDetails channelRealTimeClock = new ChannelDetails(
			ObjectClusterSensorName.REAL_TIME_CLOCK,
			ObjectClusterSensorName.REAL_TIME_CLOCK,
			DatabaseChannelHandles.REAL_TIME_CLOCK,
			CHANNEL_UNITS.MILLISECONDS,
			Arrays.asList(CHANNEL_TYPE.UNCAL ,CHANNEL_TYPE.CAL), false, true);
	{
		//TODO put into above constructor
		channelRealTimeClock.mChannelSource = CHANNEL_SOURCE.API;
	}

	//TODO: Move to separate class
	public static final ChannelDetails channelBattPercentage = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
			Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelBattPercentage.mChannelSource = CHANNEL_SOURCE.API;
	}

	//TODO: Move to separate class
	public static final ChannelDetails channelReceptionRateCurrent = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,
			"Packet Reception Rate (per second)",
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelReceptionRateCurrent.mChannelSource = CHANNEL_SOURCE.API;
	}

	//TODO: Move to separate class
	public static final ChannelDetails channelReceptionRateTrial = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL,
			"Packet Reception Rate (overall)",
			DatabaseChannelHandlesCommon.NONE,
			CHANNEL_UNITS.PERCENT,
			Arrays.asList(CHANNEL_TYPE.CAL), true, false);
	{
		//TODO put into above constructor
		channelReceptionRateTrial.mChannelSource = CHANNEL_SOURCE.API;
	}

	//TODO: Move to separate class
	public static final ChannelDetails channelEventMarker = new ChannelDetails(
			ShimmerStreamingProperties.ObjectClusterSensorName.EVENT_MARKER,
			ShimmerStreamingProperties.ObjectClusterSensorName.EVENT_MARKER,
			ShimmerStreamingProperties.DatabaseChannelHandles.EVENT_CHANNEL,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL), false, false);
	{
		//TODO put into above constructor
		channelEventMarker.mChannelSource = CHANNEL_SOURCE.API;
	}
	//--------- Channel info end --------------

	public ShimmerClock(ShimmerDevice shimmerDevice) {
		super(SENSORS.CLOCK, shimmerDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		Map<String, ChannelDetails> channelMapRef = new LinkedHashMap<String, ChannelDetails>();
		
		//TODO move entries that shouldn't be here to their own dedicated sensor classes
		
		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP, ShimmerClock.channelSystemTimestamp);
		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, ShimmerClock.channelSystemTimestampPlot);
		channelMapRef.put(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE, ShimmerClock.channelSystemTimestampDiff);

		if(mShimmerVerObject.isShimmerGenGq()){
			channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.RSSI, ShimmerStreamingProperties.channelRssi);
			channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.SENSOR_DISTANCE, ShimmerStreamingProperties.channelSensorDistance);
			channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.EVENT_MARKER, ShimmerClock.channelEventMarker);
		}
		else {// if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			if(mShimmerVerObject.getFirmwareVersionCode()>=6 || mShimmerVerObject.mHardwareVersion==HW_ID.ARDUINO){
				channelMapRef.put(ShimmerClock.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock3byte);
				mTimeStampPacketRawMaxValue = (int) Math.pow(2, 24);
			}
			else{
				channelMapRef.put(ShimmerClock.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock2byte);
				mTimeStampPacketRawMaxValue = (int) Math.pow(2, 16);
			}
			channelMapRef.put(ShimmerClock.ObjectClusterSensorName.TIMESTAMP_DIFFERENCE, ShimmerClock.channelShimmerTsDiffernce);
			channelMapRef.put(ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET, ShimmerClock.channelShimmerClockOffset);
			channelMapRef.put(ShimmerClock.ObjectClusterSensorName.REAL_TIME_CLOCK, ShimmerClock.channelRealTimeClock);
			
			channelMapRef.put(SensorBattVoltage.ObjectClusterSensorName.BATT_PERCENTAGE, ShimmerClock.channelBattPercentage);
			
			channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT, ShimmerClock.channelReceptionRateCurrent);
			channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL, ShimmerClock.channelReceptionRateTrial);
			channelMapRef.put(ShimmerStreamingProperties.ObjectClusterSensorName.EVENT_MARKER, ShimmerClock.channelEventMarker);
		}
		
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, channelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		//NOT USED IN THIS CLASS
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.STREAMING_PROPERTIES.ordinal(), sensorGroupStreamingProperties);
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
					if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TIMESTAMP)){
						
						//PARSE DATA
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

						calculateTrialPacketLoss(calibratedTS);

						if(commType==COMMUNICATION_TYPE.SD){
							double samplingClockFreq = mShimmerDevice.getSamplingClockFreq();

							//TIMESTAMP
							// RTC timestamp uncal. (shimmer timestamp + RTC offset from header); unit = ticks
							double unwrappedRawTimestamp = calibratedTS*samplingClockFreq/1000;
							unwrappedRawTimestamp -= mFirstRawTS; //deduct this so it will start from 0
							
							long sdlogRawTimestamp = (long)mInitialTimeStamp + (long)unwrappedRawTimestamp;
							timestampUnCalToSave = (double)sdlogRawTimestamp;
							if (mEnableCalibration){
								double sdLogCalTimestamp = (double)sdlogRawTimestamp/samplingClockFreq*1000;
								timestampCalToSave = sdLogCalTimestamp; 
							}

//							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)sdlogRawTimestamp));
//							uncalibratedData[iTimeStamp] = (double)sdlogRawTimestamp;
//							uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.CLOCK_UNIT;
//							sensorNames[iTimeStamp]= Shimmer3.ObjectClusterSensorName.TIMESTAMP;
				//
//							if (mEnableCalibration){
//								double sdLogcalTimestamp = (double)sdlogRawTimestamp/samplingClockFreq*1000;
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
								objectCluster.setTimeStampMilliSecs(calibratedTS);
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
					
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TIMESTAMP_DIFFERENCE)){
						FormatCluster fCal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.CAL.toString());
						FormatCluster fUncal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.UNCAL.toString());

						objectCluster.addCalData(channelDetails, fUncal.mData - mPreviousTimeStamp);
						objectCluster.incrementIndexKeeper();
						
						mPreviousTimeStamp = fUncal.mData; 
					}
						
					
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.REAL_TIME_CLOCK)){
						
						if(commType==COMMUNICATION_TYPE.SD){
							FormatCluster fUncal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.UNCAL.toString());
							FormatCluster fCal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.CAL.toString());
							if(fUncal!=null && fCal!=null){
								long timestampUncal = (long) fUncal.mData;
								long timestampCal = (long) fUncal.mData;
								
								//RAW RTC
								if(mRTCOffset!=0){
									double samplingClockFreq = mShimmerDevice.getSamplingClockFreq();
									
									long rtctimestamp = timestampUncal + mRTCOffset;
									double rtctimestampcal = Double.NaN;
									if (mEnableCalibration){
										rtctimestampcal = timestampCal;
										if(mInitialTimeStamp!=0){
											rtctimestampcal += ((double)mInitialTimeStamp/samplingClockFreq*1000.0);
										}
										if(mRTCOffset!=0){
											rtctimestampcal += ((double)mRTCOffset/samplingClockFreq*1000.0);
										}
										if(mFirstRawTS!=0){
											rtctimestampcal -= (mFirstRawTS/samplingClockFreq*1000.0);
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
//											rtctimestampcal += ((double)mInitialTimeStamp/samplingClockFreq*1000.0);
//										}
//										if(mRTCOffset!=0){
//											rtctimestampcal += ((double)mRTCOffset/samplingClockFreq*1000.0);
//										}
//										if(mFirstRawTS!=0){
//											rtctimestampcal -= (mFirstRawTS/samplingClockFreq*1000.0);
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

					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TIMESTAMP_OFFSET)){
						
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
								
								
		//						int iOffset=getSignalIndex(ObjectClusterSensorName.TIMESTAMP_OFFSET); //find index
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
		//						objectCluster.mPropertyCluster.put(ObjectClusterSensorName.TIMESTAMP_OFFSET,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,offsetValue));
		//						uncalibratedData[iOffset] = offsetValue;
		//						calibratedData[iOffset] = Double.NaN;
		//						uncalibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
		//						calibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
							} 
						}
					}
					
//					//TODO move to Shimmer4 class? (copied from ShimmerPCMSS)
//					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC)){
//						if (mEnableTimeSync) {
//							//TODO
////							objectCluster = mSync.CalculateTimeSync(ojc);
//						} else {
//							objectCluster.addCalData(channelDetails, Double.NaN);
//						}
//					}
				}
			}
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.SYSTEM_TIMESTAMP)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){

					if(channelDetails.mObjectClusterName.equals(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP)){
						long systemTime = pcTimestamp;
						if(commType==COMMUNICATION_TYPE.SD){
							systemTime = System.currentTimeMillis();
						}
						objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,systemTime));
						objectCluster.addData(channelDetails, Double.NaN, systemTime);
						objectCluster.incrementIndexKeeper();
					}
					else if(channelDetails.mObjectClusterName.equals(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT)){
						if(mShimmerVerObject.isShimmerGenGq()){
							//TODO: Hack -> just copying from elsewhere (forgotten where exactly)
							double systemTime = 0;
							FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP), CHANNEL_TYPE.CAL.toString());
							if(f!=null){
								systemTime = f.mData;
							}

							objectCluster.mSystemTimeStamp = ByteBuffer.allocate(8).putLong((long) systemTime).array();;
							objectCluster.addCalData(channelDetails, systemTime);
							objectCluster.incrementIndexKeeper();
						}
						else{
							if(mFirstPacketParsed) {
								mFirstPacketParsed=false;
//								FormatCluster f = ObjectCluster.returnFormatCluster(sobjectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.CAL.toString());
								byte[] bSystemTS = objectCluster.mSystemTimeStamp;
								ByteBuffer bb = ByteBuffer.allocate(8);
						    	bb.put(bSystemTS);
						    	bb.flip();
						    	mSystemTimeStamp = bb.getLong();
								mOffsetFirstTime = mSystemTimeStamp-objectCluster.getTimestampMilliSecs();
							}
							
//							objectCluster.addData(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, objectCluster.mShimmerCalibratedTimeStamp+mOffsetFirstTime);
							double calTimestamp = objectCluster.getTimestampMilliSecs();
							double systemTimestampPlot = calTimestamp+mOffsetFirstTime;
							objectCluster.addCalData(channelDetails, systemTimestampPlot);
							objectCluster.incrementIndexKeeper();
						}
					}
					else if(channelDetails.mObjectClusterName.equals(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_DIFFERENCE)){
						FormatCluster fCal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(SensorSystemTimeStamp.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT), CHANNEL_TYPE.CAL.toString());
//						FormatCluster fCal = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.SYSTEM_TIMESTAMP), CHANNEL_TYPE.CAL.toString());

						objectCluster.addCalData(channelDetails, fCal.mData - mSystemTimeStampPrevious);
						objectCluster.incrementIndexKeeper();
						
						mSystemTimeStampPrevious = fCal.mData; 
					}
				}
			}
			
			//TODO move device properties to difference class? 
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.DEVICE_PROPERTIES)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE)){
						objectCluster.addCalData(channelBattPercentage, mShimmerDevice.getEstimatedChargePercentage());
						objectCluster.incrementIndexKeeper();
					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT)){
						double packetReceptionRateCurrent = (double)mShimmerDevice.getPacketReceptionRateCurrent();
						if(!Double.isNaN(packetReceptionRateCurrent) && !Double.isInfinite(packetReceptionRateCurrent)){
							objectCluster.addCalData(channelReceptionRateCurrent, packetReceptionRateCurrent);
							objectCluster.incrementIndexKeeper();
						}
					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL)){
						double packetReceptionRateOverall = (double)mShimmerDevice.getPacketReceptionRateOverall();
						if(!Double.isNaN(packetReceptionRateOverall) && !Double.isInfinite(packetReceptionRateOverall)){
							objectCluster.addCalData(channelReceptionRateTrial, packetReceptionRateOverall);
							objectCluster.incrementIndexKeeper();
						}
					}
					else if(channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER)){
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EVENT_MARKER,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, mEventMarkers);
						objectCluster.addCalData(channelEventMarker, mShimmerDevice.mEventMarkers);
						objectCluster.incrementIndexKeeper();
						mShimmerDevice.untriggerEventIfLastOneWasPulse();
						
//						mShimmerDevice.processEventMarkerCh(objectCluster);
//						objectCluster.incrementIndexKeeper();
					}					
				}
				
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL, CHANNEL_TYPE.CAL.toString()} 
							));
				}
			}
		}


		return objectCluster;
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		//NOT USED IN THIS CLASS
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
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
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void parseConfigMap(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		//Initial TimeStamp
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.INITIAL_TIMESTAMP)){
			setInitialTimeStamp(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.INITIAL_TIMESTAMP)).longValue());
		}
		
	}

	/** Mirrors calibrateTimeStamp() in ShimmerObject */
	protected double calibrateTimeStamp(double timeStamp){
		//first convert to continuous time stamp
		double calibratedTimeStamp = 0;
		if (mLastReceivedTimeStamp>(timeStamp+(mTimeStampPacketRawMaxValue*mCurrentTimeStampCycle))){ 
			mCurrentTimeStampCycle=mCurrentTimeStampCycle+1;
		}

		mLastReceivedTimeStamp = (timeStamp+(mTimeStampPacketRawMaxValue*mCurrentTimeStampCycle));
		calibratedTimeStamp = mLastReceivedTimeStamp/mShimmerDevice.getSamplingClockFreq()*1000;   // to convert into mS
		if (!mStreamingStartTimeSaved){
			mStreamingStartTimeSaved=true;
			mStreamingStartTimeMs = calibratedTimeStamp;
		}

		return calibratedTimeStamp;
	}

	private void calculateTrialPacketLoss(double currentTimeStampMs) {
//		if (mLastReceivedCalibratedTimeStamp!=-1){
//			double timeDifference = currentTimeStampMs - mLastReceivedCalibratedTimeStamp;
//			double expectedTimeDifference = (1/mShimmerDevice.getSamplingRateShimmer())*1000;
//			double expectedTimeDifferenceLimit = expectedTimeDifference * 1.1; // 10% limit? 
//			if (timeDifference>expectedTimeDifferenceLimit){
//				long packetLossCountPerTrial = mShimmerDevice.getPacketLossCountPerTrial() + (long) (timeDifference/expectedTimeDifference);
//				mShimmerDevice.setPacketLossCountPerTrial(packetLossCountPerTrial);
//			}
//		}
//		
//		Long totalNumberOfPackets = (long) ((currentTimeStampMs-mCalTimeStart)/(1/mShimmerDevice.getSamplingRateShimmer()*1000));
//		if(totalNumberOfPackets>0){
//			double packetReceptionRateTrial = (double)((totalNumberOfPackets-mShimmerDevice.getPacketLossCountPerTrial())/(double)totalNumberOfPackets)*100;
//			mShimmerDevice.setPacketReceptionRateOverall(packetReceptionRateTrial);
//		}
//		
//		mLastReceivedCalibratedTimeStamp = currentTimeStampMs;
		
		if(mStreamingStartTimeMs>0){
			double timeDifference = currentTimeStampMs - mStreamingStartTimeMs;
			double expectedTimeDifference = (1/mShimmerDevice.getSamplingRateShimmer())*1000;
	
			long packetExpectedCount = (long) (timeDifference/expectedTimeDifference);
			mShimmerDevice.setPacketExpectedCountOverall(packetExpectedCount);
			
			long packetReceivedCount = mShimmerDevice.getPacketReceivedCountOverall();
	
			//For legacy support
			long packetLossCountPerTrial = packetExpectedCount + packetReceivedCount;
			mShimmerDevice.setPacketLossCountPerTrial(packetLossCountPerTrial);
	
			double packetReceptionRateTrial = ((double)packetReceivedCount/(double)packetExpectedCount)*100; 
			mShimmerDevice.setPacketReceptionRateOverall(packetReceptionRateTrial);
		}
	}
	

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}
	public void setInitialTimeStamp(long initialTimeStamp){
		mInitialTimeStamp = initialTimeStamp;
	}

//	protected double mLastReceivedCalibratedTimeStamp=-1; 
	double mLastSavedCalibratedTimeStamp = 0.0;
	
	
	/**Replaced by simpler approach in ShimmerDevice
	 * @param intervalMs
	 * @return
	 */
	@Deprecated 
	public double calculatePacketReceptionRateCurrent(int intervalMs) {
		double numPacketsShouldHaveReceived = (((double)intervalMs)/1000) * getSamplingRateShimmer();
		
		if (mLastReceivedCalibratedTimeStamp!=-1){
			double timeDifference=mLastReceivedCalibratedTimeStamp-mLastSavedCalibratedTimeStamp;
			double numPacketsReceived= ((timeDifference/1000) * getSamplingRateShimmer());
			mShimmerDevice.setPacketReceptionRateCurrent((numPacketsReceived/numPacketsShouldHaveReceived)*100.0);
		}	

		mShimmerDevice.setPacketReceptionRateCurrent(UtilShimmer.nudgeDouble(mShimmerDevice.getPacketReceptionRateCurrent(), 0.0, 100.0));

		mLastSavedCalibratedTimeStamp = mLastReceivedCalibratedTimeStamp;
		return mShimmerDevice.getPacketReceptionRateCurrent();
	}

	//Contents copied from ShimmerBluetooth.initialiseStreaming()
	public void resetShimmerClock() {
		mFirstPacketParsed=true;
		resetCalibratedTimeStamp();
		//Already done in Shimmer4 class
//		resetPacketLossTrial();
//		mSync=true; // a backup sync done every time you start streaming
	}
	
	//Copied from ShimmerBluetooth.resetCalibratedTimeStamp()
	public void resetCalibratedTimeStamp(){
		mLastReceivedTimeStamp = 0;
		mLastReceivedCalibratedTimeStamp = -1;
		
		mStreamingStartTimeSaved = false;
		
		mCurrentTimeStampCycle = 0;
	}

	//--------- Optional methods to override in Sensor Class start --------
	//--------- Optional methods to override in Sensor Class end --------

}
