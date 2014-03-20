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
package at.alladin.rmbt.client.v2.task.service;

/**
 * 
 * @author lb
 *
 */
public class TestMeasurement {
	public enum TrafficDirection {
		TX,
		RX,
		TOTAL
	}
	
	private long rxBytes;
	private long txBytes;
	private boolean isRunning = false;
	private TrafficService service;
	private long timeStampStart;
	private long timeStampStop;
	
	//debug purpose:
	private String id;
	
	/**
	 * 
	 * @param id is only for debug purpose
	 * @param service the {@link TrafficService}, may be <code>null</code>
	 */
	public TestMeasurement(String id, TrafficService service) {
		this.service = service;
		this.id = id;
	}
	
	/**
	 * 
	 * @param rxBytes
	 * @param txBytes
	 */
	public TestMeasurement(long rxBytes, long txBytes, long timeStampStart, long timeStampStop) {
		this.rxBytes = rxBytes;
		this.txBytes = txBytes;
		this.timeStampStart = timeStampStart;
		this.timeStampStop = timeStampStop;
	}
	
	/**
	 * 
	 * @param threadId a thread id (only for debug purpose)
	 */
	public synchronized void start(int threadId) {
		if (!isRunning) {
			isRunning = true;
			this.timeStampStart = System.nanoTime();
			if (service != null) {
	    		service.start();
	    		System.out.println("TRAFFICSERVICE '" + id + "' STARTED BY THREAD " + threadId);
			}	
		}
	}
	
	/**
	 * 
	 * @param threadId a thread id (only for debug purpose)
	 */
	public synchronized void stop(int threadId) {
		if (isRunning) {
			isRunning = false;
			this.timeStampStop = System.nanoTime();
			if (service != null) {
				service.stop();
				System.out.println("TRAFFICSERVICE '" + id + "' STOPPED BY THREAD " + threadId + " RX/TX: " + service.getRxBytes() + "/" + service.getTxBytes());
	    		setRxBytes(service.getRxBytes());
	    		setTxBytes(service.getTxBytes());    							
			}
		}
	}
	
	public long getRxBytes() {
		return rxBytes;
	}
	public void setRxBytes(long rxBytes) {
		this.rxBytes = rxBytes;
	}
	public long getTxBytes() {
		return txBytes;
	}
	public void setTxBytes(long txBytes) {
		this.txBytes = txBytes;
	}
	public long getTimeStampStart() {
		return timeStampStart;
	}
	public void setTimeStampStart(long timeStampStart) {
		this.timeStampStart = timeStampStart;
	}
	public long getTimeStampStop() {
		return timeStampStop;
	}
	public void setTimeStampStop(long timeStampStop) {
		this.timeStampStop = timeStampStop;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TestMeasurement [rxBytes=" + rxBytes + ", txBytes=" + txBytes
				+ ", isRunning=" + isRunning + ", service=" + service
				+ ", timeStampStart=" + timeStampStart + ", timeStampStop="
				+ timeStampStop + ", id=" + id + "]";
	}
}
