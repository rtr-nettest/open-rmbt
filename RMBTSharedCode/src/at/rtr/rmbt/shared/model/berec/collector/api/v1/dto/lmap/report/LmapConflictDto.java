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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The names of Tasks overlapping with the execution of the Task that has produced this result.
 * @author lb
 *
 */
@io.swagger.annotations.ApiModel(description = "The names of Tasks overlapping with the execution of the Task that has produced this result.")
@JsonClassDescription("The names of Tasks overlapping with the execution of the Task that has produced this result.")
@JsonInclude(Include.NON_EMPTY)
public class LmapConflictDto {

	/**
	 * The names of Tasks overlapping with the execution of the Task that has produced this result.
	 * 
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The names of Tasks overlapping with the execution of the Task that has produced this result.")
	@JsonPropertyDescription("The names of Tasks overlapping with the execution of the Task that has produced this result.")
	@Expose
	@SerializedName("schedule-name")
	@JsonProperty(required = false, value = "schedule-name")	
	private String scheduleName;
	
	/**
	 * The name of an Action within the Schedule that might have impacted the execution of the Task 
	 * that has produced this result.
	 * 
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The name of an Action within the Schedule that might have impacted the execution of the Task that has produced this result.")
	@JsonPropertyDescription("The name of an Action within the Schedule that might have impacted the execution of the Task that has produced this result.")
	@Expose
	@SerializedName("action-name")
	@JsonProperty(required = false, value = "action-name")	
	private String actionName;
	
	/**
	 * The name of the Task executed by an Action within the Schedule that might have impacted 
	 * the execution of the Task that has produced this result.
	 * 
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The name of the Task executed by an Action within the Schedule that might have impacted the execution of the Task that has produced this result.")
	@JsonPropertyDescription("The name of the Task executed by an Action within the Schedule that might have impacted the execution of the Task that has produced this result.")
	@Expose
	@SerializedName("task-name")
	@JsonProperty(required = false, value = "task-name")	
	private String taskName;

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
