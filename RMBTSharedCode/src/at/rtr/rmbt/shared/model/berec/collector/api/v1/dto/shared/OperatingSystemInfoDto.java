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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains information about the measurement agent's OS.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains information about the measurement agent's OS.")
@JsonClassDescription("Contains information about the measurement agent's OS.")
public class OperatingSystemInfoDto {

	/**
	 * Operating system name (e.g. Android or iOS).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Operating system name (e.g. Android or iOS).")
	@JsonPropertyDescription("Operating system name (e.g. Android or iOS).")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;
	
	/**
	 * Operating system version.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Operating system version.")
	@JsonPropertyDescription("Operating system version.")
	@Expose
	@SerializedName("version")
	@JsonProperty(required = true, value = "version")
	private String version;
	
	/**
	 * API level of operating system or SDK (e.g. Android API level or Swift SDK version).
	 */
	@io.swagger.annotations.ApiModelProperty("API level of operating system or SDK (e.g. Android API level or Swift SDK version).")
	@JsonPropertyDescription("API level of operating system or SDK (e.g. Android API level or Swift SDK version).")
	@Expose
	@SerializedName("api_level")
	@JsonProperty("api_level")
	private String apiLevel;
	
	/**
	 * Minimum CPU usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Minimum CPU usage, as double between 0 - 100.")
	@JsonPropertyDescription("Minimum CPU usage, as double between 0 - 100.")
	@Expose
	@SerializedName("cpu_min")
	@JsonProperty("cpu_min")
	private Double cpuMin;
	
	/**
	 * Maximum CPU usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Maximum CPU usage, as double between 0 - 100.")
	@JsonPropertyDescription("Maximum CPU usage, as double between 0 - 100.")
	@Expose
	@SerializedName("cpu_max")
	@JsonProperty("cpu_max")
	private Double cpuMax;
	
	/**
	 * Average CPU usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Average CPU usage, as double between 0 - 100.")
	@JsonPropertyDescription("Average CPU usage, as double between 0 - 100.")
	@Expose
	@SerializedName("cpu_average")
	@JsonProperty("cpu_average")
	private Double cpuAverage;
	
	/**
	 * Median CPU usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Median CPU usage, as double between 0 - 100.")
	@JsonPropertyDescription("Median CPU usage, as double between 0 - 100.")
	@Expose
	@SerializedName("cpu_median")
	@JsonProperty("cpu_median")
	private Double cpuMedian;
	
	/**
	 * Minimum Memory usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Minimum Memory usage, as double between 0 - 100.")
	@JsonPropertyDescription("Minimum Memory usage, as double between 0 - 100.")
	@Expose
	@SerializedName("memory_min")
	@JsonProperty("memory_min")
	private Double memoryMin;
	
	/**
	 * Maximum Memory usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Maximum Memory usage, as double between 0 - 100.")
	@JsonPropertyDescription("Maximum Memory usage, as double between 0 - 100.")
	@Expose
	@SerializedName("memory_max")
	@JsonProperty("memory_max")
	private Double memoryMax;
	
	/**
	 * Average Memory usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Average Memory usage, as double between 0 - 100.")
	@JsonPropertyDescription("Average Memory usage, as double between 0 - 100.")
	@Expose
	@SerializedName("memory_average")
	@JsonProperty("memory_average")
	private Double memoryAverage;
	
	/**
	 * Median Memory usage, as double between 0 - 100.
	 */
	@io.swagger.annotations.ApiModelProperty("Median Memory usage, as double between 0 - 100.")
	@JsonPropertyDescription("Median Memory usage, as double between 0 - 100.")
	@Expose
	@SerializedName("memory_median")
	@JsonProperty("memory_median")
	private Double memoryMedian;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getApiLevel() {
		return apiLevel;
	}

	public void setApiLevel(String apiLevel) {
		this.apiLevel = apiLevel;
	}

	public Double getCpuMin() {
		return cpuMin;
	}

	public void setCpuMin(Double cpuMin) {
		this.cpuMin = cpuMin;
	}

	public Double getCpuMax() {
		return cpuMax;
	}

	public void setCpuMax(Double cpuMax) {
		this.cpuMax = cpuMax;
	}

	public Double getCpuAverage() {
		return cpuAverage;
	}

	public void setCpuAverage(Double cpuAverage) {
		this.cpuAverage = cpuAverage;
	}

	public Double getCpuMedian() {
		return cpuMedian;
	}

	public void setCpuMedian(Double cpuMedian) {
		this.cpuMedian = cpuMedian;
	}

	public Double getMemoryMin() {
		return memoryMin;
	}

	public void setMemoryMin(Double memoryMin) {
		this.memoryMin = memoryMin;
	}

	public Double getMemoryMax() {
		return memoryMax;
	}

	public void setMemoryMax(Double memoryMax) {
		this.memoryMax = memoryMax;
	}

	public Double getMemoryAverage() {
		return memoryAverage;
	}

	public void setMemoryAverage(Double memoryAverage) {
		this.memoryAverage = memoryAverage;
	}

	public Double getMemoryMedian() {
		return memoryMedian;
	}

	public void setMemoryMedian(Double memoryMedian) {
		this.memoryMedian = memoryMedian;
	}
}
