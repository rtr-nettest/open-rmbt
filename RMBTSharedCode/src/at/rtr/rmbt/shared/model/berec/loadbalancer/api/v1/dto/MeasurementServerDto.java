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

package at.rtr.rmbt.shared.model.berec.loadbalancer.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author lb@alladin.at
 *
 */
public class MeasurementServerDto {

	/**
	 * The measurement peer's public identifier which is sent back to server by the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The measurement peer's public identifier which is sent back to server by the measurement agent.")
	@JsonPropertyDescription("The measurement peer's public identifier which is sent back to server by the measurement agent.")
	@Expose
	@SerializedName("identifier")
	@JsonProperty(required = true, value = "identifier")
	private String identifier;
	
	/**
	 * The measurement peer's public name.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The measurement peer's public name.")
	@JsonPropertyDescription("The measurement peer's public name.")
	@Expose
	@SerializedName("name")
	@JsonProperty(required = true, value = "name")
	private String name;
	
	/**
	 * Load API URL
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Load API URL")
	@JsonPropertyDescription("Load API URL")
	@Expose
	@SerializedName("load_api_url")
	@JsonProperty(required = true, value = "load_api_url")	
	private String loadApiUrl;

	/**
	 * Load API secret key
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Load API secret key")
	@JsonPropertyDescription("Load API secret key")
	@Expose
	@SerializedName("load_api_secret")
	@JsonProperty(required = true, value = "load_api_secret")
	private String loadApiSecretKey;
	
	/**
	 * Port used for non-encrypted communication.
	 */
	@JsonPropertyDescription("Port used for non-encrypted communication.")
	@Expose
	@SerializedName("port")
	@JsonProperty("port")
	private Integer port;
	
	/**
	 * Port used for encrypted communication.
	 */
	@JsonPropertyDescription("Port used for encrypted communication.")
	@Expose
	@SerializedName("port_tls")
	@JsonProperty("port_tls")
	private Integer portTls;

	/**
	 * The measurement server's IPv4 address or name.
	 */
	@JsonPropertyDescription("The measurement server's IPv4 address or name.")
	@Expose
	@SerializedName("address_ipv4")
	@JsonProperty("address_ipv4")
	private String addressIpv4;

	/**
	 * The measurement server's IPv6 address or name.
	 */
	@JsonPropertyDescription("The measurement server's IPv6 address or name.")
	@Expose
	@SerializedName("address_ipv6")
	@JsonProperty("address_ipv6")
	private String addressIpv6;

	/**
	 * The measurement server's preferred encryption.
	 */
	@JsonPropertyDescription("The measurement server's preferred encryption.")
	@Expose
	@SerializedName("prefer_encryption")
	@JsonProperty("prefer_encryption")	
	private boolean preferEncryption;
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoadApiUrl() {
		return loadApiUrl;
	}

	public void setLoadApiUrl(String loadApiUrl) {
		this.loadApiUrl = loadApiUrl;
	}

	public String getLoadApiSecretKey() {
		return loadApiSecretKey;
	}

	public void setLoadApiSecretKey(String loadApiSecretKey) {
		this.loadApiSecretKey = loadApiSecretKey;
	}
	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getPortTls() {
		return portTls;
	}

	public void setPortTls(Integer portTls) {
		this.portTls = portTls;
	}

	public String getAddressIpv4() {
		return addressIpv4;
	}

	public void setAddressIpv4(String addressIpv4) {
		this.addressIpv4 = addressIpv4;
	}

	public String getAddressIpv6() {
		return addressIpv6;
	}

	public void setAddressIpv6(String addressIpv6) {
		this.addressIpv6 = addressIpv6;
	}

	public boolean isPreferEncryption() {
		return preferEncryption;
	}

	public void setPreferEncryption(boolean preferEncryption) {
		this.preferEncryption = preferEncryption;
	}

	@Override
	public String toString() {
		return "MeasurementServerDto [identifier=" + identifier + ", name=" + name + ", loadApiUrl=" + loadApiUrl
				+ ", loadApiSecretKey=" + loadApiSecretKey + ", port=" + port + ", portTls=" + portTls
				+ ", addressIpv4=" + addressIpv4 + ", addressIpv6=" + addressIpv6 + ", preferEncryption="
				+ preferEncryption + "]";
	}
}
