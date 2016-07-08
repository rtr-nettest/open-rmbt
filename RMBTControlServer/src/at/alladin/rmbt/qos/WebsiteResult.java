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

import at.alladin.rmbt.shared.hstoreparser.annotation.HstoreKey;

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
public class WebsiteResult extends AbstractResult<WebsiteResult> {
	
	@HstoreKey("website_result_info")
	private String info;
	
	@HstoreKey("website_objective_url")
	private String url;
	
	@HstoreKey("website_result_status")
	private String status;
	
	@HstoreKey("website_result_duration")
	private Long duration;
	
	@HstoreKey("website_result_rx_bytes")
	private Long rxBytes;
	
	@HstoreKey("website_result_tx_bytes")
	private Long txBytes;
	
	@HstoreKey("website_objective_timeout")
	private Long timeout;
	
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
