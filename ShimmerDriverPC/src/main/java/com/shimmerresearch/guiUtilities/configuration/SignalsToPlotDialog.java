package com.shimmerresearch.guiUtilities.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JDialog;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.guiUtilities.plot.BasicPlotManagerPC;
import com.shimmerresearch.pcDriver.ShimmerPC;

import info.monitorenter.gui.chart.Chart2D;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;

public class SignalsToPlotDialog {

	boolean mUseGeneratedXAxis = false;
	public SignalsToPlotDialog() {
		
	}
	
	public SignalsToPlotDialog(boolean useGeneratedXAxis) {
		mUseGeneratedXAxis = useGeneratedXAxis;
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize(ShimmerDevice shimmerDevice, BasicPlotManagerPC plotManager, Chart2D chart) {
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Select Signals to Plot");
		dialog.setSize(300, 800);
		
		
		JPanel panel = new JPanel();
		panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		List<String[]> listOfEnabledChannelsAndFormats = new ArrayList<String[]>();
		List<String> sensorList = new ArrayList<String>();
		
		LinkedHashMap<String, ChannelDetails> channelsMap = shimmerDevice.getMapOfEnabledChannelsForStreaming();
		
		Iterator<ChannelDetails> iterator = channelsMap.values().iterator();
		while(iterator.hasNext()) {
			ChannelDetails details = iterator.next();
			listOfEnabledChannelsAndFormats.addAll(details.getListOfChannelSignalsAndFormats());
		}
		
		List<String[]> mList = new ArrayList<String[]>();
		
		String shimmerName = shimmerDevice.getShimmerUserAssignedName();
		
		int p = 0;

		for(ChannelDetails details : channelsMap.values()) {
			
			List<ChannelDetails.CHANNEL_TYPE> listOfChannelTypes = details.mListOfChannelTypes;
			
			for(int i=0; i<listOfChannelTypes.size(); i++) {
				
				String format = listOfChannelTypes.get(i).name();
				if(format.contains("UNCAL")) {
					String[] temp = new String[] {shimmerName, details.getChannelObjectClusterName(), format, details.mDefaultUncalUnit};
					mList.add(p, temp);
					p++;
				}
				else {
					String[] temp = new String[] {shimmerName, details.getChannelObjectClusterName(), format, details.mDefaultCalUnits};
					mList.add(p, temp);
					p++;
				}
			}
		}
		
		for(int i=0; i<mList.size(); i++) {
			String[] array = mList.get(i);
			//Remove the device name and units before adding to sensorList:
			String s = array[1] + " " + array[2];
			sensorList.add(i, s);	
		}
					
		JCheckBox[] checkBoxList = new JCheckBox[sensorList.size()];
		
		int count = 0;	
		for(String item : sensorList) {
			checkBoxList[count] = new JCheckBox(item, false);
			panel.add(checkBoxList[count]);
			count++;
		}
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneLayout.VERTICAL_SCROLLBAR_AS_NEEDED);
		dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
				
		JButton btnSet = new JButton("Set");
		btnSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				for(int i = 0; i<checkBoxList.length; i++) {
					if(checkBoxList[i].isSelected()) {
						if(!plotManager.checkIfPropertyExist(mList.get(i))) {
							try {
								plotManager.addSignal(mList.get(i), chart);
								String[] xAxis = new String[3];
								if (mUseGeneratedXAxis) {
									xAxis[0] = shimmerName+"genX"; //force the plot to generate its own x axis value
								} else {
									xAxis[0] = shimmerName;
								}
								xAxis[1] = Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT;
								xAxis[2] = CHANNEL_TYPE.CAL.toString(); 
								plotManager.addXAxis(xAxis);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
					else {
						if(plotManager.checkIfPropertyExist(mList.get(i))) {
							plotManager.removeSignal(mList.get(i));
						}
					}
				}
				
				dialog.dispose();
				
			}
		});
		dialog.getContentPane().add(btnSet, BorderLayout.SOUTH);
		
		dialog.setVisible(true);
	}
	
	
}
