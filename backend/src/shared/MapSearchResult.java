package shared;

import java.io.Serializable;
import java.util.List;


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