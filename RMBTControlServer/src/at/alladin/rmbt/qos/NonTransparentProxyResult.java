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
 * @author lb
 *
 */
public class NonTransparentProxyResult extends AbstractResult<NonTransparentProxyResult> {
	
	@HstoreKey("nontransproxy_objective_request")
	private String request;

	@HstoreKey("nontransproxy_objective_timeout")
	private Long timeout;

	@HstoreKey("nontransproxy_result")
	private String result;

	@HstoreKey("nontransproxy_result_response")
	private String response;

	@HstoreKey("nontransproxy_objective_port")
	private Integer port;

	public NonTransparentProxyResult() {
		
	}
	
	public String getResponse() {
		return this.response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getRequest() {
		return this.request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "NonTransparentProxyResult [request=" + request + ", timeout="
				+ timeout + ", result=" + result + ", response=" + response
				+ ", port=" + port + "]";
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
