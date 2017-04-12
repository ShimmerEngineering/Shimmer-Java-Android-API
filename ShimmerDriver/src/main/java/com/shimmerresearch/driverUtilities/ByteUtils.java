package com.shimmerresearch.driverUtilities;

import java.nio.ByteBuffer;

public class ByteUtils {
	//Java8
    //private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);    
	private static ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/8);    

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
}