package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensor.AbstractSensor;
import com.shimmerresearch.sensor.AbstractSensor.SENSOR_NAMES;
import com.shimmerresearch.sensor.ShimmerECGtoHRSensor;
import com.shimmerresearch.sensor.ShimmerGSRSensor;
import com.shimmerresearch.uartViaDock.ComponentPropertyDetails;
import com.shimmerresearch.uartViaDock.UartPacketDetails.COMPONENT_PROPERTY;

public class ShimmerGQ_802154 extends ShimmerDevice implements Serializable {
	
	
	
	/** This is derived from all the sensors
	 * 
	 */
	public HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>> mMapOfChannel = new HashMap<COMMUNICATION_TYPE,HashMap<String,ChannelDetails>>();

	public static final int VARIABLE_NOT_SET = -1;
	public int mRadioChannel = VARIABLE_NOT_SET;
	public int mRadioGroupId = VARIABLE_NOT_SET;
	public int mRadioMyAddress = VARIABLE_NOT_SET;
	public int mRadioResponseWindow = VARIABLE_NOT_SET; 

	public String mSpanId = "N/A";
	
	
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
	
	//

	public enum GQ_STATE{
		IDLE("Idle"),
		STREAMING("Streaming"),
		STREAMING_AND_SDLOGGING("Streaming and SD Logging");
		
	    private final String text;

	    /**
	     * @param text
	     */
	    private GQ_STATE(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
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

	public ShimmerGQ_802154(String dockID, int slotNumber, COMMUNICATION_TYPE connectionType) {
		mDockID = dockID;
		parseDockType();
		
		mSlotNumber = slotNumber;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
		
		addCommunicationRoute(connectionType);
	}

	// ----------------- Constructors End ---------------------------------

	
	// ----------------- Local Sets/Gets Start ----------------------------

	public void setRadioConfig(byte[] radioConfigArray) {
		if(radioConfigArray.length>=7){
	        this.mRadioChannel = radioConfigArray[0] & 0x00FF;
	        
	        //LSB first
	        this.mRadioGroupId = (((radioConfigArray[2]&0x00FF)<<8) + (radioConfigArray[1]&0x00FF)) & 0x00FFFF;
	        
	        //LSB first
	        this.mRadioMyAddress = (((radioConfigArray[4]&0x00FF)<<8) + (radioConfigArray[3]&0x00FF)) & 0x00FFFF;
	        
	        //MSB first
//	        this.mRadioResponseWindow  = (radioConfigArray[5]<<24) + (radioConfigArray[6]<<16) + (radioConfigArray[7]<<8) + radioConfigArray[8];
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
        
        //LSB first
        radioConfigArray[1] = (byte)((mRadioGroupId >> 0) & 0x00FF);
        radioConfigArray[2] = (byte)((mRadioGroupId >> 8) & 0x00FF);
        
        //LSB first
        radioConfigArray[3] = (byte)((mRadioMyAddress >> 0) & 0x00FF);
        radioConfigArray[4] = (byte)((mRadioMyAddress >> 8) & 0x00FF);

        //MSB first
//        radioConfigArray[5] = (byte)((mRadioResponseWindow >> 24) & 0xFF);
//        radioConfigArray[6] = (byte)((mRadioResponseWindow >> 16) & 0xFF);
        radioConfigArray[5] = (byte)((mRadioResponseWindow >> 8) & 0x00FF);
        radioConfigArray[6] = (byte)((mRadioResponseWindow >> 0) & 0x00FF);
        
		return radioConfigArray;
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
	public Map<Integer, SensorEnabledDetails> getSensorMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
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
		mInfoMemBytes = createEmptyInfoMemByteArray(infoMemLayout.calculateInfoMemByteLength());
		
		//TODO: Trial name, shimmer name
		
		//Configuration Time
		mInfoMemBytes[infoMemLayout.idxSDConfigTime0] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime0) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime1] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime1) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime2] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime2) & 0xFF);
		mInfoMemBytes[infoMemLayout.idxSDConfigTime3] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime3) & 0xFF);

		byte[] radioConfig = getRadioConfigByteArray();
		System.arraycopy(radioConfig, 0, mInfoMemBytes, infoMemLayout.idxSrRadioConfigStart, radioConfig.length);

//		mInfoMemBytes[infoMemLayout.idxSrRadioChannel] = (byte) mRadioChannel;
//		mInfoMemBytes[infoMemLayout.idxSrRadioGroupId] = (byte) ((mRadioGroupId >> 8) & 0xFF);
//		mInfoMemBytes[infoMemLayout.idxSrRadioGroupId+1] = (byte) (mRadioGroupId & 0xFF);
//		mInfoMemBytes[infoMemLayout.idxSrRadioMyAddress] = (byte) ((mRadioMyAddress >> 8) & 0xFF);
//		mInfoMemBytes[infoMemLayout.idxSrRadioMyAddress+1] = (byte) (mRadioMyAddress & 0xFF);
//		mInfoMemBytes[infoMemLayout.idxSrRadioResponseWindow] = (byte) ((mRadioResponseWindow >> 8) & 0xFF);
//		mInfoMemBytes[infoMemLayout.idxSrRadioResponseWindow+1] = (byte) (mRadioResponseWindow & 0xFF);
		
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
			
			
		} else {
			mMapOfSensors.put(SENSOR_NAMES.GSR,new ShimmerGSRSensor(mShimmerVerObject));
			mMapOfSensors.put(SENSOR_NAMES.ECG_TO_HR,new ShimmerECGtoHRSensor(mShimmerVerObject));
		}
		
	}

	@Override
	public Object buildMsg(byte[] packetByteArray,COMMUNICATION_TYPE comType){
		ObjectCluster ojc = (ObjectCluster) super.buildMsg(packetByteArray, comType);
		//sendCallBackMsg();
		return ojc;
	}
	
	
	// ----------------- Overrides from ShimmerDevice end -------------




}
