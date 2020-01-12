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
 * Cell identity information from a point in time on the measurement agent.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 * @author Lukasz Budryk (alladin-IT GmbH)
 *
 */
@io.swagger.annotations.ApiModel(description = "Cell identity information from a point in time on the measurement agent.")
@JsonClassDescription("Cell identity information from a point in time on the measurement agent.")
public class CellInfoDto {

	/**
	 * Contains the cell-ID, if available.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains the cell-ID, if available.")
	@JsonPropertyDescription("Contains the cell-ID, if available.")
	@Expose
	@SerializedName("cell_id")
	@JsonProperty("cell_id")
	private Integer cellId;

	/**
	 * Contains the area code (e.g. location area code (GSM), tracking area code (LTE)), if available.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains the area code (e.g. location area code (GSM), tracking area code (LTE)), if available.")
	@JsonPropertyDescription("Contains the area code (e.g. location area code (GSM), tracking area code (LTE)), if available.")
	@Expose
	@SerializedName("area_code")
	@JsonProperty("area_code")
	private Integer areaCode;

	/**
	 * Contains the primary scrambling code, if available.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains the primary scrambling code, if available.")
	@JsonPropertyDescription("Contains the primary scrambling code, if available.")
	@Expose
	@SerializedName("primary_scrambling_code")
	@JsonProperty("primary_scrambling_code")
	private Integer primaryScramblingCode;

	/**
	 * Contains the ARFCN (Absolute Radio Frequency Channel Number) (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains the frequency (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.")
	@JsonPropertyDescription("Contains the frequency (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.")
	@Expose
	@SerializedName("frequency")
	@JsonProperty("frequency")
	private Integer frequency;

	public Integer getCellId() {
		return cellId;
	}

	public void setCellId(Integer cellId) {
		this.cellId = cellId;
	}

	public Integer getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(Integer areaCode) {
		this.areaCode = areaCode;
	}

	public Integer getPrimaryScramblingCode() {
		return primaryScramblingCode;
	}

	public void setPrimaryScramblingCode(Integer primaryScramblingCode) {
		this.primaryScramblingCode = primaryScramblingCode;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
}
