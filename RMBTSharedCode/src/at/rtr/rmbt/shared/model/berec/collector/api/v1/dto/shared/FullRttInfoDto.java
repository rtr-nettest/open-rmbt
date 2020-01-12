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
 * Other than @see RttInfoDto, this class contains additional values which were calculated by the server.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Other than RttInfoDto, this class contains additional values which were calculated by the server.")
@JsonClassDescription("Other than RttInfoDto, this class contains additional values which were calculated by the server.")
public class FullRttInfoDto extends RttInfoDto {

	/**
	 * Minimum (best) RTT value in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Minimum (best) RTT value in nanoseconds.")
	@JsonPropertyDescription("Minimum (best) RTT value in nanoseconds.")
	@Expose
	@SerializedName("min_ns")
	@JsonProperty(required = true, value = "min_ns")
	private Long minNs;
	
	/**
	 * Common logarithm of the minimum (best) RTT.
	 */
	@io.swagger.annotations.ApiModelProperty("Common logarithm of the minimum (best) RTT.")
	@JsonPropertyDescription("Common logarithm of the minimum (best) RTT.")
	@Expose
	@SerializedName("min_log")
	@JsonProperty("min_log")
	private Double minLog;
	
	/**
	 * Maximum (worst) RTT value in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Maximum (worst) RTT value in nanoseconds.")
	@JsonPropertyDescription("Maximum (worst) RTT value in nanoseconds.")
	@Expose
	@SerializedName("max_ns")
	@JsonProperty(required = true, value = "max_ns")
	private Long maxNs;
	
	/**
	 * Common logarithm of the maximum (worst) RTT.
	 */
	@io.swagger.annotations.ApiModelProperty("Common logarithm of the maximum (worst) RTT.")
	@JsonPropertyDescription("Common logarithm of the maximum (worst) RTT.")
	@Expose
	@SerializedName("max_log")
	@JsonProperty("max_log")
	private Double maxLog;
	
	/**
	 * Average RTT value in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Average RTT value in nanoseconds.")
	@JsonPropertyDescription("Average RTT value in nanoseconds.")
	@Expose
	@SerializedName("average_ns")
	@JsonProperty(required = true, value = "average_ns")
	private Long averageNs;
	
	/**
	 * Common logarithm of the average RTT.
	 */
	@io.swagger.annotations.ApiModelProperty("Common logarithm of the average RTT.")
	@JsonPropertyDescription("Common logarithm of the average RTT.")
	@Expose
	@SerializedName("average_log")
	@JsonProperty("average_log")
	private Double averageLog;
	
	/**
	 * Median RTT value in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Median RTT value in nanoseconds.")
	@JsonPropertyDescription("Median RTT value in nanoseconds.")
	@Expose
	@SerializedName("median_ns")
	@JsonProperty(required = true, value = "median_ns")
	private Long medianNs;
	
	/**
	 * Common logarithm of the median RTT.
	 */
	@io.swagger.annotations.ApiModelProperty("Common logarithm of the median RTT.")
	@JsonPropertyDescription("Common logarithm of the median RTT.")
	@Expose
	@SerializedName("median_log")
	@JsonProperty("median_log")
	private Double medianLog;
	
	/**
	 * Calculated RTT variance.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Calculated RTT variance.")
	@JsonPropertyDescription("Calculated RTT variance.")
	@Expose
	@SerializedName("variance")
	@JsonProperty(required = true, value = "variance")
	private Long variance;
	
	/**
	 * Calculated RTT standard deviation in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Calculated RTT standard deviation in nanoseconds.")
	@JsonPropertyDescription("Calculated RTT standard deviation in nanoseconds.")
	@Expose
	@SerializedName("standard_deviation_ns")
	@JsonProperty(required = true, value = "standard_deviation_ns")
	private Long standardDeviationNs;

	public Long getMinNs() {
		return minNs;
	}

	public void setMinNs(Long minNs) {
		this.minNs = minNs;
	}

	public Double getMinLog() {
		return minLog;
	}

	public void setMinLog(Double minLog) {
		this.minLog = minLog;
	}

	public Long getMaxNs() {
		return maxNs;
	}

	public void setMaxNs(Long maxNs) {
		this.maxNs = maxNs;
	}

	public Double getMaxLog() {
		return maxLog;
	}

	public void setMaxLog(Double maxLog) {
		this.maxLog = maxLog;
	}

	public Long getAverageNs() {
		return averageNs;
	}

	public void setAverageNs(Long averageNs) {
		this.averageNs = averageNs;
	}

	public Double getAverageLog() {
		return averageLog;
	}

	public void setAverageLog(Double averageLog) {
		this.averageLog = averageLog;
	}

	public Long getMedianNs() {
		return medianNs;
	}

	public void setMedianNs(Long medianNs) {
		this.medianNs = medianNs;
	}

	public Double getMedianLog() {
		return medianLog;
	}

	public void setMedianLog(Double medianLog) {
		this.medianLog = medianLog;
	}

	public Long getVariance() {
		return variance;
	}

	public void setVariance(Long variance) {
		this.variance = variance;
	}

	public Long getStandardDeviationNs() {
		return standardDeviationNs;
	}

	public void setStandardDeviationNs(Long standardDeviationNs) {
		this.standardDeviationNs = standardDeviationNs;
	}
}
