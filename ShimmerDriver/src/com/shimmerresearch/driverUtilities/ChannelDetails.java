package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

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

	public static final class CHANNEL_DATA_TYPE {
		public static final String UNKOWN = "";
		public static final String UINT8 = "uint8";
		public static final String UINT12 = "uint12";
		public static final String UINT16 = "uint16";
		public static final String UINT24 = "int24";
		public static final String UINT32 = "uint32";
		public static final String UINT32_SIGNED = "uint32Signed"; //??
		public static final String INT8 = "int8";
		public static final String INT12 = "int12";
		public static final String INT16_to_12 = "int16to12"; //??
		public static final String INT16 = "int16";
		public static final String INT24 = "int24";
		public static final String INT32 = "int32";
		public static final String UINT64 = "uint64";
		public static final String UINT72_SIGNED = "uint72Signed"; //??
		public static final String UINT48 = "uint48";
	}
	
	public static final class CHANNEL_DATA_ENDIAN {
		public static final String UNKOWN = "";
		public static final String LSB = "LSB";
		public static final String MSB = "MSB";
	}
	
	public enum CHANNEL_TYPE{
//		RAW("RAW"),
		CAL("CAL"),
		UNCAL("UNCAL"),
		DERIVED("DERIVED");
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
	public String mDatabaseChannelHandle = "";
	public int mChannelId = -1;
	public String mDefaultChannelDataType = CHANNEL_DATA_TYPE.UNKOWN;
	public int mDefaultNumBytes = 0;
	
	//JC: default means the original signal this channel is derived form, it can be derived from a calibrated/noncalibrated/algorithm source.
	public String mDefaultChannelDataEndian = CHANNEL_DATA_ENDIAN.UNKOWN;
	public String mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
	public String mDefaultCalibratedUnits = CHANNEL_UNITS.NO_UNITS; //deprecate this?
	public List<CHANNEL_TYPE> mListOfChannelTypes = new ArrayList<CHANNEL_TYPE>();
	
	public boolean mShowWhileStreaming = true;
	public boolean mStoreToDatabase = true;
	
	//JC: FOR GQ
	public boolean mIsEnabled = true;
	
	public enum CHANNEL_SOURCE{
		SHIMMER,
		API
	}
	public CHANNEL_SOURCE mChannelSource = CHANNEL_SOURCE.SHIMMER;
	//each channel if originates from a packetbytearray/sensorbytearray should have this variable defined, null indicates this channel is created within the API
	public CHANNEL_TYPE mChannelFormatDerivedFromShimmerDataPacket = null; 
	

	/**
	 * Empty constructor not used in standard Shimmer operations (GQ BLE related). 
	 *  
	 */
	public ChannelDetails() {
		// TODO Auto-generated constructor stub
	}
	
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes, 
			boolean showWhileStreaming, 
			boolean storeToDatabase) {
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes);
		
		mShowWhileStreaming = showWhileStreaming;
		mStoreToDatabase = storeToDatabase;
		
		checkDatabaseChannelHandle();
	}
	
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes) {
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes, databaseChannelHandle);
		checkDatabaseChannelHandle();
	}

	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes, 
			boolean showWhileStreaming, 
			boolean storeToDatabase){
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes, databaseChannelHandle);
		
		mShowWhileStreaming = showWhileStreaming;
		mStoreToDatabase = storeToDatabase;
		
		checkDatabaseChannelHandle();
	}
	
	/**
	 * Holds Channel details for parsing. Experimental feature not used
	 * currently in standard Shimmer operations.
	 * 
	 * @param guiName the String name to assign to the channel 
	 * @param defaultChannelDataType the ChannelDataType of the channel
	 * @param numBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			String defaultChannelDataType, 
			int numBytes, 
			String channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes){
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes, databaseChannelHandle);

		mDefaultChannelDataType = defaultChannelDataType;
		mDefaultNumBytes = numBytes;
		mDefaultChannelDataEndian = channelDataEndian;
		
		checkDatabaseChannelHandle();
	}
	
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			String defaultChannelDataType, 
			int numBytes, 
			String channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes, 
			boolean showWhileStreaming, 
			boolean storeToDatabase){
		
		this(objectClusterName,
			guiName,
			databaseChannelHandle,
			defaultChannelDataType,
			numBytes,
			channelDataEndian, 
			defaultCalibratedUnits,
			listOfChannelTypes);
		
		mShowWhileStreaming = showWhileStreaming;
		mStoreToDatabase = storeToDatabase;
	}

	//TODO below not currently used
	/**
	 * Holds Channel details for parsing. Experimental feature not used
	 * currently in standard Shimmer operations.
	 * 
	 * @param guiName the String name to assign to the channel 
	 * @param channelDataType the ChannelDataType of the channel
	 * @param numBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			int channelId, 
			String defaultChannelDataType, 
			int numBytes, 
			String channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes){
		
		this(objectClusterName,
				guiName,
				databaseChannelHandle,
				defaultChannelDataType,
				numBytes,
				channelDataEndian, 
				defaultCalibratedUnits,
				listOfChannelTypes);

		mChannelId = channelId;
		
		checkDatabaseChannelHandle();
	}

	public ChannelDetails(String objectClusterName, String guiName, String defaultCalibratedUnits, List<CHANNEL_TYPE> listOfChannelTypes) {
		mObjectClusterName = objectClusterName;
		mGuiName = guiName;
		mDefaultCalibratedUnits = defaultCalibratedUnits;
		mListOfChannelTypes = listOfChannelTypes;
	}

	public ChannelDetails(String objectClusterName, String guiName, String defaultCalibratedUnits, List<CHANNEL_TYPE> listOfChannelTypes, String databaseChannelHandle) {
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes);
		mDatabaseChannelHandle = databaseChannelHandle;
	}

	private void checkDatabaseChannelHandle(){
		if(mDatabaseChannelHandle.isEmpty()){
			mStoreToDatabase = false;
		}
	}

}
