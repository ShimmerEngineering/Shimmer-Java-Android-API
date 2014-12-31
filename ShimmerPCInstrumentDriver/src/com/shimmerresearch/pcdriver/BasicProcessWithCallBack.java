package com.shimmerresearch.pcdriver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public abstract class BasicProcessWithCallBack {

	protected Callable mThread;
	protected BlockingQueue<ShimmerMSG> mQueue = new ArrayBlockingQueue<ShimmerMSG>(1024);
	protected ConsumerThread mGUIConsumerThread;
	WaitForData mWaitForData = null;
	public BasicProcessWithCallBack(){
		mGUIConsumerThread = new ConsumerThread();
		mGUIConsumerThread.start();
	}
	
	public void queueMethod(int i,Object ojc){
		try {
			mQueue.put(new ShimmerMSG(i,ojc));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**This is a seperate thread running on the callback msgs from lower layer
	 * @param shimmerMSG
	 */
	protected abstract void processMsgFromCallback(ShimmerMSG shimmerMSG);
	
	
	
	
	
	public class ConsumerThread extends Thread {
		public boolean stop = false;
		public void run() {
			while (!stop) {
				try {
					ShimmerMSG shimmerMSG = mQueue.take();
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
		mWaitForData = new WaitForData(b);
	};
	
	public void passCallback(Callable c) {
		// TODO Auto-generated method stub
		mThread = c;
	}
	
	
    public void sendCallBackMsg(ShimmerMSG s){
    	if (mThread!=null){
    		mThread.callBackMethod(s);
    	}
    }
    
    public void sendCallBackMsg(int i, Object ojc){
    	if (mThread!=null){
    		mThread.callBackMethod( i, ojc);
    	}
    }
    
    //this is for the upper layer
	public class WaitForData implements com.shimmerresearch.pcdriver.Callable  
	{

		public WaitForData(BasicProcessWithCallBack bpwcb)  
		{  
			bpwcb.passCallback(this);
		} 
		
		@Override
		public void callBackMethod(ShimmerMSG s) {
			try {
				mQueue.put(s);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}

		@Override
		public void callBackMethod(int i, Object ojc) {
			try {
				mQueue.put(new ShimmerMSG(i,ojc));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}		
	
	}

	
}
