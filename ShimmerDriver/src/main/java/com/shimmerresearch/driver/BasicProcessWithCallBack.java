package com.shimmerresearch.driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BasicProcessWithCallBack {

	protected Callable mThread = null;
	//protected BlockingQueue<ShimmerMSG> mQueue = new ArrayBlockingQueue<ShimmerMSG>(1024);
	protected LinkedBlockingDeque<ShimmerMsg> mQueue = new LinkedBlockingDeque<ShimmerMsg>(1024);
	protected ConsumerThread mGUIConsumerThread = null;
	private WaitForData mWaitForData = null;
	
	//private List<Callable> mListOfThreads = new ArrayList<Callable>();
	private List<Callable> mListOfThreads = Collections.synchronizedList(new ArrayList<Callable>());
	
//	private List<WaitForData> mListWaitForData = new ArrayList<WaitForData>();
	private List<WaitForData> mListWaitForData = Collections.synchronizedList(new ArrayList<WaitForData>());
	
	private String threadName = "";
	private boolean mIsDebug = false;

	/**This is a seperate thread running on the callback msgs from lower layer
	 * @param shimmerMSG
	 */
	protected abstract void processMsgFromCallback(ShimmerMsg shimmerMSG);
	
	public BasicProcessWithCallBack(){
		
	}
	
	public BasicProcessWithCallBack(BasicProcessWithCallBack b){
		mWaitForData = new WaitForData(b);
	}

	public void queueMethod(ShimmerMsg smsg){
		try {
			mQueue.put(smsg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void queueMethod(int i,Object ojc){
		try {
			mQueue.put(new ShimmerMsg(i,ojc));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getQueueSize(){
		return mQueue.size();
	}

	public void startConsumerThreadIfNull(){
		if (mGUIConsumerThread==null){
			mGUIConsumerThread = new ConsumerThread();
			if(!threadName.isEmpty()){
				mGUIConsumerThread.setName(threadName);
			}
			mGUIConsumerThread.start();
		}
	}

	public class ConsumerThread extends Thread {
		public boolean stop = false;
		
		public void run() {
			while (!stop) {
				try {
					ShimmerMsg shimmerMSG = mQueue.take();
					processMsgFromCallback(shimmerMSG);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.print("QUE BLOCKED");
					e.printStackTrace();
				}
			}
		};
	}
	
	public void setWaitForData(BasicProcessWithCallBack b){
		startConsumerThreadIfNull();
		
		if (mWaitForData!=null){
			synchronized (mListWaitForData){
				mListWaitForData.add(new WaitForData(b));
			}
		} else {
			mWaitForData = new WaitForData(b);
		}
		
		if(mIsDebug){
			printListOfThreads();
		}
	};

	/** TODO needs work to include all cases in "setWaitForData" */
	public void removeSetWaitForData(BasicProcessWithCallBack b){
		
		if(mWaitForData!=null){
			BasicProcessWithCallBack bpwc = mWaitForData.returnBasicProcessWithCallBack();
			if(bpwc.equals(b)){
				consolePrintLn("Removing thread\tHashCode: " + mWaitForData.hashCode());
				mWaitForData = null;
			}
		}
		
    	synchronized (mListWaitForData) {
        	Iterator<WaitForData> entries = mListWaitForData.iterator();
    		while (entries.hasNext()) {
    			WaitForData wFD = entries.next();
    			BasicProcessWithCallBack bpwc = wFD.returnBasicProcessWithCallBack();
    			if(bpwc.equals(b)){
    				consolePrintLn("Removing thread\tHashCode: " + wFD.hashCode());
    				entries.remove();
    				return;
    			}
    		}
    	}
	}
	
	public void setWaitForDataWithSingleInstanceCheck(BasicProcessWithCallBack b){
		startConsumerThreadIfNull();

		if (mWaitForData!=null){
			boolean found = false;
			if (mWaitForData.returnBasicProcessWithCallBack().equals(b)){
				found = true;
			}
			if (!found){
		    	synchronized (mListWaitForData) {
		        	Iterator<WaitForData> entries = mListWaitForData.iterator();
		    		while (entries.hasNext()) {
		    			WaitForData wfd = entries.next();
						if(wfd != null && wfd.returnBasicProcessWithCallBack() != null){
							if (wfd.returnBasicProcessWithCallBack().equals(b)){
								found = true;
							}
						}
		    		}
		    	}
			}
			if(!found){
				synchronized (mListWaitForData){
					mListWaitForData.add(new WaitForData(b));
				}
			}
		} else {
			mWaitForData = new WaitForData(b);
		}
		
	};
	
	public void passCallback(Callable c) {
		if (mThread!=null){
	    	synchronized (mListOfThreads) {
	    		mListOfThreads.add(c);
	    	}
		} 
		else {
			mThread = c;
		}
	}
	
	
    public void sendCallBackMsg(ShimmerMsg s){
    	if (mThread!=null){
    		mThread.callBackMethod(s);
    	} 
    	
    	// April 2017: RM changed for loop to iterator and added synchronisation block  as concurrentmodification with for loop from time to time (this solution may not resolve concurrentmodification, monitor over time)
    	synchronized (mListOfThreads) {
        	Iterator <Callable> entries = mListOfThreads.iterator();
    		while (entries.hasNext()) {
    			Callable c = entries.next();
    			c.callBackMethod(s);
    		}
    	}

    	
//    	for (Callable c: mListOfThreads){
//    		c.callBackMethod(s);
//    	}
    }
    
    public void sendCallBackMsg(int i, Object ojc){
    	if (mThread!=null){
    		mThread.callBackMethod( i, ojc);
    	}
    	
    	// May 2017: RM changed for loop to iterator and added synchronisation block as concurrentmodification with for loop from time to time (this solution may not resolve concurrentmodification, monitor over time)
    	synchronized (mListOfThreads) {
        	Iterator <Callable> entries = mListOfThreads.iterator();
    		while (entries.hasNext()) {
    			Callable c = entries.next();
    			c.callBackMethod(i,ojc);
    		}
    	}

//    	for (Callable c:mListOfThreads){
//    		c.callBackMethod(i,ojc);
//    	}
    }
    
    //this is for the upper layer
	public class WaitForData implements com.shimmerresearch.driver.Callable  
	{
		BasicProcessWithCallBack track;
		
		public BasicProcessWithCallBack returnBasicProcessWithCallBack(){
			return track;
		}
		
		public WaitForData(BasicProcessWithCallBack bpwcb) {  
			track = bpwcb;
			bpwcb.passCallback(this);
		} 
		
		@Override
		public void callBackMethod(ShimmerMsg s) {
			try {
				mQueue.put(s);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}

		@Override
		public void callBackMethod(int i, Object ojc) {
			try {
				mQueue.put(new ShimmerMsg(i,ojc));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}		
	
	}
	
	public void setThreadName(String name){
		threadName = name;
		if(mGUIConsumerThread!=null){
			mGUIConsumerThread.setName(name);
		}
	}
	
	public void processMsgFromCallbackFromUpperLevel(ShimmerMsg shimmerMSG){
		processMsgFromCallback(shimmerMSG);
	}
	
	private void printListOfThreads() {
		consolePrintLn("BasicProcessWithCallBack:\t" + threadName);
		
		if (mWaitForData!=null){
			consolePrintLn("\tSimple Name: " + mWaitForData.getClass().getSimpleName() + "\t" + mWaitForData.returnBasicProcessWithCallBack().getClass().getSimpleName() + "\tHashCode: " + mWaitForData.hashCode());
		}

    	synchronized (mListOfThreads) {
        	Iterator <Callable> entries = mListOfThreads.iterator();
    		while (entries.hasNext()) {
    			Callable c = entries.next();
    			consolePrintLn("\tSimple Name: " + c.getClass().getSimpleName() + "\tHashCode: " + c.hashCode());
    		}
    	}
    	
    	synchronized (mListWaitForData) {
        	Iterator<WaitForData> entries = mListWaitForData.iterator();
    		while (entries.hasNext()) {
    			WaitForData c = entries.next();
    			consolePrintLn("\tSimple Name: " + c.getClass().getSimpleName() + "\t" + c.returnBasicProcessWithCallBack().getClass().getSimpleName() + "\tHashCode: " + c.hashCode());
    		}
    	}
		consolePrintLn("");
	}
	
	private void consolePrintLn(String toPrint){
		if(mIsDebug){
			System.out.println(toPrint);
		}
	}
	
}
