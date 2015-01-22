//TCPClient.java

import java.io.*;
import java.net.*;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Shimmer3Configuration;
import com.shimmerresearch.driver.ShimmerObject;

public class TCPClientTX
{
	public static void main(String argv[]) throws Exception
	{
		Socket clientSocket = new Socket("127.0.0.1", 5000);

		
		DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());

		ObjectCluster ojc = new ObjectCluster();
		ojc.mBluetoothAddress="Shimmer:Research";
		ojc.mMyName = "ShimmerDev1";
		ojc.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",ShimmerObject.NO_UNIT,9.81));
		ojc.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",ShimmerObject.NO_UNIT,0.81));
		ojc.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",ShimmerObject.NO_UNIT,0.1));
		byte[] dataByte = ojc.serialize();
		while (true)
		{
			dOut.writeInt(dataByte.length);
			dOut.write(dataByte);
		}   
	}
}