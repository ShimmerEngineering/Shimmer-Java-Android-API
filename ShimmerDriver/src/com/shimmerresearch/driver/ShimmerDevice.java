package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
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
import java.util.Map.Entry;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.algorithms.AlgorithmResultObject;
import com.shimmerresearch.algorithms.ConfigOptionDetailsAlgorithm;
import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmDetails.SENSOR_CHECK_METHOD;
import com.shimmerresearch.algorithms.OrientationModule;
import com.shimmerresearch.algorithms.OrientationModule6DOF;
import com.shimmerresearch.algorithms.OrientationModule9DOF;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.wiredProtocol.UartComponentPropertyDetails;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerLogDetails;
import com.shimmerresearch.driverUtilities.ShimmerSDCardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

public abstract class ShimmerDevice extends BasicProcessWithCallBack implements Serializable{

	/** * */
	private static final long serialVersionUID = 5087199076353402591L;

	public static final String DEFAULT_DOCKID = "Default.01";
	public static final int DEFAULT_SLOTNUMBER = -1;
	public static final String DEFAULT_SHIMMER_NAME = "Shimmer";
	public static final String DEFAULT_EXPERIMENT_NAME = "DefaultTrial";
	public static final String DEFAULT_MAC_ID = "";
	public static final String DEVICE_ID = "Device_ID";
	
	public static final String STRING_CONSTANT_PENDING = "Pending";
	public static final String STRING_CONSTANT_UNKNOWN = "Unknown";
	public static final String STRING_CONSTANT_SD_ERROR = "SD Error";

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
	public String mShimmerUserAssignedName = ""; // This stores the user assigned name
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
	public boolean mFirstSdAccess = true;
	public boolean mIsSDError = false;
	
	protected boolean mIsConnected = false;
	protected boolean mIsSensing = false;
	protected boolean mIsSDLogging = false;											// This is used to monitor whether the device is in sd log mode
	protected boolean mIsStreaming = false;											// This is used to monitor whether the device is in streaming mode
	protected boolean mIsInitialised = false;
	protected boolean mIsDocked = false;
	protected boolean mHaveAttemptedToReadConfig = false;

	//BSL related start
	public String mActivityLog = "";
	public int mFwImageWriteProgress = 0;
	public int mFwImageTotalSize = 0;
	public float mFwImageWriteSpeed = 0;
	//BSL related end
	public List<MsgDock> mListOfFailMsg = new ArrayList<MsgDock>();
	
	

	//TODO: are these variables too specific to different versions of Shimmer HW?
	public long mShimmerRealTimeClockConFigTime = 0;
	public long mShimmerLastReadRealTimeClockValue = 0;
	public String mShimmerLastReadRtcValueParsed = "";
	protected InfoMemLayout mInfoMemLayout;// = new InfoMemLayoutShimmer3(); //default
	protected byte[] mInfoMemBytes = InfoMemLayout.createEmptyInfoMemByteArray(512);
	
	protected String mTrialName = "";
	protected long mConfigTime; //this is in milliseconds, utc

	public long mPacketReceivedCount = 0; 	//Used by ShimmerGQ
	public long mPacketExpectedCount = 0; 	//Used by ShimmerGQ
	protected long mPacketLossCount = 0;		//Used by ShimmerBluetooth
	protected double mPacketReceptionRate = 100;
	protected double mPacketReceptionRateCurrent = 100;
	
	//Events markers
	protected int mEventMarkersCodeLast = 0;
	protected boolean mEventMarkersIsPulse = false;
	protected int mEventMarkerDefault = -1; // using -1 as the default event marker value as as a value of 0 was hanging the plots and the software
	protected int mEventMarkers = mEventMarkerDefault;
	
	public ObjectCluster mLastProcessedObjectCluster = null;
	public List<ShimmerLogDetails> mListofLogs = new ArrayList<ShimmerLogDetails>();

	protected long mEnabledSensors = (long)0;												// This stores the enabled sensors
	protected long mDerivedSensors = (long)0;	

	public BT_STATE mBluetoothRadioState = BT_STATE.DISCONNECTED;
	
	protected int mInternalExpPower=-1;													// This shows whether the internal exp power is enabled.
	
	public boolean mVerboseMode = true;



	// --------------- Abstract Methods Start --------------------------
	
	/**
	 * Performs a deep copy of the parent class by Serializing
	 * @return ShimmerDevice the deep copy of the current ShimmerDevice (should
	 *         be substituted by the extended ShimmerDevice instance)
	 * @see java.io.Serializable
	 */
	public abstract ShimmerDevice deepClone();

	// Device sensor map related
//	public abstract boolean setSensorEnabledState(int sensorMapKey, boolean state);
//	public abstract List<Integer> sensorMapConflictCheck(Integer key);
//	public abstract void checkConfigOptionValues(String stringKey);
	public abstract void sensorAndConfigMapsCreate();
	/**
	 * @param object in some cases additional details might be required for building the packer format, e.g. inquiry response
	 */
	protected abstract void interpretDataPacketFormat(Object object,COMMUNICATION_TYPE commType);
	public abstract void infoMemByteArrayParse(byte[] infoMemContents);
	public abstract byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer);
//	public abstract byte[] refreshShimmerInfoMemBytes();
	public abstract void createInfoMemLayout();

	// --------------- Abstract Methods End --------------------------

	/**
	 * Constructor for this class
	 */
	public ShimmerDevice(){
		setThreadName("ShimmerDevice");
	}
	
	// --------------- Get/Set Methods Start --------------------------
	
	public void setShimmerVersionObject(ShimmerVerObject sVO) {
		mShimmerVerObject = sVO;
		sensorAndConfigMapsCreate();
		
//		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
//			addCommunicationRoute(COMMUNICATION_TYPE.SD);
//		}
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
		// TODO Auto-generated method stub
		
//		mMapOfSamplingRatesShimmer
		
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
		mParserMap = new HashMap<COMMUNICATION_TYPE, TreeMap<Integer, SensorDetails>>();
		for(COMMUNICATION_TYPE commType:COMMUNICATION_TYPE.values()){
			for(Entry<Integer, SensorDetails> sensorEntry:mSensorMap.entrySet()){
				if(sensorEntry.getValue().isEnabled(commType)){
					TreeMap<Integer, SensorDetails> parserMapPerComm = mParserMap.get(commType);
					if(parserMapPerComm==null){
						parserMapPerComm = new TreeMap<Integer, SensorDetails>();
						mParserMap.put(commType, parserMapPerComm);
					}
					parserMapPerComm.put(sensorEntry.getKey(), sensorEntry.getValue());
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
	
	
	public void setBattStatusDetails(ShimmerBattStatusDetails shimmerBattStatusDetails) {
		mShimmerBattStatusDetails = shimmerBattStatusDetails;
	}
	
	public void clearBattStatusDetails() {
		setBattStatusDetails(new ShimmerBattStatusDetails());
	}

	public void setShimmerDriveInfo(ShimmerSDCardDetails shimmerSDCardDetails) {
		mShimmerSDCardDetails = shimmerSDCardDetails;
	}
	
	/**
	 * @param macIdFromUart the mMacIdFromUart to set
	 */
	public void setMacIdFromUart(String macIdFromUart) {
		this.mMacIdFromUart = macIdFromUart;
		this.setThreadName("Shimmer_" + mMacIdFromUart);
	}
	
	/**
	 * @param shimmerUserAssignedName the mShimmerUserAssignedName to set
	 */
	public void setShimmerUserAssignedName(String shimmerUserAssignedName) {
		if(!shimmerUserAssignedName.isEmpty()){
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
			this.mShimmerUserAssignedName = shimmerUserAssignedName.substring(0, 12);
		}
		else { 
			this.mShimmerUserAssignedName = shimmerUserAssignedName;
		}
		this.setThreadName("mShimmerUserAssignedName" + getMacId());
	}
	
	public void setShimmerUserAssignedNameWithMac(String shimmerUserAssignedName) {
		//Don't allow the first char to be numeric - causes problems with MATLAB variable names
		if(shimmerUserAssignedName.length()==0 || UtilShimmer.isNumeric("" + shimmerUserAssignedName.charAt(0))){
			shimmerUserAssignedName = "S" + shimmerUserAssignedName; 
		}
			
		//Limit the name to 12 Char
		String addition = "_" + getMacIdParsed();
		if((shimmerUserAssignedName.length()+addition.length())>12) {
			this.mShimmerUserAssignedName = shimmerUserAssignedName.substring(0, (12-addition.length())) + addition;
		}
		else { 
			this.mShimmerUserAssignedName = shimmerUserAssignedName + addition;
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
		infoMemByteArrayParse(shimmerInfoMemBytes);
	}
	
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
	
	public void addCommunicationRoute(COMMUNICATION_TYPE communicationType) {
		if(!mListOfAvailableCommunicationTypes.contains(communicationType)){
			mListOfAvailableCommunicationTypes.add(communicationType);
		}
//		Collections.sort(mListOfAvailableCommunicationTypes);
		
		if(communicationType==COMMUNICATION_TYPE.DOCK){
			setDocked(true);
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
			setDocked(false);
			setFirstDockRead();
			clearDockInfo();
		}
		
		//TODO temp here -> check if the best place for it
		updateSensorDetailsWithCommsTypes();
		updateSamplingRatesMapWithCommsTypes();
	}

	public String getDriveUsedSpaceParsed() {
		return mShimmerSDCardDetails.getDriveUsedSpaceParsed();
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
		return mExpansionBoardDetails.mExpansionBoardParsed;
	}
	
	public String getExpansionBoardParsedWithVer() {
		return mExpansionBoardDetails.mExpansionBoardParsedWithVer;
	}

	public void clearExpansionBoardDetails(){
		mExpansionBoardDetails = new ExpansionBoardDetails();
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
		return mShimmerBattStatusDetails.mBattVoltageParsed;
	}

	/**
	 * @return the mEstimatedChargePercentageParsed
	 */
	public String getEstimatedChargePercentageParsed() {
		return mShimmerBattStatusDetails.mEstimatedChargePercentageParsed;
	}
	
	/**
	 * @return the mEstimatedChargePercentage
	 */
	public Double getEstimatedChargePercentage() {
		return mShimmerBattStatusDetails.mEstimatedChargePercentage;
	}
	
	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean isHaveAttemptedToRead() {
		return haveAttemptedToRead();
	}

	/**
	 * @param docked the mDocked to set
	 */
	public void setDocked(boolean docked) {
		mIsDocked = docked;
	}

	/**
	 * @return the mDocked
	 */
	public boolean isDocked() {
		return mIsDocked;
	}

	public void setConnected(boolean state) {
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

	public void setStreaming(boolean state) {
		mIsStreaming = state;
	}

	/**Only used for LogAndStream
	 * @return
	 */
	public boolean isSensing(){
		return (mIsSensing || mIsSDLogging || mIsStreaming);
	}
	
	public void setSensing(boolean state) {
		mIsSensing = state;
	}
	
	public boolean isSDLogging(){
		return mIsSDLogging;
	}	


	/**
	 * @param isInitialized the mSuccessfullyInitialized to set
	 */
	public void setInitialised(boolean isInitialized) {
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
	public boolean haveAttemptedToRead() {
		return mHaveAttemptedToReadConfig;
	}
	
	/**
	 * @param haveAttemptedToRead the mHaveAttemptedToRead to set
	 */
	public void setHaveAttemptedToRead(boolean haveAttemptedToRead) {
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
		if(this.mMacIdFromUart.length()>=12) {
			return this.mMacIdFromUart.substring(8, 12);
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
		return mInfoMemBytes;
	}

	public double getPacketReceptionRate(){
		return mPacketReceptionRate;
	}

	public double getPacketReceptionRateCurrent(){
		return mPacketReceptionRateCurrent;
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
			createInfoMemLayout();
		}
	}
	
	protected void parseDockType(){
		if(mDockID.contains(HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASICDOCK.getLabel())){
			mDockType = DEVICE_TYPE.BASICDOCK;
			//Below to ensure Shimmer shows as unknown in BasicDock rather then pending
			mHaveAttemptedToReadConfig = true;
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
		
		ObjectCluster ojc = new ObjectCluster(mShimmerUserAssignedName, getMacId());
		ojc.mRawData = newPacket;
		ojc.createArrayData(getNumberOfEnabledChannels(commType));

//		System.out.println("\nNew Parser loop. Packet length:\t" + newPacket.length);
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
						//TODO replace with consolePrintSystem
						System.out.println(mShimmerUserAssignedName + " ERROR PARSING " + sensor.mSensorDetailsRef.mGuiFriendlyLabel);
					}
				}
				sensor.processData(sensorByteArray, commType, ojc, isTimeSyncEnabled, pcTimestamp);

//				System.out.println(sensor.mSensorDetails.mGuiFriendlyLabel + "\texpectedPacketArraySize:" + length + "\tcurrentIndex:" + index);
				index += length;
			}
		}
		else{
			consolePrintLn("ERROR!!!! Parser map null");
		}
		
		//add in algorithm processing
//		ojc = processAlgorithmData(ojc);
		
		return ojc;
	}
	
	protected ObjectCluster processAlgorithms(ObjectCluster ojc) {
		//TODO sort out the flow of the below structure
		for (AbstractAlgorithm aA:mMapOfAlgorithmModules.values()) {
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
	protected void interpretDataPacketFormat(){
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
	
	public int getExpectedDataPacketSize(COMMUNICATION_TYPE commsType){
		int dataPacketSize = 0;
		TreeMap<Integer, SensorDetails> parserMapPerCommType = mParserMap.get(commsType);
		if(parserMapPerCommType!=null){
			Iterator<SensorDetails> iterator = parserMapPerCommType.values().iterator();
			while(iterator.hasNext()){
				SensorDetails sensorDetails = iterator.next();
				dataPacketSize += sensorDetails.getExpectedDataPacketSize();
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
			return macToUse.substring(8, 12);
		}
		return "";
	}
	
	public String getMacId() {
		return mMacIdFromUart;
	}
	
	/**
	 * @return the mTrialName
	 */
	public String getTrialName() {
		return mTrialName;
	}
	
	/**
	 * @param trialName the trialName to set
	 */
	public void setTrialName(String trialName) {
		if(trialName.length()>12)
			this.mTrialName = trialName.substring(0, 11);
		else
			this.mTrialName = trialName;
	}
	
	/**
	 * @param mConfigTime the trialConfigTime to set
	 */
	public void setConfigTime(long trialConfigTime) {
		mConfigTime = trialConfigTime;
	}
	
	public void setTrialConfig(String trialName, long trialConfigTime) {
		mTrialName = trialName;
		mConfigTime = trialConfigTime;
	}

	public long getDriveTotalSpace() {
		return mShimmerSDCardDetails.getDriveTotalSpace();
	}
	
	public void setFirstDockRead() {
		mFirstSdAccess = true;
		mConfigurationReadSuccess = false;
		mReadHwFwSuccess = false;
		mReadDaughterIDSuccess = false;
		writeRealWorldClockFromPcTimeSuccess = false;
		mIsSDError = false;
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
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
			if(sensorDetails!=null){
				return sensorDetails.isEnabled();
			}
		}		
		return false;
	}

	public boolean isSensorEnabled(COMMUNICATION_TYPE commType, int sensorKey) {
		SensorDetails sensorDetails = mSensorMap.get(sensorKey);
		if(sensorDetails!=null){
			return sensorDetails.isEnabled(commType);
		}
	    return false;
	}

	public String getChannelLabel(int sensorMapKey) {
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

	public List<ShimmerVerObject> getListOfCompatibleVersionInfo(int sensorMapKey) {
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		if(sensorDetails!=null){
			return sensorDetails.mSensorDetailsRef.mListOfCompatibleVersionInfo;
		}
		return null;
		
//		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
//		while(iterator.hasNext()){
//			AbstractSensor abstractSensor = iterator.next();
//			SensorDetails sensorDetails = abstractSensor.getSensorDetails(sensorMapKey);
//			if(sensorDetails!=null){
//				return sensorDetails.mSensorDetailsRef.mListOfCompatibleVersionInfo;
//			}
//			
////			List<ShimmerVerObject> listOfCompatibleVersionInfo = abstractSensor.getSensorListOfCompatibleVersionInfo(sensorMapKey);
////			if(listOfCompatibleVersionInfo!=null){
////				return listOfCompatibleVersionInfo;
////			}
//		}
//		return null;
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
	
	public void setAlgorithmSettings(String groupName, String configLabel, Object valueToSet){
		List<AbstractAlgorithm> listOfAlgorithms = null;
		
		if((groupName != null && !groupName.isEmpty())){
			listOfAlgorithms = getListOfAlgorithmModulesPerGroup(groupName);		}
		else {
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

	public Object setConfigValueUsingConfigLabel(String configLabel, Object valueToSet){
		return setConfigValueUsingConfigLabel("", configLabel, valueToSet);
	}

	public Object setConfigValueUsingConfigLabel(String groupName, String configLabel, Object valueToSet){
		
		System.err.println("GROUPNAME:\t" + (groupName.isEmpty()? "EMPTY":groupName) + "\tCONFIGLABEL:\t" + configLabel);
		
		Object returnValue = null;

		//TODO check sensor classes return a null if the setting is successfully found
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			returnValue = abstractSensor.setConfigValueUsingConfigLabel(groupName,configLabel, valueToSet);
			if(returnValue!=null){
				return returnValue;
			}
		}
		
		//TODO remove below when ready and use "getAlgorithmConfigValueUsingConfigLabel"
		setAlgorithmSettings(groupName, configLabel, valueToSet);

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
        		setTrialName((String)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE):
	          	// don't let sampling rate be empty
	          	Double enteredSamplingRate;
	          	if(((String)valueToSet).isEmpty()) {
	          		enteredSamplingRate = 1.0;
	          	}            	
	          	else {
	          		enteredSamplingRate = Double.parseDouble((String)valueToSet);
	          	}
	      		setShimmerAndSensorsSamplingRate(enteredSamplingRate);
	      		
	      		returnValue = Double.toString(getSamplingRateShimmer());
	        default:
	        	break;
		}
		
		return returnValue;		
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
	
	public Object getConfigValueUsingConfigLabel(String configLabel){
		Object returnValue = null;
		
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			returnValue = abstractSensor.getConfigValueUsingConfigLabel(configLabel);
			if(returnValue!=null){
				return returnValue;
			}
		}
		
		//TODO remove below when ready and use "getAlgorithmConfigValueUsingConfigLabel"
		returnValue = getAlgorithmSettings("", configLabel);
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
	        default:
	        	break;
		}
		
		return returnValue;		
	}
	
	
	public void incrementPacketExpectedCount() {
		mPacketExpectedCount += 1;
	}

	public void incrementPacketReceivedCount() {
		mPacketReceivedCount += 1;
	}

	public void clearPacketReceptionCounters(){
		mPacketExpectedCount = 0;
		mPacketReceivedCount = 0;
	}
	
	public void setPacketReceivedCount(int i) {
		mPacketReceivedCount = i;
	}
	
	
	public String getSdCapacityParsed() {
		String strPending = STRING_CONSTANT_PENDING;
		
		if(!isSdCardAccessSupported(this)){
			return STRING_CONSTANT_UNKNOWN;
		}

		if(mIsSDError){
			return STRING_CONSTANT_SD_ERROR;
		}
		else {
			if (!getDriveUsedSpaceParsed().isEmpty()) {
				return (getDriveUsedSpaceParsed() + " / " + getDriveTotalSpaceParsed());
			}
			else{
				return (isHaveAttemptedToRead() ? STRING_CONSTANT_UNKNOWN : (mFirstSdAccess ? strPending: STRING_CONSTANT_UNKNOWN));
			}
		}
	}

	public boolean isRtcConfigViaUartSupported() {
		return isRtcConfigViaUartSupported(this);
	}
	
	public static boolean isRtcConfigViaUartSupported(ShimmerDevice shimmerDevice) {
		int hwVer = shimmerDevice.getHardwareVersion(); 
		int fwId = shimmerDevice.getFirmwareIdentifier();
		if (((hwVer==HW_ID.SHIMMER_3)&&(fwId == FW_ID.SDLOG))
				|| ((hwVer==HW_ID.SHIMMER_3)&&(fwId == FW_ID.LOGANDSTREAM))
				|| ((hwVer==HW_ID.SHIMMER_GQ_BLE)&&(fwId == FW_ID.GQ_BLE))
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_LR)
				|| (hwVer==HW_ID.SHIMMER_2R_GQ)
				|| (hwVer==HW_ID.SHIMMER_4_SDK)){
			return true;
		}
		return false;
	}
	
	public boolean isConfigViaUartSupported() {
		return isConfigViaUartSupported(this);
	}

	public static boolean isConfigViaUartSupported(ShimmerDevice shimmerDevice) {
		int hwVer = shimmerDevice.getHardwareVersion(); 
		if((hwVer==HW_ID.SHIMMER_3)
				||(hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				||(hwVer==HW_ID.SHIMMER_GQ_802154_LR)
				||(hwVer==HW_ID.SHIMMER_2R_GQ)
				|| (hwVer==HW_ID.SHIMMER_4_SDK)){
			return true;
		}
		return false;
	}

	public boolean isSdCardAccessSupported() {
		return isSdCardAccessSupported(this);
	}

	public static boolean isSdCardAccessSupported(ShimmerDevice shimmerDevice) {
		int hwVer = shimmerDevice.getHardwareVersion();
		int fwId = shimmerDevice.getFirmwareIdentifier();
		if (((hwVer==HW_ID.SHIMMER_3) && (fwId == FW_ID.SDLOG))
				|| ((hwVer==HW_ID.SHIMMER_3) && (fwId == FW_ID.LOGANDSTREAM))
				|| ((hwVer==HW_ID.SHIMMER_GQ_BLE) && (fwId == FW_ID.GQ_BLE))
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_LR)
				|| (hwVer==HW_ID.SHIMMER_2R_GQ)
				|| (hwVer==HW_ID.SHIMMER_4_SDK)){
			return true;
		}
		return false;
	}

//	public boolean isShimmer3Gen(){
//		if(getFirmwareIdentifier()==FW_ID.BTSTREAM 
//				||getFirmwareIdentifier()==FW_ID.SDLOG
//				||getFirmwareIdentifier()==FW_ID.LOGANDSTREAM
//				||getFirmwareIdentifier()==FW_ID.GQ_BLE){
//			return true;
//		}
//		return false;
//	}

	public boolean isShimmer4Gen(){
		if(getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			return true;
		}
		return false;
	}

	public void consolePrintLn(String message) {
		if(mVerboseMode) {
			Calendar rightNow = Calendar.getInstance();
			String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
					+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
			System.out.println(rightNowString + " " + getClass().getSimpleName() + ": " + getMacId() + " " + message);
		}		
	}
	
	public void consolePrint(String message) {
		if(mVerboseMode) {
			System.out.print(message);
		}		
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
				break;
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
	protected void setInternalExpPower(int state) {
		this.mInternalExpPower = state;
	}
	
	/**
	 * @return the mInternalExpPower
	 */
	public boolean isInternalExpPower() {
		if(mInternalExpPower > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param state the mInternalExpPower state to set
	 */
	protected void setInternalExpPower(boolean state) {
		if(state) 
			mInternalExpPower = 0x01;
		else 
			mInternalExpPower = 0x00;
	}

	public int getInternalExpPower(){
		return mInternalExpPower;
	}


	public Map<String, ChannelDetails> getListOfEnabledChannelsForStoringToDb() {
		return getListOfEnabledChannelsForStoringToDb(null);
	}

	public Map<String, ChannelDetails> getListOfEnabledChannelsForStoringToDb(COMMUNICATION_TYPE commType) {
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
					if(channelDetails.mStoreToDatabase){
						listOfChannels.put(channelDetails.mObjectClusterName, channelDetails);
					}
				}
			}
		}
		return listOfChannels;
	}

	public Map<String, ChannelDetails> getListOfEnabledChannelsForStreaming() {
		return getMapOfEnabledChannelsForStreaming(null);
	}

	public Map<String, ChannelDetails> getMapOfEnabledChannelsForStreaming(COMMUNICATION_TYPE commType) {
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
			
			if(isEnabled){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if(channelDetails.mShowWhileStreaming){
						listOfChannels.put(channelDetails.mObjectClusterName, channelDetails);
					}
				}
			}
		}
		
		if(listOfChannels.size()==0){
			System.out.println(getMacIdFromUartParsed() + "\tNO SENSORS ENABLED");
		}
		
		return listOfChannels;
	}
	
	/**
	 * @return the mSensorMap
	 */
	public Map<Integer, SensorDetails> getSensorMap() {
		return mSensorMap;
	}

	
	public boolean isDerivedSensorsSupported(){
		return isDerivedSensorsSupported(mShimmerVerObject);
	}

	public static boolean isDerivedSensorsSupported(ShimmerVerObject svo){
		if((isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.BTSTREAM, 0, 7, 0))
			||(isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.SDLOG, 0, 8, 69))
			||(isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 3, 17))
			||(isVerCompatibleWith(svo, HW_ID.SHIMMER_4_SDK, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION))){
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
			
//			int hwIdToUse = compatibleVersionInfo.mHardwareVersion;
//			if(hwIdToUse==ShimmerVerDetails.ANY_VERSION){
//				hwIdToUse = getHardwareVersion();
//			}
//			
//			int fwIdToUse = compatibleVersionInfo.mFirmwareIdentifier;
//			if(fwIdToUse==ShimmerVerDetails.ANY_VERSION){
//				fwIdToUse = getFirmwareIdentifier();
//			}
//			
//			if(isThisVerCompatibleWith( 
//					hwIdToUse,
//					fwIdToUse, 
//					compatibleVersionInfo.mFirmwareVersionMajor, 
//					compatibleVersionInfo.mFirmwareVersionMinor, 
//					compatibleVersionInfo.mFirmwareVersionInternal)){
//				return true;
//			}
			
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
		return UtilShimmer.compareVersions(svo.getFirmwareIdentifier(), svo.getFirmwareVersionMajor(), svo.getFirmwareVersionMinor(), svo.getFirmwareVersionInternal(),
				firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
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
			
			//Automatically handle required channels for each sensor
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
			
			if(sensorDetails!=null){
				List<Integer> listOfRequiredKeys = sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired;
				if(listOfRequiredKeys != null && listOfRequiredKeys.size()>0) {
					for(Integer i:listOfRequiredKeys) {
						mSensorMap.get(i).setIsEnabled(state);
					}
				}
				
				//Set sensor state
				sensorDetails.setIsEnabled(state);
				
				sensorMapConflictCheckandCorrect(sensorMapKey);
				setDefaultConfigForSensor(sensorMapKey, sensorDetails.isEnabled());
				
				// Automatically control internal expansion board power
				checkIfInternalExpBrdPowerIsNeeded();

				refreshEnabledSensorsFromSensorMap();
				
				generateParserMap();
				//refresh algorithms
				algorithmRequiredSensorCheck();

//				//Debugging
//				printSensorAndParserMaps();
				
				boolean result = sensorDetails.isEnabled();
				return (result==state? true:false);
			}
			return false;
		}
		else {
			System.out.println("setSensorEnabledState:\t SensorMap=null");
			return false;
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
		SensorDetails sensorDetails = mSensorMap.get(Configuration.Shimmer3.SensorMapKey.SHIMMER_TIMESTAMP);
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
		SensorDetails sdOriginal = mSensorMap.get(originalSensorMapkey); 
		if(sdOriginal != null) {
			if(sdOriginal.mSensorDetailsRef.mListOfSensorMapKeysConflicting != null) {
				for(Integer sensorMapKeyConflicting:sdOriginal.mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
					SensorDetails sdConflicting = mSensorMap.get(sensorMapKeyConflicting); 
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
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey); 
			if(sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired != null) {
				for(Integer requiredSensorKey:sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired) {
					if(!mSensorMap.get(requiredSensorKey).isEnabled()) {
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
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey); 
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
	

	//TODO update sensor map with enabledSensors 
	public void setEnabledSensors(long mEnabledSensors) {
		this.mEnabledSensors = mEnabledSensors;
//		sensorMapUpdateFromEnabledSensorsVars();
	}
	
	public long getEnabledSensors() {
		return mEnabledSensors;
	}


	//TODO update sensor map with derivedSensors
	public void setDerivedSensors(long mDerivedSensors) {
		this.mDerivedSensors = mDerivedSensors;
//		sensorMapUpdateFromEnabledSensorsVars();
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
	
	public void prepareAllAfterConfigRead() {
		//TODO Complete and tidy below
		sensorAndConfigMapsCreate();
		sensorMapUpdateFromEnabledSensorsVars();
		algorithmMapUpdateFromEnabledSensorsVars();
//		sensorMapCheckandCorrectSensorDependencies();
		
//		//Debugging
//		printSensorAndParserMaps();
	}
	

	public void refreshEnabledSensorsFromSensorMap(){
		if(mSensorMap!=null) {
			printSensorAndParserMaps();			
			if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_4_SDK) {
				mEnabledSensors = (long)0;
				mDerivedSensors = (long)0;
				sensorMapCheckandCorrectHwDependencies();
				List<SensorDetails> listOfEnabledSensors  = getListOfEnabledSensors();
				for (SensorDetails sED:listOfEnabledSensors) {
					mEnabledSensors |= sED.mSensorDetailsRef.mSensorBitmapIDSDLogHeader;
				}
				
				// add in algorithm map compatible with device
				updateDerivedSensors();
				
				handleSpecCasesUpdateEnabledSensors();

			}
		}
	}
	
	/** For use when debugging */
	protected void printSensorAndParserMaps(){
		//For debugging
		for(SensorDetails sensorDetails:mSensorMap.values()){
			System.out.println("SENSOR\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel + "\tIsEnabled:\t" + sensorDetails.isEnabled());
		}
		for(COMMUNICATION_TYPE commType:mParserMap.keySet()){
			for(SensorDetails sensorDetails:mParserMap.get(commType).values()){
				System.out.println("PARSER SENSOR\tCOMM TYPE:\t" + commType + "\t" + sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
			}
		}
	}
	
	//RS (30/5/2016) - added special case for EXG
	public void handleSpecCasesUpdateEnabledSensors() {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.handleSpecCasesUpdateEnabledSensors(mEnabledSensors);
		}
	}
	
	
	public List<SensorDetails> getListOfEnabledSensors(){
		List<SensorDetails> listOfEnabledSensors = new ArrayList<SensorDetails>();
		for (SensorDetails sED : mSensorMap.values()) {
			if (sED.isEnabled()) {
				listOfEnabledSensors.add(sED);
			}
		}
		return listOfEnabledSensors;
	}
	
	public void algorithmMapUpdateFromEnabledSensorsVars() {
		for(AbstractAlgorithm aA:mMapOfAlgorithmModules.values()){
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
	//TODO tidy the below. Remove? Almost exact same in ShimmerObject and not sure if this needs to be here 
	public void sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE commType) {
		//TODO below was in ShimmerObject but not carried forward to here?
		//checkExgResolutionFromEnabledSensorsVar();

		if(mSensorMap==null){
			sensorAndConfigMapsCreate();
		}
		
		if(mSensorMap!=null) {
			mapLoop:
			for(Integer sensorMapKey:mSensorMap.keySet()) {
				if(handleSpecCasesBeforeSensorMapUpdate(sensorMapKey)){
					continue mapLoop;
				}
					
				// Process remaining channels
				SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
				sensorDetails.updateFromEnabledSensorsVars(mEnabledSensors, mDerivedSensors);

//				// Process remaining channels
//				mSensorMap.get(sensorMapKey).setIsEnabled(false);
//				// Check if this sensor is a derived sensor
//				if(mSensorMap.get(sensorMapKey).isDerivedChannel()) {
//					//Check if associated derived channels are enabled 
//					if((mDerivedSensors&mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID) == mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID) {
//						//TODO add comment
//						if((mEnabledSensors&mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) == mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) {
//							mSensorMap.get(sensorMapKey).setIsEnabled(true);
//						}
//					}
//				}
//				// This is not a derived sensor
//				else {
//					//Check if sensor's bit in sensor bitmap is enabled
//					if((mEnabledSensors&mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) == mSensorMap.get(sensorMapKey).mSensorDetailsRef.mSensorBitmapIDSDLogHeader) {
//						mSensorMap.get(sensorMapKey).setIsEnabled(true);
//					}
//				}
			}
				
			// Now that all main sensor channels have been parsed, deal with
			// sensor channels that have special conditions. E.g. deciding
			// what type of signal the ExG is configured for or what derived
			// channel is enabled like whether PPG is on ADC12 or ADC13
			handleSpecCasesAfterSensorMapUpdate();
		}
		
//		//Debugging
//		printSensorAndParserMaps();

	}
	
	public boolean handleSpecCasesBeforeSensorMapUpdate(Integer sensorMapKey) {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			if(abstractSensor.handleSpecCasesBeforeSensorMapUpdate(this, sensorMapKey)){
				return true;
			}
		}
		return false;
	}

	public void handleSpecCasesAfterSensorMapUpdate() {
		Iterator<AbstractSensor> iterator = mMapOfSensorClasses.values().iterator();
		while(iterator.hasNext()){
			AbstractSensor abstractSensor = iterator.next();
			abstractSensor.handleSpecCasesAfterSensorMapUpdate();
		}
	}



	public List<Integer> sensorMapConflictCheck(Integer key){
		List<Integer> listOfChannelConflicts = new ArrayList<Integer>();
		
		//TODO: handle Shimmer2/r exceptions which involve get5VReg(), getPMux() and writePMux()
		
		if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			if(mSensorMap.get(key).mSensorDetailsRef.mListOfSensorMapKeysConflicting != null) {
				for(Integer sensorMapKey:mSensorMap.get(key).mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
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
		return infoMemByteArrayGenerate(false);
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
			SensorDetails sensorDetails = abstractSensor.mSensorMap.get(sensorMapKey);
			if(sensorDetails!=null){
				return abstractSensor.isSensorUsingDefaultCal(sensorMapKey);
			}
		}
		return false;
	}
	
	protected void setBluetoothRadioState(BT_STATE state){
		
	}
	
	
	public BT_STATE getBluetoothRadioState() {
		return mBluetoothRadioState;
	}

	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	public boolean ignoreAndDisable(Integer sensorMapKey) {
		//Check if a derived channel is enabled, if it is ignore disable and skip 
//		innerloop:
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		if(sensorDetails!=null){
			for(Integer conflictKey:sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
				SensorDetails conflictingSensor = mSensorMap.get(conflictKey);
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
		mDerivedSensors = (long)0;

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
		for(AbstractAlgorithm aa:mMapOfAlgorithmModules.values()){
//		for(AlgorithmDetails aD:mMapOfAlgorithmDetails.values()){
			if(aa.isEnabled()){
				listOfEnabledAlgorithms.add(aa.mAlgorithmDetails);
			}
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

	
//	//migrated from Shimmer Object 19-5-2016 by EN - all algorithm related functions - UNTESTED
//	//list of algorithms to configure from panel configure algorithm GUI
//	protected void addDerivedSensorConfig(int configAlgorithmInt){
//		//adding in configuration for algorithms
//		//test bitwise OR
//		mDerivedSensorsClone = mDerivedSensorsClone | configAlgorithmInt;
//	}
	
	
	
//	public void configDerivedSensor(){
//		mDerivedSensorsClone=0;
//		
//		// FAKE DATA - WILL BE REMOVED
//		algorithmGuiData = getListOfCompatibleAlgorithmChannels();
//		
//		for (AlgorithmDetails algoDetails:algorithmGuiData) {
//			algoDetails.mEnabled = true;
//			// all algorithm has been switched on for testing
//		}		
//		
//		// looping through algorithms to see which ones are enabled
//		for (AlgorithmDetails algoDetails:algorithmGuiData) {
//			if (algoDetails.isEnabled()) { // an algorithm has been switched on
//				// configure byte
//				addDerivedSensorConfig(algoDetails.mAlgorithmDetails.mDerivedSensorBitmapId);
//				//switch on sensors
//				for (Integer sensor : algoDetails.mAlgorithmDetails.mListOfRequiredSensors) {
//					//this will call a refresh 
//					setSensorEnabledState(sensor, true);
//				}
//			}
//		}
//	}	
	
	public void setAlgorithmEnabled(String key, boolean state){
		AbstractAlgorithm abstractAlgorithm = mMapOfAlgorithmModules.get(key);
		String groupName = abstractAlgorithm.mAlgorithmGroupingName;
		Boolean groupEnable= false;

		if(abstractAlgorithm!=null){
		//find if anything else in the group is on 
			if(getListOfAlgorithmModulesPerGroup(groupName)!=null){
				if (getListOfEnabledAlgorithmModulesPerGroup(groupName).size() > 1) {
					groupEnable = true;
				} else {
					groupEnable = false;
				}
				abstractAlgorithm.setIsEnabled(state, groupEnable);
			} else {
				abstractAlgorithm.setIsEnabled(state);
			}
				
			if (abstractAlgorithm.isEnabled()){ // an algorithm has been switched on
				//switch on sensors
				//update config options 
				for(String s:abstractAlgorithm.mConfigOptionsMap.keySet()){
				setConfigValueUsingConfigLabel(groupName, s, abstractAlgorithm.getDefaultSettings(s));
				}
				
				for (Integer sensor : abstractAlgorithm.mAlgorithmDetails.mListOfRequiredSensors) {
					//this will call a refresh 
					setSensorEnabledState(sensor, true);
				}
			}
		}
		updateDerivedSensors();
		initializeAlgorithms();
	}
	
	
	// check to ensure sensors that algorithm requires hasn't been switched off
	//to be called during sensor pane mouse press?
	public void algorithmRequiredSensorCheck() {
		// looping through algorithms to see which ones are enabled
		for (AbstractAlgorithm abstractAlgorithm:mMapOfAlgorithmModules.values()) {
			if (abstractAlgorithm.isEnabled()) { // run check to see if accompanying s
				for (Integer sensor : abstractAlgorithm.mAlgorithmDetails.mListOfRequiredSensors) {
					SensorDetails sensorDetails = mSensorMap.get(sensor);
					if (sensorDetails.isEnabled() == false) {
						abstractAlgorithm.setIsEnabled(false); // TURNS ALGORITHM OFF
						initializeAlgorithms();
						// refresh panels??
					}
				}
			}
		}
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
	
//	public Map<String, AlgorithmDetails> getAlgorithmChannelsMap() {
//		return mMapOfAlgorithmDetails;
//	}

//	public List<AlgorithmDetails> getListOfSupportedAlgorithmChannels() {
//		
//		List<AlgorithmDetails> listOfSupportAlgorihmChannels = new ArrayList<AlgorithmDetails>();
//		parentLoop:
//    	for(AlgorithmDetails algorithmDetails:mSupportedAlgorithmChannelsMap) {
//    		if(algorithmDetails.mAlgorithmDetails.mSensorCheckMethod == SENSOR_CHECK_METHOD.ANY){
//        		for(Integer sensorMapKey:algorithmDetails.mAlgorithmDetails.mListOfRequiredSensors){
//    				if(isSensorEnabled(sensorMapKey)){
//    					listOfSupportAlgorihmChannels.add(algorithmDetails);
//    					continue parentLoop;
//    				}
//        		}
//    		}
//    		else if(algorithmDetails.mAlgorithmDetails.mSensorCheckMethod == SENSOR_CHECK_METHOD.ALL){
//        		for(Integer sensorMapKey:algorithmDetails.mAlgorithmDetails.mListOfRequiredSensors){
//        			if(!isSensorEnabled(sensorMapKey)){
//    					continue parentLoop;
//        			}
////        			if(!mSensorMap.containsKey(sensorMapKey)){
////    					continue parentLoop;
////        			}
////        			else{
////        				if(!mSensorMap.get(sensorMapKey).isEnabled()){
////        					continue parentLoop;
////        				}
////        			}
//        		
//       			
//      			//made it to past the last sensor
//        			if(sensorMapKey==algorithmDetails.mAlgorithmDetails.mListOfRequiredSensors.get(algorithmDetails.mAlgorithmDetails.mListOfRequiredSensors.size()-1)){
//    					listOfSupportAlgorihmChannels.add(algorithmDetails);
//        			}
//        		}
//		
//    		}	
//    	}
//
//		// TODO Auto-generated method stub
//		return listOfSupportAlgorihmChannels;
//	}

	
//	public void createMapOfSupportedAlgorithmChannels() {
//
//		// returns list of compatible algorithms based on Shimmer hardware
//		parentLoop:
//			for (AlgorithmDetails aD : mAlgorithmChannelsMap.values()) {
//			for (Integer sensorMapKey : aD.mAlgorithmDetails.mListOfRequiredSensors) {
//				if (mSensorMap.containsKey(sensorMapKey)) {
//					mAlgorithmChannelsMap.put(aD.mAlgorithmDetails.mAlgorithmName, aD);
//				}
//			}
//		}
//
//	}
	
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
    	for(AbstractAlgorithm abstractAlgorithm:mMapOfAlgorithmModules.values()) {
    		if(abstractAlgorithm.isEnabled()){
        		algorithmGroupingMap.putAll(abstractAlgorithm.mMapOfAlgorithmGrouping);
    		}
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
					}else{
						mConfigOptionsMapAlgorithms.put(s,configOptionsMapPerAlgorithm.get(s));
					}				
				
				}
			}
		}
	}

	/**
	 * Load general algorithm modules here. Method can be overwritten in order
	 * to load licenced Shimmer algorithms - as done in ShimmerPCMSS
	 */
	protected void generateMapOfAlgorithmModules(){
		mMapOfAlgorithmModules = new HashMap<String, AbstractAlgorithm>();
		
		double samplingRate = getSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH);
		LinkedHashMap<String, AlgorithmDetails> mapOfSupported9DOFCh = OrientationModule9DOF.getMapOfSupportedAlgorithms(mShimmerVerObject);
		for (AlgorithmDetails algorithmDetails:mapOfSupported9DOFCh.values()) {
			OrientationModule9DOF orientationModule9DOF = new OrientationModule9DOF(algorithmDetails, samplingRate);
			mMapOfAlgorithmModules.put(algorithmDetails.mAlgorithmName, orientationModule9DOF);
		}
		
		LinkedHashMap<String, AlgorithmDetails> mapOfSupported6DOFCh = OrientationModule6DOF.getMapOfSupportedAlgorithms(mShimmerVerObject);
		for (AlgorithmDetails algorithmDetails:mapOfSupported6DOFCh.values()) {
			OrientationModule6DOF orientationModule6DOF = new OrientationModule6DOF(algorithmDetails, samplingRate);
			mMapOfAlgorithmModules.put(algorithmDetails.mAlgorithmName, orientationModule6DOF);
		}
		// TODO load algorithm modules automatically from any included algorithm
		// jars depending on licence?
	}

	public Map<String,AbstractAlgorithm> getMapOfAlgorithmModules(){
		return mMapOfAlgorithmModules;
	}

	protected void initializeAlgorithms() {
		for (AbstractAlgorithm aa:mMapOfAlgorithmModules.values()){
			try {
				if(!aa.isInitialized() && aa.isEnabled()){
					aa.initialize();
				}
				else {
					if(!aa.isEnabled()){
//						aa.reset();
						//TODO stop the algorithm
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
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
	
//	//@Override
//	protected void initializeDerivedSensors(){
//		//insert map of algorithms here 
//		generateMapOfAlgorithmModules();
//			
//		try {
//			initializeAlgorithms();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	}
//	
//	protected void initializeAlgorithms() throws Exception{
//		clearExtraSignalProperties();
//		for (AbstractAlgorithm aa:mMapOfAlgorithms.values()){
//			aa.initialize();
//			String[] outputNameArray = aa.getSignalOutputNameArray();
//			String[] outputFormatArray = aa.getSignalOutputFormatArray();
//			String[] outputUnitArray = aa.getSignalOutputUnitArray();
//
//			for (int i=0;i<outputNameArray.length;i++){
//				String[] prop= new String[4];
//				prop[0] = mShimmerUserAssignedName;
//				prop[1] = outputNameArray[i];
//				prop[2] = outputFormatArray[i];
//				prop[3] = outputUnitArray[i];
//				addExtraSignalProperty(prop);
//			}
//		}
//	}
	
//	public ObjectCluster processAlgorithmData(ObjectCluster ojc) {
//		try {
//			// update to work with consensys 4.3 with time sync switched off
//			ojc.addData(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC, CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, Double.NaN);
//			String[] sensorNames = new String[ojc.mSensorNames.length + 1];
//			String[] unitCal = new String[ojc.mUnitCal.length + 1];
//			String[] unitUncal = new String[ojc.mUnitUncal.length + 1];
//			double[] uncalData = new double[ojc.mUncalData.length + 1];
//			double[] calData = new double[ojc.mCalData.length + 1];
//			System.arraycopy(ojc.mSensorNames, 0, sensorNames, 0, ojc.mSensorNames.length);
//			System.arraycopy(ojc.mUnitCal, 0, unitCal, 0, ojc.mUnitCal.length);
//			System.arraycopy(ojc.mUnitUncal, 0, unitUncal, 0, ojc.mUnitUncal.length);
//			System.arraycopy(ojc.mUncalData, 0, uncalData, 0, ojc.mUncalData.length);
//			System.arraycopy(ojc.mCalData, 0, calData, 0, ojc.mCalData.length);
//			sensorNames[sensorNames.length - 1] = Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC;
//			unitCal[unitCal.length - 1] = CHANNEL_UNITS.MILLISECONDS;
//			unitUncal[unitUncal.length - 1] = "";
//			uncalData[uncalData.length - 1] = Double.NaN;
//			calData[calData.length - 1] = Double.NaN;
//			ojc.mSensorNames = sensorNames;
//			ojc.mUnitCal = unitCal;
//			ojc.mUnitUncal = unitUncal;
//			ojc.mUncalData = uncalData;
//			ojc.mCalData = calData;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// create new functions
//		for (AbstractAlgorithm aA:mMapOfAlgorithmModules.values()) {
//			if (mAlgorithmChannelsMap.get(aA).mEnabled) {
//				try {
//					ojc = (ObjectCluster) ((AlgorithmResultObject) aA.processDataRealTime(ojc)).mResult;
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return ojc;
//	}

	// ------------- Algorithm Code end -----------------------

	
	/**
	 * Computes the closest compatible sampling rate for the Shimmer based on
	 * the passed in 'rate' variable. Also computes the next highest available
	 * sampling rate for the Shimmer's sensors (dependent on pre-set low-power
	 * modes).
	 * 
	 * @param rate
	 */
	public void setShimmerAndSensorsSamplingRate(double rate){
		double maxSamplingRateHz = calcMaxSamplingRate();
		double maxShimmerSamplingRateTicks = 32768.0;
		
    	// don't let sampling rate < 0 OR > maxRate
    	if(rate < 1) {
    		rate = 1.0;
    	}
    	else if (rate > maxSamplingRateHz) {
    		rate = maxSamplingRateHz;
    	}
    	
    	// RM: get Shimmer compatible sampling rate (use ceil or floor depending on which is appropriate to the user entered sampling rate)
    	Double actualSamplingRate;
    	if((Math.ceil(maxShimmerSamplingRateTicks/rate) - maxShimmerSamplingRateTicks/rate) < 0.05){
           	actualSamplingRate = maxShimmerSamplingRateTicks/Math.ceil(maxShimmerSamplingRateTicks/rate);
    	}
    	else{
        	actualSamplingRate = maxShimmerSamplingRateTicks/Math.floor(maxShimmerSamplingRateTicks/rate);
    	}
    	
    	 // round sampling rate to two decimal places
    	actualSamplingRate = (double)Math.round(actualSamplingRate * 100) / 100;
		setSamplingRateShimmer(actualSamplingRate);
		
//		setSamplingRateSensors(getSamplingRateShimmer());
//		setSamplingRateAlgorithms(getSamplingRateShimmer());
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
	
	public void setSamplingRateShimmer(double samplingRate){
		Iterator<COMMUNICATION_TYPE> iterator = mMapOfSamplingRatesShimmer.keySet().iterator();
		while(iterator.hasNext()){
			setSamplingRateShimmer(iterator.next(), samplingRate);
		}
	}

	public void setSamplingRateShimmer(COMMUNICATION_TYPE communicationType, double samplingRate){
		mMapOfSamplingRatesShimmer.put(communicationType, samplingRate);
		setSamplingRateSensors(samplingRate);
		setSamplingRateAlgorithms(samplingRate);
	}

	public double getSamplingRateShimmer() {
		//return the first value
		Iterator<Double> iterator = mMapOfSamplingRatesShimmer.values().iterator();
		while(iterator.hasNext()){
			double samplingRate = iterator.next();
			if(!Double.isNaN(samplingRate)){
				return samplingRate;
			}
		}
		return 0.0;
	}

	public double getSamplingRateShimmer(COMMUNICATION_TYPE communicationType){
		double samplingRate = mMapOfSamplingRatesShimmer.get(communicationType);
		if(!Double.isNaN(samplingRate)){
			return samplingRate;
		}
		else {
			return 0.0;
		}
	}

	//TODO revise
	public Double getMaxSetShimmerSamplingRate(){
		double maxSetRate = 0.001;
		Iterator<Double> iterator = mMapOfSamplingRatesShimmer.values().iterator();
		while(iterator.hasNext()){
			double samplingRate = iterator.next();
			maxSetRate = Math.max(maxSetRate, samplingRate);
		}
		return maxSetRate;
	}
	
	private void setSamplingRateAlgorithms(double samplingRateShimmer) {
		for(AbstractAlgorithm abstractAlgorithm:mMapOfAlgorithmModules.values()){
			if(abstractAlgorithm.isEnabled()){
				abstractAlgorithm.setSettings(AbstractAlgorithm.GuiLabelConfigCommon.SAMPLING_RATE, samplingRateShimmer);
			}
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
	
	public List<AbstractAlgorithm> getListOfEnabled6DoF9DoFQuatAlgorithmModulesPerGroup(String groupName) {
		for(SensorGroupingDetails sGD:mMapOfAlgorithmGrouping.values()){
			if(sGD.mGroupName.equals(groupName)){
				return getListOfEnabled6DoF9DoFQuatAlgorithmModulesPerGroup(sGD);
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
	
	public List<AbstractAlgorithm> getListOfEnabled6DoF9DoFQuatAlgorithmModulesPerGroup(SensorGroupingDetails sGD) {
		List<AbstractAlgorithm> listOfEnabled6DoF9DoFQuatAlgorithmModulesPerGroup = new ArrayList<AbstractAlgorithm>();
		if(sGD!=null){
			Map<String, AbstractAlgorithm> mapOfSupportAlgorithms = getSupportedAlgorithmChannels();
			for(AlgorithmDetails aD:sGD.mListOfAlgorithmDetails){
				AbstractAlgorithm aA = mapOfSupportAlgorithms.get(aD.mAlgorithmName);
				if(aA!=null){
					if(aA instanceof OrientationModule){
						OrientationModule orientationModule = (OrientationModule)aA;
						if(orientationModule.isEnabled() && orientationModule.isQuaternionOutput()){
							listOfEnabled6DoF9DoFQuatAlgorithmModulesPerGroup.add(aA);
						}
					}
				}
			}			
		}
		return listOfEnabled6DoF9DoFQuatAlgorithmModulesPerGroup;
	}
	
	public List<AbstractAlgorithm> getListOfAlgorithmModulesPerGroup(String groupName) {
		for(SensorGroupingDetails sGD:mMapOfAlgorithmGrouping.values()){
			if(sGD.mGroupName.equals(groupName)){
				return getListOfAlgorithmModulesPerGroup(sGD);
			}
		}
		return null;
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

	
	public List<String> getSensorsAndAlgorithmToStoreInDB(){
		Set<String> setOfSensorsAndAlgorithms = getSensorChannelsToStoreInDB();
		setOfSensorsAndAlgorithms.addAll(getAlgortihmChannelsToStoreInDB());
		
		List<String> listOfObjectClusterSensors = new ArrayList<String>(setOfSensorsAndAlgorithms.size());
		listOfObjectClusterSensors.addAll(setOfSensorsAndAlgorithms);
		
		return listOfObjectClusterSensors;
	}
	
	private Set<String> getSensorChannelsToStoreInDB(){
		
		Set<String> setOfObjectClusterSensors = new LinkedHashSet<String>();
		for(SensorDetails sensorEnabled: mSensorMap.values()){
			if(sensorEnabled.isEnabled() && !sensorEnabled.mSensorDetailsRef.mIsDummySensor){
    			for(ChannelDetails channelDetails:sensorEnabled.mListOfChannels) {
    					if(channelDetails.mStoreToDatabase){
        					setOfObjectClusterSensors.add(channelDetails.mObjectClusterName);
    					}
    			}
			}
		}
		
		return setOfObjectClusterSensors;
	}
	
	private Set<String> getAlgortihmChannelsToStoreInDB(){
		
		Set<String> setOfObjectClusterChannels = new LinkedHashSet<String>();
		for(AbstractAlgorithm algortihm: mMapOfAlgorithmModules.values()){
			if(algortihm.isEnabled()){
				List<ChannelDetails> listOfDetails = algortihm.mAlgorithmDetails.mListOfChannelDetails;
				for(ChannelDetails details: listOfDetails){
					if(details.mStoreToDatabase){
						setOfObjectClusterChannels.add(details.mObjectClusterName);
//						setOfObjectClusterSensors.add(details.mDatabaseChannelHandle); AS: use this one better??
					}
				}
			}
		}
		
		return setOfObjectClusterChannels;
	}
	
	
}
