package com.shimmerresearch.pcdriver;

public class MsgDock {

	//callback msg options
	public final static int MSG_IDENTIFIER_BSL_FW_WRITE_SUCCESS = 30;
	public final static int MSG_IDENTIFIER_BSL_WRITE_PROGRESS = 31; 
	public final static int MSG_IDENTIFIER_BSL_FW_WRITE_FAIL = 32;
	public final static int MSG_IDENTIFIER_BSL_FW_WRITE_BUSY = 33;
	public final static int MSG_IDENTIFIER_BSL_WRITE_PROGRESS_LOG = 34;
	
	public final static int MSG_IDENTIFIER_SMARTDOCK_SLOT_DOCKED = 40;
	public final static int MSG_IDENTIFIER_SMARTDOCK_SLOT_REMOVED = 41;
	public final static int MSG_IDENTIFIER_SMARTDOCK_NO_STATE = 44;
	public final static int MSG_IDENTIFIER_SMARTDOCK_IS_BUSY = 42;
	public final static int MSG_IDENTIFIER_SMARTDOCK_IS_FREE = 43;
	public final static int MSG_IDENTIFIER_SMARTDOCK_ERROR = 49; //USED TO INDICATE CASES WHERE THE SHIMMER SHOULD BE REDOCKED
	
	public final static int MSG_IDENTIFIER_SHIMMERUART_READ_SHIMMER_DETAILS_SUCCESS = 50;
	public final static int MSG_IDENTIFIER_SHIMMERUART_INFOMEM_WRITE_SUCCESS = 51;
//	public final static int MSG_IDENTIFIER_SHIMMERUART_INFOMEM_WRITE_PROGRESS = 52;
	public final static int MSG_IDENTIFIER_SHIMMERUART_INFOMEM_WRITE_FAIL = 53;
	public final static int MSG_IDENTIFIER_SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE = 54;
	public final static int MSG_IDENTIFIER_SHIMMERUART_INFOMEM_READ_REQEST_EXCEEDS_INFO_RANGE = 55;
	public final static int MSG_IDENTIFIER_SHIMMERUART_READ_SHIMMER_DETAILS_ERROR = 58;
	public final static int MSG_IDENTIFIER_SHIMMERUART_READ_SHIMMER_DETAILS_FAIL = 59;
	

	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_NONE = 60;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_PORT_EXCEPTION = 61;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_TIMEOUT = 62;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_CRC = 63;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_PACKAGE_FORMAT = 64;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_WRITING_DATA = 65;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_READING_DATA = 66;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_RESPONSE_BAD_CMD = 67;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_RESPONSE_BAD_ARG = 68;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_RESPONSE_BAD_CRC = 69;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_RESPONSE_UNEXPECTED = 70;
	public final static int MSG_IDENTIFIER_SHIMMERUART_COMM_ERR_MESSAGE_CONTENTS = 71;

	public final static int MSG_IDENTIFIER_DEVICEINFO_CHANGE_IN_SYSTEM_SETTINGS_DETECTED = 80;
	
	public final static int MSG_IDENTIFIER_DOCKMANAGER_INITIALIZED_SUCCESS = 91;
	public final static int MSG_IDENTIFIER_DOCKMANAGER_INITIALIZED_FAIL = 99;
	
	public final static int MSG_IDENTIFIER_DOCK_STATE_CHANGE = 100;
	public final static int MSG_IDENTIFIER_DOCK_SD_SCAN_UPDATE = 101;
	public final static int MSG_IDENTIFIER_DOCK_SD_COPY_UPDATE = 102;
	public final static int MSG_IDENTIFIER_DOCK_WRITE_INFOMEM_UPDATE = 103;
	public final static int MSG_IDENTIFIER_DOCK_SD_SCAN_COMPLETED = 104;
	public final static int MSG_IDENTIFIER_DOCK_SD_COPY_COMPLETED = 105;
	public final static int MSG_IDENTIFIER_DOCK_SD_COPY_FAILED = 106;
	public final static int MSG_IDENTIFIER_DATA_SYNC_UPDATE = 107;
//	public final static int MSG_IDENTIFIER_DOCK_WRITE_INFOMEM_COMPLETED = 106;
	
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_ERR_NONE = 110;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_AUTONOTIFY_MESSAGE = 111;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_AUTONOTIFY_STATE_ON = 112;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_AUTONOTIFY_STATE_OFF = 113;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_QUERY_SLOTS = 114;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_HWFW_VERSION = 115;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_ACTIVE_SLOT_WITHOUT_SD = 116;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_ACTIVE_SLOT_WITH_SD = 117;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_ACTIVE_SLOT_DISCONNECTED = 118;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_BOOT_MESSAGE = 119;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_SLOT_MAP_UPDATE = 120;

	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_COMM_ERR_WRITING_DATA = 121;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_COMM_ERR_READING_DATA = 122;
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_COMM_ERR_TIMEOUT = 123;
	
	public final static int MSG_IDENTIFIER_SMARTDOCK_UART_ERROR = 129;
	
	public final static int MSG_IDENTIFIER_DB_PARSER_UPDATE = 130;
	
	
	public int mMessageIdentifier = -1; //identifies what type of dock this is 0 for basic 1 for advance
	public int mSlotIdentifier = -1; //only applicable for the smart dock
	public String mUniqueID = "";
	public String mBSLComPort = "";
	public String mUARTComPort = "";
	public String mDockID = "";
	public int mState = 0;
	
	public int mFwImageTotalSize = 0;
	public int mFwImageWriteProgress = 0;
	public float mFwImageWriteSpeed = 0;
	public String mMessage = "";
	public double mValue;
	
	public int mErrorCode;
	public String mErrorMessage; //if needed, should probably have a list of strings each corresponding with an error code  
	
	public byte[] mSlotMap = new byte[]{};
	
	public MsgDock(){
	}
	
	public MsgDock(int msgIdentifier,String uniqueID){
		mMessageIdentifier = msgIdentifier;
		mUniqueID = uniqueID;
	}
	
	public MsgDock(int msgIdentifier,String dockID,int slotIdentifier){
		mMessageIdentifier = msgIdentifier;
		mSlotIdentifier = slotIdentifier;
		mDockID = dockID;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotIdentifier);
	}	
	
}
