package com.shimmerresearch.sensors;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/** 
 * @author Ruud Stolk 
 * 
 */
public class SensorTemplate extends AbstractSensor{
	/**
	 * SensorTemplate is a template for sensor classes for new sensors.
	 *  
	 *  - Sensor classes need to extend AbstractSensor, hence all abstract methods need to be implemented.
	 *  - Put the variables and methods in the pre-defined environments:
	 *  
	 *  	 Sensor specific variables;
	 *  	 Bluetooth commands;
	 *  	 Configuration options; 
	 *  	 Sensor info;
	 *  	 Channel info;  
	 *  	 Constructor;
	 *  	 Abstract methods;
	 *  	 Sensor specific methods;
	 *  
	 *  further specified below with examples/explanation below/on the right-hand side of the specifications.
	 * 
	 * @param svo
	 */
	private static final long serialVersionUID = -1313629173441403991L;

	//--------- Sensor specific variables start --------------
			/**	
			 * 	initialise boolean variables   					-> e.g. mLowPowerAccelWR = false;
			 * 	initialise int variables       					-> e.g. mMagRange = 1;
			 * 	calibration matrices           					-> alignment, sensitivity, offset matrices for each range for each (sub)sensor
			 * 	class GuiLabelConfig           					-> class containing GUI configuration labels 
			 * 	class GuiLabelSensorTiles      					-> class containing GUI sensor tile labels
			 * 	class GuiLabelSensors		   					-> class containing GUI sensor labels
			 * 	class DatabaseChannelHandles   					-> class containing Database handles
			 * 	class ObjectClusterSensorName  					-> class containing ObjectClusterSensorName (channel name)
			 * 
			 */
	//--------- Sensor specific variables end --------------

	
	
	//--------- Bluetooth commands start --------------
			/**
			 *  Bluetooth commands related to the sensor 		-> public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
			 *													-> public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;
			 *													-> public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;
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
			 *  mSensorMapRef - LinkedHashmap<K,V>   			-> with for each (sub)sensor:  K=SensorMapKey, V=SensorDetailsRef
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
				super(svo);
				setSensorName(SENSORS.TEMPLATE.toString());
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
			 *	public abstract boolean setDefaultConfigForSensor(int sensorMapKey, boolean state);
			 *	public abstract boolean checkConfigOptionValues(String stringKey);
			 *	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
			 *	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);
			 */
			@Override
			public void generateSensorMap(ShimmerVerObject svo) {
			/** 
			 *  call one of the two methods:
			 *  
			 *  1) super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
			 *  2) super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
			 *
			 */
			}
		
			@Override
			public void generateConfigOptionsMap(ShimmerVerObject svo) {
				// TODO Auto-generated method stub
				
			}
		
			@Override
			public void generateSensorGroupMapping(ShimmerVerObject svo) {
				// TODO Auto-generated method stub
				
			}
		
			@Override
			public ObjectCluster processDataCustom(SensorDetails sensorDetails,	byte[] sensorByteArray, COMMUNICATION_TYPE commType,ObjectCluster objectCluster) {
				// TODO Auto-generated method stub
				return null;
			}
		
			@Override
			public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
				// TODO Auto-generated method stub
				
			}
		
			@Override
			public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
				// TODO Auto-generated method stub
				
			}
		
			@Override
			public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
				// TODO Auto-generated method stub
				return null;
			}
		
			@Override
			public Object getConfigValueUsingConfigLabel(String componentName) {
				// TODO Auto-generated method stub
				return null;
			}
		
			@Override
			public void setSensorSamplingRate() {
				// TODO Auto-generated method stub
				
			}
		
			@Override
			public boolean setDefaultConfigForSensor(int sensorMapKey, boolean state) {
				// TODO Auto-generated method stub
				return false;
			}
		
			@Override
			public boolean checkConfigOptionValues(String stringKey) {
				// TODO Auto-generated method stub
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
			//--------- Abstract methods implemented end --------------
			
	
			
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


}
