package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerLogDetails;
import com.shimmerresearch.driverUtilities.ShimmerSDCardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;
import com.shimmerresearch.sensor.AbstractSensor;
import com.shimmerresearch.shimmerUartProtocol.UartComponentPropertyDetails;

public abstract class ShimmerDevice extends BasicProcessWithCallBack implements Serializable{

	/** * */
	private static final long serialVersionUID = 5087199076353402591L;

	/**Holds unique location information on a dock or COM port number for Bluetooth connection*/
	public String mUniqueID = "";
	
	/** A shimmer device will have multiple sensors, depending on HW type and revision, 
	 * these type of sensors can change, this holds a list of all the sensors for different versions.
	 * This only works with classes which implements the ShimmerHardwareSensors interface. E.g. ShimmerGQ
	 * 
	 */
	protected LinkedHashMap<Integer,AbstractSensor> mMapOfSensors = new LinkedHashMap<Integer,AbstractSensor>();
//	protected HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>> mMapOfCommTypeToSensorMaps = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>>();

	public ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	public ExpansionBoardDetails mExpansionBoardDetails = new ExpansionBoardDetails();
	public ShimmerBattStatusDetails mShimmerBattStatusDetails = new ShimmerBattStatusDetails(); 
	public ShimmerSDCardDetails mShimmerSDCardDetails = new ShimmerSDCardDetails(); 

	public List<COMMUNICATION_TYPE> mListOfAvailableCommunicationTypes = new ArrayList<COMMUNICATION_TYPE>();
	
	//Temp here from ShimmerDocked - start
	
	/** Used in UART command through the base/dock*/
	protected String mMacIdFromUart = DEFAULT_MAC_ID;
	
	protected String mShimmerUserAssignedName = ""; // This stores the user assigned name
	protected HashMap<COMMUNICATION_TYPE, Double> mMapOfSamplingRatesShimmer = new HashMap<COMMUNICATION_TYPE, Double>(); // 51.2Hz is the default sampling rate 
//	protected double mSamplingRateShimmer; 	                                        	// 51.2Hz is the default sampling rate 

	{
		mMapOfSamplingRatesShimmer.put(COMMUNICATION_TYPE.SD, 51.2);
	}
	
	public final static String DEFAULT_DOCKID = "Default.01";
	public final static int DEFAULT_SLOTNUMBER = -1;
	public final static String DEFAULT_SHIMMER_NAME = "Shimmer";
	public final static String DEFAULT_EXPERIMENT_NAME = "DefaultTrial";
	public final static String DEFAULT_MAC_ID = "";
	
	public final static String DEVICE_ID = "Device_ID";

	public List<ShimmerLogDetails> mListofLogs = new ArrayList<ShimmerLogDetails>();
	
	public String mDockID = DEFAULT_DOCKID;
	public DEVICE_TYPE mDockType = DEVICE_TYPE.UNKOWN;
	public int mSlotNumber = DEFAULT_SLOTNUMBER;
	public static final int ANY_VERSION = -1;

	public boolean mReadHwFwSuccess = false;
	public boolean mConfigurationReadSuccess = false;
	public boolean mReadDaughterIDSuccess = false;
	public boolean writeRealWorldClockFromPcTimeSuccess = false;
	public boolean mFirstSdAccess = true;
	public boolean mReadSdAccessFail = false;
	
	protected boolean mIsConnected = false;
	protected boolean mIsSensing = false;
	protected boolean mIsSDLogging = false;											// This is used to monitor whether the device is in sd log mode
	protected boolean mIsStreaming = false;											// This is used to monitor whether the device is in streaming mode
	protected boolean mIsInitialised = false;
	protected boolean mIsDocked = false;
	protected boolean mHaveAttemptedToReadConfig = false;

	//TODO Below items are based on progress details being stored in each 
	// slotdetails, should these be removed in favor of the newer method for 
	// reporting operation progress information to the GUI (i.e. GUIDockManager 
	// vs. SmartSense)  
	public String mActivityLog = "";
	public int mFwImageWriteProgress = 0;
	public int mFwImageTotalSize = 0;
	public float mFwImageWriteSpeed = 0;
	public List<MsgDock> mListOfFailMsg = new ArrayList<MsgDock>();
	//Temp here from ShimmerDocked - end
	

	//TODO: are these variables too specific to different versions of Shimmer HW?
	public long mShimmerRealTimeClockConFigTime = 0;
	public long mShimmerLastReadRealTimeClockValue = 0;
	public String mShimmerLastReadRtcValueParsed = "";
	protected InfoMemLayout mInfoMemLayout;// = new InfoMemLayoutShimmer3(); //default
	protected byte[] mInfoMemBytes = InfoMemLayout.createEmptyInfoMemByteArray(512);
	
	protected String mTrialName = "";
	protected long mConfigTime; //this is in milliseconds, utc

	public long mPacketReceivedCount = 0; 	//Used by ShimmerGQ
	protected long mPacketLossCount = 0;		//Used by ShimmerBluetooth
	protected double mPacketReceptionRate = 100;
	protected double mPacketReceptionRateCurrent = 100;
	
	//Events markers
	protected int mEventMarkersCodeLast = 0;
	protected boolean mEventMarkersIsPulse = false;
	protected int mEventMarkers=0;
	
	// --------------- Abstract Methods Start --------------------------
	
	protected abstract void checkBattery();
	public abstract ShimmerDevice deepClone();

	public abstract void setDefaultShimmerConfiguration();

	// Device sensor map related
//	public abstract Map<Integer, SensorEnabledDetails> getSensorEnabledMap();
	public abstract Map<String, SensorGroupingDetails> getSensorGroupingMap();
	public abstract boolean setSensorEnabledState(int sensorMapKey, boolean state);
	public abstract List<Integer> sensorMapConflictCheck(Integer key);
	// Device Config related
	public abstract Map<String, SensorConfigOptionDetails> getConfigOptionsMap();
	public abstract void checkConfigOptionValues(String stringKey);
	public abstract void sensorAndConfigMapsCreate();
	
	/**
	 * @param object in some cases additional details might be required for building the packer format, e.g. inquiry response
	 */
	protected abstract void interpretDataPacketFormat(Object object,COMMUNICATION_TYPE commType);
		
	public abstract void infoMemByteArrayParse(byte[] infoMemContents);
	
	public abstract byte[] refreshShimmerInfoMemBytes();
	public abstract void createInfoMemLayout();

	/**Hash Map: Key integer, is to indicate the communication type, e.g. interpreting data via sd or bt might be different
	 * 
	 * Linked Hash Map :Integer is the index of where the sensor data for a particular sensor starts, String is the name of that particular sensor
	 * 
	 */
	protected HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,String>> mMapOfPacketFormat = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,String>>();
	

	public ShimmerDevice(){
		setThreadName("ShimmerDevice");
	}
	
	
	// --------------- Abstract Methods End --------------------------

	
	// --------------- Get/Set Methods Start --------------------------
	
	public void setShimmerVersionObject(ShimmerVerObject sVO) {
		mShimmerVerObject = sVO;
		sensorAndConfigMapsCreate();
	}
	
	public void clearShimmerVersionObject() {
		setShimmerVersionObject(new ShimmerVerObject());
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
	}
	
	/**
	 * @param shimmerUserAssignedName the mShimmerUserAssignedName to set
	 */
	public void setShimmerUserAssignedName(String shimmerUserAssignedName) {
		//Don't allow the first char to be numeric - causes problems with MATLAB variable names
		if(UtilShimmer.isNumeric("" + shimmerUserAssignedName.charAt(0))){
			shimmerUserAssignedName = "S" + shimmerUserAssignedName; 
		}
			
		//Limit the name to 12 Char
		if(shimmerUserAssignedName.length()>12) {
			this.mShimmerUserAssignedName = shimmerUserAssignedName.substring(0, 12);
		}
		else { 
			this.mShimmerUserAssignedName = shimmerUserAssignedName;
		}
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
		mEventMarkers += eventCode;
		
		//TOGGLE(1),
		//PULSE(2);
		//event type is defined in UtilDb, defined it here too
		if(eventType == 2){ 
			mEventMarkersIsPulse = true;
		}
	}
	
	public void setEventUntrigger(int eventCode){
		mEventMarkers -= eventCode;
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
		Collections.sort(mListOfAvailableCommunicationTypes);
		
		if(communicationType==COMMUNICATION_TYPE.DOCK){
			setDocked(true);
		}
	}

	public void removeCommunicationRoute(COMMUNICATION_TYPE communicationType) {
		if(mListOfAvailableCommunicationTypes.contains(communicationType)){
			mListOfAvailableCommunicationTypes.remove(communicationType);
		}
		Collections.sort(mListOfAvailableCommunicationTypes);
		
		if(communicationType==COMMUNICATION_TYPE.DOCK){
			setDocked(false);
			setFirstDockRead();
			clearDockInfo();
		}
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
	 * @return the mFirmwareIdentifier
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
	 * Get the FW Identifier. It is equal to 3 when LogAndStream, and equal to 4 when BTStream. 
	 * @return The FW identifier
	 */
	public int getFWIdentifier(){
		return mShimmerVerObject.mFirmwareIdentifier;
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
		return UtilShimmer.fromMilToDateExcelCompatible(Long.toString(mConfigTime*1000), false);
	}

	
	


	// --------------- Get/Set Methods End --------------------------

	
	//TODO improve this method - was changed at the last minute and is not fully operational
	public boolean checkIfVersionCompatible(List<ShimmerVerObject> listOfCompatibleVersionInfo) {
		if(listOfCompatibleVersionInfo == null) {
			return true;
		}
		
		for(ShimmerVerObject compatibleVersionInfo:listOfCompatibleVersionInfo) {

			boolean compatible = true;
			
			boolean checkHardwareVersion = false;
			boolean checkExpansionBoardId = false;
			boolean checkFirmwareIdentifier = false;
			boolean checkFirmwareVersionMajor = false;
			boolean checkFirmwareVersionMinor = false;
			boolean checkFirmwareVersionInternal = false;
			
			if(compatibleVersionInfo.mHardwareVersion!=ANY_VERSION) {
				checkHardwareVersion = true;
			}
			if(compatibleVersionInfo.mShimmerExpansionBoardId!=ANY_VERSION) {
				checkExpansionBoardId = true;
			}
			if(compatibleVersionInfo.mFirmwareIdentifier!=ANY_VERSION) {
				checkFirmwareIdentifier = true;
			}
			if(compatibleVersionInfo.mFirmwareVersionMajor!=ANY_VERSION) {
				checkFirmwareVersionMajor = true;
			}
			if(compatibleVersionInfo.mFirmwareVersionMinor!=ANY_VERSION) {
				checkFirmwareVersionMinor = true;
			}
			if(compatibleVersionInfo.mFirmwareVersionInternal!=ANY_VERSION) {
				checkFirmwareVersionInternal = true;
			}
			
			if((compatible)&&(checkHardwareVersion)) {
				if(getHardwareVersion() != compatibleVersionInfo.mHardwareVersion) {
					compatible = false;
				}
			}
			if((compatible)&&(checkExpansionBoardId)) {
				if(getExpansionBoardId() != compatibleVersionInfo.mShimmerExpansionBoardId) {
					compatible = false;
				}
			}
//			if((compatible)&&(checkFirmwareIdentifier)) {
//				if(getFirmwareIdentifier() != compatibleVersionInfo.getFirmwareIdentifier()) {
//					compatible = false;
//				}
//			}
			
//			if((compatible)&&(checkFirmwareVersionMajor)) {
//				if(getFirmwareVersionMajor() < compatibleVersionInfo.getFirmwareVersionMajor()) {
//					compatible = false;
//				}
//				if((compatible)&&(checkFirmwareVersionMinor)) {
//					if(getFirmwareVersionMinor() < compatibleVersionInfo.getFirmwareVersionMinor()) {
//						compatible = false;
//					}
//				}
//				if((compatible)&&(checkFirmwareVersionInternal)) {
//					if(getFirmwareVersionInternal() < compatibleVersionInfo.getFirmwareVersionInternal()) {
//						compatible = false;
//					}
//				}
//			}
//			else if((compatible)&&(checkFirmwareVersionMinor)) {
//				if(getFirmwareVersionMinor() < compatibleVersionInfo.getFirmwareVersionMinor()) {
//					compatible = false;
//				}
//				if((compatible)&&(checkFirmwareVersionInternal)) {
//					if(getFirmwareVersionInternal() < compatibleVersionInfo.getFirmwareVersionInternal()) {
//						compatible = false;
//					}
//				}
//			}
//			else if((compatible)&&(checkFirmwareVersionInternal)) {
//				if(getFirmwareVersionInternal() < compatibleVersionInfo.getFirmwareVersionInternal()) {
//					compatible = false;
//				}
//			}
			
			if(checkFirmwareVersionMajor){
				// Using the tree structure below each of the FW Major, Minor or Internal Release variables can be ignored
				if((compatible)&&(!UtilShimmer.compareVersions(getFirmwareIdentifier(), 
						getFirmwareVersionMajor(), 
						getFirmwareVersionMinor(), 
						getFirmwareVersionInternal(), 
						compatibleVersionInfo.mFirmwareIdentifier, 
						compatibleVersionInfo.mFirmwareVersionMajor, 
						compatibleVersionInfo.mFirmwareVersionMinor, 
						compatibleVersionInfo.mFirmwareVersionInternal))){
					compatible = false;
				}
			}
			
			if(compatible) {
				return true;
			}
		}
		return false;
	}
	
	public double getSamplingRateShimmer(COMMUNICATION_TYPE communicationType){
		return mMapOfSamplingRatesShimmer.get(communicationType); 
	}
	
	public void setSamplingRateShimmer(COMMUNICATION_TYPE communicationType, double samplingRate){
		mMapOfSamplingRatesShimmer.put(communicationType, samplingRate);
	}

//	public void setShimmerSamplingRate(double samplingRate){
//		mSamplingRateShimmer = samplingRate;
//	}

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
		if(mDockID.contains(HwDriverShimmerDeviceDetails.DOCK_LABEL[HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASICDOCK.ordinal()])){
			mDockType = DEVICE_TYPE.BASICDOCK;
			//Below to ensure Shimmer shows as unknown in BasicDock rather then pending
			mHaveAttemptedToReadConfig = true;
		}
		else if(mDockID.contains(HwDriverShimmerDeviceDetails.DOCK_LABEL[HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASE15.ordinal()])){
			mDockType = DEVICE_TYPE.BASE15;
		}
		else if(mDockID.contains(HwDriverShimmerDeviceDetails.DOCK_LABEL[HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASE6.ordinal()])){
			mDockType = DEVICE_TYPE.BASE6;
		}
		else {
			mDockType = DEVICE_TYPE.UNKOWN;
		}
	}
	
	public ObjectCluster buildMsg(byte[] dataPacketFormat, byte[] packetByteArray,COMMUNICATION_TYPE commType){
		interpretDataPacketFormat(dataPacketFormat, commType);
		return buildMsg(packetByteArray, commType);
	}
	
	/** The packet format can be changed by calling interpretpacketformat
	 * @param packetByteArray
	 * @param commType
	 * @return
	 */
	public ObjectCluster buildMsg(byte[] packetByteArray, COMMUNICATION_TYPE commType){
		
		ObjectCluster ojc = new ObjectCluster(mShimmerUserAssignedName, getMacId());
		ojc.createArrayData(getNumberOfEnabledChannels(commType));
//		ojc.mMyName = mUniqueID;
		int index=0;
		for (AbstractSensor sensor:mMapOfSensors.values()){
			int length = sensor.getExpectedPacketByteArray(commType);
			//TODO process API sensors, not just bytes from Shimmer packet 
			if (length!=0){ //if length 0 means there are no channels to be processed
				byte[] sensorByteArray = new byte[length];
				if((index+sensorByteArray.length)<=packetByteArray.length){
					System.arraycopy(packetByteArray, index, sensorByteArray, 0, sensorByteArray.length);
					sensor.processData(sensorByteArray, commType, ojc);
				}
				else{
					//TODO replace with consolePrintSystem
					System.out.println(mShimmerUserAssignedName + " ERROR PARSING " + sensor.getSensorName());
				}
			}
			index += length;
		}
		return ojc;
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
		for (AbstractSensor sensor:mMapOfSensors.values()){
			total = total + sensor.getNumberOfEnabledChannels(commType);
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
	
	public byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer){
		return mInfoMemBytes;
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
		mReadSdAccessFail = false;
	}
	
	// ----------------- Overrides from ShimmerDevice end -------------

	public LinkedHashMap<Integer, AbstractSensor> getMapOfSensors() {
		return mMapOfSensors;
	}
	
	@Deprecated
	public LinkedHashMap<Integer, AbstractSensor> getMapOfSensorsForCommType(COMMUNICATION_TYPE commType) {
		LinkedHashMap<Integer, AbstractSensor> mapOfSensorsForCommType = new LinkedHashMap<Integer, AbstractSensor>(); 
		for(int abstractSensorKey:mMapOfSensors.keySet()){
			AbstractSensor abstractSensor = mMapOfSensors.get(abstractSensorKey);
			if(abstractSensor.mMapOfCommTypeToSensorMap.containsKey(commType)){
				mapOfSensorsForCommType.put(abstractSensorKey, abstractSensor);
			}
		}
		return mapOfSensorsForCommType;
	}
	
	@Deprecated
	public Map<Integer, SensorEnabledDetails> getMapOfSensorEnabledForCommType(COMMUNICATION_TYPE commType) {
		LinkedHashMap<Integer, SensorEnabledDetails> mapOfSensorsForCommType = new LinkedHashMap<Integer, SensorEnabledDetails>(); 
		for(int abstractSensorKey:mMapOfSensors.keySet()){
			AbstractSensor abstractSensor = mMapOfSensors.get(abstractSensorKey);
			if(abstractSensor.mMapOfCommTypeToSensorMap.containsKey(commType)){
				LinkedHashMap<Integer, SensorEnabledDetails> temp = abstractSensor.mMapOfCommTypeToSensorMap.get(commType);
				if(temp!=null){
					mapOfSensorsForCommType.putAll(temp);
				}
			}
		}
		return mapOfSensorsForCommType;
	}
	
	/** not working because channels don't have a unique integer value so values in output map are just being replaced*/
	public LinkedHashMap<Integer, ChannelDetails> getMapOfChannelsForCommType(COMMUNICATION_TYPE commType) {
		LinkedHashMap<Integer,ChannelDetails> mapOfCommTypetoAllChannels = new LinkedHashMap<Integer,ChannelDetails>(); 
		for(AbstractSensor abstractSensor:mMapOfSensors.values()){
			LinkedHashMap<Integer, ChannelDetails> mapOfCommTypetoChannel = abstractSensor.mMapOfCommTypetoChannel.get(commType);
			if(mapOfCommTypetoChannel!=null){
				mapOfCommTypetoAllChannels.putAll(mapOfCommTypetoChannel);
			}
		}
		return mapOfCommTypetoAllChannels;
	}


	public LinkedHashMap<Integer, LinkedHashMap<Integer, ChannelDetails>> getMapOfChannelsPerSensorForCommType(COMMUNICATION_TYPE commType) {
		LinkedHashMap<Integer,LinkedHashMap<Integer,ChannelDetails>> mapOfChannelsPerSensorForCommType = new LinkedHashMap<Integer,LinkedHashMap<Integer,ChannelDetails>>();
		for(int abstractSensorKey:mMapOfSensors.keySet()){
			AbstractSensor abstractSensor = mMapOfSensors.get(abstractSensorKey);
			LinkedHashMap<Integer, ChannelDetails> mapOfCommTypetoChannel = abstractSensor.mMapOfCommTypetoChannel.get(commType);
			if(mapOfCommTypetoChannel!=null){
				mapOfChannelsPerSensorForCommType.put(abstractSensorKey, mapOfCommTypetoChannel);
			}
		}
		return mapOfChannelsPerSensorForCommType;
	}
	
    public abstract boolean doesSensorKeyExist(int sensorKey);

    public abstract boolean isChannelEnabled(int sensorKey);
//    private boolean isChannelEnabled(int sensorKey) {
//		if(selectedDockedShimmer instanceof ShimmerPCMSS){
//		    Map<Integer, SensorEnabledDetails> sensorMap = ((ShimmerPCMSS)selectedDockedShimmer).getSensorEnabledMap();
//			SensorEnabledDetails sensor = sensorMap.get(sensorKey);
//		    if(sensor!=null){
//			    return sensor.mIsEnabled;
//		    }
//		}
//		else if(selectedDockedShimmer instanceof ShimmerGQ_802154){
//		    Map<Integer, AbstractSensor> sensorMap = ((ShimmerGQ_802154)selectedDockedShimmer).getMapOfSensors();
//		    AbstractSensor sensor = sensorMap.get(sensorKey);
//		    if(sensor!=null){
//			    return sensor.mIsEnabled;
//		    }
//		}
//		return false;
//	}

    public abstract String getChannelLabel(int sensorKey);
//    private String getChannelLabel(int sensorKey) {
//		if(selectedDockedShimmer instanceof ShimmerPCMSS){
//		    Map<Integer, SensorEnabledDetails> sensorMap = ((ShimmerPCMSS)selectedDockedShimmer).getSensorEnabledMap();
//			SensorEnabledDetails sensor = sensorMap.get(sensorKey);
//		    if(sensor!=null){
//			    return sensor.mSensorDetails.mLabel;
//		    }
//		}
//		else if(selectedDockedShimmer instanceof ShimmerGQ_802154){
//		    Map<Integer, AbstractSensor> sensorMap = ((ShimmerGQ_802154)selectedDockedShimmer).getMapOfSensors();
//		    AbstractSensor sensor = sensorMap.get(sensorKey);
//		    if(sensor!=null){
//			    return sensor.mGuiFriendlyLabel;
//		    }
//		}
//		return "";
//	}

    public abstract List<ShimmerVerObject> getListOfCompatibleVersionInfo(int sensorKey);
//    private List<ShimmerVerObject> getListOfCompatibleVersionInfo(int sensorKey) {
//		if(this instanceof ShimmerPCMSS){
//		    Map<Integer, SensorEnabledDetails> sensorMap = ((ShimmerPCMSS)selectedDockedShimmer).getSensorEnabledMap();
//			SensorEnabledDetails sensor = sensorMap.get(sensorKey);
//		    if(sensor!=null){
//			    return sensor.mSensorDetails.mListOfCompatibleVersionInfo;
//		    }
//		}
//		else if(selectedDockedShimmer instanceof ShimmerGQ_802154){
//		    Map<Integer, AbstractSensor> sensorMap = ((ShimmerGQ_802154)selectedDockedShimmer).getMapOfSensors();
//		    AbstractSensor sensor = sensorMap.get(sensorKey);
//		    if(sensor!=null){
//			    return sensor.mListOfCompatibleVersionInfo;
//		    }
//		}
//		return null;
//	}

	public abstract Set<Integer> getSensorMapKeySet();

	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet){
		Object returnValue = null;
		int buf = 0;

		switch(componentName){
//Booleans
//Integers

//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
        		setShimmerUserAssignedName((String)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
        		setTrialName((String)valueToSet);
	        	break;
	        default:
	        	break;
		}
		
		return returnValue;		
	}
	
	public Object getConfigValueUsingConfigLabel(String componentName){
		Object returnValue = null;
		
		switch(componentName){
//Booleans

//Integers
	    		
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

	
}
