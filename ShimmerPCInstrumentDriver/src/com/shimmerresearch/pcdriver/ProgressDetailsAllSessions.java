package com.shimmerresearch.pcdriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class ProgressDetailsAllSessions implements Serializable{

	
	public enum Operation {
		NONE("None"),
//		SD_SCAN("SD Scan"),
		SD_COPY("SD Copy"),
		SD_DELETE("SD Delete"),
		IMPORTING("Importing Data");
		
//		private int value;
		private String tag;
 
		private Operation(String tag) {
			this.tag = tag;
//			this.value = value;
		}
	}
	
		//the key is the full trial name + "." + session id
		public SerializableTreeMap<String,ProgressDetailsPerSession> mMapOfSessionsProgressInfo = new SerializableTreeMap<String,ProgressDetailsPerSession>();

		public Operation mOperationCurrent = Operation.NONE;

		public int mNumberOfSessions = 0;
		public int mProgressCounter = 0;
		public int mNumberOfFails = 0;
		public List<String> mListOfFailedSessions = new ArrayList<String>();
		public int mProgressPercentageComplete = 0;
		public int mProgressStepCompleted = 0;
		public double mProgressPercentagePerStep = 0;
		
		//needed for the delete feedback
		public List<ShimmerLogDetails> mListOfFilesToDelete;
		
		//this indicates if we are only deleting the data from the SD card, that's it, the copy and import is not done
		public boolean mOnlyDelete=false;
		
		public ProgressDetailsAllSessions(Operation operationCurrent) {
			super();
			mOperationCurrent = operationCurrent;
		}


		public void updateProgressTotal() {
			mNumberOfSessions = mMapOfSessionsProgressInfo.keySet().size();
		}
		
		
		public void updateProgress(String uniqueID,boolean operationSuccessful) {
			
			double progress=0;
			for(ProgressDetailsPerSession dps: mMapOfSessionsProgressInfo.values()){
				progress += dps.mProgressPercentageComplete;
			}
			mProgressPercentagePerStep = progress/(double)mNumberOfSessions;
			if(mProgressPercentagePerStep==100 && mOperationCurrent==Operation.SD_DELETE){
				mProgressPercentageComplete = 100;
			}
			else
				mProgressPercentageComplete = (int) ((int) mProgressStepCompleted + (mProgressPercentagePerStep/3.0)); //divede between 3 because there are 3 steps, copy, import and delete
			
			if(mProgressPercentagePerStep==100)
				mProgressStepCompleted += (int) (mProgressPercentagePerStep/3.0);

			if(!operationSuccessful) {
				mListOfFailedSessions.add(uniqueID);
				mNumberOfFails = mListOfFailedSessions.size();
				if(mNumberOfFails==mMapOfSessionsProgressInfo.size()){
					mProgressPercentageComplete=100;
				}
			}
		}
		
		public void updateProgressOnlyDelete(String uniqueID,boolean operationSuccessful){
			
			double progress=0;
			for(ProgressDetailsPerSession dps: mMapOfSessionsProgressInfo.values()){
				progress += dps.mProgressPercentageComplete;
			}
			
			mProgressPercentageComplete = (int) (progress/(double)mNumberOfSessions);
			
			if(!operationSuccessful) {
				mListOfFailedSessions.add(uniqueID);
				mNumberOfFails = mListOfFailedSessions.size();
				if(mNumberOfFails==mMapOfSessionsProgressInfo.size()){
					mProgressPercentageComplete=100;
				}
			}
		}
		
		public void updateDeleteProgress(String dock, int slotNumber, String uniqueID){
			
			String key=null;
//			List<ShimmerLogDetails> tmpList = new ArrayList<ShimmerLogDetails>();
			for(ShimmerLogDetails ld: mListOfFilesToDelete){
				if(ld.mDockID.equals(dock) && ld.mSlotID == slotNumber){
					key = ld.mFullTrialName+"."+ld.mNewSessionId;
					String path = ld.mAbsolutePath;
					ProgressDetailsPerSession dps = mMapOfSessionsProgressInfo.get(key);
					ProgressDetailsPerFile dpf = dps.mMapOfFilesProgressInfo.get(path);
					dpf.mOperationState = ProgressDetailsPerFile.OperationState.SUCCESS;
					dpf.mProgressPercentageComplete=100;
					dps.updateProgressDelete(path, true);
					if(!mOnlyDelete)
						updateProgress(key, true);
					else
						updateProgressOnlyDelete(key, true);
				}
			}				
		}
		
		public void setDataForCopy(List<ShimmerLogDetails> list){
			
			mOperationCurrent = Operation.SD_COPY;
			for(ShimmerLogDetails ld: list){
				String key = ld.mFullTrialName+"."+ld.mNewSessionId;
				if(mMapOfSessionsProgressInfo.containsKey(key)){
					ProgressDetailsPerSession dps = mMapOfSessionsProgressInfo.get(key);
					dps.mMapOfFilesProgressInfo.put(ld.mAbsolutePath, new ProgressDetailsPerFile());
					dps.updateProgressTotal();
				}
				else{
					ProgressDetailsPerSession dps = new ProgressDetailsPerSession(ProgressDetailsPerSession.OperationState.PENDING);
					dps.mMapOfFilesProgressInfo.put(ld.mAbsolutePath, new ProgressDetailsPerFile());
					dps.updateProgressTotal();
					mMapOfSessionsProgressInfo.put(key, dps);
				}
			}
			updateProgressTotal();
		}
		
		public void setDataForImport(TreeMap<String, TreeMap<Integer, TreeMap<String, TreeMap<Integer, ShimmerLogDetails>>>> map){
			
			mOperationCurrent = Operation.IMPORTING;
			mMapOfSessionsProgressInfo.clear();
			mListOfFailedSessions.clear();
			for(String trialName: map.keySet()){
				TreeMap<Integer, TreeMap<String, TreeMap<Integer, ShimmerLogDetails>>> mapOfSessions = map.get(trialName);
				for(Integer sessionId: mapOfSessions.keySet()){
					String key = trialName+"."+sessionId;
					ProgressDetailsPerSession dps = mMapOfSessionsProgressInfo.get(key);
					TreeMap<String, TreeMap<Integer, ShimmerLogDetails>> mapOfFolders = mapOfSessions.get(sessionId);
					for(String folder: mapOfFolders.keySet()){
						String absPath = "";
						int i=0;
						TreeMap<Integer, ShimmerLogDetails> mapOfFiles = mapOfFolders.get(folder);
						for(ShimmerLogDetails ld: mapOfFiles.values()){
							if(dps!=null){
//								dps = mMapOfSessionsProgressInfo.get(key);
//								String path = ld.mAbsolutePathWhereFileWasCopied.replace("//", "\\");
//								path = path.replace("/", "\\");
								dps.mMapOfFilesProgressInfo.put(ld.mAbsolutePathWhereFileWasCopied, new ProgressDetailsPerFile());
								dps.updateProgressTotal();
								if(i==0){
									absPath = ld.mAbsolutePathWhereFileWasCopied;
									i++;
								}
							}
							else{
								dps = new ProgressDetailsPerSession(ProgressDetailsPerSession.OperationState.PENDING);
//								String path = ld.mAbsolutePathWhereFileWasCopied.replace("//", "\\");
//								path = path.replace("/", "\\");
								dps.mMapOfFilesProgressInfo.put(ld.mAbsolutePathWhereFileWasCopied, new ProgressDetailsPerFile());
								dps.updateProgressTotal();
								mMapOfSessionsProgressInfo.put(key, dps);
								if(i==0){
									absPath = ld.mAbsolutePathWhereFileWasCopied;
									i++;
								}
							}
						}
						String[] splitPath = absPath.split(folder);
						String folderPath = splitPath[0]+folder;
						
						dps.mMapOfFoldersProgressInfo.put(folderPath, 0D);
					}
					dps = mMapOfSessionsProgressInfo.get(key);
					dps.updateFoldersTotal();
				}
			}
			updateProgressTotal();
		}
		
		public void setDataForDelete(List<ShimmerLogDetails> list){
			
			mOperationCurrent = Operation.SD_DELETE;
			mMapOfSessionsProgressInfo.clear();
			mListOfFailedSessions.clear();
			for(ShimmerLogDetails ld: list){
				String key = ld.mFullTrialName+"."+ld.mNewSessionId;
				if(mMapOfSessionsProgressInfo.containsKey(key)){
					ProgressDetailsPerSession dps = mMapOfSessionsProgressInfo.get(key);
					dps.mMapOfFilesProgressInfo.put(ld.mAbsolutePath, new ProgressDetailsPerFile());
					dps.updateProgressTotal();
				}
				else{
					ProgressDetailsPerSession dps = new ProgressDetailsPerSession(ProgressDetailsPerSession.OperationState.PENDING);
					dps.mMapOfFilesProgressInfo.put(ld.mAbsolutePath, new ProgressDetailsPerFile());
					dps.updateProgressTotal();
					mMapOfSessionsProgressInfo.put(key, dps);
				}
			}
			updateProgressTotal();
		}
		
		public boolean isImportFinished(){
			boolean finished = false;
			
			if(mOperationCurrent==Operation.IMPORTING){
				int counter=0;
				for(ProgressDetailsPerSession dps: mMapOfSessionsProgressInfo.values()){
					if(dps.mProgressPercentageComplete==100)
						counter++;
				}
				if(counter==mMapOfSessionsProgressInfo.size())
					finished=true;
				
			}
			else if(mOperationCurrent==Operation.SD_DELETE){
				finished=true;
			}
			
			return finished;
		}
		
		public void clearFailedSessions(){
			mListOfFailedSessions.clear();
			mNumberOfFails=0;
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
