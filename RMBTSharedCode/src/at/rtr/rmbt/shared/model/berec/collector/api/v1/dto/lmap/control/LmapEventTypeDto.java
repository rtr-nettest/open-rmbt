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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author fk
 */
//if we do support multiple events in the future, annotate as in LmapStopDto to support proper de/serializing
@JsonDeserialize(as = LmapImmediateEventDto.class)
public abstract class LmapEventTypeDto {
	
	/**
	 * Type identifier of the given event.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Type identifier of the given event.")
	@JsonPropertyDescription("Type identifier of the given event.")
	@Expose
	@SerializedName("type")
	@JsonProperty(required = true, value = "type")
	protected EventTypeEnum type; // TODO: rename EventTypeDto
	
	public LmapEventTypeDto (final EventTypeEnum type) {
		this.type = type;
	}
	
	public EventTypeEnum getType() {
		return type;
	}
	
	public void setType(EventTypeEnum type) {
		this.type = type;
	}
	
	public static enum EventTypeEnum {
		IMMEDIATE
	}
}
