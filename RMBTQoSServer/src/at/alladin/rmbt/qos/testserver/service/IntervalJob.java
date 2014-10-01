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
package at.alladin.rmbt.qos.testserver.service;

import java.util.concurrent.atomic.AtomicInteger;

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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {		
		log("STARTING SERVICE '" + service.getName() + "'", 0);
		
		try {
			while (isRunning.get()) {
				Thread.sleep(interval.get());
				result = execute();
				log(result.toString(), 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			isRunning.set(false);
		}
		
		log("STOPPED SERVICE '" + service.getName() + "'", 0);
	}
}
