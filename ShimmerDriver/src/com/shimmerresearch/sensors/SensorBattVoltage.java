package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

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
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.SensorLSM303.GuiLabelSensors;
import com.shimmerresearch.sensors.SensorLSM303.ObjectClusterSensorName;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */
public class SensorBattVoltage extends AbstractSensor{
	
	/** *  */
	private static final long serialVersionUID = -2835431738933986391L;
	
	//--------- Sensor specific variables start --------------
//	protected DescriptiveStatistics mVSenseBattMA= new DescriptiveStatistics(1024);
	
	/** GQ BLE */
	protected int mSamplingDividerVBatt = 0;

	private ShimmerBattStatusDetails shimmerBattStatusDetails = new ShimmerBattStatusDetails();
	
	public class GuiLabelConfig{
		// Not in this class
	}
	
	public class GuiLabelSensors{
		public static final String BATTERY = "Battery Voltage";
	}
	
	public class GuiLabelSensorTiles{
		public static final String BATTERY_MONITORING = GuiLabelSensors.BATTERY;
	}
	
	public static class DatabaseChannelHandles{
		public static final String BATTERY = "F5437a_Int_A2_Battery";
	}
	public static class ObjectClusterSensorName{
		public static final String BATT_PERCENTAGE = "Batt_Percentage";
		public static final String BATTERY = "Battery";
	}
	
	
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start ------------------
	
	public static final byte VBATT_RESPONSE                         = (byte) 0x94;
	public static final byte GET_VBATT_COMMAND                      = (byte) 0x95;
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_VBATT_COMMAND, new BtCommandDetails(GET_VBATT_COMMAND, "GET_VBATT_COMMAND", VBATT_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
	
//    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
//    static {
//       mBtSetCommandMap = Collections.unmodifiableMap(aMap);
//    }
//	
    
    
    
  //--------- Configuration options start -------------- 
  //--------- Configuration options end --------------
	
	
  	//--------- Sensor info start --------------
  	public static final SensorDetailsRef sensorBattVoltageRef = new SensorDetailsRef(
  			0x2000, 
  			0x2000, 
  			GuiLabelSensors.BATTERY,
  			null,
  			null,
  			Arrays.asList(ObjectClusterSensorName.BATTERY,
  					ObjectClusterSensorName.BATT_PERCENTAGE));
    
  	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_VBATT, SensorBattVoltage.sensorBattVoltageRef);  
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
  	
    public static final SensorGroupingDetails sensorBattVoltage = new SensorGroupingDetails(
			GuiLabelSensorTiles.BATTERY_MONITORING,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_VBATT),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
  	
	//--------- Sensor info end --------------
	
	
	//--------- Channel info start --------------
  	
  	 public static final ChannelDetails channelBattPercentage = new ChannelDetails(
 			ObjectClusterSensorName.BATT_PERCENTAGE,
 			ObjectClusterSensorName.BATT_PERCENTAGE,
 			ObjectClusterSensorName.BATT_PERCENTAGE,
 			CHANNEL_UNITS.PERCENT,
 			Arrays.asList(CHANNEL_TYPE.CAL),
 			true,
 			false);
  	
  	 public static final ChannelDetails channelBattVolt = new ChannelDetails(
					ObjectClusterSensorName.BATTERY,
					ObjectClusterSensorName.BATTERY,
					DatabaseChannelHandles.BATTERY,
					CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
					CHANNEL_UNITS.MILLIVOLTS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
					true,
					true);
  	 
  	 
  	public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(ObjectClusterSensorName.BATT_PERCENTAGE, channelBattPercentage);
        aMap.put(ObjectClusterSensorName.BATTERY, channelBattVolt);

        mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
  	
  	// -----------Channel info end ------
    
    //--------- Constructors for this class start --------------  	
	public SensorBattVoltage(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.Battery.toString());
	}
	 //--------- Constructors for this class end --------------
	
	
	//--------- Abstract methods implemented start --------------
	
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
//		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		//Not in this class
		
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BATTERY_MONITORING.ordinal(), sensorBattVoltage);
			
		}
		super.updateSensorGroupingMap();
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// Not in this class
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] rawData, COMMUNICATION_TYPE commType,ObjectCluster objectCluster, boolean isTimeSyncEnabled,
			long pcTimeStamp) {

		
		if (mEnableCalibration){
			for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.BATTERY)){
				objectCluster = SensorADC.processMspAdcChannel(sensorDetails, rawData, commType, objectCluster, isTimeSyncEnabled, pcTimeStamp);
				}
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.BATT_PERCENTAGE)){
					double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.BATTERY), CHANNEL_TYPE.CAL.toString())).mData;

					shimmerBattStatusDetails.processBattPercentage(calData);
					
					objectCluster.addCalData(channelDetails, shimmerBattStatusDetails.mEstimatedChargePercentage);
					
					System.err.println(shimmerBattStatusDetails.mEstimatedChargePercentageParsed);
					objectCluster.incrementIndexKeeper();
				}
			}
		}
		
		return null;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String groupName, String componentName, Object valueToSet) {
		Object returnValue = null;
//		if (componentName.equals(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT)){
//		    setSamplingDividerVBatt((int)valueToSet);
//		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
//		if(componentName.equals(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT)){
//			returnValue = getSamplingDividerVBatt();
//		}	
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey,
			boolean isSensorEnabled) {
		// //Not in this class
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		//Not in this class
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// Not in this class
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// Not in this class
		return null;
	}

	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
	
	/** ShimmerGQBle
	 * @param mSamplingDividerVBatt the mSamplingDividerVBatt to set
	 */
	public void setSamplingDividerVBatt(int mSamplingDividerVBatt) {
		this.mSamplingDividerVBatt = mSamplingDividerVBatt;
	}
	
	
	/**
	 * @return the mSamplingDividerVBatt
	 */
	public int getSamplingDividerVBatt() {
		return mSamplingDividerVBatt;
	}

	


}
