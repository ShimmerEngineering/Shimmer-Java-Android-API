package com.shimmerresearch.driver;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.algorithms.AlgorithmResultObject;
import com.shimmerresearch.algorithms.ConfigOptionDetailsAlgorithm;
import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmDetails.SENSOR_CHECK_METHOD;
import com.shimmerresearch.algorithms.AlgorithmLoaderInterface;
import com.shimmerresearch.algorithms.orientation.OrientationModule6DOFLoader;
import com.shimmerresearch.algorithms.orientation.OrientationModule9DOFLoader;
import com.shimmerresearch.bluetooth.DataProcessingInterface;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.wiredProtocol.UartComponentPropertyDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails.BATTERY_LEVEL;
import com.shimmerresearch.driverUtilities.ShimmerLogDetails;
import com.shimmerresearch.driverUtilities.ShimmerSDCardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

public abstract class ShimmerDevice extends BasicProcessWithCallBack implements Serializable{

	private static final long serialVersionUID = 5087199076353402591L;
	
	public static final String DEFAULT_DOCKID = "Default.01";
	public static final int DEFAULT_SLOTNUMBER = -1;
	public static final String DEFAULT_SHIMMER_NAME = "Shimmer";
	public static final String DEFAULT_EXPERIMENT_NAME = "DefaultTrial";
	public static final String DEFAULT_MAC_ID = "";
	public static final String DEVICE_ID = "Device_ID";
	public static final String STRING_CONSTANT_PENDING = "Pending";
	public static final String STRING_CONSTANT_UNKNOWN = "Unknown";
	public static final String STRING_CONSTANT_NOT_AVAILABLE = "N/A";
	public static final String STRING_CONSTANT_SD_ERROR = "SD Error";
	
	protected static final int MAX_CALIB_DUMP_MAX = 4096;
	
	public static final String INVALID_TRIAL_NAME_CHAR = "[^A-Za-z0-9._]";
	
	/**Holds unique location information on a dock or COM port number for Bluetooth connection*/
	public String mUniqueID = "";
	
	/** A shimmer device will have multiple sensors, depending on HW type and revision, these type of sensors can change, this holds a list of all the sensors for different versions. This only works with classes which implements the ShimmerHardwareSensors interface. E.g. ShimmerGQ. A single AbstractSensor (e.g., for MPU92X50) class can contain multiple SensorDetails (e.g., Accel, gyro etc.) */
	protected LinkedHashMap<SENSORS, AbstractSensor> mMapOfSensorClasses = new LinkedHashMap<SENSORS, AbstractSensor>();
	protected LinkedHashMap<Integer, SensorDetails> mSensorMap = new LinkedHashMap<Integer, SensorDetails>();
	/** The contents of Parser are kept in a fixed order based on the SensorMapKey */ 
	protected HashMap<COMMUNICATION_TYPE, TreeMap<Integer, SensorDetails>> mParserMap = new HashMap<COMMUNICATION_TYPE, TreeMap<Integer, SensorDetails>>();
	protected Map<String, ConfigOptionDetailsSensor> mConfigOptionsMap = new HashMap<String, ConfigOptionDetailsSensor>();
	protected TreeMap<Integer, SensorGroupingDetails> mSensorGroupingMap = new TreeMap<Integer, SensorGroupingDetails>();
	
	/** Contains all loaded Algorithm modules */
	protected Map<String, AbstractAlgorithm> mMapOfAlgorithmModules = new HashMap<String, AbstractAlgorithm>();
	/** All supported channels based on hardware, expansion board and firmware */
//	protected Map<String, AlgorithmDetails> mMapOfAlgorithmDetails = new LinkedHashMap<String, AlgorithmDetails>();
	/** for tile generation in GUI configuration */ 
	protected TreeMap<Integer, SensorGroupingDetails> mMapOfAlgorithmGrouping = new TreeMap<Integer, SensorGroupingDetails>();
	protected Map<String, ConfigOptionDetailsAlgorithm> mConfigOptionsMapAlgorithms = new HashMap<String, ConfigOptionDetailsAlgorithm>();

	public List<COMMUNICATION_TYPE> mListOfAvailableCommunicationTypes = new ArrayList<COMMUNICATION_TYPE>();

	/** Used in UART command through the base/dock*/
	public String mMacIdFromUart = DEFAULT_MAC_ID;
	public String mShimmerUserAssignedName = "";//DEFAULT_SHIMMER_NAME; // This stores the user assigned name
	public String mAlternativeName = "";
	public HashMap<COMMUNICATION_TYPE, Double> mMapOfSamplingRatesShimmer = new HashMap<COMMUNICATION_TYPE, Double>(); // 51.2Hz is the default sampling rate 
//	protected double mSamplingRateShimmer; 	                                        	// 51.2Hz is the default sampling rate 
	{
		mMapOfSamplingRatesShimmer.put(COMMUNICATION_TYPE.SD, 51.2);
		mMapOfSamplingRatesShimmer.put(COMMUNICATION_TYPE.BLUETOOTH, 51.2);
	}

	public String mDockID = DEFAULT_DOCKID;
	public DEVICE_TYPE mDockType = DEVICE_TYPE.UNKOWN;
	public int mSlotNumber = DEFAULT_SLOTNUMBER;

	public ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	public ExpansionBoardDetails mExpansionBoardDetails = new ExpansionBoardDetails();
	public ShimmerBattStatusDetails mShimmerBattStatusDetails = new ShimmerBattStatusDetails(); 
	public ShimmerSDCardDetails mShimmerSDCardDetails = new ShimmerSDCardDetails(); 

	public boolean mReadHwFwSuccess = false;
	public boolean mConfigurationReadSuccess = false;
	public boolean mReadDaughterIDSuccess = false;
	public boolean writeRealWorldClockFromPcTimeSuccess = false;
	
	protected boolean mIsConnected = false;
	protected boolean mIsSensing = false;
	protected boolean mIsStreaming = false;											// This is used to monitor whether the device is in streaming mode
	protected boolean mIsInitialised = false;
	protected boolean mIsDocked = false;
	protected boolean mHaveAttemptedToReadConfig = false;

	//BSL related start
	public String mActivityLog = "";
	public int mFwImageWriteProgress = 0;
	public int mFwImageTotalSize = 0;
	public float mFwImageWriteSpeed = 0;
	public String mFwImageWriteCurrentAction = "";
	public List<MsgDock> mListOfFailMsg = new ArrayList<MsgDock>();
	//BSL related end
	
	public List<ShimmerException> mListOfDeviceExceptions = new ArrayList<ShimmerException>();
	
	//TODO: are these variables too specific to different versions of Shimmer HW?
	public long mShimmerRealTimeClockConFigTime = 0;
	public long mShimmerLastReadRealTimeClockValue = 0;
	public String mShimmerLastReadRtcValueParsed = "";
	protected InfoMemLayout mInfoMemLayout;// = new InfoMemLayoutShimmer3(); //default
	protected byte[] mConfigBytes = InfoMemLayout.createEmptyInfoMemByteArray(512);
	/**shows the original contents of the Infomem any configuration is changed */
	protected byte[] mInfoMemBytesOriginal = InfoMemLayout.createEmptyInfoMemByteArray(512);
	
	public byte[] mCalibBytes = new byte[]{};
	public HashMap<Integer, String> mCalibBytesDescriptions = new HashMap<Integer, String>();

	protected String mTrialName = "";
	protected long mConfigTime; //this is in milliseconds, utc

	//-------- Packet reception rate start --------------
	public static final double DEFAULT_RECEPTION_RATE = 0.0;//100.0;
	public long mPacketReceivedCountCurrent = 0;
	public long mPacketExpectedCountCurrent = 0;
	private double mPacketReceptionRateCurrent = DEFAULT_RECEPTION_RATE;
	
	public long mPacketReceivedCountOverall = 0;
	public long mPacketExpectedCountOverall = 0;
	private double mPacketReceptionRateOverall = DEFAULT_RECEPTION_RATE;
	
	private long mPacketLossCountPerTrial = 0;		//Used by ShimmerBluetooth
	//-------- Packet reception rate end --------------
	
	//Events markers
	protected int mEventMarkersCodeLast = 0;
	protected boolean mEventMarkersIsPulse = false;
	protected int mEventMarkerDefault = -1; // using -1 as the default event marker value as as a value of 0 was hanging the plots and the software
	public int mEventMarkers = mEventMarkerDefault;
	
	public transient ObjectCluster mLastProcessedObjectCluster = null;
	public List<ShimmerLogDetails> mListofLogs = new ArrayList<ShimmerLogDetails>();

	protected long mEnabledSensors = (long)0;												// This stores the enabled sensors
	protected long mDerivedSensors = (long)0;	

	protected String mComPort = "";
	public transient CommsProtocolRadio mCommsProtocolRadio = null;
	public BT_STATE mBluetoothRadioState = BT_STATE.DISCONNECTED;
	
	protected int mInternalExpPower=-1;													// This shows whether the internal exp power is enabled.
	
	protected DataProcessingInterface mDataProcessing;

	public boolean mVerboseMode = true;
//	public static TreeMap<Integer,String> mMapOfErrorCodes = new TreeMap<Integer,String>();

	private static final List<AlgorithmLoaderInterface> OPEN_SOURCE_ALGORITHMS = Arrays.asList(
			new OrientationModule6DOFLoader(), 
			new OrientationModule9DOFLoader());

	public static final class DatabaseConfigHandle{
		public static final String SAMPLE_RATE = "Sample_Rate";
		public static final String ENABLE_SENSORS = "Enable_Sensors";
		public static final String DERIVED_SENSORS = "Derived_Sensors";
		
		public static final String USER_BUTTON = "User_Button";
		public static final String RTC_SOURCE = "Rtc_Source"; 
		public static final String SYNC_CONFIG = "Sync";
		public static final String MASTER_CONFIG = "Master";
		public static final String SINGLE_TOUCH_START = "Single_Touch_Start";
		public static final String TXCO = "Txco";
		public static final String BROADCAST_INTERVAL = "Broadcast_Interval";
		public static final String BAUD_RATE = "Baud_Rate";
		public static final String TRIAL_ID = "Trial_Id";
		public static final String N_SHIMMER = "NShimmer";
		
		public static final String SHIMMER_VERSION = "Shimmer_Version";
		public static final String FW_VERSION = "FW_ID";
		public static final String FW_VERSION_MAJOR = "FW_Version_Major";
		public static final String FW_VERSION_MINOR = "FW_Version_Minor";
		public static final String FW_VERSION_INTERNAL = "FW_Version_Internal";
		
		public static final String INITIAL_TIMESTAMP = "Initial_TimeStamp";
		public static final String EXP_BOARD_ID = "Exp_Board_Id";
		public static final String EXP_BOARD_REV = "Exp_Board_Rev";
		public static final String EXP_BOARD_REV_SPEC = "Exp_Board_Rev_Special";
		
		public static final String CONFIG_TIME = "Config_Time";
		
		
		//Shimmer3 specific
		public static final String EXP_PWR = "Exp_PWR";
		public static final String REAL_TIME_CLOCK_DIFFERENCE = "RTC_Difference";
	}

	// --------------- Abstract Methods Start --------------------------
	
	/**
	 * Performs a deep copy of the parent class by Serializing
	 * @return ShimmerDevice the deep copy of the current ShimmerDevice (should
	 *         be substituted by the extended ShimmerDevice instance)
	 * @see java.io.Serializable
	 */
	public abstract ShimmerDevice deepClone();

	// Device sensor map related
	public abstract void sensorAndConfigMapsCreate();
	/**
	 * @param object in some cases additional details might be required for building the packer format, e.g. inquiry response
	 */
	protected abstract void interpretDataPacketFormat(Object object,COMMUNICATION_TYPE commType);
	public abstract void configBytesParse(byte[] configBytes);
	public abstract byte[] configBytesGenerate(boolean generateForWritingToShimmer);
	public abstract void createConfigBytesLayout();


	// --------------- Abstract Methods End --------------------------

	/**
	 * Constructor for this class
	 */
	public ShimmerDevice(){
		setThreadName(this.getClass().getSimpleName());
		setupDataProcessing();
	}
	
	// --------------- Get/Set Methods Start --------------------------
	
	public void setShimmerVersionObject(ShimmerVerObject sVO) {
		mShimmerVerObject = sVO;
	}
	
	public void setShimmerVersionInfoAndCreateSensorMap(ShimmerVerObject sVO) {
		setShimmerVersionObject(sVO);
		sensorAndConfigMapsCreate();
	}

	public void clearShimmerVersionInfo() {
		setShimmerVersionInfoAndCreateSensorMap(new ShimmerVerObject());
	}

	//TODO draft code
	private void updateSensorDetailsWithCommsTypes() {
		if(mMapOfSensorClasses!=null){
			for(AbstractSensor absensorSensor:mMapOfSensorClasses.values()){
				absensorSensor.updateSensorDetailsWithCommsTypes(mListOfAvailableCommunicationTypes);
			}
		}
	}
	
	//TODO draft code
	private void updateSamplingRatesMapWithCommsTypes() {
		//Add if not there
		for(COMMUNICATION_TYPE commType:mListOfAvailableCommunicationTypes){
			if(!mMapOfSamplingRatesShimmer.containsKey(commType)){
				mMapOfSamplingRatesShimmer.containsKey(getSamplingRateShimmer());
			}
		}
		
		//Remove if not supported
		for(COMMUNICATION_TYPE commType:mMapOfSamplingRatesShimmer.keySet()){
			if(!mListOfAvailableCommunicationTypes.contains(commType)){
				mMapOfSamplingRatesShimmer.remove(getSamplingRateShimmer());
			}
		}

	}


	/** setShimmerVerionObject should be used instead
	 * @param hardwareVersion the mHardwareVersion to set
	 */
	public void setHardwareVersion(int hardwareVersion) {
		ShimmerVerObject sVOHw = new ShimmerVerObject(hardwareVersion, getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
		setShimmerVersionObject(sVOHw);
	}
	
	public void clearShimmerVersionObject() {
		setShimmerVersionObject(new ShimmerVerObject());
	}
	
	public void generateSensorAndParserMaps(){
		//Update sensorMap from all supported sensors that were generated in mMapOfSensorClasses
		generateSensorMap();
		//Create Parser from enabled sensors
		generateParserMap();
		generateConfigOptionsMap();
		generateSensorGroupingMap();
	}

	private void generateSensorMap() {
		mSensorMap = new LinkedHashMap<Integer, SensorDetails>();
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			mSensorMap.putAll(abstractSensor.mSensorMap);
		}
	}

	public void generateParserMap() {
		//TODO switch ParserMap value to list of sensorMapKeys as below?
//		HashMap<COMMUNICATION_TYPE, List<Integer>> parseMap = new HashMap<COMMUNICATION_TYPE, List<Integer>>(); 

		mParserMap = new HashMap<COMMUNICATION_TYPE, TreeMap<Integer, SensorDetails>>();
//		for(COMMUNICATION_TYPE commType:COMMUNICATION_TYPE.values()){
		for(COMMUNICATION_TYPE commType:mListOfAvailableCommunicationTypes){
			for(Integer sensorMapKey:mSensorMap.keySet()){
				SensorDetails SensorDetails = getSensorDetails(sensorMapKey);
				if(SensorDetails.isEnabled(commType)){
					TreeMap<Integer, SensorDetails> parserMapPerComm = mParserMap.get(commType);
					if(parserMapPerComm==null){
						parserMapPerComm = new TreeMap<Integer, SensorDetails>();
						mParserMap.put(commType, parserMapPerComm);
					}
					parserMapPerComm.put(sensorMapKey, SensorDetails);
				}
			}
		}
	}

	public void generateConfigOptionsMap() {
		mConfigOptionsMap = new HashMap<String, ConfigOptionDetailsSensor>();
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			HashMap<String, ConfigOptionDetailsSensor> configOptionsMapPerSensor = abstractSensor.getConfigOptionsMap();
				
			if(configOptionsMapPerSensor!=null && configOptionsMapPerSensor.keySet().size()>0){
				mConfigOptionsMap.putAll(configOptionsMapPerSensor);
				// taking out duplicates for orientation algorithm config options 
//					for(String s: configOptionsMapPerSensor.keySet()){
//						if(mConfigOptionsMap.containsKey(s)){
//							//do nothing 
//						}else{
//							mConfigOptionsMap.put(s,configOptionsMapPerSensor.get(s));
//						}				
//					
				
			//	}
			}	
		}
	}
	
	//New approach - should not be run when using ShimmerObject
	public void generateSensorGroupingMap(){
		mSensorGroupingMap = new TreeMap<Integer, SensorGroupingDetails>(); 
		for(AbstractSensor sensor:mMapOfSensorClasses.values()){
			Map<Integer, SensorGroupingDetails> sensorGroupingMap = sensor.getSensorGroupingMap(); 
			if(sensorGroupingMap!=null){
				mSensorGroupingMap.putAll(sensorGroupingMap);
			}
		}
	}

	/**	
	 * @return the mSensorGroupingMap
	 */
	public TreeMap<Integer, SensorGroupingDetails> getSensorGroupingMap() {
		return mSensorGroupingMap;
	}

	/** 
	 * @return the mConfigOptionsMap
	 */
	public Map<String, ConfigOptionDetailsSensor> getConfigOptionsMap() {
		return mConfigOptionsMap;
	}
	
	/** 
	 * @return the mConfigOptionsMap
	 */
	public Map<String, ConfigOptionDetailsAlgorithm> getConfigOptionsMapAlorithms() {
		return mConfigOptionsMapAlgorithms;
	}
	
	public void setBattStatusDetails(ShimmerBattStatusDetails shimmerBattStatusDetails) {
		mShimmerBattStatusDetails = shimmerBattStatusDetails;
	}
	
	public void clearBattStatusDetails() {
		setBattStatusDetails(new ShimmerBattStatusDetails());
	}

	/**
	 * @param macIdFromUart the mMacIdFromUart to set
	 */
	public void setMacIdFromUart(String macIdFromUart) {
		this.mMacIdFromUart = macIdFromUart;
		updateThreadName();
	}
	
	/**
	 * @param shimmerUserAssignedName the mShimmerUserAssignedName to set
	 */
	public void setShimmerUserAssignedName(String shimmerUserAssignedName) {
		if(!shimmerUserAssignedName.isEmpty()){
			//Remove any invalid characters
			shimmerUserAssignedName = shimmerUserAssignedName.replaceAll(INVALID_TRIAL_NAME_CHAR, "");

			//Don't allow the first char to be numeric - causes problems with MATLAB variable names
			if(UtilShimmer.isNumeric("" + shimmerUserAssignedName.charAt(0))){
				shimmerUserAssignedName = "S" + shimmerUserAssignedName; 
			}
		}
		else{
			shimmerUserAssignedName = ShimmerObject.DEFAULT_SHIMMER_NAME + "_" + this.getMacIdFromUartParsed();
		}

		//Limit the name to 12 Char
		if(shimmerUserAssignedName.length()>12) {
			setShimmerUserAssignedNameNoLengthCheck(shimmerUserAssignedName.substring(0, 12));
		}
		else { 
			setShimmerUserAssignedNameNoLengthCheck(shimmerUserAssignedName);
		}
	}
	
	public void setShimmerUserAssignedNameNoLengthCheck(String shimmerUserAssignedName) {
		String nameToSet = shimmerUserAssignedName.replace(" ", "_");
		mShimmerUserAssignedName = nameToSet; 
	}
	
	public void setShimmerUserAssignedNameWithMac(String shimmerUserAssignedName) {
		//Don't allow the first char to be numeric - causes problems with MATLAB variable names
		if(shimmerUserAssignedName.length()==0 || UtilShimmer.isNumeric("" + shimmerUserAssignedName.charAt(0))){
			shimmerUserAssignedName = "S" + shimmerUserAssignedName; 
		}
		
		//Remove any invalid characters
		shimmerUserAssignedName = shimmerUserAssignedName.replaceAll(INVALID_TRIAL_NAME_CHAR, "");
			
		//Limit the name to 12 Char
		String addition = "_" + getMacIdParsed();
		if((shimmerUserAssignedName.length()+addition.length())>12) {
			setShimmerUserAssignedNameNoLengthCheck(shimmerUserAssignedName.substring(0, (12-addition.length())) + addition);
		}
		else { 
			setShimmerUserAssignedNameNoLengthCheck(shimmerUserAssignedName + addition);
		}
	}
	
	public void setLastReadRealTimeClockValue(long time) {
		mShimmerLastReadRealTimeClockValue = time;
		mShimmerLastReadRtcValueParsed = UtilShimmer.fromMilToDateExcelCompatible(Long.toString(time), false);
	}

	/**
	 * @param shimmerInfoMemBytes the shimmerInfoMemBytes to set
	 */
	public void setShimmerInfoMemBytes(byte[] shimmerInfoMemBytes) {
		configBytesParse(shimmerInfoMemBytes);
	}
	
	//------------------- Event marker code Start -------------------------------
	/**
	 * @param eventCode
	 * @param eventType //2 is the code for the pulse event, 1 is the code for the toggle event
	 */
	public void setEventTriggered(int eventCode, int eventType){
		mEventMarkersCodeLast = eventCode;
		
		if(mEventMarkers > 0){
			mEventMarkers = mEventMarkers + eventCode; 
		}
		else{
			mEventMarkers = mEventMarkers + eventCode + (-mEventMarkerDefault); 
		}
		
		//TOGGLE(1),
		//PULSE(2);
		//event type is defined in UtilDb, defined it here too
		if(eventType == 2){ 
			mEventMarkersIsPulse = true;
		}
	}
	
	public void setEventUntrigger(int eventCode){
		mEventMarkers = mEventMarkers - eventCode;
		if(mEventMarkers == 0){
			mEventMarkers = mEventMarkerDefault; // using -1 as the default event marker value as as a value of 0 was hanging the plots and the software
		}
	}
	
	public void untriggerEventIfLastOneWasPulse(){
		if(mEventMarkersIsPulse){
			mEventMarkersIsPulse = false;
			setEventUntrigger(mEventMarkersCodeLast);
		}
	}
	
	public void processEventMarkerCh(ObjectCluster objectCluster) {
		//event marker channel
		objectCluster.addDataToMap(Shimmer3.ObjectClusterSensorName.EVENT_MARKER,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, mEventMarkers);
		untriggerEventIfLastOneWasPulse();
	}

	//------------------- Event marker code End -------------------------------
	
	
	//------------------- Communication route related Start -------------------------------
	/** If the device instance is already created use this to add a dock communication type to the instance
	 * @param dockID
	 * @param slotNumber
	 */
	public void addDOCKCoummnicationRoute(String dockId, int slotNumber) {
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(COMMUNICATION_TYPE.DOCK);
	}
	
	public void clearDockInfo(){
		setDockInfo(DEFAULT_DOCKID, DEFAULT_SLOTNUMBER);
	}
	
	public void setDockInfo(String dockId, int slotNumber){
		mDockID = dockId;
		parseDockType();
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
	}

	public void addCommunicationRoute(COMMUNICATION_TYPE communicationType) {
		if(!mListOfAvailableCommunicationTypes.contains(communicationType)){
			mListOfAvailableCommunicationTypes.add(communicationType);
		}
//		Collections.sort(mListOfAvailableCommunicationTypes);
		
		if(communicationType==COMMUNICATION_TYPE.DOCK){
			setIsDocked(true);
		}
		
		//TODO temp here -> check if the best place for it
		updateSensorDetailsWithCommsTypes();
		updateSamplingRatesMapWithCommsTypes();
	}

	public void removeCommunicationRoute(COMMUNICATION_TYPE communicationType) {
		if(mListOfAvailableCommunicationTypes.contains(communicationType)){
			mListOfAvailableCommunicationTypes.remove(communicationType);
		}
//		Collections.sort(mListOfAvailableCommunicationTypes);
		
		if(communicationType==COMMUNICATION_TYPE.DOCK){
			setIsDocked(false);
			setFirstDockRead();
			clearDockInfo();
		}
		
		//TODO temp here -> check if the best place for it
		updateSensorDetailsWithCommsTypes();
		updateSamplingRatesMapWithCommsTypes();
	}
	//------------------- Communication route related End -------------------------------

	//------------------- SD card related Start -------------------------------
	public boolean isFirstSdAccess() {
		return mShimmerSDCardDetails.isFirstSdAccess();
	}

	public void setFirstSdAccess(boolean state) {
		this.mShimmerSDCardDetails.setFirstSdAccess(state);
	}

	public boolean isSDError() {
		return mShimmerSDCardDetails.isSDError();
	}

	public void setIsSDError(boolean state) {
		this.mShimmerSDCardDetails.setIsSDError(state);
	}

	public boolean isSDLogging(){
		return mShimmerSDCardDetails.isSDLogging();
	}	

	public void setIsSDLogging(boolean state){
		mShimmerSDCardDetails.setIsSDLogging(state);
	}	

	public void setShimmerDriveInfo(ShimmerSDCardDetails shimmerSDCardDetails) {
		mShimmerSDCardDetails = shimmerSDCardDetails;
	}
	
	public long getDriveTotalSpace() {
		return mShimmerSDCardDetails.getDriveTotalSpace();
	}

	public String getDriveUsedSpaceParsed() {
		return mShimmerSDCardDetails.getDriveUsedSpaceParsed();
	}

	public long getDriveUsedSpace() {
		return mShimmerSDCardDetails.getDriveUsedSpace();
	}

	public long getDriveUsedSpaceKB() {
		return mShimmerSDCardDetails.getDriveUsedSpaceKB();
	}

	public String getDriveTotalSpaceParsed() {
		return mShimmerSDCardDetails.getDriveTotalSpaceParsed();
	}

	public long getDriveUsableSpace() {
		return mShimmerSDCardDetails.getDriveUsableSpace();
	}

	public long getDriveFreeSpace() {
		return mShimmerSDCardDetails.getDriveFreeSpace();
	}
	
	public void setDriveUsedSpaceKB(long driveUsedSpaceKB) {
		mShimmerSDCardDetails.setDriveUsedSpaceKB(driveUsedSpaceKB);
	}

	public void setDriveUsedSpace(long driveUsedSpace) {
		mShimmerSDCardDetails.setDriveUsedSpace(driveUsedSpace);
	}

	public String getDriveCapacityParsed() {
		String strPending = STRING_CONSTANT_PENDING;
		
		if(!isSupportedSdCardAccess()){
			return STRING_CONSTANT_UNKNOWN;
		}

		if(isSDError()){
			return STRING_CONSTANT_SD_ERROR;
		}
		else {
			String driveUsedSpaceParsed = getDriveUsedSpaceParsed();
			if (!driveUsedSpaceParsed.isEmpty()) {
				// The extra check below was added for NeuroLynQ in order to
				// increase the granularity of the reported used space while
				// logging. 
				if(this.isShimmerGenGq() && isSDLogging()){
//					driveUsedSpaceParsed = Long.toString(getDriveUsedSpace()) + " B";
					driveUsedSpaceParsed = Long.toString(getDriveUsedSpaceKB()) + " KB";
				}
				
				return (driveUsedSpaceParsed + " / " + getDriveTotalSpaceParsed());
			}
			else{
				return (isHaveAttemptedToRead() ? STRING_CONSTANT_UNKNOWN : (isFirstSdAccess() ? strPending: STRING_CONSTANT_UNKNOWN));
			}
		}
	}
	
	//------------------- SD card related End -------------------------------

	
	public ShimmerVerObject getShimmerVerObject() {
		return mShimmerVerObject;
	}

	/**
	 * @return the mShimmerVersion
	 */
	public int getHardwareVersion() {
		return mShimmerVerObject.mHardwareVersion;
	}

	/**
	 * Get the FW Identifier. It is equal to 3 when LogAndStream, and equal to 1 when BTStream. 
	 * @return The FW identifier
	 */
	public int getFirmwareIdentifier() {
		return mShimmerVerObject.mFirmwareIdentifier;
	}
	
	public int getFirmwareVersionMajor(){
		return mShimmerVerObject.mFirmwareVersionMajor;
	}
	
	public int getFirmwareVersionMinor(){
		return mShimmerVerObject.mFirmwareVersionMinor;
	}

	public int getFirmwareVersionInternal(){
		return mShimmerVerObject.mFirmwareVersionInternal;
	}
	
	
	public int getFirmwareVersionCode(){
		return mShimmerVerObject.mFirmwareVersionCode;
	}
	
	public String getFirmwareVersionParsed(){
		return mShimmerVerObject.mFirmwareVersionParsed;
	}
	

	public String getHardwareVersionParsed(){
		return mShimmerVerObject.mHardwareVersionParsed;
	}

	

	public ExpansionBoardDetails getExpansionBoardDetails(){
		return mExpansionBoardDetails;
	}

	public int getExpansionBoardId(){
		return mExpansionBoardDetails.mExpansionBoardId;
	}
	
	public int getExpansionBoardRev(){
		return mExpansionBoardDetails.mExpansionBoardRev;
	}
	
	public int getExpansionBoardRevSpecial(){
		return mExpansionBoardDetails.mExpansionBoardRevSpecial;
	}
	
	/**
	 * @return the mExpansionBoardParsed
	 */
	public String getExpansionBoardParsed() {
		return mExpansionBoardDetails.getExpansionBoardParsed();
	}
	
	public String getExpansionBoardParsedWithVer() {
		return mExpansionBoardDetails.getExpansionBoardParsedWithVer();
	}

	public void clearExpansionBoardDetails(){
		mExpansionBoardDetails = new ExpansionBoardDetails();
	}

	public void setExpansionBoardDetailsAndCreateSensorMap(ExpansionBoardDetails eBD){
		setExpansionBoardDetails(eBD);
		sensorAndConfigMapsCreate();
	}
	
	public void setExpansionBoardDetails(ExpansionBoardDetails eBD){
		mExpansionBoardDetails  = eBD;
	}
	
	/**
	 * @return the mChargingState
	 */
	public String getChargingStateParsed() {
		return mShimmerBattStatusDetails.getChargingStatusParsed();
	}

	/**
	 * @return the mBattVoltage
	 */
	public String getBattVoltage() {
		return mShimmerBattStatusDetails.getBattVoltageParsed();
	}

	/**
	 * @return the mEstimatedChargePercentageParsed
	 */
	public String getEstimatedChargePercentageParsed() {
		return mShimmerBattStatusDetails.getEstimatedChargePercentageParsed();
	}
	
	/**
	 * @return the mEstimatedChargePercentage
	 */
	public Double getEstimatedChargePercentage() {
		return mShimmerBattStatusDetails.getEstimatedChargePercentage();
	}
	
	public BATTERY_LEVEL getEstimatedBatteryLevel() {
		return mShimmerBattStatusDetails.getEstimatedBatteryLevel();
	}

	
	/**
	 * @param docked the mDocked to set
	 */
	public void setIsDocked(boolean docked) {
		mIsDocked = docked;
	}

	/**
	 * @return the mDocked
	 */
	public boolean isDocked() {
		return mIsDocked;
	}

	public void setIsConnected(boolean state) {
		mIsConnected = state;
	}

	/**
	 * @return the mIsConnected
	 */
	public boolean isConnected() {
		return mIsConnected;
	}


    /** Returns true if device is streaming (Bluetooth)
     * @return
     */
    public boolean isStreaming(){
    	return mIsStreaming;
    }

	public void setIsStreaming(boolean state) {
		mIsStreaming = state;
	}

	/**Only used for LogAndStream
	 * @return
	 */
	public boolean isSensing(){
		return (mIsSensing || isSDLogging() || isStreaming());
	}
	
	public void setIsSensing(boolean state) {
		mIsSensing = state;
	}
	
	/**
	 * @param isInitialized the mSuccessfullyInitialized to set
	 */
	public void setIsInitialised(boolean isInitialized) {
		mIsInitialised = isInitialized;
	}
	
	/**
	 * @return the mIsInitialized
	 */
	public boolean isInitialised() {
		return mIsInitialised;
	}
	
	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean isHaveAttemptedToRead() {
		return haveAttemptedToRead();
	}

	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean haveAttemptedToRead() {
		return mHaveAttemptedToReadConfig;
	}
	
	public boolean isShimmerError(){
		if(isSDError()){
			return true;
		}
		return false;
	}
	
	/**
	 * @param haveAttemptedToRead the mHaveAttemptedToRead to set
	 */
	public void setHaveAttemptedToReadConfig(boolean haveAttemptedToRead) {
		mHaveAttemptedToReadConfig = haveAttemptedToRead;
	}

	/**
	 * @return the mMacIdFromUart
	 */
	public String getMacIdFromUart() {
		return mMacIdFromUart;
	}

	/**
	 * @return the mMacIdFromUartParsed
	 */
	public String getMacIdFromUartParsed() {
		int length = mMacIdFromUart.length();
		if(length>=12) {
//			return this.mMacIdFromUart.substring(8, 12);
			return this.mMacIdFromUart.substring(length-4, length);
		}		
		return "0000";
	}

	/**
	 * @return the mShimmerUserAssignedName
	 */
	public String getShimmerUserAssignedName() {
		return mShimmerUserAssignedName;
	}
	
	/**
	 * @return the mShimmerInfoMemBytes
	 */
	public byte[] getShimmerInfoMemBytes() {
		return mConfigBytes;
	}

	public byte[] getShimmerInfoMemBytesOriginal() {
		return mInfoMemBytesOriginal;
	}

	public HashMap<Integer, String> getMapOfConfigByteDescriptions() {
		if(mInfoMemLayout!=null){
			return mInfoMemLayout.getMapOfByteDescriptions();
		}
		return null;
	}
	
	
	/** Use getPacketReceptionRateOverall() instead
	 * @return
	 */
	@Deprecated
	public double getPacketReceptionRate(){
		return getPacketReceptionRateOverall();
	}

	public double getPacketReceptionRateOverall(){
		return mPacketReceptionRateOverall;
	}

	
	public void incrementPacketsReceivedCounters(){
		incrementPacketReceivedCountCurrent();
		incrementPacketReceivedCountOverall();
	}

	protected void resetPacketLossVariables() {
		mPacketLossCountPerTrial = 0;
		resetPacketReceptionOverallVariables();
		resetPacketReceptionCurrentVariables();
	}
	

	public void setPacketExpectedCountOverall(long packetReceivedCountOverall) {
		mPacketExpectedCountOverall = packetReceivedCountOverall;
	}
	
	public long getPacketReceivedCountOverall() {
		return mPacketReceivedCountOverall;
	}
	
	private void incrementPacketReceivedCountOverall() {
		mPacketReceivedCountOverall += 1;
	}

	public void resetPacketReceptionOverallVariables(){
		mPacketExpectedCountOverall = 0;
		mPacketReceivedCountOverall = 0;
		setPacketReceptionRateOverall(DEFAULT_RECEPTION_RATE);
	}

	public void setPacketReceptionRateOverall(double packetReceptionRateTrial){
		mPacketReceptionRateOverall = UtilShimmer.nudgeDouble(packetReceptionRateTrial, 0.0, 100.0);
//		mPacketReceptionRateOverall = packetReceptionRateTrial;
	}


	public long getPacketReceivedCountCurrent() {
		return mPacketReceivedCountCurrent;
	}

	public void incrementPacketReceivedCountCurrent() {
		mPacketReceivedCountCurrent += 1;
	}

	public void incrementPacketExpectedCountCurrent() {
		mPacketExpectedCountCurrent += 1;
	}

	public void resetPacketReceptionCurrentCounters(){
		mPacketExpectedCountCurrent = 0;
		mPacketReceivedCountCurrent = 0;
	}

	public void resetPacketReceptionCurrentVariables(){
		resetPacketReceptionCurrentCounters();
		setPacketReceptionRateCurrent(DEFAULT_RECEPTION_RATE); 
	}

	public void setPacketReceptionRateCurrent(double packetReceptionRateCurrent){
		// Need to keep in range because the Bluetooth data is processed in
		// blocks and not necessarily at a fixed number of packets per second.
		// Probably the same for other communication protocols
		mPacketReceptionRateCurrent = UtilShimmer.nudgeDouble(packetReceptionRateCurrent, 0.0, 100.0);
//		mPacketReceptionRateCurrent = packetReceptionRateCurrent;
	}

	public double getPacketReceptionRateCurrent(){
		return mPacketReceptionRateCurrent;
	}

	
	public void setPacketLossCountPerTrial(long packetLossCountPerTrial){
		mPacketLossCountPerTrial = packetLossCountPerTrial;
	}

	public long getPacketLossCountPerTrial(){
		return mPacketLossCountPerTrial;
	}

	
	
	 /**
	 * @return the mConfigTime
	 */
	public long getConfigTime() {
		return mConfigTime;
	}

	 /**
	 * @return the mConfigTime in a parsed String format (yyyy-MM-dd hh:MM:ss)
	 */
	public String getConfigTimeParsed() {
		return UtilShimmer.convertSecondsToDateString(mConfigTime);
	}

	public String getConfigTimeExcelCompatible() {
		return UtilShimmer.fromMilToDateExcelCompatible(Long.toString(mConfigTime*1000L), false);
	}

	
	


	// --------------- Get/Set Methods End --------------------------

	public InfoMemLayout getInfoMemLayout(){
		createInfoMemLayoutObjectIfNeeded();
		return mInfoMemLayout;
	}
	
	 /**
	 * @return the InfoMem byte size. HW and FW version needs to be set first for this to operate correctly.
	 */
	public int getExpectedInfoMemByteLength() {
		createInfoMemLayoutObjectIfNeeded();
		return mInfoMemLayout.mInfoMemSize;
	}
	
	public void createInfoMemLayoutObjectIfNeeded(){
		boolean create = false;
		if(mInfoMemLayout==null){
			create = true;
		}
		else {
			if(mInfoMemLayout.isDifferent(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal())){
				create = true;
			}
		}
		
		if(create){
			createConfigBytesLayout();
		}
	}
	
	protected void parseDockType(){
		if(mDockID.contains(HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASICDOCK.getLabel())){
			mDockType = DEVICE_TYPE.BASICDOCK;
			//Below to ensure Shimmer shows as unknown in BasicDock rather then pending
			setHaveAttemptedToReadConfig(true);
		}
		else if(mDockID.contains(HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASE15.getLabel())){
			mDockType = DEVICE_TYPE.BASE15;
		}
		else if(mDockID.contains(HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASE6.getLabel())){
			mDockType = DEVICE_TYPE.BASE6;
		}
		else {
			mDockType = DEVICE_TYPE.UNKOWN;
		}
	}
	
	public ObjectCluster buildMsg(byte[] dataPacketFormat, byte[] packetByteArray, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, long pcTimestamp){
		interpretDataPacketFormat(dataPacketFormat, commType);
		return buildMsg(packetByteArray, commType, isTimeSyncEnabled, pcTimestamp);
	}
	
	/** The packet format can be changed by calling interpretpacketformat
	 * @param newPacket
	 * @param commType
	 * @return
	 */
	public ObjectCluster buildMsg(byte[] newPacket, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, long pcTimestamp){
		boolean debug = false;
		
		if(debug)
			consolePrintLn("Packet: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(newPacket));
		
		ObjectCluster ojc = new ObjectCluster(mShimmerUserAssignedName, getMacId());
		ojc.mRawData = newPacket;
		ojc.createArrayData(getNumberOfEnabledChannels(commType));

		incrementPacketsReceivedCounters();

		if(debug)
			System.out.println("\nNew Parser loop. Packet length:\t" + newPacket.length);
		
		TreeMap<Integer, SensorDetails> parserMapPerComm = mParserMap.get(commType);
		if(parserMapPerComm!=null){
			int index=0;
			for (SensorDetails sensor:parserMapPerComm.values()){
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

				if(debug)
					System.out.println(sensor.mSensorDetailsRef.mGuiFriendlyLabel + "\texpectedPacketArraySize:" + length + "\tcurrentIndex:" + index);
				index += length;
			}
		}
		else{
			consolePrintErrLn("ERROR!!!! Parser map null");
		}
		
		//After sensor data has been processed, now process any filters or Algorithms 
		ojc = processData(ojc);
		
		return ojc;
	}
	
	/** Perform any processing required on the data. E.g. time sync, filtering and algorithms
	 * @param ojc
	 * @return
	 */
	protected ObjectCluster processData(ObjectCluster ojc) {
		if(mDataProcessing!=null){
			ojc = mDataProcessing.processData(ojc);
		}
		
		//now process the enabled algorithms
		ojc = processAlgorithms(ojc);
		return ojc;
	}

	public ObjectCluster processAlgorithms(ObjectCluster ojc) {
		for (AbstractAlgorithm aA:getMapOfAlgorithmModules().values()) {
//			consolePrintErrLn.println(aA.mAlgorithmName + "\tisEnabled:\t" + aA.isEnabled());
			if (aA.isEnabled()) {
				try {
					AlgorithmResultObject algorithmResultObject = aA.processDataRealTime(ojc);
					if(algorithmResultObject!=null){
						ojc = (ObjectCluster) algorithmResultObject.mResult;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return ojc;
	}
	
	//TODO get below working if even needed
	/** Based on the SensorMap approach rather then legacy inquiry command */
	public void interpretDataPacketFormat(){
//		String [] signalNameArray=new String[MAX_NUMBER_OF_SIGNALS];
//		String [] signalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];
//		
//		int packetSize=0;//mTimeStampPacketByteSize; // Time stamp
//
//		int index = 0;
//		for(SensorDetails sED:mSensorMap.values()) {
//			if(sED.isEnabled() && !sED.isDerivedChannel()) {
//				for(ChannelDetails channelDetails:sED.mListOfChannels){
////				for(String channelDetailsName:sED.mListOfChannels){
////					ChannelDetails channelDetails = mChannelMap.get(channelDetailsName);
//					if(channelDetails.mDefaultNumBytes>0){
//						signalNameArray[index] = channelDetails.mObjectClusterName;
//						signalDataTypeArray[index] = channelDetails.mDefaultChannelDataType;
//						packetSize += channelDetails.mDefaultNumBytes;
//						
//						System.out.println(channelDetails.mObjectClusterName + "\t" + channelDetails.mDefaultChannelDataType + "\t" + channelDetails.mDefaultNumBytes);
//						index++;
//					}
//				}
//			}
//		}
//		
//		mSignalNameArray=signalNameArray;
//		mSignalDataTypeArray=signalDataTypeArray;
//		mPacketSize=packetSize;
	}

	// TODO this is a hack. I'm just picking off the first item from the
	// mListOfAvailableCommunicationTypes as all comms routes currently have the
	// same sampling rate.
	public int getExpectedDataPacketSize(){
		if(mListOfAvailableCommunicationTypes.size()==0){
			return 0;
		}
		generateParserMap();
		return getExpectedDataPacketSize(mListOfAvailableCommunicationTypes.get(0));
	}

	public int getExpectedDataPacketSize(COMMUNICATION_TYPE commsType){
		int dataPacketSize = 0;
		TreeMap<Integer, SensorDetails> parserMapPerCommType = mParserMap.get(commsType);
		if(parserMapPerCommType!=null){
			Iterator<SensorDetails> iterator = parserMapPerCommType.values().iterator();
			while(iterator.hasNext()){
				SensorDetails sensorDetails = iterator.next();
				int expectedPktSize = sensorDetails.getExpectedDataPacketSize();
				if(expectedPktSize>0){
//					System.out.println("Expected Packet size for:\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel + "\t:\t" + expectedPktSize);
					dataPacketSize += expectedPktSize;
				}
			}
		}
		return dataPacketSize;
	}
	
	public byte[] generateUartConfigMessage(UartComponentPropertyDetails cPD){
		//TODO: process common ShimmerDevice configs
		return null;
	}
	
	public void parseUartConfigResponse(UartComponentPropertyDetails cPD, byte[] response) {
		//TODO: process common ShimmerDevice configs
	}


	public int getNumberOfEnabledChannels(COMMUNICATION_TYPE commType){
		int total = 0;
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorEnabledDetails = iterator.next();
			if(sensorEnabledDetails.isEnabled(commType)){
				total += sensorEnabledDetails.getNumberOfChannels();
			}
		}
		return total;
	}
	
	/**
	 * @return the Parsed MAC address from any source available
	 */
	public String getMacIdParsed() {
		String macToUse = getMacId();
		if(macToUse.length()>=12) {
//			return macToUse.substring(8, 12);
			return macToUse.substring(macToUse.length()-4, macToUse.length());
		}
		return "";
	}
	
	/**
	 * @return the MAC address
	 */
	public String getMacId() {
		return mMacIdFromUart;
	}
	
	/** Sets the Shimmer Trial name but filters out invalid characters and maintains a maximum character length of 12. 
	 * @param trialName the trialName to set
	 */
	public void setTrialNameAndCheck(String trialName) {
		trialName = trialName.replaceAll(INVALID_TRIAL_NAME_CHAR, "");

		if(trialName.isEmpty()){
			trialName = DEFAULT_EXPERIMENT_NAME;
		}
		
		//Limit the name to 12 Char
		if(trialName.length()>12){
			trialName = trialName.substring(0, 11);
		}
		
		setTrialName(trialName);
	}

	/** Sets the Shimmer Trial name but bypasses all safety checks. Preferred use is setTrialNameAndCheck().
	 * @param trialName
	 * @see setTrialNameAndCheck
	 */
	public void setTrialName(String trialName) {
		this.mTrialName = trialName;
	}

	/**
	 * @return the mTrialName
	 */
	public String getTrialName() {
		return mTrialName;
	}
	
	public boolean isTrialNameInvalid(){
		return isTrialOrShimmerNameInvalid(getTrialName());
	}

	public boolean isShimmerNameInvalid(){
		return isTrialOrShimmerNameInvalid(getShimmerUserAssignedName());
	}

	public static boolean isTrialOrShimmerNameInvalid(String name){
		if(name.isEmpty()){
			return true;
		}
		
		Pattern p = Pattern.compile(INVALID_TRIAL_NAME_CHAR);
		Matcher m = p.matcher(name);
		return m.find();
	}
	
	/**
	 * @param mConfigTime the trialConfigTime to set
	 */
	public void setConfigTime(long trialConfigTime) {
		mConfigTime = trialConfigTime;
	}
	
	public void setTrialConfig(String trialName, long trialConfigTime) {
		setTrialName(trialName);
		mConfigTime = trialConfigTime;
	}

	public void setFirstDockRead() {
		setFirstSdAccess(true);
		mConfigurationReadSuccess = false;
		mReadHwFwSuccess = false;
		mReadDaughterIDSuccess = false;
		writeRealWorldClockFromPcTimeSuccess = false;
		setIsSDError(false);
	}
	
	// ----------------- Overrides from ShimmerDevice end -------------

	public LinkedHashMap<SENSORS, AbstractSensor> getMapOfSensorsClasses() {
		return mMapOfSensorClasses;
	}
	
	public boolean doesSensorKeyExist(int sensorKey) {
		return (mSensorMap.containsKey(sensorKey));
	}

	public boolean isSensorEnabled(int sensorMapKey){
		if(mSensorMap!=null) {
			SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
			if(sensorDetails!=null){
				return sensorDetails.isEnabled();
			}
		}		
		return false;
	}

	public boolean isSensorEnabled(COMMUNICATION_TYPE commType, int sensorKey) {
		SensorDetails sensorDetails = getSensorDetails(sensorKey);
		if(sensorDetails!=null){
			return sensorDetails.isEnabled(commType);
		}
	    return false;
	}

	public String getSensorLabel(int sensorMapKey) {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			String guiFriendlyLabel = abstractSensor.getSensorGuiFriendlyLabel(sensorMapKey);
			if(guiFriendlyLabel!=null){
				return guiFriendlyLabel;
			}
		}
		return null;
	}

	public List<ShimmerVerObject> getListOfCompatibleVersionInfoForSensor(int sensorMapKey) {
		SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
		if(sensorDetails!=null){
			return sensorDetails.mSensorDetailsRef.mListOfCompatibleVersionInfo;
		}
		return null;
	}
    
	/** Returns all sensor map keys in use */
	public Set<Integer> getSensorMapKeySet() {
		TreeSet<Integer> setOfSensorMapKeys = new TreeSet<Integer>();
		setOfSensorMapKeys.addAll(mSensorMap.keySet());
		return setOfSensorMapKeys;
	}
	
	/** Sets all default Shimmer settings in ShimmerDevice.
	 */
	public void setDefaultShimmerConfiguration() {
		// TODO Auto-generated method stub
	}
	
	public Object setConfigValueUsingConfigLabel(String configLabel, Object valueToSet){
		return setConfigValueUsingConfigLabel("", configLabel, valueToSet);
	}
	
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet){
		return setConfigValueUsingConfigLabel(Integer.toString(sensorMapKey), configLabel, valueToSet);
	}

	public Object setConfigValueUsingConfigLabel(String identifier, String configLabel, Object valueToSet){
		
//		System.err.println("GROUPNAME:\t" + (identifier.isEmpty()? "EMPTY":identifier) + "\tCONFIGLABEL:\t" + configLabel);
		
		Object returnValue = null;

		returnValue = setSensorClassSetting(identifier, configLabel, valueToSet);
		if(returnValue!=null){
			return returnValue;
		}
		
		//TODO remove below when ready and use "getAlgorithmConfigValueUsingConfigLabel"
		setAlgorithmSettings(identifier, configLabel, valueToSet);

		switch(configLabel){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN):
	        	setInternalExpPower((boolean)valueToSet);
	        	break;
//Integers
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER):
				setInternalExpPower((int)valueToSet);
            	break;
//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
        		setShimmerUserAssignedName((String)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
        		setTrialNameAndCheck((String)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE):
//			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_AND_SENSORS_SAMPLING_RATE):
	          	// don't let sampling rate be empty
	          	Double enteredSamplingRate;
	          	if(((String)valueToSet).isEmpty()) {
	          		enteredSamplingRate = 1.0;
	          	}            	
	          	else {
	          		enteredSamplingRate = Double.parseDouble((String)valueToSet);
	          	}
	          	
//	          	if(configLabel.equals(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_AND_SENSORS_SAMPLING_RATE)){
		      		setShimmerAndSensorsSamplingRate(enteredSamplingRate);
//	          	}
//	          	else if(configLabel.equals(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE)){
//	          		setSamplingRateShimmer(enteredSamplingRate);
//	          	}
	      		
	      		returnValue = Double.toString(getSamplingRateShimmer());
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.CALIBRATION_ALL):
				setMapOfSensorCalibrationAll((TreeMap<Integer, TreeMap<Integer, CalibDetails>>) valueToSet);
				break;
	        default:
	        	break;
		}
		
		
		return returnValue;		
	}
	
	public Object getConfigValueUsingConfigLabel(String configLabel){
		return getConfigValueUsingConfigLabel("", configLabel);
	}

	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel){
		return getConfigValueUsingConfigLabel(Integer.toString(sensorMapKey), configLabel);
	}

	public Object getConfigValueUsingConfigLabel(String identifier, String configLabel){
		Object returnValue = null;
		
		returnValue = getSensorClassSetting(identifier, configLabel);
		if(returnValue!=null){
			return returnValue;
		}
		
		//TODO remove below when ready and use "getAlgorithmConfigValueUsingConfigLabel"
		returnValue = getAlgorithmSettings(identifier, configLabel);
		if(returnValue!=null){
			return returnValue;
		}

		switch(configLabel){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN):
				returnValue = isInternalExpPower();
	        	break;
//Integers
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER):
				returnValue = getInternalExpPower();
            	break;
//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
				returnValue = getShimmerUserAssignedName();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
				returnValue = getTrialName();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME):
	        	returnValue = getConfigTimeParsed();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.CALIBRATION_ALL):
				returnValue = getMapOfSensorCalibrationAll();
				break;
	        default:
	        	break;
		}
		
		return returnValue;		
	}
	
	private Object setSensorClassSetting(String identifier, String configLabel, Object valueToSet) {
		Object returnValue = null;
		//TODO check sensor classes return a null if the setting is successfully found
		Integer sensorMapkey = Configuration.Shimmer3.SensorMapKey.RESERVED_ANY_SENSOR;
		try{
			sensorMapkey = Integer.parseInt(identifier);
		} catch(NumberFormatException eFE) {
			//DO nothing
		}
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			returnValue = abstractSensor.setConfigValueUsingConfigLabel(sensorMapkey, configLabel, valueToSet);
			if(returnValue!=null){
				return returnValue;
			}
		}
		return returnValue;
	}
	
	private Object getSensorClassSetting(String identifier, String configLabel) {
		Object returnValue = null;
		Integer sensorMapkey = Configuration.Shimmer3.SensorMapKey.RESERVED_ANY_SENSOR;
		try{
			sensorMapkey = Integer.parseInt(identifier);
		} catch(NumberFormatException eFE) {
			//DO nothing
		}
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			returnValue = abstractSensor.getConfigValueUsingConfigLabel(sensorMapkey, configLabel);
			if(returnValue!=null){
				return returnValue;
			}
		}
		return returnValue;
	}

	public void setAlgorithmSettings(String configLabel, Object valueToSet){
		setAlgorithmSettings(null, configLabel, valueToSet);
	}

	public void setAlgorithmSettings(String groupName, String configLabel, Object valueToSet){
		List<AbstractAlgorithm> listOfAlgorithms = null;
		
		if((groupName!=null && !groupName.isEmpty())){
			listOfAlgorithms = getListOfAlgorithmModulesPerGroup(groupName);		
		}
		else{
			listOfAlgorithms = getListOfAlgorithmModules();
		}
		
		// Treat algorithms differently because we normally want to set the same
		// config across multiple algorithm modules so return only after all have been set
		if(listOfAlgorithms!=null && !listOfAlgorithms.isEmpty()){
			for(AbstractAlgorithm abstractAlgorithm:listOfAlgorithms){	
				abstractAlgorithm.setSettings(configLabel, valueToSet);
			}
		}
	}

	public Object getAlgorithmSettings(String groupName, String configLabel){
		List<AbstractAlgorithm> listOfAlgorithms = null;
		if(groupName.isEmpty()){
//			listOfAlgorithms = getListOfEnabledAlgorithmModules();
			listOfAlgorithms = getListOfAlgorithmModules();
		}
		else {
//			listOfAlgorithms = getListOfEnabledAlgorithmModulesPerGroup(groupName);
			listOfAlgorithms = getListOfAlgorithmModulesPerGroup(groupName);
		}
		
		Object returnValue = null;
		if(listOfAlgorithms!=null && !listOfAlgorithms.isEmpty()){
			for(AbstractAlgorithm abstractAlgorithm:listOfAlgorithms){
				returnValue = abstractAlgorithm.getSettings(configLabel);
				if(returnValue!=null){
					return returnValue;
				}
			}
		}
		return returnValue;		
	}
	
	public HashMap<String, Object> getEnabledAlgorithmSettingsPerGroup(String groupName) {
		List<AbstractAlgorithm> listOfAlgorithms = getListOfEnabledAlgorithmModulesPerGroup(groupName);
		HashMap<String, Object> mapOfAlgorithmSettings = new HashMap<String, Object>();
		for(AbstractAlgorithm abstractAlgorithm:listOfAlgorithms){
			mapOfAlgorithmSettings.putAll(abstractAlgorithm.getAlgorithmSettings());
		}
		return mapOfAlgorithmSettings;
	}

	public HashMap<String, Object> getEnabledAlgorithmSettings() {
		List<AbstractAlgorithm> listOfAlgorithms = getListOfEnabledAlgorithmModules();
		HashMap<String, Object> mapOfAlgorithmSettings = new HashMap<String, Object>();
		for(AbstractAlgorithm abstractAlgorithm:listOfAlgorithms){
			mapOfAlgorithmSettings.putAll(abstractAlgorithm.getAlgorithmSettings());
		}
		return mapOfAlgorithmSettings;
	}
	
	public boolean isSupportedRtcConfigViaUart() {
		return mShimmerVerObject.isSupportedRtcConfigViaUart();
	}
	
	public boolean isSupportedConfigViaUart() {
		return mShimmerVerObject.isSupportedConfigViaUart();
	}

	public boolean isSupportedSdCardAccess() {
		return mShimmerVerObject.isSupportedSdCardAccess();
	}

	public boolean isSupportedCalibDump() {
		return mShimmerVerObject.isSupportedCalibDump();
	}

	public boolean isShimmerGen2(){
		return mShimmerVerObject.isShimmerGen2();
	}

	public boolean isShimmerGen3(){
		return mShimmerVerObject.isShimmerGen3();
	}

	public boolean isShimmerGen4(){
		return mShimmerVerObject.isShimmerGen4();
	}

	public boolean isShimmerGenGq(){
		return mShimmerVerObject.isShimmerGenGq();
	}

	public boolean isShimmerVideoDevice(){
		return mShimmerVerObject.isShimmerVideoDevice();
	}

	public void consolePrintExeptionLn(String message, StackTraceElement[] stackTrace) {
		if(mVerboseMode) {
			consolePrintErrLn(message + "\n" + UtilShimmer.convertStackTraceToString(stackTrace));
		}
	}

	public void consolePrintErrLn(String message) {
		if(mVerboseMode) {
			System.err.println(assemblePrintString(message));
		}
	}

	public void consolePrintLn(String message) {
		if(mVerboseMode) {
			System.out.println(assemblePrintString(message));
		}		
	}
	
	public void consolePrint(String message) {
		if(mVerboseMode) {
			System.out.print(message);
		}		
	}
	
	private String assemblePrintString(String message){
		Calendar rightNow = Calendar.getInstance();
		String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
				+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
				+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
				+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
		return (rightNowString 
				+ " " + getClass().getSimpleName() 
				+ " (Mac:" + getMacIdParsed() 
				+ " HashCode:" + Integer.toHexString(this.hashCode()) + ")" 
				+ ": " + message);
	}

	public void setVerboseMode(boolean verboseMode) {
		mVerboseMode = verboseMode;
	}

	/**Automatically control internal expansion board power based on sensor map
	 */
	protected void checkIfInternalExpBrdPowerIsNeeded() {
		for(SensorDetails sensorEnabledDetails:mSensorMap.values()) {
			if(sensorEnabledDetails.isInternalExpBrdPowerRequired()){
				mInternalExpPower = 1;
				return;
			}
			else{
				//TODO move to AbstractSensors
				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1)
					||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12)
					||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13)
					||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14)){
					
				}
				else {
					mInternalExpPower = 0;
				}
			}
		}
	}
	
	/**
	 * @param state the mInternalExpPower state to set
	 */
	public void setInternalExpPower(int state) {
		this.mInternalExpPower = state;
	}
	
	/**
	 * @return the mInternalExpPower
	 */
	public boolean isInternalExpPower() {
		return (mInternalExpPower > 0)? true:false;
	}
	
	/**
	 * @param state the mInternalExpPower state to set
	 */
	public void setInternalExpPower(boolean state) {
		mInternalExpPower = state? 1:0;
	}

	public int getInternalExpPower(){
		return mInternalExpPower;
	}


	/**
	 * @return the mSensorMap
	 */
	public Map<Integer, SensorDetails> getSensorMap() {
		return mSensorMap;
	}

	public boolean isLegacySdLog(){
		if (getFirmwareIdentifier()==FW_ID.SDLOG && getFirmwareVersionMajor()==0 && getFirmwareVersionMinor()==5){
			return true;
		}
		return false;
	}

	public boolean isDerivedSensorsSupported(){
		return isDerivedSensorsSupported(mShimmerVerObject);
	}

	public static boolean isDerivedSensorsSupported(ShimmerVerObject svo){
		if((isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.BTSTREAM, 0, 7, 0))
			||(isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.SDLOG, 0, 8, 69))
			||(isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 3, 17))
			||(svo.isShimmerGenGq())
			||(svo.isShimmerGen4())){
//			||(isVerCompatibleWith(svo, HW_ID.SHIMMER_4_SDK, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION))){
			return true;
		}
		return false;
	}

	/**
	 * Check each entry in the passed in list to see if the current Shimmer
	 * version information in this instance of ShimmerDevice is compatible with
	 * any of them. The hardware ID, firmware ID and expansion board ID need to
	 * be equal whereas the combination of firmware major:minor:internal needs
	 * to be greater or equal
	 * 
	 * @param listOfCompatibleVersionInfo
	 * @return
	 */
	public boolean isVerCompatibleWithAnyOf(List<ShimmerVerObject> listOfCompatibleVersionInfo) {

		if(listOfCompatibleVersionInfo==null || listOfCompatibleVersionInfo.isEmpty()) {
			return true;
		}

		for(ShimmerVerObject compatibleVersionInfo:listOfCompatibleVersionInfo){
			if(compatibleVersionInfo.mShimmerExpansionBoardId!=ShimmerVerDetails.ANY_VERSION) {
				if(getExpansionBoardId()!=compatibleVersionInfo.mShimmerExpansionBoardId) {
					continue;
				}
			}
			
			if(isThisVerCompatibleWith( 
					compatibleVersionInfo.mHardwareVersion,
					compatibleVersionInfo.mFirmwareIdentifier, 
					compatibleVersionInfo.mFirmwareVersionMajor, 
					compatibleVersionInfo.mFirmwareVersionMinor, 
					compatibleVersionInfo.mFirmwareVersionInternal)){
				return true;
			}

		}
		return false;
	}
	
	public static boolean isVerCompatibleWith(ShimmerVerObject svo, int hardwareVersion, int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal){
		return svo.compareVersions(hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}
	
	public boolean isThisVerCompatibleWith(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal){
		return UtilShimmer.compareVersions(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
				firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}

	public boolean isThisVerCompatibleWith(int hardwareVersion, int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal){
		return UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
				hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}
	
	
	/**
	 * Used to changed the enabled state of a sensor in the sensormap. This is
	 * only used in Consensys for dynamic configuration of a Shimmer. This
	 * method deals with everything assciated with enabling a sensor such as:
	 * 1) dealing with conflicting sensors
	 * 2) dealing with other required sensors for the chosen sensor
	 * 3) determining whether expansion board power is required
	 * 4) setting default settings for disabled sensors 
	 * 5) etc.
	 * 
	 * @param sensorMapKey the sensormap key of the sensor to be enabled/disabled
	 * @param state the sensor state to set 
	 * @return a boolean indicating if the sensors state was successfully changed
	 */
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		if(mSensorMap!=null) {
			sensorMapKey = handleSpecCasesBeforeSetSensorState(sensorMapKey,state);
			
			SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
			//System.err.println("sensorDetails.mSensorDetailsRef: " +sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
			if(sensorDetails!=null){
				//Automatically handle required channels for each sensor
				List<Integer> listOfRequiredKeys = sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired;
				if(listOfRequiredKeys != null && listOfRequiredKeys.size()>0) {
					for(Integer i:listOfRequiredKeys) {
						getSensorDetails(i).setIsEnabled(state);
					}
				}
				
				//Set sensor state
				sensorDetails.setIsEnabled(state);

				setSensorEnabledStateCommon(sensorMapKey, sensorDetails.isEnabled());

				boolean result = sensorDetails.isEnabled();
				boolean successfullySet = result==state? true:false; 
				if(!successfullySet){
					consolePrintErrLn("Failed to setSensorEnabledState for sensor:\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
				}
				return successfullySet;
			}
			return false;
		}
		else {
			consolePrintLn("setSensorEnabledState:\t SensorMap=null");
			return false;
		}
	}
	
	public boolean setSensorEnabledState(int sensorMapKey, boolean state, COMMUNICATION_TYPE commType) {
		if(mSensorMap!=null) {
			sensorMapKey = handleSpecCasesBeforeSetSensorState(sensorMapKey,state);
			
			SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
			//System.err.println("sensorDetails.mSensorDetailsRef: " +sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
			if(sensorDetails!=null){
				//Testing
//				if(sensorDetails.hashCode()!=getSensorDetailsFromMapOfSensorClasses(sensorMapKey).hashCode())
//					System.err.println("SENSOR DETAILS HASHCODES NOT EQUAL");

				//Automatically handle required channels for each sensor
				List<Integer> listOfRequiredKeys = sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired;
				if(listOfRequiredKeys != null && listOfRequiredKeys.size()>0) {
					for(Integer i:listOfRequiredKeys) {
						getSensorDetails(i).setIsEnabled(commType, state);
					}
				}
				
				//Set sensor state
				sensorDetails.setIsEnabled(commType, state);

				setSensorEnabledStateCommon(sensorMapKey, sensorDetails.isEnabled());

				boolean result = sensorDetails.isEnabled();
				boolean successfullySet = result==state? true:false; 
				if(!successfullySet){
					consolePrintErrLn("Failed to setSensorEnabledState for sensor:\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
				}
				return successfullySet;
			}
			return false;
		}
		else {
			consolePrintLn("setSensorEnabledState:\t SensorMap=null");
			return false;
		}
	}

	
	private void setSensorEnabledStateCommon(int sensorMapKey, boolean state) {
		sensorMapConflictCheckandCorrect(sensorMapKey);
		setDefaultConfigForSensor(sensorMapKey, state);
		
		// Automatically control internal expansion board power
		checkIfInternalExpBrdPowerIsNeeded();

//		printSensorParserAndAlgoMaps();
		refreshEnabledSensorsFromSensorMap();
//		printSensorParserAndAlgoMaps();
		
		generateParserMap();
		//refresh algorithms
		algorithmRequiredSensorCheck();

//		//Debugging
//		printSensorParserAndAlgoMaps();
	}

	/**
	 * This method just changes the isEnabled boolean state in the specific
	 * SensorDetails inside the SensorMap. It doesn't do any of the usual checks
	 * before or after enabling a sensor (such as checking for
	 * conflicts/required sensors/expansion board power etc.)
	 * 
	 * @param sensorMapKey
	 * @param state
	 */
	protected void setSensorEnabledStateBasic(int sensorMapKey, boolean state) {
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		if (sensorDetails!=null){
			sensorDetails.setIsEnabled(state);
		}
	}
	
	public int handleSpecCasesBeforeSetSensorState(int sensorMapKey, boolean state) {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			int newSensorMapKey = abstractSensor.handleSpecCasesBeforeSetSensorState(sensorMapKey, state);
			if(newSensorMapKey!=sensorMapKey){
				return newSensorMapKey;
			}
		}
		return sensorMapKey;
	}
	
	public boolean isTimestampEnabled(){
		SensorDetails sensorDetails = getSensorDetails(Configuration.Shimmer3.SensorMapKey.SHIMMER_TIMESTAMP);
		if(sensorDetails!=null){
			return sensorDetails.isEnabled();
		}
		return false;
	}
	
	/**
	 * @param originalSensorMapkey This takes in a single sensor map key to check for conflicts and correct
	 * @return enabledSensors This returns the new set of enabled sensors, where any sensors which conflicts with sensorToCheck is disabled on the bitmap, so sensorToCheck can be accomodated (e.g. for Shimmer2 using ECG will disable EMG,GSR,..basically any daughter board)
	 * @return boolean 
	 *  
	 */
	protected void sensorMapConflictCheckandCorrect(int originalSensorMapkey){
		SensorDetails sdOriginal = getSensorDetails(originalSensorMapkey); 
		if(sdOriginal != null) {
			if(sdOriginal.mSensorDetailsRef.mListOfSensorMapKeysConflicting != null) {
				for(Integer sensorMapKeyConflicting:sdOriginal.mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
					SensorDetails sdConflicting = getSensorDetails(sensorMapKeyConflicting); 
					if(sdConflicting != null) {
						sdConflicting.setIsEnabled(false);
						if(sdConflicting.isDerivedChannel()) {
							mDerivedSensors &= ~sdConflicting.mDerivedSensorBitmapID;
						}
						setDefaultConfigForSensor(sensorMapKeyConflicting, sdConflicting.isEnabled());
					}
				}
			}
		}
		
		sensorMapCheckandCorrectSensorDependencies();
		sensorMapCheckandCorrectHwDependencies();
	}
	

	protected void sensorMapCheckandCorrectSensorDependencies() {
		//Cycle through any required sensors and update sensorMap channel enable values
		for(Integer sensorMapKey:mSensorMap.keySet()) {
			SensorDetails sensorDetails = getSensorDetails(sensorMapKey); 
			if(sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired != null) {
				for(Integer requiredSensorKey:sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired) {
					if(!getSensorDetails(requiredSensorKey).isEnabled()) {
						sensorDetails.setIsEnabled(false);
						if(sensorDetails.isDerivedChannel()) {
							mDerivedSensors &= ~sensorDetails.mDerivedSensorBitmapID;
						}
						setDefaultConfigForSensor(sensorMapKey, sensorDetails.isEnabled());
						break;
					}
				}
			}
		}
	}
	
	protected void sensorMapCheckandCorrectHwDependencies() {
		for(Integer sensorMapKey:mSensorMap.keySet()) {
			SensorDetails sensorDetails = getSensorDetails(sensorMapKey); 
			if(sensorDetails.mSensorDetailsRef.mListOfCompatibleVersionInfo != null) {
				if(!isVerCompatibleWithAnyOf(sensorDetails.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
					sensorDetails.setIsEnabled(false);
					if(sensorDetails.isDerivedChannel()) {
						mDerivedSensors &= ~sensorDetails.mDerivedSensorBitmapID;
					}
					setDefaultConfigForSensor(sensorMapKey, sensorDetails.isEnabled());
				}
			}
		}
	}
	
	protected void setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			if(abstractSensor.setDefaultConfigForSensor(sensorMapKey, isSensorEnabled)){
				//Sensor found, break
				break;
			}
		}
	}
	
	public void setEnabledSensors(long mEnabledSensors) {
		this.mEnabledSensors = mEnabledSensors;
	}
	
	public long getEnabledSensors() {
		return mEnabledSensors;
	}

	public void setDerivedSensors(long mDerivedSensors) {
		this.mDerivedSensors = mDerivedSensors;
	}
	
	public long getDerivedSensors() {
		return mDerivedSensors;
	}

	public void setEnabledAndDerivedSensors(long enabledSensors, long derivedSensors) {
		setEnabledAndDerivedSensors(enabledSensors, derivedSensors, null);
	}
	
	public void setEnabledAndDerivedSensors(long enabledSensors, long derivedSensors, COMMUNICATION_TYPE commsType) {
		setEnabledSensors(enabledSensors);
		setDerivedSensors(derivedSensors);
		sensorMapUpdateFromEnabledSensorsVars(commsType);
		algorithmMapUpdateFromEnabledSensorsVars();
		generateParserMap();
	}
	
	public void prepareAllMapsAfterConfigRead() {
		//Debugging
//		System.err.println("Before");
//		printSensorParserAndAlgoMaps();

		//TODO ideally this line shouldn't be here
		sensorAndConfigMapsCreate();
		
		setEnabledAndDerivedSensors(mEnabledSensors, mDerivedSensors);
		
//		sensorMapUpdateFromEnabledSensorsVars();
//		algorithmMapUpdateFromEnabledSensorsVars();
////		sensorMapCheckandCorrectSensorDependencies();
//		generateParserMap();
		
		//Debugging
//		System.err.println("After");
//		printSensorParserAndAlgoMaps();
		
		//TODO include this here after testing
//		// Configuration from each Sensor settings
//		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
//			abstractSensor.configByteArrayParse(this, mConfigBytes);
//		}
//
		
		// This is to update the newly created sensor/algorithm classes (created
		// above) with the current Shimmer sampling rate
		
		setSamplingRateSensors(getSamplingRateShimmer());
	}
	
	protected void handleSpecialCasesAfterSensorMapCreate() {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.handleSpecialCasesAfterSensorMapCreate();
		}
	}
	

	public void refreshEnabledSensorsFromSensorMap(){
		if(mSensorMap!=null) {
			
//			//Debugging
//			printSensorParserAndAlgoMaps();
			
			if (!mShimmerVerObject.isShimmerGen2()) {
//			if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_4_SDK) {
				long enabledSensors = 0;
				sensorMapCheckandCorrectHwDependencies();
				List<SensorDetails> listOfEnabledSensors  = getListOfEnabledSensors();
				for (SensorDetails sED:listOfEnabledSensors) {
					enabledSensors |= sED.mSensorDetailsRef.mSensorBitmapIDSDLogHeader;
				}
				setEnabledSensors(enabledSensors);
				
				updateDerivedSensors();
				
				handleSpecCasesUpdateEnabledSensors();
			}
		}
	}
	
	/** For use when debugging */
	public void printSensorParserAndAlgoMaps(){
		//For debugging
		consolePrintLn("");
		consolePrintLn("Enabled Sensors\t" + UtilShimmer.longToHexStringWithSpacesFormatted(mEnabledSensors, 5));
		consolePrintLn("Derived Sensors\t" + UtilShimmer.longToHexStringWithSpacesFormatted(mDerivedSensors, 8));
		
		consolePrintLn("SENSOR MAP");
		for(SensorDetails sensorDetails:mSensorMap.values()){
			consolePrintLn("\tSENSOR\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel + "\tIsEnabled:\t" + sensorDetails.isEnabled());
			for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				consolePrintLn("\t\tChannel:\t" + channelDetails.getChannelObjectClusterName());
			}
		}
		consolePrintLn("");
		
		consolePrintLn("PARSER MAP" + "\tSize=" + mParserMap.keySet().size());
		for(COMMUNICATION_TYPE commType:mParserMap.keySet()){
			consolePrintLn("PARSER MAP\tCOMM TYPE:\t" + commType);
			for(SensorDetails sensorDetails:mParserMap.get(commType).values()){
				consolePrintLn("\tSENSOR\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					consolePrintLn("\t\tChannel:\t" + channelDetails.getChannelObjectClusterName());
				}
			}
			consolePrintLn("");
		}
		consolePrintLn("");
		
		consolePrintLn("ALGO MAP");
		List<AbstractAlgorithm> mapOfEnabledAlgoModules = getListOfAlgorithmModules();
		for(AbstractAlgorithm abstractAlgorithm:mapOfEnabledAlgoModules){
			consolePrintLn("\tALGO\t" + abstractAlgorithm.getAlgorithmName() + "\tIsEnabled:\t" + abstractAlgorithm.isEnabled());
			List<ChannelDetails> listOfChannelDetails = abstractAlgorithm.getChannelDetails();
			for(ChannelDetails channelDetails:listOfChannelDetails){
				consolePrintLn("\t\tChannel:\t" + channelDetails.getChannelObjectClusterName());
			}
		}
		consolePrintLn("");
	}
	
	/** added special cases (e.g., for SensorEXG) */
	public void handleSpecCasesUpdateEnabledSensors() {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.handleSpecCasesUpdateEnabledSensors();
		}
	}
	
	
	public List<SensorDetails> getListOfEnabledSensors(){
		List<SensorDetails> listOfEnabledSensors = new ArrayList<SensorDetails>();
		for (SensorDetails sED:mSensorMap.values()) {
			if (sED.isEnabled()) {
				listOfEnabledSensors.add(sED);
			}
		}
		return listOfEnabledSensors;
	}
	
	public void algorithmMapUpdateFromEnabledSensorsVars() {
//		consolePrintErrLn("algorithmMapUpdateFromEnabledSensorsVars\tmDerivedSensors = " + UtilShimmer.longToHexStringWithSpacesFormatted(mDerivedSensors, 8));
		for(AbstractAlgorithm aA:getMapOfAlgorithmModules().values()){
			aA.algorithmMapUpdateFromEnabledSensorsVars(mDerivedSensors);
		}
		initializeAlgorithms();
	}
	
//	public void sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE commType) {
//		for(AbstractSensor sensor:mMapOfSensorClasses.values()){
//			sensor.updateStateFromEnabledSensorsVars(commType, mEnabledSensors, mDerivedSensors);
//		}
//	}
	
	public void sensorMapUpdateFromEnabledSensorsVars() {
		sensorMapUpdateFromEnabledSensorsVars(null);
	}

	/**
	 * Used to convert from the enabledSensors long variable read from the
	 * Shimmer to the set enabled status of the relative entries in the Sensor
	 * Map. Used in Consensys for dynamic GUI generation to configure a Shimmer.
	 * 
	 */
	public void sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE commType) {
		//TODO should this be done afterwards (as well?)?
		handleSpecCasesBeforeSensorMapUpdateGeneral();

		if(mSensorMap==null){
			sensorAndConfigMapsCreate();
		}
		
		if(mSensorMap!=null) {
			mapLoop:
			for(Integer sensorMapKey:mSensorMap.keySet()) {
				if(handleSpecCasesBeforeSensorMapUpdatePerSensor(sensorMapKey)){
					continue mapLoop;
				}
					
				// Process remaining channels
				SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
				//TODO test out commType approach
//				sensorDetails.updateFromEnabledSensorsVars(commType, mEnabledSensors, mDerivedSensors);
				sensorDetails.updateFromEnabledSensorsVars(mEnabledSensors, mDerivedSensors);
			}
				
			// Now that all main sensor channels have been parsed, deal with
			// sensor channels that have special conditions. E.g. deciding
			// what type of signal the ExG is configured for or what derived
			// channel is enabled like whether PPG is on ADC12 or ADC13
			handleSpecCasesAfterSensorMapUpdateFromEnabledSensors();
		}
		
//		//Debugging
//		printSensorParserAndAlgoMaps();
	}

	public void handleSpecCasesBeforeSensorMapUpdateGeneral() {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.handleSpecCasesBeforeSensorMapUpdateGeneral(this);
		}
	}

	public boolean handleSpecCasesBeforeSensorMapUpdatePerSensor(Integer sensorMapKey) {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			if(abstractSensor.handleSpecCasesBeforeSensorMapUpdatePerSensor(this, sensorMapKey)){
				return true;
			}
		}
		return false;
	}

	public void handleSpecCasesAfterSensorMapUpdateFromEnabledSensors() {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.handleSpecCasesAfterSensorMapUpdateFromEnabledSensors();
		}
	}



	public List<Integer> sensorMapConflictCheck(Integer key){
		List<Integer> listOfChannelConflicts = new ArrayList<Integer>();
		
		//TODO: handle Shimmer2/r exceptions which involve get5VReg(), getPMux() and writePMux()
		
		if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			if(getSensorDetails(key).mSensorDetailsRef.mListOfSensorMapKeysConflicting != null) {
				for(Integer sensorMapKey:getSensorDetails(key).mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
					if(isSensorEnabled(sensorMapKey)) {
						listOfChannelConflicts.add(sensorMapKey);
					}
				}
			}
		}
		
		if(listOfChannelConflicts.isEmpty()) {
			return null;
		}
		else {
			return listOfChannelConflicts;
		}
	}
	
	/**
	 * Method to set force defaults for disabled sensors. Need to ensure
	 * consistency across all configured Shimmers. Without the below, if a
	 * Shimmer is read from and then configured without changing any of the
	 * configuration, the configuration will not be checked. Another application
	 * could have saved incorrect configuration to the Shimmer.
	 * 
	 */
	public void checkShimmerConfigBeforeConfiguring() {
		//1) call checkShimmerConfigBeforeConfiguring in each AbstractSensor
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.checkShimmerConfigBeforeConfiguring();
		}
		
		//2) 
		checkIfInternalExpBrdPowerIsNeeded();
	}

	
	/**
	 * @return a refreshed version of the current mShimmerInfoMemBytes
	 */
	public byte[] refreshShimmerInfoMemBytes() {
//		System.out.println("SlotDetails:" + this.mUniqueIdentifier + " " + mShimmerInfoMemBytes[3]);
		return configBytesGenerate(false);
	}
	
	public void checkAndCorrectShimmerName(String shimmerName) {
		// Set name if nothing was read from InfoMem
		if(!shimmerName.isEmpty()) {
			mShimmerUserAssignedName = new String(shimmerName);
		}
		else {
			mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME + "_" + getMacIdFromUartParsed();
		}
	}


	//TODO fill out in abstract sensor classes based on ShimmerObject equivalent
	public void checkConfigOptionValues(String stringKey){
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
		
			 if(abstractSensor.checkConfigOptionValues(stringKey)){
				break;
			}  
		
		}
	}

	/**
	 * Checks a specific sensor class to see if it is using it's default
	 * calibration parameters. Used, for example, in SensorLSM, SensorMPU and
	 * SensorKionix.
	 * 
	 * @param sensorMapKey
	 * @return boolean indicating that the sensor is using default calibration
	 *         parameters.
	 */
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			SensorDetails sensorDetails = abstractSensor.getSensorDetails(sensorMapKey);
			if(sensorDetails!=null){
				return abstractSensor.isSensorUsingDefaultCal(sensorMapKey);
			}
		}
		return false;
	}
	
	//TODO tidy up implementation of below, overwritten and handled differently in Shimmer4, ShimmerPC, NoninOnyxII
	protected void setBluetoothRadioState(BT_STATE state){
		BT_STATE stateStored = mBluetoothRadioState;
		mBluetoothRadioState = state;
		consolePrintLn("State change: Was:" + stateStored.toString() + "\tIs now:" + mBluetoothRadioState);
	}
	
	
	public BT_STATE getBluetoothRadioState() {
		return mBluetoothRadioState;
	}
	
	public String getBluetoothRadioStateString() {
		return mBluetoothRadioState.toString();
	}

	/** Generic method that can be overwritten in instances of ShimmerDevice
	 * @throws ShimmerException
	 */
	public void connect() throws ShimmerException{
		
	}

	/** Generic method that can be overwritten in instances of ShimmerDevice
	 * @throws ShimmerException
	 */
	public void disconnect() throws ShimmerException {
		//TODO
//		stopRecording();
		stopStreaming();
	}

	public boolean ignoreAndDisable(Integer sensorMapKey) {
		//Check if a derived channel is enabled, if it is ignore disable and skip 
//		innerloop:
		SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
		if(sensorDetails!=null){
			for(Integer conflictKey:sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
				SensorDetails conflictingSensor = getSensorDetails(conflictKey);
				if(conflictingSensor!=null){
					if(conflictingSensor.isDerivedChannel()) {
						if((mDerivedSensors&conflictingSensor.mDerivedSensorBitmapID)>0) {
							sensorDetails.setIsEnabled(false);
							return true;
//							skipKey = true;
//							break innerloop;
						}
					}
				}
			}
			
		}
		return false;
	}
	

	// ------------- Algorithm Code Start -----------------------
	public void updateDerivedSensors(){
		setDerivedSensors(0);

		updateDerivedSensorsFromSensorMap();
		updateDerivedSensorsFromAlgorithmMap();
	}

	private void updateDerivedSensorsFromSensorMap(){
		List<SensorDetails> listOfEnabledSensors  = getListOfEnabledSensors();
		for (SensorDetails sED:listOfEnabledSensors) {
			if (sED.isDerivedChannel()) {
				mDerivedSensors |= sED.mDerivedSensorBitmapID;
			}
		}
	}

	private void updateDerivedSensorsFromAlgorithmMap(){
		List<AbstractAlgorithm> listOfEnabledAlgorithms = getListOfEnabledAlgorithmModules();
		for(AbstractAlgorithm aA:listOfEnabledAlgorithms){
			mDerivedSensors |= aA.getDerivedSensorBitmapID();
		}
	}
	
	public List<AlgorithmDetails> getListOfEnabledAlgorithmDetails(){
		List<AlgorithmDetails> listOfEnabledAlgorithms = new ArrayList<AlgorithmDetails>();
		for(AbstractAlgorithm aa:getListOfEnabledAlgorithmModules()){
			listOfEnabledAlgorithms.add(aa.mAlgorithmDetails);
		}
		return listOfEnabledAlgorithms;
	}
	
	public List<AbstractAlgorithm> getListOfEnabledAlgorithmModules(){
		List<AbstractAlgorithm> listOfEnabledAlgorithms = new ArrayList<AbstractAlgorithm>();
		for(AbstractAlgorithm aa:mMapOfAlgorithmModules.values()){
			if(aa.isEnabled()){
				listOfEnabledAlgorithms.add(aa);
			}
		}
		return listOfEnabledAlgorithms;
	}

	public List<AbstractAlgorithm> getListOfAlgorithmModules(){
		return new ArrayList<AbstractAlgorithm>(mMapOfAlgorithmModules.values());
	}

	public void setIsAlgorithmEnabledAndSyncGroup(String algorithmName, String groupName, boolean state){
		AbstractAlgorithm abstractAlgorithmToChange = getMapOfAlgorithmModules().get(algorithmName);
		if(abstractAlgorithmToChange!=null){
			// Sync all settings for the group and pass back the configuration
			// (just a precaution as they should all have the same settings even
			// if they are enabled/disabled)
			HashMap<String, Object> mapOfAlgoSettings = syncAlgoGroupConfig(groupName, state);			
			
			setIsAlgorithmEnabled(algorithmName, state);
			
			//Load all settings for the group only if the algorithm has been enabled
			// (just a precaution as they should all have the same settings even
			// if they are enabled/disabled)
			if(state && mapOfAlgoSettings!=null){
				abstractAlgorithmToChange.setAlgorithmSettings(mapOfAlgoSettings);
			}
			
			//Set default settings if the algorithm group has been disabled or freshly enabled
			List<AbstractAlgorithm> listOfEnabledAlgosForGroup = getListOfEnabledAlgorithmModulesPerGroup(groupName);
			if((listOfEnabledAlgosForGroup.size()==0) 				//no algos in group enabled, so set defaults for off
				||(state && listOfEnabledAlgosForGroup.size()==1)){	// this is the first algo to be enabled in this group, set the default for on for the group
				setDefaultSettingsForAlgorithmGroup(groupName);
			}
			
			//Sync again
			syncAlgoGroupConfig(groupName, state);
		}
	}

	public void setIsAlgorithmEnabled(String algorithmName, boolean state){
		AbstractAlgorithm abstractAlgorithm = mMapOfAlgorithmModules.get(algorithmName);

		if(abstractAlgorithm!=null && abstractAlgorithm.mAlgorithmDetails!=null){
			if(state){ 
				//switch on the required sensors
				for (Integer sensorMapKey:abstractAlgorithm.mAlgorithmDetails.mListOfRequiredSensors) {
					setSensorEnabledState(sensorMapKey, true);
				}
			}
			abstractAlgorithm.setIsEnabled(state);
		}
		else{
			consolePrintErrLn(algorithmName + " doesn't exist in this device");
		}
		updateDerivedSensors();
		initializeAlgorithms();
	}
	
	public void disableAllAlgorithms(){
		for(AbstractAlgorithm abstractAlgorithmToChange: getMapOfAlgorithmModules().values()){
			setIsAlgorithmEnabled(abstractAlgorithmToChange.getAlgorithmName(), false);
		}
	}
	
	
	/** check to ensure sensors that algorithm requires haven't been switched off */
	public void algorithmRequiredSensorCheck() {
		for(SensorGroupingDetails sGD:mMapOfAlgorithmGrouping.values()){
			// looping through algorithms to see which ones are enabled
			for (AlgorithmDetails algorithmDetails:sGD.mListOfAlgorithmDetails) {
				AbstractAlgorithm abstractAlgorithm = mMapOfAlgorithmModules.get(algorithmDetails.mAlgorithmName);
				if (abstractAlgorithm!=null && abstractAlgorithm.isEnabled()) { // run check to see if accompanying sensors
					innerLoop:
					for (Integer sensor:abstractAlgorithm.mAlgorithmDetails.mListOfRequiredSensors) {
						SensorDetails sensorDetails = getSensorDetails(sensor);
						if (sensorDetails!=null && !sensorDetails.isEnabled()) {
							setIsAlgorithmEnabledAndSyncGroup(abstractAlgorithm.mAlgorithmName, sGD.mGroupName, false);
							break innerLoop;
						}
					}
				}
			}
		}
		initializeAlgorithms();
	}
	
	
	protected List<String[]> getListofEnabledAlgorithmsSignalsandFormats(){
		List<String[]> listAlgoSignalProperties = new ArrayList<String[]>();
		List<AlgorithmDetails> listOfEnabledAlgorithms = getListOfEnabledAlgorithmDetails();
		for(AlgorithmDetails aD:listOfEnabledAlgorithms){
			String[] signalStringArray = aD.getSignalStringArray();
			//TODO is below needed?
			signalStringArray[0] = mShimmerUserAssignedName;
			listAlgoSignalProperties.add(signalStringArray);
		}
		return listAlgoSignalProperties;
	}
	
	/** returns list of compatible algorithms based on Shimmer hardware */
	public Map<String, AbstractAlgorithm> getSupportedAlgorithmChannels(){
		Map<String, AbstractAlgorithm> mSupportedAlgorithmChannelsMap = new LinkedHashMap<String, AbstractAlgorithm>();
		for (AbstractAlgorithm aA:mMapOfAlgorithmModules.values()) {
			for (Integer sensorMapKey:aA.mAlgorithmDetails.mListOfRequiredSensors) {
				if (mSensorMap.containsKey(sensorMapKey)) {
					mSupportedAlgorithmChannelsMap.put(aA.getAlgorithmName(), aA);
				}
			}
		}		
		return mSupportedAlgorithmChannelsMap;
	}
	
	/** This pass back a map of the Algorithm groups so that a GUI can generate configuration options
	 * @return
	 */
	public TreeMap<Integer, SensorGroupingDetails> getMapOfAlgorithmGrouping() {
		TreeMap<Integer, SensorGroupingDetails> algorithmGroupingMap = new TreeMap<Integer, SensorGroupingDetails>(); 
    	for(AbstractAlgorithm abstractAlgorithm:mMapOfAlgorithmModules.values()) {
    		algorithmGroupingMap.putAll(abstractAlgorithm.mMapOfAlgorithmGrouping);
    	}
    	mMapOfAlgorithmGrouping = algorithmGroupingMap;
		return mMapOfAlgorithmGrouping;
	}

	public TreeMap<Integer, SensorGroupingDetails> getMapOfAlgorithmGroupingEnabled() {
		TreeMap<Integer, SensorGroupingDetails> algorithmGroupingMap = new TreeMap<Integer, SensorGroupingDetails>(); 
    	for(AbstractAlgorithm abstractAlgorithm:getListOfEnabledAlgorithmModules()) {
    		algorithmGroupingMap.putAll(abstractAlgorithm.mMapOfAlgorithmGrouping);
    	}
    	mMapOfAlgorithmGrouping = algorithmGroupingMap;
		return mMapOfAlgorithmGrouping;
	}

	private boolean checkIfToEnableAlgo(AlgorithmDetails algorithmDetails){
		for(Integer sensorMapKey:algorithmDetails.mListOfRequiredSensors){
			boolean isSensorEnabled = isSensorEnabled(sensorMapKey); 
			if(algorithmDetails.mSensorCheckMethod==SENSOR_CHECK_METHOD.ANY){
				if(isSensorEnabled){
					//One of the required sensors is enabled -> create algorithm
					return true;
				}
			}
			else if(algorithmDetails.mSensorCheckMethod==SENSOR_CHECK_METHOD.ALL){
				if(!isSensorEnabled){
					//One of the required sensors is not enabled -> continue to next algorithm
					return false;
				}
			}
		}
		return true;
	}
	
	protected void generateMapOfAlgorithmConfigOptions(){
		mConfigOptionsMapAlgorithms = new HashMap<String, ConfigOptionDetailsAlgorithm>();
		for(AbstractAlgorithm abstractAlgorithm:mMapOfAlgorithmModules.values()){
			HashMap<String, ConfigOptionDetailsAlgorithm> configOptionsMapPerAlgorithm = abstractAlgorithm.getConfigOptionsMap();
			
			if(configOptionsMapPerAlgorithm!=null && configOptionsMapPerAlgorithm.keySet().size()>0){
				// taking out duplicates for orientation algorithm config options 
				for(String s: configOptionsMapPerAlgorithm.keySet()){
					if(mConfigOptionsMapAlgorithms.containsKey(s)){
						//do nothing 
					}
					else{
						mConfigOptionsMapAlgorithms.put(s,configOptionsMapPerAlgorithm.get(s));
					}				
				}
			}
		}
	}
	
	protected void generateMapOfAlgorithmGroupingMap() {
		mMapOfAlgorithmGrouping = new TreeMap<Integer, SensorGroupingDetails>();
		for(AbstractAlgorithm abstractAlgorithm:mMapOfAlgorithmModules.values()){
			TreeMap<Integer, SensorGroupingDetails> algorithmGroupingMap = abstractAlgorithm.mMapOfAlgorithmGrouping;
			if(algorithmGroupingMap!=null && algorithmGroupingMap.keySet().size()>0){
				mMapOfAlgorithmGrouping.putAll(algorithmGroupingMap);
			}
		}
	}

	/**
	 * Overwritten in ShimmerPCMSS and ShimmerSDLog as they have access to
	 * closed source algorithms. Is used to set the mDataProcessing variable
	 */
	public void setupDataProcessing() {
		// TODO Auto-generated method stub
	}


	/**
	 * Load general algorithm modules here. Method can be overwritten in order
	 * to load licenced Shimmer algorithms - as done in ShimmerPCMSS
	 */
	protected void generateMapOfAlgorithmModules(){
//		consolePrintErrLn("generateMapOfAlgorithmModules");

		mMapOfAlgorithmModules = new HashMap<String, AbstractAlgorithm>();
		
		loadAlgorithms(OPEN_SOURCE_ALGORITHMS);
		
		//TODO temporarily locating updateMapOfAlgorithmModules() in DataProcessing
		if(mDataProcessing!=null){
			mDataProcessing.updateMapOfAlgorithmModules();
		}

		// TODO load algorithm modules automatically from any included algorithm
		// jars depending on licence?
	}

	public void loadAlgorithms(List<AlgorithmLoaderInterface> listOfAlgorithms) {
		for(AlgorithmLoaderInterface algorithmLoader:listOfAlgorithms){
			loadAlgorithm(algorithmLoader);
		}
	}

	public void loadAlgorithm(AlgorithmLoaderInterface algorithmLoader) {
		algorithmLoader.initialiseSupportedAlgorithms(this);
	}

	public Map<String,AbstractAlgorithm> getMapOfAlgorithmModules(){
		return mMapOfAlgorithmModules;
	}

	public AbstractAlgorithm getAlgorithmModule(String algorithmName){
		return mMapOfAlgorithmModules.get(algorithmName);
	}

	public void addAlgorithmModule(AbstractAlgorithm abstractAlgorithm) {
		mMapOfAlgorithmModules.put(abstractAlgorithm.mAlgorithmName, abstractAlgorithm);
	}

	protected void initializeAlgorithms() {
		for (AbstractAlgorithm aa:mMapOfAlgorithmModules.values()){
			try {
				if(!aa.isInitialized() && aa.isEnabled()){
					aa.initialize();
				}
				else {
					if(!aa.isEnabled()){
						//orientationChannelSync(aa.mAlgorithmName, aa.isEnabled());
//						aa.reset();
						//TODO stop the algorithm
					}
				}
			} catch (Exception e1) {
				consolePrintException("Error initialising algorithm module\t" + aa.getAlgorithmName(), e1.getStackTrace());
			}
		}
	}
	
	
	public HashMap<String, Object> syncAlgoGroupConfig(String groupName, boolean enabled){
		HashMap<String, Object> mapOfAlgoSettings = null;
		
		List<AbstractAlgorithm> listOfEnabledAlgoModulesPerGroup = getListOfEnabledAlgorithmModulesPerGroup(groupName);
		//check if another algorithm in the group has been enabled 
		if(listOfEnabledAlgoModulesPerGroup!=null && listOfEnabledAlgoModulesPerGroup.size()>0){
			//Take the first enabled algorithm settings and configure all the other algorithms to be the same
			AbstractAlgorithm firstEnabledAlgo = listOfEnabledAlgoModulesPerGroup.get(0);
			
			mapOfAlgoSettings = firstEnabledAlgo.getAlgorithmSettings();

			List<AbstractAlgorithm> listOfAlgoModulesPerGroup = getListOfAlgorithmModulesPerGroup(groupName);
			Iterator<AbstractAlgorithm> iterator = listOfAlgoModulesPerGroup.iterator();
			while(iterator.hasNext()){
				AbstractAlgorithm abstractAlgorithm = iterator.next();
				abstractAlgorithm.setAlgorithmSettings(mapOfAlgoSettings);
			}
		}
		else{
			//Set defaults for off?
			List<AbstractAlgorithm> listOfAlgoModulesPerGroup = getListOfAlgorithmModulesPerGroup(groupName);
		}
		
		return mapOfAlgoSettings;
	}

	@Deprecated
	public boolean doesAlgorithmAlreadyExist(AbstractAlgorithm obj){
		for (AbstractAlgorithm aA:mMapOfAlgorithmModules.values())
		{
			if (aA.getAlgorithmName().equals(obj.getAlgorithmName())){
				return true;
			}
		}
		return false;
	}
	
	@Deprecated
	public void removeAlgorithm(AbstractAlgorithm aobj){
		mMapOfAlgorithmModules.remove(aobj);
	}
	
	@Deprecated
	public void removeAlgorithm(String algoName){
		int index=0;
		int keepIndex=-1;
		for (AbstractAlgorithm aA:mMapOfAlgorithmModules.values()){
			if (aA.getAlgorithmName().equals(algoName)){
				keepIndex = index;
			}
			index++;
		}
		
		if (keepIndex>=0){
			mMapOfAlgorithmModules.remove(keepIndex);
		}
	}
	
	// ------------- Algorithm Code end -----------------------

	
	/**
	 * Computes the closest compatible sampling rate for the Shimmer based on
	 * the passed in 'rate' variable. Also computes the next highest available
	 * sampling rate for the Shimmer's sensors (dependent on pre-set low-power
	 * modes).
	 * 
	 * @param rateHz
	 */
	public void setShimmerAndSensorsSamplingRate(double rateHz){
		double correctedRate = correctSamplingRate(rateHz); 
		setSamplingRateShimmer(correctedRate);
		
		setSamplingRateSensors(correctedRate);
		setSamplingRateAlgorithms(correctedRate);
	}

	public void setShimmerAndSensorsSamplingRate(COMMUNICATION_TYPE communicationType, double rateHz){
		double correctedRate = correctSamplingRate(rateHz); 
		setSamplingRateShimmer(communicationType, correctedRate);

		setSamplingRateSensors(correctedRate);
		setSamplingRateAlgorithms(correctedRate);
	}

	public void setSamplingRateShimmer(double rateHz){
		Iterator<COMMUNICATION_TYPE> iterator = mMapOfSamplingRatesShimmer.keySet().iterator();
		while(iterator.hasNext()){
			setSamplingRateShimmer(iterator.next(), rateHz);
		}
	}

	public void setSamplingRateShimmer(COMMUNICATION_TYPE communicationType, double rateHz){
		mMapOfSamplingRatesShimmer.put(communicationType, rateHz);
	}
	
	/** Returns the max set sampling rate for the available communication types
	 * @return
	 */
	public double getSamplingRateShimmer() {
		double maxSetRate = 0.0;
		Iterator<COMMUNICATION_TYPE> iterator = mMapOfSamplingRatesShimmer.keySet().iterator();
		while(iterator.hasNext()){
			COMMUNICATION_TYPE commType = iterator.next();
			double samplingRate = getSamplingRateShimmer(commType);
			maxSetRate = Math.max(maxSetRate, samplingRate);
		}
		return maxSetRate;
	}

	public double getSamplingRateShimmer(COMMUNICATION_TYPE commsType){
		if(mMapOfSamplingRatesShimmer!=null && mMapOfSamplingRatesShimmer.containsKey(commsType)){
			double samplingRate = mMapOfSamplingRatesShimmer.get(commsType);
			if(!Double.isNaN(samplingRate)){
				return samplingRate;
			}
		}
		
		return 0.0;
	}

	/** This is valid for Shimmers that use a 32.768kHz crystal as the basis for their sampling rate
	 * @param rateHz
	 * @return
	 */
	private double correctSamplingRate(double rateHz) {
		double maxSamplingRateHz = calcMaxSamplingRate();
		double maxShimmerSamplingRateTicks = 32768.0;
		
    	// don't let sampling rate < 0 OR > maxRate
    	if(rateHz < 1) {
    		rateHz = 1.0;
    	}
    	else if (rateHz > maxSamplingRateHz) {
    		rateHz = maxSamplingRateHz;
    	}
    	
    	// RM: get Shimmer compatible sampling rate (use ceil or floor depending on which is appropriate to the user entered sampling rate)
    	Double actualSamplingRate;
    	if((Math.ceil(maxShimmerSamplingRateTicks/rateHz) - maxShimmerSamplingRateTicks/rateHz) < 0.05){
           	actualSamplingRate = maxShimmerSamplingRateTicks/Math.ceil(maxShimmerSamplingRateTicks/rateHz);
    	}
    	else{
        	actualSamplingRate = maxShimmerSamplingRateTicks/Math.floor(maxShimmerSamplingRateTicks/rateHz);
    	}
    	
    	 // round sampling rate to two decimal places
    	actualSamplingRate = (double)Math.round(actualSamplingRate * 100) / 100;
		return actualSamplingRate;
	}

	/**
	 * Returns the maximum allowed sampling rate for the Shimmer. This is can be
	 * overridden for a particular version of hardware inside it's respective
	 * extended class (e.g., as done in ShimmerObject)
	 * 
	 * @return
	 */
	protected double calcMaxSamplingRate() {
		return 2048.0;
	}

	/**
	 * Sets the sampling rate settings for each of the sensors based on a single
	 * passed in variable (normally the configured Shimmer sampling rate)
	 * 
	 * @param samplingRateShimmer
	 */
	protected void setSamplingRateSensors(double samplingRateShimmer) {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.setSamplingRateFromShimmer(samplingRateShimmer);
		}
	}

	private void setSamplingRateAlgorithms(double samplingRateShimmer) {
		Iterator<AbstractAlgorithm> iterator = mMapOfAlgorithmModules.values().iterator();
		while(iterator.hasNext()){
			AbstractAlgorithm abstractAlgorithm = iterator.next();
//			if(abstractAlgorithm.isEnabled()){
				abstractAlgorithm.setSettings(AbstractAlgorithm.GuiLabelConfigCommon.SHIMMER_SAMPLING_RATE, samplingRateShimmer);
//			}
		}
	}

	public boolean isAlgorithmEnabled(String algorithmName) {
		AbstractAlgorithm abstractAlgorithm = mMapOfAlgorithmModules.get(algorithmName);
		if(abstractAlgorithm!=null && abstractAlgorithm.isEnabled()){
			return true;
		}
		return false;
	}
	
	public boolean isAnyAlgortihmChannelEnabled(List<AlgorithmDetails> listOfAlgorithmDetails){
		for(AlgorithmDetails algortihmDetails:listOfAlgorithmDetails){
			if(isAlgorithmEnabled(algortihmDetails.mAlgorithmName)){
				return true;
			}
		}
		return false;
	}


	public List<AbstractAlgorithm> getListOfEnabledAlgorithmModulesPerGroup(String groupName) {
		for(SensorGroupingDetails sGD:mMapOfAlgorithmGrouping.values()){
			if(sGD.mGroupName.equals(groupName)){
				return getListOfEnabledAlgorithmModulesPerGroup(sGD);
			}
		}
		return null;
	}
	
	public List<AbstractAlgorithm> getListOfEnabledAlgorithmModulesPerGroup(SensorGroupingDetails sGD) {
		List<AbstractAlgorithm> listOfEnabledAlgorthimsPerGroup = new ArrayList<AbstractAlgorithm>();
		if(sGD!=null){
			Map<String, AbstractAlgorithm> mapOfSupportAlgorithms = getSupportedAlgorithmChannels();
			for(AlgorithmDetails aD:sGD.mListOfAlgorithmDetails){
				AbstractAlgorithm aA = mapOfSupportAlgorithms.get(aD.mAlgorithmName);
				if(aA!=null){
					if(aA.isEnabled()){
						listOfEnabledAlgorthimsPerGroup.add(aA);
					}
				}
			}			
		}
		return listOfEnabledAlgorthimsPerGroup;
	}

	public List<AbstractAlgorithm> getListOfAlgorithmModulesPerGroup(String groupName) {
		for(SensorGroupingDetails sGD:mMapOfAlgorithmGrouping.values()){
			if(sGD.mGroupName.equals(groupName)){
				return getListOfAlgorithmModulesPerGroup(sGD);
			}
		}
		return null;
	}
	
	public List<AbstractAlgorithm> getListOfAlgorithmModulesPerGroup(String[] listOfGroupNames){
		
		List<AbstractAlgorithm> listOfAbstractAlgorithms = new ArrayList<AbstractAlgorithm>();
		
		for(String groupName: listOfGroupNames){
			for(SensorGroupingDetails sGD:mMapOfAlgorithmGrouping.values()){
				if(sGD.mGroupName.equals(groupName)){
					listOfAbstractAlgorithms.addAll(getListOfAlgorithmModulesPerGroup(sGD));
				}
			}
		}
		return listOfAbstractAlgorithms;
	}

	public List<AbstractAlgorithm> getListOfAlgorithmModulesPerGroup(SensorGroupingDetails sGD) {
		List<AbstractAlgorithm> listOfEnabledAlgorthimsPerGroup = new ArrayList<AbstractAlgorithm>();
		if(sGD!=null){
			Map<String, AbstractAlgorithm> mapOfSupportAlgorithms = getSupportedAlgorithmChannels();
			for(AlgorithmDetails aD:sGD.mListOfAlgorithmDetails){
				AbstractAlgorithm aA = mapOfSupportAlgorithms.get(aD.mAlgorithmName);
				if(aA!=null){
					listOfEnabledAlgorthimsPerGroup.add(aA);
				}
			}			
		}
		return listOfEnabledAlgorthimsPerGroup;
	}

	//---------- Storing to Database related - start --------------------
	
	public List<String> getSensorsAndAlgorithmChannelsToStoreInDB(){
		return getSensorsAndAlgorithmChannelsToStoreInDB(null, null);
	}
	
	public List<String> getSensorsAndAlgorithmChannelsToStoreInDB(COMMUNICATION_TYPE commType, CHANNEL_TYPE channelType){
		Set<String> setOfSensorsAndAlgorithms = getSensorChannelsToStoreInDB(commType, channelType);
		Set<String> setOfAlgorithms = getAlgortihmChannelsToStoreInDB(commType, channelType);
		setOfSensorsAndAlgorithms.addAll(setOfAlgorithms);
		
		List<String> listOfObjectClusterSensors = new ArrayList<String>(setOfSensorsAndAlgorithms.size());
		listOfObjectClusterSensors.addAll(setOfSensorsAndAlgorithms);
		
		return listOfObjectClusterSensors;
	}
	
	//get the enabled sensors
//	Map<String, ChannelDetails> mapOfChannels = shimmerDevice.getMapOfEnabledSensorChannelsForStoringToDb(commType);
//	listOfObjectClusterSensors = new ArrayList<String>(mapOfChannels.keySet());
	

	
	//2017-02-01 MN: Old code
//	private Set<String> getAlgortihmChannelsToStoreInDB(){
//		Set<String> setOfObjectClusterChannels = new LinkedHashSet<String>();
//		for(AbstractAlgorithm algortihm: getListOfEnabledAlgorithmModules()){
//			List<ChannelDetails> listOfDetails = algortihm.getChannelDetails();
//			for(ChannelDetails details:listOfDetails){
//				if(details.mStoreToDatabase){
//					setOfObjectClusterChannels.add(details.mObjectClusterName);
////					setOfObjectClusterSensors.add(details.mDatabaseChannelHandle); AS: use this one better??
//				}
//			}
//		}
//		return setOfObjectClusterChannels;
//	}
	
	//2017-02-01 MN: New code
	//TODO get algorithm isenabled per commType
	private Set<String> getAlgortihmChannelsToStoreInDB(COMMUNICATION_TYPE commType, CHANNEL_TYPE channelType){
		Set<String> setOfObjectClusterChannels = new LinkedHashSet<String>();
		Iterator<AbstractAlgorithm> iteratorAlgorithms = getListOfEnabledAlgorithmModules().iterator();
		while(iteratorAlgorithms.hasNext()){
			AbstractAlgorithm algorithm = iteratorAlgorithms.next();
			List<ChannelDetails> listOfDetails = algorithm.getChannelDetails();
			for(ChannelDetails channelDetails:listOfDetails){
				if(channelType!=null && !channelDetails.mListOfChannelTypes.contains(channelType)){
					continue;
				}

				if(channelDetails.mStoreToDatabase){
					setOfObjectClusterChannels.add(channelDetails.mObjectClusterName);
					//TODO AS: use this one better?? MN: yes it is so I don't know why it isn't like the one below and if we change will it mess anything up  
//					setOfObjectClusterSensors.add(channelDetails.mDatabaseChannelHandle);
				}
			}
		}
		return setOfObjectClusterChannels;
	}
	
	private Set<String> getSensorChannelsToStoreInDB(COMMUNICATION_TYPE commType, CHANNEL_TYPE channelType){
		Map<String, ChannelDetails> mapOfEnabledChannelsForStoringToDb = getMapOfEnabledSensorChannelsForStoringToDb(commType, channelType);
		
		Set<String> setOfObjectClusterSensors = new LinkedHashSet<String>();
		//TODO MN&AS: shouldn't this be using the mDatabaseChannelHandle rather then the mObjectClusterName??
		setOfObjectClusterSensors.addAll(mapOfEnabledChannelsForStoringToDb.keySet());
		
		return setOfObjectClusterSensors;
	}

	public Map<String, ChannelDetails> getMapOfEnabledSensorChannelsForStoringToDb(COMMUNICATION_TYPE commType, CHANNEL_TYPE channelType) {
		HashMap<String, ChannelDetails> listOfChannels = new HashMap<String, ChannelDetails>();
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorDetails = iterator.next();
			
			boolean isEnabled = false;
			if(commType==null){
				isEnabled = sensorDetails.isEnabled();
			}
			else{
				isEnabled = sensorDetails.isEnabled(commType);
			}
			
			if(isEnabled && !sensorDetails.mSensorDetailsRef.mIsDummySensor){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if(channelType!=null && !channelDetails.mListOfChannelTypes.contains(channelType)){
						continue;
					}
					
					if(channelDetails.mStoreToDatabase){
						listOfChannels.put(channelDetails.mObjectClusterName, channelDetails);
					}
				}
			}
		}
		return listOfChannels;
	}
	
	public LinkedHashMap<String, Object> getConfigMapForDb(){
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		//General Shimmer configuration
		mapOfConfig.put(DatabaseConfigHandle.SAMPLE_RATE, getSamplingRateShimmer());
		mapOfConfig.put(DatabaseConfigHandle.ENABLE_SENSORS, getEnabledSensors());
		mapOfConfig.put(DatabaseConfigHandle.DERIVED_SENSORS, getDerivedSensors());
		
		mapOfConfig.put(DatabaseConfigHandle.SHIMMER_VERSION, getHardwareVersion());
		mapOfConfig.put(DatabaseConfigHandle.FW_VERSION, getFirmwareVersionCode()); // getFirmwareIdentifier()?
		mapOfConfig.put(DatabaseConfigHandle.FW_VERSION_MAJOR, getFirmwareVersionMajor());
		mapOfConfig.put(DatabaseConfigHandle.FW_VERSION_MINOR, getFirmwareVersionMinor());
		mapOfConfig.put(DatabaseConfigHandle.FW_VERSION_INTERNAL, getFirmwareVersionInternal());

		mapOfConfig.put(DatabaseConfigHandle.EXP_BOARD_ID, getExpansionBoardId());
		mapOfConfig.put(DatabaseConfigHandle.EXP_BOARD_REV, getExpansionBoardRev());
		mapOfConfig.put(DatabaseConfigHandle.EXP_BOARD_REV_SPEC, getExpansionBoardRevSpecial());

		mapOfConfig.put(DatabaseConfigHandle.EXP_PWR, getInternalExpPower());
		mapOfConfig.put(DatabaseConfigHandle.CONFIG_TIME, getConfigTime());

		//Sensor configuration
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			LinkedHashMap<String, Object> configMapPerSensor = abstractSensor.getConfigMapForDb();
			if(configMapPerSensor!=null){
				mapOfConfig.putAll(configMapPerSensor);
			}
		}
		
		//TODO Algorithm configuration
//		HashMap<String, Object> algorithmsConfig = getEnabledAlgorithmSettings();
//		mapOfConfig.putAll(algorithmsConfig);
		
		//Useful for debugging
//		printMapOfConfigForDb();
		
		return mapOfConfig;
	}
	
	public void parseConfigMapFromDb(ShimmerVerObject svo, LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		setShimmerVersionObject(svo);
		
		//General Shimmer configuration
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.EXP_BOARD_ID)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.EXP_BOARD_REV)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.EXP_BOARD_REV_SPEC)){
			ExpansionBoardDetails eBD = new ExpansionBoardDetails(
					((Double)mapOfConfigPerShimmer.get(DatabaseConfigHandle.EXP_BOARD_ID)).intValue(), 
					((Double)mapOfConfigPerShimmer.get(DatabaseConfigHandle.EXP_BOARD_REV)).intValue(), 
					((Double)mapOfConfigPerShimmer.get(DatabaseConfigHandle.EXP_BOARD_REV_SPEC)).intValue());
			setExpansionBoardDetails(eBD);
		}

		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.ENABLE_SENSORS)
				&&mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DERIVED_SENSORS)){
			setEnabledSensors(((Double)mapOfConfigPerShimmer.get(DatabaseConfigHandle.ENABLE_SENSORS)).longValue());
			setDerivedSensors(((Double)mapOfConfigPerShimmer.get(DatabaseConfigHandle.DERIVED_SENSORS)).longValue());
		}

//		printSensorParserAndAlgoMaps();
		prepareAllMapsAfterConfigRead();
		
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.SAMPLE_RATE)){
			setSamplingRateShimmer((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.SAMPLE_RATE));
		}
		
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.EXP_PWR)){
			setInternalExpPower(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.EXP_PWR)).intValue());
		}
		
		//Configuration Time
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.CONFIG_TIME)){
			setConfigTime(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.CONFIG_TIME)).longValue());
		}
		
		//Sensor configuration
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.parseConfigMapFromDb(mapOfConfigPerShimmer);
		}
		
		//TODO Algorithm configuration
//		Iterator<AbstractAlgorithm> iteratorAlgo = getListOfAlgorithmModules().iterator();
//		while(iteratorAlgo.hasNext()){
//			AbstractAlgorithm abstractAlgorithm = iteratorAlgo.next();
//			abstractAlgorithm.setAlgorithmSettings(mapOfConfigPerShimmer);
//		}
		
		//Useful for debugging
//		printMapOfConfigForDb();
	}

	public void printMapOfConfigForDb() {
		HashMap<String, Object> mapOfConfigForDb = getConfigMapForDb();
		printMapOfConfigForDb(mapOfConfigForDb);
	}

	public static void printMapOfConfigForDb(HashMap<String, Object> mapOfConfigForDb) {
		System.out.println("Printing map of Config for DB, size = " + mapOfConfigForDb.keySet().size());
		for(String configLbl:mapOfConfigForDb.keySet()){
			String stringToPrint = configLbl + " = ";
			Object val = mapOfConfigForDb.get(configLbl);
			
			if(val instanceof String){
				stringToPrint += (String)val; 
			}
			else if(val instanceof Boolean){
				stringToPrint += Boolean.toString((boolean) val); 
			}
			else if(val instanceof Double){
				stringToPrint += Double.toString((double) val); 
			}
			else if(val instanceof Integer){
				stringToPrint += Integer.toString((int) val); 
			}
			else if(val instanceof Long){
				stringToPrint += Long.toString((long) val); 
			}
			System.out.println(stringToPrint);
		}
	}
	
	//---------- Storing to Database related - end --------------------


	public LinkedHashMap<String, ChannelDetails> getMapOfEnabledChannelsForStreaming() {
		return getMapOfEnabledChannelsForStreaming(null);
	}

	public LinkedHashMap<String, ChannelDetails> getMapOfEnabledChannelsForStreaming(COMMUNICATION_TYPE commType) {
		LinkedHashMap<String, ChannelDetails> mapOfChannels = new LinkedHashMap<String, ChannelDetails>();
		Iterator<SensorDetails> iteratorSensors = mSensorMap.values().iterator();
		while(iteratorSensors.hasNext()){
			SensorDetails sensorDetails = iteratorSensors.next();
			
			boolean isEnabled = false;
			if(commType == null){
				isEnabled = sensorDetails.isEnabled();
			}
			else{
				isEnabled = sensorDetails.isEnabled(commType);
			}
			
			if(isEnabled){
				for(ChannelDetails channelDetails : sensorDetails.getListOfChannels()){
					if(channelDetails.isShowWhileStreaming()){
						mapOfChannels.put(channelDetails.mObjectClusterName, channelDetails);
					}
				}
			}
		}
		
		Iterator<AbstractAlgorithm> iteratorAlgorithms = getMapOfAlgorithmModules().values().iterator();
		while(iteratorAlgorithms.hasNext()){
			AbstractAlgorithm abstractAlgorithm = iteratorAlgorithms.next();
			if(abstractAlgorithm.isEnabled()){
				for(ChannelDetails channelDetails : abstractAlgorithm.getChannelDetails()){
					if(channelDetails.isShowWhileStreaming()){
						mapOfChannels.put(channelDetails.mObjectClusterName, channelDetails);
					}
				}
			}
		}
		
		if(mapOfChannels.size()==0){
			consolePrintLn(getMacIdFromUartParsed() + "\tNO SENSORS ENABLED");
		}
		
		return mapOfChannels;
	}
	
	public String[] getListofEnabledChannelSignals(){
		List<String> listofSignals = new ArrayList<String>(getMapOfEnabledChannelsForStreaming().keySet());
//		return listofSignals;
		
		String[] enabledSignals = listofSignals.toArray(new String[listofSignals.size()]);
		return enabledSignals;
	}
	
	public List<String[]> getListofEnabledChannelSignalsandFormats(){
		List<String[]> listofEnabledSensorSignalsandFormats = new ArrayList<String[]>();
		Iterator<ChannelDetails> iterator = getMapOfEnabledChannelsForStreaming().values().iterator();
		while(iterator.hasNext()){
			ChannelDetails channelDetails = iterator.next();
			listofEnabledSensorSignalsandFormats.addAll(channelDetails.getListOfChannelSignalsAndFormats());
		}
		return listofEnabledSensorSignalsandFormats;
	}

	public double getMinAllowedSamplingRate() {
		return getMinAllowedEnabledAlgorithmSamplingRate();
	}

	public double getMinAllowedEnabledAlgorithmSamplingRate(){
		double minAllowedAlgoSamplingRate = 0.0;
		Iterator<AbstractAlgorithm> iterator = getListOfEnabledAlgorithmModules().iterator();
		while(iterator.hasNext()){
			AbstractAlgorithm abstractAlgorithm = iterator.next();
			minAllowedAlgoSamplingRate = Math.max(abstractAlgorithm.getMinSamplingRateForAlgorithm(), minAllowedAlgoSamplingRate);
		}
		return minAllowedAlgoSamplingRate;
	}

	public void setDefaultSettingsForAlgorithmGroup(String groupName) {
		List<AbstractAlgorithm> listOfAlgosForGroup = getListOfAlgorithmModulesPerGroup(groupName);
		for(AbstractAlgorithm abstractAlgorithm:listOfAlgosForGroup){
			abstractAlgorithm.setDefaultSetting();
		}
	}

	public SensorDetails getSensorDetails(Integer sensorMapKey) {
		if(mSensorMap!=null && mSensorMap.containsKey(sensorMapKey)){
			return mSensorMap.get(sensorMapKey);
		}
		return null;
	}

	public void addSensorClass(AbstractSensor.SENSORS sensorClassKey, AbstractSensor abstractSensor){
		mMapOfSensorClasses.put(sensorClassKey, abstractSensor);
	}

	public AbstractSensor getSensorClass(AbstractSensor.SENSORS sensorClassKey){
		if(mMapOfSensorClasses!=null && mMapOfSensorClasses.containsKey(sensorClassKey)){
			return mMapOfSensorClasses.get(sensorClassKey);
		}
		return null;
	}

	@Deprecated
	public AbstractSensor getSensorClass(long sensorClassKeyInt){
		if(mMapOfSensorClasses!=null){
			for(AbstractSensor.SENSORS sensorClassKey:mMapOfSensorClasses.keySet()){
				if(sensorClassKey.ordinal()==sensorClassKeyInt){
					return getSensorClass(sensorClassKey);
				}
			}
		}
		return null;
	}

	//Testing
//	public SensorDetails getSensorDetailsFromMapOfSensorClasses(Integer sensorMapKey) {
//		for(AbstractSensor aS:mMapOfSensorClasses.values()){
//			SensorDetails sD = aS.getSensorDetails(sensorMapKey);
//			if(sD!=null)
//				return sD;
//		}
//		return null;
//	}
	
	public void threadSleep(long millis){
		try {
			Thread.sleep(millis);	
		} catch (InterruptedException e) {
			consolePrintLn("threadSleep ERROR");
			e.printStackTrace();
		}
	}

	//*************** Sensor Calibration Related Start ************************* 
	
	public boolean isCalibrationValid() {
		// debugging
		TreeMap<Integer, TreeMap<Integer, CalibDetails>> calibrationMap = getMapOfSensorCalibrationAll();
		Boolean validCal = true;
		Integer rangeCast = null;

		if (calibrationMap != null) {
			Iterator<Integer> iterator = calibrationMap.keySet().iterator();
			while(iterator.hasNext()){
				Integer sensorMapKey = iterator.next();
				TreeMap<Integer, CalibDetails> mapOfKinematicDetails = getMapOfSensorCalibrationAll().get(sensorMapKey);

				Object returnedRange = getConfigValueUsingConfigLabel(sensorMapKey,AbstractSensor.GuiLabelConfigCommon.RANGE);
				if (returnedRange != null) {
					if (returnedRange instanceof Integer) {
						rangeCast = (Integer) returnedRange;
					}

					CalibDetails calibDetails = mapOfKinematicDetails.get(rangeCast);
					if(calibDetails!=null && calibDetails instanceof CalibDetailsKinematic){
						CalibDetailsKinematic kinematicDetails = (CalibDetailsKinematic)calibDetails;
						if (kinematicDetails.isCurrentValuesSet()) {
							if (!kinematicDetails.isAllCalibrationValid()) {
								validCal = false;
							}
						}
					}
				}
			}
		}
		return validCal;
	}

	public TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> getMapOfSensorCalibrationAllKinematic(){
		//Get all calibration for all sensors
		TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfAllSensorCalibration = getMapOfSensorCalibrationAll();
		
		// Filter out just Kinematic calibration parameters
		TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfAllSensorCalibrationKinematic = new TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>();
		Iterator<Integer> iteratorSensor = mapOfAllSensorCalibration.keySet().iterator();
		while(iteratorSensor.hasNext()){
			Integer sensorMapKey = iteratorSensor.next();
			TreeMap<Integer, CalibDetailsKinematic> mapOfSensorCalibrationKinematic = new TreeMap<Integer, CalibDetailsKinematic>();
			TreeMap<Integer, CalibDetails> mapOfCalibPerRange = mapOfAllSensorCalibration.get(sensorMapKey);
			Iterator<Integer> iteratorRange = mapOfCalibPerRange.keySet().iterator();
			while(iteratorRange.hasNext()){
				Integer range = iteratorRange.next();
				CalibDetails calibDetails = mapOfCalibPerRange.get(range);
				if(calibDetails instanceof CalibDetailsKinematic){
					mapOfSensorCalibrationKinematic.put(range, (CalibDetailsKinematic)calibDetails);
				}
			}
			
			if(!mapOfSensorCalibrationKinematic.isEmpty()){
				mapOfAllSensorCalibrationKinematic.put(sensorMapKey, mapOfSensorCalibrationKinematic);
			}

		}
		return mapOfAllSensorCalibrationKinematic;
	}
	
	public TreeMap<Integer, TreeMap<Integer, CalibDetails>> getMapOfSensorCalibrationAll(){
		TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfSensorCalibration = new TreeMap<Integer, TreeMap<Integer, CalibDetails>>();
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			Object returnVal = abstractSensor.getConfigValueUsingConfigLabel(AbstractSensor.GuiLabelConfigCommon.CALIBRATION_ALL);
			if(returnVal!=null){
				TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfCalibDetails = (TreeMap<Integer, TreeMap<Integer, CalibDetails>>) returnVal;
				mapOfSensorCalibration.putAll(mapOfCalibDetails);
			}
		}
		return mapOfSensorCalibration;
	}

	public void resetAllCalibParametersToDefault() {
		TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfCalibAll = getMapOfSensorCalibrationAll();
		if(mapOfCalibAll != null){
			// for each sensor
			for(TreeMap<Integer, CalibDetails> mapOfCalibPerSensor:mapOfCalibAll.values()){
				if(mapOfCalibPerSensor != null){
					// for each range
					for(CalibDetails calibDetails:mapOfCalibPerSensor.values()){
						if(calibDetails != null){
							// set to default
							calibDetails.resetToDefaultParameters();
						}
					}
				}
			}
		}
	}
	
	protected void setMapOfSensorCalibrationAll(TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfKinematicSensorCalibration){
		Iterator<Integer> iterator = mapOfKinematicSensorCalibration.keySet().iterator();
		while(iterator.hasNext()){
			Integer sensorMapKey = iterator.next();
			AbstractSensor abstractSensor = getSensorClass(sensorMapKey);
			if(abstractSensor!=null){
				abstractSensor.setConfigValueUsingConfigLabel(AbstractSensor.GuiLabelConfigCommon.CALIBRATION_PER_SENSOR, mapOfKinematicSensorCalibration.get(sensorMapKey));
			}
		}
	}
	
	public byte[] calibByteDumpGenerate(){
		byte[] calibBytesAll = new byte[]{};
		
//		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
//		while(iterator.hasNext()){
//			AbstractSensor abstractSensor = iterator.next();
//			byte[] calibBytesPerSensor = abstractSensor.generateAllCalibByteArray();
//			if(calibBytesPerSensor!=null){
//				byte[] newCalibBytesAll = ArrayUtils.addAll(calibBytesAll, calibBytesPerSensor);
//				calibBytesAll = newCalibBytesAll;
//			}
//		}
		
		TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfAllCalib = getMapOfSensorCalibrationAll();
		for(Integer sensorMapKey:mapOfAllCalib.keySet()){
			TreeMap<Integer, CalibDetails> calibMapPerSensor = mapOfAllCalib.get(sensorMapKey);
			for(CalibDetails calibDetailsPerRange:calibMapPerSensor.values()){
				
				byte[] calibBytesPerSensor = calibDetailsPerRange.generateCalibDump();
				if(calibBytesPerSensor!=null){
					byte[] calibSensorKeyBytes = new byte[2];
					calibSensorKeyBytes[0] = (byte)((sensorMapKey>>0)&0xFF);
					calibSensorKeyBytes[1] = (byte)((sensorMapKey>>8)&0xFF);
					calibBytesPerSensor = ArrayUtils.addAll(calibSensorKeyBytes, calibBytesPerSensor);
					
					byte[] newCalibBytesAll = ArrayUtils.addAll(calibBytesAll, calibBytesPerSensor);
					calibBytesAll = newCalibBytesAll;
				}
				
				if(calibDetailsPerRange.mRangeValue == 7){
					System.err.println("MAG RANGE 7");
				}
				
				//Debugging
//				if(calibDetailsPerRange instanceof CalibDetailsKinematic){
//					System.out.println(((CalibDetailsKinematic)calibDetailsPerRange).generateDebugString());
//				}
			}
		}

		byte[] svoBytes = mShimmerVerObject.generateVersionByteArrayNew();
		byte[] concatBytes = ArrayUtils.addAll(svoBytes,calibBytesAll);
		
		byte[] packetLength = new byte[2];
		packetLength[0] = (byte) (concatBytes.length&0xFF);
		packetLength[1] = (byte) ((concatBytes.length>>8)&0xFF);
//		consolePrintLn("Packet byte/t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(packetLength));
//		consolePrintLn("calibByteAll/t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(calibBytesAll));
		
		concatBytes = ArrayUtils.addAll(packetLength,concatBytes);

//		consolePrintLn("Concat byte/t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(concatBytes));
		
		return concatBytes;
	}

	/**
	 * Parsed the calibration byte array dump that is saved on the SD card or
	 * else read over bluetooth/dock from a specific calibration dump read
	 * command
	 * 
	 * @param calibBytesAll
	 */
	public void calibByteDumpParse(byte[] calibBytesAll, CALIB_READ_SOURCE calibReadSource){

		mCalibBytesDescriptions = new HashMap<Integer, String>();
		mCalibBytes = calibBytesAll;
		
		if(UtilShimmer.isAllZeros(calibBytesAll)){
			return;
		}
		
		if(calibBytesAll.length>2){
			//1) Packet lENGTH -> don't need here
			mCalibBytesDescriptions.put(0, "PacketLength_LSB");
			mCalibBytesDescriptions.put(1, "PacketLength_MSB");
			byte[] packetLength = Arrays.copyOfRange(calibBytesAll, 0, 2);
			
			byte[] svoBytes = Arrays.copyOfRange(calibBytesAll, 2, 10);
			ShimmerVerObject svo = new ShimmerVerObject(svoBytes);

			mCalibBytesDescriptions.put(2, "HwID_LSB (" + svo.mHardwareVersionParsed + ")");
			mCalibBytesDescriptions.put(3, "HwID_MSB");
			mCalibBytesDescriptions.put(4, "FwID_LSB (" + svo.mFirmwareIdentifierParsed + ")");
			mCalibBytesDescriptions.put(5, "FwID_MSB");
			mCalibBytesDescriptions.put(6, "FWVerMjr_LSB");
			mCalibBytesDescriptions.put(7, "FWVerMjr_MSB");
			mCalibBytesDescriptions.put(8, "FWVerMinor");
			mCalibBytesDescriptions.put(9, "FWVerInternal");

			int currentOffset = 10;
			byte[] remainingBytes = Arrays.copyOfRange(calibBytesAll, 10, calibBytesAll.length);;
			while(remainingBytes.length>12){
				//2) parse sensorMapKey (2 bytes LSB)
				byte[] sensorIdBytes = Arrays.copyOfRange(remainingBytes, 0, 2);
				int sensorMapKey = ((sensorIdBytes[1]<<8) | sensorIdBytes[0])&0xFFFF;
				
				String sensorName = "";
				SensorDetails sensorDetails = getSensorDetails(sensorMapKey);
				if(sensorDetails!=null && sensorDetails.mSensorDetailsRef!=null){
					sensorName = sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel;
				}
				mCalibBytesDescriptions.put(currentOffset, "SensorMapKey_LSB (" + sensorName + ")");
				mCalibBytesDescriptions.put(currentOffset+1, "SensorMapKey_MSB");
				
				//3) parse range (1 byte)
				mCalibBytesDescriptions.put(currentOffset+2, "SensorRange");
				byte[] rangeIdBytes = Arrays.copyOfRange(remainingBytes, 2, 3);
				int rangeValue = (rangeIdBytes[0]&0xFF);
	
				//4) parse calib byte length (1 byte)
				mCalibBytesDescriptions.put(currentOffset+3, "CalibByteLength");
	//			byte[] calibLength = Arrays.copyOfRange(remainingBytes, 3, 4);
				int calibLength = (remainingBytes[3]&0xFF);
	//			int calibLength = parseCalibrationLength(sensorMapKey);
				
				//5) parse timestamp (8 bytes MSB/LSB?)
				mCalibBytesDescriptions.put(currentOffset+4, "CalibTimeTicks_LSB");
				mCalibBytesDescriptions.put(currentOffset+11, "CalibTimeTicks_MSB");
				byte[] calibTimeBytesTicks = Arrays.copyOfRange(remainingBytes, 4, 12);
				
//				//Debugging
//				consolePrintLn("");
//				consolePrintLn("Sensor id Bytes - \t" + sensorMapKey + "\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(sensorIdBytes));
//				consolePrintLn("Range id Bytes - \t" + rangeValue + "\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rangeIdBytes));
//				consolePrintLn("Calib Bytes Length - \t" + calibLength);
//				consolePrintLn("Time Stamp id Bytes - \t" + UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(calibTimeBytesTicks) + "\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(calibTimeBytesTicks));
				
				int endIndex = 12+calibLength;
				//6) parse calibration bytes (X bytes)
				if(remainingBytes.length>=endIndex){
					mCalibBytesDescriptions.put(currentOffset+12, "Calib_Start");
					mCalibBytesDescriptions.put(currentOffset+endIndex-1, "Calib_End");
					byte[] calibBytes = Arrays.copyOfRange(remainingBytes, 12, endIndex);
					
//					//Debugging
//					consolePrintLn("Calibration id Bytes - \t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(calibBytes));

					calibByteDumpParsePerSensor(sensorMapKey, rangeValue, calibTimeBytesTicks, calibBytes, calibReadSource);
					remainingBytes = Arrays.copyOfRange(remainingBytes, endIndex, remainingBytes.length);
//					consolePrintLn("Remaining Bytes - " + remainingBytes);
					
					currentOffset += endIndex;
				}
				else {
					break;
				}
	
			}
		}
	}
	
	protected void calibByteDumpParsePerSensor(int sensorMapKey, int rangeValue, byte[] calibTimeBytesTicks, byte[] calibBytes, CALIB_READ_SOURCE calibReadSource) {
//		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
//		while(iterator.hasNext()){
//			AbstractSensor abstractSensor = iterator.next();
//			if(abstractSensor.parseAllCalibByteArray(sensorMapKey, rangeValue, calibTime, calibBytes)){
////				consolePrintLn("SUCCESSFULLY PARSED");
//				break;
//			}
//		}
		
		TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfAllCalib = getMapOfSensorCalibrationAll();
		TreeMap<Integer, CalibDetails> mapOfCalibPerSensor = mapOfAllCalib.get(sensorMapKey);
		if(mapOfCalibPerSensor!=null){
			CalibDetails calibDetailsPerRange = mapOfCalibPerSensor.get(rangeValue);
			if(calibDetailsPerRange!=null){
				calibDetailsPerRange.parseCalibDump(calibTimeBytesTicks, calibBytes, calibReadSource);
				
				//Debugging
//				if(calibDetailsPerRange instanceof CalibDetailsKinematic){
//					System.out.println(((CalibDetailsKinematic)calibDetailsPerRange).generateDebugString());
//				}
			}
		}
	}
	//*************** Sensor Calibration Related end ************************* 

	
	//*************** Radio Connection start ************************* 
	//TODO copied from ShimmerBluetooth
	public void operationPrepare() {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.operationPrepare();
		}
	}

	//TODO copied from ShimmerBluetooth
	public void operationStart(BT_STATE configuring) {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.operationStart(configuring);
		}
	}

	public void startStreaming() {
		resetPacketLossVariables();
		updateExpectedDataPacketSize();
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.startStreaming();
		}
	}

	public void stopStreaming() {
		resetPacketLossVariables();
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.stopStreaming();
		}
	}
	
	public void startSDLogging() {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.startSDLogging();
		}
	}

	public void stopSDLogging() {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.stopSDLogging();
		}
	}
	
	public void startDataLogAndStreaming() {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.startDataLogAndStreaming();
		}
	}
	
	public void stopStreamingAndLogging() {
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.stopLoggingAndStreaming();
		}
	}
	
	public void updateExpectedDataPacketSize() {
		generateParserMap();
		int expectedDataPacketSize = getExpectedDataPacketSize(COMMUNICATION_TYPE.BLUETOOTH);
//		int expectedDataPacketSize = getExpectedDataPacketSize(COMMUNICATION_TYPE.ALL);
		if(mCommsProtocolRadio!=null){
			mCommsProtocolRadio.setPacketSize(expectedDataPacketSize);
		}
	}

	
	/**This method will not only return the Com Port but also update it from the CommsProtocolRadio if it is in use.  
	 * @return
	 */
	public String getComPort() {
		if(mCommsProtocolRadio!=null && mCommsProtocolRadio.mRadioHal!=null){
			setComPort(((AbstractSerialPortHal) mCommsProtocolRadio.mRadioHal).getConnectionHandle()); 
		}
		return mComPort;
	}
	
	//*************** Radio Connection end ************************* 

	
	public String getBtConnectionHandle() {
		String comPort = getComPort();
		if(comPort!=null && !comPort.isEmpty()){
			return comPort;
		}
		else{
			return getMacId();
		}
	}
	
	/** Only supported in ShimmerPCMSS currently*/
	public void setComPort(String comPort){
		mComPort = comPort;
		updateThreadName();
	}
	
	public void setUniqueId(String uniqueId){
		mUniqueID = uniqueId;
	}

	public String getUniqueId(){
		return mUniqueID;
	}

	public void calculatePacketReceptionRateCurrent(int intervalMs) {
		//Old code -> not functioning well because it only takes into account the mLastSavedCalibratedTimeStamp and not all packets lost in the interval
//		double numPacketsShouldHaveReceived = (((double)intervalMs)/1000) * getSamplingRateShimmer();
//		
//		if (mLastReceivedCalibratedTimeStamp!=-1 && mLastSavedCalibratedTimeStamp!=-1){
//			double timeDifference=mLastReceivedCalibratedTimeStamp-mLastSavedCalibratedTimeStamp;
//			double numPacketsReceived= ((timeDifference/1000) * getSamplingRateShimmer());
//			setPacketReceptionRateCurrent((numPacketsReceived/numPacketsShouldHaveReceived)*100.0);
//		}	
//
//		if (mLastReceivedCalibratedTimeStamp!=-1){
//			mLastSavedCalibratedTimeStamp = mLastReceivedCalibratedTimeStamp;
//		}

		double numPacketsShouldHaveReceived = (((double)intervalMs)/1000) * getSamplingRateShimmer();
		double packetsReceivedSinceLastRequest = getPacketReceivedCountCurrent();
		setPacketReceptionRateCurrent((packetsReceivedSinceLastRequest/numPacketsShouldHaveReceived)*100.0);
		
		resetPacketReceptionCurrentCounters();
	}
	
	//-------------- Data processing related start --------------------------
	/**
	 * 
	 * Register a callback to be invoked after buildmsg has executed (A new
	 * packet has been successfully received -> raw bytes interpreted into Raw
	 * and Calibrated Sensor data)
	 * 
	 * @param d
	 *            The callback that will be invoked
	 */
	public void setDataProcessing(DataProcessingInterface dataProcessing) {
		mDataProcessing = dataProcessing;
	}

	public DataProcessingInterface getDataProcessing() {
		return mDataProcessing;
	}

	protected void initaliseDataProcessing() {
		//provides a callback for users to initialize their algorithms when start streaming is called
		if(mDataProcessing!=null){
			mDataProcessing.initializeProcessData((int)getSamplingRateShimmer());
		} 	
	}

	//-------------- Data processing related end --------------------------
	

	public void updateThreadName() {
		String macId = getMacIdParsed();
		String newThreadName = this.getClass().getSimpleName(); 
		if(!macId.isEmpty()){
			newThreadName += "-" + macId;
		}
		setThreadName(newThreadName);
	}
	
	protected void consolePrintException(String message, StackTraceElement[] stackTrace) {
		consolePrintLn("Exception!");
		System.out.println("Message: " + message);
		
		Exception e = new Exception();
		e.setStackTrace(stackTrace);
		//create new StringWriter object
		StringWriter sWriter = new StringWriter();
		//create PrintWriter for StringWriter
		PrintWriter pWriter = new PrintWriter(sWriter);
		//now print the stacktrace to PrintWriter we just created
		e.printStackTrace(pWriter);
		//use toString method to get stacktrace to String from StringWriter object
		String strStackTrace = sWriter.toString();
		System.out.println(strStackTrace);
	}

	public void disableAllSensors() {
		for(int sensorMapKey:mSensorMap.keySet()){
			setSensorEnabledState(sensorMapKey, false);
		}
	}
	
	public void setRadio(AbstractSerialPortHal commsProtocolRadio) {
		// TODO Auto-generated method stub
	}

//	public static void addToMapOfErrorCodes(TreeMap<Integer, String> mapOfErrorCodes) {
//		mMapOfErrorCodes.putAll(mapOfErrorCodes);
//	}
	
	public void addDeviceException(ShimmerException dE) {
		dE.mUniqueID = mUniqueID;
		mListOfDeviceExceptions.add(dE);
		consolePrintLn(dE.getErrStringFormatted());
	}

	public void sensorAndConfigMapsCreateCommon() {
		generateSensorAndParserMaps();
		
		generateMapOfAlgorithmModules();
		generateMapOfAlgorithmConfigOptions();
		generateMapOfAlgorithmGroupingMap();
		
		handleSpecialCasesAfterSensorMapCreate();
	}

	public void setAlternativeName(String alternativeName) {
		mAlternativeName = alternativeName;
	}

	public String getAlternativeName() {
		return mAlternativeName;
	}


}