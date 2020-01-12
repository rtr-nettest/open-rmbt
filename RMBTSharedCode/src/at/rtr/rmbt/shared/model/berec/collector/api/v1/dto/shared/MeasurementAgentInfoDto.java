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
 * Contains information about the measurement agent.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains information about the measurement agent.")
@JsonClassDescription("Contains information about the measurement agent.")
public class MeasurementAgentInfoDto {

	/**
	 * The agent UUID.
	 */
	@io.swagger.annotations.ApiModelProperty("The agent UUID.")
	@JsonPropertyDescription("The agent UUID.")
	@Expose
	@SerializedName("uuid")
	@JsonProperty("uuid")
	private String uuid;
	
	/**
	 * Application version name (e.g. 1.0.0).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Application version name (e.g. 1.0.0).")
	@JsonPropertyDescription("Application version name (e.g. 1.0.0).")
	@Expose
	@SerializedName("app_version_name")
	@JsonProperty(required = true, value = "app_version_name")
	private String appVersionName;
	
	/**
	 * Application version code number (e.g. 10).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Application version code number (e.g. 10).")
	@JsonPropertyDescription("Application version code number (e.g. 10).")
	@Expose
	@SerializedName("app_version_code")
	@JsonProperty(required = true, value = "app_version_code")
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
	 * The measurement agent's language.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The agent's language.")
	@JsonPropertyDescription("The agent's language.")
	@Expose
	@SerializedName("language")
	@JsonProperty(required = true, value = "language")
	private String language;
	
	/**
	 * The agent's time zone (e.g. UTC-6h).
	 */
	@io.swagger.annotations.ApiModelProperty("The agent's time zone (e.g. UTC-6h).")
	@JsonPropertyDescription("The agent's time zone (e.g. UTC-6h).")
	@Expose
	@SerializedName("timezone")
	@JsonProperty("timezone")
	private String timezone;
	
	/**
	 * @see MeasurementAgentTypeDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The type of agent.")
	@JsonPropertyDescription("The type of agent.")
    @Expose
	@SerializedName("type")
    @JsonProperty(required = true, value = "type")
    private MeasurementAgentTypeDto type;

	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public MeasurementAgentTypeDto getType() {
		return type;
	}

	public void setType(MeasurementAgentTypeDto type) {
		this.type = type;
	}
}
