package com.shimmerresearch.driverUtilities;
import static org.junit.Assert.*;

import org.junit.Test;

public class API_XXXXX_UtilParseData {
	 @Test
	    public void testSingleByteTypes() {
	        String[] types = {"u8", "i8"};
	        assertEquals(2, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testTwoByteTypes() {
	        String[] types = {"u12", "u14", "i12>", "i12*>", "u16", "u16r", "i16", "i16r"};
	        assertEquals(2 * types.length, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testThreeByteTypes() {
	        String[] types = {"u24", "u24r", "i24r"};
	        assertEquals(9, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testFourByteTypes() {
	        String[] types = {"u32", "u32r", "i32", "i32r"};
	        assertEquals(4 * types.length, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testFiveByteType() {
	        String[] types = {"u32signed"};
	        assertEquals(5, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testNineByteType() {
	        String[] types = {"u72"};
	        assertEquals(9, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testMixedTypes() {
	        String[] types = {"u8", "i8", "u16", "u24", "u32signed", "u72"};
	        int expected = 1 + 1 + 2 + 3 + 5 + 9;
	        assertEquals(expected, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test
	    public void testEmptyInput() {
	        String[] types = {};
	        assertEquals(0, UtilParseData.countBytesFromDataTypes(types));
	    }

	    @Test(expected = IllegalArgumentException.class)
	    public void testUnknownTypeThrows() {
	        String[] types = {"u8", "invalidType"};
	        UtilParseData.countBytesFromDataTypes(types);
	    }
}
