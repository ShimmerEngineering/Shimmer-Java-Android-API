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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
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
	
	
	//for shimmersdlog 'support'
	int mNumberofBytes=0;
	protected int mSyncWhenLogging = 0;
	public int mNChannelsSync;
	public int mNChannelsNoSync;
	public int mSampleCount=0;
	public String mAbsoluteFilePath;
	public long mFileSize=0;
	public String mFileName;
	protected long mEstimatedDuration; //in milliSeconds
	FileInputStream fin = null;
	public static final int MAX_NUMBER_OF_SIGNALS = 50; //used to be 11 but now 13 because of the SR30 + 8 for 3d orientation
	protected String[] mSignalNameArray=new String[MAX_NUMBER_OF_SIGNALS];							// 19 is the maximum number of signal thus far
	protected String[] mSignalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];						// 19 is the maximum number of signal thus far
	protected String[] mSignalNameArraySync=new String[MAX_NUMBER_OF_SIGNALS];							// 19 is the maximum number of signal thus far
	protected String[] mSignalDataTypeArraySync=new String[MAX_NUMBER_OF_SIGNALS];						// 19 is the maximum number of signal thus far
	protected String[] mSignalNameArrayNoSync=new String[MAX_NUMBER_OF_SIGNALS];							// 19 is the maximum number of signal thus far
	protected String[] mSignalDataTypeArrayNoSync=new String[MAX_NUMBER_OF_SIGNALS];						// 19 is the maximum number of signal thus far
	protected int mNChannels=0;	                                                // Default number of sensor channels set to three because of the on board accelerometer 
	private long mTrackBytesRead=256; // the header will always be read
	public int OFFSET_LENGTH = 9;
	protected final static int FW_TYPE_BT=0;
	protected final static int FW_TYPE_SD=1;
	int mNumberofSamplesPerBlock=0;
	//
	
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

	/**
	 * @return Null if end of file
	 * @throws Exception
	 */
	public ObjectCluster readPacketMsg() throws Exception{
		
		int fullPacketSize;
		byte newPacket[] = new byte[mNumberofBytes];
		
		byte[] packetContent;
		int timeSync =0; //indicates when there will be an offset value
		if (mSyncWhenLogging==1 && (mSampleCount==0 || mSampleCount==mNumberofSamplesPerBlock)){
			mSignalNameArray = mSignalNameArraySync;
			mSignalDataTypeArray = mSignalDataTypeArraySync;
			mNChannels = mNChannelsSync;
			mSampleCount=0;
			timeSync=1;
			fullPacketSize = mNumberofBytes;
		} else if (mSyncWhenLogging==1){
			timeSync=0;
			fullPacketSize = mNumberofBytes-OFFSET_LENGTH;
			mSignalNameArray = mSignalNameArrayNoSync;
			mSignalDataTypeArray = mSignalDataTypeArrayNoSync;
			mNChannels = mNChannelsNoSync;
		} else {
			fullPacketSize = mNumberofBytes;
		}
		
		
		packetContent = new byte[fullPacketSize];
		
		
		
		
		if (fin.available()>=fullPacketSize){
			fin.read(packetContent);
			mTrackBytesRead = mTrackBytesRead + packetContent.length;
			/*if(mSDTimeSync==1){
				System.arraycopy(packetContent, 5, newPacket, 0, mNumberofBytes);
			} else {
				newPacket = packetContent;	
			}*/
			newPacket = packetContent;	
			mSampleCount++;
			if (timeSync==1){
				interpretDataPacketFormat(timeSync,COMMUNICATION_TYPE.SD);
			} else {
				interpretDataPacketFormat(timeSync,COMMUNICATION_TYPE.SD);
			}
			ObjectCluster ojc = buildMsg(newPacket, COMMUNICATION_TYPE.SD,timeSync, -1);
			//add offset if sync is on and there are no offsets available
			if(mSyncWhenLogging==1 && !ojc.mSensorNames[0].equals("Offset")){
				// add offset to signal name
				String[] sensorNames = ojc.mSensorNames;
				ojc.mSensorNames = new String[sensorNames.length+1];
				System.arraycopy(sensorNames, 0,ojc.mSensorNames, 1, sensorNames.length);
				ojc.mSensorNames[0]="Offset";
				
				// add offset to uncalibrated data
				double[] uncalData = ojc.mUncalData;
				ojc.mUncalData = new double[uncalData.length+1];
				System.arraycopy(uncalData, 0,ojc.mUncalData, 1, uncalData.length);
				ojc.mUncalData[0] = Double.NaN;
				
				// add offset to calibrated data
				double[] calData = ojc.mCalData;
				ojc.mCalData = new double[calData.length+1];
				System.arraycopy(calData, 0,ojc.mCalData, 1, calData.length);
				ojc.mCalData[0] = Double.NaN;
				
				// add offset to uncal unit
				String[] unitUncal = ojc.mUnitUncal;
				ojc.mUnitUncal = new String[unitUncal.length+1];
				System.arraycopy(unitUncal, 0,ojc.mUnitUncal, 1, unitUncal.length);
				ojc.mUnitUncal[0]=CHANNEL_UNITS.NO_UNITS;
				
				// add offset to cal unit
				String[] unitCal = ojc.mUnitCal;
				ojc.mUnitCal = new String[unitCal.length+1];
				System.arraycopy(unitCal, 0,ojc.mUnitCal, 1, unitCal.length);
				ojc.mUnitCal[0]=CHANNEL_UNITS.NO_UNITS;
				
			}
			
			
			return ojc;	
		} else {
			if (fin.available()==0){
				System.out.print("EOF");
			} else {
				throw new Exception("Not enough bytes to form a new packet");
			}
			
		}
		return null;
	}
	
	public void openLog() throws Exception{
		try {
			File file = new File(mAbsoluteFilePath);
			mFileSize = file.length();
			mFileName = file.getName();
			fin = new FileInputStream(file);
			readSDConfigHeader();
			
			double sampleDuration = 1/mShimmerSamplingRate;
			
			int numberofoffsetbytes = 0;
			int numberofbytesperblock = (mNumberofSamplesPerBlock*(mPacketSize));
			long numberOfRows = (mFileSize-numberofoffsetbytes)/(mPacketSize);
			if(mSyncWhenLogging ==1){ //if logging there are additional non packet bytes
				numberofbytesperblock = OFFSET_LENGTH+(mNumberofSamplesPerBlock*(mPacketSize-OFFSET_LENGTH));
				numberofoffsetbytes =  (int)(((double)mFileSize/numberofbytesperblock)*OFFSET_LENGTH);
				numberOfRows = (mFileSize-numberofoffsetbytes)/(mPacketSize-OFFSET_LENGTH);
			}
			
			mEstimatedDuration = (long) (numberOfRows*sampleDuration)*1000;
			
			mInitialized = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw e;
		}


}
