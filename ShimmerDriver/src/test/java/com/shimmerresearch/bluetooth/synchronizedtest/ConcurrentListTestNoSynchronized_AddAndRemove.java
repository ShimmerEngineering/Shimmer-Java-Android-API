//This is related to test conducted in AA-192
//Null being placed in the list because the list is not synchronized
package com.shimmerresearch.bluetooth.synchronizedtest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConcurrentListTestNoSynchronized_AddAndRemove{

	Thread mCurrentThread;
	private List<byte []> mListofInstructions = new  ArrayList<byte[]>();
	
	public static void main(String[] args) {
		ConcurrentListTestNoSynchronized_AddAndRemove c = new ConcurrentListTestNoSynchronized_AddAndRemove();
		c.startTest();
		
	}
	
	public void addinstruction(byte[] b){
		mListofInstructions.add(b);
	}
	
	public void removeinstruction(){
	
			if(mListofInstructions.size()>0){
				byte[] b = mListofInstructions.remove(0);
				if (b==null){
					System.out.println("Null");
				}
			}
		
	}
	
	public void clearinstructions(){
		mListofInstructions.clear();
	}
	
	public void startTest(){
		listthreadadd t1 = new listthreadadd(this);
/*		listthread t2 = new listthread(this);
		Thread th2 = new Thread(t2);*/
		
		listthreadremove rt1 = new listthreadremove(this);
		Thread th2 = new Thread(rt1);
		
		/*listthreadclear ct1 = new listthreadclear(this);
		Thread th2 = new Thread(ct1);
		*/
		
		Thread th1 = new Thread(t1);
		
		
		th1.start();
		th2.start();
	}
	
	class listthreadadd implements Runnable{

		ConcurrentListTestNoSynchronized_AddAndRemove app;
		
		public listthreadadd(ConcurrentListTestNoSynchronized_AddAndRemove concurrentListTest2) {
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

		ConcurrentListTestNoSynchronized_AddAndRemove app;
		
		public listthreadremove(ConcurrentListTestNoSynchronized_AddAndRemove concurrentListTest2) {
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

		ConcurrentListTestNoSynchronized_AddAndRemove app;
		
		public listthreadclear(ConcurrentListTestNoSynchronized_AddAndRemove concurrentListTest2) {
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
