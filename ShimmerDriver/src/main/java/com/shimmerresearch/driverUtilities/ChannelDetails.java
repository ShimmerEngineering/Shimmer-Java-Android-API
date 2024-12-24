package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

/**
 * Holds Channel details for parsing. Experimental feature not used currently
 * in standard Shimmer operations.
 * 
 * @author Mark Nolan
 *
 */
public class ChannelDetails implements Serializable {
	
	private static final long serialVersionUID = -2662151922286820989L;

	/* Channels are Right bit justified unless otherwise stated */
	public enum CHANNEL_DATA_TYPE{
		UNKOWN(0, 0, false),
		UINT8(8, 1, false),
		UINT12(12, 2, false),
		UINT14(14, 2, false),
		UINT16(16, 2, false),
		UINT24(24, 3, false),
		UINT32(32, 4, false),
//		UINT32_SIGNED(4, true),
		UINT48(48, 6, false),
		UINT64(64, 8, false),
		//TODO UtilParseData.parseData currently can not handle over 64 bits
//		UINT72(72, 9, false),
		
		INT8(8, 1, true),
		INT12(12, 2, true),
		INT14(14, 2, true),
//		INT12_LBJ(2, true), //Left bit justified 
		INT16(16, 2, true),
		INT24(24, 3, true),
		INT32(32, 4, true),
		INT64(64, 8, true);
		//TODO UtilParseData.parseData currently can not handle over 64 bits
//		INT72(72, 9, true); 

		private final int numBits;
		private final int numBytes;
		private final boolean isSigned;

	    /**
	     * @param text
	     */
	    private CHANNEL_DATA_TYPE(int numBits, int numBytes, boolean isSigned) {
	    	this.numBits = numBits;
	        this.numBytes = numBytes;
	        this.isSigned = isSigned;
	    }

	    public int getNumBytes(){
	    	return numBytes;
	    }

	    public int getNumBits(){
	    	return numBits;
	    }

	    public boolean isSigned(){
	    	return isSigned;
	    }
	    
	    public long getMaxVal(){
	    	if(isSigned){
		    	long mask = 0;
		    	for(int i=0;i<numBits-1;i++){
		    		mask|=(0x01<<i);
		    	}
	    		return UtilParseData.calculatetwoscomplement(mask, numBits);
	    	}
	    	else{
	    		return (long) (Math.pow(2, numBits)-1);
	    	}
	    }

	    public long getMinVal(){
	    	if(isSigned){
	    		return UtilParseData.calculatetwoscomplement((0x01<<(numBits-1)), numBits);
	    	}
	    	else{
	    		return 0;
	    	}
	    }
	}
	
	public enum CHANNEL_DATA_ENDIAN{
		UNKOWN,
		LSB,
		MSB
	}
	
	public enum CHANNEL_TYPE{
//		RAW("RAW"),
		CAL("CAL", "Calibrated"),
		UNCAL("UNCAL", "Uncalibrated"),
		DERIVED("DERIVED", "Derived");
		
		private final String shortText;
		private final String longText;

	    /**
	     * @param text
	     */
	    private CHANNEL_TYPE(final String text, final String longText) {
	        this.shortText = text;
	        this.longText = longText;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return shortText;
	    }
	    
	    public String getLongText(){
	    	return longText;
	    }
	}
	
	//TODO switch to using this approach rather then all of the variables below
	//TODO have a list of supported units. Have a second variable list stating which one(s) are currently enabled
//	public class ChannelInfo{
//		CHANNEL_TYPE mChannelType = CHANNEL_TYPE.CAL;
//		String mChannelUnits = CHANNEL_UNITS.NO_UNITS;
//		public ChannelInfo(CHANNEL_TYPE channelType, String channelUnits){
//			mChannelType = channelType;
//			mChannelUnits = channelUnits;
//		}
//	}
//	private List<ChannelInfo> mListOfAvailableChannels = new ArrayList<ChannelInfo>(); 
	
	public String mGuiName = "";
	public String mObjectClusterName = "";
	private String mDatabaseChannelHandle = "";
	public int mChannelId = -1;
	public int mDefaultNumBytes = 0;
	public int mLegacyChannelId = -1;
	public CHANNEL_DATA_TYPE mDefaultChannelDataType = CHANNEL_DATA_TYPE.UNKOWN;
	
	// JC: default means the original signal this channel is derived form, it
	// can be derived from a calibrated/noncalibrated/algorithm source.
	public CHANNEL_DATA_ENDIAN mDefaultChannelDataEndian = CHANNEL_DATA_ENDIAN.UNKOWN;
	
	public String mDefaultUncalUnit = CHANNEL_UNITS.NO_UNITS;
	public String mDefaultCalUnits = CHANNEL_UNITS.NO_UNITS; //deprecate this?

	/** each channel if originates from a packetbytearray/sensorbytearray should have this variable defined, null indicates this channel is created within the API */
	public CHANNEL_TYPE mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL; 
	public List<CHANNEL_TYPE> mListOfChannelTypes = new ArrayList<CHANNEL_TYPE>();

	public boolean mShowWhileStreaming = true;
	public boolean mStoreToDatabase = true;

	//JC: FOR GQ
//	public boolean mIsEnabled = true;
	
	public CHANNEL_SOURCE mChannelSource = CHANNEL_SOURCE.SHIMMER;
	public enum CHANNEL_SOURCE{
		SHIMMER,
		API
	}

	//Mark test code
//	public CHANNEL_AXES mChannelAxes = CHANNEL_AXES.TIME;
	public enum CHANNEL_AXES{
		TIME,
		FREQUENCY,
		VALUE
	}

	/**
	 * Empty constructor not used in standard Shimmer operations (GQ BLE related). 
	 *  
	 */
	public ChannelDetails() {
		// TODO Auto-generated constructor stub
	}
	
	public ChannelDetails(String objectClusterName, String guiName, String defaultCalibratedUnits, List<CHANNEL_TYPE> listOfChannelTypes) {
		mObjectClusterName = objectClusterName;
		mGuiName = guiName;
		mDefaultCalUnits = defaultCalibratedUnits;
		mListOfChannelTypes = listOfChannelTypes;
		setDatabaseChannelHandleFromChannelLabel(objectClusterName);
	}

	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes, 
			String databaseChannelHandle) {
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes);
		setDatabaseChannelHandle(databaseChannelHandle);
	}

	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			CHANNEL_DATA_TYPE defaultChannelDataType, 
			int defaultNumBytes, 
			CHANNEL_DATA_ENDIAN channelDataEndian, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes,
			int legacyChannelId) {
		this(objectClusterName, guiName, databaseChannelHandle, defaultChannelDataType, defaultNumBytes, channelDataEndian, defaultCalibratedUnits, listOfChannelTypes);
		mLegacyChannelId = legacyChannelId;
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
	}
	
	public ChannelDetails(String objectClusterName, 
			String guiName, 
			String databaseChannelHandle, 
			String defaultCalibratedUnits, 
			List<CHANNEL_TYPE> listOfChannelTypes) {
		this(objectClusterName, guiName, defaultCalibratedUnits, listOfChannelTypes, databaseChannelHandle);
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
	}

	public int getLegacyChannelId() {
		return mLegacyChannelId;
	}
	
	public String getChannelObjectClusterName(){
		return mObjectClusterName;
	}

	public String getDatabaseChannelHandle(){
		if(!mDatabaseChannelHandle.isEmpty()){
			return mDatabaseChannelHandle;
		}
		else{
			return getChannelObjectClusterName();
		}
	}

	public void setDatabaseChannelHandle(String databaseChannelHandle){
		mDatabaseChannelHandle = databaseChannelHandle;
	}
	
	private void setDatabaseChannelHandleFromChannelLabel(String objectClusterName) {
		mDatabaseChannelHandle = objectClusterName;
		mDatabaseChannelHandle = mDatabaseChannelHandle.replace(" ", "_");
		mDatabaseChannelHandle = mDatabaseChannelHandle.replace("-", "_");
	}

	public boolean isShowWhileStreaming(){
		return mShowWhileStreaming;
	}
	
	public List<String[]> getListOfChannelSignalsAndFormats(){
		List<String[]> listOfChannelSignalsAndFormats = new ArrayList<String[]>();
		for(CHANNEL_TYPE channelType:mListOfChannelTypes){
			String[] signalProperties = getChannelSignalsAndFormats(channelType);
			listOfChannelSignalsAndFormats.add(signalProperties);
		}
		return listOfChannelSignalsAndFormats;
	}

	public String[] getChannelSignalsAndFormats(CHANNEL_TYPE channelType){
		String[] signalProperties = new String[4];
		signalProperties[0] = mObjectClusterName;
		signalProperties[1] = channelType.toString();
		
		if(channelType==CHANNEL_TYPE.UNCAL){
			signalProperties[2] = mDefaultUncalUnit; 
		}
		else if(channelType==CHANNEL_TYPE.CAL){
			signalProperties[2] = mDefaultCalUnits; 
		}
		
		//TODO
		signalProperties[3] = "";
//		signalProperties[3] = mIsChannelUsingDefaultCal? "*":""; 
		return signalProperties;
	}
	
	public String getChannelNameJoined(CHANNEL_TYPE channelType){
		return UtilShimmer.joinStrings(getChannelSignalsAndFormats(channelType));
	}

	public boolean isStoreToDatabase(){
		return mStoreToDatabase;
	}
	
}
