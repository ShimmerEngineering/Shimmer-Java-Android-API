package com.shimmerresearch.driverUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.DockJobDetails.DOCK_JOB_TYPE;

/**
 * 
 * @author Mark Nolan
 *
 */
public class DockJobDetails extends AbstractErrorCodes {
	
	public final static int ERROR_CODES_ID = 4;

	public enum DOCK_JOB_TYPE {
		NONE,
		GET_VERSION_INFO,
		GET_AUTO_NOTIFY_STATE,
		SET_AUTO_NOTIFY_STATE,
		SET_BSL_MASK_DOCK,
		GET_BSL_MASK_DOCK,
		SET_BSL_MASK_SHIMMER,
		GET_BSL_MASK_SHIMMER,
		GET_CONNECTED_SLOTS,
		DISCONNECT_ALL_SLOTS,
		SET_ACTIVE_SLOT_WITHOUT_SD,
		SET_ACTIVE_SLOT_WITH_SD,
		GET_ACTIVE_SLOT,
		SET_SHIMMERS_RESET_STATE,
		GET_SHIMMERS_RESET_STATE,
		SHIMMER_RESET_ALL,
		SHIMMER_RESET_SINGLE,
		PERFORM_IDENTIFY_SLOT,
		PERFORM_IDENTIFY_DOCK,
		SET_GPIO_STATE,
		GET_GPIO_STATE,
		SET_GPIO_OUT_TOGGLE,
		SET_INDICATOR_LEDS_STATE,
		GET_INDICATOR_LEDS_STATE,
		ACCESS_SD_CARD,
		
		DOCK_BOOT,//Only used during Base FW write
		DOCK_RESET_VIA_FW,
		DOCK_RESET_VIA_BSL,
		
		FW_DOCK, 
		
		// Start of individual Shimmer communication operations
		SHIMMER_WRITE_EXP_BRD_MEMORY,
		SHIMMER_WRITE_DAUGHTER_CARD_ID,

//		READ_SHIMMERS,
//		FW_SHIMMERS,
//		SCAN_SHIMMERS_SD,
//		COPY_SHIMMERS_SD,
//		CONFIG_SHIMMERS,
//		DELETE_SHIMMERS_SD_LOGS
		
		//Experimental
		DOCK_MANAGER_RELOAD,
		DOCK_MANAGER_LOAD,
		DOCK_MANAGER_RETRY_SETUP_DOCK,
		DOCK_MANAGER_RETRY_SETUP_DOCKS,

	}
	
	public static final Map<Integer, String> mMapOfErrorCodes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.NONE), "none");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_VERSION_INFO), "Dock_Cmd_Version_Info_Get");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_AUTO_NOTIFY_STATE), "Can't read the SmartDock auto-notify enable setting");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_AUTO_NOTIFY_STATE), "Can't set the SmartDock auto-notify enable setting");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_BSL_MASK_DOCK), "BSL_MASK_DOCK_SET");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_BSL_MASK_DOCK), "BSL_MASK_DOCK_GET");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_BSL_MASK_SHIMMER), "BSL_MASK_SHIMMER_SET");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_BSL_MASK_SHIMMER), "BSL_MASK_SHIMMER_GET");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_CONNECTED_SLOTS), "Dock_Cmd_Query_Connected_Slots");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DISCONNECT_ALL_SLOTS), "Dock_cmd_Slots_Disconnect");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_ACTIVE_SLOT_WITHOUT_SD), "Fail to set the active slot");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_ACTIVE_SLOT_WITH_SD), "Fail to set the active slot");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_ACTIVE_SLOT), "Can't read the dock position");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_SHIMMERS_RESET_STATE), "SHIMMER_RESET_SET");        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_SHIMMERS_RESET_STATE), "SHIMMER_RESET_GET");        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SHIMMER_RESET_ALL), "PERFORM_SHIMMER_RESET_ALL");        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SHIMMER_RESET_SINGLE), "PERFORM_SHIMMER_RESET_SINGLE");    
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.PERFORM_IDENTIFY_SLOT), "PERFORM_IDENTIFY_SLOT");    
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.PERFORM_IDENTIFY_DOCK), "PERFORM_IDENTIFY_DOCK");    
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_GPIO_STATE), "SHIMMER_GPIO_SET");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_GPIO_STATE), "SHIMMER_GPIO_GET");        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_GPIO_OUT_TOGGLE), "SET_GPIO_OUT_TOGGLE");        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SET_INDICATOR_LEDS_STATE), "INDICATOR_LEDS_SET");        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.GET_INDICATOR_LEDS_STATE), "INDICATOR_LEDS_GET"); 
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.ACCESS_SD_CARD), "ACCESS_SD_CARD"); 
        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.FW_DOCK), "FW_DOCK");

        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.SHIMMER_WRITE_EXP_BRD_MEMORY), "SHIMMER_WRITE_EXP"); 

        
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_BOOT), "Fail to detect bootup");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_RESET_VIA_FW), "Failed to reset Base");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_RESET_VIA_BSL), "Failed to reset Base");

		//Experimental
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_MANAGER_RELOAD), "DOCK_MANAGER_RELOAD");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_MANAGER_LOAD), "DOCK_MANAGER_LOAD");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_MANAGER_RETRY_SETUP_DOCK), "DOCK_MANAGER_RETRY_SETUP_DOCK");
        aMap.put(getJobErrorCode(DOCK_JOB_TYPE.DOCK_MANAGER_RETRY_SETUP_DOCKS), "DOCK_MANAGER_RETRY_SETUP_DOCKS");
        

    	mMapOfErrorCodes = Collections.unmodifiableMap(aMap);
    }
    
	public enum GPIO_STATE{
		UNKNOWN,
		GPIO_IN,
		GPIO_OUT_HIGH,
		GPIO_OUT_LOW,
	}
	
    public static int getJobErrorCode(DOCK_JOB_TYPE jT){
    	return ((ERROR_CODES_ID*1000) + jT.ordinal()); 
    }
	
	public static enum JobState {
		PENDING,
		INPROGRESS,
		SUCCESS,
		FAIL
	}

	public DOCK_JOB_TYPE currentJob = DOCK_JOB_TYPE.NONE;
	
	List<ShimmerDevice> listofDockedShimmersForJob = new ArrayList<ShimmerDevice>();
	public boolean state = false;
	
	//SET_GPIO_STATE
	public GPIO_STATE gpioState = GPIO_STATE.UNKNOWN;
	public int gpioToggleCount = 1;
	public int gpioToggleDelay = 1000;
	
	//SET_INDICATOR_LEDS_STATE
	public List<Integer> ledDisplayList = new ArrayList<Integer>();
	public int ledToggleDelay = 1000;

	//SET_ACTIVE_SLOT_WITHOUT_SD
	//SET_ACTIVE_SLOT_WITH_SD
	public int slotNumber = -1;	

	//DockManager
	public String dockId = "";
	public List<String> listOfDockIds = new ArrayList<String>();
	
	//GET_VERSION_INFO
	//GET_CONNECTED_SLOTS
	//GET_SHIMMERS_RESET_STATE,
	//GET_INDICATOR_LEDS_STATE
	public DockJobDetails(DOCK_JOB_TYPE dockJobType){
		this.currentJob = dockJobType;
		
	}

	//SET_BSL_MASK_DOCK
	//SET_BSL_MASK_SHIMMER
	public DockJobDetails(DOCK_JOB_TYPE jobType, boolean b) {
		this(jobType);
		state = b;
	}

//	public DockJobDetails(JOB_TYPE jobType, List<SlotDetails> listofDockedShimmersForJob){
//		this.currentJob = jobType;
//	}

	//SET_SHIMMERS_RESET_STATE,
	public DockJobDetails(DOCK_JOB_TYPE jobType, GPIO_STATE gpioState) {
		this(jobType);
		this.gpioState = gpioState;
	}
	
	//SET_GPIO_OUT_TOGGLE
	public DockJobDetails(DOCK_JOB_TYPE jobType, int count, int milliSecDelay) {
		this(jobType);
		gpioToggleCount = count;
		gpioToggleDelay = milliSecDelay;
	}

	//SET_INDICATOR_LEDS_STATE
	public DockJobDetails(DOCK_JOB_TYPE jobType, List<Integer> displayList, int milliSecDelay) {
		this(jobType);
		this.ledDisplayList = displayList;
		this.ledToggleDelay = milliSecDelay;
	}
	
	//ACCESS_SD_CARD
	public DockJobDetails(DOCK_JOB_TYPE jobType, int slotNumber) {
		this(jobType);
		this.slotNumber = slotNumber;
	}

	
	//DockManager
	public DockJobDetails(DOCK_JOB_TYPE jobType, String dockId) {
		this(jobType);
		this.dockId = dockId;
	}

	public DockJobDetails(DOCK_JOB_TYPE jobType, List<String> listOfDockIds) {
		this(jobType);
		this.listOfDockIds = listOfDockIds;
	}

}
