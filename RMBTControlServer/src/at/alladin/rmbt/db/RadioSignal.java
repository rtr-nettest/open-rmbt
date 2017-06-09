package at.alladin.rmbt.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Extern on 06.06.2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioSignal {
    private Integer signal;
    private Integer timingAdvance;
    private Integer lteRsrp;
    private Integer lteRsrq;
    private Integer lteRssnr;
    private Integer lteCqi;
    private Integer bitErrorRate;
    private Integer wifiLinkSpeed;
    private UUID cellUuid;
    private UUID openTestUuid;

    private Long timeNs;
    private Long timeNsLast;
    private Date time;


    public Integer getSignal() {
        return signal;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }

    public Integer getTimingAdvance() {
        return timingAdvance;
    }

    public void setTimingAdvance(Integer timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public Integer getLteRsrp() {
        return lteRsrp;
    }

    public void setLteRsrp(Integer lteRsrp) {
        this.lteRsrp = lteRsrp;
    }

    public Integer getLteRsrq() {
        return lteRsrq;
    }

    public void setLteRsrq(Integer lteRsrq) {
        this.lteRsrq = lteRsrq;
    }

    public Integer getLteRssnr() {
        return lteRssnr;
    }

    public void setLteRssnr(Integer lteRssnr) {
        this.lteRssnr = lteRssnr;
    }

    public Integer getLteCqi() {
        return lteCqi;
    }

    public void setLteCqi(Integer lteCqi) {
        this.lteCqi = lteCqi;
    }

    public Integer getBitErrorRate() {
        return bitErrorRate;
    }

    public void setBitErrorRate(Integer bitErrorRate) {
        this.bitErrorRate = bitErrorRate;
    }

    public Integer getWifiLinkSpeed() {
        return wifiLinkSpeed;
    }

    public void setWifiLinkSpeed(Integer wifiLinkSpeed) {
        this.wifiLinkSpeed  = wifiLinkSpeed;
    }

    public UUID getCellUuid() {
        return cellUuid;
    }

    public void setCellUuid(UUID cellUuid) {
        this.cellUuid = cellUuid;
    }


    public UUID getOpenTestUuid() {
        return openTestUuid;
    }

    public void setOpenTestUuid(UUID openTestUuid) {
        this.openTestUuid = openTestUuid;
    }

    public Long getTimeNs() {
        return timeNs;
    }

    public void setTimeNs(Long timeNs) {
        this.timeNs = timeNs;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bitErrorRate=");
        sb.append(getBitErrorRate());
        sb.append(" cqpi=");
        sb.append(getLteCqi());
        sb.append(" linkSpeed=");
        sb.append(getWifiLinkSpeed());
        sb.append(" rsrp=");
        sb.append(getLteRsrp());
        sb.append(" rsrq=");
        sb.append(getLteRsrq());
        sb.append(" rssnr=");
        sb.append(getLteRssnr());
        sb.append(" signal=");
        sb.append(getSignal());
        sb.append(" timingA=");
        sb.append(getTimingAdvance());
        return sb.toString();
    }

    public Long getTimeNsLast() {
        return timeNsLast;
    }

    public void setTimeNsLast(Long timeNsLast) {
        this.timeNsLast = timeNsLast;
    }
}
