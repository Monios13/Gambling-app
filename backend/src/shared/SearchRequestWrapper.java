package shared;

import java.io.Serializable;

public class SearchRequestWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String jobId;
    private final SearchRequest searchRequest;

    public SearchRequestWrapper(String jobId, SearchRequest searchRequest) {
        this.jobId = jobId;
        this.searchRequest = searchRequest;
    }

    public String getJobId() { return jobId; }
    public SearchRequest getSearchRequest() { return searchRequest; }
}