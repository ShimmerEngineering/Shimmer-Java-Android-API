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
		COPYING("Copying"),
		SYNC("Sync"),
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
	//this map stores the progress of the sync for each folder/iteration
	//the key is the absolute path of the folder
	public SerializableTreeMap<String, Double> mMapOfFoldersProgressInfo = new SerializableTreeMap<String, Double>();
	
	public OperationState mOperationState = OperationState.PENDING;

	public int mNumberOfFiles = 0;
	public int mNumberOfFolders = 0;
	public int mProgressCounter = 0;
	public int mNumberOfFails = 0;
	public List<String> mListOfFailedFiles = new ArrayList<String>();
	public List<String> mListOfFailedSync = new ArrayList<String>();
	public int mProgressPercentageComplete = 0;

	
	public ProgressDetailsPerSession(OperationState operationCurrent) {
		mOperationState = operationCurrent;
	}
	
	
	public void updateProgressTotal() {
		mNumberOfFiles = mMapOfFilesProgressInfo.keySet().size();
	}
	
	public void updateFoldersTotal() {
		mNumberOfFolders = mMapOfFoldersProgressInfo.keySet().size();
	}
	
	public void updateProgressCopy(String uniqueID,boolean operationSuccessful) {
		mProgressCounter += 1;
		mProgressPercentageComplete = ((int)(((double)mProgressCounter/(double)mNumberOfFiles)*100));

		if(!operationSuccessful) {
			mListOfFailedFiles.add(uniqueID);
			mNumberOfFails = mListOfFailedFiles.size();
		}
		
		if(mProgressCounter==mNumberOfFiles){
			mOperationState = OperationState.SUCCESS;
			mProgressPercentageComplete=100;
		}
	}
	
	public void updateProgressImport(String uniqueID, boolean operationSuccessful){
		
		double progressParse=0;
		for(ProgressDetailsPerFile detailsPerFile: mMapOfFilesProgressInfo.values()){
			progressParse += detailsPerFile.mProgressPercentageComplete;
		}
		
		progressParse = (progressParse*100)/(double) mNumberOfFiles;
		
		double progressSync=0;
		for(Double p: mMapOfFoldersProgressInfo.values()){
			progressSync += p;
		}
		
		progressSync = (progressSync*100)/(double) mNumberOfFolders;
		mProgressPercentageComplete = (int) ((progressParse+progressSync)/2);
		
		if(mProgressPercentageComplete==100){
			mOperationState = OperationState.SUCCESS;
		}
		
		if(!operationSuccessful) {
			if(mMapOfFilesProgressInfo.containsKey(uniqueID)){
				mListOfFailedFiles.add(uniqueID);
				mNumberOfFails = mListOfFailedFiles.size() + mListOfFailedSync.size();
			}
			else if(mMapOfFoldersProgressInfo.containsKey(uniqueID)){
				mListOfFailedSync.add(uniqueID);
				mNumberOfFails = mListOfFailedFiles.size() + mListOfFailedSync.size();
			}
		}
	}
	
//	public void updateProgressParse(String uniqueID, boolean operationSuccessful){
//		
//		//calculate the progress here
////		double fpf = filesPerFolder.get(key);
////		double Px = (percentageDone*100)/fpf; // calculate the percent regarding the total number of files
////		double progress = (progressBarValue + Px) / 2;
//		
//		double progressParse=0;
//		for(ProgressDetailsPerFile detailsPerFile: mMapOfFilesProgressInfo.values()){
//			progressParse += detailsPerFile.mProgressPercentageComplete;
//		}
//		
//		if(!operationSuccessful) {
//			mListOfFailedFiles.add(uniqueID);
//			mNumberOfFails = mListOfFailedFiles.size();
//		}
//	}
//	
//	public void updateProgressSynchronization(String uniqueID, boolean operationSuccessful){
//		
//		//calculate the progress here
//		
//		if(!operationSuccessful) {
//			mListOfFailedFiles.add(uniqueID);
//			mNumberOfFails = mListOfFailedFiles.size();
//		}
//	}
	
	
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
