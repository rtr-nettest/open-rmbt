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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Suppression information to prevent Schedules or certain Actions from starting.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "Suppression information to prevent Schedules or certain Actions from starting.")
@JsonClassDescription("Suppression information to prevent Schedules or certain Actions from starting.")
@JsonInclude(Include.NON_EMPTY)
public class LmapSuppressionDto {
	
	/**
	 * The locally unique, administratively assigned name for this Suppression.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The locally unique, administratively assigned name for this Suppression.")
	@JsonPropertyDescription("The locally unique, administratively assigned name for this Suppression.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;
	
	/**
	 * The event source controlling the start of the Suppression period.
	 * Referencing the {@link LmapEventDto#getName()} of an Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The event source controlling the start of the Suppression period. Referencing the LmapEventDto.name of an Action.")
	@JsonPropertyDescription("The event source controlling the start of the Suppression period. Referencing the LmapEventDto.name of an Action.")
	@Expose
	@SerializedName("start")
	@JsonProperty(required = false, value = "start")
	private Integer startEvent;
	
	/**
	 * The event source controlling the end of the Suppression period.
	 * If not present, Suppression continues indefinitely.
	 * Referencing the {@link LmapEventDto#getName()} of an Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The event source controlling the end of the Suppression period. If not present, Suppression continues indefinitely. Referencing the LmapEventDto.name of an Action.")
	@JsonPropertyDescription("The event source controlling the end of the Suppression period. If not present, Suppression continues indefinitely. Referencing the LmapEventDto.name of an Action.")
	@Expose
	@SerializedName("end")
	@JsonProperty(required = false, value = "end")
	private Integer endEvent;
	
	/**
	 * A set of Suppression match patterns. 
	 * The Suppression will apply to all Schedules (and their Actions)
	 * that have a matching value in their suppression-tags 
	 * and to all Actions that have a matching value in their suppression-tags.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Suppression match patterns. The Suppression will apply to all Schedules (and their Actions) that have a matching value in their suppression-tags and to all Actions that have a matching value in their suppression-tags.")
	@JsonPropertyDescription("A set of Suppression match patterns. The Suppression will apply to all Schedules (and their Actions) that have a matching value in their suppression-tags and to all Actions that have a matching value in their suppression-tags.")
	@Expose
	@SerializedName("match")
	@JsonProperty(required = true, value = "match")
	private List<String> matchList; // TODO: rename matches
	
	/**
	 * If 'stop-running' is true, running Schedules and Actions 
	 * matching the Suppression will be terminated when Suppression is activated. 
	 * If 'stop-running' is false, running Schedules and Actions will not be affected if Suppression is activated.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "If 'stop-running' is true, running Schedules and Actions matching the Suppression will be terminated when Suppression is activated. If 'stop-running' is false, running Schedules and Actions will not be affected if Suppression is activated.")
	@JsonPropertyDescription("If 'stop-running' is true, running Schedules and Actions matching the Suppression will be terminated when Suppression is activated. If 'stop-running' is false, running Schedules and Actions will not be affected if Suppression is activated.")
	@Expose
	@SerializedName("stop-running")
	@JsonProperty(required = false, value = "stop-running")
	private Boolean stopRunning;
	/**
	 * The current state of the Suppression.
	 * Possible values are: enabled, disabled, active.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The current state of the Suppression.")
	@JsonPropertyDescription("The current state of the Suppression.")
	@Expose
	@SerializedName("state")
	@JsonProperty(required = true, value = "state")
	private SuppressionState state; // TODO: rename ...Dto

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Integer getStartEvent() {
		return startEvent;
	}

	public void setStartEvent(Integer startEvent) {
		this.startEvent = startEvent;
	}

	public Integer getEndEvent() {
		return endEvent;
	}

	public void setEndEvent(Integer endEvent) {
		this.endEvent = endEvent;
	}

	public List<String> getMatchList() {
		return matchList;
	}

	public void setMatchList(List<String> matchList) {
		this.matchList = matchList;
	}

	public Boolean getStopRunning() {
		return stopRunning;
	}

	public void setStopRunning(Boolean stopRunning) {
		this.stopRunning = stopRunning;
	}

	public SuppressionState getState() {
		return state;
	}

	public void setState(SuppressionState state) {
		this.state = state;
	}

	/**
	 * 
	 * TODO
	 *
	 */
	public enum SuppressionState {
		ENABLED,
		DISABLED,
		ACTIVE
	}
}
