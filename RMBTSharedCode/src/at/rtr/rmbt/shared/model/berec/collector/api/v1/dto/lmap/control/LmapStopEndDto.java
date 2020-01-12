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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author fk
 */
@JsonInclude(Include.NON_EMPTY)
public class LmapStopEndDto extends LmapStopDto {
	
	/**
	 * The event source controlling the graceful forced termination of the scheduled Actions.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The event source controlling the graceful forced termination of the scheduled Actions.")
	@JsonPropertyDescription("The event source controlling the graceful forced termination of the scheduled Actions.")
	@Expose
	@SerializedName("end")
	@JsonProperty(required = true, value = "end")
	private String end;

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
}
