package com.shimmerresearch.algorithms;

import com.shimmerresearch.driver.Configuration;

public class Shimmer9DoF{
	
	private String shimmerName = "";
	private String selectedAccel = "";
	private boolean is9DoFSelected = false;
	private boolean isBothAccelsAvailable = false;
	
	public Shimmer9DoF(String shimmerName, String enabledAccel, boolean isBothAccelsAvailable){
		this.shimmerName = shimmerName;
		this.selectedAccel = enabledAccel;
		this.isBothAccelsAvailable = isBothAccelsAvailable;
	}
	
	public void setSelectedAccel(String selectedAccel){
		this.selectedAccel = selectedAccel;
	}
	
	public void setIs9DoFSelected(String deviceNameAndDoF){
		this.is9DoFSelected = (deviceNameAndDoF.contains(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.toString()));
	}
	
	public String getUserAssignedShimmerName(){
		return this.shimmerName;
	}
	
	public String getEnabledAccel(){
		return this.selectedAccel;
	}
	
	public boolean isLowNoiseAccelSelected(){
		return (getEnabledAccel().contains(OrientationModule9DOF.LN));
	}
	
	public boolean isBothAccelsAvailable(){
		return this.isBothAccelsAvailable;
	}
	
	public boolean is9DoFSelected(){
		return this.is9DoFSelected;
	}
};