package com.shimmerresearch.driverUtilities;

import static org.junit.Assert.*;

import java.nio.ByteOrder;

import org.junit.Test;

public class ByteUtilsTest {

	 @Test
	    public void testIntToByteArrayBigEndian() {
	        int value = 123456789;
	        byte[] expected = {(byte)0x7, (byte)0x5B, (byte) 0xCD, (byte)0x15};
	        byte[] result = ByteUtils.intToByteArray(value, ByteOrder.BIG_ENDIAN);
	        assertArrayEquals(expected, result);
	    }
	    
	    @Test
	    public void testIntToByteArrayLittleEndian() {
	        int value = 123456789;
	        byte[] expected = {(byte)0x15, (byte)0xCD, (byte) 0x5B, (byte)0x7};
	        byte[] result = ByteUtils.intToByteArray(value, ByteOrder.LITTLE_ENDIAN);
	        assertArrayEquals(expected, result);
	    }
	 
}
