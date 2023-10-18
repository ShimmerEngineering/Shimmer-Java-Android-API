package com.shimmerresearch.javacsharp.throughput;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SocketUnitTest {

	@Before
	public void Setup() {
		
	}
	
	//steps
	//1.set IsSendDataFromJavaToCSharp variable to true and period to 5 in c sharp 
	//2.run the test
	//3.run the c# application
	@Test
	public void ReadAndWriteDataUsingSocketTest() {
		final ThroughputTestUsingSocket test = new ThroughputTestUsingSocket();
		
		//initialize socket
  		test.InitializeSocket();
  		
  		test.MeasureLatency();
		
		Thread ReadDataUsingSocket = new Thread(){
		    public void run(){
		    	test.ReadDataUsingSocket();
		    }
		};
		
	  	ReadDataUsingSocket.start();
	  		
  		Thread WriteDataUsingSocket = new Thread(){
		    public void run(){
		    	test.WriteDataUsingSocket();
		    }
		};
		
	  	WriteDataUsingSocket.start();
	  	
	  	try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  	
	  	assertTrue (test.maximumThroughput != 0);
	  	assertTrue (!test.writeToCSharpString.equals(null));
	}
}
