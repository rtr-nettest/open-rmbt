package at.rtr.rmbt.statisticServer.opendata.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class SpeedGraphItemDTO {
    protected long timeElapsedNs;
    protected double bytesTotal;


    public long getTimeElapsed() {
        return Math.round(timeElapsedNs / 1e6);
    }

    public void setTimeElapsed(long timeElapsedNs) {
        this.timeElapsedNs = timeElapsedNs;
    }

    public double getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(double bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public static class SpeedItemThreadwise extends SpeedGraphItemDTO {
        @JsonProperty("time_elapsed_ns")
        public long getTimeElapsed() {
            return timeElapsedNs;
        }
    }
}
