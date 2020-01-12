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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.detail;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;

/**
 * This DTO contains a list of detail groups.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO contains a list of detail groups.")
@JsonClassDescription("This DTO contains a list of detail groups.")
public class DetailMeasurementResponse extends BasicResponse {

	/**
	 * A list of detail groups.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "A list of detail groups.")
	@JsonPropertyDescription("A list of detail groups.")
	@Expose
	@SerializedName("groups")
	@JsonProperty(required = true, value = "groups")
	private List<DetailMeasurementGroup> groups;
	
	/**
	 * The share text for this specific measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The share text for this specific measurement.")
	@JsonPropertyDescription("The share text for this specific measurement.")
	@Expose
	@SerializedName("share_measurement_text")
	@JsonProperty(required = true, value = "share_measurement_text")
	private String shareMeasurementText;
	
	/**
	 * True, if the measurement has results for QoS tests.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "True, if the measurement has results for QoS tests.")
	@JsonPropertyDescription("True, if the measurement has results for QoS tests.")
	@Expose
	@SerializedName("has_qos_results")
	@JsonProperty(required = true, value = "has_qos_results")
	private Boolean hasQoSResults;
	
	public List<DetailMeasurementGroup> getGroups() {
		return groups;
	}
	
	public void setGroups(List<DetailMeasurementGroup> groups) {
		this.groups = groups;
	}
	
	public String getShareMeasurementText() {
		return shareMeasurementText;
	}

	public void setShareMeasurementText(String shareMeasurementText) {
		this.shareMeasurementText = shareMeasurementText;
	}

	public Boolean getHasQoSResults() {
		return hasQoSResults;
	}

	public void setHasQoSResults(Boolean hasQoSResults) {
		this.hasQoSResults = hasQoSResults;
	}

	@Override
	public String toString() {
		return "DetailMeasurementResponse [groups=" + groups + ", shareMeasurementText=" + shareMeasurementText
				+ ", hasQoSResults=" + hasQoSResults + "]";
	}
	
}
