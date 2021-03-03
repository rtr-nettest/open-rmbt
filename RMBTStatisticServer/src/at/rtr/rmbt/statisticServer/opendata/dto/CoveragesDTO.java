package at.rtr.rmbt.statisticServer.opendata.dto;

import java.util.List;

public class CoveragesDTO {
    private List<CoverageDTO> coverages;
    private long durationMs;

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public List<CoverageDTO> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<CoverageDTO> coverages) {
        this.coverages = coverages;
    }
}
