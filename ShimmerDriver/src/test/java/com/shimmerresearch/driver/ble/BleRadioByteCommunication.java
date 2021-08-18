package com.shimmerresearch.driver.ble;

import java.lang.Runtime;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.shimmerresearch.comms.radioProtocol.RadioListener;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;  

public class BleRadioByteCommunication extends AbstractByteCommunication {
	
		Process p;
		String executablePath;
		BufferedReader reader;
		BufferedWriter writer;
		Boolean flag;
		BleRadioByteCommunication radio1;
		BleRadioByteCommunication radio2;
		String uuid;
		
		public BleRadioByteCommunication(String uuid, String exePath, ByteCommunicationListener listener) {
			this.uuid = uuid;
			this.executablePath = exePath;
			mByteCommunicationListener = listener;
			InitializeProcess();
		}
		

		public void DestroyProcess() {
			if (p!=null) {
				p.destroyForcibly();
			}
		}
		
		
		
		public void WriteDataToProcess(String s) {
			
			try {
				writer.write(s, 0, s.length());
				writer.newLine();
				writer.flush();
				//writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		public static void WriteDataToProcess(byte[] buf) {
			try {
				p.getOutputStream().write(buf, 0, buf.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
		
		protected void InitializeProcess() {
			Runtime runTime = Runtime.getRuntime();
	        
			try {
				p = runTime.exec(executablePath + " " + uuid);
				writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
				reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Thread Example = new Thread(){
				public void run(){
					try {
					    String line = null;
					    while (true) {
							if ((line = reader.readLine()) != null) {
								System.out.println("BleRadioByteComm: " + line);
								if (line.equals("Connected")) {
									if (mByteCommunicationListener!=null) {
										mByteCommunicationListener.eventConnected();
									}
								} else if (line.equals("Disconnected")) {
									if (mByteCommunicationListener!=null) {
										mByteCommunicationListener.eventDisconnected();
									}
								} else if (line.substring(0, 4).equals(("RXB:"))) {
									String bytes = line.substring(4, line.length());
									try {
										byte[] dataBytes = Hex.decodeHex(bytes);
										if (mByteCommunicationListener!=null) {
											mByteCommunicationListener.eventNewBytesReceived(dataBytes);
										}
									} catch (DecoderException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}	
						}
			    	}
			    	catch (IOException e) {
						e.printStackTrace();
					} 
			    }
		  	};
		  	
		  	Example.start();
		}


		@Override
		public void connect() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void disconnect() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void writeBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			
		}
		
		
}
