//TO BE DELETED ONCE UPDATES ARE MADE

package com.shimmerresearch.simpleexamples;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
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

public class EnableSensorsDialogOLD {

	ShimmerPC shimmer;
	ShimmerPC clone;
//	ShimmerDevice shimmer;
	BasicShimmerBluetoothManagerPc bluetoothManager;
	private static JDialog dialog = new JDialog();
	
	public EnableSensorsDialogOLD(ShimmerPC shimmerPC,BasicShimmerBluetoothManagerPc btManager) {
		shimmer = shimmerPC;
		this.bluetoothManager = btManager;
	}
	
//	public EnableSensorsDialog(ShimmerDevice shimmerDevice) {
//		shimmer = shimmerDevice;
//	}
	
	public static void main(String[] args) {
		
//		dialog.setVisible(true);
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
//		 JDialog dialog = new JDialog();
		 dialog.setModal(true);
		 dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		 dialog.setTitle("Enable Sensors");
		 //Create a clone device
		 clone = shimmer.deepClone();
		 JButton btnWriteConfig = new JButton("Save");
		 btnWriteConfig.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent e) {
		 		//TODO: Write the config from clone to shimmer here
		 		AssembleShimmerConfig.generateSingleShimmerConfig(clone, COMMUNICATION_TYPE.BLUETOOTH);
		 		bluetoothManager.configureShimmer(clone);
		 		dialog.dispose();
		 	}
		 });
		 btnWriteConfig.setToolTipText("Write the current sensors configuration to the Shimmer device");
		 dialog.getContentPane().add(btnWriteConfig, BorderLayout.SOUTH);
		 
		 //JPanel panel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
		 JPanel panel = new JPanel();
		 panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));

		 dialog.getContentPane().add(panel, BorderLayout.CENTER);
		 
		 
	
//		 ShimmerDevice clone = shimmer.deepClone();
		 
		 //Setup the list of sensors checkboxes
		 
		 Map<Integer, SensorDetails> sensorMap = clone.getSensorMap();
		 int count = 0;
		 
		 //Check how many sensors the device is compatible with
		 for(SensorDetails details : sensorMap.values()) {
			 if(clone.isVerCompatibleWithAnyOf(details.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
				 count++;
			 }
		 }
		 
//		 String arraySensors[] = new String[count];
//		 final boolean[] listEnabled = new boolean[count];
		 final int[] sensorKeys = new int[count];
		 JCheckBox[] listOfSensors = new JCheckBox[count];
		 count = 0;
		 
//		 for(int key : sensorMap.keySet()) {
//			 SensorDetails sd = sensorMap.get(key);
//			 if(clone.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
//				 arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
//				 listEnabled[count] = sd.isEnabled();
//				 sensorKeys[count] = key;
//				 count++;
//			 }
//		 }
		 
		 
		 for(int key : sensorMap.keySet()) {
			 SensorDetails sd = sensorMap.get(key);
			 if(clone.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
				 String sensorName = sd.mSensorDetailsRef.mGuiFriendlyLabel;
				 listOfSensors[count] = new JCheckBox(sensorName, sd.isEnabled());
				 panel.add(listOfSensors[count]);
								 
				 sensorKeys[count] = key;
				 count++;
			 }
		 }
		 
		 for(int i=0; i<listOfSensors.length; i++) {
			 final int a = i;
			 listOfSensors[a].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						
						if(listOfSensors[a].isSelected()) {
							clone.setSensorEnabledState(sensorKeys[a], true);
						} else {
							clone.setSensorEnabledState(sensorKeys[a], false);
						}
						
						updateCheckboxes(listOfSensors, clone, sensorKeys);
						
					}
				});
		 }
		 
		
		 updateCheckboxes(listOfSensors, clone, sensorKeys);
		 
		 int dialogHeight = 75+(listOfSensors.length*25);
		 dialog.setSize(300, dialogHeight);
		 
		 dialog.setVisible(true);

		 
		 
	}
	
	private void updateCheckboxes(JCheckBox[] checkboxes, ShimmerPC shimmer, int[] sensorKeys) {
		
		for(int i=0; i<checkboxes.length; i++) {
			
			if(shimmer.isSensorEnabled(sensorKeys[i])) {
				checkboxes[i].setSelected(true);
			} else {
				checkboxes[i].setSelected(false);
			}
			
		}
	}
	
	
}
