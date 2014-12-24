# TCP server example
import socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", 5000))
server_socket.listen(5)

print "TCPServer Waiting for client on port 5000"

while 1:
    client_socket, address = server_socket.accept()
    print "I got a connection from ", address
    while 1:
        data = client_socket.recv(16)
        print "RECEIVED:" , data
    