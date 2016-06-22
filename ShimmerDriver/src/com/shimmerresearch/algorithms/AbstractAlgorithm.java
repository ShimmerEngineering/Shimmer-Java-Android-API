package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.MsgDock;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;

public abstract class AbstractAlgorithm extends BasicProcessWithCallBack implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 1L;

	public static final double mVersion=1.0;

	public enum FILTERING_OPTION{
		NONE,
		DEFAULT
	};
	
	public enum ALGORITHM_TYPE{
		ALGORITHM_TYPE_CONTINUOUS("ALGORITHM TYPE CONTINUOUS"),
		ALGORITHM_TYPE_ONE_SHOT("ALGORITHM TYPE ONE SHOT"),
		ALGORITHM_TYPE_EVENT_DRIVEN("ALGORITHM TYPE EVENT DRIVEN");
				
	    private final String text;

	    /** @param text */
	    private ALGORITHM_TYPE(final String text) {
	        this.text = text;
	    }

	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	public enum ALGORITHM_RESULT_TYPE{
		ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER("ALGORITHM RESULT TYPE SINGLE"),
		ALGORITHM_RESULT_TYPE_ARRAY_OBJECT_CLUSTER("ALGORITHM RESULT TYPE ARRAY");
				
	    private final String text;

	    /** @param text */
	    private ALGORITHM_RESULT_TYPE(final String text) {
	        this.text = text;
	    }

	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	public enum ALGORITHM_INPUT_TYPE{
		ALGORITHM_INPUT_TYPE_SINGLE_OBJECT_CLUSTER("ALGORITHM RESULT TYPE SINGLE"),
		ALGORITHM_INPUT_TYPE_ARRAY_OBJECT_CLUSTER("ALGORITHM RESULT TYPE ARRAY");
				
	    private final String text;

	    /** @param text */
	    private ALGORITHM_INPUT_TYPE(final String text) {
	        this.text = text;
	    }

	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	public class GuiLabelConfigCommon{
		public final static String SAMPLING_RATE = Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE;
		public final static String TIMESTAMP_SIGNAL_NAME = "Time Stamp Signal Name";
		public final static String TIMESTAMP_SIGNAL_FORMAT = "Time Stamp Signal Format";
	}
	
	//This identifies what inputs are required for the signal, and the corresponding gui option (e.g. checkbox, textbox)
	/** AKA ObjectClusterName */
	public String mAlgorithmName;
	public String mAlgorithmGroupingName;
	protected String mTrialName;
	
	//This are the core variables every algorithm should have
	
	@Deprecated
	/** this is the objectClusterName of the Signal that the algorithm is calculated on */
	protected String[] mSignalName = new String[1]; // an array because you might use multiple signals for an algorithm, note for now only single signal supported but this should be fwd compatible
	@Deprecated
	protected String[] mSignalFormat = new String[1];
	protected String mTimeStampName="";
	protected String mTimeStampFormat="";
	protected boolean mInitialized = false;
	protected boolean mIsEnabled = false;
	public AlgorithmDetails mAlgorithmDetails;
	protected double mMinSamplingRateForAlgorithhm = 0.0;
	
	/**
     * @deprecated
     * This method is to be replaced by the channeldetails object, see mListofChannelDetails
     */
	@Deprecated
	protected String[] mSignalOutputNameArray;
	/**
     * @deprecated
     * This method is to be replaced by the channeldetails object, see mListofChannelDetails
     */
	@Deprecated
	protected String[] mSignalOutputFormatArray;
	/**
     * @deprecated
     * This method is to be replaced by the channeldetails object, see mListofChannelDetails
     */
	@Deprecated
	protected String[] mSignalOutputUnitArray;
	
//	public List<ChannelDetails> mListofChannelDetails;
	public List<String> mOutputChannels; // list of all output channels - can be used for extracting specific metrics after processing
	public FILTERING_OPTION mFilteringOptions = FILTERING_OPTION.NONE;
	public ALGORITHM_TYPE mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
	public ALGORITHM_RESULT_TYPE mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
	public ALGORITHM_INPUT_TYPE mAlgorithmInputType = ALGORITHM_INPUT_TYPE.ALGORITHM_INPUT_TYPE_SINGLE_OBJECT_CLUSTER;
		
	public HashMap<String,ConfigOptionDetailsAlgorithm> mConfigOptionsMap = new HashMap<String,ConfigOptionDetailsAlgorithm>();//Define the gui to be generated
	public HashMap<String, AlgorithmDetails> mAlgorithmChannelsMap = new HashMap<String,AlgorithmDetails>();//Defines algorithm requirements
	
	public TreeMap<Integer, SensorGroupingDetails> mMapOfAlgorithmGrouping = new TreeMap<Integer, SensorGroupingDetails>();
	public abstract Object getSettings(String componentName);
	public abstract Object getDefaultSettings(String componentName);
//	public abstract void setSettings(String componentName, Object valueToSet) throws Exception;
	public abstract void setSettings(String componentName, Object valueToSet);
	
	/** Takes in a single data object and processes the data, the standard way of using this is through the use of ObjectCluster. NOTE: The processing time should never be longer than the Shimmer sampling period, as the method will hold up the thread calling it
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public abstract AlgorithmResultObject processDataRealTime(ObjectCluster object) throws Exception;
	
	public abstract AlgorithmResultObject processDataPostCapture(Object object) throws Exception;
	
	public abstract void reset() throws Exception;
	public abstract void initialize() throws Exception;
	public abstract String printBatchMetrics();
	
	/** For event driven algorithm implementation. Event Driven Algorithm is best to be used for algorithms whose processing duration is longer than the Shimmer's sampling rate. It can also be used for algorithms which do not require real time results.
	 *  Once processing is done use sendProcessingResultMsg method to send your results back as an event to be handled.
	 * @param shimmerMSG
	 */
	public abstract void eventDataReceived(ShimmerMsg shimmerMSG);
	
	
	public String getAlgorithmName() {
		return mAlgorithmName;
	}
	
	public void setSignalName(String signalName){
		mSignalName[0] = signalName;		
	}

	public void setSignalName(String signalName, int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			mSignalName[channelNum] = signalName;	
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to set element of mSignalName array.");
	}

	public String getSignalName(){
		return mSignalName[0];
	}
	
	public String getSignalName(int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			return mSignalName[channelNum];
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to get element of mSignalName array.");
	}
	
	public void setSignalFormat(String signalFormat){
		mSignalFormat[0] = signalFormat;		
	}
	
	public void setSignalFormat(String signalFormat, int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			mSignalFormat[channelNum] = signalFormat;		
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to set element of mSignalFormat array.");
	}
	
	public String getSignalFormat(){
		return mSignalFormat[0];
	}
	
	public String getSignalFormat(int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			return mSignalFormat[channelNum];
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to get element of mSignalFormat array.");
}
	
	public void setTimeStampName(String TimeStampName){
		mTimeStampName = TimeStampName;
	}
	
	public String getTimeStampName(){
		return mTimeStampName;
	}
	
	public void setTimeStampFormat(String TimeStampFormat){
		mTimeStampFormat = TimeStampFormat;		
	}
	
	public String getTimeStampFormat(){
		return mTimeStampFormat;
	}

	public HashMap<String, ConfigOptionDetailsAlgorithm> getConfigOptionsMap() {
		return mConfigOptionsMap;
	}
	
	public boolean isInitialized(){
		return mInitialized;
	}
	
	public boolean isEnabled() {
		return mIsEnabled;
	}
	
	public void setIsEnabled(boolean isEnabled) {
		mIsEnabled = isEnabled;
	}
	
	/** Override if needed for special cases (e.g., OrientationModule, ECGAdaptiveModule etc. */
	public void algorithmMapUpdateFromEnabledSensorsVars(long derivedSensorBitmapID){
		if(mAlgorithmDetails!=null){
			setIsEnabled((mAlgorithmDetails.mDerivedSensorBitmapID&derivedSensorBitmapID)>0? true:false);
		}
	}
	
	/** Override if needed for special cases (e.g., OrientationModule, ECGAdaptiveModule etc. */
	public long getDerivedSensorBitmapID() {
		if(mAlgorithmDetails!=null && isEnabled()){
			return mAlgorithmDetails.mDerivedSensorBitmapID;
		}
		return 0;
	}
	
	/** This returns a String array of the output signal name, the sequence of the format array MUST MATCH the array returned by the method returnSignalOutputFormatArray
	 * @return
	 */
	/**
     * @deprecated
     * This method is to be replaced by the channeldetails object, see getChannelDetails method
     */
	@Deprecated 
	public String[] getSignalOutputNameArray() {
		// TODO Auto-generated method stub
		return mSignalOutputNameArray;
	}

	/** This returns a String array of the output signal format, the sequence of the format array MUST MATCH the array returned by the method returnSignalOutputNameArray
	 * @return
	 */
	/**
     * @deprecated
     * This method is to be replaced by the channeldetails object, see getChannelDetails method
     */
	@Deprecated 
	public String[] getSignalOutputFormatArray() {
		// TODO Auto-generated method stub
		return mSignalOutputFormatArray;
	}

	/**
     * @deprecated
     * This method is to be replaced by the channeldetails object, see getChannelDetails method
     */
	@Deprecated 
	public String[] getSignalOutputUnitArray() {
		// TODO Auto-generated method stub
		return mSignalOutputUnitArray;
	}
	
	public List<ChannelDetails> getChannelDetails(){
		if(mAlgorithmDetails!=null){
			return mAlgorithmDetails.getChannelDetails();
		}
		return null;
	}
	
	//TODO EN
	//returns enabled channels
	public Integer getNumberOfEnabledChannels(){
		//redo with channels enabled
		return mAlgorithmDetails.getChannelDetails().size();
	}

	/**
	 * @return the mOutputChannels
	 */
	public List<String> getOutputChannelList() {
		return mOutputChannels;
	}
	
	public ALGORITHM_TYPE getAlgorithmType() {
		// TODO Auto-generated method stub
		return mAlgorithmType;
	}

	public ALGORITHM_INPUT_TYPE getAlgorithmInputType() {
		// TODO Auto-generated method stub
		return mAlgorithmInputType;
	}
	
	public ALGORITHM_RESULT_TYPE getAlgorithmResultType() {
		// TODO Auto-generated method stub
		return mAlgorithmResultType;
	}
	
	public void setFiltering(FILTERING_OPTION option) {
		// TODO Auto-generated method stub
		mFilteringOptions = option;
	}

	public String[] getComboBoxOptions(String key){
		return mConfigOptionsMap.get(key).mGuiValues;
	}
	
	/** To be used when the algorithm is requires a seperate thread for data processing
	 * @param ojc
	 */
	public void sendProcessingResultMsg(AlgorithmResultObject aro){
		sendCallBackMsg(MsgDock.MSG_ID_SOURCE_ALGORITHM ,aro);
	}
	
	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		if (shimmerMSG.mIdentifier == MsgDock.MSG_ID_DATA_TO_ALGO){
			eventDataReceived(shimmerMSG);
		}
	}
	
	public void setTrialName(String name){
		mTrialName = name;
	}
	
	public String getTrialName(){
		return mTrialName;
	}
	
	public double getMinSamplingRateForAlgorithm(){
		return mMinSamplingRateForAlgorithhm;
	}
	
	//TODO fix below if needed
	public AlgorithmResultObject processDataRealTime(List<ObjectCluster> objectClusterArray) {
		for(ObjectCluster ojc:objectClusterArray){
			try {
				return processDataRealTime(ojc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
//	public void setIsEnabled(boolean isEnabled, boolean groupEnabled) {
//		// TODO Auto-generated method stub
//		
//	}
	
}
