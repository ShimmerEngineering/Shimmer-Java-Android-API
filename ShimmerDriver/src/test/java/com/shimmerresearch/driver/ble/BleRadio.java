package com.shimmerresearch.driver.ble;

import java.lang.Runtime;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;  

public class BleRadio {

		Process p;
		String executablePath;
		BufferedReader reader;
		BufferedWriter writer;
		Boolean flag;
		BleRadio radio1;
		BleRadio radio2;
		String uuid;
		
		public BleRadio(String uuid, String exePath) {
			this.uuid = uuid;
			this.executablePath = exePath;
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
		
		public void InitializeProcess() {
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
								System.out.println(line);
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
		
		
}
