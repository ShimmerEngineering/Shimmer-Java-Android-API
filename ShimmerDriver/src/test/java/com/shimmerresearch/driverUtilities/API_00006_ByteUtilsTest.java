package com.shimmerresearch.driverUtilities;

import static org.junit.Assert.*;

import java.nio.ByteOrder;
import java.util.logging.Logger;

import org.junit.Test;

public class API_00006_ByteUtilsTest {

	 private static final Logger logger = Logger.getLogger(API_00006_ByteUtilsTest.class.getName());

		
		@Test
		public void testRemoveFirstByte() {
			// Original byte array
	        byte[] originalArray = { 10, 20, 30, 40, 50 };
	        
	        // Expected result after removing the first byte
	        byte[] expectedArray = { 20, 30, 40, 50 };
	        
	        // Call the method to remove the first byte
	        byte[] modifiedArray = ByteUtils.removeFirstByte(originalArray);
	        
	        // Assert that the modified array matches the expected result
	        assertArrayEquals(expectedArray, modifiedArray);
	        logger.info("Executing testRemoveFirstByte -> PASS");
		}
		
		@Test
		public void testRemoveFirstBytes() {
			// Original byte array
	        byte[] originalArray = { 10, 20, 30, 40, 50 };
	        
	        // Expected result after removing the first byte
	        byte[] expectedArray = { 30, 40, 50 };
	        
	        // Call the method to remove the first byte
	        byte[] modifiedArray = ByteUtils.removeFirstBytes(originalArray,2);
	        
	        // Assert that the modified array matches the expected result
	        assertArrayEquals(expectedArray, modifiedArray);
	        logger.info("Executing testRemoveFirstBytes -> PASS");
		}
		
		 @Test
		    public void testJoinArrays() {
		        // First array
		        byte[] array1 = {1, 2, 3};
		        
		        // Second array
		        byte[] array2 = {4, 5, 6};
		        
		        // Expected result after joining the arrays
		        byte[] expectedArray = {1, 2, 3, 4, 5, 6};
		        
		        // Call the method to join the arrays
		        byte[] joinedArray = ByteUtils.joinArrays(array1, array2);
		        
		        // Assert that the joined array matches the expected result
		        assertArrayEquals(expectedArray, joinedArray);
		        logger.info("Executing testJoinArrays -> PASS");
		    }

		    @Test
		    public void testJoinArrays_OneEmptyArray() {
		        // Non-empty first array
		        byte[] array1 = {1, 2, 3};
		        
		        // Empty second array
		        byte[] array2 = {};
		        
		        // Call the method to join the arrays
		        byte[] joinedArray = ByteUtils.joinArrays(array1, array2);
		        
		        // Assert that the joined array is the same as the first array
		        assertArrayEquals(array1, joinedArray);
		        logger.info("Executing testJoinArrays_OneEmptyArray -> PASS");
		    }
		 
	
	 @Test
	    public void testIntToByteArrayBigEndian() {
	        int value = 123456789;
	        byte[] expected = {(byte)0x7, (byte)0x5B, (byte) 0xCD, (byte)0x15};
	        byte[] result = ByteUtils.intToByteArray(value, ByteOrder.BIG_ENDIAN);
	        assertArrayEquals(expected, result);

	        logger.info("Executing testIntToByteArrayBigEndian -> PASS");
	    }
	    
	    @Test
	    public void testIntToByteArrayLittleEndian() {
	        int value = 123456789;
	        byte[] expected = {(byte)0x15, (byte)0xCD, (byte) 0x5B, (byte)0x7};
	        byte[] result = ByteUtils.intToByteArray(value, ByteOrder.LITTLE_ENDIAN);
	        assertArrayEquals(expected, result);

	        logger.info("Executing testIntToByteArrayLittleEndian -> PASS");
	    }
	 
}
