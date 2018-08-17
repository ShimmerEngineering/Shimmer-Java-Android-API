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
public class SpanJobDetails extends AbstractErrorCodes {
	
	public final static int ERROR_CODES_ID = 8;

	public enum SPAN_JOB_TYPE {
		NONE,
		SPAN_INITIALISE,
		GET_VERSION_INFO,
		WRITE_LED_STATE,
		SET_RADIO_CONFIG,
		GET_RADIO_CONFIG,
//		ASSIGN_BEST_CHANNELS_TO_SPANS,
		SPECTRUM_ANALYSER_START,
		SPECTRUM_ANALYSER_PROGRESS,
		SPECTRUM_ANALYSER_STOP, 
		BSL_SPAN_RESET, 
		BSL_SPAN_FW_WRITE, 
		CLOSE_EVERYTHING_SAFELY,
		PERFORM_CONNECTION_TEST;
	}
	
	public static final Map<Integer, String> mMapOfErrorCodes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.NONE), "none");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPAN_INITIALISE), "INITIALISE");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.GET_VERSION_INFO), "GET_VERSION_INFO");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.WRITE_LED_STATE), "WRITE_LED_STATE");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SET_RADIO_CONFIG), "SET_RADIO_CONFIG");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.GET_RADIO_CONFIG), "GET_RADIO_CONFIG");
//        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.ASSIGN_BEST_CHANNELS_TO_SPANS), "ASSIGN_BEST_CHANNELS_TO_SPANS");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPECTRUM_ANALYSER_START), "SPECTRUM_ANALYSER_START");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPECTRUM_ANALYSER_PROGRESS), "SPECTRUM_ANALYSER_PROGRESS");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.SPECTRUM_ANALYSER_STOP), "SPECTRUM_ANALYSER_STOP");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.BSL_SPAN_RESET), "SPAN_RESET_VIA_BSL");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.BSL_SPAN_FW_WRITE), "SPAN_FW_WRITE");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.CLOSE_EVERYTHING_SAFELY), "CLOSE_EVERYTHING_SAFELY");
        aMap.put(getJobErrorCode(SPAN_JOB_TYPE.PERFORM_CONNECTION_TEST), "PERFORM_CONNECTION_TEST");
        
    	mMapOfErrorCodes = Collections.unmodifiableMap(aMap);
    }
    
    //TODO add these error codes to the error codes in AbstractGUI
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
	public boolean mStateToSet = false;
	
	public int mRadioChannelToSet = -1;
	public int mRadioGroupIdToSet = -1;

	public SpanJobDetails(SPAN_JOB_TYPE jobType){
		this.currentJob = jobType;
	}

	public SpanJobDetails(SPAN_JOB_TYPE jobType, boolean state) {
		this(jobType);
		mStateToSet = state;
	}

	public SpanJobDetails(SPAN_JOB_TYPE jobType, int channel, int radioGroupId) {
		this(jobType);
		this.mRadioChannelToSet = channel;
		this.mRadioGroupIdToSet = radioGroupId;
	}

}
