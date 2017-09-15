from __future__ import print_function

import random
import time

import grpc

from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2
from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2_grpc

def helloServer(stub):
  print('Saying Hello to the Server')
  response = stub.SayHello(ShimmerGrpcAndOJC_pb2.HelloRequest(name='Helbling'))
  print('\tResponse' + response.message)

def closeApplication(stub):
  print('Requesting to close Application...')
  response = stub.CloseApplication(ShimmerGrpcAndOJC_pb2.ShimmerRequest())

def getWorkspaceDirectory(stub):
  print('Getting Workspace Directory')
  response = stub.GetWorkspaceDirectory(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
  print('\tWorkspace Directory=' + response.message)

def setWorkspaceDirectory(stub):
  newWorkspace='C:/Users/Shimmer/Documents/StroKare_Workspace/'
  print('Setting Workspace Directory to: ' + newWorkspace)
  response = stub.SetWorkspaceDirectory(ShimmerGrpcAndOJC_pb2.StringMsg(message=newWorkspace))
#  print('\tPass? ' + str(response.success))
#  print('\tMessage=' + response.message)
  print(response)

def getDockedShimmerInfo(stub):
  print('Getting docked Shimmers')
  response = stub.GetDockedShimmerInfo(ShimmerGrpcAndOJC_pb2.StreamRequest(message=''))
  print(response)
#  for key in response.shimmerMap:
#    shimmer = response.shimmerMap[key]
#    print(shimmer.name)
  

def run():
  channel = grpc.insecure_channel('localhost:50051')
  stub = ShimmerGrpcAndOJC_pb2_grpc.ShimmerServerStub(channel)
  
  helloServer(stub)
  getWorkspaceDirectory(stub)
  setWorkspaceDirectory(stub)
  getWorkspaceDirectory(stub)
  getDockedShimmerInfo(stub)
  #closeApplication(stub)

if __name__ == '__main__':
  run()