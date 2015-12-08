package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.algorithms.AlgorithmDetailsNew;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensor.AbstractSensor;
import com.shimmerresearch.sensor.AbstractSensor.SENSORS;
import com.shimmerresearch.sensor.SensorSystemTimeStamp;
import com.shimmerresearch.sensor.ShimmerClock;
import com.shimmerresearch.sensor.ShimmerECGToHRSensor;
import com.shimmerresearch.sensor.ShimmerGSRSensor;
import com.shimmerresearch.uartViaDock.ComponentPropertyDetails;
import com.shimmerresearch.uartViaDock.UartPacketDetails.COMPONENT_PROPERTY;

public class ShimmerGQ_802154 extends ShimmerDevice implements Serializable {
	
	//Should be moved to Shimmer Device Eventually, or to an abstract class extending it if this conflicts with shimmerobject
//	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>> mMapOfChannels = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer,ChannelDetails>>(); 
//	public HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>> mMapofComtoSensorMaps = new HashMap<COMMUNICATION_TYPE,LinkedHashMap<Integer, SensorEnabledDetails>>();
	
	
	//JC: TEMP to test sensor enabled
	public int SENSOR_GSR_802154_BIT = 0x01;
	public int SENSOR_ECG_HEARTRATE_802154_BIT = 0x02;
	public int SENSOR_CLOCK_802154_BIT = 0x04;	
	
	/** This is derived from all the sensors
	 * 
	 */
	public static final int VARIABLE_NOT_SET = -1;
	public int mRadioChannel = VARIABLE_NOT_SET;
	public int mRadioGroupId = VARIABLE_NOT_SET;
	public int mRadioMyAddress = VARIABLE_NOT_SET;
	public int mRadioResponseWindow = VARIABLE_NOT_SET; 

	public String mSpanId = "";
	
	private boolean mVerboseMode = true;
	private String mParentClassName = "ShimmerGQ_802154";

	
	//TODO make global or move to ShimmerDevice
	double mShimmerSamplingRate = 256;	// 256Hz is the default sampling rate
	//TODO generate from Sensor classes
	long mEnabledSensors = Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_24BIT + Configuration.Shimmer3.SensorBitmap.SENSOR_GSR;
	//TODO move to Sensor classes
	int mGSRRange = 4; 					// 4 = Auto
	int mInternalExpPower = 1;			// Enable external power for EXG + GSR

	
	 //just use fillers for now
	public static final ShimmerVerObject SVO_RELEASE_REV_0_1 = new ShimmerVerObject(
			HW_ID.UNKNOWN, 
			FW_ID.UNKNOWN,
			FW_ID.UNKNOWN, 
			FW_ID.UNKNOWN, 
			FW_ID.UNKNOWN,
			FW_ID.UNKNOWN);
	
	
	
	//This maps the channel ID to sensor
	//Map<Integer,AbstractSensor> mMapofSensorChannelToSensor = new HashMap<Integer,AbstractSensor>();
	
	//Action Setting Checker, if cant do pass it to next layer to be handled
	
	//priority of comm type to know whether it is dock and radio connected, if dock connected send uart priority
	
	public enum GQ_STATE{
		IDLE("Idle"),
		STREAMING("Streaming"),
		STREAMING_AND_SDLOGGING("Streaming and SD Logging");
		
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
	
	
	// ----------------- Constructors Start ---------------------------------
	
	/**
	 * @param shimmerVersionObject the FW and HW details of the devices
	 */
	public ShimmerGQ_802154(ShimmerVerObject sVO) {
		super.setShimmerVersionObject(sVO);
	}

	/**
	 * @param uniqueID unique id of the shimmer
	 * @param shimmerVersionObject the FW and HW details of the devices
	 */
	@Deprecated
	public ShimmerGQ_802154(ShimmerGQInitSettings settings){
		mUniqueID = settings.mShimmerGQID;
	}
	
	/** 
	 * @param dockId
	 * @param slotNumber
	 * @param macId 
	 */
	public ShimmerGQ_802154(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE connectionType){
		this(dockId, slotNumber, connectionType);
		setMacIdFromUart(macId);
	}

	public ShimmerGQ_802154(String dockId, int slotNumber, COMMUNICATION_TYPE connectionType) {
		setDockInfo(dockId, slotNumber);
		addCommunicationRoute(connectionType);
	}

	// ----------------- Constructors End ---------------------------------

	
	// ----------------- Local Sets/Gets Start ----------------------------

	public void setRadioConfig(byte[] radioConfigArray) {
		if(radioConfigArray.length>=7){
	        this.mRadioChannel = radioConfigArray[0] & 0x00FF;
	        
	        //All MSB first
	        this.mRadioGroupId = (((radioConfigArray[1]&0x00FF)<<8) + (radioConfigArray[2]&0x00FF)) & 0x00FFFF;
	        this.mRadioMyAddress = (((radioConfigArray[3]&0x00FF)<<8) + (radioConfigArray[4]&0x00FF)) & 0x00FFFF;
	        this.mRadioResponseWindow  = (((radioConfigArray[5]&0x00FF)<<8) + (radioConfigArray[6]&0x00FF)) & 0x00FFFF;
		}
	}
	
	public void setRadioConfig(int radioChannel, int radioGroupId, int radioAddr, int radioResponseWindow) {
        this.mRadioChannel = radioChannel;
        this.mRadioGroupId = radioGroupId;
        this.mRadioMyAddress = radioAddr;
        this.mRadioResponseWindow = radioResponseWindow;
	}
	
	public byte[] getRadioConfigByteArray() {
		byte[] radioConfigArray = new byte[7];
		
        radioConfigArray[0] = (byte)((mRadioChannel >> 0) & 0x00FF);
        
        //All MSB first
        radioConfigArray[1] = (byte)((mRadioGroupId >> 8) & 0x00FF);
        radioConfigArray[2] = (byte)((mRadioGroupId >> 0) & 0x00FF);
        radioConfigArray[3] = (byte)((mRadioMyAddress >> 8) & 0x00FF);
        radioConfigArray[4] = (byte)((mRadioMyAddress >> 0) & 0x00FF);
        radioConfigArray[5] = (byte)((mRadioResponseWindow >> 8) & 0x00FF);
        radioConfigArray[6] = (byte)((mRadioResponseWindow >> 0) & 0x00FF);
        
		return radioConfigArray;
	}

	/**
	 * @param statusByte
	 */
	public void parseStatusByte(byte statusByte){
//		Boolean savedDockedState = mIsDocked;
		
		mIsDocked = ((statusByte & 0x01) > 0)? true:false;
		mIsSensing = ((statusByte & 0x02) > 0)? true:false;
//		reserved = ((statusByte & 0x03) > 0)? true:false;
		mIsSDLogging = ((statusByte & 0x08) > 0)? true:false;
		mIsStreaming = ((statusByte & 0x10) > 0)? true:false; 

		consolePrintLn("Status Response = " + UtilShimmer.byteToHexStringFormatted(statusByte)
				+ "\t" + "IsDocked = " + mIsDocked
				+ "\t" + "IsSensing = " + mIsSensing
				+ "\t" + "IsSDLogging = "+ mIsSDLogging
				+ "\t" + "IsStreaming = " + mIsStreaming
				);
		
//		if(savedDockedState!=mIsDocked){
//			dockedStateChange();
//		}
		
	}
	
	public double calculatePacketLoss(long timeDifference, double samplingRate){
		Long numberofExpectedPackets = (long) ((double)((timeDifference/1000)*samplingRate));
		mPacketReceptionRateCurrent = (double)((mPacketReceivedCount)/(double)numberofExpectedPackets)*100;
		return mPacketReceptionRateCurrent;
	}
	
	@Override
	protected void createInfoMemLayout(){
		mInfoMemLayout = new InfoMemLayoutShimmerGq802154(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
	}

	// ----------------- Local Sets/Gets End ----------------------------


	// ----------------- Overrides from ShimmerDevice start -------------

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
	}

	/**Performs a deep copy of ShimmerGQ by Serializing
	 * @return ShimmerGQ the deep copy of the current ShimmerGQ
	 * @see java.io.Serializable
	 */
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
	protected void checkBattery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorConfigOptionDetails> getConfigOptionsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object configValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultShimmerConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] refreshShimmerInfoMemBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> sensorMapConflictCheck(Integer key) {
		// TODO Auto-generated method stub
		return null;
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
		
		// Sampling Rate
		int samplingRate = (int)(32768 / mShimmerSamplingRate);
		mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate] = (byte) (samplingRate & infoMemLayout.maskShimmerSamplingRate); 
		mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8) & infoMemLayout.maskShimmerSamplingRate); 

		// Sensors
//		refreshEnabledSensorsFromSensorMap();
		mInfoMemBytes[infoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors0) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors1) & infoMemLayout.maskSensors);
		mInfoMemBytes[infoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors2) & infoMemLayout.maskSensors);

		// Configuration
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mGSRRange & infoMemLayout.maskGSRRange) << infoMemLayout.bitShiftGSRRange);
		
		mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & infoMemLayout.maskEXPPowerEnable) << infoMemLayout.bitShiftEXPPowerEnable);

		//EXG Configuration
//		exgBytesGetFromConfig(); //update mEXG1Register and mEXG2Register
//		System.arraycopy(mEXG1RegisterArray, 0, mInfoMemBytes, infoMemLayout.idxEXGADS1292RChip1Config1, 10);

		
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

		
		//802.15.4 Radio config
		byte[] radioConfig = getRadioConfigByteArray();
		System.arraycopy(radioConfig, 0, mInfoMemBytes, infoMemLayout.idxSrRadioConfigStart, radioConfig.length);

		return mInfoMemBytes;
	}

	@Override
	public void infoMemByteArrayParse(byte[] infoMemContents) {
		mInfoMemBytes = infoMemContents;
		
		InfoMemLayoutShimmerGq802154 infoMemLayoutCast = (InfoMemLayoutShimmerGq802154) mInfoMemLayout;
		
		// Shimmer Name
		String shimmerName = "";
		byte[] shimmerNameBuffer = new byte[infoMemLayoutCast.lengthShimmerName];
		System.arraycopy(infoMemContents, infoMemLayoutCast.idxSDShimmerName, shimmerNameBuffer, 0 , infoMemLayoutCast.lengthShimmerName);
		for(byte b : shimmerNameBuffer) {
			if(!UtilShimmer.isAsciiPrintable((char)b)) {
				break;
			}
			shimmerName += (char)b;
		}
		
		// Experiment Name
		byte[] experimentNameBuffer = new byte[infoMemLayoutCast.lengthExperimentName];
		System.arraycopy(infoMemContents, infoMemLayoutCast.idxSDEXPIDName, experimentNameBuffer, 0 , infoMemLayoutCast.lengthExperimentName);
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
			mConfigTime += (((long)(infoMemContents[infoMemLayoutCast.idxSDConfigTime0+x] & 0xFF)) << bitShift);
			bitShift -= 8;
		}

		byte[] radioConfig = new byte[infoMemLayoutCast.lengthRadioConfig];
		System.arraycopy(infoMemContents, infoMemLayoutCast.idxSrRadioConfigStart, radioConfig, 0 , infoMemLayoutCast.lengthRadioConfig);
		setRadioConfig(radioConfig);
		
		
		//TODO below copied from Shimmer Object -> make single shared class
		// Set name if nothing was read from InfoMem
		if(!shimmerName.isEmpty()) {
			mShimmerUserAssignedName = new String(shimmerName);
		}
		else {
			mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME + "_" + getMacIdFromUartParsed();
		}

	}
	
	@Override
	public void parseUartConfigResponse(ComponentPropertyDetails cPD, byte[] response){
		// Parse response string
		if(cPD==COMPONENT_PROPERTY.RADIO_802154.SETTINGS){
			setRadioConfig(response);
		}
		else {
			super.parseUartConfigResponse(cPD, response);
		}
	}
	
	@Override
	public byte[] generateUartConfigMessage(ComponentPropertyDetails cPD){
		
//		System.out.println("Component:" + cPD.component + " Property:" + cPD.property + " ByteArray:" + cPD.byteArray.length);
		
		if(cPD==COMPONENT_PROPERTY.RADIO_802154.SETTINGS){
			return getRadioConfigByteArray();
		}
		else {
			return super.generateUartConfigMessage(cPD);
		}			

	}

	@Override
	public void sensorAndConfigMapsCreate() {
		
		//in future should compare and build map
		if( UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
				SVO_RELEASE_REV_0_1.mHardwareVersion, SVO_RELEASE_REV_0_1.mFirmwareIdentifier, SVO_RELEASE_REV_0_1.mFirmwareVersionMajor, SVO_RELEASE_REV_0_1.mFirmwareVersionMinor, SVO_RELEASE_REV_0_1.mFirmwareVersionInternal)){
//			mMapOfSensors.put(SENSOR_NAMES.CLOCK,new ShimmerClock(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.SYSTEM_TIMESTAMP.ordinal(),new SensorSystemTimeStamp(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.GSR.ordinal(),new ShimmerGSRSensor(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.ECG_TO_HR.ordinal(),new ShimmerECGToHRSensor(mShimmerVerObject));
			
		} else {
//			mMapOfSensors.put(SENSOR_NAMES.CLOCK,new ShimmerClock(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.SYSTEM_TIMESTAMP.ordinal(),new SensorSystemTimeStamp(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.GSR.ordinal(),new ShimmerGSRSensor(mShimmerVerObject));
			mMapOfSensors.put(SENSORS.ECG_TO_HR.ordinal(),new ShimmerECGToHRSensor(mShimmerVerObject));
		}
		
	}

	
	@Override
	public Object buildMsg(byte[] packetByteArray,COMMUNICATION_TYPE commType){
//		//if the packet byte has a starting byte indicating enabled channels
//		interpretDataPacketFormat(packetByteArray[0],commType);
//		byte[] newPBA = new byte[packetByteArray.length-1];
//		System.arraycopy(packetByteArray, 1, newPBA, 0, newPBA.length);
//		
//		ObjectCluster ojc = (ObjectCluster) super.buildMsg(newPBA, commType);
//		//sendCallBackMsg();
		
		
		ObjectCluster ojc = (ObjectCluster) super.buildMsg(packetByteArray, commType);

		return ojc;
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
//		byte enabledSensors = (byte) object;
		byte[] enabledSensorsByteArray = (byte[]) object;
		long enabledSensors = 0;
		
		for(int i=0;i<enabledSensorsByteArray.length;i++){
			enabledSensors += enabledSensorsByteArray[i] << (i*8);
		}
		
		//TODO MN: change to for loop looping through the mMapOfSensors -> bit index should be copied out of SensorDetails and placed in the AbstractSensor class 
		if (commType == COMMUNICATION_TYPE.IEEE802154){
			if (enabledSensors == 0){
//				mMapOfSensors.get(SENSOR_NAMES.CLOCK).enableSensorChannels(commType);
				mMapOfSensors.get(SENSORS.SYSTEM_TIMESTAMP.ordinal()).enableSensorChannels(commType);
				mMapOfSensors.get(SENSORS.GSR.ordinal()).enableSensorChannels(commType);
				mMapOfSensors.get(SENSORS.ECG_TO_HR.ordinal()).enableSensorChannels(commType);
			} else {
				if ((enabledSensors & SENSOR_GSR_802154_BIT) >0){
					mMapOfSensors.get(SENSORS.GSR.ordinal()).enableSensorChannels(commType);
				} else {
					mMapOfSensors.get(SENSORS.GSR.ordinal()).disableSensorChannels(commType);
				}
				
				if ((enabledSensors & SENSOR_ECG_HEARTRATE_802154_BIT) >0){
					mMapOfSensors.get(SENSORS.ECG_TO_HR.ordinal()).enableSensorChannels(commType);
				} else {
					mMapOfSensors.get(SENSORS.ECG_TO_HR.ordinal()).disableSensorChannels(commType);
				}
				
				if ((enabledSensors & SENSOR_CLOCK_802154_BIT) >0){
					mMapOfSensors.get(SENSORS.CLOCK.ordinal()).enableSensorChannels(commType);
				} else {
					mMapOfSensors.get(SENSORS.CLOCK.ordinal()).disableSensorChannels(commType);
				}
			}
		}
		
	}

	@Override
	public Map<Integer, SensorEnabledDetails> getSensorEnabledMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}



	private void consolePrintLn(String message) {
		if(mVerboseMode) {
			Calendar rightNow = Calendar.getInstance();
			String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
					+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
			System.out.println(rightNowString + " " + mParentClassName + ": " + getMacId() + " " + message);
		}		
	}

	public void pairToSpan(String spanId) {
		setConnected(true);
		mSpanId = spanId;
	}
	


}
