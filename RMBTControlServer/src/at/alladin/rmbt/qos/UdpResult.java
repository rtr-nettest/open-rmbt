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
public class UdpResult extends AbstractResult<UdpResult> {
	
	@HstoreKey("udp_objective_delay")
	private Object delay;

	@HstoreKey("udp_objective_out_port")
	private Object outPort;
	
	@HstoreKey("udp_result_out_num_packets")
	private Object resultOutNumPackets;
	
	@HstoreKey("udp_result_out_response_num_packets")
	private Object resultOutNumPacketsResponse;
	
	@HstoreKey("udp_objective_out_num_packets")
	private Object outNumPackets;
	
	@HstoreKey("udp_objective_in_port")
	private Object inPort;
	
	@HstoreKey("udp_result_in_num_packets")
	private Object resultInNumPackets;
	
	@HstoreKey("udp_objective_in_num_packets")
	private Object inNumPackets;
	
	@HstoreKey("udp_result_in_response_num_packets")
	private Object resultInNumPacketsResponse;
	
	@HstoreKey("udp_result_in_packet_loss_rate")
	private Object incomingPlr;

	@HstoreKey("udp_result_out_packet_loss_rate")
	private Object outgoingPlr;

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
