package com.shimmerresearch.bluetooth.synchronizedtest;
//AA-192
import java.util.ArrayList;
import java.util.List;

public class SpeedTestSynchronized {
	List<byte []> mListofInstructions = new  ArrayList<byte[]>(1000000);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SpeedTestSynchronized c= new SpeedTestSynchronized();
		double totalsync=0;
		double totalsyncsync=0;
		double totalnonsync=0;
		long ntimeStart=0;
		long ntimeEnd=0;
		
		
		for (int k=1;k<10;k++){
			
			ntimeStart = System.nanoTime();
			for (int i=0;i<1000000;i++){
				c.count();
			}
			ntimeEnd = System.nanoTime();
			totalnonsync = totalnonsync+(ntimeEnd-ntimeStart)/1000;
			
			ntimeStart = System.nanoTime();
			for (int i=0;i<1000000;i++){
				c.countsynchronizedmethodandlist();
			}
			ntimeEnd = System.nanoTime();
			totalsyncsync = totalsyncsync+(ntimeEnd-ntimeStart)/1000;
			

			
			
			ntimeStart = System.nanoTime();
			for (int i=0;i<1000000;i++){
				c.countsynchronized();
			}
			ntimeEnd = System.nanoTime();
			totalsync = totalsync+(ntimeEnd-ntimeStart)/1000;
			
			
		}
		System.out.println("sync:" + totalsync);
		System.out.println("nonsync:" + totalnonsync);
		System.out.println("syncsync:" + totalsyncsync);
		

	}
	
	public synchronized void countsynchronized(){
		for (byte[]b :mListofInstructions){
			
		}
	}
	
	public void count(){
		for (byte[]b :mListofInstructions){
			
		}
	}
	
	public synchronized void countsynchronizedmethodandlist(){
		synchronized(mListofInstructions){
			for (byte[]b :mListofInstructions){
				
			}
		}
	}

	
	
}
