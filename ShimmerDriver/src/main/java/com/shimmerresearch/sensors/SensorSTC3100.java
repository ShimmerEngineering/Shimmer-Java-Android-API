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
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorBattVoltage.LABEL_SENSOR_TILE;
import com.shimmerresearch.sensors.SensorBattVoltage.GuiLabelSensors;
import com.shimmerresearch.sensors.SensorBattVoltage.ObjectClusterSensorName;

/**
 * Sensor class for the STC3100 which monitors the critical parameters of a single cell Li-Ion Battery,
 * voltage,temperature and current.
 * 
 * 
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */
//TODO switch over to using (SensorSTC3100Details mStc3100Details) rather then the individual variables declared and parsed in this class 
public class SensorSTC3100 extends AbstractSensor{

	/****/
	private static final long serialVersionUID = 9001303055918168581L;
	
	//--------- Sensor specific variables start --------------
	private SensorSTC3100Details mStc3100Details = new SensorSTC3100Details();

	public static final double BATT_CHARGE_CAPACITY = 0.45;        //Ah
	public static final double BATT_MAX_VOLTAGE = 4.167;
	public double mBattInitialCharge = 0;            //mAh
	
	//RAW channels
	@Deprecated // No longer a streaming channel
	public double mBattCurrentVoltage = 0;           //mV
	@Deprecated // No longer a streaming channel
	public double mBattCurrent = 0;                  //mA
	@Deprecated // No longer a streaming channel
	public double mBattTemperature = 0;				// degrees C

	@Deprecated // No longer a streaming channel
	public double mBattCurrentCharge = 0;            //mAh

	public double mBattPercentage = 0;
	public double mBattTimeRemaining =0;             //mins
	
	public String mBattPercentageParsed = "";
	
	
	public class GuiLabelConfig{
		public static final String STC3100_SENSOR = "STC3100 Sensor";
	}
	
	public class GuiLabelSensors{
		public static final String STC3100 = "Battery Monitor (STC3100)";
	}
	
	public class LABEL_SENSOR_TILE{
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
		
		// Derived Channels
		public static  String DERIVED_STC_CHARGE = "Derived STC3100_Charge";
		public static  String DERIVED_STC_BATTERY_PERCENTAGE = "Derived STC3100_Battery_Percentage";
		public static  String DERIVED_STC_TIME_REMAINING = "Derived STC3100_Time_Remaining";
		
		
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
  					ObjectClusterSensorName.STC_TIME_REMAINING,
  					ObjectClusterSensorName.DERIVED_STC_CHARGE,
  					ObjectClusterSensorName.DERIVED_STC_BATTERY_PERCENTAGE,
  					ObjectClusterSensorName.DERIVED_STC_TIME_REMAINING));
    
  	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_STC3100, SensorSTC3100.sensorSTC3100Ref);  
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
  	
    public static final SensorGroupingDetails sensorSTC3100 = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.STC3100_MONITORING,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_STC3100),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
  	
	//--------- Sensor info end --------------
	
  //--------- Channel info start --------------
  	
    public static final ChannelDetails channelSTMVoltage = new ChannelDetails(
    		ObjectClusterSensorName.STC_VOLTAGE,
    		ObjectClusterSensorName.STC_VOLTAGE,
    		ObjectClusterSensorName.STC_VOLTAGE,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.MILLIVOLTS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		true,
    		false);

    public static final ChannelDetails channelSTMCurrent = new ChannelDetails(
    		ObjectClusterSensorName.STC_CURRENT,
    		ObjectClusterSensorName.STC_CURRENT,
    		ObjectClusterSensorName.STC_CURRENT,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.MILLIAMPS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		true,
    		false);

    public static final ChannelDetails channelSTMTemp = new ChannelDetails(
    		ObjectClusterSensorName.STC_TEMP,
    		ObjectClusterSensorName.STC_TEMP,
    		ObjectClusterSensorName.STC_TEMP,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_CELSIUS,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		true,
    		false);

    public static final ChannelDetails channelSTMCharge = new ChannelDetails(
    		ObjectClusterSensorName.STC_CHARGE,
    		ObjectClusterSensorName.STC_CHARGE,
    		ObjectClusterSensorName.STC_CHARGE,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.MILLIAMP_HOUR,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		true,
    		false);

    public static final ChannelDetails channelSTMBatteryPercentage = new ChannelDetails(
    		ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
    		ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
    		ObjectClusterSensorName.STC_BATERY_PERCENTAGE,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.PERCENT,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		true,
    		false);

    public static final ChannelDetails channelSTMTimeRemaining = new ChannelDetails(
    		ObjectClusterSensorName.STC_TIME_REMAINING,
    		ObjectClusterSensorName.STC_TIME_REMAINING,
    		ObjectClusterSensorName.STC_TIME_REMAINING,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.MINUTES,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		true,
    		false);

    // Derived Channels
    public static final ChannelDetails channelDerivedSTMCharge = new ChannelDetails(
    		ObjectClusterSensorName.DERIVED_STC_CHARGE,
    		ObjectClusterSensorName.DERIVED_STC_CHARGE,
    		ObjectClusterSensorName.DERIVED_STC_CHARGE,
    		CHANNEL_UNITS.MILLIAMP_HOUR,
    		Arrays.asList(CHANNEL_TYPE.CAL),
    		true,
    		false);
    {
    	channelDerivedSTMCharge.mChannelSource = CHANNEL_SOURCE.API;
    }

    public static final ChannelDetails channelDeriveSTMBatteryPercentage = new ChannelDetails(
    		ObjectClusterSensorName.DERIVED_STC_BATTERY_PERCENTAGE,
    		ObjectClusterSensorName.DERIVED_STC_BATTERY_PERCENTAGE,
    		ObjectClusterSensorName.DERIVED_STC_BATTERY_PERCENTAGE,
    		CHANNEL_UNITS.PERCENT,
    		Arrays.asList(CHANNEL_TYPE.CAL),
    		true,
    		false);
    {
    	channelDeriveSTMBatteryPercentage.mChannelSource = CHANNEL_SOURCE.API;
    }

    public static final ChannelDetails channelDerivedSTMTimeRemaining = new ChannelDetails(
    		ObjectClusterSensorName.DERIVED_STC_TIME_REMAINING,
    		ObjectClusterSensorName.DERIVED_STC_TIME_REMAINING,
    		ObjectClusterSensorName.DERIVED_STC_TIME_REMAINING,
    		CHANNEL_UNITS.MINUTES,
    		Arrays.asList(CHANNEL_TYPE.CAL),
    		true,
    		false);
    {
    	channelDerivedSTMTimeRemaining.mChannelSource = CHANNEL_SOURCE.API;
    }
	 
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
    	Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
    	aMap.put(ObjectClusterSensorName.STC_VOLTAGE, channelSTMVoltage);
    	aMap.put(ObjectClusterSensorName.STC_CURRENT, channelSTMCurrent);
    	aMap.put(ObjectClusterSensorName.STC_TEMP, channelSTMTemp );
    	aMap.put(ObjectClusterSensorName.STC_CHARGE, channelSTMCharge);
    	aMap.put(ObjectClusterSensorName.STC_BATERY_PERCENTAGE, channelSTMBatteryPercentage);
    	aMap.put(ObjectClusterSensorName.STC_TIME_REMAINING, channelSTMTimeRemaining);
    	aMap.put(ObjectClusterSensorName.DERIVED_STC_CHARGE, channelDerivedSTMCharge);
    	aMap.put(ObjectClusterSensorName.DERIVED_STC_BATTERY_PERCENTAGE, channelDeriveSTMBatteryPercentage);
    	aMap.put(ObjectClusterSensorName.DERIVED_STC_TIME_REMAINING, channelDerivedSTMTimeRemaining);

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
		//No configuration options.
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.STC3100_MONITORING.ordinal(), sensorSTC3100);
		}
		super.updateSensorGroupingMap();		
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] rawData, COMMUNICATION_TYPE commType,ObjectCluster objectCluster, boolean isTimeSyncEnabled,
			double pctimeStampMs) {
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pctimeStampMs);
		double unCalData = 0; double calData = 0;
		
		for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){

			if ((channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_VOLTAGE))
					||(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_CURRENT))
					||(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_TEMP))
					||(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_CHARGE))
					||(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_BATERY_PERCENTAGE))
					||(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_TIME_REMAINING))){

				unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				calData = unCalData/100;

				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_VOLTAGE)){
					setBattCurrentVoltage(calData);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-6);
				}			     
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_CURRENT)){
					setBattCurrent(calData);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-5);
				}

				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_TEMP)){
					setBattTemp(calData);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-4);
				}
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_CHARGE)){
					setBattCurrentCharge(calData);
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-3);
				}
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_BATERY_PERCENTAGE)){
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
				}
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.STC_TIME_REMAINING)){
					objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
				}
			}
			//SW derived
			else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.DERIVED_STC_CHARGE)){
				calculateBattInitialCharge();
				objectCluster.addCalData(channelDetails, mBattInitialCharge);
				objectCluster.incrementIndexKeeper();
			}
			else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.DERIVED_STC_BATTERY_PERCENTAGE)){
				calculateBattPercentage();
				objectCluster.addCalData(channelDetails, mBattPercentage);
				objectCluster.incrementIndexKeeper();
			}
			else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.DERIVED_STC_TIME_REMAINING)){
				calculateBattTimeRemaining();
				objectCluster.addCalData(channelDetails, mBattTimeRemaining);
				objectCluster.incrementIndexKeeper();
				
			}
		}
		return objectCluster;
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId,
			String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId,
			String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId,
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
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void setBattCurrentVoltage(double calData) {
		mBattCurrentVoltage = calData;
	}
	private void setBattCurrentCharge(double calData) {
		mBattCurrentCharge = calData;
	}
	private void calculateBattInitialCharge() {
		mBattInitialCharge = BATT_CHARGE_CAPACITY*(mBattCurrentVoltage/BATT_MAX_VOLTAGE);
	}
	private void calculateBattPercentage() {
		mBattPercentage = 100*((mBattInitialCharge+mBattCurrentCharge)/BATT_CHARGE_CAPACITY);
		
		if (mBattPercentage > 100) {
			mBattPercentage = 100.0;
        }
        else if (mBattPercentage < 0) {
        	mBattPercentage = 0.0;
        }

		mBattPercentageParsed = String.format("%,.1f",mBattPercentage) + "%";
	}

	private void setBattCurrent(double calData) {
		mBattCurrent = calData;
		
	}
	private void setBattTemp(double calData) {
		mBattTemperature = calData;
	}

	private void calculateBattTimeRemaining() {
		mBattTimeRemaining = 60*((mBattInitialCharge+mBattCurrentCharge)/mBattCurrent);
	}

	private double getBattPercentage() {
		return mBattPercentage;
	}
	public String getBattPercentageParsed() {
		return mBattPercentageParsed;
	}

	public void setStc3100Details(SensorSTC3100Details sensorSTC3100Details) {
		mStc3100Details = sensorSTC3100Details;
	}

	public SensorSTC3100Details setStc3100Details() {
		return mStc3100Details;
	}

}
