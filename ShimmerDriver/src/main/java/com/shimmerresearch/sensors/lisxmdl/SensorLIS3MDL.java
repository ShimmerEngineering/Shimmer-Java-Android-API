package com.shimmerresearch.sensors.lisxmdl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLIS3MDL extends AbstractSensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4028368641088628178L;

	protected int mWRMagRange = 0;
	public boolean mIsUsingDefaultWRMagParam = true;
	protected int mLISWRMagRate = 4;
	protected int mSensorIdWRMag = -1;
	
	
	//--------- Sensor specific variables start --------------	
	
	public static final double[][] DefaultAlignmentLIS3MDL = {{-1,0,0},{0,-1,0},{0,0,-1}};	
	
// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixWRMagShimmer3r = DefaultAlignmentLIS3MDL; 				
	public static final double[][] DefaultOffsetVectorWRMagShimmer3r = {{0},{0},{0}};	

	public static final double[][] DefaultSensitivityMatrixWRMagShimmer3r = {{667,0,0},{0,667,0},{0,0,667}};

	private CalibDetailsKinematic calibDetailsMagWr = new CalibDetailsKinematic(
			ListofLIS3MDLWRMagRangeConfigValues[0],
			ListofLIS3MDLWRMagRange[0],
			DefaultAlignmentMatrixWRMagShimmer3r,
			DefaultSensitivityMatrixWRMagShimmer3r,
			DefaultOffsetVectorWRMagShimmer3r);
	
	public CalibDetailsKinematic mCurrentCalibDetailsMagWr = calibDetailsMagWr;

	// ----------   Mag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String WR_MAG_X = "LIS3MDL_MAG_X";
		public static final String WR_MAG_Y = "LIS3MDL_MAG_Y";
		public static final String WR_MAG_Z = "LIS3MDL_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String WR_MAG_RATE = "LIS3MDL_WR_Mag_Rate";
		public static final String WR_MAG_RANGE = "LIS3MDL_WR_Mag_Range";
		
		public static final String MAG_WR_CALIB_TIME = "LIS3MDL_Mag_WR_Calib_Time";
		public static final String MAG_WR_OFFSET_X = "LIS3MDL_Mag_WR_Offset_X";
		public static final String MAG_WR_OFFSET_Y = "LIS3MDL_Mag_WR_Offset_Y";
		public static final String MAG_WR_OFFSET_Z = "LIS3MDL_Mag_WR_Offset_Z";
		public static final String MAG_WR_GAIN_X = "LIS3MDL_Mag_WR_Gain_X";
		public static final String MAG_WR_GAIN_Y = "LIS3MDL_Mag_WR_Gain_Y";
		public static final String MAG_WR_GAIN_Z = "LIS3MDL_Mag_WR_Gain_Z";
		public static final String MAG_WR_ALIGN_XX = "LIS3MDL_Mag_WR_Align_XX";
		public static final String MAG_WR_ALIGN_XY = "LIS3MDL_Mag_WR_Align_XY";
		public static final String MAG_WR_ALIGN_XZ = "LIS3MDL_Mag_WR_Align_XZ";
		public static final String MAG_WR_ALIGN_YX = "LIS3MDL_Mag_WR_Align_YX";
		public static final String MAG_WR_ALIGN_YY = "LIS3MDL_Mag_WR_Align_YY";
		public static final String MAG_WR_ALIGN_YZ = "LIS3MDL_Mag_WR_Align_YZ";
		public static final String MAG_WR_ALIGN_ZX = "LIS3MDL_Mag_WR_Align_ZX";
		public static final String MAG_WR_ALIGN_ZY = "LIS3MDL_Mag_WR_Align_ZY";
		public static final String MAG_WR_ALIGN_ZZ = "LIS3MDL_Mag_WR_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_WR_OFFSET_X, DatabaseConfigHandle.MAG_WR_OFFSET_Y, DatabaseConfigHandle.MAG_WR_OFFSET_Z,
				DatabaseConfigHandle.MAG_WR_GAIN_X, DatabaseConfigHandle.MAG_WR_GAIN_Y, DatabaseConfigHandle.MAG_WR_GAIN_Z,
				DatabaseConfigHandle.MAG_WR_ALIGN_XX, DatabaseConfigHandle.MAG_WR_ALIGN_XY, DatabaseConfigHandle.MAG_WR_ALIGN_XZ,
				DatabaseConfigHandle.MAG_WR_ALIGN_YX, DatabaseConfigHandle.MAG_WR_ALIGN_YY, DatabaseConfigHandle.MAG_WR_ALIGN_YZ,
				DatabaseConfigHandle.MAG_WR_ALIGN_ZX, DatabaseConfigHandle.MAG_WR_ALIGN_ZY, DatabaseConfigHandle.MAG_WR_ALIGN_ZZ);
		
	}
	
	public class GuiLabelConfig{
		
		public static final String LIS3MDL_WR_MAG_RANGE = "Wide Range Mag Range";
		public static final String LIS3MDL_WR_MAG_RATE = "Wide Range Mag Rate";

		public static final String LIS3MDL_WR_MAG_DEFAULT_CALIB = "Wide Range Mag Default Calibration";

		//NEW
		public static final String LIS3MDL_WR_MAG_CALIB_PARAM = "Wide Range Mag Calibration Details";
		public static final String LIS3MDL_WR_MAG_VALID_CALIB = "Wide Range Mag Valid Calibration";
	}
	
	public static class ObjectClusterSensorName{
		
		public static  String MAG_WR_X = "Mag_WR_X";
		public static  String MAG_WR_Y = "Mag_WR_Y";
		public static  String MAG_WR_Z = "Mag_WR_Z";				
	}
	
	public class GuiLabelSensors{
		public static final String MAG_WR = "Wide-Range Magnetometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String WIDE_RANGE_MAG = GuiLabelSensors.MAG_WR;
	}

	//--------- Sensor specific variables end --------------	
	
	//--------- Configuration options start --------------
	
	public static final String[] ListofLIS3MDLWRMagRate={"10.0Hz","20.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofLIS3MDLWRMagRateConfigValues={0,1,2,3};
	
	public static final String[] ListofLIS3MDLWRMagRange={"+/- 49.152Ga"}; 
	public static final Integer[] ListofLIS3MDLWRMagRangeConfigValues={0};  
	
	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LIS3MDL_WR_MAG_RANGE,
			SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE,
			ListofLIS3MDLWRMagRange, 
			ListofLIS3MDLWRMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);

	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LIS3MDL_WR_MAG_RATE,
			SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE,
			ListofLIS3MDLWRMagRate, 
			ListofLIS3MDLWRMagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);

	//--------- Configuration options end --------------
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorLIS3MDLMag = new SensorDetailsRef(
			0x200000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x200000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL,
			Arrays.asList(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE,
					GuiLabelConfig.LIS3MDL_WR_MAG_RATE),
			Arrays.asList(ObjectClusterSensorName.MAG_WR_X,
					ObjectClusterSensorName.MAG_WR_Y,
					ObjectClusterSensorName.MAG_WR_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>(); 
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, SensorLIS3MDL.sensorLIS3MDLMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    //--------- Channel info start --------------
    
    public static final ChannelDetails channelLIS3MDLMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_X,
			ObjectClusterSensorName.MAG_WR_X,
			DatabaseChannelHandles.WR_MAG_X,
			CHANNEL_DATA_TYPE.INT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x17);
	
	public static final ChannelDetails channelLIS3MDLMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_Y,
			ObjectClusterSensorName.MAG_WR_Y,
			DatabaseChannelHandles.WR_MAG_Y,
			CHANNEL_DATA_TYPE.INT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x18);
	
	public static final ChannelDetails channelLIS3MDLMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_Z,
			ObjectClusterSensorName.MAG_WR_Z,
			DatabaseChannelHandles.WR_MAG_Z,
			CHANNEL_DATA_TYPE.INT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x19);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_WR_X, SensorLIS3MDL.channelLIS3MDLMagX);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_WR_Z, SensorLIS3MDL.channelLIS3MDLMagZ);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_WR_Y, SensorLIS3MDL.channelLIS3MDLMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

    public static final SensorGroupingDetails sensorGroupLisMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.WIDE_RANGE_MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);
    
	//--------- Bluetooth commands start --------------
	//still not being implemented for wr mag sensor due to unavailability in docs
	public static final byte SET_ALT_MAG_CALIBRATION_COMMAND      		= (byte) 0xAF; 
	public static final byte ALT_MAG_CALIBRATION_RESPONSE         		= (byte) 0xB0; 
	public static final byte GET_ALT_MAG_CALIBRATION_COMMAND      		= (byte) 0xB1; 

	public static final byte SET_ALT_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0xB2; 
	public static final byte ALT_MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0xB3; 
	public static final byte GET_ALT_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0xB4; 
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_ALT_MAG_CALIBRATION_COMMAND, "GET_ALT_MAG_CALIBRATION_COMMAND", ALT_MAG_CALIBRATION_RESPONSE));
        aMap.put(GET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ALT_MAG_SAMPLING_RATE_COMMAND, "GET_ALT_MAG_SAMPLING_RATE_COMMAND", ALT_MAG_SAMPLING_RATE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(SET_ALT_MAG_CALIBRATION_COMMAND, "SET_ALT_MAG_CALIBRATION_COMMAND"));
        aMap.put(SET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ALT_MAG_SAMPLING_RATE_COMMAND, "SET_ALT_MAG_SAMPLING_RATE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
    
    //--------- Constructors for this class start --------------
	public SensorLIS3MDL() {
		super(SENSORS.LIS3MDL);
		initialise();
	}
	
	public SensorLIS3MDL(ShimmerObject obj) {
		super(SENSORS.LIS3MDL, obj);
		initialise();
	}
	
	public SensorLIS3MDL(ShimmerDevice shimmerDevice) {
		super(SENSORS.LIS3MDL, shimmerDevice);
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
		addConfigOption(configOptionMagRange);
		addConfigOption(configOptionMagRate);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.WIDE_RANGE_MAG_3R.ordinal(), sensorGroupLisMag);
		super.updateSensorGroupingMap();	
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
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();

		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE, getWRMagRange());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE, getLIS3MDLWRMagRate());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMagWr(), 
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_WR_CALIB_TIME);

		return mapOfConfig;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMagWr(){
		if(mCurrentCalibDetailsMagWr==null){
			updateCurrentMagWrCalibInUse();
		}
		return mCurrentCalibDetailsMagWr;
	}
	
	public void updateCurrentMagWrCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMagWr = getCurrentCalibDetailsIfKinematic(mSensorIdWRMag, getWRMagRange());
	}
	
	public int getWRMagRange() {
		return mWRMagRange;
	}
	
	public int getLIS3MDLWRMagRate() {
		return mLISWRMagRate;
	}
	
	public void setLIS3MDLWRMagRate(int valueToSet){
		mLISWRMagRate = valueToSet;
	}
	
	public void setWRMagRange(int valueToSet) {
		setLIS3MDLWRMagRange(valueToSet);
	}
	
	public void setLIS3MDLWRMagRange(int i) {
		if(ArrayUtils.contains(ListofLIS3MDLWRMagRateConfigValues, i)){
			mWRMagRange = i;
			updateCurrentMagWrCalibInUse();
		}

	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {

		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE)){
			setWRMagRange(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE)){
			setLIS3MDLWRMagRate(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE)).intValue());
		}
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, 
				getWRMagRange(), 
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_WR_CALIB_TIME);
	}
	
	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	//--------- Abstract methods implemented end --------------

	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdWRMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT;
		super.initialise();
		
		mWRMagRange = ListofLIS3MDLWRMagRangeConfigValues[0];
		
		updateCurrentMagWrCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMagWr = new TreeMap<Integer, CalibDetails>();
		calibMapMagWr.put(calibDetailsMagWr.mRangeValue, calibDetailsMagWr);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, calibMapMagWr);
		
		updateCurrentMagWrCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------
	
	//--------- Sensor specific methods start --------------
	
	public double getCalibTimeMagWR() {
		return mCurrentCalibDetailsMagWr.getCalibTimeMs();
	}
	
	public boolean isUsingValidMagWRParam() {
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixMagWr()) && !UtilShimmer.isAllZeros(getSensitivityMatrixMagWr())){
			return true;
		}else{
			return false;
		}
	}
	
	public void updateIsUsingDefaultWRMagParam() {
		mIsUsingDefaultWRMagParam = getCurrentCalibDetailsMagWr().isUsingDefaultParameters();
	}
	
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq) {
		int magRate = 0; // 10Hz

		if (freq<10.0){
			magRate = 0; // 10Hz
		} else if (freq<20.0){
			magRate = 1; // 20Hz
		} else if (freq<50.0) {
			magRate = 2; // 50Hz
		} else {
			magRate = 3; // 100Hz
		}
		return magRate;
	}

	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {

		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_WR) && mCurrentCalibDetailsMagWr!=null){
				double[] unCalibratedMagWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Mag WR data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_X)){
						unCalibratedMagWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Y)){
						unCalibratedMagWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Z)){
						unCalibratedMagWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedMagWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagWrData, mCurrentCalibDetailsMagWr);
//				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultMagWRParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultMagWRParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultMagWRParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.MAG_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.MAG_WR_Z, CHANNEL_TYPE.CAL.toString()}));
				}
	
			}
		}
		return objectCluster;
	}
	
	public boolean isUsingDefaultMagWRParam(){
		return getCurrentCalibDetailsMagWr().isUsingDefaultParameters(); 
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdWRMag)) {
			setDefaultLisMagWrSensorConfig(false);
		}
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) ((getLIS3MDLWRMagRate() & configByteLayoutCast.maskLIS3MDLAltMagSamplingRate) << configByteLayoutCast.bitShiftLIS3MDLAltMagSamplingRate);

			byte[] bufferCalibrationParameters = generateCalParamLIS3MDLMag();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLIS3MDLAltMagCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
		}
	}

	public byte[] generateCalParamLIS3MDLMag(){
		return mCurrentCalibDetailsMagWr.generateCalParamByteArray();
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		//still not being implemented for wr mag sensor
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			setLIS3MDLWRMagRate((configBytes[configByteLayoutCast.idxConfigSetupByte4] >> configByteLayoutCast.bitShiftLIS3MDLAltMagSamplingRate) & configByteLayoutCast.maskLIS3MDLAltMagSamplingRate);
			
			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsMagWr().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLIS3MDLAltMagCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketMagWr(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}
	
	public void parseCalibParamFromPacketMagWr(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsMagWr.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
		case(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE):
			setLIS3MDLWRMagRange((int)valueToSet);
			break;
		case(GuiLabelConfig.LIS3MDL_WR_MAG_RATE):
			setLIS3MDLWRMagRate((int)valueToSet);
			break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdWRMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE, valueToSet);
					break;
				}
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdWRMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RATE, valueToSet);
					break;
				}
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorLIS3MDL.GuiLabelConfig.LIS3MDL_WR_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LIS3MDL_WR_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE): 
				returnValue = getWRMagRange();
	    		break;
			case(GuiLabelConfig.LIS3MDL_WR_MAG_RATE): 
				int configValue = getLIS3MDLWRMagRate(); 
				returnValue = configValue;
				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdWRMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE);
//					returnValue = 0;
					break;
				}
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdWRMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RATE);
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
		setLISWRMagRateFromFreq(samplingRateHz);
	}
	
	public int setLISWRMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdWRMag);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelWR:" + mLowPowerAccelWR);
		setLISWRMagRateInternal(getMagRateFromFreqForSensor(isEnabled, freq));
		return mLISWRMagRate;
	}
	
	public void setLISWRMagRateInternal(int valueToSet) {
		//System.out.println("Accel Rate:\t" + valueToSet);
		//UtilShimmer.consolePrintCurrentStackTrace();
		mLISWRMagRate = valueToSet;
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdWRMag) {
				setDefaultLisMagWrSensorConfig(isSensorEnabled);		
			}
			return true;
		}
		return false;
	}
	
	public void setDefaultLisMagWrSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLIS3MDLWRMagRange(0);
		}
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public double[][] getAlignmentMatrixMagWr(){
		return mCurrentCalibDetailsMagWr.getValidAlignmentMatrix();
	}
	
	public double[][] getSensitivityMatrixMagWr(){
		return mCurrentCalibDetailsMagWr.getValidSensitivityMatrix();
	}
	
	public double[][] getOffsetVectorMatrixMagWr(){
		return mCurrentCalibDetailsMagWr.getValidOffsetVector();
	}

	//--------- Sensor specific methods end --------------

}
