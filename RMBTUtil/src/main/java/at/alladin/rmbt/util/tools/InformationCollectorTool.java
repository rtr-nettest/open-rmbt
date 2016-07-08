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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Tool for running {@link Collector}s, an interface that can be implemented by other classes to create information collectors
 * @author lb
 *
 * @param <T>
 */
public class InformationCollectorTool {
	
	/**
	 * 
	 */
	final static String TAG = InformationCollectorTool.class.getCanonicalName();
	
	/**
	 * 
	 */
	final static int PAUSE_BETWEEN_AUTO_EXECUTION_IN_MS = 500;
	
	/**
	 * Default timeout. Stops the information collector after this period of ms, if it has not been stopped manually
	 */
	final static int DEFAULT_TIMEOUT = 30000;
	
	/**
	 * 
	 */
	final List<CollectorHolder> collectorList = new ArrayList<>();
	
	final TimeUnit deltaTimeUnit;
	
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	private final AtomicLong timeout = new AtomicLong(DEFAULT_TIMEOUT);
	
	public final class CollectorHolder {
		final Collector<?,?> collector;
		private long lastUpdate = Long.MIN_VALUE;
		
		public CollectorHolder(final Collector<?,?> collector) {
			this.collector = collector;
		}
		public Collector<?,?> getCollector() {
			return collector;
		}
		/**
		 * returns the last update in nanoseconds
		 * @return
		 */
		public long getLastUpdateNs() {
			return lastUpdate;
		}
		/**
		 * returns the last update in the time unit defined by the {@link InformationCollectorTool}
		 * @return
		 */
		public long getLastUpdate() {
			return deltaTimeUnit.convert(lastUpdate, TimeUnit.NANOSECONDS);
		}
	}
	
	/**
	 * 
	 * @param timeUnit
	 * @param timeout
	 */
	public InformationCollectorTool(final TimeUnit timeUnit, final long timeout) {
		this.timeout.set(timeout);
		this.deltaTimeUnit = timeUnit;
	}
	
	/**
	 * 
	 * @param timeUnit
	 */
	public InformationCollectorTool(final TimeUnit timeUnit) {
		this(timeUnit, timeUnit.convert(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
	}

	/**
	 * 
	 * @return
	 */
	public List<CollectorHolder> getCollectorHolderList() {
		return collectorList;
	}
	
	/**
	 * 
	 * @param collector
	 */
	public void addCollector(final Collector<?,?> collector) {
		collectorList.add(new CollectorHolder(collector));
	}
	
	/**
	 * 
	 * @param collector
	 */
	public void removeCollector(final Collector<?,?> collector) {
		Iterator<CollectorHolder> iterator = collectorList.iterator();
		while (iterator.hasNext()) {
			CollectorHolder holder = iterator.next();
			if (holder.collector == collector) {
				iterator.remove();
			}
		}
	}
	
	public void start(final ExecutorService executor) {
		final long startTimeNs = System.nanoTime(); 
		if (!this.running.get()) {
			running.set(true);
			executor.submit(new Runnable() {
				
				public void run() {
					try {
						while (InformationCollectorTool.this.running.get()) {							
							update();
							Thread.sleep(PAUSE_BETWEEN_AUTO_EXECUTION_IN_MS);							

							final long executingTimeNs = System.nanoTime() - startTimeNs;
							if (executingTimeNs >= TimeUnit.NANOSECONDS.convert(timeout.get(), deltaTimeUnit)) {
								System.out.println("Timeout reached. Stopping InformationCollectorTool");
								InformationCollectorTool.this.running.set(false);
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					running.set(false);
				}
			});
		}
	}
	
	/**
	 * 
	 * @param timeout
	 */
	public void setTimeOut(final long timeout) {
		this.timeout.set(timeout);
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTimeOut() {
		return timeout.get();
	}
	
	/**
	 * 
	 */
	public void stop() {
		System.out.println("stopping InformationCollectorTool");
		this.running.set(false);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running.get();
	}
	
	/**
	 * update all collectors
	 */
	public void update() {
		final long now = System.nanoTime();
		
		try {
			for (CollectorHolder c : collectorList) {
				if ((c.lastUpdate + c.collector.getNanoPause()) <= now) {
					c.collector.update(deltaTimeUnit.convert(now - c.lastUpdate, TimeUnit.NANOSECONDS), deltaTimeUnit);
					c.lastUpdate = now;
				}
				else if (c.lastUpdate <= 0) {
					c.collector.update(0, deltaTimeUnit);
					c.lastUpdate = now;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param jsonObjectName
	 * @return
	 */
	public JSONObject getJsonObject(final boolean clean) {
		return getJsonObject(clean, 0);
	}

	/**
	 * 
	 * @param clean
	 * @param relativeTimeStamp
	 * @return
	 */
	public JSONObject getJsonObject(final boolean clean, long relativeTimeStamp) {
		try {
			JSONObject json = new JSONObject();
			for (CollectorHolder c : collectorList) {
				synchronized (c.collector) {
					json.put(c.collector.getJsonKey(), c.collector.getJsonResult(clean, relativeTimeStamp, deltaTimeUnit));
				}
			}
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}	
}
