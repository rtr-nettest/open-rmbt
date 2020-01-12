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

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * GeoLocation object to be used on the measurement agent side and inside DTOs.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "GeoLocation object to be used on the measurement agent side and inside DTOs.")
@JsonClassDescription("GeoLocation object to be used on the measurement agent side and inside DTOs.")
public class GeoLocationDto {

	/**
     * Time and date the geographic location information was captured (UTC).
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Time and date the geographic location information was captured (UTC).")
	@JsonPropertyDescription("Time and date the geographic location information was captured (UTC).")
	@Expose
    @SerializedName("time")
    @JsonProperty(required = true, value = "time")
    private LocalDateTime time;
    
    /**
     * Geographic location accuracy.
     */
	@io.swagger.annotations.ApiModelProperty("Geographic location accuracy.")
	@JsonPropertyDescription("Geographic location accuracy.")
    @Expose
    @SerializedName("accuracy")
    @JsonProperty("accuracy")
    private Double accuracy;

    /**
     * Geographic location altitude.
     */
	@io.swagger.annotations.ApiModelProperty("Geographic location altitude.")
	@JsonPropertyDescription("Geographic location altitude.")
    @Expose
    @SerializedName("altitude")
    @JsonProperty("altitude")
    private Double altitude;

    /**
     * Movement heading.
     */
	@io.swagger.annotations.ApiModelProperty("Movement heading.")
	@JsonPropertyDescription("Movement heading.")
    @Expose
    @SerializedName("heading")
    @JsonProperty("heading")
    private Double heading;

    /**
     * Movement speed.
     */
	@io.swagger.annotations.ApiModelProperty("Movement speed.")
	@JsonPropertyDescription("Movement speed.")
    @Expose
    @SerializedName("speed")
    @JsonProperty("speed")
    private Double speed;

    /**
     * Geographic location provider.
     */
	@io.swagger.annotations.ApiModelProperty("Geographic location provider.")
	@JsonPropertyDescription("Geographic location provider.")
    @Expose
    @SerializedName("provider")
    @JsonProperty("provider")
    private String provider;
    
    /**
     * Geographic location latitude.
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Geographic location latitude.")
	@JsonPropertyDescription("Geographic location latitude.")
    @Expose
    @SerializedName("latitude")
    @JsonProperty(required = true, value = "latitude")
    private Double latitude;
    
    /**
     * Geographic location longitude.
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Geographic location longitude.")
	@JsonPropertyDescription("Geographic location longitude.")
    @Expose
    @SerializedName("longitude")
    @JsonProperty(required = true, value = "longitude")
    private Double longitude;
    
    /**
     * Relative time in nanoseconds (to measurement begin).
     */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Relative time in nanoseconds (to measurement begin).")
	@JsonPropertyDescription("Relative time in nanoseconds (to measurement begin).")
    @Expose
    @SerializedName("relative_time_ns")
    @JsonProperty(required = true, value = "relative_time_ns")
    private Long relativeTimeNs;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getRelativeTimeNs() {
        return relativeTimeNs;
    }

    public void setRelativeTimeNs(Long relativeTimeNs) {
        this.relativeTimeNs = relativeTimeNs;
    }
}
