package com.shimmerresearch.driver;

import java.io.Serializable;

public class ChannelDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2662151922286820989L;

	public class ChannelDataType {
		public static final String UINT8 = "unt8";
		public static final String UINT12 = "uint12";
		public static final String UINT16 = "uint16";
		public static final String UINT24 = "int24";
		public static final String UINT32 = "uint32";
		
		public static final String INT16 = "int16";
		public static final String INT24 = "int24";
		public static final String INT32 = "int32";
	}

//	public enum ChannelDataType {
//		UINT8,
//		UINT12,
//		UINT16,
//		UINT24,
//		UINT32,
//		INT16,
//		INT24,
//		INT32
//	}
	
	public class ChannelDataEndian {
		public static final String LSB = "LSB";
		public static final String MSB = "MSB";
	}
	
	public String mChannel = "";
	public String mChannelDataType = "";
	public int mNumBytes = 0;
	public String mChannelType = "";

	public ChannelDetails(String channel, String channelDataType, int numBytes, String channelType){
		mChannel = channel;
		mChannelDataType = channelDataType;
		mNumBytes = numBytes;
		mChannelType = channelType;
	}
	
}
