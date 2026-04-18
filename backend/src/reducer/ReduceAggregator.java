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

    long startTime = System.currentTimeMillis();
    long timeoutLimit = 10000; // 10 δευτερόλεπτα αναμονή το μέγιστο

    // 1. Μηχανισμός Αναμονής με Timeout
    while (searchPartials.getOrDefault(jobId, Collections.emptyList()).size() < expectedWorkers) {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= timeoutLimit) {
            System.err.println("[Reducer] Timeout reached for job: " + jobId + 
                               ". Proceeding with " + searchPartials.get(jobId).size() + 
                               "/" + expectedWorkers + " results.");
            break; // Βγαίνουμε από το loop ακόμα και αν λείπουν workers
        }
        wait(2000); // Περιμένουμε 2 δευτερόλεπτα πριν τον επόμενο έλεγχο
    }


    // Παίρνουμε τη λίστα με τις υπο-λίστες που έστειλαν οι Workers
    List<List<GameSummary>> allPartials = searchPartials.remove(jobId); 
    List<GameSummary> mergedList = new ArrayList<>();

    if (allPartials != null) {
        // Εδώ γίνεται το Merge: Ενώνουμε όλα τα κομμάτια σε μία λίστα
        for (List<GameSummary> partial : allPartials) {
            mergedList.addAll(partial); 
        }
    }

    // 3. Τελική Επεξεργασία (Sorting)
    // Ταξινομούμε τη συγχωνευμένη λίστα βάσει αστεριών (Descending)
    mergedList.sort(Comparator.comparingDouble(GameSummary::getStars).reversed());

    return mergedList;
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

        List<Map.Entry<String, Double>> orderedEntries = new ArrayList<>(merged.entrySet());
        orderedEntries.sort(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER));

        Map<String, Double> ordered = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : orderedEntries) {
            ordered.put(entry.getKey(), entry.getValue());
        }

        double total = ordered.values().stream().mapToDouble(Double::doubleValue).sum();
        ordered.put("Total", total);

        return ordered;
    }
}
