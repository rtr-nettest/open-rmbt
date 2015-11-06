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

import java.net.InetAddress;
import java.net.InetSocketAddress;


/**
 * 
 * @author lb
 *
 */
public abstract class ConnectionInformation {
	
	/**
	 * found in:
	 * http://git.kernel.org/cgit/linux/kernel/git/torvalds/linux.git/tree/include/net/tcp_states.h?id=HEAD
	 * <br><br>
	 * the enum value {@link ConnectionState#UNKNOWN} is only a placeholder, 
	 * because the state value is not zero-based (the first "real" state is {@link ConnectionState#ESTABLISHED} which = 1)
	 * 
	 * @author lb
	 *
	 */
	public static enum ConnectionState {
		UNKNOWN, 
		ESTABLISHED,
		SYN_SENT,
		SYN_RECV,
		FIN_WAIT1,
		FIN_WAIT2,
		TIME_WAIT,
		CLOSE,
		CLOSE_WAIT,
		LAST_ACK,
		LISTEN,
		CLOSING,	/* Now a valid state */
		MAX_STATES	/* Leave at the end! */
	}
	
	public static enum ProtocolType {
		UDP,
		UDP6,
		TCP,
		TCP6
	}
	
	InetSocketAddress localAddr;
	
	InetSocketAddress remoteAddr;

	ConnectionState connectionState;
	
	ProtocolType protocolType;
	
	int uid;
	
	public ConnectionInformation() {
		
	}

	public ConnectionInformation(InetSocketAddress localAddr, InetSocketAddress remoteAddr, ConnectionState state, 
			ProtocolType type, int uid) {
		this.localAddr = localAddr;
		this.remoteAddr = remoteAddr;
		this.connectionState = state;
		this.protocolType = type;
		this.uid = uid;
	}	
	
	public InetSocketAddress getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(InetSocketAddress localAddr) {
		this.localAddr = localAddr;
	}

	public InetSocketAddress getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(InetSocketAddress remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}

	public ProtocolType getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(ProtocolType protocolType) {
		this.protocolType = protocolType;
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "ConnectionInformation [localAddr=" + localAddr
				+ ", remoteAddr=" + remoteAddr + ", connectionState="
				+ connectionState + ", protocolType=" + protocolType + ", uid="
				+ uid + "]";
	}

	/**
	 * parse a /proc/net/ address string to an InetSocketAddress
	 * @param addrSizeInBytes the address size in bytes (IPv4 = 4 bytes, IPv6 = 16 bytes)
	 * @param str
	 * @return
	 */
	protected static InetSocketAddress getAddress(int addrSizeInBytes, String str) {
		InetSocketAddress ret = null;
		String[] token = str.split(":");
		byte[] addr = new byte[addrSizeInBytes];

		try {
			addr = ipHexStringToBytes(str, addrSizeInBytes);
			InetAddress inetAddress = InetAddress.getByAddress(addr);
			ret = new InetSocketAddress(inetAddress, Integer.parseInt(token[1], 16));
		} 
		catch (Exception e) { }

		return ret;
	}
	
	/**
	 * 
	 * @param ipString
	 * @return
	 */
	public static boolean isMappedIPv4Address(String ipString) {
		byte[] bytes = ipHexStringToBytes(ipString, 16);
		return isMappedIPv4Address(bytes);
	}

	/**
	 * 
	 * @param ipString
	 * @return
	 */
	public static boolean isMappedIPv4Address(byte[] bytes) {
		if (bytes != null && bytes.length == 16) {
			for (int i = 0; i < 10; i++) {
				if (bytes[i] != 0) {
				return false;
				}
			}
			for (int i = 10; i < 12; i++) {
				if (bytes[i] != (byte) 0xff) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param ipString
	 * @return
	 */
	 private static byte[] ipHexStringToBytes(String ipString, int addrSizeInBytes) {
		 byte[] addr = new byte[addrSizeInBytes];
		 if (addrSizeInBytes == 16) {
			 //if IPv6 then use counter-intuitive order:
			 //address = 4 WORDS (each with 4 bytes). In each WORD all bytes are written backwards
			 for (int i = 0; i < 4; i++) {
				 String fword = ipString.substring(i*8, i*8 + 8);
				 for (int j = 3; j >= 0; j--) {
					 addr[(i*4) + (3-j)] = (byte) Integer.parseInt(fword.substring(j*2, j*2+2), 16);
				 }
			 }
		 }
		 else {
			 for (int i = 0; i < addrSizeInBytes; i++) {
				 addr[i] = (byte) Integer.parseInt(ipString.substring(i * 2, i * 2 + 2), 16);
			 }
		 }
		 
		 return addr;
	 }
}