package shared;

import java.io.Serializable;
import java.util.Map;

public class MapStatsResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String              jobId;
    private final Map<String, Double> stats;

    public MapStatsResult(String jobId, Map<String, Double> stats) {
        this.jobId  = jobId;
        this.stats  = stats;
    }

    public String              getJobId() { return jobId; }
    public Map<String, Double> getStats() { return stats; }

    @Override
    public String toString() {
        return "MapStatsResult{jobId='" + jobId + "', entries=" + stats.size() + "}";
    }
}