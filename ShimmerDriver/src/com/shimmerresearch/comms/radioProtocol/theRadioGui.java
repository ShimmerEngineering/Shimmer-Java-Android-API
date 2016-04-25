package com.shimmerresearch.comms.radioProtocol;

import javax.swing.JFrame;

import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.driver.DeviceException;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class theRadioGui{

	static JFrame frame;
	static ShimmerRadioProtocol mSRP;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		initialize();

	}

	public static void initialize(){
		frame = new JFrame();
		frame.setVisible(true);
		frame.setSize(500, 500);
		frame.getContentPane().setLayout(null);
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mSRP.disconnect();
				} catch (DeviceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnDisconnect.setBounds(271, 11, 89, 23);
		frame.getContentPane().add(btnDisconnect);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					mSRP.connect();
				} catch (DeviceException de) {
					// TODO Auto-generated catch block
					de.printStackTrace();
				}
				
			}
		});
		
		JButton btnStartStreaming = new JButton("Start Streaming");
		btnStartStreaming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mSRP.startStreaming();
			}
		});
		btnStartStreaming.setBounds(141, 11, 120, 23);
		frame.getContentPane().add(btnStartStreaming);
		
		
		btnConnect.setBounds(42, 11, 89, 23);
		frame.getContentPane().add(btnConnect);
		
		
		
		mSRP = new ShimmerRadioProtocol();
		
		mSRP.initialize("COM30");
	
		mSRP.setRadioListener(new RadioListener(){

			@Override
			public void connected() {
				// TODO Auto-generated method stub
				
				mSRP.mRadioProtocol.initialize();
				
				//Read infomem, and set packetsize
				//mSRP.mRadioProtocol.writeInstruction(new byte[]{(byte) LiteProtocolInstructionSet.Instructions.GET_INFOMEM_COMMAND_VALUE});
				
				mSRP.mRadioProtocol.setPacketSize(41);
			}

			@Override
			public void disconnected() {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventNewPacket() {
				// TODO Auto-generated method stub

			}

			@Override
			public void configurationResponse(byte[] responseBytes) {
				// TODO Auto-generated method stub

			}
		});
		
		
	}
}
