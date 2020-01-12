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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.agent.registration;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.agent.settings.SettingsResponse;

/**
 * Measurement agent registration response object which is returned to the measurement agent after successful registration.
 * For convenience this response also contains the current settings.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Measurement agent registration response object which is returned to the measurement agent after successful registration. For convenience this response also contains the current settings.")
@JsonClassDescription("Measurement agent registration response object which is returned to the measurement agent after successful registration. For convenience this response also contains the current settings.")
public class RegistrationResponse extends BasicResponse {

	/**
	 * The generated measurement agent UUID.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The generated measurement agent UUID.")
	@JsonPropertyDescription("The generated measurement agent UUID.")
	@Expose
	@SerializedName("agent_uuid")
	@JsonProperty(required = true, value = "agent_uuid")
	private String agentUuid;
	
	/**
	 * @see SettingsResponse
	 */
	@io.swagger.annotations.ApiModelProperty("The settings response object sent to the measurement agent.")
	@JsonPropertyDescription("The settings response object sent to the measurement agent.")
	@Expose
	@SerializedName("settings")
	@JsonProperty("settings")
	private SettingsResponse settings;
	
	/**
	 * 
	 * @return
	 */
	public String getAgentUuid() {
		return agentUuid;
	}
	
	/**
	 * 
	 * @param agentUuid
	 */
	public void setAgentUuid(String agentUuid) {
		this.agentUuid = agentUuid;
	}
	
	/**
	 * 
	 * @return
	 */
	public SettingsResponse getSettings() {
		return settings;
	}
	
	/**
	 * 
	 * @param settings
	 */
	public void setSettings(SettingsResponse settings) {
		this.settings = settings;
	}

	@Override
	public String toString() {
		return "RegistrationResponse [agentUuid=" + agentUuid + ", settings=" + settings + "]";
	}
}
