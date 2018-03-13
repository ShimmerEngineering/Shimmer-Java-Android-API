package com.shimmerresearch.algorithms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.MsgDock;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;

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
		public final static String SHIMMER_SAMPLING_RATE = Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE;
		//TODO implement support for the below across all algorithms
		public final static String MIN_ALGO_SAMPLING_RATE = "Algo Min " + SHIMMER_SAMPLING_RATE;
		public final static String TIMESTAMP_SIGNAL_NAME = "Time Stamp Signal Name";
		public final static String TIMESTAMP_SIGNAL_FORMAT = "Time Stamp Signal Format";
	}
	
	//This identifies what inputs are required for the signal, and the corresponding gui option (e.g. checkbox, textbox)
	/** AKA ObjectClusterName */
	public String mAlgorithmName;
	@Deprecated
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
	protected double mShimmerSamplingRate = 128.0;

	protected ShimmerDevice mShimmerDevice = null;
	protected UtilShimmer mUtilShimmer = new UtilShimmer(this.getClass().getSimpleName(), true);

	/** this is to specify what fw version/hardware should be allowed to use the algorithm */
	public List<ShimmerVerObject> mListOfCompatibleSVO = new ArrayList<ShimmerVerObject>(); 

	/** @deprecated This method is to be replaced by the channeldetails object, see mListofChannelDetails */
	@Deprecated
	protected String[] mSignalOutputNameArray;
	/** @deprecated This method is to be replaced by the channeldetails object, see mListofChannelDetails */
	@Deprecated
	protected String[] mSignalOutputFormatArray;
	/** @deprecated This method is to be replaced by the channeldetails object, see mListofChannelDetails */
	@Deprecated
	protected String[] mSignalOutputUnitArray;
	
//	public List<ChannelDetails> mListofChannelDetails;
	public List<String> mOutputChannels; // list of all output channels - can be used for extracting specific metrics after processing
	public FILTERING_OPTION mFilteringOptions = FILTERING_OPTION.NONE;
	public ALGORITHM_TYPE mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
	public ALGORITHM_RESULT_TYPE mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
	public ALGORITHM_INPUT_TYPE mAlgorithmInputType = ALGORITHM_INPUT_TYPE.ALGORITHM_INPUT_TYPE_SINGLE_OBJECT_CLUSTER;
		
	/** The mConfigOptionsMap is used to generate the GUI and provide specific
	 * algorithm settings to users, take note that for combo box there is an
	 * extra string array of values which need to be declared as options for the
	 * comobobox */
	public HashMap<String,ConfigOptionDetails> mConfigOptionsMap = new HashMap<String,ConfigOptionDetails>();//Define the gui to be generated
	public HashMap<String, AlgorithmDetails> mAlgorithmChannelsMap = new HashMap<String,AlgorithmDetails>();//Defines algorithm requirements
	
	public TreeMap<Integer, SensorGroupingDetails> mMapOfAlgorithmGrouping = new TreeMap<Integer, SensorGroupingDetails>();
	
	/**
	 * This is a quick approach (rather then having a map with isEnabled per
	 * commType as done in AbstractSensor and ShimmerDevice) to handle the fact
	 * that some algorithms only support certain COMM Types (e.g., Activity or
	 * Gait support SD data but not real-time BT)
	 */
	public List<COMMUNICATION_TYPE> mListOfCommunicationTypesSupported = Arrays.asList(COMMUNICATION_TYPE.values());
	
	//General methods used during algorithm initialisation
	public abstract void setGeneralAlgorithmName();
	public abstract void setFilteringOption();
	public abstract void setMinSamplingRateForAlgorithm();
	/** This Identifies what version (firmware/hardware) specific algorithm settings should be limited to which is declared in mConfigMap.*/
	public abstract void setSupportedVerInfo();
	/** The mConfigOptionsMap is used to generate the GUI and provide specific
	 * algorithm settings to users, take note that for combo box there is an
	 * extra string array of values which need to be declared as options for the
	 * comobobox */
	public abstract void generateConfigOptionsMap();
	public abstract void generateAlgorithmGroupingMap();
	
	public abstract void initialize() throws Exception;
	/** Can be used to reset all variables within an algorithm - not needed in most cases */
	public abstract void resetAlgorithm() throws Exception;
	/** Used to reset all filters/buffers on which an algorithm is based - i.e. if a device is streaming, stopped and then streams again. */
	public abstract void resetAlgorithmBuffers();
	
	public abstract Object getSettings(String componentName);
//	public abstract void setSettings(String componentName, Object valueToSet) throws Exception;
	public abstract void setSettings(String componentName, Object valueToSet);
	public abstract Object getDefaultSettings(String componentName);
	
	/** Takes in a single data object and processes the data, the standard way of using this is through the use of ObjectCluster. NOTE: The processing time should never be longer than the Shimmer sampling period, as the method will hold up the thread calling it
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public abstract AlgorithmResultObject processDataRealTime(ObjectCluster ojc) throws Exception;
	
	/** currently just used by Gait and Balance. MN: not sure if this is actually needed*/
	public abstract AlgorithmResultObject processDataPostCapture(Object object) throws Exception;
	
	/** currently just used by Gait and Balance.*/
	public abstract String printBatchMetrics();
	
	/** For event driven algorithm implementation. Event Driven Algorithm is best to be used for algorithms whose processing duration is longer than the Shimmer's sampling rate. It can also be used for algorithms which do not require real time results.
	 *  Once processing is done use sendProcessingResultMsg method to send your results back as an event to be handled.
	 * @param shimmerMSG
	 */
	public abstract void eventDataReceived(ShimmerMsg shimmerMSG);

	public abstract LinkedHashMap<String, Object> generateConfigMap();
	public abstract void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer);

	// ------  Constructors start (should be overridden in Algorithm classes in order to setupAlgorithm() locally)
	
	public AbstractAlgorithm(){
		setGeneralAlgorithmName();
	}
	
	public AbstractAlgorithm(AlgorithmDetails algorithmDetails) {
		setAlgorithmDetails(algorithmDetails);
	}

	public AbstractAlgorithm(ShimmerDevice shimmerDevice, AlgorithmDetails algorithmDetails) {
		setAlgorithmDetails(algorithmDetails);
		mShimmerDevice = shimmerDevice;
	}

	// ------  Constructors end

	
	private void setAlgorithmDetails(AlgorithmDetails algorithmDetails) {
		mAlgorithmDetails = algorithmDetails;
//		setSignalName(algorithmDetails.mAlgorithmName);
		mAlgorithmName = algorithmDetails.mAlgorithmName;
		mAlgorithmGroupingName = algorithmDetails.mAlgorithmName;
		mAlgorithmChannelsMap.put(mAlgorithmName, mAlgorithmDetails);
	}
	
	public void setupAlgorithm() {
		setFilteringOption();
		setMinSamplingRateForAlgorithm();
		setSupportedVerInfo();

		generateConfigOptionsMap();
		generateAlgorithmGroupingMap();
	}
	
	public String getAlgorithmName() {
		return mAlgorithmName;
	}
	
	@Deprecated
	public void setSignalName(String signalName){
		mSignalName[0] = signalName;		
	}

	@Deprecated
	public void setSignalName(String signalName, int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			mSignalName[channelNum] = signalName;	
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to set element of mSignalName array.");
	}

	@Deprecated
	public String getSignalName(){
		return mSignalName[0];
	}
	
	@Deprecated
	public String getSignalName(int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			return mSignalName[channelNum];
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to get element of mSignalName array.");
	}
	
	@Deprecated
	public void setSignalFormat(String signalFormat){
		mSignalFormat[0] = signalFormat;		
	}
	
	@Deprecated
	public void setSignalFormat(String signalFormat, int channelNum){
		if(channelNum >= 0 && channelNum < mSignalName.length)
			mSignalFormat[channelNum] = signalFormat;		
		else 
			throw new ArrayIndexOutOfBoundsException("Invalid attempt to set element of mSignalFormat array.");
	}
	
	@Deprecated
	public String getSignalFormat(){
		return mSignalFormat[0];
	}
	
	@Deprecated
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

	public HashMap<String, ConfigOptionDetails> getConfigOptionsMap() {
		return mConfigOptionsMap;
	}
	
	public boolean isInitialized(){
		return mInitialized;
	}
	
	public void setIsInitialized(boolean state){
		mInitialized = state;
	}
	
	public boolean isEnabled() {
		return mIsEnabled;
	}
	
	public void setIsEnabled(boolean isEnabled) {
//		if(isEnabled){
//			System.out.print(mAlgorithmName + " is being set to " + isEnabled);
//			System.out.print(UtilShimmer.convertStackTraceToString(Thread.currentThread().getStackTrace()));
//		}
		
		mIsEnabled = isEnabled;
		if(!isEnabled){
			setIsInitialized(false);
		}
	}
	
	/** Override if needed for special cases (e.g., OrientationModule, ECGAdaptiveModule etc. */
	public void algorithmMapUpdateFromEnabledSensorsVars(long derivedSensorBitmapID){
		if(mAlgorithmDetails!=null){
			boolean state = (mAlgorithmDetails.mDerivedSensorBitmapID&derivedSensorBitmapID)>0? true:false; 
			setIsEnabled(state);
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

	/** Returns a list of channel details from enabled channels.
	 * @return
	 */
	public List<ChannelDetails> getChannelDetails(){
		return getChannelDetails(false);
	}

	/**
	 * Generally returns a list of channel details from enabled channels. Can be
	 * overridden in Algorithm classes depending on whether extra channels are
	 * enabled by config options within the algorithm.
	 * 
	 * @param showDisabledChannels
	 * @return
	 */
	public List<ChannelDetails> getChannelDetails(boolean showDisabledChannels){
		if(mAlgorithmDetails!=null){
			return mAlgorithmDetails.getChannelDetails();
		}
		return null;
	}
	
	/** returns enabled channels
	 * @return
	 */
	public Integer getNumberOfEnabledChannels(){
		List<ChannelDetails> listOfChannels = getChannelDetails(false);
		if(listOfChannels!=null){
			return listOfChannels.size();
		}
		return 0;
	}

	/**
	 * @return the mOutputChannels
	 */
	public List<String> getOutputChannelList() {
		return mOutputChannels;
	}
	
	public ALGORITHM_TYPE getAlgorithmType() {
		return mAlgorithmType;
	}

	public ALGORITHM_INPUT_TYPE getAlgorithmInputType() {
		return mAlgorithmInputType;
	}
	
	public ALGORITHM_RESULT_TYPE getAlgorithmResultType() {
		return mAlgorithmResultType;
	}
	
	public void setFiltering(FILTERING_OPTION option) {
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
	
	public HashMap<String, Object> getAlgorithmSettings() {
		HashMap<String, Object> mapOfAlgorithmSettings = new HashMap<String, Object>();
		for(String configOptionKey:mConfigOptionsMap.keySet()){
			Object configValue = getSettings(configOptionKey);
			if(configValue!=null){
				consolePrintLn("getEnabledAlgorithmSettings\t" + "\tConfigKey=" + configOptionKey + "\tConfigValue=" + configValue);
				mapOfAlgorithmSettings.put(configOptionKey, configValue);
			}
		}
		return mapOfAlgorithmSettings;
	}
	
	public void setAlgorithmSettings(HashMap<String, Object> mapOfAlgoSettings) {
		for(String configOptionKey:mapOfAlgoSettings.keySet()){
			setSettings(configOptionKey, mapOfAlgoSettings.get(configOptionKey));
		}
	}
	
	
	public void setDefaultSetting() {
		// TODO Auto-generated method stub
		
	}
	
	public void setGeneralAlgorithmName(String generalAlgorithmName) {
		mAlgorithmName = generalAlgorithmName;
	}
	
	public void setShimmerSamplingRate(double samplingRate){
		mShimmerSamplingRate = samplingRate;
		if(isEnabled()){
			try {
				initialize();
			} catch (Exception e) {
				System.err.println("sampling rate=" + samplingRate);
				e.printStackTrace();
			}
		}
	}
	
	public double getShimmerSamplingRate(){
		return mShimmerSamplingRate;
	}
	
	public void addConfigOption(ConfigOptionDetails configOptionDetails) {
		mConfigOptionsMap.put(configOptionDetails.mGuiHandle, configOptionDetails); 
	}
	
	protected void setListOfCommunicationTypesSupported(COMMUNICATION_TYPE commType) {
		mListOfCommunicationTypesSupported = Arrays.asList(commType);
	}

	protected void setListOfCommunicationTypesSupported(List<COMMUNICATION_TYPE> listOfCommTypesSupported) {
		mListOfCommunicationTypesSupported = listOfCommTypesSupported;
	}
	
	protected void consolePrintLn(String message) {
		if(mShimmerDevice!=null){
			mShimmerDevice.consolePrintLn(message);
		}
		else{
//			System.out.println(message);
			mUtilShimmer.consolePrintLn(message);			
		}
	}

	protected void consolePrintErrLn(String message) {
		if(mShimmerDevice!=null){
			mShimmerDevice.consolePrintErrLn(message);
		}
		else{
//			System.out.println(message);
			mUtilShimmer.consolePrintErrLn(message);			
		}
	}

	protected void consolePrintException(String message, StackTraceElement[] stackTrace) {
		if(mShimmerDevice!=null){
			mShimmerDevice.consolePrintExeptionLn(message, stackTrace);
		}
		else{
//			System.out.println(message);
			mUtilShimmer.consolePrintExeptionLn(message, stackTrace);			
		}
	}

	/** Needs to be overridden in extending algorithm modules */
	public void loadAlgorithmVariables(AbstractAlgorithm abstractAlgorithmSource) {
		// TODO Auto-generated method stub
		
	}

}
