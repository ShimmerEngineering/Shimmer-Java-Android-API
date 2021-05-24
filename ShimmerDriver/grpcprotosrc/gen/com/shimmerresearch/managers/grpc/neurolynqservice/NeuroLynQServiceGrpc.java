package com.shimmerresearch.managers.grpc.neurolynqservice;

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
    comments = "Source: src/NeuroLynQService_v1.0.0.proto")
public final class NeuroLynQServiceGrpc {

  private NeuroLynQServiceGrpc() {}

  public static final String SERVICE_NAME = "neurolynqservice.NeuroLynQService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getGetDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getGetDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getGetDataStreamMethod;
    if ((getGetDataStreamMethod = NeuroLynQServiceGrpc.getGetDataStreamMethod) == null) {
      synchronized (NeuroLynQServiceGrpc.class) {
        if ((getGetDataStreamMethod = NeuroLynQServiceGrpc.getGetDataStreamMethod) == null) {
          NeuroLynQServiceGrpc.getGetDataStreamMethod = getGetDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()))
              .setSchemaDescriptor(new NeuroLynQServiceMethodDescriptorSupplier("GetDataStream"))
              .build();
        }
      }
    }
    return getGetDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getSendDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getSendDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getSendDataStreamMethod;
    if ((getSendDataStreamMethod = NeuroLynQServiceGrpc.getSendDataStreamMethod) == null) {
      synchronized (NeuroLynQServiceGrpc.class) {
        if ((getSendDataStreamMethod = NeuroLynQServiceGrpc.getSendDataStreamMethod) == null) {
          NeuroLynQServiceGrpc.getSendDataStreamMethod = getSendDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
              .setSchemaDescriptor(new NeuroLynQServiceMethodDescriptorSupplier("SendDataStream"))
              .build();
        }
      }
    }
    return getSendDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getGetClientStatsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetClientStats",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getGetClientStatsMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getGetClientStatsMethod;
    if ((getGetClientStatsMethod = NeuroLynQServiceGrpc.getGetClientStatsMethod) == null) {
      synchronized (NeuroLynQServiceGrpc.class) {
        if ((getGetClientStatsMethod = NeuroLynQServiceGrpc.getGetClientStatsMethod) == null) {
          NeuroLynQServiceGrpc.getGetClientStatsMethod = getGetClientStatsMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetClientStats"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats.getDefaultInstance()))
              .setSchemaDescriptor(new NeuroLynQServiceMethodDescriptorSupplier("GetClientStats"))
              .build();
        }
      }
    }
    return getGetClientStatsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getResetClientStatsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ResetClientStats",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getResetClientStatsMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getResetClientStatsMethod;
    if ((getResetClientStatsMethod = NeuroLynQServiceGrpc.getResetClientStatsMethod) == null) {
      synchronized (NeuroLynQServiceGrpc.class) {
        if ((getResetClientStatsMethod = NeuroLynQServiceGrpc.getResetClientStatsMethod) == null) {
          NeuroLynQServiceGrpc.getResetClientStatsMethod = getResetClientStatsMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResetClientStats"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats.getDefaultInstance()))
              .setSchemaDescriptor(new NeuroLynQServiceMethodDescriptorSupplier("ResetClientStats"))
              .build();
        }
      }
    }
    return getResetClientStatsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NeuroLynQServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NeuroLynQServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NeuroLynQServiceStub>() {
        @java.lang.Override
        public NeuroLynQServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NeuroLynQServiceStub(channel, callOptions);
        }
      };
    return NeuroLynQServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NeuroLynQServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NeuroLynQServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NeuroLynQServiceBlockingStub>() {
        @java.lang.Override
        public NeuroLynQServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NeuroLynQServiceBlockingStub(channel, callOptions);
        }
      };
    return NeuroLynQServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NeuroLynQServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NeuroLynQServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NeuroLynQServiceFutureStub>() {
        @java.lang.Override
        public NeuroLynQServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NeuroLynQServiceFutureStub(channel, callOptions);
        }
      };
    return NeuroLynQServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static abstract class NeuroLynQServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     *Client receiving data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDataStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      return asyncUnimplementedStreamingCall(getSendDataStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *Get Client to Server Packets Stats
     * </pre>
     */
    public void getClientStats(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> responseObserver) {
      asyncUnimplementedUnaryCall(getGetClientStatsMethod(), responseObserver);
    }

    /**
     * <pre>
     *Reset Client to Server Packet Stats
     * </pre>
     */
    public void resetClientStats(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> responseObserver) {
      asyncUnimplementedUnaryCall(getResetClientStatsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDataStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>(
                  this, METHODID_GET_DATA_STREAM)))
          .addMethod(
            getSendDataStreamMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_SEND_DATA_STREAM)))
          .addMethod(
            getGetClientStatsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats>(
                  this, METHODID_GET_CLIENT_STATS)))
          .addMethod(
            getResetClientStatsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats>(
                  this, METHODID_RESET_CLIENT_STATS)))
          .build();
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class NeuroLynQServiceStub extends io.grpc.stub.AbstractAsyncStub<NeuroLynQServiceStub> {
    private NeuroLynQServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NeuroLynQServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NeuroLynQServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     *Client receiving data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetDataStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSendDataStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     *Get Client to Server Packets Stats
     * </pre>
     */
    public void getClientStats(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetClientStatsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Reset Client to Server Packet Stats
     * </pre>
     */
    public void resetClientStats(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getResetClientStatsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class NeuroLynQServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<NeuroLynQServiceBlockingStub> {
    private NeuroLynQServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NeuroLynQServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NeuroLynQServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     *Client receiving data
     * </pre>
     */
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getDataStream(
        com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetDataStreamMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *Get Client to Server Packets Stats
     * </pre>
     */
    public com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats getClientStats(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetClientStatsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *Reset Client to Server Packet Stats
     * </pre>
     */
    public com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats resetClientStats(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getResetClientStatsMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class NeuroLynQServiceFutureStub extends io.grpc.stub.AbstractFutureStub<NeuroLynQServiceFutureStub> {
    private NeuroLynQServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NeuroLynQServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NeuroLynQServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     *Get Client to Server Packets Stats
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> getClientStats(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetClientStatsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *Reset Client to Server Packet Stats
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats> resetClientStats(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getResetClientStatsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_DATA_STREAM = 0;
  private static final int METHODID_GET_CLIENT_STATS = 1;
  private static final int METHODID_RESET_CLIENT_STATS = 2;
  private static final int METHODID_SEND_DATA_STREAM = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final NeuroLynQServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(NeuroLynQServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DATA_STREAM:
          serviceImpl.getDataStream((com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>) responseObserver);
          break;
        case METHODID_GET_CLIENT_STATS:
          serviceImpl.getClientStats((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats>) responseObserver);
          break;
        case METHODID_RESET_CLIENT_STATS:
          serviceImpl.resetClientStats((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.ClientStats>) responseObserver);
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
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class NeuroLynQServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NeuroLynQServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shimmerresearch.managers.grpc.neurolynqservice.ShimmerNeuroLynQService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NeuroLynQService");
    }
  }

  private static final class NeuroLynQServiceFileDescriptorSupplier
      extends NeuroLynQServiceBaseDescriptorSupplier {
    NeuroLynQServiceFileDescriptorSupplier() {}
  }

  private static final class NeuroLynQServiceMethodDescriptorSupplier
      extends NeuroLynQServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    NeuroLynQServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (NeuroLynQServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NeuroLynQServiceFileDescriptorSupplier())
              .addMethod(getGetDataStreamMethod())
              .addMethod(getSendDataStreamMethod())
              .addMethod(getGetClientStatsMethod())
              .addMethod(getResetClientStatsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
