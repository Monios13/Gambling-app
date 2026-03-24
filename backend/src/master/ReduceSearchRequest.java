package master;

import shared.SearchRequest;
import java.io.Serializable;

/**
 * ReduceSearchRequest — sent from the Master to the Reducer after all workers
 * have pushed their intermediate Map results.
 *
 * The Reducer uses {@code expectedWorkers} to know how many partial results
 * to wait for before it starts the Reduce phase and replies.
 */
public class ReduceSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SearchRequest originalRequest;
    private final int           expectedWorkers;

    public ReduceSearchRequest(SearchRequest originalRequest, int expectedWorkers) {
        this.originalRequest = originalRequest;
        this.expectedWorkers = expectedWorkers;
    }

    public SearchRequest getOriginalRequest() { return originalRequest; }
    public int           getExpectedWorkers() { return expectedWorkers; }

    @Override
    public String toString() {
        return "ReduceSearchRequest{workers=" + expectedWorkers + ", filters=" + originalRequest + "}";
    }
}