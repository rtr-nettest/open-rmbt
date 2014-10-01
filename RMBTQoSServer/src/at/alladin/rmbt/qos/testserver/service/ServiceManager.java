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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.service.EventJob.EventType;

/**
 * 
 * @author lb
 *
 */
public class ServiceManager {

	/**
	 * 
	 * @author lb
	 *
	 */
	public static class FutureService {
		protected final Future<?> future;
		protected final AbstractJob<?> service;
		
		public FutureService(Future<?> future, AbstractJob<?> service) {
			this.future = future;
			this.service = service;
		}

		public Future<?> getFuture() {
			return future;
		}

		public AbstractJob<?> getService() {
			return service;
		}
	}
	
	/**
	 * 
	 */
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * 
	 */
	private final ConcurrentMap<TestServerServiceEnum, FutureService> serviceMap = new ConcurrentHashMap<>();
	
	/**
	 * 
	 * @param service
	 */
	public void addService(AbstractJob<?> service) {
		if (service instanceof IntervalJob<?>) {
			Future<?> future = executor.submit(service);
			serviceMap.putIfAbsent(service.getService(), new FutureService(future, service));
		}
		else if (service instanceof EventJob<?>) {
			serviceMap.putIfAbsent(service.getService(), new FutureService(null, service));			
		}
	}
	
	/**
	 * attempts to stop all running services and returns all results with their service names
	 */
	public Map<String, Object> shutdownAll(boolean mayInterruptIfRunning) {
		Map<String, Object> resultMap = new HashMap<>();
		for (Entry<TestServerServiceEnum, FutureService> e : serviceMap.entrySet()) {
			try {
				if (e.getValue().getFuture() != null) {
					if (!e.getValue().getFuture().isDone()) {
						e.getValue().getService().stop();
						if (mayInterruptIfRunning) {
							e.getValue().getService().interrupt();
						}
					}
				}
				else {
					e.getValue().getService().stop();
				}
				Object result;
				result = e.getValue().getService().getResult();
				resultMap.put(e.getKey().getName(), result);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return resultMap;
	}
	
	/**
	 * 
	 */
	public void shutdownAllNow() {
		executor.shutdownNow();
	}
	
	/**
	 * 
	 * @param type
	 */
	public void dispatchEvent(EventType type) {
		for (FutureService serviceEntry : serviceMap.values()) {
			if (serviceEntry.getFuture() == null && serviceEntry.getService() instanceof EventJob<?>) {
				if (((EventJob<?>)serviceEntry.getService()).shouldLaunch(type)) {
					executor.execute(serviceEntry.getService());
				}
			}
		}
	}
	
	/**
	 * attempts to stop a service and return its result
	 * @param serviceName
	 * @return
	 */
	public Object shutdownService(String serviceName) {
		FutureService service = serviceMap.remove(serviceName);
		if (service != null) {
			try {
				service.getService().stop();
				return service.future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public ConcurrentMap<TestServerServiceEnum, FutureService> getServiceMap() {
		return serviceMap;
	}
}
