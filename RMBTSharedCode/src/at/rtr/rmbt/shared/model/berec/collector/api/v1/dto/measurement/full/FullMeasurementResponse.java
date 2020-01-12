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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.full;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.MeasurementTypeDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.ComputedNetworkPointInTimeInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.DeviceInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.GeoLocationDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.MeasurementAgentInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.NetworkInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.QosAdvancedEvaluationDto;

/**
 * This DTO class contains all measurement information that is sent to the measurement agent.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO class contains all measurement information that is sent to the measurement agent.")
@JsonClassDescription("This DTO class contains all measurement information that is sent to the measurement agent.")
public class FullMeasurementResponse extends BasicResponse {

	/**
	 * The unique identifier (UUIDv4) of the measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The unique identifier (UUIDv4) of the measurement.")
	@JsonPropertyDescription("The unique identifier (UUIDv4) of the measurement.")
	@Expose
	@SerializedName("uuid")
	@JsonProperty(required = true, value = "uuid")
	private String uuid;
	
	/**
	 * The open-data identifier (UUIDv4) of the measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The open-data identifier (UUIDv4) of the measurement.")
	@JsonPropertyDescription("The open-data identifier (UUIDv4) of the measurement.")
	@Expose
	@SerializedName("open_data_uuid")
	@JsonProperty(required = true, value = "open_data_uuid")
	private String openDataUuid;

	/**
	 * Measurement system uuid. Can be either own system or imported from open-data.
	 */
	@JsonPropertyDescription("Measurement system uuid. Can be either own system or imported from open-data.")
	@Expose
	@SerializedName("system_uuid")
	@JsonProperty("system_uuid")
	private String systemUuid;
	
	/**
	 * A tag provided by the agent.
	 */
	@JsonPropertyDescription("A tag provided by the agent.")
	@Expose
	@SerializedName("tag")
	@JsonProperty("tag")
	private String tag;
	
	/**
	 * Contains the result of a Speed and/or QoS measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains the result of a Speed and/or QoS measurement.")
	@JsonPropertyDescription("Contains the result of a Speed and/or QoS measurement.")
	@Expose
	@SerializedName("measurements")
	@JsonProperty(required = true, value = "measurements")
	private /*Enum*/Map<MeasurementTypeDto, FullSubMeasurement> measurements;
	
// MeasurementTime
	
	/**
	 * Start Date and time for this (sub-) measurement. Date and time is always stored as UTC.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Start Date and time for this (sub-) measurement.")
	@JsonPropertyDescription("Start Date and time for this (sub-) measurement.")
	@Expose
	@SerializedName("start_time")
	@JsonProperty(required = true, value = "start_time")
	private LocalDateTime startTime;
	
	/**
	 * End Date and time for this (sub-) measurement. Date and time is always stored as UTC.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "End Date and time for this (sub-) measurement. Date and time is always stored as UTC.")
	@JsonPropertyDescription("End Date and time for this (sub-) measurement. Date and time is always stored as UTC.")
	@Expose
	@SerializedName("end_time")
	@JsonProperty(required = true, value = "end_time")
	private LocalDateTime endTime;
	
	/**
	 * Duration of a measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Duration of a measurement.")
	@JsonPropertyDescription("Duration of a measurement.")
	@Expose
	@SerializedName("duration_ns")
	@JsonProperty(required = true, value = "duration_ns")
	private Long durationNs;
	
// GeoLocationInfo

	/**
	 * List of all captured geographic locations.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "List of all captured geographic locations.")
	@JsonPropertyDescription("List of all captured geographic locations.")
	@Expose
	@SerializedName("geo_locations")
	@JsonProperty(required = true, value = "geo_locations")
	private List<GeoLocationDto> geoLocations;
	
	/**
	 * The distance moved in metres, calculated from the geoLocations.
	 */
	@JsonPropertyDescription("The distance moved in metres, calculated from the geoLocations.")
	@Expose
	@SerializedName("distance_moved_metres")
	@JsonProperty("distance_moved_metres")
	private Integer distanceMovedMetres;
	
// measurement agentInfo
	
	/**
	 * @see MeasurementAgentInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains information about the measurement measurement agent.")
	@JsonPropertyDescription("Contains information about the measurement measurement agent.")
	@Expose
	@SerializedName("agent_info")
	@JsonProperty(required = true, value = "agent_info")
	private MeasurementAgentInfoDto agentInfo;
	
// DeviceInfo
	
	/**
	 * @see DeviceInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains information about the device the measurement software is running on.")
	@JsonPropertyDescription("Contains information about the device the measurement software is running on.")
	@Expose
	@SerializedName("device_info")
	@JsonProperty(required = true, value = "device_info")
	private DeviceInfoDto deviceInfo;
	
// NetworkInfo
	
	/**
	 * @see NetworkInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains network related information gathered during the test.")
	@JsonPropertyDescription("Contains network related information gathered during the test.")
	@Expose
	@SerializedName("network_info")
	@JsonProperty(required = true, value = "network_info")
	private NetworkInfoDto networkInfo;
	
	/**
	 * @see ComputedNetworkPointInTimeInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains post-processed network related information gathered during the test.")
	@JsonPropertyDescription("Contains post-processed network related information gathered during the test.")
	@Expose
	@SerializedName("computed_network_info")
	@JsonProperty(required = true, value = "computed_network_info")
	private ComputedNetworkPointInTimeInfoDto computedNetworkInfo;
	
	/**
	 * @see QosAdvancedEvaluation
	 */
	@JsonPropertyDescription("Contains advanced QoS related information.")
	@Expose
	@SerializedName("qos_advanced_evaluation")
	@JsonProperty("qos_advanced_evaluation")
	private QosAdvancedEvaluationDto qosAdvancedEvaluation;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOpenDataUuid() {
		return openDataUuid;
	}

	public void setOpenDataUuid(String openDataUuid) {
		this.openDataUuid = openDataUuid;
	}

	public String getSystemUuid() {
		return systemUuid;
	}

	public void setSystemUuid(String systemUuid) {
		this.systemUuid = systemUuid;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}

	public Map<MeasurementTypeDto, FullSubMeasurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(Map<MeasurementTypeDto, FullSubMeasurement> measurements) {
		this.measurements = measurements;
	}

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

	public MeasurementAgentInfoDto getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(MeasurementAgentInfoDto agentInfo) {
		this.agentInfo = agentInfo;
	}

	public DeviceInfoDto getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfoDto deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
	
	public NetworkInfoDto getNetworkInfo() {
		return networkInfo;
	}
	
	public void setNetworkInfo(NetworkInfoDto networkInfo) {
		this.networkInfo = networkInfo;
	}

	public ComputedNetworkPointInTimeInfoDto getComputedNetworkInfo() {
		return computedNetworkInfo;
	}
	
	public void setComputedNetworkInfo(ComputedNetworkPointInTimeInfoDto computedNetworkInfo) {
		this.computedNetworkInfo = computedNetworkInfo;
	}

	public QosAdvancedEvaluationDto getQosAdvancedEvaluation() {
		return qosAdvancedEvaluation;
	}

	public void setQosAdvancedEvaluation(QosAdvancedEvaluationDto qosAdvancedEvaluation) {
		this.qosAdvancedEvaluation = qosAdvancedEvaluation;
	}

	public Integer getDistanceMovedMetres() {
		return distanceMovedMetres;
	}

	public void setDistanceMovedMetres(Integer distanceMovedMetres) {
		this.distanceMovedMetres = distanceMovedMetres;
	}
	
}
