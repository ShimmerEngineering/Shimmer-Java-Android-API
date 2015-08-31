package com.shimmerresearch.driver;

public interface ShimmerHardwareSensors {

	void updateSensors();
	
	void processRawDataUsingSensors();
	
	void deleteSensor();
	
	void manageSensor();
	
}
