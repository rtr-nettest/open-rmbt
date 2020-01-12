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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.initiation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO contains speed measurement instructions for the measurement agent.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO contains speed measurement instructions for the measurement agent.")
@JsonClassDescription("This DTO contains speed measurement instructions for the measurement agent.")
@JsonInclude(Include.NON_EMPTY)
public class SpeedMeasurementTypeParameters extends MeasurementTypeParameters {

	/**
	 * URL to the measurement code. Overrides the measurement agent's implementation if set.
	 */
	@io.swagger.annotations.ApiModelProperty("URL to the measurement code. Overrides the measurement agent's implementation if set.")
	@JsonPropertyDescription("URL to the measurement code. Overrides the measurement agent's implementation if set.")
	@Expose
	@SerializedName("javascript_measurement_code_url")
	@JsonProperty("javascript_measurement_code_url")
	private String javascriptMeasurementCodeUrl;
	
	/**
	 * The measurement server that should be used, or the first measurement server that should be requested when load balancing.
	 * @see MeasurementServerConfig
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The measurement server that should be used, or the first measurement server that should be requested when load balancing.")
	@JsonPropertyDescription("The measurement server that should be used, or the first measurement server that should be requested when load balancing.")
	@Expose
	@SerializedName("measurement_server")
	@JsonProperty(required = true, value = "measurement_server")
	private MeasurementServerConfig measurementServerConfig;

	/**
	 * Number of RTT packets that should be send in the RTT measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Number of RTT packets that should be send in the RTT measurement.")
	@JsonPropertyDescription("Number of RTT packets that should be send in the RTT measurement.")
	@Expose
	@SerializedName("rtt_count")
	@JsonProperty(required = true, value = "rtt_count")
	private Integer rttCount;

	@Expose
	@SerializedName("measurement_configuration")
	@JsonProperty(required = true, value = "measurement_configuration")
	private SpeedMeasurementConfiguration measurementConfiguration;


	public static class SpeedMeasurementConfiguration {
		/**
		 * Contains all measurement class configurations for the upload test.
		 */
		@JsonPropertyDescription("Contains all measurement class configurations for the upload test.")
		@Expose
		@SerializedName("upload")
		@JsonProperty("upload")
		private List<SpeedMeasurementClass> uploadClassList = new ArrayList<>();

		/**
		 * Contains all measurement class configurations for the download test.
		 */
		@JsonPropertyDescription("Contains all measurement class configurations for the download test.")
		@Expose
		@SerializedName("download")
		@JsonProperty("download")
		private List<SpeedMeasurementClass> downloadClassList = new ArrayList<>();
		
		@Override
		public String toString() {
			return "SpeedMeasurementSettings{" +
					"uploadClassList=" + uploadClassList +
					", downloadClassList=" + downloadClassList +
					"} " + super.toString();
		}

		/**
		 * Holds a single measurement class configuration.
		 *
		 * @author Lukasz Budryk (alladin-IT GmbH)
		 */
		@JsonClassDescription("Holds a single measurement class configuration.")
		public static class SpeedMeasurementClass {

			/**
			 *
			 */
			@JsonPropertyDescription("")
			@Expose
			@SerializedName("default")
			@JsonProperty("default")
			private Boolean isDefault = false;

			/**
			 * The requested number of streams for the measurement.
			 */
			@JsonPropertyDescription("The requested number of streams for the measurement.")
			@Expose
			@SerializedName("streams")
			@JsonProperty("streams")
			private Integer numStreams;

			/**
			 * The frame size of the measurement.
			 */
			@JsonPropertyDescription("The frame size of the measurement.")
			@Expose
			@SerializedName("frameSize")
			@JsonProperty("frameSize")
			private Integer frameSize;

			/**
			 * The boundaries for this specific measurement class.
			 */
			@JsonPropertyDescription("The boundaries for this specific measurement class.")
			@Expose
			@SerializedName("bounds")
			@JsonProperty("bounds")
			private Bounds bounds;

			/**
			 * The number of frames sent per upload method call.
			 */
			@JsonPropertyDescription("The number of frames sent per upload method call.")
			@Expose
			@SerializedName("framesPerCall")
			@JsonProperty("framesPerCall")
			private Integer framesPerCall;

			/**
			 *
			 * @author Lukasz Budryk (alladin-IT GmbH)
			 */
			@JsonClassDescription("")
			public static class Bounds {

				/**
				 * The lower bound.
				 */
				@JsonPropertyDescription("The lower bound.")
				@Expose
				@SerializedName("lower")
				@JsonProperty("lower")
				private Double lower;

				/**
				 * The upper bound.
				 */
				@JsonPropertyDescription("The upper bound.")
				@Expose
				@SerializedName("upper")
				@JsonProperty("upper")
				private Double upper;

				public Double getLower() {
					return lower;
				}

				public void setLower(Double lower) {
					this.lower = lower;
				}

				public Double getUpper() {
					return upper;
				}

				public void setUpper(Double upper) {
					this.upper = upper;
				}

				@Override
				public String toString() {
					return "Bounds{" +
							"lower=" + lower +
							", upper=" + upper +
							'}';
				}
			}

			public Boolean getDefault() {
				return isDefault;
			}

			public void setDefault(Boolean aDefault) {
				isDefault = aDefault;
			}

			public Integer getNumStreams() {
				return numStreams;
			}

			public void setNumStreams(Integer numStreams) {
				this.numStreams = numStreams;
			}

			public Integer getFrameSize() {
				return frameSize;
			}

			public void setFrameSize(Integer frameSize) {
				this.frameSize = frameSize;
			}

			public Bounds getBounds() {
				return bounds;
			}

			public void setBounds(Bounds bounds) {
				this.bounds = bounds;
			}

			public Integer getFramesPerCall() {
				return framesPerCall;
			}

			public void setFramesPerCall(Integer framesPerCall) {
				this.framesPerCall = framesPerCall;
			}

			@Override
			public String toString() {
				return "SpeedMeasurementClass{" +
						"isDefault=" + isDefault +
						", numStreams=" + numStreams +
						", frameSize=" + frameSize +
						", bounds=" + bounds +
						", framesPerCall=" + framesPerCall +
						'}';
			}
		}

		public List<SpeedMeasurementClass> getUploadClassList() {
			return uploadClassList;
		}

		public void setUploadClassList(List<SpeedMeasurementClass> uploadClassList) {
			this.uploadClassList = uploadClassList;
		}

		public List<SpeedMeasurementClass> getDownloadClassList() {
			return downloadClassList;
		}

		public void setDownloadClassList(List<SpeedMeasurementClass> downloadClassList) {
			this.downloadClassList = downloadClassList;
		}
		
	}

	/**
	 * Configuration object that holds the measurement server information.
	 * 
	 * @author alladin-IT GmbH (bp@alladin.at)
	 *
	 */
	@JsonClassDescription("Configuration object that holds the measurement server information.")
	public static class MeasurementServerConfig {
		
		/**
		 * Measurement server base URL.
		 */
		@io.swagger.annotations.ApiModelProperty("Measurement server base URL.")
		@JsonPropertyDescription("Measurement server base URL.")
		@Expose
		@SerializedName("base_url")
		@JsonProperty("base_url")
		private String baseUrl;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}

	public String getJavascriptMeasurementCodeUrl() {
		return javascriptMeasurementCodeUrl;
	}

	public void setJavascriptMeasurementCodeUrl(String javascriptMeasurementCodeUrl) {
		this.javascriptMeasurementCodeUrl = javascriptMeasurementCodeUrl;
	}

	public MeasurementServerConfig getMeasurementServerConfig() {
		return measurementServerConfig;
	}

	public void setMeasurementServerConfig(MeasurementServerConfig measurementServerConfig) {
		this.measurementServerConfig = measurementServerConfig;
	}

	public Integer getRttCount() {
		return rttCount;
	}

	public void setRttCount(Integer rttCount) {
		this.rttCount = rttCount;
	}

	public SpeedMeasurementConfiguration getMeasurementConfiguration() {
		return measurementConfiguration;
	}

	public void setMeasurementConfiguration(SpeedMeasurementConfiguration measurementConfiguration) {
		this.measurementConfiguration = measurementConfiguration;
	}
	
}
