package com.shimmerresearch.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class ShimmerGQ_802154_SD extends ShimmerGQ_802154 {

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
	public ShimmerGQ_802154_SD(ShimmerVerObject sVO) {
		super(sVO);
		// TODO Auto-generated constructor stub
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
			ObjectCluster ojc = super.buildMsg(newPacket, COMMUNICATION_TYPE.SD,timeSync, -1);
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
