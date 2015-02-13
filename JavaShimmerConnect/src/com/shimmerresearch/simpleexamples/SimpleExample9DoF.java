package com.shimmerresearch.simpleexamples;

import java.awt.EventQueue;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.pcdriver.CallbackObject;
import com.shimmerresearch.pcdriver.ShimmerPC;

import javax.swing.JButton;
import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class SimpleExample9DoF extends BasicProcessWithCallBack {

	private JFrame frame;
	ShimmerPC mShimmer = new ShimmerPC("ShimmerDevice", true);
	private JTextField textFieldQW;
	private JTextField textFieldQX;
	private JTextField textFieldQY;
	private JTextField textFieldQZ;
	private boolean mFirstTime = true;
	private JTextField textFieldm20;
	private JTextField textFieldm10;
	private JTextField textFieldm00;
	private JTextField textFieldm01;
	private JTextField textFieldm11;
	private JTextField textFieldm21;
	private JTextField textFieldm02;
	private JTextField textFieldm12;
	private JTextField textFieldm22;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimpleExample9DoF window = new SimpleExample9DoF();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SimpleExample9DoF() {
		initialize();
		frame.getContentPane().setLayout(null);
		setWaitForData(mShimmer);
		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mShimmer.startStreaming();
			}
		});
		btnNewButton.setBounds(28, 11, 89, 23);
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblW = new JLabel("w");
		lblW.setBounds(138, 15, 46, 14);
		frame.getContentPane().add(lblW);
		
		JLabel lblX = new JLabel("x");
		lblX.setBounds(138, 40, 46, 14);
		frame.getContentPane().add(lblX);
		
		JLabel lblY = new JLabel("y");
		lblY.setBounds(138, 65, 46, 14);
		frame.getContentPane().add(lblY);
		
		JLabel lblZ = new JLabel("z");
		lblZ.setBounds(138, 90, 46, 14);
		frame.getContentPane().add(lblZ);
		
		textFieldQW = new JTextField();
		textFieldQW.setBounds(158, 12, 86, 20);
		frame.getContentPane().add(textFieldQW);
		textFieldQW.setColumns(10);
		
		textFieldQX = new JTextField();
		textFieldQX.setColumns(10);
		textFieldQX.setBounds(158, 37, 86, 20);
		frame.getContentPane().add(textFieldQX);
		
		textFieldQY = new JTextField();
		textFieldQY.setColumns(10);
		textFieldQY.setBounds(158, 62, 86, 20);
		frame.getContentPane().add(textFieldQY);
		
		textFieldQZ = new JTextField();
		textFieldQZ.setColumns(10);
		textFieldQZ.setBounds(158, 90, 86, 20);
		frame.getContentPane().add(textFieldQZ);
		
		textFieldm20 = new JTextField();
		textFieldm20.setColumns(10);
		textFieldm20.setBounds(158, 222, 86, 20);
		frame.getContentPane().add(textFieldm20);
		
		textFieldm10 = new JTextField();
		textFieldm10.setColumns(10);
		textFieldm10.setBounds(158, 194, 86, 20);
		frame.getContentPane().add(textFieldm10);
		
		textFieldm00 = new JTextField();
		textFieldm00.setColumns(10);
		textFieldm00.setBounds(158, 169, 86, 20);
		frame.getContentPane().add(textFieldm00);
		
		textFieldm01 = new JTextField();
		textFieldm01.setColumns(10);
		textFieldm01.setBounds(273, 169, 86, 20);
		frame.getContentPane().add(textFieldm01);
		
		textFieldm11 = new JTextField();
		textFieldm11.setColumns(10);
		textFieldm11.setBounds(273, 194, 86, 20);
		frame.getContentPane().add(textFieldm11);
		
		textFieldm21 = new JTextField();
		textFieldm21.setColumns(10);
		textFieldm21.setBounds(273, 222, 86, 20);
		frame.getContentPane().add(textFieldm21);
		
		textFieldm02 = new JTextField();
		textFieldm02.setColumns(10);
		textFieldm02.setBounds(384, 169, 86, 20);
		frame.getContentPane().add(textFieldm02);
		
		textFieldm12 = new JTextField();
		textFieldm12.setColumns(10);
		textFieldm12.setBounds(384, 194, 86, 20);
		frame.getContentPane().add(textFieldm12);
		
		textFieldm22 = new JTextField();
		textFieldm22.setColumns(10);
		textFieldm22.setBounds(384, 222, 86, 20);
		frame.getContentPane().add(textFieldm22);
		mShimmer.connect("COM19", "");
		mShimmer.enable3DOrientation(true);
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 770, 592);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		  int ind = shimmerMSG.mIdentifier;
		  Object object = shimmerMSG.mB;
		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject)object;
			int state = callbackObject.mIndicator;
			if (state == ShimmerBluetooth.STATE_CONNECTING) {
				
			} else if (state == ShimmerBluetooth.STATE_CONNECTED) {
				if(mFirstTime){
					mShimmer.writeSamplingRate(10.1);
					mShimmer.writeEnabledSensors(ShimmerObject.SENSOR_ACCEL|ShimmerObject.SENSOR_GYRO|ShimmerObject.SENSOR_MAG);
					mFirstTime = false;
				}
			} else {
				
			}
		}
		else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			ObjectCluster objectCluster = (ObjectCluster)object;

			if (objectCluster!=null){
				Collection<FormatCluster> accelXFormats = objectCluster.mPropertyCluster.get("Quaternion 0");  // first retrieve all the possible formats for the current sensor device
				float q0 = 0,x = 0,y=0,z=0;
				if (accelXFormats != null){
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					q0 = (float) formatCluster.mData;
					textFieldQW.setText(Double.toString(q0));
				}
				Collection<FormatCluster> accelYFormats = objectCluster.mPropertyCluster.get("Quaternion 1");  // first retrieve all the possible formats for the current sensor device
				if (accelYFormats != null){
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					x=(float) formatCluster.mData;
					textFieldQX.setText(Double.toString(x));
				}
				Collection<FormatCluster> accelZFormats = objectCluster.mPropertyCluster.get("Quaternion 2");  // first retrieve all the possible formats for the current sensor device
				if (accelZFormats != null){
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
					y=(float) formatCluster.mData;
					textFieldQY.setText(Double.toString(y));
				}
				Collection<FormatCluster> aaFormats = objectCluster.mPropertyCluster.get("Quaternion 3");  // first retrieve all the possible formats for the current sensor device
				if (aaFormats != null){
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(aaFormats,"CAL")); // retrieve the calibrated data
					z=(float) formatCluster.mData;
					textFieldQZ.setText(Double.toString(z));
				}
				Quat4d q = new Quat4d(x, y,z,q0);
				Matrix3d m3d = new Matrix3d();
	 	    	m3d.set(q);
	 	    	textFieldm00.setText(Double.toString(m3d.m00));
	 	    	textFieldm01.setText(Double.toString(m3d.m01));
	 	    	textFieldm02.setText(Double.toString(m3d.m02));
	 	    	textFieldm10.setText(Double.toString(m3d.m10));
	 	    	textFieldm11.setText(Double.toString(m3d.m11));
	 	    	textFieldm12.setText(Double.toString(m3d.m12));
	 	    	textFieldm20.setText(Double.toString(m3d.m20));
	 	    	textFieldm21.setText(Double.toString(m3d.m21));
	 	    	textFieldm22.setText(Double.toString(m3d.m22));
			}
			
			}

		}
}
