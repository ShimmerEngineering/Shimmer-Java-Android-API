package com.shimmerresearch.javacsharp.throughput;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ProcessUnitTest {

	@Before
	public void Setup() {
		
	}
	
	@Test
	public void ReadDataUsingProcess() {
		ThroughputTestUsingProcess a = new ThroughputTestUsingProcess();		
		a.InitializeProcess();		
		int count = a.ReadDataUsingProcess();		
		assertTrue (count != 0);
	}
	
	@Test
	public void WriteDataUsingProcess() {
		//TODO
	}
}
