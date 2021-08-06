package com.shimmerresearch.javacsharp.throughput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

public class ThroughputTestUsingProcess {

	static Process p;
	static String executablePath;
	BufferedWriter writer;
	static boolean IsSendDataFromJavaToCSharp = false;

	public ThroughputTestUsingProcess() {

	}

	public int ReadDataUsingProcess() {
		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			int bytePerLine = 20;
			int totalSeconds = 60;
			String line = null;

			while ((line = reader.readLine()) != null) {
				count ++;
				System.out.println(line);
			}
			System.out.println("Total kilobytes received in " + totalSeconds + " seconds = " + count * bytePerLine / 1000);
			System.out.println("Maximum throughput = " + (((count * bytePerLine) / 1000) / totalSeconds) + "kb per second");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	public void WriteDataUsingProcess() {
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		Timer writeDataTimer = new Timer();
		writeDataTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					writer.write("testing", 0, 7);
					writer.flush();
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}, 0, 1000);
	}

	public void InitializeProcess() {
		Runtime runTime = Runtime.getRuntime();
		executablePath = "C:\\Users\\Mas Azalya\\source\\repos\\ShimmerCSharpBLEAPI\\ThroughputTestUsingProcess\\bin\\Debug\\netcoreapp3.1\\ThroughputTestUsingProcess.exe";

		try {
			p = runTime.exec(executablePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		final ThroughputTestUsingProcess test = new ThroughputTestUsingProcess();

		test.InitializeProcess();

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
