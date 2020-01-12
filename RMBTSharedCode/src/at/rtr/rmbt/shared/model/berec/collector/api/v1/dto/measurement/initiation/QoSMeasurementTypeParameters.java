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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.initiation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.QoSMeasurementTypeDto;

/**
 * This DTO contains QoS measurement instructions for the measurement agent.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO contains QoS measurement instructions for the measurement agent.")
@JsonClassDescription("This DTO contains QoS measurement instructions for the measurement agent.")
public class QoSMeasurementTypeParameters extends MeasurementTypeParameters {

	/**
	 * QoS objectives that should be executed by the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "QoS objectives that should be executed by the measurement agent.")
	@JsonPropertyDescription("QoS objectives that should be executed by the measurement agent.")
	@Expose
	@SerializedName("objectives")
	@JsonProperty(required = true, value = "objectives")
    private Map<QoSMeasurementTypeDto, List<Map<String, Object>>> objectives = new HashMap<>();

	public Map<QoSMeasurementTypeDto, List<Map<String, Object>>> getObjectives() {
		return objectives;
	}

	public void setObjectives(Map<QoSMeasurementTypeDto, List<Map<String, Object>>> objectives) {
		this.objectives = objectives;
	}
}
