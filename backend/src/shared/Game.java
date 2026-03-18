package shared;

import java.io.Serializable;

public class Game implements Serializable {
    private String gameName;
    private String providerName;
    private double stars;
    private int noOfVotes;
    private String gameLogo;
    private double minBet;
    private double maxBet;
    private RiskLevel riskLevel;
    private String hashKey;
    private String secret;

    // Derived (not necessarily from JSON)
    private BetCategory betCategory;
    private boolean jackpot;

    public Game() {}

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public double getStars() {
        return stars;
    }

    public void setStars(double stars) {
        this.stars = stars;
    }

    public int getNoOfVotes() {
        return noOfVotes;
    }

    public void setNoOfVotes(int noOfVotes) {
        this.noOfVotes = noOfVotes;
    }

    public String getGameLogo() {
        return gameLogo;
    }

    public void setGameLogo(String gameLogo) {
        this.gameLogo = gameLogo;
    }

    public double getMinBet() {
        return minBet;
    }

    public void setMinBet(double minBet) {
        this.minBet = minBet;
    }

    public double getMaxBet() {
        return maxBet;
    }

    public void setMaxBet(double maxBet) {
        this.maxBet = maxBet;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public BetCategory getBetCategory() {
        return betCategory;
    }

    public void setBetCategory(BetCategory betCategory) {
        this.betCategory = betCategory;
    }

    public boolean isJackpot() {
        return jackpot;
    }

    public void setJackpot(boolean jackpot) {
        this.jackpot = jackpot;
    }
}