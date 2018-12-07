package at.rtr.rmbt.statisticServer.opendata.dto;

import at.rtr.rmbt.util.BandCalculationUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class SignalGraphItemDTO {

    private long timeElapsed;
    private String networkType;
    private Integer signalStrength;
    private Integer lteRsrp;
    private Integer lteRsrq;
    private String catTechnology;
    private CellInfo2G cellInfo2G;
    private CellInfo3G cellInfo3G;
    private CellInfo4G cellInfo4G;


    public SignalGraphItemDTO(long timeElapsed, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology, Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
        this.timeElapsed = timeElapsed;
        this.networkType = networkType;
        locationId = (locationId == 0) ? null : locationId;
        areaCode = (areaCode == 0) ? null : areaCode;
        channelNumber = (channelNumber == 0) ? null : channelNumber;
        primaryScramblingCode = (primaryScramblingCode == 0) ? null : primaryScramblingCode;

        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.catTechnology = catTechnology;

        switch (catTechnology) {
            case "2G":
                cellInfo2G = new CellInfo2G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            case "3G":
                cellInfo3G = new CellInfo3G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            case "4G":
                cellInfo4G = new CellInfo4G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            default:
                break;
        }
    }

    public SignalGraphItemDTO(long timeElapsed, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology) {
        this.timeElapsed = timeElapsed;
        this.networkType = networkType;
        signalStrength = (signalStrength == 0) ? null : signalStrength;
        lteRsrp = (lteRsrp == 0) ? null : lteRsrp;
        lteRsrq = (lteRsrq == 0) ? null : lteRsrq;
        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.catTechnology = catTechnology;
    }

    public SignalGraphItemDTO() {

    }

    @JsonProperty("time_elapsed")
    @ApiModelProperty(value = "The time elapsed since the start of the test in milliseconds.",
            example = "55")
    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    @JsonProperty("network_type")
    @ApiModelProperty(value = "Type of the network, e.g. GSM, EDGE, UMTS, HSPA, LTE, LAN, WLAN…",
            example = "LTE")
    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
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
    @ApiModelProperty(value = "Technology category of the network, e.g. “3G”, “4G”, “WLAN”.",
            example = "3G")
    public String getCatTechnology() {
        return catTechnology;
    }

    public void setCatTechnology(String catTechnology) {
        this.catTechnology = catTechnology;
    }

    @JsonProperty("cell_info_2G")
    @ApiModelProperty(value = "Additional information about the used 2G radio cell")
    public CellInfo2G getCellInfo2G() {
        return cellInfo2G;
    }

    public void setCellInfo2G(CellInfo2G cellInfo2G) {
        this.cellInfo2G = cellInfo2G;
    }

    @JsonProperty("cell_info_3G")
    @ApiModelProperty(value = "Additional information about the used 3G radio cell")
    public CellInfo3G getCellInfo3G() {
        return cellInfo3G;
    }

    public void setCellInfo3G(CellInfo3G cellInfo3G) {
        this.cellInfo3G = cellInfo3G;
    }

    @JsonProperty("cell_info_4G")
    @ApiModelProperty(value = "Additional information about the used 4G radio cell")
    public CellInfo4G getCellInfo4G() {
        return cellInfo4G;
    }

    public void setCellInfo4G(CellInfo4G cellInfo4G) {
        this.cellInfo4G = cellInfo4G;
    }


    public abstract static class CellInfo {
        private BandCalculationUtil.FrequencyInformation fi;

        protected void setFrequencyInformation(BandCalculationUtil.FrequencyInformation fi) {
            this.setFi(fi);
        }

        @JsonProperty("frequency_dl")
        @ApiModelProperty(value = "Frequency of the downlink in MHz",
                example = "934.4")
        public Double getFrequencyDl() {
            return (getFi() == null) ? null : getFi().getFrequencyDL();
        }

        @JsonProperty("band")
        @ApiModelProperty(value = "Band",
                example = "1")
        public Integer getBand() {
            return (getFi() == null) ? null : getFi().getBand();
        }

        @JsonIgnore
        public BandCalculationUtil.FrequencyInformation getFi() {
            return fi;
        }

        public void setFi(BandCalculationUtil.FrequencyInformation fi) {
            this.fi = fi;
        }
    }

    public static class CellInfo2G extends CellInfo {
        private Integer lac;
        private Integer cid;
        private Integer bsic;
        private Integer arfcn;

        public CellInfo2G(Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setLac(locationId);
            setCid(areaCode);
            setBsic(primaryScramblingCode);
            setArfcn(channelNumber);
        }

        @JsonProperty("lac")
        @ApiModelProperty(value = "16-bit Location Area Code, 0..65535",
                example = "47170")
        public Integer getLac() {
            return lac;
        }

        @JsonProperty("cid")
        @ApiModelProperty(value = "16-bit GSM Cell Identity described in TS 27.007, 0..65535",
                example = "18804")
        public Integer getCid() {
            return cid;
        }

        @JsonProperty("bsic")
        @ApiModelProperty(value = "6-bit Base Station Identity Code",
                example = "58")
        public Integer getBsic() {
            return bsic;
        }

        @JsonProperty("arfcn")
        @ApiModelProperty(value = "16-bit GSM Absolute RF Channel Number",
                example = "1021")
        public Integer getArfcn() {
            return arfcn;
        }


        public void setLac(Integer lac) {
            this.lac = lac;
        }

        public void setCid(Integer cid) {
            this.cid = cid;
        }

        public void setBsic(Integer bsic) {
            this.bsic = bsic;
        }

        public void setArfcn(Integer arfcn) {
            if (arfcn != null) {
                setFrequencyInformation(BandCalculationUtil.getBandFromArfcn(arfcn));
            }
            this.arfcn = arfcn;
        }
    }

    public static class CellInfo3G extends CellInfo {
        private Integer lac;
        private Integer cid;
        private Integer psc;
        private Integer uarfcn;

        public CellInfo3G(Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setLac(locationId);
            setCid(areaCode);
            setPsc(primaryScramblingCode);
            setUarfcn(channelNumber);
        }


        @JsonProperty("lac")
        @ApiModelProperty(value = "16-bit Location Area Code, 0..65535",
                example = "2510")
        public Integer getLac() {
            return lac;
        }

        @JsonProperty("cid")
        @ApiModelProperty(value = "CID 28-bit UMTS Cell Identity described in TS 25.331, 0..268435455",
                example = "9908484")
        public Integer getCid() {
            return cid;
        }

        @JsonProperty("psc")
        @ApiModelProperty(value = "9-bit UMTS Primary Scrambling Code described in TS 25.331, 0..511",
                example = "27")
        public Integer getPsc() {
            return psc;
        }

        @JsonProperty("uarfcn")
        @ApiModelProperty(value = "16-bit UMTS Absolute RF Channel Number",
                example = "10687")
        public Integer getUarfcn() {
            return uarfcn;
        }


        public void setLac(Integer lac) {
            this.lac = lac;
        }

        public void setCid(Integer cid) {
            this.cid = cid;
        }

        public void setPsc(Integer psc) {
            this.psc = psc;
        }

        public void setUarfcn(Integer uarfcn) {
            if (uarfcn != null) {
                setFrequencyInformation(BandCalculationUtil.getBandFromUarfcn(uarfcn));
            }
            this.uarfcn = uarfcn;
        }
    }

    public static class CellInfo4G extends CellInfo {
        private Integer tac;
        private Integer ci;
        private Integer pci;
        private Integer earfcn;

        public CellInfo4G(Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setTac(locationId);
            setCi(areaCode);
            setPci(primaryScramblingCode);
            setEarfcn(channelNumber);
        }

        @ApiModelProperty(value = "16-bit Tracking Area Code",
                example = "6710")
        public Integer getTac() {
            return tac;
        }

        @ApiModelProperty(value = "28-bit Cell Identity",
                example = "189954")
        public Integer getCi() {
            return ci;
        }

        @ApiModelProperty(value = "Physical Cell Id 0..503",
                example = "35")
        public Integer getPci() {
            return pci;
        }

        @ApiModelProperty(value = "18-bit Absolute RF Channel Number",
                example = "2850")
        public Integer getEarfcn() {
            return earfcn;
        }


        public void setTac(Integer tac) {
            this.tac = tac;
        }

        public void setCi(Integer ci) {
            this.ci = ci;
        }

        public void setPci(Integer pci) {
            this.pci = pci;
        }

        public void setEarfcn(Integer earfcn) {
            if (earfcn != null) {
                setFrequencyInformation(BandCalculationUtil.getBandFromEarfcn(earfcn));
            }
            this.earfcn = earfcn;
        }
    }
}
