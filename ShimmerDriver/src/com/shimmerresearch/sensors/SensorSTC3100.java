package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorBattVoltage.GuiLabelSensorTiles;
import com.shimmerresearch.sensors.SensorBattVoltage.GuiLabelSensors;
import com.shimmerresearch.sensors.SensorBattVoltage.ObjectClusterSensorName;

/**
 * Sensor class for the STC3100 which monitors the critical parameters of a single cell Li-Ion Battery,
 * voltage,temperature and current.
 * 
 * 
 * @author Ronan McCormack
 *
 */

public class SensorSTC3100 extends AbstractSensor{

	/****/
	private static final long serialVersionUID = 9001303055918168581L;
	
	//--------- Sensor specific variables start --------------

	
	public class GuiLabelConfig{
		public static final String STC3100_SENSOR = "STC3100 Sensor";
	}
	
	public class GuiLabelSensors{
		public static final String STC3100 = "Battery Monitor (STC3100)";
	}
	
	public class GuiLabelSensorTiles{
		public static final String STC3100_MONITORING = GuiLabelSensors.STC3100;
	}

	
	public static class DatabaseChannelHandles{
	// NOT IN THIS CLASS
	}
	
	
	public static class ObjectClusterSensorName{
		public static  String STC_VOLTAGE = "STC3100_Voltage";
		public static  String STC_CURRENT = "STC3100_Current";
		public static  String STC_TEMP = "STC3100_Temperature";
		public static  String STC_CHARGE = "STC3100_Charge";
		public static  String STC_BATERY_PERCENTAGE = "STC3100_Battery_Percentage";
		public static  String STC_TIME_REMAINING = "STC3100_Time_Remaining";
		
		
	}
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start ------------------
	
	
	  //--------- Bluetooth commands end ------------------
	
  	//--------- Sensor info start --------------
  	public static final SensorDetailsRef sensorSTC3100Ref = new SensorDetailsRef(
  			0x01<<16, // CHECK FOR CORRECT VALUE
  			0x01<<16, 
  			GuiLabelSensors.STC3100,
  			null,
  			null,
  			Arrays.asList(ObjectClusterSensorName.STC_VOLTAGE,
  					ObjectClusterSensorName.STC_CURRENT,
  					ObjectClusterSensorName.STC_TEMP,
  					ObjectClusterSensorName.STC_CHARGE,
  					ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
  					ObjectClusterSensorName.STC_TIME_REMAINING));
    
  	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_STC3100, SensorSTC3100.sensorSTC3100Ref);  
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
  	
    public static final SensorGroupingDetails sensorSTC3100 = new SensorGroupingDetails(
			GuiLabelSensorTiles.STC3100_MONITORING,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_STC3100),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
  	
	//--------- Sensor info end --------------
	
  //--------- Channel info start --------------
  	
 	 public static final ChannelDetails channelSTMVoltage = new ChannelDetails(
			ObjectClusterSensorName.STC_VOLTAGE,
			ObjectClusterSensorName.STC_VOLTAGE,
			ObjectClusterSensorName.STC_VOLTAGE,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.UNCAL),
			true,
			false);
 	 
	 public static final ChannelDetails channelSTMCurrent = new ChannelDetails(
				ObjectClusterSensorName.STC_CURRENT,
				ObjectClusterSensorName.STC_CURRENT,
				ObjectClusterSensorName.STC_CURRENT,
				CHANNEL_UNITS.MILLIAMPS,
				Arrays.asList(CHANNEL_TYPE.UNCAL),
				true,
				false);
	
	 public static final ChannelDetails channelSTMTemp = new ChannelDetails(
				ObjectClusterSensorName.STC_TEMP,
				ObjectClusterSensorName.STC_TEMP,
				ObjectClusterSensorName.STC_TEMP,
				CHANNEL_UNITS.DEGREES_CELSUIS,
				Arrays.asList(CHANNEL_TYPE.UNCAL),
				true,
				false);
	 
	 public static final ChannelDetails channelSTMCharge = new ChannelDetails(
				ObjectClusterSensorName.STC_CHARGE,
				ObjectClusterSensorName.STC_CHARGE,
				ObjectClusterSensorName.STC_CHARGE,
				CHANNEL_UNITS.MILLIAMP_HOUR,
				Arrays.asList(CHANNEL_TYPE.UNCAL),
				true,
				false);
	 
	 public static final ChannelDetails channelSTMBatteryPercentage = new ChannelDetails(
				ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
				ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
				ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
				CHANNEL_UNITS.PERCENT,
				Arrays.asList(CHANNEL_TYPE.UNCAL),
				true,
				false);
	 
	 public static final ChannelDetails channelSTMTimeRemaining = new ChannelDetails(
				ObjectClusterSensorName.STC_TIME_REMAINING,
				ObjectClusterSensorName.STC_TIME_REMAINING,
				ObjectClusterSensorName.STC_TIME_REMAINING,
				CHANNEL_UNITS.MINUTES,
				Arrays.asList(CHANNEL_TYPE.UNCAL),
				true,
				false);
	 
	   public static final Map<String, ChannelDetails> mChannelMapRef;
	    static {
	        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
	        aMap.put(ObjectClusterSensorName.STC_VOLTAGE, channelSTMVoltage);
	        aMap.put(ObjectClusterSensorName.STC_CURRENT, channelSTMCurrent);
	        aMap.put(ObjectClusterSensorName.STC_TEMP, channelSTMTemp );
	        aMap.put(ObjectClusterSensorName.STC_CHARGE, channelSTMCharge );
	        aMap.put(ObjectClusterSensorName.STC_BATERY_PERCENTAGE, channelSTMBatteryPercentage );
	        aMap.put(ObjectClusterSensorName.STC_TIME_REMAINING, channelSTMTimeRemaining);
			mChannelMapRef = Collections.unmodifiableMap(aMap);
	    }
	    
	  //--------- Channel info end --------------
	 
	public SensorSTC3100(ShimmerVerObject svo) {
		super(SENSORS.STC3100, svo);
			initialise();
		
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		
	}

	@Override
	public void generateConfigOptionsMap() {
		// Not in this class
		
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.STC3100_MONITORING.ordinal(), sensorSTC3100);
		}
		super.updateSensorGroupingMap();		
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] rawData, COMMUNICATION_TYPE commType,ObjectCluster objectCluster, boolean isTimeSyncEnabled,
			long pctimeStamp) {
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pctimeStamp);
		double unCalData = 0; double calData = 0;
		for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_VOLTAGE)){
		     unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.STC_VOLTAGE), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
		     calData = unCalData/100;
		     objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-6);
				objectCluster.incrementIndexKeeper();
			}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_CURRENT)){
			     unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.STC_CURRENT), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			     calData = unCalData/100;
			     objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-5);
				 objectCluster.incrementIndexKeeper();
				}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_TEMP)){
			     unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.STC_TEMP), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			     calData = unCalData/100;
			     objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-4);
				 objectCluster.incrementIndexKeeper();
				}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_CHARGE)){
			     unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.STC_CHARGE), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			     calData = unCalData/100;
			     objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-3);
				 objectCluster.incrementIndexKeeper();
				}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_BATERY_PERCENTAGE)){
			     unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.STC_BATERY_PERCENTAGE), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			     calData = unCalData/100;
			     objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
				 objectCluster.incrementIndexKeeper();
				}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_TIME_REMAINING)){
			     unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.STC_TIME_REMAINING), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			     calData = unCalData/100;
			     objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
				 objectCluster.incrementIndexKeeper();
				}
		}
		
		return objectCluster;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey,
			String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey,
			String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey,
			boolean isSensorEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

}
