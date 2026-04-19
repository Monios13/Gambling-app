package master;

import shared.WorkerInfo;
import java.util.List;


public class WorkerSelector {

    private final List<WorkerInfo> workers;

    public WorkerSelector(List<WorkerInfo> workers) {
        this.workers = workers;
    }

    
    public WorkerInfo select(String gameName) {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalStateException("No workers registered.");
        }
        int index = hash(gameName) % workers.size();
        return workers.get(index);
    }

    
    public int selectIndex(String gameName) {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalStateException("No workers registered.");
        }
        return hash(gameName) % workers.size();
    }

    // ── internal hash

   
    static int hash(String gameName) {
        int h = gameName.hashCode();
        return (h == Integer.MIN_VALUE) ? 0 : Math.abs(h);
    }
}