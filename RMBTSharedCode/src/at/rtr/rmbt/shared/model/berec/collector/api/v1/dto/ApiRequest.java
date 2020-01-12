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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Object that is used as wrapper for every request.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Object that is used as wrapper for every request.")
@JsonClassDescription("Object that is used as wrapper for every request.")
public class ApiRequest<T> extends ApiBase<T> {
	
	/**
	 * @see ApiRequestInfo
	 */
	@io.swagger.annotations.ApiModelProperty("Additional information that is sent by measurement agent alongside the request.")
	@JsonPropertyDescription("Additional information that is sent by measurement agent alongside the request.")
	@Expose
	@SerializedName("request_info")
	@JsonProperty("request_info")
	private ApiRequestInfo requestInfo;
	
	/**
	 * 
	 */
	public ApiRequest() {
		super();
	}
	
	/**
	 * 
	 * @param data
	 */
	public ApiRequest(T data) {
		super(data);
	}
	
	/**
	 * 
	 * @param data
	 * @param errors
	 */
	public ApiRequest(T data, ApiRequestInfo requestInfo) {
		super(data);
		
		this.requestInfo = requestInfo;
	}
	
	/**
	 * 
	 * @return
	 */
	public ApiRequestInfo getRequestInfo() {
		return requestInfo;
	}
	
	/**
	 * 
	 * @param requestInfo
	 */
	public void setRequestInfo(ApiRequestInfo requestInfo) {
		this.requestInfo = requestInfo;
	}
}
