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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.ApiRequestInfo;

/**
 * Configuration and control of a Measurement Agent.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "Configuration and control of a Measurement Agent.")
@JsonClassDescription("Configuration and control of a Measurement Agent.")
@JsonInclude(Include.NON_EMPTY)
public class LmapControlDto {

	/**
	 * Agent capabilities including a list of supported Tasks.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Agent capabilities including a list of supported Tasks.")
	@JsonPropertyDescription("Agent capabilities including a list of supported Tasks.")
	@Expose
	@SerializedName("capabilities")
	@JsonProperty(required = true, value = "capabilities")
	private LmapCapabilityDto capabilities;

	/**
	 * Configuration of parameters affecting the whole Measurement Agent.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Configuration of parameters affecting the whole Measurement Agent.")
	@JsonPropertyDescription("Configuration of parameters affecting the whole Measurement Agent.")
	@Expose
	@SerializedName("agent")
	@JsonProperty(required = true, value = "agent")
	private LmapAgentDto agent;
	
	/**
	 * Configuration of LMAP Tasks.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Configuration of LMAP Tasks.")
	@JsonPropertyDescription("Configuration of LMAP Tasks.")
	@Expose
	@SerializedName("tasks")
	@JsonProperty(required = true, value = "tasks")
	private List<LmapTaskDto> tasks = new ArrayList<>();
	
	/**
	 * Configuration of LMAP Schedules. Schedules control which Tasks are executed by the LMAP implementation.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Configuration of LMAP Schedules. Schedules control which Tasks are executed by the LMAP implementation.")
	@JsonPropertyDescription("Configuration of LMAP Schedules. Schedules control which Tasks are executed by the LMAP implementation.")
	@Expose
	@SerializedName("schedules")
	@JsonProperty(required = true, value = "schedules")
	private List<LmapScheduleDto> schedules = new ArrayList<>();
	
	/**
	 * Suppression information to prevent Schedules or certain Actions from starting.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Suppression information to prevent Schedules or certain Actions from starting.")
	@JsonPropertyDescription("Suppression information to prevent Schedules or certain Actions from starting.")
	@Expose
	@SerializedName("suppressions")
	@JsonProperty(required = true, value = "suppressions")
	private List<LmapSuppressionDto> suppressions = new ArrayList<>();
	
	/**
	 * Configuration of LMAP events. 
	 * Implementations may be forced to delay acting upon the occurrence of events in the face of local constraints.
	 * An Action triggered by an event therefore should not rely on the accuracy provided by the scheduler implementation.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Configuration of LMAP events. Implementations may be forced to delay acting upon the occurrence of events in the face of local constraints. An Action triggered by an event therefore should not rely on the accuracy provided by the scheduler implementation.")
	@JsonPropertyDescription("Configuration of LMAP events. Implementations may be forced to delay acting upon the occurrence of events in the face of local constraints. An Action triggered by an event therefore should not rely on the accuracy provided by the scheduler implementation.")
	@Expose
	@SerializedName("events")
	@JsonProperty(required = true, value = "events")
	private List<LmapEventDto> events = new ArrayList<>();
	
	/**
	 * Additional information that is sent by agent alongside the request.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Additional information that is sent by agent alongside the request.")
	@JsonPropertyDescription("Additional information that is sent by agent alongside the request.")
	@Expose
	@SerializedName("additional-request-info")
	@JsonProperty(required = true, value = "additional-request-info")
	private ApiRequestInfo additionalRequestInfo;

	public LmapCapabilityDto getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(LmapCapabilityDto capabilities) {
		this.capabilities = capabilities;
	}

	public LmapAgentDto getAgent() {
		return agent;
	}

	public void setAgent(LmapAgentDto agent) {
		this.agent = agent;
	}

	public List<LmapScheduleDto> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<LmapScheduleDto> schedules) {
		this.schedules = schedules;
	}

	public List<LmapSuppressionDto> getSuppressions() {
		return suppressions;
	}

	public void setSuppressions(List<LmapSuppressionDto> suppressions) {
		this.suppressions = suppressions;
	}

	public List<LmapEventDto> getEvents() {
		return events;
	}

	public void setEvents(List<LmapEventDto> events) {
		this.events = events;
	}

	public ApiRequestInfo getAdditionalRequestInfo() {
		return additionalRequestInfo;
	}

	public void setAdditionalRequestInfo(ApiRequestInfo additionalRequestInfo) {
		this.additionalRequestInfo = additionalRequestInfo;
	}

	public List<LmapTaskDto> getTasks() {
		return tasks;
	}

	public void setTasks(List<LmapTaskDto> tasks) {
		this.tasks = tasks;
	}
}
