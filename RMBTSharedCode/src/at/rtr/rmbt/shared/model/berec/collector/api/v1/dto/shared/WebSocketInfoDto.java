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
 * This class contains additional information gathered from the WebSocket protocol.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This class contains additional information gathered from the WebSocket protocol.")
@JsonClassDescription("This class contains additional information gathered from the WebSocket protocol.")
public class WebSocketInfoDto {

	/**
	 * Size of a transmitted frame over the WebSocket protocol.
	 */
	@io.swagger.annotations.ApiModelProperty("Size of a transmitted frame over the WebSocket protocol.")
	@JsonPropertyDescription("Size of a transmitted frame over the WebSocket protocol.")
	@Expose
	@SerializedName("frame_size")
	@JsonProperty("frame_size")
	private Integer frameSize; // *load_frame_size
	
	/**
	 * Number of frames sent over the WebSocket protocol during measurement excluding slow-start phase.
	 */
	@io.swagger.annotations.ApiModelProperty("Number of frames sent over the WebSocket protocol during measurement excluding slow-start phase.")
	@JsonPropertyDescription("Number of frames sent over the WebSocket protocol during measurement excluding slow-start phase.")
	@Expose
	@SerializedName("frame_count")
	@JsonProperty("frame_count")
	private Integer frameCount; // *load_frames
	
	/**
	 * Number of frames sent over the WebSocket protocol during measurement including slow-start phase.
	 */
	@io.swagger.annotations.ApiModelProperty("Number of frames sent over the WebSocket protocol during measurement including slow-start phase.")
	@JsonPropertyDescription("Number of frames sent over the WebSocket protocol during measurement including slow-start phase.")
	@Expose
	@SerializedName("frame_count_including_slow_start")
	@JsonProperty("frame_count_including_slow_start")
	private Integer frameCountIncludingSlowStart; // *load_frames_total
	
	/**
	 * The overhead sent during the communication via the WebSocket protocol excluding slow-start phase.
	 */
	@io.swagger.annotations.ApiModelProperty("The overhead sent during the communication via the WebSocket protocol excluding slow-start phase.")
	@JsonPropertyDescription("The overhead sent during the communication via the WebSocket protocol excluding slow-start phase.")
	@Expose
	@SerializedName("overhead")
	@JsonProperty("overhead")
	private Integer overhead; // *load_overhead
	
	/**
	 * The overhead sent during the communication via the WebSocket protocol including slow-start phase.
	 */
	@io.swagger.annotations.ApiModelProperty("The overhead sent during the communication via the WebSocket protocol including slow-start phase.")
	@JsonPropertyDescription("The overhead sent during the communication via the WebSocket protocol including slow-start phase.")
	@Expose
	@SerializedName("overhead_including_slow_start")
	@JsonProperty("overhead_including_slow_start")
	private Integer overheadIncludingSlowStart; // *load_overhead_total
	
	/**
	 * The protocol overhead of a single WebSocket frame.
	 */
	@io.swagger.annotations.ApiModelProperty("The protocol overhead of a single WebSocket frame.")
	@JsonPropertyDescription("The protocol overhead of a single WebSocket frame.")
	@Expose
	@SerializedName("overhead_per_frame")
	@JsonProperty("overhead_per_frame")
	private Integer overheadPerFrame; // *load_overhead_per_frame

	public Integer getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(Integer frameSize) {
		this.frameSize = frameSize;
	}

	public Integer getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(Integer frameCount) {
		this.frameCount = frameCount;
	}

	public Integer getFrameCountIncludingSlowStart() {
		return frameCountIncludingSlowStart;
	}

	public void setFrameCountIncludingSlowStart(Integer frameCountIncludingSlowStart) {
		this.frameCountIncludingSlowStart = frameCountIncludingSlowStart;
	}

	public Integer getOverhead() {
		return overhead;
	}

	public void setOverhead(Integer overhead) {
		this.overhead = overhead;
	}

	public Integer getOverheadIncludingSlowStart() {
		return overheadIncludingSlowStart;
	}

	public void setOverheadIncludingSlowStart(Integer overheadIncludingSlowStart) {
		this.overheadIncludingSlowStart = overheadIncludingSlowStart;
	}

	public Integer getOverheadPerFrame() {
		return overheadPerFrame;
	}

	public void setOverheadPerFrame(Integer overheadPerFrame) {
		this.overheadPerFrame = overheadPerFrame;
	}
}
