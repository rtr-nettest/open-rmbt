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

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicRequest;

/**
 * Registration request DTO. It contains the fields needed to register a new measurement agent.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Registration request DTO. It contains the fields needed to register a new measurement agent.")
@JsonClassDescription("Registration request DTO. It contains the fields needed to register a new measurement agent.")
public class RegistrationRequest extends BasicRequest {

	/**
	 * Boolean whether the measurement agent has accepted the presented terms and conditions.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Boolean whether the measurement agent has accepted the presented terms and conditions.")
	@JsonPropertyDescription("Boolean whether the measurement agent has accepted the presented terms and conditions.")
	@Expose
	@SerializedName("terms_and_conditions_accepted")
	@JsonProperty(required = true, value = "terms_and_conditions_accepted")
	private boolean termsAndConditionsAccepted;

	/**
	 * The version of the presented terms and conditions that the measurement agent agreed to (or declined).
	 */
	@io.swagger.annotations.ApiModelProperty("The version of the presented terms and conditions that the measurement agent agreed to (or declined).")
	@JsonPropertyDescription("The version of the presented terms and conditions that the measurement agent agreed to (or declined).")
	@Expose
	@SerializedName("terms_and_conditions_accepted_version")
	@JsonProperty("terms_and_conditions_accepted_version")
	private Integer termsAndConditionsAcceptedVersion;

	/**
	 * The measurement agent's group name/identifier.
	 */
	@io.swagger.annotations.ApiModelProperty("The measurement agent's group name/identifier.")
	@JsonPropertyDescription("The measurement agent's group name/identifier.")
	@Expose
	@SerializedName("group_name")
	@JsonProperty("group_name")
	private String groupName;

	/**
	 * 
	 * @return
	 */
	public boolean isTermsAndConditionsAccepted() {
		return termsAndConditionsAccepted;
	}

	/**
	 * 
	 * @param termsAndConditionsAccepted
	 */
	public void setTermsAndConditionsAccepted(boolean termsAndConditionsAccepted) {
		this.termsAndConditionsAccepted = termsAndConditionsAccepted;
	}
	
	/**
	 *
	 * @return
	 */
	public Integer getTermsAndConditionsAcceptedVersion() {
		return termsAndConditionsAcceptedVersion;
	}

	/**
	 *
	 * @param termsAndConditionsAcceptedVersion
	 */
	public void setTermsAndConditionsAcceptedVersion(Integer termsAndConditionsAcceptedVersion) {
		this.termsAndConditionsAcceptedVersion = termsAndConditionsAcceptedVersion;
	}

	/**
	 *
	 * @return
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 *
	 * @param groupName
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
