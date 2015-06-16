package com.shimmerresearch.exgConfig;

import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.CHIP_INDEX;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.SettingType;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class PanelExGConfigOptions extends JPanel {
	
	private Font labelFont = new Font("Calibri", Font.PLAIN, 13);
	
	PanelExGConfigBytes pnlExGBytes;
	ShimmerAbstract selectedShimmer;

//	ExGConfigBytesDetails eCBD; 
	
	HashMap<JPanel,String[]> configOptionsPanelMap = new HashMap<JPanel,String[]>();
	
	private String[] reg0ChannelRegister1Options = {
			ExGConfigBytesDetails.REG1_CONVERSION_MODES,
			ExGConfigBytesDetails.REG1_DATA_RATE};
	private String[] reg1ChannelRegister2Options = {
			ExGConfigBytesDetails.REG2_LEAD_OFF_COMPARATORS,
			ExGConfigBytesDetails.REG2_REFERENCE_BUFFER,
			ExGConfigBytesDetails.REG2_VOLTAGE_REFERENCE,
			ExGConfigBytesDetails.REG2_OSCILLATOR_CLOCK_CONNECTION,
			ExGConfigBytesDetails.REG2_TEST_SIGNAL_SELECTION,
			ExGConfigBytesDetails.REG2_TEST_SIGNAL_FREQUENCY};
	private String[] reg2LeadOffControlRegister = {
			ExGConfigBytesDetails.REG3_COMPARATOR_THRESHOLD,
			ExGConfigBytesDetails.REG3_LEAD_OFF_CURRENT,
			ExGConfigBytesDetails.REG3_LEAD_OFF_FREQUENCY};
	private String[] reg3Channel1Settings = {
			ExGConfigBytesDetails.REG4_CHANNEL_1_POWER_DOWN,
			ExGConfigBytesDetails.REG4_CHANNEL_1_PGA_GAIN,
			ExGConfigBytesDetails.REG4_CHANNEL_1_INPUT_SELECTION};
	private String[] reg4Channel2Settings = {
			ExGConfigBytesDetails.REG5_CHANNEL_2_POWER_DOWN,
			ExGConfigBytesDetails.REG5_CHANNEL_2_PGA_GAIN,
			ExGConfigBytesDetails.REG5_CHANNEL_2_INPUT_SELECTION};
	
	private String[][] globalChannelRegisters = {
			reg0ChannelRegister1Options,
			reg1ChannelRegister2Options,
			reg2LeadOffControlRegister};
	private String[][] channelControlRegisters = {
			reg3Channel1Settings,
			reg4Channel2Settings};
	private String[][][] tab1ChannelRegisters = {
			globalChannelRegisters,
			channelControlRegisters};

	private String[] reg5LoffP = {
			ExGConfigBytesDetails.REG6_CH2_RLD_POS_INPUTS,
			ExGConfigBytesDetails.REG6_CH1_RLD_POS_INPUTS};
	private String[] reg5LoffN = {
			ExGConfigBytesDetails.REG6_CH2_RLD_NEG_INPUTS,
			ExGConfigBytesDetails.REG6_CH1_RLD_NEG_INPUTS};
	private String[] reg6Flip = {
			ExGConfigBytesDetails.REG7_CH2_FLIP_CURRENT,
			ExGConfigBytesDetails.REG7_CH1_FLIP_CURRENT};
	private String[] reg7LoffStat = {
			ExGConfigBytesDetails.REG8_CLOCK_DIVIDER_SELECTION};

	private String[] reg6RldP = {
			ExGConfigBytesDetails.REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS,
			ExGConfigBytesDetails.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS};
	private String[] reg6RldN = {
			ExGConfigBytesDetails.REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS,
			ExGConfigBytesDetails.REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS};
	private String[] reg5RldSense = {
			ExGConfigBytesDetails.REG6_PGA_CHOP_FREQUENCY,
			ExGConfigBytesDetails.REG6_RLD_BUFFER_POWER,
			ExGConfigBytesDetails.REG6_RLD_LEAD_OFF_SENSE_FUNCTION};
	
	private String[][] leadOffDetectionAndCurrentDirectioControlRegisters = {
			reg5LoffP,
			reg5LoffN,
			reg6Flip,
			reg7LoffStat};
	private String[][] rldDerivationControlRegisters = {
			reg6RldP,
			reg6RldN,
			reg5RldSense};
	private String[][][] tab2LoffAndRld = {
			leadOffDetectionAndCurrentDirectioControlRegisters,
			rldDerivationControlRegisters};
	
	private String[] reg8RespirationControlReg1 = {
			ExGConfigBytesDetails.REG9_RESPIRATION_DEMOD_CIRCUITRY,
			ExGConfigBytesDetails.REG9_RESPIRATION_MOD_CIRCUITRY,
			ExGConfigBytesDetails.REG9_RESPIRATION_PHASE,
			ExGConfigBytesDetails.REG9_RESPIRATION_CONTROL};
	private String[] reg9RespirationControlReg2 = {
			ExGConfigBytesDetails.REG10_RESPIRATION_CALIBRATION,
			ExGConfigBytesDetails.REG10_RESPIRATION_CONTROL_FREQUENCY,
			ExGConfigBytesDetails.REG10_RLD_REFERENCE_SIGNAL};
	private String[][] respirationControlRegisters = {
			reg8RespirationControlReg1,
			reg9RespirationControlReg2};
	private String[][][] tab3RespirationRegisters = {
			respirationControlRegisters};
	
	private String[][][][] allTabs = {
			tab1ChannelRegisters,
			tab2LoffAndRld,
			tab3RespirationRegisters};

	private String[] tabTitles = {
			"Channel Registers",
			"LOFF and RLD",
			"Respiration Registers"};

	
	public PanelExGConfigOptions() {
		super();
		setLayout(new BorderLayout(0, 0));
		
//		configOptionsPanelMap.put(pnlConfigOptionsGeneral, s3ConfigOptionKeysGeneral);
//		configOptionsPanelMap.put(pnlConfigOptionsSync, s3ConfigOptionKeysSync);

		
		selectedShimmer = new ShimmerAbstract();
		selectedShimmer.initialise(HW_ID.SHIMMER_3);
//		selectedShimmer.setDefaultRespirationConfiguration();
		selectedShimmer.setDefaultECGConfiguration();
//		selectedShimmer.setEXGTestSignal();
//		selectedShimmer.setEXGCustom();
		
		
		pnlExGBytes = new PanelExGConfigBytes(selectedShimmer);
		add(pnlExGBytes, BorderLayout.SOUTH);
		

		JPanel panelCenter = new JPanel();
		add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new GridLayout(0, 1, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		panelCenter.add(tabbedPane);
		
//		// Create vertical labels to render tab titles
//		JLabel labTab1 = new JLabel("Tab #1");
//		labTab1.setUI(new VerticalLabelUI(false)); // true/false to make it upwards/downwards
//		tabbedPane.setTabComponentAt(0, labTab1); // For component1
		
		for(int i=0;i<allTabs.length;i++){
			String[][][] tabContents = allTabs[i];

			JPanel pnlTab = new JPanel();
			tabbedPane.addTab(tabTitles[i], null, pnlTab, null);
			pnlTab.setLayout(new GridLayout(0, 2, 0, 0));
			
			for(int x=1;x<=2;x++){
				JPanel chipSettings = new JPanel();
				chipSettings.setLayout(new GridLayout(0, 1, 0, 0));
				chipSettings.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				
				HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = selectedShimmer.getMapOfExGSettingsChip1();
				if(x==2){
					mMapOfExGSettingsToUse = selectedShimmer.getMapOfExGSettingsChip2();
				}
				
				
				for(String[][] bytePanelCat:tabContents){
					
					JPanel bytePanelParent = new JPanel();
					
					for(String[] bytePanelChild:bytePanelCat){
						JPanel bytePanel = new JPanel();
						bytePanel.setLayout(new GridLayout(0, 1, 0, 0));
						bytePanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
//						bytePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

						for(String key:bytePanelChild){
							ExGConfigOptionDetails setting = mMapOfExGSettingsToUse.get(key);
							if(setting.settingType == SettingType.COMBOBOX){
							    JPanel panel = new JPanel(new BorderLayout());

							    panel.setBackground(null);
							    //panel.setName(stringKey + "_panel");
							    
						        JLabel lbl = new JLabel(key);
						        lbl.setFont(labelFont);
						        lbl.setBackground(null);
						        //lbl.setName(stringKey + "_label");
						        String labelAlignment = "North";

								JComboBox<String> cmBx = new JComboBox(setting.GuiValues);
						        cmBx.setName(x+key);
						        cmBx.setToolTipText(key);
						        cmBx.setFont(labelFont);
						        cmBx.setBackground(null);
						        cmBx.setFocusable(false);
						        
						        updateGuiComponentFromConfig(cmBx);
						        
						        cmBx.addActionListener(new ActionListener() {
						            @Override
						            public void actionPerformed(ActionEvent e) {
						                JComboBox comboBox = (JComboBox)e.getSource();
					
//						                selectedShimmer.eCBD.setValue(comboBox.getName(), comboBox.geselectedShimmer.selectedShimmer.eCBD.ctedItem())
						                updateConfigFromGuiComponent(comboBox);
				//	
						                pnlExGBytes.updateFromShimmer(selectedShimmer);
//						                if (selectedDockedShimmer instanceof SlotDetails){
//						                	((SlotDetails)selectedDockedShimmer).refreshShimmerInfoMemBytes();
//						                	updateShimmerInfoMemBytesPanel();
//						                }
//										updateShimmerConfigPanel();
//						        		updateCalibrationDisplayPanel();
						            }
						        });
						        
						        cmBx.setEnabled(true);
//					    		componentMap.put(stringKey,cmBx);

						        panel.add(lbl, labelAlignment);
						        panel.add(cmBx);
						        bytePanel.add(panel);
							}
							else if(setting.settingType == SettingType.CHECKBOX){
				    	        JCheckBox cBx = new JCheckBox(key);
				    	        cBx.setFont(labelFont);
				    	        cBx.setBackground(null);
				    	        cBx.setFocusable(false);
				    	        cBx.setName(x+key);
				    	        cBx.setToolTipText(key);
				    	        
						        updateGuiComponentFromConfig(cBx);

				    	        cBx.addActionListener(new ActionListener() {
				    	        	public void actionPerformed(ActionEvent arg0) {
				    	                JCheckBox checkBox = (JCheckBox)arg0.getSource();
				    	                updateConfigFromGuiComponent(checkBox);
//				    	                
						                pnlExGBytes.updateFromShimmer(selectedShimmer);

//				    	                if (selectedDockedShimmer instanceof SlotDetails){
//						                	((SlotDetails)selectedDockedShimmer).refreshShimmerInfoMemBytes();
//						                	updateShimmerInfoMemBytesPanel();
//						                }
//				    	        		updateShimmerConfigPanel();
//				    	        		updateCalibrationDisplayPanel();
				    	            }
				    	        });
//				                updateGuiComponentFromConfig(cBx);
				                
//				                // Special case for single touch start, supported in the driver but not in the GUI
//		    	    	    	switch(stringKey){
//		    					case(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START):
//		    						enabledState = false;
//		    						break;
//		    					default:
//			    					break;
//		    	    	    	}
		    	    	    	
						        cBx.setEnabled(true);
//					    		componentMap.put(stringKey,cBx);

						        bytePanel.add(cBx);
							}
							
							bytePanelParent.add(bytePanel);
						}
						chipSettings.add(bytePanelParent);
					}
					

					pnlTab.add(chipSettings);
				}
				
			}
		}		

	}
	
	
	private void updateGuiComponentFromConfig(Component myComponent) {
		Object returnedValue = null;
		String name = myComponent.getName();
		int chipNumber = Integer.parseInt(name.substring(0, 1)); 
		String componentName = name.substring(1); 
		
		HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = selectedShimmer.getMapOfExGSettingsChip1();
		if(chipNumber==2){
			mMapOfExGSettingsToUse = selectedShimmer.getMapOfExGSettingsChip2();
		}

		
		if(myComponent instanceof JCheckBox) {
			JCheckBox cBx = (JCheckBox) myComponent;

			if(chipNumber==1){
				returnedValue = mMapOfExGSettingsToUse.get(componentName).valueBool;
			}
			else{
				returnedValue = mMapOfExGSettingsToUse.get(componentName).valueBool;
			}

			if(returnedValue != null) {
				boolean state = (boolean)returnedValue;
				
				//Remove action listener, change value and then add action listener again
				ActionListener[] aL = cBx.getActionListeners();
				if(aL.length>0) {
					cBx.removeActionListener(aL[0]);
				}
				cBx.setSelected(state);
				if(aL.length>0) {
					cBx.addActionListener(aL[0]);
				}
			}
			
		}
		else if(myComponent instanceof JComboBox) {
			JComboBox<String> cmBx = (JComboBox) myComponent;
			
			if(chipNumber==1){
				returnedValue = mMapOfExGSettingsToUse.get(componentName).valueInt;
			}
			else{
				returnedValue = mMapOfExGSettingsToUse.get(componentName).valueInt;
			}

			if(returnedValue != null) {
				int configValue = (int)returnedValue; 
				
				
		    	switch(componentName){
	    		case(ExGConfigBytesDetails.REG9_RESPIRATION_PHASE):
//	    		case(ExGConfigBytesDetails.RE):
//	    			selectedDockedShimmer.checkConfigOptionValues(componentName);
//			        String[] guiValues = configOptionsMap.get(componentName).getGuiValues();
//	    		    DefaultComboBoxModel<String> cmbxModel = new DefaultComboBoxModel<String>(guiValues);
//	    		    cmBx.setModel(cmbxModel);
	    			break;
		        default:
		        	break;
		    	}
	    	
		    	if(configValue >= 0) {
		    		
					int itemIndex = Arrays.asList(mMapOfExGSettingsToUse.get(componentName).getConfigValues()).indexOf(configValue);
					int itemIndexInCmbx = 0;
					if(itemIndex<0) {
//						consolePrintLn("ERROR");
						itemIndexInCmbx = 0;				
					}
					else {
						String searchString = Arrays.asList(mMapOfExGSettingsToUse.get(componentName).getGuiValues()).get(itemIndex);
						// Needed if range of values changes - for example Respiration detect phase range dependent on respiration detect freq.
						itemIndexInCmbx = ((DefaultComboBoxModel<String>) cmBx.getModel()).getIndexOf(searchString);
						if(itemIndexInCmbx<0) {
							itemIndexInCmbx = 0;
						}
					}
	
					//Remove action listener, change value and then add action listener again
					ActionListener[] aL = cmBx.getActionListeners();
					if(aL.length>0) {
						cmBx.removeActionListener(aL[0]);
					}
		    		cmBx.setSelectedIndex(itemIndexInCmbx);
					if(aL.length>0) {
						cmBx.addActionListener(aL[0]);
					}
		    	}
			}
		}
			
	}

	private void updateConfigFromGuiComponent(Component myComponent) {
		Object returnedValue = null;
		String name = myComponent.getName();
		int chipNumber = Integer.parseInt(name.substring(0, 1)); 
		String componentName = name.substring(1); 

		CHIP_INDEX chipIndex = CHIP_INDEX.CHIP1;
		HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = selectedShimmer.getMapOfExGSettingsChip1();
		if(chipNumber==2){
			chipIndex = CHIP_INDEX.CHIP2;
			mMapOfExGSettingsToUse = selectedShimmer.getMapOfExGSettingsChip2();
		}

		if(myComponent instanceof JCheckBox) {
			JCheckBox cBx = (JCheckBox) myComponent;
			boolean state = cBx.isSelected();
			
			selectedShimmer.setExgPropertyValue(chipIndex,componentName,state);
		}
		else if(myComponent instanceof JComboBox) {
			JComboBox cmBx = (JComboBox) myComponent;
		
			int itemIndex = Arrays.asList(mMapOfExGSettingsToUse.get(componentName).getGuiValues()).indexOf(cmBx.getSelectedItem());
			int configValue = 0;
			if(itemIndex==-1) {
				configValue = mMapOfExGSettingsToUse.get(componentName).getConfigValues()[0];				
			}
			else {
				configValue = mMapOfExGSettingsToUse.get(componentName).getConfigValues()[itemIndex];				
			}
			
			selectedShimmer.setExgPropertyValue(chipIndex,componentName,configValue);
		}

	}	
	
	
	public class ShimmerAbstract extends ShimmerObject{

		public void initialise(int hardwareVersion) {
			super.mHardwareVersion = hardwareVersion;
			super.sensorAndConfigMapsCreate();
		}

		@Override
		public void setDefaultEMGConfiguration(){
			super.setDefaultEMGConfiguration();
		}

		@Override
		public void setDefaultECGConfiguration(){
			super.setDefaultECGConfiguration();
		}

		
		@Override
		public void setDefaultRespirationConfiguration(){
			super.setDefaultRespirationConfiguration();
		}

		@Override
		public void setEXGTestSignal(){
			super.setEXGTestSignal();
		}

		@Override
		public void setEXGCustom(){
			super.setEXGCustom();
		}

		@Override
		public void setExgPropertyValue(CHIP_INDEX chipIndex, String propertyName, Object value){
			super.setExgPropertyValue(chipIndex, propertyName, value);
		}
		
		
		@Override
		protected void checkBattery() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	PanelExGConfigOptions ex = new PanelExGConfigOptions();
                //ex.setVisible(true);
                JFrame f = new JFrame();
                f.getContentPane().add(ex);
                f.setSize(1100, 730);
                f.setVisible(true);
                f.addWindowListener(new WindowAdapter() {
        		    @Override
        		    public void windowClosing(WindowEvent e) {
        		        System.exit(1);
        		    }
        		});
            }
        });
	}

}
