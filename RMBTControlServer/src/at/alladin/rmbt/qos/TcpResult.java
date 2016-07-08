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
 * example results:
 * <br>
 * IN:
 * "tcp_result_in"=>"OK", 
 * "tcp_objective_in_port"=>"20774", 
 * "tcp_objective_timeout"=>"3000", 
 * "tcp_result_in_response"=>"HELLO TO 20774"
 * <br>
 * OUT:
 * "tcp_result_out"=>"OK", 
 * "tcp_objective_timeout"=>"3000", 
 * "tcp_objective_out_port"=>"33194", 
 * "tcp_result_out_response"=>"PING"
 * 
 * 
 * @author lb
 * 
 */
public class TcpResult extends AbstractResult<TcpResult> {

	@HstoreKey("tcp_result_in")
	private String inResult;
	
	@HstoreKey("tcp_objective_in_port")
	private Integer inPort;

	@HstoreKey("tcp_result_in_response")
	private String inResponse;
	
	@HstoreKey("tcp_result_out")
	private String outResult;
	
	@HstoreKey("tcp_objective_out_port")
	private Integer outPort;

	@HstoreKey("tcp_result_out_response")
	private String outResponse;

	@HstoreKey("tcp_objective_timeout")
	private Long timeout;

	/**
	 * 
	 */
	public TcpResult() {
		
	}
	
	public String getInResult() {
		return inResult;
	}

	public void setInResult(String inResult) {
		this.inResult = inResult;
	}

	public Integer getInPort() {
		return inPort;
	}

	public void setInPort(Integer inPort) {
		this.inPort = inPort;
	}

	public String getInResponse() {
		return inResponse;
	}

	public void setInResponse(String inResponse) {
		this.inResponse = inResponse;
	}

	public String getOutResult() {
		return outResult;
	}

	public void setOutResult(String outResult) {
		this.outResult = outResult;
	}

	public Integer getOutPort() {
		return outPort;
	}

	public void setOutPort(Integer outPort) {
		this.outPort = outPort;
	}

	public String getOutResponse() {
		return outResponse;
	}

	public void setOutResponse(String outResponse) {
		this.outResponse = outResponse;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return "TcpResult [inResult=" + inResult + ", inPort=" + inPort
				+ ", inResponse=" + inResponse + ", outResult=" + outResult
				+ ", outPort=" + outPort + ", outResponse=" + outResponse
				+ ", timeout=" + timeout + ", toString()=" + super.toString()
				+ "]";
	}	
}
