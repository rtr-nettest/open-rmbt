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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains information about the connection(s) used for the speed measurement.
 * 
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Contains information about the connection(s) used for the speed measurement.")
@JsonClassDescription("Contains information about the connection(s) used for the speed measurement.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionInfoDto {

	/**
	 * The address of the measurement server.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The address of the measurement server.")
	@JsonPropertyDescription("The address of the measurement server.")
	@Expose
	@SerializedName("address")
	@JsonProperty(required = true, value = "address")
	private String address;
	
	/**
	 * The identifier of the measurement server.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The identifier of the measurement server.")
	@JsonPropertyDescription("The identifier of the measurement server.")
	@Expose
	@SerializedName("identifier")
	@JsonProperty(required = true, value = "identifier")
	private String identifier;

	/**
	 * The ip address of the measurement server (can be either v4 or v6).
	 */
	@JsonPropertyDescription("The ip address of the measurement server (can be either v4 or v6).")
	@Expose
	@SerializedName("ip_address")
   	@JsonProperty("ip_address")
   	private String ipAddress;

	/**
	 * Port used for the communication.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Port used for the communication.")
	@JsonPropertyDescription("Port used for the communication.")
	@Expose
	@SerializedName("port")
	@JsonProperty(required = true, value = "port")
	private Integer port;
	
	/**
	 * Indicates if the communication with the measurement server will be encrypted.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Indicates if the communication with the measurement server will be encrypted.")
	@JsonPropertyDescription("Indicates if the communication with the measurement server will be encrypted.")
	@Expose
	@SerializedName("encrpyted")
	@JsonProperty(required = true, value = "encrypted")
	private boolean encrypted;
	
	/**
	 * Cryptographic protocol and cipher suite used for encrypted communication, if available. E.g. TLSv1.2 (TLS_RSA_WITH_AES_128_GCM_SHA256).
	 */
	@io.swagger.annotations.ApiModelProperty("Cryptographic protocol and cipher suite used for encrypted communication, if available. E.g. TLSv1.2 (TLS_RSA_WITH_AES_128_GCM_SHA256).")
	@JsonPropertyDescription("Cryptographic protocol and cipher suite used for encrypted communication, if available. E.g. TLSv1.2 (TLS_RSA_WITH_AES_128_GCM_SHA256).")
	@Expose
	@SerializedName("encryption_info")
	@JsonProperty("encryption_info")
	private String encryptionInfo;
	
	/**
	 * Contains information about total bytes transferred during the speed measurement, as reported by the measurement agent's interface, if available.
	 * Only used for displaying to the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains information about total bytes transferred during the speed measurement, as reported by the measurement agent's interface, if available. Only used for displaying to the measurement agent.")
	@JsonPropertyDescription("Contains information about total bytes transferred during the speed measurement, as reported by the measurement agent's interface, if available. Only used for displaying to the measurement agent.")
	@Expose
	@SerializedName("agent_interface_total_traffic")
	@JsonProperty("agent_interface_total_traffic")
	private TrafficDto agentInterfaceTotalTraffic;
	
	/**
	 * Contains information about bytes transferred during the download measurement, as reported by the measurement agent's interface, if available.
	 * Only used for displaying to the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains information about bytes transferred during the download measurement, as reported by the measurement agent's interface, if available. Only used for displaying to the measurement agent.")
	@JsonPropertyDescription("Contains information about bytes transferred during the download measurement, as reported by the measurement agent's interface, if available. Only used for displaying to the measurement agent.")
	@Expose
	@SerializedName("agent_interface_download_measurement_traffic")
	@JsonProperty("agent_interface_download_measurement_traffic")
	private TrafficDto agentInterfaceDownloadMeasurementTraffic;
	
	/**
	 * Contains information about bytes transferred during the upload measurement, as reported by the measurement agent's interface, if available.
	 * Only used for displaying to the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains information about bytes transferred during the upload measurement, as reported by the measurement agent's interface, if available. Only used for displaying to the measurement agent.")
	@JsonPropertyDescription("Contains information about bytes transferred during the upload measurement, as reported by the measurement agent's interface, if available. Only used for displaying to the measurement agent.")
	@Expose
	@SerializedName("agent_interface_upload_measurement_traffic")
	@JsonProperty("agent_interface_upload_measurement_traffic")
	private TrafficDto agentInterfaceUploadMeasurementTraffic;

	/**
	 * The requested number of streams for the download measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The requested number of streams for the download measurement.")
	@JsonPropertyDescription("The requested number of streams for the download measurement.")
	@Expose
	@SerializedName("requested_num_streams_download")
	@JsonProperty(required = true, value = "requested_num_streams_download")
	private Integer requestedNumStreamsDownload;
	
	/**
	 * The requested number of streams for the upload measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The requested number of streams for the upload measurement.")
	@JsonPropertyDescription("The requested number of streams for the upload measurement.")
	@Expose
	@SerializedName("requested_num_streams_upload")
	@JsonProperty(required = true, value = "requested_num_streams_upload")
	private Integer requestedNumStreamsUpload;
	
	/**
	 * The actual number of streams used by the download measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual number of streams used by the download measurement.")
	@JsonPropertyDescription("The actual number of streams used by the download measurement.")
	@Expose
	@SerializedName("actual_num_streams_download")
	@JsonProperty(required = true, value = "actual_num_streams_download")
	private Integer actualNumStreamsDownload;
	
	/**
	 * The actual number of streams used by the upload measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The actual number of streams used by the upload measurement.")
	@JsonPropertyDescription("The actual number of streams used by the upload measurement.")
	@Expose
	@SerializedName("actual_num_streams_upload")
	@JsonProperty(required = true, value = "actual_num_streams_upload")
	private Integer actualNumStreamsUpload;
	
	/**
	 * Flag if TCP SACK (Selective Acknowledgement) is enabled/requested.
	 * See <a href="https://tools.ietf.org/html/rfc2018">https://tools.ietf.org/html/rfc2018</a>.
	 */
	@io.swagger.annotations.ApiModelProperty("Flag if TCP SACK (Selective Acknowledgement) is enabled/requested.")
	@JsonPropertyDescription("Flag if TCP SACK (Selective Acknowledgement) is enabled/requested.")
	@Expose
	@SerializedName("tcp_opt_sack_requested")
	@JsonProperty("tcp_opt_sack_requested")
	private Boolean tcpOptSackRequested;
	
	/**
	 * Flag if the TCP window scale options are requested.
	 */
	@io.swagger.annotations.ApiModelProperty("Flag if the TCP window scale options are requested.")
	@JsonPropertyDescription("Flag if the TCP window scale options are requested.")
	@Expose
	@SerializedName("tcp_opt_wscale_requested")
	@JsonProperty("tcp_opt_wscale_requested")
	private Boolean tcpOptWscaleRequested;
	
	/**
	 * Maximum Segment Size (MSS) value from the server-side.
	 */
	@io.swagger.annotations.ApiModelProperty("Maximum Segment Size (MSS) value from the server-side.")
	@JsonPropertyDescription("Maximum Segment Size (MSS) value from the server-side.")
	@Expose
	@SerializedName("server_mss")
	@JsonProperty("server_mss")
	private Integer serverMss;
	
	/**
	 * Maximum Transmission Unit (MTU) value from the server-side.
	 */
	@io.swagger.annotations.ApiModelProperty("Maximum Transmission Unit (MTU) value from the server-side.")
	@JsonPropertyDescription("Maximum Transmission Unit (MTU) value from the server-side.")
	@Expose
	@SerializedName("server_mtu")
	@JsonProperty("server_mtu")
	private Integer serverMtu;
	
	/**
	 * @see WebSocketInfo
	 */
	@io.swagger.annotations.ApiModelProperty("This class contains additional information gathered from the WebSocket protocol during the download measurement.")
	@JsonPropertyDescription("This class contains additional information gathered from the WebSocket protocol during the download measurement.")
	@Expose
	@SerializedName("web_socket_info_download")
	@JsonProperty("web_socket_info_download")
	private WebSocketInfoDto webSocketInfoDownload;
	
	/**
	 * @see WebSocketInfo
	 */
	@io.swagger.annotations.ApiModelProperty("This class contains additional information gathered from the WebSocket protocol during the upload measurement.")
	@JsonPropertyDescription("This class contains additional information gathered from the WebSocket protocol during the upload measurement.")
	@Expose
	@SerializedName("web_socket_info_upload")
	@JsonProperty("web_socket_info_upload")
	private WebSocketInfoDto webSocketInfoUpload;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public String getEncryptionInfo() {
		return encryptionInfo;
	}

	public void setEncryptionInfo(String encryptionInfo) {
		this.encryptionInfo = encryptionInfo;
	}

	public TrafficDto getAgentInterfaceTotalTraffic() {
		return agentInterfaceTotalTraffic;
	}

	public void setAgentInterfaceTotalTraffic(TrafficDto agentInterfaceTotalTraffic) {
		this.agentInterfaceTotalTraffic = agentInterfaceTotalTraffic;
	}

	public TrafficDto getAgentInterfaceDownloadMeasurementTraffic() {
		return agentInterfaceDownloadMeasurementTraffic;
	}

	public void setAgentInterfaceDownloadMeasurementTraffic(TrafficDto agentInterfaceDownloadMeasurementTraffic) {
		this.agentInterfaceDownloadMeasurementTraffic = agentInterfaceDownloadMeasurementTraffic;
	}

	public TrafficDto getAgentInterfaceUploadMeasurementTraffic() {
		return agentInterfaceUploadMeasurementTraffic;
	}

	public void setAgentInterfaceUploadMeasurementTraffic(TrafficDto agentInterfaceUploadMeasurementTraffic) {
		this.agentInterfaceUploadMeasurementTraffic = agentInterfaceUploadMeasurementTraffic;
	}

	public Integer getRequestedNumStreamsDownload() {
		return requestedNumStreamsDownload;
	}

	public void setRequestedNumStreamsDownload(Integer requestedNumStreamsDownload) {
		this.requestedNumStreamsDownload = requestedNumStreamsDownload;
	}

	public Integer getRequestedNumStreamsUpload() {
		return requestedNumStreamsUpload;
	}

	public void setRequestedNumStreamsUpload(Integer requestedNumStreamsUpload) {
		this.requestedNumStreamsUpload = requestedNumStreamsUpload;
	}

	public Integer getActualNumStreamsDownload() {
		return actualNumStreamsDownload;
	}

	public void setActualNumStreamsDownload(Integer actualNumStreamsDownload) {
		this.actualNumStreamsDownload = actualNumStreamsDownload;
	}

	public Integer getActualNumStreamsUpload() {
		return actualNumStreamsUpload;
	}

	public void setActualNumStreamsUpload(Integer actualNumStreamsUpload) {
		this.actualNumStreamsUpload = actualNumStreamsUpload;
	}

	public Boolean getTcpOptSackRequested() {
		return tcpOptSackRequested;
	}

	public void setTcpOptSackRequested(Boolean tcpOptSackRequested) {
		this.tcpOptSackRequested = tcpOptSackRequested;
	}

	public Boolean getTcpOptWscaleRequested() {
		return tcpOptWscaleRequested;
	}

	public void setTcpOptWscaleRequested(Boolean tcpOptWscaleRequested) {
		this.tcpOptWscaleRequested = tcpOptWscaleRequested;
	}

	public Integer getServerMss() {
		return serverMss;
	}

	public void setServerMss(Integer serverMss) {
		this.serverMss = serverMss;
	}

	public Integer getServerMtu() {
		return serverMtu;
	}

	public void setServerMtu(Integer serverMtu) {
		this.serverMtu = serverMtu;
	}

	public WebSocketInfoDto getWebSocketInfoDownload() {
		return webSocketInfoDownload;
	}

	public void setWebSocketInfoDownload(WebSocketInfoDto webSocketInfoDownload) {
		this.webSocketInfoDownload = webSocketInfoDownload;
	}

	public WebSocketInfoDto getWebSocketInfoUpload() {
		return webSocketInfoUpload;
	}

	public void setWebSocketInfoUpload(WebSocketInfoDto webSocketInfoUpload) {
		this.webSocketInfoUpload = webSocketInfoUpload;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "ConnectionInfoDto [address=" + address + ", identifier=" + identifier + ", ipAddress=" + ipAddress
				+ ", port=" + port + ", encrypted=" + encrypted + ", encryptionInfo=" + encryptionInfo
				+ ", agentInterfaceTotalTraffic=" + agentInterfaceTotalTraffic
				+ ", agentInterfaceDownloadMeasurementTraffic=" + agentInterfaceDownloadMeasurementTraffic
				+ ", agentInterfaceUploadMeasurementTraffic=" + agentInterfaceUploadMeasurementTraffic
				+ ", requestedNumStreamsDownload=" + requestedNumStreamsDownload + ", requestedNumStreamsUpload="
				+ requestedNumStreamsUpload + ", actualNumStreamsDownload=" + actualNumStreamsDownload
				+ ", actualNumStreamsUpload=" + actualNumStreamsUpload + ", tcpOptSackRequested=" + tcpOptSackRequested
				+ ", tcpOptWscaleRequested=" + tcpOptWscaleRequested + ", serverMss=" + serverMss + ", serverMtu="
				+ serverMtu + ", webSocketInfoDownload=" + webSocketInfoDownload + ", webSocketInfoUpload="
				+ webSocketInfoUpload + "]";
	}
	
	
}