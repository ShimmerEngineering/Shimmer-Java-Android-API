package com.shimmerresearch.tcpServer;
//TCPServer.java

import java.io.*;
import java.net.*;
import java.util.Collection;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

/**
 * 25 May 2015
 * -Demonstrates how to receive serialized objectclusters
 * -Example is to be used with ShimmerTCPExample of ShimmerPCTCPExample
 * 
 * @author JC
 *
 */
class TCPServer 
{
	public static void main(String argv[]) throws Exception
	{
		ServerSocket Server = new ServerSocket (5000);

		System.out.println ("TCPServer Waiting for client on port 5000");

				while(true) 
				{
					Socket connected = Server.accept();
					System.out.println( " THE CLIENT"+" "+
							connected.getInetAddress() +":"+connected.getPort()+" IS CONNECTED ");
					DataInputStream dIn = new DataInputStream(connected.getInputStream());  


					while ( true )
					{	     
						int length = dIn.readInt();                    // read length of incoming message
						if(length>0) {
							byte[] message = new byte[length];
							dIn.readFully(message, 0, message.length); // read the message
							ByteArrayInputStream bais = new ByteArrayInputStream(message);
							ObjectInputStream ois = new ObjectInputStream(bais);
							ObjectCluster rxojc =  (ObjectCluster) ois.readObject();
							
							Collection<FormatCluster> accelXFormats = rxojc.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
							FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); 
							double x=formatCluster.mData;
							Collection<FormatCluster> accelYFormats = rxojc.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);
							formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); 
							double y=formatCluster.mData;
							Collection<FormatCluster> accelZFormats = rxojc.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
							formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); 
							double z=formatCluster.mData;
							System.out.print(rxojc.getShimmerName() + "\t"+ Double.toString(x) + "\t"+ Double.toString(y) + "\t"+ Double.toString(z) + "\n");
							
						}



					}  

				}
	}
}