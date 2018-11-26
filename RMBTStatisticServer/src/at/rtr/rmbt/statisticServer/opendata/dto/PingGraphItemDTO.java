package at.rtr.rmbt.statisticServer.opendata.dto;

public class PingGraphItemDTO {
    private double pingMs;
    private long timeElapsed;

    public PingGraphItemDTO() {

    }

    public double getPingMs() {
        return pingMs;
    }

    public void setPingMs(double pingMs) {
        this.pingMs = pingMs;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }
}
