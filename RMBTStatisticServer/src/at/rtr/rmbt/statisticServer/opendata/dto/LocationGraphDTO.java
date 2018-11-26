package at.rtr.rmbt.statisticServer.opendata.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

        public Double getBearing() {
            if (getProvider().equals("gps")) {
                return bearing;
            }
            return null;
        }

        public Double getSpeed() {
            if (getProvider().equals("gps")) {
                return speed;
            }
            return null;
        }


        @JsonProperty("long")
        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        @JsonProperty("lat")
        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        @JsonProperty("loc_accuracy")
        public Double getLocAccuracy() {
            if (locAccuracy > 0) {
                return locAccuracy;
            }
            return null;
        }

        public void setLocAccuracy(Double locAccuracy) {
            this.locAccuracy = locAccuracy;
        }

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