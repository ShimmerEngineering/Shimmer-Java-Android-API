package com.shimmerresearch.javacsharp.throughput;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ProcessUnitTest {

	@Before
	public void Setup() {
		
	}
	
	//steps
	//1.set IsSendDataFromJavaToCSharp variable to true and period to 5 in c sharp 
	//2.run the test
	@Test
	public void ReadAndWriteDataUsingProcessTest() {
		final ThroughputTestUsingProcess test = new ThroughputTestUsingProcess();

		test.InitializeProcess();

		test.MeasureLatency();
		
		Thread ReadDataUsingProcess = new Thread(){
			public void run(){
				test.ReadDataUsingProcess();
			}
		};

		ReadDataUsingProcess.start();

		Thread WriteDataUsingProcess = new Thread(){
			public void run(){
				test.WriteDataUsingProcess();
			}
		};

		WriteDataUsingProcess.start();
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue (test.maximumThroughput > 0);
		assertTrue (!test.writeToCSharpString.equals(null));
	}
}
