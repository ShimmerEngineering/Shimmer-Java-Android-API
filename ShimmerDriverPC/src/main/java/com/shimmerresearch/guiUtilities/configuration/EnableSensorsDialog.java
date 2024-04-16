package com.shimmerresearch.guiUtilities.configuration;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.bluetoothmanager.guiUtilities.AbstractEnableSensorsDialog;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import javax.swing.JDialog;
import javax.swing.AbstractButton;
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

public class EnableSensorsDialog extends AbstractEnableSensorsDialog{

	private static JDialog dialog = new JDialog();
	JPanel panel = new JPanel();
	
	public EnableSensorsDialog(ShimmerDevice shimmerPC,BasicShimmerBluetoothManagerPc btManager) {
		super(shimmerPC,btManager);
	}
	
//	public EnableSensorsDialog(ShimmerDevice shimmerDevice) {
//		shimmer = shimmerDevice;
//	}
	
	public static void main(String[] args) {
		
//		dialog.setVisible(true);
		
	}
	
	/**
	 * Call this to initialize and display the dialog.
	 * @wbp.parser.entryPoint
	 */
	public void showDialog() {
		//Filter out the sensors we don't want before initializing the dialog:
		List<Integer> sensorKeysToFilter = new ArrayList<Integer>();
		sensorKeysToFilter.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY);
		sensorKeysToFilter.add(Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM);
		setSensorKeysFilter(sensorKeysToFilter, true);

		super.initialize();
	}

	@Override
	protected void createWriteButton() {
// TODO Auto-generated method stub
		 JButton btnWriteConfig = new JButton("Save");
		 btnWriteConfig.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent e) {
		 		//TODO: Write the config from clone to shimmer here
		 		for(int i=0; i<listOfSensors.length; i++) {
		 			if(((JCheckBox) listOfSensors[i]).isSelected()) {
		 				enabledSensorKeys.add(sensorKeys[i]);
		 			}
		 		}
		 		Integer[] sensorIds = new Integer[enabledSensorKeys.size()];
		 		enabledSensorKeys.toArray(sensorIds);
		 		clone.setSensorIdsEnabled(sensorIds);
		 		
		 		writeConfiguration();
		 		dialog.dispose();
		 	}
		 });
		 btnWriteConfig.setToolTipText("Write the current sensors configuration to the Shimmer device");
		 dialog.getContentPane().add(btnWriteConfig, BorderLayout.SOUTH);
	}

	@Override
	protected void createFrame() {
		 dialog = new JDialog();
		 dialog.setModal(true);
		 dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		 dialog.setTitle("Enable Sensors");
		
		 panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));

		 dialog.getContentPane().add(panel, BorderLayout.CENTER);
	}

	@Override
	protected void createCheckBox(String sensorName,boolean state,int count) {
		// TODO Auto-generated method stub
		 listOfSensors[count] = new JCheckBox(sensorName, state);
		 panel.add((JCheckBox)listOfSensors[count]);
		 ((JCheckBox)listOfSensors[count]).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
//					if(((JCheckBox) listOfSensors[count]).isSelected()) {
//						clone.setSensorEnabledState(sensorKeys[count], true);
//					} else {
//						clone.setSensorEnabledState(sensorKeys[count], false);
//					}
					
					//updateCheckboxes(listOfSensors, clone, sensorKeys);
					
				}
			});
		 
	}

	@Override
	protected void showFrame() {
		// TODO Auto-generated method stub
		 //maybe should be in a different abstract method? lets see how android needs to handles this
		 int dialogHeight = 75+(listOfSensors.length*25);
		 dialog.setSize(300, dialogHeight);
		 
		 dialog.setVisible(true);

	}
	
	
}
