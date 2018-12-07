package at.rtr.rmbt.statisticServer.opendata.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationGraphDTO {
    private double totalDistance;
    private List<LocationGraphItem> locations = new ArrayList<>();


    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<LocationGraphItem> getLocations() {
        return locations;
    }

    public void addLocation(LocationGraphItem location) {
        this.getLocations().add(location);
    }

    public void setLocations(List<LocationGraphItem> locations) {
        this.locations = locations;
    }

    public static class LocationGraphItem {
        private Double longitude;
        private Double latitude;
        private Double locAccuracy;
        private long timeElapsed;
        private Date time;
        private Double bearing;
        private Double speed;
        private Double altitude;
        private String provider;

        @ApiModelProperty(value = "Direction of travel of the hosting device in degrees, where 0° ≤ bearing < 360°, counting clockwise relative to the true north",
                example = "195.4")
        public Double getBearing() {
            if (getProvider().equals("gps")) {
                return bearing;
            }
            return null;
        }

        @ApiModelProperty(value = "Speed of the client device in meters per second",
                example = "22.4")
        public Double getSpeed() {
            if (getProvider().equals("gps")) {
                return speed;
            }
            return null;
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

        @JsonProperty("loc_accuracy")
        @ApiModelProperty(value = "Estimation of accuracy of client location.",
                example = "8")
        public Double getLocAccuracy() {
            if (locAccuracy > 0) {
                return locAccuracy;
            }
            return null;
        }

        public void setLocAccuracy(Double locAccuracy) {
            this.locAccuracy = locAccuracy;
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

        public Double getAltitude() {
            if (altitude == null && altitude != 0) {
                return altitude;
            }
            return null;
        }

        public void setAltitude(Double altitude) {
            this.altitude = altitude;
        }

        @JsonProperty("loc_src")
        @ApiModelProperty(value = "Source for the geo location-data. Values: “gps”, “network”.",
                example = "gps")
        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }


        @JsonIgnore
        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }
    }
}