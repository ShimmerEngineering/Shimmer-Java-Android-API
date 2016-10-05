package com.shimmerresearch.algorithms.orientation;

import java.util.List;

import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.driverUtilities.ChannelDetails;

public class Shimmer6DoFor9DoFGui{
	
	private String shimmerName = "";
	private String selectedAccel = "";
	private boolean is9DoFSelected = false;
	private boolean isBothAccelsAvailable = false;
	private List<AbstractAlgorithm> listOfEnabledAlgorithmModulesPerGroup;
	private OrientationModule orientationModule;
	
	public Shimmer6DoFor9DoFGui(String shimmerName, String enabledAccel, List<AbstractAlgorithm> listOfEnabledAlgorithmModulesPerGroup){
		this.shimmerName = shimmerName;
		this.selectedAccel = enabledAccel;
		this.listOfEnabledAlgorithmModulesPerGroup = listOfEnabledAlgorithmModulesPerGroup;
		this.isBothAccelsAvailable = this.listOfEnabledAlgorithmModulesPerGroup.size() > 1;
		
		setChannelDetails(selectedAccel);
	}
	
	public void setSelectedAccelandChannelDetails(String selectedAccel){
		this.selectedAccel = selectedAccel;
		setChannelDetails(this.selectedAccel);
	}
	
//	public void setIs9DoFSelected(String deviceNameAndDoF){
//		this.is9DoFSelected = (deviceNameAndDoF.contains(OrientationModule9DOF.sGD9Dof.mGroupName));
//	}
	
	private void setChannelDetails(String selectedAccel){
		for(AbstractAlgorithm a: listOfEnabledAlgorithmModulesPerGroup){
			if(a instanceof OrientationModule){
				if(((OrientationModule)a).getAccelerometer().equals(selectedAccel)){
					orientationModule = (OrientationModule)a;
				}
			}
		}
	}
	
	public String getUserAssignedShimmerName(){
		return this.shimmerName;
	}
	
	public String getEnabledAccel(){
		return this.selectedAccel;
	}
	
	public List<ChannelDetails> getChannelDetails(){
		if(orientationModule != null){
			return orientationModule.getChannelDetails();
		}
		else{
			System.err.println("orientationModule.getChannelDetails() is NULL!");
			return null;
		}
		
	}
	
	/**
	 * Method to create GUI object for 6DoF and 9DoF quaternion Shimmer3 visualisations
	 */
	
	public boolean isBothAccelsAvailable(){
		return this.isBothAccelsAvailable;
	}
	
//	public boolean isLowNoiseAccelSelected(){
//		return (getEnabledAccel().equals(OrientationModule.GuiFriendlyLabelConfig.ORIENTATAION_LN));
//	}
//	
//	public boolean is9DoFSelected(){
//		return this.is9DoFSelected;
//	}
};