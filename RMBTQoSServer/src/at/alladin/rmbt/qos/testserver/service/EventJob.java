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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;

/**
 * 
 * @author lb
 *
 */
public abstract class EventJob<R> extends AbstractJob<R> {

	/**
	 * 
	 * @author lb
	 *
	 */
	public static enum EventType {
		NONE("Unknown Event"),
		ON_TEST_SERVER_START("Server Start"),
		ON_TEST_SERVER_STOP("Server Stop");
		
		protected String info;
		
		private EventType(String info) {
			this.info = info;
		}

		public String getInfo() {
			return info;
		}

		public void setInfo(String info) {
			this.info = info;
		}
	}
	
	/**
	 * 
	 */
	protected Set<EventType> launchEventSet = new HashSet<>();
	
	/**
	 * 
	 */
	protected AtomicReference<EventType> lastEventType = new AtomicReference<EventJob.EventType>(EventType.NONE);
	
	/**
	 * 
	 * @param service
	 */
	public EventJob(TestServerServiceEnum service) {
		super(service);
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<EventType> getLaunchEventSet() {
		return launchEventSet;
	}

	/**
	 * 
	 * @param launchEventSet
	 */
	public void setLaunchEventSet(Set<EventType> launchEventSet) {
		this.launchEventSet = launchEventSet;
	}
	
	/**
	 * 
	 * @param type
	 */
	public void setLastEventType(EventType type) {
		lastEventType.set(type);
	}
	
	/**
	 * 
	 * @return
	 */
	public EventType getLastEventType() {
		return lastEventType.get();
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public boolean shouldLaunch(EventType type) {
		setLastEventType(type);
		return launchEventSet.contains(type);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {		
		log("Called service '" + service.getName() + "' on event: '" + lastEventType.get().getInfo() + "'", 0);
		try {
			if (isRunning.get() && launchEventSet.contains(lastEventType.get())) {
				result = execute();
				if (result != null) {
					log(result.toString(), 1);
				}
			}
			dispatchEvent(JobState.RUN, result);
		}
		catch (Exception e) {
			error(e, 0);
			dispatchEvent(JobState.ERROR, result);
		}
		
		log("Service call to '" + service.getName() + "' completed", 0);
	}
}
