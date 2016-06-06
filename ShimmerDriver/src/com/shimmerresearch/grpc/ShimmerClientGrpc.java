package com.shimmerresearch.grpc;

import com.shimmerresearch.grpc.ShimmerGRPC.HelloReply;
import com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ShimmerClientGrpc {
	private final ManagedChannel channel;
	private final ShimmerServerGrpc.ShimmerServerBlockingStub blockingStub;
	
	public ShimmerClientGrpc(String host, int port) {
		  channel = ManagedChannelBuilder.forAddress(host, port)
		      .usePlaintext(true)
		      .build();
		  blockingStub = ShimmerServerGrpc.newBlockingStub(channel);
		  HelloRequest req = HelloRequest.newBuilder().setName("JC").build();
		  HelloReply reply = blockingStub.sayHello(req);
		  System.out.println(reply.getMessage());
		  reply = blockingStub.sayHello(req);
		  System.out.println(reply.getMessage());
		  try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  req = HelloRequest.newBuilder().setName("Jong Chern").build();
		  reply = blockingStub.sayHello(req);
		  System.out.println(reply.getMessage());
	}
}
