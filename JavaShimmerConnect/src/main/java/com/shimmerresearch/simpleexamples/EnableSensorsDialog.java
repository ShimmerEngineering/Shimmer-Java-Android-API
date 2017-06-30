package com.shimmerresearch.simpleexamples;

import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.pcDriver.ShimmerPC;

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
import javax.swing.JList;

public class EnableSensorsDialog {

	ShimmerPC shimmer;
	private static JDialog dialog = new JDialog();
	
	public EnableSensorsDialog(ShimmerPC shimmerPC) {
		shimmer = shimmerPC;
	}
	
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
		 dialog.setSize(600, 500);
		 
		 JButton btnWriteConfig = new JButton("Save");
		 btnWriteConfig.setToolTipText("Write the current sensors configuration to the Shimmer device");
		 dialog.getContentPane().add(btnWriteConfig, BorderLayout.SOUTH);
		 
		 //JPanel panel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
		 JPanel panel = new JPanel();
		 panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));

		 dialog.getContentPane().add(panel, BorderLayout.CENTER);
		 
		 
		 //Create a clone device
		 ShimmerPC clone = shimmer.deepClone();
		 
		 //Setup the list of sensors checkboxes
		 
		 Map<Integer, SensorDetails> sensorMap = clone.getSensorMap();
		 int count = 0;
		 
		 //Check how many sensors the device is compatible with
		 for(SensorDetails details : sensorMap.values()) {
			 if(clone.isVerCompatibleWithAnyOf(details.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
				 count++;
			 }
		 }
		 
		 String arraySensors[] = new String[count];
		 final boolean[] listEnabled = new boolean[count];
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
				 //dialog.getContentPane().add(listOfSensors[count], BorderLayout.NORTH);
				 //listOfSensors[count].setBounds(10, 10+(20*count), 150, 15);
				 sensorKeys[count] = key;
				 count++;
			 }
		 }
		 
		 
		 dialog.setVisible(true);

		 		 
		 
		 
	}
	
	
}
