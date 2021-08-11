package com.shimmerresearch.javacsharp.throughput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

//steps
//1.change the executablePath to the path to the exe file of the c# application
//2.make sure the period and IsSendDataFromJavaToCSharp are the same in both c# and java application
//3.run the ThroughputTestUsingProcess.java

public class ThroughputTestUsingProcess {

	static Process p;
	static String executablePath = "C:\\Users\\weiwe\\source\\repos\\ShimmerCSharpBLEAPI\\ThroughputTestUsingProcess\\bin\\Debug\\netcoreapp3.1\\ThroughputTestUsingProcess.exe";
	BufferedWriter writer;
	static int bytePerLine = 20;
	static boolean IsSendDataFromJavaToCSharp = true;
	static int period = 5;
	static Timer writeDataTimer;
	static String unixTimeStampWhenSend;
	static long unixTimeStampWhenReceive;
	int maximumThroughput = 0;
	String writeToCSharpString = null;

	public ThroughputTestUsingProcess() {

	}

	public void ReadDataUsingProcess() {
		try {
			int count = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;

			while ((line = reader.readLine()) != null) {
				count ++;
				System.out.println(line);
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
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void MeasureLatency() {
		//long unixTime = Instant.now().getEpochSecond();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
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

	public void WriteDataUsingProcess() {
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		writeDataTimer = new Timer();
		writeDataTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					writer.write("a", 0, 1);
					writer.newLine();
					writer.flush();
				} catch (IOException e) 
				{
					writeDataTimer.cancel();
					writeDataTimer.purge();
					System.out.println("Disconnected");
				}
			}
		}, 0, 1000);
	}

	public void InitializeProcess() {
		Runtime runTime = Runtime.getRuntime();
		
		try {
			p = runTime.exec(executablePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		final ThroughputTestUsingProcess test = new ThroughputTestUsingProcess();

		//run the c sharp exe file
		test.InitializeProcess();

		test.MeasureLatency();
		
		Thread ReadDataUsingProcess = new Thread(){
			public void run(){
				test.ReadDataUsingProcess();
			}
		};

		ReadDataUsingProcess.start();

		if(IsSendDataFromJavaToCSharp) {
			Thread WriteDataUsingProcess = new Thread(){
				public void run(){
					test.WriteDataUsingProcess();
				}
			};

			WriteDataUsingProcess.start();
		}

	}
}
