QoS Testserver Documentation
============================

1.  Communication between server and client
2.  QoS Testserver Protocol (= QTP)
3.  Settings
4.  Testserver debug modes

1\. Communication between server and client
-------------------------------------------

### 1.1 Overview

Each client is communicating with the test server by using a simple protocol. This protocol is called QoS Testserver Protocol (QTP) and is seperated into 3 parts. These parts have to be executed in the correct order. All parts are important and none must be omitted. They are:  

1.  establishing connection / handshake
2.  test request / test result
3.  close connection

  
The rules for the QTP are:

*   Each QTP command has to be in a single line
*   A new line character must follow the command string. This marks the end of the command.

  

2\. QoS Testserver Protocol (QTP)
---------------------------------

### 2.1 Handshake

A client tries to open a connection on the port the test server is listening (see 3. Settings). If the connection can be established the server sends two lines of information: The first one is the greeting. This is the name and version number of the protocol this server uses. The second one is an **ACCEPT** command (see 2.4.1).  
  
After receiving these two lines of code the client needs to send back the identity token (by using the **TOKEN string** command). If the token is valid the server will send back an **OK** command followed by another **ACCEPT** command. The connection has been established.  

### 2.2 Test requests

Tests are beeing requested by using a special test request command (see 2.4.2). The server's reaction is different for each test. Every command sent by the client get a response. This response differs. The only exception is the **QUIT** command (see 2.3. Close connection) where a connection is cloesd immediately.

### 2.3 Close connection

By using the **QUIT** command the client can close a connection by itself

### 2.4 Command overview

All commands can have appendices with a special functionality. See 2.4.3  

#### 2.4.1 Server side responses

*   **ACCEPT \[?\]** - by sending this command the server is telling the client which commands it will accept for the next request. Example: _ACCEPT \[TOKEN string\]_ -> means that the server will only accept a _TOKEN_ command followed by a string (=this one is needed to complete the handshake and identify the client)
*   **OK** - each command that is beeing received by the server, that is valid and doesn't need a different reply from the server is confirmed with this command
*   **RCV ? ?** - this is used after an UDP test to tell the client how much packtes have been received (first INTEGER) and how many of them have been duplicates (second INTEGER)
*   **ERR ?** - if an error occurs this message followed by the specific error is beeing returned. Possible errors are:
    *   **ERR ILLARG** - illegal argument. This is returned if an argument could be parsed but is illegal (out of range, etc.).
    *   **ERR UNSUPP** - argument unsupported. This is returned if an argument could be parsed but is not valid because it's not supported (old protocol version, etc.).

#### 2.4.2 Client side commands and possible responses

*   **TOKEN string** - sends a token (received from the control server) tnat is used to identify the client.  
    The response is: **OK** and an **ACCEPT** command
*   **UDPTEST IN int int** - sends an UDP incoming (servers sends packets to client) test request. First parameter is the port number, the second one is the number of packets.
    
    RETURNS: **nothing**  
    The response are UDP packets, that are sent to the client.
    
*   **UDPTEST OUT int int** - sends an UDP outgoing (servers receives packets) test request. First parameter is the port number (see _GET UDPPORT_), the second one is the number of packets.
    
    RETURNS:
    
    *   **OK** test request successful
    
*   **GET UDPPORT** - request a random UDP port number the server is listening on.
    
    RETURNS:
    
    *   **An integer value** that represents an available port number.
    
*   **TCPTEST IN int** - TCP incoming (server tries to establish a connection to client) test request. The only parameter is the port number.
    
    RETURNS: **nothing**  
    The response is a **HELLO TO port\_number** message to the requested port\_number.
    
*   **TCPTEST OUT int** - TCP outgoing (client tries to establish a connection to server) test request. The only parameter is the port number.  
    The response is an **OK** message after a server socket has been opened and is listening on the requested port.
    
    RETURNS:
    
    *   **OK** - test request successful
    
*   **NTPTEST int** - Non transparent proxy test request. The only parameter is the port number.  
    The response is an **OK** message after a server socket has been opened and is listening on the requested port.
    
    RETURNS:
    
    *   **OK** - test request successful
    
*   **GET UDPRESULT IN int** - Requests result for an incoming UDP test on a specific port. This can requested any time by the client. The UDP test doesn't need to be finished.
    
    RETURNS:
    
    *   **RCV int int** - see 2.4.1 RCV
    
*   **GET UDPRESULT OUT int** - Requests result for an outgoing UDP test on a specific port. This can requested any time by the client. The UDP test doesn't need to be finished.
    
    RETURNS:
    
    *   **RCV int int** - see 2.4.1 RCV
    
*   **REQUEST CONN TIMEOUT int** - Request a new connection timeout, in case the tests on the client side could last much longer than the default timeout (=15s). This value may not be lower than the default timeout (=15s).
    
    RETURNS:
    
    *   **OK** - connection timeout has been changed
    *   **ERR ILLARG** - requested timeout is invalid (too small, not a number, etc.)
    
*   **REQUEST PROTOCOL VERSION int** - If the client wish to use a different protocol version as the default one it can request a specific version by using this command.
    
    RETURNS:
    
    *   **OK** - the version has been changed
    *   **ERR UNSUPP** - protocol version is not supported by the server
    
*   **QUIT** - closes the connection.  
    There is no response to this command

#### 2.4.3 Command appendices

An appendix can be added to each command for extended functionality. Each appendix is added after a command, but before the new-line chanaracter. An appendix must follow a plus (+) sign. Example: **TCPTEST OUT 80 +ID17**  
The following appendices are supported:

*   **ID** - If this appendix is added to a command the server will answer this command by adding this same appendix to the response. This is used for multi-threaded environments. The example from above**TCPTEST OUT 80 +ID17** would produce a reponse that will look like this: **OK +ID17**

  

3\. Server Settings
-------------------

The server settings can be set by using either predefined default values, command line parameters or a settings file.  

### 3.1 Preset default values

If neither a configuration file is available nor the command line parameters are set then the default values are used. They are:

1.  _Configuration file:_ **config.properties**
2.  _Testserver IP:_ **all available interfaces** (see: _3.2.1 server.ip_)
3.  _Testserver port number:_ **5233** (see: _3.2.2 server.port_)
4.  _Supported UDP ports:_ **none** (see: _3.2.3, 3.2.4 'server.udp.'_)
5.  _Max number of threads:_ **200** (see: _3.2.5 server.threads_)
6.  _Secret key:_ **there is no default secret key. This setting is required** (see: _3.2.6 server.secret_)
7.  _Verbose level:_ **the verbose (debug output) level: either 0 (=some debug), 1 (=more debug) or 2 (=most debug)** (see: _3.2.7 server.verbose_)
8.  _Secure flag:_ **tells the server to use SSL Sockets** (see: _3.2.8 server.ssl_)
9.  _Log files:_ **none** (see: _3.2.10 'server.log.'_)
10.  _Command console:_ **disabled** (see: _3.2.11 'server.console'_)
11.  _Log console:_ **disabled** (see: _3.2.11 'server.log.console'_)

### 3.2 Settings file

If a settings file is available the parameters set inside have the highest priority. The following parameters are available inside a configuration file:

1.  **server.ip** _Testserver IPs:_ binds the server to these IPs. Multiple IPs can be seperated by a comma. The test server accepts both: IPv4 and IPv6.
2.  **server.port** _Testserver port number_
3.  **server.udp.minport** and **server.udp.maxport** _Supported UDP port range_: All UDP ports inside this range (inclusive boudaries) will be opened for incoming UDP tests.
4.  **server.udp.ports** _Supported UPD port list:_ multiple ports can be seperated by a comma, e.g.: **22,443,4551,23435**: All ports on this list will be opened for incoming UDP tests.
5.  **server.threads** _Max number of threads (=max number of control connections)_
6.  **server.secret** _Secret key:_
7.  **server.verbose** _Verbose level:_ values: **0/1/2**
8.  **server.ssl** _SSL settings:_ values: **true/false**
9.  **server.ip.check** _Client IP check:_ values: **true/false**. Checks the IP of tcp test candidates. If set to true a candidate map will be managed by the server, where only allowed client IPs (got during the test registration process) will get responses from the qos server.
10.  _Log files. The file names should contain the full path with a prefix (e.g.: **/var/log/main**). There will be an automatically generated suffix (date + ".log" ending)_
    *   **server.log** main qos log file
    *   **server.log.udp** log file for all udp oprations
    *   **server.log.tcp** log file for all tcp operations
11.  Other log settings:
    *   **server.log.console** values: **true/false**, if true, all debug will be send to the available console
    *   **server.console** values: **true/false**, if true the server command mode (=console) can be accessed

### 3.3 Command line parameter

By launching the test server the following command line parameters can be used:

1.  _Configuration file:_ **\-f \[file\_name\]**
2.  _Testserver port number:_ **\-p \[port\_number\]**
3.  _Supported UDP port range:_ **\-u \[port\_from\] \[port\_to\]**
4.  _Max number of threads:_ **\-t \[num\_of\_threads\]**
5.  _Secret key:_ **\-k \[secret\_key\]**
6.  _Show help:_ **\-h**
7.  _Verbose:_ **\-v** (= verbose level 1) or **\-vv** (= verbose level 2), if this parameter is not set verbose is set to level 0 automatically.
8.  _Secure flag:_ **\-s**
9.  _Main log file (for more log files use a config file - see 3.4):_ **\-l**

  

4\. Testserver debug modes
--------------------------

### 4.1 Simple debug mode

This is the default mode, if debug output is enabled (see **3.2.11 server.log.console**). In this mode the test server will print debug text (the amount of debug messages depends on the verbose setting (see **3.2.7 server.verbose**)  

### 4.2 Command mode

To enter the command mode press the **return** key (if enabled, see **3.2.11 server.console**). A prompt will indicate that commands can be submitted now.  
In this mode there will be no debug output. To exit the command mode type '**exit**' and press enter.  
The following commands are supported:

*   _help_ - help and a command reference
*   _show tcp \[force\]_ - displays the current active tcp sockets. If the number is greater than 500 the command will be ignored unless _force_ is appended to it.
*   _show udp_ - displays the current udp port range, sub options are available:

*   _show udp data_ - displays all UDP ports containing client data
*   _show udp nodata_ - displays all "empty" UDP ports containing no client data

*   _show clients_ - displays a list of active/opened client connections
*   _show info_ - displays some information about the qos test server
*   _set_ - displays the current test server settings
*   _set verbose \[0/1/2\]_ - set a new verbose level
*   _exit_ - switch back to debug mode

Appendix
--------

### TLS Key Generation

```bash
keytool -genkey -keyalg RSA -alias qos-server -keystore qosserver.jks -storepass \[ENTER\_YOUR\_STORE\_PW\_HERE\] -keypass \[ENTER\_YOUR\_ALIAS\_PW\_HERE\]
```

