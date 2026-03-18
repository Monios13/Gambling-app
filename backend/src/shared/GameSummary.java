package shared;


//  gia na min epistrefoume olo to game kyrios UI
import java.io.Serializable;

public class GameSummary implements Serializable {
    private String gameName;
    private String providerName;
    private double stars;
    private String gameLogo;
    private double minBet;
    private double maxBet;
    private RiskLevel riskLevel;
    private BetCategory betCategory;
    private boolean jackpot;

    public GameSummary() {}

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