/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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
 * @author fk 
 */
public class EchoProtocolResult extends AbstractResult {

	@JsonProperty("echo_protocol_objective_host")
	private Object host;

	@JsonProperty("echo_protocol_objective_port")
	private Object port;

	@JsonProperty("echo_protocol_objective_protocol")
	private Object protocol;

	@JsonProperty("echo_protocol_objective_payload")
	private Object payload;

	@JsonProperty("echo_protocol_status")
	private Object status;

	@JsonProperty("echo_protocol_result")
	private Object result;

	public Object getHost() {
		return host;
	}

	public void setHost(Object host) {
		this.host = host;
	}

	public Object getPort() {
		return port;
	}

	public void setPort(Object port) {
		this.port = port;
	}

	public Object getProtocol() {
		return protocol;
	}

	public void setProtocol(Object protocol) {
		this.protocol = protocol;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Object getStatus() {
		return status;
	}

	public void setStatus(Object status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "EchoProtocolResult{" +
				"host=" + host +
				", port=" + port +
				", protocol=" + protocol +
				", payload=" + payload +
				", status=" + status +
				", result=" + result +
				'}';
	}
}
