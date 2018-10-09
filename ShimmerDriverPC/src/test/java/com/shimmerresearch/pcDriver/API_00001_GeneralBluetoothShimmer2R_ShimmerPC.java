package com.shimmerresearch.pcDriver;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.shimmerresearch.exceptions.ShimmerException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_00001_GeneralBluetoothShimmer2R_ShimmerPC {

	static ShimmerPC shimmer = new ShimmerPC("ShimmerDevice"); 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		shimmer.connect("COM7",null);
		Thread.sleep(10000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Test
	public void testAConnect() {
		if (shimmer.isConnected()) {
			assert(true);
		} else {
			assert(false);
		}
	}

	@Test
	public void testBDisconnect() {
		try {
			shimmer.disconnect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assert(false);
		}
		if (shimmer.isConnected()) {
			assert(false);
		} else {
			assert(true);
		}
	}

	  

}
