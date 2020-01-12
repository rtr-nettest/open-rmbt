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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.result;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO contains the QoS measurement results from the measurement agent.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO contains the QoS measurement results from the measurement agent.")
@JsonClassDescription("This DTO contains the QoS measurement results from the measurement agent.")
public class QoSMeasurementResult extends SubMeasurementResult {

	/**
	 * QoS measurement results.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "QoS measurement results.")
	@JsonPropertyDescription("QoS measurement results.")
	@Expose
	@SerializedName("results")
	@JsonProperty(required = true, value = "results")
	private List<Map<String, Object>> objectiveResults;

	public List<Map<String, Object>> getObjectiveResults() {
		return objectiveResults;
	}

	public void setObjectiveResults(List<Map<String, Object>> objectiveResults) {
		this.objectiveResults = objectiveResults;
	}
}
