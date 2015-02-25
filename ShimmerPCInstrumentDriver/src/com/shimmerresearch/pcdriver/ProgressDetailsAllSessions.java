package com.shimmerresearch.pcdriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ProgressDetailsAllSessions implements Serializable{

	
	public enum Operation {
		NONE("None"),
		SD_SCAN("SD Scan"),
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
			progress = progress/(double)mNumberOfSessions;
			mProgressPercentageComplete = (int) progress;

			if(!operationSuccessful) {
				mListOfFailedSessions.add(uniqueID);
				mNumberOfFails = mListOfFailedSessions.size();
			}
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
