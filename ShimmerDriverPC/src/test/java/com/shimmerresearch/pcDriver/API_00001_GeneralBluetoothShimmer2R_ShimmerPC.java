package com.shimmerresearch.pcDriver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exceptions.ShimmerException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_00001_GeneralBluetoothShimmer2R_ShimmerPC {

	static ShimmerPC shimmer = new ShimmerPC("ShimmerDevice"); 
	public static final int SHIMMER_2R=2; 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		shimmer.connect("COM14",null);
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
	public void testBGetHW() {
		if (shimmer.getHardwareVersion()==HW_ID.SHIMMER_2R){
			assert(true);
		} else {
			assert(false);
		}
	}
	
	@Test
	public void testCGetFW() {
		if (shimmer.getFirmwareIdentifier()==FW_ID.BTSTREAM){
			assert(true);
		} else {
			assert(false);
		}
	}
	
	@Test
	public void testDWriteReadSamplingRate() throws Exception {
		shimmer.writeShimmerAndSensorsSamplingRate(128);
		Thread.sleep(5000);
		if (shimmer.getSamplingRateShimmer()==128) {
			assert(true);
		} else {
			assert(false);
		}
	}
	
	@Test
	public void testEStartStreaming() throws Exception {
			shimmer.startStreaming();
			Thread.sleep(5000);
			if (shimmer.isStreaming()) {
				assert(true);
			} else {
				assert(false);
			}
		}
	
	@Test
	public void testFStopStreaming() throws Exception {
			shimmer.stopStreaming();
			Thread.sleep(5000);
			if (shimmer.isStreaming()) {
				assert(false);
			} else {
				assert(true);
			}
		}
	
	@Test
	public void testGMultipleStartStopStreaming() throws Exception {
		for (double i=1; i<8 ; i++) {
			if (i%2!=0) {	//odd value of i will start streaming
				shimmer.startStreaming();
				Thread.sleep(3000);
				if (shimmer.isStreaming()) {
					assert(true);
				} else {
					assert(false);
				}
			}
			
			else		//even value of i will stop streaming
			{
				shimmer.stopStreaming();
				Thread.sleep(3000);
				if (shimmer.isStreaming()) {
					assert(false);
				} else {
					assert(true);
				}
			}
			}
		}
		
	
	@Test
	public void testHDisconnect() {
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
