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

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Configuration of a particular Schedule.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "Configuration of a particular Schedule.")
@JsonClassDescription("Configuration of a particular Schedule.")
@JsonInclude(Include.NON_EMPTY)
public class LmapScheduleDto {
	
	/**
	 * The locally unique, administratively assigned name for this Schedule.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The locally unique, administratively assigned name for this Schedule.")
	@JsonPropertyDescription("The locally unique, administratively assigned name for this Schedule.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;
	
	/**
	 * The event source controlling the start of the scheduled Actions.
	 * Referencing the {@link LmapEventDto#getName()} of an Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The event source controlling the start of the scheduled Actions. Referencing the LmapEventDto.name of an Action.")
	@JsonPropertyDescription("The event source controlling the start of the scheduled Actions. Referencing the LmapEventDto.name of an Action.")
	@Expose
	@SerializedName("start")
	@JsonProperty(required = true, value = "start")
	private String start;
	
	/**
	 * This choice contains optional leafs that control the graceful forced termination of scheduled Actions. 
	 * When the end has been reached, the scheduled Actions should be forced to terminate the measurements. 
	 * This may involve being active some additional time in order to properly finish the Action's activity 
	 * (e.g., waiting for any messages that are still outstanding).
	 * If set to a {@link LmapStopDurationDto} it will behave like a typical timeout set for the execution of this schedule.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "This choice contains optional leafs that control the graceful forced termination of scheduled Actions. When the end has been reached, the scheduled Actions should be forced to terminate the measurements. This may involve being active some additional time in order to properly finish the Action's activity (e.g., waiting for any messages that are still outstanding). If set to a LmapStopDurationDto it will behave like a typical timeout set for the execution of this schedule.")
	@JsonPropertyDescription("This choice contains optional leafs that control the graceful forced termination of scheduled Actions. When the end has been reached, the scheduled Actions should be forced to terminate the measurements. This may involve being active some additional time in order to properly finish the Action's activity (e.g., waiting for any messages that are still outstanding). If set to a LmapStopDurationDto it will behave like a typical timeout set for the execution of this schedule.")
	@Expose
	@SerializedName("stop")
	@JsonProperty(required = false, value = "stop")
	private LmapStopDto stop;
	
	/**
	 * The execution mode of this Schedule determines in which order the Actions of the Schedule are executed.
	 * Supported values are: sequential, pipelined, parallel.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The execution mode of this Schedule determines in which order the Actions of the Schedule are executed. Supported values are: sequential, pipelined, parallel.")
	@JsonPropertyDescription("The execution mode of this Schedule determines in which order the Actions of the Schedule are executed. Supported values are: sequential, pipelined, parallel.")
	@Expose
	@SerializedName("execution-mode")
	@JsonProperty(required = false, value = "execution-mode")
	private ExecutionMode executionMode; // TODO: rename ...Dto
	
	/**
	 * A set of Schedule-specific tags that are reported together with the measurement results to a Collector.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Schedule-specific tags that are reported together with the measurement results to a Collector.")
	@JsonPropertyDescription("A set of Schedule-specific tags that are reported together with the measurement results to a Collector.")
	@Expose
	@SerializedName("tag")
	@JsonProperty(required = true, value = "tag")
	private List<String> tags = new ArrayList<>();
	
	/**
	 * A set of Suppression tags that are used to select Actions to be suppressed.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Suppression tags that are used to select Schedules to be suppressed.")
	@JsonPropertyDescription("A set of Suppression tags that are used to select Schedules to be suppressed.")
	@Expose
	@SerializedName("suppression-tag")
	@JsonProperty(required = false, value = "suppression-tag")
	private List<String> suppressionTags = new ArrayList<>();
	
	/**
	 * The current state of the Schedule (One of: enabled, disabled, running, suppressed). 
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The current state of the Schedule (One of: enabled, disabled, running, suppressed).")
	@JsonPropertyDescription("The current state of the Schedule (One of: enabled, disabled, running, suppressed).")
	@Expose
	@SerializedName("state")
	@JsonProperty(required = false, value = "state")
	private LmapStateDto state;
	
	/**
	 * The amount of secondary storage (e.g., allocated in a file system) 
	 * holding temporary data allocated to the Schedule in bytes. 
	 * This object reports the amount of allocated physical storage and not the storage used by logical data records.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The amount of secondary storage (e.g., allocated in a file system) holding temporary data allocated to the Schedule in bytes.  This object reports the amount of allocated physical storage and not the storage used by logical data records.")
	@JsonPropertyDescription("The amount of secondary storage (e.g., allocated in a file system) holding temporary data allocated to the Schedule in bytes.  This object reports the amount of allocated physical storage and not the storage used by logical data records.")
	@Expose
	@SerializedName("storage")
	@JsonProperty(required = false, value = "storage")
	private Long storage;
	
	/**
	 * Number of invocations of this Schedule. 
	 * This counter does not include suppressed invocations or invocations that were prevented 
	 * due to an overlap with a previous invocation of this Schedule.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of invocations of this Schedule. This counter does not include suppressed invocations or invocations that were prevented due to an overlap with a previous invocation of this Schedule.")
	@JsonPropertyDescription("Number of invocations of this Schedule. This counter does not include suppressed invocations or invocations that were prevented due to an overlap with a previous invocation of this Schedule.")
	@Expose
	@SerializedName("invocations")
	@JsonProperty(required = false, value = "invocations")
	private Integer invocations;

	/**
	 * Number of suppressed executions of this Schedule. 
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of suppressed executions of this Schedule.")
	@JsonPropertyDescription("Number of suppressed executions of this Schedule.")
	@Expose
	@SerializedName("suppressions")
	@JsonProperty(required = false, value = "suppressions")
	private Integer suppressions;
	
	/**
	 * Number of executions prevented due to overlaps with a previous invocation of this Schedule.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of executions prevented due to overlaps with a previous invocation of this Schedule.")
	@JsonPropertyDescription("Number of executions prevented due to overlaps with a previous invocation of this Schedule.")
	@Expose
	@SerializedName("overlaps")
	@JsonProperty(required = false, value = "overlaps")
	private Integer overlaps;
	
	/**
	 * Number of failed executions of this Schedule. A failed execution is an execution where at least one Action failed.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of failed executions of this Schedule. A failed execution is an execution where at least one Action failed.")
	@JsonPropertyDescription("Number of failed executions of this Schedule. A failed execution is an execution where at least one Action failed.")
	@Expose
	@SerializedName("failures")
	@JsonProperty(required = false, value = "failures")
	private Integer failures;
	
	/**
	 * The date and time of the last invocation of this Schedule.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The date and time of the last invocation of this Schedule.")
	@JsonPropertyDescription("The date and time of the last invocation of this Schedule.")
	@Expose
	@SerializedName("last-invocation")
	@JsonProperty(required = false, value = "last-invocation")
	private LocalDateTime lastInvocation;
	
	/**
	 * An Action describes a Task that is invoked by the Schedule.
	 * Multiple Actions are invoked according to the execution-mode of the Schedule.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "An Action describes a Task that is invoked by the Schedule. Multiple Actions are invoked according to the execution-mode of the Schedule.")
	@JsonPropertyDescription("An Action describes a Task that is invoked by the Schedule. Multiple Actions are invoked according to the execution-mode of the Schedule.")
	@Expose
	@SerializedName("action")
	@JsonProperty(required = true, value = "action")
	private List<LmapActionDto> actions = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public LmapStopDto getStop() {
		return stop;
	}

	public void setStop(LmapStopDto stop) {
		this.stop = stop;
	}

	public ExecutionMode getExecutionMode() {
		return executionMode;
	}

	public void setExecutionMode(ExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getSuppressionTags() {
		return suppressionTags;
	}

	public void setSuppressionTags(List<String> suppressionTags) {
		this.suppressionTags = suppressionTags;
	}

	public LmapStateDto getState() {
		return state;
	}

	public void setState(LmapStateDto state) {
		this.state = state;
	}

	public Long getStorage() {
		return storage;
	}

	public void setStorage(Long storage) {
		this.storage = storage;
	}

	public Integer getInvocations() {
		return invocations;
	}

	public void setInvocations(Integer invocations) {
		this.invocations = invocations;
	}

	public Integer getSuppressions() {
		return suppressions;
	}

	public void setSuppressions(Integer suppressions) {
		this.suppressions = suppressions;
	}

	public Integer getOverlaps() {
		return overlaps;
	}

	public void setOverlaps(Integer overlaps) {
		this.overlaps = overlaps;
	}

	public Integer getFailures() {
		return failures;
	}

	public void setFailures(Integer failures) {
		this.failures = failures;
	}

	public LocalDateTime getLastInvocation() {
		return lastInvocation;
	}

	public void setLastInvocation(LocalDateTime lastInvocation) {
		this.lastInvocation = lastInvocation;
	}

	public List<LmapActionDto> getActions() {
		return actions;
	}

	public void setActions(List<LmapActionDto> actions) {
		this.actions = actions;
	}

	/**
	 * 
	 * TODO
	 *
	 */
	public static enum ExecutionMode {
		SEQUENTIAL,	// The Actions of the Schedule are executed sequentially.
		PARALLEL, // The Actions of the Schedule are executed concurrently.
		PIPELINED, // The Actions of the Schedule are executed in a pipelined mode. Output created by an Action is passed as input to the subsequent Action.
	}
}






























