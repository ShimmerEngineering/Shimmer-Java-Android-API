package com.shimmerresearch.simpleexamples;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.pcDriver.ShimmerPC;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class SensorConfigDialog {

	ShimmerPC cloneDevice;
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize(ShimmerPC shimmerDevice) {
		JDialog dialog = new JDialog();
		dialog.setTitle("Sensors Configuration");
		dialog.setSize(300, 500);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JButton btnSav = new JButton("Save");
		btnSav.setToolTipText("Writes the config to the Shimmer");
		dialog.getContentPane().add(btnSav, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		 panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		
//		Box verticalBox = Box.createVerticalBox();
//		panel.add(verticalBox);
				
		cloneDevice = shimmerDevice.deepClone();
		Map<Integer, SensorDetails> sensorMap = cloneDevice.getSensorMap();
		Map<String, ConfigOptionDetailsSensor> configOptionsMap = cloneDevice.getConfigOptionsMap();
		List<String> listOfKeys = new ArrayList<String>();
		
		for(SensorDetails sd : sensorMap.values()) {
			if(sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated!=null && sd.isEnabled()) {
				listOfKeys.addAll(sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated);
			}
		}
		
		//Box[] box = Box.createVerticalBox();
		
		
		for(String key : listOfKeys) {
			ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
			String[] cs = cods.getGuiValues();
			
			//scrollPane.add(arg0)
			Box box = Box.createVerticalBox();
			JLabel label = new JLabel();
			label.setText(key);
			box.add(label);
			
			
				
				if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
					if(cs != null) {
						for(String text : cs) {
							JCheckBox checkBox = new JCheckBox(text, false);
							box.add(checkBox);
						}					
					}
				}				
				else if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD) {
					JPanel textPanel = new JPanel();
					textPanel.setLayout((LayoutManager) new BoxLayout(textPanel, BoxLayout.X_AXIS));
					//textPanel.setLayout((LayoutManager) new FlowLayout(FlowLayout.LEFT));
					textPanel.setSize(50, 15);
					
					JTextField textField = new JTextField();
					textField.setToolTipText("Values are in Hz");
					//textField.setColumns(5);
					
					JButton saveTextButton = new JButton("set");
					textPanel.add(textField);
					textPanel.add(saveTextButton);
					
					//box.setLayout((LayoutManager) new FlowLayout(FlowLayout.LEFT));
					box.add(textPanel);
					box.setSize(10, 15);

					
					System.out.println("It has entered the TEXTFIELD>>>>>>>>>>>!!!!!!!!");
				}

			
			panel.add(box);

			System.out.println("CODS: " + cods.mGuiComponentType);
		}
		
		System.out.println("TEXTFIELD: " + ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD + "\nCOMBOBOX: " + ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX);
		
		dialog.setVisible(true);
		
		
	}
	
	
	
	
	
}
