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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.QosBlockedPortsDto.QosBlockedPortTypeDto;


/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
@JsonClassDescription("Model that contains advanced QoS evaluation.")
public class QosAdvancedEvaluationDto {

	/**
	 * see {@link QosBlockedPorts}
	 */
	@JsonPropertyDescription("Contains information about blocked ports.")
	@Expose
	@SerializedName("blocked_ports")
	@JsonProperty("blocked_ports")
	Map<QosBlockedPortTypeDto, QosBlockedPortsDto> blockedPorts;

	@JsonPropertyDescription("The total number of blocked ports of all blocked port types.")
	@Expose
	@SerializedName("total_count_blocked_ports")
	@JsonProperty("total_count_blocked_ports")
	Integer totalCountBlockedPorts;

	public Map<QosBlockedPortTypeDto, QosBlockedPortsDto> getBlockedPorts() {
		return blockedPorts;
	}

	public void setBlockedPorts(Map<QosBlockedPortTypeDto, QosBlockedPortsDto> blockedPorts) {
		this.blockedPorts = blockedPorts;
	}

	public Integer getTotalCountBlockedPorts() {
		return totalCountBlockedPorts;
	}

	public void setTotalCountBlockedPorts(Integer totalCountBlockedPorts) {
		this.totalCountBlockedPorts = totalCountBlockedPorts;
	}
	
}
