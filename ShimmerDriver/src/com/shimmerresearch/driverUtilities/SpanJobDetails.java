package com.shimmerresearch.driverUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.ShimmerDevice;

/**
 * 
 * @author Mark Nolan
 *
 */
public class SpanJobDetails {
	
	public final static int ERROR_CODES_ID = 9;

	public enum SPAN_JOB_TYPE {
		NONE,
		SPAN_INITIALISE,
		GET_VERSION_INFO,
		SET_CHANNEL,
		GET_CHANNEL,
		ASSIGN_BEST_CHANNELS_TO_SPANS,
		SPECTRUM_ANALYSER_RUN,
		SPECTRUM_ANALYSER_STOP, 
		SPAN_RESET_VIA_BSL;
	}
	
	public static final Map<Integer, String> mMapOfErrorCodes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.NONE), "none");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPAN_INITIALISE), "INITIALISE");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.GET_VERSION_INFO), "GET_VERSION_INFO");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SET_CHANNEL), "SET_CHANNEL");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.GET_CHANNEL), "GET_CHANNEL");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.ASSIGN_BEST_CHANNELS_TO_SPANS), "ASSIGN_BEST_CHANNELS_TO_SPANS");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPECTRUM_ANALYSER_RUN), "SPECTRUM_ANALYSER_RUN");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPECTRUM_ANALYSER_STOP), "SPECTRUM_ANALYSER_STOP");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPAN_RESET_VIA_BSL), "SPAN_RESET_VIA_BSL");

    	mMapOfErrorCodes = Collections.unmodifiableMap(aMap);
    }
    
    public static int getJobErrorCode(SPAN_JOB_TYPE jT){
    	return ((ERROR_CODES_ID*1000) + jT.ordinal()); 
    }
	
	public static enum JobState {
		PENDING,
		INPROGRESS,
		SUCCESS,
		FAIL
	}

	public SPAN_JOB_TYPE currentJob = SPAN_JOB_TYPE.NONE;
	
	List<ShimmerDevice> listofDockedShimmersForJob = new ArrayList<ShimmerDevice>();
	public boolean state = false;
	
	public int channelToSet = -1;

	public SpanJobDetails(SPAN_JOB_TYPE jobType){
		this.currentJob = jobType;
	}

	public SpanJobDetails(SPAN_JOB_TYPE jobType, boolean b) {
		this(jobType);
		state = b;
	}

	public SpanJobDetails(SPAN_JOB_TYPE jobType, int channel) {
		this(jobType);
		this.channelToSet = channel;
	}

	public Object deepClone() {
		// TODO Auto-generated method stub
		return null;
	}

//	public DockJobDetails(JOB_TYPE jobType, List<SlotDetails> listofDockedShimmersForJob){
//		this.currentJob = jobType;
//	}

//	//SET_SHIMMERS_RESET_STATE,
//	public SpanJobDetails(JOB_TYPE jobType, GPIO_STATE gpioState) {
//		this.currentJob = jobType;
//		this.gpioState = gpioState;
//	}
//	
//	//SET_GPIO_OUT_TOGGLE
//	public SpanJobDetails(JOB_TYPE jobType, int count, int milliSecDelay) {
//		this.currentJob = jobType;
//		gpioToggleCount = count;
//		gpioToggleDelay = milliSecDelay;
//	}
//
//	//SET_INDICATOR_LEDS_STATE
//	public SpanJobDetails(JOB_TYPE jobType, List<Integer> displayList, int milliSecDelay) {
//		this.currentJob = jobType;
//		this.ledDisplayList = displayList;
//		this.ledToggleDelay = milliSecDelay;
//	}
//	
//	//ACCESS_SD_CARD
//	public SpanJobDetails(JOB_TYPE jobType, int slotNumber) {
//		this.currentJob = jobType;
//		this.slotNumber = slotNumber;
//	}

}
