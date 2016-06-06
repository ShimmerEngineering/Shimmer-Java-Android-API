package com.shimmerresearch.grpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Doubles;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.FormatCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2;



public class testing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjectCluster2.Builder ojcb = ObjectCluster2.newBuilder();
		ojcb.setName("JC");
		ojcb.setBluetoothAddress("009A");
		FormatCluster2.Builder fcb = FormatCluster2.newBuilder();
		DataCluster2.Builder dcb = DataCluster2.newBuilder();
		dcb.setUnit("ms2");
		dcb.setData(9.81);
		
		fcb.getMutableFormatMap().put("CAL", dcb.build());
		dcb = DataCluster2.newBuilder();
		dcb.setUnit("no unit");
		dcb.setData(2500);
		fcb.getMutableFormatMap().put("RAW", dcb.build());
		double[] dataarray ={1,2.2,4};
		Iterable<Double> doubleIte = Doubles.asList(dataarray);
		dcb.addAllDataArray(doubleIte);
		fcb.getMutableFormatMap().put("RAW_ARRAY", dcb.build());
		
		FormatCluster2 fc = fcb.build();
		
	
		ojcb.getMutableDataMap().put("Accel X", fc);
		ObjectCluster2 ojc = ojcb.build();
		
		System.out.println(ojc.getName());
		System.out.println(ojc.getDataMap().get("Accel X").getFormatMap().get("CAL").getData());
		System.out.println(ojc.getDataMap().get("Accel X").getFormatMap().get("RAW").getData());
		System.out.println(ojc.getDataMap().get("Accel X").getFormatMap().get("RAW_ARRAY").getDataArrayList());
		
	}

}
