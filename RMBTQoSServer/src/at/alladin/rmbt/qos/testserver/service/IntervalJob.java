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
package at.alladin.rmbt.qos.testserver.service;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;

public abstract class IntervalJob<R> extends AbstractJob<R> {

	/**
	 * 
	 */
	public final static int DEFAULT_JOB_INTERVAL = 60000;
	
	/**
	 * 
	 */
	private final AtomicInteger interval = new AtomicInteger(0);
	
	/**
	 * 
	 */
	private final AtomicLong executionCounter = new AtomicLong(0);
	
	/**
	 * 
	 */
	private final AtomicLong executionDuration = new AtomicLong(0);

	/**
	 * tell the ServiceManager either to restart this service after an error occurred or to let it die
	 * @return
	 */
	public abstract boolean restartOnError();
	
	/**
	 * 
	 * @param service
	 */
	public IntervalJob(TestServerServiceEnum service) {
		super(service);
		setJobInterval(DEFAULT_JOB_INTERVAL);
	}

	
	/**
	 * 
	 * @return
	 */
	public int getJobInterval() {
		return interval.get();
	}

	/**
	 * 
	 * @param intervalMs
	 */
	public void setJobInterval(int intervalMs) {
		interval.set(intervalMs);
	}
	
	/**
	 * 
	 * @return
	 */
	public long getExecutionCounter() {
		return executionCounter.get();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			log("STARTING SERVICE '" + service.getName() + "'", 0);
			
			final DecimalFormat df = new DecimalFormat("##0.000");
			
			while (isRunning.get()) {
				Thread.sleep(interval.get());
				
				final long tsStart = System.nanoTime();
				result = execute();
				final long timePassed = (System.nanoTime() - tsStart);
				
				executionCounter.addAndGet(1);
				executionDuration.addAndGet(timePassed);
				
				log(result.toString(), 0);
				log("times executed: " + executionCounter.get() + ",  this time it took: " + df.format(((double)(System.nanoTime() - tsStart) / 1000000d)) + "ms"
						+ ", total time since start: " + df.format(((double)executionDuration.get()/1000000000d)) + "s", 1);

				dispatchEvent(JobState.RUN, result);
			}
		}
		catch (Exception e) {
			error(e, 0);
			state.set(JobState.ERROR);
		}
		finally {
			isRunning.set(false);
		}

		dispatchEvent(getState(), result);
		
		log("STOPPED SERVICE '" + service.getName() + "'", 0);
	}
}
