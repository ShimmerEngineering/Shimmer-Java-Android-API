package com.shimmerresearch.grpc;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;

@javax.annotation.Generated("by gRPC proto compiler")
public class ShimmerServerGrpc {

  private ShimmerServerGrpc() {}

  public static final String SERVICE_NAME = "shimmerGRPC.ShimmerServer";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SAY_HELLO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SayHello"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> METHOD_GET_DATA_STREAM =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetDataStream"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()));

  public static ShimmerServerStub newStub(io.grpc.Channel channel) {
    return new ShimmerServerStub(channel);
  }

  public static ShimmerServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ShimmerServerBlockingStub(channel);
  }

  public static ShimmerServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ShimmerServerFutureStub(channel);
  }

  public static interface ShimmerServer {

    public void sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver);

    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver);
  }

  public static interface ShimmerServerBlockingClient {

    public com.shimmerresearch.grpc.ShimmerGRPC.HelloReply sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request);

    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getDataStream(
        com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request);
  }

  public static interface ShimmerServerFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> sayHello(
        com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request);
  }

  public static class ShimmerServerStub extends io.grpc.stub.AbstractStub<ShimmerServerStub>
      implements ShimmerServer {
    private ShimmerServerStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ShimmerServerStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerServerStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ShimmerServerStub(channel, callOptions);
    }

    @java.lang.Override
    public void sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SAY_HELLO, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_GET_DATA_STREAM, getCallOptions()), request, responseObserver);
    }
  }

  public static class ShimmerServerBlockingStub extends io.grpc.stub.AbstractStub<ShimmerServerBlockingStub>
      implements ShimmerServerBlockingClient {
    private ShimmerServerBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ShimmerServerBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerServerBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ShimmerServerBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public com.shimmerresearch.grpc.ShimmerGRPC.HelloReply sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SAY_HELLO, getCallOptions(), request);
    }

    @java.lang.Override
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getDataStream(
        com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_GET_DATA_STREAM, getCallOptions(), request);
    }
  }

  public static class ShimmerServerFutureStub extends io.grpc.stub.AbstractStub<ShimmerServerFutureStub>
      implements ShimmerServerFutureClient {
    private ShimmerServerFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ShimmerServerFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerServerFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ShimmerServerFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> sayHello(
        com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SAY_HELLO, getCallOptions()), request);
    }
  }

  private static final int METHODID_SAY_HELLO = 0;
  private static final int METHODID_GET_DATA_STREAM = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ShimmerServer serviceImpl;
    private final int methodId;

    public MethodHandlers(ShimmerServer serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SAY_HELLO:
          serviceImpl.sayHello((com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>) responseObserver);
          break;
        case METHODID_GET_DATA_STREAM:
          serviceImpl.getDataStream((com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServerServiceDefinition bindService(
      final ShimmerServer serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_SAY_HELLO,
          asyncUnaryCall(
            new MethodHandlers<
              com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
              com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                serviceImpl, METHODID_SAY_HELLO)))
        .addMethod(
          METHOD_GET_DATA_STREAM,
          asyncServerStreamingCall(
            new MethodHandlers<
              com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
              com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>(
                serviceImpl, METHODID_GET_DATA_STREAM)))
        .build();
  }
}
