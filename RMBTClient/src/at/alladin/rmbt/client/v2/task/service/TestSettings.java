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
package at.alladin.rmbt.client.v2.task.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.AbstractQoSTask;
import at.alladin.rmbt.client.v2.task.service.TestProgressListener.TestProgressEvent;
import at.alladin.rmbt.util.tools.TracerouteService;

public class TestSettings {
	private boolean useSsl;
	private long startTimeNs;
	private File cacheFolder;
	private TrafficService trafficService;
	private WebsiteTestService websiteTestService;
	private Class<? extends TracerouteService> tracerouteServiceClazz;
	private final List<TestProgressListener> testProgressListenerList = new ArrayList<TestProgressListener>();
	
	public TestSettings() { }
	
	public TestSettings(long startTimeNs) {
		this.startTimeNs = startTimeNs;
	}

	public File getCacheFolder() {
		return cacheFolder;
	}

	public void setCacheFolder(File cacheFolder) {
		this.cacheFolder = cacheFolder;
	}

	public TrafficService getTrafficService() {
		return trafficService;
	}

	public void setTrafficService(TrafficService trafficService) {
		this.trafficService = trafficService;
	}

	public WebsiteTestService getWebsiteTestService() {
		return websiteTestService;
	}

	public void setWebsiteTestService(WebsiteTestService websiteTestService) {
		this.websiteTestService = websiteTestService;
	}

	/**
	TracerouteServicehe {@link TracerouteService} implementation for traceroute functionalitTracerouteServiceeturn
	 */
	public Class<? extends TracerouteService> getTracerouteServiceClazz() {
		return tracerouteServiceClazz;
	}

	/**
	 * set the {@link TracerouteService} implementation for traceroute functionality
	 * @TracerouteServicengTool
	 */
	public void setTracerouteServiceClazz(Class<? extends TracerouteService> tracerouteServiceClazz) {
		this.tracerouteServiceClazz = tracerouteServiceClazz;
	}

	public List<TestProgressListener> getTestProgressListener() {
		return testProgressListenerList;
	}
	
	public void addTestProgressListener(TestProgressListener listener) {
		if (!testProgressListenerList.contains(listener)) {
			testProgressListenerList.add(listener);
		}
	}
	
	public void dispatchTestProgressEvent(TestProgressEvent event, AbstractQoSTask test) {
		dispatchTestProgressEvent(event, test, null);
	}
	
	public void dispatchTestProgressEvent(TestProgressEvent event, AbstractQoSTask test, QualityOfServiceTest qosTest) {
			switch (event) {
			case ON_START:
				for (TestProgressListener listener : testProgressListenerList) {
					if (listener != null) {
						listener.onQoSTestStart(test);
					}
				}
				break;
			case ON_END:
				for (TestProgressListener listener : testProgressListenerList) {
					if (listener != null) {
						listener.onQoSTestEnd(test);
					}
				}
				break;
			case ON_CREATED:
				for (TestProgressListener listener : testProgressListenerList) {
					if (listener != null) {
						listener.onQoSCreated(qosTest);
					}
				}
				break;
			}
	}

	/**
	 * the absolute starting point of the qos test.<br>
	 * needed for all relative time measurements
	 * @return
	 */
	public long getStartTimeNs() {
		return startTimeNs;
	}

	public void setStartTimeNs(long startTimeNs) {
		this.startTimeNs = startTimeNs;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isUseSsl() {
		return useSsl;
	}

	/**
	 * 
	 * @param useSsl
	 */
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}

	@Override
	public String toString() {
		return "TestSettings [useSsl=" + useSsl + ", startTimeNs="
				+ startTimeNs + ", cacheFolder=" + cacheFolder
				+ ", trafficService=" + trafficService
				+ ", websiteTestService=" + websiteTestService
				+ ", testProgressListenerList=" + testProgressListenerList
				+ "]";
	}
}
