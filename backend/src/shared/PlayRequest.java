package shared;

import java.io.Serializable;

public class PlayRequest implements Serializable {
    private String playerId;
    private String gameName;
    private double amount;

    public PlayRequest() {}

    public PlayRequest(String playerId, String gameName, double amount) {
        this.playerId = playerId;
        this.gameName = gameName;
        this.amount = amount;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
