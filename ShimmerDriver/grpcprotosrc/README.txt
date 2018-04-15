Protoc.exe https://github.com/google/protobuf/releases 
Grpc tools for java http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22protoc-gen-grpc-java%22
Grpc tools for c# http://www.grpc.io/docs/tutorials/basic/csharp.html#generating-client-and-server-code



Example protoc.exe + grpc arguments


ShimmerGrpcAndOJC

Generating Java code, this holds the structure of ObjectCluster and a simple GRPC Client Server
	-I=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc --java_out=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\gen --plugin=protoc-gen-grpc-java=D:\grpcbin\protoc-gen-grpc-java-0.13.2-windows-x86_32.exe --grpc-java_out=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\gen C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\src\ShimmerGrpcAndOJC.proto
OR (relative path generation when the ShimmerDriver project it selected)
	-I=${project_loc}/grpcprotosrc/src --java_out=${project_loc}/grpcprotosrc/gen --plugin=protoc-gen-grpc-java=${project_loc}/src/main/resources/protoc-gen-grpc-java/protoc-gen-grpc-java-1.6.1-windows-x86_32.exe --grpc-java_out=${project_loc}/grpcprotosrc/gen ${project_loc}/grpcprotosrc/src/ShimmerGrpcAndOJC.proto
OR (relative path generation when the ShimmerDriver project it selected - copying directly to the package in use)
	-I=${project_loc}/grpcprotosrc/src --java_out=${project_loc}/src/main/java --plugin=protoc-gen-grpc-java=${project_loc}/src/main/resources/protoc-gen-grpc-java/protoc-gen-grpc-java-1.6.1-windows-x86_32.exe --grpc-java_out=${project_loc}/src/main/java ${project_loc}/grpcprotosrc/src/ShimmerGrpcAndOJC.proto

Generating C# code, this holds the structure of ObjectCluster and a simple GRPC Client Server
	-I=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc --csharp_out=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\gen --plugin=protoc-gen-grpc=C:\Users\Lim\Documents\Shimmer\C#\Shimmer_Csharp_API_v0.2.1\Source\GrpcTest\packages\Grpc.Tools.0.14.0\tools\windows_x64\grpc_csharp_plugin.exe --grpc_out=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\gen C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\src\ShimmerGrpcAndOJC.proto

Generating Python code
	python -m grpc_tools.protoc -IC:\Users\User\ShimmerDevelWorkspace\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\ --python_out=. --grpc_python_out=. C:\Users\User\ShimmerDevelWorkspace\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\src\ShimmerGrpcAndOJC.proto
OR (relative path generation when the ShimmerDriver project it selected)
	-m grpc_tools.protoc -I${project_loc}/grpcprotosrc/src/ --python_out=${project_loc}/grpcprotosrc/gen/Python/ShimmerGrpcPythonLib/ --grpc_python_out=${project_loc}/grpcprotosrc/gen/Python/ShimmerGrpcPythonLib/ ${project_loc}/grpcprotosrc/src/ShimmerGrpcAndOJC.proto

	
LiteProtocolInstructionSet

Generating Java code, this holds the instruction set of lite protocol
	-I=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc --java_out=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\gen C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\src\LiteProtocolInstructionSet.proto

Generating C# code, this holds the instruction set of lite protocol
	-I=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc --csharp_out=C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\gen C:\Users\Lim\git\Shimmer-Java-Android-API\ShimmerDriver\grpcprotosrc\src\LiteProtocolInstructionSet.proto
	
	
	
Help:

Error:
	"google/protobuf/descriptor.proto: File not found.
	ShimmerGrpcAndOJC.proto: Import "google/protobuf/descriptor.proto" was not found or had errors."
Solution:


Error:
	warning: Import google/protobuf/descriptor.proto but not used.
Solution:
	Normal, ignore
	