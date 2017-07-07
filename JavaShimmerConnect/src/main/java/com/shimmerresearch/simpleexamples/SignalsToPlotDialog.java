package com.shimmerresearch.simpleexamples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JDialog;

import com.shimmerresearch.driverUtilities.ChannelDetails;
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

public class SignalsToPlotDialog {

	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize(ShimmerPC shimmerDevice, BasicPlotManagerPC plotManager, Chart2D chart) {
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Select Signals to Plot");
		dialog.setSize(300, 1000);
		
		JButton btnSet = new JButton("Set");
		btnSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				
			}
		});
		dialog.getContentPane().add(btnSet, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		
//		String[] channelSignals = shimmerDevice.getListofEnabledChannelSignals();
//		List<String[]> signalsFormatsList = shimmerDevice.getListofEnabledChannelSignalsandFormats();
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

			final int a = count;
			
			
			checkBoxList[count].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(plotManager.checkIfPropertyExist(mList.get(a))) {
							plotManager.removeSignal(mList.get(a));
						}
						else {
							try {
								plotManager.addSignal(mList.get(a), chart);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
			
			panel.add(checkBoxList[count]);
			
			count++;
		}
		
//		for(int i=0; i<checkBox.length; i++) {
//			
//			checkBox[i] = new JCheckBox(sensorList[i], false);
//			
//			
//		}
		
			
		
		dialog.setVisible(true);
	}
	
	
}
