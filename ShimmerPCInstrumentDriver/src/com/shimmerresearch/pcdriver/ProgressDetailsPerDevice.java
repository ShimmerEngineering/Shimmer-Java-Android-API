package com.shimmerresearch.pcdriver;

import java.io.Serializable;


public class ProgressDetailsPerDevice implements Serializable{

	
	public enum OperationState {
		PENDING("Pending"),
		INPROGRESS("In Progress"),
		SUCCESS("Success"),
		FAIL("Fail");
		
		private String tag;
 
		private OperationState(String tag) {
			this.tag = tag;
		}
	}
	
	public OperationState mOperationState = OperationState.PENDING;
	public double mProgressPercentageComplete = 0;
	
	
	public ProgressDetailsPerDevice() {
		super();
	}
	
	public void updateProgressDataRetreived(){
		
	}
	
	public void updateProgressFiltersApplied(){
		
	}
	
	public void updateProgressAlgorithmsApplied(){
		
	}
	
	public void updateProgressDataInserted(int rowsInserted){
		
	}
	
}
