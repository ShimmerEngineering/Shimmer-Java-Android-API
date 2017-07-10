package com.shimmerresearch.simpleexamples;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import javax.swing.Box;
import javax.swing.BoxLayout;

public class SensorConfigDialog {

	ShimmerPC cloneDevice;
	BasicShimmerBluetoothManagerPc bluetoothManager;
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize(ShimmerPC shimmerDevice, BasicShimmerBluetoothManagerPc bluetoothManager) {
		JDialog dialog = new JDialog();
		dialog.setTitle("Sensors Configuration");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				
		JButton btnSav = new JButton("Save");
		btnSav.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		 		//TODO: Write the config from clone to shimmer here
		 		AssembleShimmerConfig.generateSingleShimmerConfig(cloneDevice, COMMUNICATION_TYPE.BLUETOOTH);
		 		bluetoothManager.configureShimmer(cloneDevice);
		 		dialog.dispose();
			}
		});
		btnSav.setToolTipText("Writes the config to the Shimmer");
		dialog.getContentPane().add(btnSav, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
		//dialog.getContentPane().add(panel, BorderLayout.CENTER);
		
		//JScrollPane scrollPane = new JScrollPane();
		//scrollPane.setLayout((LayoutManager) new ScrollPaneLayout());
		//scrollPane.setVerticalScrollBarPolicy(ScrollPaneLayout.VERTICAL_SCROLLBAR_AS_NEEDED);
		//dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
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
		int dialogHeight = 0;
		
		for(String key : listOfKeys) {
			ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
			
			if(cods != null) {
			String[] cs = cods.getGuiValues();
			
			//System.out.println("For key: " + key + " configValue is: " + cloneDevice.getConfigValueUsingConfigLabel(key));
			
			Box box = Box.createVerticalBox();
			JLabel label = new JLabel();
			label.setText(key);
			box.add(label);
			
			if(cs != null) {
				int numOfCheckboxes = cs.length;
				JCheckBox[] checkBox = new JCheckBox[numOfCheckboxes];
				if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
					
					String currentConfigLabel = getConfigValueLabelFromConfigLabel(key);
					
					for(int i=0; i<numOfCheckboxes; i++) {
						
						if(cs[i].equals(currentConfigLabel)) {
							checkBox[i] = new JCheckBox(cs[i], true);
						} else {
							checkBox[i] = new JCheckBox(cs[i], false);
						}
						
						final int a = i;
						checkBox[i].addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								
								if(checkBox[a].isSelected()) {
									cloneDevice.setConfigValueUsingConfigLabel(key, cods.mConfigValues[a]);
									clearOtherCheckboxes(checkBox, a);
								} else {
									//The current config setting has been selected again: maintain the state of the checkbox as true
									checkBox[a].setSelected(true);
								}
								
							}
						});
						
						box.add(checkBox[i]);
						
					}
				}
				
				dialogHeight = dialogHeight + (checkBox.length*20) + 20;
			}
			
			if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD) {
//					JPanel textPanel = new JPanel();
//					textPanel.setLayout((LayoutManager) new BoxLayout(textPanel, BoxLayout.X_AXIS));
//					textPanel.setMaximumSize(new Dimension(200, 20));
				
				Box textFieldBox = Box.createVerticalBox();
				
				
					
					JTextField textField = new JTextField();
					textField.setToolTipText("Values are in Hz");
					textField.setText((String) cloneDevice.getConfigValueUsingConfigLabel(key));
					textField.setMaximumSize(new Dimension(150, 20));
					
					textFieldBox.add(textField);
					
					JButton saveTextButton = new JButton("set");
					saveTextButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							
							String text  = textField.getText();
							if(text.matches("[0-9.]*")) {
								cloneDevice.setConfigValueUsingConfigLabel(key, text);
								System.out.println("The numerical value is: \n\n\n" + cloneDevice.getConfigValueUsingConfigLabel(key));
							
							} else{
								JOptionPane.showMessageDialog(dialog, 
										"Error! The value entered is not a numeric value", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					});
					
					textFieldBox.add(saveTextButton);

//					textPanel.add(textField);
//					textPanel.add(saveTextButton);
					
					//textFieldBox.setMaximumSize(new Dimension(200, 20));
					box.add(textFieldBox);
					box.setSize(10, 15);
					dialogHeight = dialogHeight + 50;
				}
			
			panel.add(box);
			//scrollPane.add(box);
			
			}
		}
		
		JScrollPane scroller = new JScrollPane(panel);
		scroller.setVerticalScrollBarPolicy(ScrollPaneLayout.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.getVerticalScrollBar().setUnitIncrement(20);
		dialog.getContentPane().add(scroller, BorderLayout.CENTER);
		
		System.out.println("TEXTFIELD: " + ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD + "\nCOMBOBOX: " + ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX);
		dialog.setSize(300, dialogHeight+75);
		dialog.setVisible(true);
		
	}
	
	private void clearOtherCheckboxes (JCheckBox[] checkBox, int selectedCheckbox) {
		for(int i=0; i<checkBox.length; i++) {
			if(i != selectedCheckbox) {
				checkBox[i].setSelected(false);
			}
		}
	}
	
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

	
	
	
	
}

