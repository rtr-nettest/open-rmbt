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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.control;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Configuration of parameters affecting the whole Measurement Agent.
 * Corresponds to the Agent in the LMAP standard.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "Configuration of parameters affecting the whole Measurement Agent. Corresponds to the Agent in the LMAP standard")
@JsonClassDescription("Configuration of parameters affecting the whole Measurement Agent. Corresponds to the Agent in the LMAP standard")
@JsonInclude(Include.NON_EMPTY)
public class LmapAgentDto {
	
	/**
	 * The agent-id identifies a Measurement Agent with a very low probability of collision (i.e. a UUID v4).
	 * In certain deployments, the agent-id may be considered sensitive, and hence this object is optional.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The agent-id identifies a Measurement Agent with a very low probability of collision (i.e. a UUID v4). In certain deployments, the agent-id may be considered sensitive, and hence this object is optional.")
	@JsonPropertyDescription("The agent-id identifies a Measurement Agent with a very low probability of collision (i.e. a UUID v4). In certain deployments, the agent-id may be considered sensitive, and hence this object is optional.")
	@Expose
	@SerializedName("agent-id")
	@JsonProperty(required = false, value = "agent-id")
	private String agentId;
	
	/**
	 * The group-id identifies a group of Measurement Agents.
	 * In certain deployments, the group-id may be considered less sensitive than the agent-id.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The group-id identifies a group of Measurement Agents. In certain deployments, the group-id may be considered less sensitive than the agent-id.")
	@JsonPropertyDescription("The group-id identifies a group of Measurement Agents. In certain deployments, the group-id may be considered less sensitive than the agent-id.")
	@Expose
	@SerializedName("group-id")
	@JsonProperty(required = false, value = "group-id")
	private String groupId;
	
	/**
	 * The measurement point indicating where the Measurement Agent is located on a path.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The measurement point indicating where the Measurement Agent is located on a path.")
	@JsonPropertyDescription("The measurement point indicating where the Measurement Agent is located on a path.")
	@Expose
	@SerializedName("measurement-point")
	@JsonProperty(required = false, value = "measurement-point")
	private String measurementPoint;
	
	/**
	 * The 'report-agent-id' controls whether the 'agent-id' is reported to Collectors.
	 * Set to true if anonymized results are desired.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The 'report-agent-id' controls whether the 'agent-id' is reported to Collectors. Set to true if anonymized results are desired.")
	@JsonPropertyDescription("The 'report-agent-id' controls whether the 'agent-id' is reported to Collectors. Set to true if anonymized results are desired.")
	@Expose
	@SerializedName("report-agent-id")
	@JsonProperty(required = false, value = "report-agent-id")
	private Boolean reportAgentId;
	
	/**
	 * The 'report-group-id' controls whether the 'group-id' is reported to Collectors.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The 'report-group-id' controls whether the 'group-id' is reported to Collectors.")
	@JsonPropertyDescription("The 'report-group-id' controls whether the 'group-id' is reported to Collectors.")
	@Expose
	@SerializedName("report-group-id")
	@JsonProperty(required = false, value = "report-group-id")
	private Boolean reportGroupId;
	
	/**
	 * The 'report-measurement-point' controls whether the 'measurement-point' is reported to Collectors.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The 'report-measurement-point' controls whether the 'measurement-point' is reported to Collectors.")
	@JsonPropertyDescription("The 'report-measurement-point' controls whether the 'measurement-point' is reported to Collectors.")
	@Expose
	@SerializedName("report-measurement-point")
	@JsonProperty(required = false, value = "report-measurement-point")
	private Boolean reportMeasurementPoint;
	
	/**
	 * A timer is started after each successful contact with a Controller. 
	 * When the timer reaches the controller-timeout, an event (controller-lost) 
	 * is raised indicating that connectivity to the Controller has been lost.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "A timer is started after each successful contact with a Controller. When the timer reaches the controller-timeout, an event (controller-lost) is raised indicating that connectivity to the Controller has been lost.")
	@JsonPropertyDescription("A timer is started after each successful contact with a Controller. When the timer reaches the controller-timeout, an event (controller-lost) is raised indicating that connectivity to the Controller has been lost.")
	@Expose
	@SerializedName("controller-timeout")
	@JsonProperty(required = false, value = "controller-timeout")
	private Integer controllerTimeout;
	
	/**
	 * The date and time the Measurement Agent last started (i.e. the date to the previous execution of the measurement agent before the current one).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The date and time the Measurement Agent last started (i.e. the date to the previous execution of the measurement agent before the current one).")
	@JsonPropertyDescription("The date and time the Measurement Agent last started (i.e. the date to the previous execution of the measurement agent before the current one).")
	@Expose
	@SerializedName("last-started")
	@JsonProperty(required = true, value = "last-started")
	private LocalDateTime lastStarted;

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

	public Boolean getReportAgentId() {
		return reportAgentId;
	}

	public void setReportAgentId(Boolean reportAgentId) {
		this.reportAgentId = reportAgentId;
	}

	public Boolean getReportGroupId() {
		return reportGroupId;
	}

	public void setReportGroupId(Boolean reportGroupId) {
		this.reportGroupId = reportGroupId;
	}

	public Boolean getReportMeasurementPoint() {
		return reportMeasurementPoint;
	}

	public void setReportMeasurementPoint(Boolean reportMeasurementPoint) {
		this.reportMeasurementPoint = reportMeasurementPoint;
	}

	public Integer getControllerTimeout() {
		return controllerTimeout;
	}

	public void setControllerTimeout(Integer controllerTimeout) {
		this.controllerTimeout = controllerTimeout;
	}

	public LocalDateTime getLastStarted() {
		return lastStarted;
	}

	public void setLastStarted(LocalDateTime lastStarted) {
		this.lastStarted = lastStarted;
	}
	
}
