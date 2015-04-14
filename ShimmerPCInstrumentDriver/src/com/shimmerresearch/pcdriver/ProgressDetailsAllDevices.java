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
		RETRIEVING("Retreiving Data"),
		PROCESSING("Processing Data"),
		INSERTING("Inserting Data");
		
		private String tag;
 
		private Operation(String tag) {
			this.tag = tag;
		}
	}
	
	//the key is the mac address + "." + session id
	SerializableTreeMap<String , ProgressDetailsPerDevice> mMapOfDeviceProgressInfo = new SerializableTreeMap<String, ProgressDetailsPerDevice>();
	
	public int mNumberOfFails = 0;
	public List<String> mListOfFailedDevices = new ArrayList<String>();
	public int mProgressPercentageComplete = 0;
	
	
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
