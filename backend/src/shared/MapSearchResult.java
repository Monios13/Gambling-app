package shared;

import java.io.Serializable;
import java.util.List;

/**
 * MapSearchResult — the intermediate (key, value) pair produced by a Worker's
 * Map function during a SEARCH MapReduce job.
 *
 * Sent from Worker → Reducer via TCP.
 *
 * jobId is built as:  "search:" + searchRequest.hashCode()
 * Both the Worker and the Reducer must agree on this convention.
 */
public class MapSearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String          jobId;
    private final List<GameSummary> games;

    public MapSearchResult(String jobId, List<GameSummary> games) {
        this.jobId = jobId;
        this.games = games;
    }

    public String           getJobId() { return jobId; }
    public List<GameSummary> getGames() { return games; }

    @Override
    public String toString() {
        return "MapSearchResult{jobId='" + jobId + "', count=" + games.size() + "}";
    }
}