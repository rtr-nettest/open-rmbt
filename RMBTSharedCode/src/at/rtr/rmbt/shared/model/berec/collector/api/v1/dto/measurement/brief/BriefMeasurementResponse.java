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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.brief;

import java.util.Map;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.MeasurementTypeDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.GeoLocationDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.MeasurementAgentTypeDto;

/**
 * The BriefMeasurementResponse contains the most important values of a measurement.
 * It is used to show a preview (list) of measurements to the end user.
 *
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "The BriefMeasurementResponse contains the most important values of a measurement. It is used to show a preview (list) of measurements to the end user.")
@JsonClassDescription("The BriefMeasurementResponse contains the most important values of a measurement. It is used to show a preview (list) of measurements to the end user.")
public class BriefMeasurementResponse {

// Measurement

	/**
	 * The UUIDv4 identifier of the measurement object.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The UUIDv4 identifier of the measurement object.")
	@JsonPropertyDescription("The UUIDv4 identifier of the measurement object.")
	@Expose
	@SerializedName("uuid")
	@JsonProperty(required = true, value = "uuid")
	private String uuid;

	/**
	 * Overall start time in UTC.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Overall start time in UTC.")
	@JsonPropertyDescription("Overall start time in UTC.")
	@Expose
	@SerializedName("start_time")
	@JsonProperty(required = true, value = "start_time")
	private LocalDateTime startTime;

	/**
	 * Overall duration of all sub measurements.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Overall duration of all sub measurements.")
	@JsonPropertyDescription("Overall duration of all sub measurements.")
	@Expose
	@SerializedName("duration_ns")
	@JsonProperty(required = true, value = "duration_ns")
	private Long durationNs;

// GeoLocationInfo

	/**
	 * The first accurate GeoLocation i.e. the location where the measurement was started.
	 */
	@io.swagger.annotations.ApiModelProperty("The first accurate GeoLocation i.e. the location where the measurement was started.")
	@JsonPropertyDescription("The first accurate GeoLocation i.e. the location where the measurement was started.")
	@Expose
	@SerializedName("first_accurate_geo_location")
	@JsonProperty("first_accurate_geo_location")
	private GeoLocationDto firstAccurateGeoLocation;

// AgentInfo

	/**
	 * @see AgentType
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The type of measurement agent.")
	@JsonPropertyDescription("The type of measurement agent.")
    @Expose
	@SerializedName("type")
    @JsonProperty(required = true, value = "type")
    private MeasurementAgentTypeDto type;

// DeviceInfo

    /**
     * @see BriefDeviceInfo
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "BriefDeviceInfo contains the most important values from DeviceInfo class.")
	@JsonPropertyDescription("BriefDeviceInfo contains the most important values from DeviceInfo class.")
    @Expose
	@SerializedName("device_info")
    @JsonProperty(required = true, value = "device_info")
    private BriefDeviceInfo deviceInfo;

// NetworkInfo

    /**
     * Network type id (@see NetworkType).
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Network type id.")
	@JsonPropertyDescription("Network type id.")
    @Expose
	@SerializedName("network_type_id")
    @JsonProperty(required = true, value = "network_type_id")
    private Integer networkTypeId;

    /**
     * Network type name (@see NetworkType).
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Network type name.")
	@JsonPropertyDescription("Network type name.")
    @Expose
	@SerializedName("network_type_name")
    @JsonProperty(required = true, value = "network_type_name")
    private String networkTypeName;

// SubMeasurement

	/**
	 * Map that contains available information for each measurement type (Speed, QoS).
	 * If map misses speed then no speed measurement was done, likewise for QoS, ...
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Map that contains available information for each measurement type (Speed, QoS). If map misses speed then no speed measurement was done, likewise for QoS, ...")
	@JsonPropertyDescription("Map that contains available information for each measurement type (Speed, QoS). If map misses speed then no speed measurement was done, likewise for QoS, ...")
	@Expose
	@SerializedName("measurements")
	@JsonProperty(required = true, value = "measurements")
	private Map<MeasurementTypeDto, BriefSubMeasurement> measurements;
	
	////

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public Long getDurationNs() {
		return durationNs;
	}

	public void setDurationNs(Long durationNs) {
		this.durationNs = durationNs;
	}

	public GeoLocationDto getFirstAccurateGeoLocation() {
		return firstAccurateGeoLocation;
	}

	public void setFirstAccurateGeoLocation(GeoLocationDto firstAccurateGeoLocation) {
		this.firstAccurateGeoLocation = firstAccurateGeoLocation;
	}

	public MeasurementAgentTypeDto getType() {
		return type;
	}

	public void setType(MeasurementAgentTypeDto type) {
		this.type = type;
	}

	public BriefDeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(BriefDeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public Integer getNetworkTypeId() {
		return networkTypeId;
	}

	public void setNetworkTypeId(Integer networkTypeId) {
		this.networkTypeId = networkTypeId;
	}

	public String getNetworkTypeName() {
		return networkTypeName;
	}

	public void setNetworkTypeName(String networkTypeName) {
		this.networkTypeName = networkTypeName;
	}

	public Map<MeasurementTypeDto, BriefSubMeasurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(Map<MeasurementTypeDto, BriefSubMeasurement> measurements) {
		this.measurements = measurements;
	}

	////
	
	/**
	 * Convenience method to check if this BriefMeasurementResponse contains a speed measurement.
	 * @return true if there is a speed measurement, false otherwise.
	 */
	@JsonIgnore
	public boolean isSpeedMeasurementAvailable() {
		return measurements != null && measurements.containsKey(MeasurementTypeDto.SPEED);
	}

	/**
	 * Convenience method to check if this BriefMeasurementResponse contains a QoS measurement.
	 * @return true if there is a QoS measurement, false otherwise.
	 */
	@JsonIgnore
	public boolean isQoSMeasurementAvailable() {
		return measurements != null && measurements.containsKey(MeasurementTypeDto.QOS);
	}

	////
	
	/**
	 * BriefDeviceInfo contains the most important values from DeviceInfo class.
	 *
	 * @author alladin-IT GmbH (bp@alladin.at)
	 *
	 */
	@io.swagger.annotations.ApiModel(description = "BriefDeviceInfo contains the most important values from DeviceInfo class.")
	@JsonClassDescription("BriefDeviceInfo contains the most important values from DeviceInfo class.")
	public static class BriefDeviceInfo {

		/**
		 * Device code name.
		 */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Device code name.")
		@JsonPropertyDescription("Device code name.")
		@Expose
		@SerializedName("device_code_name")
		@JsonProperty(required = true, value = "device_code_name")
		private String deviceCodeName;

	    /**
	     * The device name that is commonly known to users (e.g. Google Pixel).
	     */
		@io.swagger.annotations.ApiModelProperty("The device name that is commonly known to users (e.g. Google Pixel).")
		@JsonPropertyDescription("The device name that is commonly known to users (e.g. Google Pixel).")
	    @Expose
	    @SerializedName("device_full_name")
	    @JsonProperty("device_full_name")
	    private String deviceFullName;

	    /**
	     * Device operating system name.
	     */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Device operating system name.")
		@JsonPropertyDescription("Device operating system name.")
	    @Expose
	    @SerializedName("os_name")
	    @JsonProperty(required = true, value = "os_name")
	    private String osName;

	    /**
	     * Device operating system version.
	     */
		@io.swagger.annotations.ApiModelProperty(required = true, value = "Device operating system version.")
		@JsonPropertyDescription("Device operating system version.")
	    @Expose
	    @SerializedName("os_version")
	    @JsonProperty(required = true, value = "os_version")
	    private String osVersion;

	    /**
	     * Average CPU usage during the measurement.
	     */
		@io.swagger.annotations.ApiModelProperty("Average CPU usage during the measurement.")
		@JsonPropertyDescription("Average CPU usage during the measurement.")
	    @Expose
	    @SerializedName("avg_cpu_usage")
	    @JsonProperty("avg_cpu_usage")
	    private Double averageCpuUsage;

	    /**
	     * Average Memory usage during the measurement.
	     */
		@io.swagger.annotations.ApiModelProperty("Average Memory usage during the measurement.")
		@JsonPropertyDescription("Average Memory usage during the measurement.")
	    @Expose
	    @SerializedName("avg_mem_usage")
	    @JsonProperty("avg_mem_usage")
	    private Double averageMemUsage;

		public String getDeviceCodeName() {
			return deviceCodeName;
		}

		public void setDeviceCodeName(String deviceCodeName) {
			this.deviceCodeName = deviceCodeName;
		}

		public String getDeviceFullName() {
			return deviceFullName;
		}

		public void setDeviceFullName(String deviceFullName) {
			this.deviceFullName = deviceFullName;
		}

		public String getOsName() {
			return osName;
		}

		public void setOsName(String osName) {
			this.osName = osName;
		}

		public String getOsVersion() {
			return osVersion;
		}

		public void setOsVersion(String osVersion) {
			this.osVersion = osVersion;
		}

		public Double getAverageCpuUsage() {
			return averageCpuUsage;
		}

		public void setAverageCpuUsage(Double averageCpuUsage) {
			this.averageCpuUsage = averageCpuUsage;
		}

		public Double getAverageMemUsage() {
			return averageMemUsage;
		}

		public void setAverageMemUsage(Double averageMemUsage) {
			this.averageMemUsage = averageMemUsage;
		}
	}
}
