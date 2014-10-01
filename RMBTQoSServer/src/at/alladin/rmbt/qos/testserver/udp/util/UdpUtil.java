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
package at.alladin.rmbt.qos.testserver.udp.util;

import java.net.InetAddress;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.entity.ClientToken;
import at.alladin.rmbt.qos.testserver.udp.ClientUdpData;
import at.alladin.rmbt.qos.testserver.udp.UdpMultiClientServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class UdpUtil {

	/**
	 * 
	 * @param localAddr
	 * @param port
	 * @param token
	 * @param udpData
	 * @return
	 */
	public static UdpMultiClientServer registerCandidate(InetAddress localAddr, int port, ClientToken token, ClientUdpData udpData) {
		TestServerConsole.log("Trying to register UDP Candidate on " + localAddr + ":" + port , 0, TestServerServiceEnum.UDP_SERVICE);
		for (UdpMultiClientServer udpServer : TestServer.udpServerMap.get(port)) {
			TestServerConsole.log("Comparing: " + localAddr + " <-> " + udpServer.getAddress(), 0, TestServerServiceEnum.UDP_SERVICE);
			if (udpServer.getAddress().equals(localAddr)) {
				TestServerConsole.log("Registering UDP Candidate on " + localAddr + ":" + port , 0, TestServerServiceEnum.UDP_SERVICE);
				udpServer.getIncomingMap().put(token.getUuid(), udpData);
				return udpServer;
			}
		}
		
		return null;
	}	
}
