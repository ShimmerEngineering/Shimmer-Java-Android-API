# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
import grpc

import ShimmerGrpcAndOJC_pb2 as ShimmerGrpcAndOJC__pb2


class ShimmerServerStub(object):
  """The greeter service definition.
  """

  def __init__(self, channel):
    """Constructor.

    Args:
      channel: A grpc.Channel.
    """
    self.SayHello = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/SayHello',
        request_serializer=ShimmerGrpcAndOJC__pb2.HelloRequest.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.HelloReply.FromString,
        )
    self.GetDataStream = channel.unary_stream(
        '/shimmerGRPC.ShimmerServer/GetDataStream',
        request_serializer=ShimmerGrpcAndOJC__pb2.StreamRequest.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.ObjectCluster2.FromString,
        )
    self.SendDataStream = channel.stream_unary(
        '/shimmerGRPC.ShimmerServer/SendDataStream',
        request_serializer=ShimmerGrpcAndOJC__pb2.ObjectCluster2.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.HelloReply.FromString,
        )
    self.SendFileStream = channel.stream_unary(
        '/shimmerGRPC.ShimmerServer/SendFileStream',
        request_serializer=ShimmerGrpcAndOJC__pb2.FileByteTransfer.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.HelloReply.FromString,
        )
    self.ConnectShimmer = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/ConnectShimmer',
        request_serializer=ShimmerGrpcAndOJC__pb2.ShimmerRequest.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.CommandStatus.FromString,
        )
    self.StartStreaming = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/StartStreaming',
        request_serializer=ShimmerGrpcAndOJC__pb2.ShimmerRequest.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.CommandStatus.FromString,
        )
    self.CloseApplication = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/CloseApplication',
        request_serializer=ShimmerGrpcAndOJC__pb2.ShimmerRequest.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.CommandStatus.FromString,
        )
    self.GetDockedShimmerInfo = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/GetDockedShimmerInfo',
        request_serializer=ShimmerGrpcAndOJC__pb2.StringMsg.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.ShimmersInfo.FromString,
        )
    self.SetWorkspaceDirectory = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/SetWorkspaceDirectory',
        request_serializer=ShimmerGrpcAndOJC__pb2.StringMsg.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.OperationRequest.FromString,
        )
    self.GetWorkspaceDirectory = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/GetWorkspaceDirectory',
        request_serializer=ShimmerGrpcAndOJC__pb2.StringMsg.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.StringMsg.FromString,
        )
    self.PairShimmers = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/PairShimmers',
        request_serializer=ShimmerGrpcAndOJC__pb2.StringArrayMsg.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.StringMsg.FromString,
        )
    self.GetOperationProgress = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/GetOperationProgress',
        request_serializer=ShimmerGrpcAndOJC__pb2.StringMsg.SerializeToString,
        response_deserializer=ShimmerGrpcAndOJC__pb2.OperationRequest.FromString,
        )


class ShimmerServerServicer(object):
  """The greeter service definition.
  """

  def SayHello(self, request, context):
    """Sends a greeting
    """
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetDataStream(self, request, context):
    """Client asking for data
    """
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def SendDataStream(self, request_iterator, context):
    """Client sending data
    """
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def SendFileStream(self, request_iterator, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def ConnectShimmer(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def StartStreaming(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def CloseApplication(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetDockedShimmerInfo(self, request, context):
    """ConsensysApi related
    """
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def SetWorkspaceDirectory(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetWorkspaceDirectory(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def PairShimmers(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetOperationProgress(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')


def add_ShimmerServerServicer_to_server(servicer, server):
  rpc_method_handlers = {
      'SayHello': grpc.unary_unary_rpc_method_handler(
          servicer.SayHello,
          request_deserializer=ShimmerGrpcAndOJC__pb2.HelloRequest.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.HelloReply.SerializeToString,
      ),
      'GetDataStream': grpc.unary_stream_rpc_method_handler(
          servicer.GetDataStream,
          request_deserializer=ShimmerGrpcAndOJC__pb2.StreamRequest.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.ObjectCluster2.SerializeToString,
      ),
      'SendDataStream': grpc.stream_unary_rpc_method_handler(
          servicer.SendDataStream,
          request_deserializer=ShimmerGrpcAndOJC__pb2.ObjectCluster2.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.HelloReply.SerializeToString,
      ),
      'SendFileStream': grpc.stream_unary_rpc_method_handler(
          servicer.SendFileStream,
          request_deserializer=ShimmerGrpcAndOJC__pb2.FileByteTransfer.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.HelloReply.SerializeToString,
      ),
      'ConnectShimmer': grpc.unary_unary_rpc_method_handler(
          servicer.ConnectShimmer,
          request_deserializer=ShimmerGrpcAndOJC__pb2.ShimmerRequest.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.CommandStatus.SerializeToString,
      ),
      'StartStreaming': grpc.unary_unary_rpc_method_handler(
          servicer.StartStreaming,
          request_deserializer=ShimmerGrpcAndOJC__pb2.ShimmerRequest.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.CommandStatus.SerializeToString,
      ),
      'CloseApplication': grpc.unary_unary_rpc_method_handler(
          servicer.CloseApplication,
          request_deserializer=ShimmerGrpcAndOJC__pb2.ShimmerRequest.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.CommandStatus.SerializeToString,
      ),
      'GetDockedShimmerInfo': grpc.unary_unary_rpc_method_handler(
          servicer.GetDockedShimmerInfo,
          request_deserializer=ShimmerGrpcAndOJC__pb2.StringMsg.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.ShimmersInfo.SerializeToString,
      ),
      'SetWorkspaceDirectory': grpc.unary_unary_rpc_method_handler(
          servicer.SetWorkspaceDirectory,
          request_deserializer=ShimmerGrpcAndOJC__pb2.StringMsg.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.OperationRequest.SerializeToString,
      ),
      'GetWorkspaceDirectory': grpc.unary_unary_rpc_method_handler(
          servicer.GetWorkspaceDirectory,
          request_deserializer=ShimmerGrpcAndOJC__pb2.StringMsg.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.StringMsg.SerializeToString,
      ),
      'PairShimmers': grpc.unary_unary_rpc_method_handler(
          servicer.PairShimmers,
          request_deserializer=ShimmerGrpcAndOJC__pb2.StringArrayMsg.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.StringMsg.SerializeToString,
      ),
      'GetOperationProgress': grpc.unary_unary_rpc_method_handler(
          servicer.GetOperationProgress,
          request_deserializer=ShimmerGrpcAndOJC__pb2.StringMsg.FromString,
          response_serializer=ShimmerGrpcAndOJC__pb2.OperationRequest.SerializeToString,
      ),
  }
  generic_handler = grpc.method_handlers_generic_handler(
      'shimmerGRPC.ShimmerServer', rpc_method_handlers)
  server.add_generic_rpc_handlers((generic_handler,))
