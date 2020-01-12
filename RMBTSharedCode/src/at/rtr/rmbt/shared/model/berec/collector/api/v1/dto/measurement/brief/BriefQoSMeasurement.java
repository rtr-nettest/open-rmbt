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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.brief;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Brief/short information of a QoS measurement.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Brief/short information of a QoS measurement.")
@JsonClassDescription("Brief/short information of a QoS measurement.")
public class BriefQoSMeasurement extends BriefSubMeasurement {

	/**
	 * Indicated how many objective where run during the QoS measurement.
	 */
	@io.swagger.annotations.ApiModelProperty("Indicated how many objective where run during the QoS measurement.")
	@JsonPropertyDescription("Indicated how many objective where run during the QoS measurement.")
	@Expose
	@SerializedName("objective_count")
	@JsonProperty("objective_count")
	private Integer objectiveCount;

	public Integer getObjectiveCount() {
		return objectiveCount;
	}
	
	public void setObjectiveCount(Integer objectiveCount) {
		this.objectiveCount = objectiveCount;
	}
}
