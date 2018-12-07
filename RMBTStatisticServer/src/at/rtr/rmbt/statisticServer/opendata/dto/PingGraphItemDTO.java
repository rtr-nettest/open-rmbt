package at.rtr.rmbt.statisticServer.opendata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class PingGraphItemDTO {
    private double pingMs;
    private long timeElapsed;

    public PingGraphItemDTO() {

    }

    @JsonProperty("ping_ms")
    @ApiModelProperty(value = "Ping (round-trip time) in milliseconds, measured on the server side.",
            example = "8")
    public double getPingMs() {
        return pingMs;
    }

    public void setPingMs(double pingMs) {
        this.pingMs = pingMs;
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
}
