package com.shimmerresearch.sensors.lsm303;

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
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.ConfigOptionObject;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

//TODO update alignments for accel and mag (x swapped with y)
//TODO take into account Accel 12-bit vs. 14-bit vs. 10-bit modes when setting default calibration sensitivity values 
public class SensorLSM303AH extends SensorLSM303 {

	private static final long serialVersionUID = 5566898733753766631L;

	
	//--------- Sensor specific variables start --------------	

	// ----------   Wide-range accel start ---------------

	public static final double[][] DefaultAlignmentLSM303AH = {{0,-1,0},{1,0,0},{0,0,-1}};	

	public static final double[][] DefaultAlignmentMatrixWideRangeAccelShimmer3 = DefaultAlignmentLSM303AH;	
	public static final double[][] DefaultOffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};	
	// Manufacturer stated +-2g -> 0.061 mg/LSB -> or 16393.4 LSB/g or 1671.1 LSB/(m/s2) 
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel2gShimmer3 = {{1671,0,0},{0,1671,0},{0,0,1671}};
	// Manufacturer stated +-4g -> 0.122 mg/LSB -> or 8196.72 LSB/g or 835.55 LSB/(m/s2) 
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel4gShimmer3 = {{836,0,0},{0,836,0},{0,0,836}};
	// Manufacturer stated +-8g -> 0.244 mg/LSB -> or 4098.36 LSB/g or 417.77 LSB/(m/s2) 
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel8gShimmer3 = {{418,0,0},{0,418,0},{0,0,418}};
	// Manufacturer stated +-16g -> 0.488 mg/LSB -> or 2049.18 LSB/g or 208.89 LSB/(m/s2) 
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel16gShimmer3 = {{209,0,0},{0,209,0},{0,0,209}};

	private CalibDetailsKinematic calibDetailsAccelWr2g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[0],
			ListofLSM303AccelRange[0],
			DefaultAlignmentMatrixWideRangeAccelShimmer3, 
			DefaultSensitivityMatrixWideRangeAccel2gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr4g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[1], 
			ListofLSM303AccelRange[1],
			DefaultAlignmentMatrixWideRangeAccelShimmer3,
			DefaultSensitivityMatrixWideRangeAccel4gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr8g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[2], 
			ListofLSM303AccelRange[2],
			DefaultAlignmentMatrixWideRangeAccelShimmer3, 
			DefaultSensitivityMatrixWideRangeAccel8gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr16g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[3], 
			ListofLSM303AccelRange[3],
			DefaultAlignmentMatrixWideRangeAccelShimmer3,
			DefaultSensitivityMatrixWideRangeAccel16gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);

	// ----------   Wide-range accel end ---------------

	// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixMagShimmer3 = DefaultAlignmentLSM303AH; 				
	public static final double[][] DefaultOffsetVectorMagShimmer3 = {{0},{0},{0}};	
	//  Based on a manufacturer stated, 1.5 mgauss/LSB (or 667 LSB/mgauss) over a full range of +-49.152 gauss 
	// [16-bit ADC -> 0-65536 for 0-98.304gauss -> 65536 LSBs/98304 mgauss = 0.6666 LSB/mgauss]
	public static final double[][] DefaultSensitivityMatrixMag50GaShimmer3 = {{667,0,0},{0,667,0},{0,0,667}};
	//TODO figure out if the output units are correct give that the above sensitivity calculation is off by 1000 

	private CalibDetailsKinematic calibDetailsMag50Ga = new CalibDetailsKinematic(
			ListofLSM303AHMagRangeConfigValues[0],
			ListofLSM303AHMagRange[0],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag50GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	public CalibDetailsKinematic mCurrentCalibDetailsMag = calibDetailsMag50Ga;

	// ----------   Mag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String WR_ACC_X = "LSM303AHTR_ACC_X";
		public static final String WR_ACC_Y = "LSM303AHTR_ACC_Y";
		public static final String WR_ACC_Z = "LSM303AHTR_ACC_Z";
		public static final String MAG_X = "LSM303AHTR_MAG_X";
		public static final String MAG_Y = "LSM303AHTR_MAG_Y";
		public static final String MAG_Z = "LSM303AHTR_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String MAG_RANGE = "LSM303AHTR_Mag_Range";
		public static final String MAG_RATE = "LSM303AHTR_Mag_Rate";
		
		public static final String WR_ACC = "LSM303AHTR_Acc";
		public static final String WR_ACC_RATE = "LSM303AHTR_Acc_Rate";
		public static final String WR_ACC_RANGE = "LSM303AHTR_Acc_Range";
		
		public static final String WR_ACC_LPM = "LSM303AHTR_Acc_LPM";
		public static final String WR_ACC_HRM = "LSM303AHTR_Acc_HRM";
		
		public static final String WR_ACC_CALIB_TIME = "LSM303AHTR_Acc_Calib_Time";
		public static final String WR_ACC_OFFSET_X = "LSM303AHTR_Acc_Offset_X";
		public static final String WR_ACC_OFFSET_Y = "LSM303AHTR_Acc_Offset_Y";
		public static final String WR_ACC_OFFSET_Z = "LSM303AHTR_Acc_Offset_Z";
		public static final String WR_ACC_GAIN_X = "LSM303AHTR_Acc_Gain_X";
		public static final String WR_ACC_GAIN_Y = "LSM303AHTR_Acc_Gain_Y";
		public static final String WR_ACC_GAIN_Z = "LSM303AHTR_Acc_Gain_Z";
		public static final String WR_ACC_ALIGN_XX = "LSM303AHTR_Acc_Align_XX";
		public static final String WR_ACC_ALIGN_XY = "LSM303AHTR_Acc_Align_XY";
		public static final String WR_ACC_ALIGN_XZ = "LSM303AHTR_Acc_Align_XZ";
		public static final String WR_ACC_ALIGN_YX = "LSM303AHTR_Acc_Align_YX";
		public static final String WR_ACC_ALIGN_YY = "LSM303AHTR_Acc_Align_YY";
		public static final String WR_ACC_ALIGN_YZ = "LSM303AHTR_Acc_Align_YZ";
		public static final String WR_ACC_ALIGN_ZX = "LSM303AHTR_Acc_Align_ZX";
		public static final String WR_ACC_ALIGN_ZY = "LSM303AHTR_Acc_Align_ZY";
		public static final String WR_ACC_ALIGN_ZZ = "LSM303AHTR_Acc_Align_ZZ";
		
		public static final String MAG_CALIB_TIME = "LSM303AHTR_Mag_Calib_Time";
		public static final String MAG_OFFSET_X = "LSM303AHTR_Mag_Offset_X";
		public static final String MAG_OFFSET_Y = "LSM303AHTR_Mag_Offset_Y";
		public static final String MAG_OFFSET_Z = "LSM303AHTR_Mag_Offset_Z";
		public static final String MAG_GAIN_X = "LSM303AHTR_Mag_Gain_X";
		public static final String MAG_GAIN_Y = "LSM303AHTR_Mag_Gain_Y";
		public static final String MAG_GAIN_Z = "LSM303AHTR_Mag_Gain_Z";
		public static final String MAG_ALIGN_XX = "LSM303AHTR_Mag_Align_XX";
		public static final String MAG_ALIGN_XY = "LSM303AHTR_Mag_Align_XY";
		public static final String MAG_ALIGN_XZ = "LSM303AHTR_Mag_Align_XZ";
		public static final String MAG_ALIGN_YX = "LSM303AHTR_Mag_Align_YX";
		public static final String MAG_ALIGN_YY = "LSM303AHTR_Mag_Align_YY";
		public static final String MAG_ALIGN_YZ = "LSM303AHTR_Mag_Align_YZ";
		public static final String MAG_ALIGN_ZX = "LSM303AHTR_Mag_Align_ZX";
		public static final String MAG_ALIGN_ZY = "LSM303AHTR_Mag_Align_ZY";
		public static final String MAG_ALIGN_ZZ = "LSM303AHTR_Mag_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_OFFSET_X, DatabaseConfigHandle.MAG_OFFSET_Y, DatabaseConfigHandle.MAG_OFFSET_Z,
				DatabaseConfigHandle.MAG_GAIN_X, DatabaseConfigHandle.MAG_GAIN_Y, DatabaseConfigHandle.MAG_GAIN_Z,
				DatabaseConfigHandle.MAG_ALIGN_XX, DatabaseConfigHandle.MAG_ALIGN_XY, DatabaseConfigHandle.MAG_ALIGN_XZ,
				DatabaseConfigHandle.MAG_ALIGN_YX, DatabaseConfigHandle.MAG_ALIGN_YY, DatabaseConfigHandle.MAG_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALIGN_ZX, DatabaseConfigHandle.MAG_ALIGN_ZY, DatabaseConfigHandle.MAG_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_WR_ACCEL = Arrays.asList(
				DatabaseConfigHandle.WR_ACC_OFFSET_X, DatabaseConfigHandle.WR_ACC_OFFSET_Y, DatabaseConfigHandle.WR_ACC_OFFSET_Z,
				DatabaseConfigHandle.WR_ACC_GAIN_X, DatabaseConfigHandle.WR_ACC_GAIN_Y, DatabaseConfigHandle.WR_ACC_GAIN_Z,
				DatabaseConfigHandle.WR_ACC_ALIGN_XX, DatabaseConfigHandle.WR_ACC_ALIGN_XY, DatabaseConfigHandle.WR_ACC_ALIGN_XZ,
				DatabaseConfigHandle.WR_ACC_ALIGN_YX, DatabaseConfigHandle.WR_ACC_ALIGN_YY, DatabaseConfigHandle.WR_ACC_ALIGN_YZ,
				DatabaseConfigHandle.WR_ACC_ALIGN_ZX, DatabaseConfigHandle.WR_ACC_ALIGN_ZY, DatabaseConfigHandle.WR_ACC_ALIGN_ZZ);
	}

	//--------- Sensor specific variables end --------------	
	
	
	
	//--------- Configuration options start --------------
	
	public static final Integer[] ListofLSM303AccelRangeConfigValues={0,2,3,1};  

	public static final String[] ListofLSM303AHAccelRateHr={"Power-down","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz","1600.0Hz","3200.0Hz","6400.0Hz"};
	public static final Integer[] ListofLSM303AHAccelRateHrConfigValues={0,1,2,3,4,5,6,7,8,9,10};

	public static final String[] ListofLSM303AHAccelRateLpm={"Power-down","1.0Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz"};
	public static final Integer[] ListofLSM303AHAccelRateLpmConfigValues={0,8,9,10,11,12,13,14,15};

	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RANGE,
			SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RANGE,
			ListofLSM303AccelRange, 
			ListofLSM303AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);

	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RATE,
			SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RATE,
			ListofLSM303AHAccelRateHr, 
			ListofLSM303AHAccelRateHrConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH,
			Arrays.asList(
				new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303_ACCEL_RATE.IS_LPM, 
						ListofLSM303AHAccelRateLpm, 
						ListofLSM303AHAccelRateLpmConfigValues)));

	public static final String[] ListofLSM303AHMagRate={"10.0Hz","20.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofLSM303AHMagRateConfigValues={0,1,2,3};

	public static final String[] ListofLSM303AHMagRange={"+/- 49.152Ga"}; 
	public static final Integer[] ListofLSM303AHMagRangeConfigValues={0};  

	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_MAG_RANGE,
			SensorLSM303AH.DatabaseConfigHandle.MAG_RANGE,
			ListofLSM303AHMagRange, 
			ListofLSM303AHMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);
	
	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_MAG_RATE,
			SensorLSM303AH.DatabaseConfigHandle.MAG_RATE,
			ListofLSM303AHMagRate, 
			ListofLSM303AHMagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);

	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_LPM,
			SensorLSM303AH.DatabaseConfigHandle.WR_ACC_LPM,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);

	//--------- Configuration options end --------------

	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLSM303AHAccel = new SensorDetailsRef(
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH,
			Arrays.asList(GuiLabelConfig.LSM303_ACCEL_RANGE,
					GuiLabelConfig.LSM303_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,
					ObjectClusterSensorName.ACCEL_WR_Y,
					ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final SensorDetailsRef sensorLSM303AHMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH,
			Arrays.asList(GuiLabelConfig.LSM303_MAG_RANGE,
					GuiLabelConfig.LSM303_MAG_RATE),
			//MAG channel order is XYZ for the LSM303AH
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Y,
					ObjectClusterSensorName.MAG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, SensorLSM303AH.sensorLSM303AHAccel);  
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, SensorLSM303AH.sensorLSM303AHMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
    public static final ChannelDetails channelLSM303AHAccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_X,
			ObjectClusterSensorName.ACCEL_WR_X,
			DatabaseChannelHandles.WR_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x04);
    
    public static final ChannelDetails channelLSM303AHAccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Y,
			ObjectClusterSensorName.ACCEL_WR_Y,
			DatabaseChannelHandles.WR_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x05);
    
    public static final ChannelDetails channelLSM30AH3AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Z,
			ObjectClusterSensorName.ACCEL_WR_Z,
			DatabaseChannelHandles.WR_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x06);
    
    public static final ChannelDetails channelLSM303AHMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLSM303AHMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLSM303AHMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_Z,
			ObjectClusterSensorName.MAG_Z,
			DatabaseChannelHandles.MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x09);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLSM303AH.ObjectClusterSensorName.ACCEL_WR_X, SensorLSM303AH.channelLSM303AHAccelX);
        aMap.put(SensorLSM303AH.ObjectClusterSensorName.ACCEL_WR_Y, SensorLSM303AH.channelLSM303AHAccelY);
        aMap.put(SensorLSM303AH.ObjectClusterSensorName.ACCEL_WR_Z, SensorLSM303AH.channelLSM30AH3AccelZ);
        aMap.put(SensorLSM303AH.ObjectClusterSensorName.MAG_X, SensorLSM303AH.channelLSM303AHMagX);
        aMap.put(SensorLSM303AH.ObjectClusterSensorName.MAG_Z, SensorLSM303AH.channelLSM303AHMagZ);
        aMap.put(SensorLSM303AH.ObjectClusterSensorName.MAG_Y, SensorLSM303AH.channelLSM303AHMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLsmAccel = new SensorGroupingDetails(
			GuiLabelSensorTiles.WIDE_RANGE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);
    
    public static final SensorGroupingDetails sensorGroupLsmMag = new SensorGroupingDetails(
			GuiLabelSensorTiles.MAG,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);

    
    //--------- Constructors for this class start --------------
	public SensorLSM303AH() {
		super();
		initialise();
	}

//	public SensorLSM303AH(ShimmerVerObject svo) {
//		super(svo);
//	}

	public SensorLSM303AH(ShimmerDevice shimmerDevice) {
		super(shimmerDevice);
		initialise();
	}
    //--------- Constructors for this class end --------------
	
	//--------- Abstract methods implemented start --------------

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		addConfigOption(configOptionAccelRange);
		addConfigOption(configOptionMagRange);
		addConfigOption(configOptionAccelRate);
		addConfigOption(configOptionMagRate);
		addConfigOption(configOptionAccelLpm);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL.ordinal(), sensorGroupLsmAccel);
		mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MAG.ordinal(), sensorGroupLsmMag);
		super.updateSensorGroupingMap();	
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
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RATE, getLSM303DigitalAccelRate());
		mapOfConfig.put(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RANGE, getAccelRange());
		mapOfConfig.put(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_LPM, getLowPowerAccelEnabled());
		mapOfConfig.put(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_HRM, getHighResAccelWREnabled());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelWr(), 
				SensorLSM303AH.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL,
				SensorLSM303AH.DatabaseConfigHandle.WR_ACC_CALIB_TIME);

		mapOfConfig.put(SensorLSM303AH.DatabaseConfigHandle.MAG_RANGE, getMagRange());
		mapOfConfig.put(SensorLSM303AH.DatabaseConfigHandle.MAG_RATE, getLSM303MagRate());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMag(), 
				SensorLSM303AH.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLSM303AH.DatabaseConfigHandle.MAG_CALIB_TIME);

		return mapOfConfig;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {

		if(mapOfConfigPerShimmer.containsKey(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RATE)){
			setLSM303DigitalAccelRate(((Double) mapOfConfigPerShimmer.get(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RANGE)){
			setLSM303AccelRange(((Double) mapOfConfigPerShimmer.get(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_LPM)){
			setLowPowerAccelWR(((Double) mapOfConfigPerShimmer.get(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_LPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_HRM)){
			setHighResAccelWR(((Double) mapOfConfigPerShimmer.get(SensorLSM303AH.DatabaseConfigHandle.WR_ACC_HRM))>0? true:false);
		}
		
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303AH.DatabaseConfigHandle.MAG_RANGE)){
			setLSM303MagRange(((Double) mapOfConfigPerShimmer.get(SensorLSM303AH.DatabaseConfigHandle.MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303AH.DatabaseConfigHandle.MAG_RATE)){
			setLSM303MagRate(((Double) mapOfConfigPerShimmer.get(SensorLSM303AH.DatabaseConfigHandle.MAG_RATE)).intValue());
		}
		
		//Digital Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, 
				getAccelRange(), 
				SensorLSM303AH.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL,
				SensorLSM303AH.DatabaseConfigHandle.WR_ACC_CALIB_TIME);
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 
				getMagRange(), 
				SensorLSM303AH.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLSM303AH.DatabaseConfigHandle.MAG_CALIB_TIME);
	}

	//--------- Abstract methods implemented end --------------

	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorMapKeyAccel = Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL;
		mSensorMapKeyMag = Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG;
		super.initialise();
		
		mMagRange = ListofLSM303AHMagRangeConfigValues[0];
		
		updateCurrentAccelWrCalibInUse();
		updateCurrentMagCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelWr = new TreeMap<Integer, CalibDetails>();
		calibMapAccelWr.put(calibDetailsAccelWr2g.mRangeValue, calibDetailsAccelWr2g);
		calibMapAccelWr.put(calibDetailsAccelWr4g.mRangeValue, calibDetailsAccelWr4g);
		calibMapAccelWr.put(calibDetailsAccelWr8g.mRangeValue, calibDetailsAccelWr8g);
		calibMapAccelWr.put(calibDetailsAccelWr16g.mRangeValue, calibDetailsAccelWr16g);
		setCalibrationMapPerSensor(mSensorMapKeyAccel, calibMapAccelWr);

		updateCurrentAccelWrCalibInUse();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag50Ga.mRangeValue, calibDetailsMag50Ga);
		setCalibrationMapPerSensor(mSensorMapKeyMag, calibMapMag);
		
		updateCurrentMagCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------

	
	//--------- Sensor specific methods start --------------

	@Override
	public void setLSM303AccelRange(int valueToSet){
		if(ArrayUtils.contains(ListofLSM303AccelRangeConfigValues, valueToSet)){
			mAccelRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
	}

	@Override
	public void setLSM303DigitalAccelRate(int valueToSet) {
		mLSM303DigitalAccelRate = valueToSet;
		if(mLowPowerAccelWR){
			//LPM is not compatible with mLSM303DigitalAccelRate == 1 to 7, set to next higher rate
			for(Integer i:SensorLSM303AH.ListofLSM303AHAccelRateLpmConfigValues){
				if(i==valueToSet){
					return;
				}
			}
			mLSM303DigitalAccelRate = SensorLSM303AH.ListofLSM303AHAccelRateLpmConfigValues[SensorLSM303AH.ListofLSM303AHAccelRateLpmConfigValues.length-1];
		} else {
			//HR is not compatible with mLSM303DigitalAccelRate > 10, set to higher rate
			for(Integer i:SensorLSM303AH.ListofLSM303AHAccelRateHrConfigValues){
				if(i==valueToSet){
					return;
				}
			}
			mLSM303DigitalAccelRate = SensorLSM303AH.ListofLSM303AHAccelRateHrConfigValues[SensorLSM303AH.ListofLSM303AHAccelRateHrConfigValues.length-1];
		}
	}


	@Override
	public boolean checkLowPowerMag() {
		setLowPowerMag((getLSM303MagRate() == 0)? true:false); // ==10Hz
		return isLowPowerMagEnabled();
	}

	@Override
	public void setLSM303MagRange(int valueToSet) {
		//Not needed for LSM303AH as it only has one range
	}

	@Override
	public int getAccelRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode) {
		return SensorLSM303AH.getAccelRateFromFreq(isEnabled, freq, isLowPowerMode);
	}

	public static int getAccelRateFromFreq(boolean isEnabled, double freq, boolean isLowPowerMode) {
		int accelRate = 0; // Power down
		
		if(isEnabled){
			if(isLowPowerMode){
				if (freq<1.0){
					accelRate = 8; // 1Hz
				} else if (freq<12.5){
					accelRate = 9; // 12.5Hz
				} else if (freq<25){
					accelRate = 10; // 25Hz
				} else if (freq<50){
					accelRate = 11; // 50Hz
				} else if (freq<100){
					accelRate = 12; // 100Hz
				} else if (freq<200){
					accelRate = 13; // 200Hz
				} else if (freq<400){
					accelRate = 14; // 400Hz
				} else {
					accelRate = 15; // 800Hz
				}
			} else {
				if (freq<12.5){
					accelRate = 1; // 12.5Hz
				} else if (freq<25){
					accelRate = 2; // 25Hz
				} else if (freq<50){
					accelRate = 3; // 50Hz
				} else if (freq<100){
					accelRate = 4; // 100Hz
				} else if (freq<200){
					accelRate = 5; // 200Hz
				} else if (freq<400){
					accelRate = 6; // 400Hz
				} else if (freq<400){
					accelRate = 7; // 800Hz
				} else if (freq<1600){
					accelRate = 8; // 1600Hz
				} else if (freq<3200){
					accelRate = 9; // 3200Hz
				} else {
					accelRate = 10; // 6400Hz
				}
			}
		}
		return accelRate;
	}
	
	@Override
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode) {
		int magRate = 0; // 10Hz

		if(isEnabled){
			if (freq<10.0){
				magRate = 0; // 10Hz
			} else if (freq<20.0 || isLowPowerMode){
				magRate = 1; // 20Hz
			} else if (freq<50.0) {
				magRate = 2; // 50Hz
			} else {
				magRate = 3; // 100Hz
			}
		}
		return magRate;
	}

	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		//TODO Old approach, can be removed
//		String objectClusterName = "";
//		if (databaseChannelHandle.equals(SensorLSM303AH.DatabaseChannelHandles.WR_ACC_X)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X;
//		} else if (databaseChannelHandle.equals(SensorLSM303AH.DatabaseChannelHandles.WR_ACC_Y)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y;
//		} else if (databaseChannelHandle.equals(SensorLSM303AH.DatabaseChannelHandles.WR_ACC_Z)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z;
//		} else if (databaseChannelHandle.equals(SensorLSM303AH.DatabaseChannelHandles.MAG_X)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.MAG_X;
//		} else if (databaseChannelHandle.equals(SensorLSM303AH.DatabaseChannelHandles.MAG_Y)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.MAG_Y;
//		} else if (databaseChannelHandle.equals(SensorLSM303AH.DatabaseChannelHandles.MAG_Z)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.MAG_Z;
//		}
//		return objectClusterName;
		
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		//TODO Old approach, can be removed
//		String databaseChannelHandle = "";
//		if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X)) {
//			databaseChannelHandle = SensorLSM303AH.DatabaseChannelHandles.WR_ACC_X;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y)) {
//			databaseChannelHandle = SensorLSM303AH.DatabaseChannelHandles.WR_ACC_Y;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z)) {
//			databaseChannelHandle = SensorLSM303AH.DatabaseChannelHandles.WR_ACC_Z;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.MAG_X)) {
//			databaseChannelHandle = SensorLSM303AH.DatabaseChannelHandles.MAG_X;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.MAG_Y)) {
//			databaseChannelHandle = SensorLSM303AH.DatabaseChannelHandles.MAG_Y;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.MAG_Z)) {
//			databaseChannelHandle = SensorLSM303AH.DatabaseChannelHandles.MAG_Z;
//		}
//		return databaseChannelHandle;
		
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}

	//--------- Sensor specific methods end --------------

	
}
