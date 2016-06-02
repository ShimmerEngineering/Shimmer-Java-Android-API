package com.shimmerresearch.sensors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.SensorLSM303.GuiLabelSensors;
import com.shimmerresearch.sensors.SensorMPU9X50.GuiLabelConfig;
import com.shimmerresearch.sensors.SensorMPU9X50.ObjectClusterSensorName;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */

public class SensorADC extends AbstractSensor {
	
	private static final long serialVersionUID = -1313629173441403991L; // check for correct value
	//--------- Sensor specific variables start --------------
	/**	
	 * 	initialise boolean variables   					-> e.g. mLowPowerAccelWR = false;
	 * 	initialise other variables       					-> e.g. mMagRange = 1;
	 * 	calibration matrices           					-> alignment, sensitivity, offset matrices for each range for each (sub)sensor
	 * 	class GuiLabelConfig           					-> class containing GUI configuration labels 
	 * 	class GuiLabelSensors		   					-> class containing GUI sensor labels
	 *  class GuiLabelSensorTiles      					-> class containing GUI sensor tile labels
	 * 	class DatabaseChannelHandles   					-> class containing Database handles
	 * 	class ObjectClusterSensorName  					-> class containing ObjectClusterSensorName (channel name)
	 * 
	 * What TODO with this in ShimmerObject? In Sensor Class?:
	 * 	- SensorBitMap (for ID/Fw -> What does this mean?)
	 * 	- SDLogHeader
	 *  - SDLogHeaderDerivedSensors
	 *  - BTStreamDerivedSensors
	 *  - BTStream
	 * 
	 */
	public class GuiLabelConfig{
		public static final String PPG_ADC_SELECTION =  SensorPPG.GuiLabelConfig.PPG_ADC_SELECTION; //"PPG Channel";
		public static final String PPG1_ADC_SELECTION = SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION; //"Channel1";
		public static final String PPG2_ADC_SELECTION = SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION; // "Channel2";
	}
	
	// GUI Label Sensors 
	public class GuiLabelSensors{
		public static final String EXT_EXP_A7 = "Ext A7";
		public static final String EXT_EXP_A6 = "Ext A6";
		public static final String EXT_EXP_A15 = "Ext A15";
		public static final String INT_EXP_A12 = "Int A12";
		public static final String INT_EXP_A13 = "Int A13";
		public static final String INT_EXP_A14 = "Int A14";
		public static final String INT_EXP_A1 = "Int A1";
	}
	
	// GUI Sensor Tiles
	public class GuiLabelSensorTiles{
		public static final String EXTERNAL_EXPANSION_ADC = "External Expansion ADCs";
		public static final String INTERNAL_EXPANSION_ADC = "Internal Expansion ADCs";
		
		public static final String EXT_EXP_ADC = "External Expansion";
		}
	
	//DATABASE NAMES
	public static class DatabaseChannelHandles{
		public static final String EXT_ADC_A7 = "F5437a_Ext_A7";
		public static final String EXT_ADC_A6 = "F5437a_Ext_A6";
		public static final String EXT_ADC_A15 = "F5437a_Ext_A15";
		public static final String INT_ADC_A1 = "F5437a_Int_A1";
		public static final String INT_ADC_A12 = "F5437a_Int_A12";
		public static final String INT_ADC_A13 = "F5437a_Int_A13";
		public static final String INT_ADC_A14 = "F5437a_Int_A14";
	}
	
	//GUI AND EXPORT CHANNELS
	public static class ObjectClusterSensorName{
		public static String EXT_EXP_ADC_A7 = "Ext_Exp_A7";
		public static String EXT_EXP_ADC_A6 = "Ext_Exp_A6";
		public static String EXT_EXP_ADC_A15 = "Ext_Exp_A15";
		public static String INT_EXP_ADC_A1 = "Int_Exp_A1";
		public static String INT_EXP_ADC_A12 = "Int_Exp_A12";
		public static String INT_EXP_ADC_A13 = "Int_Exp_A13";
		public static String INT_EXP_ADC_A14 = "Int_Exp_A14";
		
		
	}
	
	
	protected int mPpgAdcSelectionGsrBoard = 0;
	protected int mPpg1AdcSelectionProto3DeluxeBoard = 0;
	protected int mPpg2AdcSelectionProto3DeluxeBoard = 0;
	
	
//--------- Sensor specific variables end --------------
	
	
	//--------- Bluetooth commands start --------------
	/**
	 *  Bluetooth commands related to the sensor 		-> public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
	 *													-> public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;
	 *													-> public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;
	 *  mBtGetCommandMap - LinkedHashmap<K,V>  			-> with for get command:  K=command's byte value, V=BtCommandDetails
	 *  mBtSetCommandMap - LinkedHashmap<K,V>  			-> with for set command:  K=command's byte value, V=BtCommandDetails
	 */
//--------- Bluetooth commands end --------------
	
	//--------- Configuration options start --------------
	public static final String[] ListofCompatibleSensors={"Low Noise Accelerometer","Wide Range Accelerometer","Gyroscope","Magnetometer","Battery Voltage","External ADC A7","External ADC A6","External ADC A15","Internal ADC A1","Internal ADC A12","Internal ADC A13","Internal ADC A14","Pressure","GSR","EXG1","EXG2","EXG1 16Bit","EXG2 16Bit", "Bridge Amplifier"}; 

	
	public static final String[] ListOfPpgAdcSelection= SensorPPG.ListOfPpgAdcSelection; //{"Int A13","Int A12"};
	public static final Integer[] ListOfPpgAdcSelectionConfigValues= SensorPPG.ListOfPpgAdcSelectionConfigValues; // {0,1};
	public static final String[] ListOfPpg1AdcSelection=SensorPPG.ListOfPpg1AdcSelection; //{"Int A13","Int A12"};
	public static final Integer[] ListOfPpg1AdcSelectionConfigValues=SensorPPG.ListOfPpg1AdcSelectionConfigValues; //{0,1};
	public static final String[] ListOfPpg2AdcSelection=SensorPPG.ListOfPpg2AdcSelection; //{"Int A1","Int A14"};
	public static final Integer[] ListOfPpg2AdcSelectionConfigValues= SensorPPG.ListOfPpg2AdcSelectionConfigValues; //{0,1
	
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A1Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.INT_EXP_A1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA1,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1),
			null,  //GuiLabelConfig.PPG1_ADC_SELECTION
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A1), 
			false);
	
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A12Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.INT_EXP_A12,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA12,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12),
			null,
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A12), 
			false);
	
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A13Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.INT_EXP_A13,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA13,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13),
			null,
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A13), 
			false);
	
	public static final SensorDetailsRef sensorADC_INT_EXP_ADC_A14Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.INT_EXP_A14,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA14,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14),
			null,
			Arrays.asList(ObjectClusterSensorName.INT_EXP_ADC_A14), 
			false);
	
	
	public static final SensorDetailsRef sensorADC_EXT_EXP_ADC_A7Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.EXT_EXP_A7,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A7),
			null,
			Arrays.asList(ObjectClusterSensorName.EXT_EXP_ADC_A7), 
			false);
	
	public static final SensorDetailsRef sensorADC_EXT_EXP_ADC_A6Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.EXT_EXP_A6,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A6),
			null,
			Arrays.asList(ObjectClusterSensorName.EXT_EXP_ADC_A6), 
			false);
	
	public static final SensorDetailsRef sensorADC_EXT_EXP_ADC_A15Ref = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.EXT_EXP_A15,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A15),
			null,
			Arrays.asList(ObjectClusterSensorName.EXT_EXP_ADC_A15), 
			false);
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1, sensorADC_INT_EXP_ADC_A1Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, sensorADC_INT_EXP_ADC_A12Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13, sensorADC_INT_EXP_ADC_A13Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14, sensorADC_INT_EXP_ADC_A14Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A6, sensorADC_EXT_EXP_ADC_A6Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A7, sensorADC_EXT_EXP_ADC_A7Ref);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A15, sensorADC_EXT_EXP_ADC_A15Ref);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	
	
	//--------- Sensor info end --------------
	
	
	//--------- Constructors for this class start --------------
	public SensorADC(ShimmerVerObject svo) {
		super(svo);
		// TODO Auto-generated constructor stub
	}
	
	//--------- Constructors for this class end --------------
	
	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,
			byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled,
			long pctimestamp) {
		// TODO Auto-generated method stub
		return null;
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
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
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
	
	//--------- Abstract methods implemented end --------------
	
}
