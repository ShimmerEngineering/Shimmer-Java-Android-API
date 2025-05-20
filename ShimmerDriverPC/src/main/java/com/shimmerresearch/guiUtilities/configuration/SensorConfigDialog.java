package com.shimmerresearch.guiUtilities.configuration;

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

import com.shimmerresearch.bluetoothmanager.guiUtilities.AbstractSensorConfigDialog;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;

import javax.swing.Box;
import javax.swing.BoxLayout;

public class SensorConfigDialog extends AbstractSensorConfigDialog {
	JDialog dialog;
	JPanel panel;
	Box box;
	
	public SensorConfigDialog(ShimmerDevice shimmerDevice, BasicShimmerBluetoothManagerPc bluetoothManager) {
		super(shimmerDevice, bluetoothManager);
	}
	
	/**
	 * Initializes the variables and sets the list of sensor keys to be filtered out and ignored.
	 * @param shimmerDevice
	 * @param bluetoothManager
	 * @param filterKeys
	 */
	public SensorConfigDialog(ShimmerPC shimmerDevice, BasicShimmerBluetoothManagerPc bluetoothManager, List<String> filterKeys) {
		super(shimmerDevice, bluetoothManager);
		setSensorKeysFilter(filterKeys, true);
	}

	
	public static void main(String[] args) {
		
	}
	
	
	private void clearOtherCheckboxes (Object[] checkBox, int selectedCheckbox) {
		for(int i=0; i<checkBox.length; i++) {
			if(i != selectedCheckbox) {
				((JCheckBox)checkBox[i]).setSelected(false);
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

    
	@Override
	public void createComboBox(int numOfOptions,String key,ConfigOptionDetailsSensor cods,Object[] checkBox, boolean isEnabled) {
		// TODO Auto-generated method stub
		String[] cs = cods.getGuiValues();
		
		String currentConfigLabel = getConfigValueLabelFromConfigLabel(key);
		
		for(int i=0; i<numOfOptions; i++) {
			
			if(cs[i].equals(currentConfigLabel)) {
				checkBox[i] = new JCheckBox(cs[i], true);
			} else {
				checkBox[i] = new JCheckBox(cs[i], false);
			}
			
			final int a = i;
			((JCheckBox)checkBox[i]).addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if(((JCheckBox)checkBox[a]).isSelected()) {
						cloneDevice.setConfigValueUsingConfigLabel(key, cods.mConfigValues[a]);
						clearOtherCheckboxes(checkBox, a);
					} else {
						//The current config setting has been selected again: maintain the state of the checkbox as true
						((JCheckBox)checkBox[a]).setSelected(true);
					}
					
				}
			});
			((JCheckBox) checkBox[i]).setEnabled(isEnabled);
			box.add((JCheckBox) checkBox[i]);
			
		}
		dialogHeight = dialogHeight + (checkBox.length*20) + 20;
	
	}

	
	@Override
	public void createEditText(String key, boolean isEnabled) {

//		JPanel textPanel = new JPanel();
//		textPanel.setLayout((LayoutManager) new BoxLayout(textPanel, BoxLayout.X_AXIS));
//		textPanel.setMaximumSize(new Dimension(200, 20));
	
	Box textFieldBox = Box.createVerticalBox();
	
	
		
		JTextField textField = new JTextField();
		textField.setToolTipText("Values are in Hz");
		textField.setText((String) cloneDevice.getConfigValueUsingConfigLabel(key));
		textField.setMaximumSize(new Dimension(150, 20));
		textField.setEnabled(isEnabled);
		textFieldBox.add(textField);
		/*
		JButton saveTextButton = new JButton("set");
		saveTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String text  = textField.getText();
				if(text.matches("[0-9.]*")) {
					cloneDevice.setConfigValueUsingConfigLabel(key, text);
					System.out.println("The numerical value is: \n\n\n" + cloneDevice.getConfigValueUsingConfigLabel(key));
				
				} else{
					
				}
			}
		});
		
		textFieldBox.add(saveTextButton);
		*/

//		textPanel.add(textField);
//		textPanel.add(saveTextButton);
		
		//textFieldBox.setMaximumSize(new Dimension(200, 20));
		box.add(textFieldBox);
		box.setSize(10, 15);
		dialogHeight = dialogHeight + 50;
	
		
	}

	@Override
	public void createLabel(String labelName) {
		// TODO Auto-generated method stub
		JLabel label = new JLabel();
		label.setText(labelName);
		box.add(label);
		
	}

	@Override
	public void createFrame() {
		// TODO Auto-generated method stub
		panel = new JPanel();
		box = Box.createVerticalBox();
		dialog = new JDialog();
		dialog.setTitle("Sensors Configuration");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				
		JButton btnSav = new JButton("Save");
		btnSav.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		 		//TODO: Write the config from clone to shimmer here
		 		writeConfiguration();
		 		dialog.dispose();
			}
		});
		btnSav.setToolTipText("Writes the config to the Shimmer");
		dialog.getContentPane().add(btnSav, BorderLayout.SOUTH);
		

		panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
	}

	@Override
	public void showFrame() {
		panel.add(box);
		// TODO Auto-generated method stub
		JScrollPane scroller = new JScrollPane(panel);
		scroller.setVerticalScrollBarPolicy(ScrollPaneLayout.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.getVerticalScrollBar().setUnitIncrement(20);
		dialog.getContentPane().add(scroller, BorderLayout.CENTER);
		
		System.out.println("TEXTFIELD: " + ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD + "\nCOMBOBOX: " + ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX);
		dialog.setSize(300, dialogHeight+75);
		dialog.setVisible(true);
	}

	
	/**
	 * Call this to initialize and display the dialog.
	 */
	public void showDialog() {
		//Filter out the sensors we don't want to display before initializing the dialog:
		List<String> filterList = new ArrayList<String>();
		//filterList.add("Wide Range Accel Rate");
		setSensorKeysFilter(filterList, true);
		
		//Filter out the sensors config option we want to display but to disable before initializing the dialog:
		List<String> displayButDisableFilterList = new ArrayList<String>();
		displayButDisableFilterList.add("Wide Range Accel Rate");
		displayButDisableFilterList.add("Mag Rate");
		displayButDisableFilterList.add("Gyro Sampling Rate");
		displayButDisableFilterList.add("Alternate Mag Rate");
		displayButDisableFilterList.add("High G Accel Rate");
		setSensorDisplayButDisableKeysFilter(displayButDisableFilterList, true);

		createFrame();
		initialize();
		showFrame();
	}
	
	
	
	
}

