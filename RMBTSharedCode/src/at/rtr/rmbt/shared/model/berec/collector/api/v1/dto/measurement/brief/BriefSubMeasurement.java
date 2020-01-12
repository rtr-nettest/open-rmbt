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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.brief;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Brief/short information of a sub measurement.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Brief/short information of a sub measurement.")
@JsonClassDescription("Brief/short information of a sub measurement.")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "deserialize_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BriefSpeedMeasurement.class, name = "speed_measurement"),
        @JsonSubTypes.Type(value = BriefQoSMeasurement.class, name = "qos_measurement")
})
public class BriefSubMeasurement {

	/**
	 * Start time of this sub measurement in UTC.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Start time of this sub measurement in UTC.")
	@JsonPropertyDescription("Start time of this sub measurement in UTC.")
	@Expose
	@SerializedName("start_time")
	@JsonProperty(required = true, value = "start_time")
	private LocalDateTime startTime;
	
	/**
	 * Duration of this sub measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Duration of this sub measurement.")
	@JsonPropertyDescription("Duration of this sub measurement.")
	@Expose
	@SerializedName("duration_ns")
	@JsonProperty(required = true, value = "duration_ns")
	private Long durationNs;
	
	public LocalDateTime getStartTime() {
		return startTime;
	}
	
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}
	
	public Long getDurationNs() {
		return durationNs;
	}
	
	public void setDurationNs(Long durationNs) {
		this.durationNs = durationNs;
	}
}
