/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
	private String delay;

	@HstoreKey("udp_objective_out_port")
	private String outPort;
	
	@HstoreKey("udp_result_out_num_packets")
	private String resultOutNumPackets;
	
	@HstoreKey("udp_result_out_response_num_packets")
	private String resultOutNumPacketsResponse;
	
	@HstoreKey("udp_objective_out_num_packets")
	private String outNumPackets;
	
	@HstoreKey("udp_objective_in_port")
	private String inPort;
	
	@HstoreKey("udp_result_in_num_packets")
	private String resultInNumPackets;
	
	@HstoreKey("udp_objective_in_num_packets")
	private String inNumPackets;
	
	@HstoreKey("udp_result_in_response_num_packets")
	private String resultInNumPacketsResponse;
	
	@HstoreKey("udp_result_in_packet_loss_rate")
	private String incomingPlr;

	@HstoreKey("udp_result_out_packet_loss_rate")
	private String outgoingPlr;

	/**
	 * 
	 */
	public UdpResult() {
		
	}

	public String getOutPort() {
		return outPort;
	}

	public void setOutPort(String outPort) {
		this.outPort = outPort;
	}

	public String getResultOutNumPackets() {
		return resultOutNumPackets;
	}

	public void setResultOutNumPackets(String resultOutNumPackets) {
		this.resultOutNumPackets = resultOutNumPackets;
	}

	public String getOutNumPackets() {
		return outNumPackets;
	}

	public void setOutNumPackets(String outNumPackets) {
		this.outNumPackets = outNumPackets;
	}

	public String getInPort() {
		return inPort;
	}

	public void setInPort(String inPort) {
		this.inPort = inPort;
	}

	public String getResultInNumPackets() {
		return resultInNumPackets;
	}

	public void setResultInNumPackets(String resultInNumPackets) {
		this.resultInNumPackets = resultInNumPackets;
	}

	public String getInNumPackets() {
		return inNumPackets;
	}

	public void setInNumPackets(String inNumPackets) {
		this.inNumPackets = inNumPackets;
	}

	public String getIncomingPlr() {
		return incomingPlr;
	}

	public void setIncomingPlr(String incomingPlr) {
		this.incomingPlr = incomingPlr;
	}

	public String getOutgoingPlr() {
		return outgoingPlr;
	}

	public void setOutgoingPlr(String outgoingPlr) {
		this.outgoingPlr = outgoingPlr;
	}

	public String getDelay() {
		return delay;
	}

	public void setDelay(String delay) {
		this.delay = delay;
	}

	public String getResultOutNumPacketsResponse() {
		return resultOutNumPacketsResponse;
	}

	public void setResultOutNumPacketsResponse(String resultOutNumPacketsResponse) {
		this.resultOutNumPacketsResponse = resultOutNumPacketsResponse;
	}

	public String getResultInNumPacketsResponse() {
		return resultInNumPacketsResponse;
	}

	public void setResultInNumPacketsResponse(String resultInNumPacketsResponse) {
		this.resultInNumPacketsResponse = resultInNumPacketsResponse;
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
