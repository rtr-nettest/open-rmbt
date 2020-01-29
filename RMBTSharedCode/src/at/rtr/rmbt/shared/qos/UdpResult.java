/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
 * Copyright 2014-2016 SPECURE GmbH
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
 * example result:
 * 
 * OUTGOING:
 * "udp_objective_outgoing_port"=>"10016", 
 * "udp_result_outgoing_num_packets"=>"8", 
 * "udp_objective_outgoing_num_packets"=>"8"
 * 
 * INCOMING:
 * "udp_objective_incoming_port"=>"37865", 
 * "udp_result_incoming_num_packets"=>"11", 
 * "udp_objective_incoming_num_packets"=>"11"
 * 
 * @author lb
 *
 */
public class UdpResult extends AbstractResult {
	
	@JsonProperty("udp_objective_delay")
	private Object delay;

	@JsonProperty("udp_objective_out_port")
	private Object outPort;
	
	@JsonProperty("udp_result_out_num_packets")
	private Object resultOutNumPackets;
	
	@JsonProperty("udp_result_out_response_num_packets")
	private Object resultOutNumPacketsResponse;
	
	@JsonProperty("udp_objective_out_num_packets")
	private Object outNumPackets;
	
	@JsonProperty("udp_objective_in_port")
	private Object inPort;
	
	@JsonProperty("udp_result_in_num_packets")
	private Object resultInNumPackets;
	
	@JsonProperty("udp_objective_in_num_packets")
	private Object inNumPackets;
	
	@JsonProperty("udp_result_in_response_num_packets")
	private Object resultInNumPacketsResponse;
	
	@JsonProperty("udp_result_in_packet_loss_rate")
	private Object incomingPlr;

	@JsonProperty("udp_result_out_packet_loss_rate")
	private Object outgoingPlr;

	@JsonProperty("udp_result_out_rtt_avg_ns")
	private Object outRttAvgNs;

	@JsonProperty("udp_result_in_rtt_avg_ns")
	private Object inRttAvgNs;

	/**
	 * 
	 */
	public UdpResult() {
		
	}

	public Object getDelay() {
		return delay;
	}

	public void setDelay(Object delay) {
		this.delay = delay;
	}

	public Object getOutPort() {
		return outPort;
	}

	public void setOutPort(Object outPort) {
		this.outPort = outPort;
	}

	public Object getResultOutNumPackets() {
		return resultOutNumPackets;
	}

	public void setResultOutNumPackets(Object resultOutNumPackets) {
		this.resultOutNumPackets = resultOutNumPackets;
	}

	public Object getResultOutNumPacketsResponse() {
		return resultOutNumPacketsResponse;
	}

	public void setResultOutNumPacketsResponse(Object resultOutNumPacketsResponse) {
		this.resultOutNumPacketsResponse = resultOutNumPacketsResponse;
	}

	public Object getOutNumPackets() {
		return outNumPackets;
	}

	public void setOutNumPackets(Object outNumPackets) {
		this.outNumPackets = outNumPackets;
	}

	public Object getInPort() {
		return inPort;
	}

	public void setInPort(Object inPort) {
		this.inPort = inPort;
	}

	public Object getResultInNumPackets() {
		return resultInNumPackets;
	}

	public void setResultInNumPackets(Object resultInNumPackets) {
		this.resultInNumPackets = resultInNumPackets;
	}

	public Object getInNumPackets() {
		return inNumPackets;
	}

	public void setInNumPackets(Object inNumPackets) {
		this.inNumPackets = inNumPackets;
	}

	public Object getResultInNumPacketsResponse() {
		return resultInNumPacketsResponse;
	}

	public void setResultInNumPacketsResponse(Object resultInNumPacketsResponse) {
		this.resultInNumPacketsResponse = resultInNumPacketsResponse;
	}

	public Object getIncomingPlr() {
		return incomingPlr;
	}

	public void setIncomingPlr(Object incomingPlr) {
		this.incomingPlr = incomingPlr;
	}

	public Object getOutgoingPlr() {
		return outgoingPlr;
	}

	public void setOutgoingPlr(Object outgoingPlr) {
		this.outgoingPlr = outgoingPlr;
	}

	public Object getOutRttAvgNs() {
		return outRttAvgNs;
	}

	public void setOutRttAvgNs(Object outRttAvgNs) {
		this.outRttAvgNs = outRttAvgNs;
	}

	public Object getInRttAvgNs() {
		return inRttAvgNs;
	}

	public void setInRttAvgNs(Object inRttAvgNs) {
		this.inRttAvgNs = inRttAvgNs;
	}

	@Override
	public String toString() {
		return "UdpResult [delay=" + delay + ", outPort=" + outPort
				+ ", resultOutNumPackets=" + resultOutNumPackets
				+ ", resultOutNumPacketsResponse="
				+ resultOutNumPacketsResponse + ", outNumPackets="
				+ outNumPackets + ", inPort=" + inPort
				+ ", resultInNumPackets=" + resultInNumPackets
				+ ", inNumPackets=" + inNumPackets
				+ ", resultInNumPacketsResponse=" + resultInNumPacketsResponse
				+ ", incomingPlr=" + incomingPlr + ", outgoingPlr="
				+ outgoingPlr + "]";
	}
}
