package com.shimmerresearch.comms.wiredProtocol;

import com.shimmerresearch.driverUtilities.UtilShimmer;

public class ShimmerCrc {
	
    /** Calculate the CRC per byte
	 * @param crc the start CRC value
	 * @param b the byte to calculate the CRC on
	 * @return the new CRC value
	 */
	protected static int shimmerUartCrcByte(int crc, byte b) {
    	crc &= 0xFFFF;
        crc = ((crc & 0xFFFF) >>> 8) | ((crc & 0xFFFF) << 8);
        crc ^= (b&0xFF);
        crc ^= (crc & 0xFF) >>> 4;
        crc ^= crc << 12;
        crc ^= (crc & 0xFF) << 5;
    	crc &= 0xFFFF;
        return crc;
    }

    /** Calculate the CRC for a byte array. array[0] is CRC LSB, array[1] is CRC MSB 
	 * @param msg the input byte array
	 * @param len the length of the byte array to calculate the CRC on
	 * @return the calculated CRC value
	 */
	public static byte[] shimmerUartCrcCalc(byte[] msg, int len) {
        int CRC_INIT = 0xB0CA;
        int crcCalc;
        int i;

        crcCalc = shimmerUartCrcByte(CRC_INIT, msg[0]);
        for (i = 1; i < len; i++) {
            crcCalc = shimmerUartCrcByte(crcCalc, (msg[i]));
        }
        if (len % 2 > 0) {
            crcCalc = shimmerUartCrcByte(crcCalc, (byte)0x00);
        }
        
        byte[] crcCalcArray = new byte[2];
        crcCalcArray[0] = (byte)(crcCalc & 0xFF);  // CRC LSB
        crcCalcArray[1] = (byte)((crcCalc >> 8) & 0xFF); // CRC MSB 
        
        return crcCalcArray;
    }

    /** Check the CRC stored at the end of the byte array 
	 * @param msg the input byte array
	 * @return a boolean value value, true if CRC matches and false if CRC doesn't match
	 */
	public static boolean shimmerUartCrcCheck(byte[] msg) {
        byte[] crc = shimmerUartCrcCalc(msg, msg.length - 2);
        
        if ((crc[0] == msg[msg.length - 2])
        		&& (crc[1] == msg[msg.length - 1]))
            return true;
        else
            return false;
    }	

	
	//Testing
	public static void main(String[] args) {
		
		byte[] testPacket = new byte[] {
				(byte) 0x00,
				(byte) 0xb6, (byte) 0xf8, (byte) 0xbb,
				(byte) 0xff, (byte) 0x80, (byte) 0x00, (byte) 0x01, (byte) 0x80, (byte) 0x00, (byte) 0x01,
				(byte) 0xff, (byte) 0x80, (byte) 0x00, (byte) 0x01, (byte) 0x80, (byte) 0x00, (byte) 0x01,
				(byte) 0x9a, (byte) 0xb0
		};
		System.out.println("Valid CRC?:\t" + shimmerUartCrcCheck(testPacket));
		
		System.out.println("CRC Bytes should be [LSB MSB]:\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(shimmerUartCrcCalc(testPacket, testPacket.length-2)));
	}
	
}
