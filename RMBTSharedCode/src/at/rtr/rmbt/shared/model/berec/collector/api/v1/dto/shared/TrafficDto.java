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
 * Holds information about the received and transmitted amount of data on the measurement agent.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *	
 */
@io.swagger.annotations.ApiModel(description = "Holds information about the received and transmitted amount of data on the measurement agent.")
@JsonClassDescription("Holds information about the received and transmitted amount of data on the measurement agent.")
public class TrafficDto {

	/**
	 * Bytes received.
	 */
	@io.swagger.annotations.ApiModelProperty("Bytes received.")
	@JsonPropertyDescription("Bytes received.")
	@Expose
	@SerializedName("bytes_rx")
	@JsonProperty("bytes_rx")
	private Long bytesRx;

	/**
	 * Bytes transmitted.
	 */
	@io.swagger.annotations.ApiModelProperty("Bytes transmitted.")
	@JsonPropertyDescription("Bytes transmitted.")
	@Expose
	@SerializedName("bytes_tx")
	@JsonProperty("bytes_tx")
	private Long bytesTx;

	public Long getBytesRx() {
		return bytesRx;
	}

	public void setBytesRx(Long bytesRx) {
		this.bytesRx = bytesRx;
	}

	public Long getBytesTx() {
		return bytesTx;
	}

	public void setBytesTx(Long bytesTx) {
		this.bytesTx = bytesTx;
	}
}
