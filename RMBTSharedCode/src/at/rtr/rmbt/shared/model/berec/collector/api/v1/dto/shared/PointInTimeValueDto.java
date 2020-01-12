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
 * Holds a value from a point in time.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 * @param <T> Type of the value object
 */
@io.swagger.annotations.ApiModel(description = "Holds a measurement agent-side value from a point in time.")
@JsonClassDescription("Holds a measurement agent-side value from a point in time.")
public class PointInTimeValueDto<T> {

	/**
	 * The relative time in nanoseconds to the test start.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The relative time in nanoseconds to the measurement start.")
	@JsonPropertyDescription("The relative time in nanoseconds to the measurement start.")
	@Expose
	@SerializedName("relative_time_ns")
	@JsonProperty(required = true, value = "relative_time_ns")
	private Long relativeTimeNs;
	
	/**
	 * The value recorded at this point in time.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The value recorded at this point in time.")
	@JsonPropertyDescription("The value recorded at this point in time.")
	@Expose
	@SerializedName("value")
	@JsonProperty(required = true, value = "value")
	private T value;

	public Long getRelativeTimeNs() {
		return relativeTimeNs;
	}

	public void setRelativeTimeNs(Long relativeTimeNs) {
		this.relativeTimeNs = relativeTimeNs;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
