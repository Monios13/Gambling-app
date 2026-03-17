package shared;

import java.io.Serializable;

/**
 * Αποτέλεσμα πονταρίσματος που στέλνει ο Worker πίσω.
 */
public class PlayResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gameName;
    private String playerId;
    private double betAmount;
    private double profitLoss; // >0 κέρδος παίκτη, <0 ζημιά
    private boolean jackpot;

    public PlayResult(String gameName, String playerId, double betAmount, double profitLoss, boolean jackpot) {
        this.gameName = gameName;
        this.playerId = playerId;
        this.betAmount = betAmount;
        this.profitLoss = profitLoss;
        this.jackpot = jackpot;
    }

    public String getGameName() { return gameName; }
    public String getPlayerId() { return playerId; }
    public double getBetAmount() { return betAmount; }
    public double getProfitLoss() { return profitLoss; }
    public boolean isJackpot() { return jackpot; }
}

