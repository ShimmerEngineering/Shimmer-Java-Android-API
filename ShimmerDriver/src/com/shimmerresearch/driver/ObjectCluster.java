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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

final public class ObjectCluster implements Cloneable,Serializable{
	
	/** * */
	private static final long serialVersionUID = -7601464501144773539L;
	
	private Multimap<String, FormatCluster> mPropertyCluster = HashMultimap.create();
	private String mMyName;
	private String mBluetoothAddress;
	public byte[] mRawData;
	public double[] mUncalData;
	public double[] mCalData;
	public String[] mSensorNames;
	public String[] mUnitCal;
	public String[] mUnitUncal;
	
	private int indexKeeper = 0;
	
	public BT_STATE mState;
	
	String[] mSensorFormats;
	String[] mSensorUnits;
	String[] mSensorIsUsingDefaultCal;
	
	public byte[] mSystemTimeStamp = new byte[8];
	public double mShimmerCalibratedTimeStamp;
	public boolean mIsValidObjectCluster = true;
	
	public enum OBJECTCLUSTER_TYPE{
		ARRAYS,
		FORMAT_CLUSTER,
		PROTOBUF
	}
	public static List<OBJECTCLUSTER_TYPE> mListOfOCTypesEnabled = Arrays.asList(
			OBJECTCLUSTER_TYPE.ARRAYS,
			OBJECTCLUSTER_TYPE.FORMAT_CLUSTER,
			OBJECTCLUSTER_TYPE.PROTOBUF);
	
	public ObjectCluster(){
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
	
	public ObjectCluster(String myName){
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
	
	/**
	 * Takes in a collection of Format Clusters and returns the Format Cluster specified by the string format
	 * @param collectionFormatCluster
	 * @param format 
	 * @return FormatCluster
	 */
	public static FormatCluster returnFormatCluster(Collection<FormatCluster> collectionFormatCluster, String format){
		Iterator<FormatCluster> iFormatCluster=collectionFormatCluster.iterator();
		FormatCluster formatCluster;
		FormatCluster returnFormatCluster = null;

		while(iFormatCluster.hasNext()){
			formatCluster=(FormatCluster)iFormatCluster.next();
			if (formatCluster.mFormat.equals(format)){
				returnFormatCluster=formatCluster;
			}
		}
		return returnFormatCluster;
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
	
	private List<String[]> getListofEnabledSensorSignalsandFormats(){
		List<String[]> listofSignals = new ArrayList<String[]>();
		for (int i=0;i<mSensorNames.length;i++){
			String[] channel = new String[]{mMyName,mSensorNames[i],mSensorFormats[i],mSensorUnits[i],mSensorIsUsingDefaultCal[i]};
			listofSignals.add(channel);
		}
		
		return listofSignals;
	}
	
	public List<String[]> generateArrayOfChannels(){
		//First retrieve all the unique keys from the objectClusterLog
		Multimap<String, FormatCluster> m = mPropertyCluster;

		int size = m.size();
		System.out.print(size);
		mSensorNames=new String[size];
		mSensorFormats=new String[size];
		mSensorUnits=new String[size];
		mSensorIsUsingDefaultCal=new String[size];
		int i=0;
		int p=0;
		for(String key : m.keys()) {
			//first check that there are no repeat entries

			if(compareStringArray(mSensorNames, key) == true) {
				for(FormatCluster formatCluster : m.get(key)) {
					mSensorFormats[p]=formatCluster.mFormat;
					mSensorUnits[p]=formatCluster.mUnits;
					mSensorIsUsingDefaultCal[p]=(formatCluster.mIsUsingDefaultCalibration? "*":"");
					//Log.d("Shimmer",key + " " + mSensorFormats[p] + " " + mSensorUnits[p]);
					p++;
				}

			}	

			mSensorNames[i]=key;
			i++;				 
		}
		return getListofEnabledSensorSignalsandFormats();
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
		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)){
			mUncalData = new double[length];
			mCalData = new double[length];
			mSensorNames = new String[length];
			mUnitCal = new String[length];
			mUnitUncal = new String[length];
		}
	}

	public void addData(ChannelDetails channelDetails, double uncalData, double calData) {
		addData(channelDetails, uncalData, calData, indexKeeper);
	}

	public void addData(ChannelDetails channelDetails, double uncalData, double calData, int index) {
		if(channelDetails.mListOfChannelTypes.contains(CHANNEL_TYPE.UNCAL)){
			addUncalData(channelDetails, uncalData, index);
		}
		if(channelDetails.mListOfChannelTypes.contains(CHANNEL_TYPE.CAL)){
			addCalData(channelDetails, calData, index);
		}
		//TODO decide whether to include the below here
//		incrementIndexKeeper();
	}

	public void addCalData(ChannelDetails channelDetails, double calData) {
		addCalData(channelDetails, calData, indexKeeper);
	}

	public void addCalData(ChannelDetails channelDetails, double calData, int index) {
		addData(channelDetails.mObjectClusterName, CHANNEL_TYPE.CAL, channelDetails.mDefaultCalibratedUnits, calData, index);
	}

	public void addUncalData(ChannelDetails channelDetails, double uncalData) {
		addUncalData(channelDetails, uncalData, indexKeeper);
	}

	@Deprecated
	public void addData(String channelName,String channelType, String units, double data){
		mPropertyCluster.put(channelName,new FormatCluster(channelType,units,data));
		
	}
	
	@Deprecated
	public void addData(String channelName,String channelType, String units, List<Double> data){
		mPropertyCluster.put(channelName,new FormatCluster(channelType,units,data));
		
	}
	
	@Deprecated
	public void addData(String channelName,String channelType, String units, double data,boolean defaultCal){
		mPropertyCluster.put(channelName,new FormatCluster(channelType,units,data,defaultCal));
		
	}
	
	@Deprecated
	public void removeAll(String channelName){
		mPropertyCluster.removeAll(channelName);
	}
	
	public void addUncalData(ChannelDetails channelDetails, double uncalData, int index) {
		addData(channelDetails.mObjectClusterName, CHANNEL_TYPE.UNCAL, channelDetails.mDefaultUnit, uncalData, index);
	}
	
	
	public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data) {
		addData(objectClusterName, channelType, units, data, indexKeeper);
	}

	public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data, int index) {
		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)){
			if(channelType==CHANNEL_TYPE.CAL){
				mCalData[index] = data;
				mUnitCal[index] = units;
			}
			else if(channelType==CHANNEL_TYPE.UNCAL){
				mUncalData[index] = data;
				mUnitUncal[index] = units;
			}
			mSensorNames[index] = objectClusterName;
		}
		
		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.FORMAT_CLUSTER)){
			mPropertyCluster.put(objectClusterName, new FormatCluster(channelType.toString(), units, data));
		}
		
		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.PROTOBUF)){
			//TODO
		}
	}

	public void incrementIndexKeeper(){
		if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)){
			indexKeeper++;
		}
	}

	public int getIndexKeeper() {
		return indexKeeper;
	}

	public void setIndexKeeper(int indexKeeper) {
		this.indexKeeper = indexKeeper;
	}
	
	public Collection<FormatCluster> getCollectionOfFormatClusters(String channelName){
		return mPropertyCluster.get(channelName);
	}

	public Set<String> getKeySet(){
		return mPropertyCluster.keySet();
	}
	
	public Multimap<String, FormatCluster> getPropertyCluster(){
		return mPropertyCluster;
	}
	
	
	
}
