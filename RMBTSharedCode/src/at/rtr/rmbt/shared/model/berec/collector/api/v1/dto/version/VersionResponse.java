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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.version;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;

/**
 * Class for all kind of versions that the server reveals.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Class for all kind of versions that the server reveals.")
@JsonClassDescription("Class for all kind of versions that the server reveals.")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class VersionResponse extends BasicResponse {

	/**
	 * Controller service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Controller service version number.")
	@JsonPropertyDescription("Controller service version number.")
	@Expose
	@SerializedName("controller_service_version")
	@JsonProperty("controller_service_version")
	private String controllerServiceVersion;
	
	/**
	 * Collector service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Collector service version number.")
	@JsonPropertyDescription("Collector service version number.")
	@Expose
	@SerializedName("collector_service_version")
	@JsonProperty("collector_service_version")
	private String collectorServiceVersion;
	
	/**
	 * Result service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Result service version number.")
	@JsonPropertyDescription("Result service version number.")
	@Expose
	@SerializedName("result_service_version")
	@JsonProperty("result_service_version")
	private String resultServiceVersion;
	
	/**
	 * Search service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Search service version number.")
	@JsonPropertyDescription("Search service version number.")
	@Expose
	@SerializedName("search_service_version")
	@JsonProperty("search_service_version")
	private String searchServiceVersion;
	
	/**
	 * Map service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Map service version number.")
	@JsonPropertyDescription("Map service version number.")
	@Expose
	@SerializedName("map_service_version")
	@JsonProperty("map_service_version")
	private String mapServiceVersion;
	
	/**
	 * Statistic service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Statistic service version number.")
	@JsonPropertyDescription("Statistic service version number.")
	@Expose
	@SerializedName("statistic_service_version")
	@JsonProperty("statistic_service_version")
	private String statisticServiceVersion;

	/**
	 * Opendata Collector service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Opendata Collector service version number.")
	@JsonPropertyDescription("Opendata Collector service version number.")
	@Expose
	@SerializedName("opendata_collector_service_version")
	@JsonProperty("opendata_collector_service_version")
	private String opendataCollectorServiceVersion;
	
	/**
	 * Loadbalancer service version number.
	 */
	@io.swagger.annotations.ApiModelProperty("Loadbalancer service version number.")
	@JsonPropertyDescription("Loadbalancer service version number.")
	@Expose
	@SerializedName("loadbalancer_service_version")
	@JsonProperty("loadbalancer_service_version")
	private String LoadbalancerServiceVersion;
	
	/**
	 * 
	 * @return
	 */
	public String getControllerServiceVersion() {
		return controllerServiceVersion;
	}
	
	/**
	 * 
	 * @param controllerServiceVersion
	 */
	public void setControllerServiceVersion(String controllerServiceVersion) {
		this.controllerServiceVersion = controllerServiceVersion;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getCollectorServiceVersion() {
		return collectorServiceVersion;
	}

	/**
	 * 
	 * @param collectorServiceVersion
	 */
	public void setCollectorServiceVersion(String collectorServiceVersion) {
		this.collectorServiceVersion = collectorServiceVersion;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getResultServiceVersion() {
		return resultServiceVersion;
	}

	/**
	 * 
	 * @param resultServiceVersion
	 */
	public void setResultServiceVersion(String resultServiceVersion) {
		this.resultServiceVersion = resultServiceVersion;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSearchServiceVersion() {
		return searchServiceVersion;
	}
	
	/**
	 * 
	 * @param searchServiceVersion
	 */
	public void setSearchServiceVersion(String searchServiceVersion) {
		this.searchServiceVersion = searchServiceVersion;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getMapServiceVersion() {
		return mapServiceVersion;
	}

	/**
	 * 
	 * @param mapServiceVersion
	 */
	public void setMapServiceVersion(String mapServiceVersion) {
		this.mapServiceVersion = mapServiceVersion;
	}

	/**
	 * 
	 * @return
	 */
	public String getStatisticServiceVersion() {
		return statisticServiceVersion;
	}

	/**
	 * 
	 * @param statisticServiceVersion
	 */
	public void setStatisticServiceVersion(String statisticServiceVersion) {
		this.statisticServiceVersion = statisticServiceVersion;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getOpendataCollectorServiceVersion() {
		return opendataCollectorServiceVersion;
	}
	
	/**
	 * 
	 * @param opendataCollectorServiceVersion
	 */
	public void setOpendataCollectorServiceVersion(String opendataCollectorServiceVersion) {
		this.opendataCollectorServiceVersion = opendataCollectorServiceVersion;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLoadbalancerServiceVersion() {
		return LoadbalancerServiceVersion;
	}
	
	/**
	 * 
	 * @param loadbalancerServiceVersion
	 */
	public void setLoadbalancerServiceVersion(String loadbalancerServiceVersion) {
		LoadbalancerServiceVersion = loadbalancerServiceVersion;
	}
}
