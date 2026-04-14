package shared;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsResponse implements Serializable {
    private String title;
    private Map<String, Double> stats;

    public StatsResponse() {
        this.stats = new LinkedHashMap<>();
    }

    public StatsResponse(String title, Map<String, Double> stats) {
        this.title = title;
        this.stats = new LinkedHashMap<>(stats);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public void setStats(Map<String, Double> stats) {
        this.stats = new LinkedHashMap<>(stats);
    }
}
