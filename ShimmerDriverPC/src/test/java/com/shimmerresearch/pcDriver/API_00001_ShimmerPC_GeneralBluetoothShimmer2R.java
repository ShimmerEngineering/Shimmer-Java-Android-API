package com.shimmerresearch.pcDriver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exceptions.ShimmerException;

/* 
 * This test class is to test Shimmer 2R connectivity, version, sampling rate, 
 * and check its start and stop streaming functionality.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_00001_ShimmerPC_GeneralBluetoothShimmer2R {

	static ShimmerPC shimmer = new ShimmerPC("ShimmerDevice"); 
	public static final int SHIMMER_2R=2; 
	final int DELAY_DURATION_MS = 3000;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		shimmer.connect("COM7",null);
		Thread.sleep(10000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}
	
	
	@Test	//test Shimmer 2R connection
	public void testAConnect() {
		if (shimmer.isConnected()) {
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//get hardware version of Shimmer 2R
	public void testBGetHW() {
		if (shimmer.getHardwareVersion()==HW_ID.SHIMMER_2R){
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//get firmware version of Shimmer 2R
	public void testCGetFW() {
		if (shimmer.getFirmwareIdentifier()==FW_ID.BTSTREAM){
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//write and read sampling rate of the hardware
	public void testDWriteReadSamplingRate() throws Exception {
		double samplingrate;
		shimmer.readSamplingRate();
		Thread.sleep(DELAY_DURATION_MS);
		System.out.println("Current Shimmer 2R sampling rate is:" + shimmer.getSamplingRateShimmer());
		
		if (shimmer.getSamplingRateShimmer()==128) {
			samplingrate = 51.2;
		}
		else {
			samplingrate = 128;
		}
		shimmer.writeShimmerAndSensorsSamplingRate(samplingrate);
		Thread.sleep(DELAY_DURATION_MS);
		shimmer.readSamplingRate();
		Thread.sleep(DELAY_DURATION_MS);
		if (shimmer.getSamplingRateShimmer()== samplingrate ) {
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//test Shimmer 2R start streaming 
	public void testEStartStreaming() throws Exception {
			shimmer.startStreaming();
			Thread.sleep(DELAY_DURATION_MS);
			if (shimmer.isStreaming()) {
				assert(true);
			} else {
				assert(false);
			}
		}
	
	@Test	//test Shimmer 2R stop streaming 
	public void testFStopStreaming() throws Exception {
			shimmer.stopStreaming();
			Thread.sleep(DELAY_DURATION_MS);
			if (shimmer.isStreaming()) {
				assert(false);
			} else {
				assert(true);
			}
		}
	
	
	@Test	//test for multiple start and stop streaming 
	public void testGMultipleStartStopStreaming() throws Exception {
		for (double i=1; i<8 ; i++) {
			
			//odd value of i will start streaming
			if (i%2!=0) {	
				shimmer.startStreaming();
				Thread.sleep(DELAY_DURATION_MS);
				if (shimmer.isStreaming()) {
					assert(true);
				} else {
					assert(false);
				}
			}
			//even value of i will stop streaming
			else		
			{
				shimmer.stopStreaming();
				Thread.sleep(DELAY_DURATION_MS);
				if (shimmer.isStreaming()) {
					assert(false);
				} else {
					assert(true);
				}
			}
			}
		}
		
	
	@Test	//test Shimmer 2R disconnection
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
