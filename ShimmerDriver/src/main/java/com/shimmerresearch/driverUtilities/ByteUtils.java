package com.shimmerresearch.driverUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtils {
	//Java8
    //private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);    
//	private static ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/8);   
	
	//Above can't be static as they'll be used in multiple places in code.
	private static int longSize = Long.SIZE / Byte.SIZE;
	private static int shortSize = Long.SIZE / Byte.SIZE;

    public static byte[] longToBytes(long x) {
    	ByteBuffer buffer = ByteBuffer.allocate(longSize);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
    	ByteBuffer buffer = ByteBuffer.allocate(longSize);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }

	public static long bytesToLong(byte[] bytes, ByteOrder byteOrder) {
    	ByteBuffer buffer = ByteBuffer.allocate(longSize);    
    	buffer.order(byteOrder);
        buffer.put(bytes, 0, bytes.length);
        return buffer.getLong();
	}

    public static byte[] shortToBytes(short x, ByteOrder byteOrder) {
    	ByteBuffer bufferShort = ByteBuffer.allocate(shortSize);    
    	bufferShort.putShort(0, x).order(byteOrder);
        return bufferShort.array();
    }

    public static short bytesToShort(byte[] bytes, ByteOrder byteOrder) {
    	ByteBuffer bufferShort = ByteBuffer.allocate(2);    
    	bufferShort.order(byteOrder);
    	bufferShort.put(bytes);
        return bufferShort.getShort(0);
    }
    
    public static byte[] intToByteArray(int value, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(byteOrder);
        buffer.putInt(value);
        return buffer.array();
    }

}