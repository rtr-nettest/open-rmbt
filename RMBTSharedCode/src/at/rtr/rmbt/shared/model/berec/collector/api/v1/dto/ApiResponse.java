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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Object that is used as wrapper for every response.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Object that is used as wrapper for every response.")
@JsonClassDescription("Object that is used as wrapper for every response.")
public class ApiResponse<T> extends ApiBase<T> {

	/**
	 * Optional list of errors that occurred during request processing.
	 */
	@io.swagger.annotations.ApiModelProperty("Optional list of errors that occurred during request processing.")
	@JsonPropertyDescription("Optional list of errors that occurred during request processing.")
	@Expose
	@SerializedName("errors")
	@JsonProperty("errors")
	private final List<ApiError> errors;

	/**
	 *
	 * @param data
	 */
	public ApiResponse(T data) {
		this(data, null);
	}

	/**
	 *
	 * @param data
	 * @param errors
	 */
	@JsonCreator
	public ApiResponse(@JsonProperty("data") T data, @JsonProperty("errors") List<ApiError> errors) {
		super(data);

		this.errors = errors != null ? new ArrayList<>(errors) : null;
	}

	/**
	 *
	 * @return
	 */
	public List<ApiError> getErrors() {
		return errors;
	}

	@Override
	public String toString() {
		return "ApiResponse{" +
				"errors=" + errors +
				"} " + super.toString();
	}
}
