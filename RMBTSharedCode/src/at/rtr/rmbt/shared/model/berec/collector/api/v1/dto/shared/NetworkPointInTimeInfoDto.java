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
 * Contains all relevant network information of a single point in time.
 *
 * @author lb@alladin.at
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains all relevant network information of a single point in time.")
@JsonClassDescription("Contains all relevant network information of a single point in time.")
public class NetworkPointInTimeInfoDto {

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
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Time and date the signal information was captured (UTC).")
	@JsonPropertyDescription("Relative time in nanoseconds (to test begin).")
    @Expose
    @SerializedName("relative_time_ns")
    @JsonProperty(required = true, value = "relative_time_ns")
    private Long relativeTimeNs;

	/**
	 * Public IP address of the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty("Public IP address of the measurement agent.")
	@JsonPropertyDescription("Public IP address of the measurement agent.")
	@Expose
	@SerializedName("agent_public_ip")
	@JsonProperty("agent_public_ip")
	private String agentPublicIp;

	/**
	 * Private IP address of the client.
	 */
	@JsonPropertyDescription("Private IP address of the client.")
	@Expose
	@SerializedName("agent_private_ip")
	@JsonProperty("agent_private_ip")
	private String agentPrivateIp;

	/**
	 * Country of the measurement agent which is gathered by Geo-IP lookup.
	 */
	@io.swagger.annotations.ApiModelProperty("Country of the measurement agent which is gathered by Geo-IP lookup.")
	@JsonPropertyDescription("Country of the measurement agent which is gathered by Geo-IP lookup.")
	@Expose
	@SerializedName("agent_public_ip_country_code")
	@JsonProperty("agent_public_ip_country_code")
	private String agentPublicIpCountryCode;

	/**
	 * Reverse DNS for the public IP address.
	 */
	@io.swagger.annotations.ApiModelProperty("Reverse DNS for the public IP address.")
	@JsonPropertyDescription("Reverse DNS for the public IP address.")
	@Expose
	@SerializedName("public_ip_rdns")
	@JsonProperty("public_ip_rdns")
	private String publicIpRdns;

// _ EmbeddedNetworkType

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
	 * Network type name.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Network type name.")
	@JsonPropertyDescription("Network type name.")
	@Expose
	@SerializedName("network_type_name")
	@JsonProperty(required = true, value = "network_type_name")
	private String networkTypeName;

	/**
	 * Network group name (e.g. 2G, 3G, LAN, etc).
	 */
	@io.swagger.annotations.ApiModelProperty("Network group name (e.g. 2G, 3G, LAN, etc).")
	@JsonPropertyDescription("Network group name (e.g. 2G, 3G, LAN, etc).")
	@Expose
	@SerializedName("network_type_group_name")
	@JsonProperty("network_type_group_name")
	private String networkTypeGroupName;

	/**
	 * @see NetworkTypeCategory
	 */
	@io.swagger.annotations.ApiModelProperty("Contains the different network categories.")
	@JsonPropertyDescription("Contains the different network categories.")
	@Expose
	@SerializedName("network_type_category")
	@JsonProperty("network_type_category")
	private String networkTypeCategory;

// _ ProviderInfo

	/**
	 * ASN for the public IP address.
	 */
	@io.swagger.annotations.ApiModelProperty("ASN for the public IP address.")
	@JsonPropertyDescription("ASN for the public IP address.")
	@Expose
	@SerializedName("public_ip_asn")
	@JsonProperty("public_ip_asn")
	private Long publicIpAsn;

	/**
	 * Name of ASN.
	 */
	@io.swagger.annotations.ApiModelProperty("Name of ASN.")
	@JsonPropertyDescription("Name of ASN.")
	@Expose
	@SerializedName("public_ip_as_name")
	@JsonProperty("public_ip_as_name")
	private String publicIpAsName;

	/**
	 * Country code derived from the AS (e.g. "AT").
	 */
	@io.swagger.annotations.ApiModelProperty("Country code derived from the AS (e.g. \"AT\").")
	@JsonPropertyDescription("Country code derived from the AS (e.g. \"AT\").")
	@Expose
	@SerializedName("country_code_asn")
	@JsonProperty("country_code_asn")
	private String countryCodeAsn;

	/**
	 * The name of the provider.
	 */
	@io.swagger.annotations.ApiModelProperty("The name of the provider.")
	@JsonPropertyDescription("The name of the provider.")
	@Expose
	@SerializedName("provider_name")
	@JsonProperty("provider_name")
	private String providerName;

	/**
	 * The short name (or shortcut) of the provider.
	 */
	@io.swagger.annotations.ApiModelProperty("The short name (or shortcut) of the provider.")
	@JsonPropertyDescription("The short name (or shortcut) of the provider.")
	@Expose
	@SerializedName("provider_short_name")
	@JsonProperty("provider_short_name")
	private String providerShortName;

// _ NetworkWifiInfo

	/**
     * SSID of the network.
     */
	@io.swagger.annotations.ApiModelProperty("SSID of the network.")
	@JsonPropertyDescription("SSID of the network.")
	@Expose
    @SerializedName("ssid")
	@JsonProperty("ssid")
    private String ssid;

    /**
     * BSSID of the network.
     */
	@io.swagger.annotations.ApiModelProperty("BSSID of the network.")
	@JsonPropertyDescription("BSSID of the network.")
	@Expose
    @SerializedName("bssid")
	@JsonProperty("bssid")
    private String bssid;
	
	/**
     * Radio frequency of the wifi network.
     */
	@io.swagger.annotations.ApiModelProperty("Radio frequency of the wifi network.")
	@JsonPropertyDescription("Radio frequency of the wifi network.")
	@Expose
    @SerializedName("frequency")
	@JsonProperty("frequency")
	private Integer frequency;

// _ NetworkMobileInfo

	/**
     * The network operator country code (e.g. "AT"), if available.
     */
	@io.swagger.annotations.ApiModelProperty("The network operator country code (e.g. \"AT\"), if available.")
	@JsonPropertyDescription("The network operator country code (e.g. \"AT\"), if available.")
    @Expose
	@SerializedName("network_country")
    @JsonProperty("network_country")
    private String networkCountry;

    /**
     * The MCC/MNC of the network operator, if available.
     */
	@io.swagger.annotations.ApiModelProperty("The MCC/MNC of the network operator, if available.")
	@JsonPropertyDescription("The MCC/MNC of the network operator, if available.")
    @Expose
    @SerializedName("network_operator_mcc_mnc")
    @JsonProperty("network_operator_mcc_mnc")
    private String networkOperatorMccMnc;

    /**
     * The network operator name, if available.
     */
	@io.swagger.annotations.ApiModelProperty("The network operator name, if available.")
	@JsonPropertyDescription("The network operator name, if available.")
    @Expose
    @SerializedName("network_operator_name")
    @JsonProperty("network_operator_name")
    private String networkOperatorName;

    /**
     * The SIM operator country code (e.g. "AT"), if available.
     */
	@io.swagger.annotations.ApiModelProperty("The SIM operator country code (e.g. \"AT\"), if available.")
	@JsonPropertyDescription("The SIM operator country code (e.g. \"AT\"), if available.")
    @Expose
    @SerializedName("sim_country")
    @JsonProperty("sim_country")
    private String simCountry;

    /**
     * The MCC/MNC of the SIM operator, if available.
     */
	@io.swagger.annotations.ApiModelProperty("The MCC/MNC of the SIM operator, if available.")
	@JsonPropertyDescription("The MCC/MNC of the SIM operator, if available.")
    @Expose
    @SerializedName("sim_operator_mcc_mnc")
    @JsonProperty("sim_operator_mcc_mnc")
    private String simOperatorMccMnc;

    /**
     * SIM operator name, if available.
     */
	@io.swagger.annotations.ApiModelProperty("SIM operator name, if available.")
	@JsonPropertyDescription("SIM operator name, if available.")
    @Expose
    @SerializedName("sim_operator_name")
    @JsonProperty("sim_operator_name")
    private String simOperatorName;
	
    /**
     * Indicates if this is a roaming connection, if available (null = unknown).
     */
	@JsonPropertyDescription("Indicates if this is a roaming connection, if available (null = unknown).")
    @Expose
    @SerializedName("roaming")
    @JsonProperty("roaming")
    private Boolean roaming;

    /**
     * The roaming type, if available.
     */
	@JsonPropertyDescription("The roaming type, if available.")
    @Expose
    @SerializedName("roaming_type")
    @JsonProperty("roaming_type")
    private String roamingType;

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

	public String getAgentPublicIp() {
		return agentPublicIp;
	}

	public void setAgentPublicIp(String agentPublicIp) {
		this.agentPublicIp = agentPublicIp;
	}

	public String getAgentPublicIpCountryCode() {
		return agentPublicIpCountryCode;
	}

	public void setAgentPublicIpCountryCode(String agentPublicIpCountryCode) {
		this.agentPublicIpCountryCode = agentPublicIpCountryCode;
	}

	public String getPublicIpRdns() {
		return publicIpRdns;
	}

	public void setPublicIpRdns(String publicIpRdns) {
		this.publicIpRdns = publicIpRdns;
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

	public String getNetworkTypeGroupName() {
		return networkTypeGroupName;
	}

	public void setNetworkTypeGroupName(String networkTypeGroupName) {
		this.networkTypeGroupName = networkTypeGroupName;
	}

	public String getNetworkTypeCategory() {
		return networkTypeCategory;
	}

	public void setNetworkTypeCategory(String networkTypeCategory) {
		this.networkTypeCategory = networkTypeCategory;
	}

	public Long getPublicIpAsn() {
		return publicIpAsn;
	}

	public void setPublicIpAsn(Long publicIpAsn) {
		this.publicIpAsn = publicIpAsn;
	}

	public String getPublicIpAsName() {
		return publicIpAsName;
	}

	public void setPublicIpAsName(String publicIpAsName) {
		this.publicIpAsName = publicIpAsName;
	}

	public String getCountryCodeAsn() {
		return countryCodeAsn;
	}

	public void setCountryCodeAsn(String countryCodeAsn) {
		this.countryCodeAsn = countryCodeAsn;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderShortName() {
		return providerShortName;
	}

	public void setProviderShortName(String providerShortName) {
		this.providerShortName = providerShortName;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getBssid() {
		return bssid;
	}

	public void setBssid(String bssid) {
		this.bssid = bssid;
	}
	
	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public String getNetworkCountry() {
		return networkCountry;
	}

	public void setNetworkCountry(String networkCountry) {
		this.networkCountry = networkCountry;
	}

	public String getNetworkOperatorMccMnc() {
		return networkOperatorMccMnc;
	}

	public void setNetworkOperatorMccMnc(String networkOperatorMccMnc) {
		this.networkOperatorMccMnc = networkOperatorMccMnc;
	}

	public String getNetworkOperatorName() {
		return networkOperatorName;
	}

	public void setNetworkOperatorName(String networkOperatorName) {
		this.networkOperatorName = networkOperatorName;
	}

	public String getSimCountry() {
		return simCountry;
	}

	public void setSimCountry(String simCountry) {
		this.simCountry = simCountry;
	}

	public String getSimOperatorMccMnc() {
		return simOperatorMccMnc;
	}

	public void setSimOperatorMccMnc(String simOperatorMccMnc) {
		this.simOperatorMccMnc = simOperatorMccMnc;
	}

	public String getSimOperatorName() {
		return simOperatorName;
	}

	public void setSimOperatorName(String simOperatorName) {
		this.simOperatorName = simOperatorName;
	}

	public String getAgentPrivateIp() {
		return agentPrivateIp;
	}

	public void setAgentPrivateIp(String agentPrivateIp) {
		this.agentPrivateIp = agentPrivateIp;
	}

	public Boolean getRoaming() {
		return roaming;
	}

	public void setRoaming(Boolean roaming) {
		this.roaming = roaming;
	}

	public String getRoamingType() {
		return roamingType;
	}

	public void setRoamingType(String roamingType) {
		this.roamingType = roamingType;
	}
	
}
