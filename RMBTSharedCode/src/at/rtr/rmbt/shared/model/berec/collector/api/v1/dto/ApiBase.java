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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Abstract wrapper for every request and response.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Abstract wrapper for every request and response.")
@JsonClassDescription("Abstract wrapper for every request and response.")
public abstract class ApiBase<T> {

	/**
	 * Actual data that is returned for the request/response.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Actual data that is returned for the request/response.")
	@JsonPropertyDescription("Actual data that is returned for the request/response.")
	@Expose
	@SerializedName("data")
	@JsonProperty(required = true, value = "data")
	@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="deserialize_type")
	private T data;
	
	/**
	 * 
	 */
	public ApiBase() {
		
	}
	
	/**
	 * 
	 * @param data
	 */
	public ApiBase(T data) {
		this.data = data;
	}
	
	/**
	 * 
	 * @return
	 */
	public T getData() {
		return data;
	}
	
	/**
	 * 
	 * @param data
	 */
	public void setData(T data) {
		this.data = data;
	}
}
