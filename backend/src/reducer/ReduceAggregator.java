package reducer;

import shared.GameSummary;

import java.util.*;

/**
 * ReduceAggregator — thread-safe store for intermediate Map results.
 *
 * Workers push their partial results here (keyed by a unique jobId).
 * The Master then asks the Reducer to merge everything for that job.
 *
 * A "job" is identified by a string key:
 *   - For SEARCH:          "search:<requestHashCode>"
 *   - For STATS_PROVIDER:  "provider:<providerName>"
 *   - For STATS_PLAYER:    "player:<playerId>"
 *
 * The Reducer accumulates partial results until the expected number of
 * workers have pushed their data, then runs reduce() and clears the slot.
 */
public class ReduceAggregator {

    // ── Search results ────────────────────────────────────────────────────────
    // jobId → list of partial GameSummary lists (one per worker)
    private final Map<String, List<List<GameSummary>>> searchPartials = new HashMap<>();

    // ── Stats results ─────────────────────────────────────────────────────────
    // jobId → list of partial profit/loss maps  (game/player → amount)
    private final Map<String, List<Map<String, Double>>> statsPartials = new HashMap<>();

    // ── Search ─────────────────────────────────────────────────────────────────

    public synchronized void addSearchPartial(String jobId, List<GameSummary> partial) {
        searchPartials.computeIfAbsent(jobId, k -> new ArrayList<>()).add(partial);
        notifyAll();
    }

    /**
     * Block until {@code expectedWorkers} partial results have arrived,
     * then merge them all into a single sorted list and clean up.
     */
    public synchronized List<GameSummary> reduceSearch(String jobId, int expectedWorkers)
            throws InterruptedException {

        while (searchPartials.getOrDefault(jobId, Collections.emptyList()).size() < expectedWorkers) {
            wait(5000);   // 5 s timeout guard
        }

        List<List<GameSummary>> all = searchPartials.remove(jobId);
        List<GameSummary> merged = new ArrayList<>();
        if (all != null) {
            for (List<GameSummary> chunk : all) {
                merged.addAll(chunk);
            }
        }
        // Sort by stars descending so the best games appear first
        merged.sort(Comparator.comparingDouble(GameSummary::getStars).reversed());
        return merged;
    }

    // ── Stats ──────────────────────────────────────────────────────────────────

    public synchronized void addStatsPartial(String jobId, Map<String, Double> partial) {
        statsPartials.computeIfAbsent(jobId, k -> new ArrayList<>()).add(partial);
        notifyAll();
    }

    /**
     * Block until all worker partials have arrived, then sum per key.
     * Returns a map of gameName/playerId → totalProfitLoss.
     */
    public synchronized Map<String, Double> reduceStats(String jobId, int expectedWorkers)
            throws InterruptedException {

        while (statsPartials.getOrDefault(jobId, Collections.emptyList()).size() < expectedWorkers) {
            wait(5000);
        }

        List<Map<String, Double>> all = statsPartials.remove(jobId);
        Map<String, Double> merged = new LinkedHashMap<>();
        if (all != null) {
            for (Map<String, Double> chunk : all) {
                for (Map.Entry<String, Double> entry : chunk.entrySet()) {
                    merged.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }

        // Add a "Total" key
        double total = merged.values().stream().mapToDouble(Double::doubleValue).sum();
        merged.put("Total", total);

        return merged;
    }
}