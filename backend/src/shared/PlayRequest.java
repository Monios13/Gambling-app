package shared;

import java.io.Serializable;

/**
 * Αίτημα πονταρίσματος από παίκτη προς Master/Worker.
 */
public class PlayRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String playerId;
    private String gameName;
    private double betAmount;

    public PlayRequest(String playerId, String gameName, double betAmount) {
        this.playerId = playerId;
        this.gameName = gameName;
        this.betAmount = betAmount;
    }

    public String getPlayerId() { return playerId; }
    public String getGameName() { return gameName; }
    public double getBetAmount() { return betAmount; }
}

