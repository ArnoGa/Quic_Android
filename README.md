# LINFO2142 - QUIC on Android

## APK
You can download "quic_android.apk" to install the application on your Android device.

## How to use the application
### Test server selection
This is the first screen you will encounter when launching the application.

The drop down list simply lists HTTP/3 test servers from https://bagder.github.io/HTTP3-test. For each of these, its QUIC implementation is also displayed. 

When you press the "Launch" button, a QUIC GET request will be sent by your phone to the contacted server. As soon as you receive the response, response headers and statistics will appear on your screen.

If you want to connect to our virtual machine hosted on INGI cloud, just select the first server (https://linfo2142-grp4.info.ucl.ac.be)

### Enter domain name
If you click on the "Domain name" button, you will be taken to a screen where you can enter the domain name of the server you want to contact.

The application handles the case where the domain name does not exist or the server cannot be contacted. 

If the domain name is valid and the server can be contacted, statistics of the QUIC transfer will be displayed in the output text (e.g. number of packets sent/received/lost, RTT). Response headers will also be shown on the screen.

Even if the domain name is valid and the server can be contacted, sometimes it simply doesn’t support the QUIC protocol. In this case, the application detects that the number of packets received back from the server is zero, which means that it doesn’t support QUIC or only partially.

### Test your network’s QUIC support on HTTP/3 test servers
This screen appears when you tap the "Test QUIC" button.

This feature allows to user to verify if the QUIC protocol is supported by the network he’s connected to. Indeed, QUIC requests are sent to HTTP/3 test servers and the result for each server is displayed on screen: in green if the request was successful or in red if it failed.
    
## File Hierarchy
- android: contains the Android Studio code to create the app
- cargo: contains the Rust library that uses quiche to send packets to the server
- script: contains scripts to analyze qlogs generated from data transfers
- server: contains the quiche server
    
## Resources
The application and the server are built on top of cloudfare/quiche (https://github.com/cloudflare/quiche), an implementation of the QUIC transport protocol and HTTP/3.
    
## Authors
Maxime de Neuville \
Arno Galand \
Group 4
