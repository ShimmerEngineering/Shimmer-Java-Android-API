package com.shimmerresearch.exgConfig;

import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.SettingType;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import java.awt.GridLayout;
import java.util.HashMap;


public class PanelExGConfig extends JPanel {
	
	private Font labelFont = new Font("Calibri", Font.PLAIN, 13);

	ExGConfigBytesDetails eCBD = new ExGConfigBytesDetails(); 
	
	HashMap<JPanel,String[]> configOptionsPanelMap = new HashMap<JPanel,String[]>();
	
	private String[] channelRegister1Options = {
			ExGConfigBytesDetails.REG0_CONVERSION_MODES,
			ExGConfigBytesDetails.REG0_DATA_RATE,
			};
	private String[] channelRegister2Options = {
			ExGConfigBytesDetails.REG1_LEAD_OFF_COMPARATORS,
			ExGConfigBytesDetails.REG1_REFERENCE_BUFFER,
			ExGConfigBytesDetails.REG1_VOLTAGE_REFERENCE,
			ExGConfigBytesDetails.REG1_OSCILLATOR_CLOCK_CONNECTION,
			ExGConfigBytesDetails.REG1_TEST_SIGNAL_SELECTION,
			ExGConfigBytesDetails.REG1_TEST_SIGNAL_FREQUENCY,
			};
	private String[] leadOffControlRegister = {
			ExGConfigBytesDetails.REG2_COMPARATOR_THRESHOLD,
			ExGConfigBytesDetails.REG2_LEAD_OFF_CURRENT,
			ExGConfigBytesDetails.REG2_LEAD_OFF_FREQUENCY,
			};
	private String[][] globalChannelRegisters = {
			channelRegister1Options,
			channelRegister2Options,
			leadOffControlRegister};
	
	private String[] channel1Settings = {
			ExGConfigBytesDetails.REG3_CHANNEL_1_POWER_DOWN,
			ExGConfigBytesDetails.REG3_CHANNEL_1_PGA_GAIN,
			ExGConfigBytesDetails.REG3_CHANNEL_1_INPUT_SELECTION};
	private String[] channel2Settings = {
			ExGConfigBytesDetails.REG4_CHANNEL_2_POWER_DOWN,
			ExGConfigBytesDetails.REG4_CHANNEL_2_PGA_GAIN,
			ExGConfigBytesDetails.REG4_CHANNEL_2_INPUT_SELECTION};
	private String[][] channelControlRegisters = {
			channel1Settings,
			channel2Settings};
	
	private String[][][] channelRegisters = {
			globalChannelRegisters,
			channelControlRegisters};

	
	
	public PanelExGConfig() {
		super();
		setLayout(new BorderLayout(0, 0));
		
//		configOptionsPanelMap.put(pnlConfigOptionsGeneral, s3ConfigOptionKeysGeneral);
//		configOptionsPanelMap.put(pnlConfigOptionsSync, s3ConfigOptionKeysSync);

		
		ShimmerAbstract shimmerAbstract = new ShimmerAbstract();
		shimmerAbstract.initialise(HW_ID.SHIMMER_3);
		shimmerAbstract.setDefaultRespirationConfiguration();
		
		PanelExGBytes pnlExGBytes = new PanelExGBytes(shimmerAbstract); 
		add(pnlExGBytes, BorderLayout.SOUTH);

		JPanel panelCenter = new JPanel();
		add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new GridLayout(0, 1, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		panelCenter.add(tabbedPane);
		
		JPanel pnlChannelRegisters = new JPanel();
		tabbedPane.addTab("Channel Registers", null, pnlChannelRegisters, null);
		pnlChannelRegisters.setLayout(new GridLayout(0, 2, 0, 0));
		
//		// Create vertical labels to render tab titles
//		JLabel labTab1 = new JLabel("Tab #1");
//		labTab1.setUI(new VerticalLabelUI(false)); // true/false to make it upwards/downwards
//		tabbedPane.setTabComponentAt(0, labTab1); // For component1
		
		for(int x=0;x<=1;x++){
			JPanel chipSettings = new JPanel();
			chipSettings.setLayout(new GridLayout(0, 1, 0, 0));
			chipSettings.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			
			HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToUse = eCBD.mMapOfExGSettingsChip1;
			if(x==1){
				mMapOfExGSettingsToUse = eCBD.mMapOfExGSettingsChip2;
			}
			
			
			for(String[][] bytePanelCat:channelRegisters){
				
				JPanel bytePanelParent = new JPanel();
				
				for(String[] bytePanelChild:bytePanelCat){
					JPanel bytePanel = new JPanel();
					bytePanel.setLayout(new GridLayout(0, 1, 0, 0));
					bytePanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
//					bytePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

					for(String key:bytePanelChild){
						ExGConfigOptionDetails setting = mMapOfExGSettingsToUse.get(key);
						if(setting.settingType == SettingType.combobox){
						    JPanel panel = new JPanel(new BorderLayout());

						    panel.setBackground(null);
						    //panel.setName(stringKey + "_panel");
						    
					        JLabel lbl = new JLabel(key);
					        lbl.setFont(labelFont);
					        lbl.setBackground(null);
					        //lbl.setName(stringKey + "_label");
					        String labelAlignment = "North";

							JComboBox<String> cmBx = new JComboBox(setting.GuiValues);
					        cmBx.setName(key);
					        cmBx.setFont(labelFont);
					        cmBx.setBackground(null);
					        cmBx.setFocusable(false);
					        cmBx.addActionListener(new ActionListener() {
					            @Override
					            public void actionPerformed(ActionEvent e) {
					                JComboBox comboBox = (JComboBox)e.getSource();
				
//					                updateConfigFromGuiComponent(comboBox);
			//	
//					                if (selectedDockedShimmer instanceof SlotDetails){
//					                	((SlotDetails)selectedDockedShimmer).refreshShimmerInfoMemBytes();
//					                	updateShimmerInfoMemBytesPanel();
//					                }
//									updateShimmerConfigPanel();
//					        		updateCalibrationDisplayPanel();
					            }
					        });
					        
					        cmBx.setEnabled(true);
//				    		componentMap.put(stringKey,cmBx);

					        panel.add(lbl, labelAlignment);
					        panel.add(cmBx);
					        bytePanel.add(panel);
						}
						bytePanelParent.add(bytePanel);
					}
					chipSettings.add(bytePanelParent);
				}
				

				pnlChannelRegisters.add(chipSettings);
			}
			
		}


		
		JPanel pnlLoffAndRld = new JPanel();
		tabbedPane.addTab("LOFF and RLD", null, pnlLoffAndRld, null);
		pnlLoffAndRld.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel pnlGpioRegisters = new JPanel();
		tabbedPane.addTab("GPIO Registers", null, pnlGpioRegisters, null);
		pnlGpioRegisters.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel pnlRespirationRegisters = new JPanel();
		tabbedPane.addTab("Respiration Registers", null, pnlRespirationRegisters, null);
		

		
//		eCBD.mMapOfExGSettingsChip1
//		eCBD.mMapOfExGSettingsChip2
		

	}
	
	
	
	public class ShimmerAbstract extends ShimmerObject{

		public void initialise(int hardwareVersion) {
			super.mHardwareVersion = hardwareVersion;
			super.sensorAndConfigMapsCreate();
		}
		
		@Override
		public void setDefaultRespirationConfiguration(){
			super.setDefaultRespirationConfiguration();
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
            	PanelExGConfig ex = new PanelExGConfig();
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
