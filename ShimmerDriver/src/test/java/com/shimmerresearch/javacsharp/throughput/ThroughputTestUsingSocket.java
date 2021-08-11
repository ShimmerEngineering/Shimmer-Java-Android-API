package com.shimmerresearch.javacsharp.throughput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//steps
//1.make sure the period and IsSendDataFromJavaToCSharp are the same in both c# and java application
//2.run the ThroughputTestUsingProcess.java
//3.run the c# application

public class ThroughputTestUsingSocket {
	
	static ServerSocket serversocket;
	static Socket socket;
	static boolean IsSendDataFromJavaToCSharp = true;
	static int period = 5;
	static String unixTimeStampWhenSend;
	static long unixTimeStampWhenReceive;
	int maximumThroughput = 0;
	String writeToCSharpString = null;
	
	public ThroughputTestUsingSocket() {
		
	}
	
	public void ReadDataUsingSocket() {
		int count = 0;
		String line = null;
		int bytePerLine = 20;
		
		try {
	        InputStream input = socket.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	        System.out.println("Using socket");
	        
			while ((line = reader.readLine()) != null) {
            	System.out.println(line);
            	count++;
            	
            	if(line.length() == 1) {
            		writeToCSharpString = line;
            	}
			}
			System.out.println("Total kilobytes received in " + period + " seconds = " + count * bytePerLine / 1000);
			maximumThroughput = (((count * bytePerLine) / 1000) / period);
            System.out.println("Maximum throughput = " + maximumThroughput + "kb per second");
            Long delay = unixTimeStampWhenReceive - Long.parseLong(unixTimeStampWhenSend);
			System.out.println("Latency = " + delay + " milliseconds");
    	}
    	catch (IOException e) 
    	{
    		e.printStackTrace();
    	}
	}
	
	public void WriteDataUsingSocket() {
		try {
	        final byte[] bytes = new byte[1];
	        new Random().nextBytes(bytes);
	        final OutputStream output = socket.getOutputStream();
	        
	        final Timer timer = new Timer();
	        timer.schedule(new TimerTask() {
	          @Override
	          public void run() {
	        	  try {
					output.write(bytes);
	        	  } catch (IOException e) 
	        	  {
					//e.printStackTrace();
					System.out.println("Client Disconnected");
					timer.cancel();
	        	  }
	          }
	        }, 0, 1000);
    	}
    	catch (IOException e) 
    	{
    		e.printStackTrace();
    	}
	}
	
	public void MeasureLatency() {
		//long unixTime = Instant.now().getEpochSecond();
		try {
			InputStream input = socket.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line = null;
			while ((line = reader.readLine()) != null) {
		        unixTimeStampWhenSend = line;
		        unixTimeStampWhenReceive = Calendar.getInstance().getTime().getTime();
		        break;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void InitializeSocket() {
		try {
			serversocket = new ServerSocket(1133, 10);
			socket = serversocket.accept();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		System.out.println("Waiting for C# application to connect.");
		
		final ThroughputTestUsingSocket test = new ThroughputTestUsingSocket();
			
		//initialize socket
  		test.InitializeSocket();
  		
  		test.MeasureLatency();
		
		Thread ReadDataUsingSocket = new Thread(){
		    public void run(){
		    	test.ReadDataUsingSocket();
		    }
		};
		
	  	ReadDataUsingSocket.start();
	  	
	  	if(IsSendDataFromJavaToCSharp) {
	  		
	  		Thread WriteDataUsingSocket = new Thread(){
			    public void run(){
			    	test.WriteDataUsingSocket();
			    }
			};
			
		  	WriteDataUsingSocket.start();
	  	}
	  	
	}
}
