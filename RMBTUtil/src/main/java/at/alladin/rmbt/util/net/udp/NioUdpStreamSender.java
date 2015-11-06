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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * udp stream sender used by the udp and voip qos test
 * @author lb
 *
 */
public class NioUdpStreamSender implements StreamSender<DatagramChannel> {

	UdpStreamSenderSettings<DatagramChannel> settings;
	
	UdpStreamCallback callback;
	
	final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public NioUdpStreamSender(UdpStreamSenderSettings<DatagramChannel> settings, UdpStreamCallback udpStreamCallback) {
		this.settings = settings;
		this.callback = udpStreamCallback;		
	}
		
	public void stop() {
		isRunning.set(false);
	}


	/**
	 * send a stream of udp packets
	 * @return the {@link DatagramChannel} used for this stream or null if an exception occurred
	 * @throws InterruptedException
	 * @throws IOException 
	 * @throws TimeoutException 
	 */
	public DatagramChannel send() throws InterruptedException, TimeoutException {		
	    isRunning.set(true);
	    
		int packetsSent = 0;
		int packetsRcv = 0;
	    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    final DataOutputStream dataOut = new DataOutputStream(byteOut);
	    final ByteBuffer buffer = ByteBuffer.allocate(1024);
	    final SocketAddress targetAddress = new InetSocketAddress(settings.targetHost, settings.getTargetPort()); 

	    final long delayMs = TimeUnit.MILLISECONDS.convert(settings.delay, settings.timeUnit);
	    long lastSendTimestamp = 0;
	    
	    final long startTimeMs = System.currentTimeMillis();
	    final long timeoutMs = TimeUnit.MILLISECONDS.convert(settings.timeout, settings.timeUnit);
	    final long stopTimeMs = timeoutMs > 0 ? timeoutMs + startTimeMs : 0;

	    DatagramChannel channel = null;
	    Selector selector = null;
	    
	    try {
	    	if (settings.socket == null) {
	    		channel = DatagramChannel.open();
	    		channel.configureBlocking(false);
	    		if (settings.incomingPort != null) {
	    			channel.socket().bind(new InetSocketAddress(settings.incomingPort));
	    			if (callback != null) {
	    				callback.onBind(channel.socket().getLocalPort());
	    			}
	    		}
	    		else {
		    		channel.socket().bind(null);
	    		}
	    		
    			if (callback != null) {
    				callback.onBind(channel.socket().getLocalPort());
    			}
	    	}
	    	else {
	    		channel = settings.socket;
	    	}
	    	
		    selector = Selector.open();
		    
		    if (settings.writeOnly) {
		    	channel.register(selector, SelectionKey.OP_WRITE);
		    }
		    else {
		    	channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		    }
	
		    while(isRunning.get()) {
		    	if (Thread.interrupted()) {
		    		isRunning.set(false);
		            throw new InterruptedException();	
	            }
		    	
		    	if (stopTimeMs > 0 && stopTimeMs < System.currentTimeMillis()) {
		    		isRunning.set(false);
		            throw new TimeoutException("Exceeded timeout of " + timeoutMs + "ms");
		    	}
	
		    	//calculate correct packet delay
		    	long currentDelay = System.currentTimeMillis() - lastSendTimestamp;
		    	currentDelay = currentDelay > delayMs ? 0 : delayMs - currentDelay;
		    	if (currentDelay > 0) {
		    		Thread.sleep(currentDelay);
		    	}
		    	
		    	selector.select(1000);
		    	Set<SelectionKey> readyKeys = selector.selectedKeys();
		    	if (!readyKeys.isEmpty()) {
			    	Iterator<SelectionKey> iterator = readyKeys.iterator();
			    	while (iterator.hasNext()) {
			    		SelectionKey key = (SelectionKey) iterator.next();
						iterator.remove();
						if (key.isReadable() && (packetsRcv < settings.packets) && key.isValid()) {
							buffer.clear();
							channel.receive(buffer);
							buffer.flip();
							final DatagramPacket dp = new DatagramPacket(buffer.array(), buffer.array().length);
							if (callback != null) {
								callback.onReceive(dp);
							}
							packetsRcv++;
						}
						if (key.isWritable() && (packetsSent < settings.packets) && key.isValid()) {
							byteOut.reset();
							buffer.clear();
					    	try {
					    		if (callback != null) {
					    			if (callback.onSend(dataOut, packetsSent)) {
								    	final byte[] data = byteOut.toByteArray();
								    	buffer.put(data);
								    	buffer.flip();
								    	channel.send(buffer, targetAddress);
										packetsSent++;
								    	lastSendTimestamp = System.currentTimeMillis();
					    			}
					    		}
					    	} catch (IOException e) {
					    		e.printStackTrace();
					    		return null;
					    	}
						}
			    	}
		    	}
		    	
		    	if (!settings.writeOnly) {
		    		if ((packetsSent >= settings.packets) && (packetsRcv >= settings.packets)) {
		    			isRunning.set(false);
		    		}		    		
		    	}
		    	else {
		    		if ((packetsSent >= settings.packets)) {
		    			isRunning.set(false);
		    		}
		    	}
		    }
		    
		    return channel;
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
	    finally {
	    	if (selector != null && selector.isOpen()) {
	    		try {
					selector.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	
	    	if (channel != null && channel.socket() != null && !channel.socket().isClosed() && settings.closeOnFinish) {
	    		try {
					channel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    }
	    
	    return null;

	}
}
