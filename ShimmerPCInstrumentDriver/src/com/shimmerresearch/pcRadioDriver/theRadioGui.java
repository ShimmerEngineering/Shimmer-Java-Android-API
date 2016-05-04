package com.shimmerresearch.pcRadioDriver;

import javax.swing.JFrame;

import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsGet;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.pcserialport.ShimmerSerialPortJssc;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import jssc.SerialPort;

public class theRadioGui{

	static JFrame frame;
	static ShimmerRadioProtocol mSRP;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		initialize();

	}

	public static void initialize(){
		frame = new JFrame();
		
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
		
		JButton btnGetInfoMem = new JButton("Read Infomem");
		btnGetInfoMem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mSRP.mRadioProtocol.writeInstruction(new byte[]{(byte) LiteProtocolInstructionSet.InstructionsGet.GET_INFOMEM_COMMAND_VALUE,(byte)0x80,0,0});
			}
		});
		btnGetInfoMem.setBounds(42, 45, 114, 23);
		frame.getContentPane().add(btnGetInfoMem);
		
		JButton btnReadAccelRange = new JButton("Read Accel Range");
		btnReadAccelRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mSRP.mRadioProtocol.writeInstruction(new byte[]{(byte) LiteProtocolInstructionSet.InstructionsGet.GET_ACCEL_SENSITIVITY_COMMAND_VALUE});
			}
		});
		btnReadAccelRange.setBounds(165, 45, 138, 23);
		frame.getContentPane().add(btnReadAccelRange);
		
		
		
		mSRP = new ShimmerRadioProtocol(new ShimmerSerialPortJssc("COM89", "COM89", SerialPort.BAUDRATE_115200),new LiteProtocol());
		
    	mSRP.initialize();

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
			public void eventResponseReceived(byte[] responseBytes) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventNewPacket(byte[] packetByteArray) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void eventAckReceived(byte[] responseBytes) {
				// TODO Auto-generated method stub
				
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
