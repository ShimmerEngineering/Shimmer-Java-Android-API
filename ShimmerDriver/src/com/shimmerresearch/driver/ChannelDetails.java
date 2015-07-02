package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.driver.Configuration.CHANNEL_TYPE;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;

/**
 * Holds Channel details for parsing. Experimental feature not used currently
 * in standard Shimmer operations.
 * 
 * @author Mark Nolan
 *
 */
public class ChannelDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2662151922286820989L;

	public class ChannelDataType {
		public static final String UNKOWN = "";
		public static final String UINT8 = "uint8";
		public static final String UINT12 = "uint12";
		public static final String UINT16 = "uint16";
		public static final String UINT24 = "int24";
		public static final String UINT32 = "uint32";
		public static final String INT16 = "int16";
		public static final String INT24 = "int24";
		public static final String INT32 = "int32";
		public static final String UINT64 = "uint64";
		public static final String UINT48 = "uint48";
	}
	
	public class ChannelDataEndian {
		public static final String UNKOWN = "";
		public static final String LSB = "LSB";
		public static final String MSB = "MSB";
	}
	
	public String mChannelName = "";
//	public String mObjectClusterName = "";
	public String mChannelDataType = ChannelDataType.UNKOWN;
	public int mNumBytes = 0;
	public String mChannelDataEndian = ChannelDataEndian.UNKOWN;
	public String mCalibratedUnits = CHANNEL_UNITS.NO_UNITS;

	/**
	 * Holds Channel details for parsing. Experimental feature not used
	 * currently in standard Shimmer operations.
	 * 
	 * @param channelName the String name to assign to the channel 
	 * @param channelDataType the ChannelDataType of the channel
	 * @param numBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String channelName, String channelDataType, int numBytes, String channelDataEndian, String units){
//	public ChannelDetails(String channelName, String objectClusterName, String channelDataType, int numBytes, String channelDataEndian, String units){
		mChannelName = channelName;
//		mObjectClusterName = objectClusterName;
		mChannelDataType = channelDataType;
		mNumBytes = numBytes;
		mChannelDataEndian = channelDataEndian;
		mCalibratedUnits = units;
	}

	/**
	 * Empty constructor not used in standard Shimmer operations (GQ related). 
	 *  
	 */
	public ChannelDetails() {
		// TODO Auto-generated constructor stub
	}
	
}
