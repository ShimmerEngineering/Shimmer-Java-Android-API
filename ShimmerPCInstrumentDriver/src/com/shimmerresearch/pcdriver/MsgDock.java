package com.shimmerresearch.pcdriver;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Mark Nolan
 *
 */
public class MsgDock {

	//callback msg options
	public final static int MSG_ID_BSL_FW_WRITE_SUCCESS = 30;
	public final static int MSG_ID_BSL_FW_WRITE_PROGRESS = 31; 
	public final static int MSG_ID_BSL_FW_WRITE_FAIL = 32;
	public final static int MSG_ID_BSL_FW_WRITE_BUSY = 33;
	public final static int MSG_ID_BSL_FW_WRITE_PROGRESS_LOG = 34;
	public final static int MSG_ID_BSL_FW_WRITE_FINISHED_PER_DOCK = 35;
	public final static int MSG_ID_BSL_FW_WRITE_FINISHED_ALL = 36;
	
	public final static int MSG_ID_SMARTDOCK_SLOT_DOCKED = 40;
	public final static int MSG_ID_SMARTDOCK_SLOT_REMOVED = 41;
	public final static int MSG_ID_SMARTDOCK_NO_STATE = 44;
	public final static int MSG_ID_SMARTDOCK_IS_BUSY = 42;
	public final static int MSG_ID_SMARTDOCK_IS_FREE = 43;
	public final static int MSG_ID_SMARTDOCK_ACTIVE_SLOT_CHANGE = 44;
	public final static int MSG_ID_SMARTDOCK_ERROR = 49; //USED TO INDICATE CASES WHERE THE SHIMMER SHOULD BE REDOCKED
	
	public final static int MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_SUCCESS = 50;
	public final static int MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_ERROR = 58;
	public final static int MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_FAIL = 59;
	public final static int MSG_ID_SHIMMERUART_INFOMEM_WRITE_SUCCESS = 51;
	public final static int MSG_ID_SHIMMERUART_INFOMEM_WRITE_FAIL = 52;
	public final static int MSG_ID_DOCK_INFOMEM_WRITE_FINISHED_PER_DOCK = 53;
	public final static int MSG_ID_DOCK_INFOMEM_WRITE_FINISHED_ALL = 54;
	
	public final static int MSG_ID_DEVICEINFO_CHANGE_IN_SYSTEM_SETTINGS_DETECTED = 80;
	
	public final static int MSG_ID_DOCKMANAGER_INITIALIZED_SUCCESS = 91;
	public final static int MSG_ID_DOCKMANAGER_INITIALIZED_FAIL = 99;
	
	public final static int MSG_ID_DOCK_STATE_CHANGE = 100;
	public final static int MSG_ID_DOCK_SD_SCAN_UPDATE = 101;
	public final static int MSG_ID_DOCK_SD_COPY_UPDATE = 102;
	public final static int MSG_ID_DOCK_WRITE_INFOMEM_UPDATE = 103;
	public final static int MSG_ID_DOCK_SD_SCAN_COMPLETED = 104;
	public final static int MSG_ID_DOCK_SD_COPY_COMPLETED = 105;
	public final static int MSG_ID_DOCK_SD_COPY_FAILED = 106;
	public final static int MSG_ID_DATA_SYNC_UPDATE = 107;
	public final static int MSG_ID_DOCK_SD_CLEAR_UPDATE = 108;
	public final static int MSG_ID_DOCK_SD_SCAN_FAILED = 109;
//	public final static int MSG_ID_DOCK_WRITE_INFOMEM_COMPLETED = 106;
	
	//TODO move closer to SmartDockUart?
	//TODO rename UART to MSG?
//	public final static int MSG_ID_SMARTDOCK_UART_ERR_NONE = 110;
	public final static int MSG_ID_SMARTDOCK_UART_AUTONOTIFY_MESSAGE = 111;
	public final static int MSG_ID_SMARTDOCK_UART_AUTONOTIFY_STATE_ON = 112;
	public final static int MSG_ID_SMARTDOCK_UART_AUTONOTIFY_STATE_OFF = 113;
	public final static int MSG_ID_SMARTDOCK_UART_QUERY_SLOTS = 114;
	public final static int MSG_ID_SMARTDOCK_UART_HWFW_VERSION = 115;
	public final static int MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_WITHOUT_SD = 116;
	public final static int MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_WITH_SD = 117;
	public final static int MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_DISCONNECTED = 118;
	public final static int MSG_ID_SMARTDOCK_UART_BOOT_MESSAGE = 119;
	public final static int MSG_ID_SMARTDOCK_UART_SLOT_MAP_UPDATE = 120;
	public final static int MSG_ID_SMARTDOCK_UART_ERROR = 129;

	
	public final static int MSG_IDENTIFIER_DB_PARSER_UPDATE = 130;

    public static final Map<Integer, String> mMapOfMsgCodes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        aMap.put(MSG_ID_BSL_FW_WRITE_SUCCESS, "MSG_ID_BSL_FW_WRITE_SUCCESS");
        aMap.put(MSG_ID_BSL_FW_WRITE_PROGRESS, "MSG_ID_BSL_FW_WRITE_PROGRESS");
        aMap.put(MSG_ID_BSL_FW_WRITE_FAIL, "MSG_ID_BSL_FW_WRITE_FAIL");
        aMap.put(MSG_ID_BSL_FW_WRITE_BUSY, "MSG_ID_BSL_FW_WRITE_BUSY");
        aMap.put(MSG_ID_BSL_FW_WRITE_PROGRESS_LOG, "MSG_ID_BSL_FW_WRITE_PROGRESS_LOG");
        aMap.put(MSG_ID_BSL_FW_WRITE_FINISHED_PER_DOCK, "MSG_ID_BSL_FW_WRITE_FINISHED_PER_DOCK");
        aMap.put(MSG_ID_BSL_FW_WRITE_FINISHED_ALL, "MSG_ID_BSL_FW_WRITE_FINISHED_ALL");
        aMap.put(MSG_ID_SMARTDOCK_SLOT_DOCKED, "MSG_ID_SMARTDOCK_SLOT_DOCKED");
        aMap.put(MSG_ID_SMARTDOCK_SLOT_REMOVED, "MSG_ID_SMARTDOCK_SLOT_REMOVED");
        aMap.put(MSG_ID_SMARTDOCK_NO_STATE, "MSG_ID_SMARTDOCK_NO_STATE");
        aMap.put(MSG_ID_SMARTDOCK_IS_BUSY, "MSG_ID_SMARTDOCK_IS_BUSY");
        aMap.put(MSG_ID_SMARTDOCK_IS_FREE, "MSG_ID_SMARTDOCK_IS_FREE");
        aMap.put(MSG_ID_SMARTDOCK_ACTIVE_SLOT_CHANGE, "MSG_ID_SMARTDOCK_ACTIVE_SLOT_CHANGE");
        aMap.put(MSG_ID_SMARTDOCK_ERROR, "MSG_ID_SMARTDOCK_ERROR");
        aMap.put(MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_SUCCESS, "MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_SUCCESS");
        aMap.put(MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_ERROR, "MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_ERROR");
        aMap.put(MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_FAIL, "MSG_ID_SHIMMERUART_READ_SHIMMER_DETAILS_FAIL");
        aMap.put(MSG_ID_SHIMMERUART_INFOMEM_WRITE_SUCCESS, "MSG_ID_SHIMMERUART_INFOMEM_WRITE_SUCCESS");
        aMap.put(MSG_ID_SHIMMERUART_INFOMEM_WRITE_FAIL, "MSG_ID_SHIMMERUART_INFOMEM_WRITE_FAIL");
        aMap.put(MSG_ID_DOCK_INFOMEM_WRITE_FINISHED_PER_DOCK, "MSG_ID_DOCK_INFOMEM_WRITE_FINISHED_PER_DOCK");
        aMap.put(MSG_ID_DOCK_INFOMEM_WRITE_FINISHED_ALL, "MSG_ID_DOCK_INFOMEM_WRITE_FINISHED_ALL");
        aMap.put(MSG_ID_DEVICEINFO_CHANGE_IN_SYSTEM_SETTINGS_DETECTED, "MSG_ID_DEVICEINFO_CHANGE_IN_SYSTEM_SETTINGS_DETECTED");
        aMap.put(MSG_ID_DOCKMANAGER_INITIALIZED_SUCCESS, "MSG_ID_DOCKMANAGER_INITIALIZED_SUCCESS");
        aMap.put(MSG_ID_DOCKMANAGER_INITIALIZED_FAIL, "MSG_ID_DOCKMANAGER_INITIALIZED_FAIL");
        aMap.put(MSG_ID_DOCK_STATE_CHANGE, "MSG_ID_DOCK_STATE_CHANGE");
        aMap.put(MSG_ID_DOCK_SD_SCAN_UPDATE, "MSG_ID_DOCK_SD_SCAN_UPDATE");
        aMap.put(MSG_ID_DOCK_SD_COPY_UPDATE, "MSG_ID_DOCK_SD_COPY_UPDATE");
        aMap.put(MSG_ID_DOCK_WRITE_INFOMEM_UPDATE, "MSG_ID_DOCK_WRITE_INFOMEM_UPDATE");
        aMap.put(MSG_ID_DOCK_SD_SCAN_COMPLETED, "MSG_ID_DOCK_SD_SCAN_COMPLETED");
        aMap.put(MSG_ID_DOCK_SD_COPY_COMPLETED, "MSG_ID_DOCK_SD_COPY_COMPLETED");
        aMap.put(MSG_ID_DOCK_SD_COPY_FAILED, "MSG_ID_DOCK_SD_COPY_FAILED");
        aMap.put(MSG_ID_DATA_SYNC_UPDATE, "MSG_ID_DATA_SYNC_UPDATE");
        
//        aMap.put(MSG_ID_SMARTDOCK_UART_ERR_NONE, "MSG_ID_SMARTDOCK_UART_ERR_NONE");
        aMap.put(MSG_ID_SMARTDOCK_UART_AUTONOTIFY_MESSAGE, "MSG_ID_SMARTDOCK_UART_AUTONOTIFY_MESSAGE");
        aMap.put(MSG_ID_SMARTDOCK_UART_AUTONOTIFY_STATE_ON, "MSG_ID_SMARTDOCK_UART_AUTONOTIFY_STATE_ON");
        aMap.put(MSG_ID_SMARTDOCK_UART_AUTONOTIFY_STATE_OFF, "MSG_ID_SMARTDOCK_UART_AUTONOTIFY_STATE_OFF");
        aMap.put(MSG_ID_SMARTDOCK_UART_QUERY_SLOTS, "MSG_ID_SMARTDOCK_UART_QUERY_SLOTS");
        aMap.put(MSG_ID_SMARTDOCK_UART_HWFW_VERSION, "MSG_ID_SMARTDOCK_UART_HWFW_VERSION");
        aMap.put(MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_WITHOUT_SD, "MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_WITHOUT_SD");
        aMap.put(MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_WITH_SD, "MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_WITH_SD");
        aMap.put(MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_DISCONNECTED, "MSG_ID_SMARTDOCK_UART_ACTIVE_SLOT_DISCONNECTED");
        aMap.put(MSG_ID_SMARTDOCK_UART_BOOT_MESSAGE, "MSG_ID_SMARTDOCK_UART_BOOT_MESSAGE");
        aMap.put(MSG_ID_SMARTDOCK_UART_SLOT_MAP_UPDATE, "MSG_ID_SMARTDOCK_UART_SLOT_MAP_UPDATE");
        aMap.put(MSG_ID_SMARTDOCK_UART_ERROR, "MSG_ID_SMARTDOCK_UART_ERROR");
    	mMapOfMsgCodes = Collections.unmodifiableMap(aMap);
    }
	
	
	public int mMessageIdentifier = -1; //identifies what type of dock this is 0 for basic 1 for advance
	public int mSlotNumber = -1; //only applicable for the smart dock
	public String mUniqueID = "";
	public String mBSLComPort = "";
	public String mUARTComPort = "";
	public String mDockID = "";
	public int mDockState = 0;
	
	public int mFwImageTotalSize = 0;
	public int mFwImageWriteProgress = 0;
	public float mFwImageWriteSpeed = 0;
	/**
	 * Used for BSL progress reporting and lists SmartDock UART responses
	 */
	public String mMessage = "";
	public double mValue;
	
	public int mErrorCode;
	public int mLowLevelErrorCode;
	public String mExceptionMsg;
	
	public byte[] mSlotMap = new byte[]{};
	
	
	/**SmartDockActiveSlotDetails
	 * @see SmartDockActiveSlotDetails
	 */
	public int mConnectionType = 0;
	
//	public MsgDock(){
//	}

	/** Used exclusively by the DockManager class for messages associated with no specific slot or dock 
	 * @param msgIdentifier
	 */
	public MsgDock(int msgIdentifier){
		mMessageIdentifier = msgIdentifier;
	}
	
	/** Used by MspBsl with uniqueID, SmartDockUartListener with dockID
	 * @param msgIdentifier
	 * @param uniqueID
	 */
	public MsgDock(int msgIdentifier,String iD){
		mMessageIdentifier = msgIdentifier;
		mUniqueID = iD;
		String[] subString = iD.split("\\.");
		mDockID  = subString[0]+"."+subString[1];
		if(subString.length>2) {
			mSlotNumber = Integer.parseInt(subString[2]);
		}
		else {
			mSlotNumber = -1;
		}
	}
	
	/** Used by BasicDock, SmartDock and SmartDockUart
	 * @param msgIdentifier
	 * @param dockID
	 * @param slotIdentifier
	 */
	public MsgDock(int msgIdentifier,String dockID,int slotIdentifier){
		mMessageIdentifier = msgIdentifier;
		mSlotNumber = slotIdentifier;
		mDockID = dockID;
		mUniqueID = mDockID + "." + String.format("%02d",mSlotNumber);
	}	
	
}
