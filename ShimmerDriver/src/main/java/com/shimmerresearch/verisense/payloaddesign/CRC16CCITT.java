package com.shimmerresearch.verisense.payloaddesign;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/******************************************************************************
 *  Compilation:  javac CRC16CCITT.java
 *  Execution:    java CRC16CCITT s
 *  Dependencies: 
 *  
 *  Reads in a sequence of bytes and prints out its 16 bit
 *  Cylcic Redundancy Check (CRC-CCIIT 0xFFFF).
 *
 *  1 + x + x^5 + x^12 + x^16 is irreducible polynomial.
 *
 *  % java CRC16-CCITT 123456789
 *  CRC16-CCITT = 29b1
 *  
 *  Modified by: Mark Nolan (02/08/2018)
 *
 ******************************************************************************/

public class CRC16CCITT { 
	
	public int initialValue = 0xFFFF;	// initial value
    public int polynomial = 0x1021;		// 0001 0000 0010 0001  (0, 5, 12) 

    public byte[] computeCrc(byte[] bytes, int length, ByteOrder byteOrder) { 
    	int crc = computeCrc(bytes, length);

    	byte[] crcBuf = new byte[2];
    	if(byteOrder==ByteOrder.LITTLE_ENDIAN) {
        	crcBuf[0] = (byte) (crc&0xFF);
        	crcBuf[1] = (byte) ((crc>>8)&0xFF);
    	} else {
    		
    	}
    	return crcBuf;
    }
    
    public int computeCrc(byte[] bytes) {
    	return computeCrc(bytes, bytes.length);
	}

    public int computeCrc(byte[] bytes, int length) { 
        int crc = initialValue;          // initial value

        for (int x=0;x<length;x++) {
        	byte b = bytes[x];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        return crc;
    }

    public boolean checkCrc(byte[] payload, int crcOriginal) {
    	int crcComputed = computeCrc(payload);
    	return (crcOriginal==crcComputed? true:false);
    }

	public int getCrcFromEndOfBuffer(byte[] bytes) {
    	byte[] crcOriginalBuf = new byte[2];
    	System.arraycopy(bytes, bytes.length-2, crcOriginalBuf, 0, 2);
    	return crcBytesToInt(crcOriginalBuf);
	}

    public int crcBytesToInt(byte[] byteArray) {
    	int crc = (ByteBuffer.wrap(byteArray).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF);
		return crc;
	}

}