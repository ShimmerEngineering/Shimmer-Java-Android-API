# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
import grpc

from src import ShimmerGrpcAndOJC_pb2 as src_dot_ShimmerGrpcAndOJC__pb2


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
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloRequest.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloReply.FromString,
        )
    self.GetDataStream = channel.unary_stream(
        '/shimmerGRPC.ShimmerServer/GetDataStream',
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.StreamRequest.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.ObjectCluster2.FromString,
        )
    self.SendDataStream = channel.stream_unary(
        '/shimmerGRPC.ShimmerServer/SendDataStream',
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.ObjectCluster2.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloReply.FromString,
        )
    self.SendFileStream = channel.stream_unary(
        '/shimmerGRPC.ShimmerServer/SendFileStream',
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.File.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloReply.FromString,
        )
    self.ConnectShimmer = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/ConnectShimmer',
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.ShimmerRequest.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.CommandStatus.FromString,
        )
    self.StartStreaming = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/StartStreaming',
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.ShimmerRequest.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.CommandStatus.FromString,
        )
    self.CloseApplication = channel.unary_unary(
        '/shimmerGRPC.ShimmerServer/CloseApplication',
        request_serializer=src_dot_ShimmerGrpcAndOJC__pb2.ShimmerRequest.SerializeToString,
        response_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.CommandStatus.FromString,
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


def add_ShimmerServerServicer_to_server(servicer, server):
  rpc_method_handlers = {
      'SayHello': grpc.unary_unary_rpc_method_handler(
          servicer.SayHello,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloRequest.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloReply.SerializeToString,
      ),
      'GetDataStream': grpc.unary_stream_rpc_method_handler(
          servicer.GetDataStream,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.StreamRequest.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.ObjectCluster2.SerializeToString,
      ),
      'SendDataStream': grpc.stream_unary_rpc_method_handler(
          servicer.SendDataStream,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.ObjectCluster2.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloReply.SerializeToString,
      ),
      'SendFileStream': grpc.stream_unary_rpc_method_handler(
          servicer.SendFileStream,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.File.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.HelloReply.SerializeToString,
      ),
      'ConnectShimmer': grpc.unary_unary_rpc_method_handler(
          servicer.ConnectShimmer,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.ShimmerRequest.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.CommandStatus.SerializeToString,
      ),
      'StartStreaming': grpc.unary_unary_rpc_method_handler(
          servicer.StartStreaming,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.ShimmerRequest.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.CommandStatus.SerializeToString,
      ),
      'CloseApplication': grpc.unary_unary_rpc_method_handler(
          servicer.CloseApplication,
          request_deserializer=src_dot_ShimmerGrpcAndOJC__pb2.ShimmerRequest.FromString,
          response_serializer=src_dot_ShimmerGrpcAndOJC__pb2.CommandStatus.SerializeToString,
      ),
  }
  generic_handler = grpc.method_handlers_generic_handler(
      'shimmerGRPC.ShimmerServer', rpc_method_handlers)
  server.add_generic_rpc_handlers((generic_handler,))
