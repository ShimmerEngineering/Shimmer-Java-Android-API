package com.shimmerresearch.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import com.shimmerresearch.driver.ShimmerObject;

/** Hold all progress details for Bluetooth activity.
 * @author mnolan
 *
 */
//TODO add proper comments
//TODO remove unnecessary code carried over from dock progress details
public class ProgressReportAll implements Serializable {

	public List<ShimmerObject> mListOfShimmers;
	public LinkedHashMap<String, ProgressReportPerDevice> mMapOfOperationProgressInfo = new LinkedHashMap<String, ProgressReportPerDevice>();
	
	public enum BLUETOOTH_JOB{
		NONE,
		CONFIGURE
	}
	public BLUETOOTH_JOB currentJob = BLUETOOTH_JOB.NONE;
	
	public static enum OperationState {
		PENDING,
		INPROGRESS,
		FINISHED,
		CANCELLING,
		CANCELLED
	}
	public OperationState mOperationState = OperationState.PENDING;
	
    public static final String[] mListOfOperationStates = new String[]{
		"Pending",
    	"In Progress",
    	"Finished",
    	"Cancelling",
    	"Cancelled"
    };
	
	public int mProgressEndValue = 0;
	public int mProgressCounter = 0;
	public int mNumberOfFails = 0;
//	public List<Integer> mListOfFailedCmds = new ArrayList<Integer>();
	public int mProgressPercentageComplete = 0;

	public ProgressReportAll(BLUETOOTH_JOB currentJob, List<ShimmerObject> lso, int total) {
		this.currentJob = currentJob;
		mListOfShimmers = lso;
		
		mMapOfOperationProgressInfo.clear();
		for(ShimmerObject shimmer:lso){
			mMapOfOperationProgressInfo.put(shimmer.mUniqueID, new ProgressReportPerDevice(shimmer));
			mMapOfOperationProgressInfo.get(shimmer.mUniqueID).mProgressEndValue = total;
		}
		updateProgressTotal();
	}
	
	public void updateProgressTotal() {
		mProgressEndValue = mMapOfOperationProgressInfo.keySet().size();
	}
	
	public void updateProgressCount() {
		mProgressCounter += 1;
		mProgressPercentageComplete = ((int)(((double)mProgressCounter/(double)mProgressEndValue)*100));
	}
	
	public void updateProgressPerDevice(String bluetoothAddress, ProgressReportPerCmd pRPC){
		mMapOfOperationProgressInfo.get(bluetoothAddress).updateProgress(pRPC);
		if(mMapOfOperationProgressInfo.get(bluetoothAddress).mProgressCounter == mMapOfOperationProgressInfo.get(bluetoothAddress).mProgressEndValue){
			updateProgressCount();
		}
	}

	/**Performs a deep copy of ProgressDetailsAll by Serializing
	 * @return ProgressDetailsAll the deep copy of the current ProgressDetailsAll
	 * @see java.io.Serializable
	 */
	public ProgressReportAll deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ProgressReportAll) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
