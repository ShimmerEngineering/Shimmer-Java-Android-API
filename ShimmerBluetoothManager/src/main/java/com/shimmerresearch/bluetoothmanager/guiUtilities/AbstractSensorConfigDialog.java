package com.shimmerresearch.bluetoothmanager.guiUtilities;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

public abstract class AbstractSensorConfigDialog {

	protected ShimmerDevice cloneDevice;
	protected ShimmerDevice shimmerDevice;
	protected ShimmerBluetoothManager bluetoothManager;
	public abstract void createComboBox(int numOfOptions,String key,ConfigOptionDetailsSensor cods,Object[] checkBox, boolean isEnabled);
	public abstract void createEditText(String key, boolean isEnabled);
	public abstract void createLabel(String label);
	public abstract void createFrame();
	public abstract void showFrame();
	protected int dialogHeight = 0;
	protected List<String> listOfKeys;
	protected Map<Integer, SensorDetails> sensorMap;
	protected Map<String, ConfigOptionDetailsSensor> configOptionsMap;
	
	
	public AbstractSensorConfigDialog(ShimmerDevice shimmer, ShimmerBluetoothManager btManager){
		this.shimmerDevice = shimmer;
		this.bluetoothManager = btManager;
	}
	
	
	protected boolean mEnableFilter = false;
	protected List<String> keysToFilter = null;
	protected boolean mDisplayButDisableEnableFilter = false;
	protected List<String> keysToDisplayButDisableFilter = null;
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
//	public void initialize(ShimmerDevice shimmerDevice, ShimmerBluetoothManager bluetoothManager) {
	public void initialize() {
				
		cloneDevice = shimmerDevice.deepClone();
		sensorMap = cloneDevice.getSensorMap();
		configOptionsMap = cloneDevice.getConfigOptionsMap();
		listOfKeys = new ArrayList<String>();
		
		for(SensorDetails sd : sensorMap.values()) {
			if(sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated!=null && sd.isEnabled()) {
				listOfKeys.addAll(sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated);
			}
		}
		dialogHeight=0;
		//Box[] box = Box.createVerticalBox();

		//Remove keys for which ConfigOptionDetailsSensor is null to avoid runtime errors:
		List<String> keysToRemove = new ArrayList<String>();

        for(String key : listOfKeys) {
            ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
            if(cods == null) {
                keysToRemove.add(key);
            }
        }
        
        //If filter is enabled, add those filter keys to the list of keys to remove as well:
        if(mEnableFilter == true && keysToFilter != null) {
        	keysToRemove.addAll(keysToFilter);
        }

        for(String key : keysToRemove) {
            listOfKeys.remove(key);
        }
		
		for(String key : listOfKeys) {
			ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
			
			if(cods != null) {
			String[] cs = cods.getGuiValues();
			
			//System.out.println("For key: " + key + " configValue is: " + cloneDevice.getConfigValueUsingConfigLabel(key));
			
			
			createLabel(key);
			
			if(cs != null) {
				int numOfCheckboxes = cs.length;
				Object[] checkBox = new Object[numOfCheckboxes];
				if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
					 if(keysToDisplayButDisableFilter.contains(key)) 
					 {
							createComboBox(numOfCheckboxes, key, cods,checkBox,false);
					 }
					 else 
					 {
							createComboBox(numOfCheckboxes, key, cods,checkBox, true);
					 }
				}
			}
			
			if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD) {
				 if(keysToDisplayButDisableFilter.contains(key)) 
				 {
					createEditText(key, false);
				 }
				 else 
				 {
					 createEditText(key, true);
				 }
			}
			//scrollPane.add(box);
			
			}
		}
		
	}
	
	protected void writeConfiguration(){
		AssembleShimmerConfig.generateSingleShimmerConfig(cloneDevice, COMMUNICATION_TYPE.BLUETOOTH);
 		bluetoothManager.configureShimmer(cloneDevice);
	}
	
	/**
	 * Pass a String list of keys for sensors to be ignored when generating the list of config options
	 * @param filterKeys	Keys to be filtered out from the list
	 * @param enableFilter	To enable or disable the filter
	 */
	public void setSensorKeysFilter(List<String> filterKeys, boolean enableFilter) {
		mEnableFilter = enableFilter;
		keysToFilter = filterKeys;
	}
	
	public void setSensorDisplayButDisableKeysFilter(List<String> filterKeys, boolean enableFilter) {
		mDisplayButDisableEnableFilter = enableFilter;
		keysToDisplayButDisableFilter = filterKeys;
	}
}

