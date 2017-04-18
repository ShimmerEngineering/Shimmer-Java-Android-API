package com.shimmerresearch.driver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BasicProcessWithCallBack {

	protected Callable mThread = null;
	//protected BlockingQueue<ShimmerMSG> mQueue = new ArrayBlockingQueue<ShimmerMSG>(1024);
	protected LinkedBlockingDeque<ShimmerMsg> mQueue = new LinkedBlockingDeque<ShimmerMsg>(1024);
	protected ConsumerThread mGUIConsumerThread = null;
	WaitForData mWaitForData = null;
	List<Callable> mListOfThreads = new ArrayList<Callable>();
	List<WaitForData> mListWaitForData = new ArrayList<WaitForData>();
	String threadName = "";
	
	public BasicProcessWithCallBack(){
		
	}
	
	public void queueMethod(ShimmerMsg smsg){
		try {
			mQueue.put(smsg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startConsumerThread(){
		if (mGUIConsumerThread==null){
			mGUIConsumerThread = new ConsumerThread();
			if(!threadName.isEmpty()){
				mGUIConsumerThread.setName(threadName);
			}
			mGUIConsumerThread.start();
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
	
	/**This is a seperate thread running on the callback msgs from lower layer
	 * @param shimmerMSG
	 */
	protected abstract void processMsgFromCallback(ShimmerMsg shimmerMSG);
	
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
	
	
	
	public BasicProcessWithCallBack(BasicProcessWithCallBack b){
		mWaitForData = new WaitForData(b);
	}
	
	public void setWaitForData(BasicProcessWithCallBack b){
		if (mGUIConsumerThread==null){
			mGUIConsumerThread = new ConsumerThread();
			if(!threadName.isEmpty()){
				mGUIConsumerThread.setName(threadName);
			}
			mGUIConsumerThread.start();
		}
		
		if (mWaitForData!=null){
			mListWaitForData.add(new WaitForData(b));
		} else {
			mWaitForData = new WaitForData(b);
		}
		
	};
	
	public void setWaitForDataWithSingleInstanceCheck(BasicProcessWithCallBack b){
		if (mGUIConsumerThread==null){
			mGUIConsumerThread = new ConsumerThread();
			if(!threadName.isEmpty()){
				mGUIConsumerThread.setName(threadName);
			}
			mGUIConsumerThread.start();
		}
		
		if (mWaitForData!=null){
			boolean found = false;
			if (mWaitForData.returnBasicProcessWithCallBack().equals(b)){
				found = true;
			}
			if (!found){
				for (WaitForData wfd:mListWaitForData){
					if (wfd.returnBasicProcessWithCallBack().equals(b)){
						found = true;
					}
				}
			}
			if(!found){
				mListWaitForData.add(new WaitForData(b));
			}
		} else {
			mWaitForData = new WaitForData(b);
		}
		
	};
	
	public void passCallback(Callable c) {
		// TODO Auto-generated method stub
		if (mThread!=null){
			mListOfThreads.add(c);
		} else {
			mThread = c;
		}
	}
	
	
    public void sendCallBackMsg(ShimmerMsg s){
    	if (mThread!=null){
    		mThread.callBackMethod(s);
    	} 
    	
    	// April 2017: RM changed for loop to iterator as concurrentmodification with for loop from time to time (this solution may not resolve concurrentmodification, monitor over time)
		Iterator <Callable> entries = mListOfThreads.iterator();
		while (entries.hasNext()) {
			Callable c = entries.next();
			c.callBackMethod(s);
		}
    	
//    	for (Callable c: mListOfThreads){
//    		c.callBackMethod(s);
//    	}
    }
    
    public void sendCallBackMsg(int i, Object ojc){
    	if (mThread!=null){
    		mThread.callBackMethod( i, ojc);
    	}
    	
    	// April 2017: RM changed for loop to iterator as concurrentmodification with for loop from time to time (this solution may not resolve concurrentmodification, monitor over time)
		Iterator <Callable> entries = mListOfThreads.iterator();
		while (entries.hasNext()) {
			Callable c = entries.next();
			c.callBackMethod(i,ojc);
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
		
		public WaitForData(BasicProcessWithCallBack bpwcb)  
		{  
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

	
}
