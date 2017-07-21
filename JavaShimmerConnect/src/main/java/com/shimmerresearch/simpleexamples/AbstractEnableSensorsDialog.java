package com.shimmerresearch.simpleexamples;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import javax.swing.JDialog;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;

public abstract class AbstractEnableSensorsDialog {

	ShimmerDevice shimmer;
	ShimmerDevice clone;
//	ShimmerDevice shimmer;
	ShimmerBluetoothManager bluetoothManager;
	private static JDialog dialog = new JDialog();
	Object[] listOfSensors;
	protected int[] sensorKeys;
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
		 count = 0;
		 
		 
		 for(int key : sensorMap.keySet()) {
			 SensorDetails sd = sensorMap.get(key);
			 if(clone.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
				 String sensorName = sd.mSensorDetailsRef.mGuiFriendlyLabel;
				 createCheckBox(sensorName,sd.isEnabled(),count);
				 sensorKeys[count] = key;
				 count++;
			 }
		 }
		
		 //updateCheckboxes(listOfSensors, clone, sensorKeys);
		 showFrame();
	}
	/*
	private void updateCheckboxes(Object[] checkboxes, ShimmerDevice shimmer, int[] sensorKeys) {
		
		for(int i=0; i<checkboxes.length; i++) {
			
			if(shimmer.isSensorEnabled(sensorKeys[i])) {
				checkboxes[i].setSelected(true);
			} else {
				checkboxes[i].setSelected(false);
			}
			
		}
	}
	*/
	protected void writeConfiguration(){
		AssembleShimmerConfig.generateSingleShimmerConfig(clone, COMMUNICATION_TYPE.BLUETOOTH);
 		bluetoothManager.configureShimmer(clone);
	}
	
	
}
