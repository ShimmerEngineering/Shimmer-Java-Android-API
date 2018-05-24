package com.shimmerresearch.sensors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.shimmerresearch.driverUtilities.UtilParseData;

public class UnitTestGSR {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void simpleMethodsTest() {
		double gsrkohm = SensorGSR.convertUSiemensTokOhm(2);
		double rawData = SensorGSR.uncalibrateGsrDataTokOhmsUsingAmplifierEq(gsrkohm);
		
		int gsrAdcValueUnCal = ((int)rawData & 4095); 
		
		int	currentGSRRange=(49152 & (int)rawData)>>14; 
		
		double gsrResistanceKOhms = SensorGSR.calibrateGsrDataToKOhmsUsingAmplifierEq(gsrAdcValueUnCal, currentGSRRange);
		gsrResistanceKOhms = SensorGSR.nudgeGsrResistance(gsrResistanceKOhms, currentGSRRange);
		
		double gsrConductanceUSiemens = SensorGSR.convertkOhmToUSiemens(gsrResistanceKOhms);
		System.out.println(gsrConductanceUSiemens);
		
		if (gsrConductanceUSiemens<2.1 && gsrConductanceUSiemens>1.9){
			assert(true);
		}
	}
	
	@Test
	public void simpleTest() {
		for (int i=0;i<1000;i++){
			double newts = i+0.2;
		double gsr = Math.sin(newts/1)+1;
		System.out.println(gsr);
		}
		}

}
