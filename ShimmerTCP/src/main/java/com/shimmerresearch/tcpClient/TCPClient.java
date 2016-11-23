package com.shimmerresearch.tcpClient;
//TCPClient.java

import java.io.*;
import java.net.*;

class TCPClient {

	public static void main(String argv[]) throws Exception {

		String FromServer = "";
		String ToServer;

		Socket clientSocket = new Socket("127.0.0.1", 5000);

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		while (true) {
			// FromServer = inFromServer.readLine();
			int i = inFromServer.read();
			if (FromServer.equals("q") || FromServer.equals("Q")) {
				clientSocket.close();
				break;
			}

			else

			{
				System.out.println("RECEIVED:" + FromServer);
				System.out.println("SEND(Type Q or q to Quit):");

				ToServer = inFromUser.readLine();

				if (ToServer.equals("Q") || ToServer.equals("q")) {
					outToServer.println(ToServer);
					clientSocket.close();
					break;
				}

				else {
					outToServer.println(ToServer);
				}
			}
		}
	}
}