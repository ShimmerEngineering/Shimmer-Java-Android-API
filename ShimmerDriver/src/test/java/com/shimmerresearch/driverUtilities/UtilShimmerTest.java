package com.shimmerresearch.driverUtilities;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilShimmerTest {

	@Test
	public void testRemoveFirstByte() {
		String res = this.getClass().getSimpleName();
		System.out.println(res);
		// Original byte array
        byte[] originalArray = { 10, 20, 30, 40, 50 };
        
        // Expected result after removing the first byte
        byte[] expectedArray = { 20, 30, 40, 50 };
        
        // Call the method to remove the first byte
        byte[] modifiedArray = UtilShimmer.removeFirstByte(originalArray);
        
        // Assert that the modified array matches the expected result
        assertArrayEquals(expectedArray, modifiedArray);
	}
	
	@Test
	public void testRemoveFirstBytes() {
		// Original byte array
        byte[] originalArray = { 10, 20, 30, 40, 50 };
        
        // Expected result after removing the first byte
        byte[] expectedArray = { 30, 40, 50 };
        
        // Call the method to remove the first byte
        byte[] modifiedArray = UtilShimmer.removeFirstBytes(originalArray,2);
        
        // Assert that the modified array matches the expected result
        assertArrayEquals(expectedArray, modifiedArray);
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
	        byte[] joinedArray = UtilShimmer.joinArrays(array1, array2);
	        
	        // Assert that the joined array matches the expected result
	        assertArrayEquals(expectedArray, joinedArray);
	    }

	    @Test
	    public void testJoinArrays_OneEmptyArray() {
	        // Non-empty first array
	        byte[] array1 = {1, 2, 3};
	        
	        // Empty second array
	        byte[] array2 = {};
	        
	        // Call the method to join the arrays
	        byte[] joinedArray = UtilShimmer.joinArrays(array1, array2);
	        
	        // Assert that the joined array is the same as the first array
	        assertArrayEquals(array1, joinedArray);
	    }
	 
}
