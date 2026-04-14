package shared;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameName;
    private String providerName;
    private double stars;
    private int noOfVotes;
    private String gameLogo;
    private double minBet;
    private double maxBet;
    private String riskLevel;
    private String hashKey;

    private String betCategory;
    private int jackpot;
    private boolean active = true;
    private double totalProfitLoss;

    public Game() {}

    public Game(String gameName, String providerName, double stars, int noOfVotes,
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
        recalculateDerivedFields();
    }

    private void calculateBetCategory() {
        if (this.minBet >= 5.0) {
            this.betCategory = "$$$";
        } else if (this.minBet >= 1.0) {
            this.betCategory = "$$";
        } else {
            this.betCategory = "$";
        }
    }

    private void calculateJackpot() {
        if (riskLevel == null) {
            this.jackpot = 0;
            return;
        }

        if (riskLevel.equalsIgnoreCase("low")) {
            this.jackpot = 10;
        } else if (riskLevel.equalsIgnoreCase("medium")) {
            this.jackpot = 20;
        } else if (riskLevel.equalsIgnoreCase("high")) {
            this.jackpot = 40;
        } else {
            this.jackpot = 0;
        }
    }

    public synchronized void recalculateDerivedFields() {
        calculateBetCategory();
        calculateJackpot();
    }

    public String getGameName() { return gameName; }
    public double getStars() { return stars; }
    public String getProviderName() { return providerName; }
    public int getNoOfVotes() { return noOfVotes; }
    public String getGameLogo() { return gameLogo; }
    public String getRiskLevel() { return riskLevel; }
    public String getHashKey() { return hashKey; }
    public int getJackpot() { return jackpot; }
    public double getMinBet() { return minBet; }
    public double getMaxBet() { return maxBet; }
    public String getBetCategory() { return betCategory; }

    public void setGameName(String gameName) { this.gameName = gameName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public void setStars(double stars) { this.stars = stars; }
    public void setNoOfVotes(int noOfVotes) { this.noOfVotes = noOfVotes; }
    public void setGameLogo(String gameLogo) { this.gameLogo = gameLogo; }
    public void setMinBet(double minBet) { this.minBet = minBet; }
    public void setMaxBet(double maxBet) { this.maxBet = maxBet; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setHashKey(String hashKey) { this.hashKey = hashKey; }

    public synchronized boolean isActive() { return active; }
    public synchronized void setActive(boolean active) { this.active = active; }

    public double getTotalProfitLoss() { return totalProfitLoss; }

    public synchronized void addToTotalProfitLoss(double delta) {
        this.totalProfitLoss += delta;
    }

    public synchronized void updateRiskLevel(String newRiskLevel) {
        this.riskLevel = newRiskLevel;
        recalculateDerivedFields();
    }

    public synchronized void applyRating(int rating) {
        double totalStars = this.stars * this.noOfVotes;
        this.noOfVotes++;
        this.stars = (totalStars + rating) / this.noOfVotes;
    }

    @Override
    public String toString() {
        return "Game: " + gameName + " [" + betCategory + "], Risk: " + riskLevel
                + ", Jackpot: " + jackpot;
    }
}
