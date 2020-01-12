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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.result;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.GeoLocationDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.PointInTimeValueDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.SignalDto;

/**
 * This module defines a data model for reporting time based results from Measurement Agents.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This module defines a data model for reporting time based results from Measurement Agents.")
@JsonClassDescription("This module defines a data model for reporting time based results from Measurement Agents.")
public class TimeBasedResultDto {

	/**
	 * Start date and time for this measurement. Date and time is always stored as UTC.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Start date and time for this measurement. Date and time is always stored as UTC.")
	@JsonPropertyDescription("Start date and time for this measurement. Date and time is always stored as UTC.")
	@Expose
	@SerializedName("start_time")
	@JsonProperty(required = true, value = "start_time")
	private LocalDateTime startTime;
	
	/**
	 * End date and time for this measurement. Date and time is always stored as UTC.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "End date and time for this measurement. Date and time is always stored as UTC.")
	@JsonPropertyDescription("End date and time for this measurement. Date and time is always stored as UTC.")
	@Expose
	@SerializedName("end_time")
	@JsonProperty(required = true, value = "end_time")
	private LocalDateTime endTime;
	
	/**
	 * Overall duration of this measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Overall duration of this measurement.")
	@JsonPropertyDescription("Overall duration of this measurement.")
	@Expose
	@SerializedName("duration_ns")
	@JsonProperty(required = true, value = "duration_ns")
	private Long durationNs;
	
// GeoLocationInfo
	
	/**
	 * List of all captured geographic locations.
	 */
	@io.swagger.annotations.ApiModelProperty("List of all captured geographic locations.")
	@JsonPropertyDescription("List of all captured geographic locations.")
	@Expose
	@SerializedName("geo_locations")
	@JsonProperty("geo_locations")
	private List<GeoLocationDto> geoLocations;
	
// AgentInfo
	
	// -> everything already submitted by ApiRequestInfo
	
// DeviceInfo
	
	// -> everything already submitted by ApiRequestInfo
	
// _ OperatingSystemInfo
		
	/**
	 * CPU usage during the test, if available.
	 */
	@io.swagger.annotations.ApiModelProperty("CPU usage during the test, if available.")
	@JsonPropertyDescription("CPU usage during the test, if available.")
	@Expose
	@SerializedName("cpu_usage")
	@JsonProperty("cpu_usage")
	private List<PointInTimeValueDto<Double>> cpuUsage;
	
	/**
	 * Memory usage during the test, if available.
	 */
	@io.swagger.annotations.ApiModelProperty("Memory usage during the test, if available.")
	@JsonPropertyDescription("Memory usage during the test, if available.")
	@Expose
	@SerializedName("mem_usage")
	@JsonProperty("mem_usage")
	private List<PointInTimeValueDto<Double>> memUsage;
	
// NetworkInfo
	
// _ EmbeddedNetworkType

	/**
	 * Contains all relevant network information of a single point in time.
	 * @see at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.NetworkPointInTimeInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains all relevant network information of a single point in time.")
	@JsonPropertyDescription("Contains all relevant network information of a single point in time.")
	@Expose
	@SerializedName("network_points_in_time")
	@JsonProperty(required = true, value = "network_points_in_time")
	private List<MeasurementResultNetworkPointInTimeDto> networkPointsInTime;

// _ SignalInfo

	/**
	 * List of captured signal information.
	 */
	@io.swagger.annotations.ApiModelProperty("List of captured signal information.")
	@JsonPropertyDescription("List of captured signal information.")
	@Expose
	@SerializedName("signals")
	@JsonProperty("signals")
	private List<SignalDto> signals;

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Long getDurationNs() {
		return durationNs;
	}

	public void setDurationNs(Long durationNs) {
		this.durationNs = durationNs;
	}

	public List<GeoLocationDto> getGeoLocations() {
		return geoLocations;
	}

	public void setGeoLocations(List<GeoLocationDto> geoLocations) {
		this.geoLocations = geoLocations;
	}

	public List<PointInTimeValueDto<Double>> getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(List<PointInTimeValueDto<Double>> cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public List<PointInTimeValueDto<Double>> getMemUsage() {
		return memUsage;
	}

	public void setMemUsage(List<PointInTimeValueDto<Double>> memUsage) {
		this.memUsage = memUsage;
	}

	public List<MeasurementResultNetworkPointInTimeDto> getNetworkPointsInTime() {
		return networkPointsInTime;
	}

	public void setNetworkPointsInTime(List<MeasurementResultNetworkPointInTimeDto> networkPointsInTime) {
		this.networkPointsInTime = networkPointsInTime;
	}

	public List<SignalDto> getSignals() {
		return signals;
	}

	public void setSignals(List<SignalDto> signals) {
		this.signals = signals;
	}
}
