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
	protected ShimmerDevice shimmer;
	protected ShimmerBluetoothManager bluetoothManager;
	public abstract void createComboBox(int numOfOptions,String key,ConfigOptionDetailsSensor cods,Object[] checkBox);
	public abstract void createEditText(String key);
	public abstract void createLabel(String label);
	public abstract void createFrame();
	public abstract void showFrame();
	protected int dialogHeight = 0;
	protected List<String> listOfKeys;
	protected Map<Integer, SensorDetails> sensorMap;
	protected Map<String, ConfigOptionDetailsSensor> configOptionsMap;
	public AbstractSensorConfigDialog(ShimmerDevice shimmerDevice, ShimmerBluetoothManager bluetoothManager){
		cloneDevice = shimmerDevice.deepClone();
		shimmer = shimmerDevice;
		this.bluetoothManager = bluetoothManager;
	}
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize(ShimmerDevice shimmerDevice, ShimmerBluetoothManager bluetoothManager) {
				
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

		//Taken from Android is this needed - start
		List<String> keysToRemove = new ArrayList<String>();

        for(String key : listOfKeys) {
            ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
            if(cods == null) {
                keysToRemove.add(key);
            }
        }

        for(String key : keysToRemove) {
            listOfKeys.remove(key);
        }
      //Taken from ANdroid is this needed - end
		
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
					createComboBox(numOfCheckboxes, key, cods,checkBox);
				}
			}
			
			if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD) {
				createEditText(key);
			}
			//scrollPane.add(box);
			
			}
		}
		
	}
	

	//JC: Can this replaced by getConfigValueUsingConfigLabel ? in Shimmer Device Class
    private String getConfigValueLabelFromConfigLabel(String label){
        ConfigOptionDetailsSensor cods = cloneDevice.getConfigOptionsMap().get(label);
        int currentConfigInt = (int) cloneDevice.getConfigValueUsingConfigLabel(label);
        int index = -1;
        Integer[] values = cods.getConfigValues();
        String[] valueLabels = cods.getGuiValues();
        for (int i=0;i<values.length;i++){
            if (currentConfigInt==values[i]){
                index=i;
            }
        }
        if (index==-1){
            System.out.println();
            return "";
        }
        return valueLabels[index];
    }

	protected void writeConfiguration(){
		AssembleShimmerConfig.generateSingleShimmerConfig(cloneDevice, COMMUNICATION_TYPE.BLUETOOTH);
 		bluetoothManager.configureShimmer(cloneDevice);
	}
	
	
	
}

