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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.peer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;

/**
 * Response DTO that contains a list of speed measurement peers.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Response DTO that contains a list of speed measurement peers.")
@JsonClassDescription("Response DTO that contains a list of speed measurement peers.")
public class SpeedMeasurementPeerResponse extends BasicResponse {

	/**
	 * The list of speed measurement peers.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The list of speed measurement peers.")
	@JsonPropertyDescription("The list of speed measurement peers.")
	@Expose
	@SerializedName("peers")
	@JsonProperty(required = true, value = "peers")
	private List<SpeedMeasurementPeer> speedMeasurementPeers;
	
	public List<SpeedMeasurementPeer> getSpeedMeasurementPeers() {
		return speedMeasurementPeers;
	}

	public void setSpeedMeasurementPeers(List<SpeedMeasurementPeer> speedMeasurementPeers) {
		this.speedMeasurementPeers = speedMeasurementPeers;
	}

	/**
	 * This class describes a single speed measurement peer.
	 *
	 * @author alladin-IT GmbH (bp@alladin.at)
	 *
	 */
	@io.swagger.annotations.ApiModel(description = "This class describes a single speed measurement peer.")
	@JsonClassDescription("This class describes a single speed measurement peer.")
	public static class SpeedMeasurementPeer {
		
		/**
		 * The measurement peer's public identifier which is sent back to server by the measurement agent.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "The measurement peer's public identifier which is sent back to server by the measurement agent.")
		@JsonPropertyDescription("The measurement peer's public identifier which is sent back to server by the measurement agent.")
		@Expose
		@SerializedName("identifier")
		@JsonProperty(required = true, value = "identifier")
		private String identifier;
		
		/**
		 * The measurement peer's public name.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "The measurement peer's public name.")
		@JsonPropertyDescription("The measurement peer's public name.")
		@Expose
		@SerializedName("name")
		@JsonProperty(required = true, value = "name")
		private String name;
		
		/**
		 * The measurement peer's public description.
		 */
		@io.swagger.annotations.ApiModelProperty(required = false, value = "The measurement peer's public description.")
		@JsonPropertyDescription("The measurement peer's public description.")
		@Expose
		@SerializedName("description")
		@JsonProperty(required = false, value = "description")
		private String description;
		
		/**
		 * A flag indicating if this measurement peer is the default one.
		 */
		@io.swagger.annotations.ApiModelProperty(required = false, value = "A flag indicating if this measurement peer is the default one.")
		@JsonPropertyDescription("A flag indicating if this measurement peer is the default one.")
		@Expose
		@SerializedName("default")
		@JsonProperty(required = false, value = "default")
		private boolean defaultPeer;
		
		public String getIdentifier() {
			return identifier;
		}
		
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		
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
		
		public boolean isDefaultPeer() {
			return defaultPeer;
		}

		public void setDefaultPeer(boolean defaultPeer) {
			this.defaultPeer = defaultPeer;
		}
	}
}

