package at.alladin.rmbt.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

/**
 * Created by Extern on 06.06.2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioCell {

    public enum Technology {
        CONNECTION_2G("2G"),
        CONNECTION_3G("3G"),
        CONNECTION_4G("4G"),
        CONNECTION_WLAN("WLAN");

        private String val;

        Technology(String val) {
            this.val = val;
        }

        @JsonCreator
        public static Technology forValue(String value) {
            switch (value) {
                case "2G":
                    return CONNECTION_2G;
                case "3G":
                    return CONNECTION_3G;
                case "4G":
                    return CONNECTION_4G;
                case "WLAN":
                    return CONNECTION_WLAN;
                default:
                    return null;
            }
        }

        @Override
        @JsonValue
        public String toString() {
            return val;
        }
    };

    private Integer mnc;
    private Integer mcc;

    private Integer locationId;
    private Integer areaCode;

    private Integer primaryScramblingCode;
    private Integer channelNumber;
    private UUID uuid;

    private Technology technology;
    private Boolean registered;
    private Boolean active;

    private UUID openTestUuid;

    public Integer getMnc() {
        return mnc;
    }

    public void setMnc(Integer mnc) {
        this.mnc = mnc;
    }

    public Integer getMcc() {
        return mcc;
    }

    public void setMcc(Integer mcc) {
        this.mcc = mcc;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getPrimaryScramblingCode() {
        return primaryScramblingCode;
    }

    public void setPrimaryScramblingCode(Integer primaryScramblingCode) {
        this.primaryScramblingCode = primaryScramblingCode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public UUID getOpenTestUuid() {
        return openTestUuid;
    }

    public void setOpenTestUuid(UUID openTestUuid) {
        this.openTestUuid = openTestUuid;
    }

    public Integer getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(Integer channelNumber) {
        this.channelNumber = channelNumber;
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
        sb.append(getPrimaryScramblingCode());
        return sb.toString();
    }
}
