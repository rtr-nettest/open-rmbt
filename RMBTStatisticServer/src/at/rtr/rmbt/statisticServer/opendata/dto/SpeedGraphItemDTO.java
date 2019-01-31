package at.rtr.rmbt.statisticServer.opendata.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class SpeedGraphItemDTO {
    protected long timeElapsedNs;
    protected double bytesTotal;


    @JsonProperty("time_elapsed")
    @ApiModelProperty(value = "The time elapsed since the start of the test phase in milliseconds.",
            example = "55")
    public long getTimeElapsed() {
        return Math.round(timeElapsedNs / 1e6);
    }

    public void setTimeElapsed(long timeElapsedNs) {
        this.timeElapsedNs = timeElapsedNs;
    }

    @JsonProperty("bytes_total")
    @ApiModelProperty(value = "The sum of all bytes transferred since the start of the test phase.",
            example = "4096")
    public double getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(double bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public static class SpeedItemThreadwise extends SpeedGraphItemDTO {
        @JsonProperty("time_elapsed_ns")
        @ApiModelProperty(value = "The time elapsed since the start of the test phase for this thread, in nanoseconds.",
                example = "41183662")
        public long getTimeElapsed() {
            return timeElapsedNs;
        }
    }
}
