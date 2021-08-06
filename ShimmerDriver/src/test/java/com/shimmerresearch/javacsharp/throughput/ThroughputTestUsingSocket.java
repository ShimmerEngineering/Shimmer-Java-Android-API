package com.shimmerresearch.javacsharp.throughput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ThroughputTestUsingSocket {
	
	static ServerSocket serversocket;
	static Socket socket;
	public boolean writeResult;
	static boolean IsSendDataFromJavaToCSharp = true;
	public ThroughputTestUsingSocket() {
		writeResult = false;
	}
	
	public int ReadDataUsingSocket() {
		
		String line = null;
		int count = 0;
		int bytePerLine = 20;
	    int totalSeconds = 60;
		
		try {
	        InputStream input = socket.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	        System.out.println("Using socket");
	        
			while ((line = reader.readLine()) != null) {
            	System.out.println(line);
            	count++;
            	if(line.length() == 1) {
            		writeResult = true;
            	}
			}
			System.out.println(writeResult);
			System.out.println("Total kilobytes received in " + totalSeconds + " seconds = " + count * bytePerLine / 1000);
            System.out.println("Maximum throughput = " + (((count * bytePerLine) / 1000) / totalSeconds) + "kb per second");
	    	
    	}
    	catch (IOException e) 
    	{
    		e.printStackTrace();
    	}
		
		return count;
	}
	
	public void WriteDataUsingSocket() {
		try {
	        final byte[] bytes = new byte[1];
	        new Random().nextBytes(bytes);
	        final OutputStream output = socket.getOutputStream();
	        
	        final Timer timer1 = new Timer();
	        timer1.schedule(new TimerTask() {
	          @Override
	          public void run() {
	        	  try {
					output.write(bytes);
	        	  } catch (IOException e) 
	        	  {
					//e.printStackTrace();
					System.out.println("Client Disconnected");
					timer1.cancel();
	        	  }
	          }
	        }, 0, 1000);
    	}
    	catch (IOException e) 
    	{
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

		final ThroughputTestUsingSocket test = new ThroughputTestUsingSocket();
			
		//initialize socket
  		test.InitializeSocket();
		
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
