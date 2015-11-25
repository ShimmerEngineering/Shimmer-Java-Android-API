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
	public int mRadioAddr = VARIABLE_NOT_SET;
	private int mRadioResponseWindow = VARIABLE_NOT_SET; 

	public String mSpanId = "N/A";
	
	
		public static final ShimmerVerObject SVO_RELEASE_REV_0_1 = new ShimmerVerObject( //just use fillers for now
				10, 
				10,
				10, 
				10, 
				10,
				10);
	
	
	
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
	public void infoMemByteArrayParse(byte[] infoMemContents) {
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
	public byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {
		// TODO Auto-generated method stub
		return null;
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

	public void setRadioConfig(byte[] radioConfigArray) {
		if(radioConfigArray.length>=9){
	        this.mRadioChannel = radioConfigArray[0];
	        this.mRadioGroupId = (radioConfigArray[1]<<8) + radioConfigArray[2];
	        this.mRadioAddr = (radioConfigArray[3]<<8) + radioConfigArray[4];
	        this.mRadioResponseWindow  = (radioConfigArray[5]<<24) + (radioConfigArray[6]<<16) + (radioConfigArray[7]<<8) + radioConfigArray[8];
		}
	}
	
	public void setRadioConfig(int radioChannel, int radioGroupId, int radioAddr, int radioResponseWindow) {
        this.mRadioChannel = radioChannel;
        this.mRadioGroupId = radioGroupId;
        this.mRadioAddr = radioAddr;
        this.mRadioResponseWindow = radioResponseWindow;
	}
	
	public byte[] getRadioConfig() {
		byte[] radioConfigArray = new byte[9];
		
        radioConfigArray[0] = (byte)(mRadioChannel & 0xFF);
        radioConfigArray[2] = (byte)((mRadioGroupId & 0xFF00) >> 8);
        radioConfigArray[3] = (byte)(mRadioGroupId & 0xFF);
        radioConfigArray[4] = (byte)((mRadioAddr & 0xFF00) >> 8);
        radioConfigArray[5] = (byte)(mRadioAddr & 0xFF);

        radioConfigArray[5] = (byte)((mRadioResponseWindow >> 24) & 0xFF);
        radioConfigArray[6] = (byte)((mRadioResponseWindow >> 16) & 0xFF);
        radioConfigArray[7] = (byte)((mRadioResponseWindow >> 8) & 0xFF);
        radioConfigArray[8] = (byte)((mRadioResponseWindow >> 0) & 0xFF);
        
		return radioConfigArray;
	}


//	public boolean isConnected() {
//		if(mRadioChannel==ShimmerGQ_802154.VARIABLE_NOT_SET){
//			return false;
//		}
//		else {
//			return true;
//		}
//	}
	
	
	/*
	public Object generateInfoMem(enum fwtype){
		byte[] array = new byte[x];
		for(sensor){
			array = sensor.gimmeInfoMem(array) 
		}
		//determine if you can act on it if not
		//return action setting
 	}
	*/
	
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
			return getRadioConfig();
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

	public Object buildMsg(byte[] packetByteArray,COMMUNICATION_TYPE comType){
		ObjectCluster ojc = (ObjectCluster) super.buildMsg(packetByteArray, comType);
		//sendCallBackMsg();
		return ojc;
	}


}
