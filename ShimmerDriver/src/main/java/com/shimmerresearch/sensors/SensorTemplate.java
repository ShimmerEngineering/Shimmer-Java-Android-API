package com.shimmerresearch.sensors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * <b>SensorTemplate</b> is a template for adding classes for new sensors.
 *  A Sensor Class needs to extend AbstractSensor, hence all abstract methods need to be implemented.
 *  Put the variables and methods in the pre-defined environments:
 *  <br />
 *  	<li>Sensor specific variables<br />
 *  	 <li>Bluetooth commands<br />
 *  	 <li>Configuration options<br />
 *  	 <li>Sensor info<br />
 *  	 <li>Channel info<br />  
 *  	 <li>Constructor<br />
 *  	 <li>Abstract methods<br />
 *  	 <li>Sensor specific methods<br />
 *       <li>Optional methods to override in Sensor Class<br />

 *  
 *  The environments are further specified below with examples/explanation below/on the right-hand side of the specifications.
 * 
 * @author Ruud Stolk 
 */
public class SensorTemplate extends AbstractSensor{

	private static final long serialVersionUID = -1313629173441403991L;

	//--------- Sensor specific variables start --------------
	/**	
	 * 	initialise boolean variables   					-> e.g. mLowPowerAccelWR = false;
	 * 	initialise other variables       					-> e.g. mMagRange = 1;
	 * 	calibration matrices           					-> alignment, sensitivity, offset matrices for each range for each (sub)sensor
	 * 	class GuiLabelConfig           					-> class containing GUI configuration labels 
	 * 	class GuiLabelSensors		   					-> class containing GUI sensor labels
	 *  class LABEL_SENSOR_TILE      					-> class containing GUI sensor tile labels
	 * 	class DatabaseChannelHandles   					-> class containing Database handles
	 * 	class ObjectClusterSensorName  					-> class containing ObjectClusterSensorName (channel name)
	 * 
	 * What TODO with this in ShimmerObject? In Sensor Class?:
	 * 	- SensorBitMap (for ID/Fw -> What does this mean?)
	 * 	- SDLogHeader
	 *  - SDLogHeaderDerivedSensors
	 *  - BTStreamDerivedSensors
	 *  - BTStream
	 * 
	 */
	//--------- Sensor specific variables end --------------



	//--------- Bluetooth commands start --------------
	/**
	 *  Bluetooth commands related to the sensor 		-> public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
	 *													-> public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;
	 *													-> public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;
	 *  mBtGetCommandMap - LinkedHashmap<K,V>  			-> with for get command:  K=command's byte value, V=BtCommandDetails
	 *  mBtSetCommandMap - LinkedHashmap<K,V>  			-> with for set command:  K=command's byte value, V=BtCommandDetails
	 */
	//--------- Bluetooth commands end --------------



	//--------- Configuration options start --------------
	/**
	 * 	String[] - Lists with configuration options		-> see SensorLSM303.java for an example
	 * 	Integer[] - Lists with configuration options	-> see SensorLSM303.java for an example
	 * 	SensorConfigOptionDetails 						-> SensorConfigOptionDetails for each configuration option; see SensorLSM303.java for an example
	 */
	//--------- Configuration options end --------------



	//--------- Sensor info start --------------
	/**
	 * 	SensorDetailsRef 								-> SensorDetailsRef for each (sub)sensor; see SensorLSM303.java for an example
	 *  mSensorMapRef - LinkedHashmap<K,V>   			-> with for each (sub)sensor:  K=SensorId, V=SensorDetailsRef
	 */
	//--------- Sensor info end --------------



	//--------- Channel info start --------------
	/**
	 *	ChannelDetails 									-> ChannelDetails for each channel of each (sub)sensor.
	 *	mChannelMapRef - LinkedHashmap<K,V>				-> with for each channel:  K=ObjectClusterSensorName, V=ChannelDetails.  
	 */
	//--------- Channel info end --------------



	//--------- Constructors for this class start --------------
	/**
	 *  One or more constructors of the sensor class in here.
	 */

	public SensorTemplate(ShimmerVerObject svo) {
		super(SENSORS.TEMPLATE, svo);
		/**initialise() Must be called after the constructor*/ 
		initialise();
	}
	//--------- Constructors for this class end --------------



	//--------- Abstract methods implemented start --------------
	/** 
	 * 	public abstract void generateSensorMap(ShimmerVerObject svo);
	 *	public abstract void generateConfigOptionsMap(ShimmerVerObject svo);
	 *	public abstract void generateSensorGroupMapping(ShimmerVerObject svo);
	 * 	public abstract ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster);
	 *	public abstract void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	 *	public abstract void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	 *	public abstract Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet);
	 *	public abstract Object getConfigValueUsingConfigLabel(String componentName);
	 *	public abstract void setSensorSamplingRate();
	 *	public abstract boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled);
	 *	public abstract boolean checkConfigOptionValues(String stringKey);
	 *	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
	 *	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);
	 */
	@Override
	public void generateSensorMap() {
		/** 
		 *  call one of the two methods:
		 *  
		 *  1) super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
		 *  	- SensorDetails.processData() is called.
		 *  2) super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		 *		- SensorDetails.processData() is overwritten by AbstractSensor.processDataCustom().
		 */
	}

	@Override
	public void generateConfigOptionsMap() {
		/**
		 *  put all the Config Options on mConfigOptionsMap:
		 *  
		 *  	mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, configOptionAccelRange);
		 *  
		 */

	}

	@Override
	public void generateSensorGroupMapping() {
		/**
		 *  put all the Sensor Grouping Details on mSensorGroupingMap and call updateSensorGroupingMap() :
		 *  
		 *  		if((mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4())){
		 *				mSensorGroupingMap.put(LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL, new SensorGroupingDetails(
		 *						Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303DLHC_ACCEL),
		 *						CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
		 *				mSensorGroupingMap.put(LABEL_SENSOR_TILE.MAG, new SensorGroupingDetails(
		 *						Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303DLHC_MAG),
		 *						CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
		 *			}
		 *			super.updateSensorGroupingMap();	
		 *  
		 */
		super.updateSensorGroupingMap();	
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,	byte[] sensorByteArray, COMMUNICATION_TYPE commType,ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			// first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);

			/**
			 *  Get the uncalibrated data from the ObjectCluster if calibration is needed. Example - SensorKionixKXRB52042:
			 * 
			 * 		//Uncalibrated Accelerometer data
			 *		if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_X)){
			 *		unCalibratedAccelData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			 *		}
			 *		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Y)){
			 *			unCalibratedAccelData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			 *		}
			 *		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Z)){
			 *			unCalibratedAccelData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			 *		}	
			 */

			index = index + channelDetails.mDefaultNumBytes;
		}

		/**
		 *  Perform calibration and add the calibrated data to the ObjectCluster. Example - SensorKionixKXRB52042:
		 *  //Calibration
		 *	double[] calibratedAccelData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
		 *
		 *	//Add calibrated data to Object cluster
		 *	for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
		 *		if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_X)){
		 *			objectCluster.addCalData(channelDetails, calibratedAccelData[0]);
		 *			objectCluster.incrementIndexKeeper();
		 *		}
		 *		else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Y)){
		 *			objectCluster.addCalData(channelDetails, calibratedAccelData[1]);
		 *			objectCluster.incrementIndexKeeper();
		 *		}
		 *		else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Z)){
		 *			objectCluster.addCalData(channelDetails, calibratedAccelData[2]);
		 *			objectCluster.incrementIndexKeeper();
		 *		}
		 *	}
		 *
		 */

		return objectCluster;
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			/** Set defaults for particular sensor here if applicable. 
			 *  Original means that if the sensor has just been enabled, leave the resolution the way it is.
			 *  However, if it is disabled, reset the resolution to default  
			 */
			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			//Set values for particular Config Option here if applicable
			return true;
		}
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void parseConfigMap(
			LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}



	//--------- Abstract methods implemented end --------------



	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub

	}



	//--------- Sensor specific methods start --------------
	/**	
	 * 	sensor specific methods in the following order:
	 *  
	 * 	 1) methods that do not fall in categories 2-5:
	 * 	 2) calibration related methods
	 * 	 3) set methods
	 * 	 4) "is" methods 								-> get methods that return a boolean)
	 * 	 5) get methods 								-> get methods that do not return a boolean) 
	 */
	//--------- Sensor specific methods end --------------



	//--------- Optional methods to override in Sensor Class start --------
	/**
	 * Used, for example, in SensorLSM303, SensorMPU9X50 and
	 * SensorKionixKXRB52042
	 */			
	//--------- Optional methods to override in Sensor Class end --------


}
