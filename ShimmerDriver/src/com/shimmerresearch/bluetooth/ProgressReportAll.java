package com.shimmerresearch.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;



import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
//import com.shimmerresearch.bluetooth.ShimmerBluetooth.CURRENT_OPERATION;
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
	
	public BT_STATE currentOperationBtState = BT_STATE.DISCONNECTED;
	
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
	
	public int mProgressEndValue = 100;
	public int mProgressCounter = 0;
	public int mNumberOfFails = 0;
//	public List<Integer> mListOfFailedCmds = new ArrayList<Integer>();
	public int mProgressPercentageComplete = 0;

	public ProgressReportAll(BT_STATE currentOperationBtState, List<ShimmerObject> lso) {
		this.currentOperationBtState = currentOperationBtState;
		mListOfShimmers = lso;
		
		mMapOfOperationProgressInfo.clear();
		for(ShimmerObject shimmer:lso){
			mMapOfOperationProgressInfo.put(shimmer.mUniqueID, new ProgressReportPerDevice(shimmer, currentOperationBtState, 1));
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
	
	public void updateProgressPerDevice(String comPort, ProgressReportPerDevice pRPD){
		mMapOfOperationProgressInfo.put(comPort, pRPD);
		if(mMapOfOperationProgressInfo.get(comPort).mProgressCounter == mMapOfOperationProgressInfo.get(comPort).mProgressEndValue){
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