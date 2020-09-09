package at.rtr.rmbt.statisticServer.opendata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

public class SignalLocationDTO {

    private String openTestUuid;
    private String time;

    //signal data
    private Integer signalStrength;
    private Integer lteRsrp;
    private Integer lteRsrq;
    private String catTechnology;
    private String networkType;
    private Integer timingAdvance;
    private SignalGraphItemDTO.CellInfo2G cellInfo2G;
    private SignalGraphItemDTO.CellInfo3G cellInfo3G;
    private SignalGraphItemDTO.CellInfo4G cellInfo4G;

    //geolocation data
    private Double longitude;
    private Double latitude;

    public SignalLocationDTO(String openTestUuid, String time, Double latitude, Double longitude, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology,
    Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber, Integer timingAdvance) {
        this.time = time;
        this.openTestUuid = openTestUuid;
        this.networkType = networkType;
        this.latitude = latitude;
        this.longitude = longitude;
        locationId = (locationId == 0) ? null : locationId;
        areaCode = (areaCode == 0) ? null : areaCode;
        channelNumber = (channelNumber == 0) ? null : channelNumber;
        primaryScramblingCode = (primaryScramblingCode == 0) ? null : primaryScramblingCode;

        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.catTechnology = catTechnology;
        this.timingAdvance = timingAdvance;

        switch (catTechnology) {
            case "2G":
                cellInfo2G = new SignalGraphItemDTO.CellInfo2G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            case "3G":
                cellInfo3G = new SignalGraphItemDTO.CellInfo3G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            case "4G":
                cellInfo4G = new SignalGraphItemDTO.CellInfo4G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            default:
                break;
        }
    }

    public SignalLocationDTO(String openTestUuid, String time, Double latitude, Double longitude, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology) {
        this.time = time;
        this.openTestUuid = openTestUuid;
        this.networkType = networkType;
        this.latitude = latitude;
        this.longitude = longitude;
        signalStrength = (signalStrength == 0) ? null : signalStrength;
        lteRsrp = (lteRsrp == 0) ? null : lteRsrp;
        lteRsrq = (lteRsrq == 0) ? null : lteRsrq;
        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.catTechnology = catTechnology;
    }

    @JsonProperty("open_test_uuid")
    @ApiModelProperty(value = "Open test uuid of the measurement where this signal originates from",
    example = "O4497bd97-59c2-4586-a801-b2a48e7b3d4a")
    public String getOpenTestUuid() {
        return openTestUuid;
    }

    @JsonProperty("time")
    @ApiModelProperty(value = "timestamp of this signal",
    example = "2020-08-11 09:03:47:809")
    public String getTime() {
        return time;
    }

    @JsonProperty("long")
    @ApiModelProperty(value = "Longitude of the client position.",
            example = "14.20882799")
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @JsonProperty("lat")
    @ApiModelProperty(value = "Latitude of the client position.",
            example = "47.76077786")
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    @JsonProperty("signal_strength")
    @ApiModelProperty(value = "Signal strength (RSSI) in dBm.",
            example = "-85")
    public Integer getSignalStrength() {
        if (signalStrength != null && signalStrength == 0) {
            return null;
        }
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    @JsonProperty("lte_rsrp")
    @ApiModelProperty(value = "LTE signal strength in dBm.",
            example = "-77")
    public Integer getLteRsrp() {
        if (lteRsrp != null && lteRsrp == 0) {
            return null;
        }
        return lteRsrp;
    }

    public void setLteRsrp(Integer lteRsrp) {
        this.lteRsrp = lteRsrp;
    }

    @JsonProperty("lte_rsrq")
    @ApiModelProperty(value = "LTE signal quality in decibels.",
            example = "-6")
    public Integer getLteRsrq() {
        if (lteRsrq != null && lteRsrq == 0) {
            return null;
        }
        return lteRsrq;
    }

    public void setLteRsrq(Integer lteRsrq) {
        this.lteRsrq = lteRsrq;
    }

    @JsonProperty("cat_technology")
    @ApiModelProperty(value = "Technology category of the network, e.g. “3G”, “4G”, “5G”, “WLAN”.",
            example = "3G")
    public String getCatTechnology() {
        return catTechnology;
    }

    public void setCatTechnology(String catTechnology) {
        this.catTechnology = catTechnology;
    }


    @JsonProperty("timing_advance")
    @ApiModelProperty(value = "Timing advance value for LTE, as a value in range of 0..1282. Refer to 3GPP 36.213 Sec 4.2.3")
    public Integer getTimingAdvance() {
        return timingAdvance;
    }

    public void setTimingAdvance(Integer timingAdvance) {
        this.timingAdvance = timingAdvance;
    }


    @JsonProperty("cell_info_2G")
    @JsonUnwrapped(suffix = "_2G")
    @ApiModelProperty(value = "Additional information about the used 2G radio cell")
    public SignalGraphItemDTO.CellInfo2G getCellInfo2G() {
        return cellInfo2G;
    }

    public void setCellInfo2G(SignalGraphItemDTO.CellInfo2G cellInfo2G) {
        this.cellInfo2G = cellInfo2G;
    }

    @JsonProperty("cell_info_3G")
    @JsonUnwrapped(suffix = "_3G")
    @ApiModelProperty(value = "Additional information about the used 3G radio cell")
    public SignalGraphItemDTO.CellInfo3G getCellInfo3G() {
        return cellInfo3G;
    }

    public void setCellInfo3G(SignalGraphItemDTO.CellInfo3G cellInfo3G) {
        this.cellInfo3G = cellInfo3G;
    }

    @JsonProperty("cell_info_4G")
    @JsonUnwrapped(suffix = "_4G")
    @ApiModelProperty(value = "Additional information about the used 4G radio cell")
    public SignalGraphItemDTO.CellInfo4G getCellInfo4G() {
        return cellInfo4G;
    }

    public void setCellInfo4G(SignalGraphItemDTO.CellInfo4G cellInfo4G) {
        this.cellInfo4G = cellInfo4G;
    }

}
