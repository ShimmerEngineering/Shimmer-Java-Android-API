/*Rev 0.3
 * 
 *  Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim
 * @date   October, 2013
 * 
 * Changes since 0.2
 * - SDLog support
 * 
 * Changes since 0.1
 * - Added method to remove a format 
 * 
 */
package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.Builder;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.FormatCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2;
import com.shimmerresearch.sensors.SensorShimmerClock;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

final public class ObjectCluster implements Cloneable,Serializable{
	
	private static final long serialVersionUID = -7601464501144773539L;
	
	//JC: Only temporary for testing this class can be deleted if we decide not to use it in the future
	public ArrayList<SensorData> mSensorDataList = new ArrayList<SensorData>();
	
	public Multimap<String, FormatCluster> mPropertyCluster = HashMultimap.create();
	//TODO implement below to remove the need for the Guava library?
//	private HashMap<String, HashMap<CHANNEL_TYPE, FormatCluster>> mPropertyClusterProposed = new HashMap<String, HashMap<CHANNEL_TYPE, FormatCluster>>();

	/**
	 * Some times it is necessary to get a list of Channels from the
	 * mPropertyCluster in order of insertion which is not possible with the
	 * Multimap approach. This separate list was created to keep a record
	 * of the order of insertion.
	 */
	private List<String> listOfChannelNames = new ArrayList<String>(); 

	private String mMyName;
	private String mBluetoothAddress;
	
	// ------- Old Array approach - Start -----------
	public byte[] mRawData;
	public double[] mUncalData;
	public double[] mCalData;
	/** The SensorNames array is an older approach and has been largely replaced
	 * by the mPropertyCluster. Suggest using getChannelNamesByInsertionOrder()
	 * or getChannelNamesFromKeySet() instead. */
	@Deprecated
	public String[] mSensorNames;
	public String[] mUnitCal;
	public String[] mUnitUncal;
	// ------- Old Array approach - End -----------
	
	// ------- New Array approach -------
	public SensorDataArray sensorDataArray;
//	public SensorDataPerType[] sensorDataArray = new SensorDataPerType[2];
	
	/** mObjectClusterBuilder needs to be uninitialized to avoid crash when connecting on Android */
	private Builder mObjectClusterBuilder; 
	
	private int indexKeeper = 0;
	
	//TODO is mSystemTimeStampBytes actually used by anything?
	private byte[] mSystemTimeStampBytes = new byte[8];
	public double mSystemTimeStamp = 0;
	public double mLSLTimeStamp = 0;
	private double mTimeStampMilliSecs;
	/**
	 * Used in PC software for ShimmerGQ units to indicate if the data packet is
	 * originating from the Shimmer itself or the Span driver (i.e., when no
	 * ShimmerGQ packet has been received)
	 */
	public boolean mIsShimmerObjectCluster = true;
	
	public boolean useList = false;
	
	public int mPacketIdValue = 0;
	
	public enum OBJECTCLUSTER_TYPE{
		ARRAYS_LEGACY,
		FORMAT_CLUSTER,
		PROTOBUF,
		ARRAYS
	}
	/**
	 * Size must be kept equal to the number of values in OBJECTCLUSTER_TYPE. Can be initialised via:
	 * 1) set equal to "OBJECTCLUSTER_TYPE.values()" to enable everything in one line
	 * 2) set equal to "new OBJECTCLUSTER_TYPE[OBJECTCLUSTER_TYPE.values().length]" to disable
	 * everything. 
	 * 3) As below to set defaults
	 */
	public static OBJECTCLUSTER_TYPE[] mOCTypesEnabled = new OBJECTCLUSTER_TYPE[] {
			null,
			OBJECTCLUSTER_TYPE.FORMAT_CLUSTER,
			null,
			null
	};

	/** Used in PC software to determine if a Webcam is paused during playback */
	public BT_STATE mState;

	public static final int CAL_INDEX = CHANNEL_TYPE.CAL.ordinal();
	public static final int UNCAL_INDEX = CHANNEL_TYPE.CAL.ordinal();
	public int mIndexCal = 0;
	public int mIndexUncal = 0;
	public boolean mEnableArraysDataStructure = false;
	
	public class SensorDataPerType {
		
		public SensorDataPerType(int length) {
			mSensorNames = new String[length];
			mUnits = new String[length];
			mData = new double[length];
			mIsUsingDefaultCalibParams = new boolean[length];
		}
		
		public String[] mSensorNames;
		public String[] mUnits;
		public double[] mData;
		public boolean[] mIsUsingDefaultCalibParams;
		
	}
	
	public ObjectCluster(){

	}
	
	public ObjectCluster(String myName){
		this();
		mMyName = myName;
	}

	public ObjectCluster(String myName, String myBlueAdd){
		this(myName);
		mBluetoothAddress=myBlueAdd;
	}

	public ObjectCluster(String myName, String myBlueAdd, BT_STATE state){
		this(myName, myBlueAdd);
		mState = state;
	}
	
	public ObjectCluster(ObjectCluster2 ojc2){
		ojc2.getDataMap().get("");
		for (String channelName:ojc2.getDataMap().keySet()){
			FormatCluster2 fc=ojc2.getDataMap().get(channelName);
			for (String formatName:fc.getFormatMap().keySet()){
				DataCluster2 data = fc.getFormatMap().get(formatName);
				addDataToMap(channelName,formatName,data.getUnit(),data.getData(),data.getDataArrayList());
			}
		}
		mBluetoothAddress = ojc2.getBluetoothAddress();
		mMyName = ojc2.getName();
	}
	
	public String getShimmerName(){
		return mMyName;
	}
	
	public void setShimmerName(String name){
		mMyName = name;
	}
	
	public String getMacAddress(){
		return mBluetoothAddress;
	}
	
	public void setMacAddress(String macAddress){
		mBluetoothAddress = macAddress;
	}
	
	/**
	 * Takes in a collection of Format Clusters and returns the Format Cluster specified by the string format
	 * @param collectionFormatCluster
	 * @param format 
	 * @return FormatCluster
	 */
	public static FormatCluster returnFormatCluster(Collection<FormatCluster> collectionFormatCluster, String format){
		FormatCluster returnFormatCluster = null;

		Iterator<FormatCluster> iFormatCluster = collectionFormatCluster.iterator();
		while(iFormatCluster.hasNext()){
			FormatCluster formatCluster = iFormatCluster.next();
			if (formatCluster.mFormat.equals(format)){
				returnFormatCluster = formatCluster;
			}
		}
		return returnFormatCluster;
	}

	public double getFormatClusterValueDefaultFormat(ChannelDetails channelDetails){
		return getFormatClusterValue(channelDetails.mObjectClusterName, channelDetails.mListOfChannelTypes.get(0).toString());
	}

	public double getFormatClusterValue(ChannelDetails channelDetails, CHANNEL_TYPE channelType){
		return getFormatClusterValue(channelDetails.mObjectClusterName, channelType.toString());
	}
	
//	public double getFormatClusterValue(ChannelDetails channelDetails, String format){
//		return getFormatClusterValue(channelDetails.mObjectClusterName, format);
//	}

	/**
	 * Returns data for the given channel name and format (CAL/UNCAL). Works for both Multimap/FormatCluster & Arrays data structures.
	 * Note: returns Double.NaN if channel name and/or type is not found. Also, using this method with the Arrays may cause a performance impact.
	 * @param channelName
	 * @param format
	 * @return data
	 */
	public double getFormatClusterValue(String channelName, String format){
		if(mEnableArraysDataStructure) {
			int index = getIndexForChannelName(channelName);
			if(index == -1) {	//Index was not found
				return Double.NaN;
			} else {
				if(format.equals(CHANNEL_TYPE.CAL.toString())) {
					return sensorDataArray.mCalData[index];
				} else if(format.equals(CHANNEL_TYPE.UNCAL.toString())) {
					return sensorDataArray.mUncalData[index];
				} else {
					return Double.NaN;	//TODO JOS: Implement support for DERIVED format(?)
				}
			}
		} else {
			FormatCluster formatCluster = getLastFormatCluster(channelName, format);
			if(formatCluster!=null){
				return formatCluster.mData;
			}
			return Double.NaN;
		}
	}
	
	public FormatCluster getLastFormatCluster(String channelName, String format){
		Collection<FormatCluster> formatClusterCollection = getCollectionOfFormatClusters(channelName);
		if(formatClusterCollection != null){
			FormatCluster formatCluster = ObjectCluster.returnFormatCluster(formatClusterCollection, format);
			if(formatCluster!=null){
				return formatCluster;
			}
		}
		return null;
	}

	/**
	 * Users should note that a property has to be removed before it is replaced
	 * @param propertyname Property name you want to delete
	 * @param formatname Format you want to delete
	 */
	public void removePropertyFormat(String propertyname, String formatname){
		Collection<FormatCluster> colFormats = mPropertyCluster.get(propertyname); 
		// first retrieve all the possible formats for the current sensor device
		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(colFormats,formatname)); // retrieve format;
		mPropertyCluster.remove(propertyname, formatCluster);
	}
	
	/**Serializes the object cluster into an array of bytes
	 * @return byte[] an array of bytes
	 * @see java.io.Serializable
	 */
	public byte[] serialize() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<String[]> generateArrayOfChannelsSorted(){
		List<String[]> listofSignals = new ArrayList<String[]>();
		int size=0;
		for (String fckey : getChannelNamesFromKeySet() ) {
			size++;
		}
		
		//arrange the properties
		String[] properties = new String[size];
		int y=0;
		for (String fckey : getChannelNamesFromKeySet() ) {
			properties[y]=fckey;
			y++;
		}
		
		Arrays.sort(properties);
		
		// now need to try arrange the formats
		int index=0;
		String property;
		for (int k=0;k<size;k++){
			property = properties[k];
			Collection<FormatCluster> ofFormatstemp = getCollectionOfFormatClusters(property);
			// the iterator does not have the same order
			int tempSize=0;
			for (FormatCluster fctemp:ofFormatstemp){
				tempSize++;
			}
			
			String[] formats = new String[tempSize];
			String[] units = new String[tempSize];
			int p=0;
			//sort the formats
			for (FormatCluster fctemp:ofFormatstemp){
				formats[p]=fctemp.mFormat;
				p++;
			
			}
			
			Arrays.sort(formats);
			for (int u=0;u<formats.length;u++){
				for (FormatCluster fctemp:ofFormatstemp){
					if (fctemp.mFormat.equals(formats[u])){
						units[u]=fctemp.mUnits;
					}
				}
			}
			
			for (int u=0;u<formats.length;u++){
				String[] channel = {mMyName,property,formats[u],units[u]};
				listofSignals.add(channel);
				//System.out.println(":::" + address + property + fc.mFormat);		
				System.out.println("Index" + index); 
				
			}
			
		
		}
		return listofSignals;
	}
	
	public List<String[]> generateArrayOfChannels(){
		//First retrieve all the unique keys from the objectClusterLog
		Multimap<String, FormatCluster> m = mPropertyCluster;

		int size = m.size();
		System.out.print(size);
		mSensorNames=new String[size];
		String[] sensorFormats=new String[size];
		String[] sensorUnits=new String[size];
		String[] sensorIsUsingDefaultCal=new String[size];
		int i=0;
		int p=0;
		for(String key : m.keys()) {
			//first check that there are no repeat entries

			if(compareStringArray(mSensorNames, key) == true) {
				for(FormatCluster formatCluster : m.get(key)) {
					sensorFormats[p]=formatCluster.mFormat;
					sensorUnits[p]=formatCluster.mUnits;
					sensorIsUsingDefaultCal[p]=(formatCluster.mIsUsingDefaultCalibration? "*":"");
					//Log.d("Shimmer",key + " " + mSensorFormats[p] + " " + mSensorUnits[p]);
					p++;
				}

			}	

			mSensorNames[i]=key;
			i++;				 
		}
		return getListofEnabledSensorSignalsandFormats(mMyName, mSensorNames, sensorFormats, sensorUnits, sensorIsUsingDefaultCal);
	}
	
	private static List<String[]> getListofEnabledSensorSignalsandFormats(String myName, String[] sensorNames, String[] sensorFormats, String[] sensorUnits, String[] sensorIsUsingDefaultCal){
		List<String[]> listofSignals = new ArrayList<String[]>();
		for (int i=0;i<sensorNames.length;i++){
			String[] channel = new String[]{myName,sensorNames[i],sensorFormats[i],sensorUnits[i],sensorIsUsingDefaultCal[i]};
			listofSignals.add(channel);
		}
		
		return listofSignals;
	}
	
	private boolean compareStringArray(String[] stringArray, String string){
		boolean uniqueString=true;
		int size = stringArray.length;
		for (int i=0;i<size;i++){
			if (stringArray[i]==string){
				uniqueString=false;
			}	
					
		}
		return uniqueString;
	}
	
	public void createArrayData(int length){
/*		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS_LEGACY)){
			mUncalData = new double[length];
			mCalData = new double[length];
			mSensorNames = new String[length];
			mUnitCal = new String[length];
			mUnitUncal = new String[length];
		}
		
		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)) {
			sensorDataArray = new SensorDataArray(length);		
//			sensorDataArray[CHANNEL_TYPE.CAL.ordinal()] = new SensorDataPerType(50);
//			sensorDataArray[CHANNEL_TYPE.UNCAL.ordinal()] = new SensorDataPerType(50);
		}
*/		
		if(mEnableArraysDataStructure) {
			sensorDataArray = new SensorDataArray(length);		
		} else {
			mUncalData = new double[length];
			mCalData = new double[length];
			mSensorNames = new String[length];
			mUnitCal = new String[length];
			mUnitUncal = new String[length];
		}
	}

	public void addData(ChannelDetails channelDetails, double uncalData, double calData) {
		addData(channelDetails, uncalData, calData, false);
	}

	public void addData(ChannelDetails channelDetails, double uncalData, double calData, boolean usingDefaultParameters) {
		addData(channelDetails, uncalData, calData, indexKeeper, usingDefaultParameters);
	}

	public void addData(ChannelDetails channelDetails, double uncalData, double calData, int index, boolean usingDefaultParameters) {
		if(channelDetails.mListOfChannelTypes.contains(CHANNEL_TYPE.UNCAL)){
			addUncalData(channelDetails, uncalData, index);
		}
		if(channelDetails.mListOfChannelTypes.contains(CHANNEL_TYPE.CAL)){
			addCalData(channelDetails, calData, index, usingDefaultParameters);
		}
		//TODO decide whether to include the below here
//		incrementIndexKeeper();
	}

	public void addCalData(ChannelDetails channelDetails, double calData) {
		addCalData(channelDetails, calData, indexKeeper);
	}

	public void addCalData(ChannelDetails channelDetails, double calData, int index) {
		addCalData(channelDetails, calData, index, false);
	}

	public void addCalData(ChannelDetails channelDetails, double calData, int index, boolean usingDefaultParameters) {
		addData(channelDetails.mObjectClusterName, CHANNEL_TYPE.CAL, channelDetails.mDefaultCalUnits, calData, index, usingDefaultParameters);
	}

	public void addUncalData(ChannelDetails channelDetails, double uncalData) {
		addUncalData(channelDetails, uncalData, indexKeeper);
	}

	public void addUncalData(ChannelDetails channelDetails, double uncalData, int index) {
		addData(channelDetails.mObjectClusterName, CHANNEL_TYPE.UNCAL, channelDetails.mDefaultUncalUnit, uncalData, index);
	}
	
	public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data) {
		addData(objectClusterName, channelType, units, data, indexKeeper);
	}

	public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data, int index) {
		addData(objectClusterName, channelType, units, data, index, false);
	}
	
	public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data, int index, boolean isUsingDefaultCalib) {
		if(mOCTypesEnabled[OBJECTCLUSTER_TYPE.ARRAYS_LEGACY.ordinal()]!=null){
			if(channelType==CHANNEL_TYPE.CAL){
				mCalData[index] = data;
				mUnitCal[index] = units;
			}
			else if(channelType==CHANNEL_TYPE.UNCAL){
				mUncalData[index] = data;
				mUnitUncal[index] = units;
			}
			//TODO below not really needed, just put in to match some legacy code but can be removed. 
			else if(channelType==CHANNEL_TYPE.DERIVED){
				mCalData[index] = data;
				mUnitCal[index] = units;
				mUncalData[index] = data;
				mUnitUncal[index] = units;
			}
			mSensorNames[index] = objectClusterName;
			
			//TODO implement below here and remove everywhere else in the code
//			incrementIndexKeeper();
			
			
			//TODO JOS: Add new arrays data here
			
//			if(channelType.equals(CHANNEL_TYPE.CAL.toString())) {
//				sensorDataArray.mSensorNames[mIndexCal] = channelName;
//				sensorDataArray.mCalUnits[mIndexCal] = units;
//				sensorDataArray.mCalData[mIndexCal] = data;
//				sensorDataArray.mIsUsingDefaultCalibrationParams[mIndexCal] = isUsingDefaultCalib;
////				sensorDataArray.mCalArraysIndex++;
//				
////				sensorDataArray[CAL_INDEX].mSensorNames[mIndexCal] = channelName;
////				sensorDataArray[CAL_INDEX].mUnits[mIndexCal] = units;
////				sensorDataArray[CAL_INDEX].mData[mIndexCal] = data;
////				sensorDataArray[CAL_INDEX].mIsUsingDefaultCalibParams[mIndexCal] = isUsingDefaultCalib;
//				//mIndexCal++;
//			} else if(channelType.equals(CHANNEL_TYPE.UNCAL.toString())) {
//				sensorDataArray.mSensorNames[mIndexUncal] = channelName;
//				sensorDataArray.mUncalUnits[mIndexUncal] = units;
//				sensorDataArray.mUncalData[mIndexUncal] = data;
////				sensorDataArray.mUncalArraysIndex++;
//				
////				sensorDataArray[UNCAL_INDEX].mSensorNames[mIndexUncal] = channelName;
////				sensorDataArray[UNCAL_INDEX].mUnits[mIndexUncal] = units;
////				sensorDataArray[UNCAL_INDEX].mData[mIndexUncal] = data;
////				sensorDataArray[UNCAL_INDEX].mIsUsingDefaultCalibParams[mIndexUncal] = isUsingDefaultCalib;
//				//mIndexUncal++;
//			}

		}
		
		if(mOCTypesEnabled[OBJECTCLUSTER_TYPE.FORMAT_CLUSTER.ordinal()]!=null){
			addDataToMap(objectClusterName, channelType.toString(), units, data, isUsingDefaultCalib);
		}
		
		if(mOCTypesEnabled[OBJECTCLUSTER_TYPE.PROTOBUF.ordinal()]!=null){
			//TODO
		}
	}

	public void incrementIndexKeeper(){
		if(mOCTypesEnabled[OBJECTCLUSTER_TYPE.ARRAYS_LEGACY.ordinal()]!=null){
			if(indexKeeper<mCalData.length){
				indexKeeper++;
			}
		}
	}

	public int getIndexKeeper() {
		return indexKeeper;
	}

	public void setIndexKeeper(int indexKeeper) {
		this.indexKeeper = indexKeeper;
	}
	
	public void addCalDataToMap(ChannelDetails channelDetails, double data){
		addDataToMap(channelDetails.mObjectClusterName, CHANNEL_TYPE.CAL.toString(), channelDetails.mDefaultCalUnits, data);
	}

	public void addUncalDataToMap(ChannelDetails channelDetails, double data){
		addDataToMap(channelDetails.mObjectClusterName, CHANNEL_TYPE.UNCAL.toString(), channelDetails.mDefaultCalUnits, data);
	}

	public void addDataToMap(String channelName, String channelType, String units, double data){
		addDataToMap(channelName, channelType, units, data, false);
	}

	public void addDataToMap(String channelName, String channelType, String units, double data, boolean isUsingDefaultCalib){
//		if(mOCTypesEnabled[OBJECTCLUSTER_TYPE.ARRAYS.ordinal()]!=null){
		if(mEnableArraysDataStructure) {
			
		} else {
			if(mOCTypesEnabled[OBJECTCLUSTER_TYPE.FORMAT_CLUSTER.ordinal()]!=null){
				mPropertyCluster.put(channelName,new FormatCluster(channelType, units, data, isUsingDefaultCalib));
				addChannelNameToList(channelName);
			}
		}
	}

	@Deprecated
	public void addDataToMap(String channelName, String channelType, String units, List<Double> data){
		mPropertyCluster.put(channelName,new FormatCluster(channelType, units, data));
		addChannelNameToList(channelName);
	}
	
	@Deprecated
	public void addDataToMap(String channelName,String channelType, String units, double data, List<Double> dataArray){
		mPropertyCluster.put(channelName,new FormatCluster(channelType, units, data, dataArray));
		addChannelNameToList(channelName);
	}
	
	private void addChannelNameToList(String channelName) {
		if(!listOfChannelNames.contains(channelName)){
			listOfChannelNames.add(channelName);
		}
	}
	
	@Deprecated
	public void removeAll(String channelName){
		mPropertyCluster.removeAll(channelName);
		listOfChannelNames = new ArrayList<String>();
	}
	
	public Collection<FormatCluster> getCollectionOfFormatClusters(String channelName){
		return mPropertyCluster.get(channelName);
	}

	public Set<String> getChannelNamesFromKeySet(){
		return mPropertyCluster.keySet();
	}

	public List<String> getChannelNamesByInsertionOrder(){
		return listOfChannelNames;
	}

	public Multimap<String, FormatCluster> getPropertyCluster(){
		return mPropertyCluster;
	}
	
	public ObjectCluster2 buildProtoBufMsg(){
		mObjectClusterBuilder = ObjectCluster2.newBuilder();
		for (String channelName:mPropertyCluster.keys()){
			Collection<FormatCluster> fcs = mPropertyCluster.get(channelName);
			FormatCluster2.Builder fcb = FormatCluster2.newBuilder();
			for(FormatCluster fc:fcs){
				DataCluster2.Builder dcb = DataCluster2.newBuilder();
				if (fc.mData!=Double.NaN){
					dcb.setData(fc.mData);	
				}
				if (fc.mDataObject!=null && fc.mDataObject.size()>0){
					dcb.addAllDataArray(fc.mDataObject);
				}
				dcb.setUnit(fc.mUnits);
				fcb.getMutableFormatMap().put(fc.mFormat, dcb.build());
			}
			mObjectClusterBuilder.getMutableDataMap().put(channelName, fcb.build());
		}
		if(mBluetoothAddress!=null)
			mObjectClusterBuilder.setBluetoothAddress(mBluetoothAddress);
		if(mMyName!=null)
			mObjectClusterBuilder.setName(mMyName);
		mObjectClusterBuilder.setCalibratedTimeStamp(mTimeStampMilliSecs);
//		ByteBuffer bb = ByteBuffer.allocate(8);
//    	bb.put(mSystemTimeStampBytes);
//    	bb.flip();
//    	long systemTimeStamp = bb.getLong();
//		mObjectClusterBuilder.setSystemTime(systemTimeStamp);
		mObjectClusterBuilder.setSystemTime((long)mSystemTimeStamp);
		return mObjectClusterBuilder.build();
	}
	
	public double getTimestampMilliSecs() {
		return mTimeStampMilliSecs;
	}

	public void setTimeStampMilliSecs(double timeStampMilliSecs) {
		this.mTimeStampMilliSecs = timeStampMilliSecs;
	}
	
	/**
	 * @return the mListOfOCTypesEnabled
	 */
	public static OBJECTCLUSTER_TYPE[] getOCTypesEnabled() {
		return mOCTypesEnabled;
	}

	/**
	 * @param listOfOCTypesEnabled the mListOfOCTypesEnabled to set
	 */
	public static void setOCTypesEnabled(List<OBJECTCLUSTER_TYPE> listOfOCTypesEnabled) {
		ObjectCluster.mOCTypesEnabled = new OBJECTCLUSTER_TYPE[OBJECTCLUSTER_TYPE.values().length];
		for(OBJECTCLUSTER_TYPE ocType:listOfOCTypesEnabled) {
			ObjectCluster.mOCTypesEnabled[ocType.ordinal()] = ocType;
		}
	}

	public void consolePrintChannelsAndDataSingleLine() {
		System.out.println("ShimmerName:" + mMyName);
		System.out.println("Channels in ObjectCluster:");
		String channelsCal = "Cal:\t";
		String channelsUncal = "Uncal:\t";
		for(String channel:getChannelNamesByInsertionOrder()){
			channelsCal += channel + "=" + getFormatClusterValue(channel, CHANNEL_TYPE.CAL.toString()) + "\t";
			channelsUncal += channel + "=" + getFormatClusterValue(channel, CHANNEL_TYPE.UNCAL.toString()) + "\t";
		}
		System.out.println(channelsCal);
		System.out.println(channelsUncal);
		System.out.println("");
	}

	public void consolePrintChannelsAndDataGrouped() {
		System.out.println("Channels in ObjectCluster:");
		for(String channel:getChannelNamesByInsertionOrder()){
			System.out.println("\t" + channel + ":\t(" + getFormatClusterValue(channel, CHANNEL_TYPE.UNCAL.toString()) + "," + getFormatClusterValue(channel, CHANNEL_TYPE.CAL.toString()) + ")");
		}
		System.out.println("");
	}

	public static ObjectCluster[] generateRandomObjectClusterArray(String deviceName, String signalName, int numSamples, int minValue, int maxValue) {
		Random rand = new Random();
		
		double[] dataArray = new double[numSamples];
		for(int i=0;i<numSamples;i++){
			dataArray[i] = rand.nextInt(maxValue);
		}

//		double[] dataArray = rand.doubles(numSamples, minValue, maxValue);
		
		double timestamp = 0.00001;
		ObjectCluster[] ojcArray = new ObjectCluster[numSamples];
		for(int i=0;i<numSamples;i++){
			ObjectCluster ojc = new ObjectCluster(deviceName);
			ojc.createArrayData(1);
			ojc.addData(signalName, CHANNEL_TYPE.CAL, "", dataArray[i]);
			ojc.addCalData(SensorShimmerClock.channelSystemTimestampPlot, timestamp);
			timestamp+=1;
			ojcArray[i] = ojc;
		}
		
		return ojcArray;
	}

	public ObjectCluster deepClone() {
//		System.out.println("Cloning:" + mUniqueID);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ObjectCluster) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retrieves the index for a particular channel name in the SensorDataArray. This index can then be used to retrieve CAL/UNCAL data from the SensorDataArray
	 * Note: returns -1 if the index is not found.
	 * @param channelName
	 * @return index 
	 */
	public int getIndexForChannelName(String channelName) {
		if(mEnableArraysDataStructure) {
			//TODO JOS: add parser map support here so we don't have to parse the array
			for(int i=0; i<sensorDataArray.mSensorNames.length; i++) {
				if(sensorDataArray.mSensorNames[i] != null) {
					if(sensorDataArray.mSensorNames[i].equals(channelName)) {
						return i;
					}
				}
			}
		} else {
			for(int i=0; i<mSensorNames.length; i++) {
				if(mSensorNames[i] != null) {
					if(mSensorNames[i].equals(channelName)) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public void setSystemTimeStampBytes(byte[] systemTimeStampBytes) {
		mSystemTimeStampBytes = systemTimeStampBytes;
		
		ByteBuffer bb = ByteBuffer.allocate(8);
    	bb.put(mSystemTimeStampBytes);
    	bb.flip();
    	mSystemTimeStamp = bb.getLong();
	}

	public void setSystemTimeStamp(double systemTimeStamp) {
		mSystemTimeStamp = systemTimeStamp;
//		mSystemTimeStampBytes=ByteBuffer.allocate(8).putLong(systemTimeStamp).array();
		mSystemTimeStampBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong((long)systemTimeStamp).array();
	}

	public boolean isValidObjectCluster() {
		return mState==null && mIsShimmerObjectCluster;
	}

	public void setIsShimmerObjectCluster(boolean isShimmerObjectCluster) {
		mIsShimmerObjectCluster = isShimmerObjectCluster;
	}

}
