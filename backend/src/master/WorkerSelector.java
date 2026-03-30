package master;

import shared.WorkerInfo;
import java.util.List;

/**
 * WorkerSelector — implements the routing function
 *
 *   nodeIndex = H(GameName) mod numberOfWorkers
 *
 * H() uses Java's standard String.hashCode(), with the absolute value taken
 * so that negative hash codes still map to a valid index.
 */
public class WorkerSelector {

    private final List<WorkerInfo> workers;

    public WorkerSelector(List<WorkerInfo> workers) {
        this.workers = workers;
    }

    /**
     * Return the WorkerInfo that should own {@code gameName}.
     */
    public WorkerInfo select(String gameName) {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalStateException("No workers registered.");
        }
        int index = hash(gameName) % workers.size();
        return workers.get(index);
    }

    /**
     * Return only the zero-based index (useful for logging / debugging).
     */
    public int selectIndex(String gameName) {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalStateException("No workers registered.");
        }
        return hash(gameName) % workers.size();
    }

    // ── internal hash ─────────────────────────────────────────────────────────

    /**
     * H(GameName) — non-negative integer derived from the game name.
     * Uses Java's built-in hashCode() with Math.abs so the modulo is always valid.
     * Integer.MIN_VALUE edge-case is handled explicitly.
     */
    static int hash(String gameName) {
        int h = gameName.hashCode();
        // Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE (still negative) — guard it
        return (h == Integer.MIN_VALUE) ? 0 : Math.abs(h);
    }
}