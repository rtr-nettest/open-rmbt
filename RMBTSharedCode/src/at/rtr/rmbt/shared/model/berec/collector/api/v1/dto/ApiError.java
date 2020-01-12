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

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DTO that wraps server errors and/or exceptions.
 * The Java stack-trace is only added if the service runs in development mode.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "DTO that wraps server errors and/or exceptions. The Java stack-trace is only added if the service runs in development mode.")
@JsonClassDescription("DTO that wraps server errors and/or exceptions. The Java stack-trace is only added if the service runs in development mode.")
public class ApiError {

	/**
	 * Date and time at which the error occurred.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Date and time at which the error occurred.")
	@JsonPropertyDescription("Date and time at which the error occurred.")
	@Expose
	@SerializedName("time")
	@JsonProperty(required = true, value = "time")
	private final LocalDateTime time;

	/**
	 * URI path/resource that caused the error.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "URI path/resource that caused the error.")
	@JsonPropertyDescription("URI path/resource that caused the error.")
	@Expose
	@SerializedName("path")
	@JsonProperty(required = true, value = "path")
	private final String path;

	/**
	 * Status code for the error. Example: 400, 404, 500, ...
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Status code for the error.", example = "400, 404, 500, ...")
	@JsonPropertyDescription("Status code for the error. Example: 400, 404, 500, ...")
	@Expose
	@SerializedName("status")
	@JsonProperty(required = true, value = "status")
	private final Integer status;

	/**
	 * String representation of the status. Example: "Internal Server Error, "Not Found", ...
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "String representation of the status.", example = "'Internal Server Error', 'Not Found', ...")
	@JsonPropertyDescription("String representation of the status. Example: \"Internal Server Error, \"Not Found\", ...")
	@Expose
	@SerializedName("error")
	@JsonProperty(required = true, value = "error")
	private final String error;

	/**
	 * The error or exception message. Example: "java.lang.RuntimeException".
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The error or exception message.", example = "java.lang.RuntimeException")
	@JsonPropertyDescription("The error or exception message. Example: \"java.lang.RuntimeException\".")
	@Expose
	@SerializedName("message")
	@JsonProperty(required = true, value = "message")
	private final String message;

	/**
	 * Exception class name.
	 */
	@io.swagger.annotations.ApiModelProperty("Exception class name.")
	@JsonPropertyDescription("Exception class name.")
	@Expose
	@SerializedName("exception")
	@JsonProperty("exception")
	private final String exception;

	/**
	 * Exception stack trace.
	 */
	@io.swagger.annotations.ApiModelProperty("Exception stack tracewhich is only added if the service runs in development mode.")
	@JsonPropertyDescription("Exception stack tracewhich is only added if the service runs in development mode.")
	@Expose
	@SerializedName("trace")
	@JsonProperty("trace")
	private final String trace;

	/**
	 *
	 * @param title
	 * @param message
	 */
	public ApiError(LocalDateTime times, String path, Integer status, String error, String message, String exception, String trace) {
		this.time = times;
		this.path = path;
		this.status = status;
		this.error = error;
		this.message = message;
		this.exception = exception;
		this.trace = trace;
	}

	/**
	 *
	 * @return
	 */
	public LocalDateTime getTime() {
		return time;
	}

	/**
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 *
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 *
	 * @return
	 */
	public String getError() {
		return error;
	}

	/**
	 *
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 *
	 * @return
	 */
	public String getException() {
		return exception;
	}

	/**
	 *
	 * @return
	 */
	public String getTrace() {
		return trace;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerError [time=" + time + ", path=" + path + ", status=" + status + ", error=" + error
				+ ", message=" + message + ", exception=" + exception + "]";
	}
}
