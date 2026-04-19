package master;

import shared.SearchRequest;
import java.io.Serializable;

public class ReduceSearchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String jobId; 
    private final SearchRequest originalRequest;
    private final int expectedWorkers;

    public ReduceSearchRequest(String jobId, SearchRequest originalRequest, int expectedWorkers) {
        this.jobId = jobId;
        this.originalRequest = originalRequest;
        this.expectedWorkers = expectedWorkers;
    }

    public String getJobId() { return jobId; } // 
    public SearchRequest getOriginalRequest() { return originalRequest; }
    public int getExpectedWorkers() { return expectedWorkers; }
}