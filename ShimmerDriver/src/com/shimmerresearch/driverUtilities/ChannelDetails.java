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
	
	/** * */
	private static final long serialVersionUID = -2662151922286820989L;

	/* Channels are Right bit justified unless otherwise stated */
	public enum CHANNEL_DATA_TYPE{
		UNKOWN(0, false),
		UINT8(8, false),
		UINT12(2, false),
		UINT16(2, false),
		UINT24(3, false),
		UINT32(4, false),
//		UINT32_SIGNED(4, true),
		UINT48(6, false),
		UINT64(8, false),
		
		INT8(1, true),
		INT12(2, true),
		INT12_LBJ(2, true), //Left bit justified
		INT16(2, true),
		INT24(3, true),
		INT32(4, true),
		INT72_SIGNED(9, true);

		private final int numBytes;
		private final boolean isSigned;

	    /**
	     * @param text
	     */
	    private CHANNEL_DATA_TYPE(final int numBytes, boolean isSigned) {
	        this.numBytes = numBytes;
	        this.isSigned = isSigned;
	    }

	    public int getNumBytes(){
	    	return numBytes;
	    }
	    
	    public boolean isSigned(){
	    	return isSigned;
	    }
	}
	
	public enum CHANNEL_DATA_ENDIAN{
		UNKOWN,
		LSB,
		MSB
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
	public int mDefaultNumBytes = 0;
	public CHANNEL_DATA_TYPE mDefaultChannelDataType = CHANNEL_DATA_TYPE.UNKOWN;
	
	// JC: default means the original signal this channel is derived form, it
	// can be derived from a calibrated/noncalibrated/algorithm source.
	public CHANNEL_DATA_ENDIAN mDefaultChannelDataEndian = CHANNEL_DATA_ENDIAN.UNKOWN;
	public String mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
	public String mDefaultCalibratedUnits = CHANNEL_UNITS.NO_UNITS; //deprecate this?
	public List<CHANNEL_TYPE> mListOfChannelTypes = new ArrayList<CHANNEL_TYPE>();
	
	public boolean mShowWhileStreaming = true;
	public boolean mStoreToDatabase = true;
	
	//JC: FOR GQ
//	public boolean mIsEnabled = true;
	
	public enum CHANNEL_SOURCE{
		SHIMMER,
		API
	}
	public CHANNEL_SOURCE mChannelSource = CHANNEL_SOURCE.SHIMMER;
	/** each channel if originates from a packetbytearray/sensorbytearray should have this variable defined, null indicates this channel is created within the API */
	public CHANNEL_TYPE mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL; 

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
	 * @param defaultNumBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			CHANNEL_DATA_TYPE defaultChannelDataType, 
			int defaultNumBytes, 
			CHANNEL_DATA_ENDIAN channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes){
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes, databaseChannelHandle);

		mDefaultChannelDataType = defaultChannelDataType;
		mDefaultNumBytes = defaultNumBytes;
		mDefaultChannelDataEndian = channelDataEndian;
		
		checkDatabaseChannelHandle();
	}
	
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			CHANNEL_DATA_TYPE defaultChannelDataType, 
			int defaultNumBytes, 
			CHANNEL_DATA_ENDIAN channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes, 
			boolean showWhileStreaming, 
			boolean storeToDatabase){
		
		this(objectClusterName,
			guiName,
			databaseChannelHandle,
			defaultChannelDataType,
			defaultNumBytes,
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
	 * @param defaultNumBytes the number of bytes the channel takes up in a data packet
	 * @param channelDataEndian the endianness of the byte order in a data packet
	 */
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			int channelId, 
			CHANNEL_DATA_TYPE defaultChannelDataType, 
			int defaultNumBytes, 
			CHANNEL_DATA_ENDIAN channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes){
		
		this(objectClusterName,
				guiName,
				databaseChannelHandle,
				defaultChannelDataType,
				defaultNumBytes,
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
