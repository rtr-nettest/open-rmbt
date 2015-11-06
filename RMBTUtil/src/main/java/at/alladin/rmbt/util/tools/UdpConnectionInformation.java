/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.util.tools;

/**
 * holder for udp connection information
 * @author lb
 *
 */
public class UdpConnectionInformation extends ConnectionInformation {

	/**
	 * parses a linux /proc/net/ address string and returns an object
	 * @param str
	 * @param addrSize
	 * @return
	 */
	public static UdpConnectionInformation parseAndroid(String str, int addrSize) {
		UdpConnectionInformation conn = new UdpConnectionInformation();

		String[] token = str.trim().replaceAll(" +", " ").split(" ");
		if (token.length < 12)
			return null;

		try {
			conn.setLocalAddr(getAddress(addrSize, token[1]));
			conn.setConnectionState(ConnectionState.ESTABLISHED);
			conn.setProtocolType(addrSize == 16 ? ProtocolType.UDP6 : ProtocolType.UDP);
			conn.setUid(Integer.parseInt(token[7]));
		}
		catch (Exception e) { 
			//return null in case of an exception 
			return null;
		}
		
		return conn;
	}
}
