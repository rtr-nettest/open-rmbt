/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
package at.alladin.rmbt.android.test;

import android.net.TrafficStats;
import android.os.Process;
import at.alladin.rmbt.client.v2.task.service.TrafficService;

/**
 * 
 * @author lb
 *
 */
public class TrafficServiceByUidImpl implements TrafficService {

	private long trafficRxStart = -1;
	
	private long trafficTxStart = -1;
	
	private long trafficRxEnd = -1;
	
	private long trafficTxEnd = -1;

	private int uid;
	
	private boolean running = false;
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.service.TrafficService#start()
	 */
	@Override
	public int start() {
		uid = Process.myUid();
		if ((trafficRxStart = TrafficStats.getUidRxBytes(uid)) == TrafficStats.UNSUPPORTED) {
			return SERVICE_NOT_SUPPORTED;
		}		
		
		running = true;
		trafficTxStart = TrafficStats.getUidTxBytes(uid);
		return SERVICE_START_OK;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.service.TrafficService#getTxBytes()
	 */
	@Override
	public long getTxBytes() {
		return (trafficTxStart != -1 ? trafficTxEnd - trafficTxStart : -1);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.service.TrafficService#getRxBytes()
	 */
	@Override
	public long getRxBytes() {
		return (trafficRxStart != -1 ? trafficRxEnd - trafficRxStart : -1);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.service.TrafficService#stop()
	 */
	@Override
	public void stop() {
		if (running) {
			trafficTxEnd = TrafficStats.getUidTxBytes(uid);
			trafficRxEnd = TrafficStats.getUidRxBytes(uid);
		}
	}

	@Override
	public long getTotalTxBytes() {
		return TrafficStats.getUidTxBytes(uid);
	}

	@Override
	public long getTotalRxBytes() {
		return TrafficStats.getUidRxBytes(uid);
	}

	@Override
	public long getCurrentTxBytes() {
		return (trafficTxStart != -1 ? getTotalTxBytes() - trafficTxStart : -1);
	}

	@Override
	public long getCurrentRxBytes() {
		return (trafficRxStart != -1 ? getTotalRxBytes() - trafficRxStart : -1);
	}

	@Override
	public void update() {
		if (running) {
			stop();
			running = true;
		}
	}
}
