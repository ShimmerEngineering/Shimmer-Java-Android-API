package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public static class ChannelDataType {
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
	
	public static class ChannelDataEndian {
		public static final String UNKOWN = "";
		public static final String LSB = "LSB";
		public static final String MSB = "MSB";
	}
	
	public enum CHANNEL_TYPE{
//		RAW("RAW"),
		CAL("CAL"),
		UNCAL("UNCAL");
		
	    private final String text;

	    /**
	     * @param text
	     */
	    private CHANNEL_TYPE(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	public String mGuiName = "";
	public String mObjectClusterName = "";
	public int mChannelId = -1;
	public String mDefaultChannelDataType = ChannelDataType.UNKOWN;
	public int mDefaultNumBytes = 0;
	public String mDefaultChannelDataEndian = ChannelDataEndian.UNKOWN;
	public String mDefaultCalibratedUnits = CHANNEL_UNITS.NO_UNITS;
	public List<CHANNEL_TYPE> mListOfChannelTypes = new ArrayList<CHANNEL_TYPE>();
	
	public boolean mShowWhileStreaming = true;
	public boolean mStoreToDatabase = true;
	
//	public enum CHANNEL_SOURCE{
//	SHIMMER,
//	API
//}
//public CHANNEL_SOURCE mChannelSource = CHANNEL_SOURCE.SHIMMER;


	/**
	 * Holds Channel details for parsing. Experimental feature not used
	 * currently in standard Shimmer operations.
	 * 
	 * @param guiName the String name to assign to the channel 
	 * @param channelDataType the ChannelDataType of the channel
	 * @param numBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String objectClusterName, String guiName, String channelDataType, int numBytes, String channelDataEndian, String units, List<CHANNEL_TYPE> listOfChannelTypes){
		mObjectClusterName = objectClusterName;
		mGuiName = guiName;
		mDefaultChannelDataType = channelDataType;
		mDefaultNumBytes = numBytes;
		mDefaultChannelDataEndian = channelDataEndian;
		mDefaultCalibratedUnits = units;
		mListOfChannelTypes = listOfChannelTypes;
	}
	
	public ChannelDetails(String objectClusterName, String guiName, String channelDataType, int numBytes, String channelDataEndian, String units, List<CHANNEL_TYPE> listOfChannelTypes, boolean showWhileStreaming, boolean storeToDatabase){
		mObjectClusterName = objectClusterName;
		mGuiName = guiName;
		mDefaultChannelDataType = channelDataType;
		mDefaultNumBytes = numBytes;
		mDefaultChannelDataEndian = channelDataEndian;
		mDefaultCalibratedUnits = units;
		mListOfChannelTypes = listOfChannelTypes;
		
		mShowWhileStreaming = showWhileStreaming;
		mStoreToDatabase = storeToDatabase;
	}
	
	/**
	 * Holds Channel details for parsing. Experimental feature not used
	 * currently in standard Shimmer operations.
	 * 
	 * @param guiName the String name to assign to the channel 
	 * @param channelDataType the ChannelDataType of the channel
	 * @param numBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String objectClusterName, String guiName, int channelId, String channelDataType, int numBytes, String channelDataEndian, String units, List<CHANNEL_TYPE> listOfChannelTypes){
		mObjectClusterName = objectClusterName;
		mGuiName = guiName;
		mChannelId = channelId;
		mDefaultChannelDataType = channelDataType;
		mDefaultNumBytes = numBytes;
		mDefaultChannelDataEndian = channelDataEndian;
		mDefaultCalibratedUnits = units;
		mListOfChannelTypes = listOfChannelTypes;
	}

	
	public ChannelDetails(String objectClusterName, String guiName, String units, List<CHANNEL_TYPE> listOfChannelTypes, boolean showWhileStreaming, boolean storeToDatabase) {
		mObjectClusterName = objectClusterName;
		mGuiName = guiName;
		mDefaultCalibratedUnits = units;
		mListOfChannelTypes = listOfChannelTypes;
		
		mShowWhileStreaming = showWhileStreaming;
		mStoreToDatabase = storeToDatabase;
	}
	
	/**
	 * Empty constructor not used in standard Shimmer operations (GQ related). 
	 *  
	 */
	public ChannelDetails() {
		// TODO Auto-generated constructor stub
	}

}
