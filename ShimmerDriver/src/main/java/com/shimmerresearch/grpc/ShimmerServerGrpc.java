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
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> METHOD_GET_DOCKED_SHIMMER_INFO =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetDockedShimmerInfo"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> METHOD_GET_MADGEWICK_BETA_VALUE =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetMadgewickBetaValue"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_PAIR_SHIMMERS =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "PairShimmers"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_GET_OPERATION_PROGRESS =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "GetOperationProgress"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_IMPORT_SD_DATA_FROM_SHIMMERS =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "ImportSdDataFromShimmers"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_PARSE_SD_DATA_FROM_PATH =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "ParseSdDataFromPath"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_SCAN_SD_DATA_AND_COPY =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "ScanSdDataAndCopy"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_CLEAR_SD_CARD_DATA =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "ClearSdCardData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "DockAccessSlotWithSdCard"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> METHOD_DOCK_RESTORE_AUTO_TASKS =
      io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "shimmerGRPC.ShimmerServer", "DockRestoreAutoTasks"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
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

    /**
     */
    public void getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_DOCKED_SHIMMER_INFO, responseObserver);
    }

    /**
     */
    public void getMadgewickBetaValue(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_MADGEWICK_BETA_VALUE, responseObserver);
    }

    /**
     */
    public void pairShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PAIR_SHIMMERS, responseObserver);
    }

    /**
     */
    public void getOperationProgress(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_OPERATION_PROGRESS, responseObserver);
    }

    /**
     */
    public void importSdDataFromShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_IMPORT_SD_DATA_FROM_SHIMMERS, responseObserver);
    }

    /**
     */
    public void parseSdDataFromPath(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PARSE_SD_DATA_FROM_PATH, responseObserver);
    }

    /**
     */
    public void scanSdDataAndCopy(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SCAN_SD_DATA_AND_COPY, responseObserver);
    }

    /**
     */
    public void clearSdCardData(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CLEAR_SD_CARD_DATA, responseObserver);
    }

    /**
     */
    public void dockAccessSlotWithSdCard(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD, responseObserver);
    }

    /**
     */
    public void dockRestoreAutoTasks(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DOCK_RESTORE_AUTO_TASKS, responseObserver);
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
          .addMethod(
            METHOD_GET_DOCKED_SHIMMER_INFO,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>(
                  this, METHODID_GET_DOCKED_SHIMMER_INFO)))
          .addMethod(
            METHOD_GET_MADGEWICK_BETA_VALUE,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg>(
                  this, METHODID_GET_MADGEWICK_BETA_VALUE)))
          .addMethod(
            METHOD_PAIR_SHIMMERS,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_PAIR_SHIMMERS)))
          .addMethod(
            METHOD_GET_OPERATION_PROGRESS,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_GET_OPERATION_PROGRESS)))
          .addMethod(
            METHOD_IMPORT_SD_DATA_FROM_SHIMMERS,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_IMPORT_SD_DATA_FROM_SHIMMERS)))
          .addMethod(
            METHOD_PARSE_SD_DATA_FROM_PATH,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_PARSE_SD_DATA_FROM_PATH)))
          .addMethod(
            METHOD_SCAN_SD_DATA_AND_COPY,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_SCAN_SD_DATA_AND_COPY)))
          .addMethod(
            METHOD_CLEAR_SD_CARD_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_CLEAR_SD_CARD_DATA)))
          .addMethod(
            METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_DOCK_ACCESS_SLOT_WITH_SD_CARD)))
          .addMethod(
            METHOD_DOCK_RESTORE_AUTO_TASKS,
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_DOCK_RESTORE_AUTO_TASKS)))
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

    /**
     */
    public void getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_DOCKED_SHIMMER_INFO, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMadgewickBetaValue(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_MADGEWICK_BETA_VALUE, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void pairShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PAIR_SHIMMERS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getOperationProgress(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_OPERATION_PROGRESS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void importSdDataFromShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_SD_DATA_FROM_SHIMMERS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void parseSdDataFromPath(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PARSE_SD_DATA_FROM_PATH, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void scanSdDataAndCopy(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SCAN_SD_DATA_AND_COPY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void clearSdCardData(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CLEAR_SD_CARD_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dockAccessSlotWithSdCard(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dockRestoreAutoTasks(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DOCK_RESTORE_AUTO_TASKS, getCallOptions()), request, responseObserver);
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

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_DOCKED_SHIMMER_INFO, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg getMadgewickBetaValue(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_MADGEWICK_BETA_VALUE, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest pairShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PAIR_SHIMMERS, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest getOperationProgress(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_OPERATION_PROGRESS, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest importSdDataFromShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_SD_DATA_FROM_SHIMMERS, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest parseSdDataFromPath(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PARSE_SD_DATA_FROM_PATH, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest scanSdDataAndCopy(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SCAN_SD_DATA_AND_COPY, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest clearSdCardData(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CLEAR_SD_CARD_DATA, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest dockAccessSlotWithSdCard(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD, getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest dockRestoreAutoTasks(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DOCK_RESTORE_AUTO_TASKS, getCallOptions(), request);
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

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getDockedShimmerInfo(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_DOCKED_SHIMMER_INFO, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> getMadgewickBetaValue(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_MADGEWICK_BETA_VALUE, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> pairShimmers(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PAIR_SHIMMERS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getOperationProgress(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_OPERATION_PROGRESS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> importSdDataFromShimmers(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_SD_DATA_FROM_SHIMMERS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> parseSdDataFromPath(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PARSE_SD_DATA_FROM_PATH, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> scanSdDataAndCopy(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SCAN_SD_DATA_AND_COPY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> clearSdCardData(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CLEAR_SD_CARD_DATA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> dockAccessSlotWithSdCard(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> dockRestoreAutoTasks(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DOCK_RESTORE_AUTO_TASKS, getCallOptions()), request);
    }
  }

  private static final int METHODID_SAY_HELLO = 0;
  private static final int METHODID_GET_DATA_STREAM = 1;
  private static final int METHODID_CONNECT_SHIMMER = 2;
  private static final int METHODID_START_STREAMING = 3;
  private static final int METHODID_CLOSE_APPLICATION = 4;
  private static final int METHODID_SET_WORKSPACE_DIRECTORY = 5;
  private static final int METHODID_GET_WORKSPACE_DIRECTORY = 6;
  private static final int METHODID_GET_DOCKED_SHIMMER_INFO = 7;
  private static final int METHODID_GET_MADGEWICK_BETA_VALUE = 8;
  private static final int METHODID_PAIR_SHIMMERS = 9;
  private static final int METHODID_GET_OPERATION_PROGRESS = 10;
  private static final int METHODID_IMPORT_SD_DATA_FROM_SHIMMERS = 11;
  private static final int METHODID_PARSE_SD_DATA_FROM_PATH = 12;
  private static final int METHODID_SCAN_SD_DATA_AND_COPY = 13;
  private static final int METHODID_CLEAR_SD_CARD_DATA = 14;
  private static final int METHODID_DOCK_ACCESS_SLOT_WITH_SD_CARD = 15;
  private static final int METHODID_DOCK_RESTORE_AUTO_TASKS = 16;
  private static final int METHODID_SEND_DATA_STREAM = 17;
  private static final int METHODID_SEND_FILE_STREAM = 18;

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
        case METHODID_SET_WORKSPACE_DIRECTORY:
          serviceImpl.setWorkspaceDirectory((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_GET_WORKSPACE_DIRECTORY:
          serviceImpl.getWorkspaceDirectory((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>) responseObserver);
          break;
        case METHODID_GET_DOCKED_SHIMMER_INFO:
          serviceImpl.getDockedShimmerInfo((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>) responseObserver);
          break;
        case METHODID_GET_MADGEWICK_BETA_VALUE:
          serviceImpl.getMadgewickBetaValue((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg>) responseObserver);
          break;
        case METHODID_PAIR_SHIMMERS:
          serviceImpl.pairShimmers((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_GET_OPERATION_PROGRESS:
          serviceImpl.getOperationProgress((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_IMPORT_SD_DATA_FROM_SHIMMERS:
          serviceImpl.importSdDataFromShimmers((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_PARSE_SD_DATA_FROM_PATH:
          serviceImpl.parseSdDataFromPath((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_SCAN_SD_DATA_AND_COPY:
          serviceImpl.scanSdDataAndCopy((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_CLEAR_SD_CARD_DATA:
          serviceImpl.clearSdCardData((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_DOCK_ACCESS_SLOT_WITH_SD_CARD:
          serviceImpl.dockAccessSlotWithSdCard((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_DOCK_RESTORE_AUTO_TASKS:
          serviceImpl.dockRestoreAutoTasks((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
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
              .addMethod(METHOD_SET_WORKSPACE_DIRECTORY)
              .addMethod(METHOD_GET_WORKSPACE_DIRECTORY)
              .addMethod(METHOD_GET_DOCKED_SHIMMER_INFO)
              .addMethod(METHOD_GET_MADGEWICK_BETA_VALUE)
              .addMethod(METHOD_PAIR_SHIMMERS)
              .addMethod(METHOD_GET_OPERATION_PROGRESS)
              .addMethod(METHOD_IMPORT_SD_DATA_FROM_SHIMMERS)
              .addMethod(METHOD_PARSE_SD_DATA_FROM_PATH)
              .addMethod(METHOD_SCAN_SD_DATA_AND_COPY)
              .addMethod(METHOD_CLEAR_SD_CARD_DATA)
              .addMethod(METHOD_DOCK_ACCESS_SLOT_WITH_SD_CARD)
              .addMethod(METHOD_DOCK_RESTORE_AUTO_TASKS)
              .build();
        }
      }
    }
    return result;
  }
}
