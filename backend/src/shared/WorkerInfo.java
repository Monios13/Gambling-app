package shared;

import java.io.Serializable;

public class WorkerInfo implements Serializable {
    private int workerId;
    private String host;
    private int port;

    public WorkerInfo() {}

    public WorkerInfo(int workerId, String host, int port) {
        this.workerId = workerId;
        this.host = host;
        this.port = port;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}