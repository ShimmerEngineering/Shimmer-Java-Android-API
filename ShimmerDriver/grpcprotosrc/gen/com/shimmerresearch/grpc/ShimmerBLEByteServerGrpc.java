package com.shimmerresearch.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * The greeter service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.32.1)",
    comments = "Source: src/ShimmerBLEGrpcAndPacketByteArray.proto")
public final class ShimmerBLEByteServerGrpc {

  private ShimmerBLEByteServerGrpc() {}

  public static final String SERVICE_NAME = "shimmerBLEGRPC.ShimmerBLEByteServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getGetDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getGetDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getGetDataStreamMethod;
    if ((getGetDataStreamMethod = ShimmerBLEByteServerGrpc.getGetDataStreamMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getGetDataStreamMethod = ShimmerBLEByteServerGrpc.getGetDataStreamMethod) == null) {
          ShimmerBLEByteServerGrpc.getGetDataStreamMethod = getGetDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("GetDataStream"))
              .build();
        }
      }
    }
    return getGetDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getGetTestDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTestDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getGetTestDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getGetTestDataStreamMethod;
    if ((getGetTestDataStreamMethod = ShimmerBLEByteServerGrpc.getGetTestDataStreamMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getGetTestDataStreamMethod = ShimmerBLEByteServerGrpc.getGetTestDataStreamMethod) == null) {
          ShimmerBLEByteServerGrpc.getGetTestDataStreamMethod = getGetTestDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTestDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("GetTestDataStream"))
              .build();
        }
      }
    }
    return getGetTestDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getSendDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getSendDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getSendDataStreamMethod;
    if ((getSendDataStreamMethod = ShimmerBLEByteServerGrpc.getSendDataStreamMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getSendDataStreamMethod = ShimmerBLEByteServerGrpc.getSendDataStreamMethod) == null) {
          ShimmerBLEByteServerGrpc.getSendDataStreamMethod = getSendDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("SendDataStream"))
              .build();
        }
      }
    }
    return getSendDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus> getConnectShimmerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ConnectShimmer",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Request.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus> getConnectShimmerMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request, com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus> getConnectShimmerMethod;
    if ((getConnectShimmerMethod = ShimmerBLEByteServerGrpc.getConnectShimmerMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getConnectShimmerMethod = ShimmerBLEByteServerGrpc.getConnectShimmerMethod) == null) {
          ShimmerBLEByteServerGrpc.getConnectShimmerMethod = getConnectShimmerMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request, com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ConnectShimmer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("ConnectShimmer"))
              .build();
        }
      }
    }
    return getConnectShimmerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getDisconnectShimmerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DisconnectShimmer",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Request.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getDisconnectShimmerMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getDisconnectShimmerMethod;
    if ((getDisconnectShimmerMethod = ShimmerBLEByteServerGrpc.getDisconnectShimmerMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getDisconnectShimmerMethod = ShimmerBLEByteServerGrpc.getDisconnectShimmerMethod) == null) {
          ShimmerBLEByteServerGrpc.getDisconnectShimmerMethod = getDisconnectShimmerMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DisconnectShimmer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("DisconnectShimmer"))
              .build();
        }
      }
    }
    return getDisconnectShimmerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getWriteBytesShimmerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WriteBytesShimmer",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getWriteBytesShimmerMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getWriteBytesShimmerMethod;
    if ((getWriteBytesShimmerMethod = ShimmerBLEByteServerGrpc.getWriteBytesShimmerMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getWriteBytesShimmerMethod = ShimmerBLEByteServerGrpc.getWriteBytesShimmerMethod) == null) {
          ShimmerBLEByteServerGrpc.getWriteBytesShimmerMethod = getWriteBytesShimmerMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WriteBytesShimmer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("WriteBytesShimmer"))
              .build();
        }
      }
    }
    return getWriteBytesShimmerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getSayHelloMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SayHello",
      requestType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Request.class,
      responseType = com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
      com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getSayHelloMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> getSayHelloMethod;
    if ((getSayHelloMethod = ShimmerBLEByteServerGrpc.getSayHelloMethod) == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        if ((getSayHelloMethod = ShimmerBLEByteServerGrpc.getSayHelloMethod) == null) {
          ShimmerBLEByteServerGrpc.getSayHelloMethod = getSayHelloMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerBLEGRPC.Request, com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SayHello"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerBLEByteServerMethodDescriptorSupplier("SayHello"))
              .build();
        }
      }
    }
    return getSayHelloMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ShimmerBLEByteServerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ShimmerBLEByteServerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ShimmerBLEByteServerStub>() {
        @java.lang.Override
        public ShimmerBLEByteServerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ShimmerBLEByteServerStub(channel, callOptions);
        }
      };
    return ShimmerBLEByteServerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ShimmerBLEByteServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ShimmerBLEByteServerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ShimmerBLEByteServerBlockingStub>() {
        @java.lang.Override
        public ShimmerBLEByteServerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ShimmerBLEByteServerBlockingStub(channel, callOptions);
        }
      };
    return ShimmerBLEByteServerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ShimmerBLEByteServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ShimmerBLEByteServerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ShimmerBLEByteServerFutureStub>() {
        @java.lang.Override
        public ShimmerBLEByteServerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ShimmerBLEByteServerFutureStub(channel, callOptions);
        }
      };
    return ShimmerBLEByteServerFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static abstract class ShimmerBLEByteServerImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDataStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getTestDataStream(com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTestDataStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      return asyncUnimplementedStreamingCall(getSendDataStreamMethod(), responseObserver);
    }

    /**
     */
    public void connectShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getConnectShimmerMethod(), responseObserver);
    }

    /**
     */
    public void disconnectShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      asyncUnimplementedUnaryCall(getDisconnectShimmerMethod(), responseObserver);
    }

    /**
     */
    public void writeBytesShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      asyncUnimplementedUnaryCall(getWriteBytesShimmerMethod(), responseObserver);
    }

    /**
     */
    public void sayHello(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      asyncUnimplementedUnaryCall(getSayHelloMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDataStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray>(
                  this, METHODID_GET_DATA_STREAM)))
          .addMethod(
            getGetTestDataStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray>(
                  this, METHODID_GET_TEST_DATA_STREAM)))
          .addMethod(
            getSendDataStreamMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>(
                  this, METHODID_SEND_DATA_STREAM)))
          .addMethod(
            getConnectShimmerMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus>(
                  this, METHODID_CONNECT_SHIMMER)))
          .addMethod(
            getDisconnectShimmerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>(
                  this, METHODID_DISCONNECT_SHIMMER)))
          .addMethod(
            getWriteBytesShimmerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>(
                  this, METHODID_WRITE_BYTES_SHIMMER)))
          .addMethod(
            getSayHelloMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Request,
                com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>(
                  this, METHODID_SAY_HELLO)))
          .build();
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerBLEByteServerStub extends io.grpc.stub.AbstractAsyncStub<ShimmerBLEByteServerStub> {
    private ShimmerBLEByteServerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerBLEByteServerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ShimmerBLEByteServerStub(channel, callOptions);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetDataStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getTestDataStream(com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetTestDataStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSendDataStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void connectShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getConnectShimmerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void disconnectShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDisconnectShimmerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void writeBytesShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getWriteBytesShimmerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sayHello(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerBLEByteServerBlockingStub extends io.grpc.stub.AbstractBlockingStub<ShimmerBLEByteServerBlockingStub> {
    private ShimmerBLEByteServerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerBLEByteServerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ShimmerBLEByteServerBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getDataStream(
        com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetDataStreamMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray> getTestDataStream(
        com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetTestDataStreamMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus> connectShimmer(
        com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request) {
      return blockingServerStreamingCall(
          getChannel(), getConnectShimmerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply disconnectShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request) {
      return blockingUnaryCall(
          getChannel(), getDisconnectShimmerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply writeBytesShimmer(com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes request) {
      return blockingUnaryCall(
          getChannel(), getWriteBytesShimmerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply sayHello(com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request) {
      return blockingUnaryCall(
          getChannel(), getSayHelloMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerBLEByteServerFutureStub extends io.grpc.stub.AbstractFutureStub<ShimmerBLEByteServerFutureStub> {
    private ShimmerBLEByteServerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerBLEByteServerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ShimmerBLEByteServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> disconnectShimmer(
        com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request) {
      return futureUnaryCall(
          getChannel().newCall(getDisconnectShimmerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> writeBytesShimmer(
        com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes request) {
      return futureUnaryCall(
          getChannel().newCall(getWriteBytesShimmerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply> sayHello(
        com.shimmerresearch.grpc.ShimmerBLEGRPC.Request request) {
      return futureUnaryCall(
          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_DATA_STREAM = 0;
  private static final int METHODID_GET_TEST_DATA_STREAM = 1;
  private static final int METHODID_CONNECT_SHIMMER = 2;
  private static final int METHODID_DISCONNECT_SHIMMER = 3;
  private static final int METHODID_WRITE_BYTES_SHIMMER = 4;
  private static final int METHODID_SAY_HELLO = 5;
  private static final int METHODID_SEND_DATA_STREAM = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ShimmerBLEByteServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ShimmerBLEByteServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DATA_STREAM:
          serviceImpl.getDataStream((com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray>) responseObserver);
          break;
        case METHODID_GET_TEST_DATA_STREAM:
          serviceImpl.getTestDataStream((com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray>) responseObserver);
          break;
        case METHODID_CONNECT_SHIMMER:
          serviceImpl.connectShimmer((com.shimmerresearch.grpc.ShimmerBLEGRPC.Request) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus>) responseObserver);
          break;
        case METHODID_DISCONNECT_SHIMMER:
          serviceImpl.disconnectShimmer((com.shimmerresearch.grpc.ShimmerBLEGRPC.Request) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>) responseObserver);
          break;
        case METHODID_WRITE_BYTES_SHIMMER:
          serviceImpl.writeBytesShimmer((com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>) responseObserver);
          break;
        case METHODID_SAY_HELLO:
          serviceImpl.sayHello((com.shimmerresearch.grpc.ShimmerBLEGRPC.Request) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>) responseObserver);
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
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ShimmerBLEByteServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ShimmerBLEByteServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shimmerresearch.grpc.ShimmerBLEGRPC.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ShimmerBLEByteServer");
    }
  }

  private static final class ShimmerBLEByteServerFileDescriptorSupplier
      extends ShimmerBLEByteServerBaseDescriptorSupplier {
    ShimmerBLEByteServerFileDescriptorSupplier() {}
  }

  private static final class ShimmerBLEByteServerMethodDescriptorSupplier
      extends ShimmerBLEByteServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ShimmerBLEByteServerMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ShimmerBLEByteServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ShimmerBLEByteServerFileDescriptorSupplier())
              .addMethod(getGetDataStreamMethod())
              .addMethod(getGetTestDataStreamMethod())
              .addMethod(getSendDataStreamMethod())
              .addMethod(getConnectShimmerMethod())
              .addMethod(getDisconnectShimmerMethod())
              .addMethod(getWriteBytesShimmerMethod())
              .addMethod(getSayHelloMethod())
              .build();
        }
      }
    }
    return result;
  }
}
