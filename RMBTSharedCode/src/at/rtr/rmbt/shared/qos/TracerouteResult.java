/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
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

package at.rtr.rmbt.shared.qos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * 
 * @author lb
 * 
 */
public class TracerouteResult extends AbstractResult {
	
	@JsonProperty("traceroute_objective_host")
	@SerializedName("traceroute_objective_host")
	private String url;
	
	@JsonProperty("traceroute_result_status")
	@SerializedName("traceroute_result_status")
	private String status;
	
	@JsonProperty("traceroute_result_duration")
	@SerializedName("traceroute_result_duration")
	private Long duration;
		
	@JsonProperty("traceroute_objective_timeout")
	@SerializedName("traceroute_objective_timeout")
	private Long timeout;
	
	@JsonProperty("traceroute_objective_max_hops")
	@SerializedName("traceroute_objective_max_hops")
	private Integer maxHops;
	
	@JsonProperty("traceroute_result_hops")
	@SerializedName("traceroute_result_hops")
	private Integer hops;
	
	@JsonProperty("traceroute_result_details")
	@SerializedName("traceroute_result_details")
	private List<PathElement> resultEntries;
	
	public final static class PathElement {
		@JsonProperty("time")
		@SerializedName("time")
		long time;
		
		@JsonProperty("host")
		@SerializedName("host")
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
	
	public List<PathElement> getResultEntries() {
		return resultEntries;
	}

	public void setResultEntries(List<PathElement> resultEntries) {
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
