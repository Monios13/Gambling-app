package shared;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    // Στοιχεία από το JSON [cite: 44-52]
    private String gameName;
    private String providerName;
    private int stars;
    private int noOfVotes;
    private String gameLogo; 
    private double minBet;
    private double maxBet;
    private String riskLevel;
    private String hashKey; // Το secret S για τον SRG [cite: 82]

    // Στοιχεία που υπολογίζονται αυτόματα 
    private String betCategory; 
    private int jackpot;

    public Game(String gameName, String providerName, int stars, int noOfVotes, 
                String gameLogo, double minBet, double maxBet, String riskLevel, String hashKey) {
        this.gameName = gameName;
        this.providerName = providerName;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.gameLogo = gameLogo;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.riskLevel = riskLevel;
        this.hashKey = hashKey;
        
        // Καλούμε τις μεθόδους για τον αυτόματο ορισμό πεδίων
        calculateBetCategory();
        calculateJackpot();
    }

    // Λογική κατηγορίας πονταρίσματος [cite: 56-58]
    private void calculateBetCategory() {
        if (this.minBet >= 5.0) this.betCategory = "$$$";
        else if (this.minBet >= 1.0) this.betCategory = "$$";
        else this.betCategory = "$";
    }

    // Λογική Jackpot βάσει πινάκων ρίσκου [cite: 76, 77]
    private void calculateJackpot() {
        if (riskLevel.equalsIgnoreCase("low")) this.jackpot = 10;
        else if (riskLevel.equalsIgnoreCase("medium")) this.jackpot = 20;
        else if (riskLevel.equalsIgnoreCase("high")) this.jackpot = 40;
    }

    // Getters (χρειάζονται για να διαβάζουν οι Workers τα δεδομένα)
    public String getGameName() { return gameName; }
    public String getRiskLevel() { return riskLevel; }
    public String getHashKey() { return hashKey; }
    public int getJackpot() { return jackpot; }
    public double getMinBet() { return minBet; }
    public double getMaxBet() { return maxBet; }
}