package shared;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    // Στοιχεία από το JSON
    private String gameName;
    private String providerName;
    private int stars;
    private int noOfVotes;
    private String gameLogo; 
    private double minBet;
    private double maxBet;
    private String riskLevel;
    private String hashKey; // Το secret S για τον SRG

    // Στοιχεία που υπολογίζονται αυτόματα 
    private String betCategory; 
    private int jackpot;

    // Κατάσταση παιχνιδιού (αν είναι ορατό στους παίκτες)
    private boolean active = true;

    // Στατιστικά για κέρδη / ζημιές (συγκεντρωτικά στο επίπεδο Worker)
    // Τα ακριβή aggregates ανά πάροχο / παίκτη θα υπολογίζονται με MapReduce
    private double totalProfitLoss; // >0 κέρδος για το σύστημα, <0 ζημιά

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

    // Λογική κατηγορίας πονταρίσματος
    private void calculateBetCategory() {
        if (this.minBet >= 5.0) this.betCategory = "$$$";
        else if (this.minBet >= 1.0) this.betCategory = "$$";
        else this.betCategory = "$";
    }

    // Λογική Jackpot βάσει πινάκων ρίσκου
    private void calculateJackpot() {
        if (riskLevel.equalsIgnoreCase("low")) this.jackpot = 10;
        else if (riskLevel.equalsIgnoreCase("medium")) this.jackpot = 20;
        else if (riskLevel.equalsIgnoreCase("high")) this.jackpot = 40;
    }

    // Getters / setters (χρειάζονται για να διαβάζουν / τροποποιούν οι Workers & Master τα δεδομένα)
    public String getGameName() { return gameName; }
    public String getProviderName() { return providerName; }
    public int getStars() { return stars; }
    public String getProviderName() { return providerName; }
    public String getRiskLevel() { return riskLevel; }
    public String getHashKey() { return hashKey; }
    public int getJackpot() { return jackpot; }
    public double getMinBet() { return minBet; }
    public double getMaxBet() { return maxBet; }
    public String getBetCategory() { return betCategory; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getTotalProfitLoss() { return totalProfitLoss; }

    public synchronized void addToTotalProfitLoss(double delta) {
        this.totalProfitLoss += delta;
    }

    public synchronized void updateRiskLevel(String newRiskLevel) {
        this.riskLevel = newRiskLevel;
        calculateJackpot();
    }
}