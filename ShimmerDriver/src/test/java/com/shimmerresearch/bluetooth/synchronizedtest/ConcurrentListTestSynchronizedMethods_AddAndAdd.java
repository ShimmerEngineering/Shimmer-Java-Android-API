//This is related to test conducted in AA-192
package com.shimmerresearch.bluetooth.synchronizedtest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConcurrentListTestSynchronizedMethods_AddAndAdd{

	Thread mCurrentThread;
	private List<byte []> mListofInstructions = new  ArrayList<byte[]>();
	
	public static void main(String[] args) {
		ConcurrentListTestSynchronizedMethods_AddAndAdd c = new ConcurrentListTestSynchronizedMethods_AddAndAdd();
		c.startTest();
		
	}
	
	public synchronized void addinstruction(byte[] b){
		mListofInstructions.add(b);
	}
	
	public synchronized void removeinstruction(){
		if (!mListofInstructions.isEmpty()){
			byte[] b = mListofInstructions.remove(0);
			if (b==null){
				System.out.println("Null");
			}
		}
	}
	
	public synchronized void clearinstructions(){
		mListofInstructions.clear();
	}
	
	public void startTest(){
		listthreadadd t1 = new listthreadadd(this);
		listthreadadd t2 = new listthreadadd(this);
		Thread th2 = new Thread(t2);
		
		/*listthreadremove rt1 = new listthreadremove(this);
		Thread th2 = new Thread(rt1);
		*/
		/*listthreadclear ct1 = new listthreadclear(this);
		Thread th2 = new Thread(ct1);
		*/
		
		Thread th1 = new Thread(t1);
		
		
		th1.start();
		th2.start();
	}
	
	class listthreadadd implements Runnable{

		ConcurrentListTestSynchronizedMethods_AddAndAdd app;
		
		public listthreadadd(ConcurrentListTestSynchronizedMethods_AddAndAdd concurrentListTest2) {
			app=concurrentListTest2;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (int i=1;i<1000;i++){
				byte[] b = {1,1};
				app.addinstruction(b);
			}
		}
		
	}
	
	class listthreadremove implements Runnable{

		ConcurrentListTestSynchronizedMethods_AddAndAdd app;
		
		public listthreadremove(ConcurrentListTestSynchronizedMethods_AddAndAdd concurrentListTest2) {
			app=concurrentListTest2;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (int i=1;i<1000;i++){
				app.removeinstruction();
			}
		}
		
	}

	class listthreadclear implements Runnable{

		ConcurrentListTestSynchronizedMethods_AddAndAdd app;
		
		public listthreadclear(ConcurrentListTestSynchronizedMethods_AddAndAdd concurrentListTest2) {
			app=concurrentListTest2;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (int i=1;i<1000;i++){
				app.clearinstructions();
			}
		}
		
	}
	
	
}
