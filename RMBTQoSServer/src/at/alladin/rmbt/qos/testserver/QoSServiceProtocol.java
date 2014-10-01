/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
 ******************************************************************************/
package at.alladin.rmbt.qos.testserver;

/**
 * 
 * @author lb
 *
 */
public class QoSServiceProtocol {
	
	/**
	 * 
	 */
	public final static String GREETING = "QoSSP0.1";
	
	/**
	 * 
	 */
	public final static String ACCEPT_TOKEN = "ACCEPT [TOKEN string]";
	
	/**
	 * 
	 */
	public final static String ACCEPT_COMMANDS = "ACCEPT [UDPTEST IN/OUT int int] [TCPTEST IN/OUT int] [NTPTEST int] [GET UDPPORT/UDPPORTS] [QUIT]";
			
	/**
	 * 
	 */
	public final static String UDP_TEST_IN = "UDPTEST IN";
	
	/**
	 * 
	 */
	public final static String UDP_TEST_IN_RESPONSE = "UDPTEST IN RESPONSE";

	/**
	 * 
	 */
	public final static String UDP_TEST_OUT = "UDPTEST OUT";
	
	/**
	 * 
	 */
	public final static String UDP_TEST_OUT_RESPONSE = "UDPTEST OUT RESPONSE";

	/**
	 * 
	 */
	public final static String TCP_TEST_IN = "TCPTEST IN";
	
	/**
	 * 
	 */
	public final static String TCP_TEST_OUT = "TCPTEST OUT";
	
	/**
	 * 
	 */
	public final static String NON_TRANSPARENT_PROXY_TEXT = "NTPTEST";
	
	/**
	 * 
	 */
	public final static String REQUEST_UDP_PORT_RANGE = "GET UDPPORTS";
	
	/**
	 * 
	 */
	public final static String REQUEST_UDP_PORT = "GET UDPPORT";
	
	/**
	 * 
	 */
	public final static String REQUEST_QUIT = "QUIT";
	
	
	/********************************************
	 * SERVER RESPONSES:
	 ********************************************/
	
	/**
	 * 
	 */
	public final static String OK_RESPONSE = "OK";
	
	/**
	 * 
	 */
	public final static String TOKEN_OK_RESPONSE = "OK";
	
	/**
	 * 
	 */
	public final static String RESPONSE_UDP_NUM_PACKETS_RECEIVED = "RCV";
	
	
	/********************************************
	 * TIMEOUTS:
	 ********************************************/
	
	/**
	 * 
	 */
	public final static int TIMEOUT_NON_TRANSPARENT_PROXY_TEST = 10000;

	
	/********************************************
	 * UDP PACKET DATA
	 ********************************************/
	
	/**
	 * the first byte of an UDP packet, no response
	 */
	public final static byte UDP_TEST_ONE_DIRECTION_IDENTIFIER = 1;
	
	/**
	 * the first byte of an UDP packet, with a request for a response
	 */
	public final static byte UDP_TEST_AWAIT_RESPONSE_IDENTIFIER = 3;
	
	/**
	 * the first byte of an response UDP packet
	 */
	public final static byte UDP_TEST_RESPONSE = 2;
}
