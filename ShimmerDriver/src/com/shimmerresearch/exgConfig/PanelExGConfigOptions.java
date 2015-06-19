package com.shimmerresearch.exgConfig;

import java.awt.Component;
import java.awt.Font;
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
import javax.swing.SwingUtilities;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerObject;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTING;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.CHIP_INDEX;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.SettingType;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.HashMap;


public class PanelExGConfigOptions extends JPanel {
	
	private Font labelFont = new Font("Calibri", Font.PLAIN, 13);
	
	static ShimmerObject mSelectedShimmer;

	HashMap<JPanel,String[]> configOptionsPanelMap = new HashMap<JPanel,String[]>();
	HashMap<String, Component> componentMap = new HashMap<String, Component>();

	private SettingGrouping reg0ChannelRegister1Options = new SettingGrouping(
			ExGConfigBytesDetails.REG1, new String[]{
				ExGConfigBytesDetails.REG1_CONVERSION_MODES,
				ExGConfigBytesDetails.REG1_DATA_RATE});
	private SettingGrouping reg1ChannelRegister2Options = new SettingGrouping(
			ExGConfigBytesDetails.REG2, new String[]{
				ExGConfigBytesDetails.REG2_LEAD_OFF_COMPARATORS,
				ExGConfigBytesDetails.REG2_REFERENCE_BUFFER,
				ExGConfigBytesDetails.REG2_VOLTAGE_REFERENCE,
				ExGConfigBytesDetails.REG2_OSCILLATOR_CLOCK_CONNECTION,
				ExGConfigBytesDetails.REG2_TEST_SIGNAL_SELECTION,
				ExGConfigBytesDetails.REG2_TEST_SIGNAL_FREQUENCY});
	private SettingGrouping reg2LeadOffControlRegister = new SettingGrouping(
			ExGConfigBytesDetails.REG3, new String[]{
				ExGConfigBytesDetails.REG3_COMPARATOR_THRESHOLD,
				ExGConfigBytesDetails.REG3_LEAD_OFF_CURRENT,
				ExGConfigBytesDetails.REG3_LEAD_OFF_FREQUENCY});
	private SettingGrouping reg3Channel1Settings = new SettingGrouping(
			ExGConfigBytesDetails.REG4, new String[]{
				ExGConfigBytesDetails.REG4_CHANNEL_1_POWER_DOWN,
				ExGConfigBytesDetails.REG4_CHANNEL_1_PGA_GAIN,
				ExGConfigBytesDetails.REG4_CHANNEL_1_INPUT_SELECTION});
	private SettingGrouping reg4Channel2Settings = new SettingGrouping(
			ExGConfigBytesDetails.REG5, new String[]{
				ExGConfigBytesDetails.REG5_CHANNEL_2_POWER_DOWN,
				ExGConfigBytesDetails.REG5_CHANNEL_2_PGA_GAIN,
				ExGConfigBytesDetails.REG5_CHANNEL_2_INPUT_SELECTION});
	private PanelGrouping globalChannelRegisters = new PanelGrouping(
			"", new SettingGrouping[]{
					reg0ChannelRegister1Options,
					reg1ChannelRegister2Options,
					reg2LeadOffControlRegister});
	private PanelGrouping channelControlRegisters = new PanelGrouping(
			"", new SettingGrouping[]{
					reg3Channel1Settings,
					reg4Channel2Settings});
	private TabGrouping tab1ChannelRegisters = new TabGrouping(
			"Channel Registers", new PanelGrouping[]{
					globalChannelRegisters,
					channelControlRegisters});

	private SettingGrouping reg5LoffP = new SettingGrouping(
			"", new String[]{
				ExGConfigBytesDetails.REG6_CH2_RLD_POS_INPUTS,
				ExGConfigBytesDetails.REG6_CH1_RLD_POS_INPUTS});
	private SettingGrouping reg5LoffN = new SettingGrouping(
			"", new String[]{
				ExGConfigBytesDetails.REG6_CH2_RLD_NEG_INPUTS,
				ExGConfigBytesDetails.REG6_CH1_RLD_NEG_INPUTS});
	private SettingGrouping reg6Flip = new SettingGrouping(
			"", new String[]{
				ExGConfigBytesDetails.REG7_CH2_FLIP_CURRENT,
				ExGConfigBytesDetails.REG7_CH1_FLIP_CURRENT});
	private SettingGrouping reg7LoffStat = new SettingGrouping(
			"LOFF STAT", new String[]{
				ExGConfigBytesDetails.REG8_CLOCK_DIVIDER_SELECTION});

	private SettingGrouping reg6RldP = new SettingGrouping(
			"", new String[]{
				ExGConfigBytesDetails.REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS,
				ExGConfigBytesDetails.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS});
	private SettingGrouping reg6RldN = new SettingGrouping(
			"", new String[]{
				ExGConfigBytesDetails.REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS,
				ExGConfigBytesDetails.REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS});
	private SettingGrouping reg5RldSense = new SettingGrouping(
			"RLD SENS", new String[]{
				ExGConfigBytesDetails.REG6_PGA_CHOP_FREQUENCY,
				ExGConfigBytesDetails.REG6_RLD_BUFFER_POWER,
				ExGConfigBytesDetails.REG6_RLD_LEAD_OFF_SENSE_FUNCTION});
	
	private PanelGrouping leadOffDetectionAndCurrentDirectionControlRegisters = new PanelGrouping(
			"Lead-Off Detection and Current Direction Control Registers", new SettingGrouping[]{
					reg5LoffP,
					reg5LoffN,
					reg6Flip,
					reg7LoffStat});
	private PanelGrouping rldDerivationControlRegisters = new PanelGrouping(
			"Right Leg Drive Derivation Control Registers", new SettingGrouping[]{
					reg6RldP,
					reg6RldN,
					reg5RldSense});
	
	private TabGrouping tab2LoffAndRld = new TabGrouping(
			"LOFF and RLD", new PanelGrouping[]{
					leadOffDetectionAndCurrentDirectionControlRegisters,
					rldDerivationControlRegisters});

	private SettingGrouping reg8RespirationControlReg1 = new SettingGrouping(
			ExGConfigBytesDetails.REG9, new String[]{
				ExGConfigBytesDetails.REG9_RESPIRATION_DEMOD_CIRCUITRY,
				ExGConfigBytesDetails.REG9_RESPIRATION_MOD_CIRCUITRY,
				ExGConfigBytesDetails.REG9_RESPIRATION_PHASE,
				ExGConfigBytesDetails.REG9_RESPIRATION_CONTROL});
	private SettingGrouping reg9RespirationControlReg2 = new SettingGrouping(
			ExGConfigBytesDetails.REG10, new String[]{
				ExGConfigBytesDetails.REG10_RESPIRATION_CALIBRATION,
				ExGConfigBytesDetails.REG10_RESPIRATION_CONTROL_FREQUENCY,
				ExGConfigBytesDetails.REG10_RLD_REFERENCE_SIGNAL});

	private PanelGrouping respirationControlRegisters = new PanelGrouping(
			"Respiration Control Registers", new SettingGrouping[]{
					reg8RespirationControlReg1,
					reg9RespirationControlReg2});

	private TabGrouping tab3RespirationRegisters = new TabGrouping(
			"Respiration Registers", new PanelGrouping[]{
					respirationControlRegisters});
	
	private TabGrouping[] allTabs = {
			tab1ChannelRegisters,
			tab2LoffAndRld,
			tab3RespirationRegisters};

	public class UnsupportedOption{
		public CHIP_INDEX chipIndex;
		public String exgSetting;
		public UnsupportedOption(CHIP_INDEX chipIndex, String exgSetting){
			this.chipIndex = chipIndex;
			this.exgSetting = exgSetting;
		}
	}
	
	private UnsupportedOption[] unsupportedOptions = new UnsupportedOption[]{
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG9_RESPIRATION_DEMOD_CIRCUITRY),
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG9_RESPIRATION_MOD_CIRCUITRY),
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG9_RESPIRATION_PHASE),
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG9_RESPIRATION_CONTROL),
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG10_RESPIRATION_CALIBRATION),
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG10_RESPIRATION_CONTROL_FREQUENCY),
			new UnsupportedOption(CHIP_INDEX.CHIP1,ExGConfigBytesDetails.REG10_RLD_REFERENCE_SIGNAL),
			
			new UnsupportedOption(CHIP_INDEX.CHIP2,ExGConfigBytesDetails.REG10_RLD_REFERENCE_SIGNAL),
	};
	
	private ExGConfigChangeCallback mCallback;

	
	public PanelExGConfigOptions(ShimmerObject selectedShimmer) {
		
		mSelectedShimmer = selectedShimmer;
//		configOptionsPanelMap.put(pnlConfigOptionsGeneral, s3ConfigOptionKeysGeneral);
//		configOptionsPanelMap.put(pnlConfigOptionsSync, s3ConfigOptionKeysSync);

//		JPanel panelCenter = new JPanel();
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		add(tabbedPane);
		
//		// Create vertical labels to render tab titles
//		JLabel labTab1 = new JLabel("Tab #1");
//		labTab1.setUI(new VerticalLabelUI(false)); // true/false to make it upwards/downwards
//		tabbedPane.setTabComponentAt(0, labTab1); // For component1
		
		for(int i=0;i<allTabs.length;i++){
			TabGrouping tabContents = allTabs[i];

			JPanel pnlTab = new JPanel();
			tabbedPane.addTab(tabContents.title, null, pnlTab, null);
			pnlTab.setLayout(new GridLayout(0, 2, 0, 0));
			
			for(int x=1;x<=2;x++){
				JPanel chipSettings = new JPanel();
				chipSettings.setLayout(new GridLayout(0, 1, 0, 0));
				chipSettings.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				chipSettings.setBorder(BorderFactory.createTitledBorder("Chip " + x));
				
				HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = mSelectedShimmer.getMapOfExGSettingsChip1();
				if(x==2){
					mMapOfExGSettingsToUse = mSelectedShimmer.getMapOfExGSettingsChip2();
				}
				
				
				for(PanelGrouping bytePanelCat:tabContents.panelGroupings){
					
					JPanel bytePanelParent = new JPanel();
					bytePanelParent.setBorder(BorderFactory.createTitledBorder(bytePanelCat.title));
					
					for(SettingGrouping bytePanelChild:bytePanelCat.settingGroupings){
						JPanel bytePanel = new JPanel();
						bytePanel.setLayout(new GridLayout(0, 1, 0, 0));
						bytePanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
//						bytePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
						bytePanel.setBorder(BorderFactory.createTitledBorder(bytePanelChild.title));

						for(String key:bytePanelChild.settingGroupings){
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
						                updateConfigFromGuiComponent(comboBox);
						                if(mCallback!=null){
						                	mCallback.configPanelChange();
						                }
						            }
						        });
						        
//						        cmBx.setEnabled(true);
					    		componentMap.put(cmBx.getName(),cmBx);

						        panel.add(lbl, labelAlignment);
						        panel.add(cmBx);
						        
						        if(!isSupported(x, key)){
						        	cmBx.setEnabled(false);
						        }

						        
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
						                if(mCallback!=null){
						                	mCallback.configPanelChange();
						                }
				    	            }
				    	        });
		    	    	    	
						        cBx.setEnabled(true);
					    		componentMap.put(cBx.getName(),cBx);

						        bytePanel.add(cBx);
						        
						        if(!isSupported(x, key)){
									cBx.setEnabled(false);
						        }

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
	
	private boolean isSupported(int x, String key){
		for(UnsupportedOption unsupportedOption:unsupportedOptions){
			if(unsupportedOption.chipIndex==(x==1? CHIP_INDEX.CHIP1:CHIP_INDEX.CHIP2)){
				if(unsupportedOption.exgSetting.equals(key)){
					return false;
				}
			}
		}
		return true;
	}
	
	
	public void updateFromShimmer(ShimmerObject selectedShimmer) {
		this.mSelectedShimmer = selectedShimmer;
		for(Component component:componentMap.values()){
			updateGuiComponentFromConfig(component);
//			System.out.println(component.getName());
		}
	}

	
	private void updateGuiComponentFromConfig(Component myComponent) {
		Object returnedValue = null;
		String name = myComponent.getName();
		int chipNumber = Integer.parseInt(name.substring(0, 1)); 
		String componentName = name.substring(1); 
		
		HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = mSelectedShimmer.getMapOfExGSettingsChip1();
		if(chipNumber==2){
			mMapOfExGSettingsToUse = mSelectedShimmer.getMapOfExGSettingsChip2();
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
		HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = mSelectedShimmer.getMapOfExGSettingsChip1();
		if(chipNumber==2){
			chipIndex = CHIP_INDEX.CHIP2;
			mMapOfExGSettingsToUse = mSelectedShimmer.getMapOfExGSettingsChip2();
		}

		if(myComponent instanceof JCheckBox) {
			JCheckBox cBx = (JCheckBox) myComponent;
			boolean state = cBx.isSelected();
			
			mSelectedShimmer.setExgPropertyValue(chipIndex,componentName,state);
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
			
			mSelectedShimmer.setExgPropertyValue(chipIndex,componentName,configValue);
		}

	}	
	
	public class SettingGrouping{
		String title = "";
		String[] settingGroupings;
		
		public SettingGrouping(String title, String[] settingGroupings){
			this.title = title;
			this.settingGroupings = settingGroupings;
		}
	}

	public class PanelGrouping{
		String title = "";
		SettingGrouping[] settingGroupings;
		
		public PanelGrouping(String title, SettingGrouping[] settingGroupings){
			this.title = title;
			this.settingGroupings = settingGroupings;
		}
	}

	public class TabGrouping{
		String title = "";
		PanelGrouping[] panelGroupings;
		
		public TabGrouping(String title, PanelGrouping[] panelGroupings){
			this.title = title;
			this.panelGroupings = panelGroupings;
		}
	}


	/**
	 * @param callback
	 */
	public void registerCallback(ExGConfigChangeCallback callback){
		this.mCallback = callback;
	}
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
        		ShimmerObject selectedShimmer = new ShimmerAbstract();
            	ShimmerVerObject hwfw = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,11,0);
        		((ShimmerAbstract) selectedShimmer).setShimmerVersionInfo(hwfw);
        		((ShimmerAbstract) selectedShimmer).setExpansionBoardId(HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
        		
//        		((ShimmerAbstract) selectedShimmer).setDefaultRespirationConfiguration();
        		((ShimmerAbstract) selectedShimmer).setDefaultECGConfiguration();
//        		((ShimmerAbstract) selectedShimmer).setEXGTestSignal();
//        		((ShimmerAbstract) selectedShimmer).setEXGCustom();

            	PanelExGConfigOptions ex = new PanelExGConfigOptions(selectedShimmer);
            	
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
