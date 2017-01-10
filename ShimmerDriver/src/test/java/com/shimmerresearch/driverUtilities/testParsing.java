package com.shimmerresearch.driverUtilities;

import java.nio.ByteBuffer;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public class testParsing {

	public static void main(String[] args) {
		UtilParseData.mIsDebugEnabled = true;
		
		for(long i=0;i<100000;i+=10001){
//		for(long i=0;i<10;i++){
			byte[] byteArray = new byte[8];
			byteArray = UtilShimmer.convertLongToByteArray(i);
			
			System.out.println(i + "\tByteArray\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(byteArray));

			long parsedChannelData1 = UtilShimmer.convertByteArrayToLong(byteArray);

			byteArray = UtilShimmer.convertLongToByteArray(i);
			long parsedChannelData2 = UtilParseData.parseData(byteArray, CHANNEL_DATA_TYPE.UINT64, CHANNEL_DATA_ENDIAN.MSB);

//			byteArray = UtilShimmer.convertLongToByteArray(i);
//			long parsedChannelData3 = UtilParseData.parseData(byteArray, CHANNEL_DATA_TYPE.INT64, CHANNEL_DATA_ENDIAN.MSB);
//
//			byteArray = UtilShimmer.convertLongToByteArray(i);
//			long parsedChannelData4 = UtilParseData.parseData(byteArray, CHANNEL_DATA_TYPE.UINT64, CHANNEL_DATA_ENDIAN.LSB);
			
			
			System.out.println(parsedChannelData1 + "\t" + parsedChannelData2);// + "\t" + parsedChannelData3 + "\t" + parsedChannelData4);
			System.out.println("");
			
		}
		
		

		for(int i=0;i<100000;i+=10001){
//			for(long i=0;i<10;i++){
			byte[] byteArray = ByteBuffer.allocate(4).putInt(i).array();
				
			System.out.println(i + "\tByteArray\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(byteArray));
			
			byteArray = ByteBuffer.allocate(4).putInt(i).array();
			long parsedChannelData2 = UtilParseData.parseData(byteArray, CHANNEL_DATA_TYPE.UINT32, CHANNEL_DATA_ENDIAN.MSB);
			System.out.println(parsedChannelData2);// + "\t" + parsedChannelData3 + "\t" + parsedChannelData4);
			System.out.println("");
		}
		
	}

}
