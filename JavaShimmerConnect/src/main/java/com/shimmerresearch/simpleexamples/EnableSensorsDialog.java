package com.shimmerresearch.simpleexamples;

import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.pcDriver.ShimmerPC;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnableSensorsDialog {

	ShimmerPC shimmer;
	
	public EnableSensorsDialog(ShimmerPC shimmerPC) {
		shimmer = shimmerPC;
	}
	
	public static void main(String[] args) {
		
		
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
		 JDialog dialog = new JDialog();
		 
		 JButton btnWriteConfig = new JButton("Save");
		 btnWriteConfig.setToolTipText("Write the current sensors configuration to the Shimmer device");
		 dialog.getContentPane().add(btnWriteConfig, BorderLayout.SOUTH);
		 
		 //Setup the list of sensors checkboxes
		 JCheckBox[] listOfSensors;
		 
		 Map<Integer, SensorDetails> sensorMap = shimmer.getSensorMap();
		 List<SensorDetails> enabledSensorsList = shimmer.getListOfEnabledSensors();
		 
		 int count = sensorMap.size();
		 int count2 = count;
		 
		 List<SensorDetails> sensorList = new ArrayList<SensorDetails>();
		 
		 for(SensorDetails details : sensorMap.values()) {
			 sensorList.add(details);
		 }
		 
		 List<SensorDetails> sensorList2 = sensorList;
		 sensorList2.size();
	}
	
	
}
