package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
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
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.sensor.AbstractSensor;
import com.shimmerresearch.sensor.AbstractSensor.SENSOR_NAMES;
import com.shimmerresearch.uartViaDock.ComponentPropertyDetails;

public abstract class ShimmerDevice extends BasicProcessWithCallBack implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5087199076353402591L;

	/**Holds unique location information on a dock or COM port number for Bluetooth connection*/
	public String mUniqueID = "";
	
	/** A shimmer device will have multiple sensors, depending on HW type and revision, 
	 * these type of sensors can change, this holds a list of all the sensors for different versions.
	 * This only works with classes which implements the ShimmerHardwareSensors interface. E.g. ShimmerGQ
	 * 
	 */
	protected HashMap<String,AbstractSensor> mMapOfSensors = new HashMap<String,AbstractSensor>();
	
	public ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	public ExpansionBoardDetails mExpansionBoardDetails = new ExpansionBoardDetails();
	public ShimmerBattStatusDetails mShimmerBattStatusDetails = new ShimmerBattStatusDetails(); 
	public ShimmerSDCardDetails mShimmerSDCardDetails = new ShimmerSDCardDetails(); 

	private List<COMMUNICATION_TYPE> mListOfAvailableCommunicationTypes = new ArrayList<COMMUNICATION_TYPE>();
	
	//Temp here from ShimmerDocked - start
	
	/** Used in UART command through the base/dock*/
	protected String mMacIdFromUart = "";
	
	protected String mShimmerUserAssignedName = ""; // This stores the user assigned name

	public final static String DEFAULT_DOCKID = "Default.01";
	public final static int DEFAULT_SLOTNUMBER = 1;
	public final static String DEFAULT_SHIMMER_NAME = "Shimmer";
	public final static String DEFAULT_EXPERIMENT_NAME = "DefaultTrial";
	
	public final static String DEVICE_ID = "Device_ID";

	public List<ShimmerLogDetails> mListofLogs = new ArrayList<ShimmerLogDetails>();
	
	public boolean mFirstSdAccess = true;
	public String mDockID;
	public DEVICE_TYPE mDockType = DEVICE_TYPE.UNKOWN;
	public int mSlotNumber = -1;
	public static final int ANY_VERSION = -1;

	public boolean mReadHwFwSuccess = false;
	public boolean mConfigurationReadSuccess = false;
	public boolean mReadDaughterIDSuccess = false;
	public boolean writeRealWorldClockFromPcTimeSuccess = false;
	
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
	public InfoMemLayout mInfoMemLayout = new InfoMemLayoutShimmer3(); //default
	protected byte[] mInfoMemBytes = createEmptyInfoMemByteArray(512);
	
	protected String mTrialName = "";
	protected long mConfigTime; //this is in milliseconds, utc

	protected long mPacketLossCount=0;
	protected double mPacketReceptionRate=100;
	protected double mPacketReceptionRateCurrent=100;

	// --------------- Abstract Methods Start --------------------------
	
	protected abstract void checkBattery();
	public abstract ShimmerDevice deepClone();

	public abstract void setDefaultShimmerConfiguration();

	// Device sensor map related
	public abstract Map<Integer, SensorEnabledDetails> getSensorMap();
	public abstract boolean setSensorEnabledState(int sensorMapKey, boolean state);
	public abstract List<Integer> sensorMapConflictCheck(Integer key);
	public abstract Map<String, SensorGroupingDetails> getSensorGroupingMap();
	// Device Config related
	public abstract Map<String, SensorConfigOptionDetails> getConfigOptionsMap();
	public abstract Object setConfigValueUsingConfigLabel(String componentName, Object configValue);
	public abstract Object getConfigValueUsingConfigLabel(String componentName);
	public abstract void checkConfigOptionValues(String stringKey);
	public abstract void sensorAndConfigMapsCreate();
		
	public abstract void infoMemByteArrayParse(byte[] infoMemContents);
	
	public abstract byte[] refreshShimmerInfoMemBytes();
	/**Hash Map: Key integer, is to indicate the communication type, e.g. interpreting data via sd or bt might be different
	 * 
	 * Linked Hash Map :Integer is the index of where the sensor data for a particular sensor starts, String is the name of that particular sensor
	 * 
	 */
	protected HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,String>> mMapOfPacketFormat = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,String>>();
	

	
	// --------------- Abstract Methods End --------------------------

	
	// --------------- Set Methods Start --------------------------
	
	public void setShimmerVersionObject(ShimmerVerObject sVO) {
		mShimmerVerObject = sVO;
		sensorAndConfigMapsCreate();
		interpretDataPacketFormat(null);
	}
	
	public void clearShimmerVersionObject() {
		setShimmerVersionObject(new ShimmerVerObject());
	}

	/** If the device instance is already created use this to add a dock communication type to the instance
	 * @param dockID
	 * @param slotNumber
	 */
	public void addDOCKCoummnicationRoute(String dockId, int slotNumber) {
		mDockID = dockId;
		parseDockType();
		
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
		
		addCommunicationRoute(COMMUNICATION_TYPE.DOCK);
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
	 * @param mShimmerUserAssignedName the mShimmerUserAssignedName to set
	 */
	public void setShimmerUserAssignedName(String mShimmerUserAssignedName) {
		if(mShimmerUserAssignedName.length()>12) {
			this.mShimmerUserAssignedName = mShimmerUserAssignedName.substring(0, 12);
		}
		else { 
			this.mShimmerUserAssignedName = mShimmerUserAssignedName;
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
	
	public void addCommunicationRoute(COMMUNICATION_TYPE communicationType) {
		mListOfAvailableCommunicationTypes.add(communicationType);
		Collections.sort(mListOfAvailableCommunicationTypes);
	}

	public void removeCommunicationRoute(COMMUNICATION_TYPE communicationType) {
		mListOfAvailableCommunicationTypes.remove(communicationType);
		Collections.sort(mListOfAvailableCommunicationTypes);
	}

	// --------------- Set Methods End --------------------------

	
	
	// --------------- Get Methods Start --------------------------
	
	
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
	 * @param docked the mDocked to set
	 */
	public void setDocked(boolean docked) {
		mIsDocked = docked;
	}

	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean isHaveAttemptedToRead() {
		return haveAttemptedToRead();
	}

	/**
	 * @return the mDocked
	 */
	public boolean isDocked() {
		return mIsDocked;
	}
	
	/**
	 * @return the mIsConnected
	 */
	public boolean isConnected() {
		return mIsConnected;
	}

	public void setConnected(boolean state) {
		mIsConnected = state;
	}

    /** Returns true if device is streaming (Bluetooth)
     * @return
     */
    public boolean isStreaming(){
    	return mIsStreaming;
    }

	/**Only used for LogAndStream
	 * @return
	 */
	public boolean isSensing(){
		return (mIsSensing || mIsSDLogging || mIsStreaming);
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

	// --------------- Get Methods End --------------------------

	
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
	
	/**
	 * Creates an empty byte array for the purposes of generating the
	 * configuration bytes to write to the Shimmer (default all bytes = 0xFF).
	 * 
	 * @param size the size of the byte array to create.
	 * @return byte array
	 */
	public byte[] createEmptyInfoMemByteArray(int size) {
		byte[] newArray = new byte[size];
		for(byte b:newArray) {
			b = (byte)0xFF;
		}
		return newArray;
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
	
	protected void createInfoMemLayout(){
		mInfoMemLayout = new InfoMemLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
	}
	
	protected void parseDockType(){
		if(mDockID.contains(HwDriverShimmerDeviceDetails.DOCK_LABEL[HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASICDOCK.ordinal()])){
			mDockType = DEVICE_TYPE.BASICDOCK;
		}
		else if(mDockID.contains(HwDriverShimmerDeviceDetails.DOCK_LABEL[HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASE15.ordinal()])){
			mDockType = DEVICE_TYPE.BASE15;
		}
		else if(mDockID.contains(HwDriverShimmerDeviceDetails.DOCK_LABEL[HwDriverShimmerDeviceDetails.DEVICE_TYPE.BASE6.ordinal()])){
			mDockType = DEVICE_TYPE.BASE6;
		}
	}
	
	public Object buildMsg(byte[] packetByteArray,COMMUNICATION_TYPE comType){
		if (mShimmerVerObject.mFirmwareIdentifier == FW_ID.GQ_802154){
			interpretDataPacketFormat(packetByteArray[0]);
		}
		ObjectCluster ojc = new ObjectCluster();
		for (int index:mMapOfPacketFormat.get(comType).keySet()){
			AbstractSensor sensor = mMapOfSensors.get(mMapOfPacketFormat.get(comType).get(index));
			sensor.processData(packetByteArray,comType,ojc);
		}
		return ojc;
	}
	
	public byte[] generateUartConfigMessage(ComponentPropertyDetails cPD){
		//TODO: process common ShimmerDevice configs
		return null;
	}
	
	public void parseUartConfigResponse(ComponentPropertyDetails cPD, byte[] response) {
		//TODO: process common ShimmerDevice configs
	}

	/**
	 * @param object in some cases additional details might be required for building the packer format, e.g. inquiry response
	 */
	private void interpretDataPacketFormat(Object object){
		if (mShimmerVerObject.mFirmwareIdentifier == FW_ID.GQ_802154){
			if (object==null){
				LinkedHashMap<Integer,String> mIndexToSensor = new LinkedHashMap<Integer,String>();
				//byte 0 is the enabled sensors and assuming no time stamps
				mIndexToSensor.put(1,SENSOR_NAMES.GSR);
				mIndexToSensor.put(3,SENSOR_NAMES.ECG_TO_HR);
				mMapOfPacketFormat.put(COMMUNICATION_TYPE.IEEE802154,mIndexToSensor);
				mMapOfPacketFormat.put(COMMUNICATION_TYPE.SD,mIndexToSensor); //assuming this is the same
			} else {
				//TODO: some bit tester to find enabled sensors
				LinkedHashMap<Integer,String> mIndexToSensor = new LinkedHashMap<Integer,String>();
				//byte 0 is the enabled sensors and assuming no time stamps
				mIndexToSensor.put(1,SENSOR_NAMES.CLOCK); //assuming clock is 3 bytes
				mIndexToSensor.put(4,SENSOR_NAMES.GSR); 
				mIndexToSensor.put(6,SENSOR_NAMES.ECG_TO_HR);
				mMapOfPacketFormat.put(COMMUNICATION_TYPE.IEEE802154,mIndexToSensor);
				mMapOfPacketFormat.put(COMMUNICATION_TYPE.SD,mIndexToSensor); //assuming this is the same
			}
		} 
		
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
	
}
