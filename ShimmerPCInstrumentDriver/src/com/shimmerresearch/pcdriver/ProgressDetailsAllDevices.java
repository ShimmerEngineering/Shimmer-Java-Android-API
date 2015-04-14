package com.shimmerresearch.pcdriver;

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
	
	
	SerializableTreeMap<String , ProgressDetailsPerDevice> mMapOfDeviceProgressInfo = new SerializableTreeMap<String, ProgressDetailsPerDevice>();
	
}
