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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.agent.settings;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.QoSMeasurementTypeDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.version.VersionResponse;

/**
 * The settings response object sent to the measurement agent.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "The settings response object sent to the measurement agent.")
@JsonClassDescription("The settings response object sent to the measurement agent.")
public class SettingsResponse extends BasicResponse {

	/**
	 * @see Urls
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Object that contains base URLs for controller, collector, map and statistic services.")
	@JsonPropertyDescription("Object that contains base URLs for controller, collector, map and statistic services.")
	@Expose
	@SerializedName("urls")
	@JsonProperty(required = true, value = "urls")
	private Urls urls;

	/**
	 * Map of QoS measurement types to translated type information.
	 */
	@io.swagger.annotations.ApiModelProperty("Map of QoS measurement types to translated type information.")
	@JsonPropertyDescription("Map of QoS measurement types to translated type information.")
	@Expose
	@SerializedName("qos_type_info")
	@JsonProperty("qos_type_info")
	private Map<QoSMeasurementTypeDto, TranslatedQoSTypeInfo> qosTypeInfo;

	/**
	 * @see VersionResponse
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Class for all kind of versions that the server reveals.")
	@JsonPropertyDescription("Class for all kind of versions that the server reveals.")
	@Expose
	@SerializedName("versions")
	@JsonProperty(required = true, value = "versions")
	private VersionResponse versions;

	public Urls getUrls() {
		return urls;
	}

	public void setUrls(Urls urls) {
		this.urls = urls;
	}

	public Map<QoSMeasurementTypeDto, TranslatedQoSTypeInfo> getQosTypeInfo() {
		return qosTypeInfo;
	}

	public void setQosTypeInfo(Map<QoSMeasurementTypeDto, TranslatedQoSTypeInfo> qosTypeInfo) {
		this.qosTypeInfo = qosTypeInfo;
	}

	public VersionResponse getVersions() {
		return versions;
	}

	public void setVersions(VersionResponse versions) {
		this.versions = versions;
	}

	/**
	 * Object that contains base URLs for controller, collector, map and statistic services.
	 *
	 * @author alladin-IT GmbH (bp@alladin.at)
	 *
	 */
	@io.swagger.annotations.ApiModel(description = "Object that contains base URLs for controller, collector, map and statistic services.")
	@JsonClassDescription("Object that contains base URLs for controller, collector, map and statistic services.")
	public static class Urls {

		/**
		 * Base URL of the controller service of the form "[protocol]://[domain]:[port]/[path]".
		 * This domain name should have A and AAAA records.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Base URL of the controller service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@JsonPropertyDescription("Base URL of the controller service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@Expose
		@SerializedName("controller_service")
		@JsonProperty(required = true, value = "controller_service")
		private String controllerService;
		
		/**
		 * IPv4-only base URL of the controller service.
		 * This domain name must only have an A record.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "IPv4-only base URL of the controller service. This domain name must only have an A record.")
		@JsonPropertyDescription("IPv4-only base URL of the controller service. This domain name must only have an A record.")
		@Expose
		@SerializedName("controller_service_ipv4")
		@JsonProperty(required = true, value = "controller_service_ipv4")
		private String controllerServiceIpv4;
		
		/**
		 * IPv6-only base URL of the controller service.
		 * This domain name must only have an AAAA record.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "IPv6-only base URL of the controller service. This domain name must only have an AAAA record.")
		@JsonPropertyDescription("IPv6-only base URL of the controller service. This domain name must only have an AAAA record.")
		@Expose
		@SerializedName("controller_service_ipv6")
		@JsonProperty(required = true, value = "controller_service_ipv6")
		private String controllerServiceIpv6;
		
		/**
		 * Base URL of the collector service of the form "[protocol]://[domain]:[port]/[path]".
		 * This domain name should have A and AAAA records.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Base URL of the collector service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@JsonPropertyDescription("Base URL of the collector service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@Expose
		@SerializedName("collector_service")
		@JsonProperty(required = true, value = "collector_service")
		private String collectorService;

		/**
		 * Base URL of the map service of the form "[protocol]://[domain]:[port]/[path]".
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Base URL of the map service of the form \"[protocol]://[domain]:[port]/[path]\".")
		@JsonPropertyDescription("Base URL of the map service of the form \"[protocol]://[domain]:[port]/[path]\".")
		@Expose
		@SerializedName("map_service")
		@JsonProperty(required = true, value = "map_service")
		private String mapService;

		/**
		 * Base URL of the statistic service of the form "[protocol]://[domain]:[port]/[path]".
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Base URL of the statistic service of the form \"[protocol]://[domain]:[port]/[path]\".")
		@JsonPropertyDescription("Base URL of the statistic service of the form \"[protocol]://[domain]:[port]/[path]\".")
		@Expose
		@SerializedName("statistic_service")
		@JsonProperty(required = true, value = "statistic_service")
		private String statisticService;
		
		/**
		 * Base URL of the result service of the form "[protocol]://[domain]:[port]/[path]".
		 * This domain name should have A and AAAA records.
		 */
		@io.swagger.annotations.ApiModelProperty("Base URL of the result service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@JsonPropertyDescription("Base URL of the result service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@Expose
		@SerializedName("result_service")
		@JsonProperty("result_service")
		private String resultService;
		
		/**
		 * Base URL of the load balancing service of the form "[protocol]://[domain]:[port]/[path]".
		 * This domain name should have A and AAAA records.
		 */
		@io.swagger.annotations.ApiModelProperty("Base URL of the load balancing service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@JsonPropertyDescription("Base URL of the load balancing service of the form \"[protocol]://[domain]:[port]/[path]\". This domain name should have A and AAAA records.")
		@Expose
		@SerializedName("load_balancing_service")
		@JsonProperty("load_balancing_service")
		private String loadBalancingService;

		/**
		 * Base URL of the web site of the form "[protocol]://[domain]:[port]/[path]".
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Base URL of the web site of the form \"[protocol]://[domain]:[port]/[path]\".")
		@JsonPropertyDescription("Base URL of the web site of the form \"[protocol]://[domain]:[port]/[path]\".")
		@Expose
		@SerializedName("website")
		@JsonProperty(required = true, value = "website")
		private String website;

		public String getControllerService() {
			return controllerService;
		}

		public void setControllerService(String controllerService) {
			this.controllerService = controllerService;
		}

		public String getControllerServiceIpv4() {
			return controllerServiceIpv4;
		}

		public void setControllerServiceIpv4(String controllerServiceIpv4) {
			this.controllerServiceIpv4 = controllerServiceIpv4;
		}

		public String getControllerServiceIpv6() {
			return controllerServiceIpv6;
		}

		public void setControllerServiceIpv6(String controllerServiceIpv6) {
			this.controllerServiceIpv6 = controllerServiceIpv6;
		}

		public String getCollectorService() {
			return collectorService;
		}

		public void setCollectorService(String collectorService) {
			this.collectorService = collectorService;
		}

		public String getMapService() {
			return mapService;
		}

		public void setMapService(String mapService) {
			this.mapService = mapService;
		}

		public String getStatisticService() {
			return statisticService;
		}

		public void setStatisticService(String statisticService) {
			this.statisticService = statisticService;
		}
		
		public String getResultService() {
			return resultService;
		}

		public void setResultService(String resultService) {
			this.resultService = resultService;
		}

		public String getLoadBalancingService() {
			return loadBalancingService;
		}

		public void setLoadBalancingService(String loadBalancingService) {
			this.loadBalancingService = loadBalancingService;
		}

		public String getWebsite() {
			return website;
		}

		public void setWebsite(String website) {
			this.website = website;
		}

		@Override
		public String toString() {
			return "Urls [controllerService=" + controllerService + ", controllerServiceIpv4=" + controllerServiceIpv4
					+ ", controllerServiceIpv6=" + controllerServiceIpv6 + ", collectorService=" + collectorService
					+ ", mapService=" + mapService + ", statisticService=" + statisticService + ", resultService="
					+ resultService + ", loadBalancingService=" + loadBalancingService + ", website=" + website + "]";
		}
	}

	/**
	 * Contains translated information for each available QoS measurement type.
	 *
	 * @author alladin-IT GmbH (bp@alladin.at)
	 *
	 */
	@io.swagger.annotations.ApiModel(description = "Contains translated information for each available QoS measurement type.")
	@JsonClassDescription("Contains translated information for each available QoS measurement type.")
	public static class TranslatedQoSTypeInfo {

		/**
		 * The translated QoS type name.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "The translated QoS type name.")
		@JsonPropertyDescription("The translated QoS type name.")
		@Expose
		@SerializedName("name")
		@JsonProperty(required = true, value = "name")
		private String name;

		/**
		 * The translated QoS type description.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "The translated QoS type description.")
		@JsonPropertyDescription("The translated QoS type description.")
		@Expose
		@SerializedName("description")
		@JsonProperty(required = true, value = "description")
		private String description;

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return "TranslatedQoSTypeInfo [name=" + name + ", description=" + description + "]";
		}
	}

	@Override
	public String toString() {
		return "SettingsResponse [urls=" + urls + ", qosTypeInfo=" + qosTypeInfo + ", versions=" + versions + "]";
	}
}
