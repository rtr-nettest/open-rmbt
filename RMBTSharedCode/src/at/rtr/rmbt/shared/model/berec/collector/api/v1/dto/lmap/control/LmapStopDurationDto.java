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
public class LmapStopDurationDto extends LmapStopDto {
	
	/**
	 * The duration controlling the graceful forced termination of the scheduled Actions, in ms.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The duration controlling the graceful forced termination of the scheduled Actions, in ms.")
	@JsonPropertyDescription("The duration controlling the graceful forced termination of the scheduled Actions, in ms.")
	@Expose
	@SerializedName("duration")
	@JsonProperty(required = true, value = "duration")
	private Integer duration;

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
