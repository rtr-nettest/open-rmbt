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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains round trip time information measured during the measurement on the measurement agent.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains round trip time information measured during the measurement on the measurement agent.")
@JsonClassDescription("Contains round trip time information measured during the measurement on the measurement agent.")
public class RttInfoDto {

	/**
	 * The address of the measurement server.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The address of the rtt server.")
	@JsonPropertyDescription("The address of the rtt server.")
	@Expose
	@SerializedName("address")
	@JsonProperty(required = true, value = "address")
	private String address;

	/**
	 * List of all measured RTTs.
	 */
	@io.swagger.annotations.ApiModelProperty("List of all measured RTTs.")
	@JsonPropertyDescription("List of all measured RTTs.")
	@Expose
	@SerializedName("rtts")
	@JsonProperty("rtts")
	private List<RttDto> rtts;

	/**
	 * The number of RTT packets to send, as instructed by the server.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The number of RTT packets to send, as instructed by the server.")
	@JsonPropertyDescription("The number of RTT packets to send, as instructed by the server.")
	@Expose
	@SerializedName("requested_num_packets")
	@JsonProperty(required = true, value = "requested_num_packets")
	private Integer requestedNumPackets;
	
	/**
	 * The actual number of sent RTT packets.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual number of sent RTT packets.")
	@JsonPropertyDescription("The actual number of sent RTT packets.")
	@Expose
	@SerializedName("num_sent")
	@JsonProperty(required = true, value = "num_sent")
	private Integer numSent;
	
	/**
	 * The actual number of received RTT packets.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual number of received RTT packets.")
	@JsonPropertyDescription("The actual number of received RTT packets.")
	@Expose
	@SerializedName("num_received")
	@JsonProperty(required = true, value = "num_received")
	private Integer numReceived;
	
	/**
	 * The actual number of failed RTT packets.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual number of failed RTT packets.")
	@JsonPropertyDescription("The actual number of failed RTT packets.")
	@Expose
	@SerializedName("num_error")
	@JsonProperty(required = true, value = "num_error")
	private Integer numError;
	
	/**
	 * The actual number of missing RTT packets.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual number of missing RTT packets.")
	@JsonPropertyDescription("The actual number of missing RTT packets.")
	@Expose
	@SerializedName("num_missing")
	@JsonProperty(required = true, value = "num_missing")
	private Integer numMissing;
	
	/**
	 * The actual size of RTT packets.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual size of RTT packets.")
	@JsonPropertyDescription("The actual size of RTT packets.")
	@Expose
	@SerializedName("packet_size")
	@JsonProperty(required = true, value = "packet_size")
	private Integer packetSize;

	/**
     * Average rtt in nanoseconds
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Average rtt in nanoseconds.")
	@JsonPropertyDescription("Average rtt in nanoseconds.")
    @Expose
    @SerializedName("average_ns")
    @JsonProperty(required = true, value = "average_ns")
    private Long averageNs;

	/**
     * Maximum rtt in nanoseconds
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Maximum rtt in nanoseconds.")
	@JsonPropertyDescription("Maximum rtt in nanoseconds.")
    @Expose
    @SerializedName("maximum_ns")
    @JsonProperty(required = true, value = "maximum_ns")
	private Long maximumNs;
	
	/**
     * Median rtt in nanoseconds
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Median rtt in nanoseconds.")
	@JsonPropertyDescription("Median rtt in nanoseconds.")
    @Expose
    @SerializedName("median_ns")
    @JsonProperty(required = true, value = "median_ns")
	private Long medianNs;
		
	/**
     * Minimum rtt in nanoseconds
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Minimum rtt in nanoseconds.")
	@JsonPropertyDescription("Minimum rtt in nanoseconds.")
    @Expose
    @SerializedName("minimum_ns")
    @JsonProperty(required = true, value = "minimum_ns")
	private Long minimumNs;
	
	/**
     * Standard deviation rtt in nanoseconds
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Standard deviation rtt in nanoseconds.")
	@JsonPropertyDescription("Standard deviation rtt in nanoseconds.")
    @Expose
    @SerializedName("standard_deviation_ns")
    @JsonProperty(required = true, value = "standard_deviation_ns")
	private Long standardDeviationNs;

	public List<RttDto> getRtts() {
		return rtts;
	}

	public void setRtts(List<RttDto> rtts) {
		this.rtts = rtts;
	}

	public Integer getRequestedNumPackets() {
		return requestedNumPackets;
	}

	public void setRequestedNumPackets(Integer requestedNumPackets) {
		this.requestedNumPackets = requestedNumPackets;
	}

	public Integer getNumSent() {
		return numSent;
	}

	public void setNumSent(Integer numSent) {
		this.numSent = numSent;
	}

	public Integer getNumReceived() {
		return numReceived;
	}

	public void setNumReceived(Integer numReceived) {
		this.numReceived = numReceived;
	}

	public Integer getNumError() {
		return numError;
	}

	public void setNumError(Integer numError) {
		this.numError = numError;
	}

	public Integer getNumMissing() {
		return numMissing;
	}

	public void setNumMissing(Integer numMissing) {
		this.numMissing = numMissing;
	}

	public Integer getPacketSize() {
		return packetSize;
	}

	public void setPacketSize(Integer packetSize) {
		this.packetSize = packetSize;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Long getAverageNs() {
		return averageNs;
	}

	public void setAverageNs(Long averageNs) {
		this.averageNs = averageNs;
	}

	public Long getMaximumNs() {
		return maximumNs;
	}

	public void setMaximumNs(Long maximumNs) {
		this.maximumNs = maximumNs;
	}

	public Long getMedianNs() {
		return medianNs;
	}

	public void setMedianNs(Long medianNs) {
		this.medianNs = medianNs;
	}

	public Long getMinimumNs() {
		return minimumNs;
	}

	public void setMinimumNs(Long minimumNs) {
		this.minimumNs = minimumNs;
	}

	public Long getStandardDeviationNs() {
		return standardDeviationNs;
	}

	public void setStandardDeviationNs(Long standardDeviationNs) {
		this.standardDeviationNs = standardDeviationNs;
	}
}
