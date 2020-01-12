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

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.common.LmapFunctionDto;

/**
 * A list of Tasks that the Measurement Agent supports.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "A list of Tasks that the Measurement Agent supports.")
@JsonClassDescription("A list of Tasks that the Measurement Agent supports.")
@JsonInclude(Include.NON_EMPTY)
public class LmapCapabilityTaskDto {
	
	/**
	 * The unique name of a Task capability.
	 * Refers to the {@link LmapTaskDto#getName()} and needs be the exact same in order to match.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The unique name of a Task capability. Refers to the LmapTaskDto.name and needs be the exact same in order to match.")
	@JsonPropertyDescription("The unique name of a Task capability. Refers to the LmapTaskDto.name and needs be the exact same in order to match.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String taskName;
	
	/**
	 * A list of entries in a registry identifying functions.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A list of entries in a registry identifying functions.")
	@JsonPropertyDescription("A list of entries in a registry identifying functions.")
	@Expose
	@SerializedName("function")
	@JsonProperty(required = true, value = "function")
	private List<LmapFunctionDto> functions = new ArrayList<>();
	
	/**
	 * The (local) program to invoke in order to execute the Task. 
	 * If this leaf is not set, then the system will try to identify a suitable program based on the registry information present.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The (local) program to invoke in order to execute the Task. If this leaf is not set, then the system will try to identify a suitable program based on the registry information present.")
	@JsonPropertyDescription("The (local) program to invoke in order to execute the Task. If this leaf is not set, then the system will try to identify a suitable program based on the registry information present.")
	@Expose
	@SerializedName("program")
	@JsonProperty(required = false, value = "program")
	private String program;
	
	/**
	 * A short description of the software implementing the Task.
	 * This should include the version number of the Measurement Task software.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "A short description of the software implementing the Task. This should include the version number of the Measurement Task software.")
	@JsonPropertyDescription("A short description of the software implementing the Task. This should include the version number of the Measurement Task software.")
	@Expose
	@SerializedName("version")
	@JsonProperty(required = false, value = "version")
	private String version;

	/**
	 * The measurement peer identifier the agent wishes to measure against for this task.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The measurement peer identifier the agent wishes to measure against for this task.")
	@JsonPropertyDescription("The measurement peer identifier the agent wishes to measure against for this task.")
	@Expose
	@SerializedName("selected_measurement_peer_identifier")
	@JsonProperty(required = true, value = "selected_measurement_peer_identifier")
	private String selectedMeasurementPeerIdentifier;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public List<LmapFunctionDto> getFunctions() {
		return functions;
	}

	public void setFunctions(List<LmapFunctionDto> functions) {
		this.functions = functions;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSelectedMeasurementPeerIdentifier() {
		return selectedMeasurementPeerIdentifier;
	}

	public void setSelectedMeasurementPeerIdentifier(String selectedMeasurementPeerIdentifier) {
		this.selectedMeasurementPeerIdentifier = selectedMeasurementPeerIdentifier;
	}
}
