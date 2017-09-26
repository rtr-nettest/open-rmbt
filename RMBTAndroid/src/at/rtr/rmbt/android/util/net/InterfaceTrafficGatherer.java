/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * Copyright 2017 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.android.util.net;

import android.net.TrafficStats;
import at.alladin.rmbt.android.R;


/**
 * 
 * @author lb
 *
 */
public class InterfaceTrafficGatherer {

	/**
	 * 
	 * @author lb
	 *
	 */
	public static enum TrafficClassificationEnum {
		UNKNOWN(Long.MIN_VALUE, Long.MIN_VALUE, R.drawable.traffic_speed_none),
		NONE(0,      12499, R.drawable.traffic_speed_none),  //      0  <  x <  100 kBit/s
		LOW (1250,   124999, R.drawable.traffic_speed_low),  //   100k  <= x <  1 MBit/s
		MID (125000, 1249999, R.drawable.traffic_speed_mid),  //    1M  <= x <  10 MBit/s
		HIGH(1250000, Long.MAX_VALUE, R.drawable.traffic_speed_high); //   x >= 10 MBit/s

		protected long minBytes;
		protected long maxBytes;
		protected int resId;
		
		TrafficClassificationEnum(long minBytes, long maxBytes, int resId) {
			this.minBytes = minBytes;
			this.maxBytes = maxBytes;
			this.resId = resId;
		}
		
		public long getMinBytes() {
			return this.minBytes;
		}
		
		public long getMaxBytes() {
			return this.maxBytes;
		}
		
		public int getResId() {
			return this.resId;
		}
		
		/**
		 * 
		 * @param bytesPerSecond
		 * @return
		 */
		public static TrafficClassificationEnum classify(long bytesPerSecond) {
			for (TrafficClassificationEnum e : TrafficClassificationEnum.values()) {
				if (e.getMinBytes() <= bytesPerSecond && e.getMaxBytes() >= bytesPerSecond) {
					return e; 
				}
			}
			
			return UNKNOWN;
		}
	}
	
	private long prevTxBytes;
	private long prevRxBytes;
	private long txBytes;
	private long rxBytes;
	private long rxTraffic;
	private long txTraffic;
	private long nsElapsed;
	private long nsTimestamp;
	
	public InterfaceTrafficGatherer() {
		prevTxBytes = TrafficStats.getTotalTxBytes();
		prevRxBytes = TrafficStats.getTotalRxBytes();
		txBytes = prevTxBytes;
		rxBytes = prevRxBytes;
		nsTimestamp = System.nanoTime();
	}
	
	public void run() {
		this.prevRxBytes = this.rxBytes;
		this.prevTxBytes = this.txBytes;
		this.rxBytes = TrafficStats.getTotalRxBytes();
		this.txBytes = TrafficStats.getTotalTxBytes();
		final long timestamp = System.nanoTime();
		this.nsElapsed = timestamp - this.nsTimestamp;
		this.nsTimestamp = timestamp;
		//double perSecMultiplier = 1000d / msInterval;
		this.txTraffic = (nsElapsed > 0 ? (long)((double)(txBytes - prevTxBytes) / (double)(nsElapsed / 1000000000D)) : 0);
		this.rxTraffic = (nsElapsed > 0 ? (long)((double)(rxBytes - prevRxBytes) / (double)(nsElapsed / 1000000000D)) : 0);
	}

	public long getTxRate() {
		return txTraffic;  
	}

	public long getRxRate() {
		return rxTraffic;
	}
}
