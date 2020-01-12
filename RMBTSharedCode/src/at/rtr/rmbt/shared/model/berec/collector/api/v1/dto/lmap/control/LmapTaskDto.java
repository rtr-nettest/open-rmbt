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
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.common.LmapOptionDto;

/**
 * Configuration of LMAP Tasks.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "Configuration of LMAP Tasks.")
@JsonClassDescription("Configuration of LMAP Tasks.")
@JsonInclude(Include.NON_EMPTY)
public class LmapTaskDto {
	
	public final static String MEASUREMENT_PARAMETER_QOS = "parameters_qos";
	
	public final static String MEASUREMENT_PARAMETER_SPEED = "parameters_speed";
	
	public final static String QOS_NAME = "QOS";
	
	public final static String CONCURRENCY_GROUP = "concurrency_group";
	
	public final static String QOS_TEST_UID = "qos_test_uid";
	
	public final static String QOS_TEST_TYPE = "qostest";
	
	public final static String SERVER_ADDRESS = "server_addr";
	
	public final static String SERVER_ADDRESS_IPV6 = "server_addr_ipv6";
	
	public final static String SERVER_PORT = "server_port";
	
	public final static String ENCRYPTION = "encryption";
	
	public final static String RESULT_COLLECTOR_URL = "result_collector_base_url";

	public final static String SERVER_ADDRESS_DEFAULT = "server_addr_default"; // should be only "server_addr"
	
	/**
	 * The unique name of a Task.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The unique name of a Task.")
	@JsonPropertyDescription("The unique name of a Task.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;
	
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
	 * The list of Task-specific options.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The list of Task-specific options.")
	@JsonPropertyDescription("The list of Task-specific options.")
	@Expose
	@SerializedName("option")
	@JsonProperty(required = true, value = "option")
	private List<LmapOptionDto> options = new ArrayList<>();
	
	/**
	 * A set of Task-specific tags that are reported together with the measurement results to a Collector. 
	 * A tag can be used, for example, to carry the Measurement Cycle ID.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A set of Task-specific tags that are reported together with the measurement results to a Collector. A tag can be used, for example, to carry the Measurement Cycle ID.")
	@JsonPropertyDescription("A set of Task-specific tags that are reported together with the measurement results to a Collector. A tag can be used, for example, to carry the Measurement Cycle ID.")
	@Expose
	@SerializedName("tag")
	@JsonProperty(required = true, value = "tag")
	private List<String> tagList = new ArrayList<>(); // TODO: rename tags

	public String getName() {
		return name;
	}

	public void setName(String taskName) {
		this.name = taskName;
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

	public List<LmapOptionDto> getOptions() {
		return options;
	}

	public void setOptions(List<LmapOptionDto> options) {
		this.options = options;
	}

	public List<String> getTagList() {
		return tagList;
	}

	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}
}
