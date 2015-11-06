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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface StreamSender<T> {

	public T send() throws InterruptedException, IOException, TimeoutException;
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static interface UdpStreamCallback {
		
		/**
		 * is called before sending data
		 * @param dataOut output stream for the packet's payload
		 * @param packetNumber the current packet number
		 * @throws IOException
		 */
		boolean onSend(final DataOutputStream dataOut, final int packetNumber) throws IOException;
		
		/**
		 * is called after a datagram packet has been received
		 * @param dp the received datagram packet
		 * @throws IOException
		 */
		void onReceive(final DatagramPacket dp) throws IOException;
		
		/**
		 * is called when the socket/channel is bound to a specific port
		 * @param port
		 * @throws IOException
		 */
		void onBind(final Integer port) throws IOException;
	}
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static class UdpStreamSenderSettings<T> {
		int packets;
		long delay;
		TimeUnit timeUnit;
		InetAddress targetHost;
		int targetPort;
		Integer incomingPort = null;
		long responseSoTimeout = 0;
		long timeout = 0;
		T socket;
		boolean isNonblocking = false;
		boolean writeOnly = false;
		boolean closeOnFinish = false;

		public UdpStreamSenderSettings(final T socket, final boolean closeOnFinish, final InetAddress targetHost, 
				final int targetPort, final int packets, final long delay, final long timeout, final TimeUnit timeUnit,
				boolean writeOnly, int responseSoTimeout) {
			this.socket = socket;
			this.targetHost = targetHost;
			this.targetPort = targetPort;
			this.packets = packets;
			this.delay = delay;
			this.timeout = timeout;
			this.timeUnit = timeUnit;
			this.writeOnly = writeOnly;
			this.responseSoTimeout = responseSoTimeout;
			this.closeOnFinish = closeOnFinish;
		}
		
		public Integer getIncomingPort() {
			return incomingPort;
		}

		public void setIncomingPort(Integer incomingPort) {
			this.incomingPort = incomingPort;
		}

		public UdpStreamSenderSettings(final T socket, final boolean closeOnFinish, final InetAddress targetHost, 
				final int targetPort, final int packets, final long delay, final TimeUnit timeUnit) {
			this(socket, closeOnFinish, targetHost, targetPort, packets, delay, 0, timeUnit, false, 0);
		}

		public T getSocket() {
			return socket;
		}
		public void setSocket(T socket) {
			this.socket = socket;
		}
		public long getResponseSoTimeout() {
			return responseSoTimeout;
		}
		public void setResponseSoTimeout(long responseSoTimeout) {
			this.responseSoTimeout = responseSoTimeout;
		}
		public int getPackets() {
			return packets;
		}
		public void setPackets(int packets) {
			this.packets = packets;
		}
		public long getDelay() {
			return delay;
		}
		public void setDelay(long delay) {
			this.delay = delay;
		}
		public TimeUnit getTimeUnit() {
			return timeUnit;
		}
		public void setTimeUnit(TimeUnit timeUnit) {
			this.timeUnit = timeUnit;
		}
		public InetAddress getTargetHost() {
			return targetHost;
		}
		public void setTargetHost(InetAddress targetHost) {
			this.targetHost = targetHost;
		}
		public int getTargetPort() {
			return targetPort;
		}
		public void setTargetPort(int targetPort) {
			this.targetPort = targetPort;
		}
		public long getTimeout() {
			return timeout;
		}
		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}
		public boolean isNonblocking() {
			return isNonblocking;
		}
		public void setNonblocking(boolean isNonblocking) {
			this.isNonblocking = isNonblocking;
		}
		public boolean isWriteOnly() {
			return writeOnly;
		}
		public void setWriteOnly(boolean writeOnly) {
			this.writeOnly = writeOnly;
		}

		@Override
		public String toString() {
			return "UdpStreamSenderSettings [packets=" + packets + ", delay="
					+ delay + ", timeUnit=" + timeUnit + ", targetHost="
					+ targetHost + ", targetPort=" + targetPort
					+ ", responseSoTimeout=" + responseSoTimeout + ", timeout="
					+ timeout + ", socket=" + socket + ", isNonblocking="
					+ isNonblocking + ", writeOnly=" + writeOnly + "]";
		}
	}
}
