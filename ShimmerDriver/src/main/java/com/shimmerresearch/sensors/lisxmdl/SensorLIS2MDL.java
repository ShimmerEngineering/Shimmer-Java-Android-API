package com.shimmerresearch.sensors.lisxmdl;

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
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.ConfigOptionObject;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLIS2MDL extends AbstractSensor{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2366590050010873738L;
	
	protected int mMagRange = 0;
	protected int mLISMagRate = 0;
	public boolean mIsUsingDefaultMagParam = true;
	protected int mSensorIdMag = -1;
	protected boolean mLowPowerMag = false;
	
	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;

	
	//--------- Sensor specific variables start --------------	
	
	public static final double[][] DefaultAlignmentLIS2MDL = {{-1,0,0},{0,-1,0},{0,0,-1}};	
	
// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixMagShimmer3r = DefaultAlignmentLIS2MDL; 				
	public static final double[][] DefaultOffsetVectorMagShimmer3r = {{0},{0},{0}};	

	public static final double[][] DefaultSensitivityMatrixMagShimmer3r = {{667,0,0},{0,667,0},{0,0,667}};

	private CalibDetailsKinematic calibDetailsMag = new CalibDetailsKinematic(
			ListofLIS2MDLMagRangeConfigValues[0],
			ListofLIS2MDLMagRange[0],
			DefaultAlignmentMatrixMagShimmer3r,
			DefaultSensitivityMatrixMagShimmer3r,
			DefaultOffsetVectorMagShimmer3r);

	// ----------   Mag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String MAG_X = "LIS2MDL_MAG_X";
		public static final String MAG_Y = "LIS2MDL_MAG_Y";
		public static final String MAG_Z = "LIS2MDL_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String MAG_RANGE = "LIS2MDL_Mag_Range";
		public static final String MAG_RATE = "LIS2MDL_Mag_Rate";
		
		public static final String MAG_LPM = "LIS2MDL_Mag_LPM";

//		public static final String MAG = "LIS2MDL_Mag";
		
		public static final String MAG_CALIB_TIME = "LIS2MDL_Mag_Calib_Time";
		public static final String MAG_OFFSET_X = "LIS2MDL_Mag_Offset_X";
		public static final String MAG_OFFSET_Y = "LIS2MDL_Mag_Offset_Y";
		public static final String MAG_OFFSET_Z = "LIS2MDL_Mag_Offset_Z";
		public static final String MAG_GAIN_X = "LIS2MDL_Mag_Gain_X";
		public static final String MAG_GAIN_Y = "LIS2MDL_Mag_Gain_Y";
		public static final String MAG_GAIN_Z = "LIS2MDL_Mag_Gain_Z";
		public static final String MAG_ALIGN_XX = "LIS2MDL_Mag_Align_XX";
		public static final String MAG_ALIGN_XY = "LIS2MDL_Mag_Align_XY";
		public static final String MAG_ALIGN_XZ = "LIS2MDL_Mag_Align_XZ";
		public static final String MAG_ALIGN_YX = "LIS2MDL_Mag_Align_YX";
		public static final String MAG_ALIGN_YY = "LIS2MDL_Mag_Align_YY";
		public static final String MAG_ALIGN_YZ = "LIS2MDL_Mag_Align_YZ";
		public static final String MAG_ALIGN_ZX = "LIS2MDL_Mag_Align_ZX";
		public static final String MAG_ALIGN_ZY = "LIS2MDL_Mag_Align_ZY";
		public static final String MAG_ALIGN_ZZ = "LIS2MDL_Mag_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_OFFSET_X, DatabaseConfigHandle.MAG_OFFSET_Y, DatabaseConfigHandle.MAG_OFFSET_Z,
				DatabaseConfigHandle.MAG_GAIN_X, DatabaseConfigHandle.MAG_GAIN_Y, DatabaseConfigHandle.MAG_GAIN_Z,
				DatabaseConfigHandle.MAG_ALIGN_XX, DatabaseConfigHandle.MAG_ALIGN_XY, DatabaseConfigHandle.MAG_ALIGN_XZ,
				DatabaseConfigHandle.MAG_ALIGN_YX, DatabaseConfigHandle.MAG_ALIGN_YY, DatabaseConfigHandle.MAG_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALIGN_ZX, DatabaseConfigHandle.MAG_ALIGN_ZY, DatabaseConfigHandle.MAG_ALIGN_ZZ);
	}
	
	public class GuiLabelConfig{
	
		public static final String LIS2MDL_MAG_RANGE = "Mag Range";
		public static final String LIS2MDL_MAG_RATE = "Mag Rate";

		public static final String LIS2MDL_MAG_LP = "Mag Low-Power Mode";
		public static final String LIS2MDL_MAG_DEFAULT_CALIB = "Mag Default Calibration";

		//NEW
		public static final String LIS2MDL_MAG_CALIB_PARAM = "Mag Calibration Details";
		public static final String LIS2MDL_MAG_VALID_CALIB = "Mag Valid Calibration";
		
	}
	
	public static class ObjectClusterSensorName{
		
		public static  String MAG_X = "Mag_X";
		public static  String MAG_Y = "Mag_Y";
		public static  String MAG_Z = "Mag_Z";		
	}
	
	public class GuiLabelSensors{
		public static final String MAG = "Magnetometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String MAG = GuiLabelSensors.MAG;
	}
	
	//--------- Sensor specific variables end --------------	
	
	//--------- Configuration options start --------------
	
	public static final String[] ListofLIS2MDLMagRate={"10.0Hz","20.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofLIS2MDLMagRateConfigValues={0,1,2,3};

	public static final String[] ListofLIS2MDLMagRange ={"+/- 49.152Ga"}; 
	public static final Integer[] ListofLIS2MDLMagRangeConfigValues={0};

	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLIS2MDL.GuiLabelConfig.LIS2MDL_MAG_RANGE,
			SensorLIS2MDL.DatabaseConfigHandle.MAG_RANGE,
			ListofLIS2MDLMagRange, 
			ListofLIS2MDLMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL);
	
	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLIS2MDL.GuiLabelConfig.LIS2MDL_MAG_RATE,
			SensorLIS2MDL.DatabaseConfigHandle.MAG_RATE,
			ListofLIS2MDLMagRate, 
			ListofLIS2MDLMagRateConfigValues,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL);

	//--------- Configuration options end --------------
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorLIS2MDLMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL,
			Arrays.asList(GuiLabelConfig.LIS2MDL_MAG_RANGE,
					GuiLabelConfig.LIS2MDL_MAG_RATE),
			//MAG channel parsing order is XZY instead of XYZ but it would be better to represent it on the GUI in XYZ
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Y,
					ObjectClusterSensorName.MAG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG, SensorLIS2MDL.sensorLIS2MDLMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	
	//--------- Sensor info end --------------
    
  //--------- Channel info start --------------
    
    public static final ChannelDetails channelLIS2MDLMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLIS2MDLMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLIS2MDLMagZ = new ChannelDetails(
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
        aMap.put(SensorLIS2MDL.ObjectClusterSensorName.MAG_X, SensorLIS2MDL.channelLIS2MDLMagX);
        aMap.put(SensorLIS2MDL.ObjectClusterSensorName.MAG_Z, SensorLIS2MDL.channelLIS2MDLMagZ);
        aMap.put(SensorLIS2MDL.ObjectClusterSensorName.MAG_Y, SensorLIS2MDL.channelLIS2MDLMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
    
    //--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLisMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL);
    
  //--------- Bluetooth commands start --------------
  	public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;
  	public static final byte MAG_CALIBRATION_RESPONSE         		= (byte) 0x18;
  	public static final byte GET_MAG_CALIBRATION_COMMAND      		= (byte) 0x19;
  	
  	public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;
  	public static final byte MAG_GAIN_RESPONSE                		= (byte) 0x38;
  	public static final byte GET_MAG_GAIN_COMMAND             		= (byte) 0x39;
  	
  	public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;
  	public static final byte MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0x3B;
  	public static final byte GET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3C;
  	
	  public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	  static {
	      Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	      aMap.put(GET_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_MAG_CALIBRATION_COMMAND, "GET_MAG_CALIBRATION_COMMAND", MAG_CALIBRATION_RESPONSE));
	      aMap.put(GET_MAG_GAIN_COMMAND, new BtCommandDetails(GET_MAG_GAIN_COMMAND, "GET_MAG_GAIN_COMMAND", MAG_GAIN_RESPONSE));
	      aMap.put(GET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_MAG_SAMPLING_RATE_COMMAND, "GET_MAG_SAMPLING_RATE_COMMAND", MAG_SAMPLING_RATE_RESPONSE));
	      mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	  }
	  
	  public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	  static {
	      Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	      aMap.put(SET_MAG_CALIBRATION_COMMAND, new BtCommandDetails(SET_MAG_CALIBRATION_COMMAND, "SET_MAG_CALIBRATION_COMMAND"));
	      aMap.put(SET_MAG_GAIN_COMMAND, new BtCommandDetails(SET_MAG_GAIN_COMMAND, "SET_MAG_GAIN_COMMAND"));
	      aMap.put(SET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_MAG_SAMPLING_RATE_COMMAND, "SET_MAG_SAMPLING_RATE_COMMAND"));
	      mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	  }
	  //--------- Bluetooth commands end --------------
    
  //--------- Constructors for this class start --------------
    
	public SensorLIS2MDL() {
		super(SENSORS.LIS2MDL);
		initialise();
	}
	
	public SensorLIS2MDL(ShimmerObject obj) {
		super(SENSORS.LIS2MDL, obj);
		initialise();
	}
	
	public SensorLIS2MDL(ShimmerDevice shimmerDevice) {
		super(SENSORS.LIS2MDL, shimmerDevice);
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
		mConfigOptionsMap.clear();
		addConfigOption(configOptionMagRange);
		addConfigOption(configOptionMagRate);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MAG_3R.ordinal(), sensorGroupLisMag);
		super.updateSensorGroupingMap();	
	}	
	
	@Override 
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		//XXX - RS: Also returning null in BMP180 and GSR sensors classes 
		return null;
	}

	@Override 
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// 		Object returnValue = null;
		ActionSetting actionsetting = new ActionSetting(commType);
		
		return actionsetting;
		
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorLIS2MDL.DatabaseConfigHandle.MAG_RATE, getLIS2MDLMagRate());
		mapOfConfig.put(SensorLIS2MDL.DatabaseConfigHandle.MAG_RANGE, getMagRange());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMag(), 
				SensorLIS2MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS2MDL.DatabaseConfigHandle.MAG_CALIB_TIME);

		return mapOfConfig;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
//		return getCurrentCalibDetails(mSensorIdMag, getMagRange());
		if(mCurrentCalibDetailsMag==null){
			updateCurrentMagCalibInUse();
		}
		return mCurrentCalibDetailsMag;
	}
	
	public void updateCurrentMagCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMag = getCurrentCalibDetailsIfKinematic(mSensorIdMag, getMagRange());
	}
	
	public int getMagRange() {
		return mMagRange;
	}
	
	public int getLIS2MDLMagRate() {
		return mLISMagRate;
	}
	
	public void setLISMagRange(int valueToSet){
		if(ArrayUtils.contains(ListofLIS2MDLMagRangeConfigValues, valueToSet)){
			mMagRange = valueToSet;
			updateCurrentMagCalibInUse();
		}
	}
	
	public void setLIS2MDLMagRate(int valueToSet){
		mLISMagRate = valueToSet;
	}
	
	public void setLISMagRateInternal(int samplingRateHz){
		mLISMagRate = samplingRateHz;
	}
	
	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		if (mapOfConfigPerShimmer.containsKey(SensorLIS2MDL.DatabaseConfigHandle.MAG_LPM)) {
			setLowPowerMag(
					((Double) mapOfConfigPerShimmer.get(SensorLIS2MDL.DatabaseConfigHandle.MAG_LPM)) > 0 ? true
							: false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2MDL.DatabaseConfigHandle.MAG_RANGE)){
			setLISMagRange(((Double) mapOfConfigPerShimmer.get(SensorLIS2MDL.DatabaseConfigHandle.MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2MDL.DatabaseConfigHandle.MAG_RATE)){
			setLIS2MDLMagRate(((Double) mapOfConfigPerShimmer.get(SensorLIS2MDL.DatabaseConfigHandle.MAG_RATE)).intValue());
		}
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG, 
				getMagRange(), 
				SensorLIS2MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS2MDL.DatabaseConfigHandle.MAG_CALIB_TIME);
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
		mSensorIdMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG;
		super.initialise();
		
		mMagRange = ListofLIS2MDLMagRangeConfigValues[0];
		
		updateCurrentMagCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag.mRangeValue, calibDetailsMag);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG, calibMapMag);
		
		updateCurrentMagCalibInUse();
	}
	
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG){
			return isUsingDefaultMagParam();
		}
		return false;
	}
	
	//--------- Optional methods to override in Sensor Class end --------
	
	//--------- Sensor specific methods start --------------

	public int setLIS2MDLAltMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdMag);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelAlt:" + mLowPowerAccelAlt);
		//setLISMagRateInternal(getMagRateFromFreqForSensor(isEnabled, freq));
		if (isLowPowerMagEnabled()) {
			mLISMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 0);
		} else {
			mLISMagRate = getMagRateFromFreqForSensor(isEnabled, freq, -1);
		}
		return mLISMagRate;
	}
	
	public boolean checkLowPowerMag() {
		setLowPowerMag((getLIS2MDLMagRate() == 0) ? true : false); // 10Hz
		return isLowPowerMagEnabled();
	}
	
	public boolean isLowPowerMagEnabled() {
		return mLowPowerMag;
	}
	
	public int getLowPowerMagEnabled() {
		return (isLowPowerMagEnabled() ? 1 : 0);
	}
	
	public void setLowPowerMag(boolean enable) {
		mLowPowerMag = enable;
		if (mShimmerDevice != null) {
			setLIS2MDLAltMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq, int powerMode) {
		int magRate = 0; // 10Hz
		
		if (isEnabled) {
			if (powerMode == 0) // low power mode enabled
			{
				magRate = 0; // 10Hz
			} else {
				if (freq <= 10.0){
					magRate = 0; // 10Hz
				} else if (freq <= 20.0){
					magRate = 1; // 20Hz
				} else if (freq <= 50.0) {
					magRate = 2; // 50Hz
				} else {
					magRate = 3; // 100Hz
				}
			}
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
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG) && mCurrentCalibDetailsMag!=null){
				double[] unCalibratedMagData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Magnetometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
						unCalibratedMagData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
						unCalibratedMagData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
						unCalibratedMagData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}	
				}
				
//				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mCurrentCalibDetailsMag);

				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG)){
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
							objectCluster.addCalData(channelDetails, calibratedMagData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultMagParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultMagParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultMagParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.CAL.toString()}));
				}
			}
		}
		return objectCluster;
	}
	
	public boolean isUsingDefaultMagParam(){
		return getCurrentCalibDetailsMag().isUsingDefaultParameters(); 
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdMag)) {
			setDefaultLisMagSensorConfig(false);
		}
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			//configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getMagRange() & configByteLayoutCast.maskLSM303DLHCMagRange) << configByteLayoutCast.bitShiftLSM303DLHCMagRange);
			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getLIS2MDLMagRate() & configByteLayoutCast.maskLSM303DLHCMagSamplingRate) << configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate);
			//configBytes[configByteLayoutCast.idxConfigSetupByte5] |= (byte) ((getLIS2MDLMagRate() >> 3 & configByteLayoutCast.maskLIS2MDLMagRateMSB) << configByteLayoutCast.bitShiftLIS2MDLMagRateMSB);

			// LISM3MDL Magnetometer Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamLIS2MDLMag();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLSM303DLHCMagCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
		}
	}
	
	public byte[] generateCalParamLIS2MDLMag(){
		return getCurrentCalibDetailsMag().generateCalParamByteArray();
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			//setLISMagRange((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagRange) & configByteLayoutCast.maskLSM303DLHCMagRange);
			//setLISMagRate((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate) & configByteLayoutCast.maskLSM303DLHCMagSamplingRate);
			int magRate = (configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate) & configByteLayoutCast.maskLSM303DLHCMagSamplingRate;
			//int msbMagRate = (configBytes[configByteLayoutCast.idxConfigSetupByte5] >> configByteLayoutCast.bitShiftLIS2MDLMagRateMSB) & configByteLayoutCast.maskLIS2MDLMagRateMSB;
			setLIS2MDLMagRate(magRate);
			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsMag().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			// LSM303DLHC Magnetometer Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketMag(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}

	public void parseCalibParamFromPacketMag(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		getCurrentCalibDetailsMag().parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
		case (GuiLabelConfig.LIS2MDL_MAG_LP):
			setLowPowerMag((boolean) valueToSet);
			break;	
		case(GuiLabelConfig.LIS2MDL_MAG_RANGE):
				setLISMagRange((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2MDL_MAG_RATE):
				setLIS2MDLMagRate((int)valueToSet);
				break;
				
//			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_ALL):
//				TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfKinematicSensorCalibration = (TreeMap<Integer, TreeMap<Integer, CalibDetails>>) valueToSet;
//				setCalibration(mapOfKinematicSensorCalibration);
//				returnValue = valueToSet;
//	    		break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS2MDL_MAG_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS2MDL_MAG_RATE, valueToSet);
				}
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorLIS2MDL.GuiLabelConfig.LIS2MDL_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LIS2MDL_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case (GuiLabelConfig.LIS2MDL_MAG_LP):
				returnValue = isLowPowerMagEnabled();
				break;
			case(GuiLabelConfig.LIS2MDL_MAG_RATE): 
				returnValue = getLIS2MDLMagRate();
		    	break;
			case(GuiLabelConfig.LIS2MDL_MAG_RANGE):
				//TODO check below and commented out code (RS (20/5/2016): Same as in ShimmerObject.)
				returnValue = getMagRange();
	        	
//			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL):
//				returnValue = getKinematicCalibration();
//				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS2MDL_MAG_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS2MDL_MAG_RATE);
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
			
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		setLIS2MDLAltMagRateFromFreq(samplingRateHz);
	}
	
	public void setDefaultLisMagSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLISMagRange(0);
		}	
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdMag) {
				setDefaultLisMagSensorConfig(isSensorEnabled);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//--------- Sensor specific methods end --------------
	
	public double getCalibTimeMag() {
		return mCurrentCalibDetailsMag.getCalibTimeMs();
	}
	
	public boolean isUsingValidMagParam() {
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixMag()) && !UtilShimmer.isAllZeros(getSensitivityMatrixMag())){
			return true;
		}else{
			return false;
		}
	}
	
	public double[][] getAlignmentMatrixMag(){
		return getCurrentCalibDetailsMag().getValidAlignmentMatrix();
	}
	
	public void updateIsUsingDefaultMagParam() {
		mIsUsingDefaultMagParam = getCurrentCalibDetailsMag().isUsingDefaultParameters();
	}
	
	public double[][] getSensitivityMatrixMag(){
		return getCurrentCalibDetailsMag().getValidSensitivityMatrix();
	}
	
	public double[][] getOffsetVectorMatrixMag(){
		return getCurrentCalibDetailsMag().getValidOffsetVector();
	}

}
