package com.shimmerresearch.grpc;

/*
 * Copyright 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.shimmerresearch.grpc.ShimmerGRPC.HelloReply;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest;
import com.shimmerresearch.grpc.ShimmerServerGrpc.ShimmerServer;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class ShimmerServerTest {
  private static final Logger logger = Logger.getLogger(ShimmerGRPC.class.getName());

  private StreamObserver<ObjectCluster2> mResponseObserver=null;
  
  /* The port on which the server should run */
  private int port = 50051;
  private Server server;

  private void start() throws IOException {
	  server = ServerBuilder.forPort(port)
		      .addService(ShimmerServerGrpc.bindService(new ShimmerServerImpl()))
		      .build()
		      .start();
		  logger.info("Server started, listening on " + port);
		  TestThread tt = new TestThread();
		  tt.start();
		  Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
		      // Use stderr here since the logger may has been reset by its JVM shutdown hook.
		      System.err.println("*** shutting down gRPC server since JVM is shutting down");
		      ShimmerServerTest.this.stop();
		      System.err.println("*** server shut down");
		    }
		  });
		  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
	    JFrame frame = new JFrame();
	    frame.setName("SERVER");
	    frame.setTitle("Server");
	    frame.setSize(500, 500);
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    final ShimmerServerTest server = new ShimmerServerTest();
	    server.start();
	    server.blockUntilShutdown();

  }

  private class ShimmerServerImpl implements ShimmerServer {

    
	@Override
	public void sayHello(
			com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
			StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
			HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
	      responseObserver.onNext(reply);
	      responseObserver.onCompleted();
	    }

	@Override
	public void getDataStream(StreamRequest request,
			StreamObserver<ObjectCluster2> responseObserver) {
		// TODO Auto-generated method stub
		ObjectCluster2.Builder ojcb = ObjectCluster2.newBuilder();
		ojcb.setName("Shimmer 1");
		mResponseObserver = responseObserver;
		mResponseObserver.onNext(ojcb.build());
		
	}
  }
  
  class TestThread extends Thread{
	  
	  @Override
	  public void run(){
		  int count=0;
		  while(true){
			  ObjectCluster2.Builder ojcb = ObjectCluster2.newBuilder();
			  ojcb.setName("Shimmer 1");
			  
			  ojcb.setSystemTime(count);
			  if (mResponseObserver!=null){
			  mResponseObserver.onNext(ojcb.build());
			  }
			  count++;
			  try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
	  
  }
  
}