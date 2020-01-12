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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDateTime;

/**
 * Contains signal information from a point in time on the measurement agent.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains signal information from a point in time on the measurement agent.")
@JsonClassDescription("Contains signal information from a point in time on the measurement agent.")
@JsonInclude(Include.NON_EMPTY)
public class SignalDto {

	/**
	 * Cell location information from a point in time on the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Cell identity information from a point in time on the measurement agent.")
	@JsonPropertyDescription("Cell identity information from a point in time on the measurement agent.")
	@Expose
	@SerializedName("cell_info")
	@JsonProperty(required = true, value = "cell_info")
	private CellInfoDto cellInfo;

	/**
	 * Network type id as it gets returned by the Android API.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Network type id as it gets returned by the Android API.")
	@JsonPropertyDescription("Network type id as it gets returned by the Android API.")
	@Expose
	@SerializedName("network_type_id")
	@JsonProperty(required = true, value = "network_type_id")
	private Integer networkTypeId;

	/**
	 * Time and date the signal information was captured (UTC).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Time and date the signal information was captured (UTC).")
	@JsonPropertyDescription("Time and date the signal information was captured (UTC).")
	@Expose
	@SerializedName("time")
	@JsonProperty(required = true, value = "time")
	private LocalDateTime time;
	
	/**
     * Relative time in nanoseconds (to test begin).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Relative time in nanoseconds (to test begin).")
	@JsonPropertyDescription("Relative time in nanoseconds (to test begin).")
    @Expose
    @SerializedName("relative_time_ns")
    @JsonProperty(required = true, value = "relative_time_ns")
    private Long relativeTimeNs;

	/**
	 * The current WiFi link speed, in bits per second (If available).
	 */
	@io.swagger.annotations.ApiModelProperty("The current WiFi link speed, in bits per second (If available).")
	@JsonPropertyDescription("The current WiFi link speed, in bits per second (If available).")
	@Expose
	@SerializedName("wifi_link_speed_bps")
	@JsonProperty("wifi_link_speed_bps")	
	private Integer wifiLinkSpeedBps;
	
	/**
	 * The received signal strength indicator of the current 802.11 network, in dBm (If available).
	 */
	@io.swagger.annotations.ApiModelProperty("The received signal strength indicator of the current 802.11 network, in dBm (If available).")
	@JsonPropertyDescription("The received signal strength indicator of the current 802.11 network, in dBm (If available).")
	@Expose
	@SerializedName("wifi_rssi_dbm")
	@JsonProperty("wifi_rssi_dbm")
	private Integer wifiRssiDbm;
	
	/**
	 * The received signal strength of 2G or 3G connections, in dBm (If available).
	 */
	@io.swagger.annotations.ApiModelProperty("The received signal strength of 2G or 3G connections, in dBm (If available).")
	@JsonPropertyDescription("The received signal strength of 2G or 3G connections, in dBm (If available).")
	@Expose
	@SerializedName("signal_strength_2g3g_dbm")
	@JsonProperty("signal_strength_2g3g_dbm")	
	private Integer signalStrength2g3gDbm;
	
//	/**
//	 * The bit error rate as defined in (ETSI) TS 27.007 8.5 (If available).
//	 */
//	@io.swagger.annotations.ApiModelProperty("The bit error rate as defined in (ETSI) TS 27.007 8.5 (If available).")
//	@JsonPropertyDescription("The bit error rate as defined in (ETSI) TS 27.007 8.5 (If available).")
//	@Expose
//	@SerializedName("gsm_bit_error_rate")
//	@JsonProperty("gsm_bit_error_rate")	
//	private Integer gsmBitErrorRate;
	
    /**
     * The LTE reference signal received power, in dBm (If available).
     */
	@io.swagger.annotations.ApiModelProperty("The LTE reference signal received power, in dBm (If available).")
	@JsonPropertyDescription("The LTE reference signal received power, in dBm (If available).")
    @Expose
    @SerializedName("lte_rsrp_dbm")
    @JsonProperty("lte_rsrp_dbm")
    private Integer lteRsrpDbm;
    
    /**
     * The LTE reference signal received quality, in dB (If available).
     */
	@io.swagger.annotations.ApiModelProperty("The LTE reference signal received quality, in dB (If available).")
	@JsonPropertyDescription("The LTE reference signal received quality, in dB (If available).")
    @Expose
    @SerializedName("lte_rsrq_db")
    @JsonProperty("lte_rsrq_db")
    private Integer lteRsrqDb;
    
    /**
     * The LTE reference signal signal-to-noise ratio, in dB (If available).
     */
	@io.swagger.annotations.ApiModelProperty("The LTE reference signal signal-to-noise ratio, in dB (If available).")
	@JsonPropertyDescription("The LTE reference signal signal-to-noise ratio, in dB (If available).")
    @Expose
    @SerializedName("lte_rssnr_db")
    @JsonProperty("lte_rssnr_db")
    private Integer lteRssnrDb;
    
    /**
     * The LTE channel quality indicator (If available).
     */
	@io.swagger.annotations.ApiModelProperty("The LTE channel quality indicator (If available).")
	@JsonPropertyDescription("The LTE channel quality indicator (If available).")
    @Expose
    @SerializedName("lte_cqi")
    @JsonProperty("lte_cqi")
    private Integer lteCqi;
	
    /**
     * SSID of the network.
     */
	@io.swagger.annotations.ApiModelProperty("SSID of the network (if available).")
	@JsonPropertyDescription("SSID of the network (if available).")
	@Expose
    @SerializedName("wifi_ssid")
	@JsonProperty("wifi_ssid")
    private String wifiSsid;
    
    /**
     * BSSID of the network.
     */
	@io.swagger.annotations.ApiModelProperty("BSSID of the network (if available).")
	@JsonPropertyDescription("BSSID of the network (if available).")
	@Expose
    @SerializedName("wifi_bssid")
	@JsonProperty("wifi_bssid")
    private String wifiBssid;

	public CellInfoDto getCellInfo() {
		return cellInfo;
	}

	public void setCellInfo(CellInfoDto cellInfo) {
		this.cellInfo = cellInfo;
	}

	public Integer getNetworkTypeId() {
		return networkTypeId;
	}

	public void setNetworkTypeId(Integer networkTypeId) {
		this.networkTypeId = networkTypeId;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public Long getRelativeTimeNs() {
		return relativeTimeNs;
	}

	public void setRelativeTimeNs(Long relativeTimeNs) {
		this.relativeTimeNs = relativeTimeNs;
	}

	public Integer getWifiLinkSpeedBps() {
		return wifiLinkSpeedBps;
	}

	public void setWifiLinkSpeedBps(Integer wifiLinkSpeedBps) {
		this.wifiLinkSpeedBps = wifiLinkSpeedBps;
	}

	public Integer getWifiRssiDbm() {
		return wifiRssiDbm;
	}

	public void setWifiRssiDbm(Integer wifiRssiDbm) {
		this.wifiRssiDbm = wifiRssiDbm;
	}

	public Integer getSignalStrength2g3gDbm() {
		return signalStrength2g3gDbm;
	}

	public void setSignalStrength2g3gDbm(Integer signalStrength2g3gDbm) {
		this.signalStrength2g3gDbm = signalStrength2g3gDbm;
	}

	public Integer getLteRsrpDbm() {
		return lteRsrpDbm;
	}

	public void setLteRsrpDbm(Integer lteRsrpDbm) {
		this.lteRsrpDbm = lteRsrpDbm;
	}

	public Integer getLteRsrqDb() {
		return lteRsrqDb;
	}

	public void setLteRsrqDb(Integer lteRsrqDb) {
		this.lteRsrqDb = lteRsrqDb;
	}

	public Integer getLteRssnrDb() {
		return lteRssnrDb;
	}

	public void setLteRssnrDb(Integer lteRssnrDb) {
		this.lteRssnrDb = lteRssnrDb;
	}

	public Integer getLteCqi() {
		return lteCqi;
	}

	public void setLteCqi(Integer lteCqi) {
		this.lteCqi = lteCqi;
	}

	public String getWifiSsid() {
		return wifiSsid;
	}

	public void setWifiSsid(String wifiSsid) {
		this.wifiSsid = wifiSsid;
	}

	public String getWifiBssid() {
		return wifiBssid;
	}

	public void setWifiBssid(String wifiBssid) {
		this.wifiBssid = wifiBssid;
	}
}
