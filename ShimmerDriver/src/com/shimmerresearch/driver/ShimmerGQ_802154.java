package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shimmerresearch.comms.wiredProtocol.UartComponentPropertyDetails;
import com.shimmerresearch.comms.wiredProtocol.UartPacketDetails.UART_COMPONENT_PROPERTY;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.SensorECGToHRFw;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorSystemTimeStampGq;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.ShimmerClock;

/**
 * @author JC
 * @author Mark Nolan
 */
public class ShimmerGQ_802154 extends ShimmerDevice implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 76977946997596234L;
	
	/** This is derived from all the sensors
	 * 
	 */
	public static final int VARIABLE_NOT_SET = -1;
	public int mRadioChannel = VARIABLE_NOT_SET;
	public int mRadioGroupId = VARIABLE_NOT_SET;
	public int mRadioDeviceId = VARIABLE_NOT_SET;
	public int mRadioResponseWindow = VARIABLE_NOT_SET; 

	public String mSpanId = "";
	
	//TODO generate from Sensor classes
	public static final int SENSOR_ECG_TO_HR_FW				= (0x40 << (8*1));
	public long mEnabledSensors = SENSOR_ECG_TO_HR_FW + Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_24BIT + Configuration.Shimmer3.SensorBitmap.SENSOR_GSR;
	public static final int ALGORITHM_ECG_TO_HR_CHP1_CH1	= (0x80 << (8*1));
	public long mDerivedSensors = ALGORITHM_ECG_TO_HR_CHP1_CH1;
	
	//TODO tidy: carried from ShimmerObject
	public int mInternalExpPower = 1;			// Enable external power for EXG + GSR
	public boolean mSyncWhenLogging = true;
	public boolean mIsFwTestMode = false;
//	public boolean mIsSDError = false;
	
	/** Read from the InfoMem from UART command through the base/dock*/
	protected String mMacIdFromInfoMem = "";
	
	 //just use fillers for now
	public static final ShimmerVerObject SVO_RELEASE_REV_0_1 = new ShimmerVerObject(
			HW_ID.UNKNOWN, 
			FW_ID.UNKNOWN,
			FW_ID.UNKNOWN, 
			FW_ID.UNKNOWN, 
			FW_ID.UNKNOWN,
			FW_ID.UNKNOWN);
	
	//Action Setting Checker, if cant do pass it to next layer to be handled
	
	//priority of comm type to know whether it is dock and radio connected, if dock connected send uart priority
	
	public enum GQ_STATE{
		IDLE("Idle"),
		STREAMING("Streaming"),
		STREAMING_AND_SDLOGGING("Streaming and SD Logging"),
		FW_TEST_MODE("FW Test Mode"), 
		SD_ERROR("SD Card Error");
		
	    private final String text;

	    /** @param text */
	    private GQ_STATE(final String text) {
	        this.text = text;
	    }

	    @Override
	    public String toString() {
	        return text;
	    }
	}
	public GQ_STATE mState = GQ_STATE.IDLE;
	private int mCurrentSessionIdStreamingToDB = 0;

	private int mSyncSuccessCount = 0;
	private long mLastSyncSuccessTime = 0;
	
	// ----------------- Constructors Start ---------------------------------
	
	/**
	 * @param shimmerVersionObject the FW and HW details of the devices
	 */
	public ShimmerGQ_802154(ShimmerVerObject sVO) {
		super.setShimmerVersionInfoAndCreateSensorMap(sVO);
	}

	/**
	 * @param dockId
	 * @param slotNumber
	 * @param connectionType
	 */
	public ShimmerGQ_802154(String dockId, int slotNumber, COMMUNICATION_TYPE connectionType) {
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(connectionType);
	}
	
	/**
	 * @param dockId
	 * @param slotNumber
	 * @param macId
	 * @param connectionType
	 */
	public ShimmerGQ_802154(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE connectionType){
		this(dockId, slotNumber, connectionType);
		setMacIdFromUart(macId);
	}


	// ----------------- Constructors End ---------------------------------

	
	// ----------------- Local Sets/Gets Start ----------------------------

	/**
	 * @param radioConfigArray
	 */
	public void setRadioConfig(byte[] radioConfigArray) {
		if(radioConfigArray.length>=7){
	        this.mRadioChannel = radioConfigArray[0] & 0x00FF;
	        
	        //All MSB first
	        this.mRadioGroupId = (((radioConfigArray[1]&0x00FF)<<8) + (radioConfigArray[2]&0x00FF)) & 0x00FFFF;
	        this.mRadioDeviceId = (((radioConfigArray[3]&0x00FF)<<8) + (radioConfigArray[4]&0x00FF)) & 0x00FFFF;
	        this.mRadioResponseWindow  = (((radioConfigArray[5]&0x00FF)<<8) + (radioConfigArray[6]&0x00FF)) & 0x00FFFF;
		}
	}
	
	/**
	 * @param radioChannel
	 * @param radioGroupId
	 * @param radioDeviceId
	 * @param radioResponseWindow
	 */
	public void setRadioConfig(int radioChannel, int radioGroupId, int radioDeviceId, int radioResponseWindow) {
        this.mRadioChannel = radioChannel;
        this.mRadioGroupId = radioGroupId;
        this.mRadioDeviceId = radioDeviceId;
        this.mRadioResponseWindow = radioResponseWindow;
	}
	
	public byte[] getRadioConfigByteArray() {
		return UtilShimmer.generateRadioConfigByteArray(mRadioChannel, mRadioGroupId, mRadioDeviceId, mRadioResponseWindow);
	}

	/**
	 * @param statusByte
	 */
	public void parseStatusByte(byte statusByte){
//		Boolean savedDockedState = mIsDocked;
		
		mIsDocked = ((statusByte & (0x01 << 0)) > 0)? true:false;
		mIsSensing = ((statusByte & (0x01 << 1)) > 0)? true:false;
//		reserved = ((statusByte & (0x01 << 2)) > 0)? true:false;
		mIsSDLogging = ((statusByte & (0x01 << 3)) > 0)? true:false;
		mIsStreaming = ((statusByte & (0x01 << 4)) > 0)? true:false; 
		mIsFwTestMode = ((statusByte & (0x01 << 5)) > 0)? true:false;
		mIsSDError = ((statusByte & (0x01 << 6)) > 0)? true:false;

		consolePrintLn("Status Response = " + UtilShimmer.byteToHexStringFormatted(statusByte)
				+ "\t" + "IsDocked = " + mIsDocked
				+ "\t" + "IsSensing = " + mIsSensing
				+ "\t" + "IsSDLogging = "+ mIsSDLogging
				+ "\t" + "IsStreaming = " + mIsStreaming
				+ "\t" + "mIsFwTestMode = " + mIsFwTestMode
				+ "\t" + "mIsSdError = " + mIsSDError
				);
		
//		if(savedDockedState!=mIsDocked){
//			dockedStateChange();
//		}
		
		if(mIsSDError){
			mState = GQ_STATE.SD_ERROR;
		}
		else if(mIsFwTestMode){
			mState = GQ_STATE.FW_TEST_MODE;
		}
		else if(mIsStreaming&&mIsSDLogging){
			mState = GQ_STATE.STREAMING_AND_SDLOGGING;
		}
		else if(mIsStreaming){
			mState = GQ_STATE.STREAMING;
		}
		else {
			mState = GQ_STATE.IDLE;
		}
		
	}

	
	public double calculatePacketLoss(long timeDifference, double samplingRate){
		mPacketExpectedCount = (long) ((double)((timeDifference/1000)*samplingRate));
		return calculatePacketLoss(mPacketExpectedCount, mPacketReceivedCount);
	}

	public double calculatePacketLoss(){
		return calculatePacketLoss(mPacketExpectedCount, mPacketReceivedCount);
	}

	private double calculatePacketLoss(long packetExpectedCount, long packetReceivedCount){
		mPacketReceptionRateCurrent = (double)((packetReceivedCount)/(double)packetExpectedCount)*100;
		mPacketReceptionRateCurrent = UtilShimmer.nudgeDouble(mPacketReceptionRateCurrent, 0.0, 100.0);
		return mPacketReceptionRateCurrent;
	}
	

	
//	@Override
//	protected void createInfoMemLayout(){
//		mInfoMemLayout = new InfoMemLayoutShimmerGq802154(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
//	}

	// ----------------- Local Sets/Gets End ----------------------------


	// ----------------- Overrides from ShimmerDevice start -------------

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		//NOT USED IN THIS CLASS
	}

	@Override
	public ShimmerGQ_802154 deepClone() {
//		System.out.println("Cloning:" + mUniqueID);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ShimmerGQ_802154) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public void checkConfigOptionValues(String stringKey) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public Object setConfigValueUsingConfigLabel(String groupName, String componentName, Object valueToSet) {

		Object returnValue = null;
		int buf = 0;

		switch(componentName){
//Booleans
//Integers
//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
	    		setShimmerUserAssignedNameWithMac((String)valueToSet);
	        	break;
		
	        default:
	        	returnValue = super.setConfigValueUsingConfigLabel(groupName, componentName, valueToSet);
	        	break;
		}
			
		return returnValue;
	}	
	
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
    	returnValue = super.getConfigValueUsingConfigLabel(componentName);
		return returnValue;
	}

	@Override
	public byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {
		
		InfoMemLayoutShimmerGq802154 infoMemLayout = new InfoMemLayoutShimmerGq802154(
				getFirmwareIdentifier(), 
				getFirmwareVersionMajor(), 
				getFirmwareVersionMinor(), 
				getFirmwareVersionInternal());
		
//		byte[] infoMemBackup = mInfoMemBytes.clone();
		mInfoMemBytes = infoMemLayout.createEmptyInfoMemByteArray();
		
		setSamplingRateShimmer(COMMUNICATION_TYPE.SD, 256);	// 256Hz is the default sampling rate

		// Sampling Rate
		int samplingRate = (int)(32768 / getSamplingRateShimmer(COMMUNICATION_TYPE.SD));
		mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate] = (byte) (samplingRate & infoMemLayout.maskShimmerSamplingRate); 
		mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8) & infoMemLayout.maskShimmerSamplingRate); 

		//TODO loop through mapOfSensors
		// Sensors
//		checkExgResolutionFromEnabledSensorsVar();
//		refreshEnabledSensorsFromSensorMap();
		mEnabledSensors = SENSOR_ECG_TO_HR_FW + Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_24BIT + Configuration.Shimmer3.SensorBitmap.SENSOR_GSR;
		mInfoMemBytes[infoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors0) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors1) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors2) & infoMemLayout.maskSensors);

		//TODO loop through mapOfAlgorithms
		// Derived Sensors
		mDerivedSensors = ALGORITHM_ECG_TO_HR_CHP1_CH1;
		mInfoMemBytes[infoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors0) & infoMemLayout.maskDerivedChannelsByte);
		mInfoMemBytes[infoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors1) & infoMemLayout.maskDerivedChannelsByte);
		mInfoMemBytes[infoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors2) & infoMemLayout.maskDerivedChannelsByte);
		
		// Shimmer Name
		for (int i = 0; i < infoMemLayout.lengthShimmerName; i++) {
			if (i < mShimmerUserAssignedName.length()) {
				mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
			}
			else {
				mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) 0xFF;
			}
		}
		
		// Experiment Name
		for (int i = 0; i < infoMemLayout.lengthExperimentName; i++) {
			if (i < mTrialName.length()) {
				mInfoMemBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) mTrialName.charAt(i);
			}
			else {
				mInfoMemBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) 0xFF;
			}
		}

		//Configuration Time
		mInfoMemBytes[infoMemLayout.idxSDConfigTime0] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime0) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime1] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime1) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime2] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime2) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime3] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime3) & 0xFF);
		
		mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) (((mSyncWhenLogging? infoMemLayout.maskTimeSyncWhenLogging:0)) << infoMemLayout.bitShiftTimeSyncWhenLogging);

		//802.15.4 Radio config
		byte[] radioConfig = getRadioConfigByteArray();
		System.arraycopy(radioConfig, 0, mInfoMemBytes, infoMemLayout.idxSrRadioConfigStart, radioConfig.length);

		
		// Configuration from each Sensor settings
		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
			abstractSensor.infoMemByteArrayGenerate(this, mInfoMemBytes);
		}
		
		//EXG Configuration
		//TODO Temp here to get some ExGBytes
//		ShimmerEXGSensor shimmerExgSensor = new ShimmerEXGSensor(mShimmerVerObject);
//		shimmerExgSensor.setExgGq(getSamplingRateShimmer(COMMUNICATION_TYPE.SD));
//		shimmerExgSensor.infoMemByteArrayGenerate(this, mInfoMemBytes);
		
		AbstractSensor shimmerExgSensor = mMapOfSensorClasses.get(SENSORS.EXG);
		if(shimmerExgSensor!=null){
			((SensorEXG)shimmerExgSensor).setExgGq(getSamplingRateShimmer(COMMUNICATION_TYPE.SD));
			shimmerExgSensor.infoMemByteArrayGenerate(this, mInfoMemBytes);
		}
		else {
			System.err.println("ERROR: SHIMMERGQ, not EXG sensor present in map");
		}
		
		//Check if Expansion board power is required for any of the enabled sensors
		//TODO replace with checkIfInternalExpBrdPowerIsNeeded from ShimmerObject
		super.checkIfInternalExpBrdPowerIsNeeded();
//		mInternalExpPower = 0;
//		for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
//			if(abstractSensor.mIntExpBoardPowerRequired && abstractSensor.isAnySensorChannelEnabled(COMMUNICATION_TYPE.IEEE802154)){
//				mInternalExpPower = 1;
//				break;
//			}
//		}
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & infoMemLayout.maskEXPPowerEnable) << infoMemLayout.bitShiftEXPPowerEnable);

		
		return mInfoMemBytes;
	}

	@Override
	public void infoMemByteArrayParse(byte[] infoMemBytes) {
		
		String shimmerName = "";

		if(!InfoMemLayout.checkInfoMemValid(infoMemBytes)){
			// InfoMem not valid
			setDefaultShimmerConfiguration();
			//TODO
//			mShimmerUsingConfigFromInfoMem = false;

//			mShimmerInfoMemBytes = infoMemByteArrayGenerate();
//			mShimmerInfoMemBytes = new byte[infoMemContents.length];
			mInfoMemBytes = infoMemBytes;
		}
		else {
			mInfoMemBytes = infoMemBytes;
			
			InfoMemLayoutShimmerGq802154 infoMemLayoutCast = (InfoMemLayoutShimmerGq802154) mInfoMemLayout;

			//TODO
//			mShimmerUsingConfigFromInfoMem = true;

			createInfoMemLayoutObjectIfNeeded();
			
			// Sampling Rate
			setSamplingRateShimmer(COMMUNICATION_TYPE.SD, (32768/(double)((int)(infoMemBytes[infoMemLayoutCast.idxShimmerSamplingRate] & infoMemLayoutCast.maskShimmerSamplingRate) 
					+ ((int)(infoMemBytes[infoMemLayoutCast.idxShimmerSamplingRate+1] & infoMemLayoutCast.maskShimmerSamplingRate) << 8))));

			// Sensors
			mEnabledSensors = ((long)infoMemBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;

			//TODO
//			checkExgResolutionFromEnabledSensorsVar();

			// Configuration
			mInternalExpPower = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte3] >> infoMemLayoutCast.bitShiftEXPPowerEnable) & infoMemLayoutCast.maskEXPPowerEnable;
			
			// Configuration from each Sensor settings
			for(AbstractSensor abstractSensor:mMapOfSensorClasses.values()){
				abstractSensor.infoMemByteArrayParse(this, mInfoMemBytes);
			}
			
			mDerivedSensors = (long)0;
			// Check if compatible and not equal to 0xFF
			if((infoMemLayoutCast.idxDerivedSensors0>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors0]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)
					&& (infoMemLayoutCast.idxDerivedSensors1>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors1]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)) { 
				
				mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors0] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors0;
				mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors1] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors1;
				
				// Check if compatible and not equal to 0xFF
				if((infoMemLayoutCast.idxDerivedSensors2>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)){ 
					mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors2] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors2;
				}
			}

			
			// Shimmer Name
			byte[] shimmerNameBuffer = new byte[infoMemLayoutCast.lengthShimmerName];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSDShimmerName, shimmerNameBuffer, 0 , infoMemLayoutCast.lengthShimmerName);
			for(byte b : shimmerNameBuffer) {
				if(!UtilShimmer.isAsciiPrintable((char)b)) {
					break;
				}
				shimmerName += (char)b;
			}
			
			// Experiment Name
			byte[] experimentNameBuffer = new byte[infoMemLayoutCast.lengthExperimentName];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSDEXPIDName, experimentNameBuffer, 0 , infoMemLayoutCast.lengthExperimentName);
			String experimentName = "";
			for(byte b : experimentNameBuffer) {
				if(!UtilShimmer.isAsciiPrintable((char)b)) {
					break;
				}
				experimentName += (char)b;
			}
			mTrialName = new String(experimentName);
	
			//Configuration Time
			int bitShift = (infoMemLayoutCast.lengthConfigTimeBytes-1) * 8;
			mConfigTime = 0;
			for(int x=0; x<infoMemLayoutCast.lengthConfigTimeBytes; x++ ) {
				mConfigTime += (((long)(infoMemBytes[infoMemLayoutCast.idxSDConfigTime0+x] & 0xFF)) << bitShift);
				bitShift -= 8;
			}
	
			
			mSyncWhenLogging = ((infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftTimeSyncWhenLogging) & infoMemLayoutCast.maskTimeSyncWhenLogging)==infoMemLayoutCast.maskTimeSyncWhenLogging? true:false;

			byte[] macIdBytes = new byte[infoMemLayoutCast.lengthMacIdBytes];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxMacAddress, macIdBytes, 0 , infoMemLayoutCast.lengthMacIdBytes);
			mMacIdFromInfoMem = UtilShimmer.bytesToHexString(macIdBytes);

			
			byte[] radioConfig = new byte[infoMemLayoutCast.lengthRadioConfig];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSrRadioConfigStart, radioConfig, 0 , infoMemLayoutCast.lengthRadioConfig);
			setRadioConfig(radioConfig);
			
		}
		
		prepareAllAfterConfigRead();

		//TODO below copied from Shimmer Object -> make single shared class
		// Set name if nothing was read from InfoMem
		if((!shimmerName.isEmpty()) && (!shimmerName.equals("idffff"))){ //Don't allow the default InfoMem contents
			mShimmerUserAssignedName = new String(shimmerName);
		}
		else {
			setShimmerUserAssignedNameWithMac(DEFAULT_SHIMMER_NAME);
//			mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME + "_" + getMacIdFromUartParsed();
		}
	}
	
	//TODO improve flow of below, move to ShimmerDevice also?
	@Override
	public void prepareAllAfterConfigRead() {
		//TODO add in below when ready
//		sensorAndConfigMapsCreate();
		generateSensorAndParserMaps();
		sensorMapUpdateFromEnabledSensorsVars(COMMUNICATION_TYPE.IEEE802154);
		
//		//For debugging
//		for(SensorDetails sensorDetails:mSensorMap.values()){
//			System.out.println("SENSOR\t" + sensorDetails.mSensorDetails.mGuiFriendlyLabel);
//		}
//		for(COMMUNICATION_TYPE commType:mParserMap.keySet()){
//			for(SensorDetails sensorDetails:mParserMap.get(commType).values()){
//				System.out.println("ENABLED SENSOR\tCOMM TYPE:\t" + commType + "\t" + sensorDetails.mSensorDetails.mGuiFriendlyLabel);
//			}
//		}
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		//in future should compare and build map
		if( UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
				SVO_RELEASE_REV_0_1.mHardwareVersion, SVO_RELEASE_REV_0_1.mFirmwareIdentifier, SVO_RELEASE_REV_0_1.mFirmwareVersionMajor, SVO_RELEASE_REV_0_1.mFirmwareVersionMinor, SVO_RELEASE_REV_0_1.mFirmwareVersionInternal)){
//			mMapOfSensors.put(SENSOR_NAMES.CLOCK,new ShimmerClock(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStampGq(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.GSR, new SensorGSR(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.ECG_TO_HR, new SensorECGToHRFw(mShimmerVerObject));
		} 
		else {
			mMapOfSensorClasses.put(SENSORS.CLOCK, new ShimmerClock(this));
			mMapOfSensorClasses.put(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStampGq(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.GSR, new SensorGSR(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.ECG_TO_HR, new SensorECGToHRFw(mShimmerVerObject));
//			mMapOfSensorClasses.put(SENSORS.EXG, new SensorEXG(mShimmerVerObject));
			mMapOfSensorClasses.put(SENSORS.EXG, new SensorEXG(this));
		}
		
		setDefaultShimmerConfiguration();
		generateSensorAndParserMaps();
	}
	
	@Override
	public void setDefaultShimmerConfiguration() {
		mMapOfSensorClasses.get(SENSORS.SYSTEM_TIMESTAMP).setIsEnabledSensor(COMMUNICATION_TYPE.IEEE802154, true);
		mMapOfSensorClasses.get(SENSORS.GSR).setIsEnabledSensor(COMMUNICATION_TYPE.IEEE802154, true);
		mMapOfSensorClasses.get(SENSORS.ECG_TO_HR).setIsEnabledSensor(COMMUNICATION_TYPE.IEEE802154, true);
		
		//TODO Currently SD communication type is not used via this class, GQ relies on the Shimmer3 SD parsing structure (to save devel time). Including here for future work.
//		mMapOfSensorClasses.get(SENSORS.CLOCK).setIsEnabledSensor(COMMUNICATION_TYPE.SD, true);
//		mMapOfSensorClasses.get(SENSORS.EXG).setIsEnabledSensor(COMMUNICATION_TYPE.SD, true, Configuration.Shimmer3.SensorMapKey.HOST_ECG);
//		mMapOfSensorClasses.get(SENSORS.GSR).setIsEnabledSensor(COMMUNICATION_TYPE.SD, true);
//		mMapOfSensorClasses.get(SENSORS.ECG_TO_HR).setIsEnabledSensor(COMMUNICATION_TYPE.SD, true);
	}

	/**
	 * overriding buildMsg here to allow us to store the last received packet.
	 * Used in GQ for GUI generation and to repeat data samples if reception
	 * drops
	 */
	@Override
	public ObjectCluster buildMsg(byte[] packetByteArray, COMMUNICATION_TYPE commType, boolean isTimeSyncEnabled, long pcTimestamp){
		ObjectCluster objectCluster = super.buildMsg(packetByteArray, commType, isTimeSyncEnabled, pcTimestamp);
		super.mLastProcessedObjectCluster = objectCluster;
		return objectCluster;
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		byte[] enabledSensorsByteArray = (byte[]) object;
		long enabledSensors = 0;
		
		for(int i=0;i<enabledSensorsByteArray.length;i++){
			enabledSensors += enabledSensorsByteArray[i] << (i*8);
		}
		
		//TODO MN: change to for loop looping through the mMapOfSensors -> bit index should be copied out of SensorDetails and placed in the AbstractSensor class 
		if (commType == COMMUNICATION_TYPE.IEEE802154){
			if (enabledSensors == 0){
//				mMapOfSensors.get(SENSOR_NAMES.CLOCK).enableSensorChannels(commType);
				mMapOfSensorClasses.get(SENSORS.SYSTEM_TIMESTAMP).setIsEnabledSensor(commType, true);
				mMapOfSensorClasses.get(SENSORS.GSR).setIsEnabledSensor(commType, true);
				mMapOfSensorClasses.get(SENSORS.ECG_TO_HR).setIsEnabledSensor(commType, true);
			} 
			else {
//				//TODO: needs a lot of work
//				for(AbstractSensor sensor:mMapOfSensorClasses.values()){
//					sensor.updateStateFromEnabledSensorsVars(commType, enabledSensors, 0); 
//				}
				//TODO does the below work?
				sensorMapUpdateFromEnabledSensorsVars();
				
//				if ((enabledSensors & SENSOR_GSR_802154_BIT) >0){
//					mMapOfSensors.get(SENSORS.GSR).enableSensorChannels(commType);
//				} 
//				else {
//					mMapOfSensors.get(SENSORS.GSR).disableSensorChannels(commType);
//				}
//				
//				if ((enabledSensors & SENSOR_ECG_HEARTRATE_802154_BIT) >0){
//					mMapOfSensors.get(SENSORS.ECG_TO_HR).enableSensorChannels(commType);
//				} 
//				else {
//					mMapOfSensors.get(SENSORS.ECG_TO_HR).disableSensorChannels(commType);
//				}
//				
//				if ((enabledSensors & SENSOR_CLOCK_802154_BIT) >0){
//					mMapOfSensors.get(SENSORS.CLOCK).enableSensorChannels(commType);
//				} 
//				else {
//					mMapOfSensors.get(SENSORS.CLOCK).disableSensorChannels(commType);
//				}
			}
		}
		
	}

	/**
	 * @return the MAC address from any source available
	 */
	@Override
	public String getMacId() {
		return mMacIdFromUart; 
	}

	@Override
	public void parseUartConfigResponse(UartComponentPropertyDetails cPD, byte[] response){
		// Parse response string
		if(cPD==UART_COMPONENT_PROPERTY.RADIO_802154.SETTINGS){
			setRadioConfig(response);
		}
		else {
			super.parseUartConfigResponse(cPD, response);
		}
	}
	
	@Override
	public byte[] generateUartConfigMessage(UartComponentPropertyDetails cPD){
		
//		System.out.println("Component:" + cPD.component + " Property:" + cPD.property + " ByteArray:" + cPD.byteArray.length);
		
		if(cPD==UART_COMPONENT_PROPERTY.RADIO_802154.SETTINGS){
			return getRadioConfigByteArray();
		}
		else {
			return super.generateUartConfigMessage(cPD);
		}			

	}
	
	public boolean hasSameConfiguration(ShimmerGQ_802154 shimmerGQToCompareWith){

		if(shimmerGQToCompareWith.mRadioDeviceId == this.mRadioDeviceId
			&& shimmerGQToCompareWith.mRadioChannel == this.mRadioChannel
			&& shimmerGQToCompareWith.mRadioGroupId == this.mRadioGroupId
			&& shimmerGQToCompareWith.getTrialName().equals(this.getTrialName())
			&& shimmerGQToCompareWith.getConfigTime() == this.getConfigTime()
			&& shimmerGQToCompareWith.getShimmerUserAssignedName().equals(this.getShimmerUserAssignedName())
			){
			return true;
		}
		else{
			return false;
		}
	}

	public void pairToSpan(String spanId) {
		setIsConnected(true);
		mSpanId = spanId;
		addCommunicationRoute(COMMUNICATION_TYPE.IEEE802154);
	}

	public void unpairFromSpan() {
		setIsConnected(false);
		mSpanId = "";
	}
	
	public String getSpanIDAndRadioDeviceID(){
		return mSpanId + "." + mRadioDeviceId;
	}
	
	@Override
	public void createInfoMemLayout() {
		mInfoMemLayout = new InfoMemLayoutShimmerGq802154(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
	}

	
	public List<Double> getShimmerConfigToInsertInDB(){
		
		List<Double> mConfigValues = new ArrayList<Double>();
		//0-1 Byte = Sampling Rate
		mConfigValues.add(getSamplingRateShimmer(COMMUNICATION_TYPE.IEEE802154));
		
		//3-7 Byte = Sensors
		mConfigValues.add((double) mEnabledSensors);
		//40-71 Byte = Derived Sensors
		mConfigValues.add((double) mDerivedSensors);
		
		//The Configuration byte index 8 - 19
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		AbstractSensor sensorGsr = mMapOfSensorClasses.get(SENSORS.GSR); 
		if(sensorGsr!=null){
			mConfigValues.add((double) ((SensorGSR)sensorGsr).mGSRRange);
		}
		else {
			mConfigValues.add((double) 0);
		}
		mConfigValues.add((double) mInternalExpPower);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
//		mConfigValues[25] = mSyncWhenLogging; This is already inserted in the Trial table
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
//		mConfigValues[29] = mBroadcastInterval; This is already inserted in the Trial table
		
		//Firmware and Shimmer Parameters
		mConfigValues.add((double) getHardwareVersion());
//		mConfigValues[29] = mMyTrialID; 
//		mConfigValues[30] = mNShimmer; This is already insrted in the Trial table
		mConfigValues.add((double) getFirmwareIdentifier());
		mConfigValues.add((double) getFirmwareVersionMajor());
		mConfigValues.add((double) getFirmwareVersionMinor());
		mConfigValues.add((double) getFirmwareVersionInternal());
		
		//Configuration Time
		mConfigValues.add((double) getConfigTime());
		
		//RTC Difference
		mConfigValues.add((double) 0);
		
		//EXG Configuration
		SensorEXG exgSensonr = (SensorEXG) mMapOfSensorClasses.get(SENSORS.EXG);
		byte[] exg1Array = exgSensonr.getEXG1RegisterArray();
		byte[] exg2Array = exgSensonr.getEXG2RegisterArray();
		
		for(int i=0; i<exg1Array.length; i++){
			mConfigValues.add((double) exg1Array[i]);
		}
		
		for(int i=0; i<exg2Array.length; i++){
			mConfigValues.add((double) exg2Array[i]);
		}
		

		//Digital Accel Calibration Configuration
		double[][] mOffsetVectorWRAccel = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mSensitivityMatrixWRAccel = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mAlignmentMatrixWRAccel = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(mOffsetVectorWRAccel[0][0]);
		mConfigValues.add(mOffsetVectorWRAccel[1][0]);
		mConfigValues.add(mOffsetVectorWRAccel[2][0]);
		mConfigValues.add(mSensitivityMatrixWRAccel[0][0]);
		mConfigValues.add(mSensitivityMatrixWRAccel[1][1]);
		mConfigValues.add(mSensitivityMatrixWRAccel[2][2]);
		mConfigValues.add(mAlignmentMatrixWRAccel[0][0]);
		mConfigValues.add(mAlignmentMatrixWRAccel[0][1]);
		mConfigValues.add(mAlignmentMatrixWRAccel[0][2]);
		mConfigValues.add(mAlignmentMatrixWRAccel[1][0]);
		mConfigValues.add(mAlignmentMatrixWRAccel[1][1]);
		mConfigValues.add(mAlignmentMatrixWRAccel[1][2]);
		mConfigValues.add(mAlignmentMatrixWRAccel[2][0]);
		mConfigValues.add(mAlignmentMatrixWRAccel[2][1]);
		mConfigValues.add(mAlignmentMatrixWRAccel[2][2]);

		//Gyroscope Calibration Configuration
		double[][] mOffsetVectorGyroscope = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mSensitivityMatrixGyroscope = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mAlignmentMatrixGyroscope = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(mOffsetVectorGyroscope[0][0]);
		mConfigValues.add(mOffsetVectorGyroscope[1][0]);
		mConfigValues.add(mOffsetVectorGyroscope[2][0]);
		mConfigValues.add(mSensitivityMatrixGyroscope[0][0]);
		mConfigValues.add(mSensitivityMatrixGyroscope[1][1]);
		mConfigValues.add(mSensitivityMatrixGyroscope[2][2]);
		mConfigValues.add(mAlignmentMatrixGyroscope[0][0]);
		mConfigValues.add(mAlignmentMatrixGyroscope[0][1]);
		mConfigValues.add(mAlignmentMatrixGyroscope[0][2]);
		mConfigValues.add(mAlignmentMatrixGyroscope[1][0]);
		mConfigValues.add(mAlignmentMatrixGyroscope[1][1]);
		mConfigValues.add(mAlignmentMatrixGyroscope[1][2]);
		mConfigValues.add(mAlignmentMatrixGyroscope[2][0]);
		mConfigValues.add(mAlignmentMatrixGyroscope[2][1]);
		mConfigValues.add(mAlignmentMatrixGyroscope[2][2]);
		
		//Magnetometer Calibration Configuration
		double[][] mOffsetVectorMagnetometer = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mSensitivityMatrixMagnetometer = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mAlignmentMatrixMagnetometer = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(mOffsetVectorMagnetometer[0][0]);
		mConfigValues.add(mOffsetVectorMagnetometer[1][0]);
		mConfigValues.add(mOffsetVectorMagnetometer[2][0]);
		mConfigValues.add(mSensitivityMatrixMagnetometer[0][0]);
		mConfigValues.add(mSensitivityMatrixMagnetometer[1][1]);
		mConfigValues.add(mSensitivityMatrixMagnetometer[2][2]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[0][0]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[0][1]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[0][2]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[1][0]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[1][1]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[1][2]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[2][0]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[2][1]);
		mConfigValues.add(mAlignmentMatrixMagnetometer[2][2]);

		//Analog Accel Calibration Configuration
		double[][] mOffsetVectorAnalogAccel = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mSensitivityMatrixAnalogAccel = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] mAlignmentMatrixAnalogAccel = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(mOffsetVectorAnalogAccel[0][0]);
		mConfigValues.add(mOffsetVectorAnalogAccel[1][0]);
		mConfigValues.add(mOffsetVectorAnalogAccel[2][0]);
		mConfigValues.add(mSensitivityMatrixAnalogAccel[0][0]);
		mConfigValues.add(mSensitivityMatrixAnalogAccel[1][1]);
		mConfigValues.add(mSensitivityMatrixAnalogAccel[2][2]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[0][0]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[0][1]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[0][2]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[1][0]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[1][1]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[1][2]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[2][0]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[2][1]);
		mConfigValues.add(mAlignmentMatrixAnalogAccel[2][2]);
		
		//PRESSURE (BMP180) CAL PARAMS
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);
		mConfigValues.add((double) 0);

		//MPL Accel Calibration Configuration
		double[][] OffsetVectorMPLAccel = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] SensitivityMatrixMPLAccel = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] AlignmentMatrixMPLAccel = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(OffsetVectorMPLAccel[0][0]);
		mConfigValues.add(OffsetVectorMPLAccel[1][0]);
		mConfigValues.add(OffsetVectorMPLAccel[2][0]);
		mConfigValues.add(SensitivityMatrixMPLAccel[0][0]);
		mConfigValues.add(SensitivityMatrixMPLAccel[1][1]);
		mConfigValues.add(SensitivityMatrixMPLAccel[2][2]);
		mConfigValues.add(AlignmentMatrixMPLAccel[0][0]);
		mConfigValues.add(AlignmentMatrixMPLAccel[0][1]);
		mConfigValues.add(AlignmentMatrixMPLAccel[0][2]);
		mConfigValues.add(AlignmentMatrixMPLAccel[1][0]);
		mConfigValues.add(AlignmentMatrixMPLAccel[1][1]);
		mConfigValues.add(AlignmentMatrixMPLAccel[1][2]);
		mConfigValues.add(AlignmentMatrixMPLAccel[2][0]);
		mConfigValues.add(AlignmentMatrixMPLAccel[2][1]);
		mConfigValues.add(AlignmentMatrixMPLAccel[2][2]);

		//MPL Mag Calibration Configuration
		double[][] OffsetVectorMPLMag = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] SensitivityMatrixMPLMag = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] AlignmentMatrixMPLMag = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(OffsetVectorMPLMag[0][0]);
		mConfigValues.add(OffsetVectorMPLMag[1][0]);
		mConfigValues.add(OffsetVectorMPLMag[2][0]);
		mConfigValues.add(SensitivityMatrixMPLMag[0][0]);
		mConfigValues.add(SensitivityMatrixMPLMag[1][1]);
		mConfigValues.add(SensitivityMatrixMPLMag[2][2]);
		mConfigValues.add(AlignmentMatrixMPLMag[0][0]);
		mConfigValues.add(AlignmentMatrixMPLMag[0][1]);
		mConfigValues.add(AlignmentMatrixMPLMag[0][2]);
		mConfigValues.add(AlignmentMatrixMPLMag[1][0]);
		mConfigValues.add(AlignmentMatrixMPLMag[1][1]);
		mConfigValues.add(AlignmentMatrixMPLMag[1][2]);
		mConfigValues.add(AlignmentMatrixMPLMag[2][0]);
		mConfigValues.add(AlignmentMatrixMPLMag[2][1]);
		mConfigValues.add(AlignmentMatrixMPLMag[2][2]);
		
		//MPL Gyro Calibration Configuration
		double[][] OffsetVectorMPLGyro = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] SensitivityMatrixMPLGyro = {{0,0,0},{0,0,0},{0,0,0}};
		double[][] AlignmentMatrixMPLGyro = {{0,0,0},{0,0,0},{0,0,0}};
		mConfigValues.add(OffsetVectorMPLGyro[0][0]);
		mConfigValues.add(OffsetVectorMPLGyro[1][0]);
		mConfigValues.add(OffsetVectorMPLGyro[2][0]);
		mConfigValues.add(SensitivityMatrixMPLGyro[0][0]);
		mConfigValues.add(SensitivityMatrixMPLGyro[1][1]);
		mConfigValues.add(SensitivityMatrixMPLGyro[2][2]);
		mConfigValues.add(AlignmentMatrixMPLGyro[0][0]);
		mConfigValues.add(AlignmentMatrixMPLGyro[0][1]);
		mConfigValues.add(AlignmentMatrixMPLGyro[0][2]);
		mConfigValues.add(AlignmentMatrixMPLGyro[1][0]);
		mConfigValues.add(AlignmentMatrixMPLGyro[1][1]);
		mConfigValues.add(AlignmentMatrixMPLGyro[1][2]);
		mConfigValues.add(AlignmentMatrixMPLGyro[2][0]);
		mConfigValues.add(AlignmentMatrixMPLGyro[2][1]);
		mConfigValues.add(AlignmentMatrixMPLGyro[2][2]);
		
		//Initial TimeStamp
		mConfigValues.add((double) 0);

		//Expansion board
		mConfigValues.add((double) getExpansionBoardId());
		mConfigValues.add((double) getExpansionBoardRev());
		mConfigValues.add((double) getExpansionBoardRevSpecial());
		
		return mConfigValues;
	}

	public boolean isSDError() {
		return mIsSDError;
	}

	public void setSDError(boolean state) {
		mIsSDError = state;
	}

	public void setConfigFromSpan(int radioChannel, 
			int radioGroupId,
			int radioAddress, 
			int radioResponseWindow, 
			String trialName,
			long trialConfigTime, 
			double samplingFreq) {
		
		//Radio Config
		setRadioConfig(radioChannel, radioGroupId, radioAddress, radioResponseWindow);
		//Trial Config
		setTrialConfig(trialName, trialConfigTime);
		
		setSamplingRateShimmer(COMMUNICATION_TYPE.IEEE802154, samplingFreq);
	}

	/**
	 * @return the mIsFwTestMode
	 */
	public boolean isFwTestMode() {
		return mIsFwTestMode;
	}

	public int getSyncSuccessCount() {
		return this.mSyncSuccessCount ;
	}
	
	/**
	* @param mSyncSuccessCount the mSyncSuccessCount to set
	*/
	public void setSyncSuccessCount(int mSyncSuccessCount) {
		this.mSyncSuccessCount = mSyncSuccessCount;
	}
	
	public void clearSyncSuccessCount(){
		setSyncSuccessCount(0);
	}

	public void incrementSyncSuccessCount(){
		this.mSyncSuccessCount += 1;
		this.mLastSyncSuccessTime = System.currentTimeMillis();
	}

	/**
	 * @return the mLastSyncSuccessTime
	 */
	public long getLastSyncSuccessTime() {
		return mLastSyncSuccessTime;
	}

	/**
	 * @param mLastSyncSuccessTime the mLastSyncSuccessTime to set
	 */
	public void setLastSyncSuccessTime(long mLastSyncSuccessTime) {
		this.mLastSyncSuccessTime = mLastSyncSuccessTime;
	}


}
