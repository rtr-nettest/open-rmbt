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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.result;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.ConnectionInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.RttInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.SpeedMeasurementRawDataItemDto;

/**
 * This DTO contains the speed measurement results from the measurement agent.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO contains the speed measurement results from the measurement agent.")
@JsonClassDescription("This DTO contains the speed measurement results from the measurement agent.")
public class SpeedMeasurementResult extends SubMeasurementResult {

	/**
	 * Bytes received during the speed measurement (Download).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Bytes received during the speed measurement (Download).")
	@JsonPropertyDescription("Bytes received during the speed measurement (Download).")
	@Expose
	@SerializedName("bytes_download")
	@JsonProperty(required = true, value = "bytes_download")	
	private Long bytesDownload;
	
	/**
	 * Bytes received during the speed measurement (Download) with slow-start phase.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Bytes received during the speed measurement (Download) with slow-start phase.")
	@JsonPropertyDescription("Bytes received during the speed measurement (Download) with slow-start phase.")
	@Expose
	@SerializedName("bytes_download_including_slow_start")
	@JsonProperty(required = true, value = "bytes_download_including_slow_start")	
	private Long bytesDownloadIncludingSlowStart;

	/**
	 * Bytes transferred during the speed measurement (Upload).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Bytes transferred during the speed measurement (Upload).")
	@JsonPropertyDescription("Bytes transferred during the speed measurement (Upload).")
	@Expose
    @SerializedName("bytes_upload")
	@JsonProperty(required = true, value = "bytes_upload")
	private Long bytesUpload;
	
	/**
	 * Bytes transferred during the speed measurement (Upload) with slow-start phase.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Bytes transferred during the speed measurement (Upload) with slow-start phase.")
	@JsonPropertyDescription("Bytes transferred during the speed measurement (Upload) with slow-start phase.")
	@Expose
	@SerializedName("bytes_upload_including_slow_start")
	@JsonProperty(required = true, value = "bytes_upload_including_slow_start")	
	private Long bytesUploadIncludingSlowStart;
	
	/**
	 * Duration of the RTT measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Duration of the RTT measurement.")
	@JsonPropertyDescription("Duration of the RTT measurement.")
	@Expose
	@SerializedName("duration_rtt_ns")
	@JsonProperty(required = true, value = "duration_rtt_ns")
	private Long durationRttNs;
	
	/**
	 * Duration of the download measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Duration of the download measurement.")
	@JsonPropertyDescription("Duration of the download measurement.")
	@Expose
	@SerializedName("duration_download_ns")
	@JsonProperty(required = true, value = "duration_download_ns")
	private Long durationDownloadNs;
	
	/**
	 * Duration of the upload measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Duration of the upload measurement.")
	@JsonPropertyDescription("Duration of the upload measurement.")
	@Expose
	@SerializedName("duration_upload_ns")
	@JsonProperty(required = true, value = "duration_upload_ns")
	private Long durationUploadNs;
	
	/**
	 * Relative start time of the RTT measurement in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Relative start time of the RTT measurement in nanoseconds.")
	@JsonPropertyDescription("Relative start time of the RTT measurement in nanoseconds.")
	@Expose
	@SerializedName("relative_start_time_rtt_ns")
	@JsonProperty(required = true, value = "relative_start_time_rtt_ns")
	private Long relativeStartTimeRttNs;
	
	/**
	 * Relative start time of the download measurement in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Relative start time of the download measurement in nanoseconds.")
	@JsonPropertyDescription("Relative start time of the download measurement in nanoseconds.")
	@Expose
	@SerializedName("relative_start_time_download_ns")
	@JsonProperty(required = true, value = "relative_start_time_download_ns")
	private Long relativeStartTimeDownloadNs;

	/**
	 * Relative start time of the upload measurement in nanoseconds.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Relative start time of the upload measurement in nanoseconds.")
	@JsonPropertyDescription("Relative start time of the upload measurement in nanoseconds.")
	@Expose
	@SerializedName("relative_start_time_upload_ns")
	@JsonProperty(required = true, value = "relative_start_time_upload_ns")
	private Long relativeStartTimeUploadNs;
	
// RttInfo
	
	/**
	 * @see RttInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains round trip time information measured during the measurement on the measurement agent.")
	@JsonPropertyDescription("Contains round trip time information measured during the measurement on the measurement agent.")
	@Expose
	@SerializedName("rtt_info")
	@JsonProperty(required = true, value = "rtt_info")
	private RttInfoDto rttInfo;
	
// SpeedMeasurementRawData
	
	/**
	 * Contains a list of all captured byte transfers during the download speed measurement on the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains a list of all captured byte transfers during the download speed measurement on the measurement agent.")
	@JsonPropertyDescription("Contains a list of all captured byte transfers during the download speed measurement on the measurement agent.")
	@Expose
	@SerializedName("download_raw_data")
	@JsonProperty(required = true, value = "download_raw_data")
	private List<SpeedMeasurementRawDataItemDto> downloadRawData;
	
	/**
	 * Contains a list of all captured byte transfers during the upload speed measurement on the measurement agent.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains a list of all captured byte transfers during the upload speed measurement on the measurement agent.")
	@JsonPropertyDescription("Contains a list of all captured byte transfers during the upload speed measurement on the measurement agent.")
	@Expose
	@SerializedName("upload_raw_data")
	@JsonProperty(required = true, value = "upload_raw_data")
	private List<SpeedMeasurementRawDataItemDto> uploadRawData;
	
// ConnectionInfo
	
	/**
	 * @see ConnectionInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains information about the connection(s) used for the speed measurement.")
	@JsonPropertyDescription("Contains information about the connection(s) used for the speed measurement.")
	@Expose
	@SerializedName("connection_info")
	@JsonProperty(required = true, value = "connection_info")
	private ConnectionInfoDto connectionInfo;
	
	// We don't need to submit the following values because they are either generated by the server or 
	// already submitted in the initiation request or via ApiRequestInfo:
	// - throughputAvgDownloadBps
	// - throughputAvgDownloadLog
	// - throughputAvgUploadBps
	// - throughputAvgUploadLog

	public Long getBytesDownload() {
		return bytesDownload;
	}

	public void setBytesDownload(Long bytesDownload) {
		this.bytesDownload = bytesDownload;
	}

	public Long getBytesDownloadIncludingSlowStart() {
		return bytesDownloadIncludingSlowStart;
	}

	public void setBytesDownloadIncludingSlowStart(Long bytesDownloadIncludingSlowStart) {
		this.bytesDownloadIncludingSlowStart = bytesDownloadIncludingSlowStart;
	}

	public Long getBytesUpload() {
		return bytesUpload;
	}

	public void setBytesUpload(Long bytesUpload) {
		this.bytesUpload = bytesUpload;
	}

	public Long getBytesUploadIncludingSlowStart() {
		return bytesUploadIncludingSlowStart;
	}

	public void setBytesUploadIncludingSlowStart(Long bytesUploadIncludingSlowStart) {
		this.bytesUploadIncludingSlowStart = bytesUploadIncludingSlowStart;
	}

	public Long getDurationRttNs() {
		return durationRttNs;
	}

	public void setDurationRttNs(Long durationRttNs) {
		this.durationRttNs = durationRttNs;
	}

	public Long getDurationDownloadNs() {
		return durationDownloadNs;
	}

	public void setDurationDownloadNs(Long durationDownloadNs) {
		this.durationDownloadNs = durationDownloadNs;
	}

	public Long getDurationUploadNs() {
		return durationUploadNs;
	}

	public void setDurationUploadNs(Long durationUploadNs) {
		this.durationUploadNs = durationUploadNs;
	}

	public Long getRelativeStartTimeRttNs() {
		return relativeStartTimeRttNs;
	}

	public void setRelativeStartTimeRttNs(Long relativeStartTimeRttNs) {
		this.relativeStartTimeRttNs = relativeStartTimeRttNs;
	}

	public Long getRelativeStartTimeDownloadNs() {
		return relativeStartTimeDownloadNs;
	}

	public void setRelativeStartTimeDownloadNs(Long relativeStartTimeDownloadNs) {
		this.relativeStartTimeDownloadNs = relativeStartTimeDownloadNs;
	}

	public Long getRelativeStartTimeUploadNs() {
		return relativeStartTimeUploadNs;
	}

	public void setRelativeStartTimeUploadNs(Long relativeStartTimeUploadNs) {
		this.relativeStartTimeUploadNs = relativeStartTimeUploadNs;
	}

	public RttInfoDto getRttInfo() {
		return rttInfo;
	}

	public void setRttInfo(RttInfoDto rttInfo) {
		this.rttInfo = rttInfo;
	}

	public List<SpeedMeasurementRawDataItemDto> getDownloadRawData() {
		return downloadRawData;
	}

	public void setDownloadRawData(List<SpeedMeasurementRawDataItemDto> downloadRawData) {
		this.downloadRawData = downloadRawData;
	}

	public List<SpeedMeasurementRawDataItemDto> getUploadRawData() {
		return uploadRawData;
	}

	public void setUploadRawData(List<SpeedMeasurementRawDataItemDto> uploadRawData) {
		this.uploadRawData = uploadRawData;
	}

	public ConnectionInfoDto getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(ConnectionInfoDto connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	@Override
	public String toString() {
		return "SpeedMeasurementResult{" +
				"bytesDownload=" + bytesDownload +
				", bytesDownloadIncludingSlowStart=" + bytesDownloadIncludingSlowStart +
				", bytesUpload=" + bytesUpload +
				", bytesUploadIncludingSlowStart=" + bytesUploadIncludingSlowStart +
				", durationRttNs=" + durationRttNs +
				", durationDownloadNs=" + durationDownloadNs +
				", durationUploadNs=" + durationUploadNs +
				", relativeStartTimeRttNs=" + relativeStartTimeRttNs +
				", relativeStartTimeDownloadNs=" + relativeStartTimeDownloadNs +
				", relativeStartTimeUploadNs=" + relativeStartTimeUploadNs +
				", rttInfo=" + rttInfo +
				", downloadRawData=" + downloadRawData +
				", uploadRawData=" + uploadRawData +
				", connectionInfo=" + connectionInfo +
				'}';
	}
}
