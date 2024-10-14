package com.shimmerresearch.driverUtilities;

import static org.junit.Assert.*;

import org.junit.Test;

public class API_00007_UtilShimmerTest {

	@Test
    public void testRoundZeroDecimalPoints() {
        assertTrue(UtilShimmer.round(5.567, 0) == 6.0);
        assertTrue(UtilShimmer.round(5.444, 0) == 5.0);
    }
    
    @Test
    public void testRoundOneDecimalPoint() {
        assertTrue(UtilShimmer.round(5.567, 1) == 5.6);
        assertTrue(UtilShimmer.round(5.444, 1) == 5.4);
        assertTrue(UtilShimmer.round(-5.567, 1) == -5.6);
        assertTrue(UtilShimmer.round(-5.444, 1) == -5.4);
    }
    
    @Test
    public void testRoundTwoDecimalPoints() {
        assertTrue(UtilShimmer.round(5.567, 2) == 5.57);
        assertTrue(UtilShimmer.round(5.444, 2) == 5.44);
        assertTrue(UtilShimmer.round(-5.567, 2) == -5.57);
        assertTrue(UtilShimmer.round(-5.444, 2) == -5.44);
    }
    
    @Test
    public void testRoundNegativeDecimalPoints() {
        try {
            UtilShimmer.round(5.567, -1);
            assert(false);
        } catch (IllegalArgumentException e) {
        	assert(true);
        }
    }
    
    @Test
    public void testRoundLargeDecimalPoints() {
        assertTrue(UtilShimmer.round(5.567, 5) == 5.56700);
        assertTrue(UtilShimmer.round(5.567, 6) == 5.567000);
    }
    
    @Test
    public void testRoundZeroValue() {
        assertTrue(UtilShimmer.round(0.0, 2) == 0.0);
        assertTrue(UtilShimmer.round(0.0, 0) == 0.0);
    }

    @Test
    public void testRoundVeryLargeValue() {
        assertTrue(UtilShimmer.round(1.0E10 + 0.4, 0) == 1.0E10);
        assertTrue(UtilShimmer.round(1.0E10 - 0.5, 0) == 1.0E10);
    }
}
