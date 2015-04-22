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
	
	public int mNumberOfIterations = 0;
	public int mInserterdIterations = 0;
	
	
	public ProgressDetailsPerDevice() {
		super();
	}
	
	public void updateProgressDataRetreived(){
		mProgressPercentageComplete += 20.0/mNumberOfIterations;
	}
	
	public void updateProgressFiltersApplied(){
		mProgressPercentageComplete += 10.0/mNumberOfIterations;
	}
	
	public void updateProgressAlgorithmsApplied(){
		mProgressPercentageComplete += 10.0/mNumberOfIterations;
	}
	
	public void updateProgressDataInserted(int rowsInserted, int totalRows){
		if(rowsInserted==totalRows){
			mInserterdIterations++;
			if(mInserterdIterations==mNumberOfIterations){
				mProgressPercentageComplete = 100;
				mOperationState = OperationState.SUCCESS;
			}
			else{
				mProgressPercentageComplete += (rowsInserted/totalRows*0.6)/mNumberOfIterations;
				if(mProgressPercentageComplete>=100){
					mProgressPercentageComplete=100;
					mOperationState = OperationState.SUCCESS;
//					mInserterdIterations++;
				}
			}
		}
		else{
			mProgressPercentageComplete += (rowsInserted/totalRows*0.6)/mNumberOfIterations;
			if(mProgressPercentageComplete>=100){
				mProgressPercentageComplete=100;
				mOperationState = OperationState.SUCCESS;
//				mInserterdIterations++;
			}
		}
		
	}
	
	public void updateProgressFailed(){
		mProgressPercentageComplete = 100;
		mOperationState = OperationState.FAIL;
	}
	
}
