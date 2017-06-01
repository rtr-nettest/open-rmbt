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
package at.alladin.rmbt.android.util;

import android.net.wifi.WifiInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CellInformationWrapper {
    public enum Technology {
        CONNECTION_2G("2G"),
        CONNECTION_3G("3G"),
        CONNECTION_4G("4G"),
        CONNECTION_WLAN("WLAN");

        private String val;

        Technology(String val) {
            this.val = val;
        }

        @JsonValue
        public String toString() {
            return val;
        }
    };

    private Technology technology;
    private CellIdentity ci;
    private CellSignalStrength cs;
    private Boolean registered;
    private long timeStamp;
    private Long startTimestampNs;
    private long time = System.currentTimeMillis();


    public CellInformationWrapper(CellInfo cellInfo) {
        setRegistered(cellInfo.isRegistered());
        this.setTimeStamp(cellInfo.getTimeStamp());

        //adjust timestamp for the offset
        time -= cellInfo.getTimeStamp() / 1e6;

        if (cellInfo.getClass().equals(CellInfoLte.class)) {
            setTechnology(Technology.CONNECTION_4G);
            this.ci = new CellIdentity(((CellInfoLte) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoLte) cellInfo).getCellSignalStrength());
        }
        else if (cellInfo.getClass().equals(CellInfoWcdma.class)) {
            setTechnology(Technology.CONNECTION_3G);
            this.ci = new CellIdentity(((CellInfoWcdma) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoWcdma) cellInfo).getCellSignalStrength());
        }
        else if (cellInfo.getClass().equals(CellInfoGsm.class)) {
            setTechnology(Technology.CONNECTION_2G);
            this.ci = new CellIdentity(((CellInfoGsm) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoGsm) cellInfo).getCellSignalStrength());
        }
    }

    public CellInformationWrapper(WifiInfo wifiInfo) {
        setTechnology(Technology.CONNECTION_WLAN);
        this.setTimeStamp(System.nanoTime());
        setRegistered(true);

        this.ci = new CellIdentity(wifiInfo);
        this.cs = new CellSignalStrength(wifiInfo);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class CellSignalStrength {
        private Integer signal;
        private Integer timingAdvance;
        private Integer rsrp;
        private Integer rsrq;
        private Integer rssnr;
        private Integer cqi;
        private Integer bitErrorRate;
        private Integer linkSpeed;

        private String cellUuid; //reference to a specific cell

        public CellSignalStrength(CellSignalStrengthLte ss) {
            String desc = ss.toString();
            setRsrp(getSignalStrengthValueFromDescriptionString(desc,"rsrp"));
            setRsrq(getSignalStrengthValueFromDescriptionString(desc,"rsrq"));
            setRssnr(getSignalStrengthValueFromDescriptionString(desc,"rssnr"));
            setCqi(getSignalStrengthValueFromDescriptionString(desc,"cqi"));
            setTimingAdvance(ss.getTimingAdvance());
            //setSignal(ss.getDbm());
        }

        public CellSignalStrength(CellSignalStrengthWcdma ss) {
            setSignal(ss.getDbm());
            String desc = ss.toString();
            setSignal(ss.getDbm());
            setBitErrorRate(getSignalStrengthValueFromDescriptionString(desc, "ber"));
        }

        public CellSignalStrength(CellSignalStrengthGsm ss) {
            setSignal(ss.getDbm());
            String desc = ss.toString();
            setSignal(ss.getDbm());
            setBitErrorRate(getSignalStrengthValueFromDescriptionString(desc, "ber"));
            setTimingAdvance(getSignalStrengthValueFromDescriptionString(desc, "mTa"));
        }

        public CellSignalStrength(WifiInfo wifiInfo) {
            setSignal(wifiInfo.getRssi());
            setLinkSpeed(wifiInfo.getLinkSpeed());
        }

        private Integer getSignalStrengthValueFromDescriptionString(String description, String field) {
            int index = description.indexOf(field + "=");
            if (index >= 0) {
                description = description.substring(index + field.length() + 1);
                int ret = Integer.parseInt(description.split(" ")[0]);
                return maxIntToUnknown(ret);
            }
            else {
                return null;
            }
        }

        private Integer maxIntToUnknown(int value) {
            if (objectsEquals(value, Integer.MAX_VALUE)) {
                return null;
            }
            return value;
        }

        public Integer getSignal() {
            return signal;
        }

        public void setSignal(Integer signal) {
            this.signal = signal;
        }

        @JsonProperty("timing_advance")
        public Integer getTimingAdvance() {
            if (objectsEquals(timingAdvance, Integer.MAX_VALUE)) {
                return null;
            }
            return timingAdvance;
        }

        public void setTimingAdvance(Integer timingAdvance) {
            this.timingAdvance = timingAdvance;
        }

        @JsonProperty("lte_rsrp")
        public Integer getRsrp() {
            return rsrp;
        }

        public void setRsrp(Integer rsrp) {
            this.rsrp = rsrp;
        }

        @JsonProperty("lte_rsrq")
        public Integer getRsrq() {
            return rsrq;
        }

        public void setRsrq(Integer rsrq) {
            this.rsrq = rsrq;
        }

        @JsonProperty("lte_rssnr")
        public Integer getRssnr() {
            return rssnr;
        }

        public void setRssnr(Integer rssnr) {
            this.rssnr = rssnr;
        }

        @JsonProperty("lte_cqi")
        public Integer getCqi() {
            return cqi;
        }

        public void setCqi(Integer cqi) {
            this.cqi = cqi;
        }

        @JsonProperty("cell_uuid")
        public String getCellUuid() {
            if (this.cellUuid != null) {
                return this.cellUuid;
            }
            else {
                return getCi().getCellUuid();
            }
        }

        public void setCellUuid(String cellUuid) {
            this.cellUuid = cellUuid;
        }

        @JsonProperty("gsm_bit_error_rate")
        public Integer getBitErrorRate() {
            if (objectsEquals(bitErrorRate,99)) {
                return null;
            }
            return bitErrorRate;
        }

        public void setBitErrorRate(Integer bitErrorRate) {
            this.bitErrorRate = bitErrorRate;
        }

        @JsonProperty("wifi_link_speed")
        public Integer getLinkSpeed() {
            return linkSpeed;
        }

        public void setLinkSpeed(Integer linkSpeed) {
            this.linkSpeed = linkSpeed;
        }

        @JsonProperty("time_ns")
        public Long getTimeStampNs() {
            return CellInformationWrapper.this.getTimeStampNs();
        }

        @JsonProperty("time")
        public Long getTime() {
            return CellInformationWrapper.this.getTime();
        }

        //@JsonProperty("wifi_rssi")
        //@TODO: Wifi_rssi

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return super.equals(obj);
            }

            CellSignalStrength ss = (CellSignalStrength) obj;

            boolean equals = true;
            equals &= objectsEquals(this.getSignal(), ss.getSignal());
            equals &= objectsEquals(this.getBitErrorRate(), ss.getBitErrorRate());
            equals &= objectsEquals(this.getCqi(), ss.getCqi());
            equals &= objectsEquals(this.getLinkSpeed(), ss.getLinkSpeed());
            equals &= objectsEquals(this.getRsrp(), ss.getRsrp());
            equals &= objectsEquals(this.getRsrq(), ss.getRsrq());
            equals &= objectsEquals(this.getRssnr(), ss.getRssnr());
            equals &= objectsEquals(this.getTimingAdvance(), ss.getTimingAdvance());

            return equals;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("bitErrorRate=");
            sb.append(getBitErrorRate());
            sb.append(" cqpi=");
            sb.append(getCqi());
            sb.append(" linkSpeed=");
            sb.append(getLinkSpeed());
            sb.append(" rsrp=");
            sb.append(getRsrp());
            sb.append(" rsrq=");
            sb.append(getRsrq());
            sb.append(" rssnr=");
            sb.append(getRssnr());
            sb.append(" signal=");
            sb.append(getSignal());
            sb.append(" timingA=");
            sb.append(getTimingAdvance());
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class CellIdentity {
        private Integer channelNumber;
        private Integer mnc;
        private Integer mcc;
        private Integer locationId;
        private Integer areaCode;
        private Integer scramblingCode;
        private String cellUuid = UUID.randomUUID().toString();


        public CellIdentity(CellIdentityGsm cellIdentity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setChannelNumber(cellIdentity.getArfcn());
            }
            this.setMnc(cellIdentity.getMnc());
            this.setMcc(cellIdentity.getMcc());
            this.setLocationId(cellIdentity.getLac());

            /* CID Either 16-bit GSM Cell Identity described in
            * TS 27.007, 0..65535, Integer.MAX_VALUE if unknown */
            this.setAreaCode(cellIdentity.getCid());

            /* 6-bit Base Station Identity Code, Integer.MAX_VALUE if unknown */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setScramblingCode(cellIdentity.getBsic());
            }
        }

        public CellIdentity(CellIdentityWcdma cellIdentity) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setChannelNumber(cellIdentity.getUarfcn());
            }
            this.setMnc(cellIdentity.getMnc());
            this.setMcc(cellIdentity.getMcc());
            this.setLocationId(cellIdentity.getLac());
            this.setAreaCode(cellIdentity.getCid());
            this.setScramblingCode(cellIdentity.getPsc());
        }

        public CellIdentity(CellIdentityLte cellIdentity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setChannelNumber(cellIdentity.getEarfcn());
            }
            this.setMnc(cellIdentity.getMnc());
            this.setMcc(cellIdentity.getMcc());
            this.setLocationId(cellIdentity.getTac());
            this.setAreaCode(cellIdentity.getCi());
            this.setScramblingCode(cellIdentity.getPci());
        }

        public CellIdentity(WifiInfo wifiInfo) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setChannelNumber(wifiInfo.getFrequency());
            }
        }

        public Integer getChannelNumber() {
            if (objectsEquals(channelNumber,Integer.MAX_VALUE)) {
                return null;
            }
            return channelNumber;
        }

        public void setChannelNumber(int channelNumber) {
            this.channelNumber = channelNumber;
        }

        @JsonProperty("cell_mnc")
        public Integer getMnc() {
            if (mnc == null ||
                    objectsEquals(mnc, Integer.MAX_VALUE) ||
                    mnc <= 0 ||
                    mnc > 999) {
                return null;
            }
            return mnc;
        }

        public void setMnc(int mnc) {
            this.mnc = mnc;
        }

        @JsonProperty("cell_mcc")
        public Integer getMcc() {
            if (mcc == null ||
                    objectsEquals(mcc, Integer.MAX_VALUE) ||
                    mcc < 0 ||
                    mcc > 999) {
                return null;
            }
            return mcc;
        }

        public void setMcc(int mcc) {
            this.mcc = mcc;
        }

        /**
         *
         * 16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
         * 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown
         *
         * @return
         */
        @JsonProperty("location_id")
        public Integer getLocationId() {
            if (objectsEquals(locationId, Integer.MAX_VALUE)) {
                return null;
            }
            return locationId;
        }

        public void setLocationId(int locationId) {
            this.locationId = locationId;
        }

        @JsonProperty("area_code")
        public Integer getAreaCode() {
            if (objectsEquals(areaCode, Integer.MAX_VALUE) ||
                    objectsEquals(areaCode, -1)) {
                return null;
            }
            return areaCode;
        }

        public void setAreaCode(int areaCode) {
            this.areaCode = areaCode;
        }

        @JsonProperty("primary_scrambling_code")
        public Integer getScramblingCode() {
            if (objectsEquals(scramblingCode, Integer.MAX_VALUE)) {
                return null;
            }
            return scramblingCode;
        }

        public void setScramblingCode(Integer scramblingCode) {
            this.scramblingCode = scramblingCode;
        }

        @JsonProperty("uuid")
        public String getCellUuid() {
            return this.cellUuid;
        }

        @JsonProperty("technology")
        public Technology getCITechnology() {
            return getTechnology();
        }

        @JsonProperty("registered")
        public Boolean isRegistered() {
            return CellInformationWrapper.this.isRegistered();
        }

        @JsonIgnore
        public boolean isEmpty() {
            return this.getAreaCode() == null &&
                    this.getChannelNumber() == null &&
                    this.getCITechnology() == null &&
                    this.getLocationId() == null &&
                    this.getMcc() == null &&
                    this.getMnc() == null &&
                    this.getScramblingCode() == null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return super.equals(obj);
            }

            CellIdentity ci = (CellIdentity) obj;

            boolean equals = true;
            equals &= objectsEquals(this.getAreaCode(), ci.getAreaCode());
            equals &= objectsEquals(this.getChannelNumber(), ci.getChannelNumber());
            equals &= objectsEquals(this.getLocationId(), ci.getLocationId());
            equals &= objectsEquals(this.getMcc(), ci.getMcc());
            equals &= objectsEquals(this.getMnc(), ci.getMnc());
            equals &= objectsEquals(this.getScramblingCode(), ci.getScramblingCode());
            equals &= objectsEquals(getTechnology(),(ci.getCITechnology()));

            return equals;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("AreaCode=");
            sb.append(getAreaCode());
            sb.append(" ChannelNumber=");
            sb.append(getChannelNumber());
            sb.append(" LocId=");
            sb.append(getLocationId());
            sb.append(" Mcc=");
            sb.append(getMcc());
            sb.append(" Mnc=");
            sb.append(getMnc());
            sb.append(" Scrambling=");
            sb.append(getScramblingCode());
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }


    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean equals(Object obj) {
        String a1 = obj.getClass().toString();
        String a2 = this.getClass().toString();
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return super.equals(obj);
        }

        CellInformationWrapper ci = (CellInformationWrapper) obj;

        boolean equals = true;
        equals &= this.getTimeStamp() == ci.getTimeStamp();
        equals &= this.getTechnology() == ci.getTechnology();
        equals &= ci.getCi().equals(this.getCi());
        equals &= ci.getCs().equals(this.getCs());

        return equals;
    }

    @JsonProperty("cellIdentity")
    public CellIdentity getCi() {
        return ci;
    }

    @JsonProperty("cellSignalStrength")
    public CellSignalStrength getCs() {
        return cs;
    }

    @JsonIgnore
    public Long getStartTimestampNs() {
        return startTimestampNs;
    }

    public void setStartTimestampNs(Long startTimestampNs) {
        this.startTimestampNs = startTimestampNs;
    }

    /**
     * Relative timestamp compared with the beginning of the test
     * NULL if startTimestampNs is not set
     * @return
     */
    @JsonProperty("time_ns")
    public Long getTimeStampNs() {
        if (startTimestampNs != null) {
            return this.timeStamp - startTimestampNs;
        }
        return null;
    }

    @JsonProperty("time")
    public Long getTime() {
        return this.time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTechnology());
        sb.append(" isRegistered=" + isRegistered());
        sb.append(" timeStamp=" + getTimeStamp());
        sb.append(" [cellSignal] " + getCs().toString());
        sb.append(" [cellIdentity] " + getCi().toString());
        return sb.toString();
    }


    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public static boolean objectsEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
