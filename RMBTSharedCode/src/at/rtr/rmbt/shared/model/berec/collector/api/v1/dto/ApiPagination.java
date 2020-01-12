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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Wrapper for paginated responses (e.g. list of measurements).
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Wrapper for paginated responses (e.g. list of measurements).")
@JsonClassDescription("Wrapper for paginated responses (e.g. list of measurements).")
public class ApiPagination<T> {

	/**
	 * Paginated list of objects.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Paginated list of objects.")
	@JsonPropertyDescription("Paginated list of objects.")
	@Expose
	@SerializedName("content")
	@JsonProperty(required = true, value = "content")
	@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="deserialize_type")
	private final List<T> content;
	
	/**
	 * Current page number (>= 0).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Current page number (>= 0).")
	@JsonPropertyDescription("Current page number (>= 0).")
	@Expose
	@SerializedName("page_number")
	@JsonProperty(required = true, value = "page_number")
	private final int pageNumber;

	/**
	 * Current page size (> 0).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Current page size (> 0).")
	@JsonPropertyDescription("Current page size (> 0).")
	@Expose
	@SerializedName("page_size")
	@JsonProperty(required = true, value = "page_size")
	private final int pageSize;

	/**
	 * Total amount of pages (>= 0).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Total amount of pages (>= 0).")
	@JsonPropertyDescription("Total amount of pages (>= 0).")
	@Expose
	@SerializedName("total_pages")
	@JsonProperty(required = true, value = "total_pages")
	private final int totalPages;
	
	/**
	 * Total amount of objects (>= 0).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Total amount of objects (>= 0).")
	@JsonPropertyDescription("Total amount of objects (>= 0).")
	@Expose
	@SerializedName("total_elements")
	@JsonProperty(required = true, value = "total_elements")
	private final long totalElements;

	/**
	 * 
	 * @param page
	 */
	@JsonCreator
	public ApiPagination(@JsonProperty("content") List<T> content, 
			@JsonProperty("page_number") int pageNumber, 
			@JsonProperty("page_size") int pageSize, 
			@JsonProperty("total_pages") int totalPages, 
			@JsonProperty("total_elements") long totalElements) {
		this.content = content;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalPages = totalPages;
		this.totalElements = totalElements;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<T> getContent() {
		return content;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * 
	 * @return
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 
	 * @return
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * 
	 * @return
	 */
	public long getTotalElements() {
		return totalElements;
	}
}
