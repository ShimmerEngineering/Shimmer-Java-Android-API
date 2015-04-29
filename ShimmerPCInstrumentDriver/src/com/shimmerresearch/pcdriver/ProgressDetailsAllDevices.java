package com.shimmerresearch.pcdriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class ProgressDetailsAllDevices {

	
	public enum Operation {
		NONE("None"),
		PROCESSING("Processing Data"),
		PROCESSED("Data Processed");
		
		private String tag;
 
		private Operation(String tag) {
			this.tag = tag;
		}
	}
	
	//the key is thedevice name + "." + session id
	public SerializableTreeMap<String , ProgressDetailsPerDevice> mMapOfDeviceProgressInfo = new SerializableTreeMap<String, ProgressDetailsPerDevice>();
	public Operation mOperationCurrent = Operation.NONE;
	
	public int mNumberOfFails = 0;
	public int mNumberOfDevices = 0;
	public List<String> mListOfFailedDevices = new ArrayList<String>();
	public int mProgressPercentageComplete = 0;
	
	
	public ProgressDetailsAllDevices() {
		super();
	}

	public void updateProgress(String uniqueID, boolean operationSuccessful) {
		
		double progress=0;
		for(ProgressDetailsPerDevice dpd: mMapOfDeviceProgressInfo.values()){
			progress += dpd.mProgressPercentageComplete/mMapOfDeviceProgressInfo.size();
		}
		mProgressPercentageComplete = (int) progress;
		
		if(!operationSuccessful) {
			mListOfFailedDevices.add(uniqueID);
			mNumberOfFails = mListOfFailedDevices.size();
			if(mNumberOfFails==mMapOfDeviceProgressInfo.size()){
				mProgressPercentageComplete=100;
			}
		}
	}
	
	public boolean updateProgressFinish(){
		if(mProgressPercentageComplete!=100){
			mProgressPercentageComplete=100;
			return true;
		}
		else
			return false;
	}
	
	
	/**Performs a deep copy of ProgressDetailsAll by Serializing
	 * @return ProgressDetailsAll the deep copy of the current ProgressDetailsAll
	 * @see java.io.Serializable
	 */
	public ProgressDetailsAllSessions deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ProgressDetailsAllSessions) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
