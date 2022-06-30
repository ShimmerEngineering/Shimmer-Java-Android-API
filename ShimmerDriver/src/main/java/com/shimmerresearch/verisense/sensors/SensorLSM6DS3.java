package com.shimmerresearch.verisense.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
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
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.payloads.OperationalConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

/** Sensor chip containing a an accelerometer and a gyroscope. 
 * 
 * This sensor is typically refereed to as Accel2/Gyro in the Verisense hardware.
 * 
 * @author Mark Nolan
 *
 */
public class SensorLSM6DS3 extends AbstractSensor {
	
	private static final long serialVersionUID = -8572264157525359641L;

	public static final String ACCEL_ID = "Accel2";

	private static final int DEFAULT_FIFO_BYTE_SIZE_IN_CHIP = 8112;
	@Deprecated
	private static final int DEFAULT_MAX_FIFOS_IN_PAYLOAD = 4;
	private int fifoSizeInChip = DEFAULT_FIFO_BYTE_SIZE_IN_CHIP/2;
	
	/** using the same rate setting for the gyro, accel and fifo */
	protected LSM6DS3_RATE rate = LSM6DS3_RATE.RATE_52_HZ;
	/** 0 Disable/1 Enable step counter and timestamp data as 4th FIFO dataset */
	protected boolean timerPedoFifodEnable = false;
	/** 0 = Enable write in FIFO based on XL/Gyro data-ready
	 *  1 = Disable write in FIFO at every step detected by step counter */
	protected boolean timerPedoFifodDrdy = false;
	/** Gyro FIFO Decimation Settings */
	protected FIFO_DECIMATION_GYRO decimationFifoGyro = FIFO_DECIMATION_GYRO.SENSOR_NOT_IN_FIFO;
	/** Accel FIFO Decimation Settings */
	protected FIFO_DECIMATION_ACCEL decimationFifoAccel = FIFO_DECIMATION_ACCEL.SENSOR_NOT_IN_FIFO;
	/** FIFO Mode Selection */
	protected FIFO_MODE fifoMode = FIFO_MODE.CONTINUOUS_MODE;
	/** Accel Full scale Selection */
	protected LSM6DS3_ACCEL_RANGE rangeAccel = LSM6DS3_ACCEL_RANGE.RANGE_4G;
	/** Anti- aliasing Filter bandwidth selection */
	protected ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER accelAntiAliasingBandwidthFilter = ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER.AT_400HZ;
	/** Gyro full scale selection */
	protected LSM6DS3_GYRO_RANGE rangeGyro = LSM6DS3_GYRO_RANGE.RANGE_500DPS;
	/** Gyro full scale at 12 dps */
	protected boolean gyroFullScaleAt12dps = false;
	/** Gyro High Performance Operating Mode (0 = Enabled, 1 = Disabled) */
	protected boolean gyroHighPerFormanceModeDisable = true;
	/** Gyro Digital High Pass Filter Enable */
	protected boolean gyroDigitalHighPassFilterEnable = false;
	/** Gyro High Pass Filter Cut off Frequency Selection */
	protected HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO gyroHighPassFilterCutOffFreq = HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO.AT_0_0081_HZ;
	/** Gyro Digital HP Filter reset */
	protected boolean gyroDigitalHighPassFilterReset = false;
	/** Source register rounding function */
	protected boolean roundingStatus = false;
	/** Low Pass Filter LPF2 selection (Figure 7  in LSM6DS3US datasheet) */
	protected boolean accelLowPassFilterLpf2Selection = false;
	/** Slope filter and High pass filter configuration and cut off Settings (Table 68 in LSM6DS3US datasheet) */
	protected HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL accelHighPassFilterCutOffFreq = HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.SLOPE;
	/** Accel2 Slope filter and High pass filter Selection (Figure 7  in LSM6DS3US datasheet) */
	protected boolean accelHighPassOrSlopeFilterSelectionEnable = false;
	/** Low Pass Filter on 6D function Selection */
	protected boolean lowPassFilterOn6D = false;

	public static enum LSM6DS3_RATE implements ISensorConfig {
		POWER_DOWN("Power-down", 0b0000, 0.0),
		RATE_12_5_HZ("12.5Hz", 0b0001, 12.5),
		RATE_26_HZ("26.0Hz", 0b0010, 26.0),
		RATE_52_HZ("52.0Hz", 0b0011, 52.0),
		RATE_104_HZ("104.0Hz", 0b0100, 104.0),
		RATE_208_HZ("208.0Hz", 0b0101, 208.0),
		RATE_416_HZ("416.0Hz", 0b0110, 416.0),
		RATE_833_HZ("833.0Hz", 0b0111, 833.0),
		RATE_1666_HZ("1666.0Hz", 0b1000, 1666.0);
		
		public String label;
		public Integer configValue;
		public double freqHz;

		public static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LSM6DS3_RATE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LSM6DS3_RATE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LSM6DS3_RATE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LSM6DS3_RATE(String label, Integer configValue, double freqHz) {
			this.label = label;
			this.configValue = configValue;
			this.freqHz = freqHz;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LSM6DS3_RATE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, POWER_DOWN.configValue, RATE_1666_HZ.configValue));
		}
	}

	public static enum LSM6DS3_ACCEL_RANGE implements ISensorConfig {
		RANGE_2G(UtilShimmer.UNICODE_PLUS_MINUS + " 2g", 0),
		RANGE_4G(UtilShimmer.UNICODE_PLUS_MINUS + " 4g", 2),
		RANGE_8G(UtilShimmer.UNICODE_PLUS_MINUS + " 8g", 3),
		RANGE_16G(UtilShimmer.UNICODE_PLUS_MINUS + " 16g", 1);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LSM6DS3_ACCEL_RANGE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}
		
		static Map<Integer, LSM6DS3_ACCEL_RANGE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LSM6DS3_ACCEL_RANGE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LSM6DS3_ACCEL_RANGE(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LSM6DS3_ACCEL_RANGE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, RANGE_2G.configValue, RANGE_8G.configValue));
		}
	}

	public static enum LSM6DS3_GYRO_RANGE implements ISensorConfig {
		RANGE_250DPS(UtilShimmer.UNICODE_PLUS_MINUS + " 250dps", 0),
		RANGE_500DPS(UtilShimmer.UNICODE_PLUS_MINUS + " 500dps", 1),
		RANGE_1000DPS(UtilShimmer.UNICODE_PLUS_MINUS + " 1000dps", 2),
		RANGE_2000DPS(UtilShimmer.UNICODE_PLUS_MINUS + " 2000dps", 3);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LSM6DS3_GYRO_RANGE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LSM6DS3_GYRO_RANGE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LSM6DS3_GYRO_RANGE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LSM6DS3_GYRO_RANGE(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LSM6DS3_GYRO_RANGE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, RANGE_250DPS.configValue, RANGE_2000DPS.configValue));
		}
	}

	public static enum FIFO_DECIMATION_GYRO implements ISensorConfig {
		SENSOR_NOT_IN_FIFO("Sensor not in FIFO", 0b000),
		NO_DECIMATION("No decimation", 0b001),
		DECIMATION_WITH_FACTOR_2("Decimation with factor 2", 0b010),
		DECIMATION_WITH_FACTOR_3("Decimation with factor 3", 0b011),
		DECIMATION_WITH_FACTOR_4("Decimation with factor 4", 0b100),
		DECIMATION_WITH_FACTOR_8("Decimation with factor 8", 0b101),
		DECIMATION_WITH_FACTOR_16("Decimation with factor 16", 0b110),
		DECIMATION_WITH_FACTOR_32("Decimation with factor 32", 0b111);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (FIFO_DECIMATION_GYRO e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, FIFO_DECIMATION_GYRO> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (FIFO_DECIMATION_GYRO e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private FIFO_DECIMATION_GYRO(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static FIFO_DECIMATION_GYRO getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, SENSOR_NOT_IN_FIFO.configValue, DECIMATION_WITH_FACTOR_32.configValue));
		}
	}
	
	public static enum FIFO_DECIMATION_ACCEL implements ISensorConfig {
		SENSOR_NOT_IN_FIFO("Sensor not in FIFO", 0b000),
		NO_DECIMATION("No decimation", 0b001),
		DECIMATION_WITH_FACTOR_2("Decimation with factor 2", 0b010),
		DECIMATION_WITH_FACTOR_3("Decimation with factor 3", 0b011),
		DECIMATION_WITH_FACTOR_4("Decimation with factor 4", 0b100),
		DECIMATION_WITH_FACTOR_8("Decimation with factor 8", 0b101),
		DECIMATION_WITH_FACTOR_16("Decimation with factor 16", 0b110),
		DECIMATION_WITH_FACTOR_32("Decimation with factor 32", 0b111);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (FIFO_DECIMATION_ACCEL e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, FIFO_DECIMATION_ACCEL> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (FIFO_DECIMATION_ACCEL e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private FIFO_DECIMATION_ACCEL(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static FIFO_DECIMATION_ACCEL getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, SENSOR_NOT_IN_FIFO.configValue, DECIMATION_WITH_FACTOR_32.configValue));
		}
	}

	public static enum FIFO_MODE implements ISensorConfig {
		BYPASS_MODE_FIFO_OFF("Bypass mode: FIFO turned off", 0b000),
		FIFO_MODE("FIFO mode: Stops collecting data when FIFO is full2", 0b001), 
		CONTINUOUS_MODE_THEN_FIFO_MODE("Continuous mode until trigger is deasserted, then FIFO mode", 0b011),
		BYPASS_MODE_THEN_CONTINOUS_MODE("Bypass mode until trigger is deasserted, then continuous mode", 0b100),
		CONTINUOUS_MODE("Continuous mode: If the FIFO is full, the new sample overwrites the older sample", 0b110);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (FIFO_MODE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, FIFO_MODE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (FIFO_MODE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private FIFO_MODE(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static FIFO_MODE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, BYPASS_MODE_FIFO_OFF.configValue, CONTINUOUS_MODE.configValue));
		}
	}
	
	public static enum ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER implements ISensorConfig {
		AT_400HZ("400 Hz", 0b00),
		AT_200HZ("200 Hz", 0b01),
		AT_100HZ("100 Hz", 0b10),
		AT_50HZ("50 Hz", 0b11);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, AT_400HZ.configValue, AT_50HZ.configValue));
		}
	}

	public static enum HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO implements ISensorConfig {
		AT_0_0081_HZ("0.0081 Hz", 0b00),
		AT_0_0324_HZ("0.0324 Hz", 0b01),
		AT_2_07_HZ("2.07 Hz", 0b10),
		AT_16_32_HZ("16.32 Hz", 0b11);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, AT_0_0081_HZ.configValue, AT_16_32_HZ.configValue));
		}
	}

	public static enum HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL implements ISensorConfig {
		SLOPE("Rate/4", 0b00),
		HIGH_PASS_RATE_DIVIDED_BY_100("Rate/100", 0b01),
		HIGH_PASS_RATE_DIVIDED_BY_9("Rate/9", 0b10),
		HIGH_PASS_RATE_DIVIDED_BY_400("Rate/400", 0b11),
		LOW_PASS_RATE_DIVIDED_BY_50("Rate/50", 0b00),
		LOW_PASS_RATE_DIVIDED_BY_100("Rate/100", 0b01),
		LOW_PASS_RATE_DIVIDED_BY_9("Rate/9", 0b10),
		LOW_PASS_RATE_DIVIDED_BY_400("Rate/400", 0b11);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL> BY_CONFIG_VALUE_LOW_PASS_FILTER = new HashMap<>();
		static Map<Integer, HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL> BY_CONFIG_VALUE_SLOPE_OR_HIGH_PASS_FILTER = new HashMap<>();
		static {
			for (HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL e : values()) {
				if (e == LOW_PASS_RATE_DIVIDED_BY_50 
						|| e == LOW_PASS_RATE_DIVIDED_BY_100
						|| e == LOW_PASS_RATE_DIVIDED_BY_9 
						|| e == LOW_PASS_RATE_DIVIDED_BY_400) {
					BY_CONFIG_VALUE_LOW_PASS_FILTER.put(e.configValue, e);
				} else {
					BY_CONFIG_VALUE_SLOPE_OR_HIGH_PASS_FILTER.put(e.configValue, e);
				}
			}
		}

		private HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL getForConfigValue(int configValue, boolean isAccelLowPassFilterLpf2Selection) {
			Map<Integer, HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL> refMap;
			if(isAccelLowPassFilterLpf2Selection) {
				refMap = HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.BY_CONFIG_VALUE_LOW_PASS_FILTER;
			} else {
				refMap = HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.BY_CONFIG_VALUE_SLOPE_OR_HIGH_PASS_FILTER;
			}
			return refMap.get(UtilShimmer.nudgeInteger(configValue, 0b00, 0b11));
		}
	}

	// --------------- Configuration options start ----------------

	public class GuiLabelSensors{
		public static final String ACCEL2 = "Accelerometer2"; 
		public static final String GYRO = "Gyroscope"; 
	}
	
	public static class LABEL_SENSOR_TILE{
		public static final String ACCEL2_GYRO = "ACCEL2 GYRO";
	}

	public static class DatabaseChannelHandles{
		public static final String LSM6DS3_ACC_X = "LSM6DS3_ACC_X";
		public static final String LSM6DS3_ACC_Y = "LSM6DS3_ACC_Y";
		public static final String LSM6DS3_ACC_Z = "LSM6DS3_ACC_Z";
		public static final String LSM6DS3_GYRO_X = "LSM6DS3_GYRO_X";
		public static final String LSM6DS3_GYRO_Y = "LSM6DS3_GYRO_Y";
		public static final String LSM6DS3_GYRO_Z = "LSM6DS3_GYRO_Z";
	}

	public class GuiLabelConfig{
		public static final String LSM6DS3_RATE = "Accel_Gyro_Rate";
		public static final String LSM6DS3_ACCEL_RANGE = "Accel_Range";
		public static final String LSM6DS3_GYRO_RANGE = "Gyro_Range";
	}

	public static class ObjectClusterSensorName{
		public static  String LSM6DS3_ACC_X = ACCEL_ID + "_X";
		public static  String LSM6DS3_ACC_Y = ACCEL_ID + "_Y";
		public static  String LSM6DS3_ACC_Z= ACCEL_ID + "_Z";
		public static  String LSM6DS3_GYRO_X = "Gyro_X";
		public static  String LSM6DS3_GYRO_Y = "Gyro_Y";
		public static  String LSM6DS3_GYRO_Z= "Gyro_Z";
	}

	public static final class DatabaseConfigHandle{
		public static final String LSM6DS3_RANGE = "LSM6DS3_Range";
		public static final String LSM6DS3_RATE = "LSM6DS3_Rate";
	}

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RANGE = new ConfigOptionDetailsSensor (
			SensorLSM6DS3.GuiLabelConfig.LSM6DS3_ACCEL_RANGE,
			SensorLSM6DS3.DatabaseConfigHandle.LSM6DS3_RANGE,
			LSM6DS3_ACCEL_RANGE.getLabels(), 
			LSM6DS3_ACCEL_RANGE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3);

	// Rate is same for both accel and gyro
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_RATE = new ConfigOptionDetailsSensor (
			SensorLSM6DS3.GuiLabelConfig.LSM6DS3_RATE,
			SensorLSM6DS3.DatabaseConfigHandle.LSM6DS3_RATE,
			LSM6DS3_RATE.getLabels(), 
			LSM6DS3_RATE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_GYRO_RANGE = new ConfigOptionDetailsSensor (
			SensorLSM6DS3.GuiLabelConfig.LSM6DS3_GYRO_RANGE,
			SensorLSM6DS3.DatabaseConfigHandle.LSM6DS3_RANGE,
			LSM6DS3_GYRO_RANGE.getLabels(), 
			LSM6DS3_GYRO_RANGE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3);

	// --------------- Configuration options end ----------------
	
	// ----------------- Calibration Start -----------------------

	public static final double[][] DEFAULT_OFFSET_VECTOR_LSM6DS3 = {{0},{0},{0}};	
	public static final double[][] DEFAULT_ALIGNMENT_MATRIX_LSM6DS3 = {{0,0,1},{-1,0,0},{0,-1,0}};
	
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2G = {{1671.665922915,0,0},{0,1671.665922915,0},{0,0,1671.665922915}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_4G = {{835.832961457,0,0},{0,835.832961457,0},{0,0,835.832961457}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_8G = {{417.916480729,0,0},{0,417.916480729,0},{0,0,417.916480729}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_16G = {{208.958240364,0,0},{0,208.958240364,0},{0,0,208.958240364}};
	
//	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_125DPS = {{228.571428571,0,0},{0,228.571428571,0},{0,0,228.571428571}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_250DPS = {{114.285714286,0,0},{0,114.285714286,0},{0,0,114.285714286}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_500DPS = {{57.142857143,0,0},{0,57.142857143,0},{0,0,57.142857143}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_1000DPS = {{28.571428571,0,0},{0,28.571428571,0},{0,0,28.571428571}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2000DPS = {{14.285714286,0,0},{0,14.285714286,0},{0,0,14.285714286}};

	public CalibDetailsKinematic calibDetailsAccel2g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE.RANGE_2G.configValue,
			LSM6DS3_ACCEL_RANGE.RANGE_2G.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3, 
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsAccel4g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE.RANGE_4G.configValue,
			LSM6DS3_ACCEL_RANGE.RANGE_4G.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_4G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsAccel8g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE.RANGE_8G.configValue,
			LSM6DS3_ACCEL_RANGE.RANGE_8G.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3, 
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_8G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsAccel16g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE.RANGE_16G.configValue,
			LSM6DS3_ACCEL_RANGE.RANGE_16G.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_16G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	
	public CalibDetailsKinematic calibDetailsGyro250dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE.RANGE_250DPS.configValue, 
			LSM6DS3_GYRO_RANGE.RANGE_250DPS.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_250DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsGyro500dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE.RANGE_500DPS.configValue, 
			LSM6DS3_GYRO_RANGE.RANGE_500DPS.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3, 
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_500DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsGyro1000dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE.RANGE_1000DPS.configValue, 
			LSM6DS3_GYRO_RANGE.RANGE_1000DPS.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_1000DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsGyro2000dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE.RANGE_2000DPS.configValue, 
			LSM6DS3_GYRO_RANGE.RANGE_2000DPS.label,
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2000DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccel = calibDetailsAccel2g;
	public CalibDetailsKinematic mCurrentCalibDetailsGyro = calibDetailsGyro250dps;

	// ----------------- Calibration end -----------------------

  	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENSOR_LSM6DS3_ACCEL = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.LSM6DS3_ACCEL,
			Configuration.Verisense.SensorBitmap.LSM6DS3_ACCEL,
			GuiLabelSensors.ACCEL2,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3,
			Arrays.asList(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL),
			Arrays.asList(GuiLabelConfig.LSM6DS3_ACCEL_RANGE,
					GuiLabelConfig.LSM6DS3_GYRO_RANGE,
					GuiLabelConfig.LSM6DS3_RATE),
			Arrays.asList(ObjectClusterSensorName.LSM6DS3_ACC_X,
					ObjectClusterSensorName.LSM6DS3_ACC_Y,
					ObjectClusterSensorName.LSM6DS3_ACC_Z),
			false);

	public static final SensorDetailsRef SENSOR_LSM6DS3_GYRO = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.LSM6DS3_GYRO,
			Configuration.Verisense.SensorBitmap.LSM6DS3_GYRO,
			GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3,
			Arrays.asList(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL),
			Arrays.asList(GuiLabelConfig.LSM6DS3_ACCEL_RANGE,
					GuiLabelConfig.LSM6DS3_GYRO_RANGE,
					GuiLabelConfig.LSM6DS3_RATE),
			Arrays.asList(ObjectClusterSensorName.LSM6DS3_GYRO_X,
					ObjectClusterSensorName.LSM6DS3_GYRO_Y,
					ObjectClusterSensorName.LSM6DS3_GYRO_Z),
			false);
	
	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL, SensorLSM6DS3.SENSOR_LSM6DS3_ACCEL);  
		aMap.put(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO, SensorLSM6DS3.SENSOR_LSM6DS3_GYRO);  
		SENSOR_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
	
	public static final ChannelDetails CHANNEL_LSM6DS3_ACCEL_X = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_ACC_X,
			ObjectClusterSensorName.LSM6DS3_ACC_X,
			DatabaseChannelHandles.LSM6DS3_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_ACCEL_Y = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_ACC_Y,
			ObjectClusterSensorName.LSM6DS3_ACC_Y,
			DatabaseChannelHandles.LSM6DS3_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_ACCEL_Z = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_ACC_Z,
			ObjectClusterSensorName.LSM6DS3_ACC_Z,
			DatabaseChannelHandles.LSM6DS3_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));

	public static final ChannelDetails CHANNEL_LSM6DS3_GYRO_X = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_GYRO_X,
			ObjectClusterSensorName.LSM6DS3_GYRO_X,
			DatabaseChannelHandles.LSM6DS3_GYRO_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_GYRO_Y = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_GYRO_Y,
			ObjectClusterSensorName.LSM6DS3_GYRO_Y,
			DatabaseChannelHandles.LSM6DS3_GYRO_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_GYRO_Z = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_GYRO_Z,
			ObjectClusterSensorName.LSM6DS3_GYRO_Z,
			DatabaseChannelHandles.LSM6DS3_GYRO_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final Map<String, ChannelDetails> CHANNEL_MAP_REF;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_ACC_X, SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_X);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_ACC_Y, SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Y);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_ACC_Z, SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Z);
		
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_X, SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_X);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_Y, SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Y);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_Z, SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Z);

		CHANNEL_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Channel info end --------------

	public SensorLSM6DS3(ShimmerDevice shimmerDevice) {
		super(SENSORS.LSM6DS3, shimmerDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF, CHANNEL_MAP_REF);
	}

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(CONFIG_OPTION_ACCEL_RANGE);
		addConfigOption(CONFIG_OPTION_GYRO_RANGE);
		addConfigOption(CONFIG_OPTION_RATE);
	}

	@Override
	public void generateSensorGroupMapping() {
		
		int groupIndex = Configuration.Verisense.LABEL_SENSOR_TILE.ACCEL2_GYRO.ordinal();
	
		if(mShimmerVerObject.isShimmerGenVerisense()){
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					LABEL_SENSOR_TILE.ACCEL2_GYRO,
					Arrays.asList(
							Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO,
							Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3));
		}
		super.updateSensorGroupingMap();
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		
		// get uncalibrated data for each (sub)sensor
		if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL2) && mCurrentCalibDetailsAccel!=null){
			// ADC values
			objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);

			double[] unCalibratedAccel = new double[3];
			unCalibratedAccel[0] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_X, CHANNEL_TYPE.UNCAL);
			unCalibratedAccel[1] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Y, CHANNEL_TYPE.UNCAL);
			unCalibratedAccel[2] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Z, CHANNEL_TYPE.UNCAL);
			
			//Add default calibrated data to Object cluster
			double[] defaultCalAccel = UtilCalibration.calibrateInertialSensorData(unCalibratedAccel, mCurrentCalibDetailsAccel.getDefaultMatrixMultipliedInverseAMSM(), mCurrentCalibDetailsAccel.getDefaultOffsetVector());
			objectCluster.addCalData(CHANNEL_LSM6DS3_ACCEL_X, defaultCalAccel[0], objectCluster.getIndexKeeper()-3);
			objectCluster.addCalData(CHANNEL_LSM6DS3_ACCEL_Y, defaultCalAccel[1], objectCluster.getIndexKeeper()-2);
			objectCluster.addCalData(CHANNEL_LSM6DS3_ACCEL_Z, defaultCalAccel[2], objectCluster.getIndexKeeper()-1);

			//Add auto-calibrated data to Object cluster - if available
			boolean isCurrentValuesSet = mCurrentCalibDetailsAccel.isCurrentValuesSet();
			if(isCurrentValuesSet) {
				double[] autoCalAccel = UtilCalibration.calibrateImuData(defaultCalAccel, mCurrentCalibDetailsAccel.getCurrentSensitivityMatrix(), mCurrentCalibDetailsAccel.getCurrentOffsetVector());
				//Add calibrated data to Object cluster
				objectCluster.addData(CHANNEL_LSM6DS3_ACCEL_X.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_ACCEL_X.mDefaultCalUnits, autoCalAccel[0], objectCluster.getIndexKeeper()-3, false);
				objectCluster.addData(CHANNEL_LSM6DS3_ACCEL_Y.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_ACCEL_Y.mDefaultCalUnits, autoCalAccel[1], objectCluster.getIndexKeeper()-2, false);
				objectCluster.addData(CHANNEL_LSM6DS3_ACCEL_Z.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_ACCEL_Z.mDefaultCalUnits, autoCalAccel[2], objectCluster.getIndexKeeper()-1, false);
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.GYRO) && mCurrentCalibDetailsGyro!=null){
			// ADC values
			objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);

			double[] unCalibratedGyro = new double[3];
			unCalibratedGyro[0] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_X, CHANNEL_TYPE.UNCAL);
			unCalibratedGyro[1] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Y, CHANNEL_TYPE.UNCAL);
			unCalibratedGyro[2] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Z, CHANNEL_TYPE.UNCAL);

			//Add default calibrated data to Object cluster
			double[] defaultCalGyro = UtilCalibration.calibrateInertialSensorData(unCalibratedGyro, mCurrentCalibDetailsGyro.getDefaultMatrixMultipliedInverseAMSM(), mCurrentCalibDetailsGyro.getDefaultOffsetVector());
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_X.mObjectClusterName, CHANNEL_TYPE.CAL, CHANNEL_LSM6DS3_GYRO_X.mDefaultCalUnits, defaultCalGyro[0], objectCluster.getIndexKeeper()-3, true);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Y.mObjectClusterName, CHANNEL_TYPE.CAL, CHANNEL_LSM6DS3_GYRO_Y.mDefaultCalUnits, defaultCalGyro[1], objectCluster.getIndexKeeper()-2, true);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Z.mObjectClusterName, CHANNEL_TYPE.CAL, CHANNEL_LSM6DS3_GYRO_Z.mDefaultCalUnits, defaultCalGyro[2], objectCluster.getIndexKeeper()-1, true);
			
			//Add auto-calibrated data to Object cluster
			//TODO use below if calibration CSVs contain valid Alignment values
//			double[] autoCalGyro = UtilCalibration.calibrateInertialSensorData(unCalibratedGyro, mCurrentCalibDetailsGyro);
			//TODO use below if calibration CSVs contains an identity matrix
			double[] autoCalGyro = UtilCalibration.calibrateInertialSensorData(unCalibratedGyro, mCurrentCalibDetailsGyro.getDefaultAlignmentMatrix(), mCurrentCalibDetailsGyro.getValidSensitivityMatrix(), mCurrentCalibDetailsGyro.getValidOffsetVector());
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_X.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_GYRO_X.mDefaultCalUnits, autoCalGyro[0], objectCluster.getIndexKeeper()-3, false);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Y.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_GYRO_Y.mDefaultCalUnits, autoCalGyro[1], objectCluster.getIndexKeeper()-2, false);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Z.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_GYRO_Z.mDefaultCalUnits, autoCalGyro[2], objectCluster.getIndexKeeper()-1, false);
		}
		return objectCluster;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
//		if(isAnySensorChannelEnabled(VerisenseDevice.defaultCommType)) {
		if(isEitherLsm6ds3ChannelEnabled()) {
			ConfigByteLayoutLsm6ds3 cbl = new ConfigByteLayoutLsm6ds3(shimmerDevice, commType);

			//TODO decide where best to put this (i.e., when sampling rate & enabled sensors are changed?)
			updateFifoSizeInChip();

			int fifoSizeInChip = getFifoSizeInChip();
			configBytes[cbl.idxGyroAccel2Cfg0] = (byte) (fifoSizeInChip & cbl.maskFifoThresholdLsb);

			if (commType == COMMUNICATION_TYPE.SD) {
				configBytes[cbl.idxFsAccel2] &= ~(cbl.maskFs<<cbl.bitShiftFsAccel2);
				configBytes[cbl.idxFsAccel2] |= (getAccelRangeConfigValue()&cbl.maskFs)<<cbl.bitShiftFsAccel2;
				
				configBytes[cbl.idxGyroAccel2Cfg1] &= ~cbl.maskFifoThresholdMsb;
				configBytes[cbl.idxGyroAccel2Cfg1] |= (byte) (((byte) (fifoSizeInChip >> 8)) & cbl.maskFifoThresholdMsb);

				configBytes[cbl.idxGyroAccel2Cfg5] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg5] |= (getRateConfigValue()&cbl.maskOdr)<<cbl.bitShiftOdrAccelGyro;
				configBytes[cbl.idxGyroAccel2Cfg5] |= (getGyroRangeConfigValue()&cbl.maskFs)<<cbl.bitShiftGyroFs;
			} else {
				configBytes[cbl.idxGyroAccel2Cfg1] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg1] |= ((isTimerPedoFifodEnable()? 0x01:0x00) & cbl.maskTimerPedoFifD0Enable) << cbl.bitShiftTimerPedoFifD0Enable; 
				configBytes[cbl.idxGyroAccel2Cfg1] |= ((isTimerPedoFifodDrdy()? 0x01:0x00) & cbl.maskTimerPedoFifoDrdy) << cbl.bitShiftTimerPedoFifoDrdy; 
				configBytes[cbl.idxGyroAccel2Cfg1] |= (byte) (((byte) (fifoSizeInChip >> 8)) & cbl.maskFifoThresholdMsb);

				configBytes[cbl.idxGyroAccel2Cfg2] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg2] |= (getDecimationFifoAccel().configValue & cbl.maskDecimationFifoAccel) << cbl.bitShiftDecimationFifoAccel; 
				configBytes[cbl.idxGyroAccel2Cfg2] |= (getDecimationFifoGyro().configValue & cbl.maskDecimationFifoGyro) << cbl.bitShiftDecimationFifoGyro; 

				configBytes[cbl.idxGyroAccel2Cfg3] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg3] |= (getRateConfigValue()&cbl.maskOdr)<<cbl.bitShiftOdrFifo;
				configBytes[cbl.idxGyroAccel2Cfg3] |= (getFifoMode().configValue&cbl.maskFifoMode)<<cbl.bitShiftFifoMode;
				
				configBytes[cbl.idxGyroAccel2Cfg4] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg4] |= (getRateConfigValue()&cbl.maskOdr)<<cbl.bitShiftOdrAccelGyro;
				configBytes[cbl.idxGyroAccel2Cfg4] |= (getAccelRangeConfigValue()&cbl.maskFs)<<cbl.bitShiftFsAccel2;
				configBytes[cbl.idxGyroAccel2Cfg4] |= (getAccelAntiAliasingBandwidthFilter().configValue&cbl.maskBwAccel)<<cbl.bitShiftBwAccel;

				configBytes[cbl.idxGyroAccel2Cfg5] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg5] |= (getRateConfigValue()&cbl.maskOdr)<<cbl.bitShiftOdrAccelGyro;
				configBytes[cbl.idxGyroAccel2Cfg5] |= (getGyroRangeConfigValue()&cbl.maskFs)<<cbl.bitShiftGyroFs;
				configBytes[cbl.idxGyroAccel2Cfg5] |= ((isGyroFullScaleAt12dps() ? 0x01 : 0x00) & cbl.maskFs125) << cbl.bitShiftFs125;

				configBytes[cbl.idxGyroAccel2Cfg6] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg6] |= ((isGyroHighPerformanceMode() ? 0x01 : 0x00) & cbl.maskGHmMode) << cbl.bitShiftGHmMode;
				configBytes[cbl.idxGyroAccel2Cfg6] |= ((isGyroDigitalHighPassFilterEnable() ? 0x01 : 0x00) & cbl.maskHpGEnable) << cbl.bitShiftHpGEnable;
				configBytes[cbl.idxGyroAccel2Cfg6] |= (getGyroHighPassFilterCutOffFreq().configValue & cbl.maskHpcfG) << cbl.bitShiftHpcfG;
				configBytes[cbl.idxGyroAccel2Cfg6] |= ((isGyroDigitalHpFilterReset() ? 0x01 : 0x00) & cbl.maskHpGRst) << cbl.bitShiftHpGRst;
				configBytes[cbl.idxGyroAccel2Cfg6] |= ((isRoundingStatus() ? 0x01 : 0x00) & cbl.maskRoundingStatus) << cbl.bitShiftRoundingStatus;
				
				configBytes[cbl.idxGyroAccel2Cfg7] = 0x00;
				configBytes[cbl.idxGyroAccel2Cfg7] |= ((isAccelLowPassFilterLpf2Selection() ? 0x01 : 0x00) & cbl.maskLpf2AccelEnable) << cbl.bitShiftLpf2AccelEnable;
				configBytes[cbl.idxGyroAccel2Cfg7] |= (getAccelHighPassFilterCutOffFreq().configValue & cbl.maskHpcfAccel) << cbl.bitShiftHpcfAccel;
				configBytes[cbl.idxGyroAccel2Cfg7] |= ((isAccelHighPassOrSlopeFilterSelectionEnable() ? 0x01 : 0x00) & cbl.maskHpSlopeAccelEnable) << cbl.bitShiftHpSlopeAccelEnable;
				configBytes[cbl.idxGyroAccel2Cfg7] |= ((isLowPassOn6D() ? 0x01 : 0x00) & cbl.maskLowPassOn6D) << cbl.bitShiftLowPassOn6D;
			}
		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
//		if(isAnySensorChannelEnabled(VerisenseDevice.defaultCommType)) {
		if(isEitherLsm6ds3ChannelEnabled()) {
			ConfigByteLayoutLsm6ds3 cbl = new ConfigByteLayoutLsm6ds3(shimmerDevice, commType);

			int fifoSizeInChip = (configBytes[cbl.idxGyroAccel2Cfg0]&cbl.maskFifoThresholdLsb) 
					| ((configBytes[cbl.idxGyroAccel2Cfg1]&cbl.maskFifoThresholdMsb)<<8);
			setFifoSizeInChip(fifoSizeInChip);

			if (commType == COMMUNICATION_TYPE.SD) {
				setAccelRangeConfigValue((configBytes[cbl.idxFsAccel2]>>cbl.bitShiftFsAccel2)&cbl.maskFs);
	
				setRateConfigValue((configBytes[cbl.idxGyroAccel2Cfg5]>>cbl.bitShiftOdrAccelGyro)&cbl.maskOdr);
				setGyroRangeConfigValue((configBytes[cbl.idxGyroAccel2Cfg5]>>cbl.bitShiftGyroFs)&cbl.maskFs);
			} else {
				setTimerPedoFifodEnable(((configBytes[cbl.idxGyroAccel2Cfg1] >> cbl.bitShiftTimerPedoFifD0Enable) & cbl.maskTimerPedoFifD0Enable) == cbl.maskTimerPedoFifD0Enable);
				setTimerPedoFifodDrdy(((configBytes[cbl.idxGyroAccel2Cfg1] >> cbl.bitShiftTimerPedoFifoDrdy) & cbl.maskTimerPedoFifoDrdy) == cbl.maskTimerPedoFifoDrdy);

				setDecimationFifoAccel(FIFO_DECIMATION_ACCEL.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg2] >> cbl.bitShiftDecimationFifoAccel) & cbl.maskDecimationFifoAccel));
				setDecimationFifoGyro(FIFO_DECIMATION_GYRO.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg2] >> cbl.bitShiftDecimationFifoGyro) & cbl.maskDecimationFifoGyro));

				setRate(LSM6DS3_RATE.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg3] >> cbl.bitShiftOdrFifo) & cbl.maskOdr));
				setFifoMode(FIFO_MODE.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg3] >> cbl.bitShiftFifoMode) & cbl.maskFifoMode));

				setAccelRange(LSM6DS3_ACCEL_RANGE.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg4]>>cbl.bitShiftFsAccel2)&cbl.maskFs));
				setAccelAntiAliasingBandwidthFilter(ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg4]>>cbl.bitShiftBwAccel)&cbl.maskBwAccel));

				setGyroRange(LSM6DS3_GYRO_RANGE.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg5]>>cbl.bitShiftGyroFs)&cbl.maskFs));
				setGyroFullScaleAt12dps(((configBytes[cbl.idxGyroAccel2Cfg5] >> cbl.bitShiftFs125) & cbl.maskFs125) == cbl.maskFs125);

				setGyroHighPerformanceMode(((configBytes[cbl.idxGyroAccel2Cfg6] >> cbl.bitShiftGHmMode) & cbl.maskGHmMode) == cbl.maskGHmMode);
				setGyroDigitalHighPassFilterEnable(((configBytes[cbl.idxGyroAccel2Cfg6] >> cbl.bitShiftHpGEnable) & cbl.maskHpGEnable) == cbl.maskHpGEnable);
				setGyroHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO.getForConfigValue((configBytes[cbl.idxGyroAccel2Cfg6] >> cbl.bitShiftHpcfG) & cbl.maskHpcfG));
				setGyroDigitalHighPassFilterReset(((configBytes[cbl.idxGyroAccel2Cfg6] >> cbl.bitShiftHpGRst) & cbl.maskHpGRst) == cbl.maskHpGRst);
				setRoundingStatus(((configBytes[cbl.idxGyroAccel2Cfg6] >> cbl.bitShiftRoundingStatus) & cbl.maskRoundingStatus) == cbl.maskRoundingStatus);
				
				setAccelLowPassFilterLpf2Selection(((configBytes[cbl.idxGyroAccel2Cfg7] >> cbl.bitShiftLpf2AccelEnable) & cbl.maskLpf2AccelEnable) == cbl.maskLpf2AccelEnable);
				setAccelHighPassFilterCutOffFreqFromConfigValue((configBytes[cbl.idxGyroAccel2Cfg7] >> cbl.bitShiftHpcfAccel) & cbl.maskHpcfAccel);
				setAccelHighPassOrSlopeFilterSelectionEnable(((configBytes[cbl.idxGyroAccel2Cfg7] >> cbl.bitShiftHpSlopeAccelEnable) & cbl.maskHpSlopeAccelEnable) == cbl.maskHpSlopeAccelEnable);
				setLowPassOn6D(((configBytes[cbl.idxGyroAccel2Cfg7] >> cbl.bitShiftLowPassOn6D) & cbl.maskLowPassOn6D) == cbl.maskLowPassOn6D);
			}
		}
	}

	public void setFifoSizeInChip(int fifoSizeInChip) {
		// FIFO size was added to the payload config midway through development (FW v1.02.005?) so need to check for 0 
		if(fifoSizeInChip==0) {
			this.fifoSizeInChip = DEFAULT_FIFO_BYTE_SIZE_IN_CHIP/2;
		} else {
			this.fifoSizeInChip = fifoSizeInChip;
		}
	}

	public int getFifoSizeInChip() {
		return fifoSizeInChip;
	}

	public int getFifoByteSizeInChip() {
		return getFifoSizeInChip()*2;
	}

	/** 
	 * NOTE: Only appropriate for Gen 1 to 7 of the Payload Design
	 * 
	 * @param memAvailable
	 * @return
	 */
	public int calculateMaxPayloadsInFifo(int memAvailable) {
		int maxFifosInPayload = DEFAULT_MAX_FIFOS_IN_PAYLOAD;
		if(mShimmerDevice instanceof VerisenseDevice) {
			maxFifosInPayload = (int) Math.floor(memAvailable/(AsmBinaryFileConstants.ACCEL_SPI_BUS_HEADER_BYTES+getFifoByteSizeInChip()));
		}
		return maxFifosInPayload;
	}
	
	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LSM6DS3_RATE):
				setRateConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM6DS3_ACCEL_RANGE):
				setAccelRangeConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM6DS3_GYRO_RANGE):
				setGyroRangeConfigValue((int)valueToSet);
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;

		switch(configLabel){
			case(GuiLabelConfig.LSM6DS3_RATE):
				returnValue = getRateConfigValue();
				break;
			case(GuiLabelConfig.LSM6DS3_ACCEL_RANGE):
				returnValue = getAccelRangeConfigValue();
				break;
			case(GuiLabelConfig.LSM6DS3_GYRO_RANGE):
				returnValue = getGyroRangeConfigValue();
				break;
			case(GuiLabelConfigCommon.CALIBRATION_CURRENT_PER_SENSOR):
				if(sensorId==Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO) {
					returnValue = mCurrentCalibDetailsGyro;
				} else if(sensorId==Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL) {
					returnValue = mCurrentCalibDetailsAccel;
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				returnValue = getRateFreq();
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		if (samplingRateHz==0){
			setRate(LSM6DS3_RATE.POWER_DOWN);
		} else {
			for(LSM6DS3_RATE rate : LSM6DS3_RATE.values()) {
				if(rate==LSM6DS3_RATE.POWER_DOWN) {
					continue;
				}
				if(samplingRateHz<=rate.freqHz) {
					setRate(rate);
					break;
				}
			}
		}
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			//TODO handle isSensorEnabled = true and false

			setGyroRange(LSM6DS3_GYRO_RANGE.RANGE_500DPS);
			setAccelRange(LSM6DS3_ACCEL_RANGE.RANGE_4G);
			setRate(LSM6DS3_RATE.RATE_52_HZ);
			
			setTimerPedoFifodEnable(false);
			setTimerPedoFifodDrdy(false);
			setDecimationFifoAccel(FIFO_DECIMATION_ACCEL.NO_DECIMATION);
			setDecimationFifoGyro(FIFO_DECIMATION_GYRO.NO_DECIMATION);
			setFifoMode(FIFO_MODE.CONTINUOUS_MODE);
			setAccelAntiAliasingBandwidthFilter(ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER.AT_400HZ);
			setGyroFullScaleAt12dps(false);
			setGyroHighPerformanceMode(false);
			setGyroDigitalHighPassFilterEnable(false);
			setGyroHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO.AT_0_0081_HZ);
			setGyroDigitalHighPassFilterReset(false);
			setRoundingStatus(false);
			setAccelLowPassFilterLpf2Selection(false);
			setAccelHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.SLOPE);
			setAccelHighPassOrSlopeFilterSelectionEnable(false);
			setLowPassOn6D(false);

			updateFifoSizeInChip();
			
			return true;
		}
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
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void initialise() {
		super.initialise();
		
		updateCurrentAccelCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccel = new TreeMap<Integer, CalibDetails>();
		calibMapAccel.put(calibDetailsAccel2g.mRangeValue, calibDetailsAccel2g);
		calibMapAccel.put(calibDetailsAccel4g.mRangeValue, calibDetailsAccel4g);
		calibMapAccel.put(calibDetailsAccel8g.mRangeValue, calibDetailsAccel8g);
		calibMapAccel.put(calibDetailsAccel16g.mRangeValue, calibDetailsAccel16g);
		setCalibrationMapPerSensor(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL, calibMapAccel);
		updateCurrentAccelCalibInUse();
		
		TreeMap<Integer, CalibDetails> calibMapGyro = new TreeMap<Integer, CalibDetails>();
		calibMapGyro.put(calibDetailsGyro250dps.mRangeValue, calibDetailsGyro250dps);
		calibMapGyro.put(calibDetailsGyro500dps.mRangeValue, calibDetailsGyro500dps);
		calibMapGyro.put(calibDetailsGyro1000dps.mRangeValue, calibDetailsGyro1000dps);
		calibMapGyro.put(calibDetailsGyro2000dps.mRangeValue, calibDetailsGyro2000dps);
		setCalibrationMapPerSensor(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO, calibMapGyro);
		updateCurrentGyroCalibInUse();
	}
	
	@Override
	public void setSensorConfig(ISensorConfig sensorConfig) {
		if(sensorConfig instanceof LSM6DS3_RATE) {
			setRate((LSM6DS3_RATE)sensorConfig);
		} else if(sensorConfig instanceof LSM6DS3_ACCEL_RANGE) {
			setAccelRange((LSM6DS3_ACCEL_RANGE)sensorConfig);
		} else if (sensorConfig instanceof LSM6DS3_GYRO_RANGE) {
			setGyroRange((LSM6DS3_GYRO_RANGE)sensorConfig);
		} else if (sensorConfig instanceof FIFO_DECIMATION_GYRO) {
			setDecimationFifoGyro((FIFO_DECIMATION_GYRO)sensorConfig);
		} else if (sensorConfig instanceof FIFO_DECIMATION_ACCEL) {
			setDecimationFifoAccel((FIFO_DECIMATION_ACCEL)sensorConfig);
		} else if (sensorConfig instanceof FIFO_MODE) {
			setFifoMode((FIFO_MODE)sensorConfig);
		} else if (sensorConfig instanceof ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER) {
			setAccelAntiAliasingBandwidthFilter((ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER)sensorConfig);
		} else if (sensorConfig instanceof HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO) {
			setGyroHighPassFilterCutOffFreq((HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO)sensorConfig);
		} else {
			super.setSensorConfig(sensorConfig);
		}
	}

	@Override
	public List<ISensorConfig> getSensorConfig() {
		List<ISensorConfig> listOfSensorConfig = super.getSensorConfig();
		listOfSensorConfig.add(getRate());
		listOfSensorConfig.add(getAccelRange());
		listOfSensorConfig.add(getGyroRange());
		listOfSensorConfig.add(getDecimationFifoAccel());
		listOfSensorConfig.add(getDecimationFifoGyro());
		listOfSensorConfig.add(getFifoMode());
		listOfSensorConfig.add(getAccelAntiAliasingBandwidthFilter());
		listOfSensorConfig.add(getGyroHighPassFilterCutOffFreq());
		return listOfSensorConfig;
	}

	public void updateCurrentAccelCalibInUse(){
		mCurrentCalibDetailsAccel = getCurrentCalibDetailsIfKinematic(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL, getAccelRangeConfigValue());
	}
	
	public void updateCurrentGyroCalibInUse(){
		mCurrentCalibDetailsGyro = getCurrentCalibDetailsIfKinematic(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO, getGyroRangeConfigValue());
	}

	public LSM6DS3_ACCEL_RANGE getAccelRange() {
		return rangeAccel;
	}

	public int getAccelRangeConfigValue() {
		return rangeAccel.configValue;
	}
	
	public void setAccelRangeConfigValue(int valueToSet){
		setAccelRange(LSM6DS3_ACCEL_RANGE.getForConfigValue(valueToSet));
	}

	public void setAccelRange(LSM6DS3_ACCEL_RANGE valueToSet) {
		rangeAccel = valueToSet;
		updateCurrentAccelCalibInUse();
	}

	public int getGyroRangeConfigValue() {
		return rangeGyro.configValue;
	}

	public LSM6DS3_GYRO_RANGE getGyroRange() {
		return rangeGyro;
	}

	public void setGyroRangeConfigValue(int valueToSet){
		setGyroRange(LSM6DS3_GYRO_RANGE.getForConfigValue(valueToSet));
	}

	public void setGyroRange(LSM6DS3_GYRO_RANGE valueToSet){
		rangeGyro = valueToSet;
		updateCurrentGyroCalibInUse();
	}

	public LSM6DS3_RATE getRate() {
		return rate;
	}

	public int getRateConfigValue() {
		return rate.configValue;
	}

	public double getRateFreq() {
		return getRate().freqHz;
	}

	public void setRateConfigValue(int valueToSet) {
		setRate(LSM6DS3_RATE.getForConfigValue(valueToSet));
	}

	public void setRate(LSM6DS3_RATE valueToSet) {
		rate = valueToSet;
	}

	public boolean isTimerPedoFifodEnable() {
		return timerPedoFifodEnable;
	}

	public void setTimerPedoFifodEnable(boolean timerPedoFifodEnable) {
		this.timerPedoFifodEnable = timerPedoFifodEnable;
	}

	public boolean isTimerPedoFifodDrdy() {
		return timerPedoFifodDrdy;
	}

	public void setTimerPedoFifodDrdy(boolean timerPedoFifodDrdy) {
		this.timerPedoFifodDrdy = timerPedoFifodDrdy;
	}

	public FIFO_DECIMATION_GYRO getDecimationFifoGyro() {
		return decimationFifoGyro;
	}

	public void setDecimationFifoGyro(FIFO_DECIMATION_GYRO decimationFifoGyro) {
		this.decimationFifoGyro = decimationFifoGyro;
	}

	public FIFO_DECIMATION_ACCEL getDecimationFifoAccel() {
		return decimationFifoAccel;
	}

	public void setDecimationFifoAccel(FIFO_DECIMATION_ACCEL decimationFifoAccel) {
		this.decimationFifoAccel = decimationFifoAccel;
	}

	public FIFO_MODE getFifoMode() {
		return fifoMode;
	}

	public void setFifoMode(FIFO_MODE fifoMode) {
		this.fifoMode = fifoMode;
	}

	public LSM6DS3_ACCEL_RANGE getRangeAccel() {
		return rangeAccel;
	}

	public void setRangeAccel(LSM6DS3_ACCEL_RANGE rangeAccel) {
		this.rangeAccel = rangeAccel;
	}

	public ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER getAccelAntiAliasingBandwidthFilter() {
		return accelAntiAliasingBandwidthFilter;
	}

	public void setAccelAntiAliasingBandwidthFilter(ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER accelAntiAliasingBandwidthFilter) {
		this.accelAntiAliasingBandwidthFilter = accelAntiAliasingBandwidthFilter;
	}

	public LSM6DS3_GYRO_RANGE getRangeGyro() {
		return rangeGyro;
	}

	public void setRangeGyro(LSM6DS3_GYRO_RANGE rangeGyro) {
		this.rangeGyro = rangeGyro;
	}

	public boolean isGyroFullScaleAt12dps() {
		return gyroFullScaleAt12dps;
	}

	public void setGyroFullScaleAt12dps(boolean gyroFullScaleAt12dps) {
		this.gyroFullScaleAt12dps = gyroFullScaleAt12dps;
	}

	public boolean isGyroHighPerformanceMode() {
		return gyroHighPerFormanceModeDisable;
	}

	public void setGyroHighPerformanceMode(boolean gyroHighPerFormanceModeDisable) {
		this.gyroHighPerFormanceModeDisable = gyroHighPerFormanceModeDisable;
	}

	public boolean isGyroDigitalHighPassFilterEnable() {
		return gyroDigitalHighPassFilterEnable;
	}

	public void setGyroDigitalHighPassFilterEnable(boolean gyroDigitalHighPassFilterEnable) {
		this.gyroDigitalHighPassFilterEnable = gyroDigitalHighPassFilterEnable;
	}

	public HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO getGyroHighPassFilterCutOffFreq() {
		return gyroHighPassFilterCutOffFreq;
	}

	public void setGyroHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO gyroHighPassFilterCutOffFreq) {
		this.gyroHighPassFilterCutOffFreq = gyroHighPassFilterCutOffFreq;
	}

	public boolean isGyroDigitalHpFilterReset() {
		return gyroDigitalHighPassFilterReset;
	}

	public void setGyroDigitalHighPassFilterReset(boolean gyroDigitalHighPassFilterReset) {
		this.gyroDigitalHighPassFilterReset = gyroDigitalHighPassFilterReset;
	}

	public boolean isRoundingStatus() {
		return roundingStatus;
	}

	public void setRoundingStatus(boolean roundingStatus) {
		this.roundingStatus = roundingStatus;
	}

	public boolean isAccelLowPassFilterLpf2Selection() {
		return accelLowPassFilterLpf2Selection;
	}

	public void setAccelLowPassFilterLpf2Selection(boolean accelLowPassFilterLpf2Selection) {
		this.accelLowPassFilterLpf2Selection = accelLowPassFilterLpf2Selection;
		updateAccelHighPassFilterCutOffFreq();
	}

	public HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL getAccelHighPassFilterCutOffFreq() {
		return accelHighPassFilterCutOffFreq;
	}

	public void setAccelHighPassFilterCutOffFreq(HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL accelHighPassFilterCutOffFreq) {
		this.accelHighPassFilterCutOffFreq = accelHighPassFilterCutOffFreq;
		updateLowPassFilterLpf2Selection();
	}

	private void setAccelHighPassFilterCutOffFreqFromConfigValue(int configValue) {
		this.accelHighPassFilterCutOffFreq = HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.getForConfigValue(configValue, isAccelLowPassFilterLpf2Selection());
	}

	private void updateLowPassFilterLpf2Selection() {
		this.accelLowPassFilterLpf2Selection = (accelHighPassFilterCutOffFreq==HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.LOW_PASS_RATE_DIVIDED_BY_50
				|| accelHighPassFilterCutOffFreq==HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.LOW_PASS_RATE_DIVIDED_BY_100
				|| accelHighPassFilterCutOffFreq==HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.LOW_PASS_RATE_DIVIDED_BY_9
				|| accelHighPassFilterCutOffFreq==HIGH_PASS_FILTER_CUT_OFF_FREQ_ACCEL.LOW_PASS_RATE_DIVIDED_BY_400)? true:false;
	}

	private void updateAccelHighPassFilterCutOffFreq() {
		setAccelHighPassFilterCutOffFreqFromConfigValue(getAccelHighPassFilterCutOffFreq().configValue);
	}

	public boolean isAccelHighPassOrSlopeFilterSelectionEnable() {
		return accelHighPassOrSlopeFilterSelectionEnable;
	}

	public void setAccelHighPassOrSlopeFilterSelectionEnable(boolean hpSlopeAccelEnable) {
		this.accelHighPassOrSlopeFilterSelectionEnable = hpSlopeAccelEnable;
	}

	public boolean isLowPassOn6D() {
		return lowPassFilterOn6D;
	}

	public void setLowPassOn6D(boolean lowPassOn6D) {
		this.lowPassFilterOn6D = lowPassOn6D;
	}

	public CalibDetailsKinematic getmCurrentCalibDetailsAccel() {
		return mCurrentCalibDetailsAccel;
	}

	public void setmCurrentCalibDetailsAccel(CalibDetailsKinematic mCurrentCalibDetailsAccel) {
		this.mCurrentCalibDetailsAccel = mCurrentCalibDetailsAccel;
	}

	public CalibDetailsKinematic getmCurrentCalibDetailsGyro() {
		return mCurrentCalibDetailsGyro;
	}

	public void setmCurrentCalibDetailsGyro(CalibDetailsKinematic mCurrentCalibDetailsGyro) {
		this.mCurrentCalibDetailsGyro = mCurrentCalibDetailsGyro;
	}

	public static double calibrateTemperature(long temperatureUncal) {
		double temp = ((temperatureUncal>>4)/16.0) + 25.0;
		return temp;
	}

	public boolean isEitherLsm6ds3ChannelEnabled() {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO)) {
			return true;
		}
		return false;
	}
	
	private class ConfigByteLayoutLsm6ds3 {
		public int idxGyroAccel2Cfg0 = -1, idxGyroAccel2Cfg1 = -1, idxGyroAccel2Cfg2 = -1, idxGyroAccel2Cfg3 = -1, idxGyroAccel2Cfg4 = -1, idxGyroAccel2Cfg5 = -1, idxGyroAccel2Cfg6 = -1, idxGyroAccel2Cfg7 = -1;
		public int idxFsAccel2 = -1, bitShiftFsAccel2 = -1;
		public int maskFs = 0x03, bitShiftGyroFs = 2;
		public int maskOdr = 0x0F, bitShiftOdrAccelGyro = 4;
		public int maskFifoThresholdLsb = 0xFF, maskFifoThresholdMsb = 0x0F;
		
		public int maskTimerPedoFifD0Enable = 0x01, bitShiftTimerPedoFifD0Enable = 7;
		public int maskTimerPedoFifoDrdy = 0x01, bitShiftTimerPedoFifoDrdy = 6;
		public int maskDecimationFifoGyro = 0x07, bitShiftDecimationFifoGyro = 3;
		public int maskDecimationFifoAccel = 0x07, bitShiftDecimationFifoAccel = 0;
		public int bitShiftOdrFifo = 3;
		public int maskFifoMode = 0x07, bitShiftFifoMode = 0;
		public int maskBwAccel = 0x03, bitShiftBwAccel = 0;
		public int maskFs125 = 0x01, bitShiftFs125 = 1;

		public int maskGHmMode = 0x01, bitShiftGHmMode = 7;
		public int maskHpGEnable = 0x01, bitShiftHpGEnable = 6;
		public int maskHpcfG = 0x03, bitShiftHpcfG = 4;
		public int maskHpGRst = 0x01, bitShiftHpGRst = 3;
		public int maskRoundingStatus = 0x01, bitShiftRoundingStatus = 2;

		public int maskLpf2AccelEnable = 0x01, bitShiftLpf2AccelEnable = 7;
		public int maskHpcfAccel = 0x03, bitShiftHpcfAccel = 5;
		public int maskHpSlopeAccelEnable = 0x01, bitShiftHpSlopeAccelEnable = 2;
		public int maskLowPassOn6D = 0x01, bitShiftLowPassOn6D = 0;

		public ConfigByteLayoutLsm6ds3(ShimmerDevice shimmerDevice, COMMUNICATION_TYPE commType) {
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(commType==COMMUNICATION_TYPE.SD) {
					if(verisenseDevice.isPayloadDesignV8orAbove()) {
						idxGyroAccel2Cfg5 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
						idxGyroAccel2Cfg0 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4;
						idxGyroAccel2Cfg1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5;
						
						idxFsAccel2 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
						bitShiftFsAccel2 = 0;
					} else if(verisenseDevice.isPayloadDesignV5orAbove()) {
						idxGyroAccel2Cfg5 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
						idxGyroAccel2Cfg0 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
						idxGyroAccel2Cfg1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4;
						
						idxFsAccel2 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0;
						bitShiftFsAccel2 = 2;
					} else {
						idxGyroAccel2Cfg5 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
						idxGyroAccel2Cfg0 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG2;
						idxGyroAccel2Cfg1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
						
						idxFsAccel2 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0;
						bitShiftFsAccel2 = 2;
					}
				} else {
					idxGyroAccel2Cfg0 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_0;
					idxGyroAccel2Cfg1 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_1;
					idxGyroAccel2Cfg2 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_2;
					idxGyroAccel2Cfg3 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_3;
					idxGyroAccel2Cfg4 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_4;
					idxGyroAccel2Cfg5 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_5;
					idxGyroAccel2Cfg6 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_6;
					idxGyroAccel2Cfg7 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_7;
					
					idxFsAccel2 = OP_CONFIG_BYTE_INDEX.GYRO_ACCEL2_CFG_4;
					bitShiftFsAccel2 = 2;
				}
			}
		}
	}

	public String getAccelRangeString() {
		int accelRangeConfigValue = getAccelRangeConfigValue();
		String accelRange = SensorLSM6DS3.CONFIG_OPTION_ACCEL_RANGE.getConfigStringFromConfigValue(accelRangeConfigValue);
		accelRange = accelRange.replaceAll(UtilShimmer.UNICODE_PLUS_MINUS, "+-");
		
		if(mShimmerDevice instanceof VerisenseDevice && !((VerisenseDevice)mShimmerDevice).isCsvHeaderDesignAzMarkingPoint()) {
			accelRange = accelRange.replaceAll(CHANNEL_UNITS.GRAVITY, (" " + CHANNEL_UNITS.GRAVITY));
		}

		return accelRange;
	}

	public String getGyroRangeString() {
		int gyroRangeConfigValue = getGyroRangeConfigValue();
		String gyroRange = SensorLSM6DS3.CONFIG_OPTION_GYRO_RANGE.getConfigStringFromConfigValue(gyroRangeConfigValue);
		gyroRange = gyroRange.replaceAll(UtilShimmer.UNICODE_PLUS_MINUS, "+-");
		
		if(mShimmerDevice instanceof VerisenseDevice && !((VerisenseDevice)mShimmerDevice).isCsvHeaderDesignAzMarkingPoint()) {
			gyroRange = gyroRange.replaceAll("dps", " dps");
		}

		return gyroRange;
	}
	
	public void updateFifoSizeInChip() {
		setFifoSizeInChip(calculateFifoThreshold());
	}

	/**
	 * This function calculates the appropriate FIFO size based on the number of channels enabled and the sampling rate. The size of the FIFO needs to be reduced at higher sampling rates so that the microcontroller reads from the chip more often and in shorter bursts so that we don't miss any samples that might be recorded during an SPI read operation. Values of FTH have been decided upon experiementally by measuring the time it takes to read the FIFO and the restriction that imposes on the max sampling rate. 
	 * @return
	 */
	public int calculateFifoThreshold() {
		if (!isEitherLsm6ds3ChannelEnabled()) {
			return 0;
		}

		int fth = 0;
		// 0001	ODR is set to 12.5Hz
		// 0010	ODR is set to 26 Hz
		// 0011	ODR is set to 52 Hz
		LSM6DS3_RATE rate = getRate();
		if (rate==LSM6DS3_RATE.RATE_12_5_HZ || rate==LSM6DS3_RATE.RATE_26_HZ || rate==LSM6DS3_RATE.RATE_52_HZ) {
			fth = 4056;
		// 0100	ODR is set to 104 Hz
		} else if (rate==LSM6DS3_RATE.RATE_104_HZ) {
			fth = 2028;
		// 0101	ODR is set to 208 Hz
		} else if (rate==LSM6DS3_RATE.RATE_208_HZ) {
			fth = 1014;
		// 0110	ODR is set to 416 Hz
		} else if (rate==LSM6DS3_RATE.RATE_416_HZ) {
			fth = 540;
		// 0111	ODR is set to 833 Hz
		} else if (rate==LSM6DS3_RATE.RATE_833_HZ) {
			fth = 288;
		// 1000	ODR is set to 1.66 kHz
		} else if (rate==LSM6DS3_RATE.RATE_1666_HZ) {
			fth = 150;
		}

		if ((isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL) && isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO)) 
			|| (rate==LSM6DS3_RATE.RATE_12_5_HZ || rate==LSM6DS3_RATE.RATE_26_HZ || rate==LSM6DS3_RATE.RATE_52_HZ)) {
		} else {
			fth = fth*2;
		}

		return fth;
	}

}
