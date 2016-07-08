/*******************************************************************************
 * Copyright 2016 Specure GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * <h1>QoS Testserver Documentation</h1>
 * <ol>
 * 		<li>Communication between server and client</li>
 * 		<li>QoS Testserver Protocol (= QTP)</li>
 * 		<li>Settings</li>
 * 		<li>Testserver debug modes</li> 
 * </ol>  
 *
 * <h2>1. Communication between server and client</h2>
 * <h3>1.1 Overview</h3>
 * Each client is communicating with the test server by using a simple protocol. This protocol is called QoS Testserver Protocol (QTP) and is seperated into 3 parts. 
 * These parts have to be executed in the correct order. All parts are important and none must be omitted. They are:<br>
 * <ol>
 * 		<li>establishing connection / handshake</h1>
 *		<li>test request / test result</h1>
 *		<li>close connection</h1> 		
 * </ol>
 * <br>
 * The rules for the QTP are:
 * <ul>
 * 		<li>Each QTP command has to be in a single line</li>
 * 		<li>A new line character must follow the command string. This marks the end of the command.</li>
 * </ul>
 * <br>
 * <h2>2. QoS Testserver Protocol (QTP)</h2>
 * <h3>2.1 Handshake</h3>
 * A client tries to open a connection on the port the test server is listening (see 3. Settings). If the connection can be established the server sends two lines of information:
 * The first one is the greeting. This is the name and version number of the protocol this server uses. The second one is an <b>ACCEPT</b> command (see 2.4.1).<br>
 * <br>After receiving these two lines of code the client needs to send back the identity token (by using the <b>TOKEN string</b> command). 
 * If the token is valid the server will send back an <b>OK</b> command followed by another <b>ACCEPT</b> command. The connection has been established.<br>
 * <h3>2.2 Test requests</h3>
 * Tests are beeing requested by using a special test request command (see 2.4.2). The server's reaction is different for each test. 
 * Every command sent by the client get a response. This response differs. The only exception is the <b>QUIT</b> command (see 2.3. Close connection) where a connection is cloesd immediately.
 * <h3>2.3 Close connection</h3>
 * By using the <b>QUIT</b> command the client can close a connection by itself    
 * <h3>2.4 Command overview</h3> 
 * All commands can have appendices with a special functionality. See 2.4.3<br>
 * <h4>2.4.1 Server side responses</h4>
 * <ul>
 * 		<li><b>ACCEPT [?]</b> - by sending this command the server is telling the client which commands it will accept for the next request. Example: <i>ACCEPT [TOKEN string]</i> -> means that the server will only accept a <i>TOKEN</i> command followed by a string (=this one is needed to complete the handshake and identify the client)</li>
 * 		<li><b>OK</b> - each command that is beeing received by the server, that is valid and doesn't need a different reply from the server is confirmed with this command</li>
 * 		<li><b>RCV ? ?</b> - this is used after an UDP test to tell the client how much packtes have been received (first INTEGER) and how many of them have been duplicates (second INTEGER)</li>
 * 		<li><b>ERR ?</b> - if an error occurs this message followed by the specific error is beeing returned. Possible errors are:
 * 			<ul>
 * 				<li><b>ERR ILLARG</b> - illegal argument. This is returned if an argument could be parsed but is illegal (out of range, etc.).</li>
 * 				<li><b>ERR UNSUPP</b> - argument unsupported. This is returned if an argument could be parsed but is not valid because it's not supported (old protocol version, etc.).</li>
 * 			</ul>
 * 		</li>
 * </ul>
 * <h4>2.4.2 Client side commands and possible responses</h4>
 * <ul>
 * 		<li><b>TOKEN string</b> - sends a token (received from the control server) tnat is used to identify the client.<br>The response is: <b>OK</b> and an <b>ACCEPT</b> command</li>
 * 		<li><b>UDPTEST IN int int</b> - sends an UDP incoming (servers sends packets to client) test request. First parameter is the port number, the second one is the number of packets.
 *			<p>RETURNS: <b>nothing</b><br>
 * 				The response are UDP packets, that are sent to the client.
 * 			</p>
 * 		</li> 
 * 		<li><b>UDPTEST OUT int int</b> - sends an UDP outgoing (servers receives packets) test request. First parameter is the port number (see <i>GET UDPPORT</i>), the second one is the number of packets.
 *			<p>RETURNS:
 *				<ul>
 *					<li><b>OK</b> test request successful</li>
 *				</ul>
 *			</p>
 *		</li> 
 * 		<li><b>GET UDPPORT</b> - request a random UDP port number the server is listening on.
 *			<p>RETURNS:
 *				<ul>
 *					<li><b>An integer value</b> that represents an available port number.</li>
 *				</ul>
 *			</p>
 *		</li>
 * 		<li><b>TCPTEST IN int</b> - TCP incoming (server tries to establish a connection to client) test request. The only parameter is the port number.
 *			<p>RETURNS: <b>nothing</b><br>
 *			The response is a <b>HELLO TO port_number</b> message to the requested port_number.
 *			</p>
 *		</li>
 * 		<li><b>TCPTEST OUT int</b> - TCP outgoing (client tries to establish a connection to server) test request. The only parameter is the port number.<br>The response is an <b>OK</b> message after a server socket has been opened and is listening on the requested port.
 *			<p>RETURNS:
 * 				<ul>
 * 					<li><b>OK</b> - test request successful</li>
 * 				</ul>
 * 			</p>
 * 		</li>
 * 		<li><b>NTPTEST int</b> - Non transparent proxy test request. The only parameter is the port number.<br>The response is an <b>OK</b> message after a server socket has been opened and is listening on the requested port.
 *			<p>RETURNS:
 *	 			<ul>
 * 					<li><b>OK</b> - test request successful</li>
 * 				</ul>
 * 			</p>
 * 		</li>
 * 		<li><b>GET UDPRESULT IN int</b> - Requests result for an incoming UDP test on a specific port. This can requested any time by the client. The UDP test doesn't need to be finished.
 *			<p>RETURNS:
 *	 			<ul>
 * 					<li><b>RCV int int</b> - see 2.4.1 RCV</li>
 * 				</ul>
 * 			</p>
 * 		</li>
 * 		<li><b>GET UDPRESULT OUT int</b> - Requests result for an outgoing UDP test on a specific port. This can requested any time by the client. The UDP test doesn't need to be finished.
 *			<p>RETURNS:
 *	 			<ul>
 * 					<li><b>RCV int int</b> - see 2.4.1 RCV</li>
 * 				</ul>
 * 			</p>
 * 		</li>
 * 		<li><b>REQUEST CONN TIMEOUT int</b> - Request a new connection timeout, in case the tests on the client side could last much longer than the default timeout (=15s). This value may not be lower than the default timeout (=15s).
 * 			<p>RETURNS:
 * 				<ul>
 * 					<li><b>OK</b> - connection timeout has been changed</li>
 * 					<li><b>ERR ILLARG</b> - requested timeout is invalid (too small, not a number, etc.)</li>
 * 				</ul>
 * 			</p>
 * 		</li>
 * 		<li><b>REQUEST PROTOCOL VERSION int</b> - If the client wish to use a different protocol version as the default one it can request a specific version by using this command.
 * 			<p>RETURNS:
 * 				<ul>
 * 					<li><b>OK</b> - the version has been changed</li>
 * 					<li><b>ERR UNSUPP</b> - protocol version is not supported by the server</li>
 *	 			</ul>
 * 			</p>
 * 		</li>
 * 	 	<li><b>QUIT</b> - closes the connection.<br>There is no response to this command</li>
 * </ul>
 * <h4>2.4.3 Command appendices</h4>
 * An appendix can be added to each command for extended functionality. Each appendix is added after a command, but before the new-line chanaracter. An appendix must follow a plus (+) sign. Example: <b>TCPTEST OUT 80 +ID17</b>
 * <br>The following appendices are supported:
 * <ul>
 * 		<li><b>ID</b> - If this appendix is added to a command the server will answer this command by adding this same appendix to the response. This is used for multi-threaded environments. The example from above<b>TCPTEST OUT 80 +ID17</b> would produce a reponse that will look like this: <b>OK +ID17</b></li>
 * </ul>
 * <br>
 * <h2>3. Server Settings</h3>
 * The server settings can be set by using either predefined default values, command line parameters or a settings file.<br>
 * <h3>3.1 Preset default values</h3>
 * If neither a configuration file is available nor the command line parameters are set then the default values are used. They are:  
 * <ol>
 * 		<li><i>Configuration file:</i> <b>config.properties</b></li>
 *		<li><i>Testserver IP:</i> <b>all available interfaces</b> (see: <i>3.2.1 server.ip</i>)</li>
 *		<li><i>Testserver port number:</i> <b>5233</b> (see: <i>3.2.2 server.port</i>)</li>
 *		<li><i>Supported UDP ports:</i> <b>none</b> (see: <i>3.2.3, 3.2.4 'server.udp.*'</i>)</li>
 *		<li><i>Max number of threads:</i> <b>200</b> (see: <i>3.2.5 server.threads</i>)</li>
 *		<li><i>Secret key:</i> <b>there is no default secret key. This setting is required</b> (see: <i>3.2.6 server.secret</i>)</li>
 *		<li><i>Verbose level: </i> <b>the verbose (debug output) level: either 0 (=some debug), 1 (=more debug) or 2 (=most debug)</b> (see: <i>3.2.7 server.verbose</i>)</li>
 *		<li><i>Secure flag: </i> <b>tells the server to use SSL Sockets</b> (see: <i>3.2.8 server.ssl</i>)</li>
 *		<li><i>Log files:</i> <b>none</b> (see: <i>3.2.10 'server.log.*'</i>)</li>
 *		<li><i>Command console:</i> <b>disabled</b> (see: <i>3.2.11 'server.console'</i>)</li>
 *		<li><i>Log console:</i> <b>disabled</b> (see: <i>3.2.11 'server.log.console'</i>)</li>
 * </ol>
 * <h3>3.2 Settings file</h3>
 * If a settings file is available the parameters set inside have the highest priority. The following parameters are available inside a configuration file:   
 * <ol>
 *		<li><b>server.ip</b> <i>Testserver IPs:</i> binds the server to these IPs. Multiple IPs can be seperated by a comma. The test server accepts both: IPv4 and IPv6.</li>
 *		<li><b>server.port</b> <i>Testserver port number</i></li>
 *		<li><b>server.udp.minport</b> and <b>server.udp.maxport</b> <i>Supported UDP port range</i>: All UDP ports inside this range (inclusive boudaries) will be opened for incoming UDP tests.</li>
 *		<li><b>server.udp.ports</b> <i>Supported UPD port list:</i> multiple ports can be seperated by a comma, e.g.: <b>22,443,4551,23435</b>: All ports on this list will be opened for incoming UDP tests.</li>
 *		<li><b>server.threads</b> <i>Max number of threads (=max number of control connections)</i></li>
 *		<li><b>server.secret</b> <i>Secret key:</i></li>
 *		<li><b>server.verbose</b> <i>Verbose level:</i> values: <b>0/1/2</b></li>
 *		<li><b>server.ssl</b> <i>SSL settings:</i> values: <b>true/false</b></li>
 *		<li><b>server.ip.check</b> <i>Client IP check:</i> values: <b>true/false</b>. Checks the IP of tcp test candidates. If set to true a candidate map will be managed by the server, where only allowed client IPs (got during the test registration process) will get responses from the qos server.</li>
 *		<li><i>Log files. The file names should contain the full path with a prefix  (e.g.: <b>/var/log/main</b>). There will be an automatically generated suffix (date + ".log" ending)</i> 
 *			<ul>
 *				<li><b>server.log</b> main qos log file</li>
 *				<li><b>server.log.udp</b> log file for all udp oprations</li>
 *				<li><b>server.log.tcp</b> log file for all tcp operations</li>
 *			</ul>
 *		</li>
 *		<li>Other log settings:
 *			<ul>
 *				<li><b>server.log.console</b> values: <b>true/false</b>, if true, all debug will be send to the available console</li>
 *				<li><b>server.console</b> values: <b>true/false</b>, if true the server command mode (=console) can be accessed</li>
 *			</ul>
 *		</li>
 * </ol>
 * <h3>3.3 Command line parameter</h3>
 * By launching the test server the following command line parameters can be used:  
 * <ol>
 * 		<li><i>Configuration file:</i> <b>-f [file_name]</b></li>
 *		<li><i>Testserver port number:</i> <b>-p [port_number]</b></li>
 *		<li><i>Supported UDP port range:</i> <b>-u [port_from] [port_to]</b></li>
 *		<li><i>Max number of threads:</i> <b>-t [num_of_threads]</b></li>
 *		<li><i>Secret key:</i> <b>-k [secret_key]</b></li>
 *		<li><i>Show help:</i> <b>-h</b></li>
 *		<li><i>Verbose:</i> <b>-v</b> (= verbose level 1) or <b>-vv</b> (= verbose level 2), if this parameter is not set verbose is set to level 0 automatically.</li>
 *		<li><i>Secure flag:</i> <b>-s</b></li>
 *		<li><i>Main log file (for more log files use a config file - see 3.4):</i> <b>-l</b></li>
 * </ol>
 * <br>
 *
 * <h2>4. Testserver debug modes</h2>
 * <h3>4.1 Simple debug mode</h3>
 * This is the default mode, if debug output is enabled (see <b>3.2.11 server.log.console</b>). In this mode the test server will print debug text (the amount of debug messages depends on the verbose setting (see <b>3.2.7 server.verbose</b>)<br>
 * <h3>4.2 Command mode</h3>
 * To enter the command mode press the <b>return</b> key (if enabled, see <b>3.2.11 server.console</b>). A prompt will indicate that commands can be submitted now.<br>
 * In this mode there will be no debug output. To exit the command mode type '<b>exit</b>' and press enter.<br>
 * The following commands are supported: 
 * <ul>
 * 		<li><i>help</i> - help and a command reference</li>
 * 		<li><i>show tcp [force]</i> - displays the current active tcp sockets. If the number is greater than 500 the command will be ignored unless <i>force</i> is appended to it.</li>
 * 		<li><i>show udp</i> - displays the current udp port range, sub options are available:</li>
 * 		<ul>
 *			<li><i>show udp data</i> - displays all UDP ports containing client data</li>
 *			<li><i>show udp nodata</i> - displays all "empty" UDP ports containing no client data</li>
 *		</ul>
 * 		<li><i>show clients</i> - displays a list of active/opened client connections</li>
 * 		<li><i>show info</i> - displays some information about the qos test server</li>
 * 		<li><i>set</i> - displays the current test server settings</li>
 * 		<li><i>set verbose [0/1/2]</i> - set a new verbose level</li>
 * 		<li><i>exit</i> - switch back to debug mode</li>
 * </ul>
 *
 */
package at.alladin.rmbt.qos.testserver;