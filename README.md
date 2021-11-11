# LINFO2142 - QUIC on Android

## APK
    APK of the application can be downloaded on https://we.tl/t-BXtxVySbn7

## How to use the application
    The input text allows the user to type the domain name he wants to send QUIC packets.
    If the domain name is valid and the server can be contacted, statistics of the QUIC transfer will be displayed in the output text. 
    If the domain name entered is "ingi", the packets will be sent to a VM hosted on Ingi Cloud.
    The application also detects and notify if the contacted server does not support QUIC.
    
## File Hierarchy
    - android: contains the Android Studio code to create the app
    - cargo: contains the Rust library that uses quiche to send packets to the server
    - script: contains scripts to analyze qlogs generated from data transfers
    - server: contains the quiche server
    
## Resources
    The application and the server are built on top of cloudfare/quiche (https://github.com/cloudflare/quiche), an implementation of the QUIC transport protocol and HTTP/3.
    
## Authors
    Maxime de Neuville
    Arno Galand
    Group 4