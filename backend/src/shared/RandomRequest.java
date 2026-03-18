package shared;

import java.io.Serializable;

public class RandomRequest implements Serializable {
    private String gameName;
    private String playerId;
    private String workerNonce;

    public RandomRequest() {}

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getWorkerNonce() {
        return workerNonce;
    }

    public void setWorkerNonce(String workerNonce) {
        this.workerNonce = workerNonce;
    }
}