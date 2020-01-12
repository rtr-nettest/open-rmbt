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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.BasicResponse;

/**
 * This DTO is returned after the measurement agent successfully submitted it's test result to the server.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO is returned after the measurement agent successfully submitted it's test result to the server.")
@JsonClassDescription("This DTO is returned after the measurement agent successfully submitted it's test result to the server.")
public class MeasurementResultResponse extends BasicResponse {

	/**
	 * The UUIDv4 identifier of the measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The UUIDv4 identifier of the measurement.")
	@JsonPropertyDescription("The UUIDv4 identifier of the measurement.")
	@Expose
	@SerializedName("uuid")
	@JsonProperty(required = true, value = "uuid")
	private String uuid;

	/**
	 * An UUIDv4 identifier that is used to find this measurement in an open-data context.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "An UUIDv4 identifier that is used to find this measurement in an open-data context.")
	@JsonPropertyDescription("An UUIDv4 identifier that is used to find this measurement in an open-data context.")
	@Expose
	@SerializedName("open_data_uuid")
	@JsonProperty(required = true, value = "open_data_uuid")
	private String openDataUuid;

	/**
	 *
	 * @return
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 *
	 * @param uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 *
	 * @return
	 */
	public String getOpenDataUuid() {
		return openDataUuid;
	}

	/**
	 *
	 * @param openDataUuid
	 */
	public void setOpenDataUuid(String openDataUuid) {
		this.openDataUuid = openDataUuid;
	}
}
