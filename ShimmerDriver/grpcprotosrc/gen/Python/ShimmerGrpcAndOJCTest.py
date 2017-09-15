from __future__ import print_function

import random
import time

import grpc

from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2
from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2_grpc

def helloServer(stub):
  response = stub.SayHello(ShimmerGrpcAndOJC_pb2.HelloRequest(name='Mark'))
  print(response.message)

def closeApplication(stub):
  response = stub.CloseApplication(ShimmerGrpcAndOJC_pb2.ShimmerRequest())
  print('closeApplication')

def getWorkspaceDirectory(stub):
  response = stub.GetWorkspaceDirectory(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
  print('Workspace Directory=' + response.message)

def run():
  channel = grpc.insecure_channel('localhost:50051')
  stub = ShimmerGrpcAndOJC_pb2_grpc.ShimmerServerStub(channel)
  helloServer(stub)
  getWorkspaceDirectory(stub)
  #closeApplication(stub)

if __name__ == '__main__':
  run()