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

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.common.LmapOptionDto;

/**
 * An Action describes a Task that is invoked by the Schedule. 
 * Multiple Actions are invoked according to the execution-mode of the Schedule.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "An Action describes a Task that is invoked by the Schedule. Multiple Actions are invoked according to the execution-mode of the Schedule.")
@JsonClassDescription("An Action describes a Task that is invoked by the Schedule. Multiple Actions are invoked according to the execution-mode of the Schedule.")
@JsonInclude(Include.NON_EMPTY)
public class LmapActionDto {
	
	/**
	 * The unique identifier for this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The unique identifier for this Action.")
	@JsonPropertyDescription("The unique identifier for this Action.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;

	/**
	 * The Task invoked by this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The Task invoked by this Action.")
	@JsonPropertyDescription("The Task invoked by this Action.")
	@Expose
	@SerializedName("task")
	@JsonProperty(required = true, value = "task")
	private String taskName;
	
	/**
	 * This container is a placeholder for runtime parameters
	 * defined in Task-specific data models augmenting the base LMAP report data model.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "This container is a placeholder for runtime parameters defined in Task-specific data models augmenting the base LMAP report data model.")
	@JsonPropertyDescription("This container is a placeholder for runtime parameters defined in Task-specific data models augmenting the base LMAP report data model.")
	@Expose
	@SerializedName("parameters")
	@JsonProperty(required = false, value = "parameters")
	private Object parameters;
	
	/**
	 * The list of Action-specific options that are appended to the list of Task-specific options.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The list of Action-specific options that are appended to the list of Task-specific options.")
	@JsonPropertyDescription("The list of Action-specific options that are appended to the list of Task-specific options.")
	@Expose
	@SerializedName("option")
	@JsonProperty(required = true, value = "option")
	private List<LmapOptionDto> optionList = new ArrayList<>(); // TODO: rename options
	
	/**
	 * A set of Schedules receiving the output produced by this Action. 
	 * The output is stored temporarily since the Destination Schedules will in general not be running when output is passed to them. 
	 * The behavior of an Action passing data to its own Schedule is implementation specific.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Schedules receiving the output produced by this Action. The output is stored temporarily since the Destination Schedules will in general not be running when output is passed to them. The behavior of an Action passing data to its own Schedule is implementation specific.")
	@JsonPropertyDescription("A set of Schedules receiving the output produced by this Action. The output is stored temporarily since the Destination Schedules will in general not be running when output is passed to them. The behavior of an Action passing data to its own Schedule is implementation specific.")
	@Expose
	@SerializedName("destination")
	@JsonProperty(required = true, value = "destination")
	private List<String> destinationList = new ArrayList<>(); // TODO: rename destinations
	
	/**
	 * A set of Action-specific tags that are reported together with the measurement results to a Collector.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Action-specific tags that are reported together with the measurement results to a Collector.")
	@JsonPropertyDescription("A set of Action-specific tags that are reported together with the measurement results to a Collector.")
	@Expose
	@SerializedName("tag")
	@JsonProperty(required = true, value = "tag")
	private List<String> tagList = new ArrayList<>(); // TODO: rename tags
	
	/**
	 * A set of Suppression tags that are used to select Actions to be suppressed.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Suppression tags that are used to select Actions to be suppressed.")
	@JsonPropertyDescription("A set of Suppression tags that are used to select Actions to be suppressed.")
	@Expose
	@SerializedName("suppression-tag")
	@JsonProperty(required = true, value = "suppression-tag")
	private List<String> suppressionTagList = new ArrayList<>(); // TODO: rename suppressionTags
	
	/**
	 * The current state of the Action (One of: enabled, disabled, running, suppressed).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The current state of the Action (One of: enabled, disabled, running, suppressed).")
	@JsonPropertyDescription("The current state of the Action (One of: enabled, disabled, running, suppressed).")
	@Expose
	@SerializedName("state")
	@JsonProperty(required = true, value = "state")
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
	@JsonProperty(required = true, value = "storage")
	private Long storage;

	/**
	 * Number of invocations of this Action. 
	 * This counter does not include suppressed invocations or invocations that were prevented 
	 * due to an overlap with a previous invocation of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of invocations of this Action. This counter does not include suppressed invocations or invocations that were prevented due to an overlap with a previous invocation of this Action.")
	@JsonPropertyDescription("Number of invocations of this Action. This counter does not include suppressed invocations or invocations that were prevented due to an overlap with a previous invocation of this Action.")
	@Expose
	@SerializedName("invocations")
	@JsonProperty(required = true, value = "invocations")
	private Integer invocations;

	/**
	 * Number of suppressed executions of this Action. 
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of suppressed executions of this Action.")
	@JsonPropertyDescription("Number of suppressed executions of this Action.")
	@Expose
	@SerializedName("suppressions")
	@JsonProperty(required = true, value = "suppressions")
	private Integer suppressions;
	
	/**
	 * Number of executions prevented due to overlaps with a previous invocation of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of executions prevented due to overlaps with a previous invocation of this Action.")
	@JsonPropertyDescription("Number of executions prevented due to overlaps with a previous invocation of this Action.")
	@Expose
	@SerializedName("overlaps")
	@JsonProperty(required = true, value = "overlaps")
	private Integer overlaps;
	
	/**
	 * Number of failed executions of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of failed executions of this Action.")
	@JsonPropertyDescription("Number of failed executions of this Action.")
	@Expose
	@SerializedName("failures")
	@JsonProperty(required = true, value = "failures")
	private Integer failures;
	
	/**
	 * The date and time of the last invocation of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The date and time of the last invocation of this Action.")
	@JsonPropertyDescription("The date and time of the last invocation of this Action.")
	@Expose
	@SerializedName("last-invocation")
	@JsonProperty(required = true, value = "last-invocation")
	private LocalDateTime lastInvocation;
	
	/**
	 * The date and time of the last completion of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The date and time of the last completion of this Action.")
	@JsonPropertyDescription("The date and time of the last completion of this Action.")
	@Expose
	@SerializedName("last-completion")
	@JsonProperty(required = true, value = "last-completion")
	private LocalDateTime lastCompletion;
	
	/**
	 * The status code returned by the last execution of this Action (with 0 indicating successful execution).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The status code returned by the last execution of this Action (with 0 indicating successful execution).")
	@JsonPropertyDescription("The status code returned by the last execution of this Action (with 0 indicating successful execution).")
	@Expose
	@SerializedName("last-status")
	@JsonProperty(required = true, value = "last-status")
	private Integer lastStatus;
	
	/**
	 * The status message produced by the last execution of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The status message produced by the last execution of this Action.")
	@JsonPropertyDescription("The status message produced by the last execution of this Action.")
	@Expose
	@SerializedName("last-message")
	@JsonProperty(required = true, value = "last-message")
	private String lastMessage;
	
	/**
	 * The date and time of the last failed completion of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The date and time of the last failed completion of this Action.")
	@JsonPropertyDescription("The date and time of the last failed completion of this Action.")
	@Expose
	@SerializedName("last-failed-completion")
	@JsonProperty(required = true, value = "last-failed-completion")
	private LocalDateTime lastFailedCompletion;
	
	/**
	 * The status code returned by the last failed execution of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The status code returned by the last failed execution of this Action.")
	@JsonPropertyDescription("The status code returned by the last failed execution of this Action.")
	@Expose
	@SerializedName("last-failed-status")
	@JsonProperty(required = true, value = "last-failed-status")
	private Integer lastFailedStatus;
	
	/**
	 * The status message produced by the last failed execution of this Action.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The status message produced by the last failed execution of this Action.")
	@JsonPropertyDescription("The status message produced by the last failed execution of this Action.")
	@Expose
	@SerializedName("last-failed-message")
	@JsonProperty(required = true, value = "last-failed-message")
	private String lastFailedMessage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Object getParameters() {
		return parameters;
	}

	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}

	public List<LmapOptionDto> getOptionList() {
		return optionList;
	}

	public void setOptionList(List<LmapOptionDto> optionList) {
		this.optionList = optionList;
	}

	public List<String> getDestinationList() {
		return destinationList;
	}

	public void setDestinationList(List<String> destinationList) {
		this.destinationList = destinationList;
	}

	public List<String> getTagList() {
		return tagList;
	}

	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}

	public List<String> getSuppressionTagList() {
		return suppressionTagList;
	}

	public void setSuppressionTagList(List<String> suppressionTagList) {
		this.suppressionTagList = suppressionTagList;
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

	public LocalDateTime getLastCompletion() {
		return lastCompletion;
	}

	public void setLastCompletion(LocalDateTime lastCompletion) {
		this.lastCompletion = lastCompletion;
	}

	public Integer getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(Integer lastStatus) {
		this.lastStatus = lastStatus;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public LocalDateTime getLastFailedCompletion() {
		return lastFailedCompletion;
	}

	public void setLastFailedCompletion(LocalDateTime lastFailedCompletion) {
		this.lastFailedCompletion = lastFailedCompletion;
	}

	public Integer getLastFailedStatus() {
		return lastFailedStatus;
	}

	public void setLastFailedStatus(Integer lastFailedStatus) {
		this.lastFailedStatus = lastFailedStatus;
	}

	public String getLastFailedMessage() {
		return lastFailedMessage;
	}

	public void setLastFailedMessage(String lastFailedMessage) {
		this.lastFailedMessage = lastFailedMessage;
	}
}






























