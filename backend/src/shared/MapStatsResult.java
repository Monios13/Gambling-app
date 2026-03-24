package shared;

import java.io.Serializable;
import java.util.Map;

/**
 * MapStatsResult — the intermediate (key, value) pair produced by a Worker's
 * Map function during a GET_STATS_PROVIDER or GET_STATS_PLAYER MapReduce job.
 *
 * Sent from Worker → Reducer via TCP.
 *
 * stats: Map of  gameName → profitLoss  (for provider queries)
 *             or playerId → profitLoss  (for player queries)
 *
 * jobId convention:
 *   provider queries:  "provider:<providerName>"
 *   player   queries:  "player:<playerId>"
 */
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