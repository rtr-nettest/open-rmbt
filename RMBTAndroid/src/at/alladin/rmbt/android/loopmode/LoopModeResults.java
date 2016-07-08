/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.android.loopmode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.os.SystemClock;
import android.util.SparseArray;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.v2.task.service.TrafficService;

/**
 * loop mode results (full results including medians and last test status + last test results)
 * @author lb
 *
 */
public class LoopModeResults {

	public static enum Status {
		RUNNING,
		IDLE
	}
	
	public final static class TrafficStats {
		long tx = 0;
		long rx = 0;
		
		public TrafficStats(final TrafficService trafficService) {
			if (trafficService != null) {
				this.tx = trafficService.getTxBytes();
				this.rx = trafficService.getRxBytes();
			}
		}
		
		public TrafficStats(final long tx, final long rx) {
			this.tx = tx;
			this.rx = rx;
		}
		
		public void add(final TrafficStats trafficStats) {
			if (trafficStats != null) {
				this.tx += trafficStats.tx;
				this.rx += trafficStats.rx;
			}
		}
		
		public void addCurrent(final TrafficService trafficService) {
			if (trafficService != null) {
				trafficService.update();
				this.tx+=trafficService.getCurrentTxBytes();
				this.rx+=trafficService.getCurrentRxBytes();
			}
		}

		public void addTotal(final TrafficService trafficService) {
			if (trafficService != null) {
				this.tx+=trafficService.getTxBytes();
				this.rx+=trafficService.getRxBytes();
			}
		}
		
		public void setTx(long tx) {
			this.tx = tx;
		}

		public void setRx(long rx) {
			this.rx = rx;
		}

		/**
		 * returns amount of transferred bytes 
		 * @return
		 */
		public long getTx() {
			return tx;
		}
		
		/**
		 * returns amount of received bytes
		 * @return
		 */
		public long getRx() {
			return rx;
		}
	}
	
	final List<Long> pingList = new ArrayList<Long>();
	
	final List<Long> downList = new ArrayList<Long>();
	
	final List<Long> upList = new ArrayList<Long>();
	
	long pingMedian = 0;
	
	long downMedian = 0;
	
	long upMedian = 0;
	
	final SparseArray<TrafficStats> trafficStatsArray = new SparseArray<LoopModeResults.TrafficStats>();
	
	final TrafficStats totalTrafficStats = new TrafficStats(0, 0);
	
	TrafficStats currentTrafficStats = new TrafficStats(0, 0);

	final AtomicReference<LoopModeCurrentTest> currentTest = new AtomicReference<LoopModeCurrentTest>(new LoopModeCurrentTest());
	
	private AtomicReference<LoopModeLastTestResults> lastTestResults = new AtomicReference<LoopModeLastTestResults>();
	
	private Status status = Status.IDLE;
	
    private long minDelay;
    
    private long maxDelay;
    
    private float maxMovement;
    
    private int maxTests;
    
    private int numberOfTests;
    
    private long lastTestTime; // SystemClock.elapsedRealtime()

    private float lastDistance;
    
    private float lastAccuracy;

	
	/**
	 * current traffic stats (= current test)
	 * @return
	 */
	public TrafficStats getCurrentTrafficStats() {
		return currentTrafficStats;
	}

	public void setCurrentTrafficStats(TrafficStats trafficStats) {
		this.currentTrafficStats = trafficStats;
	}

	public SparseArray<TrafficStats> getTrafficStatsArray() {
		return trafficStatsArray;
	}
	
	/**
	 * total traffic stats
	 * @return
	 */
	public TrafficStats getTotalTrafficStats() {
		return totalTrafficStats;
	}

	/**
	 * updates total traffic statistics
	 * @param ts
	 */
	public void updateTrafficStats(final TrafficStats ts) {
		trafficStatsArray.put(trafficStatsArray.size(), ts);
		totalTrafficStats.add(ts);
	}

	/**
	 * updates all medians by adding new values
	 * @param ping
	 * @param up
	 * @param down
	 */
	public void updateMedians(final long ping, final long up, final long down) {
		pingList.add(ping);
		upList.add(up);
		downList.add(down);
		
		pingMedian = calculateMedian(pingList);
		upMedian = calculateMedian(upList);
		downMedian = calculateMedian(downList);
	}
	
	public LoopModeLastTestResults getLastTestResults() {
		return lastTestResults.get();
	}

	public void setLastTestResults(LoopModeLastTestResults lastTestResults) {
		this.lastTestResults.set(lastTestResults);
	}

	private long calculateMedian(List<Long> list) {
		Collections.sort(list);
		int center = list.size() / 2;
		return list.size() % 2 == 1 ? list.get(center) : (list.get(center-1)+list.get(center))/2 ;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getPingMedian() {
		return pingMedian;
	}

	public long getDownMedian() {
		return downMedian;
	}

	public long getUpMedian() {
		return upMedian;
	}

	public LoopModeCurrentTest getCurrentTest() {
		return currentTest.get();
	}
	
	public void setCurrentTest(final LoopModeCurrentTest currentTest) {
		this.currentTest.set(currentTest);
	}
	
	public IntermediateResult getIntermediateResult() {
		final LoopModeCurrentTest test = currentTest.get();
		return test != null ? test.getResult() : null;
	}

	public long getMinDelay() {
		return minDelay;
	}

	public void setMinDelay(long minDelay) {
		this.minDelay = minDelay;
	}

	public long getMaxDelay() {
		return maxDelay;
	}

	public void setMaxDelay(long maxDelay) {
		this.maxDelay = maxDelay;
	}

	public float getMaxMovement() {
		return maxMovement;
	}

	public void setMaxMovement(float maxMovement) {
		this.maxMovement = maxMovement;
	}

	public int getMaxTests() {
		return maxTests;
	}

	public void setMaxTests(int maxTests) {
		this.maxTests = maxTests;
	}

	public int getNumberOfTests() {
		return numberOfTests;
	}

	public void setNumberOfTests(int numberOfTests) {
		this.numberOfTests = numberOfTests;
	}

	/**
	 * returns the last test time (method used: {@link SystemClock#elapsedRealtime()})
	 * @return
	 */
	public long getLastTestTime() {
		return lastTestTime;
	}

	public void setLastTestTime(long lastTestTime) {
		this.lastTestTime = lastTestTime;
	}

	public float getLastDistance() {
		return lastDistance;
	}

	public void setLastDistance(float lastDistance) {
		this.lastDistance = lastDistance;
	}
	
	public void setLastAccuracy(float accuracy) {
		this.lastAccuracy = accuracy;
	}
	
	public float getLastAccuracy() {
		return lastAccuracy;
	}

	/**
	 * a list of median pings (1 item per test)
	 * @return
	 */
	public List<Long> getPingList() {
		return pingList;
	}

	/**
	 * a list of download speeds (1 item per test)
	 * @return
	 */
	public List<Long> getDownList() {
		return downList;
	}

	/**
	 * a list of upload speeds (1 item per test)
	 * @return
	 */
	public List<Long> getUpList() {
		return upList;
	}
}
