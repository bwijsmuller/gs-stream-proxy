# gs-stream-proxy 

A proof of concept to be able to stream (big) files from clients to one
or multiple servers running on or connected to a GigaSpaces setup.

The basics:
- the client calls a method on a remote service (rmi)
- the client receives the parameters to run a proxy
- the service meanwhile has setup a listener on a GigaSpace (or an existing one)
- using the proxy the client starts sending data
- the GigaSpace receives the data packets and starts forwarding them to the final destination

It also includes a simple demo to send files using the command line to another machine running the application.

Automatically exported from code.google.com/p/gs-stream-proxy
