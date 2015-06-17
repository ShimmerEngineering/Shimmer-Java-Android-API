package com.shimmerresearch.exgConfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.SensorConfigOptionDetails;
import com.shimmerresearch.driver.SensorDetails;
import com.shimmerresearch.driver.ShimmerVerObject;
import com.shimmerresearch.driver.SensorConfigOptionDetails.GUI_COMPONENT_TYPE;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Util;

public class PanelExGConfigGeneral extends JPanel {
	
	HashMap<String, SensorConfigOptionDetails> configOptionsMap;
	private JPanel pnlSensorsInternalExpansion, pnlSensorsOtherEast;
    private JPanel pnlConfigOptionsAdvancedExG;
	
	HashMap<JPanel,String[]> configOptionsPanelMap = new HashMap<JPanel,String[]>();
	HashMap<JPanel,Integer[]> sensorPanelMap = new HashMap<JPanel,Integer[]>();
	private HashMap<String,Component> componentMap = new HashMap<String,Component>();


	private Font labelFont = new Font("Calibri", Font.PLAIN, 13);
	
	private Util util = new Util("PanelExGConfigGeneral", true);

	private Integer[] s3SensorKeysInternalExpansion = {
			Configuration.Shimmer3.SensorMapKey.EMG, 
			Configuration.Shimmer3.SensorMapKey.ECG, 
			Configuration.Shimmer3.SensorMapKey.EXG_TEST, 
			Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION, 
			Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM};
	private Integer[] s3SensorKeysOtherEast = {
			Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
			Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
			Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
			Configuration.Shimmer3.SensorMapKey.EXG2_24BIT}; 

	private String[] s3ConfigOptionKeysAdvancedExG = {
			Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
			Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,

			Configuration.Shimmer3.GuiLabelConfig.EXG_RATE,
			Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
			Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
			Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
			Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
			Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
			Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE};
	
	private static ShimmerObject mSelectedShimmer;
	private ExGConfigChangeCallback mCallback;
	private JFrame mMainFrame;
	
	public PanelExGConfigGeneral(JFrame mainFrame, ShimmerObject selectedShimmer){
		this.mMainFrame = mainFrame;
		this.mSelectedShimmer = selectedShimmer;
    	configOptionsMap = mSelectedShimmer.getConfigOptionsMap();
		
		this.setLayout(new GridLayout(1,2,0,0));
		
		pnlSensorsInternalExpansion = new JPanel();
		pnlSensorsInternalExpansion.setName("panelSensorsExpansion");
		pnlSensorsInternalExpansion.setLayout(new GridLayout(0, 1, 0, 0));

		pnlSensorsOtherEast = new JPanel();
		pnlSensorsOtherEast.setName("pnlSensorsOther");
		pnlSensorsOtherEast.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlSensorsOtherEast.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel pnlChannels = new JPanel();
		pnlChannels.setLayout(new GridLayout(1,2,0,0));
		pnlChannels.add(pnlSensorsInternalExpansion);
		pnlChannels.add(pnlSensorsOtherEast);
		this.add(pnlChannels);
		
		pnlConfigOptionsAdvancedExG = new JPanel();
		pnlConfigOptionsAdvancedExG.setName("pnlConfigOptionsAdvancedExG");
		pnlConfigOptionsAdvancedExG.setLayout(new GridLayout(0, 1, 0, 0));
		this.add(pnlConfigOptionsAdvancedExG);
		
		updateConfigOptions();
	}

	
	
	private void updateConfigOptions(){

		sensorPanelMap.clear();
		configOptionsPanelMap.clear();
		
		pnlSensorsInternalExpansion.removeAll();
		pnlSensorsOtherEast.removeAll();
		
		pnlConfigOptionsAdvancedExG.removeAll();

		
		TreeMap<Integer, SensorDetails> sensorMap = mSelectedShimmer.getSensorMap();
		
		sensorPanelMap.put(pnlSensorsInternalExpansion, s3SensorKeysInternalExpansion);
		sensorPanelMap.put(pnlSensorsOtherEast, s3SensorKeysOtherEast);
		
		//Auto-generate Shimmer sensor checkboxes
	    for(JPanel sensorPnl:sensorPanelMap.keySet()) {
		    // for each item in each panel auto-generated list of sensor options
		    for(Integer sensorKey:sensorPanelMap.get(sensorPnl)) { // add components
		
		    	if(sensorMap.get(sensorKey) != null) {
		    		String label = sensorMap.get(sensorKey).mLabel;
			        JCheckBox cbx = new JCheckBox(label);
			        cbx.setFont(labelFont);
			        cbx.setToolTipText(label);
			        cbx.setBackground(null);
			        cbx.setFocusable(false);
			        cbx.addActionListener(new ActionListener() {
			        	public void actionPerformed(ActionEvent arg0) {
			                JCheckBox checkBox = (JCheckBox)arg0.getSource();
			                String checkBoxName = checkBox.getName();
			                Boolean state = checkBox.isSelected();
			                
			                //TODO searching through map to find corresponding entry for the checkbox name - is there a better method? Change key to sensor label (String vs Integer)?
			            	TreeMap<Integer, SensorDetails> sensorMap = mSelectedShimmer.getSensorMap();
			        		
			        		for (Integer key:sensorMap.keySet()) {
			        			if(sensorMap.get(key).mLabel.equals(checkBoxName)) {
			        				List<Integer> listOfConflictingSensors = mSelectedShimmer.sensorMapConflictCheck(key);
			        				if (listOfConflictingSensors == null) {
				        				mSelectedShimmer.setSensorEnabledState(key, state);
			        				}
			        				else {
			        					Object[] options = {"Save","Cancel"};
			        					String message = "The following sensor conflicts were detected:\n";
			        					for(Integer i:listOfConflictingSensors) {
			        						message += "   - " + sensorMap.get(i).mLabel + "\n";
			        					}

			        	                int n = JOptionPane.showOptionDialog(mMainFrame, 
										        	                		message, 
										        	                		"Sensor Conflict", 
										        	                		JOptionPane.YES_NO_CANCEL_OPTION, 
										        	                		JOptionPane.QUESTION_MESSAGE, 
										        	                		null, 
										        	                		options, 
										        	                		options[0]);
			        	                if(n==0) {
					        				mSelectedShimmer.setSensorEnabledState(key, state);
			        	                }
			        				}
			        				break;
			        			}
			        		}
//			        		mSelectedShimmer.setShimmerSensorsMap(sensorMap);
//			        		if (mSelectedShimmer instanceof SlotDetails){
//			        			((SlotDetails)mSelectedShimmer).refreshShimmerInfoMemBytes();
//			        		} else {
			        			//seems like this is needed to get the enabled sensors to generate new values
			        			mSelectedShimmer.refreshShimmerInfoMemBytes();
//			        		}
			        		updateShimmerConfigPanel(mSelectedShimmer);
			        		mCallback.generalConfigPanelChange();
			            }
			        });
			        cbx.setName(sensorMap.get(sensorKey).mLabel);
		    		cbx.setSelected(sensorMap.get(sensorKey).mIsEnabled);
		    		componentMap.put(label,cbx);
		    		
		    		//check compatibility
		    		if(mSelectedShimmer.checkIfVersionCompatible(sensorMap.get(sensorKey).mListOfCompatibleVersionInfo)) {
		    			cbx.setEnabled(true);
		    		}
		    		else {
		    			cbx.setEnabled(false);
		    		}

		    		sensorPnl.add(cbx);
		    	}
		    }	    	
	    }
	    
	    String[] availableRates = new String[256];
	    for(int i=0;i<availableRates.length;i++){
	    	availableRates[i] = Double.toString(mSelectedShimmer.getSamplingRate() / (i+1));
	    }
	    
		configOptionsPanelMap.put(pnlConfigOptionsAdvancedExG, s3ConfigOptionKeysAdvancedExG);
		
		//Auto-generate all config options
	    for(JPanel pnl:configOptionsPanelMap.keySet()) {
	    	
		    for (int i = 0; i < configOptionsPanelMap.get(pnl).length; i++) { // add sensor ranges
		    	
		    	String stringKey = configOptionsPanelMap.get(pnl)[i];
		    	
		    	if(configOptionsMap.get(stringKey) != null) {
		    		
		    		//check compatibility
		    		boolean enabledState = true;
		    		if(mSelectedShimmer.checkIfVersionCompatible(configOptionsMap.get(stringKey).mCompatibleVersionInfo)) {
		    			enabledState = true;
		    		}
		    		else {
		    			enabledState = false;
		    		}
		    	
		    		if(configOptionsMap.get(stringKey).mGuiComponentType == GUI_COMPONENT_TYPE.COMBOBOX) {
					    JPanel panel = new JPanel(new BorderLayout());
					    panel.setBackground(null);
					    //panel.setName(stringKey + "_panel");
					    
				        JLabel lbl = new JLabel(stringKey);
				        lbl.setFont(labelFont);
				        lbl.setBackground(null);
				        //lbl.setName(stringKey + "_label");
				        String labelAlignment = "West";

				        if((stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE))
				        		||(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE))
				        		||(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE))){
				        	mSelectedShimmer.checkConfigOptionValues(stringKey);
				        }
				        
				        String[] tempguiValues = configOptionsMap.get(stringKey).getGuiValues();
						JComboBox<String> cmBx = new JComboBox(tempguiValues);
				        
				        cmBx.setName(stringKey);
				        cmBx.setFont(labelFont);
				        cmBx.setBackground(null);
				        cmBx.setFocusable(false);
				        cmBx.addActionListener(new ActionListener() {
				            @Override
				            public void actionPerformed(ActionEvent e) {
				                JComboBox comboBox = (JComboBox)e.getSource();
			
				                updateConfigFromGuiComponent(comboBox);
			
//				                if (mSelectedShimmer instanceof SlotDetails){
//				                	((SlotDetails)mSelectedShimmer).refreshShimmerInfoMemBytes();
//				                	updateShimmerInfoMemBytesPanel();
//				                }
								updateShimmerConfigPanel(mSelectedShimmer);
				        		mCallback.generalConfigPanelChange();

				            }
				        });
				        
				        cmBx.setEnabled(enabledState);
			    		componentMap.put(stringKey,cmBx);

				        panel.add(lbl, labelAlignment);
				        panel.add(cmBx);
				        pnl.add(panel);
		    		}
		    		else if(configOptionsMap.get(stringKey).mGuiComponentType == GUI_COMPONENT_TYPE.CHECKBOX) {
		    	        JCheckBox cBx = new JCheckBox(stringKey);
		    	        cBx.setFont(labelFont);
		    	        cBx.setBackground(null);
		    	        cBx.setFocusable(false);
		    	        cBx.setName(stringKey);
		    	        cBx.setToolTipText(stringKey);
		    	        cBx.addActionListener(new ActionListener() {
		    	        	public void actionPerformed(ActionEvent arg0) {
		    	                JCheckBox checkBox = (JCheckBox)arg0.getSource();
		    	                updateConfigFromGuiComponent(checkBox);
		    	                
//		    	                if (mSelectedShimmer instanceof SlotDetails){
//				                	((SlotDetails)mSelectedShimmer).refreshShimmerInfoMemBytes();
//				                	updateShimmerInfoMemBytesPanel();
//				                }
		    	        		updateShimmerConfigPanel(mSelectedShimmer);
				        		mCallback.generalConfigPanelChange();
		    	            }
		    	        });
		                updateGuiComponentFromConfig(cBx);
		                
//		                // Special case for single touch start, supported in the driver but not in the GUI
//    	    	    	switch(stringKey){
//    					case(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START):
//    						enabledState = false;
//    						break;
//    					default:
//	    					break;
//    	    	    	}
    	    	    	
				        cBx.setEnabled(enabledState);
			    		componentMap.put(stringKey,cBx);

		                pnl.add(cBx);
		    		}
		    	}

		    	else {
		    		util.consolePrintLn("Sensor Options Map missing key: " + stringKey);
		    	}
		    }
	    }
	}
	

	public void updateShimmerConfigPanel(ShimmerObject selectedShimmer){
		this.mSelectedShimmer = selectedShimmer;

	    updateConfigOptions();

	    //Update all auto-generated config options
	    // for each panel in the auto-generated list of sensor options
	    for(JPanel pnl:configOptionsPanelMap.keySet()) {
		    // for each item in each panel auto-generated list of sensor options
		    for(String label:configOptionsPanelMap.get(pnl)) {
		    	if((componentMap != null)&&(configOptionsMap.get(label) != null)) {
		    		Component foundComponent = (Component) componentMap.get(label);
		    		
		    		if(foundComponent != null) {
		    			updateGuiComponentFromConfig(foundComponent);
		    			
			    		//check compatibility
			    		if(mSelectedShimmer.checkIfVersionCompatible(configOptionsMap.get(label).mCompatibleVersionInfo)) {
			    			foundComponent.setEnabled(true);
			    		}
			    		else {
			    			foundComponent.setEnabled(false);
			    		}
		    		}
		    		else {
			    		util.consolePrintLn("Component:" + label + " NOT FOUND");
		    		}
		    	}
		    }
	    }
	    
	    //Update Shimmer Sensor
    	TreeMap<Integer, SensorDetails> sensorMap = mSelectedShimmer.getSensorMap();
	    // for each panel in the auto-generated list of sensor options
	    for(JPanel pnl:sensorPanelMap.keySet()) {
		    // for each item in each panel auto-generated list of sensor options
		    for(Integer sensorKey:sensorPanelMap.get(pnl)) {
		    	if(sensorMap.get(sensorKey) != null) {
		    		Component foundComponent = (Component) componentMap.get(sensorMap.get(sensorKey).mLabel);
		    		if(foundComponent != null) {
		    			if(foundComponent instanceof JCheckBox) {
			    			((JCheckBox)foundComponent).setSelected(sensorMap.get(sensorKey).mIsEnabled);
		    			}
		    			
		    			if(sensorKey.equals(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL)) {
		    				int i = 0;		    				
		    			}
		    			
			    		//check compatibility
			    		if(mSelectedShimmer.checkIfVersionCompatible(sensorMap.get(sensorKey).mListOfCompatibleVersionInfo)) {
			    			foundComponent.setEnabled(true);
			    		}
			    		else {
			    			foundComponent.setEnabled(false);
			    		}
		    		}
		    	}
		    }	    	
	    }
	}
	
	
	private void updateGuiComponentFromConfig(Component myComponent) {
		String componentName = null;
		Object returnedValue = null;
		
		if(myComponent instanceof JCheckBox) {
			JCheckBox cBx = (JCheckBox) myComponent;
			componentName = cBx.getName();
			
			 returnedValue = mSelectedShimmer.slotDetailsGetMethods(componentName);
			
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
			componentName = cmBx.getName();
			
			 returnedValue = mSelectedShimmer.slotDetailsGetMethods(componentName);

			if(returnedValue != null) {
				int configValue = (int)returnedValue; 
				
				
		    	switch(componentName){
	    		case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE):
	    		case(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE):
	    			mSelectedShimmer.checkConfigOptionValues(componentName);
			        String[] guiValues = configOptionsMap.get(componentName).getGuiValues();
	    		    DefaultComboBoxModel<String> cmbxModel = new DefaultComboBoxModel<String>(guiValues);
	    		    cmBx.setModel(cmbxModel);
	    			break;
		        default:
		        	break;
		    	}
	    	
		    	if(configValue >= 0) {
		    		
					int itemIndex = Arrays.asList(configOptionsMap.get(componentName).getConfigValues()).indexOf(configValue);
					int itemIndexInCmbx = 0;
					if(itemIndex<0) {
						util.consolePrintLn("ERROR");
						itemIndexInCmbx = 0;				
					}
					else {
						String searchString = Arrays.asList(configOptionsMap.get(componentName).getGuiValues()).get(itemIndex);
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
		String componentName = null;
		Object returnedValue = null;

		if(myComponent instanceof JCheckBox) {
			JCheckBox cBx = (JCheckBox) myComponent;
			boolean state = cBx.isSelected();
			componentName = cBx.getName();

//			if (mSelectedShimmer instanceof SlotDetails){             
//				returnedValue = ((SlotDetails)mSelectedShimmer).slotDetailsSetMethods(componentName, state);
//			} 
			if (mSelectedShimmer instanceof ShimmerBluetooth){
				 returnedValue = ((ShimmerBluetooth)mSelectedShimmer).slotDetailsSetMethods(componentName, state);
			}
			else if (mSelectedShimmer instanceof ShimmerAbstract){
				 returnedValue = ((ShimmerAbstract)mSelectedShimmer).slotDetailsSetMethods(componentName, state);
			}

		}
		else if(myComponent instanceof JComboBox) {
			JComboBox cmBx = (JComboBox) myComponent;
			componentName = cmBx.getName();
		
			int itemIndex = Arrays.asList(configOptionsMap.get(componentName).getGuiValues()).indexOf(cmBx.getSelectedItem());
			int configValue = 0;
			if(itemIndex==-1) {
				configValue = configOptionsMap.get(componentName).getConfigValues()[0];				
			}
			else {
				configValue = configOptionsMap.get(componentName).getConfigValues()[itemIndex];				
			}
//			if (mSelectedShimmer instanceof SlotDetails){             
//				returnedValue = ((SlotDetails)mSelectedShimmer).slotDetailsSetMethods(componentName, configValue);
//			}
			if (mSelectedShimmer instanceof ShimmerBluetooth){
				 returnedValue = ((ShimmerBluetooth)mSelectedShimmer).slotDetailsSetMethods(componentName, configValue);
			}
			else if (mSelectedShimmer instanceof ShimmerAbstract){
				 returnedValue = ((ShimmerAbstract)mSelectedShimmer).slotDetailsSetMethods(componentName, configValue);
			}
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
            	
                //ex.setVisible(true);
                JFrame f = new JFrame();
                PanelExGConfigGeneral pnlExGConfigGeneral = new PanelExGConfigGeneral(f, selectedShimmer);
                f.getContentPane().add(pnlExGConfigGeneral);
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
