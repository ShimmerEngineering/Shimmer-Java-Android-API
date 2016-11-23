package com.shimmerresearch.tcpClient;

//TCPClient.java

import java.io.*;
import java.net.*;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;

/**
 * 25 May 2015
 * - This example is to be used with ShimmerTCPReceiver, it demonstrates how to use the serialize method to transmit objectclusters via tcpip
 * 
 * @author JC
 *
 */
public class TCPClientTX {

	public static void main(String argv[]) throws Exception {
		Socket clientSocket = new Socket("127.0.0.1", 5000);

		DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());

		ObjectCluster ojc = new ObjectCluster();
		ojc.setMacAddress("Shimmer:Research");
		ojc.setShimmerName("ShimmerDev1");
		ojc.addDataToMap(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X, "CAL", CHANNEL_UNITS.NO_UNITS, 9.81);
		ojc.addDataToMap(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y, "CAL", CHANNEL_UNITS.NO_UNITS, 0.81);
		ojc.addDataToMap(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z, "CAL", CHANNEL_UNITS.NO_UNITS, 0.1);
		byte[] dataByte = ojc.serialize();
		while (true) {
			dOut.writeInt(dataByte.length);
			dOut.write(dataByte);
		}
	}
}