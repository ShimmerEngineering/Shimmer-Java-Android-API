package com.shimmerresearch.algorithms;

import javax.vecmath.Vector3d;

public class ActivityProcessing {
	
	/**
	 * Constructor.
	 * 
	 * @param samplingRate in Hz, of Shimmer device , please use 51.2Hz or higher
	 *            
	 */
	
	private double activityIntensity = 1.0;
	private double activityPeriodShort = 1.0;
	private double activityPeriodMedium = 1.0;
	private double activityPeriodLong = 1.0;
	private double activityPercentageSendentary = 1.0;
	private double activityPercentageActive = 1.0;
	private double stepCount = 1.0;
	
	public ActivityProcessing(double samplingRate) {
		setParameters(samplingRate);
	}

	public void setParameters(double samplingRate) {
		
	}
	
	public void activityProcessing(Vector3d data) {
		setActivityIntensity();
		setActivityPeriodShort();
		setActivityPeriodMedium();
		setActivityPeriodLong();
		setActivityStepCount();
		setActivityPercentageSendentary();
		setActivityPercentageActive();
	}

	private void setActivityIntensity() {
		
	}
	
	private void setActivityPeriodShort() {
		
	}

	private void setActivityPeriodMedium() {
		
	}
	
	private void setActivityPeriodLong() {
		
	}
	
	private void setActivityStepCount() {
		
	}

	private void setActivityPercentageSendentary() {
		
	}
	
	private void setActivityPercentageActive() {
		
	}
	
	public double getActivityIntensity(){
		return activityIntensity;
	}
	
	public double getActivityPeriodShort(){
		return activityPeriodShort;
	}
	
	public double getActivityPeriodMedium(){
		return activityPeriodMedium;
	}
	
	public double getActivityPeriodLong(){
		return activityPeriodLong;
	}

	public double getStepCount(){
		return stepCount;
	}
	
	public double getActivityPercentageSendentary(){
		return activityPercentageSendentary;
	}
	
	public double getActivityPercentageActive(){
		return activityPercentageActive;
	}

}
