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
    value = "by gRPC proto compiler (version 1.6.1)",
    comments = "Source: ShimmerGrpcAndOJC.proto")
public final class ShimmerServerGrpc {

  private ShimmerServerGrpc() {}

  public static final String SERVICE_NAME = "shimmerGRPC.ShimmerServer";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SAY_HELLO =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SayHello"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> METHOD_GET_DATA_STREAM =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetDataStream"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SEND_DATA_STREAM =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SendDataStream"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> METHOD_SEND_FILE_STREAM =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SendFileStream"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> METHOD_CONNECT_SHIMMER =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "ConnectShimmer"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> METHOD_START_STREAMING =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "StartStreaming"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> METHOD_CLOSE_APPLICATION =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "CloseApplication"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> METHOD_GET_DOCKED_SHIMMER_INFO =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetDockedShimmerInfo"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_SET_WORKSPACE_DIRECTORY =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "SetWorkspaceDirectory"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> METHOD_GET_WORKSPACE_DIRECTORY =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetWorkspaceDirectory"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .build();

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
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
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
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer> sendFileStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_SEND_FILE_STREAM, responseObserver);
    }

    /**
     */
    public void connectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CONNECT_SHIMMER, responseObserver);
    }

    /**
     */
    public void startStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_START_STREAMING, responseObserver);
    }

    /**
     */
    public void closeApplication(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CLOSE_APPLICATION, responseObserver);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public void getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_DOCKED_SHIMMER_INFO, responseObserver);
    }

    /**
     */
    public void setWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SET_WORKSPACE_DIRECTORY, responseObserver);
    }

    /**
     */
    public void getWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_WORKSPACE_DIRECTORY, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
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
                com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SEND_FILE_STREAM)))
          .addMethod(
            METHOD_CONNECT_SHIMMER,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_CONNECT_SHIMMER)))
          .addMethod(
            METHOD_START_STREAMING,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_START_STREAMING)))
          .addMethod(
            METHOD_CLOSE_APPLICATION,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_CLOSE_APPLICATION)))
          .addMethod(
            METHOD_GET_DOCKED_SHIMMER_INFO,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>(
                  this, METHODID_GET_DOCKED_SHIMMER_INFO)))
          .addMethod(
            METHOD_SET_WORKSPACE_DIRECTORY,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_SET_WORKSPACE_DIRECTORY)))
          .addMethod(
            METHOD_GET_WORKSPACE_DIRECTORY,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>(
                  this, METHODID_GET_WORKSPACE_DIRECTORY)))
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
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer> sendFileStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_SEND_FILE_STREAM, getCallOptions()), responseObserver);
    }

    /**
     */
    public void connectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CONNECT_SHIMMER, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_STREAMING, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void closeApplication(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CLOSE_APPLICATION, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public void getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_DOCKED_SHIMMER_INFO, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_WORKSPACE_DIRECTORY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_WORKSPACE_DIRECTORY, getCallOptions()), request, responseObserver);
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

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus connectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CONNECT_SHIMMER, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus startStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_START_STREAMING, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus closeApplication(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CLOSE_APPLICATION, getCallOptions(), request);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_DOCKED_SHIMMER_INFO, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest setWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_WORKSPACE_DIRECTORY, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.StringMsg getWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_WORKSPACE_DIRECTORY, getCallOptions(), request);
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

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> connectShimmer(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CONNECT_SHIMMER, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> startStreaming(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_STREAMING, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> closeApplication(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CLOSE_APPLICATION, getCallOptions()), request);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getDockedShimmerInfo(
        com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_DOCKED_SHIMMER_INFO, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> setWorkspaceDirectory(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_WORKSPACE_DIRECTORY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> getWorkspaceDirectory(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_WORKSPACE_DIRECTORY, getCallOptions()), request);
    }
  }

  private static final int METHODID_SAY_HELLO = 0;
  private static final int METHODID_GET_DATA_STREAM = 1;
  private static final int METHODID_CONNECT_SHIMMER = 2;
  private static final int METHODID_START_STREAMING = 3;
  private static final int METHODID_CLOSE_APPLICATION = 4;
  private static final int METHODID_GET_DOCKED_SHIMMER_INFO = 5;
  private static final int METHODID_SET_WORKSPACE_DIRECTORY = 6;
  private static final int METHODID_GET_WORKSPACE_DIRECTORY = 7;
  private static final int METHODID_SEND_DATA_STREAM = 8;
  private static final int METHODID_SEND_FILE_STREAM = 9;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ShimmerServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ShimmerServerImplBase serviceImpl, int methodId) {
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
        case METHODID_CONNECT_SHIMMER:
          serviceImpl.connectShimmer((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_START_STREAMING:
          serviceImpl.startStreaming((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_CLOSE_APPLICATION:
          serviceImpl.closeApplication((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_GET_DOCKED_SHIMMER_INFO:
          serviceImpl.getDockedShimmerInfo((com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>) responseObserver);
          break;
        case METHODID_SET_WORKSPACE_DIRECTORY:
          serviceImpl.setWorkspaceDirectory((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_GET_WORKSPACE_DIRECTORY:
          serviceImpl.getWorkspaceDirectory((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>) responseObserver);
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

  private static final class ShimmerServerDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shimmerresearch.grpc.ShimmerGRPC.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ShimmerServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ShimmerServerDescriptorSupplier())
              .addMethod(METHOD_SAY_HELLO)
              .addMethod(METHOD_GET_DATA_STREAM)
              .addMethod(METHOD_SEND_DATA_STREAM)
              .addMethod(METHOD_SEND_FILE_STREAM)
              .addMethod(METHOD_CONNECT_SHIMMER)
              .addMethod(METHOD_START_STREAMING)
              .addMethod(METHOD_CLOSE_APPLICATION)
              .addMethod(METHOD_GET_DOCKED_SHIMMER_INFO)
              .addMethod(METHOD_SET_WORKSPACE_DIRECTORY)
              .addMethod(METHOD_GET_WORKSPACE_DIRECTORY)
              .build();
        }
      }
    }
    return result;
  }
}
