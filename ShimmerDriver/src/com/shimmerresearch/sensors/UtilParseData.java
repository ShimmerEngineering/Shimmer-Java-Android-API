package com.shimmerresearch.sensors;

import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class UtilParseData {
	
	private static boolean mIsDebugEnabled = false;

	/**
	 * Converts the raw packet byte values, into the corresponding calibrated and uncalibrated sensor values, the Instruction String determines the output 
	 * @param newPacket a byte array containing the current received packet
	 * @param Instructions an array string containing the commands to execute. It is currently not fully supported
	 * @return
	 */
	public static long parseData(byte[] data, CHANNEL_DATA_TYPE dataType, CHANNEL_DATA_ENDIAN dataEndian){
		long formattedData=0;

		consolePrintLnDebugging("Parsing:\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(data) + "\twith\t" + dataType + "\t&\t" + dataEndian);

		if(data.length<dataType.getNumBytes()){
			consolePrintLnDebugging("Parsing error, not enough bytes");
			return formattedData;
		}
		
		//1) an if statement to change the order of the bytes if required
		if(dataEndian==CHANNEL_DATA_ENDIAN.LSB){
			reverse(data);
			consolePrintLnDebugging("Reversed data:\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(data));
		}
		
		//2) a single for loop looping over the required number of bytes
		long maskToApply = 0;
		for(int i=0;i<dataType.getNumBytes();i++){
			//based on old parseData approach -> seems to be working
			formattedData = (int)((int)(data[i] & 0xFF) | ((int)formattedData << 8));
			//new -> not working properly
//			formattedData = (formattedData << 8)  + data[i];
			//new new -> seems to be working?
//			formattedData = (formattedData << 8)  | (data[i]&0xFF);

			maskToApply = (maskToApply << 8) | 0xFF;
		}
		consolePrintLnDebugging("Mask to apply:\t" + Long.toHexString(maskToApply));
		formattedData &= maskToApply;

		//3) an if statement to calculate the twos complement if required
		if(dataType.isSigned()){
			formattedData=calculatetwoscomplement(formattedData,data.length*8);
		}

		//4) handle special cases like bit shifting for the LSM303
//		if(dataType==CHANNEL_DATA_TYPE.INT12_LBJ){
//			formattedData=formattedData>>4; // shift right by 4 bits
//			formattedData &= 0x0FFF;
//		}

		consolePrintLnDebugging("Parsing result:\t" + formattedData);

		return formattedData;
	}
	
	public static void consolePrintLnDebugging(String stringToPrint){
		if(mIsDebugEnabled){
			System.out.println(stringToPrint);
		}
	}
	
	/**
	 * Converts the raw packet byte values, into the corresponding calibrated and uncalibrated sensor values, the Instruction String determines the output 
	 * @param newPacket a byte array containing the current received packet
	 * @param Instructions an array string containing the commands to execute. It is currently not fully supported
	 * @return
	 */
	@Deprecated // Moving to constant data type declarations rather then declaring strings in multiple classes
	public static long[] parseData(byte[] data, String[] dataType){
		int iData=0;
		long[] formattedData=new long[dataType.length];

		for (int i=0;i<dataType.length;i++)
			if (dataType[i]=="u8") {
				formattedData[i]=(int)0xFF & data[iData];
				iData=iData+1;
			} else if (dataType[i]=="i8") {
				formattedData[i]=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			} else if (dataType[i]=="u12") {

				formattedData[i]=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="i12>") {
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				formattedData[i]=formattedData[i]>>4; // shift right by 4 bits
				iData=iData+2;
			} else if (dataType[i]=="u16") {				
				formattedData[i]=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="u16r") {				
				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData+0] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="i16") {
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				//formattedData[i]=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType[i]=="i16r"){
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				//formattedData[i]=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType[i]=="u24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=xmsb + msb + lsb;
				iData=iData+3;
			}  else if (dataType[i]=="u24") {				
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF));
				formattedData[i]=xmsb + msb + lsb;
				iData=iData+3;
			} else if (dataType[i]=="i24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=calculatetwoscomplement((int)(xmsb + msb + lsb),24);
				iData=iData+3;
			} else if (dataType[i]=="u32signed") {
				//TODO: should this be called i32?
				//TODO: are the indexes incorrect, current '+1' to '+4', should this be '+0' to '+3' the the others listed here?
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				long xxmsb =(((long)data[iData+4] & 0xFF) << 24);
				long xmsb =(((long)data[iData+3] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData[i]=(1-2*offset)*(xxmsb + xmsb + msb + lsb);
				iData=iData+5;
			//TODO: Newly added below up to u72 - check
			} else if (dataType[i]=="u32") {
				long forthmsb =(((long)data[iData+3] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+2] & 0xFF) << 16);
				long msb =(((long)data[iData+1] & 0xFF) << 8);
				long lsb =(((long)data[iData+0] & 0xFF) << 0);
				formattedData[i]=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType[i]=="u32r") {
				long forthmsb =(((long)data[iData+0] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+1] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+3] & 0xFF) << 0);
				formattedData[i]=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType[i]=="i32") {
				long xxmsb =((long)(data[iData+3] & 0xFF) << 24);
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF) << 0);
				formattedData[i]=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType[i]=="i32r") {
				long xxmsb =((long)(data[iData+0] & 0xFF) << 24);
				long xmsb =((long)(data[iData+1] & 0xFF) << 16);
				long msb =((long)(data[iData+2] & 0xFF) << 8);
				long lsb =((long)(data[iData+3] & 0xFF) << 0);
				formattedData[i]=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType[i]=="u72"){
				// do something to parse the 9 byte data
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				
				long eigthmsb =(((long)data[iData+8] & 0x0FL) << 56);
				long seventhmsb =(((long)data[iData+7] & 0xFFL) << 48);
				long sixthmsb =(((long)data[iData+6] & 0xFFL) << 40);
				long fifthmsb =(((long)data[iData+5] & 0xFFL) << 32);
				long forthmsb =(((long)data[iData+4] & 0xFFL) << 24);
				long thirdmsb =(((long)data[iData+3] & 0xFFL) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData[i]=(1-2*offset)*(eigthmsb + seventhmsb + sixthmsb + fifthmsb+ forthmsb+ thirdmsb + msb + lsb);
				iData=iData+9;
			}
		return formattedData;
	}
	
	public static int calculatetwoscomplement(int signedData, int bitLength){
		int newData=signedData;
		if (signedData>=(1<<(bitLength-1))) {
			newData=-((signedData^(int)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}

	public static long calculatetwoscomplement(long signedData, int bitLength){
		long newData=signedData;
		if (signedData>=(1L<<(bitLength-1))) {
			newData=-((signedData^(long)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}
	
	/**
	 * Data Methods
	 */  
	public static int[] formatDataPacketReverse(byte[] data,String[] dataType){
		int iData=0;
		int[] formattedData=new int[dataType.length];

		for (int i=0;i<dataType.length;i++)
			if (dataType[i]=="u8") {
				formattedData[i]=(int)data[iData];
				iData=iData+1;
			}
			else if (dataType[i]=="i8") {
				formattedData[i]=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			}
			else if (dataType[i]=="u12") {

				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8));
				iData=iData+2;
			}
			else if (dataType[i]=="u16") {

				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8));
				iData=iData+2;
			}
			else if (dataType[i]=="i16") {

				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				iData=iData+2;
			}
		return formattedData;
	}
	
	  /**
	   * <p>Reverses the order of the given array.</p>
	   * 
	   * <p>This method does nothing for a <code>null</code> input array.</p>
	   * 
	   * @param array  the array to reverse, may be <code>null</code>
	   */
	  public static void reverse(byte[] array) {
	      if (array == null) {
	          return;
	      }
	      int i = 0;
	      int j = array.length - 1;
	      byte tmp;
	      while (j > i) {
	          tmp = array[j];
	          array[j] = array[i];
	          array[i] = tmp;
	          j--;
	          i++;
	      }
	  }
	
	
}
