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


/**This test class is to test Shimmer 2R connectivity, version, sampling rate, 
 * and check its start and stop streaming functionality. Note to set the COM PORT
 * @author Mas Azalya and Jong Chern
 * @version 001
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_00001_ShimmerPC_GeneralBluetoothShimmer2R {

	static ShimmerPC shimmer = new ShimmerPC("ShimmerDevice"); 
	public static final int SHIMMER_2R=2; 
	final int DELAY_DURATION_MS = 3000;
	final static String COM_PORT = "COM7";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		shimmer.connect(COM_PORT,null);
		Thread.sleep(10000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}
	
	
	@Test	//test Shimmer 2R connection
	public void test001_Connect() {
		if (shimmer.isConnected()) {
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//get hardware version of Shimmer 2R
	public void test002_GetHW() {
		if (shimmer.getHardwareVersion()==HW_ID.SHIMMER_2R){
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//get firmware version of Shimmer 2R
	public void test003_GetFW() {
		if (shimmer.getFirmwareIdentifier()==FW_ID.BTSTREAM){
			assert(true);
		} else {
			assert(false);
		}
	}
	
	
	@Test	//write and read sampling rate of the hardware
	public void test004_WriteReadSamplingRate() throws Exception {
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
	public void test005_StartStreaming() throws Exception {
			shimmer.startStreaming();
			Thread.sleep(DELAY_DURATION_MS);
			if (shimmer.isStreaming()) {
				assert(true);
			} else {
				assert(false);
			}
		}
	
	@Test	//test Shimmer 2R stop streaming 
	public void test006_StopStreaming() throws Exception {
			shimmer.stopStreaming();
			Thread.sleep(DELAY_DURATION_MS);
			if (shimmer.isStreaming()) {
				assert(false);
			} else {
				assert(true);
			}
		}
	
	
	@Test	//test for multiple start and stop streaming 
	public void test007_MultipleStartStopStreaming() throws Exception {
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
	public void test008_Disconnect() {
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
