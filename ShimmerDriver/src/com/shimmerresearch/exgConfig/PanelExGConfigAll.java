package com.shimmerresearch.exgConfig;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.driver.ShimmerVerObject;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;


public class PanelExGConfigAll extends JPanel {

	public static CallbackCallerExGConfig vcCallBack = new CallbackCallerExGConfig();

	static PanelExGConfigBytes pnlExGBytes;
	static PanelExGConfigOptions pnlConfigOptions;
	static PanelExGConfigGeneral pnlConfigGeneral;
	static ShimmerObject mSelectedShimmer;
	JFrame mMainFrame;
	
	public PanelExGConfigAll(JFrame mainFrame, ShimmerObject selectedShimmer) {
		super();
		mMainFrame = mainFrame;
		mSelectedShimmer = selectedShimmer;
		
		setLayout(new BorderLayout(0, 0));

		pnlConfigGeneral = new PanelExGConfigGeneral(mMainFrame, selectedShimmer);
		pnlConfigGeneral.registerCallback(vcCallBack);
		add(pnlConfigGeneral, BorderLayout.NORTH);
		
		pnlExGBytes = new PanelExGConfigBytes(selectedShimmer);
		pnlExGBytes.registerCallback(vcCallBack);
		add(pnlExGBytes, BorderLayout.SOUTH);

		pnlConfigOptions = new PanelExGConfigOptions(selectedShimmer);
		pnlConfigOptions.registerCallback(vcCallBack);
		add(pnlConfigOptions, BorderLayout.CENTER);
	}
	
	public void updateFromShimmer(ShimmerObject selectedShimmer){
		this.mSelectedShimmer = selectedShimmer;
		
		pnlExGBytes.updateFromShimmer(mSelectedShimmer);
		pnlConfigOptions.updateFromShimmer(mSelectedShimmer);
		pnlConfigGeneral.updateShimmerConfigPanel(mSelectedShimmer);
	}
	
	private static class CallbackCallerExGConfig implements ExGConfigChangeCallback{ 
		@Override
		public void bytesPanelChange() {
			pnlConfigOptions.updateFromShimmer(mSelectedShimmer);
			pnlConfigGeneral.updateShimmerConfigPanel(mSelectedShimmer);
		}

		@Override
		public void configPanelChange() {
			pnlExGBytes.updateFromShimmer(mSelectedShimmer);
			pnlConfigGeneral.updateShimmerConfigPanel(mSelectedShimmer);
		}

		@Override
		public void generalConfigPanelChange() {
			pnlConfigOptions.updateFromShimmer(mSelectedShimmer);
			pnlExGBytes.updateFromShimmer(mSelectedShimmer);
		} 
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
                
        		PanelExGConfigAll ex = new PanelExGConfigAll(f, selectedShimmer);

                
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
