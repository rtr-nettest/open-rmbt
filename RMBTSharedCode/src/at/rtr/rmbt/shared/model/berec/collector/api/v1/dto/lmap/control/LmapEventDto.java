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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Implementations may be forced to delay acting upon the occurrence of events in the face of local constraints. 
 * An Action triggered by an event therefore should not rely on the accuracy provided by the scheduler implementation.
 * @author fk
 *
 */
@io.swagger.annotations.ApiModel(description = "Implementations may be forced to delay acting upon the occurrence of events in the face of local constraints. An Action triggered by an event therefore should not rely on the accuracy provided by the scheduler implementation.")
@JsonClassDescription("Implementations may be forced to delay acting upon the occurrence of events in the face of local constraints. An Action triggered by an event therefore should not rely on the accuracy provided by the scheduler implementation.")
@JsonInclude(Include.NON_EMPTY)
public class LmapEventDto {
	
	/**
	 * The unique name of an event source, used when referencing this event.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The unique name of an event source, used when referencing this event.")
	@JsonPropertyDescription("The unique name of an event source, used when referencing this event.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;
	
	/**
	 * This optional leaf adds a random spread to the computation of the event's trigger time. 
	 * The random spread is a uniformly distributed random number taken from the interval [0:random-spread].
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "This optional leaf adds a random spread to the computation of the event's trigger time. The random spread is a uniformly distributed random number taken from the interval [0:random-spread].")
	@JsonPropertyDescription("This optional leaf adds a random spread to the computation of the event's trigger time. The random spread is a uniformly distributed random number taken from the interval [0:random-spread].")
	@Expose
	@SerializedName("random-spread")
	@JsonProperty(required = false, value = "random-spread")
	private Integer randomSpread;
	
	/**
	 * The optional cycle-interval defines the duration of the time interval in seconds
	 *  that is used to calculate cycle numbers. 
	 *  No cycle number is calculated if the optional cycle-interval does not exist.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "The optional cycle-interval defines the duration of the time interval in seconds that is used to calculate cycle numbers. No cycle number is calculated if the optional cycle-interval does not exist.")
	@JsonPropertyDescription("The optional cycle-interval defines the duration of the time interval in seconds that is used to calculate cycle numbers. No cycle number is calculated if the optional cycle-interval does not exist.")
	@Expose
	@SerializedName("cycle-interval")
	@JsonProperty(required = false, value = "cycle-interval")
	private Integer cycleInterval;

	/**
	 * Different types of events are handled by different branches of this choice. 
	 * Note that this choice can be extended via augmentations.
	 */
	@io.swagger.annotations.ApiModelProperty(required = false, value = "Different types of events are handled by different branches of this choice. Note that this choice can be extended via augmentations.")
	@JsonPropertyDescription("Different types of events are handled by different branches of this choice. Note that this choice can be extended via augmentations.")
	@Expose
	@SerializedName("event-type")
	@JsonProperty(required = false, value = "event-type")
	private LmapEventTypeDto event = new LmapImmediateEventDto();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRandomSpread() {
		return randomSpread;
	}

	public void setRandomSpread(Integer randomSpread) {
		this.randomSpread = randomSpread;
	}

	public Integer getCycleInterval() {
		return cycleInterval;
	}

	public void setCycleInterval(Integer cycleInterval) {
		this.cycleInterval = cycleInterval;
	}

	public LmapEventTypeDto getEvent() {
		return event;
	}

	public void setEvent(LmapEventTypeDto event) {
		this.event = event;
	}
}
