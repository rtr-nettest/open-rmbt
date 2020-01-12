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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.report;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.ApiRequestInfo;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.result.TimeBasedResultDto;

/**
 * This module defines a data model for reporting results from Measurement Agents, which are part of a Large-Scale Measurement Platform (LMAP), to result data Collectors.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This module defines a data model for reporting results from Measurement Agents, which are part of a Large-Scale Measurement Platform (LMAP), to result data Collectors.")
@JsonClassDescription("This module defines a data model for reporting results from Measurement Agents, which are part of a Large-Scale Measurement Platform (LMAP), to result data Collectors.")
@JsonInclude(Include.NON_EMPTY)
public class LmapReportDto {
			
	/**
	 * The date and time when this result report was sent to a Collector.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The date and time when this result report was sent to a Collector.")
	@JsonPropertyDescription("The date and time when this result report was sent to a Collector.")
	@Expose
	@SerializedName("date")
	@JsonProperty(required = true, value = "date")
	private LocalDateTime date;
	
	/**
	 * The agent-id of the agent from which this report originates.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The agent-id of the agent from which this report originates.")
	@JsonPropertyDescription("The agent-id of the agent from which this report originates.")
	@Expose
	@SerializedName("agent-id")
	@JsonProperty(value = "agent-id")
	private String agentId;

	/**
	 * The group-id of the agent from which this report originates.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The group-id of the agent from which this report originates.")
	@JsonPropertyDescription("The group-id of the agent from which this report originates.")
	@Expose
	@SerializedName("group-id")
	@JsonProperty(value = "group-id")
	private String groupId;

	/**
	 * The measurement-point of the agent from which this report originates.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The measurement-point of the agent from which this report originates.")
	@JsonPropertyDescription("The measurement-point of the agent from which this report originates.")
	@Expose
	@SerializedName("measurement-point")
	@JsonProperty(value = "measurement-point")
	private String measurementPoint;
	
	/**
	 * The list of Tasks for which results are reported.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The list of Tasks for which results are reported.")
	@JsonPropertyDescription("The list of Tasks for which results are reported.")
	@Expose
	@SerializedName("result")
	@JsonProperty(required = true, value = "result")
	private List<LmapResultDto> results = new ArrayList<>();
	
	/**
	 * Additional information that is sent by agent alongside the request.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Additional information that is sent by agent alongside the request.")
	@JsonPropertyDescription("Additional information that is sent by agent alongside the request.")
	@Expose
	@SerializedName("additional_request_info")
	@JsonProperty(required = true, value = "additional_request_info")	
	private ApiRequestInfo additionalRequestInfo;

	/**
	 * This module defines a data model for reporting time based results from Measurement Agents.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "This module defines a data model for reporting time based results from Measurement Agents.")
	@JsonPropertyDescription("This module defines a data model for reporting time based results from Measurement Agents.")
	@Expose
	@SerializedName("time_based_result")
	@JsonProperty(required = true, value = "time_based_result")
	private TimeBasedResultDto timeBasedResult;

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getMeasurementPoint() {
		return measurementPoint;
	}

	public void setMeasurementPoint(String measurementPoint) {
		this.measurementPoint = measurementPoint;
	}

	public List<LmapResultDto> getResults() {
		return results;
	}

	public void setResults(List<LmapResultDto> results) {
		this.results = results;
	}

	public ApiRequestInfo getAdditionalRequestInfo() {
		return additionalRequestInfo;
	}

	public void setAdditionalRequestInfo(ApiRequestInfo additionalRequestInfo) {
		this.additionalRequestInfo = additionalRequestInfo;
	}

	public TimeBasedResultDto getTimeBasedResult() {
		return timeBasedResult;
	}

	public void setTimeBasedResult(TimeBasedResultDto timeBasedResult) {
		this.timeBasedResult = timeBasedResult;
	}
}
