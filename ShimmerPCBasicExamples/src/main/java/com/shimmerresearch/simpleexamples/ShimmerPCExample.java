package com.shimmerresearch.simpleexamples;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.grpc.GrpcBLERadioByteTools;
import com.shimmerresearch.guiUtilities.configuration.SignalsToPlotDialog;
import com.shimmerresearch.guiUtilities.plot.BasicPlotManagerPC;
import com.shimmerresearch.pcDriver.ShimmerGRPC;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.pcDriver.ShimmerGRPC.SensorDataReceived;
import com.shimmerresearch.sensors.kionix.SensorKionixAccel;

import info.monitorenter.gui.chart.Chart2D;

public class ShimmerPCExample {
	ShimmerPC shimmer;
	final static Chart2D mChart = new Chart2D();
	static BasicPlotManagerPC plotManager = new BasicPlotManagerPC();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ShimmerPCExample example = new ShimmerPCExample();
		example.initialize();
	}
	static int mPort;
	public void initialize() {
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(null);

		JButton btnNewButton = new JButton("Connect");
		
		ShimmerPCExample example = new ShimmerPCExample();
		SensorDataReceived sdr = example.new SensorDataReceived();
		shimmer = new ShimmerPC("E454");
		//shimmer.connect("COM14","");
		sdr.setWaitForData(shimmer);
	
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//shimmer = new ShimmerPC("E454");
				shimmer.connect("COM12","");
				//sdr.setWaitForData(shimmer);
			}
		});
		btnNewButton.setBounds(10, 30, 89, 23);
		frame.getContentPane().add(btnNewButton);

		JButton btnNewButton_1 = new JButton("Disconnect");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					shimmer.disconnect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton_1.setBounds(10, 132, 89, 23);
		frame.getContentPane().add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("start streaming");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					shimmer.startStreaming();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton_2.setBounds(10, 64, 89, 23);
		frame.getContentPane().add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("Stop Streaming");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shimmer.stopStreaming();
			}
		});
		btnNewButton_3.setBounds(10, 98, 89, 23);
		frame.getContentPane().add(btnNewButton_3);
		frame.setSize(1000, 1000);
		mChart.setLocation(12, 200);
		mChart.setSize(587, 246);
		frame.getContentPane().add(mChart);
		plotManager.addChart(mChart);
		
		JButton btnNewButton_4 = new JButton("Plot");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SignalsToPlotDialog signalsToPlotDialog = new SignalsToPlotDialog();
				signalsToPlotDialog.initialize(shimmer, plotManager, mChart);
			}
		});
		btnNewButton_4.setBounds(10, 166, 89, 23);
		frame.getContentPane().add(btnNewButton_4);
		
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public class SensorDataReceived extends BasicProcessWithCallBack{

		@Override
		protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
			// TODO Auto-generated method stub
			System.out.println(shimmerMSG.mIdentifier);
			

			// TODO Auto-generated method stub

			// TODO Auto-generated method stub
			  int ind = shimmerMSG.mIdentifier;

			  Object object = (Object) shimmerMSG.mB;

			if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
				CallbackObject callbackObject = (CallbackObject)object;
				
				if (callbackObject.mState == BT_STATE.CONNECTING) {
				} else if (callbackObject.mState == BT_STATE.CONNECTED) {} else if (callbackObject.mState == BT_STATE.DISCONNECTED
//						|| callbackObject.mState == BT_STATE.NONE
						|| callbackObject.mState == BT_STATE.CONNECTION_LOST){
					
				}
			} else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
				CallbackObject callbackObject = (CallbackObject)object;
				int msg = callbackObject.mIndicator;
				if (msg== ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){}
				if (msg == ShimmerPC.NOTIFICATION_SHIMMER_STOP_STREAMING) {
					
				} else if (msg == ShimmerPC.NOTIFICATION_SHIMMER_START_STREAMING) {
					
				} else {}
			} else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
			ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
			
			try {
				plotManager.filterDataAndPlot(objc);
			} catch (Exception e) {
				e.printStackTrace();
			}} else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {
				
			}
		
		
			
			
		
		}
		
	}
}
