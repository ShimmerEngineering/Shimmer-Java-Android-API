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
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * The greeter service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.0-pre1)",
    comments = "Source: src/ShimmerGrpcAndOJC.proto")
public class ShimmerServerGrpc {

  private ShimmerServerGrpc() {}

  public static final String SERVICE_NAME = "shimmerGRPC.ShimmerServer";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SAY_HELLO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SayHello"),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest.getDefaultInstance()),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> METHOD_GET_DATA_STREAM =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetDataStream"),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.getDefaultInstance()),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SEND_DATA_STREAM =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
          generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SendDataStream"),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.File,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SEND_FILE_STREAM =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
          generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SendFileStream"),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.File.getDefaultInstance()),
          io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ShimmerServerStub newStub(io.grpc.Channel channel) {
    return new ShimmerServerStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ShimmerServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ShimmerServerBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static ShimmerServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ShimmerServerFutureStub(channel);
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static abstract class ShimmerServerImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public void sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SAY_HELLO, responseObserver);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_DATA_STREAM, responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_SEND_DATA_STREAM, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.File> sendFileStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_SEND_FILE_STREAM, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_SAY_HELLO,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SAY_HELLO)))
          .addMethod(
            METHOD_GET_DATA_STREAM,
            asyncServerStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>(
                  this, METHODID_GET_DATA_STREAM)))
          .addMethod(
            METHOD_SEND_DATA_STREAM,
            asyncClientStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SEND_DATA_STREAM)))
          .addMethod(
            METHOD_SEND_FILE_STREAM,
            asyncClientStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.File,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SEND_FILE_STREAM)))
          .build();
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerServerStub extends io.grpc.stub.AbstractStub<ShimmerServerStub> {
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

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public void sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SAY_HELLO, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_GET_DATA_STREAM, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_SEND_DATA_STREAM, getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.File> sendFileStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_SEND_FILE_STREAM, getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerServerBlockingStub extends io.grpc.stub.AbstractStub<ShimmerServerBlockingStub> {
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

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.HelloReply sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SAY_HELLO, getCallOptions(), request);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getDataStream(
        com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_GET_DATA_STREAM, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerServerFutureStub extends io.grpc.stub.AbstractStub<ShimmerServerFutureStub> {
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

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> sayHello(
        com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SAY_HELLO, getCallOptions()), request);
    }
  }

  private static final int METHODID_SAY_HELLO = 0;
  private static final int METHODID_GET_DATA_STREAM = 1;
  private static final int METHODID_SEND_DATA_STREAM = 2;
  private static final int METHODID_SEND_FILE_STREAM = 3;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ShimmerServerImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(ShimmerServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
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

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND_DATA_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sendDataStream(
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>) responseObserver);
        case METHODID_SEND_FILE_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sendFileStream(
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_SAY_HELLO,
        METHOD_GET_DATA_STREAM,
        METHOD_SEND_DATA_STREAM,
        METHOD_SEND_FILE_STREAM);
  }

}
