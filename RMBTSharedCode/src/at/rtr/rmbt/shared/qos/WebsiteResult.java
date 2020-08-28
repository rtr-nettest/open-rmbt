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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Result example:
 * 
 * "website_result_info"=>"OK", 
 * "website_objective_url"=>"http://alladin.at", 
 * "website_result_status"=>"200", 
 * "website_result_duration"=>"2194170609", 
 * "website_result_rx_bytes"=>"18535", 
 * "website_result_tx_bytes"=>"1170", 
 * "website_objective_timeout"=>"10000"
 * 
 * 
 * @author lb
 * 
 */
public class WebsiteResult extends AbstractResult {
	
	/**
	 * 
	 */
	@JsonProperty("website_objective_url")
	private String url;
	
	/**
	 * 
	 */
	@JsonProperty("website_objective_clear_cache")
	private Boolean clearCache;
	
	/**
	 * 
	 */
	@JsonProperty("website_objective_user_agent")
	private String userAgent;
	
	/**
	 * 
	 */
	@JsonProperty("website_objective_timeout")
	private Long timeout;
	
	////
	
	@JsonProperty("website_result_info")
	private String info;
	
	@JsonProperty("website_result_status")
	private String status;
	
	@JsonProperty("website_result_duration")
	private Long duration;
	
	@JsonProperty("website_result_rx_bytes")
	private Long rxBytes;
	
	@JsonProperty("website_result_tx_bytes")
	private Long txBytes;
	
	@JsonProperty("website_result_first_http_response_time_ns")
	private Long firstHttpResponseTimeNs;
	
	@JsonProperty("website_result_request_count")
	private Integer requestCount;
	
	/**
	 * 
	 */
	public WebsiteResult() {
		
	}
	
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
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

	public Long getRxBytes() {
		return rxBytes;
	}

	public void setRxBytes(Long rxBytes) {
		this.rxBytes = rxBytes;
	}

	public Long getTxBytes() {
		return txBytes;
	}

	public void setTxBytes(Long txBytes) {
		this.txBytes = txBytes;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	/**
	 * 
	 * @return
	 */
	public Boolean getClearCache() {
		return clearCache;
	}

	/**
	 * 
	 * @param clearCache
	 */
	public void setClearCache(Boolean clearCache) {
		this.clearCache = clearCache;
	}

	/**
	 * 
	 * @return
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * 
	 * @param userAgent
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * 
	 * @return
	 */
	public Long getFirstHttpResponseTimeNs() {
		return firstHttpResponseTimeNs;
	}

	/**
	 * 
	 * @param firstHttpResponseTimeNs
	 */
	public void setFirstHttpResponseTimeNs(Long firstHttpResponseTimeNs) {
		this.firstHttpResponseTimeNs = firstHttpResponseTimeNs;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getRequestCount() {
		return requestCount;
	}

	/**
	 * 
	 * @param requestCount
	 */
	public void setRequestCount(Integer requestCount) {
		this.requestCount = requestCount;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WebsiteResult [info=" + info + ", url=" + url + ", status="
				+ status + ", duration=" + duration + ", rxBytes=" + rxBytes
				+ ", txBytes=" + txBytes + ", timeout=" + timeout + "]";
	}
}
