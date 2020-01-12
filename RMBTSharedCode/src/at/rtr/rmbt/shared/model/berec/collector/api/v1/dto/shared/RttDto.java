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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains information from a single round trip time measurement on the measurement agent.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains information from a single round trip time measurement on the measurement agent.")
@JsonClassDescription("Contains information from a single round trip time measurement on the measurement agent.")
public class RttDto {

	/**
	 * Round trip time recorded in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Round trip time recorded in nanoseconds.")
	@JsonPropertyDescription("Round trip time recorded in nanoseconds.")
	@Expose
	@SerializedName("rtt_ns")
	@JsonProperty(required = true, value = "rtt_ns")
	private Long rttNs;

	/**
     * Relative time in nanoseconds (to test begin).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Relative time in nanoseconds (to test begin).")
	@JsonPropertyDescription("Relative time in nanoseconds (to test begin).")
    @Expose
    @SerializedName("relative_time_ns")
    @JsonProperty(required = true, value = "relative_time_ns")
    private Long relativeTimeNs;

	public Long getRttNs() {
		return rttNs;
	}

	public void setRttNs(Long rttNs) {
		this.rttNs = rttNs;
	}

	public Long getRelativeTimeNs() {
		return relativeTimeNs;
	}

	public void setRelativeTimeNs(Long relativeTimeNs) {
		this.relativeTimeNs = relativeTimeNs;
	}

}
