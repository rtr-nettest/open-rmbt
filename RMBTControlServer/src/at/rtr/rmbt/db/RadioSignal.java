/*******************************************************************************
 * Copyright 2017 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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

package at.rtr.rmbt.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.UUID;

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
    private Integer networkTypeId;
    private UUID cellUuid;
    private UUID openTestUuid;

    private Long timeNs;
    private Long timeNsLast;
    private Date time;


    public Integer getSignal() {
        //some devices return invalid values (#913)
        if (signal != null &&
                (signal >= 0 || signal < -140)) {
            return null;
        }
        return signal;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }

    public Integer getTimingAdvance() {
        //https://developer.android.com/reference/android/telephony/CellSignalStrengthLte.html#getTimingAdvance()
        if (timingAdvance == null ||
            timingAdvance < 0 ||
            timingAdvance > 1282) {
            return null;
        }

        return timingAdvance;
    }

    public void setTimingAdvance(Integer timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public Integer getLteRsrp() {
        //some devices return invalid values (#913)
        if (lteRsrp != null &&
                (lteRsrp >= 0 || lteRsrp < -140 || (lteRsrq != null && lteRsrq == -1))) {
            return null;
        }
        return lteRsrp;
    }

    public void setLteRsrp(Integer lteRsrp) {
        this.lteRsrp = lteRsrp;
    }

    public Integer getLteRsrq() {
        //some devices return invalid values (#913)
        if (lteRsrq == null) {
            return null;
        }

        // fix invalid rsrq values (see #913)
        if (Math.abs(lteRsrq) > 19.5 || Math.abs(lteRsrq) < 3.0) {
            return null;
        }
        // fix invalid rsrq values for some devices (see #996)
        if (lteRsrq > 0) {
            return -lteRsrq;
        }
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

    public Integer getNetworkTypeId() {
        return networkTypeId;
    }

    public void setNetworkTypeId(Integer networkTypeId) {
        this.networkTypeId = networkTypeId;
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
