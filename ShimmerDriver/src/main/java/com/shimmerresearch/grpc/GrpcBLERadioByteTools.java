package com.shimmerresearch.grpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GrpcBLERadioByteTools {

	private Process runningProcess;
	String mExeName = "ShimmerBLEGrpc.exe";
	String mExePath = "C:\\Github\\Shimmer-C-API\\ShimmerAPI\\ShimmerBLEGrpc\\bin\\Debug\\" + mExeName; // Replace with the path to your .exe file
	//String exePath = "C:\\Users\\JC\\Desktop\\testgrpc\\ShimmerBLEGrpc.exe"; // Replace with the path to your .exe file

	public GrpcBLERadioByteTools() {
		
	}
	
	public GrpcBLERadioByteTools(String exeName, String exePath) {
		mExeName = exeName;
		mExePath = exePath;
	}
	

	/**
	 * @return 
	 * @throws Exception 
	 */
	public int getFreePort() throws Exception {
		for(int i=50000;i<60000;i++) {
			try {
				// Try to create a ServerSocket on the specified port.
				// If the port is in use, this will throw an IOException.
				ServerSocket serverSocket = new ServerSocket(i);
				serverSocket.close(); // Close the socket if it was successfully created.
				return i; // Port is available.
			} catch (IOException e) {
				System.out.println("Port in use: " + i);
			}

		}
		throw new Exception("No Port Found");
	}

	public boolean isExeRunning(String exeName) {
		try {
			Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + exeName + "\"");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.toLowerCase().contains(exeName.toLowerCase())) {
					return true;
				}
			}

			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public int startServer() throws Exception {


		int port = getFreePort();

		System.out.println(port + " is free");
		List<String> command = new ArrayList<>();

		// Add the command itself
		command.add(mExePath);
		command.add(Integer.toString(port));
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true); // Redirect standard error to the input stream
		runningProcess = processBuilder.start();
		Thread processThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(runningProcess.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		processThread.start();
		return port;
		// You can continue with other tasks here

	}

	public void stopServer() {
		if (runningProcess != null) {
			runningProcess.destroy();
			System.out.println("Server Stopped");
		} else {
			System.err.println("No external process is currently running.");
		}
	}

	public static void main(String[] args) {

		GrpcBLERadioByteTools grpcTools = new GrpcBLERadioByteTools();
		// TODO Auto-generated method stub
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(null);

		JButton btnNewButton = new JButton("start");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int port = grpcTools.startServer();
					System.out.println("Server Started : " + port);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton.setBounds(10, 44, 224, 23);
		frame.getContentPane().add(btnNewButton);

		JButton btnStop = new JButton("stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				grpcTools.stopServer();
			}
		});
		btnStop.setBounds(10, 91, 224, 23);
		frame.getContentPane().add(btnStop);

		JButton btnCheck = new JButton("checkServerApp");
		btnCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (grpcTools.isExeRunning(grpcTools.mExeName)) {
					System.out.println("EXE RUNNING");	
				} else {
					System.out.println("EXE NOT RUNNING");
				}

			}
		});
		btnCheck.setBounds(10, 139, 224, 23);
		frame.getContentPane().add(btnCheck);

		JButton button = new JButton("find free port");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port;
				try {
					port = grpcTools.getFreePort();

					System.out.println(port + " is free");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		button.setBounds(10, 185, 224, 23);
		frame.getContentPane().add(button);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
