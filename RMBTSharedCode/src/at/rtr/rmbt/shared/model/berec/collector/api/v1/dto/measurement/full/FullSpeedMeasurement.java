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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.full;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.ConnectionInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.FullRttInfoDto;
import at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared.SpeedMeasurementRawDataItemDto;

/**
 * This DTO class contains all speed measurement information that is sent to the measurement agent.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "This DTO class contains all speed measurement information that is sent to the measurement agent.")
@JsonClassDescription("This DTO class contains all speed measurement information that is sent to the measurement agent.")
public class FullSpeedMeasurement extends FullSubMeasurement {

	/**
	 * The calculated (average) download throughput in bits per second.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The calculated (average) download throughput in bits per second.")
	@JsonPropertyDescription("The calculated (average) download throughput in bits per second.")
	@Expose
	@SerializedName("throughput_avg_download_bps")
	@JsonProperty(required = true, value = "throughput_avg_download_bps")
	private Long throughputAvgDownloadBps;
	
	/**
	 * Common logarithm of the (average) download throughput.
	 */
	@io.swagger.annotations.ApiModelProperty("Common logarithm of the (average) download throughput.")
	@JsonPropertyDescription("Common logarithm of the (average) download throughput.")
	@Expose
	@SerializedName("throughput_avg_download_log")
	@JsonProperty("throughput_avg_download_log")
	private Double throughputAvgDownloadLog;

	/**
	 * The calculated (average) upload throughput in bits per second.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The calculated (average) upload throughput in bits per second.")
	@JsonPropertyDescription("The calculated (average) upload throughput in bits per second.")
	@Expose
	@SerializedName("throughput_avg_upload_bps")
	@JsonProperty(required = true, value = "throughput_avg_upload_bps")
	private Long throughputAvgUploadBps;
	
	/**
	 * Common logarithm of the average upload throughput.
	 */
	@io.swagger.annotations.ApiModelProperty("Common logarithm of the average upload throughput.")
	@JsonPropertyDescription("Common logarithm of the average upload throughput.")
	@Expose
	@SerializedName("throughput_avg_upload_log")
	@JsonProperty("throughput_avg_upload_log")
	private Double throughputAvgUploadLog;

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
	 * The nominal measurement duration of the download measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The nominal measurement duration of the download measurement.")
	@JsonPropertyDescription("The nominal measurement duration of the download measurement.")
	@Expose
	@SerializedName("requested_duration_download_ns")
	@JsonProperty(required = true, value = "requested_duration_download_ns")
	private Long requestedDurationDownloadNs;
	
	/**
	 * The nominal measurement duration of the upload measurement.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The nominal measurement duration of the upload measurement.")
	@JsonPropertyDescription("The nominal measurement duration of the upload measurement.")
	@Expose
	@SerializedName("requested_duration_upload_ns")
	@JsonProperty(required = true, value = "requested_duration_upload_ns")
	private Long requestedDurationUploadNs;
	
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
	
	/**
	 * @see FullRttInfoDto
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains round trip time information measured during the measurement.")
	@JsonPropertyDescription("Contains round trip time information measured during the measurement.")
	@Expose
	@SerializedName("rtt_info")
	@JsonProperty(required = true, value = "rtt_info")
	private FullRttInfoDto rttInfo;
	
// SpeedMeasurementRawData

	/**
	 * Contains a list of all captured byte transfers during the download speed measurement.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains a list of all captured byte transfers during the download speed measurement.")
	@JsonPropertyDescription("Contains a list of all captured byte transfers during the download speed measurement.")
	@Expose
	@SerializedName("download_raw_data")
	@JsonProperty("download_raw_data")
	private List<SpeedMeasurementRawDataItemDto> downloadRawData;
	
	/**
	 * Contains a list of all captured byte transfers during the upload speed measurement.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains a list of all captured byte transfers during the upload speed measurement.")
	@JsonPropertyDescription("Contains a list of all captured byte transfers during the upload speed measurement.")
	@Expose
	@SerializedName("upload_raw_data")
	@JsonProperty("upload_raw_data")
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

	public Long getThroughputAvgDownloadBps() {
		return throughputAvgDownloadBps;
	}

	public void setThroughputAvgDownloadBps(Long throughputAvgDownloadBps) {
		this.throughputAvgDownloadBps = throughputAvgDownloadBps;
	}

	public Double getThroughputAvgDownloadLog() {
		return throughputAvgDownloadLog;
	}

	public void setThroughputAvgDownloadLog(Double throughputAvgDownloadLog) {
		this.throughputAvgDownloadLog = throughputAvgDownloadLog;
	}

	public Long getThroughputAvgUploadBps() {
		return throughputAvgUploadBps;
	}

	public void setThroughputAvgUploadBps(Long throughputAvgUploadBps) {
		this.throughputAvgUploadBps = throughputAvgUploadBps;
	}

	public Double getThroughputAvgUploadLog() {
		return throughputAvgUploadLog;
	}

	public void setThroughputAvgUploadLog(Double throughputAvgUploadLog) {
		this.throughputAvgUploadLog = throughputAvgUploadLog;
	}

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

	public Long getRequestedDurationDownloadNs() {
		return requestedDurationDownloadNs;
	}

	public void setRequestedDurationDownloadNs(Long requestedDurationDownloadNs) {
		this.requestedDurationDownloadNs = requestedDurationDownloadNs;
	}

	public Long getRequestedDurationUploadNs() {
		return requestedDurationUploadNs;
	}

	public void setRequestedDurationUploadNs(Long requestedDurationUploadNs) {
		this.requestedDurationUploadNs = requestedDurationUploadNs;
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

	public FullRttInfoDto getRttInfo() {
		return rttInfo;
	}

	public void setRttInfo(FullRttInfoDto rttInfo) {
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
}
