package com.shimmerresearch.verisense;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.algorithms.verisense.gyroAutoCal.GyroOnTheFlyCalModuleVerisense;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.SensorADC;
import com.shimmerresearch.sensors.AbstractSensor.GuiLabelConfigCommon;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.communication.AbstractPayload;
import com.shimmerresearch.verisense.communication.OpConfigPayload;
import com.shimmerresearch.verisense.communication.OpConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.communication.ProdConfigPayload;
import com.shimmerresearch.verisense.communication.StatusPayload;
import com.shimmerresearch.verisense.communication.TimePayload;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;
import com.shimmerresearch.verisense.payloaddesign.PayloadContentsDetails;
import com.shimmerresearch.verisense.payloaddesign.VerisenseTimeDetails;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.BYTE_COUNT;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.DATA_COMPRESSION_MODE;
import com.shimmerresearch.verisense.payloaddesign.DataBlockDetails;
import com.shimmerresearch.verisense.payloaddesign.DataBlockDetails.DATABLOCK_SENSOR_ID;

/** 
 * 
 * @author Mark Nolan, Sriram Raju Dandu
 *
 */
public class VerisenseDevice extends ShimmerDevice {

	private static final long serialVersionUID = -5496745972549472824L;

	private static final Integer INVALID_VALUE = -1;

	VerisenseProtocolByteCommunication mProtocol;

	public int dataCompressionMode = DATA_COMPRESSION_MODE.NONE;

	private static int DEFAULT_HW_ID = HW_ID.VERISENSE_IMU;
	private ShimmerVerObject defaultSvo = new ShimmerVerObject(DEFAULT_HW_ID, FW_ID.UNKNOWN, FW_ID.UNKNOWN, FW_ID.UNKNOWN, FW_ID.UNKNOWN);
	private ExpansionBoardDetails defaultEbd = new ExpansionBoardDetails(DEFAULT_HW_ID, 0, INVALID_VALUE);
	
	/** Using SD here because currently we are just processing binary files. In the
	 * future we might want to add BLE connectivity/streaming support for the Java
	 * code. */
	public COMMUNICATION_TYPE defaultCommType = COMMUNICATION_TYPE.SD;
	private static int ADC_BYTE_BUFFER_SIZE = 192;

	private byte resetReason = RESET_REASON.CLEARED.bitMask;
	public enum RESET_REASON {
		POR_OR_BOD((byte) 0x00, "POR_OR_BOD", "Battery died or disconnected"),
		RESETPIN((byte) 0x01, "RESETPIN", "Reset pin triggered"),
		DOG((byte) 0x02, "WATCHDOG", "FW froze"),
		SREQ((byte) 0x04, "SREQ", "FW update"),
		LOCKUP((byte) 0x08, "LOCKUP", "FW lockup"),
		OFF((byte) 0x10, "OFF", "Off"),
		LPCOMP((byte) 0x20, "LPCOMP", "Low-power comparator"),
		DIF((byte) 0x40, "DIF", "DIF"),
		VBUS((byte) 0x80, "VBUS", "VBUS"),
		CLEARED((byte) 0xFF, "None", "None");
		
		public byte bitMask = 0;
		public String descriptionShort = "";
		public String descriptionLong = "";
		
		private RESET_REASON(byte bitMask, String descriptionShort, String descriptionLong) {
			this.bitMask = bitMask;
			this.descriptionShort = descriptionShort;
			this.descriptionLong = descriptionLong;
		}

		public static RESET_REASON findValueForShortDescription(String descriptionShort) {
			for (VerisenseDevice.RESET_REASON resetReason : VerisenseDevice.RESET_REASON.values()) {
				if (descriptionShort.equals(resetReason.descriptionShort)) {
					return resetReason;
				}
			}
			return null;
		}
	}
	
	public class CSV_HEADER_LINES {
		public static final String RESET = "Reset: Reason = "; 
	}

	
	private Integer resetCounter = null;
	private Integer firstPayloadIndexAfterBoot = null;
	public boolean isExtendedPayloadConfig = true;

	private double timeMsCurrentSample;

	// Saving in global map as we only need to do this once per datablock sensor ID per payload, not for each datablock
	private HashMap<DATABLOCK_SENSOR_ID, List<SENSORS>> mapOfSensorIdsPerDataBlock = new HashMap<DATABLOCK_SENSOR_ID, List<SENSORS>>();

	private transient StatusPayload status;
	private transient OpConfigPayload opConfig;
	private transient ProdConfigPayload prodConfigPayload;
	
	public static class FW_CHANGES {
		/** FW Version and Reset Reason */
		public static final ShimmerVerObject CCF19_027 = new ShimmerVerObject(FW_ID.UNKNOWN, 0, 34, 1); 
		/** Reset Reason, Reset Counter, First Payload Index */
		public static final ShimmerVerObject CCF19_035 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 1, 2);
		/** PPG support (i.e., 2 bytes HW ver in payload header + 3 additional config. bytes) */
		public static final ShimmerVerObject CCF20_012_1 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 2);
		/** PPG support (i.e., LED1_PA added to payload config) */
		public static final ShimmerVerObject CCF20_012_2 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 9);
		/** PPG support (i.e., LED2_PA, LED3_PA, LED4_PA added to payload config) */
		public static final ShimmerVerObject CCF20_012_3 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 35);
		/** PPG support (i.e., LED1-4 RGE byte added to payload config) */
		public static final ShimmerVerObject CCF20_012_4 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 70);
		/** Multiple sensor support */
		public static final ShimmerVerObject CCF20_012_5 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 73);
		/** RTC ticks added back into payload footer */
		public static final ShimmerVerObject CCF20_012_6 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 74);
		/** Microcontroller time (ticks and minutes) added into payload footer */
		public static final ShimmerVerObject CCF21_010_1 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 85);
		/** Ticks per data block are now Microcontroller ticks instead of RWC ticks and
		 * RTC time (ticks and minutes) in payload footer is now the RWC offset */
		public static final ShimmerVerObject CCF21_010_2 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 87);
		/** GSR support with ADC sampling rate byte added to header */
		public static final ShimmerVerObject CCF21_010_3 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 88);
	}

	public static class FW_SPECIAL_VERSIONS {
		// FW v0.31.000 had the battery voltage added to the payload footer ASM-425
		public static final ShimmerVerObject V_0_31_000 = new ShimmerVerObject(FW_ID.UNKNOWN, 0, 32, 0);
		// FW v1.02.065 with ASM-1511 (Gen2 LTF chip) and ASM-1535 (uint16_t error in scheduler)
		public static final ShimmerVerObject V_1_02_071 = new ShimmerVerObject(FW_ID.UNKNOWN, 1, 2, 71);
	}
	
	public static class SENSOR_CONFIG_STRINGS {
		public static final String SAMPLING_RATE_CONFIGURED = "Configured = ";
		public static final String SAMPLING_RATE_CALCULATED = "Calculated = ";
		public static final String RANGE = "Range = ";
		public static final String RESOLUTION = "Resolution = ";
		public static final String RATE = "Rate = ";
	}

	public static class CSV_LINE_TITLES {
		public static final String ALGORITHM_CONFIG = "Algorithm config: ";
		public static final String SENSOR_CONFIG = "Sensor config: ";
	}

	public static List<SENSORS> LIST_OF_PPG_SENSORS = Arrays.asList(new SENSORS[] {SENSORS.MAX86150, SENSORS.MAX86916});
	
	public static final double[][] ADC_SAMPLING_RATES = new double[][] {
		{0, Double.NaN, Double.NaN},
		{1, 32768.0, 1},
		{2, 16384.0, 2},
		{3, 8192.0, 4},
		{4, 6553.6, 5},
		{5, 4096.0, 8},
		{6, 3276.8, 10},
		{7, 2048.0, 16},
		{8, 1638.4, 20},
		{9, 1310.72, 25},
		{10, 1024.0, 32},
		{11, 819.2, 40},
		{12, 655.36, 50},
		{13, 512.0, 64},
		{14, 409.6, 80},
		{15, 327.68, 100},
		{16, 256.0, 128},
		{17, 204.8, 160},
		{18, 163.84, 200},
		{19, 128.0, 256},
		{20, 102.4, 320},
		{21, 81.92, 400},
		{22, 64.0, 512},
		{23, 51.2, 640},
		{24, 40.96, 800},
		{25, 32.0, 1024},
		{26, 25.6, 1280},
		{27, 20.48, 1600},
		{28, 16.0, 2048},
		{29, 12.8, 2560},
		{30, 10.24, 3200},
		{31, 8.0, 4096},
		{32, 6.4, 5120},
		{33, 5.12, 6400},
		{34, 4.0, 8192},
		{35, 3.2, 10240},
		{36, 2.56, 12800},
		{37, 2.0, 16384},
		{38, 1.6, 20480},
		{39, 1.28, 25600},
		{40, 1.0, 32768},
		{41, 0.8, 40960},
		{42, 0.64, 51200}};
	
	/**
	 * 
	 */
	public VerisenseDevice() {
		super.setDefaultShimmerConfiguration();
		addCommunicationRoute(defaultCommType);

		setUniqueId(DEVICE_TYPE.VERISENSE.getLabel());
		setShimmerUserAssignedName(mUniqueID);
		setMacIdFromUart(mUniqueID);
	}
	
	@Override
	public String getFirmwareVersionParsed() {
		if(mShimmerVerObject.mFirmwareVersionMajor==FW_ID.UNKNOWN) {
			return UtilVerisenseDriver.FEATURE_NOT_AVAILABLE;
		} else {
			return mShimmerVerObject.getFirmwareVersionParsedVersionNumberFilled();
		}
	}

	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer, COMMUNICATION_TYPE commType) {
		byte[] configBytes = null;
		
		if (commType == COMMUNICATION_TYPE.SD) {
			int payloadConfigBytesSize = PayloadContentsDetails.calculatePayloadConfigBytesSize(mShimmerVerObject);
			configBytes = new byte[payloadConfigBytesSize];
			
			long enabledSensors = getEnabledSensors();
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0] = (byte) (enabledSensors & 0xFF);
			if(isPayloadDesignV4orAbove()) {
				configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG2] = (byte) ((enabledSensors >> 8) & 0xFF);
			}
			
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0] |= isExtendedPayloadConfig? (0x01<<4):0x00;
			configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0] |= ((dataCompressionMode&0x03)<<0);
			
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()) {
				abstractSensor.configBytesGenerate(this, configBytes, commType);
			}

			if(isExtendedPayloadConfig) {
				configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_FW_MAJOR] = (byte) (mShimmerVerObject.mFirmwareVersionMajor&0xFF);
				configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_FW_MINOR] = (byte) (mShimmerVerObject.mFirmwareVersionMinor&0xFF);
				configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_FW_INTERNAL_LSB] = (byte) (mShimmerVerObject.mFirmwareVersionInternal&0xFF);
				configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_FW_INTERNAL_MSB] = (byte) ((mShimmerVerObject.mFirmwareVersionInternal>>8)&0xFF);
				
				if(isPayloadDesignV2orAbove()) {
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.RESET_REASON] = resetReason;
				}
				if(isPayloadDesignV3orAbove()) {
					int resetCounter = (this.resetCounter==null? 0:this.resetCounter);
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.RESET_COUNTER_LSB] = (byte) (resetCounter & 0xFF);
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.RESET_COUNTER_MSB] = (byte) ((resetCounter>>8) & 0xFF);
					int firstPayloadIndexAfterBoot = (this.firstPayloadIndexAfterBoot==null? 0:this.firstPayloadIndexAfterBoot);
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.FIRST_PAYLOAD_AFTER_RESET_LSB] = (byte) (firstPayloadIndexAfterBoot & 0xFF);
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.FIRST_PAYLOAD_AFTER_RESET_MSB] = (byte) ((firstPayloadIndexAfterBoot>>8) & 0xFF);
				}
				
				if(isPayloadDesignV4orAbove()) {
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_HW_MAJOR] = (byte) getExpansionBoardId();
					configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_HW_MINOR] = (byte) getExpansionBoardRev();
				}
			}
		} else {
			//TODO parse op config bytes
		}
		
		return configBytes;
	}
	
	public void configBytesParseAndInitialiseAlgorithms(byte[] configBytes, COMMUNICATION_TYPE commType) {
		
		this.configBytesParse(configBytes, commType);

		// No issue calling this here at the moment as there is nothing that relies on
		// the sampling rates of individual sensors inside DataProcessingVerisense
		// (e.g., filters). If we want to add something in the future, we need to find a
		// way to pass individual sensor sampling rates to that class.
		initaliseDataProcessing();

		if(isPayloadDesignV8orAbove()) {
			initializeAlgorithmsWithDifferentRatesPerSensor();
		} else {
			initializeAlgorithms();
		}
	}
	
	@Override
	public void configBytesParse(byte[] configBytes, COMMUNICATION_TYPE commType) {
		mConfigBytes = configBytes;
		
		long enabledSensors = 0 ; 
				
		if (commType == COMMUNICATION_TYPE.SD) {
			isExtendedPayloadConfig = isExtendedPayloadConfig(configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0]);
			dataCompressionMode = (configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0]>>0)&0x03;
			
			ShimmerVerObject svo = defaultSvo;
			ExpansionBoardDetails eBD = defaultEbd;
			
			if(isExtendedPayloadConfig){
				byte[] payloadConfigFwVer = new byte[4];
				System.arraycopy(configBytes, PAYLOAD_CONFIG_BYTE_INDEX.REV_FW_MAJOR, payloadConfigFwVer, 0, payloadConfigFwVer.length);
				svo = parseFirmwareVersionToShimmerVerObject(payloadConfigFwVer);
			}
			setShimmerVersionObject(svo);
			
			if(isExtendedPayloadConfig){
				setResetReason(configBytes[PAYLOAD_CONFIG_BYTE_INDEX.RESET_REASON]);
				
				if(isPayloadDesignV3orAbove()) {
					Integer resetCounter = ((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.RESET_COUNTER_MSB] & 0xFF) << 8) | (configBytes[PAYLOAD_CONFIG_BYTE_INDEX.RESET_COUNTER_LSB] & 0xFF);
					setResetCounter(resetCounter);
					Integer firstPayloadResetAfterBoot = ((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.FIRST_PAYLOAD_AFTER_RESET_MSB] & 0xFF) << 8) | (configBytes[PAYLOAD_CONFIG_BYTE_INDEX.FIRST_PAYLOAD_AFTER_RESET_LSB] & 0xFF);
					setFirstPayloadResetAfterBoot(firstPayloadResetAfterBoot);
				}
				
				if(isPayloadDesignV4orAbove()) {
					int hwVerMajor = configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_HW_MAJOR];
					
					// Fix for a legacy HW ID assignment issue
					if(hwVerMajor==0) {
						hwVerMajor = HW_ID.VERISENSE_DEV_BRD;
					} else if(hwVerMajor==1) {
						hwVerMajor = HW_ID.VERISENSE_IMU;
					}
					
					int hwVerMinor = configBytes[PAYLOAD_CONFIG_BYTE_INDEX.REV_HW_MINOR];
					setHardwareVersion(hwVerMajor);
					eBD = new ExpansionBoardDetails(hwVerMajor, hwVerMinor, INVALID_VALUE);
				}
			}
			setExpansionBoardDetails(eBD);
			
			// sensorAndConfigMapsCreate needs to be called after the ShimmerVerObject
			// and ExpansionBoardDetails have been set but before any sensor config is processed.
			sensorAndConfigMapsCreate();

			enabledSensors = (configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0] & 0xE0);
			int payloadConfig2Bitmask = 0;
			if(isPayloadDesignV12orAbove()) {
				payloadConfig2Bitmask = 0xFE;
			} else if(isPayloadDesignV4orAbove()) {
				payloadConfig2Bitmask = 0xFC;
			}
			if(payloadConfig2Bitmask>0) {
				enabledSensors |= ((configBytes[PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG2] & payloadConfig2Bitmask) << 8);
			}
		} else {
			//TODO parse op config bytes
			
			enabledSensors = (configBytes[OP_CONFIG_BYTE_INDEX.GEN_CFG_0] & 0xE0);
			enabledSensors |= (configBytes[OP_CONFIG_BYTE_INDEX.GEN_CFG_1] & 0x3F) << 8;
			enabledSensors |= (configBytes[OP_CONFIG_BYTE_INDEX.GEN_CFG_2] & 0x02) << 8;
			
		}

		setEnabledAndDerivedSensorsAndUpdateMaps(enabledSensors, mDerivedSensors, commType);
		
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()) {
			abstractSensor.configBytesParse(this, configBytes, commType);
		}

		// Useful for debugging during development
//		printSensorParserAndAlgoMaps();
	}

	@Override
	public void algorithmMapUpdateFromEnabledSensorsVars() {
		updateDerivedSensorsFromEnabledSensors();
		super.algorithmMapUpdateFromEnabledSensorsVars();
	}
	
	public void updateDerivedSensorsFromEnabledSensors() {
		mDerivedSensors = 0;
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO)) {
			mDerivedSensors |= GyroOnTheFlyCalModuleVerisense.algoGyroOnTheFlyCalVerisense.mDerivedSensorBitmapID;
		}
	}

	/**
	 * @see ShimmerDevice.initializeAlgorithms() 
	 */
	public void initializeAlgorithmsWithDifferentRatesPerSensor() {
		for (AbstractAlgorithm aa:mMapOfAlgorithmModules.values()){
			try {
				if(aa.isEnabled()){
					//TODO make this more dynamic (maybe by including a sensor class key list in the AbstractAlgorithm class)
					SENSORS sensorClassKey = null;
					if(aa instanceof GyroOnTheFlyCalModuleVerisense) {
						sensorClassKey = SENSORS.LSM6DS3;
					}
					
					if(sensorClassKey!=null) {
						double sensorSamplingRate = getSamplingRateForSensor(sensorClassKey);
						
						aa.setShimmerSamplingRate(sensorSamplingRate);
						aa.initialize();
					}
				} else {
					//TODO stop the algorithm
				}
			} catch (Exception e1) {
				consolePrintException("Error initialising algorithm module\t" + aa.getAlgorithmName(), e1.getStackTrace());
			}
		}
	}
	
	//TODO copied from the ShimmerVerObject class, create a new method there
	public static ShimmerVerObject parseFirmwareVersionToShimmerVerObject(byte[] payloadConfigFwVer) {
		ShimmerVerObject svo = new ShimmerVerObject();
		svo.mHardwareVersion = DEFAULT_HW_ID;
		svo.mFirmwareVersionMajor = payloadConfigFwVer[0]&0xFF;
		svo.mFirmwareVersionMinor = payloadConfigFwVer[1]&0xFF;
		svo.mFirmwareVersionInternal = ((payloadConfigFwVer[2]) | (payloadConfigFwVer[3]<<8))&0xFFFF;
		return svo;
	}

	public boolean isPayloadDesignV1orAbove() {
		return PayloadContentsDetails.isPayloadDesignV1orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV2orAbove() {
		return PayloadContentsDetails.isPayloadDesignV2orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV3orAbove() {
		return PayloadContentsDetails.isPayloadDesignV3orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV4orAbove() {
		return PayloadContentsDetails.isPayloadDesignV4orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV5orAbove() {
		return PayloadContentsDetails.isPayloadDesignV5orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV6orAbove() {
		return PayloadContentsDetails.isPayloadDesignV6orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV7orAbove() {
		return PayloadContentsDetails.isPayloadDesignV7orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV8orAbove() {
		return PayloadContentsDetails.isPayloadDesignV8orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV9orAbove() {
		return PayloadContentsDetails.isPayloadDesignV9orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV10orAbove() {
		return PayloadContentsDetails.isPayloadDesignV10orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV11orAbove() {
		return PayloadContentsDetails.isPayloadDesignV11orAbove(getShimmerVerObject());
	}

	public boolean isPayloadDesignV12orAbove() {
		return PayloadContentsDetails.isPayloadDesignV12orAbove(getShimmerVerObject());
	}

	/**
	 * @return
	 * @see PayloadContentsDetails.isCsvHeaderDesignAzMarkingPoint()
	 */
	public boolean isCsvHeaderDesignAzMarkingPoint() {
		return PayloadContentsDetails.isCsvHeaderDesignAzMarkingPoint(getShimmerVerObject());
	}
	
	//TODO move to ShimmerDriver
	public static boolean isFwMajorMinorInternalVerEqual(ShimmerVerObject svo, ShimmerVerObject svo2) {
		if(svo.getFirmwareVersionMajor()==svo2.getFirmwareVersionMajor()
				&& svo.getFirmwareVersionMinor()==svo2.getFirmwareVersionMinor()
				&& svo.getFirmwareVersionInternal()==svo2.getFirmwareVersionInternal()) {
			return true;
		}
		return false;
	}

	@Override
	public double getSamplingRateShimmer() {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			return getSamplingRateForSensor(SENSORS.LIS2DW12);
		} else if(isEitherLsm6ds3ChannelEnabled()) {
			return getSamplingRateForSensor(SENSORS.LSM6DS3);
		} else if(isHwPpgAndAnyMaxChEnabled()) {
			return getSamplingRateForSensor(getPpgSensorClassKey());
		} else if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.VBATT)) {
			return getSamplingRateForSensor(SENSORS.Battery);
		} else if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.GSR)) {
			return getSamplingRateForSensor(SENSORS.GSR);
		}
		return 0.0;
	}

	public AbstractSensor getAbstractSensorPpg() {
		SENSORS sensorClassKey = getPpgSensorClassKey();
		return getSensorClass(sensorClassKey);
	}

	public SENSORS getPpgSensorClassKey() {
		for(SENSORS sensorClassKey:LIST_OF_PPG_SENSORS) {
			AbstractSensor abstractSensor = getSensorClass(sensorClassKey);
			if(abstractSensor!=null) {
				return sensorClassKey;
			}
		}
		return null;
	}

	public SENSORS getPrimaryAccel() {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			return SENSORS.LIS2DW12;
		} else if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL)) {
			return SENSORS.LSM6DS3;
		}
		return null;
	}

	public double getSamplingRateAccel() {
		AbstractSensor.SENSORS sensorClassKey = getPrimaryAccel();
		return getSamplingRateForSensor(sensorClassKey);
	}

	public double getSamplingRateForSensor(AbstractSensor.SENSORS sensorClassKey) {
		if(sensorClassKey!=null) {
			AbstractSensor abstractSensor = getSensorClass(sensorClassKey);
			if(abstractSensor!=null) {
				Object returnValue = abstractSensor.getConfigValueUsingConfigLabel(GuiLabelConfigCommon.RATE);
				if(returnValue!=null && returnValue instanceof Double) {
					return (double)returnValue;
				}
			}
		}
		return 0.0;
	}

	public double getFastestSamplingRateOfSensors() {
		double fastestSamplingRate = 0.0;
		if(mMapOfSensorClasses!=null){
			for(Entry<SENSORS, AbstractSensor> entry:getMapOfEnabledAbstractSensors().entrySet()){
				double sensorSamplingRate = getSamplingRateForSensor(entry.getKey());
				fastestSamplingRate = Math.max(fastestSamplingRate, sensorSamplingRate);
			}
		}
		return fastestSamplingRate;
	}
	
	public LinkedHashMap<SENSORS, AbstractSensor> getMapOfEnabledAbstractSensors() {
		LinkedHashMap<SENSORS, AbstractSensor> mapOfEnabledAbstractSensors = new LinkedHashMap<SENSORS, AbstractSensor>();
		if(mMapOfSensorClasses!=null){
			for(Entry<SENSORS, AbstractSensor> entry:mMapOfSensorClasses.entrySet()){
				if(entry.getValue().isAnySensorChannelEnabled(defaultCommType)) {
					mapOfEnabledAbstractSensors.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return mapOfEnabledAbstractSensors;
	}

	public boolean compareFwVersions(ShimmerVerObject svo) {
		return compareFwVersions(mShimmerVerObject, svo);
	}

	//TODO move to ShimmerVerObject or UtilShimmer class?
	public static boolean compareFwVersions(ShimmerVerObject svo1, ShimmerVerObject svo2) {
		return UtilShimmer.compareVersions(svo1.getFirmwareVersionMajor(), svo1.getFirmwareVersionMinor(), svo1.getFirmwareVersionInternal(),
				svo2.getFirmwareVersionMajor(), svo2.getFirmwareVersionMinor(), svo2.getFirmwareVersionInternal());
	}

	/**
	 * Checks Bit 4 of the first payload config byte to determine if the payload
	 * configuration is of the latest design (i.e., extended configuration bytes)
	 * in-which the number of bytes is greater then 2. The subsequet FW version
	 * bytes can subsequently be used to determine the exact quantity of bytes in
	 * the payload configuration.
	 * 
	 * @param payloadConfig byte 0
	 * @return true if the EXT_CFG bit (bit 4) is high
	 */
	public static boolean isExtendedPayloadConfig(byte payloadConfig) {
		return (payloadConfig&0x10)==0x10;
	}
	
	public String generateResetReasonStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(CSV_HEADER_LINES.RESET);
		if(isPayloadDesignV2orAbove()) {
			String resetReasonByte = " " + UtilShimmer.byteToHexStringFormatted(resetReason);
			if(resetReason==RESET_REASON.POR_OR_BOD.bitMask) {
				sb.append(RESET_REASON.POR_OR_BOD.descriptionShort + resetReasonByte);
			}
			else if(resetReason==RESET_REASON.CLEARED.bitMask) {
				sb.append(RESET_REASON.CLEARED.descriptionShort + resetReasonByte);
			} else {
				List<RESET_REASON> listOfReasons = new ArrayList<RESET_REASON>();
				for(RESET_REASON resetReasonToCheck:RESET_REASON.values()) {
					if(resetReasonToCheck!=RESET_REASON.POR_OR_BOD 
							&& resetReasonToCheck!=RESET_REASON.CLEARED
							&& (resetReason&resetReasonToCheck.bitMask)==resetReasonToCheck.bitMask) {
						listOfReasons.add(resetReasonToCheck);
					}
				}

				for(int i=0;i<listOfReasons.size();i++) {
					RESET_REASON resetReasonBit = listOfReasons.get(i);
					sb.append(resetReasonBit.descriptionShort);
					// Add a ";" unless this is the last loop
					if(i!=listOfReasons.size()-1) {
						sb.append(";");
					}
				}
				
				sb.append(resetReasonByte);
			}
		} else {
			sb.append(UtilVerisenseDriver.FEATURE_NOT_AVAILABLE);
		}

		sb.append("; Count = ");
		if(isPayloadDesignV3orAbove()) {
			sb.append(resetCounter==null? UtilVerisenseDriver.FEATURE_NOT_AVAILABLE:resetCounter);
		} else {
			sb.append(UtilVerisenseDriver.FEATURE_NOT_AVAILABLE);
		}

		sb.append("; Start Index = ");
		if(isPayloadDesignV3orAbove()) {
			sb.append(firstPayloadIndexAfterBoot==null? UtilVerisenseDriver.FEATURE_NOT_AVAILABLE:firstPayloadIndexAfterBoot);
		} else {
			sb.append(UtilVerisenseDriver.FEATURE_NOT_AVAILABLE);
		}

		return sb.toString();
	}

	//TODO move to sensor classes?
	public String generateSensorConfigStrSingleSensor(AbstractSensor.SENSORS sensorClassKey, double calculatedSamplingRate) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(CSV_LINE_TITLES.SENSOR_CONFIG);
		if(isCsvHeaderDesignAzMarkingPoint()) {
			sb.append(SENSOR_CONFIG_STRINGS.RATE);
			sb.append(getSamplingRateShimmer());
			sb.append(" ");
			sb.append(CHANNEL_UNITS.FREQUENCY);
			sb.append("; ");
		}
		
		if((sensorClassKey==AbstractSensor.SENSORS.LIS2DW12) 
				&& isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			SensorLIS2DW12 sensorLis2dw12 = (SensorLIS2DW12) getSensorClass(SENSORS.LIS2DW12);
			
			// Accel1 section
			if(isCsvHeaderDesignAzMarkingPoint()) {
				sb.append(SensorLIS2DW12.ACCEL_ID);
				sb.append(" ");
			} else {
				sb.append(generateCalcSamplingRateConfigStr(sensorClassKey, sensorLis2dw12.getAccelRateFreq(), calculatedSamplingRate));
			}

			// E.g. +/- 4g
			sb.append(SENSOR_CONFIG_STRINGS.RANGE);
			sb.append(sensorLis2dw12.getAccelRangeString());
			
			if(isCsvHeaderDesignAzMarkingPoint()) {
				// E.g. Low-Power
				sb.append("; Mode = ");
				sb.append(sensorLis2dw12.getAccelModeString());
				
				sb.append("; LP_Mode = ");
				sb.append(sensorLis2dw12.getAccelLpModeString());
			} else {
				sb.append("; Mode = ");
				sb.append(sensorLis2dw12.getAccelModeMergedString());
				sb.append("; ");
				sb.append(SENSOR_CONFIG_STRINGS.RESOLUTION);
				sb.append(sensorLis2dw12.getAccelResolutionString());
			}
			
			if(!isCsvHeaderDesignAzMarkingPoint()) {
				sb.append("}");
			}
		} else if((sensorClassKey==AbstractSensor.SENSORS.LSM6DS3)
				&& isEitherLsm6ds3ChannelEnabled()) {
			SensorLSM6DS3 sensorLsm6ds3 = (SensorLSM6DS3) getSensorClass(SENSORS.LSM6DS3);
			
			// Accel2 section
			if(isCsvHeaderDesignAzMarkingPoint()) {
				sb.append(SensorLSM6DS3.ACCEL_ID);
				sb.append(" ");
			} else {
				sb.append(generateCalcSamplingRateConfigStr(sensorClassKey, sensorLsm6ds3.getRateFreq(), calculatedSamplingRate));
				sb.append("Accel ");
			}

			sb.append(SENSOR_CONFIG_STRINGS.RANGE);
			if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL) 
					|| isCsvHeaderDesignAzMarkingPoint()) {
				sb.append(sensorLsm6ds3.getAccelRangeString());
			}
			else {
				sb.append("Off");
			}

			// Gyro section
			sb.append("; Gyro ");
			sb.append(SENSOR_CONFIG_STRINGS.RANGE);
			if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO) 
					|| isCsvHeaderDesignAzMarkingPoint()) {
				sb.append(sensorLsm6ds3.getGyroRangeString());
			}
			else {
				sb.append("Off");
			}
			
			
			if(!isCsvHeaderDesignAzMarkingPoint()) {
				sb.append("; ");
				sb.append(SENSOR_CONFIG_STRINGS.RESOLUTION);
				sb.append("16-bit");
				sb.append("}");
			}
		} else if(LIST_OF_PPG_SENSORS.contains(sensorClassKey) && isHwPpgAndAnyMaxChEnabled()) {
			AbstractSensor abstractSensor = getAbstractSensorPpg();
			if(abstractSensor!=null) {
				SensorMAX86XXX sensorMAX86XXX = (SensorMAX86XXX)abstractSensor;
				
				// PPG section
				if(isCsvHeaderDesignAzMarkingPoint()) {
					sb.append("PPG ");
				} else {
					sb.append(generateCalcSamplingRateConfigStr(sensorClassKey, sensorMAX86XXX.getSamplingRateFreq(), calculatedSamplingRate));
				}

				sb.append("Pulse Width = ");				// E.g. 50 us
				int ppgPulseWidthConfigValue = sensorMAX86XXX.getPpgPulseWidthConfigValue();
				String ppgPulseWidth = SensorMAX86XXX.CONFIG_OPTION_PPG_PULSE_WIDTH.getConfigStringFromConfigValue(ppgPulseWidthConfigValue);
				ppgPulseWidth = ppgPulseWidth.replaceAll(CHANNEL_UNITS.MICROSECONDS, (" "+CHANNEL_UNITS.MICROSECONDS));
				sb.append(ppgPulseWidth);
				
				// Pulse amplitude
				if(isPayloadDesignV5orAbove()) {
					sb.append("; ");
					
					List<Integer> listOfEnabledMaxPpgCh = new ArrayList<Integer>();
					int[] ppgChArray = new int[] {
							// MAX86150 and MAX86916 PPG channels
							Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED,
							Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR,
							// MAX86916 PPG channels
							Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN,
							Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE};
					for(int ppgCh:ppgChArray) {
						if(isSensorEnabled(ppgCh)) {
							listOfEnabledMaxPpgCh.add(ppgCh);
						}
					}
					
					if (listOfEnabledMaxPpgCh.size() > 0) {
						sb.append("LED Amplitude [");
						for(int chIdx=0;chIdx<listOfEnabledMaxPpgCh.size();chIdx++) {
							if(chIdx>0 && chIdx<(listOfEnabledMaxPpgCh.size())) {
								sb.append(", ");
							}
							Integer sensorId = listOfEnabledMaxPpgCh.get(chIdx);

							if (sensorId == Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED) {
								sb.append("Red = ");
								sb.append(sensorMAX86XXX.getPpgLedAmplitudeRedString());
							} else if (sensorId == Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR) {
								sb.append("IR = ");
								sb.append(sensorMAX86XXX.getPpgLedAmplitudeIrString());
							} else {
								if(abstractSensor instanceof SensorMAX86916) {
									SensorMAX86916 sensorMAX86916 = (SensorMAX86916)abstractSensor;
									if (sensorId == Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN) {
										sb.append("Green = ");
										sb.append(sensorMAX86916.getPpgLedAmplitudeGreenString());
									}
									else if (sensorId == Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE) {
										sb.append("Blue = ");
										sb.append(sensorMAX86916.getPpgLedAmplitudeBlueString());
									}
								}
							}
						}
						sb.append("]");
					}
					
				} else {
					if(abstractSensor instanceof SensorMAX86150) {
						SensorMAX86150 sensorMAX86150 = (SensorMAX86150)abstractSensor;
						sb.append("; Amplitude = ");
						sb.append(sensorMAX86150.getPpgLedAmplitudeRedString());
					}
				}
				
				// Sample average
				sb.append("; Sample Average = ");
				int ppgSampleAverageConfigValue = sensorMAX86XXX.getPpgSmpAveConfigValue();
				String ppgSampleAverage = SensorMAX86XXX.CONFIG_OPTION_PPG_SAMPLE_AVG.getConfigStringFromConfigValue(ppgSampleAverageConfigValue);
				sb.append(ppgSampleAverage);
				
				// Resolution
				sb.append("; ");
				sb.append(SENSOR_CONFIG_STRINGS.RESOLUTION);
				int ppgAdcResolutionConfigValue = sensorMAX86XXX.getPpgAdcResolutionConfigValue();
				String ppgAdcResolution = SensorMAX86XXX.CONFIG_OPTION_PPG_ADC_RESOLUTION.getConfigStringFromConfigValue(ppgAdcResolutionConfigValue);
				sb.append(ppgAdcResolution);
				
				if(!isCsvHeaderDesignAzMarkingPoint()) {
					sb.append("}");
				}
			}
		// ADC based channels	
		} else if((sensorClassKey==AbstractSensor.SENSORS.Battery && isSensorEnabled(Configuration.Verisense.SENSOR_ID.VBATT))
				|| (sensorClassKey==AbstractSensor.SENSORS.GSR && isSensorEnabled(Configuration.Verisense.SENSOR_ID.GSR))) {
			sb.append(generateCalcSamplingRateConfigStr(sensorClassKey, getSamplingRateForSensor(sensorClassKey), calculatedSamplingRate));

			sb.append(SENSOR_CONFIG_STRINGS.RESOLUTION);
			sb.append("12-bit");
			sb.append("}");
		}
		
		
		return sb.toString();
	}
	
	private StringBuilder generateCalcSamplingRateConfigStr(SENSORS sensorClassKey, double configuredSamplingRate, double calculatedSamplingRate) {
		StringBuilder sb = new StringBuilder();
		sb.append(sensorClassKey.toString());
		sb.append(" {Sampling Rate [");
		sb.append(SENSOR_CONFIG_STRINGS.SAMPLING_RATE_CONFIGURED);
		sb.append(configuredSamplingRate);
		sb.append(" ");
		sb.append(CHANNEL_UNITS.FREQUENCY);

		sb.append(", ");
		sb.append(SENSOR_CONFIG_STRINGS.SAMPLING_RATE_CALCULATED);
		if(!Double.isNaN(calculatedSamplingRate)) {
			sb.append(UtilVerisenseDriver.formatDoubleToNdecimalPlaces(calculatedSamplingRate, 3));
			sb.append(" ");
			sb.append(CHANNEL_UNITS.FREQUENCY);
		} else {
			sb.append(UtilVerisenseDriver.UNAVAILABLE);
		}
		sb.append("]; ");
		return sb;
	}

	public void setResetReason(byte resetReason) {
		this.resetReason = resetReason;
	}

	public byte getResetReason() {
		return resetReason;
	}

	public void setResetCounter(Integer resetCounter) {
		this.resetCounter = resetCounter;
	}

	public Integer getResetCounter() {
		return this.resetCounter;
	}
	
	public void setFirstPayloadResetAfterBoot(Integer firstPayloadIndexAfterBoot) {
		this.firstPayloadIndexAfterBoot = firstPayloadIndexAfterBoot;
	}
	
	public Integer getFirstPayloadIndexAfterBoot() {
		return firstPayloadIndexAfterBoot;
	}
	
	public double calibrateTemperature(long temperatureUncal) {
		double temperatureCal = 0.0;
		
		if(isPayloadDesignV9orAbove()) {
			temperatureCal = calibrateNrf52Temperatue(temperatureUncal);
		} else {
			if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
				temperatureCal = SensorLIS2DW12.calibrateTemperature(temperatureUncal);
			} else if(isEitherLsm6ds3ChannelEnabled()) {
				temperatureCal = SensorLSM6DS3.calibrateTemperature(temperatureUncal);
			} else if(isHwPpgAndAnyMaxChEnabled()) {
				temperatureCal = calibrateNrf52Temperatue(temperatureUncal);
			}
		}
		
		return temperatureCal;
	}

	public boolean isSpiChannelEnabled() {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL) || isEitherLsm6ds3ChannelEnabled()) {
			return true;
		}
		return false;
	}
	
	public boolean isHwPpgAndAnyMaxChEnabled() {
		int hwId = getHardwareVersion();
		return ((hwId==HW_ID.VERISENSE_DEV_BRD || hwId==HW_ID.VERISENSE_PPG || hwId==HW_ID.VERISENSE_PULSE_PLUS) 
				&& (isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86150_ECG)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE)));
	}
	
	public boolean isAnAdcChEnabled() {
		return (isSensorEnabled(Configuration.Verisense.SENSOR_ID.VBATT)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.GSR));
	}

	//TODO set derived algorithm enabled bit like as done for PPG rather then checking
	public boolean isAnAccelEnabled() {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL)) {
			return true;
		}
		return false;
	}

	public static boolean isSensorKeyAnAccel(SENSORS sensorClassKey) {
		if(sensorClassKey==SENSORS.LIS2DW12 || sensorClassKey==SENSORS.LSM6DS3) {
			return true;
		}
		return false;
	}

	public boolean isEitherLsm6ds3ChannelEnabled() {
		AbstractSensor abstractSensor = getSensorClass(SENSORS.LSM6DS3);
		if(abstractSensor!=null) {
			return abstractSensor.isAnySensorChannelEnabled(defaultCommType);
		}
		return false;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetails(int sensorId) {
		if (isSensorEnabled(sensorId)) {
			CalibDetailsKinematic calibDetailsGyro = (CalibDetailsKinematic) getConfigValueUsingConfigLabel(sensorId, AbstractSensor.GuiLabelConfigCommon.CALIBRATION_CURRENT_PER_SENSOR);
			if(!calibDetailsGyro.isOffsetVectorUsingDefault() || !calibDetailsGyro.isSensitivityUsingDefault()) {
				return calibDetailsGyro;
			}
		}
		return null;
	}

	@Override
	public ShimmerDevice deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (VerisenseDevice) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		mMapOfSensorClasses = new LinkedHashMap<SENSORS, AbstractSensor>();

		// Time is set in the OJC using this sensor class. The algorithms need it set in the OJC before they can be processed.
		addSensorClass(SENSORS.CLOCK, new SensorVerisenseClock(this));

		int hwId = getHardwareVersion();
		if(hwId==HW_ID.VERISENSE_DEV_BRD 
				|| hwId==HW_ID.VERISENSE_IMU 
				|| hwId==HW_ID.VERISENSE_PPG 
				|| hwId==HW_ID.VERISENSE_GSR_PLUS) {
			addSensorClass(SENSORS.LIS2DW12, new SensorLIS2DW12(this));
			addSensorClass(SENSORS.LSM6DS3, new SensorLSM6DS3(this));
			addSensorClass(SENSORS.Battery, new SensorBattVoltageVerisense(this));
			
			if(hwId==HW_ID.VERISENSE_DEV_BRD || hwId==HW_ID.VERISENSE_PPG) {
				int expansionBoardRev = getExpansionBoardRev();
				if (expansionBoardRev == -1 || expansionBoardRev == 0) {
					addSensorClass(SENSORS.MAX86150, new SensorMAX86150(this));
				} else {
					addSensorClass(SENSORS.MAX86916, new SensorMAX86916(this));
				}
			} else if(hwId==HW_ID.VERISENSE_GSR_PLUS) {
//				addSensorClass(SENSORS.PPG, new SensorPPG(this));
				addSensorClass(SENSORS.GSR, new SensorGSRVerisense(getShimmerVerObject()));
			}
		} else if(hwId==HW_ID.VERISENSE_PULSE_PLUS) {
			addSensorClass(SENSORS.LIS2DW12, new SensorLIS2DW12(this));
			addSensorClass(SENSORS.MAX86916, new SensorMAX86916(this));
			addSensorClass(SENSORS.Battery, new SensorBattVoltageVerisense(this));
			int expansionBoardRev = getExpansionBoardRev();
			if (expansionBoardRev <= 4) {
				// TODO add BioZ support
			} else {
				addSensorClass(SENSORS.GSR, new SensorGSRVerisense(getShimmerVerObject()));
			}
		}
		super.sensorAndConfigMapsCreateCommon();

		generateParserMap();
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createConfigBytesLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

	public void resetCalibParamAndAlgorithmBuffers() {
		// Config has changed so, incase the range has changed, setting
		// defaultMatrixMultipliedInverseAMSM to null will force the code to check
		// whether it's using the correct calibration parameters.
		resetAllCalibParametersToDefault();
		
		resetAlgorithmBuffers();
	}
	
	public void resetAlgorithmBufferGyroOnTheFly() {
		List<AbstractAlgorithm> listOfEnabledAlgorithmModules = getListOfEnabledAlgorithmModules();
		Iterator<AbstractAlgorithm> iterator = listOfEnabledAlgorithmModules.iterator();
		while(iterator.hasNext()) {
			AbstractAlgorithm abstractAlgorithm = iterator.next();
			if(abstractAlgorithm instanceof GyroOnTheFlyCalModuleVerisense) {
				try {
					abstractAlgorithm.resetAlgorithmBuffers();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void resetAlgorithmBuffersAssociatedWithSensor(SENSORS sensorClassKey) {
		if(sensorClassKey==SENSORS.LSM6DS3) {
			resetAlgorithmBufferGyroOnTheFly();
		} 
	}

	public static double calibrateNrf52Temperatue(long temperatureUncal) {
		return temperatureUncal * 0.25;
	}

	/**
	 * Needed to implement a custom buildMsg for the Verisense payload structure as
	 * the timestamp isn't part of newPacket as it is for Shimmer3. Additionally,
	 * super.buildMsg only accepts a long for the timestamp argument.
	 * 
	 * @param newPacket
	 * @param commType
	 * @param isTimeSyncEnabled
	 * @param timeMsCurrentSample
	 * @return
	 */
	public ObjectCluster buildMsg(byte[] newPacket, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, double timeMsCurrentSample){
		// here so we can save the timestamp in float format to the OJC before the algorithms are processed in "processData"
		this.timeMsCurrentSample = timeMsCurrentSample;
		ObjectCluster ojc = super.buildMsg(newPacket, commType, isTimeSyncEnabled, (long) timeMsCurrentSample);
		return ojc;
	}
	
	/**
	 * Created based on ShimmerDevice.buildMsg(). Whereas that function expects all
	 * sensor bytes to be in the same packet and have the same sampling rate, this
	 * method parses each sensor separately.
	 * 
	 * @param newPacket
	 * @param listOfSensorClassKeys
	 * @param timeMsCurrentSample
	 * @return
	 */
	public ObjectCluster buildMsgForSensor(byte[] newPacket, List<SENSORS> listOfSensorClassKeys, double timeMsCurrentSample) {
		// here so we can save the timestamp in float format to the OJC before the algorithms are processed in "processData"
		this.timeMsCurrentSample = timeMsCurrentSample;

		// Arguments normally passed into ShimmerDevice.buildMsg()
		COMMUNICATION_TYPE commType = defaultCommType;
		boolean isTimeSyncEnabled = false; 
		long pcTimestamp = (long) timeMsCurrentSample;
		
		ObjectCluster ojc = new ObjectCluster(mShimmerUserAssignedName, getMacId());
		ojc.mRawData = newPacket;
		ojc.createArrayData(getNumberOfEnabledChannels(commType));

		incrementPacketsReceivedCounters();

		int index=0;
		for(SENSORS sensorClassKey : listOfSensorClassKeys) {
			TreeMap<Integer, SensorDetails> sensorMap = getSensorClass(sensorClassKey).mSensorMap;
			if(sensorMap!=null){
				for (SensorDetails sensor:sensorMap.values()){
					if(sensor.isEnabled()) {
						int length = sensor.getExpectedPacketByteArray(commType);
						byte[] sensorByteArray = new byte[length];
						//TODO process API sensors, not just bytes from Shimmer packet 
						if (length!=0){ //if length 0 means there are no channels to be processed
							if((index+sensorByteArray.length)<=newPacket.length){
								System.arraycopy(newPacket, index, sensorByteArray, 0, sensorByteArray.length);
							}
							else{
								consolePrintLn(mShimmerUserAssignedName + " ERROR PARSING " + sensor.mSensorDetailsRef.mGuiFriendlyLabel);
							}
						}
						sensor.processData(sensorByteArray, commType, ojc, isTimeSyncEnabled, pcTimestamp);

//						if(debug)
//							System.out.println(sensor.mSensorDetailsRef.mGuiFriendlyLabel + "\texpectedPacketArraySize:" + length + "\tcurrentIndex:" + index);
						index += length;
					}
				}
				
				
			} else {
				consolePrintErrLn("ERROR!!!! Sensor Map null");
			}
		}

		//After sensor data has been processed, now process any filters or Algorithms 
		ojc = processData(ojc);
		
//		if(sensorClassKey==SENSORS.MAX86916) {
//			ojc.consolePrintChannelsAndDataSingleLine();
//		}
		
		return ojc;
	}

	@Override
	protected ObjectCluster processData(ObjectCluster ojc) {
		
		AbstractSensor abstractSensor = getSensorClass(SENSORS.CLOCK);
		if(abstractSensor!=null && abstractSensor instanceof SensorVerisenseClock) {
			SensorVerisenseClock sensorVerisenseClock = (SensorVerisenseClock)abstractSensor;
			sensorVerisenseClock.processDataCustom(ojc, timeMsCurrentSample);
		}
		
		return super.processData(ojc);
	}

	// TODO move to ShimmerVerObject
	public boolean isFwMajorMinorInternalVerSet() {
		ShimmerVerObject svo = getShimmerVerObject();
		if (svo.mFirmwareVersionMajor != FW_ID.UNKNOWN 
				&& svo.mFirmwareVersionMinor != FW_ID.UNKNOWN
				&& svo.mFirmwareVersionInternal != FW_ID.UNKNOWN) {
			return true;
		}
		return false;
	}
	
	// TODO move to ShimmerDevice
	public int getExpectedDataPacketSize(SENSORS sensorClassKey){
		int dataPacketSize = 0;
		AbstractSensor abstractSensor = getSensorClass(sensorClassKey);
		for(SensorDetails sensorDetails:abstractSensor.mSensorMap.values()) {
			if(sensorDetails.isEnabled()) {
				dataPacketSize += sensorDetails.getExpectedDataPacketSize();
			}
		}
		return dataPacketSize;
	}
	
	public static double calibrateVerisenseAdcChannelToVolts(int hwId, double unCalData){
		double offset = 0; double vRefP = 0.6; double gain = 6.0; 
		if(hwId==HW_ID.VERISENSE_GSR_PLUS) {
			offset = 0; 
			vRefP = 0.75;
			gain = 4.0;
		}
		double calData = SensorADC.calibrateU12AdcValueToVolts(unCalData, offset, vRefP, gain);
		return calData;
	}

	public static double calibrateVerisenseAdcChannelToMillivolts(int hwId, double unCalData){
		return calibrateVerisenseAdcChannelToVolts(hwId, unCalData) * 1000.0;
	}

	public List<SENSORS> getSensorKeysForDatablockId(DATABLOCK_SENSOR_ID datablockSensorId) {
		List<SENSORS> listOfSensorIds = new ArrayList<SENSORS>();
		switch(datablockSensorId) {
			case ADC:
				if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.VBATT)) {
					listOfSensorIds.add(SENSORS.Battery);
				}
				if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.GSR)) {
					listOfSensorIds.add(SENSORS.GSR);
				}
				break;
			case ACCEL_1:
				listOfSensorIds.add(SENSORS.LIS2DW12);
				break;
			case GYRO_ACCEL2:
				listOfSensorIds.add(SENSORS.LSM6DS3);
				break;
			case PPG:
				SENSORS sensorClassKey = getPpgSensorClassKey();
				if(sensorClassKey!=null) {
					listOfSensorIds.add(sensorClassKey);
				}
				break;
			case BIOZ:
				listOfSensorIds.add(SENSORS.BIOZ);
				break;
			default:
				break;
		}
		return listOfSensorIds;
	}

	public int calculateFifoBlockSize(SENSORS sensorClassKey) {
		int dataBlockSize = Integer.MIN_VALUE;
		switch(sensorClassKey) {
			case LIS2DW12:
				dataBlockSize = SensorLIS2DW12.FIFO_SIZE_IN_CHIP;
				break;
			case LSM6DS3:
				AbstractSensor abstractSensor = getSensorClass(SENSORS.LSM6DS3);
				if(abstractSensor!=null) {
					SensorLSM6DS3 sensorLSM6DS3 = (SensorLSM6DS3)abstractSensor;
					dataBlockSize = sensorLSM6DS3.getFifoByteSizeInChip();
				}
				break;
			case MAX86150:
			case MAX86916:
				dataBlockSize = 0;
				// 3 bytes each channel with a max of 32 samples per FIFO each
				int fifoBytesPerChannel = 3*SensorMAX86XXX.MAX_SAMPLES_PER_FIFO;
				if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED)) {
					dataBlockSize += fifoBytesPerChannel;
				}
				if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)) {
					dataBlockSize += fifoBytesPerChannel;
				}
				if (sensorClassKey == SENSORS.MAX86150) {
					if (isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86150_ECG)) {
						dataBlockSize += fifoBytesPerChannel;
					}
				}
				if (sensorClassKey == SENSORS.MAX86916) {
					if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN)) {
						dataBlockSize += fifoBytesPerChannel;
					}
					if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE)) {
						dataBlockSize += fifoBytesPerChannel;
					}
				}
				break;
			case Battery:
			case GSR:
				// There is a single buffer of a fixed size for all ADC channels (e.g., battery, GSR) no matter how many are enabled
				dataBlockSize = ADC_BYTE_BUFFER_SIZE;
				break;
			case BIOZ:
				// TODO add support in future if needed
				break;
			default:
				break;
		}
		return dataBlockSize;
	}

	public void setProtocol(VerisenseProtocolByteCommunication protocol) {
		protocol.addRadioListener(new RadioListener() {

			@Override
			public void startOperationCallback(BT_STATE currentOperation, int totalNumOfCmds) {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendProgressReportCallback(BluetoothProgressReportPerCmd progressReportPerCmd) {
				// TODO Auto-generated method stub

			}

			@Override
			public void isNowStreamingCallback() {
				// TODO Auto-generated method stub

			}

			@Override
			public void initialiseStreamingCallback() {
				// TODO Auto-generated method stub

			}

			@Override
			public void hasStopStreamingCallback() {
				// TODO Auto-generated method stub

			}

			@Override
			public void finishOperationCallback(BT_STATE currentOperation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsStreaming(boolean isStreaming) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsSensing(boolean isSensing) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsSDLogging(boolean isSdLogging) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsInitialised(boolean isInitialised) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsDocked(boolean isDocked) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetHaveAttemptedToRead(boolean haveAttemptedToRead) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventResponseReceived(int responseCommand, Object parsedResponse) {
				System.out.println("VerisenseDevice Response: " + responseCommand + " " + parsedResponse.getClass().getSimpleName());
				
				System.out.println(((AbstractPayload)parsedResponse).generateDebugString());
				
				if(parsedResponse instanceof StatusPayload) {
					status = (StatusPayload) parsedResponse;
					
				} else if(parsedResponse instanceof ProdConfigPayload) {
					prodConfigPayload = (ProdConfigPayload) parsedResponse;
					setUniqueId(prodConfigPayload.verisenseId);
					setExpansionBoardDetails(prodConfigPayload.expansionBoardDetails);
					setShimmerVersionObjectAndCreateSensorMap(prodConfigPayload.shimmerVerObject);
					//TODO remove below when tested
					printSensorParserAndAlgoMaps();
					
				} else if(parsedResponse instanceof OpConfigPayload) {
					opConfig = (OpConfigPayload) parsedResponse;
					configBytesParseAndInitialiseAlgorithms(opConfig.getPayloadContents(), COMMUNICATION_TYPE.BLUETOOTH);
					
				} else if(parsedResponse instanceof TimePayload) {
					//TODO needed?
				}
				
			}

			@Override
			public void eventNewResponse(byte[] responseBytes) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventNewPacket(byte[] packetByteArray, long pcTimestamp) {
				// TODO Auto-generated method stub
				System.out.println("VerisenseDevice New Packet: " + pcTimestamp);
				
				try {
					DataBlockDetails dataBlockDetails = parseDataBlockMetaData(packetByteArray);
					parseDataBlockData(dataBlockDetails, packetByteArray, BYTE_COUNT.PAYLOAD_CONTENTS_GEN8_SENSOR_ID + BYTE_COUNT.PAYLOAD_CONTENTS_RTC_BYTES_TICKS);
					
					System.out.println("Number of ObjectClusters generated: " + dataBlockDetails.getOjcArray().length);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void eventLogAndStreamStatusChangedCallback(int lastSentInstruction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventError(ShimmerException dE) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventDockedStateChange() {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventAckReceived(int lastSentInstruction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void disconnected() {
				// TODO Auto-generated method stub

			}

			@Override
			public void connected() {
				// TODO Auto-generated method stub

			}
		});
	}

	public DataBlockDetails parseDataBlockMetaData(byte[] byteBuffer) throws IOException {
		return parseDataBlockMetaData(byteBuffer, 0, 0, 0, 0);
	}

	public DataBlockDetails parseDataBlockMetaData(byte[] byteBuffer, int dataBlockStartByteIndexInPayload, int dataBlockStartByteIndexInFile, int dataBlockIndexInPayload, int payloadIndex) throws IOException {
		int sensorId = byteBuffer[dataBlockStartByteIndexInPayload];
		
		int ticksByteIndex = dataBlockStartByteIndexInPayload + BYTE_COUNT.PAYLOAD_CONTENTS_GEN8_SENSOR_ID;
		long rwcEndTimeTicks = VerisenseTimeDetails.parseTimeTicksAtIndex(byteBuffer, ticksByteIndex);
		
		DataBlockDetails dataBlockDetails = null;
		DATABLOCK_SENSOR_ID[] payloadSensorIdValues = DATABLOCK_SENSOR_ID.values();
		// -1 because PPG_MAX86150 has been temporarily added to PAYLOAD_SENSOR_ID 
		if(sensorId>0 && sensorId<payloadSensorIdValues.length-1) {
			DATABLOCK_SENSOR_ID datablockSensorId = payloadSensorIdValues[sensorId];
			
			List<SENSORS> listOfSensorClassKeys = getOrCreateListOfSensorClassKeysForDataBlockId(datablockSensorId);
			
			dataBlockDetails = new DataBlockDetails(datablockSensorId, payloadIndex, dataBlockIndexInPayload, listOfSensorClassKeys, 
					dataBlockStartByteIndexInFile, dataBlockStartByteIndexInPayload, 
					rwcEndTimeTicks, isPayloadDesignV11orAbove());

			SENSORS firstSensorClasskey = dataBlockDetails.listOfSensorClassKeys.get(0);
			int qtySensorDataBytesInDatablock = calculateFifoBlockSize(firstSensorClasskey);
			int dataPacketSize = 0;
			for(SENSORS sensorClassKey:dataBlockDetails.getListOfSensorClassKeys()) {
				dataPacketSize += getExpectedDataPacketSize(sensorClassKey);
			}
			// All sensors/channels within a single datablock share the same sampling rate so it's ok to pick the first one here
			double samplingRate = getSamplingRateForSensor(firstSensorClasskey);
			
			dataBlockDetails.setMetadata(qtySensorDataBytesInDatablock, dataPacketSize, samplingRate);
		} else {
			IOException e = new IOException("Error parsing Data Block. SensorID=" + UtilShimmer.intToHexStringFormatted(sensorId, 1, false) + " not supported. "
					+ "File byte index " + UtilShimmer.intToHexStringFormatted(dataBlockStartByteIndexInFile, 4, true)
					+ ", Payload index " + payloadIndex
					+ ", DataBlock index in payload " + dataBlockIndexInPayload
					+ ". See console report.");
			throw(e);
		}
		
		return dataBlockDetails;
	}
	
	public HashMap<DATABLOCK_SENSOR_ID, List<SENSORS>> getMapOfSensorIdsPerDataBlock() {
		return mapOfSensorIdsPerDataBlock;
	}
	
	public void clearMapOfSensorClassKeysForDataBlockId() {
		mapOfSensorIdsPerDataBlock.clear();
	}
	
	public List<SENSORS> getOrCreateListOfSensorClassKeysForDataBlockId(DATABLOCK_SENSOR_ID datablockSensorId) {
		List<SENSORS> listOfSensorClassKeys = mapOfSensorIdsPerDataBlock.get(datablockSensorId);
		if(listOfSensorClassKeys==null) {
			listOfSensorClassKeys = getSensorKeysForDatablockId(datablockSensorId);
			mapOfSensorIdsPerDataBlock.put(datablockSensorId, listOfSensorClassKeys);
		}
		return listOfSensorClassKeys;
	}

	public void parseDataBlockData(DataBlockDetails dataBlockDetails, byte[] byteBuffer, int currentByteIndex) {
		double timeMsCurrentSample = dataBlockDetails.getStartTimeRwcMs();
		
		for(int y=0;y<dataBlockDetails.getSampleCount();y++) {
			byte[] byteBuf = new byte[dataBlockDetails.dataPacketSize];
			System.arraycopy(byteBuffer, currentByteIndex, byteBuf, 0, byteBuf.length);
			
			ObjectCluster ojcCurrent = buildMsgForSensor(byteBuf, dataBlockDetails.listOfSensorClassKeys, timeMsCurrentSample);
			
			dataBlockDetails.setOjcArrayAtIndex(y, ojcCurrent);

			timeMsCurrentSample += (dataBlockDetails.getTimestampDiffInS() * 1000);

			currentByteIndex += dataBlockDetails.dataPacketSize;
		}
	}

}