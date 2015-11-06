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
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * udp stream sender used by the udp and voip qos test
 * @author lb
 *
 */
public class UdpStreamSender implements StreamSender<DatagramSocket> {
	
	UdpStreamSenderSettings<DatagramSocket> settings;
	
	UdpStreamCallback callback;
	
	final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public UdpStreamSender(UdpStreamSenderSettings<DatagramSocket> settings, UdpStreamCallback callback) {
		this.settings = settings;
		this.callback = callback;
	}
	
	public void stop() {
		isRunning.set(false);
	}

	/**
	 * send a stream of udp packets
	 * @return the {@link DatagramSocket} used for this stream or null if an exception occurred
	 * @throws InterruptedException
	 * @throws TimeoutException 
	 */
	public DatagramSocket send() throws InterruptedException, TimeoutException {
		System.out.println("UDP Stream: " + settings);
		
	    isRunning.set(true);
	    
		int packetsSent = 0;
	    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    final DataOutputStream dataOut = new DataOutputStream(byteOut);

	    byte[] data;

	    final long delayMs = TimeUnit.MILLISECONDS.convert(settings.delay, settings.timeUnit);
	    long lastSendTimestamp = 0;
	    
	    final long startTimeMs = System.currentTimeMillis();
	    final long timeoutMs = TimeUnit.MILLISECONDS.convert(settings.timeout, settings.timeUnit);
	    final long stopTimeMs = timeoutMs > 0 ? timeoutMs + startTimeMs : 0;

	    while(isRunning.get()) {    	
	    	if (Thread.interrupted()) {
	    		isRunning.set(false);
	            throw new InterruptedException();	
            }
	    	
	    	if (stopTimeMs > 0 && stopTimeMs < System.currentTimeMillis()) {
	    		isRunning.set(false);
	            throw new TimeoutException();	    		
	    	}

	    	//calculate correct packet delay
	    	long currentDelay = System.currentTimeMillis() - lastSendTimestamp;
	    	currentDelay = currentDelay > delayMs ? 0 : delayMs - currentDelay;
	    	if (currentDelay > 0) {
	    		Thread.sleep(currentDelay);
	    	}
	    	
	    	byteOut.reset();
	    	
	    	try {

	    		if (callback != null && callback.onSend(dataOut, packetsSent)) {
    		    	data = byteOut.toByteArray();
    		    	
    		    	DatagramPacket packet = null;
    		    	if (!settings.socket.isConnected()) {
    				    packet = new DatagramPacket(data, data.length, settings.targetHost, settings.targetPort);		    		
    		    	}
    		    	else {
    		    		packet = new DatagramPacket(data, data.length);
    		    	}
    		    	
    	    		settings.socket.send(packet);
    		    	packetsSent++;
    		    	lastSendTimestamp = System.currentTimeMillis();
	    		}

	    		
	    		if (!settings.writeOnly) {
	    			try {
	    			    byte buffer[] = new byte[1024];

	    			    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
	    			    settings.socket.setSoTimeout((int) TimeUnit.MILLISECONDS.convert(settings.responseSoTimeout, settings.timeUnit));
	    			    settings.socket.receive(dp);
	    			    if (callback != null) {
	    			    	callback.onReceive(dp);
	    			    }
	    			}
	    			catch (SocketTimeoutException e) {
	    				e.printStackTrace();
	    			}
	    		}
	    		
	    		if (packetsSent >= settings.packets) {
	    			isRunning.set(false);
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		if (settings.closeOnFinish) {
	    			settings.socket.close();
	    		}
	    		return null;
	    	}
	    }

	    return settings.socket;
	}
}
