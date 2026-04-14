package shared;

import java.io.Serializable;

public class ReduceStatsRequest implements Serializable {
    private String jobId;
    private int expectedWorkers;

    public ReduceStatsRequest() {}

    public ReduceStatsRequest(String jobId, int expectedWorkers) {
        this.jobId = jobId;
        this.expectedWorkers = expectedWorkers;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getExpectedWorkers() {
        return expectedWorkers;
    }

    public void setExpectedWorkers(int expectedWorkers) {
        this.expectedWorkers = expectedWorkers;
    }
}
