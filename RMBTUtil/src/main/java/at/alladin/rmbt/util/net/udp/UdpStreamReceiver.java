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
package at.alladin.rmbt.util.net.udp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import at.alladin.rmbt.util.net.udp.StreamSender.UdpStreamCallback;

public class UdpStreamReceiver {

	/**
	 * 
	 * @author lb
	 *
	 */
	public static class UdpStreamReceiverSettings {
		int packets;
		long delay;
		boolean sendResponse = false;
		DatagramSocket socket;

		public UdpStreamReceiverSettings(DatagramSocket socket, int packets, boolean sendResponse) {
			this.socket = socket;
			this.packets = packets;
			this.sendResponse = sendResponse;
		}
		
		public DatagramSocket getSocket() {
			return socket;
		}
		public void setSocket(DatagramSocket socket) {
			this.socket = socket;
		}
		public boolean isSendResponse() {
			return sendResponse;
		}
		public void setSendResponse(boolean sendResponse) {
			this.sendResponse = sendResponse;
		}
		public int getPackets() {
			return packets;
		}
		public void setPackets(int packets) {
			this.packets = packets;
		}
	}
	
	UdpStreamCallback callback;
	
	UdpStreamReceiverSettings settings;
	
	final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public UdpStreamReceiver(UdpStreamReceiverSettings settings, UdpStreamCallback callback) {
		this.callback = callback;
		this.settings = settings;
	}
	
	public void stop() {
		isRunning.set(false);
	}
	
	public void receive() throws InterruptedException, IOException {
	    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    final DataOutputStream dataOut = new DataOutputStream(byteOut);

		isRunning.set(true);
		int packetsReceived = 0;
		
		while(isRunning.get()) {
	    	if (Thread.interrupted()) {
	    		settings.socket.close();
	    		isRunning.set(false);
	            throw new InterruptedException();	
            }
			
		    byte data[] = new byte[1024];
		    DatagramPacket packet = new DatagramPacket(data, data.length);
		    
		    settings.socket.receive(packet);
		    packetsReceived++;
		    
		    if (callback != null) {
		    	callback.onReceive(packet);
		    }
		    
		    if (packetsReceived >= settings.packets) {
				isRunning.set(false);
			}
		    
			if (settings.sendResponse) 
			{
				byteOut.reset();

				if (callback != null && callback.onSend(dataOut, packetsReceived)) {
					final byte[] dataToSend = byteOut.toByteArray();
					DatagramPacket dp = new DatagramPacket(dataToSend, dataToSend.length, packet.getAddress(), packet.getPort());
					settings.socket.send(dp);
				}
			}
		}
	}
}
