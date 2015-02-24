package com.shimmerresearch.pcdriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ProgressDetailsPerSession implements Serializable{
	
	
	public enum OperationState {
		PENDING("Pending"),
		INPROGRESS("In Progress"),
		SUCCESS("Success"),
		FAIL("Fail");
		
//		private int value;
		public String tag;
 
		private OperationState(String tag) {
			this.tag = tag;
//			this.value = value;
		}
	}
	
	//the key is the abs path of the file
	public SerializableTreeMap<String,ProgressDetailsPerFile> mMapOfFilesProgressInfo = new SerializableTreeMap<String,ProgressDetailsPerFile>();

	public OperationState mOperationState = OperationState.PENDING;

	public int mNumberOfFiles = 0;
	public int mProgressCounter = 0;
	public int mNumberOfFails = 0;
	public List<String> mListOfFailedShimmers = new ArrayList<String>();
	public int mProgressPercentageComplete = 0;

	
	public ProgressDetailsPerSession(OperationState operationCurrent) {
		mOperationState = operationCurrent;
	}
	
	
	public void updateProgressTotal() {
		mNumberOfFiles = mMapOfFilesProgressInfo.keySet().size();
	}
	
	public void updateProgressCopy(String uniqueID,boolean operationSuccessful) {
		mProgressCounter += 1;
		mProgressPercentageComplete = ((int)(((double)mProgressCounter/(double)mNumberOfFiles)*100));

		if(!operationSuccessful) {
			mListOfFailedShimmers.add(uniqueID);
			mNumberOfFails = mListOfFailedShimmers.size();
		}
		
		if(mProgressCounter==mNumberOfFiles){
			mOperationState = OperationState.SUCCESS;
			mProgressPercentageComplete=100;
		}
	}
	
	public void updateProgressParse(String uniqueID, boolean operationSuccessful){
		
		//calculate the progress here
		
		if(!operationSuccessful) {
			mListOfFailedShimmers.add(uniqueID);
			mNumberOfFails = mListOfFailedShimmers.size();
		}
	}
	
	public void updateProgressSynchronization(String uniqueID, boolean operationSuccessful){
		
		//calculate the progress here
		
		if(!operationSuccessful) {
			mListOfFailedShimmers.add(uniqueID);
			mNumberOfFails = mListOfFailedShimmers.size();
		}
	}
	
	
	/**Performs a deep copy of ProgressDetailsAll by Serializing
	 * @return ProgressDetailsAll the deep copy of the current ProgressDetailsAll
	 * @see java.io.Serializable
	 */
	public ProgressDetailsPerSession deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ProgressDetailsPerSession) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
