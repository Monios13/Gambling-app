package shared;

import java.io.Serializable;

public class SearchRequest implements Serializable {
    private String playerId;
    private Double minStars;
    private RiskLevel riskLevel;
    private BetCategory betCategory;

    public SearchRequest() {}

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Double getMinStars() {
        return minStars;
    }

    public void setMinStars(Double minStars) {
        this.minStars = minStars;
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
}