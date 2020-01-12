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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.common.LmapOptionDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.result.SubMeasurementResult;

/**
 * This module defines a data model for a single reporting result, which is a part of a Large-Scale Measurement Platform (LMAP).
 * 
 * @author alladin-IT GmbH (lbp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This module defines a data model for a single reporting result, which is a part of a Large-Scale Measurement Platform (LMAP).")
@JsonClassDescription("This module defines a data model for a single reporting result, which is a part of a Large-Scale Measurement Platform (LMAP).")
@JsonInclude(Include.NON_EMPTY)
public class LmapResultDto {
	
	/**
	 * The name of the Schedule that produced the result.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The name of the Schedule that produced the result.")
	@JsonPropertyDescription("The name of the Schedule that produced the result.")
	@Expose
	@SerializedName("schedule")
	@JsonProperty(value = "schedule")
	private String schedule;

	/**
	 * The name of the Action in the Schedule that produced the result.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The name of the Action in the Schedule that produced the result.")
	@JsonPropertyDescription("The name of the Action in the Schedule that produced the result.")
	@Expose
	@SerializedName("action")
	@JsonProperty(value = "action")
	private String action;

	/**
	 * The name of the Task that produced the result.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The name of the Task that produced the result.")
	@JsonPropertyDescription("The name of the Task that produced the result.")
	@Expose
	@SerializedName("task")
	@JsonProperty(value = "task")
	private String task;

	/**
	 * This container is a placeholder for runtime parameters defined in Task-specific data models augmenting the base LMAP report data model.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "This container is a placeholder for runtime parameters defined in Task-specific data models augmenting the base LMAP report data model.")
	@JsonPropertyDescription("This container is a placeholder for runtime parameters defined in Task-specific data models augmenting the base LMAP report data model.")
	@Expose
	@SerializedName("parameters")
	@JsonProperty(value = "parameters")
	private Object parameters;
	
	/**
	 * The list of options there were in use when the measurement was performed. 
	 * This list must include both the Task-specific options as well as the Action-specific options.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The list of options there were in use when the measurement was performed.  This list must include both the Task-specific options as well as the Action-specific options.")
	@JsonPropertyDescription("The list of options there were in use when the measurement was performed.  This list must include both the Task-specific options as well as the Action-specific options.")
	@Expose
	@SerializedName("option")
	@JsonProperty(required = true, value = "option")
	private List<LmapOptionDto> options = new ArrayList<>();

	/**
	 * A tag contains additional information that is passed with the result record to the Collector.
	 * This is the joined set of tags defined for the Task object, the Schedule object, and the Action object. 
	 * A tag can be used to carry the Measurement Cycle ID.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A tag contains additional information that is passed with the result record to the Collector.  This is the joined set of tags defined for the Task object, the Schedule object, and the Action object. A tag can be used to carry the Measurement Cycle ID.")
	@JsonPropertyDescription("A tag contains additional information that is passed with the result record to the Collector.  This is the joined set of tags defined for the Task object, the Schedule object, and the Action object. A tag can be used to carry the Measurement Cycle ID.")
	@Expose
	@SerializedName("tag")
	@JsonProperty(required = true, value = "tag")
	private List<String> tags = new ArrayList<>();

	/**
	 * The date and time of the event that triggered the Schedule of the Action that produced the reported result values. 
	 * The date and time does not include any added randomization.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The date and time of the event that triggered the Schedule of the Action that produced the reported result values.  The date and time does not include any added randomization.")
	@JsonPropertyDescription("The date and time of the event that triggered the Schedule of the Action that produced the reported result values.  The date and time does not include any added randomization.")
	@Expose
	@SerializedName("event")
	@JsonProperty(value = "event")
	private LocalDateTime event;
	
	/**
	 * The date and time when the Task producing this result started.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The date and time when the Task producing this result started.")
	@JsonPropertyDescription("The date and time when the Task producing this result started.")
	@Expose
	@SerializedName("start")
	@JsonProperty(required = true, value = "start")
	private LocalDateTime start;	
	
	/**
	 * The date and time when the Task producing this result finished.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The date and time when the Task producing this result finished.")
	@JsonPropertyDescription("The date and time when the Task producing this result finished.")
	@Expose
	@SerializedName("end")
	@JsonProperty(value = "end")
	private LocalDateTime end;
	
	/**
	 * The optional cycle number is the time closest to the time reported in the event leaf 
	 * that is a multiple of the cycle-interval of the event that triggered the execution of the Schedule.  
	 * The value is only present if the event that triggered the execution of the Schedule has a defined cycle-interval.
	 */
	@io.swagger.annotations.ApiModelProperty(value = "The optional cycle number is the time closest to the time reported in the event leaf that is a multiple of the cycle-interval of the event that triggered the execution of the Schedule.  The value is only present if the event that triggered the execution of the Schedule has a defined cycle-interval.")
	@JsonPropertyDescription("The optional cycle number is the time closest to the time reported in the event leaf that is a multiple of the cycle-interval of the event that triggered the execution of the Schedule.  The value is only present if the event that triggered the execution of the Schedule has a defined cycle-interval.")
	@Expose
	@SerializedName("cycle-number")
	@JsonProperty(value = "cycle-number")
	private String cycleNumber;
	
	/**
	 * The status code returned by the execution of this Action.
	 * 
	 * A status code returned by the execution of a Task.  Note
	 * that the actual range is implementation dependent, but it
	 * should be portable to use values in the range 0..127 for
	 * regular exit codes.  By convention, 0 indicates successful
	 * termination.  Negative values may be used to indicate
	 * abnormal termination due to a signal; the absolute value
	 * may identify the signal number in this case.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The status code returned by the execution of this Action.")
	@JsonPropertyDescription("The status code returned by the execution of this Action.")
	@Expose
	@SerializedName("status")
	@JsonProperty(required = true, value = "status")
	private Integer status;
	
	/**
	 * The names of Tasks overlapping with the execution of the Task that has produced this result.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The names of Tasks overlapping with the execution of the Task that has produced this result.")
	@JsonPropertyDescription("The names of Tasks overlapping with the execution of the Task that has produced this result.")
	@Expose
	@SerializedName("conflict")
	@JsonProperty(required = true, value = "conflict")
	private List<LmapConflictDto> conflict = new ArrayList<>();
	
	/**
	 * A list of results. Replaces the table list from LMAP
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A list of results. Replaces the table list from LMAP")
	@JsonPropertyDescription("A list of results. Replaces the table list from LMAP")
	@Expose
	@SerializedName("results")
	@JsonProperty(required = true, value = "results")
	private List<SubMeasurementResult> results = new ArrayList<>();

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public Object getParameters() {
		return parameters;
	}

	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}

	public List<LmapOptionDto> getOptions() {
		return options;
	}

	public void setOptions(List<LmapOptionDto> options) {
		this.options = options;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public LocalDateTime getEvent() {
		return event;
	}

	public void setEvent(LocalDateTime event) {
		this.event = event;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public String getCycleNumber() {
		return cycleNumber;
	}

	public void setCycleNumber(String cycleNumber) {
		this.cycleNumber = cycleNumber;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<LmapConflictDto> getConflict() {
		return conflict;
	}

	public void setConflict(List<LmapConflictDto> conflict) {
		this.conflict = conflict;
	}

	public List<SubMeasurementResult> getResults() {
		return results;
	}

	public void setResults(List<SubMeasurementResult> results) {
		this.results = results;
	}
}
