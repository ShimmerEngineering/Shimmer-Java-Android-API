package com.shimmerresearch.tcpServer;
//TCPServer.java

import java.io.*;
import java.net.*;
import java.util.Collection;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.UtilShimmer;

/**Weibo's TCP server for testing the ESP8266 Wifi module and the Shimmer4
 * 
 * @author Weibo Pan
 *
 */
class TCPServerWP 
{
	
	public static void main(String argv[]) throws Exception
	{
		int port = 9998;
		UtilShimmer utilShimmer = new UtilShimmer("TCPServer", true);
		ServerSocket Server = new ServerSocket (port);

		System.out.println ("TCPServer Waiting for client on port "+port);
		
		while(true) 
		{
			Socket connected = Server.accept();
			System.out.println( " THE CLIENT"+" "+
					connected.getInetAddress() +":"+connected.getPort()+" IS CONNECTED ");
			DataInputStream dIn = new DataInputStream(connected.getInputStream());  
			
			long length_total = 0, time_init, time_last, time_curr;
			time_last = time_init = System.currentTimeMillis();
			
			int length = 0;
			do
			{	     
				length = dIn.available();                    // read length of incoming message
				length_total += (long)length;
				if(length>0) {
					byte[] message = new byte[length];
					dIn.readFully(message, 0, message.length); // read the message
//							ByteArrayInputStream bais = new ByteArrayInputStream(message);
//							ObjectInputStream ois = new ObjectInputStream(bais);
					
//							System.currentTimeMillis()
					
					//utilShimmer.consolePrintLn(UtilShimmer.bytesToHexStringWithSpacesFormatted(message));
					time_curr = System.currentTimeMillis();
					System.out.println( "+"+(time_curr-time_last)+": received "+length+" bytes from"+
							connected.getInetAddress());
					
					time_last = time_curr;
					
//							ObjectCluster rxojc =  (ObjectCluster) ois.readObject();
//							
//							Collection<FormatCluster> accelXFormats = rxojc.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
//							FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); 
//							double x=formatCluster.mData;
//							Collection<FormatCluster> accelYFormats = rxojc.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);
//							formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); 
//							double y=formatCluster.mData;
//							Collection<FormatCluster> accelZFormats = rxojc.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
//							formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); 
//							double z=formatCluster.mData;
//							System.out.print(rxojc.getShimmerName() + "\t"+ Double.toString(x) + "\t"+ Double.toString(y) + "\t"+ Double.toString(z) + "\n");


					System.out.println( "bytes rx  = "+ length_total);
					System.out.println( "time cost = "+ (time_last-time_init));
					if(time_last-time_init>0){
						System.out.println( "baud rate = "+ length_total*8*1000/(time_last-time_init));
					}
				}

			}  while ( connected.isConnected() );
			
			connected.close();
		}
	}
}