package com.shimmerresearch.sensors.lis2dw12;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.ConfigOptionObject;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLIS2DW12 extends AbstractSensor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5066903487855750207L;
	
	// ----------   Wide-range accel start ---------------
	protected int mSensorIdAccel = -1;
	protected int mAccelRange = 0;
	public boolean mIsUsingDefaultWRAccelParam = true;
	protected boolean mHighResAccelWR = true;
	
	protected boolean mHighPerModeAccelWR = true;
	protected boolean mLowPowerAccelWR = false;
	protected int mLIS2DW12DigitalAccelRate = 0;
	
	public class GuiLabelConfig{
		public static final String LIS2DW12_ACCEL_RANGE = "Wide Range Accel Range"; 
		public static final String LIS2DW12_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LIS2DW12_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
	}
	
	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
		public static final String ACCEL = "ACCEL";
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_WR_X = "Accel_WR_X";
		public static  String ACCEL_WR_Y = "Accel_WR_Y";
		public static  String ACCEL_WR_Z= "Accel_WR_Z";		
	}
	
	public static class DatabaseChannelHandles{
		public static final String WR_ACC_X = "LIS2DW12_ACC_X";
		public static final String WR_ACC_Y = "LIS2DW12_ACC_Y";
		public static final String WR_ACC_Z = "LIS2DW12_ACC_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String WR_ACC = "LIS2DW12_Acc";
		public static final String WR_ACC_RATE = "LIS2DW12_Acc_Rate";
		public static final String WR_ACC_RANGE = "LIS2DW12_Acc_Range";
		
		public static final String WR_ACC_LPM = "LIS2DW12_Acc_LPM";
		public static final String WR_ACC_HPM = "LIS2DW12_Acc_HPM";
		
		public static final String WR_ACC_CALIB_TIME = "LIS2DW12_Acc_Calib_Time";
		public static final String WR_ACC_OFFSET_X = "LIS2DW12_Acc_Offset_X";
		public static final String WR_ACC_OFFSET_Y = "LIS2DW12_Acc_Offset_Y";
		public static final String WR_ACC_OFFSET_Z = "LIS2DW12_Acc_Offset_Z";
		public static final String WR_ACC_GAIN_X = "LIS2DW12_Acc_Gain_X";
		public static final String WR_ACC_GAIN_Y = "LIS2DW12_Acc_Gain_Y";
		public static final String WR_ACC_GAIN_Z = "LIS2DW12_Acc_Gain_Z";
		public static final String WR_ACC_ALIGN_XX = "LIS2DW12_Acc_Align_XX";
		public static final String WR_ACC_ALIGN_XY = "LIS2DW12_Acc_Align_XY";
		public static final String WR_ACC_ALIGN_XZ = "LIS2DW12_Acc_Align_XZ";
		public static final String WR_ACC_ALIGN_YX = "LIS2DW12_Acc_Align_YX";
		public static final String WR_ACC_ALIGN_YY = "LIS2DW12_Acc_Align_YY";
		public static final String WR_ACC_ALIGN_YZ = "LIS2DW12_Acc_Align_YZ";
		public static final String WR_ACC_ALIGN_ZX = "LIS2DW12_Acc_Align_ZX";
		public static final String WR_ACC_ALIGN_ZY = "LIS2DW12_Acc_Align_ZY";
		public static final String WR_ACC_ALIGN_ZZ = "LIS2DW12_Acc_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_WR_ACCEL = Arrays.asList(
				DatabaseConfigHandle.WR_ACC_OFFSET_X, DatabaseConfigHandle.WR_ACC_OFFSET_Y, DatabaseConfigHandle.WR_ACC_OFFSET_Z,
				DatabaseConfigHandle.WR_ACC_GAIN_X, DatabaseConfigHandle.WR_ACC_GAIN_Y, DatabaseConfigHandle.WR_ACC_GAIN_Z,
				DatabaseConfigHandle.WR_ACC_ALIGN_XX, DatabaseConfigHandle.WR_ACC_ALIGN_XY, DatabaseConfigHandle.WR_ACC_ALIGN_XZ,
				DatabaseConfigHandle.WR_ACC_ALIGN_YX, DatabaseConfigHandle.WR_ACC_ALIGN_YY, DatabaseConfigHandle.WR_ACC_ALIGN_YZ,
				DatabaseConfigHandle.WR_ACC_ALIGN_ZX, DatabaseConfigHandle.WR_ACC_ALIGN_ZY, DatabaseConfigHandle.WR_ACC_ALIGN_ZZ);
	}
	
	public static final String[] ListofLIS2DW12AccelRange={
			UtilShimmer.UNICODE_PLUS_MINUS + " 2g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 4g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 8g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 16g"
	};  
	
	public static final double[][] DefaultAlignmentLIS2DW12 = {{0,-1,0},{-1,0,0},{0,0,-1}};	
	public static final double[][] DefaultAlignmentMatrixWideRangeAccelShimmer3R = DefaultAlignmentLIS2DW12;
	
	public static final double[][] DefaultOffsetVectorWideRangeAccelShimmer3R = {{0},{0},{0}};
	
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel2gShimmer3R = {{1671,0,0},{0,1671,0},{0,0,1671}};
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel4gShimmer3R = {{836,0,0},{0,836,0},{0,0,836}};
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel8gShimmer3R = {{418,0,0},{0,418,0},{0,0,418}};
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel16gShimmer3R = {{209,0,0},{0,209,0},{0,0,209}};

	private CalibDetailsKinematic calibDetailsAccelWr2g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[0],
			ListofLIS2DW12AccelRange[0],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R, 
			DefaultSensitivityMatrixWideRangeAccel2gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	private CalibDetailsKinematic calibDetailsAccelWr4g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[1], 
			ListofLIS2DW12AccelRange[1],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R,
			DefaultSensitivityMatrixWideRangeAccel4gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	private CalibDetailsKinematic calibDetailsAccelWr8g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[2], 
			ListofLIS2DW12AccelRange[2],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R, 
			DefaultSensitivityMatrixWideRangeAccel8gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	private CalibDetailsKinematic calibDetailsAccelWr16g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[3], 
			ListofLIS2DW12AccelRange[3],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R,
			DefaultSensitivityMatrixWideRangeAccel16gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccelWr = calibDetailsAccelWr2g;
	// ----------   Wide-range accel end ---------------
	
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLIS2DW12Accel = new SensorDetailsRef(
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12,
			Arrays.asList(GuiLabelConfig.LIS2DW12_ACCEL_RANGE,
					GuiLabelConfig.LIS2DW12_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,
					ObjectClusterSensorName.ACCEL_WR_Y,
					ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR, SensorLIS2DW12.sensorLIS2DW12Accel);  	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
    
    
	//--------- Bluetooth commands start --------------
	public static final byte SET_WR_ACCEL_RANGE_COMMAND    		= (byte) 0x09;
	public static final byte WR_ACCEL_RANGE_RESPONSE       		= (byte) 0x0A;
	public static final byte GET_WR_ACCEL_RANGE_COMMAND    		= (byte) 0x0B;
	
	public static final byte SET_WR_ACCEL_CALIBRATION_COMMAND 	= (byte) 0x1A;
	public static final byte WR_ACCEL_CALIBRATION_RESPONSE	 	= (byte) 0x1B;
	public static final byte GET_WR_ACCEL_CALIBRATION_COMMAND  	= (byte) 0x1C;
	
	public static final byte SET_WR_ACCEL_SAMPLING_RATE_COMMAND  	= (byte) 0x40;
	public static final byte WR_ACCEL_SAMPLING_RATE_RESPONSE  		= (byte) 0x41;
	public static final byte GET_WR_ACCEL_SAMPLING_RATE_COMMAND  	= (byte) 0x42;
	
	public static final byte SET_WR_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;
	public static final byte WR_ACCEL_LPMODE_RESPONSE		= (byte) 0x44;
	public static final byte GET_WR_ACCEL_LPMODE_COMMAND  	= (byte) 0x45;
	
	public static final byte SET_WR_ACCEL_HRMODE_COMMAND 	= (byte) 0x46;
	public static final byte WR_ACCEL_HRMODE_RESPONSE		= (byte) 0x47;
	public static final byte GET_WR_ACCEL_HRMODE_COMMAND 	= (byte) 0x48;
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_WR_ACCEL_RANGE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_RANGE_COMMAND, "GET_WR_ACCEL_RANGE_COMMAND", WR_ACCEL_RANGE_RESPONSE));
        aMap.put(GET_WR_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_WR_ACCEL_CALIBRATION_COMMAND, "GET_WR_ACCEL_CALIBRATION_COMMAND", WR_ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_WR_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_SAMPLING_RATE_COMMAND, "GET_WR_ACCEL_SAMPLING_RATE_COMMAND", WR_ACCEL_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_WR_ACCEL_LPMODE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_LPMODE_COMMAND, "GET_WR_ACCEL_LPMODE_COMMAND", WR_ACCEL_LPMODE_RESPONSE));
        aMap.put(GET_WR_ACCEL_HRMODE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_HRMODE_COMMAND, "GET_WR_ACCEL_HRMODE_COMMAND", WR_ACCEL_HRMODE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_WR_ACCEL_RANGE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_RANGE_COMMAND, "SET_WR_ACCEL_RANGE_COMMAND"));
        aMap.put(SET_WR_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_WR_ACCEL_CALIBRATION_COMMAND, "SET_WR_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_WR_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_SAMPLING_RATE_COMMAND, "SET_WR_ACCEL_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_WR_ACCEL_LPMODE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_LPMODE_COMMAND, "SET_WR_ACCEL_LPMODE_COMMAND"));
        aMap.put(SET_WR_ACCEL_HRMODE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_HRMODE_COMMAND, "SET_WR_ACCEL_HRMODE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
    
    
    
	//--------- Configuration options start --------------
	public static final Integer[] ListofLIS2DW12AccelRangeConfigValues={0,1,2,3};  

	public static final String[] ListofLIS2DW12AccelRateHpm={"Power-down","12.5Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz","1600.0Hz"};
	public static final Integer[] ListofLIS2DW12AccelRateHpmConfigValues={0,1,2,3,4,5,6,7,8,9};

	public static final String[] ListofLIS2DW12AccelRateLpm={"Power-down","1.6Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","200.0Hz","200.0Hz","200.0Hz"};
	public static final Integer[] ListofLIS2DW12AccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};

	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_RANGE,
			SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RANGE,
			ListofLIS2DW12AccelRange, 
			ListofLIS2DW12AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12); 

	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_RATE,
			SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RATE,
			ListofLIS2DW12AccelRateHpm, 
			ListofLIS2DW12AccelRateHpmConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12, 
			Arrays.asList(
				new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS2DW12_ACCEL_RATE.IS_LPM, 
						ListofLIS2DW12AccelRateLpm, 
						ListofLIS2DW12AccelRateLpmConfigValues)));
	
	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_LPM,
			SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_LPM,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);
	//--------- Configuration options end --------------
	
	
	
	//--------- Channel info start --------------
    public static final ChannelDetails channelLIS2DW12AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_X,
			ObjectClusterSensorName.ACCEL_WR_X,
			DatabaseChannelHandles.WR_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x04);
    
    public static final ChannelDetails channelLIS2DW12AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Y,
			ObjectClusterSensorName.ACCEL_WR_Y,
			DatabaseChannelHandles.WR_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x05);
    
    public static final ChannelDetails channelLIS2DW12AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Z,
			ObjectClusterSensorName.ACCEL_WR_Z,
			DatabaseChannelHandles.WR_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x06);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS2DW12.ObjectClusterSensorName.ACCEL_WR_X, SensorLIS2DW12.channelLIS2DW12AccelX);
        aMap.put(SensorLIS2DW12.ObjectClusterSensorName.ACCEL_WR_Y, SensorLIS2DW12.channelLIS2DW12AccelY);
        aMap.put(SensorLIS2DW12.ObjectClusterSensorName.ACCEL_WR_Z, SensorLIS2DW12.channelLIS2DW12AccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLpmAccel = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12); // To Be Changed
	
    
	
    //--------- Constructors for this class start --------------
	public SensorLIS2DW12() {
		super(SENSORS.LIS2DW12);
		initialise();
	}
	
	public SensorLIS2DW12(ShimmerVerObject svo) {
		super(SENSORS.LIS2DW12, svo);
		initialise();
	}

	public SensorLIS2DW12(ShimmerDevice shimmerDevice) {
		super(SENSORS.LIS2DW12, shimmerDevice);
		initialise();
	}
   //--------- Constructors for this class end --------------

	

	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub
		addConfigOption(configOptionAccelRange);
		addConfigOption(configOptionAccelRate);
		addConfigOption(configOptionAccelLpm);
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL_3R.ordinal(), sensorGroupLpmAccel);
		super.updateSensorGroupingMap();
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		
		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR) && mCurrentCalibDetailsAccelWr!=null){
				double[] unCalibratedAccelWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Accelerometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
						unCalibratedAccelWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
						unCalibratedAccelWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
						unCalibratedAccelWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mCurrentCalibDetailsAccelWr);	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultWRAccelParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultWRAccelParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultWRAccelParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.CAL.toString()}));
				}
			}
		}
		return objectCluster;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdAccel)) {
			setDefaultLIS2DW12AccelSensorConfig(false);
		}
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (byte) ((getLIS2DW12DigitalAccelRate() & configByteLayoutCast.maskLSM303DLHCAccelSamplingRate) << configByteLayoutCast.bitShiftLSM303DLHCAccelSamplingRate);
			configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (byte) ((getAccelRange() & configByteLayoutCast.maskLSM303DLHCAccelRange) << configByteLayoutCast.bitShiftLSM303DLHCAccelRange);
			if(isLowPowerAccelWR()) {
				configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (configByteLayoutCast.maskLSM303DLHCAccelLPM << configByteLayoutCast.bitShiftLSM303DLHCAccelLPM);
			}
			if(isHighPerModeAccelWR()) {
				configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (configByteLayoutCast.maskLSM303DLHCAccelHRM << configByteLayoutCast.bitShiftLSM303DLHCAccelHRM);
			}

			// LSM303DLHC Digital Accel Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamLIS2DW12Accel();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLSM303DLHCAccelCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
		}	
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			setLIS2DW12DigitalAccelRate((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelSamplingRate) & configByteLayoutCast.maskLSM303DLHCAccelSamplingRate); 
			setLIS2DW12AccelRange((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelRange) & configByteLayoutCast.maskLSM303DLHCAccelRange);
			if(((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelLPM) & configByteLayoutCast.maskLSM303DLHCAccelLPM) == configByteLayoutCast.maskLSM303DLHCAccelLPM) {
				setLowPowerAccelWR(true);
			}
			else {
				setLowPowerAccelWR(false);
			}
			if(((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelHRM) & configByteLayoutCast.maskLSM303DLHCAccelHRM) == configByteLayoutCast.maskLSM303DLHCAccelHRM) {
				setHighPerModeAccelWR(true);
			}
			else {
				setHighPerModeAccelWR(false);
			}

			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsAccelWr().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			// LIS2DW12 Digital Accel Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketAccelWR(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LIS2DW12_ACCEL_LPM):
				setLowPowerAccelWR((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_ACCEL_RANGE):
				setLIS2DW12AccelRange((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_ACCEL_RATE):
				setLIS2DW12DigitalAccelRate((int)valueToSet);
				break;

			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS2DW12_ACCEL_RANGE, valueToSet);
					break;
				}
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS2DW12_ACCEL_RATE, valueToSet);
					break;
				}
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LIS2DW12_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.LIS2DW12_ACCEL_LPM):
				returnValue = isLIS2DW12DigitalAccelLPM();
	        	break;
			case(GuiLabelConfig.LIS2DW12_ACCEL_RANGE): 
				returnValue = getAccelRange();
		    	break;
			case(GuiLabelConfig.LIS2DW12_ACCEL_RATE): 
				int configValue = getLIS2DW12DigitalAccelRate(); 
				returnValue = configValue;
				break;
	        	
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS2DW12_ACCEL_RANGE);
					break;
				}
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS2DW12_ACCEL_RATE);
					break;
				}
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		//set sampling rate of the sensors as close to the Shimmer sampling rate as possible (sensor sampling rate >= shimmer sampling rate)
		setLIS2DW12AccelRateFromFreq(samplingRateHz);

	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdAccel) {
				setDefaultLIS2DW12AccelSensorConfig(isSensorEnabled);		
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			if(stringKey==GuiLabelConfig.LIS2DW12_ACCEL_RATE){
				if(isLIS2DW12DigitalAccelLPM()) {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS2DW12_ACCEL_RATE.IS_LPM);
				}
				else {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS2DW12_ACCEL_RATE.NOT_LPM);
				}
			}		
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==mSensorIdAccel){
			return isUsingDefaultWRAccelParam();
		}
		return false;
	}
	
	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentAccelWrCalibInUse();
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		ActionSetting actionsetting = new ActionSetting(commType);		
		return actionsetting;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RATE, getLIS2DW12DigitalAccelRate());
		mapOfConfig.put(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RANGE, getAccelRange());
		mapOfConfig.put(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_LPM, getLowPowerAccelEnabled());
		mapOfConfig.put(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_HPM, getHighPerModeAccelWREnabled());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelWr(), 
				SensorLIS2DW12.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL,
				SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_CALIB_TIME);

		return mapOfConfig;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		//Better if LPM/HRM are processed first as they can override the sampling rate
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_LPM)){
			setLowPowerAccelWR(((Double) mapOfConfigPerShimmer.get(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_LPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_HPM)){
			setHighResAccelWR(((Double) mapOfConfigPerShimmer.get(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_HPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RATE)){
			setLIS2DW12DigitalAccelRate(((Double) mapOfConfigPerShimmer.get(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RANGE)){
			setLIS2DW12AccelRange(((Double) mapOfConfigPerShimmer.get(SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RANGE)).intValue());
		}
		
		//Digital Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR, 
				getAccelRange(), 
				SensorLIS2DW12.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL,
				SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_CALIB_TIME);
	}
	
	
	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR;
		super.initialise();
		
		updateCurrentAccelWrCalibInUse();
	}
	
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelWr = new TreeMap<Integer, CalibDetails>();
		calibMapAccelWr.put(calibDetailsAccelWr2g.mRangeValue, calibDetailsAccelWr2g);
		calibMapAccelWr.put(calibDetailsAccelWr4g.mRangeValue, calibDetailsAccelWr4g);
		calibMapAccelWr.put(calibDetailsAccelWr8g.mRangeValue, calibDetailsAccelWr8g);
		calibMapAccelWr.put(calibDetailsAccelWr16g.mRangeValue, calibDetailsAccelWr16g);
		setCalibrationMapPerSensor(mSensorIdAccel, calibMapAccelWr);

		updateCurrentAccelWrCalibInUse();
	}
	//--------- Optional methods to override in Sensor Class end --------
	
	
	
	//--------- Sensor specific methods start --------------
	public void setHighResAccelWR(boolean enable) {
		mHighResAccelWR = enable;
	}
	
	public void setHighResAccelWR(int i){
		mHighResAccelWR = (i>0)? true:false;
	}
	
	public void updateCurrentAccelWrCalibInUse(){
		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsIfKinematic(mSensorIdAccel, getAccelRange());
	}
	
	public int getAccelRange() {
		return mAccelRange;
	}
	
	public boolean isUsingDefaultWRAccelParam(){
		return mCurrentCalibDetailsAccelWr.isUsingDefaultParameters(); 
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsAccelWr(){
		return mCurrentCalibDetailsAccelWr;
	}
	
	public void updateIsUsingDefaultWRAccelParam() {
		mIsUsingDefaultWRAccelParam = getCurrentCalibDetailsAccelWr().isUsingDefaultParameters();
	}
	
	public void setHighPerModeAccelWR(boolean enable) {
		mHighPerModeAccelWR = enable;
	}
	
	public void setHighPerModeAccelWR(int i){
		mHighPerModeAccelWR = (i>0)? true:false;
	}
	
	public int getHighPerModeAccelWREnabled(){
		return (mHighPerModeAccelWR? 1:0);
	}
	
	public void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighPerModeAccelWR = !enable;
		if(mShimmerDevice!=null){
			setLIS2DW12AccelRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public int setLIS2DW12AccelRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdAccel);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelWR:" + mLowPowerAccelWR);
		setLIS2DW12DigitalAccelRateInternal(getAccelRateFromFreqForSensor(isEnabled, freq, mLowPowerAccelWR));
		return mLIS2DW12DigitalAccelRate;
	}
	
	public void setLIS2DW12DigitalAccelRateInternal(int valueToSet) {
		//System.out.println("Accel Rate:\t" + valueToSet);
		//UtilShimmer.consolePrintCurrentStackTrace();
		mLIS2DW12DigitalAccelRate = valueToSet;
	}
	
	public int getAccelRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode) {
		return SensorLIS2DW12.getAccelRateFromFreq(isEnabled, freq, isLowPowerMode);
	}
	
	public static int getAccelRateFromFreq(boolean isEnabled, double freq, boolean isLowPowerMode) {
		int accelRate = 0; // Power down
		
		if(isEnabled){
			if(isLowPowerMode){
				accelRate = 1; // 1.6Hz
			} else {
				if (freq<=12.5){
					accelRate = 1; // 12.5Hz
				} else if (freq<=25){
					accelRate = 3; // 25Hz
				} else if (freq<=50){
					accelRate = 4; // 50Hz
				} else if (freq<=100){
					accelRate = 5; // 100Hz
				} else if (freq<=200){
					accelRate = 6; // 200Hz
				} else if (freq<=400){
					accelRate = 7; // 400Hz
				} else if (freq<=800){
					accelRate = 8; // 800Hz
				} else{
					accelRate = 9; // 1600Hz
				} 
			}
		}
		return accelRate;
	}
	
	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}
	
	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
	
	public void setLIS2DW12AccelRange(int valueToSet){
		if(ArrayUtils.contains(ListofLIS2DW12AccelRangeConfigValues, valueToSet)){
			mAccelRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
	}
	
	public void setLIS2DW12DigitalAccelRate(int valueToSet) {
		setLIS2DW12DigitalAccelRateInternal(valueToSet);
		if(mLowPowerAccelWR){
			for(Integer i:SensorLIS2DW12.ListofLIS2DW12AccelRateLpmConfigValues){
				if(i==valueToSet){
					return;
				}
			}
			setLIS2DW12DigitalAccelRateInternal(SensorLIS2DW12.ListofLIS2DW12AccelRateLpmConfigValues[SensorLIS2DW12.ListofLIS2DW12AccelRateLpmConfigValues.length-1]);
		} else {
			for(Integer i:SensorLIS2DW12.ListofLIS2DW12AccelRateHpmConfigValues){
				if(i==valueToSet){
					return;
				}
			}
			setLIS2DW12DigitalAccelRateInternal(SensorLIS2DW12.ListofLIS2DW12AccelRateHpmConfigValues[SensorLIS2DW12.ListofLIS2DW12AccelRateHpmConfigValues.length-1]);
		}
	}
	
	public void setDefaultLIS2DW12AccelSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerAccelWR(false);
		}
		else {
			setLIS2DW12AccelRange(0);
			setLowPowerAccelWR(true);
		}
	}
	
	public boolean isHighPerModeAccelWR(){
		return isLIS2DW12DigitalAccelHPM();
	}
	
	public boolean isLIS2DW12DigitalAccelHPM() {
		return mHighPerModeAccelWR;
	}
	
	public double getCalibTimeWRAccel() {
		return mCurrentCalibDetailsAccelWr.getCalibTimeMs();
	}
	
	public boolean isUsingValidWRAccelParam(){
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixWRAccel()) && !UtilShimmer.isAllZeros(getSensitivityMatrixWRAccel())){
			return true;
		}else{
			return false;
		}
	}
	
	public double[][] getAlignmentMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getValidAlignmentMatrix();
	}
	
	public double[][] getSensitivityMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getValidSensitivityMatrix();
	}
	
	public double[][] getOffsetVectorMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getValidOffsetVector();
	}
	
	public boolean isLowPowerAccelWR(){
		return isLIS2DW12DigitalAccelLPM();
	}
	
	public boolean isLIS2DW12DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}
	
	public boolean isLowPowerAccelEnabled() {
		return isLIS2DW12DigitalAccelLPM();
	}
	
	public void setLowPowerAccelEnabled(int i){
		mLowPowerAccelWR = (i>0)? true:false;
	}
	
	public int getLowPowerAccelEnabled(){
		return (isLIS2DW12DigitalAccelLPM()? 1:0);
	}
	
	public int getLIS2DW12DigitalAccelRate() {
		return mLIS2DW12DigitalAccelRate;
	}
	
	public byte[] generateCalParamLIS2DW12Accel(){
		return mCurrentCalibDetailsAccelWr.generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelWR(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelWr.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	public void setDefaultCalibrationShimmer3rWideRangeAccel() {
		mCurrentCalibDetailsAccelWr.resetToDefaultParameters();
	}
	//--------- Sensor specific methods end --------------
	
}