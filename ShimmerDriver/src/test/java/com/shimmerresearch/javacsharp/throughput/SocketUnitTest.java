package com.shimmerresearch.javacsharp.throughput;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

class SocketUnitTest {

	@Before
	public void Setup() {
		
	}
	
	//need to run c# application manually
	@Test
	public void WriteDataUsingSocket() {
		final ThroughputTestUsingSocket test = new ThroughputTestUsingSocket();
		
		test.InitializeSocket();
		
		Thread ReadDataUsingSocket = new Thread(){
		    public void run(){
		    	test.ReadDataUsingSocket();
		    }
		};
		
		Thread WriteDataUsingSocket = new Thread(){
		    public void run(){
		    	test.WriteDataUsingSocket();
		    }
		};
	  	
	  	ReadDataUsingSocket.start();
	  	WriteDataUsingSocket.start();
		
	  	try {
			ReadDataUsingSocket.join();
			WriteDataUsingSocket.join();
			
			assertTrue (test.writeResult);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//need to run c# application manually
	@Test
	public void ReadDataUsingSocket() {
		ThroughputTestUsingSocket test = new ThroughputTestUsingSocket();
		
		test.InitializeSocket();
		
	  	int count = test.ReadDataUsingSocket();
	  	
	  	assertTrue (count != 0);
	}
}
