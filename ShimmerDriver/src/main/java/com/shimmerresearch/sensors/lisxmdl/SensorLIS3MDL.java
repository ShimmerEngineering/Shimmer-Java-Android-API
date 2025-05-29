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

public class SensorLIS3MDL extends AbstractSensor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4028368641088628178L;

	protected int mAltMagRange = 0;
	public boolean mIsUsingDefaultAltMagParam = true;
	protected int mLISAltMagRate = 4;
	protected int mSensorIdAltMag = -1;
	protected boolean mLowPowerMag = false;
	protected boolean mMedPowerMag = false;
	protected boolean mHighPowerMag = false;
	protected boolean mUltraHighPowerMag = false;

	// public CalibDetailsKinematic mCurrentCalibDetailsMagAlt = null;

	// --------- Sensor specific variables start --------------

	public static final double[][] DefaultAlignmentLIS3MDL = { { 1, 0, 0 }, { 0, -1, 0 }, { 0, 0, -1 } };

	// ---------- Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixAltMagShimmer3r = DefaultAlignmentLIS3MDL;
	public static final double[][] DefaultOffsetVectorAltMagShimmer3r = { { 0 }, { 0 }, { 0 } };
	// Manufacturer stated: X any Y any Z axis @ 6842 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixAltMag4GaShimmer3r = { { 6842, 0, 0 }, { 0, 6842, 0 },
			{ 0, 0, 6842 } };
	// Manufacturer stated: X any Y any Z axis @ 3421 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixAltMag8GaShimmer3r = { { 3421, 0, 0 }, { 0, 3421, 0 },
			{ 0, 0, 3421 } };
	// Manufacturer stated: X any Y any Z axis @ 2281 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixAltMag12GaShimmer3r = { { 2281, 0, 0 }, { 0, 2281, 0 },
			{ 0, 0, 2281 } };
	// Manufacturer stated: X any Y any Z axis @ 1711 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixAltMag16GaShimmer3r = { { 1711, 0, 0 }, { 0, 1711, 0 },
			{ 0, 0, 1711 } };

	private CalibDetailsKinematic calibDetailsMag4 = new CalibDetailsKinematic(ListofLIS3MDLAltMagRangeConfigValues[0],
			ListofLIS3MDLAltMagRange[0], DefaultAlignmentMatrixAltMagShimmer3r,
			DefaultSensitivityMatrixAltMag4GaShimmer3r, DefaultOffsetVectorAltMagShimmer3r);

	private CalibDetailsKinematic calibDetailsMag8 = new CalibDetailsKinematic(ListofLIS3MDLAltMagRangeConfigValues[1],
			ListofLIS3MDLAltMagRange[1], DefaultAlignmentMatrixAltMagShimmer3r,
			DefaultSensitivityMatrixAltMag8GaShimmer3r, DefaultOffsetVectorAltMagShimmer3r);

	private CalibDetailsKinematic calibDetailsMag12 = new CalibDetailsKinematic(ListofLIS3MDLAltMagRangeConfigValues[2],
			ListofLIS3MDLAltMagRange[2], DefaultAlignmentMatrixAltMagShimmer3r,
			DefaultSensitivityMatrixAltMag12GaShimmer3r, DefaultOffsetVectorAltMagShimmer3r);

	private CalibDetailsKinematic calibDetailsMag16 = new CalibDetailsKinematic(ListofLIS3MDLAltMagRangeConfigValues[3],
			ListofLIS3MDLAltMagRange[3], DefaultAlignmentMatrixAltMagShimmer3r,
			DefaultSensitivityMatrixAltMag16GaShimmer3r, DefaultOffsetVectorAltMagShimmer3r);

	public CalibDetailsKinematic mCurrentCalibDetailsMagAlt = calibDetailsMag4;

	// ---------- Mag end ---------------

	public static class DatabaseChannelHandles {
		public static final String ALT_MAG_X = "LIS3MDL_MAG_X";
		public static final String ALT_MAG_Y = "LIS3MDL_MAG_Y";
		public static final String ALT_MAG_Z = "LIS3MDL_MAG_Z";
	}

	public static final class DatabaseConfigHandle {
		public static final String ALT_MAG_RATE = "LIS3MDL_ALT_Mag_Rate";
		public static final String ALT_MAG_RANGE = "LIS3MDL_ALT_Mag_Range";

		public static final String ALT_MAG_LPM = "LIS3MDL_Mag_LPM";
		public static final String ALT_MAG_MPM = "LIS3MDL_Mag_MPM";
		public static final String ALT_MAG_HPM = "LIS3MDL_Mag_HPM";
		public static final String ALT_MAG_UPM = "LIS3MDL_Mag_UPM";

		public static final String MAG_ALT_CALIB_TIME = "LIS3MDL_Mag_ALT_Calib_Time";
		public static final String MAG_ALT_OFFSET_X = "LIS3MDL_Mag_ALT_Offset_X";
		public static final String MAG_ALT_OFFSET_Y = "LIS3MDL_Mag_ALT_Offset_Y";
		public static final String MAG_ALT_OFFSET_Z = "LIS3MDL_Mag_ALT_Offset_Z";
		public static final String MAG_ALT_GAIN_X = "LIS3MDL_Mag_ALT_Gain_X";
		public static final String MAG_ALT_GAIN_Y = "LIS3MDL_Mag_ALT_Gain_Y";
		public static final String MAG_ALT_GAIN_Z = "LIS3MDL_Mag_ALT_Gain_Z";
		public static final String MAG_ALT_ALIGN_XX = "LIS3MDL_Mag_ALT_Align_XX";
		public static final String MAG_ALT_ALIGN_XY = "LIS3MDL_Mag_ALT_Align_XY";
		public static final String MAG_ALT_ALIGN_XZ = "LIS3MDL_Mag_ALT_Align_XZ";
		public static final String MAG_ALT_ALIGN_YX = "LIS3MDL_Mag_ALT_Align_YX";
		public static final String MAG_ALT_ALIGN_YY = "LIS3MDL_Mag_ALT_Align_YY";
		public static final String MAG_ALT_ALIGN_YZ = "LIS3MDL_Mag_ALT_Align_YZ";
		public static final String MAG_ALT_ALIGN_ZX = "LIS3MDL_Mag_ALT_Align_ZX";
		public static final String MAG_ALT_ALIGN_ZY = "LIS3MDL_Mag_ALT_Align_ZY";
		public static final String MAG_ALT_ALIGN_ZZ = "LIS3MDL_Mag_ALT_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_ALT_OFFSET_X, DatabaseConfigHandle.MAG_ALT_OFFSET_Y,
				DatabaseConfigHandle.MAG_ALT_OFFSET_Z, DatabaseConfigHandle.MAG_ALT_GAIN_X,
				DatabaseConfigHandle.MAG_ALT_GAIN_Y, DatabaseConfigHandle.MAG_ALT_GAIN_Z,
				DatabaseConfigHandle.MAG_ALT_ALIGN_XX, DatabaseConfigHandle.MAG_ALT_ALIGN_XY,
				DatabaseConfigHandle.MAG_ALT_ALIGN_XZ, DatabaseConfigHandle.MAG_ALT_ALIGN_YX,
				DatabaseConfigHandle.MAG_ALT_ALIGN_YY, DatabaseConfigHandle.MAG_ALT_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALT_ALIGN_ZX, DatabaseConfigHandle.MAG_ALT_ALIGN_ZY,
				DatabaseConfigHandle.MAG_ALT_ALIGN_ZZ);

	}

	public class GuiLabelConfig {

		public static final String LIS3MDL_ALT_MAG_RANGE = "Alternate Mag Range";
		public static final String LIS3MDL_ALT_MAG_RATE = "Alternate Mag Rate";

		public static final String LIS3MDL_ALT_MAG_LP = "Alt Mag Low-Power Mode";
		public static final String LIS3MDL_ALT_MAG_MP = "Alt Mag Med-Power Mode";
		public static final String LIS3MDL_ALT_MAG_HP = "Alt Mag High-Power Mode";
		public static final String LIS3MDL_ALT_MAG_UP = "Alt Mag Ultra High-Power Mode";

		public static final String LIS3MDL_ALT_MAG_DEFAULT_CALIB = "Alternate Mag Default Calibration";

		// NEW
		public static final String LIS3MDL_ALT_MAG_CALIB_PARAM = "Alternate Mag Calibration Details";
		public static final String LIS3MDL_ALT_MAG_VALID_CALIB = "Alternate Mag Valid Calibration";
	}

	public static class ObjectClusterSensorName {

		public static String MAG_ALT_X = "Mag_Alt_X";
		public static String MAG_ALT_Y = "Mag_Alt_Y";
		public static String MAG_ALT_Z = "Mag_Alt_Z";
	}

	public class GuiLabelSensors {
		public static final String MAG_ALT = "Alternate Magnetometer";
	}

	public class LABEL_SENSOR_TILE {
		public static final String ALT_MAG = GuiLabelSensors.MAG_ALT;
	}

	// --------- Sensor specific variables end --------------

	// --------- Configuration options start --------------

	public static final String[] ListofLIS3MDLAltMagRate = { "1000Hz", "560Hz", "300Hz", "155Hz", "80Hz", "20Hz","10Hz" };
	public static final Integer[] ListofLIS3MDLAltMagRateConfigValues = { 0x01, 0x11, 0x21, 0x31, 0x3E, 0x3A, 0x08 };
	public static final String[] ListofLIS3MDLAltMagRange = { "+/- 4Ga", "+/- 8Ga", "+/- 12Ga", "+/- 16Ga" };
	public static final Integer[] ListofLIS3MDLAltMagRangeConfigValues = { 0, 1, 2, 3 };

	public static final ConfigOptionDetailsSensor configOptionAltMagRange = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LIS3MDL_ALT_MAG_RANGE, SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RANGE,
			ListofLIS3MDLAltMagRange, ListofLIS3MDLAltMagRangeConfigValues,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);

	public static final ConfigOptionDetailsSensor configOptionAltMagRate = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LIS3MDL_ALT_MAG_RATE, SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RATE,
			ListofLIS3MDLAltMagRate, ListofLIS3MDLAltMagRateConfigValues,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);

	// --------- Configuration options end --------------

	// --------- Sensor info start --------------

	public static final SensorDetailsRef sensorLIS3MDLAltMag = new SensorDetailsRef(0x200000, // ==
																							// Configuration.Shimmer3.SensorBitmap.SENSOR_MAG
																							// will be:
																							// SensorBitmap.SENSOR_MAG,
			0x200000, // == Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be:
						// SensorBitmap.SENSOR_MAG,
			GuiLabelSensors.MAG_ALT, CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL,
			Arrays.asList(GuiLabelConfig.LIS3MDL_ALT_MAG_RANGE, GuiLabelConfig.LIS3MDL_ALT_MAG_RATE),
			Arrays.asList(ObjectClusterSensorName.MAG_ALT_X, ObjectClusterSensorName.MAG_ALT_Y,
					ObjectClusterSensorName.MAG_ALT_Z));

	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, SensorLIS3MDL.sensorLIS3MDLAltMag);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	// --------- Sensor info end --------------

	// --------- Channel info start --------------

	public static final ChannelDetails channelLIS3MDLAltMagX = new ChannelDetails(ObjectClusterSensorName.MAG_ALT_X,
			ObjectClusterSensorName.MAG_ALT_X, DatabaseChannelHandles.ALT_MAG_X, CHANNEL_DATA_TYPE.INT12, 2,
			CHANNEL_DATA_ENDIAN.LSB, CHANNEL_UNITS.LOCAL_FLUX, Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x17);

	public static final ChannelDetails channelLIS3MDLAltMagY = new ChannelDetails(ObjectClusterSensorName.MAG_ALT_Y,
			ObjectClusterSensorName.MAG_ALT_Y, DatabaseChannelHandles.ALT_MAG_Y, CHANNEL_DATA_TYPE.INT12, 2,
			CHANNEL_DATA_ENDIAN.LSB, CHANNEL_UNITS.LOCAL_FLUX, Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x18);

	public static final ChannelDetails channelLIS3MDLAltMagZ = new ChannelDetails(ObjectClusterSensorName.MAG_ALT_Z,
			ObjectClusterSensorName.MAG_ALT_Z, DatabaseChannelHandles.ALT_MAG_Z, CHANNEL_DATA_TYPE.INT12, 2,
			CHANNEL_DATA_ENDIAN.LSB, CHANNEL_UNITS.LOCAL_FLUX, Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x19);

	public static final Map<String, ChannelDetails> mChannelMapRef;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_ALT_X, SensorLIS3MDL.channelLIS3MDLAltMagX);
		aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_ALT_Z, SensorLIS3MDL.channelLIS3MDLAltMagZ);
		aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_ALT_Y, SensorLIS3MDL.channelLIS3MDLAltMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
	}
	// --------- Channel info end --------------

	public static final SensorGroupingDetails sensorGroupLisAltMag = new SensorGroupingDetails(LABEL_SENSOR_TILE.ALT_MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);

	// --------- Bluetooth commands start --------------
	// still not being implemented for ALTt mag sensor due to unavailability in docs
	public static final byte SET_ALT_MAG_CALIBRATION_COMMAND = (byte) 0xAF;
	public static final byte ALT_MAG_CALIBRATION_RESPONSE = (byte) 0xB0;
	public static final byte GET_ALT_MAG_CALIBRATION_COMMAND = (byte) 0xB1;

	public static final byte SET_ALT_MAG_SAMPLING_RATE_COMMAND = (byte) 0xB2;
	public static final byte ALT_MAG_SAMPLING_RATE_RESPONSE = (byte) 0xB3;
	public static final byte GET_ALT_MAG_SAMPLING_RATE_COMMAND = (byte) 0xB4;

	public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	static {
		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
		aMap.put(GET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_ALT_MAG_CALIBRATION_COMMAND,
				"GET_ALT_MAG_CALIBRATION_COMMAND", ALT_MAG_CALIBRATION_RESPONSE));
		aMap.put(GET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ALT_MAG_SAMPLING_RATE_COMMAND,
				"GET_ALT_MAG_SAMPLING_RATE_COMMAND", ALT_MAG_SAMPLING_RATE_RESPONSE));
		mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	}

	public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	static {
		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
		aMap.put(SET_ALT_MAG_CALIBRATION_COMMAND,
				new BtCommandDetails(SET_ALT_MAG_CALIBRATION_COMMAND, "SET_ALT_MAG_CALIBRATION_COMMAND"));
		aMap.put(SET_ALT_MAG_SAMPLING_RATE_COMMAND,
				new BtCommandDetails(SET_ALT_MAG_SAMPLING_RATE_COMMAND, "SET_ALT_MAG_SAMPLING_RATE_COMMAND"));
		mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	}
	// --------- Bluetooth commands end --------------

	// --------- Constructors for this class start --------------
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
	// --------- Constructors for this class end --------------

	// --------- Abstract methods implemented start --------------

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(configOptionAltMagRange);
		addConfigOption(configOptionAltMagRate);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.ALT_MAG_3R.ordinal(), sensorGroupLisAltMag);
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

		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RANGE, getAltMagRange());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RATE, getLIS3MDLAltMagRate());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_LPM, getLowPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_MPM, getMedPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_HPM, getHighPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_UPM, getUltraHighPowerMagEnabled());

		super.addCalibDetailsToDbMap(mapOfConfig, getCurrentCalibDetailsMagAlt(),
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_ALT_CALIB_TIME);

		return mapOfConfig;
	}

	public CalibDetailsKinematic getCurrentCalibDetailsMagAlt() {
		return mCurrentCalibDetailsMagAlt;
	}

	public void updateCurrentMagAltCalibInUse() {
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMagAlt = getCurrentCalibDetailsIfKinematic(mSensorIdAltMag, getAltMagRange());
	}

	public int getAltMagRange() {
		return mAltMagRange;
	}

	public int getLIS3MDLAltMagRate() {
		return mLISAltMagRate;
	}
	
	public double getLIS3MDLAltMagRateInHz() {
		
		if(ArrayUtils.contains(ListofLIS3MDLAltMagRateConfigValues, mLISAltMagRate)){
			return ListofLIS3MDLAltMagRateConfigValues[mLISAltMagRate];
		}

		return mLISAltMagRate;
	}

	public void setLIS3MDLAltMagRate(int valueToSet) {
		mLISAltMagRate = valueToSet;
	}

	public void setAltMagRange(int valueToSet) {
		setLIS3MDLAltMagRange(valueToSet);
	}

	public void setLIS3MDLAltMagRange(int i) {
		if (ArrayUtils.contains(ListofLIS3MDLAltMagRangeConfigValues, i)) {

			mAltMagRange = i;
			updateCurrentMagAltCalibInUse();
		}

	}

	public int getLowPowerMagEnabled() {
		return (isLowPowerMagEnabled() ? 1 : 0);
	}

	public int getMedPowerMagEnabled() {
		return (isMedPowerMagEnabled() ? 1 : 0);
	}

	public int getHighPowerMagEnabled() {
		return (isHighPowerMagEnabled() ? 1 : 0);
	}

	public int getUltraHighPowerMagEnabled() {
		return (isUltraHighPowerMagEnabled() ? 1 : 0);
	}

	public void setLowPowerMag(boolean enable) {
		mLowPowerMag = enable;
		if (mShimmerDevice != null) {
			setLIS3MDLAltMagRateFromFreq(getSamplingRateShimmer());
		}
	}

	public void setMedPowerMag(boolean enable) {
		mMedPowerMag = enable;
		if (mShimmerDevice != null) {
			setLIS3MDLAltMagRateFromFreq(getSamplingRateShimmer());
		}
	}

	public void setHighPowerMag(boolean enable) {
		mHighPowerMag = enable;
		if (mShimmerDevice != null) {
			setLIS3MDLAltMagRateFromFreq(getSamplingRateShimmer());
		}
	}

	public void setUltraHighPowerMag(boolean enable) {
		mUltraHighPowerMag = enable;
		if (mShimmerDevice != null) {
			setLIS3MDLAltMagRateFromFreq(getSamplingRateShimmer());
		}
	}

	public boolean isLowPowerMagEnabled() {
		return mLowPowerMag;
	}

	public boolean isMedPowerMagEnabled() {
		return mMedPowerMag;
	}

	public boolean isHighPowerMagEnabled() {
		return mHighPowerMag;
	}

	public boolean isUltraHighPowerMagEnabled() {
		return mUltraHighPowerMag;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {

		if (mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_LPM)) {
			setLowPowerMag(
					((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_LPM)) > 0 ? true
							: false);
		}
		if (mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_MPM)) {
			setMedPowerMag(
					((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_MPM)) > 0 ? true
							: false);
		}
		if (mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_HPM)) {
			setHighPowerMag(
					((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_HPM)) > 0 ? true
							: false);
		}
		if (mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_UPM)) {
			setUltraHighPowerMag(
					((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_UPM)) > 0 ? true
							: false);
		}
		if (mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RANGE)) {
			setAltMagRange(
					((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RANGE)).intValue());
		}
		if (mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RATE)) {
			setLIS3MDLAltMagRate(
					((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.ALT_MAG_RATE)).intValue());
		}

		// Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer,
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, getAltMagRange(),
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_ALT_CALIB_TIME);
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	// --------- Abstract methods implemented end --------------

	// --------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdAltMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT;
		super.initialise();

		mAltMagRange = ListofLIS3MDLAltMagRangeConfigValues[0];

		updateCurrentMagAltCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMagAlt = new TreeMap<Integer, CalibDetails>();
		calibMapMagAlt.put(calibDetailsMag4.mRangeValue, calibDetailsMag4);
		calibMapMagAlt.put(calibDetailsMag8.mRangeValue, calibDetailsMag8);
		calibMapMagAlt.put(calibDetailsMag12.mRangeValue, calibDetailsMag12);
		calibMapMagAlt.put(calibDetailsMag16.mRangeValue, calibDetailsMag16);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, calibMapMagAlt);

		updateCurrentMagAltCalibInUse();
	}
	
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT){
			return isUsingDefaultMagAltParam();
		}
		return false;
	}

	// --------- Optional methods to override in Sensor Class end --------

	// --------- Sensor specific methods start --------------

	public double getCalibTimeMagAlt() {
		return mCurrentCalibDetailsMagAlt.getCalibTimeMs();
	}

	public boolean isUsingValidMagAltParam() {
		if (!UtilShimmer.isAllZeros(getAlignmentMatrixMagAlt())
				&& !UtilShimmer.isAllZeros(getSensitivityMatrixMagAlt())) {
			return true;
		} else {
			return false;
		}
	}

	public void updateIsUsingDefaultAltMagParam() {
		mIsUsingDefaultAltMagParam = getCurrentCalibDetailsMagAlt().isUsingDefaultParameters();
	}

	public int setLIS3MDLAltMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdAltMag);

		if (isLowPowerMagEnabled()) {
			mLISAltMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 0);
		} else {
			mLISAltMagRate = getMagRateFromFreqForSensor(isEnabled, freq, -1);
		}
		return mLISAltMagRate;
	}

	public boolean checkLowPowerMag() {
		setLowPowerMag((getLIS3MDLAltMagRate() == 0x08) ? true : false); // 10Hz
		return isLowPowerMagEnabled();
	}

	public boolean checkMedPowerMag() {
		setMedPowerMag((getLIS3MDLAltMagRate() >= 17 && getLIS3MDLAltMagRate() <= 30) ? true : false);
		return isMedPowerMagEnabled();
	}

	public boolean checkHighPowerMag() {
		setHighPowerMag((getLIS3MDLAltMagRate() >= 33 && getLIS3MDLAltMagRate() <= 46) ? true : false);
		return isHighPowerMagEnabled();
	}

	public boolean checkUltraHighPowerMag() {
		setUltraHighPowerMag((getLIS3MDLAltMagRate() >= 49 && getLIS3MDLAltMagRate() <= 62) ? true : false);
		return isUltraHighPowerMagEnabled();
	}

	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq, int powerMode) {
		return SensorLIS3MDL.getMagRateFromFreq(isEnabled, freq, powerMode);
	}

	public static int getMagRateFromFreq(boolean isEnabled, double freq, int powerMode) {
		int magRate = 0; // 0.625Hz

		if (isEnabled) {
			if (powerMode == 0) // low power mode enabled
			{
				magRate = 0x08;
			} else {
				if (freq > 560) {
					magRate = 0x01; // Low Power Mode (1000Hz)
				} else if (freq > 300) {
					magRate = 0x11; // Medium (560Hz)
				} else if (freq > 155) {
					magRate = 0x21; // High (300Hz)
				} else if (freq > 100) {
					magRate = 0x31; // Ultra High (155Hz)
				} else if (freq > 50) {
					magRate = 0x31; // Ultra High (155Hz)
				} else if (freq > 20) {
					magRate = 0x3E; // Ultra High (80Hz)
				} else if (freq > 10) {
					magRate = 0x3A; // Ultra High (20Hz)
				} else {
					magRate = 0x08; // Ultra High (10Hz)
				}
			}
		}

		return magRate;
	}

	public static int lowPowerMode(double freq) {
		int magRate = 0;

		if (freq <= 0.625) {
			magRate = 0;
		} else if (freq <= 1.25) {
			magRate = 2;
		} else if (freq <= 2.5) {
			magRate = 4;
		} else if (freq <= 5) {
			magRate = 6;
		} else if (freq <= 10) {
			magRate = 8;
		} else if (freq <= 20) {
			magRate = 10;
		} else if (freq <= 40) {
			magRate = 12;
		} else if (freq <= 80) {
			magRate = 14;
		} else if (freq <= 1000) {
			magRate = 1;
		}

		return magRate;
	}

	public static int medPowerMode(double freq) {
		int magRate = 18;

		if (freq <= 1.25) {
			magRate = 18;
		} else if (freq <= 2.5) {
			magRate = 20;
		} else if (freq <= 5) {
			magRate = 22;
		} else if (freq <= 10) {
			magRate = 24;
		} else if (freq <= 20) {
			magRate = 26;
		} else if (freq <= 40) {
			magRate = 28;
		} else if (freq <= 80) {
			magRate = 30;
		} else if (freq <= 560) {
			magRate = 17;
		}
		return magRate;
	}

	public static int highPowerMode(double freq) {
		int magRate = 34;

		if (freq <= 1.25) {
			magRate = 34;
		} else if (freq <= 2.5) {
			magRate = 36;
		} else if (freq <= 5) {
			magRate = 38;
		} else if (freq <= 10) {
			magRate = 40;
		} else if (freq <= 20) {
			magRate = 42;
		} else if (freq <= 40) {
			magRate = 44;
		} else if (freq <= 80) {
			magRate = 46;
		} else if (freq <= 300) {
			magRate = 33;
		}
		return magRate;
	}

	public static int ultraHighPowerMode(double freq) {
		int magRate = 50;

		if (freq <= 1.25) {
			magRate = 50;
		} else if (freq <= 2.5) {
			magRate = 52;
		} else if (freq <= 5) {
			magRate = 54;
		} else if (freq <= 10) {
			magRate = 56;
		} else if (freq <= 20) {
			magRate = 58;
		} else if (freq <= 40) {
			magRate = 60;
		} else if (freq <= 80) {
			magRate = 62;
		} else if (freq <= 155) {
			magRate = 49;
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
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled,
				pcTimestampMs);

		// Calibration
		if (mEnableCalibration) {
			// get uncalibrated data for each (sub)sensor
			if (sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_ALT)
					&& mCurrentCalibDetailsMagAlt != null) {
				double[] unCalibratedMagAltData = new double[3];
				for (ChannelDetails channelDetails : sensorDetails.mListOfChannels) {
					// Uncalibrated Mag ALT data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_ALT_X)) {
						unCalibratedMagAltData[0] = ((FormatCluster) ObjectCluster.returnFormatCluster(
								objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName),
								channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_ALT_Y)) {
						unCalibratedMagAltData[1] = ((FormatCluster) ObjectCluster.returnFormatCluster(
								objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName),
								channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_ALT_Z)) {
						unCalibratedMagAltData[2] = ((FormatCluster) ObjectCluster.returnFormatCluster(
								objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName),
								channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}

				double[] calibratedMagAltData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagAltData,
						mCurrentCalibDetailsMagAlt);
//				double[] calibratedAccelAltData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelAltData, mAlignmentMatrixAltAccel, mSensitivityMatrixAltAccel, mOffsetVectorAltAccel);

				// Add calibrated data to Object cluster
				if (sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_ALT)) {
					for (ChannelDetails channelDetails : sensorDetails.mListOfChannels) {
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_ALT_X)) {
							objectCluster.addCalData(channelDetails, calibratedMagAltData[0],
									objectCluster.getIndexKeeper() - 3, isUsingDefaultMagAltParam());
						} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_ALT_Y)) {
							objectCluster.addCalData(channelDetails, calibratedMagAltData[1],
									objectCluster.getIndexKeeper() - 2, isUsingDefaultMagAltParam());
						} else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_ALT_Z)) {
							objectCluster.addCalData(channelDetails, calibratedMagAltData[2],
									objectCluster.getIndexKeeper() - 1, isUsingDefaultMagAltParam());
						}
					}
				}

				// Debugging
				if (mIsDebugOutput) {
					super.consolePrintChannelsCal(objectCluster,
							Arrays.asList(
									new String[] { ObjectClusterSensorName.MAG_ALT_X, CHANNEL_TYPE.UNCAL.toString() },
									new String[] { ObjectClusterSensorName.MAG_ALT_Y, CHANNEL_TYPE.UNCAL.toString() },
									new String[] { ObjectClusterSensorName.MAG_ALT_Z, CHANNEL_TYPE.UNCAL.toString() },
									new String[] { ObjectClusterSensorName.MAG_ALT_X, CHANNEL_TYPE.CAL.toString() },
									new String[] { ObjectClusterSensorName.MAG_ALT_Y, CHANNEL_TYPE.CAL.toString() },
									new String[] { ObjectClusterSensorName.MAG_ALT_Z, CHANNEL_TYPE.CAL.toString() }));
				}

			}
		}
		return objectCluster;
	}

	public boolean isUsingDefaultMagAltParam() {
		return getCurrentCalibDetailsMagAlt().isUsingDefaultParameters();
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if (!isSensorEnabled(mSensorIdAltMag)) {
			setDefaultLisMagAltSensorConfig(false);
		}

	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if (configByteLayout instanceof ConfigByteLayoutShimmer3) {
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getAltMagRange()
					& configByteLayoutCast.maskLSM303DLHCMagRange) << configByteLayoutCast.bitShiftLSM303DLHCMagRange);
			
			configBytes[configByteLayoutCast.idxConfigSetupByte5] |= (byte) ((getLIS3MDLAltMagRate()
					& configByteLayoutCast.maskLIS3MDLAltMagSamplingRate) << configByteLayoutCast.bitShiftLIS3MDLAltMagSamplingRate);

			byte[] bufferCalibrationParameters = generateCalParamLIS3MDLMag();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes,
					configByteLayoutCast.idxLIS3MDLAltMagCalibration,
					configByteLayoutCast.lengthGeneralCalibrationBytes);
		}
	}

	public byte[] generateCalParamLIS3MDLMag() {
		return mCurrentCalibDetailsMagAlt.generateCalParamByteArray();
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if (configByteLayout instanceof ConfigByteLayoutShimmer3) {
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			setLIS3MDLAltMagRange((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagRange)
							& configByteLayoutCast.maskLSM303DLHCMagRange);
			setLIS3MDLAltMagRate(
					(configBytes[configByteLayoutCast.idxConfigSetupByte5] >> configByteLayoutCast.bitShiftLIS3MDLAltMagSamplingRate)
							& configByteLayoutCast.maskLIS3MDLAltMagSamplingRate);

			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			if (shimmerDevice.isConnected()) {
				getCurrentCalibDetailsMagAlt().mCalibReadSource = CALIB_READ_SOURCE.INFOMEM;
			}

			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLIS3MDLAltMagCalibration, bufferCalibrationParameters,
					0, configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketMagAlt(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}

	public void parseCalibParamFromPacketMagAlt(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsMagAlt.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;

		switch (configLabel) {
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_LP):
			setLowPowerMag((boolean) valueToSet);
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_MP):
			setMedPowerMag((boolean) valueToSet);
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_HP):
			setHighPowerMag((boolean) valueToSet);
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_UP):
			setUltraHighPowerMag((boolean) valueToSet);
			break;//LIS2MDL_MAG_RANGE
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_RANGE):
			setLIS3MDLAltMagRange((int) valueToSet);
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_RATE):
			setLIS3MDLAltMagRate((int) valueToSet);
			break;
		case (GuiLabelConfigCommon.RANGE):
			if (sensorId == mSensorIdAltMag) {
				this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_ALT_MAG_RANGE, valueToSet);
				break;
			}
		case (GuiLabelConfigCommon.RATE):
			if (sensorId == mSensorIdAltMag) {
				this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_ALT_MAG_RATE, valueToSet);
				break;
			}
		default:
			returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
			break;
		}

		if (configLabel.equals(SensorLIS3MDL.GuiLabelConfig.LIS3MDL_ALT_MAG_RATE)) {
			checkConfigOptionValues(configLabel);
		}

		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;

		if (configLabel.equals(GuiLabelConfig.LIS3MDL_ALT_MAG_RATE)) {
			checkConfigOptionValues(configLabel);
		}

		switch (configLabel) {
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_LP):
			returnValue = isLowPowerMagEnabled();
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_MP):
			returnValue = isMedPowerMagEnabled();
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_HP):
			returnValue = isHighPowerMagEnabled();
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_UP):
			returnValue = isUltraHighPowerMagEnabled();
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_RANGE):
			returnValue = getAltMagRange();
			break;
		case (GuiLabelConfig.LIS3MDL_ALT_MAG_RATE):
			int configValue = getLIS3MDLAltMagRate();
			//int configValue = (int) getLIS3MDLAltMagRateInHz();
			returnValue = configValue;
			break;
		case (GuiLabelConfigCommon.RANGE):
			if (sensorId == mSensorIdAltMag) {
				returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_ALT_MAG_RANGE);
//					returnValue = 0;
				break;
			}
		case (GuiLabelConfigCommon.RATE):
			if (sensorId == mSensorIdAltMag) {
				returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_ALT_MAG_RATE);
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
		// set sampling rate of the sensors as close to the Shimmer sampling rate as
		// possible (sensor sampling rate >= shimmer sampling rate)

		setLIS3MDLAltMagRateFromFreq(samplingRateHz);
		checkLowPowerMag();
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if (mSensorMap.containsKey(sensorId)) {
			if (sensorId == mSensorIdAltMag) {
				setDefaultLisMagAltSensorConfig(isSensorEnabled);
			}
			return true;
		}
		return false;
	}

	public void setDefaultLisMagAltSensorConfig(boolean isSensorEnabled) {
		if (isSensorEnabled) {
			setLIS3MDLAltMagRange(0);
		}
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	public double[][] getAlignmentMatrixMagAlt() {
		return mCurrentCalibDetailsMagAlt.getValidAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixMagAlt() {
		return mCurrentCalibDetailsMagAlt.getValidSensitivityMatrix();
	}

	public double[][] getOffsetVectorMatrixMagAlt() {
		return mCurrentCalibDetailsMagAlt.getValidOffsetVector();
	}

	// --------- Sensor specific methods end --------------

}
