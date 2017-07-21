package com.shimmerresearch.bluetoothmanager.guiUtilities;

import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

public abstract class AbstractEnableSensorsDialog {

	protected ShimmerDevice shimmer;
	protected ShimmerDevice clone;
//	ShimmerDevice shimmer;
	ShimmerBluetoothManager bluetoothManager;
	protected Object[] listOfSensors;
	protected int[] sensorKeys;
	protected String[] arraySensors;
	protected boolean[] listEnabled;
	public AbstractEnableSensorsDialog(ShimmerDevice shimmer,ShimmerBluetoothManager btManager) {
		this.shimmer = shimmer;
		clone = shimmer.deepClone();
		this.bluetoothManager = btManager;
	}
	
//	public EnableSensorsDialog(ShimmerDevice shimmerDevice) {
//		shimmer = shimmerDevice;
//	}
	
	public static void main(String[] args) {
		
//		dialog.setVisible(true);
		
	}
	protected abstract void createWriteButton();
	protected abstract void createFrame();
	protected abstract void showFrame();
	protected abstract void createCheckBox(String sensorName, boolean state, int count);
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {

		 createFrame();
		 createWriteButton();
		 Map<Integer, SensorDetails> sensorMap = clone.getSensorMap();
		 int count = 0;
		 
		 //Check how many sensors the device is compatible with
		 for(SensorDetails details : sensorMap.values()) {
			 if(clone.isVerCompatibleWithAnyOf(details.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
				 count++;
			 }
		 }
		 sensorKeys = new int[count];
		 listOfSensors = new Object[count];
		 arraySensors = new String[count];
		 listEnabled = new boolean[count];
		 count = 0;
		 
		 
		 for(int key : sensorMap.keySet()) {
			 SensorDetails sd = sensorMap.get(key);
			 if(clone.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
				 String sensorName = sd.mSensorDetailsRef.mGuiFriendlyLabel;
				 //filter out names
				 createCheckBox(sensorName,sd.isEnabled(),count);
				 sensorKeys[count] = key;
				 arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
				 listEnabled[count] = sd.isEnabled();
				 count++;
			 }
		 }

		 showFrame();
	}

	protected void writeConfiguration(){
		AssembleShimmerConfig.generateSingleShimmerConfig(clone, COMMUNICATION_TYPE.BLUETOOTH);
 		bluetoothManager.configureShimmer(clone);
	}
	
	
}
