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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.MeasurementAgentTypeDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.GeoLocationDto;

/**
 * Additional information that is sent by measurement agent alongside the request.
 * This contains most information from measurement agentInfo.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Additional information that is sent by measurement agent alongside the request.")
@JsonClassDescription("Additional information that is sent by measurement agent alongside the request. This contains most information from measurement agentInfo.")
@JsonInclude(Include.NON_EMPTY)
public class ApiRequestInfo {

    /**
     * Language specified by the measurement agent.
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Language specified by the measurement agent.")
	@JsonPropertyDescription("Language specified by the measurement agent.")
    @Expose
    @SerializedName("language")
    @JsonProperty(required = true, value = "language")
    private String language;

	/**
	 * The measurement agent's time zone. Is only stored if a measurement is sent to the server.
	 */
	@io.swagger.annotations.ApiModelProperty("The measurement agent's time zone. Is only stored if a measurement is sent to the server.")
	@JsonPropertyDescription("The measurement agent's time zone. Is only stored if a measurement is sent to the server.")
	@Expose
	@SerializedName("timezone")
	@JsonProperty("timezone")
	private String timezone;

    /**
     * Type of measurement agent. Can be one of 'MOBILE', 'BROWSER', 'DESKTOP'.
     */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "Type of agent.")
    @JsonPropertyDescription("Type of agent.")
    @Expose
    @SerializedName("agent_type")
    @JsonProperty(required = true, value = "agent_type")
    private MeasurementAgentTypeDto agentType;

    /**
	 * The agent's UUID.
	 * This value is ignored if the resource path already contains the agent's UUID.
	 */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "The agent's UUID. This value is ignored if the resource path already contains the agent's UUID.")
    @JsonPropertyDescription("The agent's UUID. This value is ignored if the resource path already contains the agent's UUID.")
    @Expose
	@SerializedName("agent_uuid")
	@JsonProperty("agent_uuid")
	private String agentId;

    /**
     * Operating system name.
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Operating system name.")
	@JsonPropertyDescription("Operating system name.")
    @Expose
    @SerializedName("os_name")
    @JsonProperty(required = true, value = "os_name")
    private String osName;

    /**
     * Operating system version.
     */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "Operating system version.")
    @JsonPropertyDescription("Operating system version.")
    @Expose
    @SerializedName("os_version")
    @JsonProperty(required = true, value = "os_version")
    private String osVersion;

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
	 * Device code name.
	 */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "Device code name.")
    @JsonPropertyDescription("Device code name.")
	@Expose
	@SerializedName("code_name")
	@JsonProperty("code_name")
	private String codeName;

    /**
     * Detailed device designation.
     */
    @io.swagger.annotations.ApiModelProperty("Detailed device designation.")
    @JsonPropertyDescription("Detailed device designation.")
    @Expose
    @SerializedName("model")
    @JsonProperty("model")
    private String model;

    /**
	 * Application version name (e.g. 1.0.0).
	 */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "Application version name (e.g. 1.0.0).")
    @JsonPropertyDescription("Application version name (e.g. 1.0.0).")
	@Expose
	@SerializedName("app_version_name")
	@JsonProperty("app_version_name")
	private String appVersionName;

	/**
	 * Application version code number (e.g. 10).
	 */
    @io.swagger.annotations.ApiModelProperty("Application version code number (e.g. 10).")
    @JsonPropertyDescription("Application version code number (e.g. 10).")
	@Expose
	@SerializedName("app_version_code")
	@JsonProperty("app_version_code")
	private Integer appVersionCode;

	/**
	 * Git revision name.
	 */
    @io.swagger.annotations.ApiModelProperty("Git revision name.")
    @JsonPropertyDescription("Git revision name.")
	@Expose
	@SerializedName("app_git_revision")
	@JsonProperty("app_git_revision")
	private String appGitRevision;

    /**
     * The measurement agent device location at the time the request was sent or null if the measurement agent doesn't have location information.
     */
    @io.swagger.annotations.ApiModelProperty("The measurement agent device location at the time the request was sent or null if the measurement agent doesn't have location information.")
    @JsonPropertyDescription("The measurement agent device location at the time the request was sent or null if the measurement agent doesn't have location information.")
    @Expose
    @SerializedName("geo_location")
    @JsonProperty("geo_location")
    private GeoLocationDto geoLocation;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public MeasurementAgentTypeDto getAgentType() {
		return agentType;
	}

	public void setAgentType(MeasurementAgentTypeDto agentType) {
		this.agentType = agentType;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getApiLevel() {
		return apiLevel;
	}

	public void setApiLevel(String apiLevel) {
		this.apiLevel = apiLevel;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getAppVersionName() {
		return appVersionName;
	}

	public void setAppVersionName(String appVersionName) {
		this.appVersionName = appVersionName;
	}

	public Integer getAppVersionCode() {
		return appVersionCode;
	}

	public void setAppVersionCode(Integer appVersionCode) {
		this.appVersionCode = appVersionCode;
	}

	public String getAppGitRevision() {
		return appGitRevision;
	}

	public void setAppGitRevision(String appGitRevision) {
		this.appGitRevision = appGitRevision;
	}

	public GeoLocationDto getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocationDto geoLocation) {
		this.geoLocation = geoLocation;
	}
}
