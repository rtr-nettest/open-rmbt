/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testserver.udp;

import java.util.TreeSet;

import at.alladin.rmbt.qos.testserver.entity.TestCandidate;

public class UdpTestCandidate extends TestCandidate {
	public final static int TTL = 30000;
	
	private TreeSet<Integer> packetsReceived;
	private TreeSet<Integer> packetDuplicates;
	private int numPackets;
	private int remotePort;
	private boolean error;
	private String errorMsg;
	
	private UdpTestCompleteCallback onUdpTestCompleteCallback;
	private UdpPacketReceivedCallback onUdpPacketReceivedCallback;
	
	public UdpTestCandidate() {
		this.packetsReceived = new TreeSet<>();
		this.packetDuplicates = new TreeSet<>();
		this.error = false;
	}

	/**
	 * 
	 * @return
	 */
	public TreeSet<Integer> getPacketsReceived() {
		return packetsReceived;
	}

	/**
	 * 
	 * @param packetsReceived
	 */
	public void setPacketsReceived(TreeSet<Integer> packetsReceived) {
		this.packetsReceived = packetsReceived;
	}
	
	/**
	 * 
	 * @return
	 */
	public TreeSet<Integer> getPacketDuplicates() {
		return packetDuplicates;
	}

	/**
	 * 
	 * @param packetDuplicates
	 */
	public void setPacketDuplicates(TreeSet<Integer> packetDuplicates) {
		this.packetDuplicates = packetDuplicates;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * 
	 * @param error
	 */
	public void setError(boolean error) {
		this.error = error;
	}

	/**
	 * 
	 * @return
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * 
	 * @param errorMsg
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * 
	 * @return
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * 
	 * @param remotePort
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumPackets() {
		return numPackets;
	}

	/**
	 * 
	 * @param numPackets
	 */
	public void setNumPackets(int numPackets) {
		this.numPackets = numPackets;
	}

	/**
	 * 
	 * @return
	 */
	public UdpTestCompleteCallback getOnUdpTestCompleteCallback() {
		return onUdpTestCompleteCallback;
	}

	/**
	 * 
	 * @param onUdpTestCompleteCallback
	 */
	public void setOnUdpTestCompleteCallback(
			UdpTestCompleteCallback onUdpTestCompleteCallback) {
		this.onUdpTestCompleteCallback = onUdpTestCompleteCallback;
	}
	
	/**
	 * 	
	 * @return
	 */
	public UdpPacketReceivedCallback getOnUdpPacketReceivedCallback() {
		return onUdpPacketReceivedCallback;
	}

	/**
	 * 
	 * @param onUdpPacketReceivedCallback
	 */
	public void setOnUdpPacketReceivedCallback(
			UdpPacketReceivedCallback onUdpPacketReceivedCallback) {
		this.onUdpPacketReceivedCallback = onUdpPacketReceivedCallback;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClientUdpData [packetsReceived=" + packetsReceived
				+ ", numPackets=" + numPackets + ", remotePort=" + remotePort
				+ ", error=" + error + ", errorMsg=" + errorMsg + "]";
	}
}
