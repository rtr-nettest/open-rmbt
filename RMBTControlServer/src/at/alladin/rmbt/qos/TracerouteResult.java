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
package at.alladin.rmbt.qos;

import java.util.ArrayList;

import at.alladin.rmbt.shared.hstoreparser.annotation.HstoreCollection;
import at.alladin.rmbt.shared.hstoreparser.annotation.HstoreKey;

/**
 * 
 * 
 * @author lb
 * 
 */
public class TracerouteResult extends AbstractResult<TracerouteResult> {
	
	@HstoreKey("traceroute_objective_host")
	private String url;
	
	@HstoreKey("traceroute_result_status")
	private String status;
	
	@HstoreKey("traceroute_result_duration")
	private Long duration;
		
	@HstoreKey("traceroute_objective_timeout")
	private Long timeout;
	
	@HstoreKey("traceroute_objective_max_hops")
	private Integer maxHops;
	
	@HstoreKey("traceroute_result_hops")
	private Integer hops;
	
	@HstoreKey("traceroute_result_details")
	@HstoreCollection(PathElement.class)
	private ArrayList<PathElement> resultEntries;
	
	public final static class PathElement {
		@HstoreKey("time")
		long time;
		
		@HstoreKey("host")
		String host;
		
		public PathElement() {
			
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		@Override
		public String toString() {
			return "PathElement [time=" + time + ", host=" + host + "]";
		}
	}
	
	/**
	 * 
	 */
	public TracerouteResult() {
		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Integer getMaxHops() {
		return maxHops;
	}

	public void setMaxHops(Integer maxHops) {
		this.maxHops = maxHops;
	}

	public Integer getHops() {
		return hops;
	}

	public void setHops(Integer hops) {
		this.hops = hops;
	}
	
	public ArrayList<PathElement> getResultEntries() {
		return resultEntries;
	}

	public void setResultEntries(ArrayList<PathElement> resultEntries) {
		this.resultEntries = resultEntries;
	}

	@Override
	public String toString() {
		return "TracerouteResult [url=" + url + ", status=" + status
				+ ", duration=" + duration + ", timeout=" + timeout
				+ ", maxHops=" + maxHops + ", hops=" + hops
				+ ", resultEntries=" + resultEntries + "]";
	}
}
